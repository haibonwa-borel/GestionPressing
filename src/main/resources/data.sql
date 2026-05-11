-- =======================================================
-- DONNEES DE TEST : chargees automatiquement au demarrage
-- =======================================================

-- Utilisateurs
INSERT OR IGNORE INTO utilisateurs (id, nom, prenom, email, mot_de_passe, telephone)
VALUES (1, 'Dupont', 'Jean', 'jean@email.com', 'password123', '+33612345678');
INSERT OR IGNORE INTO utilisateurs (id, nom, prenom, email, mot_de_passe, telephone)
VALUES (2, 'Martin', 'Marie', 'marie@email.com', 'password123', '+33788776655');
INSERT OR IGNORE INTO utilisateurs (id, nom, prenom, email, mot_de_passe, telephone)
VALUES (3, 'Durand', 'Pierre', 'pierre@email.com', 'password123', '+237670001122');

-- Vetements
INSERT OR IGNORE INTO vetements (id, sku, description, categorie, couleur, code_couleur)
VALUES (1, 'VET-00000001', 'Chemise blanche', 'HAUT', 'Blanc', '#FFFFFF');
INSERT OR IGNORE INTO vetements (id, sku, description, categorie, couleur, code_couleur)
VALUES (2, 'VET-00000002', 'Pantalon noir', 'BAS', 'Noir', '#000000');
INSERT OR IGNORE INTO vetements (id, sku, description, categorie, couleur, code_couleur)
VALUES (3, 'VET-00000003', 'Robe rouge', 'ROBE', 'Rouge', '#FF0000');
INSERT OR IGNORE INTO vetements (id, sku, description, categorie, couleur, code_couleur)
VALUES (4, 'VET-00000004', 'Veste en cuir', 'MANTEAU', 'Marron', '#8B4513');
INSERT OR IGNORE INTO vetements (id, sku, description, categorie, couleur, code_couleur)
VALUES (5, 'VET-00000005', 'Jean bleu', 'BAS', 'Bleu', '#0000FF');

-- Commandes
INSERT OR IGNORE INTO commandes (id, type, statut, date_creation, date_livraison, prix_total, utilisateur_id)
VALUES (1, 'NORMAL', 'EN_ATTENTE', '2026-05-07 10:00:00', '2026-05-14 10:00:00', 2000.0, 1);
INSERT OR IGNORE INTO commandes (id, type, statut, date_creation, date_livraison, prix_total, utilisateur_id)
VALUES (2, 'RAPIDE', 'EN_COURS', '2026-05-07 10:00:00', '2026-05-11 10:00:00', 4500.0, 1);

-- Table de jointure Many-to-Many
INSERT OR IGNORE INTO commande_vetement (commande_id, vetement_id) VALUES (1, 1);
INSERT OR IGNORE INTO commande_vetement (commande_id, vetement_id) VALUES (1, 2);
INSERT OR IGNORE INTO commande_vetement (commande_id, vetement_id) VALUES (2, 3);
INSERT OR IGNORE INTO commande_vetement (commande_id, vetement_id) VALUES (2, 4);
INSERT OR IGNORE INTO commande_vetement (commande_id, vetement_id) VALUES (2, 5);
