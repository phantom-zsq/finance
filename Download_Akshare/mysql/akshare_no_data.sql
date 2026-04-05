-- MySQL dump 10.13  Distrib 8.4.0, for macos13.2 (x86_64)
--
-- Host: localhost    Database: akshare
-- ------------------------------------------------------
-- Server version	9.4.0

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
-- Table structure for table `basic_information`
--

DROP TABLE IF EXISTS `basic_information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_information` (
  `交易所` text,
  `交易所代码` text,
  `首页地址` text,
  `品种名称` text,
  `品种代码` text,
  `上市时间` text,
  `集合竞价` text,
  `日盘时间` text,
  `夜盘时间` text,
  `到期日分组` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `calendar`
--

DROP TABLE IF EXISTS `calendar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calendar` (
  `交易日` bigint DEFAULT NULL,
  KEY `idx_calendar_tradedate` (`交易日`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `expiration_date`
--

DROP TABLE IF EXISTS `expiration_date`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expiration_date` (
  `到期日分组` text,
  `合约日期` text,
  `到期日` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `futures_fees_info`
--

DROP TABLE IF EXISTS `futures_fees_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `futures_fees_info` (
  `交易所` text,
  `合约代码` varchar(20) NOT NULL COMMENT '合约代码',
  `合约名称` text,
  `品种代码` text,
  `品种名称` text,
  `合约乘数` bigint DEFAULT NULL,
  `最小跳动` double DEFAULT NULL,
  `开仓费率` double DEFAULT NULL,
  `开仓费用/手` double DEFAULT NULL,
  `平仓费率` double DEFAULT NULL,
  `平仓费用/手` double DEFAULT NULL,
  `平今费率` double DEFAULT NULL,
  `平今费用/手` double DEFAULT NULL,
  `做多保证金率` double DEFAULT NULL,
  `做多保证金/手` bigint DEFAULT NULL,
  `做空保证金率` double DEFAULT NULL,
  `做空保证金/手` bigint DEFAULT NULL,
  `上日结算价` double DEFAULT NULL,
  `上日收盘价` double DEFAULT NULL,
  `最新价` double DEFAULT NULL,
  `成交量` bigint DEFAULT NULL,
  `持仓量` bigint DEFAULT NULL,
  `1手开仓费用` double DEFAULT NULL,
  `1手平仓费用` double DEFAULT NULL,
  `1手平今费用` double DEFAULT NULL,
  `做多1手保证金` double DEFAULT NULL,
  `做空1手保证金` double DEFAULT NULL,
  `1手市值` double DEFAULT NULL,
  `1Tick平仓盈亏` double DEFAULT NULL,
  `1Tick平仓净利` double DEFAULT NULL,
  `2Tick平仓净利` double DEFAULT NULL,
  `1Tick平仓收益率%` double DEFAULT NULL,
  `2Tick平仓收益率%` double DEFAULT NULL,
  `1Tick平今净利` double DEFAULT NULL,
  `2Tick平今净利` double DEFAULT NULL,
  `1Tick平今收益率%` double DEFAULT NULL,
  `2Tick平今收益率%` double DEFAULT NULL,
  `更新时间` text,
  `交易日` bigint DEFAULT NULL,
  KEY `idx_futures_fees_info_tradedate_code` (`交易日`,`合约代码`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `futures_rule`
--

DROP TABLE IF EXISTS `futures_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `futures_rule` (
  `交易所` text,
  `品种` text,
  `代码` text,
  `交易保证金比例` double DEFAULT NULL,
  `涨跌停板幅度` double DEFAULT NULL,
  `合约乘数` bigint DEFAULT NULL,
  `最小变动价位` double DEFAULT NULL,
  `限价单每笔最大下单手数` bigint DEFAULT NULL,
  `特殊合约参数调整` text,
  `调整备注` double DEFAULT NULL,
  `交易日` bigint DEFAULT NULL,
  KEY `idx_futures_rule_tradedate` (`交易日`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `get_futures_daily`
--

DROP TABLE IF EXISTS `get_futures_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `get_futures_daily` (
  `symbol` text,
  `date` text,
  `open` text,
  `high` text,
  `low` text,
  `close` text,
  `volume` text,
  `open_interest` text,
  `turnover` text,
  `settle` text,
  `pre_settle` text,
  `variety` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option_comm_info`
--

DROP TABLE IF EXISTS `option_comm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `option_comm_info` (
  `期权品种` text,
  `现价` bigint DEFAULT NULL,
  `涨/跌停板` text,
  `成交量` bigint DEFAULT NULL,
  `类型` text,
  `权利金` text,
  `开仓` text,
  `平昨` text,
  `平今` text,
  `行权` text,
  `每跳毛利/元` bigint DEFAULT NULL,
  `手续费(开+平)` text,
  `每跳净利/元` bigint DEFAULT NULL,
  `备注` text,
  `交易所` text,
  `手续费更新时间` text,
  `价格更新时间` text,
  `交易日` bigint DEFAULT NULL,
  KEY `idx_option_comm_info_tradedate` (`交易日`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option_hist_czce`
--

DROP TABLE IF EXISTS `option_hist_czce`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `option_hist_czce` (
  `合约代码` text,
  `昨结算` double DEFAULT NULL,
  `今开盘` double DEFAULT NULL,
  `最高价` double DEFAULT NULL,
  `最低价` double DEFAULT NULL,
  `今收盘` double DEFAULT NULL,
  `今结算` double DEFAULT NULL,
  `涨跌1` double DEFAULT NULL,
  `涨跌2` double DEFAULT NULL,
  `成交量(手)` double DEFAULT NULL,
  `持仓量` double DEFAULT NULL,
  `增减量` double DEFAULT NULL,
  `成交额(万元)` double DEFAULT NULL,
  `DELTA` double DEFAULT NULL,
  `隐含波动率` double DEFAULT NULL,
  `行权量` double DEFAULT NULL,
  `交易日` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option_hist_dce`
--

DROP TABLE IF EXISTS `option_hist_dce`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `option_hist_dce` (
  `品种名称` text,
  `合约` text,
  `开盘价` double DEFAULT NULL,
  `最高价` double DEFAULT NULL,
  `最低价` double DEFAULT NULL,
  `收盘价` double DEFAULT NULL,
  `前结算价` double DEFAULT NULL,
  `结算价` double DEFAULT NULL,
  `涨跌` double DEFAULT NULL,
  `涨跌1` double DEFAULT NULL,
  `Delta` double DEFAULT NULL,
  `隐含波动率(%)` double DEFAULT NULL,
  `成交量` bigint DEFAULT NULL,
  `持仓量` bigint DEFAULT NULL,
  `持仓量变化` bigint DEFAULT NULL,
  `成交额` double DEFAULT NULL,
  `行权量` bigint DEFAULT NULL,
  `交易日` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option_hist_gfex`
--

DROP TABLE IF EXISTS `option_hist_gfex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `option_hist_gfex` (
  `商品名称` text,
  `合约名称` text,
  `开盘价` double DEFAULT NULL,
  `最高价` double DEFAULT NULL,
  `最低价` double DEFAULT NULL,
  `收盘价` double DEFAULT NULL,
  `前结算价` double DEFAULT NULL,
  `结算价` double DEFAULT NULL,
  `涨跌` double DEFAULT NULL,
  `涨跌1` double DEFAULT NULL,
  `Delta` double DEFAULT NULL,
  `成交量` bigint DEFAULT NULL,
  `持仓量` bigint DEFAULT NULL,
  `持仓量变化` bigint DEFAULT NULL,
  `成交额` double DEFAULT NULL,
  `行权量` bigint DEFAULT NULL,
  `隐含波动率` double DEFAULT NULL,
  `交易日` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option_hist_shfe`
--

DROP TABLE IF EXISTS `option_hist_shfe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `option_hist_shfe` (
  `合约代码` text,
  `开盘价` text,
  `最高价` text,
  `最低价` text,
  `收盘价` double DEFAULT NULL,
  `前结算价` double DEFAULT NULL,
  `结算价` double DEFAULT NULL,
  `涨跌1` double DEFAULT NULL,
  `涨跌2` double DEFAULT NULL,
  `成交量` bigint DEFAULT NULL,
  `持仓量` bigint DEFAULT NULL,
  `持仓量变化` bigint DEFAULT NULL,
  `成交额` double DEFAULT NULL,
  `德尔塔` double DEFAULT NULL,
  `行权量` bigint DEFAULT NULL,
  `交易日` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-05 11:27:11
