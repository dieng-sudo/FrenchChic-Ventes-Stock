package Metier;

import java.util.List;

public interface CommandeRepository {
    void save(Commande commande);
    List<Commande> findAll();
}
