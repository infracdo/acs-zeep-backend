#!/bin/bash
echo "####################################################\n"
echo "STOPPING ACS ................ \n"
echo "####################################################\n"

systemctl stop acs-backend

echo "####################################################\n"
echo "BUILDING ACS APPLICATION ................ \n"
echo "####################################################\n"

mvn clean install

echo "####################################################\n"
echo "ALTER TABLES ................ \n"
echo "####################################################\n"

mysql -D "acsDB" -e "ALTER TABLE auto_complete CHANGE command command TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL;"
mysql -D "acsDB" -e "ALTER TABLE auto_complete CHANGE suggestion_list suggestion_list LONGBLOB NULL DEFAULT NULL;"

echo "####################################################\n"
echo "RUNNING ACS ................ \n"
echo "####################################################\n"

systemctl start acs-backend