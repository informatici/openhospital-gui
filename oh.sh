#!/bin/bash
#
# Open Hospital (www.open-hospital.org)
# Copyright © 2006-2022 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
#
# Open Hospital is a free and open source software for healthcare data management.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# https://www.gnu.org/licenses/gpl-3.0-standalone.html
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
#

#################### Script info and configuration - Do not edit #####################
# set script DEBUG mode
# saner programming env: these switches turn some bugs into errors
#set -o errexit -o pipefail -o noclobber -o nounset

######## get name of this shell script
SCRIPT_NAME=$(basename "$0")

############## Script startup configuration - change at your own risk :-) ##############
#
# set GENERATE_CONFIG_FILES=on "on" to force generation / overwriting of configuration files:
# data/conf/my.cnf and oh/rsc/*.properties files will be regenerated from the original .dist files
# with the settings defined in this script.
#
# Default is set to "off": configuration files will not be generated or overwritten if already present.
#
#GENERATE_CONFIG_FILES="off"

############## OH general configuration - change at your own risk :-) ##############

# OH_PATH is the directory where Open Hospital files are located
# OH_PATH=/usr/local/OpenHospital/oh-1.11

# set OH mode to PORTABLE | CLIENT | SERVER - default set to PORTABLE
#OH_MODE=PORTABLE 

# set DEMO_DATA to on to enable demo database loading - default set to off
#
# -> Warning -> __requires deletion of all portable data__
#
#DEMO_DATA=off

# language setting - default set to en
#OH_LANGUAGE=en # fr es it pt

# set log level to INFO | DEBUG - default set to INFO
#LOG_LEVEL=INFO

# set JAVA_BIN
# Uncomment this if you want to use system wide JAVA
#JAVA_BIN=`which java`

############## OH local configuration - change at your own risk :-) ##############
# Database
DATABASE_SERVER=localhost
DATABASE_PORT="3306"
DATABASE_ROOT_PW="tmp2021oh111"
DATABASE_NAME=oh
DATABASE_USER=isf
DATABASE_PASSWORD="isf123"
#DATABASE_LANGUAGE=en # default to en

DICOM_MAX_SIZE="4M"
DICOM_STORAGE="FileSystemDicomManager" # SqlDicomManager
DICOM_DIR="data/dicom_storage"

OH_DIR="."
OH_DOC_DIR="../doc"
OH_SINGLE_USER="yes" # set "no" for multiuser
CONF_DIR="data/conf"
DATA_DIR="data/db"
PHOTO_DIR="data/photo"
BACKUP_DIR="data/dump"
LOG_DIR="data/log"
SQL_DIR="sql"
SQL_EXTRA_DIR="sql/extra"
TMP_DIR="tmp"

LOG_FILE=startup.log
OH_LOG_FILE=openhospital.log

#DB_CREATE_SQL="create_all_en.sql" # default to create_all_en.sql
DB_DEMO="create_all_demo.sql"

# downloaded file extension
EXT="tar.gz"

# date format
DATE=`date +%Y-%m-%d_%H-%M-%S`

################ Architecture and external software ################

######## MySQL/MariaDB Software
# MariaDB version
MYSQL_VERSION="10.6.11"
MYSQL32_VERSION="10.5.18"
PACKAGE_TYPE="systemd" 

######## define system and software architecture
ARCH=`uname -m`

case $ARCH in
	x86_64|amd64|AMD64)
		JAVA_ARCH=64
		MYSQL_ARCH=x86_64
		MYSQL_PACKAGE_ARCH=$MYSQL_ARCH
		JAVA_PACKAGE_ARCH=x64
		;;
	i[3456789]86|x86|i86pc)
		JAVA_ARCH=32
		MYSQL_ARCH=i686
		MYSQL_VERSION=$MYSQL32_VERSION;
		MYSQL_PACKAGE_ARCH=x86
		JAVA_PACKAGE_ARCH=i686
		;;
	*)
		echo "Unknown architecture: $ARCH. Exiting."
		exit 1
		;;
esac

# set MariaDB download URL / package 
MYSQL_URL="https://archive.mariadb.org/mariadb-$MYSQL_VERSION/bintar-linux-$PACKAGE_TYPE-$MYSQL_PACKAGE_ARCH"
MYSQL_DIR="mariadb-$MYSQL_VERSION-linux-$PACKAGE_TYPE-$MYSQL_ARCH"

######## JAVA Software
######## JAVA 64bit - default architecture

### JRE 11 - openjdk distribution
#JAVA_URL="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9"
#JAVA_DISTRO="OpenJDK11U-jre_x64_linux_hotspot_11.0.11_9"
#JAVA_DIR="jdk-11.0.11+9-jre"

