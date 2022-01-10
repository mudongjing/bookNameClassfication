package com.example.demo.data.neo4j;

import com.example.demo.pojo.neo4j.KeyWord;
import com.example.demo.pojo.neo4j.Words;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyWordRepository  extends Neo4jRepository<KeyWord,String> {
}
