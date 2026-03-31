package Metier;

import Metier.InventaireService;
import Metier.ProduitRepository;
import Metier.Produit;

public final class InventaireServiceImpl implements InventaireService {

    private final ProduitRepository produitRepository;

    public InventaireServiceImpl(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Override
    public Produit obtenirProduitDuJour() {
        return produitRepository.findProduitDuJour()
                .orElseThrow(() -> new IllegalStateException("Aucun produit du jour défini."));
    }

    @Override
    public void decrementerStock(long produitId, int quantite) {
        Produit p = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable: id=" + produitId));
        p.decrementerStock(quantite);
        produitRepository.save(p); // persiste en mémoire
    }
}
