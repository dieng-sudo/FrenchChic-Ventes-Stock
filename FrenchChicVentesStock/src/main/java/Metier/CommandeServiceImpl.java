package Metier;

import java.math.BigDecimal;

/**
 * Implémentation concrète du service de commande.
 *
 * Gère :
 *  - décrémentation de stock,
 *  - création de la commande,
 *  - création et persistance du paiement,
 *  - sauvegarde durable en base.
 */
public final class CommandeServiceImpl implements CommandeService {

    private final InventaireService inventaireService;
    private final CommandeRepository commandeRepository;
    private final PaiementRepository paiementRepository;

    public CommandeServiceImpl(InventaireService inventaireService,
                               CommandeRepository commandeRepository,
                               PaiementRepository paiementRepository) {
        this.inventaireService = inventaireService;
        this.commandeRepository = commandeRepository;
        this.paiementRepository = paiementRepository;
    }

    /**
     * Sprint 2 : commande pour un seul produit du jour.
     */
    @Override
    public Commande passerCommandeProduitDuJour(
            int quantite,
            Acheteur acheteur,
            InformationCarte infosCarte
    ) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("Quantité > 0 requise");
        }

        // 1) Produit du jour
        Produit produit = inventaireService.obtenirProduitDuJour();

        // 2) Décrémentation du stock
        inventaireService.decrementerStock(produit.getId(), quantite);

        // 3) Création de la commande
        Commande commande = new Commande();
        commande.ajouterLigne(produit, quantite);

        // 4) Montant total
        BigDecimal montant = commande.calculerTotal();

        // 5) Paiement
        Paiement paiement = new Paiement(
                commande.getId(),
                acheteur,
                infosCarte,
                montant
        );
        paiementRepository.save(paiement);
        commande.associerPaiement(paiement);

        // 6) Persistance commande
        commandeRepository.save(commande);

        return commande;
    }

    /**
     * Sprint 3 : commande basée sur un panier contenant plusieurs produits différents.
     */
    @Override
    public Commande passerCommandeDepuisPanier(
            Panier panier,
            Acheteur acheteur,
            InformationCarte infosCarte
    ) {
        if (panier == null || panier.estVide()) {
            throw new IllegalArgumentException("Le panier ne peut pas être vide");
        }

        // 1) Création de la commande
        Commande commande = new Commande();

        // 2) Pour chaque ligne du panier :
        //    - décrémenter le stock,
        //    - ajouter la ligne dans la commande
        for (LigneCommande lc : panier.getLignes()) {
            Produit produit = lc.getProduit();
            int quantite = lc.getQuantite();

            inventaireService.decrementerStock(produit.getId(), quantite);
            commande.ajouterLigne(produit, quantite);
        }

        // 3) Montant total
        BigDecimal montant = commande.calculerTotal();

        // 4) Paiement
        Paiement paiement = new Paiement(
                commande.getId(),
                acheteur,
                infosCarte,
                montant
        );
        paiementRepository.save(paiement);
        commande.associerPaiement(paiement);

        // 5) Persistance commande
        commandeRepository.save(commande);

        return commande;
    }
}
