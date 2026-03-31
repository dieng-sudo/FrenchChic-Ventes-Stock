package Controleur;

import Metier.Acheteur;
import Metier.Commande;
import Metier.CommandeService;
import Metier.InformationCarte;
import Metier.InventaireService;
import Metier.LigneCommande;
import Metier.Panier;
import Metier.PanierService;
import Metier.Paiement;
import Metier.Produit;
import Metier.StockInsuffisantException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MainFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    private final CommandeService commandeService;
    private final InventaireService inventaireService;
    private final PanierService panierService;

    // UI catalogue
    private JList<Produit> listProduits;
    private DefaultListModel<Produit> modelProduits;

    // UI panier
    private JTable tablePanier;
    private DefaultTableModel modelPanier;
    private JLabel lblTotalPanier;
    private JLabel lblStockInfo; // infos stock + max ajout

    // UI paiement / acheteur
    private JTextField txtPrenom;
    private JTextField txtNom;
    private JTextField txtAdresse;
    private JTextField txtAdresseCompl; // complément d'adresse (facultatif)
    private JTextField txtNumeroCarte;
    private JTextField txtDateExpiration;
    private JTextField txtCodeVerif;
    private JSpinner spQuantite;

    private JButton btnValiderCommande;

    private JTextArea txtResult;

    // Styles
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_HEADER = new Color(18, 24, 38);
    private static final Color COLOR_PRIMARY = new Color(30, 64, 175);
    private static final Color COLOR_TEXT_DARK = new Color(31, 41, 55);
    private static final float FONT_BASE = 14f;

    public MainFrame(CommandeService commandeService,
                     InventaireService inventaireService,
                     PanierService panierService) {
        super("French Chic – Ventes & Stock (Sprint 3 – Panier)");
        this.commandeService = commandeService;
        this.inventaireService = inventaireService;
        this.panierService = panierService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 700));
        setLocationByPlatform(true);
        getContentPane().setBackground(COLOR_BG);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // centrer la fenêtre

        // Préparation finale : champs vides pour la démo
        ajouterEcouteursChampsClient();
        chargerCatalogue();
        rafraichirPanier();
        mettreAJourEtatBoutons();
    }

    // ------------------------------------------------------------------
    // HEADER
    // ------------------------------------------------------------------

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("French Chic – Commande en ligne avec panier");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Sprint 3 • Panier multi-produits • Validation et paiement sécurisés");
        subtitle.setForeground(new Color(229, 231, 235));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(4));
        texts.add(subtitle);

        header.add(texts, BorderLayout.WEST);
        return header;
    }

    // ------------------------------------------------------------------
    // CONTENU PRINCIPAL
    // ------------------------------------------------------------------

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // ------------ CATALOGUE -------------
        JPanel panelCatalogue = new JPanel(new BorderLayout(8, 8));
        panelCatalogue.setBackground(Color.WHITE);
        panelCatalogue.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblCat = new JLabel("Catalogue des produits");
        lblCat.setFont(lblCat.getFont().deriveFont(Font.BOLD, FONT_BASE + 1));
        panelCatalogue.add(lblCat, BorderLayout.NORTH);

        modelProduits = new DefaultListModel<>();
        listProduits = new JList<>(modelProduits);
        listProduits.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listProduits.setCellRenderer(new ProduitListRenderer());
        listProduits.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mettreAJourStockInfo();
            }
        });
        JScrollPane scrollProd = new JScrollPane(listProduits);
        panelCatalogue.add(scrollProd, BorderLayout.CENTER);

        JPanel panelAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelAdd.setOpaque(false);
        panelAdd.add(new JLabel("Quantité :"));
        spQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        ((JSpinner.DefaultEditor) spQuantite.getEditor()).getTextField().setColumns(4);
        panelAdd.add(spQuantite);

        JButton btnAjouterPanier = createButton("Ajouter au panier", COLOR_PRIMARY, Color.WHITE);
        btnAjouterPanier.addActionListener(e -> ajouterSelectionAuPanier());
        btnAjouterPanier.setMnemonic('A'); // Alt + A
        btnAjouterPanier.setToolTipText("Ajoute le produit sélectionné au panier (en respectant le stock).");
        panelAdd.add(btnAjouterPanier);

        lblStockInfo = labelMuted("Stock disponible : -");
        panelAdd.add(lblStockInfo);

        panelCatalogue.add(panelAdd, BorderLayout.SOUTH);

        // ------------ PANIER -------------
        JPanel panelPanier = new JPanel(new BorderLayout(8, 8));
        panelPanier.setBackground(Color.WHITE);
        panelPanier.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblPan = new JLabel("Panier courant");
        lblPan.setFont(lblPan.getFont().deriveFont(Font.BOLD, FONT_BASE + 1));
        panelPanier.add(lblPan, BorderLayout.NORTH);

        modelPanier = new DefaultTableModel(
                new Object[]{"ID produit", "Produit", "Quantité", "Prix unitaire", "Montant ligne"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePanier = new JTable(modelPanier);
        tablePanier.setRowHeight(22);
        JScrollPane scrollPanier = new JScrollPane(tablePanier);
        panelPanier.add(scrollPanier, BorderLayout.CENTER);

        JPanel panelBasPanier = new JPanel(new BorderLayout());
        panelBasPanier.setOpaque(false);

        JButton btnRetirer = createButton("Retirer du panier", new Color(185, 28, 28), Color.WHITE);
        btnRetirer.addActionListener(e -> retirerLigneSelectionneeDuPanier());
        btnRetirer.setMnemonic('R'); // Alt + R
        btnRetirer.setToolTipText("Retire la ligne sélectionnée du panier.");
        panelBasPanier.add(btnRetirer, BorderLayout.WEST);

        lblTotalPanier = new JLabel("Total panier : 0,00 €");
        lblTotalPanier.setFont(lblTotalPanier.getFont().deriveFont(Font.BOLD, FONT_BASE));
        lblTotalPanier.setHorizontalAlignment(SwingConstants.RIGHT);
        panelBasPanier.add(lblTotalPanier, BorderLayout.EAST);

        panelPanier.add(panelBasPanier, BorderLayout.SOUTH);

        JPanel panelTop = new JPanel(new GridLayout(1, 2, 16, 0));
        panelTop.setOpaque(false);
        panelTop.add(panelCatalogue);
        panelTop.add(panelPanier);

        // ------------ INFO CLIENT + PAIEMENT + RESULT -------------
        JPanel panelRight = new JPanel(new BorderLayout(8, 8));
        panelRight.setOpaque(false);

        JPanel panelClientPay = buildClientPaymentPanel();
        panelRight.add(panelClientPay, BorderLayout.NORTH);

        txtResult = new JTextArea(14, 60);
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollRes = new JScrollPane(txtResult);
        scrollRes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Récapitulatif de la commande"),
                new EmptyBorder(8, 8, 8, 8)
        ));
        scrollRes.setPreferredSize(new Dimension(400, 260));
        panelRight.add(scrollRes, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTop, panelRight);
        split.setResizeWeight(0.52);
        split.setDividerSize(6);
        split.setContinuousLayout(true);

        root.add(split, BorderLayout.CENTER);
        return root;
    }

    // ------------------------------------------------------------------
    // PIED DE PAGE
    // ------------------------------------------------------------------

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel hint = new JLabel(
                "<html>Sélectionnez un produit, ajoutez-le au panier, " +
                        "ajustez la quantité en respectant le stock, " +
                        "puis validez la commande après vérification du récapitulatif.</html>"
        );
        hint.setForeground(new Color(75, 85, 99));
        hint.setBorder(new EmptyBorder(0, 20, 16, 20));
        footer.add(hint, BorderLayout.WEST);
        return footer;
    }

    // ------------------------------------------------------------------
    // PANNEAU INFOS CLIENT + PAIEMENT
    // ------------------------------------------------------------------

    private JPanel buildClientPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int row = 0;

        JLabel title = new JLabel("Informations acheteur et paiement");
        title.setFont(title.getFont().deriveFont(Font.BOLD, FONT_BASE + 1));
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        panel.add(title, gc);
        row++;
        gc.gridwidth = 1;

        txtPrenom = new JTextField(15);
        txtNom = new JTextField(15);
        txtAdresse = new JTextField(25);
        txtAdresseCompl = new JTextField(25);
        txtNumeroCarte = new JTextField(16);
        txtDateExpiration = new JTextField(7);
        txtCodeVerif = new JTextField(4);

        txtPrenom.setToolTipText("Prénom de l'acheteur (obligatoire).");
        txtNom.setToolTipText("Nom de l'acheteur (obligatoire).");
        txtAdresse.setToolTipText("Adresse principale de livraison (obligatoire).");
        txtAdresseCompl.setToolTipText("Complément d'adresse (bâtiment, étage, etc.) – facultatif.");
        txtNumeroCarte.setToolTipText("Numéro de carte (16 chiffres, espaces autorisés).");
        txtDateExpiration.setToolTipText("Date d'expiration au format MM/AA (ex : 03/27).");
        txtCodeVerif.setToolTipText("Code de vérification au dos de la carte (3 chiffres).");

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("Prénom :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtPrenom, gc); row++;

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("Nom :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtNom, gc); row++;

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("Adresse de livraison :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtAdresse, gc); row++;

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("Complément d'adresse (optionnel) :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtAdresseCompl, gc); row++;

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("N° carte crédit :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtNumeroCarte, gc); row++;

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("Expiration (MM/AA) :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtDateExpiration, gc); row++;

        gc.gridx = 0; gc.gridy = row; panel.add(labelMuted("Code CVV :"), gc);
        gc.gridx = 1; gc.gridy = row; panel.add(txtCodeVerif, gc); row++;

        btnValiderCommande = createButton("Valider la commande", COLOR_PRIMARY, Color.WHITE);
        btnValiderCommande.addActionListener(e -> validerCommandeDepuisPanier());
        btnValiderCommande.setMnemonic('V'); // Alt + V
        btnValiderCommande.setToolTipText("Valide la commande après contrôle du panier et des informations saisies.");

        JButton btnReset = createButton("Réinitialiser", new Color(15, 23, 42), Color.WHITE);
        btnReset.addActionListener(e -> resetForm());
        btnReset.setMnemonic('X'); // Alt + X
        btnReset.setToolTipText("Réinitialise le formulaire et vide le panier.");

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBtns.setOpaque(false);
        panelBtns.add(btnValiderCommande);
        panelBtns.add(btnReset);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        panel.add(panelBtns, gc);

        return panel;
    }

    // ------------------------------------------------------------------
    // LOGIQUE UI
    // ------------------------------------------------------------------

    private void chargerCatalogue() {
        modelProduits.clear();
        List<Produit> produits = panierService.listerProduitsDisponibles();
        for (Produit p : produits) {
            modelProduits.addElement(p);
        }
        if (!produits.isEmpty()) {
            listProduits.setSelectedIndex(0);
        }
        mettreAJourStockInfo();
    }

    private void rafraichirPanier() {
        Panier panier = panierService.getPanierCourant();
        modelPanier.setRowCount(0);
        for (LigneCommande lc : panier.getLignes()) {
            modelPanier.addRow(new Object[]{
                    lc.getProduit().getId(),
                    lc.getProduit().getLibelle(),
                    lc.getQuantite(),
                    formatPrix(lc.getProduit().getPrixUnitaire()),
                    formatPrix(lc.calculerMontant())
            });
        }
        lblTotalPanier.setText("Total panier : " + formatPrix(panier.calculerTotal()));
        mettreAJourStockInfo();
        mettreAJourEtatBoutons();
    }

    private void ajouterSelectionAuPanier() {
        Produit produit = listProduits.getSelectedValue();
        if (produit == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un produit dans le catalogue.",
                    "Aucun produit sélectionné",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qte = (int) spQuantite.getValue();
        try {
            panierService.ajouterAuPanier(produit.getId(), qte);
            rafraichirPanier();
            JOptionPane.showMessageDialog(this,
                    "Produit ajouté au panier.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (StockInsuffisantException ex) {
            LOGGER.log(Level.WARNING, "Stock insuffisant à l'ajout au panier", ex);
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Stock insuffisant",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout au panier", ex);
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'ajout au panier : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void retirerLigneSelectionneeDuPanier() {
        int row = tablePanier.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner une ligne du panier à retirer.",
                    "Aucune ligne sélectionnée",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = modelPanier.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de déterminer l'identifiant du produit à retirer.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        long produitId;
        if (idObj instanceof Number n) {
            produitId = n.longValue();
        } else {
            try {
                produitId = Long.parseLong(idObj.toString());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Identifiant de produit invalide : " + idObj,
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        panierService.supprimerDuPanier(produitId);
        rafraichirPanier();
    }

    private void validerCommandeDepuisPanier() {
        try {
            Panier panier = panierService.getPanierCourant();
            if (panier.estVide()) {
                JOptionPane.showMessageDialog(this,
                        "Le panier est vide. Ajoutez au moins un produit avant de valider.",
                        "Panier vide",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String prenom = txtPrenom.getText().trim();
            String nom = txtNom.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String adresseCompl = txtAdresseCompl.getText().trim();
            String numCarte = txtNumeroCarte.getText().trim();
            String exp = txtDateExpiration.getText().trim();
            String cvv = txtCodeVerif.getText().trim();

            if (prenom.isEmpty() || nom.isEmpty() || adresse.isEmpty()) {
                throw new IllegalArgumentException("Prénom, nom et adresse de livraison sont obligatoires.");
            }

            validerInfosPaiement(numCarte, exp, cvv);

            String adresseFinale = adresseCompl.isEmpty()
                    ? adresse
                    : adresse + " - " + adresseCompl;

            Acheteur acheteur = new Acheteur(prenom, nom, adresseFinale);
            InformationCarte infosCarte = new InformationCarte(numCarte, exp, cvv);

            String recap = construireRecapitulatifCommandePrevisionnel(acheteur, infosCarte, panier);
            int confirm = afficherFenetreConfirmation(recap);
            if (confirm != JOptionPane.OK_OPTION) {
                return;
            }

            Commande commande = commandeService.passerCommandeDepuisPanier(panier, acheteur, infosCarte);
            Paiement paiement = commande.getPaiement();

            String recapFinal = construireRecapitulatifApresValidation(commande, paiement, acheteur);
            txtResult.setText(recapFinal);
            txtResult.setCaretPosition(0);

            panier.vider();
            rafraichirPanier();

            JOptionPane.showMessageDialog(this,
                    "Commande validée avec succès.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (StockInsuffisantException ex) {
            LOGGER.log(Level.WARNING, "Stock insuffisant lors de la validation de commande", ex);
            JOptionPane.showMessageDialog(this,
                    "Stock insuffisant pour l'un des produits du panier : " + ex.getMessage(),
                    "Stock insuffisant",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.INFO, "Saisie invalide lors de la validation de commande", ex);
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Saisie invalide",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la validation de la commande", ex);
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la validation de la commande : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        txtPrenom.setText("");
        txtNom.setText("");
        txtAdresse.setText("");
        txtAdresseCompl.setText("");
        txtNumeroCarte.setText("");
        txtDateExpiration.setText("");
        txtCodeVerif.setText("");
        panierService.getPanierCourant().vider();
        rafraichirPanier();
        txtResult.setText("");
        mettreAJourEtatBoutons();
    }

    // ------------------------------------------------------------------
    // GESTION ÉTAT BOUTON / STOCK / ÉCOUTEURS
    // ------------------------------------------------------------------

    private void mettreAJourStockInfo() {
        Produit produit = listProduits.getSelectedValue();
        if (produit == null) {
            lblStockInfo.setText("Stock disponible : -");
            return;
        }
        Panier panier = panierService.getPanierCourant();
        int dejaDansPanier = panier.getLignes().stream()
                .filter(lc -> lc.getProduit().getId() == produit.getId())
                .mapToInt(LigneCommande::getQuantite)
                .sum();
        int restant = produit.getStock() - dejaDansPanier;
        if (restant < 0) restant = 0;
        lblStockInfo.setText("Stock disponible : " + produit.getStock()
                + " | max ajout possible : " + restant);
    }

    private void mettreAJourEtatBoutons() {
        boolean panierVide = panierService.getPanierCourant().estVide();

        boolean champsClientOk =
                !txtPrenom.getText().trim().isEmpty() &&
                !txtNom.getText().trim().isEmpty() &&
                !txtAdresse.getText().trim().isEmpty();

        boolean champsCarteOk =
                !txtNumeroCarte.getText().trim().isEmpty() &&
                !txtDateExpiration.getText().trim().isEmpty() &&
                !txtCodeVerif.getText().trim().isEmpty();

        btnValiderCommande.setEnabled(!panierVide && champsClientOk && champsCarteOk);
    }

    private void ajouterEcouteursChampsClient() {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { mettreAJourEtatBoutons(); }
            @Override public void removeUpdate(DocumentEvent e) { mettreAJourEtatBoutons(); }
            @Override public void changedUpdate(DocumentEvent e) { mettreAJourEtatBoutons(); }
        };

        // Champs identité / adresse
        txtPrenom.getDocument().addDocumentListener(dl);
        txtNom.getDocument().addDocumentListener(dl);
        txtAdresse.getDocument().addDocumentListener(dl);

        // Champs carte bancaire
        txtNumeroCarte.getDocument().addDocumentListener(dl);
        txtDateExpiration.getDocument().addDocumentListener(dl);
        txtCodeVerif.getDocument().addDocumentListener(dl);
    }

    // ------------------------------------------------------------------
    // HELPERS UI / VALIDATION
    // ------------------------------------------------------------------

    private static JLabel labelMuted(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(55, 65, 81));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, FONT_BASE));
        return l;
    }

    private static JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFocusPainted(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setFont(b.getFont().deriveFont(Font.BOLD, FONT_BASE));
        return b;
    }

    private static String formatPrix(BigDecimal prix) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return nf.format(prix);
    }

    private void validerInfosPaiement(String numCarte, String exp, String cvv) {
        String digits = numCarte.replaceAll("\\s+", "");
        if (!digits.matches("\\d{16}")) {
            throw new IllegalArgumentException("Le numéro de carte doit contenir exactement 16 chiffres.");
        }

        if (!exp.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("La date d'expiration doit être au format MM/AA (ex : 03/27).");
        }

        if (!cvv.matches("\\d{3}")) {
            throw new IllegalArgumentException("Le code CVV doit contenir exactement 3 chiffres.");
        }
    }

    private static String masquerNumeroCarte(String numero) {
        if (numero == null || numero.isBlank()) return "****";
        String digits = numero.replaceAll("\\s+", "");
        if (digits.length() <= 4) return "****";
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }

    private String construireRecapitulatifCommandePrevisionnel(Acheteur acheteur,
                                                               InformationCarte infosCarte,
                                                               Panier panier) {
        StringBuilder sb = new StringBuilder();
        sb.append("Récapitulatif de votre commande\n");
        sb.append("Date : ").append(LocalDateTime.now()).append("\n\n");

        sb.append("Acheteur\n");
        sb.append("--------\n");
        sb.append(acheteur.getPrenom()).append(" ").append(acheteur.getNom()).append("\n");
        sb.append(acheteur.getAdresseLivraison()).append("\n\n");

        sb.append("Détail du panier\n");
        sb.append("---------------\n");
        for (LigneCommande lc : panier.getLignes()) {
            sb.append("- ")
                    .append(lc.getProduit().getLibelle())
                    .append(" | QTE=").append(lc.getQuantite())
                    .append(" | PU=").append(formatPrix(lc.getProduit().getPrixUnitaire()))
                    .append(" | MONTANT=").append(formatPrix(lc.calculerMontant()))
                    .append("\n");
        }
        sb.append("\nTotal à payer : ").append(formatPrix(panier.calculerTotal())).append("\n\n");

        sb.append("Paiement\n");
        sb.append("--------\n");
        sb.append("Carte : ").append(masquerNumeroCarte(infosCarte.getNumeroCarte())).append("\n");
        sb.append("Expiration : ").append(infosCarte.getDateExpiration()).append("\n");
        sb.append("(Le CVV n'est jamais affiché)").append("\n");

        return sb.toString();
    }

    private int afficherFenetreConfirmation(String recap) {
        JTextArea area = new JTextArea(recap, 20, 60);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);

        return JOptionPane.showConfirmDialog(
                this,
                scroll,
                "Confirmation de la commande",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String construireRecapitulatifApresValidation(Commande commande,
                                                          Paiement paiement,
                                                          Acheteur acheteur) {
        StringBuilder sb = new StringBuilder();
        sb.append("✔ Commande validée\n");
        sb.append("==================\n\n");

        sb.append("Commande\n");
        sb.append("--------\n");
        sb.append("N° : ").append(commande.getId()).append("\n");
        sb.append("Date : ").append(commande.getDateCreation()).append("\n\n");

        sb.append("Acheteur\n");
        sb.append("--------\n");
        sb.append(acheteur.getPrenom()).append(" ").append(acheteur.getNom()).append("\n");
        sb.append(acheteur.getAdresseLivraison()).append("\n\n");

        sb.append("Lignes de commande\n");
        sb.append("------------------\n");
        for (LigneCommande lc : commande.getLignes()) {
            sb.append("- ")
                    .append(lc.getProduit().getLibelle())
                    .append(" | QTE=").append(lc.getQuantite())
                    .append(" | PU=").append(formatPrix(lc.getProduit().getPrixUnitaire()))
                    .append(" | MONTANT=").append(formatPrix(lc.calculerMontant()))
                    .append("\n");
        }
        sb.append("\nTotal : ").append(formatPrix(commande.calculerTotal())).append("\n\n");

        sb.append("Paiement\n");
        sb.append("--------\n");
        sb.append("Transaction : ").append(paiement.getTransactionId()).append("\n");
        sb.append("Montant     : ").append(formatPrix(paiement.getMontant())).append("\n");
        sb.append("Date        : ").append(paiement.getDateCreation()).append("\n");
        sb.append("Carte       : ").append(masquerNumeroCarte(paiement.getInfosCarte().getNumeroCarte())).append("\n");
        sb.append("Expiration  : ").append(paiement.getInfosCarte().getDateExpiration()).append("\n");
        sb.append("(Le CVV n'est pas affiché)").append("\n");

        return sb.toString();
    }

    // ------------------------------------------------------------------
    // RENDERER LISTE PRODUITS
    // ------------------------------------------------------------------

    private static class ProduitListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Produit p) {
                l.setText(
                        "[" + p.getId() + "] " +
                        p.getLibelle() +
                        " — " + p.getStock() + " en stock — " +
                        formatPrix(p.getPrixUnitaire())
                );
            }
            l.setFont(l.getFont().deriveFont(Font.PLAIN, FONT_BASE));
            l.setForeground(COLOR_TEXT_DARK);
            return l;
        }
    }
}
