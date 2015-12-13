package uk.ac.ebi.reactome.domain.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class GO_BiologicalProcess extends DatabaseObject {

    private String accession;
    private String definition;
    @Relationship
    private ReferenceDatabase referenceDatabase;
    private String referenceDatabaseClass;

    public GO_BiologicalProcess() {
    }

    public String getAccession() {
        return this.accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public String getReferenceDatabaseClass() {
        return this.referenceDatabaseClass;
    }

    public void setReferenceDatabaseClass(String referenceDatabaseClass) {
        this.referenceDatabaseClass = referenceDatabaseClass;
    }

}