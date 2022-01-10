package com.example.demo.pojo.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;
import java.util.Set;

@Node("Book")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Words {
    @Id
    private String word;

//    private String word;
    @Relationship(type="same",direction = Relationship.Direction.OUTGOING)
    private List<KeyWord> keyWords;

    public Words(String word){
        this.word=word;
    }
}
