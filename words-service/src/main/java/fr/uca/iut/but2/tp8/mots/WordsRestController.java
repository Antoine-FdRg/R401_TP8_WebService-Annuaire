package fr.uca.iut.but2.tp8.mots;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.iut.but2.tp8.exception.ServeurPasPret;
import fr.uca.iut.but2.tp8.mots.dico.Dictionnary;
import fr.uca.iut.but2.tp8.mots.dico.DicoImportExport;
import fr.uca.iut.but2.tp8.mots.verification.WordsServiceCheck;
import jakarta.annotation.PostConstruct;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

@RestController
public class WordsRestController {

    SecureRandom rand = new SecureRandom();

    @Autowired
    WordsServiceCheck wordsServiceCheck;

    @PostConstruct
    public void finInit() throws UnknownHostException {
        wordsServiceCheck.verifierContrat();
    }

    @GetMapping("/ping")
    public String ping() {
        if (wordsServiceCheck.estPret()) return "I'm alive";
        else throw new ServeurPasPret();
    }

    @GetMapping("/langues")
    public ArrayList<String> listeDesLangues() {
        if (wordsServiceCheck.estPret()) {
            ArrayList<String> liste = new ArrayList<>();
            File langues = new File("langues");
            if (langues.exists() && langues.isDirectory()) {
                File[] fichiers = langues.listFiles();
                for (File f : fichiers) {
                    if (f.isDirectory()) {
                        File dico = new File(f.getPath() + "/anagrammes.json");
                        if (dico.exists()) liste.add(f.getName());
                    }
                }
            }
            return liste;
        } else throw new ServeurPasPret();
    }


    @PostMapping("/ajouterLangue")
    public Mono<String> addLanguage(@RequestPart("fichierLangue") FilePart filePart, @RequestPart("langue") String langue) {
        // note : FilePart pourrait aussi être un flux ou un mono (plutôt un mono, un flux voudrait dire plusieurs fichiers)
        if (wordsServiceCheck.estPret()) {
            // quelques vérifications sur le dossier "langues"
            File langDir = new File("langues");
            if (!langDir.exists()) {
                try {
                    Files.createDirectories(Paths.get("langues/"));
                } catch (IOException e) {
                    return Mono.just("problème serveur : on ne peut pas créer le dossier langues");
                }
            }

            if (!langDir.isDirectory()) return Mono.just("problème serveur : un dossier interne n'est pas un dossier");

            // on gère la langue
            langDir = new File("langues/" + langue);
            if (langDir.exists()) {
                if (!langDir.isDirectory())
                    return Mono.just("problème serveur : la langue spécifiée pose soucis (le dossier existe mais ce n'est pas un dossier)");
                File json = new File("langues/" + langue + "/anagrammes.json");
                if (json.exists())
                    return Mono.just("la langue spécifiée pose soucis (le dossier et le fichier json existent déjà)");
            } else {
                try {
                    Files.createDirectories(Paths.get("langues/" + langue));
                } catch (IOException e) {
                    return Mono.just(Arrays.toString(e.getStackTrace()));
                }
            }

            // on récupère le fichier
            Mono<InputStream> monoInputStream = filePart.content().map(dataBuffer -> dataBuffer.asInputStream(true)).reduce(SequenceInputStream::new);
            return monoInputStream.flatMap(is -> {
                DicoImportExport importText = new DicoImportExport();
                try {
                    importText.load(is);
                    is.close();
                } catch (IOException e) {
                    Mono.error(e);
                }
                importText.export("langues/" + langue + "/anagrammes.json");
                return Mono.just("le fichier a été converti en json");
            });
        } else throw new ServeurPasPret();
    }


    @GetMapping("/{langue}/anagrammes/{mot}")
    public HashSet<String> annagrams(@PathVariable("langue") String langue, @PathVariable("mot") String mot) {
        if (wordsServiceCheck.estPret()) {
            String vérification = checkFiles(langue);

            HashSet<String> possibleAnagrams = new HashSet<>();
            if (vérification.equals("")) {
                ObjectMapper mapper = new ObjectMapper();
                Dictionnary dico = null;
                try {
                    // c'est le code de l'exemple
                    dico = mapper.readValue(new File("langues/" + langue + "/anagrammes.json"), Dictionnary.class);
                    String key = dico.normalize(mot.trim());

                    // on a extrait cette partie pour l'appeler depuis anagrammes( avec jockers) pour gagner les vérifications...
                    // et le chargement du json
                    lookingForAnagrams(key, dico, possibleAnagrams);


                } catch (IOException e) {
                    return possibleAnagrams;
                }
            }

            return possibleAnagrams;
        } else throw new ServeurPasPret();
    }


