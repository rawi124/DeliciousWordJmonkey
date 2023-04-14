package fr.utln.jmonkey.tutorials.beginner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class VerifMot {



    public static boolean verif (String mot) {
        try {
            // Créer un objet File qui représente le fichier à lire
            File file = new File("src/main/resources/mots.txt");

            // Créer un objet Scanner pour lire le contenu du fichier
            Scanner scanner = new Scanner(file);

            // Lire chaque ligne du fichier et afficher son contenu
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (mot.equals(line)){
                    return true;
                }
            }

            // Fermer le scanner pour libérer les ressources
            scanner.close();
            return false;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false ;
        }
    }
}
