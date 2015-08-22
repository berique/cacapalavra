CREATE TABLE IF NOT EXISTS `pais` (
  `id` int(11) NOT NULL,
  `nome` varchar(60) DEFAULT NULL,
  `sigla` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `estado` (
  `id` int(11) NOT NULL,
  `nome` varchar(75) DEFAULT NULL,
  `uf` varchar(5) DEFAULT NULL,
  `pais` int(7) DEFAULT NULL,
  PRIMARY KEY (`id`),
);

ALTER TABLE `estado` ADD FOREIGN KEY (`pais`)  REFERENCES `pais`(`id`);

CREATE TABLE IF NOT EXISTS `cidade` (
  `id` int(11) NOT NULL,
  `nome` varchar(120) DEFAULT NULL,
  `estado` int(5) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE `cidade` ADD FOREIGN KEY (`estado`)  REFERENCES `estado`(`id`);