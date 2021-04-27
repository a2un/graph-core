//package org.reactome.server.graph.service;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.reactome.server.graph.domain.model.DatabaseObject;
//import org.reactome.server.graph.domain.result.DiagramOccurrences;
//import org.reactome.server.graph.domain.result.DiagramResult;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Collection;
//
//import static org.junit.Assert.*;
//
///**

// */
//public class DiagramServiceTest extends BaseTest {
//
//    @Autowired
//    private DiagramService diagramService;
//
//    @BeforeClass
//    public static void setUpClass() {
//        logger.info(" --- !!! Running " + DiagramServiceTest.class.getName() + " !!! --- \n");
//    }
//
//    @Test
//    public void getDiagramResultTest() {
//        logger.info("Started testing diagramService.getDiagramTest");
//        long start = System.currentTimeMillis();
//        DiagramResult dr1 = diagramService.getDiagramResult("R-HSA-6799198");
//        DiagramResult dr2 = diagramService.getDiagramResult(6799198L);
//        long time = System.currentTimeMillis() - start;
//        logger.info("GraphDb execution time: " + time + "ms");
//
//        assertNotNull("The diagram result object cannot be null", dr1);
//        assertTrue("The diagram containing 'R-HSA-6799198' is 'R-HSA-163200'", dr1.getDiagramStId().equals("R-HSA-163200"));
//        assertTrue("", dr1.getEvents().size() == 13 && dr1.getEvents().contains("R-HSA-5690023"));
//
//        assertNotNull("The diagram result object cannot be null", dr2);
//        assertTrue("The diagram containing 'R-HSA-6799198' is 'R-HSA-163200'", dr2.getDiagramStId().equals("R-HSA-163200"));
//        assertTrue("", dr2.getEvents().size() == 13 && dr2.getEvents().contains("R-HSA-5690023"));
//    }
//
//    @Test
//    public void getDiagramOccurrencesTest1(){
//        logger.info("Started testing diagramService.getDiagramOccurrences1");
//        long start = System.currentTimeMillis();
//        Collection<DiagramOccurrences> dos = diagramService.getDiagramOccurrences("R-HSA-5690771");
//        long time = System.currentTimeMillis() - start;
//        logger.info("GraphDb execution time: " + time + "ms");
//
//        assertNotNull("The diagram occurrences result object cannot be null", dos);
//        assertTrue("There are at least 6 occurrences of 'R-HSA-5690771'", dos.size() >= 6);
//
//        for (DiagramOccurrences o : dos) {
//            assertNotNull(o.getDiagram());
//            if(o.getDiagram().getStId().equals("R-HSA-5688426")){
//                assertTrue("'R-HSA-5690771' is directly contained in 'R-HSA-5688426'", o.isInDiagram());
//            }
//            if(o.getDiagram().getSchemaClass().equals("TopLevelPathway")){
//                assertNotNull(o.getOccurrences());
//                assertTrue(!o.getOccurrences().isEmpty());
//            }
//        }
//    }
//
//    @Test
//    public void getDiagramOccurrencesTest2(){
//        logger.info("Started testing diagramService.getDiagramOccurrences2");
//        long start = System.currentTimeMillis();
//        Collection<DiagramOccurrences> dos = diagramService.getDiagramOccurrences("R-HSA-372542");
//        long time = System.currentTimeMillis() - start;
//        logger.info("GraphDb execution time: " + time + "ms");
//
//        assertNotNull("The diagram occurrences result object cannot be null", dos);
//        assertTrue("There are at least 2 occurrences of 'R-HSA-372542'", dos.size() >= 2);
//
//        for (DiagramOccurrences o : dos) {
//            assertNotNull(o.getDiagram());
//            if(o.getDiagram().getStId().equals("R-HSA-112310")){
//                assertTrue("'R-HSA-372542' is directly contained in 'R-HSA-112310'", o.isInDiagram());
//            }
//            if(o.getDiagram().getSchemaClass().equals("TopLevelPathway")){
//                assertNotNull(o.getOccurrences());
//                assertTrue(!o.getOccurrences().isEmpty());
//            }
//        }
//    }
//
//    @Test
//    public void getDiagramOccurrencesTest3(){
//        logger.info("Started testing diagramService.getDiagramOccurrencesTest3");
//        long start = System.currentTimeMillis();
//        Collection<DiagramOccurrences> dos = diagramService.getDiagramOccurrences("R-HSA-879382");
//        long time = System.currentTimeMillis() - start;
//        logger.info("GraphDb execution time: " + time + "ms");
//
//        assertNotNull("The diagram occurrences result object cannot be null", dos);
//        assertTrue("There are at least 20 occurrences of 'R-HSA-879382'", dos.size() >= 20);
//
//        for (DiagramOccurrences o : dos) {
//            assertNotNull(o.getDiagram());
//            if(o.getDiagram().getStId().equals("R-HSA-168164")){
//                assertFalse("'R-HSA-879382' is not directly contained in 'R-HSA-168164'", o.isInDiagram());
//                assertFalse("", o.getOccurrences().isEmpty());
//                boolean found = false;
//                for (DatabaseObject dbObj : o.getOccurrences()) {
//                    found |= (dbObj.getStId().equals("R-HSA-445989"));
//                }
//                assertTrue("'R-HSA-879382' is contained in 'R-HSA-445989'",found);
//            }if(o.getDiagram().getStId().equals("R-HSA-168928")){
//                assertTrue("'R-HSA-879382' not directly contained in 'R-HSA-168928'", o.isInDiagram());
//                assertTrue("", o.getOccurrences().isEmpty());
//            }
//            if(o.getDiagram().getSchemaClass().equals("TopLevelPathway")){
//                assertNotNull(o.getOccurrences());
//                assertTrue(!o.getOccurrences().isEmpty());
//            }
//        }
//    }
//}
