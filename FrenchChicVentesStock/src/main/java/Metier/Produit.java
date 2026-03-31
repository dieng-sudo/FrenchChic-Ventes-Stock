package Metier;

import java.math.BigDecimal;
import java.util.Objects;

public final class Produit {
    private final long id;
    private final String libelle;
    private int stock;
    private final BigDecimal prixUnitaire; // TTC

    public Produit(long id, String libelle, int stock, BigDecimal prixUnitaire) {
        if (id <= 0) throw new IllegalArgumentException("id > 0 requis");
        if (libelle == null || libelle.isBlank()) throw new IllegalArgumentException("libellé requis");
        if (stock < 0) throw new IllegalArgumentException("stock >= 0 requis");
        if (prixUnitaire == null || prixUnitaire.signum() < 0) throw new IllegalArgumentException("prix >= 0 requis");
        this.id = id;
        this.libelle = libelle;
        this.stock = stock;
        this.prixUnitaire = prixUnitaire;
    }

    public long getId() { return id; }
    public String getLibelle() { return libelle; }
    public int getStock() { return stock; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }

    public void decrementerStock(int qte) {
        if (qte <= 0) throw new IllegalArgumentException("Quantité > 0 requise");
        if (stock - qte < 0) throw new StockInsuffisantException();
        stock -= qte;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produit)) return false;
        Produit produit = (Produit) o;
        return id == produit.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
