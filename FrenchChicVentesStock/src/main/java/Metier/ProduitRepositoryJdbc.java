package Metier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProduitRepositoryJdbc implements ProduitRepository {

    private final Connection connection;
    private final long produitDuJourId;

    public ProduitRepositoryJdbc(Connection connection, long produitDuJourId) {
        this.connection = connection;
        this.produitDuJourId = produitDuJourId;
    }

    @Override
    public Optional<Produit> findProduitDuJour() {
        return findById(produitDuJourId);
    }

    @Override
    public Optional<Produit> findById(long id) {
        String sql = "SELECT id, libelle, stock, prix_unitaire FROM produit WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Produit p = new Produit(
                        rs.getLong("id"),
                        rs.getString("libelle"),
                        rs.getInt("stock"),
                        rs.getBigDecimal("prix_unitaire")
                );
                return Optional.of(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById produit", e);
        }
    }

    @Override
    public void save(Produit produit) {
        String sql = """
                INSERT INTO produit (id, libelle, stock, prix_unitaire)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    libelle = excluded.libelle,
                    stock = excluded.stock,
                    prix_unitaire = excluded.prix_unitaire
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, produit.getId());
            ps.setString(2, produit.getLibelle());
            ps.setInt(3, produit.getStock());
            ps.setBigDecimal(4, produit.getPrixUnitaire());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save produit", e);
        }
    }

    @Override
    public List<Produit> findAll() {
        String sql = "SELECT id, libelle, stock, prix_unitaire FROM produit ORDER BY libelle";
        List<Produit> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Produit p = new Produit(
                        rs.getLong("id"),
                        rs.getString("libelle"),
                        rs.getInt("stock"),
                        rs.getBigDecimal("prix_unitaire")
                );
                result.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll produits", e);
        }
        return result;
    }
}
