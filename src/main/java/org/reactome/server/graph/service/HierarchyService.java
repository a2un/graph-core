//package org.reactome.server.graph.service;
//
//import org.apache.commons.lang3.StringUtils;
//import org.reactome.server.graph.repository.HierarchyRepository;
//import org.reactome.server.graph.service.helper.PathwayBrowserNode;
//import org.reactome.server.graph.service.util.DatabaseObjectUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Set;
//
///**
// * Created by:
// *
// * @author Florian Korninger (florian.korninger@ebi.ac.uk)
// * @since 31.05.16.
// */
//@Service
//@SuppressWarnings("WeakerAccess")
//public class HierarchyService {
//
//    @Autowired
//    private HierarchyRepository hierarchyRepository;
//
//    // -------------------------------- Locations in the Pathway Browser -----------------------------------------------
//
//    public PathwayBrowserNode getLocationsInPathwayBrowser(Object identifier, Boolean showDirectParticipants, Boolean omitNonDisplayableItems) {
//
//        if (omitNonDisplayableItems == null) omitNonDisplayableItems = true;
//        if (showDirectParticipants == null) showDirectParticipants = false;
//        String id = DatabaseObjectUtils.getIdentifier(identifier);
//        if (DatabaseObjectUtils.isStId(id)) {
//            if (showDirectParticipants) {
//                return hierarchyRepository.getLocationsInPathwayBrowserDirectParticipants(id, omitNonDisplayableItems);
//            }
//            return hierarchyRepository.getLocationsInPathwayBrowser(id, omitNonDisplayableItems);
//        } else if (DatabaseObjectUtils.isDbId(id)){
//            if (showDirectParticipants) {
//                return hierarchyRepository.getLocationsInPathwayBrowserDirectParticipants(Long.parseLong(id), omitNonDisplayableItems);
//            }
//            return hierarchyRepository.getLocationsInPathwayBrowser(Long.parseLong(id), omitNonDisplayableItems);
//        }
//        return null;
//    }
//
//    public Set<PathwayBrowserNode> getLocationInPathwayBrowserForPathways(List<?> pathways){
//        return hierarchyRepository.getLocationInPathwayBrowserForPathways(pathways);
//    }
//
//    // --------------------------------------------- Sub Hierarchy -----------------------------------------------------
//
//    public PathwayBrowserNode getSubHierarchy(Object identifier) {
//
//        String id = DatabaseObjectUtils.getIdentifier(identifier);
//        if (DatabaseObjectUtils.isStId(id)) {
//            return hierarchyRepository.getSubHierarchy(id);
//        } else if (DatabaseObjectUtils.isDbId(id)){
//            return hierarchyRepository.getSubHierarchy(Long.parseLong(id));
//        }
//        return null;
//    }
//
//    // ------------------------------------------- Event Hierarchy -----------------------------------------------------
//
//    public Collection<PathwayBrowserNode> getEventHierarchy(Object species) {
//        String speciesString = species.toString();
//        if (StringUtils.isNumeric(speciesString)) {
//            return hierarchyRepository.getEventHierarchyByTaxId(speciesString);
//        } else {
//            return hierarchyRepository.getEventHierarchyBySpeciesName(speciesString);
//        }
//    }
//}
