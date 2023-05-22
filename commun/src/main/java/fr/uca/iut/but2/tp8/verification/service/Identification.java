package fr.uca.iut.but2.tp8.verification.service;

import io.swagger.v3.oas.models.OpenAPI;

public class Identification {

    String url ="";
    OpenAPI contrat = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OpenAPI getContrat() {
        return contrat;
    }

    public void setContrat(OpenAPI contrat) {
        this.contrat = contrat;
    }

    public Identification() {

    }

    public Identification(String url, OpenAPI contrat) {
        setContrat(contrat);
        setUrl(url);
    }


}
