package com.example.demo.pojo.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyWord {
    @RelationshipId
    @GeneratedValue
    private Long id;
    @TargetNode
    private Words words;


    private int distance = 1 << 16; // 默认大小，之后只会缩小

    public void reduceDistance(int step){
        this.distance -= step;
    }

}
