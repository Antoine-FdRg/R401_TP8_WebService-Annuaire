package fr.uca.iut.but2.tp8.requete;

public class RequetesMotsAdapteur implements RequetesMots{

    class ResultatLangues {
        String[] langues = null;
    }

    class ResultatMot {
        String mot = null;
    }

    RequetesMotsAsync requetes = new RequetesMotsAsync();

    @Override
    public String[] languesDiponibles() {
        final ResultatLangues resultat = new ResultatLangues();
        Object synchro = new Object();

        requetes.languesDiponibles().subscribe(langues -> {
            System.out.println("languesDiponibles subscribe "+langues.length);

            resultat.langues = langues;
           synchronized (synchro) {
               synchro.notifyAll();
           }
        });

        System.out.println("languesDiponibles attentes");

        synchronized (synchro) {
            try {
                synchro.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return resultat.langues;
    }

    @Override
    public String obtenirUnMot(String langue, int longueur) {
        final ResultatMot resultat = new ResultatMot();
        Object synchro = new Object();

        requetes.obtenirUnMot(langue, longueur).subscribe(mot -> {
            resultat.mot = mot;
            synchronized (synchro) {
                synchro.notifyAll();
            }
        });

        synchronized (synchro) {
            try {
                synchro.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        return resultat.mot;
    }

    @Override
    public void setUrl(String url) {
        requetes.setUrl(url);
    }
}
