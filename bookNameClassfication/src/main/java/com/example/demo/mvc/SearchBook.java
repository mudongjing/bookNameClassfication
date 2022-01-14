package com.example.demo.mvc;

import com.example.demo.data.mysql.service.BookNameService;
import com.example.demo.data.neo4j.BookNeo4jService;
import com.example.demo.data.redis.RedisFactory;
import com.example.demo.pojo.common.RedisDataBase;
import com.example.demo.pojo.search.SearchResult;
import com.example.demo.utils.keywordOpt.KeywordCal;
import com.example.demo.utils.keywordOpt.KeywordExt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class SearchBook {
    /*
     * 我们目前仅试图通过给定的一个关键词搜索到所有相关的书籍，但是仅是完全包含该关键词
     * 比如，"易语言编程"和"易语言",前者按道理来说，应该是包含在后者当中，但是在实际搜索过程中，我们不会单独搜索"易语言",
     * 因为，我们的基础是最大字符子串，如果存在合法的更长的关键词，自然具有更大的优先级，
     * 例如，"领域驱动开发"和"领域",前后两者实际没有太大的关系，前者是IT行业中一个较为独特的词汇，而后者则是一个较为普遍的词
     * 由于我们采用的纯粹的统计手法，不参考额外的信息，因此没有依据能够分辨二者是基本独立，还是一方包含于一方。
     * 导致，我们的系统面向的需求是，用户明确知道搜索的关键词，不掺杂额外的信息，
     * 比如，"c语言"，我们这里直接使用"c"即可，至于"go语言"，有多个常用的名称，每次可以搜索对应的关键词，不限于"go"，"golang",
     * 我们系统的作用则是在搜索类似 "c"或"go"这类有多重含义的词汇时，
     * 可以根据已有的结果，将包含的书名进行合理的分类，使得用户能够不需要从大量的结果中筛选可能的结果，
     * 而是在已有的分类中查看，哪个分类符合我们的要求，只需要关注对应分类中的结果即可
     */

    /*
    策略是，输入一个关键词后，系统先去图数据库查找是否存在对应的关键词，如果存在，可以按照图中的邻居划分出不同的团，作为之后的分类，
    并且可以去redis中按照前两个字找到集合，逐一检查，得到所有包含该关键词的书名。再根据前面的分团，计算每个书名中对应关键词的影响权重的和，分配
    所属分类，影响权重的和的值作为排序依据。

    如果不包含该关键词，我们可以尝试找符合的最大子串，仍然是通过拆解为多个两个字的词汇，逐一检查。
    但，我们这里暂时不考虑这一情况。
     */

    /*
    划分团的操作，得到关键词之后，就可以得到所有的邻居对于该关键词的影响权重，同时需要计算邻居之间可能存在的影响权重
     */

    @Resource
    private RedisFactory redisFactory;
    @Resource
    private BookNameService bookNameService;
    @Resource
    private BookNeo4jService bookNeo4jService;
    @Resource
    private KeywordExt keywordExt;
    @Resource
    private KeywordCal keywordCal;
    @Resource
    private RedisDataBase redisDataBase;

    /*
    目前而言， 关键词应该符合规则，即不能中英文混杂
     */
    public Set<TreeSet<SearchResult>> searchKeyWord(String keyword){
        if (!bookNeo4jService.existKeyword(keyword)) return null;
        Set<TreeSet<SearchResult>> result = new HashSet<>();
        Map<Integer,TreeSet<SearchResult>> mapResult = new HashMap<>();
        Map<Integer, Set<String>> groups = bookNeo4jService.groupMapDivide(keyword);
        Map<String, Double> biasMapFromNode = bookNeo4jService.getBiasMapFromNode(keyword); // 分组中节点对于中间节点的影响权重
        Map<Character, Set<String>> characterSetMap = indexOfNeighbor(keyword);// 附带关键词的首字符索引

        Set<String> bookNameByKeyword = getBookNameByKeyword(keyword);
        bookNameByKeyword.forEach(bookName ->{
            TreeSet<SearchResult> midresult = new TreeSet<>();
            /*
            计算当前书名对应各个群组的影响权重，并分配到对应的群组中

            需要指定群组的编号等分配信息
             */
            // 当前书名包含的可用的词汇
            Set<String> strings = keywordInBookName(bookName, characterSetMap);
            TreeSet<SearchResult> bookGroup = new TreeSet<>();
            // 对包含的词汇按分组计算权重
            groups.forEach((id,group) ->{
                SearchResult searchResult = new SearchResult(id,bookName);

                AtomicReference<Double> value = new AtomicReference<>((double)0);
                Set<String> tmpSet = new HashSet<>();
                strings.forEach(s ->{
                    if (group.contains(s)){
                        tmpSet.add(s);
                        value.updateAndGet(v -> v+biasMapFromNode.get(s));
                    }
                });
                searchResult.setWeight(value.get());
            });
            SearchResult first = bookGroup.first();
            if (mapResult.containsKey(first.getGroupId())) midresult = mapResult.get(first.getGroupId());
            midresult.add(first); mapResult.put(first.getGroupId(),midresult);
        });
        mapResult.forEach((id,set)->{
            result.add(set);
        });
        return result;
    }

    private Map<Character,Set<String>> indexOfNeighbor(String keyword){
        Set<String> neighborStringById = bookNeo4jService.getNeighborStringById(keyword);
        Map<Character,Set<String>> result = new HashMap<>();
        neighborStringById.forEach(string->{
            char c = string.charAt(0);
            Set<String> set;
            if (result.containsKey(c)){
                set = result.get(c); set.add(string);
            }else{
                set = new HashSet<>(); set.add(string);
            }
            result.put(c,set);
        });
        return result;
    }

    /*
    针对指定的书名，借助得到的子串首字符索引，得到当前书名包含的对应 的子串
     */
    private Set<String> keywordInBookName(String bookName,Map<Character, Set<String>> characterSetMap){
        Set<String> result = new HashSet<>();
        final int len = bookName.length();
        for (int i=0;i<bookName.length();){
            char c = bookName.charAt(i);
            AtomicReference<Integer> nextI = new AtomicReference<>(i++);
            if (characterSetMap.containsKey(c)) {
                Set<String> strings = characterSetMap.get(c);
                final int t = i;
                strings.forEach(v ->{
                    if (v.length()<= len-t){
                        /*
                        有可能子串中包含某些元素是对方的子串，那么需要在每次对一个位置检索之后，可能存在多个符合的元素，
                        我们在得到一个符合的元素后，那么就表明这个元素覆盖的范围就不需要在考虑，但是，如果是子串之间存在互为子串，
                        就需要认为接下来的位置是最短的那个子串对应的位置
                         */
                        int tmp = 1;
                        int vLen = v.length();
                        while(bookName.charAt(t+tmp)==v.charAt(tmp)){
                            if (tmp==vLen-1){
                                if (t+tmp < nextI.get()) nextI.set(t+tmp);
                                result.add(v); break;
                            }
                            tmp++;
                        }
                    }
                });
            }
            i = nextI.get();
        }
        return result;
    }

    /*
    根据指定的关键词，查询所有包含该词汇的书名
     */
    private Set<String> getBookNameByKeyword(String keyword){
        Set<String> result = new HashSet<>();
        /*
        如果是英文，则直接到对应的redis库中搜索对应的集合
        如果是中文，则提取前面两个字符，做计算，得到对应的集合键名
         */
        Set<Object> members;
        if (keywordExt.isCh(keyword.charAt(0))){
            // 中文
            RedisTemplate<String, Object> chineseDB = redisFactory.getRedisTemplateByDb(redisDataBase.getCHINESE_DATABASE());
            String s = keywordCal.calCoordinate(keyword.substring(0, 2));
            members =  chineseDB.opsForSet().members(s);

        }else{
            // 英文
            RedisTemplate<String, Object> englishDB = redisFactory.getRedisTemplateByDb(redisDataBase.getENGLISH_DATABASE());
            members = englishDB.opsForSet().members(keyword);
        }
        if (members!=null)
            members.forEach(o->{
                String string = (String)o;
                int f = string.indexOf('?');
                int id = Integer.parseInt(string.substring(0,f));
                int location = Integer.parseInt(string.substring(f+1));
                String bookById = bookNameService.getBookById(id);
                if (hasKeywordInName(bookById,keyword,location))  result.add(bookById); // 含有关键词的书名保存
            });

        return result;
    }
    private boolean hasKeywordInName(String bookName,String keyword,int location){
        boolean result = false;
        int step = 0;
        int len = keyword.length();
        while(bookName.charAt(location+step)==keyword.charAt(step)){
            if (step==len){
                result = true;
                break;
            }
            step++;
        }

        return result;
    }
}
