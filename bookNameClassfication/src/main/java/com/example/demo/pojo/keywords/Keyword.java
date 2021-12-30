package com.example.demo.pojo.keywords;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Keyword {
    Integer id; // 书名id
    List<Classfication> locationPair;//书名中词汇存在的分类地址

    public void addElement(Classfication classfication){
        this.locationPair.add(classfication);
    }
    public Classfication getElement(int index){
        return this.locationPair.get(index);
    }
    public int getSizeOfElement(){
        return this.locationPair.size();
    }
}
