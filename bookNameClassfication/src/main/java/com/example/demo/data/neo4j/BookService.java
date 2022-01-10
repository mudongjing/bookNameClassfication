package com.example.demo.data.neo4j;

import com.example.demo.data.redis.RedisFactory;
import com.example.demo.pojo.common.RedisDataBase;
import com.example.demo.pojo.neo4j.Words;
import com.example.demo.utils.keywordOpt.KeywordExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private KeyWordRepository keyWordRepository;
    @Autowired
    private KeywordExt keywordExt;
    // 创建对应的词汇节点，并返回对应的id值
    // 之后存放到redis中
//    public void createNode(String word){
//        int id=0;
//        Words words = new Words(word);
//        Words save = bookRepository.save(words);
//        redisFactory.getRedisTemplateByDb(redisDataBase.getWORD_ID()).
//                opsForHash().put(redisDataBase.getKEY_WORD_ID(),word,save.getId());
//
//    }

//    private Long getWordIdInRedis(String word){
//        RedisTemplate<String, Object> word_idRedis = redisFactory.getRedisTemplateByDb(redisDataBase.getWORD_ID());
//        if (word_idRedis.opsForHash().hasKey(redisDataBase.getKEY_WORD_ID(),word)){
//            return (Long)word_idRedis.opsForHash().get(redisDataBase.getKEY_WORD_ID(),word);
//        }else return null;
//    }

    // 创建连接
    public boolean createRelation(String start,String end){
        if (start!= null && end != null){
            return bookRepository.createRelation(start,end);
        }
        return false;
    }
    // 获取节点的邻居
    public Collection<Words> getNeighborById(String word){
        if (word !=null){
            return  bookRepository.findNeighbour(word);
        }else return null;
    }

    // 缩小节点间距离
    public boolean reduceDistance(String start,String end,int step){
        if (start!=null && end!=null){
            Optional<Words> startId = bookRepository.findById(start);
            Optional<Words> endId = bookRepository.findById(end);
            if (startId.isPresent() && endId.isPresent() && bookRepository.findRelationById(start, end) != null){
                bookRepository.setDistanceById(start, end, step);
                return true;
            }
        }
        return false;
    }

    // 计算节点之间的相对影响权重
    // 在 leadRole角度下，costar占据的影响权重
    // 计算leadRole 所有邻居的影响权重，取costar的比值
    public double getBiasFromNode(String leadRole,String costar){
        Collection<Words> neighborOfLead = getNeighborById(leadRole);

        /**
         * 计算leadrole所有邻居的影响权重，
         * 由于是以leadRole 为中心点，所有 的邻居在计算时，都需要记录一个与leadRole的邻居交集，差集中的节点 则需要考虑是否可以通过其他的第二个节点建立连接，
         * 也就是对于中心节点的一个邻居的影响权重计算，需要大致两个步骤：
         * 1、 邻居与中心节点做邻居交集，邻居和交集的节点做计算,另外也许需要计算二者的邻居交集，计算所有路径产生的影响权重之和
         * 2、 中心节点的邻居集合与上述交集做差集，差集中的节点再一一与邻居做邻居集合的交集，差集中的节点均代表一条路径，计算所有的路径权重附加到该节点上
         */
        AtomicReference<Double> sum = new AtomicReference<>((double) 0);
        AtomicReference<Double> sum_costar = new AtomicReference<>((double) 0);


        neighborOfLead.forEach(nei ->{
            AtomicReference<Double> sum_nei = new AtomicReference<>((double)0);
            /**
             * 计算nei和所有其他nei的各种直接或间接连接 的路径 对应的影响权重
             */
            neighborOfLead.forEach(other->{
                if (!nei.getWord().equals(other.getWord())){
                    if (bookRepository.existsRelation(nei.getWord(),other.getWord())){

                    }else{

                    }
                }

            });

            sum.updateAndGet(v -> new Double((double) (v + sum_nei.get())));
            if (nei.getWord().equals(costar)){ sum_costar.set(sum_nei.get()); }
        });


        return sum_costar.get()/sum.get();
    }

    // 计算两个节点之间通过中间节点建立连接的所有路径产生的影响权重
    public double calculateMidRelation(String start,String end){

    }

    public int getDistanceByIdAndString(String id,String neighbour){
        return bookRepository.findRelationById(id,neighbour).getDistance();
    }

    // 交集
    public Collection<Words> getIntersectionFromWords(Words lead,Words costar){
        return bookRepository.getInterSection(lead.getWord(),costar.getWord());
    }
    //  差集
    public Collection<Words> getDifference(Words lead,Words costar){
        Collection<Words> leadNei = bookRepository.findNeighbour(lead.getWord());
        Collection<Words> costarNei = bookRepository.findNeighbour(costar.getWord());
        leadNei.removeAll(costarNei);
        return leadNei;

    }
}
