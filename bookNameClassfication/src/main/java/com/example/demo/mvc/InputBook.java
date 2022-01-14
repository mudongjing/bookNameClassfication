package com.example.demo.mvc;

import com.example.demo.data.mysql.service.BookNameService;
import com.example.demo.data.neo4j.BookRepository;
import com.example.demo.data.redis.RedisFactory;
import com.example.demo.pojo.common.LocationOfWord;
import com.example.demo.pojo.common.RedisDataBase;
import com.example.demo.pojo.neo4j.KeyWord;
import com.example.demo.pojo.neo4j.Words;
import com.example.demo.utils.keywordOpt.KeywordCal;
import com.example.demo.utils.keywordOpt.KeywordExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class InputBook {
    @Resource
    private BookNameService bookNameService;
    @Resource
    private KeywordCal keywordCal;
    @Resource
    private RedisFactory redisFactory;
    @Resource
    private RedisDataBase redisDataBase;
    @Resource
    private BookRepository bookRepository;
    @Resource
    private KeywordExt keywordExt;

    @GetMapping("/input/bookName/{book}")
    public void handleBookName(@PathVariable String book){
        int id = bookNameService.addBookName(book);
//        Set<String> words = keywordExt.extractWord(book);
//        List<String> strings = keywordCal.calculateKeyword(words);
//        RedisTemplate<String, Object> invertWord = redisFactory.getRedisTemplateByDb(redisDataBase.getInvert());
//        for (String s:strings){

//            Optional<Words> sd = bookRepository.findById("sd");
//            Words words1 = sd.get();
//            List<KeyWord> keyWords = words1.getKeyWords();
//            invertWord.opsForSet().add(s,id);

       // }

    }


    private void inputWordsOfBookName(String bookName,int id){
        RedisTemplate<String, Object> chineseDB = redisFactory.getRedisTemplateByDb(redisDataBase.getCHINESE_DATABASE());
        RedisTemplate<String, Object> englishDB = redisFactory.getRedisTemplateByDb(redisDataBase.getENGLISH_DATABASE());
        Set<LocationOfWord> locationOfWords = keywordCal.calculateKeyword(bookName);
        /*
        倒排索引，保存在redis中，以书名id+"?"+位置，组成字符串
         */
        locationOfWords.forEach(v ->{
            String word = v.getWord();
            Boolean ch = v.getCh();
            int location = v.getLocation();
            String s = indexBookName(location, id);
            if (ch){
                // 中文
                chineseDB.opsForSet().add(word,s);
            }else{
                // 英文
                englishDB.opsForSet().add(word,s);
            }
        });

    }
    private String indexBookName(int location,int bookId){
        return bookId+"?"+location;
    }
}
