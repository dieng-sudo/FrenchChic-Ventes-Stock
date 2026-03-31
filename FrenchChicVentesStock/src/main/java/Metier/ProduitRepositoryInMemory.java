package Metier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProduitRepositoryInMemory implements ProduitRepository {
    private final Map<Long, Produit> store = new ConcurrentHashMap<>();
    private volatile Long produitDuJourId;

    public ProduitRepositoryInMemory() {
        // Seed : un produit du jour par défaut
        Produit p = new Produit(1L, "Chemise 'French Chic' - Blanc", 25, new BigDecimal("39.90"));
        save(p);
        produitDuJourId = p.getId();
    }

    @Override
    public Optional<Produit> findProduitDuJour() {
        if (produitDuJourId == null) return Optional.empty();
        return findById(produitDuJourId);
    }

    @Override
    public Optional<Produit> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Produit produit) {
        store.put(produit.getId(), produit);
    }

    @Override
    public List<Produit> findAll() {
        return new ArrayList<>(store.values());
    }
}
