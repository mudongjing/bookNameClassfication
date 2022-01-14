package com.example.demo.pojo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
查询书名的结果类型，包含书名，和对应的影响权重值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult implements Comparable{
    private int groupId;
    private String bookName;
    private double weight;

    @Override
    public int compareTo(Object o) {
        SearchResult d = (SearchResult)o;
        if (this.weight < d.getWeight()) return 1;
        else return -1;
    }
    public SearchResult(int groupId,String bookName){
        this.groupId = groupId;
        this.bookName = bookName;
    }
}
