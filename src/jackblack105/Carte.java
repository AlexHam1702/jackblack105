package jackblack105;

public class Carte {
    private String couleur;
    private int valeur;

    public Carte(String couleur, int valeur) {
        this.couleur = couleur;
        this.valeur = valeur;
    }

    public int getValeur() {
        return this.valeur;
    }

    public String getCouleur() {
        return this.couleur;
    }

    @Override
    public String toString() {
        return this.valeur + " de " + this.couleur;
    }
}