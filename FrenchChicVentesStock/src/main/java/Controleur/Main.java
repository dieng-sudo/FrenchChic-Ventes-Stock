package Controleur;

import Metier.*;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.DriverManager;

public final class Main {

    public static void main(String[] args) {
        try {
            // Chemin vers ta base SQLite
            String url = "jdbc:sqlite:/home/alseny/frenchchic.db";
            Connection connection = DriverManager.getConnection(url);
            System.out.println("Connexion SQLite OK !");

            long produitDuJourId = 1L; // identifiant du produit du jour

            ProduitRepository produitRepo = new ProduitRepositoryJdbc(connection, produitDuJourId);
            CommandeRepository commandeRepo = new CommandeRepositoryJdbc(connection);
            PaiementRepository paiementRepo = new PaiementRepositoryJdbc(connection);

            InventaireService inventaireService = new InventaireServiceImpl(produitRepo);
            CommandeService commandeService = new CommandeServiceImpl(
                    inventaireService,
                    commandeRepo,
                    paiementRepo
            );

            // 🧺 Service de panier
            PanierService panierService = new PanierServiceImpl(produitRepo);

            SwingUtilities.invokeLater(() -> {
                MainFrame f = new MainFrame(commandeService, inventaireService, panierService);
                f.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
