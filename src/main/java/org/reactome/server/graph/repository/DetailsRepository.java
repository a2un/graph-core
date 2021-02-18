package org.reactome.server.graph.repository;

import org.neo4j.ogm.model.Result;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 14.04.16.
 */
@Repository
public class DetailsRepository {

    @Autowired
    private Neo4jOperations neo4jTemplate;

    public DatabaseObject detailsPageQuery(String stId) {
        String query = "MATCH (n:DatabaseObject{stId:{stId}})-[r]->(m)" +
                "OPTIONAL MATCH (n)<-[e:inferredTo|regulator|regulatedBy]-(l)" +
                "OPTIONAL MATCH (m:ReferenceEntity)-[t:crossReference|referenceGene|referenceTranscript]->(z)" +
                "OPTIONAL MATCH (m:AbstractModifiedResidue)-[u:psiMod|modification]-(i)" +
                "OPTIONAL MATCH (m:CatalystActivity)-[o:catalystActivity|physicalEntity|activity]-(p)" +
                "OPTIONAL MATCH (m:EntityFunctionalStatus)-[q:diseaseEntity|normalEntity|functionalStatus]-(s)-[a:functionalStatusType|structuralVariant]-(b)" +
                "RETURN n,r,m,l,e,t,z,u,i,o,p,q,s,a,b";
        Map<String, Object> map = new HashMap<>();
        map.put("stId", stId);
        Result result = neo4jTemplate.query(query, map);
        if (result != null && result.iterator().hasNext()) {
            DatabaseObject n = (DatabaseObject) result.iterator().next().get("n");
            return n;
        }
        return null;
    }

    public DatabaseObject detailsPageQuery(Long dbId) {
        String query = "MATCH (n:DatabaseObject{dbId:{dbId}})-[r]->(m)" +
                "OPTIONAL MATCH (n)<-[e:inferredTo|regulator|regulatedBy]-(l)" +
                "OPTIONAL MATCH (m:ReferenceEntity)-[t:crossReference|referenceGene|referenceTranscript]->(z)" +
                "OPTIONAL MATCH (m:AbstractModifiedResidue)-[u:psiMod|modification]-(i)" +
                "OPTIONAL MATCH (m:CatalystActivity)-[o:catalystActivity|physicalEntity|activity]-(p)" +
                "OPTIONAL MATCH (m:EntityFunctionalStatus)-[q:diseaseEntity|normalEntity|functionalStatus]-(s)-[a:functionalStatusType|structuralVariant]-(b)" +
                "RETURN n,r,m,l,e,t,z,u,i,o,p,q,s,a,b";
        Map<String, Object> map = new HashMap<>();
        map.put("dbId", dbId);
        Result result = neo4jTemplate.query(query, map);
        if (result != null && result.iterator().hasNext())
            return (DatabaseObject) result.iterator().next().get("n");
        return null;
    }

}

