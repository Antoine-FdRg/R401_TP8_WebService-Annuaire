package fr.uca.iut.but2.tp8.requete;

import org.springframework.web.reactive.function.client.WebClient;

public class RequetesMotsSync implements RequetesMots {


    private WebClient webClient;

    public RequetesMotsSync() {
        this.webClient = WebClient.create("http://localhost:8081");
    }

    public String[] languesDiponibles() {
        return webClient.get().uri("/langues").retrieve().bodyToMono(String[].class).block();
    }

    public String obtenirUnMot(String langue, int longueur) {
        return webClient.get().uri("/"+langue+"/unmot/longueur/"+longueur).retrieve().bodyToMono(String.class).block();
    }

    public void setUrl(String url) {
        webClient = WebClient.create(url);
    }
}
