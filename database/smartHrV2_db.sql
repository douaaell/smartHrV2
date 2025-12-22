-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mar. 22 déc. 2025
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `smartHrV2_db` 
--

-- --------------------------------------------------------
--
-- Structure de la table `employe` 
--
CREATE TABLE `employe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) DEFAULT NULL,
  `prenom` varchar(50) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) DEFAULT NULL,
  `departement` varchar(50) DEFAULT NULL,
  `position` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------
--
-- Structure de la table `hr` 
--
CREATE TABLE `hr` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) DEFAULT NULL,
  `prenom` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `departement` varchar(50) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `department_managed` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------
--
-- Structure de la table `conge` 
--
CREATE TABLE `conge` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `employee_id` int(11) NOT NULL,
  `type` ENUM('ANNUEL', 'MALADIE', 'MATERNITE', 'PATERNITE', 'SANS_SOLDE', 'FORMATION', 'AUTRE') NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `statut` ENUM('EN_ATTENTE', 'VALIDE', 'REFUSE', 'ANNULE') DEFAULT 'EN_ATTENTE',
  `motif` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_employee_conge` (`employee_id`),
  CONSTRAINT `fk_employee_conge` FOREIGN KEY (`employee_id`) REFERENCES `employe` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------
--
-- Structure de la table `document` 
--
CREATE TABLE `document` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `chemin` varchar(255) NOT NULL,
  `date_upload` timestamp DEFAULT CURRENT_TIMESTAMP,
  `employee_email` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_document_employee` (`employee_email`),
  CONSTRAINT `fk_document_employee` FOREIGN KEY (`employee_email`) REFERENCES `employe` (`email`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------
--
-- Structure de la table `contrat` 
--
CREATE TABLE `contrat` (
  `id` VARCHAR(50) NOT NULL,
  `employee_email` VARCHAR(100) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `salary` DOUBLE NOT NULL,
  `position` VARCHAR(50),
  `department` VARCHAR(50),
  `benefits` VARCHAR(255),
  `status` ENUM('ACTIF','SUSPENDU','TERMINE') DEFAULT 'ACTIF',
  `file_name_pdf` VARCHAR(255),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_contrat_employee` FOREIGN KEY (`employee_email`) REFERENCES `employe` (`email`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Index pour les tables déchargées
--
ALTER TABLE `conge` 
  ADD KEY `employee_id` (`employee_id`);

ALTER TABLE `employe` 
  ADD UNIQUE (`email`);

-- --------------------------------------------------------
-- AUTO_INCREMENT pour les tables déchargées
--
ALTER TABLE `conge` 
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `document` 
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `employe` 
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `hr` 
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;