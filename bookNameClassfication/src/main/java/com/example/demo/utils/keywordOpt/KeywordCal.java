package com.example.demo.utils.keywordOpt;



import com.example.demo.pojo.common.Partition;
import com.example.demo.pojo.keywords.Classfication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.Math.*;

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
    @Autowired
    private Classfication classfication;
    @Autowired
    private Partition partition;
    @Autowired
    private KeywordExt keywordExt;

    public List<String> calculateKeyword(Set<String> words){
        List<String> result = new ArrayList<>();
        // 如果为非汉字，则不参与计算，
        for (String s:words) {
            if (keywordExt.isCh(s.charAt(0))){//汉字
                result.add(calCoordinate(s));
            }else{ //英文类，直接存储
                result.add(s);
            }
        }
        return result;
    }
    // s 为两个字，利用这两个字，计算坐标
    private String calCoordinate(String s){//针对汉字
        int x=0,y=0;
        x = bernstein(s.charAt(0)) % classfication.getCoordi();
        y = bernstein(s.charAt(1)) % classfication.getCoordi();
        return divideRadian(x,y);
    }

    private String  divideRadian(int x,int y){
        double degree = calculateDegree(x,y);
        double distance = sqrt(pow(x,2)+ pow(y,2));

        int radianId = calculatePartition(degree,classfication.getCircleCapcity(),partition.getOneDPartition());
        int distanceId = calculatePartition(distance,classfication.getDistanceCapcity(),partition.getTwoDPartition());

        return radianId+"_"+distanceId;//作为redis中的键名
    }

    private double calculateDegree(int x,int y){
        double sita = atan((abs(y)*1.0)/abs(x));
        double degree = 0.0;
        if (x>=0){
            if (y>=0) degree = sita; else degree = 2 * PI - sita;
        }else{
            if (y>=0) degree = PI - sita; else degree = PI + sita;
        }
        return degree;
    }

    private int calculatePartition(double target,double capcity,int partition){
        double delta = capcity / partition;

        if (target<delta) return 0;
        else if (target >= (partition-1)*delta) return partition-1;

        int start=0;int stop=partition;
        while(true){
            if (start+1 == stop) return start;
            int mid = start + (stop - start) >> 1;
            double left = mid*delta;
            double right = (mid+1)*delta;
            if (target >= left && target < right) return mid;
            else if (target < left) stop = mid;
            else if (target == right) return mid+1;
            else if (target > right) start = mid;
        }
    }



    private int bernstein(char data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        hash = (hash ^ data )* p;
        hash += hash << 13;
        hash ^= hash >>7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }
}
