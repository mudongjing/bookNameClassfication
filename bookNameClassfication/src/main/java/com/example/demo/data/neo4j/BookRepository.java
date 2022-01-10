package com.example.demo.data.neo4j;


import com.example.demo.pojo.neo4j.KeyWord;
import com.example.demo.pojo.neo4j.Words;
import org.neo4j.driver.internal.shaded.reactor.core.publisher.Flux;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BookRepository extends Neo4jRepository<Words,String> {

    @Query("match (m:Words)-[]->(n:Words) where m.word=$id return n")
    Collection<Words> findNeighbour(@Param("id") String id);

    @Query("match (n:Words),(m:Words) where id(n)=$start and id(m)=$end  " +
            "with n,m create (n)-[:KeyWord]->(m)")
    boolean createRelation(String start,String end);

    @Query("match (n:Words)-[r:KeyWord]->(m:Words) where n.word=$start and m.word=$end return r")
    KeyWord findRelationById(@Param("start") String start,@Param("end")String end);

    @Query("match (n:Words)-[r:KeyWord]->(m:Words) where n.word=$start and m.word=$end set r.distance = r.distance-$step")
    void setDistanceById(@Param("start") String start,@Param("end") String end,int step);

    @Query("match (n),(m) where n.word=$leadId and m.word=$costarId with n,m match (n)-[*]->(p)<-[*]-(m) return distinct(p);")
    Collection<Words> getInterSection(String leadId,String costarId);

    @Query("MATCH (n:Word{word: $start}),(n1:Word{word: $end}) " +
            "RETURN " +
            "CASE " +
            "  WHEN (n)-[]-(n1) " +
            "    THEN 1 " +
            "  ELSE 0 " +
            "END AS result")
    boolean existsRelation(@Param("start") String start,@Param("end") String end);

}
