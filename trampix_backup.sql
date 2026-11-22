-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: trampix
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `trampix`
--

/*!40000 DROP DATABASE IF EXISTS `trampix`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `trampix` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `trampix`;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ativa` bit(1) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `nome` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categoria_nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,_binary '',NULL,'Pedreiro'),(2,_binary '',NULL,'Pintor'),(3,_binary '',NULL,'Encanador'),(4,_binary '',NULL,'Eletricista'),(5,_binary '',NULL,'Marceneiro e Carpinteiro'),(6,_binary '',NULL,'Marido de Aluguel'),(7,_binary '',NULL,'Gesseiro e Drywall'),(8,_binary '',NULL,'Climatização'),(9,_binary '',NULL,'Vidraceiro e Serralheiro');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contratacao`
--

DROP TABLE IF EXISTS `contratacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contratacao` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `atualizado_em` datetime(6) NOT NULL,
  `contraproposta_em` datetime(6) DEFAULT NULL,
  `criado_em` datetime(6) NOT NULL,
  `data_fim` date NOT NULL,
  `data_inicio` date NOT NULL,
  `dias_contados` int DEFAULT NULL,
  `hora_fim` time DEFAULT NULL,
  `hora_inicio` time DEFAULT NULL,
  `motivo_contraproposta` varchar(500) DEFAULT NULL,
  `motivo_recusa` varchar(500) DEFAULT NULL,
  `observacao` varchar(1000) DEFAULT NULL,
  `preco_combinado` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `cliente_id` bigint NOT NULL,
  `servico_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbqgfww0cf2e0pg7sxtyjo0thk` (`cliente_id`),
  KEY `FKnvwhjvn0wqjr5ufhiivg74fsg` (`servico_id`),
  CONSTRAINT `FKbqgfww0cf2e0pg7sxtyjo0thk` FOREIGN KEY (`cliente_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKnvwhjvn0wqjr5ufhiivg74fsg` FOREIGN KEY (`servico_id`) REFERENCES `servico` (`id`),
  CONSTRAINT `contratacao_chk_1` CHECK ((`status` in (_utf8mb4'PENDENTE',_utf8mb4'CONTRAPROPOSTA',_utf8mb4'ACEITA',_utf8mb4'RECUSADA',_utf8mb4'CONCLUIDA',_utf8mb4'CANCELADA',_utf8mb4'EXPIRADA')))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contratacao`
--

LOCK TABLES `contratacao` WRITE;
/*!40000 ALTER TABLE `contratacao` DISABLE KEYS */;
INSERT INTO `contratacao` VALUES (1,'2026-11-19 15:13:30.461834',NULL,'2026-11-19 15:10:19.951685','2026-12-11','2026-12-11',1,NULL,NULL,NULL,NULL,'lavar bem',150.00,'CANCELADA',11,19),(2,'2026-11-19 15:15:00.946436',NULL,'2026-11-19 15:14:15.308319','2026-11-19','2026-11-19',1,NULL,NULL,NULL,NULL,'lavar bem',150.00,'CONCLUIDA',11,19);
/*!40000 ALTER TABLE `contratacao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contratacao_historico`
--

DROP TABLE IF EXISTS `contratacao_historico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contratacao_historico` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `criado_em` datetime(6) NOT NULL,
  `data_fim` date NOT NULL,
  `data_inicio` date NOT NULL,
  `dias_contados` int DEFAULT NULL,
  `hora_fim` time DEFAULT NULL,
  `hora_inicio` time DEFAULT NULL,
  `motivo` varchar(500) DEFAULT NULL,
  `preco_combinado` decimal(10,2) NOT NULL,
  `status_anterior` varchar(20) DEFAULT NULL,
  `status_novo` varchar(20) NOT NULL,
  `autor_id` bigint DEFAULT NULL,
  `contratacao_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKktd1heic24k8ctg55gh247wp0` (`autor_id`),
  KEY `FK7br88dmevsri6qrut1hnwm7b5` (`contratacao_id`),
  CONSTRAINT `FK7br88dmevsri6qrut1hnwm7b5` FOREIGN KEY (`contratacao_id`) REFERENCES `contratacao` (`id`),
  CONSTRAINT `FKktd1heic24k8ctg55gh247wp0` FOREIGN KEY (`autor_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `contratacao_historico_chk_1` CHECK ((`status_anterior` in (_utf8mb4'PENDENTE',_utf8mb4'CONTRAPROPOSTA',_utf8mb4'ACEITA',_utf8mb4'RECUSADA',_utf8mb4'CONCLUIDA',_utf8mb4'CANCELADA',_utf8mb4'EXPIRADA'))),
  CONSTRAINT `contratacao_historico_chk_2` CHECK ((`status_novo` in (_utf8mb4'PENDENTE',_utf8mb4'CONTRAPROPOSTA',_utf8mb4'ACEITA',_utf8mb4'RECUSADA',_utf8mb4'CONCLUIDA',_utf8mb4'CANCELADA',_utf8mb4'EXPIRADA')))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contratacao_historico`
--

LOCK TABLES `contratacao_historico` WRITE;
/*!40000 ALTER TABLE `contratacao_historico` DISABLE KEYS */;
INSERT INTO `contratacao_historico` VALUES (1,'2026-11-19 15:10:19.954200','2026-12-11','2026-12-11',1,NULL,NULL,NULL,150.00,NULL,'PENDENTE',11,1),(2,'2026-11-19 15:12:30.663156','2026-12-11','2026-12-11',1,NULL,NULL,NULL,150.00,'PENDENTE','ACEITA',2,1),(3,'2026-11-19 15:13:30.463528','2026-12-11','2026-12-11',1,NULL,NULL,NULL,150.00,'ACEITA','CANCELADA',11,1),(4,'2026-11-19 15:14:15.310374','2026-11-19','2026-11-19',1,NULL,NULL,NULL,150.00,NULL,'PENDENTE',11,2),(5,'2026-11-19 15:14:39.724847','2026-11-19','2026-11-19',1,NULL,NULL,NULL,150.00,'PENDENTE','ACEITA',2,2),(6,'2026-11-19 15:15:00.949352','2026-11-19','2026-11-19',1,NULL,NULL,NULL,150.00,'ACEITA','CONCLUIDA',2,2);
/*!40000 ALTER TABLE `contratacao_historico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disponibilidade`
--

DROP TABLE IF EXISTS `disponibilidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disponibilidade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dia_semana` varchar(10) NOT NULL,
  `hora_fim` time NOT NULL,
  `hora_inicio` time NOT NULL,
  `prestador_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfr0da8qydx33q62px8ja0loj1` (`prestador_id`),
  CONSTRAINT `FKfr0da8qydx33q62px8ja0loj1` FOREIGN KEY (`prestador_id`) REFERENCES `prestador_perfil` (`id`),
  CONSTRAINT `disponibilidade_chk_1` CHECK ((`dia_semana` in (_utf8mb4'SEGUNDA',_utf8mb4'TERCA',_utf8mb4'QUARTA',_utf8mb4'QUINTA',_utf8mb4'SEXTA',_utf8mb4'SABADO',_utf8mb4'DOMINGO')))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disponibilidade`
--

LOCK TABLES `disponibilidade` WRITE;
/*!40000 ALTER TABLE `disponibilidade` DISABLE KEYS */;
INSERT INTO `disponibilidade` VALUES (1,'SEGUNDA','17:00:00','08:00:00',1),(2,'TERCA','17:00:00','08:00:00',1),(3,'QUARTA','17:00:00','08:00:00',1),(4,'QUINTA','17:00:00','08:00:00',1),(5,'SEXTA','17:00:00','08:00:00',1);
/*!40000 ALTER TABLE `disponibilidade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prestador_perfil`
--

DROP TABLE IF EXISTS `prestador_perfil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prestador_perfil` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agenda_cheia` bit(1) NOT NULL,
  `biografia` varchar(1000) DEFAULT NULL,
  `criado_em` datetime(6) NOT NULL,
  `raio_atendimento_km` int DEFAULT NULL,
  `verificado` bit(1) NOT NULL,
  `verificado_em` datetime(6) DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  `verificado_por_id` bigint DEFAULT NULL,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2daqitxgc067oa88675anwbtb` (`usuario_id`),
  KEY `FK4po1i085u6alhboxikbcvjpi7` (`verificado_por_id`),
  CONSTRAINT `FK4po1i085u6alhboxikbcvjpi7` FOREIGN KEY (`verificado_por_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKn8o2o0un2h7b4wch1a62j48rs` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `prestador_perfil_chk_1` CHECK ((`raio_atendimento_km` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prestador_perfil`
--

LOCK TABLES `prestador_perfil` WRITE;
/*!40000 ALTER TABLE `prestador_perfil` DISABLE KEYS */;
INSERT INTO `prestador_perfil` VALUES (1,_binary '\0','Pedreiro profissional, especializado em fundações e alvenaria.','2026-11-17 22:09:21.030208',30,_binary '\0',NULL,2,NULL,1),(2,_binary '\0','Pintor residencial e comercial.','2026-11-17 22:09:21.345528',30,_binary '\0',NULL,3,NULL,1),(3,_binary '\0','Encanador com experiência em instalações hidráulicas.','2026-11-17 22:09:21.624228',30,_binary '\0',NULL,4,NULL,1),(4,_binary '\0','Eletricista especializado em instalações residenciais.','2026-11-17 22:09:21.913996',30,_binary '\0',NULL,5,NULL,1),(5,_binary '\0','Marceneiro especializado em móveis e montagem.','2026-11-17 22:09:22.253450',30,_binary '\0',NULL,6,NULL,1),(6,_binary '\0','Profissional de pequenos reparos e instalações.','2026-11-17 22:09:22.561838',30,_binary '\0',NULL,7,NULL,1),(7,_binary '\0','Gesseiro especializado em forros e drywall.','2026-11-17 22:09:22.833900',30,_binary '\0',NULL,8,NULL,1),(8,_binary '\0','Técnico especializado em climatização.','2026-11-17 22:09:23.123344',30,_binary '\0',NULL,9,NULL,1),(9,_binary '\0','Vidraceiro e serralheiro com experiência em instalações.','2026-11-17 22:09:23.444595',30,_binary '\0',NULL,10,NULL,1);
/*!40000 ALTER TABLE `prestador_perfil` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servico`
--

DROP TABLE IF EXISTS `servico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servico` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `atualizado_em` datetime(6) NOT NULL,
  `criado_em` datetime(6) NOT NULL,
  `descricao` varchar(2000) NOT NULL,
  `preco` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `tipo_cobranca` varchar(20) NOT NULL,
  `titulo` varchar(120) NOT NULL,
  `categoria_id` bigint NOT NULL,
  `prestador_id` bigint NOT NULL,
  `subcategoria_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3s3u441gpubcnj5g69hqim1p3` (`categoria_id`),
  KEY `FKhvxtgx864xr17eumiu7u96cua` (`prestador_id`),
  KEY `FKqca1pyky1463e1kxsoad1cobp` (`subcategoria_id`),
  CONSTRAINT `FK3s3u441gpubcnj5g69hqim1p3` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`),
  CONSTRAINT `FKhvxtgx864xr17eumiu7u96cua` FOREIGN KEY (`prestador_id`) REFERENCES `prestador_perfil` (`id`),
  CONSTRAINT `FKqca1pyky1463e1kxsoad1cobp` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategoria` (`id`),
  CONSTRAINT `servico_chk_1` CHECK ((`status` in (_utf8mb4'ATIVO',_utf8mb4'PAUSADO',_utf8mb4'INATIVO'))),
  CONSTRAINT `servico_chk_2` CHECK ((`tipo_cobranca` in (_utf8mb4'POR_HORA',_utf8mb4'POR_DIA',_utf8mb4'POR_M2',_utf8mb4'POR_TRABALHO')))
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servico`
--

LOCK TABLES `servico` WRITE;
/*!40000 ALTER TABLE `servico` DISABLE KEYS */;
INSERT INTO `servico` VALUES (1,'2026-11-17 22:09:21.035518','2026-11-17 22:09:21.035541','Execução de fundações, sapatas, vigas e pilares.',1800.00,'ATIVO','POR_TRABALHO','Construção de fundações',1,1,1),(2,'2026-11-17 22:09:21.092342','2026-11-17 22:09:21.092368','Construção de paredes, muros e divisórias.',85.00,'ATIVO','POR_M2','Serviços de alvenaria',1,1,2),(3,'2026-11-17 22:09:21.348187','2026-11-17 22:09:21.348204','Pintura interna e externa com acabamento profissional.',25.00,'ATIVO','POR_M2','Pintura de paredes',2,2,7),(4,'2026-11-17 22:09:21.387434','2026-11-17 22:09:21.387457','Preparação e nivelamento de paredes.',18.00,'ATIVO','POR_M2','Aplicação de massa corrida',2,2,8),(5,'2026-11-17 22:09:21.626565','2026-11-17 22:09:21.626582','Instalação e manutenção de tubulações de água e esgoto.',90.00,'ATIVO','POR_HORA','Instalação hidráulica',3,3,12),(6,'2026-11-17 22:09:21.663141','2026-11-17 22:09:21.663161','Reparo de torneiras, registros, sifões e descargas.',120.00,'ATIVO','POR_TRABALHO','Reparo de vazamentos',3,3,13),(7,'2026-11-17 22:09:21.916064','2026-11-17 22:09:21.916075','Instalação e manutenção de fiação elétrica.',100.00,'ATIVO','POR_HORA','Instalação elétrica',4,4,17),(8,'2026-11-17 22:09:21.947894','2026-11-17 22:09:21.947937','Instalação de disjuntores e organização de quadros.',450.00,'ATIVO','POR_TRABALHO','Montagem de quadro elétrico',4,4,18),(9,'2026-11-17 22:09:22.258617','2026-11-17 22:09:22.258638','Manutenção de armários, portas e gavetas.',80.00,'ATIVO','POR_HORA','Reparo de móveis',5,5,22),(10,'2026-11-17 22:09:22.306790','2026-11-17 22:09:22.306840','Montagem e instalação de móveis residenciais.',180.00,'ATIVO','POR_TRABALHO','Montagem de móveis',5,5,23),(11,'2026-11-17 22:09:22.564061','2026-11-17 22:09:22.564077','Instalação de suportes, quadros e cortinas.',100.00,'ATIVO','POR_TRABALHO','Instalação de suporte de TV',6,6,26),(12,'2026-11-17 22:09:22.596387','2026-11-17 22:09:22.596409','Troca de puxadores, dobradiças e trincos.',70.00,'ATIVO','POR_TRABALHO','Pequenos ajustes residenciais',6,6,27),(13,'2026-11-17 22:09:22.836100','2026-11-17 22:09:22.836117','Instalação de forros de gesso e acabamento.',95.00,'ATIVO','POR_M2','Instalação de forro',7,7,30),(14,'2026-11-17 22:09:22.864018','2026-11-17 22:09:22.864037','Construção de paredes e divisórias em drywall.',110.00,'ATIVO','POR_M2','Instalação de drywall',7,7,31),(15,'2026-11-17 22:09:23.125182','2026-11-17 22:09:23.125195','Instalação de equipamentos de ar-condicionado split.',650.00,'ATIVO','POR_TRABALHO','Instalação de ar-condicionado',8,8,33),(16,'2026-11-17 22:09:23.160303','2026-11-17 22:09:23.160326','Higienização de filtros e componentes.',180.00,'ATIVO','POR_TRABALHO','Limpeza de ar-condicionado',8,8,34),(17,'2026-11-17 22:09:23.447076','2026-11-17 22:09:23.447089','Instalação de box e vidros temperados.',550.00,'ATIVO','POR_TRABALHO','Instalação de box de vidro',9,9,36),(18,'2026-11-17 22:09:23.473805','2026-11-17 22:09:23.473824','Fabricação e reparo de grades e corrimãos.',250.00,'ATIVO','POR_TRABALHO','Fabricação de estruturas metálicas',9,9,37),(19,'2026-11-19 15:08:08.817779','2026-11-19 15:08:08.817821','Lavo carros e passo',150.00,'ATIVO','POR_TRABALHO','Lavagem de carro',6,1,27),(20,'2026-11-19 15:08:40.014835','2026-11-19 15:08:31.988102','Lavo e passo',150.00,'INATIVO','POR_TRABALHO','Lavagem de carro',4,1,20);
/*!40000 ALTER TABLE `servico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subcategoria`
--

DROP TABLE IF EXISTS `subcategoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subcategoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `criado_em` datetime(6) NOT NULL,
  `descricao` varchar(200) DEFAULT NULL,
  `nome` varchar(40) NOT NULL,
  `nome_normalizado` varchar(40) NOT NULL,
  `padrao` bit(1) NOT NULL,
  `status` varchar(10) NOT NULL,
  `categoria_id` bigint NOT NULL,
  `criada_por_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subcategoria_categoria_nome` (`categoria_id`,`nome_normalizado`),
  KEY `FK8epb9x0lvgke59d469rpcnqmo` (`criada_por_id`),
  CONSTRAINT `FK8epb9x0lvgke59d469rpcnqmo` FOREIGN KEY (`criada_por_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKmh7cb7m4a5vvvhg5js699qnxk` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`),
  CONSTRAINT `subcategoria_chk_1` CHECK ((`status` in (_utf8mb4'ATIVA',_utf8mb4'INATIVA')))
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subcategoria`
--

LOCK TABLES `subcategoria` WRITE;
/*!40000 ALTER TABLE `subcategoria` DISABLE KEYS */;
INSERT INTO `subcategoria` VALUES (1,'2026-11-17 21:15:16.353427','Sapata, bloco, alicerce, viga e pilar.','Fundação','fundacao',_binary '','ATIVA',1,NULL),(2,'2026-11-17 21:15:16.369910','Levantamento de paredes, muros e divisórias.','Alvenaria','alvenaria',_binary '','ATIVA',1,NULL),(3,'2026-11-17 21:15:16.377908','Assentamento de cerâmica, porcelanato e azulejo.','Revestimento','revestimento',_binary '','ATIVA',1,NULL),(4,'2026-11-17 21:15:16.385050','Construção, reforma de coberturas e calhas.','Telhado','telhado',_binary '','ATIVA',1,NULL),(5,'2026-11-17 21:15:16.394200','Quebra de paredes, remoção de entulho e abertura de vãos.','Demolição','demolicao',_binary '','ATIVA',1,NULL),(6,'2026-11-17 21:15:16.401545','Pequenos remendos, reboco, contrapiso e infiltrações.','Reparos','reparos',_binary '','ATIVA',1,NULL),(7,'2026-11-17 21:15:16.418767','Pintura lisa interna, externa e tetos.','Paredes','paredes',_binary '','ATIVA',2,NULL),(8,'2026-11-17 21:15:16.425954','Aplicação de massa corrida, acrílica e lixamento.','Massa','massa',_binary '','ATIVA',2,NULL),(9,'2026-11-17 21:15:16.434427','Grafiato, cimento queimado, marmorato e texturas projetadas.','Textura','textura',_binary '','ATIVA',2,NULL),(10,'2026-11-17 21:15:16.443684','Verniz, seladora e laqueação em portas ou decks.','Madeira','madeira',_binary '','ATIVA',2,NULL),(11,'2026-11-17 21:15:16.453674','Pintura anticorrosiva em portões, grades e esquadrias.','Ferro','ferro',_binary '','ATIVA',2,NULL),(12,'2026-11-17 21:15:16.471205','Tubulações de água fria, água quente e esgoto (obras).','Instalação','instalacao',_binary '','ATIVA',3,NULL),(13,'2026-11-17 21:15:16.479102','Troca de torneira, registro, sifão e reparo de descarga.','Vazamento','vazamento',_binary '','ATIVA',3,NULL),(14,'2026-11-17 21:15:16.488677','Localização eletrônica de vazamento oculto (geofone).','Caça-vazamento','caca-vazamento',_binary '','ATIVA',3,NULL),(15,'2026-11-17 21:15:16.495657','Desentupir pia, ralo, vaso, esgoto e caixa de gordura.','Desentupimento','desentupimento',_binary '','ATIVA',3,NULL),(16,'2026-11-17 21:15:16.505240','Instalação de aquecedor e teste de estanqueidade de gás.','Gás','gas',_binary '','ATIVA',3,NULL),(17,'2026-11-17 21:15:16.522030','Troca de cabos, enfiação geral e balanceamento de carga.','Fiação','fiacao',_binary '','ATIVA',4,NULL),(18,'2026-11-17 21:15:16.531098','Montagem de disjuntores, DR, DPS e caixa de luz.','Quadro','quadro',_binary '','ATIVA',4,NULL),(19,'2026-11-17 21:15:16.541332','Instalação de fitas LED, spots, lustres e luminárias.','Iluminação','iluminacao',_binary '','ATIVA',4,NULL),(20,'2026-11-17 21:15:16.546909','Troca e instalação de tomadas, interruptores e chuveiros.','Tomadas','tomadas',_binary '','ATIVA',4,NULL),(21,'2026-11-17 21:15:16.552293','Conserto de curtos-circuitos e tomadas queimadas.','Manutenção','manutencao',_binary '','ATIVA',4,NULL),(22,'2026-11-17 21:15:16.566855','Reparo e ajuste de portas de armários e gavetas.','Móveis','moveis',_binary '','ATIVA',5,NULL),(23,'2026-11-17 21:15:16.574487','Montagem de painéis, prateleiras e nichos personalizados.','Instalação','instalacao',_binary '','ATIVA',5,NULL),(24,'2026-11-17 21:15:16.581421','Telhados de madeira, decks, pergolados e escadas.','Estruturas','estruturas',_binary '','ATIVA',5,NULL),(25,'2026-11-17 21:15:16.589141','Instalação de portas de madeira, batentes e fechaduras.','Esquadrias','esquadrias',_binary '','ATIVA',5,NULL),(26,'2026-11-17 21:15:16.604490','Instalação de suporte de TV, quadros, cortinas e varais.','Suportes','suportes',_binary '','ATIVA',6,NULL),(27,'2026-11-17 21:15:16.614750','Troca de puxadores, dobradiças e trincos de portas.','Ajustes','ajustes',_binary '','ATIVA',6,NULL),(28,'2026-11-17 21:15:16.623069','Montagem de móveis comprados na internet (guarda-roupas, mesas).','Montagem','montagem',_binary '','ATIVA',6,NULL),(29,'2026-11-17 21:15:16.632080','Troca de lâmpadas, instalação de filtro de água e acessórios de banheiro.','Instalações rápidas','instalacoes rapidas',_binary '','ATIVA',6,NULL),(30,'2026-11-17 21:15:16.648497','Instalação de teto de gesso acartonado ou placas.','Forro','forro',_binary '','ATIVA',7,NULL),(31,'2026-11-17 21:15:16.654034','Paredes divisórias e nichos em gesso acartonado.','Drywall','drywall',_binary '','ATIVA',7,NULL),(32,'2026-11-17 21:15:16.658142','Sancas, molduras, cortineiros e iluminação embutida.','Decoração','decoracao',_binary '','ATIVA',7,NULL),(33,'2026-11-17 21:15:16.666443','Fixação, furação e conexão do sistema split.','Instalação','instalacao',_binary '','ATIVA',8,NULL),(34,'2026-11-17 21:15:16.670272','Higienização química de filtros, evaporadora e condensadora.','Limpeza','limpeza',_binary '','ATIVA',8,NULL),(35,'2026-11-17 21:15:16.674754','Identificação de vazamento de fluido e recarga.','Carga de gás','carga de gas',_binary '','ATIVA',8,NULL),(36,'2026-11-17 21:15:16.683645','Box de banheiro, janelas de vidro temperado e espelhos.','Vidros','vidros',_binary '','ATIVA',9,NULL),(37,'2026-11-17 21:15:16.687954','Solda, fabricação e reparo de portões, grades e corrimão.','Estruturas metálicas','estruturas metalicas',_binary '','ATIVA',9,NULL);
/*!40000 ALTER TABLE `subcategoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `atualizado_em` datetime(6) NOT NULL,
  `bairro` varchar(80) DEFAULT NULL,
  `cep` varchar(8) DEFAULT NULL,
  `cidade` varchar(80) DEFAULT NULL,
  `complemento` varchar(60) DEFAULT NULL,
  `criado_em` datetime(6) NOT NULL,
  `data_nascimento` date DEFAULT NULL,
  `documento` char(14) DEFAULT NULL,
  `email` varchar(160) NOT NULL,
  `logradouro` varchar(150) DEFAULT NULL,
  `nome` varchar(120) NOT NULL,
  `numero` varchar(10) DEFAULT NULL,
  `perfil` varchar(20) NOT NULL,
  `senha_hash` varchar(100) NOT NULL,
  `status` varchar(20) NOT NULL,
  `telefone` varchar(11) DEFAULT NULL,
  `tipo_documento` varchar(10) DEFAULT NULL,
  `token_recuperacao_expira_em` datetime(6) DEFAULT NULL,
  `token_recuperacao_hash` varchar(64) DEFAULT NULL,
  `uf` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`),
  UNIQUE KEY `UKsqdsrgo7yd5nlfxh382v44rj9` (`documento`),
  CONSTRAINT `usuario_chk_1` CHECK ((`perfil` in (_utf8mb4'USUARIO',_utf8mb4'ADMIN'))),
  CONSTRAINT `usuario_chk_2` CHECK ((`status` in (_utf8mb4'ATIVO',_utf8mb4'INATIVO',_utf8mb4'SUSPENSO',_utf8mb4'BANIDO'))),
  CONSTRAINT `usuario_chk_3` CHECK ((`tipo_documento` in (_utf8mb4'CPF',_utf8mb4'CNPJ')))
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'2026-11-17 21:43:13.762333',NULL,NULL,NULL,NULL,'2026-11-17 21:43:13.762428',NULL,NULL,'admin@trampix.com',NULL,'Administrador',NULL,'ADMIN','$2a$10$VHEqT68nCSIRRD1KRiIP5Ot2JzeRMVtuTOLdqHI.cCZJXLcJ/wqnK','ATIVO',NULL,NULL,NULL,NULL,NULL),(2,'2026-11-17 22:09:20.787005','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:20.787054',NULL,'12345678143','carlos@trampix.com','Rua de Teste','Carlos Mendes','1','USUARIO','$2a$10$Qw8TFSLCWHeDlDFNep54qubX/R9ZT8baRqKBrB.SORuIE66AOLPv6','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(3,'2026-11-17 22:09:21.219935','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:21.219962',NULL,'12345678224','roberto@trampix.com','Rua de Teste','Roberto Almeida','2','USUARIO','$2a$10$XVbiFb1utiFBClrJiLqhEeTZr.fKs4HAAYOVDVVP1YRYvQ3fOThSe','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(4,'2026-11-17 22:09:21.496595','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:21.496620',NULL,'12345678305','andre@trampix.com','Rua de Teste','André Oliveira','3','USUARIO','$2a$10$LyAXjRrJhP2uxMx6JpDk6.u/3j7kbAUPgjrU0KbogW3kXMpENUrje','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(5,'2026-11-17 22:09:21.790708','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:21.790737',NULL,'12345678496','fernando@trampix.com','Rua de Teste','Fernando Costa','4','USUARIO','$2a$10$DF9W36ZLiD5WYK/9LRsefuY2fVu5Juz1YF7YGGyWdWarxz6HHDe1m','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(6,'2026-11-17 22:09:22.068827','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:22.068849',NULL,'12345678577','marcos@trampix.com','Rua de Teste','Marcos Ferreira','5','USUARIO','$2a$10$j18NdAnq9nBAoLY/tY5FsOAtKhYHWjVANmsrIVoacFkAR/ryT2WpW','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(7,'2026-11-17 22:09:22.439092','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:22.439115',NULL,'12345678658','lucas@trampix.com','Rua de Teste','Lucas Santos','6','USUARIO','$2a$10$sE8XVvs3FGti8wB3iJqPqOolM2CHrHu4bIzd0v/IKOWN109CTJfnG','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(8,'2026-11-17 22:09:22.705046','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:22.705088',NULL,'12345678739','paulo@trampix.com','Rua de Teste','Paulo Ribeiro','7','USUARIO','$2a$10$rwm2ypk18O.2IZZBLLdpYejFq5unSiFXUMh3DowdIt3KW1tEAo0pa','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(9,'2026-11-17 22:09:22.997320','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:22.997340',NULL,'12345678810','ricardo@trampix.com','Rua de Teste','Ricardo Lima','8','USUARIO','$2a$10$F8Ld5aOg2V83NDgecQxV0upcJYSUbqgL7NT20orHqRinj.hqnYLH6','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(10,'2026-11-17 22:09:23.293820','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:23.293843',NULL,'12345678909','eduardo@trampix.com','Rua de Teste','Eduardo Martins','9','USUARIO','$2a$10$MtpOU5hUdQDmueUaugkRROMyUhQWgbJA2LIPmFxtVzhKzsWsNIqU.','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(11,'2026-11-17 22:09:23.582451','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:23.582472',NULL,'12345680040','ana@trampix.com','Rua de Teste','Ana Souza','20','USUARIO','$2a$10$1SS6ev8lmLTUsIgDIhXvNeEQ181OJVyPwb3B3W3O3BtSLdq4VuIzO','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(12,'2026-11-17 22:09:23.793311','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:23.793331',NULL,'12345680121','juliana@trampix.com','Rua de Teste','Juliana Rocha','21','USUARIO','$2a$10$mTeXwlc7wlcV0H.n7/pnX.VTuVHrpVbIlynaiEC60VLn21KnO7xAS','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(13,'2026-11-17 22:09:23.990640','Centro','19900000','Ourinhos',NULL,'2026-11-17 22:09:23.990671',NULL,'12345680202','pedro@trampix.com','Rua de Teste','Pedro Carvalho','22','USUARIO','$2a$10$YrAJdRjjcUxDTUA9Eum13eIOr40.LIxP.mzZhWGLprjn9ysCFVgt2','ATIVO','14999990000','CPF',NULL,NULL,'SP'),(14,'2026-11-22 16:02:37.348066',NULL,NULL,NULL,NULL,'2026-11-22 16:02:37.348121',NULL,NULL,'admin@trampix.local',NULL,'Administrador Demo',NULL,'ADMIN','$2a$10$0SxDywnBr6YuMojfXyuo..MpDoGAf56XGcBNx9KW3dhXnDS0Iqf9m','ATIVO',NULL,NULL,NULL,NULL,NULL),(15,'2026-11-22 16:02:37.477629',NULL,NULL,NULL,NULL,'2026-11-22 16:02:37.477665',NULL,'52998224725','usuario@trampix.local',NULL,'Usuário Demo',NULL,'USUARIO','$2a$10$wUoktaG5QkmUSCh973Z1LOU5XNk.K1oacS7DV4J7d/TgoF1O0674O','ATIVO',NULL,'CPF',NULL,NULL,NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-22 21:40:57
