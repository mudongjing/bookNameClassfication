package com.example.demo.utils.keywordReview;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

@Component
public class RelationOpt {
    // 引入neo4j 连接，负责通过词汇查询相关的连接

    /**
     * 获取词汇相关的词汇，附带不同词汇 的权重
     * @param keyword
     */
    public HashMap<String,Float> getRelationOfWord(String keyword){
        HashMap<String,Float> result = new LinkedHashMap<>();


        return result;
    }

    /**
     * 将集合中的词汇在图中互相标记，如果已经存在则加强联系
     * @param words
     */
    public void addRelation(Set<String> words){

    }

}
