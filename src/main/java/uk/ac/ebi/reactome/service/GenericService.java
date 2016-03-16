package uk.ac.ebi.reactome.service;

import org.neo4j.ogm.model.Result;
import uk.ac.ebi.reactome.domain.model.Pathway;

import java.util.Collection;
import java.util.Map;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 15.11.15.
 */
@SuppressWarnings("SameParameterValue")
public interface GenericService {

    <T> T findById(Class<T> clazz, Long id, Integer depth);
    <T> T findByProperty(Class<T> clazz, String property, Object value, Integer depth);
    <T> T findByDbId(Class<T> clazz, Long dbId, Integer depth);
    <T> T findByStableIdentifier(Class<T> clazz, String stableIdentifier, Integer depth);

    <T> Collection<T> getObjectsByClassName(String className, Integer page, Integer offset) throws ClassNotFoundException;

    Object findByPropertyWithRelations (String property, Object value, String... relationships);
    Object findByPropertyWithoutRelations (String property, Object value, String... relationships);

    Collection<Pathway> findTopLevelPathways();
    Collection<Pathway> findTopLevelPathways(Long speciesId);
    Collection<Pathway> findTopLevelPathways(String speciesName);

    Result query (String query, Map<String,Object> map);
    Long countEntries(Class<?> clazz);

    boolean fitForService() ;
    void clearCache();

}
