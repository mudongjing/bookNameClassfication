package com.example.demo.pojo.common;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class RedisDataBase {
    private int invert = 9;

    // 键名 命名为 word_id
//    private int WORD_ID = 0; // 图数据库中词汇对应的id值，一个hash表中<词汇名，id>
//    private String KEY_WORD_ID = "word_id";

    private int INVERT_WORD = 1; // 分为英文类词汇和中文类词汇
    // 英文类词汇放在一个hash表中，以单词为基本依据
    // 中文类词汇以两个字为基本依据，计算角度和模，指定hash表中的键名
    // 值中存放 包含对应词汇的书名id值
    private String KEY_CHINESE_INVERT_WORD = "chinese_word_id";
    private String KEY_ENGLISH_INVERT_WORD = "english_word_id";

    /*
    中文或英文 词汇对应倒排索引的数据库
     */
    private int CHINESE_DATABASE = 9;
    private int ENGLISH_DATABASE = 10;

}
