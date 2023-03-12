USE masterdata;

CREATE TABLE `EXCHANGE_RATE` (
    `ID`                 bigint      NOT NULL AUTO_INCREMENT,
    `EXCHANGE_RATE_DATE` datetime(6) NOT NULL COMMENT 'Exchange Rate Update Date',
    `FROM_CURRENCY`      varchar(5)  NOT NULL COMMENT 'From Currency',
    `TO_CURRENCY`        varchar(5)  NOT NULL COMMENT 'To Currency',
    `RATE`               varchar(18) NOT NULL DEFAULT '0.000000',
    `LAST_UPDATE`        datetime(6) NOT NULL,
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;