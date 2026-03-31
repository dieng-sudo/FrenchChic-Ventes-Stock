package Metier;

import java.sql.*;

public final class PaiementRepositoryJdbc implements PaiementRepository {

    private final Connection connection;

    public PaiementRepositoryJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Paiement paiement) {
        String sql = """
            INSERT INTO paiement (
                transaction_id, commande_id, montant, date_creation,
                prenom, nom, adresse,
                numero_carte, date_expiration, code_verif
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paiement.getTransactionId().toString());
            ps.setString(2, paiement.getCommandeId().toString());
            ps.setBigDecimal(3, paiement.getMontant());
            ps.setTimestamp(4, Timestamp.valueOf(paiement.getDateCreation()));
            ps.setString(5, paiement.getAcheteur().getPrenom());
            ps.setString(6, paiement.getAcheteur().getNom());
            ps.setString(7, paiement.getAcheteur().getAdresseLivraison());
            ps.setString(8, paiement.getInfosCarte().getNumeroCarte());
            ps.setString(9, paiement.getInfosCarte().getDateExpiration());
            ps.setString(10, paiement.getInfosCarte().getCodeVerification());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur save paiement", e);
        }
    }
}
