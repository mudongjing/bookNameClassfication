package com.example.demo.pojo.common;

import lombok.Data;


@Data
public class LocationOfWord {
    private String word;
    private int location;
    private Boolean ch;

    public LocationOfWord(String word,int location){
        this.word = word;
        this.location= location;
    }
    public LocationOfWord(String word,int location,boolean ch){
        this.word = word;
        this.location = location;
        this.ch = ch;
    }
}