    @GetMapping("/{langue}/anagrammes/{mot}/jocker/{nb:[1|2]}")
    public HashSet<String> annagrams(@PathVariable("langue") String langue, @PathVariable("mot") String mot, @PathVariable("nb") int nb) {
        if (wordsServiceCheck.estPret()) {
            String vérification = checkFiles(langue);
            HashSet<String> possibleAnagrams = new HashSet<>();
            if (vérification.equals("")) {
                ObjectMapper mapper = new ObjectMapper();
                Dictionnary dico = null;
                try {
                    // c'est le code de l'exemple
                    dico = mapper.readValue(new File("langues/" + langue + "/anagrammes.json"), Dictionnary.class);
                    String key = dico.normalize(mot.trim());
                    key = dico.anagram(key);

                    String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "j", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

                    for (int i = 0; i < alphabet.length; i++) {
                        String motexplore = key + alphabet[i];
                        if (nb == 1) {
                            lookingForAnagrams(motexplore, dico, possibleAnagrams);

                        } else if (nb == 2) {
                            for (int j = 0; j < alphabet.length; j++) {
                                String motexplore2jockers = motexplore + alphabet[i];
                                lookingForAnagrams(motexplore2jockers, dico, possibleAnagrams);
                            }
                        }
                    }
                } catch (IOException e) {
                    return possibleAnagrams;
                }
            }

            return possibleAnagrams;
        } else throw new ServeurPasPret();
    }


    @GetMapping("/{langue}/unmot")
    public String oneWord(@PathVariable("langue") String langue) {
        if (wordsServiceCheck.estPret()) {
            String vérification = checkFiles(langue);
            String mot = "";
            if (vérification.equals("")) {
                ObjectMapper mapper = new ObjectMapper();
                Dictionnary dico = null;
                try {
                    // c'est le code de l'exemple
                    dico = mapper.readValue(new File("langues/" + langue + "/anagrammes.json"), Dictionnary.class);
                    Object[] keys = dico.getDictionnary().keySet().toArray();
                    Object key = keys[rand.nextInt(keys.length)];
                    ArrayList<String> liste = dico.getDictionnary().get(key);
                    mot = liste.get(rand.nextInt(liste.size()));
                } catch (IOException e) {
                    return mot;
                }
            }
            return mot;
        } else throw new ServeurPasPret();
    }


    @GetMapping("/{langue}/unmot/longueur/{taille:[1-9][0-9]*}")
    public String oneWord(@PathVariable("langue") String langue, @PathVariable("taille") int taille) {
        if (wordsServiceCheck.estPret()) {
            String vérification = checkFiles(langue);
            String mot = "";
            if (vérification.equals("")) {
                ObjectMapper mapper = new ObjectMapper();
                Dictionnary dico = null;
                try {
                    // c'est le code de l'exemple
                    dico = mapper.readValue(new File("langues/" + langue + "/anagrammes.json"), Dictionnary.class);
                    Object[] keys = dico.getDictionnary().keySet().stream().filter(s -> s.length() == taille).toArray();
                    if (keys.length > 0) {
                        Object key = keys[rand.nextInt(keys.length)];
                        ArrayList<String> liste = dico.getDictionnary().get(key);
                        mot = liste.get(rand.nextInt(liste.size()));
                    }

                } catch (IOException e) {
                    return mot;
                }
            }
            return mot;
        } else throw new ServeurPasPret();
    }


    private void lookingForAnagrams(String mot, Dictionnary dico, HashSet<String> possibleAnagrams) {
        if (wordsServiceCheck.estPret()) {
            String key = dico.anagram(mot);

            ArrayList<int[]> subwordIndexes = new ArrayList<>();

            // on récupère toutes les combinaisons des indices des lettres, donc toutes les combinaisons des lettres
            for (int i = 1; i <= key.length(); i++) {
                Iterator<int[]> list = CombinatoricsUtils.combinationsIterator(key.length(), i);
                while (list.hasNext()) {
                    final int[] combination = list.next();
                    subwordIndexes.add(combination);
                }
            }

            // pour chaque combinaison, on cumule les anagrammes dans le "Set", pour ne pas avoir de doublon
            for (int i = 0; i < subwordIndexes.size(); i++) {
                int[] indexes = subwordIndexes.get(i);
                String anagram = "";
                for (int j = 0; j < indexes.length; j++) anagram += key.charAt(indexes[j]);

                ArrayList<String> adds = dico.getDictionnary().get(anagram);
                if ((adds != null) && (adds.size() > 0)) possibleAnagrams.addAll(adds);

            }
        } else throw new ServeurPasPret();
    }


    /**
     * vérifier "langues" puis le dossier "langues/<la langue>" puis l'existance du fichier anagrammes.json
     *
     * @param langue la langue
     * @return un message d'erreur ou "" s'il n'y a pas de soucis
     */
    private String checkFiles(String langue) {
        if (wordsServiceCheck.estPret()) {
            File langDir = new File("langues");
            if (!langDir.exists()) {
                return "problème serveur : le dossier langues n'existe pas";
            }

            if (!langDir.isDirectory()) return "problème serveur : un dossier interne n'est pas un dossier";

            // on gère la langue
            langDir = new File("langues/" + langue);
            if (langDir.exists()) {
                if (!langDir.isDirectory())
                    return "problème serveur : la langue spécifiée pose soucis (le dossier existe mais ce n'est pas un dossier)";
                File json = new File("langues/" + langue + "/anagrammes.json");
                if (!json.exists()) return "la langue spécifiée pose soucis (lle fichier json n'existe pas)";
            } else {
                return "problème serveur : la langue spécifiée pose soucis (le dossier langue n'existe pas)";
            }

            return "";
        } else throw new ServeurPasPret();
    }
}
