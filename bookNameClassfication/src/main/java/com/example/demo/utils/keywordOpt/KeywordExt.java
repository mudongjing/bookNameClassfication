package com.example.demo.utils.keywordOpt;

import com.example.demo.pojo.common.LocationOfWord;
import com.example.demo.pojo.common.OrNot;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 负责提取书名中的词汇，以两个字为一组。
 * 将结果作为集合输出
 */
@Component
public class KeywordExt {
    /*
     * 从书名中提取词汇，并返回 结果
     */
    public Set<LocationOfWord> extractWord(String bookName){
        if(bookName==null || bookName.trim().length()==0) return null;
        bookName = bookName.trim();
        Set<LocationOfWord> result = new HashSet<>();
        int len = bookName.length();
        if (len ==1){
            if (isEn(bookName.charAt(0))) result.add(new LocationOfWord(bookName,0));
            else return null;
        }else if(len==2){
            if (isSame(bookName,0,1)) result.add(new LocationOfWord(bookName,0));
            else if(isEn(bookName.charAt(0))) result.add( new LocationOfWord(bookName.substring(0,1),0));
            else if(isEn(bookName.charAt(1))) result.add( new LocationOfWord(bookName.substring(1,2),1));
            else return null;
        } else{
            int lent = bookName.length();
            for (int i=0;i<=lent-1;){
                if (isValidChar(bookName.charAt(i))) { i++;
                } else{
                    // 判断是否可以组成一个两个字的词汇，不处理单个字
                    // 如果是英文就找到所属的单词,数字也当作中类似英文的字母，找到完整的数字
                    if (i+1< lent  && !isValidChar(bookName.charAt(i+1))){
                        i+=2;
                    }else{// 这里需要判断英文和数字的可能
                        // 不能成为一个单词或数字 或词汇
                        if (i==lent-1){
                            if (isEn(bookName.charAt(i)))
                                result.add(new LocationOfWord(bookName.substring(i,i+1),i));
                        } else if (!isSame(bookName,i,i+1)){// 当前两个字无法作为单个词
                            // 如果第一个字是英文，就记录，否则跳过
                            if (isEn(bookName.charAt(i)))
                                result.add(new LocationOfWord(bookName.substring(i,i+1),i));
                            i++;// 两个字符无法成为词汇，就把第一个作为独立的字符保存
                        }else{ // 此时，考虑是否为汉字，如果不是，那么需要找到对应的单词或数字
                            if (enOrNum(bookName.charAt(i))==OrNot.med) {//是汉字
                                result.add(new LocationOfWord(bookName.substring(i,i+2),i));
                                i++;
                            }else{//不是汉字，需要一直向后查看可能的最大词汇
                                int end = theMaxEnd(bookName,i);
                                result.add(new LocationOfWord(bookName.substring(i,end+1),i));
                                i = end+1;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private int theMaxEnd(String string,int begin){//获取最长的单词或数字
        int endMax = string.length()-1;
        OrNot flag = enOrNum(string.charAt(begin));
        int end = begin+2;
        while(end<=endMax){
            if (enOrNum(string.charAt(end))==flag) end++;
            else { end--;break; }
        }
        return end;
    }
    private boolean isSame(String string,int begin,int end){
        char c = string.charAt(begin);
        OrNot flag = enOrNum(c);
        for (int i = begin+1;i<= end;i++){
            if (enOrNum(string.charAt(i))!=flag) return false;
        }
        return true;
    }
    public OrNot enOrNum(char c){
        if (isEn(c)) return OrNot.rig;
        else if(isCh(c)) return OrNot.med;
        else return OrNot.lef;
    }
    // 英文包括数字和特殊字符
    public boolean isEn(char c){
        return (c >= 'a' && c <= 'z') ||
                ((int) c >= 33 && (int) c <= 38)
                ||
                ((int) c >= 40 && (int) c <= 43)
                ||
                ((int) c >= 45 && (int) c <= 57)
                ||
                ((int) c >= 60 && (int) c <= 62)
                ||
                ((int) c >= 64 && (int) c <= 95);
    }
    public boolean isCh(char c){
        return (int) c >= 12000 && (int) c <= 40943;
    }

    public boolean isValidChar(char c){
        if (c == ' ' || (!isEn(c) && !isCh(c))) return false;
        return true;
    }
}
