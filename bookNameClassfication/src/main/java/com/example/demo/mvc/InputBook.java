package com.example.demo.mvc;

import com.example.demo.data.mysql.service.BookNameService;
import com.example.demo.data.redis.RedisFactory;
import com.example.demo.pojo.common.RedisDataBase;
import com.example.demo.utils.keywordOpt.KeywordCal;
import com.example.demo.utils.keywordOpt.KeywordExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class InputBook {
    @Autowired
    private BookNameService bookNameService;
    @Autowired
    private KeywordCal keywordCal;
    @Autowired
    private KeywordExt keywordExt;
    @Autowired
    private RedisFactory redisFactory;
    @Autowired
    private RedisDataBase redisDataBase;

    @GetMapping("/input/bookName/{book}")
    public void handleBookName(@PathVariable String book){
        int id = bookNameService.addBookName(book);
        Set<String> words = keywordExt.extractWord(book);
        List<String> strings = keywordCal.calculateKeyword(words);
        RedisTemplate<String, Object> invertWord = redisFactory.getRedisTemplateByDb(redisDataBase.getInvert());
        for (String s:strings){
            invertWord.opsForSet().add(s,id);
        }
    }
}
