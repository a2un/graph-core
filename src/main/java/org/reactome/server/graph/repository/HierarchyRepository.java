//package org.reactome.server.graph.repository;
//
//import org.neo4j.ogm.model.Result;
//import org.reactome.server.graph.domain.model.DatabaseObject;
//import org.reactome.server.graph.domain.model.Event;
//import org.reactome.server.graph.domain.model.Pathway;
//import org.reactome.server.graph.domain.model.PhysicalEntity;
//import org.reactome.server.graph.exception.CustomQueryException;
//import org.reactome.server.graph.repository.util.HierarchyBranch;
//import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
//import org.reactome.server.graph.service.helper.PathwayBrowserNode;
//import org.reactome.server.graph.service.util.DatabaseObjectUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.neo4j.template.Neo4jOperations;
//import org.springframework.stereotype.Repository;
//
//import java.util.*;
//
///**
// * @author Florian Korninger (florian.korninger@ebi.ac.uk)
// */
//@Repository
//public class HierarchyRepository {
//
//    private static final Logger logger = LoggerFactory.getLogger(DetailsRepository.class);
//
//    private Neo4jOperations neo4jTemplate;
//
//    private AdvancedDatabaseObjectService ados;
//
//    // -------------------------------- Locations in the Pathway Browser -----------------------------------------------
//
//    /**
//     * Get locations in the Pathway Browser in the normal search flow.
//     *
//     * @return a PathwayBrowserNode.
//     */
//    public PathwayBrowserNode getLocationsInPathwayBrowser(String stId, Boolean omitNonDisplayableItems) {
//        Result result = getLocationsInPathwayBrowserByStIdRaw(stId);
//        return parseResult(result, omitNonDisplayableItems);
//    }
//
//    public PathwayBrowserNode getLocationsInPathwayBrowser(Long dbId, Boolean omitNonDisplayableItems) {
//        Result result = getLocationsInPathwayBrowserByDbIdRaw(dbId);
//        return parseResult(result, omitNonDisplayableItems);
//    }
//
//    /**
//     * Get locations in the Pathway Browser based on Interactor. It has a different query than the normal flow.
//     *
//     * @return a PathwayBrowserNode.
//     */
//    public PathwayBrowserNode getLocationsInPathwayBrowserDirectParticipants(String stId, Boolean omitNonDisplayableItems) {
//        Result result = getLocationsInPathwayBrowserForInteractorByStIdRaw(stId);
//        return parseResult(result, omitNonDisplayableItems);
//    }
//
//    public PathwayBrowserNode getLocationsInPathwayBrowserDirectParticipants(Long dbId, Boolean omitNonDisplayableItems) {
//        Result result = getLocationsInPathwayBrowserForInteractorByDbIdRaw(dbId);
//        return parseResult(result, omitNonDisplayableItems);
//    }
//
//    /**
//     * Used to build the locations in the Pathway Browser for the EHLD containing a given icon
//     * @param pathways list of pathways (can contain Pathway objects, dbIds, stIds - or a combination of them)
//     * @return a set of PathwayBrowserNode
//     */
//    public Set<PathwayBrowserNode> getLocationInPathwayBrowserForPathways(List<?> pathways){
//        Collection<HierarchyBranch> branches = getLocationInPathwayBrowserForPathwaysRaw(pathways);
//        return mergeBranches(branches);
//    }
//
//    // --------------------------------------------- Sub Hierarchy -----------------------------------------------------
//
//    public PathwayBrowserNode getSubHierarchy(String stId) {
//        Result result = getSubHierarchyByStIdRaw(stId);
//        return parseResult(result, false);
//    }
//
//    public PathwayBrowserNode getSubHierarchy(Long dbId) {
//        Result result = getSubHierarchyByDbIdRaw(dbId);
//        return parseResult(result, false);
//    }
//
//    // ------------------------------------------- Event Hierarchy -----------------------------------------------------
//
//    public Collection<PathwayBrowserNode> getEventHierarchyBySpeciesName(String speciesName) {
//        Result result = getEventHierarchyBySpeciesNameRaw(speciesName);
//        return parseResults(result, false);
//    }
//
//    public Collection<PathwayBrowserNode> getEventHierarchyByTaxId(String taxId) {
//        Result result = getEventHierarchyByTaxIdRaw(taxId);
//        return parseResults(result, false);
//    }
//
//
//    /**
//     * Build Locations in the Pathway Browser of a given query Result.
//     * <p>
//     * //     * @param id the one where querying for
//     *
//     * @param result the cypher query result
//     * @return PathwayBrowserNode having parents and children
//     */
//    private PathwayBrowserNode parseResult(Result result, Boolean omitNonDisplayableItems) {
//
//        if (result != null && result.iterator().hasNext()) {
//            Map<String, Object> stringObjectMap = result.iterator().next();
//            return parseRaw(stringObjectMap, omitNonDisplayableItems);
//        }
//        return null;
//    }
//
//
//    private Collection<PathwayBrowserNode> parseResults(Result result, Boolean omitNonDisplayableItems) {
//
//        if (result != null && result.iterator().hasNext()) {
//            Collection<PathwayBrowserNode> eventHierarchy = new ArrayList<>();
//            for (Map<String, Object> stringObjectMap : result) {
//                eventHierarchy.add(parseRaw(stringObjectMap, omitNonDisplayableItems));
//            }
//            return eventHierarchy;
//        }
//        return null;
//    }
//
//    @SuppressWarnings("unchecked")
//    private PathwayBrowserNode parseRaw(Map<String, Object> stringObjectMap, Boolean omitNonDisplayableItems) {
//        PathwayBrowserNode root = createNode((DatabaseObject) stringObjectMap.get("n"));
//        Map<String, PathwayBrowserNode> nodes = new HashMap<>();
//        PathwayBrowserNode previous = root;
//        nodes.put(root.getStId(), root);
//        int previousSize = 0;
//        Object[] nodePairCollections = (Object[]) stringObjectMap.get("nodePairCollection");
//        if (nodePairCollections != null && nodePairCollections.length > 0) {
//            for (ArrayList<Object> nodePairCollection : ((ArrayList<Object>[]) nodePairCollections)) {
//                int size = nodePairCollection.size();
//                if (size > previousSize) {
//                    ArrayList<Object> objects = (ArrayList<Object>) nodePairCollection.get(nodePairCollection.size() - 1);
//                    previous = addNode(previous, nodes, objects, omitNonDisplayableItems);
//                } else {
//                    previous = root;
//                    for (Object objects : nodePairCollection) {
//                        previous = addNode(previous, nodes, (ArrayList) objects, omitNonDisplayableItems);
//                    }
//                }
//                previousSize = size;
//            }
//        }
//        return root;
//    }
//
//    private Set<PathwayBrowserNode> mergeBranches(Collection<HierarchyBranch> branches){
//        Map<String, PathwayBrowserNode> nodes = new HashMap<>();
//
//        Set<PathwayBrowserNode> rtn = new HashSet<>();
//        for (HierarchyBranch branch : branches) {
//            List<Pathway> pathways = branch.getPathways();
//            Pathway p = pathways.get(0);
//            PathwayBrowserNode node = nodes.getOrDefault(p.getStId(), createNode(p));
//            nodes.put(node.getStId(), node);
//            rtn.add(node);
//            for (int i = 1 ; i < pathways.size(); i++) {
//                Pathway pathway = pathways.get(i);
//                PathwayBrowserNode child = nodes.getOrDefault(pathway.getStId(), createNode(pathway));
//                nodes.put(child.getStId(), child);
//                node.addChild(child);
//                child.addParent(node);
//                node = child;
//            }
//        }
//        return rtn;
//    }
//
//    @SuppressWarnings("SuspiciousMethodCalls")
//    private PathwayBrowserNode addNode(PathwayBrowserNode previous, Map<String, PathwayBrowserNode> nodes, ArrayList<Object> objects, Boolean omitNonDisplayableItems) {
//        PathwayBrowserNode node;
//
//        if (nodes.containsKey(objects.get(0))) {
//            node = nodes.get(objects.get(0));
//        } else {
//            node = createNode(objects);
//            nodes.put(node.getStId(), node);
//        }
//
//        //We do not link them in the Tree.
//        if (!omitNonDisplayableItems || (!node.getType().equals("CatalystActivity") && !node.getType().contains("Regulation") && !node.getType().equals("Requirement") && !node.getType().equals("EntityFunctionalStatus"))) {
//            previous.addChild(node);
//            node.addParent(previous);
//            previous = node;
//        }
//        return previous;
//    }
//
//    /**
//     * Create a node based on the query Result
//     *
//     * @param nodePairCollection, the query result
//     */
//    @SuppressWarnings("unchecked")
//    private PathwayBrowserNode createNode(ArrayList<Object> nodePairCollection) {
//        PathwayBrowserNode node = new PathwayBrowserNode();
//        node.setStId((String) nodePairCollection.get(0));
//        node.setName((String) nodePairCollection.get(1));
//        node.setDiagram((Boolean) nodePairCollection.get(2));
//        node.setSpecies((String) nodePairCollection.get(3));
//        node.setType((String) nodePairCollection.get(4));
//
//        doHighlighting(node);
//
//        return node;
//    }
//
//    private PathwayBrowserNode createNode(DatabaseObject databaseObject) {
//        PathwayBrowserNode node = new PathwayBrowserNode();
//        node.setStId(databaseObject.getStId());
//        node.setName(databaseObject.getDisplayName());
//        // do not use SchemaClass here
//        node.setType(databaseObject.getClass().getSimpleName());
//
//        // Root by default is clickable and highlighted
//        node.setClickable(true);
//        node.setHighlighted(true);
//
//        if (databaseObject instanceof Event) {
//            Event event = (Event) databaseObject;
//            node.setSpecies(event.getSpeciesName());
//            if (event instanceof Pathway) {
//                Pathway pathway = (Pathway) event;
//                node.setDiagram(pathway.getHasDiagram());
//            }
//        } else if (databaseObject instanceof PhysicalEntity) {
//            PhysicalEntity physicalEntity = (PhysicalEntity) databaseObject;
//            node.setSpecies(physicalEntity.getSpeciesName());
//        } else {
//            logger.error("Creating a node that is not an Event or PhysicalEntity");
//        }
//        return node;
//    }
//
//
//    private void doHighlighting(PathwayBrowserNode node) {
//        if (node.getType().equals("TopLevelPathway")) {
//            node.setClickable(true);
//            node.setHighlighted(false);
//        }
//
//        if (node.getType().contains("Reaction") || node.getType().equals("BlackBoxEvent") || node.getType().contains("Polymerisation")) {
//            node.setClickable(true);
//            node.setHighlighted(true);
//        }
//    }
//
//    // --------------------------------------------- Sub Hierarchy -----------------------------------------------------
//
//    private Result getSubHierarchyByDbIdRaw(Long dbId) {
//        String query = "Match (n:DatabaseObject{dbId:{dbId}}) OPTIONAL MATCH(n)-[r:hasEvent|input|output|repeatedUnit|hasMember|hasCandidate|hasComponent*]->(m:DatabaseObject) " +
//                "Return n.stId, .displayName, n.hasDiagram, n.speciesName, labels(n), COLLECT(EXTRACT(rel IN r | [endNode(rel).stId, endNode(rel).displayName, endNode(rel).hasDiagram, endNode(rel).speciesName, endNode(rel).schemaClass ])) AS nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("dbId", dbId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    private Result getSubHierarchyByStIdRaw(String stId) {
//        String query = "Match (n:DatabaseObject{stId:{stId}}) OPTIONAL MATCH(n)-[r:hasEvent|input|output|repeatedUnit|hasMember|hasCandidate|hasComponent*]->(m:DatabaseObject) " +
//                "Return n, COLLECT(EXTRACT(rel IN r | [endNode(rel).stId, endNode(rel).displayName, endNode(rel).hasDiagram, endNode(rel).speciesName, endNode(rel).schemaClass ])) AS nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("stId", stId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    // ------------------------------------------- Event Hierarchy -----------------------------------------------------
//
//    private Result getEventHierarchyBySpeciesNameRaw(String speciesName) {
//        String query = "Match (n:TopLevelPathway{speciesName:{speciesName}})-[r:hasEvent*]->(m:Event)" +
//                "Return n, COLLECT(EXTRACT(rel IN r | [endNode(rel).stId, endNode(rel).displayName, endNode(rel).hasDiagram, endNode(rel).speciesName, endNode(rel).schemaClass ])) AS nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("speciesName", speciesName);
//        return neo4jTemplate.query(query, map);
//    }
//
//    private Result getEventHierarchyByTaxIdRaw(String taxId) {
//        String query = "Match (s:Species{taxId:{taxId}})<-[:species]-(n:TopLevelPathway)-[r:hasEvent*]->(m:Event) " +
//                "Return n, COLLECT(EXTRACT(rel IN r | [endNode(rel).stId, endNode(rel).displayName, endNode(rel).hasDiagram, endNode(rel).speciesName, endNode(rel).schemaClass ])) AS nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("taxId", taxId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    // -------------------------------- Locations in the Pathway Browser -----------------------------------------------
//
//    /**
//     * This cypher query retrieves all the information needed to build the Locations in the PWB Tree.
//     * [StableIdentifier, _displayName, hasDiagram, [labels e.g DatabaseObject, Event, ReactionLikeEvent, Reactions]]
//     *
//     * @param stId, tree for the given StId
//     * @return nodePairCollection
//     */
//    private Result getLocationsInPathwayBrowserByStIdRaw(String stId) {
//        String query = "" +
//                "MATCH (n:DatabaseObject{stId:{stId}}) " +
//                "OPTIONAL MATCH path=(n)<-[:regulatedBy|regulator|physicalEntity|requiredInputComponent|diseaseEntity|entityFunctionalStatus|activeUnit|catalystActivity|repeatedUnit|hasMember|hasCandidate|hasComponent|input|output|hasEvent*]-() " +
//                "RETURN n, COLLECT(EXTRACT(rel IN relationships(path)| [startNode(rel).stId, startNode(rel).displayName, startNode(rel).hasDiagram,startNode(rel).speciesName, startNode(rel).schemaClass ])) as nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("stId", stId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    /**
//     * This cypher query retrieves all the information needed to build the Locations in the PWB Tree
//     * [StableIdentifier, _displayName, hasDiagram, [labels e.g DatabaseObject, Event, ReactionLikeEvent, Reactions]]
//     *
//     * @param dbId, tree for the given dbId
//     * @return nodePairCollection
//     */
//    private Result getLocationsInPathwayBrowserByDbIdRaw(Long dbId) {
//        String query = "" +
//                "MATCH (n:DatabaseObject{dbId:{dbId}}) " +
//                "OPTIONAL MATCH path=(n)<-[:regulatedBy|regulator|physicalEntity|requiredInputComponent|diseaseEntity|entityFunctionalStatus|activeUnit|catalystActivity|repeatedUnit|hasMember|hasCandidate|hasComponent|input|output|hasEvent*]-() " +
//                "RETURN n, COLLECT(EXTRACT(rel IN relationships(path)| [startNode(rel).stId, startNode(rel).displayName, startNode(rel).hasDiagram,startNode(rel).speciesName, startNode(rel).schemaClass ])) as nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("dbId", dbId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    /**
//     * This cypher query retrieves all the information needed to build the Locations in the PWB Tree based on the Interactors.
//     * It means, we are filtering the tree to show only Proteins directly associate to a Reaction, then when the user clicks on it
//     * He will see the protein in the Diagram.
//     * <p>
//     * [StableIdentifier, _displayName, hasDiagram, [labels e.g DatabaseObject, Event, ReactionLikeEvent, Reactions]]
//     *
//     * @param stId, tree for the given stId
//     * @return nodePairCollection
//     */
//    private Result getLocationsInPathwayBrowserForInteractorByStIdRaw(String stId) {
//        String query = "" +
//                "MATCH (n:DatabaseObject{stId:{stId}}) " +
//                "OPTIONAL MATCH path=(n)<-[:regulatedBy|regulator|physicalEntity|catalystActivity|requiredInputComponent|diseaseEntity|entityFunctionalStatus|input|output|hasEvent*]-() " +
//                "RETURN n, COLLECT(EXTRACT(rel IN relationships(path) | [startNode(rel).stId, startNode(rel).displayName, startNode(rel).hasDiagram,startNode(rel).speciesName, startNode(rel).schemaClass ])) as nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("stId", stId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    /**
//     * This cypher query retrieves all the information needed to build the Locations in the PWB Tree based on the Interactors.
//     * It means, we are filtering the tree to show only Proteins directly associate to a Reaction, then when the user clicks on it
//     * He will see the protein in the Diagram.
//     * <p>
//     * [StableIdentifier, _displayName, hasDiagram, [labels e.g DatabaseObject, Event, ReactionLikeEvent, Reactions]]
//     *
//     * @param dbId, tree for the given dbId
//     * @return nodePairCollection
//     */
//    private Result getLocationsInPathwayBrowserForInteractorByDbIdRaw(Long dbId) {
//        String query = "" +
//                "MATCH (n:DatabaseObject{dbId:{dbId}}) " +
//                "OPTIONAL MATCH path=(n)<-[:regulatedBy|regulator|physicalEntity|catalystActivity|requiredInputComponent|diseaseEntity|entityFunctionalStatus|input|output|hasEvent*]-() " +
//                "RETURN n, COLLECT(EXTRACT(rel IN relationships(path) | [startNode(rel).stId, startNode(rel).displayName, startNode(rel).hasDiagram,startNode(rel).speciesName, startNode(rel).schemaClass ])) as nodePairCollection";
//        Map<String, Object> map = new HashMap<>();
//        map.put("dbId", dbId);
//        return neo4jTemplate.query(query, map);
//    }
//
//    private Collection<HierarchyBranch> getLocationInPathwayBrowserForPathwaysRaw(List<?> pathways) {
//        String query = "" +
//                "MATCH (p:Pathway) " +
//                "WHERE p.dbId IN {dbIds} OR p.stId IN {stIds}" +
//                "OPTIONAL MATCH path=(:TopLevelPathway)-[:hasEvent*]->(p) " +
//                "WITH p, NODES(path) AS branch " +
//                "RETURN CASE WHEN SIZE(branch) > 0 THEN branch ELSE [p] END AS branch";
//        List<String> stIds = new ArrayList<>();
//        List<Long> dbIds = new ArrayList<>();
//        for (Object aux : pathways) {
//            if (aux instanceof DatabaseObject){
//                dbIds.add(((DatabaseObject) aux).getDbId());
//            } else {
//                String identifier = String.valueOf(aux);
//                if (DatabaseObjectUtils.isStId(identifier)) {
//                    stIds.add(identifier);
//                } else {
//                    dbIds.add(Long.valueOf(identifier));
//                }
//            }
//        }
//        Map<String, Object> params = new HashMap<>();
//        params.put("stIds", stIds);
//        params.put("dbIds", dbIds);
//        try {
//            return ados.getCustomQueryResults(HierarchyBranch.class, query, params);
//        } catch (CustomQueryException ex){
//            return null;
//        }
//    }
//
//    @Autowired
//    public void setNeo4jTemplate(Neo4jOperations neo4jTemplate) {
//        this.neo4jTemplate = neo4jTemplate;
//    }
//
//    @Autowired
//    public void setAdos(AdvancedDatabaseObjectService ados) {
//        this.ados = ados;
//    }
//}
