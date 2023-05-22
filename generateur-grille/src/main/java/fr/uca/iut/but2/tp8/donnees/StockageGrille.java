package fr.uca.iut.but2.tp8.donnees;

import fr.uca.iut.but2.tp8.donnees.exception.GrilleNonTrouvee;
import fr.uca.iut.but2.tp8.donnees.exception.GrillePasEncorePrete;

import java.util.HashMap;
import java.util.UUID;

public class StockageGrille {
    HashMap<String, Grille> grillesMemorisees = new HashMap();

    public Grille obtenirUneGrille() {
        if (grillesMemorisees.size() > 0) return (Grille) grillesMemorisees.values().toArray()[0];
        else return new Grille();
    }

    public synchronized String ajouterGrille(Grille grille) {
        UUID uuid = UUID.randomUUID();
        while (grillesMemorisees.containsKey(uuid.toString())) {
            uuid = UUID.randomUUID();
        }
        grillesMemorisees.put(uuid.toString(), grille);
        System.out.println(uuid);
        return uuid.toString();
    }

    public Grille obtenirGrille(String uuid) throws GrilleNonTrouvee, GrillePasEncorePrete {
        if (grillesMemorisees.containsKey(uuid)) {
            Grille g = grillesMemorisees.get(uuid);
            if (g == null) throw new GrillePasEncorePrete();
            else return g;
        }
        else throw new GrilleNonTrouvee();
    }


    public synchronized String reserver() {
        return ajouterGrille(null);
    }

    public synchronized boolean liberer(String uuid) {
        if (grillesMemorisees.containsKey(uuid)) {
            Grille oldG = grillesMemorisees.get(uuid);
            if (oldG == null) {
                grillesMemorisees.remove(uuid);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean mettreAJour(String uuid, Grille g) {
        if (grillesMemorisees.containsKey(uuid)) {
            Grille oldG = grillesMemorisees.get(uuid);
            if (oldG == null) {
                grillesMemorisees.put(uuid, g);
                return true;
            }
        }
        return false;
    }

    public int nombreDeGrille() {
        return grillesMemorisees.size();
    }
}
