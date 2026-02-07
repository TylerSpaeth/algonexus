-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema algonexus
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema algonexus
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `algonexus` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `algonexus` ;

-- -----------------------------------------------------
-- Table `algonexus`.`strategies`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`strategies` (
  `StrategyID` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(255) NOT NULL,
  `Description` MEDIUMTEXT NULL DEFAULT NULL,
  `Version` INT NOT NULL,
  `Active` BIT(1) NOT NULL DEFAULT b'1',
  `CreatedAt` TIMESTAMP(6) NOT NULL,
  `LastUpdated` TIMESTAMP(6) NOT NULL,
  `ParentStrategyID` INT NULL DEFAULT NULL,
  PRIMARY KEY (`StrategyID`),
  INDEX `ParentStrategyID_idx` (`ParentStrategyID` ASC) VISIBLE,
  CONSTRAINT `fk_strategies_strategies_ParentStrategyID`
    FOREIGN KEY (`ParentStrategyID`)
    REFERENCES `algonexus`.`strategies` (`StrategyID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`strategyparametersets`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`strategyparametersets` (
  `StrategyParameterSetID` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(50) NOT NULL,
  `Description` MEDIUMTEXT NULL DEFAULT NULL,
  `StrategyID` INT NOT NULL,
  PRIMARY KEY (`StrategyParameterSetID`),
  INDEX `StrategyID_idx` (`StrategyID` ASC) VISIBLE,
  CONSTRAINT `fk_strategyparametersets_strategies_StrategyID`
    FOREIGN KEY (`StrategyID`)
    REFERENCES `algonexus`.`strategies` (`StrategyID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`backtestresults`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`backtestresults` (
  `BacktestResultID` INT NOT NULL AUTO_INCREMENT,
  `StrategyParameterSetID` INT NOT NULL,
  `StartTime` TIMESTAMP(6) NOT NULL,
  `EndTime` TIMESTAMP(6) NULL DEFAULT NULL,
  PRIMARY KEY (`BacktestResultID`),
  INDEX `StrategyParameterSetID_idx` (`StrategyParameterSetID` ASC) VISIBLE,
  CONSTRAINT `fk_backtestresults_strategyparametersets_StrategyParameterSetID`
    FOREIGN KEY (`StrategyParameterSetID`)
    REFERENCES `algonexus`.`strategyparametersets` (`StrategyParameterSetID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`exchanges`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`exchanges` (
  `ExchangeID` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`ExchangeID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`symbols`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`symbols` (
  `SymbolID` INT NOT NULL AUTO_INCREMENT,
  `Ticker` VARCHAR(10) NOT NULL,
  `Name` VARCHAR(50) NULL DEFAULT NULL,
  `ExchangeID` INT NOT NULL,
  `AssetType` VARCHAR(20) NOT NULL,
  `Currency` VARCHAR(3) NULL DEFAULT NULL,
  `TickSize` FLOAT NULL DEFAULT NULL,
  `TickValue` FLOAT NULL DEFAULT NULL,
  `IBConID` INT NULL DEFAULT NULL,
  PRIMARY KEY (`SymbolID`),
  UNIQUE INDEX `IBConID_UNIQUE` (`IBConID` ASC) VISIBLE,
  INDEX `ExchangeID_idx` (`ExchangeID` ASC) VISIBLE,
  CONSTRAINT `fk_symbols_exchanges_ExchangeID`
    FOREIGN KEY (`ExchangeID`)
    REFERENCES `algonexus`.`exchanges` (`ExchangeID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`historicaldataset`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`historicaldataset` (
  `HistoricalDatasetID` INT NOT NULL AUTO_INCREMENT,
  `DatasetName` VARCHAR(50) NOT NULL,
  `DatasetSource` VARCHAR(50) NULL DEFAULT NULL,
  `SymbolID` INT NOT NULL,
  `TimeInterval` INT NOT NULL,
  `IntervalUnit` VARCHAR(20) NOT NULL,
  `DatasetStart` TIMESTAMP NOT NULL,
  `DatasetEnd` TIMESTAMP NOT NULL,
  `LastUpdated` TIMESTAMP NOT NULL,
  PRIMARY KEY (`HistoricalDatasetID`),
  INDEX `SymbolID_idx` (`SymbolID` ASC) VISIBLE,
  CONSTRAINT `fk_historicaldataset_symbols_SymbolID`
    FOREIGN KEY (`SymbolID`)
    REFERENCES `algonexus`.`symbols` (`SymbolID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`candlesticks`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`candlesticks` (
  `CandlestickID` INT NOT NULL AUTO_INCREMENT,
  `Open` FLOAT NOT NULL,
  `Close` FLOAT NOT NULL,
  `High` FLOAT NOT NULL,
  `Low` FLOAT NOT NULL,
  `Volume` FLOAT NOT NULL,
  `Timestamp` TIMESTAMP(6) NOT NULL,
  `HistoricalDatasetID` INT NOT NULL,
  PRIMARY KEY (`CandlestickID`),
  INDEX `HistoricalDatasetID_idx` (`HistoricalDatasetID` ASC) VISIBLE,
  CONSTRAINT `fk_candlesticks_historicaldataset_HistoricalDatasetID`
    FOREIGN KEY (`HistoricalDatasetID`)
    REFERENCES `algonexus`.`historicaldataset` (`HistoricalDatasetID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`commissions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`commissions` (
  `CommissionID` INT NOT NULL AUTO_INCREMENT,
  `SymbolID` INT NULL DEFAULT NULL,
  `AssetType` VARCHAR(20) NOT NULL,
  `CommissionAmount` FLOAT NOT NULL,
  PRIMARY KEY (`CommissionID`),
  INDEX `fk_commissions_symbols_SymbolID_idx` (`SymbolID` ASC) VISIBLE,
  CONSTRAINT `fk_commissions_symbols_SymbolID`
    FOREIGN KEY (`SymbolID`)
    REFERENCES `algonexus`.`symbols` (`SymbolID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`users` (
  `UserID` INT NOT NULL AUTO_INCREMENT,
  `ExternalAccountID` VARCHAR(100) NOT NULL,
  `AccountType` VARCHAR(30) NULL DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE INDEX `ExternalAccountID_UNIQUE` (`ExternalAccountID` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`orders`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`orders` (
  `OrderID` INT NOT NULL AUTO_INCREMENT,
  `SymbolID` INT NOT NULL,
  `OrderType` VARCHAR(20) NOT NULL,
  `Side` VARCHAR(10) NOT NULL,
  `Quantity` FLOAT NOT NULL,
  `Price` FLOAT NULL DEFAULT NULL,
  `TimeInForce` VARCHAR(3) NOT NULL,
  `TimePlaced` TIMESTAMP(6) NOT NULL,
  `TimeClosed` TIMESTAMP(6) NULL DEFAULT NULL,
  `Status` VARCHAR(20) NOT NULL,
  `ExternalOrderID` VARCHAR(50) NULL DEFAULT NULL,
  `BacktestResultID` INT NULL DEFAULT NULL,
  `StrategyParameterSetID` INT NOT NULL,
  `UserID` INT NOT NULL,
  `OCAGroup` VARCHAR(20) NULL DEFAULT NULL,
  `Transmit` BIT(1) NOT NULL DEFAULT b'0',
  `TrailAmount` FLOAT NULL DEFAULT NULL,
  `TrailPercent` FLOAT NULL DEFAULT NULL,
  `ParentOrderID` INT NULL DEFAULT NULL,
  `Finalized` BIT(1) NOT NULL DEFAULT b'0',
  `Version` INT NOT NULL DEFAULT '0',
  PRIMARY KEY (`OrderID`),
  INDEX `fk_orders_symbols_SymbolID_idx` (`SymbolID` ASC) VISIBLE,
  INDEX `fk_orders_stratparamsets_StrategyParameterSetID_idx` (`StrategyParameterSetID` ASC) VISIBLE,
  INDEX `fk_orders_users_UserID_idx` (`UserID` ASC) VISIBLE,
  INDEX `fk_orders_orders_ParentUserID_idx` (`ParentOrderID` ASC) VISIBLE,
  INDEX `fk_orders_backtestresults_BacktestResultID_idx` (`BacktestResultID` ASC) VISIBLE,
  CONSTRAINT `fk_orders_backtestresults_BacktestResultID`
    FOREIGN KEY (`BacktestResultID`)
    REFERENCES `algonexus`.`backtestresults` (`BacktestResultID`),
  CONSTRAINT `fk_orders_orders_ParentUserID`
    FOREIGN KEY (`ParentOrderID`)
    REFERENCES `algonexus`.`orders` (`OrderID`),
  CONSTRAINT `fk_orders_stratparamsets_StrategyParameterSetID`
    FOREIGN KEY (`StrategyParameterSetID`)
    REFERENCES `algonexus`.`strategyparametersets` (`StrategyParameterSetID`),
  CONSTRAINT `fk_orders_symbols_SymbolID`
    FOREIGN KEY (`SymbolID`)
    REFERENCES `algonexus`.`symbols` (`SymbolID`),
  CONSTRAINT `fk_orders_users_UserID`
    FOREIGN KEY (`UserID`)
    REFERENCES `algonexus`.`users` (`UserID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`orderevents`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`orderevents` (
  `OrderEventID` INT NOT NULL AUTO_INCREMENT,
  `OrderID` INT NOT NULL,
  `NewStatus` VARCHAR(20) NOT NULL,
  `Timestamp` TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (`OrderEventID`),
  INDEX `OrderID_idx` (`OrderID` ASC) VISIBLE,
  CONSTRAINT `fk_orderevents_orders_OrderID`
    FOREIGN KEY (`OrderID`)
    REFERENCES `algonexus`.`orders` (`OrderID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`strategyparameters`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`strategyparameters` (
  `StrategyParameterID` INT NOT NULL AUTO_INCREMENT,
  `StrategyParameterSetID` INT NOT NULL,
  `Name` VARCHAR(50) NOT NULL,
  `Value` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`StrategyParameterID`),
  INDEX `StrategyParameterSetID_idx` (`StrategyParameterSetID` ASC) VISIBLE,
  CONSTRAINT `fk_stratparams_stratparamsets_StrategyParameterSetID`
    FOREIGN KEY (`StrategyParameterSetID`)
    REFERENCES `algonexus`.`strategyparametersets` (`StrategyParameterSetID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `algonexus`.`trades`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `algonexus`.`trades` (
  `TradeID` INT NOT NULL AUTO_INCREMENT,
  `OrderID` INT NOT NULL,
  `FillQuantity` FLOAT NOT NULL,
  `FillPrice` FLOAT NOT NULL,
  `Side` VARCHAR(10) NOT NULL,
  `Fees` FLOAT NULL DEFAULT NULL,
  `ExternalTradeID` VARCHAR(50) NULL DEFAULT NULL,
  `Timestamp` TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (`TradeID`),
  INDEX `fk_trades_orders_OrderID_idx` (`OrderID` ASC) VISIBLE,
  CONSTRAINT `fk_trades_orders_OrderID`
    FOREIGN KEY (`OrderID`)
    REFERENCES `algonexus`.`orders` (`OrderID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
