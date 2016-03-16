package uk.ac.ebi.reactome.qualityassurance.tests;

import uk.ac.ebi.reactome.qualityassurance.QATest;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 07.03.16.
 */
@SuppressWarnings("unused")
@QATest
public class QualityAssuranceTest010 extends QualityAssuranceAbstract {

    @Override
    String getName() {
        return "QATest010-EntitySetWithoutMemberOrCandidate";
    }

    @Override
    String getQuery() {
        return "Match (n:EntitySet)<-[:created]-(a) Where NOT (n)-[:hasMember|hasCandidate]->() RETURN n.dbId AS dbId, " +
                "n.stableIdentifier AS stId, n.displayName AS name, a.displayName as author";

    }
}