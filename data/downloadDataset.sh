#!/bin/bash

# download demo user data for testing
demoUrls=(
    http://www.json-generator.com/api/json/get/bUXAaiSCTC 
    http://www.json-generator.com/api/json/get/cnZJtNQQya 
    http://www.json-generator.com/api/json/get/cqaIiIpRSG
    http://www.json-generator.com/api/json/get/cpGrBHmQPm
    http://www.json-generator.com/api/json/get/chmpnZSHAO
    http://www.json-generator.com/api/json/get/cqWzpXbfSa
    http://www.json-generator.com/api/json/get/ccYGtKxdLm
    http://www.json-generator.com/api/json/get/bWKLjNhabm
    http://www.json-generator.com/api/json/get/cfIWIfDbaW
    http://www.json-generator.com/api/json/get/cwirPDfPsi
    )
total=${#demoUrls[*]}

for (( i=0; i<=$(( $total -1 )); i++ ))
do
    echo Download "${demoUrls[$i]}" " > User-$i.json"
    curl ${demoUrls[$i]} > User-${i}.json
done

