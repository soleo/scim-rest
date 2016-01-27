# --- !Ups
CREATE TABLE users (
    id                varchar(36) not null PRIMARY KEY,
    externalId        varchar(36),
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

INSERT IGNORE INTO `users` (
`id`, `externalId`, `username`,
`formattedName`, `familyName`, `givenName`,  `nickname`, 
`profileURL`, `active`, `password`, `displayName`
)
VALUES
(
'2819c223-7f76-453a-919d-413861904646', 'soleo', 'soleoshao',
'Xinjiang Shao', 'Shao', 'Xinjiang',  'soleo',
'http://xinjiangshao.com', true, '123456', 'Xinjiang Shao'
);

INSERT IGNORE INTO `emails` (
    `userId`, `value`, `type`, `isPrimary`
)VALUES
('2819c223-7f76-453a-919d-413861904646', 'shaoxinjiang@gmail.com' ,'work', true),
('2819c223-7f76-453a-919d-413861904646', 'shaoxinjiang@gmail.com' ,'work', true);

INSERT IGNORE INTO `groups` (
    `id`, `displayName`
)VALUES(
    'e9e30dba-f08f-4109-8486-d5c6a331660a', 'Developer'
);

INSERT IGNORE INTO `groups_users` (
    `userId`, `groupId`
)VALUES(
    '2819c223-7f76-453a-919d-413861904646', 'e9e30dba-f08f-4109-8486-d5c6a331660a'
);

# --- !Downs
DROP TABLE users;
DROP TABLE emails;
DROP TABLE phoneNumbers;
DROP TABLE ims;
DROP TABLE photos;
DROP TABLE addresses;
DROP TABLE groups_users;
DROP TABLE groups;
DROP TABLE entitlements;
DROP TABLE roles;
DROP TABLE x509Certificates;