### JRE 11 - zulu distribution
JAVA_DISTRO="zulu11.60.19-ca-fx-jre11.0.17-linux_$JAVA_PACKAGE_ARCH"
JAVA_URL="https://cdn.azul.com/zulu/bin"

### JRE 8 - zulu distribution
#JAVA_DISTRO="zulu8.60.0.21-ca-jre8.0.322-linux_$JAVA_PACKAGE_ARCH"
#JAVA_URL="https://cdn.azul.com/zulu/bin/"
JAVA_DIR=$JAVA_DISTRO

######################## DO NOT EDIT BELOW THIS LINE ########################

######################## Functions ########################

function script_usage {
        # show help / user options
        echo " ---------------------------------------------------------"
        echo "|                                                         |"
        echo "|                   Open Hospital | OH                    |"
        echo "|                                                         |"
        echo " ---------------------------------------------------------"
        echo " lang $OH_LANGUAGE | arch $ARCH | mode $OH_MODE | log level $LOG_LEVEL "
        echo " ---------------------------------------------------------"
        echo ""
        echo " Usage: $SCRIPT_NAME [ -l en|fr|it|es|pt|ar ] "
        echo ""
        echo "   -C    start OH in CLIENT mode (client / server configuration)"
        echo "   -P    start OH in PORTABLE mode"
	echo "   -S    start OH in SERVER (Portable) mode"
        echo "   -d    start OH in debug mode"
        echo "   -D    start OH with Demo data"
        echo "   -g    generate configuration files"
        echo "   -G    setup GSM"
        echo "   -h    show this help"
        echo "   -i    initialize/install OH database"
        echo "   -l    set language: en|fr|it|es|pt|ar"
        echo "   -s    save OH database"
        echo "   -r    restore OH database"
        echo "   -t    test database connection (CLIENT mode only)"
        echo "   -v    show OH software version and configuration"
        echo "   -X    clean/reset OH installation"
        echo ""
}

function get_confirmation {
	read -p "(y/n)? " choice
	case "$choice" in 
		y|Y ) echo "yes";;
		n|N ) echo "Exiting."; exit 0;;
		* ) echo "Invalid choice. Exiting."; exit 1 ;;
	esac
}

function set_defaults {
	# set default values for script variables
	# config file generation - set default to off
	if [ -z ${GENERATE_CONFIG_FILES+x} ]; then
		GENERATE_CONFIG_FILES="off"
	fi

	# OH mode - set default to PORTABLE
	if [ -z ${OH_MODE+x} ]; then
		OH_MODE="PORTABLE"
	fi

	# log level - set default to INFO
	if [ -z ${LOG_LEVEL+x} ]; then
		LOG_LEVEL=INFO
	fi

	# demo data - set default to off
	if [ -z ${DEMO_DATA+x} ]; then
		DEMO_DATA=off
	fi
}

function set_path {
	# get current directory
	CURRENT_DIR=$PWD
	# set OH_PATH if not defined
	if [ -z ${OH_PATH+x} ]; then
		echo "Info: OH_PATH not defined - setting to script path"

		# set OH_PATH to script path
		OH_PATH=$(dirname "$(realpath "$0")")

		if [ ! -f "$OH_PATH/$SCRIPT_NAME" ]; then
			echo "Error - $SCRIPT_NAME not found in the current PATH. Please browse to the directory where Open Hospital was unzipped or set up OH_PATH properly."
			exit 1
		fi
	fi
	OH_PATH_ESCAPED=$(echo $OH_PATH | sed -e 's/\//\\\//g')
	DATA_DIR_ESCAPED=$(echo $DATA_DIR | sed -e 's/\//\\\//g')
	TMP_DIR_ESCAPED=$(echo $TMP_DIR | sed -e 's/\//\\\//g')
	LOG_DIR_ESCAPED=$(echo $LOG_DIR | sed -e 's/\//\\\//g')
	DICOM_DIR_ESCAPED=$(echo $DICOM_DIR | sed -e 's/\//\\\//g')
}

function set_language {
	# set OH interface language - default to en if not defined
	if [ -z ${OH_LANGUAGE+x} ]; then
		OH_LANGUAGE=en
	fi
	# set OH database language - default to en if not defined
	if [ -z ${DATABASE_LANGUAGE+x} ]; then
		DATABASE_LANGUAGE=en
	fi
	# check for valid language selection
	case $OH_LANGUAGE in 
		en|fr|it|es|pt) 
			DATABASE_LANGUAGE=$OH_LANGUAGE
			;;
		ar*)	# default to en for other languages
 			# set database creation script in english for arab interface
			DATABASE_LANGUAGE=en
			;;
		*)
			echo "Invalid language option: $OH_LANGUAGE. Exiting."
			exit 1
		;;
	esac
	# set database creation script in chosen language
	DB_CREATE_SQL="create_all_$DATABASE_LANGUAGE.sql"
}

