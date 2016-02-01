CREATE DATABASE IF NOT EXISTS `scim_test`;

CREATE USER 'scim_test'@'localhost' IDENTIFIED BY '1e0OZH5f9asCJ0Anj11ia0Wmy';

GRANT ALL ON `scim_test`.* TO 'scim_test'@'localhost';