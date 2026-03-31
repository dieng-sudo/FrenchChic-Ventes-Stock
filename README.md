# 🛍️ FrenchChic — Gestion Ventes & Stock

> Application desktop Java (Swing) de gestion commerciale pour une boutique de mode — produits, commandes, paiements, stock et clients.

---

## 🧠 Overview

**FrenchChic** est une application de bureau développée en Java avec une interface graphique Swing. Elle permet à une boutique de gérer l'ensemble de son activité commerciale : catalogue produits, suivi des commandes, gestion des paiements et contrôle du stock, le tout connecté à une base de données MySQL.

---

## 📁 Structure du projet

```
FrenchChicVentesStock/
├── src/main/java/
│   ├── Controleur/         # Point d'entrée & interface graphique Swing
│   │   ├── Main.java
│   │   └── MainFrame.java
│   └── Metier/             # Logique métier & accès données
│       ├── Produit.java
│       ├── Commande.java
│       ├── LigneCommande.java
│       ├── Panier.java
│       ├── Paiement.java
│       ├── Acheteur.java
│       ├── StatutCommande.java
│       ├── StockInsuffisantException.java
│       ├── *Repository.java         # Interfaces
│       ├── *RepositoryJdbc.java     # Implémentations MySQL
│       ├── *RepositoryInMemory.java # Implémentations en mémoire
│       └── *Service*.java           # Services métier
├── pom.xml                 # Configuration Maven
└── nbactions.xml           # Configuration NetBeans
```

---

## ✨ Fonctionnalités

| Module | Description |
|---|---|
| 🏷️ **Produits** | Ajout, modification, suppression et consultation du catalogue |
| 📦 **Stock** | Suivi des quantités, alertes stock insuffisant |
| 🛒 **Commandes** | Création de commandes, gestion des lignes et statuts |
| 💳 **Paiements** | Enregistrement et suivi des paiements par carte |
| 👤 **Clients** | Gestion des acheteurs et de leurs informations |

---

## 🏗️ Architecture

Le projet suit une architecture **3 couches** :

```
Controleur (Swing UI)
      ↓
Metier (Services & Interfaces)
      ↓
Repository (JDBC MySQL / InMemory)
```

Les implémentations `InMemory` permettent de tester sans base de données, les implémentations `Jdbc` se connectent à MySQL en production.

---

## 🛠️ Technologies

| Technologie | Usage |
|---|---|
| Java | Langage principal |
| Swing | Interface graphique desktop |
| JDBC | Connexion base de données |
| MySQL | Base de données |
| Maven | Gestion des dépendances |

---

## 🚀 Installation & Lancement

### Prérequis

- Java JDK 11+
- Maven 3.6+
- MySQL 8+

### 1. Cloner le repo

```bash
git clone https://github.com/dieng-sudo/FrenchChic-Ventes-Stock.git
cd FrenchChic-Ventes-Stock
```

### 2. Configurer la base de données

Créer une base MySQL et importer le schéma :

```sql
CREATE DATABASE frenchchic;
```

Puis mettre à jour les identifiants de connexion dans le fichier de configuration JDBC.

### 3. Compiler et lancer

```bash
cd FrenchChicVentesStock
mvn clean install
mvn exec:java -Dexec.mainClass="Controleur.Main"
```

---

## 👥 Auteurs

- **MALLET**
- **DIENG**

---

## 📄 Licence

MIT
