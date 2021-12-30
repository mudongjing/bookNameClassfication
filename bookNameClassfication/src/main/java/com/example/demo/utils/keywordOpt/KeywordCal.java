package com.example.demo.utils.keywordOpt;


import com.example.demo.pojo.common.Coordinate;
import com.example.demo.pojo.keywords.Keyword;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 接受从书名中收集的词汇，对所有词汇做计算。
 * 计算的结果 是两个字作为计算依据得到一个二维向量，通过向量继而得到这个伺候的向量方向，和向量长度。
 * 依据这两个数值再去对所有词汇做分类。
 * 这里 的方案是，向量归一化得到一个1/4的圆弧，对圆弧均分，就可以按照向量的方向划分，继而再已有 的划分中，利用向量的偿长度再做划分。
 * 在对应 的分类中，记录包含该词汇的书名的id，由此，当之后提取出词汇时，可以快速地锁定可能存在这类词汇的书名，而不需要大范围地排查，
 * 如果对所有词汇都做独立的记录，则导致存储空间 的浪费。
 */
@Component
public class KeywordCal {
    public List<Keyword> calculateKeyword(Set<String> words,Integer bookId){
        List<Keyword> locationList = new ArrayList<>();



        return locationList;
    }
    // s 为两个字，利用这两个字，计算坐标
    private Coordinate calCoordinate(String s){
        int x=0,y=0;



        return  new Coordinate(x,y);
    }
}
