package com.example.demo.utils.keywordOpt;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 负责提取书名中的词汇，以两个字为一组。
 * 将结果作为集合输出
 */
@Component
public class KeywordExt {
    /**
     * 从书名中提取词汇，并返回 结果
     * @param bookName
     * @return
     */
    public Set<String> extractWord(String bookName){
        Set<String> result = new HashSet<>();


        return result;
    }
}
