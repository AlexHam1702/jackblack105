package jackblack105;

import java.util.ArrayList;
import java.util.List;

public class Player {
    // ATTRIBUTS
    private String nom;
    private int porteMonnaie;
    private int miseActuelle;
    private ArrayList<String> main;
    private String carte;

    // CONSTRUCTEUR
    public Player(String nom, int jetonsInitiaux) {
        this.nom = nom;
        this.porteMonnaie = jetonsInitiaux;
        this.miseActuelle = 0;
        this.main = new ArrayList<String>();
    }

    // GESTION DES MISES
    public boolean miser(int montant) {
        if (montant <= 0 || montant > this.porteMonnaie) {
            return false; 
        }
        this.porteMonnaie -= montant;
        this.miseActuelle = montant;
        return true;
    }

    public void gagnerMise() {
        this.porteMonnaie += (this.miseActuelle * 2);
        this.miseActuelle = 0;
    }

    public void recupererMise() {
        this.porteMonnaie += this.miseActuelle;
        this.miseActuelle = 0;
    }

    public void perdreMise() {
        this.miseActuelle = 0;
    }

    // GESTION DE LA MAIN
    public void ajouterCarte(String nouvelleCarte) {
        if (nouvelleCarte != null) {
            this.main.add(nouvelleCarte);
        }
    }

    public void viderMain() {
        this.main.clear();
    }

    public int calculerScore() {
        int score = 0;
        int nombreAs = 0;

        for (String carte : this.main) {
            int valeur = carte.getValeur();
            score += valeur;
            if (valeur == 11) {
                nombreAs++;
            }
        }

        while (score > 21 && nombreAs > 0) {
            score -= 10;
            nombreAs--;
        }

        return score;
    }

    // --- GETTERS & AFFICHAGE 
    public String getNom() {
        return this.nom;
    }

    public int getPorteMonnaie() {
        return this.porteMonnaie;
    }

    public int getMiseActuelle() {
        return this.miseActuelle;
    }

    public ArrayList<String> getMain() {
        return this.main;
    }

    @Override
    public String toString() {
        return "Joueur : " + this.nom + 
               " | Jetons : " + this.porteMonnaie + 
               " | Mise : " + this.miseActuelle + 
               " | Main : " + this.main + 
               " (Score : " + this.calculerScore() + ")";
    }
}``
