CREATE DATABASE IF NOT EXISTS `default`;

CREATE USER 'scim_test'@'localhost' IDENTIFIED BY '1e0OZH5f9asCJ0Anj11ia0Wmy';

GRANT ALL ON `scim_test`.* TO 'default'@'localhost';