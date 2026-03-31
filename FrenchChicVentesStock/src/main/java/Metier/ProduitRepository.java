package Metier;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository {
    Optional<Produit> findProduitDuJour();
    Optional<Produit> findById(long id);
    void save(Produit produit);

    /**
     * Retourne tous les produits (catalogue).
     */
    List<Produit> findAll();
}
