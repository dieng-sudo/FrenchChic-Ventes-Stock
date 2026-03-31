package Metier;

public final class InformationCarte {
    private final String numeroCarte;
    private final String dateExpiration;
    private final String codeVerification;

    public InformationCarte(String numeroCarte, String dateExpiration, String codeVerification) {
        if (numeroCarte == null || numeroCarte.isBlank()) throw new IllegalArgumentException("Numéro requis");
        if (dateExpiration == null || dateExpiration.isBlank()) throw new IllegalArgumentException("Expiration requise");
        if (codeVerification == null || codeVerification.isBlank()) throw new IllegalArgumentException("CVV requis");

        this.numeroCarte = numeroCarte;
        this.dateExpiration = dateExpiration;
        this.codeVerification = codeVerification;
    }

    public String getNumeroCarte() { return numeroCarte; }
    public String getDateExpiration() { return dateExpiration; }
    public String getCodeVerification() { return codeVerification; }
}
