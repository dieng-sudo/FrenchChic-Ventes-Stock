package Metier;

public final class Acheteur {
    private final String prenom;
    private final String nom;
    private final String adresseLivraison;

    public Acheteur(String prenom, String nom, String adresseLivraison) {
        if (prenom == null || prenom.isBlank()) throw new IllegalArgumentException("Prénom requis");
        if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Nom requis");
        if (adresseLivraison == null || adresseLivraison.isBlank()) throw new IllegalArgumentException("Adresse requise");

        this.prenom = prenom;
        this.nom = nom;
        this.adresseLivraison = adresseLivraison;
    }

    public String getPrenom() { return prenom; }
    public String getNom() { return nom; }
    public String getAdresseLivraison() { return adresseLivraison; }
}
