package Metier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modèle métier pour une commande.
 */
public final class Commande {

    private final UUID id;
    private final LocalDateTime dateCreation;
    private final List<LigneCommande> lignes;
    private Paiement paiement;
    private StatutCommande statut;

    public Commande() {
        this.id = UUID.randomUUID();
        this.dateCreation = LocalDateTime.now();
        this.lignes = new ArrayList<>();
        this.statut = StatutCommande.ENREGISTREE;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    /**
     * Vue immuable sur les lignes de la commande.
     */
    public List<LigneCommande> getLignes() {
        return List.copyOf(lignes);
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public Paiement getPaiement() {
        return paiement;
    }

    /**
     * Ajoute une ligne à la commande (sans contrôle de stock).
     * Le contrôle de stock est réalisé au niveau du service.
     */
    public void ajouterLigne(Produit produit, int quantite) {
        lignes.add(new LigneCommande(produit, quantite));
    }

    /**
     * Montant total TTC de la commande.
     */
    public BigDecimal calculerTotal() {
        return lignes.stream()
                .map(LigneCommande::calculerMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Associe un paiement à la commande.
     * Passe automatiquement le statut à PAYEE.
     */
    public void associerPaiement(Paiement paiement) {
        if (paiement == null) {
            throw new IllegalArgumentException("Paiement requis pour associer à la commande.");
        }
        this.paiement = paiement;
        this.statut = StatutCommande.PAYEE;
    }
}
