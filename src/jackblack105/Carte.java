package jackblack105;

public class Carte {
    private String couleur;
    private String nom;
    private int valeur;

    public Carte(String couleur, String nom, int valeur) {
        this.couleur = couleur;
        this.nom = nom;
        this.valeur = valeur;
    }

    public int getValeur() {
        return this.valeur;
    }

    public String getCouleur() {
        return this.couleur;
    }

    public String getNom() {
        return this.nom;
    }

    public boolean isAs() {
        return "As".equals(this.nom);
    }

    @Override
    public String toString() {
        return this.nom + " de " + this.couleur;
    }
}