CREATE DATABASE IF NOT EXISTS `coaches`;

USE `coaches`;

CREATE TABLE IF NOT EXISTS `inputdata` (
	`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	`directory` TEXT NOT NULL,
	`mongoId` TEXT NOT NULL,
	`firstName` TEXT NOT NULL,
	`lastName` TEXT NOT NULL,
	`title` TEXT NOT NULL,
	`sport` TEXT NOT NULL,
	`gender` TEXT NOT NULL,
	`nameFromDirectory` TEXT NOT NULL,
	`isVisited` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`)
)
	COLLATE='utf8_general_ci'
	ENGINE=InnoDB;

CREATE TABLE `results` (
	`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	`inputDataId` BIGINT UNSIGNED NOT NULL,
	`coachfound` TINYINT NOT NULL,
	`detailsAboutCoachUrl` VARCHAR(1000) NULL,
	`email` VARCHAR(1000) NULL,
	`biography` LONGTEXT NULL,
	`image` LONGBLOB NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `FK__inputdata` FOREIGN KEY (`inputDataId`) REFERENCES `inputdata` (`id`)
)
	COLLATE='utf8_general_ci'
	ENGINE=InnoDB;