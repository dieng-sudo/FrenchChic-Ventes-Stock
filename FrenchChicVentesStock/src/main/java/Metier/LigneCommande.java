package Metier;

import Metier.Produit;
import java.math.BigDecimal;

public final class LigneCommande {
    private final Produit produit;
    private final int quantite;

    public LigneCommande(Produit produit, int quantite) {
        if (produit == null) throw new IllegalArgumentException("Produit requis");
        if (quantite <= 0) throw new IllegalArgumentException("Quantité > 0 requise");
        this.produit = produit;
        this.quantite = quantite;
    }

    public Produit getProduit() { return produit; }
    public int getQuantite() { return quantite; }

    public BigDecimal calculerMontant() {
        return produit.getPrixUnitaire().multiply(BigDecimal.valueOf(quantite));
    }
}
