package com.example.demo.pojo.common;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Partition {
    private int oneDPartition = 100;
    private int twoDPartition = 100;
}
