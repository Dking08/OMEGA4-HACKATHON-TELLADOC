// Disease.java
package com.dking.telladoc.essentials;

public class Disease {
    private String diseaseName, Type, Descr;
    private String since;

    public Disease() {
    }

    public Disease(String Type, String diseaseName, String Desc, String since) {
        this.Type = Type;
        this.diseaseName = diseaseName;
        this.Descr = Desc;
        this.since = since;
    }

    public String getDiseaseName() {
        return diseaseName;
    }


    public String getDiseaseDesc() {
        return Descr;
    }
    public String getDiseaseType() {
        return Type;
    }
    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }
}
