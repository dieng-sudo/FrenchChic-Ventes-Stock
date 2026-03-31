package Metier;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException() {
        super("Stock insuffisant pour la quantité demandée.");
    }
    public StockInsuffisantException(String message) {
        super(message);
    }
}
