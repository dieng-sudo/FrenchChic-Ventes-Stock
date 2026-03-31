package Metier;

import java.util.List;

/**
 * Service chargé de gérer le panier courant de l'utilisateur.
 */
public interface PanierService {

    /**
     * Retourne le panier courant (application monoposte).
     */
    Panier getPanierCourant();

    /**
     * Ajoute un produit au panier en fonction de son identifiant.
     */
    void ajouterAuPanier(long produitId, int quantite);

    /**
     * Retire complètement un produit du panier.
     */
    void supprimerDuPanier(long produitId);

    /**
     * Retourne la liste des produits disponibles (catalogue).
     */
    List<Produit> listerProduitsDisponibles();
}
