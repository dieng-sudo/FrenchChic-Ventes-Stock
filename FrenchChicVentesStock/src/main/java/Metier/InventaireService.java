package Metier;

import Metier.Produit;

public interface InventaireService {
    Produit obtenirProduitDuJour();
    void decrementerStock(long produitId, int quantite);
}
