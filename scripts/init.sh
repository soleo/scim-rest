#!/bin/bash


MYSQL_HOST=127.0.0.1
PWD=`pwd`

mysql -h $MYSQL_HOST -u root < $PWD/db_clean.sql

mysql -h $MYSQL_HOST -u root < $PWD/db_init.sql

mysql -h $MYSQL_HOST -u scim_test -p1e0OZH5f9asCJ0Anj11ia0Wmy scim_test < $PWD/db_schema.sql

mysql -h $MYSQL_HOST -u scim_test -p1e0OZH5f9asCJ0Anj11ia0Wmy scim_test -e 'show tables;'
