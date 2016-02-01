package uk.ac.ebi.reactome.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.neo4j.graphdb.*;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.reactome.domain.model.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

/**
 * This component is used to batch import Reactome data into neo4j.
 * This importer utilizes the Neo4j BatchInserter and the Reactome MySql adapter.
 * WARNING: The BatchInserter is not thread save, not transactional, and can not enforce any constraints
 *          while inserting data.
 * WARNING: DATA_DIR folder will be deleted at the start of data import
 *
 * Created by:
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 16.01.16.
 */
public class ReactomeBatchImporter {

    private static final Logger logger = LoggerFactory.getLogger(ReactomeBatchImporter.class);

    private static MySQLAdaptor dba;
    private static BatchInserter batchInserter;

    private static final String PACKAGE_NAME    = "uk.ac.ebi.reactome.domain.model.";
    private static final String DATA_DIR        = "/var/lib/neo4j/data/graph.db";


    private static final String DBID = "dbId";
    private static final String STID = "stableIdentifier";
    private static final String ACCESSION = "identifier";
    private static final String NAME = "displayName";

    private static final String CARDINALITY = "cardinality";

    private static final Map<Class, List<String>> primitiveAttributesMap = new HashMap<>();
    private static final Map<Class, List<String>> primitiveListAttributesMap = new HashMap<>();
    private static final Map<Class, List<String>> relationAttributesMap = new HashMap<>();
    private static final Map<Class, Label[]> labelMap = new HashMap<>();
    private static final Map<Long, Long> dbIds = new HashMap<>();

    public ReactomeBatchImporter(String host, String database, String user, String password, Integer port) {
        try {
            dba = new MySQLAdaptor(host,database,user,password,port);
            logger.info("Established connection to Reactome database");
        } catch (SQLException e) {
            logger.error("An error occurred while connection to the Reactome database", e);
        }
    }

    public void importAll() throws IOException {
        prepareDatabase();
        try {
            Collection<?> frontPages = dba.fetchInstancesByClass(ReactomeJavaConstants.FrontPage);
            GKInstance frontPage = (GKInstance) frontPages.iterator().next();
            Collection<?> objects = frontPage.getAttributeValuesList(ReactomeJavaConstants.frontPageItem);
            logger.info("Started importing " + objects.size() + " top level pathways");
            for (Object object : objects) {
                long start = System.currentTimeMillis();
                GKInstance instance = (GKInstance) object;
                importGkInstance(instance);
                long elapsedTime = System.currentTimeMillis() - start;
                int ms = (int) elapsedTime % 1000;
                int sec = (int) (elapsedTime / 1000) % 60;
                int min = (int) ((elapsedTime / (1000 * 60)) % 60);
                logger.info(instance.getDisplayName() + " was processed within: " + min + " min " + sec + " sec " + ms + " ms");
            }
            logger.info("All top level pathways have been imported to Neo4j");
        } catch (Exception e) {
            e.printStackTrace();
        }
        batchInserter.shutdown();
    }

    /**
     * Imports one single GkInstance into neo4j. When iterating through the relationAttributes it is possible to
     * go deeper into the GkInstance hierarchy (eg hasEvents)
     * @param instance GkInstance
     * @return Neo4j native id (generated by the BatchInserter)
     * @throws ClassNotFoundException
     */
    private Long importGkInstance(GKInstance instance) throws ClassNotFoundException {

        String clazzName = PACKAGE_NAME + instance.getSchemClass().getName();
        Class clazz = Class.forName(clazzName);

        setUpMethods(clazz);
        Long id = saveDatabaseObject(instance, clazz);

        if (!dbIds.containsKey(instance.getDBID())) {
            dbIds.put(instance.getDBID(), id);
        }

        List<String> attributes = relationAttributesMap.get(clazz);
        if(attributes != null) {
            for (String attribute : attributes) {
                try {
                    if (isValidGkInstanceAttribute(instance, attribute)) {

                        Collection<?> attributeValues = instance.getAttributeValuesList(attribute);
                        if (attributeValues == null || attributeValues.size() == 0) continue;
                        saveRelationships(id, attributeValues, attribute);

                    } else if (attribute.equals("regulatedBy")) {
                        Collection<?> referrers = instance.getReferers(ReactomeJavaConstants.regulatedEntity);
                        if (referrers == null) continue;
                        saveRelationships(id, referrers, attribute);

                    }
                } catch (Exception e) {
                    logger.error("A problem occurred when trying to retrieve data from GkInstance with attribute name: " + attribute, e);
                }

            }
        }
        instance.deflate();
        return id;
    }

