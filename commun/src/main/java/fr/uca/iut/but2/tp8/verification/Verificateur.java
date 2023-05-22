package fr.uca.iut.but2.tp8.verification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.iut.but2.tp8.verification.service.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public abstract class Verificateur {

    Object synchro = new Object();
    @Autowired
    private Environment environment;
    private boolean contratPret = false;
    private WebClient webClient = WebClient.create("http://localhost:8080/");
    private ArrayList<AttenteVerification> ecouteurs = new ArrayList<>();
    private OpenAPI contrat = null;

    protected Environment getEnvironment() {
        return environment;
    }

    protected void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected boolean isContratPret() {
        return contratPret;
    }

    protected void setContratPret(boolean contratPret) {
        this.contratPret = contratPret;
    }

    protected OpenAPI getContrat() {
        return contrat;
    }

    protected void setContrat(OpenAPI contrat) {
        this.contrat = contrat;
    }



    public abstract boolean estPret();


    public void ajouterAttenteVerification(AttenteVerification enAttente) {
        this.ecouteurs.add(enAttente);
    }

    protected void prevenirEcouteurs() {
        for(AttenteVerification a : ecouteurs) a.changementEtatVerification(estPret());
    }


    public void verifierContrat() {
        try {
            obtenirLeContrat(5);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    protected String obtenirSaPropreAdresse() throws UnknownHostException {
        final String port = getEnvironment().getProperty("local.server.port") == null ? "80" : getEnvironment().getProperty("local.server.port") ;
        final String ip = InetAddress.getLocalHost().getHostAddress();

        return "http://"+ip+":"+port+"/";
    }

    private void obtenirLeContrat(int tentativeRestante) throws UnknownHostException {
        final String monUrl = obtenirSaPropreAdresse();



        webClient.get().uri(monUrl+"v3/api-docs").retrieve().bodyToMono(String.class).onErrorResume(err -> {
            System.out.println(err);
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (tentativeRestante > 0) {
                try {
                    obtenirLeContrat(tentativeRestante - 1);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                setContratPret(false);
                System.err.println("=====> obtenirLeContrat =====> echec" );
                prevenirEcouteurs();
            }
            return Mono.empty();
        }).subscribe(json -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                setContrat(mapper.readValue(json, OpenAPI.class));
                io.swagger.v3.oas.models.Paths paths =  getContrat().getPaths();

                Identification id = new Identification(monUrl, getContrat());
                webClient.post().uri("/senregistrer").accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(id)).retrieve().bodyToMono(Boolean.class).subscribe(
                        accepte -> {
                            System.out.println("on a été accepté !");
                            setContratPret(true);
                            synchronized (synchro) { synchro.notify(); }
                            prevenirEcouteurs();
                        });

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }



    protected void sAbonner(String serviceUrl) {
        while (! isContratPret()) {
            synchronized (synchro) {
                try {
                    synchro.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        System.out.println("on s'abonne à " + serviceUrl);
        try {
            Abonnement abonnement = new Abonnement(this.obtenirSaPropreAdresse(), serviceUrl);
            webClient.post().uri("/sabonner").accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(abonnement)).retrieve().bodyToMono(String.class).subscribe();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }



    protected Mono<String[]> rechercheService(String... chemins) {
        // return webClient.get().uri("/rechercher/multiples", uri -> uri.queryParam("chemins", String.join(",", chemins)).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String[].class);
        return webClient.get().uri("/rechercher/multiples", uri -> uri.queryParam("chemins", chemins).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String[].class);
    }

    protected Mono<String[]> rechercheService(String[] chemins, String[] pathVariables) {
        // return webClient.get().uri("/rechercher/multiples", uri -> uri.queryParam("chemins", String.join(",", chemins)).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String[].class);
        return webClient.get().uri("/rechercher/multiples", uri -> uri.queryParam("chemins", chemins).build(pathVariables)).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String[].class);
    }


    @PreDestroy
    public void fin() throws UnknownHostException {
        if (getContrat() != null) {
            String monUrl = obtenirSaPropreAdresse();
            try {
                monUrl = URLEncoder.encode(monUrl, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            System.out.println("on se désenregistre de l'annuaire");
            webClient.delete().uri("/seffacer/"+monUrl).retrieve().bodyToMono(Boolean.class).block();
        }
    }

}
