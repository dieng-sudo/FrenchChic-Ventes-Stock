package Metier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Panier d'articles en mémoire (liste de lignes de commande).
 * Permet de regrouper plusieurs produits différents dans une future commande.
 */
public final class Panier {

    private final List<LigneCommande> lignes = new ArrayList<>();

    /**
     * Retourne une vue immuable des lignes du panier.
     * @return 
     */
    public List<LigneCommande> getLignes() {
        return List.copyOf(lignes);
    }

    public boolean estVide() {
        return lignes.isEmpty();
    }

    public void vider() {
        lignes.clear();
    }

    /**
     * Ajoute un produit au panier. Si le produit existe déjà, on cumule les quantités.
     * @param produit
     * @param quantite
     */
    public void ajouterProduit(Produit produit, int quantite) {
        if (produit == null) throw new IllegalArgumentException("Produit requis");
        if (quantite <= 0) throw new IllegalArgumentException("Quantité > 0 requise");

        Optional<LigneCommande> existante = lignes.stream()
                .filter(lc -> lc.getProduit().equals(produit))
                .findFirst();

        if (existante.isPresent()) {
            LigneCommande lc = existante.get();
            int nouvelleQte = lc.getQuantite() + quantite;
            lignes.remove(lc);
            lignes.add(new LigneCommande(produit, nouvelleQte));
        } else {
            lignes.add(new LigneCommande(produit, quantite));
        }
    }

    /**
     * Montant total du panier (somme des lignes).
     * @return 
     */
    public BigDecimal calculerTotal() {
        return lignes.stream()
                .map(LigneCommande::calculerMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Supprime un produit du panier (toutes les quantités).
     * @param produitId
     */
    public void supprimerProduitParId(long produitId) {
        lignes.removeIf(lc -> lc.getProduit().getId() == produitId);
    }
}
