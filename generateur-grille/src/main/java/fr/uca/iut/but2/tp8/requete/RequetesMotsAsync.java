package fr.uca.iut.but2.tp8.requete;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class RequetesMotsAsync {


    private WebClient webClient;

    public RequetesMotsAsync() {
        this.webClient = WebClient.create("http://localhost:8081");
    }

    public Mono<String[]> languesDiponibles() {
        return webClient.get().uri("/langues").retrieve().bodyToMono(String[].class);
    }


    public Mono<String> obtenirUnMot(String langue, int longueur) {
        return webClient.get().uri("/"+langue+"/unmot/longueur/"+longueur).retrieve().bodyToMono(String.class);
    }

    public void setUrl(String url) {
        webClient = WebClient.create(url);
    }
}
