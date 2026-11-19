package jackblack105;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Carte> cartes;

    public Deck() {
        this.cartes = new ArrayList<Carte>();
        genererPaquet();
        melanger();
    }

    // Fills the deck with 52 standard cards
    private void genererPaquet() {
        String[] couleurs = {"Cœur", "Carreau", "Trèfle", "Pique"};
        String[] noms = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Valet", "Dame", "Roi", "As"};
        int[] valeurs = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        for (String couleur : couleurs) {
            for (int i = 0; i < noms.length; i++) {
                this.cartes.add(new Carte(couleur, noms[i], valeurs[i]));
            }
        }
    }

    // Randomizes the order of the cards
    public void melanger() {
        Collections.shuffle(this.cartes);
    }

    // Removes and returns the top card from the deck
    public Carte tirerCarte() {
        if (this.cartes.isEmpty()) {
            System.out.println("Le paquet est vide ! Création d'un nouveau paquet...");
            genererPaquet();
            melanger();
        }
        return this.cartes.remove(0); // Removes the card at index 0 and gives it to the caller
    }
}