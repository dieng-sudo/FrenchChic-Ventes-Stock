package Metier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class CommandeRepositoryJdbc implements CommandeRepository {

    private final Connection connection;

    public CommandeRepositoryJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Commande commande) {
        String insertCommande =
            "INSERT INTO commande (id, date_creation) VALUES (?, ?)";

        String insertLigne = """
            INSERT INTO ligne_commande (commande_id, produit_id, quantite, montant_ligne)
            VALUES (?, ?, ?, ?)
        """;

        try (
            PreparedStatement psCmd = connection.prepareStatement(insertCommande);
            PreparedStatement psLigne = connection.prepareStatement(insertLigne)
        ) {
            psCmd.setString(1, commande.getId().toString());
            psCmd.setTimestamp(2, Timestamp.valueOf(commande.getDateCreation()));
            psCmd.executeUpdate();

            for (LigneCommande lc : commande.getLignes()) {
                psLigne.setString(1, commande.getId().toString());
                psLigne.setLong(2, lc.getProduit().getId());
                psLigne.setInt(3, lc.getQuantite());
                psLigne.setBigDecimal(4, lc.calculerMontant());
                psLigne.addBatch();
            }

            psLigne.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save commande", e);
        }
    }

    @Override
    public List<Commande> findAll() {
        // Pour le sprint 2, tu peux laisser non implémenté côté lecture détaillée.
        // On retourne une liste vide pour satisfaire l'interface.
        return new ArrayList<>();
    }
}
