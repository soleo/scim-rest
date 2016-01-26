-- https://github.com/ezhukov/scim-sample/blob/master/src/main/sql/create_tables.sql
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS emails;
DROP TABLE IF EXISTS phoneNumbers;
DROP TABLE IF EXISTS ims;
DROP TABLE IF EXISTS photos;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS groups_users;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS entitlements;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS x509Certificates;

CREATE TABLE users (
    id                varchar(36) not null PRIMARY KEY,
    externalId        varchar(36) not null,
    username          varchar(20) unique not null,
    formattedName     varchar(255),
    familyName        varchar(70),
    givenName         varchar(70),
    middleName        varchar(70),
    honorificPrefix   varchar(5),
    honorificSuffix   varchar(5),
    displayName       varchar(30),
    nickname          varchar(30),
    profileURL        varchar(255),
    title             varchar(50),
    userType          varchar(255),
    preferredLanguage char(5),
    locale            char(5),
    timezone          varchar(100),
    active            boolean,
    password          varchar(255),
    created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastModified      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    -- location          varchar(255) not null,
    -- version           varchar(100) not null,
    -- gender            varchar(6)
) ENGINE=InnoDB;

CREATE TABLE emails (
    id        SERIAL PRIMARY KEY,
    value     varchar(50) unique not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE phoneNumbers (
    id        SERIAL PRIMARY KEY,
    value     varchar(50) not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE ims (
    id        SERIAL PRIMARY KEY,
    value     varchar(50) not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE photos (
    id        SERIAL PRIMARY KEY,
    value     varchar(2000) not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE addresses (
    id            SERIAL PRIMARY KEY,
    value         varchar(250),
    display       varchar(50),
    isPrimary     boolean,
    type          varchar(20),
    operation     varchar(20),
    formatted     varchar(200),
    streetAddress varchar(100),
    locality      varchar(50),
    region        varchar(50),
    postalCode    varchar(10),
    country       char(2),
    userId        varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE groups (
    id            varchar(36) not null PRIMARY KEY, /** UUID of the group **/
    displayName   varchar(50),
    type          varchar(20),
    created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastModified      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE groups_users (
    id        SERIAL PRIMARY KEY,
    userId    varchar(36) not null,
    groupId   varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (groupId) REFERENCES groups(id)
) ENGINE=InnoDB;

CREATE TABLE entitlements (
    id        SERIAL PRIMARY KEY,
    value     varchar(50) not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE roles (
    id        integer PRIMARY KEY,
    value     varchar(50) not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE x509Certificates (
    id        SERIAL PRIMARY KEY,
    value     varchar(250) not null,
    display   varchar(50),
    isPrimary boolean,
    type      varchar(20),
    operation varchar(20),
    userId    varchar(36) not null,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;


