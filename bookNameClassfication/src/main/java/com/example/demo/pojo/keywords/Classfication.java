package com.example.demo.pojo.keywords;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
public class Classfication {
    private int coordi = 1 << 8;
    private double circleCapcity = 2 * Math.PI;
    private double distanceCapcity = Math.sqrt(2) * coordi;
}
