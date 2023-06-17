package fr.utln.jmonkey.DeliciousWord;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerifMot {
    private static final Logger logger = Logger.getLogger(VerifMot.class.getName());
    public static boolean verif(String mot) {
        try {
            File file = new File("src/main/resources/mots.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (mot.equals(line)) {
                    return true;
                }
            }
            scanner.close();
            return false;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Error reading file", e);
            return false;
        }
    }
}
