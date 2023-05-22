package fr.uca.iut.but2.tp8;

import fr.uca.iut.but2.tp8.donnees.Grille;
import fr.uca.iut.but2.tp8.donnees.StockageGrille;
import fr.uca.iut.but2.tp8.exception.ServeurPasPret;
import fr.uca.iut.but2.tp8.requete.RequetesMots;
import fr.uca.iut.but2.tp8.requete.RequetesMotsAdapteur;
import fr.uca.iut.but2.tp8.verification.AttenteVerification;
import fr.uca.iut.but2.tp8.verification.VerificateurPourGrille;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class GrilleRestController  extends BaseWebController implements AttenteVerification{

    RequetesMots requetesMots;

    StockageGrille stockageGrille;
    private boolean traitementPossible = false;


    VerificateurPourGrille verificateurPourGrille;


    public GrilleRestController(VerificateurPourGrille verificateurPourGrille) {
        this.verificateurPourGrille = verificateurPourGrille;
        stockageGrille = new StockageGrille();
        requetesMots = new RequetesMotsAdapteur();
    }


    @Override
    public void changementEtatVerification(boolean pret) {
        this.traitementPossible = pret;
        if (pret) {

            Grille grille;
            int l = 10;
            int h = 10;

            if (stockageGrille.nombreDeGrille() == 0) {
                GenerateurGrille generateurGrille = new GenerateurGrille("fr", requetesMots, l, h);
                grille = generateurGrille.generer(40);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> grille <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println(grille);

                stockageGrille.ajouterGrille(grille);
            }

        }
    }


    @PostConstruct
    public void finInit() {
        verificateurPourGrille.ajouterAttenteVerification(this);

        verificateurPourGrille.verifierLangue(requetesMots);
        verificateurPourGrille.verifierContrat();
    }


    /**
     * chemin /unegrille +numéro de la question
     */
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "retourne une grille mémorisée"),
            @ApiResponse(responseCode = "503", description = "il n'y a pas de service pour obtenir des mots en français ou le contrat n'est pas pret")})
    @GetMapping("/unegrille-3b")
    public Grille uneGrille3b(@RequestParam(required = false) Integer longueur, @RequestParam(required = false) Integer hauteur) {
        if (traitementPossible) return stockageGrille.obtenirUneGrille();
        else throw new ServeurPasPret();
    }


    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "grille disponible"),
            @ApiResponse(responseCode = "206", description = "grille en cours de génération"),
            @ApiResponse(responseCode = "503", description = "serveur pas pret"),
            @ApiResponse(responseCode = "404", description = "grille inconnue")})
    @GetMapping("/grilles/{uuid}")
    public Grille grilleViaUUID(@PathVariable String uuid) {
        if (traitementPossible) return stockageGrille.obtenirGrille(uuid);
        else throw new ServeurPasPret();
    }

    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "retourne un uuid pou une future grille mémorisée ou un message d'erreur pour taille non convenable"),
            @ApiResponse(responseCode = "503", description = "il n'y a pas de service pour obtenir des mots en français ou le contrat n'est pas pret")})
    @GetMapping("/grilles/nouvelle")
    public String commanderGrille(@RequestParam String langue, @RequestParam int longueur, @RequestParam int hauteur, @RequestParam(defaultValue = "1000") int nbEchecMax) {
        if (traitementPossible) {
            if ((longueur > 2) && (hauteur > 2)) {
                TravailleurGenerateurGrille travailleur = new TravailleurGenerateurGrille(langue, longueur, hauteur, stockageGrille, requetesMots, nbEchecMax);
                return travailleur.genereEnTacheDeFond();
            } else return "taille insuffisante";
        } else throw new ServeurPasPret();
    }


    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "retourne une grille générée pour l'occasion"),
            @ApiResponse(responseCode = "503", description = "il n'y a pas de service pour obtenir des mots en français ou le contrat n'est pas pret")})
    @GetMapping("/unegrille-4c")
    public Grille uneGrille4c(@RequestParam(required = false) Integer longueur, @RequestParam(required = false) Integer hauteur) {
        if (traitementPossible) {
            long timing = System.currentTimeMillis();
            Grille grille = new Grille();
            int l = 10;
            int h = 10;
            if ((longueur != null) && (longueur > 0)) {
                l = longueur;
            }
            if ((hauteur != null) && (hauteur > 0)) {
                h = hauteur;
            }

            GenerateurGrille generateurGrille = new GenerateurGrille("fr", requetesMots, l, h);
            grille = generateurGrille.generer(40);

            System.out.println("timing : " + (System.currentTimeMillis() - timing));

            return grille;
        } else throw new ServeurPasPret();
    }

    @Override
    public void serviceQuiDisparait() {
        System.out.println("service qui disparait");
        traitementPossible = false;
        verificateurPourGrille.verifierLangue(requetesMots);
        verificateurPourGrille.verifierContrat();
    }
}
