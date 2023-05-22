package fr.uca.iut.but2.tp8.annuaire;

import fr.uca.iut.but2.tp8.verification.Abonnement;
import fr.uca.iut.but2.tp8.verification.service.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class AnnuaireControlleurWeb {

    HashMap<String, OpenAPI> services = new HashMap<>();

    HashMap<String, ArrayList<String>> abonnements = new HashMap<>();

    @PostMapping("/senregistrer")
    public boolean enregistrement(@RequestBody Identification nouveauService) {
        System.out.println("enregistrement de " + nouveauService.getUrl());
        boolean accepte = !services.containsKey(nouveauService.getUrl());
        if (accepte) {
            services.put(nouveauService.getUrl(), nouveauService.getContrat());
        }
        return accepte;
    }

    @DeleteMapping("/seffacer/{url}")
    public boolean dereferrencement(@PathVariable String url) throws UnsupportedEncodingException {
        String serviceUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
        boolean accepte = services.containsKey(serviceUrl);

        if (accepte) {
            ArrayList<String> abonnes = abonnements.get(serviceUrl);
            if (abonnes != null) {
                abonnes.forEach(id -> {
                    WebClient.create(id).post().uri("/serviceeteint").retrieve().bodyToMono(String.class).subscribe();
                });
            }
            abonnements.remove(serviceUrl);
            abonnements.keySet().forEach(id -> {
                if (abonnements.get(id).contains(serviceUrl)) {
                    abonnements.get(id).remove(serviceUrl);
                }
            });

            services.remove(serviceUrl);
        }
        System.out.println("effacement de " + serviceUrl + " : " + accepte);
        return accepte;
    }

    @GetMapping("/services")
    public String[] listerLesService() {
        return services.keySet().toArray(new String[services.size()]);
    }

    @GetMapping("rechercher")
    public String[] rechercher(@RequestParam String chemin) {
        ArrayList<String> urls = new ArrayList<>();

        services.forEach((url, contrat) -> {
            if (contrat.getPaths().containsKey(chemin)) urls.add(url);
        });
        return urls.toArray(new String[urls.size()]);
    }

    @GetMapping("rechercher/multiples")
    public String[] rechercherMultiplesChemins(@RequestParam String[] chemins) {
        ArrayList<String> urls = new ArrayList<>();

        services.forEach((url, contrat) -> {
            boolean ajout = true;
            int i = 0;
            while (ajout && i < chemins.length) {
                ajout = ajout && contrat.getPaths().containsKey(chemins[i]);
                i++;
            }
            if (ajout) urls.add(url);
        });

        return urls.toArray(new String[urls.size()]);
    }

    @PostMapping("/sabonner")
    private void sabonner(@RequestBody Abonnement abo) {
        if (!abonnements.containsKey(abo.getService())) {
            abonnements.put(abo.getService(), new ArrayList<>());
        }
        abonnements.get(abo.getService()).add(abo.getId());
        //affichage de tous les abonnements
        abonnements.keySet().forEach(s -> {abonnements.get(s).forEach(a -> System.out.println(s+" -> "+a));});
    }

}