function initialize_dir_structure {
	# create directory structure
	mkdir -p "./$TMP_DIR"
	mkdir -p "./$LOG_DIR"
	mkdir -p "./$DICOM_DIR"
	mkdir -p "./$PHOTO_DIR"
	mkdir -p "./$BACKUP_DIR"
}

function java_lib_setup {
	# NATIVE LIB setup
	case $JAVA_ARCH in
		64)
		NATIVE_LIB_PATH="$OH_PATH/$OH_DIR/lib/native/Linux/amd64"
		;;
		32)
		NATIVE_LIB_PATH="$OH_PATH/$OH_DIR/lib/native/Linux/i386"
		;;
	esac

	# CLASSPATH setup
	# include OH jar file
	OH_CLASSPATH="$OH_PATH"/$OH_DIR/bin/OH-gui.jar
	
	# include all needed directories
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/bundle
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rpt
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc/icons
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc/images
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc/SmsGateway
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/lib

	# include all jar files under lib/
	DIRLIBS="$OH_PATH"/$OH_DIR/lib/*.jar
	for i in ${DIRLIBS}
	do
		OH_CLASSPATH="$i":$OH_CLASSPATH
	done
}

function java_check {
# check if JAVA_BIN is already set and it exists
if ( [ -z ${JAVA_BIN+x} ] || [ ! -x "$JAVA_BIN" ] ); then
	# set default
	echo "Setting default JAVA..."
	JAVA_BIN="$OH_PATH/$JAVA_DIR/bin/java"
fi

# if JAVA_BIN is not found download JRE
if [ ! -x "$JAVA_BIN" ]; then
	if [ ! -f "./$JAVA_DISTRO.$EXT" ]; then
		echo "Warning - JAVA not found. Do you want to download it?"
		get_confirmation;
		# download java binaries
		echo "Download $JAVA_DISTRO..."
		wget -P ./ $JAVA_URL/$JAVA_DISTRO.$EXT
	fi
	echo "Unpacking $JAVA_DISTRO..."
	tar xf ./$JAVA_DISTRO.$EXT -C ./
	if [ $? -ne 0 ]; then
		echo "Error unpacking Java. Exiting."
		exit 1
	fi
	echo "JAVA unpacked successfully!"
	echo "Removing downloaded file..."
	rm ./$JAVA_DISTRO.$EXT
	echo "Done!"
fi

echo "JAVA found!"
echo "Using $JAVA_BIN"
}

function mysql_check {
if [ ! -d "./$MYSQL_DIR" ]; then
	if [ ! -f "./$MYSQL_DIR.$EXT" ]; then
		echo "Warning - MariaDB/MySQL not found. Do you want to download it?"
		get_confirmation;
		# download mysql binary
		echo "Downloading $MYSQL_DIR..."
		wget -P ./ $MYSQL_URL/$MYSQL_DIR.$EXT
	fi
	echo "Unpacking $MYSQL_DIR..."
	tar xf ./$MYSQL_DIR.$EXT -C ./
	if [ $? -ne 0 ]; then
		echo "Error unpacking MySQL. Exiting."
		exit 1
	fi
	echo "MySQL unpacked successfully!"
	echo "Removing downloaded file..."
	rm ./$MYSQL_DIR.$EXT
	echo "Done!"
fi
# check for mysqld binary
if [ -x ./$MYSQL_DIR/bin/mysqld_safe ]; then
	echo "MySQL found!"
	echo "Using $MYSQL_DIR"
else
	echo "MySQL not found! Exiting."
	exit 1
fi
# check for libaio
ldconfig -p | grep libaio > /dev/null;
if [ $? -eq 1 ]; then
	echo "libaio not found! Please install the library. Exiting."
	exit 1
fi
# check for libncurses
ldconfig -p | grep libncurses > /dev/null;
if [ $? -eq 1 ]; then
	echo "libncurses not found! Please install the library. Exiting."
	exit 1
fi
}

function config_database {
	echo "Checking for MySQL config file..."
	if [ $GENERATE_CONFIG_FILES = "on" ] || [ ! -f ./$CONF_DIR/my.cnf ]; then
		[ -f ./$CONF_DIR/my.cnf ] && mv -f ./$CONF_DIR/my.cnf ./$CONF_DIR/my.cnf.old

		# find a free TCP port to run MySQL starting from the default port
		echo "Looking for a free TCP port for MySQL database..."
		while [[ $(ss -tl4  sport = :$DATABASE_PORT | grep LISTEN) ]]; do
			DATABASE_PORT=$(expr $DATABASE_PORT + 1)
		done
		echo "Found TCP port $DATABASE_PORT!"

		echo "Generating MySQL config file..."
		sed -e "s/DATABASE_SERVER/$DATABASE_SERVER/g" -e "s/DICOM_SIZE/$DICOM_MAX_SIZE/g" -e "s/OH_PATH_SUBSTITUTE/$OH_PATH_ESCAPED/g" \
		-e "s/TMP_DIR/$TMP_DIR_ESCAPED/g" -e "s/DATA_DIR/$DATA_DIR_ESCAPED/g" -e "s/LOG_DIR/$LOG_DIR_ESCAPED/g" \
		-e "s/DATABASE_PORT/$DATABASE_PORT/g" -e "s/MYSQL_DISTRO/$MYSQL_DIR/g" ./$CONF_DIR/my.cnf.dist > ./$CONF_DIR/my.cnf
	fi
}

function initialize_database {
	# create data directory
	mkdir -p "./$DATA_DIR"
	# inizialize MySQL
	echo "Initializing MySQL database on port $DATABASE_PORT..."
	case "$MYSQL_DIR" in 
	*mariadb*)
		./$MYSQL_DIR/scripts/mysql_install_db --basedir=./$MYSQL_DIR --datadir=./"$DATA_DIR" \
		--auth-root-authentication-method=normal >> ./$LOG_DIR/$LOG_FILE 2>&1
		;;
	*mysql*)
		./$MYSQL_DIR/bin/mysqld --initialize-insecure --basedir=./$MYSQL_DIR --datadir=./"$DATA_DIR" >> ./$LOG_DIR/$LOG_FILE 2>&1
		;;
	esac

	if [ $? -ne 0 ]; then
		echo "Error: MySQL initialization failed! Exiting."
		exit 2
	fi
}

function start_database {
	echo "Checking if MySQL server is running..."
	if [ -f "$OH_PATH/$TMP_DIR/mysql.sock" ] || [ -f "$OH_PATH/$TMP_DIR/mysql.pid" ] ; then
		echo "MySQL server already running! Exiting."
		exit 1
	fi

	echo "Starting MySQL server... "
	./$MYSQL_DIR/bin/mysqld_safe --defaults-file=./$CONF_DIR/my.cnf >> ./$LOG_DIR/$LOG_FILE 2>&1 &
	if [ $? -ne 0 ]; then
		echo "Error: MySQL server not started! Exiting."
		exit 2
	fi
	# wait till the MySQL tcp port is open
	until nc -z $DATABASE_SERVER $DATABASE_PORT; do sleep 1; done
	echo "MySQL server started! "
}

function set_database_root_pw {
	# if using MySQL/MariaDB root password need to be set
	echo "Setting MySQL root password..."
	./$MYSQL_DIR/bin/mysql -u root --skip-password --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DATABASE_ROOT_PW';" >> ./$LOG_DIR/$LOG_FILE 2>&1
	
	if [ $? -ne 0 ]; then
		echo "Error: MySQL root password not set! Exiting."
		shutdown_database;
		exit 2
	fi
}

function import_database {
	echo "Creating OH Database..."
	# create OH database and user
	./$MYSQL_DIR/bin/mysql -u root -p$DATABASE_ROOT_PW --protocol=tcp --host=$DATABASE_SERVER --port=$DATABASE_PORT \
	-e "CREATE DATABASE $DATABASE_NAME; CREATE USER '$DATABASE_USER'@'localhost' IDENTIFIED BY '$DATABASE_PASSWORD'; \
	CREATE USER '$DATABASE_USER'@'%' IDENTIFIED BY '$DATABASE_PASSWORD'; GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$DATABASE_USER'@'localhost'; \
	GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$DATABASE_USER'@'%' ; " >> ./$LOG_DIR/$LOG_FILE 2>&1
	
	if [ $? -ne 0 ]; then
		echo "Error: Database creation failed! Exiting."
		shutdown_database;
		exit 2
	fi

	# check for database creation script
	if [ -f ./$SQL_DIR/$DB_CREATE_SQL ]; then
		echo "Using SQL file $SQL_DIR/$DB_CREATE_SQL..."
	else
		echo "Error: No SQL file found! Exiting."
		shutdown_database;
		exit 2
	fi

	# create OH database structure
	echo "Importing database schema..."
	cd "./$SQL_DIR"
	../$MYSQL_DIR/bin/mysql --local-infile=1 -u root -p$DATABASE_ROOT_PW --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp $DATABASE_NAME < ./$DB_CREATE_SQL >> ../$LOG_DIR/$LOG_FILE 2>&1
	if [ $? -ne 0 ]; then
		echo "Error: Database not imported! Exiting."
		shutdown_database;
		cd "$CURRENT_DIR"
		exit 2
	fi
	echo "Database imported!"
	cd "$OH_PATH"
}

function dump_database {
	# save OH database if existing
	if [ -x ./$MYSQL_DIR/bin/mysqldump ]; then
		mkdir -p "$OH_PATH/$BACKUP_DIR"
		echo "Dumping MySQL database..."
		./$MYSQL_DIR/bin/mysqldump --skip-extended-insert -u root --password=$DATABASE_ROOT_PW --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp $DATABASE_NAME > ./$BACKUP_DIR/mysqldump_$DATE.sql
		if [ $? -ne 0 ]; then
			echo "Error: Database not dumped! Exiting."
			cd "$CURRENT_DIR"
			shutdown_database;
			exit 2
		fi
	else
		echo "Error: No mysqldump utility found! Exiting."
		shutdown_database;
		cd "$CURRENT_DIR"
		exit 2
	fi
	echo "MySQL dump file $BACKUP_DIR/mysqldump_$DATE.sql completed!"
}

function shutdown_database {
	if [ $OH_MODE != "CLIENT" ]; then
		echo "Shutting down MySQL server..."
		cd "$OH_PATH"
		./$MYSQL_DIR/bin/mysqladmin -u root -p$DATABASE_ROOT_PW --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp shutdown >> ./$LOG_DIR/$LOG_FILE 2>&1
		# wait till the MySQL tcp port is closed
		until !( nc -z $DATABASE_SERVER $DATABASE_PORT ); do sleep 1; done
		echo "MySQL server stopped!"
	else
		exit 1
	fi
}

function clean_database {
	echo "Warning: do you want to remove all existing data and databases ?"
	get_confirmation;
	echo "--->>> This operation cannot be undone"
	echo "--->>> Are you sure ?"
	get_confirmation;
	echo "Removing data..."
	# remove database files
	rm -rf ./"$DATA_DIR"/*
	# remove socket and pid file
	rm -rf ./$TMP_DIR/*
}

function test_database_connection {
        # test if mysql client is available
	if [ -x ./$MYSQL_DIR/bin/mysql ]; then
		# test connection to the OH MySQL database
		echo "Testing database connection..."
		DBTEST=$(./$MYSQL_DIR/bin/mysql --user=$DATABASE_USER --password=$DATABASE_PASSWORD --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp -e "USE $DATABASE_NAME" >> ./$LOG_DIR/$LOG_FILE 2>&1; echo "$?" )
		if [ $DBTEST -eq 0 ];then
			echo "Database connection successfully established!"
		else
			echo "Error: can't connect to database! Exiting."
			exit 2
		fi
	else
		echo "Can't test database connection..."
	fi
}

function clean_files {
	# remove all log files
	echo "Warning: do you want to remove all existing log files ?"
	get_confirmation;
	echo "Removing log files..."
	rm -f ./$LOG_DIR/*

	# remove configuration files - leave only .dist files
	echo "Warning: do you want to remove all existing configuration files ?"
	get_confirmation;
	echo "Removing configuration files..."
	rm -f ./$CONF_DIR/my.cnf
	rm -f ./$CONF_DIR/my.cnf.old
	rm -f ./$OH_DIR/rsc/settings.properties
	rm -f ./$OH_DIR/rsc/settings.properties.old
	rm -f ./$OH_DIR/rsc/database.properties
	rm -f ./$OH_DIR/rsc/database.properties.old
	rm -f ./$OH_DIR/rsc/log4j.properties
	rm -f ./$OH_DIR/rsc/log4j.properties.old
	rm -f ./$OH_DIR/rsc/dicom.properties
	rm -f ./$OH_DIR/rsc/dicom.properties.old
}

function generate_config_files {
	# set up configuration files
	echo "Checking for OH configuration files..."
	######## DICOM setup
	if [ $GENERATE_CONFIG_FILES = "on" ] || [ ! -f ./$OH_DIR/rsc/dicom.properties ]; then
		[ -f ./$OH_DIR/rsc/dicom.properties ] && mv -f ./$OH_DIR/rsc/dicom.properties ./$OH_DIR/rsc/dicom.properties.old
		echo "Generating OH configuration file -> dicom.properties..."
		sed -e "s/DICOM_SIZE/$DICOM_MAX_SIZE/g" -e "s/OH_PATH_SUBSTITUTE/$OH_PATH_ESCAPED/g" \
		-e "s/DICOM_STORAGE/$DICOM_STORAGE/g" -e "s/DICOM_DIR/$DICOM_DIR_ESCAPED/g" ./$OH_DIR/rsc/dicom.properties.dist > ./$OH_DIR/rsc/dicom.properties
	fi

	######## log4j.properties setup
	if [ $GENERATE_CONFIG_FILES = "on" ] || [ ! -f ./$OH_DIR/rsc/log4j.properties ]; then
		OH_LOG_DEST="$OH_PATH_ESCAPED/$LOG_DIR/$OH_LOG_FILE"
		[ -f ./$OH_DIR/rsc/log4j.properties ] && mv -f ./$OH_DIR/rsc/log4j.properties ./$OH_DIR/rsc/log4j.properties.old
		echo "Generating OH configuration file -> log4j.properties..."
		sed -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/" -e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		-e "s/DBNAME/$DATABASE_NAME/g" -e "s/LOG_LEVEL/$LOG_LEVEL/g" -e "s+LOG_DEST+$OH_LOG_DEST+g" \
		./$OH_DIR/rsc/log4j.properties.dist > ./$OH_DIR/rsc/log4j.properties
	fi

	######## database.properties setup 
	if [ $GENERATE_CONFIG_FILES = "on" ] || [ ! -f ./$OH_DIR/rsc/database.properties ]; then
		[ -f ./$OH_DIR/rsc/database.properties ] && mv -f ./$OH_DIR/rsc/database.properties ./$OH_DIR/rsc/database.properties.old
		echo "Generating OH configuration file -> database.properties..."
		sed -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/g" -e "s/DBNAME/$DATABASE_NAME/g" \
		-e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		./$OH_DIR/rsc/database.properties.dist > ./$OH_DIR/rsc/database.properties
	fi
	######## settings.properties setup
	# set language and DOC_DIR in OH config file
	if [ $GENERATE_CONFIG_FILES = "on" ] || [ ! -f ./$OH_DIR/rsc/settings.properties ]; then
		[ -f ./$OH_DIR/rsc/settings.properties ] && mv -f ./$OH_DIR/rsc/settings.properties ./$OH_DIR/rsc/settings.properties.old
		echo "Generating OH configuration file -> settings.properties..."
		sed -e "s/OH_LANGUAGE/$OH_LANGUAGE/g" -e "s&OH_DOC_DIR&$OH_DOC_DIR&g" -e "s/YES_OR_NO/$OH_SINGLE_USER/g" \
		-e "s&PHOTO_DIR&$PHOTO_DIR&g" ./$OH_DIR/rsc/settings.properties.dist > ./$OH_DIR/rsc/settings.properties
	fi
}


######################## Script start ########################

######## Pre-flight checks

# check user running the script
if [ $(id -u) -eq 0 ]; then
	echo "Error - do not run this script as root. Exiting."
	exit 1
fi

######## Environment setup

set_defaults;
set_path;
set_language;

# set working dir to OH base dir
cd "$OH_PATH"

######## Parse user input

# reset in case getopts has been used previously in the shell
OPTIND=1 
# list of arguments expected in user input (- option)
OPTSTRING=":CPSdDgGhil:srtvX?" 

# function to parse input
while getopts ${OPTSTRING} opt; do
	case ${opt} in
	C)	# start in CLIENT mode
		OH_MODE="CLIENT"
		;;
	P)	# start in PORTABLE mode
		OH_MODE="PORTABLE"
		;;
	S)	# start in SERVER mode
		OH_MODE="SERVER"
		;;
	d)	# debug
		LOG_LEVEL=DEBUG
		echo "Log level set to $LOG_LEVEL"
		;;
	D)	# demo mode
        	echo "Starting Open Hospital with Demo data..."
		# exit if OH is configured in CLIENT mode
		if [ $OH_MODE = "CLIENT" ]; then
			echo "Error - OH_MODE set to CLIENT mode. Cannot run with Demo data, exiting."
			exit 1;
		else OH_MODE="PORTABLE"
		fi
		DEMO_DATA="on"
		;;
	g)	# generate config files and exit
		GENERATE_CONFIG_FILES="on"
		generate_config_files;
		echo "Done!"
		exit 0;
		;;
	G)	# set up GSM
		echo "Setting up GSM..."
		java_check;
		java_lib_setup;
		$JAVA_BIN -Djava.library.path=${NATIVE_LIB_PATH} -classpath "$OH_CLASSPATH" org.isf.utils.sms.SetupGSM "$@"
		exit 0;
		;;
	h)	# help
		script_usage;
		exit 0
		;;
	i)	# initialize/install OH database
		# set mode to CLIENT
		OH_MODE="CLIENT"
		echo "Do you want to initialize/install the OH database on:"
		echo ""
		echo " Server -> $DATABASE_SERVER"
		echo " TCP port -> $DATABASE_PORT" 
		echo ""
		get_confirmation;
		set_language;
		initialize_dir_structure;
		mysql_check;
		# ask user for database root password
		read -p "Please insert the MySQL / MariaDB database root password (root@$DATABASE_SERVER) -> " DATABASE_ROOT_PW
		echo ""
		echo "Installing the database....."
		echo ""
		echo " Database name -> $DATABASE_NAME"
		echo " Database user -> $DATABASE_USER"
		echo " Database password -> $DATABASE_PASSWORD"
		echo ""
		import_database;
		test_database_connection;
		echo "Done!"
		exit 0
		;;
	l)	# set language
		OH_LANGUAGE=$OPTARG
		set_language;
		GENERATE_CONFIG_FILES="on"
		;;
	s)	# save database
		# check if mysql utilities exist
		mysql_check;
		# check if portable mode is on
		if [ $OH_MODE = "PORTABLE" ]; then
			# check if database already exists
			if [ -d ./"$DATA_DIR"/$DATABASE_NAME ]; then
				config_database;
				start_database;
			else
	        		echo "Error: no data found! Exiting."
				exit 1
			fi
		fi
		test_database_connection;
		echo "Saving Open Hospital database..."
		dump_database;
		if [ $OH_MODE = "PORTABLE" ]; then
			shutdown_database;
		fi
		echo "Done!"
		exit 0
		;;
	r)	# restore 
        	echo "Restoring Open Hospital database...."
		# ask user for database/sql script to restore
		read -p "Enter SQL dump/backup file that you want to restore - (in $SQL_DIR subdirectory) -> " DB_CREATE_SQL
		if [ ! -f ./$SQL_DIR/$DB_CREATE_SQL ]; then
			echo "Error: No SQL file found! Exiting."
			exit 2
		else
		        echo "Found $SQL_DIR/$DB_CREATE_SQL, restoring it..."
			# check if mysql utilities exist
			mysql_check;
			if [ $OH_MODE = "PORTABLE" ]; then
				# reset database if exists
				clean_database;
				config_database;
				initialize_dir_structure;
				initialize_database;
				start_database;
				set_database_root_pw;
			fi
			import_database;
			if [ $OH_MODE = "PORTABLE" ]; then
				shutdown_database;
			fi
	        	echo "Done!"
			exit 0
		fi
        	# normal startup from here
		;;
	t)	# test database connection
		if [ $OH_MODE != "CLIENT" ]; then
			echo "Error: Only for CLIENT mode. Exiting."
			exit 1
		fi
		mysql_check;
		test_database_connection;
		exit 0
		;;
	v)	# show version
		echo "--------- Software version ---------"
		source "./$OH_DIR/rsc/version.properties"
		echo "Open Hospital version" $VER_MAJOR.$VER_MINOR.$VER_RELEASE
		echo "MySQL version: $MYSQL_DIR"
		echo "JAVA version: $JAVA_DISTRO"
		# show configuration
		echo "--------- Script Configuration ---------"
		echo "Architecture is $ARCH"
		echo "Config file generation is set to $GENERATE_CONFIG_FILES"
		echo ""
		echo "--------- OH Configuration ---------"
		echo "Open Hospital is configured in $OH_MODE mode"
		echo "Language is set to $OH_LANGUAGE"
		echo "Demo data is set to $DEMO_DATA"
		echo "Log level is set to $LOG_LEVEL"
		echo ""
		echo "--- Database ---"
		echo "DATABASE_SERVER=$DATABASE_SERVER"
		echo "DATABASE_PORT=$DATABASE_PORT"
		echo "DATABASE_NAME=$DATABASE_NAME"
		echo "DATABASE_USER=$DATABASE_USER"
		echo ""
		echo "--- Dicom ---"
		echo "DICOM_MAX_SIZE=$DICOM_MAX_SIZE"
		echo "DICOM_STORAGE=$DICOM_STORAGE"
		echo "DICOM_DIR=$DICOM_DIR"
		echo ""
		echo "--- OH folders ---"
		echo "OH_DIR=$OH_DIR"
		echo "OH_DOC_DIR=$OH_DOC_DIR"
		echo "OH_SINGLE_USER=$OH_SINGLE_USER"
		echo "CONF_DIR=$CONF_DIR"
		echo "DATA_DIR=$DATA_DIR"
		echo "BACKUP_DIR=$BACKUP_DIR"
		echo "LOG_DIR=$LOG_DIR"
		echo "SQL_DIR=$SQL_DIR"
		echo "SQL_EXTRA_DIR=$SQL_EXTRA_DIR"
		echo "TMP_DIR=$TMP_DIR"
		echo ""
		echo "---  Logging ---"
		echo "LOG_FILE=$LOG_FILE"
		echo "OH_LOG_FILE=$OH_LOG_FILE"
		echo ""
		exit 0
		;;
	X)	# clean
        	echo "Cleaning Open Hospital installation..."
		clean_files;
		clean_database;
        	echo "Done!"
		exit 0
		;;
	: )	# if no lang argument is given, shows error
		echo "No language specified. See $SCRIPT_NAME -h for help"
		exit 3
		;;
	?)	# default
		echo "Invalid option: -${OPTARG}. See $SCRIPT_NAME -h for help"
		exit 3
		;;
	esac
done

######################## OH start ########################

# check OH mode
if [ -z ${OH_MODE+x} ]; then
	echo "Error - OH_MODE not defined [CLIENT - PORTABLE - SERVER]! Exiting."
	exit 1
fi

# check for demo mode
if [ $DEMO_DATA = "on" ]; then
	# exit if OH is configured in CLIENT or SERVER mode
	if [ $OH_MODE = "CLIENT" ] || [ $OH_MODE = "SERVER"] ; then
		echo "Error - OH_MODE set to $OH_MODE mode. Cannot run with Demo data, exiting."
		exit 1;
	fi

	if [ -f ./$SQL_DIR/$DB_DEMO ]; then
		echo "Found SQL demo database, starting OH in demo mode..."
		DB_CREATE_SQL=$DB_DEMO
		# reset database if exists
		clean_database;
	else
		echo "Error: no $DB_DEMO found! Exiting."
		exit 1
	fi
fi

# display running configuration
echo "Generate config files is set to $GENERATE_CONFIG_FILES"
echo "Starting Open Hospital in $OH_MODE mode..."
echo "OH_PATH is set to $OH_PATH"
echo "OH language is set to $OH_LANGUAGE"
echo "OH log level is set to $LOG_LEVEL"

# check for java
java_check;

# setup java lib
java_lib_setup;

# create directories
initialize_dir_structure;

######## Database setup

# start MySQL and create database
if [ $OH_MODE = "PORTABLE" ] || [ $OH_MODE = "SERVER" ] ; then
	# check for MySQL software
	mysql_check;
	# config MySQL
	config_database;
	# check if OH database already exists
	if [ ! -d ./"$DATA_DIR"/$DATABASE_NAME ]; then
		echo "OH database not found, starting from scratch..."
		# prepare MySQL
		initialize_database;
		# start MySQL
		start_database;	
		# set database root password
		set_database_root_pw;
		# create database and load data
		import_database;
	else
	        echo "OH database found!"
		# start MySQL
		start_database;
	fi
fi


# if SERVER mode is selected, wait for CTRL-C input to exit
if [ $OH_MODE = "SERVER" ]; then
	echo "Open Hospital - SERVER mode started"
#	echo "Database server listening on $DATABASE_SERVER:$DATABASE_PORT"
	echo "Database server ready for connections..."
	echo "Press Ctrl + C to exit"
	while true; do
		trap ctrl_c INT
		function ctrl_c() {
			echo "Exiting Open Hospital..."
			shutdown_database;		
			cd "$CURRENT_DIR"
			exit 0
		}
	done
else
	######## Open Hospital GUI startup - only for CLIENT or PORTABLE mode

	# test if database connection is working
	test_database_connection;

	# generate config files
	generate_config_files;

	echo "Starting Open Hospital GUI..."
	# OH GUI launch
	cd "$OH_PATH/$OH_DIR" # workaround for hard coded paths

	$JAVA_BIN -client -Xms64m -Xmx1024m -Dsun.java2d.dpiaware=false -Djava.library.path=${NATIVE_LIB_PATH} -classpath $OH_CLASSPATH org.isf.menu.gui.Menu >> ../$LOG_DIR/$LOG_FILE 2>&1

	if [ $? -ne 0 ]; then
		echo "An error occurred while starting Open Hospital. Exiting."
		shutdown_database;
		cd "$CURRENT_DIR"
		exit 4
	fi

	# Close and exit

	echo "Exiting Open Hospital..."
	shutdown_database;

	# go back to starting directory
	cd "$CURRENT_DIR"
fi

# Final exit
echo "Done!"
exit 0
