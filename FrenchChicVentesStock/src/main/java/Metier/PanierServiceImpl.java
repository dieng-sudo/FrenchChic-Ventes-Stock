package Metier;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implémentation par défaut du service de panier.
 */
public final class PanierServiceImpl implements PanierService {

    private static final Logger LOGGER = Logger.getLogger(PanierServiceImpl.class.getName());

    private final ProduitRepository produitRepository;
    private final Panier panier = new Panier();

    public PanierServiceImpl(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Override
    public Panier getPanierCourant() {
        return panier;
    }

    /**
     * Ajout au panier avec contrôle du stock :
     * - on récupère le produit en base,
     * - on regarde combien d'unités sont déjà dans le panier,
     * - on refuse si (déjà_dans_panier + quantité_demandée) > stock.
     */
    @Override
    public void ajouterAuPanier(long produitId, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("Quantité > 0 requise");
        }

        Produit p = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable : " + produitId));

        int dejaDansPanier = panier.getLignes().stream()
                .filter(lc -> lc.getProduit().equals(p))
                .mapToInt(LigneCommande::getQuantite)
                .sum();

        int totalDemande = dejaDansPanier + quantite;
        if (totalDemande > p.getStock()) {
            String msg = "Stock insuffisant : il reste " + p.getStock() +
                    " unité(s) pour \"" + p.getLibelle() +
                    "\", le panier en contiendrait " + totalDemande + ".";
            LOGGER.log(Level.WARNING, msg);
            throw new StockInsuffisantException(msg);
        }

        panier.ajouterProduit(p, quantite);
        LOGGER.log(Level.INFO, () -> "Produit ajouté au panier : id=" +
                p.getId() + ", qte=" + quantite + ", totalPanier=" + panier.calculerTotal());
    }

    @Override
    public void supprimerDuPanier(long produitId) {
        panier.supprimerProduitParId(produitId);
        LOGGER.log(Level.INFO, () -> "Produit retiré du panier : id=" + produitId);
    }

    @Override
    public List<Produit> listerProduitsDisponibles() {
        return produitRepository.findAll();
    }
}
