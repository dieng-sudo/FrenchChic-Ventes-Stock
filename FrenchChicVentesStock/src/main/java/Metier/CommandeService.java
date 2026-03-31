package Metier;

/**
 * Service métier responsable de la gestion des commandes.
 */
public interface CommandeService {

    /**
     * Sprint 2 : commande d'un seul produit du jour.
     */
    Commande passerCommandeProduitDuJour(int quantite,
                                         Acheteur acheteur,
                                         InformationCarte infosCarte);

    /**
     * Sprint 3 : commande d'un panier complet (plusieurs produits).
     */
    Commande passerCommandeDepuisPanier(Panier panier,
                                        Acheteur acheteur,
                                        InformationCarte infosCarte);
}
