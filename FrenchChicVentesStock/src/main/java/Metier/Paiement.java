package Metier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class Paiement {
    private final UUID transactionId;
    private final UUID commandeId;
    private final Acheteur acheteur;
    private final InformationCarte infosCarte;
    private final BigDecimal montant;
    private final LocalDateTime dateCreation;

    public Paiement(UUID commandeId, Acheteur acheteur, InformationCarte infosCarte, BigDecimal montant) {
        if (commandeId == null) throw new IllegalArgumentException("commandeId requis");
        if (montant == null || montant.signum() < 0) throw new IllegalArgumentException("Montant >= 0");

        this.transactionId = UUID.randomUUID();
        this.commandeId = commandeId;
        this.acheteur = acheteur;
        this.infosCarte = infosCarte;
        this.montant = montant;
        this.dateCreation = LocalDateTime.now();
    }

    public UUID getTransactionId() { return transactionId; }
    public UUID getCommandeId() { return commandeId; }
    public Acheteur getAcheteur() { return acheteur; }
    public InformationCarte getInfosCarte() { return infosCarte; }
    public BigDecimal getMontant() { return montant; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
