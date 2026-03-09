-- Create Database

CREATE DATABASE smart;

USE `smart` ;

CREATE USER 'smart'@'localhost' IDENTIFIED BY 'smart';
GRANT ALL PRIVILEGES ON *.* TO 'smart'@'localhost' WITH GRANT OPTION;

-- Table Create Start

DROP TABLE IF EXISTS `smart`.`product` ;

CREATE TABLE IF NOT EXISTS `smart`.`product` (
    `pid` VARCHAR(45) NOT NULL,
    `pname` VARCHAR(100) NULL DEFAULT NULL,
    `ptype` VARCHAR(200) NULL DEFAULT NULL,
    `pinfo` VARCHAR(350) NULL DEFAULT NULL,
    `pprice` DECIMAL(12,2) NULL DEFAULT NULL,
    `pquantity` INT NULL DEFAULT NULL,
    `image` LONGBLOB NULL DEFAULT NULL,
    PRIMARY KEY (`pid`)
);

-----------------------------------------------------------------------------------

DROP TABLE IF EXISTS `smart`.`orders` ;

CREATE TABLE IF NOT EXISTS `smart`.`orders` (
    `orderid` VARCHAR(45) NOT NULL,
    `prodid` VARCHAR(45) NOT NULL,
    `quantity` INT NULL DEFAULT NULL,
    `amount` DECIMAL(10,2) NULL DEFAULT NULL,
    `shipped` INT NOT NULL DEFAULT 0,
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`orderid`, `prodid`),
    INDEX `productid_idx` (`prodid` ASC),
    CONSTRAINT `productid`
        FOREIGN KEY (`prodid`)
        REFERENCES `product` (`pid`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-------------------------------------------------------------------------------------

DROP TABLE IF EXISTS `smart`.`user` ;

CREATE TABLE IF NOT EXISTS `smart`.`user` (
    `email` VARCHAR(60) NOT NULL,
    `name` VARCHAR(30) NULL DEFAULT NULL,
    `mobile` BIGINT NULL DEFAULT NULL,
    `address` VARCHAR(250) NULL DEFAULT NULL,
    `pincode` INT NULL DEFAULT NULL,
    `password` VARCHAR(20) NULL DEFAULT NULL,
    `userimage` LONGBLOB NULL DEFAULT NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (`email`)
);

--------------------------------------------------------------------------------------


DROP TABLE IF EXISTS `smart`.`transactions`;

CREATE TABLE IF NOT EXISTS `smart`.`transactions` (
    `transid` VARCHAR(45) NOT NULL,
    `username` VARCHAR(60) NULL DEFAULT NULL,
    `time` DATETIME NULL DEFAULT NULL,
    `amount` DECIMAL(10,2) NULL DEFAULT NULL,
    `status` VARCHAR(20) NULL DEFAULT 'paid',
    PRIMARY KEY (`transid`),
    INDEX `truserid_idx` (`username` ASC),
    CONSTRAINT `truserid`
        FOREIGN KEY (`username`)
        REFERENCES `user` (`email`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `transorderid`
        FOREIGN KEY (`transid`)
        REFERENCES `orders` (`orderid`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

---------------------------------------------------------------------------------------


DROP TABLE IF EXISTS `smart`.`user_demand` ;

CREATE TABLE IF NOT EXISTS `smart`.`user_demand` (
    `username` VARCHAR(60) NOT NULL,
    `prodid` VARCHAR(45) NOT NULL,
    `quantity` INT NULL DEFAULT NULL,
    PRIMARY KEY (`username`, `prodid`),
    INDEX `prodid_idx` (`prodid` ASC),
    CONSTRAINT `userdemailemail`
        FOREIGN KEY (`username`)
        REFERENCES `user` (`email`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `prodid_fk`
        FOREIGN KEY (`prodid`)
        REFERENCES `product` (`pid`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

----------------------------------------------------------------------------------------

DROP TABLE IF EXISTS `smart`.`usercart` ;

CREATE TABLE IF NOT EXISTS `smart`.`usercart` (
    `username` VARCHAR(60) NULL DEFAULT NULL,
    `prodid` VARCHAR(45) NULL DEFAULT NULL,
    `quantity` INT NULL DEFAULT NULL,
    INDEX `useremail_idx` (`username` ASC),
    INDEX `prodidcart_idx` (`prodid` ASC),
    CONSTRAINT `useremail`
        FOREIGN KEY (`username`)
        REFERENCES `user` (`email`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `prodidcart`
        FOREIGN KEY (`prodid`)
        REFERENCES `product` (`pid`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Table Create End----------------------------------------------------------------------

-- Data Insertion Start------------------------------------------------------------------

START TRANSACTION;


USE `smart`;

INSERT INTO product (pid, pname, ptype, pinfo, pprice, pquantity, image) 
VALUES ( 
    'P202408010001', 
    'APPLE iPhone 13 Pro (Graphite, 512 GB)', 
    'electronics', 
    'iPhone 13 boasts an advanced dual-camera system that allows you to click mesmerizing pictures with immaculate clarity.', 
    125999.00, 
    1000, 
    LOAD_FILE('"D:\javacodesquadz\Projects\NamasteMart\NamasteMart Images\images\Apple-13.jpeg"')
);


VALUES ( 
    'P202412080001',
    'ADIDAS Ultraboost 23 Running Shoes (Black)',
    'footwear',
    'Experience cloud-like comfort with Adidas Ultraboost 23. Features Boost cushioning technology that provides
     unparalleled energy return with every stride. Primeknit+ upper offers adaptive support for ultimate running performance.',
    15999.00,
    500,
    LOAD_FILE('C:/product_images/adidas_ultraboost_black.jpg')
);