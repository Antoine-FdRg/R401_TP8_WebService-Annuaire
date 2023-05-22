package fr.uca.iut.but2.tp8;

import fr.uca.iut.but2.tp8.donnees.Grille;
import fr.uca.iut.but2.tp8.donnees.StockageGrille;
import fr.uca.iut.but2.tp8.requete.RequetesMots;

import java.util.Arrays;

public class TravailleurGenerateurGrille implements Runnable {
    private final String langue;
    private final int longueur;
    private final int hauteur;
    private final StockageGrille stockageGrille;
    private final RequetesMots requetesMots;
    private final int nbEchecMax;

    private String uuid = "";
    Thread thread = null ;

    long timing ;

    public TravailleurGenerateurGrille(String langue, int longueur, int hauteur, StockageGrille stock, RequetesMots requetesMots, int nbEchecMax) {
        this.langue = langue;
        this.longueur = longueur;
        this.hauteur=  hauteur;
        this.stockageGrille = stock;
        this.requetesMots = requetesMots;
        this.nbEchecMax = nbEchecMax;
    }

    public String genereEnTacheDeFond() {
        uuid = stockageGrille.reserver();
        timing = System.currentTimeMillis();
        if (thread == null) {
             thread = new Thread(this);
             thread.start();
        }
        return uuid;
    }

    @Override
    public void run() {
        Grille grille = null;
        String[] langues = requetesMots.languesDiponibles();
        boolean containsFr = Arrays.stream(langues).anyMatch(langue::equals);
        if (containsFr) {
            GenerateurGrille generateurGrille = new GenerateurGrille(langue, requetesMots, longueur, hauteur);
            grille = generateurGrille.generer(nbEchecMax);
            stockageGrille.mettreAJour(uuid, grille);
            System.out.println("temps mis pour générer la grille : "+(System.currentTimeMillis()-timing));
            System.out.println(grille);
        } else {
            stockageGrille.liberer(uuid);
        }

    }
}
