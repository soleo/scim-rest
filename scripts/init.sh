#!/bin/bash


MYSQL_HOST=127.0.0.1


mysql -h $MYSQL_HOST -u root < db_clean.sql

mysql -h $MYSQL_HOST -u root < db_init.sql

mysql -h $MYSQL_HOST -u scim_test -p1e0OZH5f9asCJ0Anj11ia0Wmy scim_test < db_schema.sql

mysql -h $MYSQL_HOST -u scim_test -p1e0OZH5f9asCJ0Anj11ia0Wmy scim_test -e 'show tables;'
