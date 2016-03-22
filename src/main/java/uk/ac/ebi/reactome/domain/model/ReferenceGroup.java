package uk.ac.ebi.reactome.domain.model;

import org.neo4j.ogm.annotation.NodeEntity;
import uk.ac.ebi.reactome.domain.annotations.ReactomeProperty;

@SuppressWarnings("unused")
@NodeEntity
public class ReferenceGroup extends ReferenceEntity {

    @ReactomeProperty
    private String atomicConnectivity;
    @ReactomeProperty
    private String formula;

    public ReferenceGroup() {}

    public String getAtomicConnectivity() {
        return atomicConnectivity;
    }

    public void setAtomicConnectivity(String atomicConnectivity) {
        this.atomicConnectivity = atomicConnectivity;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

}
