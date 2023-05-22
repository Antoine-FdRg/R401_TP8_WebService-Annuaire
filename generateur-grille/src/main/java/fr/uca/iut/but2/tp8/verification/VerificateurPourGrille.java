package fr.uca.iut.but2.tp8.verification;


import fr.uca.iut.but2.tp8.requete.RequetesMots;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class VerificateurPourGrille extends Verificateur {

    private boolean containsFr = false;
    private String serviceAvecFrUrl = "";


    public boolean estPret() {
        return isContratPret() && containsFr;
    }

    public void verifierLangue(RequetesMots requetesMots) {
        verifierLangueRecursif(requetesMots, 20);
    }


    private void verifierLangueRecursif(RequetesMots requetesMots, int nbTentativeRestante) {
        String[] cheminsVoulus = {"/langues", "/{langue}/unmot/longueur/{taille}"};
        String[] pathVariables = {"{langue}", "{taille}"};
        rechercheService(cheminsVoulus, pathVariables).subscribe(
                services ->
                        new Thread(() -> {
                            System.out.println(Arrays.toString(services));
                            int i = 0;
                            containsFr = false;
                            while (!containsFr && (i < services.length)) {
                                System.out.println("verifierLangueRecursif " + i + " / " + Thread.currentThread().getName());
                                requetesMots.setUrl(services[i]);
                                System.out.println("lancement de de la verification de la langue dans " + services[i]);
                                String[] langues = requetesMots.languesDiponibles();
                                if (langues.length > 0) containsFr = Arrays.stream(langues).anyMatch("fr"::equals);
                                if (containsFr) {
                                    System.out.println("on retient " + services[i]);
                                    serviceAvecFrUrl = services[i];
                                    sAbonner(serviceAvecFrUrl);
                                }

                                i++;
                            }
                            prevenirEcouteurs();
                            if (!containsFr && (nbTentativeRestante > 0)) {
                                try {
                                    TimeUnit.SECONDS.sleep(2);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                verifierLangueRecursif(requetesMots, nbTentativeRestante - 1);
                            } else if (!containsFr) {
                                System.err.println("ON N'A PAS TROUVE DE SERVICE POUR LES MOTS");
                                // que faire dans ce cas ? on relance dans plus longtemps ? on se coupe ? ...
                            }
                        }).start()
        );
    }

    public String getServiceAvecFrUrl() {
        return serviceAvecFrUrl;
    }



    public boolean retirerService(String url) {
        boolean retour = false;
        if (url != null) retour = url.equals(getServiceAvecFrUrl());
        return retour;
    }

}
