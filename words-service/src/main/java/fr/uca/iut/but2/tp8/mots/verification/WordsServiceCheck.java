package fr.uca.iut.but2.tp8.mots.verification;


import fr.uca.iut.but2.tp8.verification.Verificateur;
import org.springframework.stereotype.Component;

@Component
public class WordsServiceCheck extends Verificateur {


    @Override
    public boolean estPret() {
        return isContratPret();
    }

}
