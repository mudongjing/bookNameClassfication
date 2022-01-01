package com.example.demo.utils.keywordOpt;

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
    /**
     * 从书名中提取词汇，并返回 结果
     * @param bookName
     * @return
     */
    public Set<String> extractWord(String bookName){
        if(bookName==null || bookName.trim().length()==0) return null;
        bookName = bookName.trim();
        Set<String> result = new HashSet<>();
        int len = bookName.length();
        if (len ==1){
            if (isEn(bookName.charAt(0))) result.add(bookName);
            else return null;
        }else if(len==2){
            if (isSame(bookName,0,1)) result.add(bookName);
            else if(isEn(bookName.charAt(0))) result.add(bookName.substring(0,1));
            else if(isEn(bookName.charAt(1))) result.add(bookName.substring(1,2));
            else return null;
        } else{
            int lent = bookName.length();
            for (int i=0;i<=lent-1;){
                if (bookName.charAt(i) == ' ') { i++;continue;
                } else{
                    // 判断是否可以组成一个两个字的词汇，不处理单个字
                    // 如果是英文就找到所属的单词,数字也当作中类似英文的字母，找到完整的数字
                    if (i+1< lent && bookName.charAt(i+1) == ' '){
                        i+=2;continue;
                    }else{// 这里需要判断英文和数字的可能
                        // 不能成为一个单词或数字 或词汇
                        if (i==lent-1){
                            if (isEn(bookName.charAt(i))) result.add(bookName.substring(i,i+1));
                        } else if (!isSame(bookName,i,i+1)){// 当前两个字无法作为单个词
                            // 如果第一个字是英文，就记录，否则跳过
                            if (isEn(bookName.charAt(i))) result.add(bookName.substring(i,i+1));
                            i++;continue;// 两个字符无法成为词汇，就把第一个作为独立的字符保存
                        }else{ // 此时，考虑是否为汉字，如果不是，那么需要找到对应的单词或数字
                            if (enOrNum(bookName.charAt(i))==OrNot.med) {//是汉字
                                result.add(bookName.substring(i,i+2));
                                i++;continue;
                            }else{//不是汉字，需要一直向后查看可能的最大词汇
                                int end = theMaxEnd(bookName,i);
                                result.add(bookName.substring(i,end+1));
                                i = end+1;continue;
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
    private boolean isEn(char c){
        int c_int = (int)c;
        if ((c>='a' && c<='z') ||
                (c_int >=33 && c_int<=38) || (c_int >=40 && c_int <=43) || (c_int >=45 && c_int <=57)||
                (c_int >=60 && c_int <=62) || (c_int>=64 && c_int<=95)
        ) return true;

        return false;
    }
    public boolean isCh(char c){
        int c_int = (int)c;
        if (c_int>=12000 && c_int <= 40943) return true;
        return false;
    }
}