    /**
     * Saves one single GkInstance to neo4j. Only primitive attributes will be saved (Attributes that are not reference
     * to another GkInstance eg values like Strings)
     * Get the attributes map and check null is slightly faster than contains.
     * @param instance GkInstance
     * @param clazz Clazz of object that will result form converting the instance (eg Pathway, Reaction)
     * @return Neo4j native id (generated by the BatchInserter)
     */
    private Long saveDatabaseObject(GKInstance instance, Class clazz) {

        Label[] labels = getLabels(clazz);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DBID, instance.getDBID());
        if (instance.getDisplayName() != null) {
            properties.put(NAME, instance.getDisplayName());
        } else {
            logger.error("Found an entry without display name! dbId: " + instance.getDBID());
        }

        try {
            List<String> attributes = primitiveAttributesMap.get(clazz);
            if (attributes != null) {
                for (String attribute : attributes) {
                    if (isValidGkInstanceAttribute(instance, attribute)) {
                        Object value = instance.getAttributeValue(attribute);
                        if (value == null) continue;
                        if (attribute.equals(STID)) {
                            GKInstance stableIdentifier = (GKInstance) value;
                            String identifier = (String) stableIdentifier.getAttributeValue(ReactomeJavaConstants.identifier);
                            properties.put(attribute, identifier);
                        } else {
                            properties.put(attribute, value);
                        }
                    }
                }
            }
            attributes = primitiveListAttributesMap.get(clazz);
            if (attributes != null) {
                for (String attribute : attributes) {
                    if (isValidGkInstanceAttribute(instance, attribute)) {
                        List valueList = instance.getAttributeValuesList(attribute);
                        if (valueList == null) continue;
                        List<String> array = new ArrayList<>();
                        for (Object value : valueList) {
                            if (value == null) continue;
                            array.add((String) value);
                        }
                        properties.put(attribute, array.toArray(new String[array.size()]));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("A problem occurred when trying to retrieve data from GkInstance :" + instance.getDisplayName() + instance.getDBID(), e);
        }
        return batchInserter.createNode(properties, labels);
    }

    /**
     * Creating a relationships between the old instance (using oldId) and its children (List objects).
     * Relationships will be created depth first, if new instance does not already exist recursion will begin
     * (newId = importGkInstance)
     * The cardinality map has to utilize a helperObject because the GkInstance does not implement Comparable and
     * comparing instances will not work. In the helperObject the instance and a counter will be saved. Counter is used
     * to set cardinality of a relationship.
     * @param oldId Old native neo4j id, used for saving a relationship to neo4j.
     * @param objects New list of GkInstances that have relationship to the old Instance (oldId).
     * @param relationName Name of the relationship.
     * @throws ClassNotFoundException
     */
    private void saveRelationships(Long oldId, Collection objects, String relationName) throws ClassNotFoundException {

        Map<Long, GkInstanceCardinalityHelper> cardinalityMap = new HashMap<>();
        for (Object object : objects) {
            if (object instanceof GKInstance) {
                GKInstance instance = (GKInstance) object;
                if(cardinalityMap.containsKey(instance.getDBID())){
                    cardinalityMap.get(instance.getDBID()).increment();
                } else {
                    cardinalityMap.put(instance.getDBID(), new GkInstanceCardinalityHelper(instance,1));
                }
            }
        }
        for (Long dbId : cardinalityMap.keySet()) {

            GKInstance instance = cardinalityMap.get(dbId).getInstance();
            Long newId;
            if (!dbIds.containsKey(dbId)) {
                newId = importGkInstance(instance);
            } else {
                newId = dbIds.get(dbId);
            }
            Map<String, Object> properties = new HashMap<>();
            properties.put(CARDINALITY,cardinalityMap.get(dbId).getCount());
            RelationshipType relationshipType = DynamicRelationshipType.withName(relationName);
            batchInserter.createRelationship(oldId, newId, relationshipType, properties);
        }
    }

    /**
     * Cleaning the old database folder, instantiate BatchInserter, create Constraints for the new DB
     */
    private void prepareDatabase() throws IOException {

        File file = cleanDatabase();
        batchInserter = BatchInserters.inserter(file);
        createConstraints();
    }

    /**
     * Creating uniqueness constraints for the new DB.
     * WARNING: Constraints can not be enforced while importing, only after batchInserter.shutdown()
     */
    private void createConstraints() {

        createSchemaConstraint(DynamicLabel.label(DatabaseObject.class.getSimpleName()), DBID);
        createSchemaConstraint(DynamicLabel.label(DatabaseObject.class.getSimpleName()), STID);

        createSchemaConstraint(DynamicLabel.label(Event.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(Event.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(Pathway.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(Pathway.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(ReactionLikeEvent.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(ReactionLikeEvent.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(Reaction.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(Reaction.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(PhysicalEntity.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(PhysicalEntity.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(Complex.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(Complex.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(EntitySet.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(EntitySet.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(GenomeEncodedEntity.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(GenomeEncodedEntity.class.getSimpleName()),STID);

        createSchemaConstraint(DynamicLabel.label(EntityWithAccessionedSequence.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(EntityWithAccessionedSequence.class.getSimpleName()),STID);
//
        createSchemaConstraint(DynamicLabel.label(ReferenceEntity.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(ReferenceEntity.class.getSimpleName()),STID);
        createIndex(DynamicLabel.label(ReferenceEntity.class.getSimpleName()), ACCESSION);

        createSchemaConstraint(DynamicLabel.label(ReferenceSequence.class.getSimpleName()),DBID);
        createSchemaConstraint(DynamicLabel.label(ReferenceSequence.class.getSimpleName()),STID);
        createIndex(DynamicLabel.label(ReferenceSequence.class.getSimpleName()), ACCESSION);
    }

    private static void createSchemaConstraint(Label label, String name) {

        try {
            batchInserter.createDeferredConstraint(label).assertPropertyIsUnique(name).create();
        } catch (ConstraintViolationException e) {
            logger.warn("Could not create Constraint on " + label + " " + name);
        }
    }

    private static void createIndex(Label label, String name) {
        batchInserter.createDeferredSchemaIndex(label).on(name);
    }

    /**
     * Cleaning the Neo4j data directory
     * Deleting of file will have worked even if error occurred here.
     */
    private File cleanDatabase() {

        File dir = new File(DATA_DIR);
        try {
            if(dir.exists()) {
                FileUtils.cleanDirectory(dir);
            } else {
                FileUtils.forceMkdir(dir);
            }
        } catch (IOException | IllegalArgumentException e) {
            logger.warn("An error occurred while cleaning the old database");
        }
        return dir;
    }

    /**
     * Getting all SimpleNames as neo4j labels, for given class.
     * @param clazz Clazz of object that will result form converting the instance (eg Pathway, Reaction)
     * @return Array of Neo4j LabelsCount
     */
    private Label[] getLabels(Class clazz) {

        if(!labelMap.containsKey(clazz)) {
            Label[] labels = getAllClassNames(clazz);
            labelMap.put(clazz, labels);
            return labels;
        } else {
            return labelMap.get(clazz);
        }
    }

    /**
     * Getting all SimpleNames as neo4j labels, for given class.
     * @param clazz Clazz of object that will result form converting the instance (eg Pathway, Reaction)
     * @return Array of Neo4j LabelsCount
     */
    private Label[] getAllClassNames(Class clazz) {
        List<?> superClasses = ClassUtils.getAllSuperclasses(clazz);
        List<Label> labels = new ArrayList<>();
        labels.add(DynamicLabel.label(clazz.getSimpleName()));
        for (Object object : superClasses) {
            Class superClass = (Class) object;
            if(!superClass.getSimpleName().equals("Object")) {
                labels.add(DynamicLabel.label(superClass.getSimpleName()));
            }
        }
        return labels.toArray(new Label[labels.size()]);
    }

    /**
     * Gets and separates all Methods for specific Class to create attribute map.
     * GetMethods are used to differentiate on the return type of the method.
     * If return type is "primitive" eg String than this method will be used to provide a primitiveAttributeName
     * If return type is "relationship" (Object of the model package) than this method will be used to provide a
     * relationshipAttributeName.
     * Getters are used here rather than setters because setters will return a Type[] when getting the
     * GenericParameterTypes.
     * getFields[] can not be utilized here because this method can not return inherited fields.
     * @param clazz Clazz of object that will result form converting the instance (eg Pathway, Reaction)
     */
    private void setUpMethods(Class clazz) {
        if(!relationAttributesMap.containsKey(clazz) && !primitiveAttributesMap.containsKey(clazz)) {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if (        methodName.startsWith("get")
                        && !methodName.startsWith("getSuper")
                        && !methodName.equals("getClass")
                        && !methodName.equals("getId") //getter/setter should be removed from model
                        && !methodName.equals("getDbId")
                        && !methodName.equals("getDisplayName")
                        && !methodName.equals("getTimestamp") //should be removed from model!
                        && !methodName.equals("getSchemaClass")) { //should be removed from model!
                    Type returnType = method.getGenericReturnType();
                    if (returnType instanceof ParameterizedType) {
                        ParameterizedType type = (ParameterizedType) returnType;
                        Type[] typeArguments = type.getActualTypeArguments();
                        for (Type typeArgument : typeArguments) {
                            Class typeArgClass = (Class) typeArgument;
                            if (DatabaseObject.class.isAssignableFrom(typeArgClass) ) {
                                setMethods(relationAttributesMap, clazz, method);
                            }
                            else {
                                setMethods(primitiveListAttributesMap, clazz, method);
                            }
                        }
                    } else {
                        if (DatabaseObject.class.isAssignableFrom(method.getReturnType())) {
                            setMethods(relationAttributesMap, clazz, method);
                        } else {
                            setMethods(primitiveAttributesMap, clazz, method);
                        }
                    }
                }
            }
        }
    }

    private boolean isValidGkInstanceAttribute(GKInstance instance, String attribute) {
        if(instance.getSchemClass().isValidAttribute(attribute)) {
            return true;
        } if (!attribute.equals("regulatedBy")) {
            logger.warn(attribute + " is not a valid attribute for instance " + instance.getSchemClass());
        }
        return false;
    }

    /**
     * Put Attribute name into map.
     * @param map attribute map
     * @param clazz Clazz of object that will result form converting the instance (eg Pathway, Reaction)
     * @param method Method specific to clazz
     */
    private void setMethods (Map<Class,List<String>> map, Class clazz, Method method) {
        String fieldName = method.getName().substring(3);
        fieldName = lowerFirst(fieldName);
        if(map.containsKey(clazz)) {
            (map.get(clazz)).add(fieldName);
        } else {
            List<String> methodList = new ArrayList<>();
            methodList.add(fieldName);
            map.put(clazz, methodList);
        }
    }

    /**
     * First letter of string made to lower case.
     * @param str String
     * @return String
     */
    private String lowerFirst(String str) {
        if(StringUtils.isAllUpperCase(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}