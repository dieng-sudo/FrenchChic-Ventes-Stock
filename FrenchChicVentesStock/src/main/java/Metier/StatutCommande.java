package Metier;

/**
 * Statut métier d'une commande.
 * Permet d'évoluer vers des workflows plus riches si besoin.
 */
public enum StatutCommande {
    ENREGISTREE,   // commande créée côté appli
    PAYEE,         // paiement associé et validé
    LIVREE         // éventuellement plus tard
}
