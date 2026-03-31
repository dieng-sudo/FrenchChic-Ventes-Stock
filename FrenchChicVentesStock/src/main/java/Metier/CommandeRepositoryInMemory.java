package Metier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implémentation en mémoire de CommandeRepository.
 * Utile pour des tests sans base de données.
 */
public final class CommandeRepositoryInMemory implements CommandeRepository {

    private final List<Commande> commandes = new ArrayList<>();

    @Override
    public synchronized void save(Commande commande) {
        commandes.add(commande);
    }

    @Override
    public synchronized List<Commande> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(commandes));
    }
}
