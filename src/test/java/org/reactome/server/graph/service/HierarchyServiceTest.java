package org.reactome.server.graph.service;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.graph.config.Neo4jConfig;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;
import org.reactome.server.graph.util.DatabaseObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 31.05.16.
 */
@ContextConfiguration(classes = {Neo4jConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class HierarchyServiceTest {

    private static final Logger logger = LoggerFactory.getLogger("testLogger");

    private static Boolean checkedOnce = false;
    private static Boolean isFit = false;

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private GeneralService generalService;

    @BeforeClass
    public static void setUpClass() {
        logger.info(" --- !!! Running DatabaseObjectServiceTests !!! --- \n");
    }

    @AfterClass
    public static void tearDownClass() {
        logger.info("\n\n");
    }

    @Before
    public void setUp() throws Exception {
        if (!checkedOnce) {
            isFit = generalService.fitForService();
            checkedOnce = true;
        }
        assumeTrue(isFit);
        generalService.clearCache();
        DatabaseObjectFactory.clearCache();
    }

    @Test
    public void getLocationsInPathwayBrowserTest() {

        logger.info("Started testing detailsService.getLocationsInPathwayBrowserTest");
        long start, time;
        start = System.currentTimeMillis();
        PathwayBrowserNode node = hierarchyService.getLocationsInPathwayBrowser("R-HSA-5205630");
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");

        assertEquals(3, node.getChildren().size());
        logger.info("Finished");
    }

    @Test
    public void getLocationsInThePathwayBrowserForInteractorsTest() {

        logger.info("Started testing detailsService.getLocationsInThePathwayBrowserForInteractorsTest");
        long start, time;
        start = System.currentTimeMillis();
        PathwayBrowserNode node = hierarchyService.getLocationsInPathwayBrowserForInteractors("R-HSA-5205630");
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");

        assertEquals(2, node.getChildren().size());
        logger.info("Finished");
    }


    // --------------------------------------------- Sub Hierarchy -----------------------------------------------------

    @Test
    public void getSubHierarchyTest() {
        logger.info("Started testing eventService.findByDbId");
        long start, time;
        start = System.currentTimeMillis();
        PathwayBrowserNode subHierarchy = hierarchyService.getSubHierarchy("R-HSA-109581");
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");

        assertEquals(4, subHierarchy.getChildren().size());
        logger.info("Finished");
    }

    // ------------------------------------------- Event Hierarchy -----------------------------------------------------

    @Test
    public void getEventHierarchyBySpeciesNameTest() {
        logger.info("Started testing eventService.getEventHierarchyBySpeciesNameTest");
        long start, time;
        start = System.currentTimeMillis();
        Collection<PathwayBrowserNode> eventHierarchy = hierarchyService.getEventHierarchyBySpeciesName("Homo sapiens");
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");

        assertEquals(24, eventHierarchy.size());
        logger.info("Finished");
    }

    @Test
    public void getEventHierarchyByTaxIdTest() {
        logger.info("Started testing eventService.getEventHierarchyByTaxIdTest");
        long start, time;
        start = System.currentTimeMillis();
        Collection<PathwayBrowserNode> eventHierarchy = hierarchyService.getEventHierarchyByTaxId("9606");
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");

        assertEquals(24, eventHierarchy.size());
        logger.info("Finished");
    }

    @Test
    public void getEventHierarchyByDbId() {
        logger.info("Started testing eventService.getEventHierarchyByDbId");
        long start, time;
        start = System.currentTimeMillis();
        Collection<PathwayBrowserNode> eventHierarchy = hierarchyService.getEventHierarchyByDbId(48887L);
        time = System.currentTimeMillis() - start;
        logger.info("GraphDb execution time: " + time + "ms");

        assertEquals(24, eventHierarchy.size());
        logger.info("Finished");
    }

}