#!/bin/bash
#
# Open Hospital (www.open-hospital.org)
# Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#

#################### Script info and configuration - Do not edit #####################
# set script DEBUG mode
# saner programming env: these switches turn some bugs into errors
#set -o errexit -o pipefail -o noclobber -o nounset

######## get name of this shell script
SCRIPT_NAME=$(basename "$0")

######################## Script configuration #######################
#
# set WRITE_CONFIG_FILES=on "on" to force generation / overwriting of configuration files:
# data/conf/my.cnf and oh/rsc/*.properties files will be regenerated from the original .dist files
# with the settings defined in this script.
#
# Default is set to "off": configuration files will not be regenerated or overwritten if already present.
#
WRITE_CONFIG_FILES="off"

############## OH general configuration - change at your own risk :-) ##############

# OH_PATH is the directory where Open Hospital files are located
# OH_PATH=/usr/local/OpenHospital/oh

# set OH mode to PORTABLE | CLIENT | SERVER - default set to PORTABLE
#OH_MODE="PORTABLE" 

# language setting - default set to en
OH_LANGUAGE_LIST=("ar" "de" "en" "es" "fr" "it" "pt" "sq")
OH_LANGUAGE_LIST_INFO=("Arabic" "German" "English" "Spanish" "French" "Italian" "Portuguese" "Albanian")

# single / multiuser - set "yes" for single user configuration
#OH_SINGLE_USER="no"

# set log level to INFO | DEBUG - default set to INFO
LOG_LEVEL="INFO"

# set DEMO_DATA to on to enable demo database loading - default set to off
# ---> Warning <--- __requires deletion of all portable data__
DEMO_DATA="off"
DEMO_DATABASE="ohdemo"

# set JAVA_BIN
# Uncomment this if you want to use system wide JAVA
#JAVA_BIN=`which java`

##################### Database configuration #######################
DATABASE_SERVER="localhost"
DATABASE_PORT="3306"
DATABASE_ROOT_PW="tmp2021oh111"
DATABASE_NAME="oh"
DATABASE_USER="isf"
DATABASE_PASSWORD="isf123"

#######################  OH configuration  #########################
# path and directories
OH_DIR="."
OH_DOC_DIR="doc"
CONF_DIR="data/conf"
DATA_DIR="data/db"
PHOTO_DIR="data/photo"
BACKUP_DIR="data/dump"
LOG_DIR="data/log"
SQL_DIR="sql"
SQL_EXTRA_DIR="sql/extra"
TMP_DIR="tmp"

# imaging / dicom
DICOM_MAX_SIZE="4M"
DICOM_STORAGE="FileSystemDicomManager" # SqlDicomManager
DICOM_DIR="data/dicom_storage"

# logging
LOG_FILE="startup.log"
OH_LOG_FILE="openhospital.log"
API_LOG_FILE="api.log"

# SQL creation files
#DB_CREATE_SQL="create_all_en.sql" # default to en
DB_DEMO="create_all_demo.sql"

######################## Other settings ########################
# date format
DATE=`date +%Y-%m-%d_%H-%M-%S`

# downloaded file extension
EXT="tar.gz"

# mysql configuration file
MYSQL_CONF_FILE="my.cnf"

# OH configuration files
OH_SETTINGS="settings.properties"
DATABASE_SETTINGS="database.properties"
EXAMINATION_SETTINGS="examination.properties"
IMAGING_SETTINGS="dicom.properties"
LOG4J_SETTINGS="log4j.properties"
PRINTER_SETTINGS="txtPrinter.properties"
SMS_SETTINGS="sms.properties"
TELEMETRY_SETTINGS="telemetry.properties"
XMPP_SETTINGS="xmpp.properties"
API_SETTINGS="application.properties"
CRED_SETTINGS="default_credentials.properties"
DEMO_CRED_SETTINGS="default_demo_credentials.properties"

# OH jar bin files
OH_GUI_JAR="OH-gui.jar"
OH_API_JAR="openhospital-api-0.1.0.jar"

# help file
HELP_FILE="OH-readme.txt"

# set default database name
DEFAULT_DATABASE_NAME="$DATABASE_NAME"
# set default data base_dir
DEFAULT_DATADIR="$DATA_DIR"

# default database admin/root user
DATABASE_ROOT_USER="root"

# activate expert mode - set to "on" to enable advanced functions - use at your own risk!
EXPERT_MODE="off"
OH_UI_URL="http://localhost:8080"
OH_API_PID="../tmp/oh-api.pid"

################ Architecture and external software ################

######## MariaDB/MySQL Software
# MariaDB version
MYSQL_VERSION="10.6.16"
MYSQL32_VERSION="10.5.23"
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
MYSQL_NAME="MariaDB" # For console output - MariaDB/MYSQL_NAME

######## JAVA Software
######## JAVA 64bit - default architecture

### JRE 11 - openjdk distribution
#JAVA_URL="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9"
#JAVA_DISTRO="OpenJDK11U-jre_x64_linux_hotspot_11.0.11_9"
#JAVA_DIR="jdk-11.0.11+9-jre"

### JRE 17 - zulu distribution
#JAVA_DISTRO="zulu11.68.17-ca-jre11.0.21-linux_$JAVA_PACKAGE_ARCH"
JAVA_DISTRO="zulu17.48.15-ca-jre17.0.10-linux_$JAVA_PACKAGE_ARCH"
JAVA_URL="https://cdn.azul.com/zulu/bin"
JAVA_DIR=$JAVA_DISTRO

######################## DO NOT EDIT BELOW THIS LINE ########################

###########################  Functions  ###########################

###################################################################
function script_menu {
	# show help / user options
	echo " -----------------------------------------------------------------"
	echo "|                                                                 |"
	echo "|                  Open Hospital - $OH_VERSION                         |"
	echo "|                                                                 |"
	echo " -----------------------------------------------------------------"
	echo " arch $ARCH | lang $OH_LANGUAGE | mode $OH_MODE | log level $LOG_LEVEL | Demo $DEMO_DATA"
	echo " -----------------------------------------------------------------"
	if [ "$EXPERT_MODE" == "on" ]; then
		echo " EXPERT MODE activated"
		echo " API server set to $API_SERVER"
		echo " -----------------------------------------------------------------"
	fi
	echo ""
	echo " Usage: $SCRIPT_NAME -[OPTION] "
	echo ""
	echo "   -C    set OH in CLIENT mode"
	echo "   -P    set OH in PORTABLE mode"
	echo "   -S    set OH in SERVER mode (portable)"
	echo "   -l    set language -> [ ${OH_LANGUAGE_LIST[*]} ]"
	echo "   -E    toggle EXPERT MODE - show advanced options"
	echo "   -h    show help"
	echo "   -q    quit"
	echo ""
	if [ "$EXPERT_MODE" == "on" ]; then
		script_menu_advanced;
	fi
}

###################################################################
function script_menu_advanced {
	# show help / user options
	echo "   -------------------------------- "
	echo "    EXPERT MODE - advanced options"
	echo ""
	echo "   -A    toggle API server - EXPERIMENTAL"
	echo "   -e    export/save OH database"
	echo "   -r    restore OH database"
 	echo "   -d    toggle log level INFO/DEBUG"
	echo "   -G    setup GSM"
	echo "   -D    initialize OH with Demo data"
	echo "   -i    initialize/install OH database"
	echo "   -m    configure database connection manually"
	echo "   -s    save OH configuration"
	echo "   -t    test database connection (CLIENT mode only)"
	echo "   -u    create Desktop shortcut"
	echo "   -v    show configuration"
	echo "   -V    check for latest OH version"
	echo "   -X    clean/reset OH installation"
	echo ""
}

###################################################################
function interactive_menu {
	until [[ "$OPTSTRING" != *"$option"* ]]
	do 
		clear;
		script_menu;
		echo ""
		#IFS=
		read -n 1 -p "Please select an option or press enter to start OH: " option
		if [[ $option != "" ]]; then 
			parse_user_input $option 1; # interactive
		else
			break # if enter pressed exit from loop and start OH
		fi
	done
#	OPTIND=1 
}

###################################################################
function get_confirmation {
	# if arg = 1 go back to interactive menu
	read -p "(y/n) ? " choice
	case "$choice" in 
		y|Y ) echo "yes"
		;;
		n|N ) echo "Exiting."; 
			if [[ ${#COMMAND_LINE_ARGS} -eq 0 ]] && [[ $1 -eq 1 ]]; then
				option="";
				interactive_menu;
			else
				exit 1;
			fi
		;;
		* ) echo "Invalid choice. Press any key to continue."; 
			read;
			if [[ ${#COMMAND_LINE_ARGS} -eq 0 ]] && [[ $1 -eq 1 ]]; then
				option="";
				interactive_menu;
			else
				exit 1;
			fi
	exit 0;
	esac
}

###################################################################
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
}

###################################################################
function read_settings {
	# check and read OH version file
	if [ -f ./$OH_DIR/rsc/version.properties ]; then
		source "./$OH_DIR/rsc/version.properties"
		OH_VERSION=$VER_MAJOR.$VER_MINOR.$VER_RELEASE
	else 
		echo "Error: Open Hospital not found! Exiting."
		exit 1;
	fi
	
	# check for OH settings file and read values
	if [ -f ./$OH_DIR/rsc/$OH_SETTINGS ]; then
		echo "Reading OH settings file..."
		. ./$OH_DIR/rsc/$OH_SETTINGS
		
		OH_MODE=$MODE
		OH_LANGUAGE=$LANGUAGE
		OH_SINGLE_USER=$SINGLE_USER
		OH_DOC_DIR=$OH_DOC_DIR
		DEMO_DATA=$DEMODATA
		API_SERVER=$APISERVER
	fi

	# check for database settings file and read values
	if [ -f ./$OH_DIR/rsc/$DATABASE_SETTINGS ]; then
		echo "Reading database settings file..."
		# source "./$OH_DIR/rsc/$DATABASE_SETTINGS"
		DATABASE_SERVER=$(cat $OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.url" | cut -d"/" -f3 | cut -d":" -f1)
		DATABASE_PORT=$(cat $OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.url" | cut -d"/" -f3 | cut -d":" -f2)
		DATABASE_NAME=$(cat $OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.url" | cut  -d"/" -f4)
		DATABASE_USER=$(cat $OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.username" | cut -d"=" -f2)
		DATABASE_PASSWORD=$(cat $OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.password" | cut -d"=" -f2)
	else 
		echo "Warning: configuration file $DATABASE_SETTINGS not found."
	fi
}

###################################################################
function set_defaults {
	# set default values for script variables
	# config file generation - set default to off
#####	if [ -n ${WRITE_CONFIG_FILES+x} ]; then
	if [ -z "$WRITE_CONFIG_FILES" ]; then
		WRITE_CONFIG_FILES="off"
	fi

	# OH mode - set default to PORTABLE
	if [ -z "$OH_MODE" ]; then
		OH_MODE="PORTABLE"
	fi

	# OH language - set default to en
	if [ -z "$OH_LANGUAGE" ]; then
		OH_LANGUAGE="en"
	fi
	
	# OH DOC DIR - set default to "doc" (../doc for oh)
	if [ -z "$OH_DOC_DIR" ]; then
		OH_DOC_DIR="doc"
	fi

	# set database creation script in chosen language
	if [ -z "$DB_CREATE_SQL" ]; then
		DB_CREATE_SQL="create_all_$OH_LANGUAGE.sql"
	fi

	# log level - set default to INFO
	if [ -z "$LOG_LEVEL" ]; then
		LOG_LEVEL="INFO"
	fi
	
	# single / multiuser - set "yes" for single user configuration
	if [ -z "$OH_SINGLE_USER" ]; then
		OH_SINGLE_USER="no"
	fi

	# demo data - set default to off
	if [ -z "$DEMO_DATA" ]; then
		DEMO_DATA="off"
	fi

	# api server - set default to off
	if [ -z "$API_SERVER" ]; then
		API_SERVER="off"
	fi
	
	# UI interface - set default to off
	if [ -z "$UI_INTERFACE" ]; then
		UI_INTERFACE="off"
	fi

	# EXPERT_MODE features - set default to off
	if [ -z "$EXPERT_MODE" ]; then
		EXPERT_MODE="off"
	fi

	# set escaped path (/ in place of \)
	OH_PATH_ESCAPED=$(echo $OH_PATH | sed -e 's/\//\\\//g')
	DICOM_DIR_ESCAPED=$(echo $DICOM_DIR | sed -e 's/\//\\\//g')
	PHOTO_DIR_ESCAPED=$(echo $PHOTO_DIR | sed -e 's/\//\\\//g')
	LOG_DIR_ESCAPED=$(echo $LOG_DIR | sed -e 's/\//\\\//g')
	TMP_DIR_ESCAPED=$(echo $TMP_DIR | sed -e 's/\//\\\//g')

}

###################################################################
function set_db_name {
	# set DATA_DIR with db name
	DATA_DIR=$DEFAULT_DATADIR/$DATABASE_NAME
	# set escaped values
	DATA_DIR_ESCAPED=$(echo $DATA_DIR | sed -e 's/\//\\\//g')
}

###################################################################
function set_oh_mode {
	# if $OH_SETTINGS is present set OH mode
	if [ -f ./$OH_DIR/rsc/$OH_SETTINGS ]; then
		echo "Configuring OH mode..."
		######## $OH_SETTINGS OH mode configuration
		echo "Setting OH mode to $OH_MODE in OH configuration file -> $OH_SETTINGS..."
		sed -e "/^"MODE="/c"MODE=$OH_MODE"" -i ./$OH_DIR/rsc/$OH_SETTINGS
	else 
		echo ""
		echo ""
		echo "Warning: $OH_SETTINGS file not found."
	fi
	echo "OH mode set to $OH_MODE"
}

###################################################################
function set_demo_data {
	# set database name for demo data
	case "$DEMO_DATA" in
			*on*)
				DATABASE_NAME=$DEMO_DATABASE
			;;
			*off*)
				DATABASE_NAME="$DEFAULT_DATABASE_NAME"
			;;
	esac
}

###################################################################
function check_language {
	# check for valid language selection

	for lang in "${OH_LANGUAGE_LIST[@]}"; do
		if [[ $lang == $OH_LANGUAGE ]]; then
			echo ""
			echo "Language set to $OH_LANGUAGE"
			return 0;
		fi
	done
	echo ""
	echo "Invalid language option [$OH_LANGUAGE]: setting to default [en]"
	OH_LANGUAGE=en
}

###################################################################
function set_language {
	# set localized database creation script
	DB_CREATE_SQL="create_all_$OH_LANGUAGE.sql"

	# if $OH_SETTINGS is present set language
	if [ -f ./$OH_DIR/rsc/$OH_SETTINGS ]; then
		echo "Configuring OH language..."
		######## $OH_SETTINGS language configuration
		echo "Setting language to $OH_LANGUAGE in OH configuration file -> $OH_SETTINGS..."
		sed -e "/^"LANGUAGE="/c"LANGUAGE=$OH_LANGUAGE"" -i ./$OH_DIR/rsc/$OH_SETTINGS
		echo "Language set to $OH_LANGUAGE."
	else 
		echo ""
		echo "Warning: $OH_SETTINGS file not found."
	fi
}

###################################################################
function set_log_level {
	if [ -f ./$OH_DIR/rsc/$LOG4J_SETTINGS ]; then
		echo ""
		######## $LOG4J_SETTINGS log_level configuration
		echo "Setting log level to $LOG_LEVEL in OH configuration file -> $LOG4J_SETTINGS..."
		case "$LOG_LEVEL" in
			*INFO*)
				sed -e "s/DEBUG/$LOG_LEVEL/g" -i ./$OH_DIR/rsc/$LOG4J_SETTINGS 
			;;
			*DEBUG*)
				sed -e "s/INFO/$LOG_LEVEL/g" -i ./$OH_DIR/rsc/$LOG4J_SETTINGS 
			;;
			*)
				echo "Invalid log level: $LOG_LEVEL. Exiting."
				exit 1
			;;
		esac
		echo "Log level set to $LOG_LEVEL"
	else 
		echo ""
		echo "Warning: $LOG4J_SETTINGS file not found."
	fi
}

###################################################################
function initialize_dir_structure {
	# create directory structure
	mkdir -p "./$TMP_DIR"
	mkdir -p "./$LOG_DIR"
	mkdir -p "./$DICOM_DIR"
	mkdir -p "./$PHOTO_DIR"
	mkdir -p "./$BACKUP_DIR"
}

###################################################################
function create_desktop_shortcut {
echo "Creating/updating OH shortcut on Desktop..."
# Create Desktop application entry
desktop_path=$(xdg-user-dir DESKTOP)
echo "[Desktop Entry]
	Type=Application
	# The version of the Desktop Entry Specification
	Version=1.x
	# The name of the application
	Name=OpenHospital
	# A comment which will be used as a tooltip
	Comment=Open Hospital 1.x shortcut
	# The path to the folder in which the executable is run
	Path=$OH_PATH
	# The executable of the application, possibly with arguments
	Exec=$OH_PATH/$SCRIPT_NAME -Z
	# The icon to display
	Icon=$OH_PATH/oh.ico
	# Describes whether this application needs to be run in a terminal or not
	Terminal=true
	# Describes the categories in which this entry should be shown
	Categories=Utility;Application;
	" > $desktop_path/OpenHospital.desktop
echo "Done!"
}

###################################################################
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
	OH_CLASSPATH="$OH_PATH"/$OH_DIR/bin/$OH_GUI_JAR
	
	# include all needed directories
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/bundle
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rpt_base
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rpt_extra
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rpt_stat
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc/images
	# OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/rsc/icons # hardcoded
	OH_CLASSPATH=$OH_CLASSPATH:"$OH_PATH"/$OH_DIR/lib

	# include all jar files under lib/
	DIRLIBS="$OH_PATH"/$OH_DIR/lib/*.jar
	for i in ${DIRLIBS}
	do
		OH_CLASSPATH="$i":$OH_CLASSPATH
	done
}

###################################################################
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

###################################################################
function mysql_check {
if [ ! -d "./$MYSQL_DIR" ]; then
	if [ ! -f "./$MYSQL_DIR.$EXT" ]; then
		echo "Warning - $MYSQL_NAME not found. Do you want to download it?"
		get_confirmation;
		# download mysql binary
		echo "Downloading $MYSQL_DIR..."
		wget -P ./ $MYSQL_URL/$MYSQL_DIR.$EXT
	fi
	echo "Unpacking $MYSQL_DIR..."
	tar xf ./$MYSQL_DIR.$EXT -C ./
	if [ $? -ne 0 ]; then
		echo "Error unpacking $MYSQL_NAME. Exiting."
		exit 1
	fi
	echo "$MYSQL_NAME unpacked successfully!"
	echo "Removing downloaded file..."
	rm ./$MYSQL_DIR.$EXT
	echo "Done!"
fi
# check for mysqld binary
if [ -x ./$MYSQL_DIR/bin/mysqld_safe ]; then
	echo "$MYSQL_NAME found!"
	echo "Using $MYSQL_DIR"
else
	echo "$MYSQL_NAME not found! Exiting."
	exit 1
fi
# check for libaio
ldconfig -p | grep libaio > /dev/null;
if [ $? -eq 1 ]; then
	echo "Error: libaio not found! Please install the library. Exiting."
	exit 1
fi
# check for libncurses - version 5
ldconfig -p | grep libncurses.so.5 > /dev/null;
if [ $? -eq 1 ]; then
	echo "Error: libncurses version 5 not found! Please install the library. Exiting."
	exit 1
fi
}

###################################################################
function config_database {
	echo "Checking for $MYSQL_NAME config file..."
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$CONF_DIR/$MYSQL_CONF_FILE ]; then
		[ -f ./$CONF_DIR/$MYSQL_CONF_FILE ] && mv -f ./$CONF_DIR/$MYSQL_CONF_FILE ./$CONF_DIR/$MYSQL_CONF_FILE.old

		# find a free TCP port to run MariaDB/MySQL starting from the default port
		echo "Looking for a free TCP port for $MYSQL_NAME database..."
		while [[ $(ss -tl4  sport = :$DATABASE_PORT | grep LISTEN) ]]; do
			DATABASE_PORT=$(expr $DATABASE_PORT + 1)
		done
		echo "Found TCP port $DATABASE_PORT!"

		echo "Writing $MYSQL_NAME config file..."
		sed -e "s/DATABASE_SERVER/$DATABASE_SERVER/g" -e "s/DICOM_SIZE/$DICOM_MAX_SIZE/g" -e "s/OH_PATH_SUBSTITUTE/$OH_PATH_ESCAPED/g" \
		-e "s/TMP_DIR/$TMP_DIR_ESCAPED/g" -e "s/DATA_DIR/$DATA_DIR_ESCAPED/g" -e "s/LOG_DIR/$LOG_DIR_ESCAPED/g" \
		-e "s/DATABASE_PORT/$DATABASE_PORT/g" -e "s/MYSQL_DISTRO/$MYSQL_DIR/g" ./$CONF_DIR/$MYSQL_CONF_FILE.dist > ./$CONF_DIR/$MYSQL_CONF_FILE
	fi
}

###################################################################
function initialize_database {
	# create data directory
	mkdir -p "./$DATA_DIR"
	# initialize MariaDB/MySQL
	echo "Initializing $MYSQL_NAME database on port $DATABASE_PORT..."
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
		echo "Error: $MYSQL_NAME initialization failed! Exiting."
		exit 2
	fi
}

###################################################################
function start_database {
	echo "Checking if $MYSQL_NAME is running..."
	if [ -f "$OH_PATH/$TMP_DIR/mysql.sock" ] || [ -f "$OH_PATH/$TMP_DIR/mysql.pid" ] ; then
		echo "$MYSQL_NAME already running ! Exiting."
		exit 1
	fi

	echo "Starting $MYSQL_NAME server... "
	./$MYSQL_DIR/bin/mysqld_safe --defaults-file=./$CONF_DIR/$MYSQL_CONF_FILE >> ./$LOG_DIR/$LOG_FILE 2>&1 &
	if [ $? -ne 0 ]; then
		echo "Error: $MYSQL_NAME server not started! Exiting."
		exit 2
	fi
	# wait till the MariaDB/MySQL tcp port is open
	until nc -z $DATABASE_SERVER $DATABASE_PORT; do sleep 1; done
	echo "$MYSQL_NAME server started! "
}

###################################################################
function set_database_root_pw {
	# if using MySQL/MariaDB root password need to be set
	echo "Setting $MYSQL_NAME $DATABASE_ROOT_USER password..."
	./$MYSQL_DIR/bin/mysql -u $DATABASE_ROOT_USER --skip-password --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp -e "ALTER USER '$DATABASE_ROOT_USER'@'$DATABASE_SERVER' IDENTIFIED BY '$DATABASE_ROOT_PW';" >> ./$LOG_DIR/$LOG_FILE 2>&1
	
	if [ $? -ne 0 ]; then
		echo "Error: $MYSQL_NAME root password not set! Try resetting installation with option [X]. Exiting."
		shutdown_database;
		exit 2
	fi
}

###################################################################
function create_database_user {
	echo "Creating database user [$DATABASE_USER]..."
	# create database user
	./$MYSQL_DIR/bin/mysql -u $DATABASE_ROOT_USER -p$DATABASE_ROOT_PW --protocol=tcp --host=$DATABASE_SERVER --port=$DATABASE_PORT \
	-e "CREATE USER '$DATABASE_USER'@'$DATABASE_SERVER' IDENTIFIED BY '$DATABASE_PASSWORD'; \
	CREATE USER '$DATABASE_USER'@'%' IDENTIFIED BY '$DATABASE_PASSWORD';" >> ./$LOG_DIR/$LOG_FILE 2>&1
	
	if [ $? -ne 0 ]; then
		echo "Error: Database user creation failed! Exiting."
		shutdown_database;
		exit 2
	fi
}

###################################################################
function create_database {
	echo "Creating database [$DATABASE_NAME]..."
	# create OH database
	./$MYSQL_DIR/bin/mysql -u $DATABASE_ROOT_USER -p$DATABASE_ROOT_PW --protocol=tcp --host=$DATABASE_SERVER --port=$DATABASE_PORT \
	-e "CREATE DATABASE $DATABASE_NAME CHARACTER SET utf8; GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$DATABASE_USER'@'$DATABASE_SERVER'; \
	GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$DATABASE_USER'@'%' ; " >> ./$LOG_DIR/$LOG_FILE 2>&1
	
	if [ $? -ne 0 ]; then
		echo "Error: Database creation failed! Exiting."
		shutdown_database;
		exit 2
	fi
}

###################################################################
function import_database {
	echo "Checking for SQL creation script..."
	# check for database creation script
	if [ -f ./$SQL_DIR/$DB_CREATE_SQL ]; then
		echo "Using SQL file $SQL_DIR/$DB_CREATE_SQL..."
	else
		echo "Error: No SQL file found! Exiting."
		shutdown_database;
		exit 2
	fi

	# create OH database structure
	echo "Importing database [$DATABASE_NAME] with user [$DATABASE_USER@$DATABASE_SERVER]..."
	cd "./$SQL_DIR"
#	../$MYSQL_DIR/bin/mysql --local-infile=1 -u $DATABASE_ROOT_USER -p$DATABASE_ROOT_PW --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp $DATABASE_NAME < ./$DB_CREATE_SQL >> ../$LOG_DIR/$LOG_FILE 2>&1
	../$MYSQL_DIR/bin/mysql --local-infile=1 -u $DATABASE_USER -p$DATABASE_PASSWORD --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp $DATABASE_NAME < ./$DB_CREATE_SQL >> ../$LOG_DIR/$LOG_FILE 2>&1
	if [ $? -ne 0 ]; then
		echo "Error: Database not imported! Exiting."
		shutdown_database;
		cd "$CURRENT_DIR"
		exit 2
	fi
	
	# EXPERIMENTAL ONLY
	# workaround for hard coded password limit - execute extra sql script 
	# not needed anymore - see OP-1078
#	if [ "$API_SERVER" = "on" ]; then
#		echo "Setting admin password..."
#		cd "$OH_PATH/$SQL_EXTRA_DIR/"
#		$OH_PATH/$MYSQL_DIR/bin/mysql --local-infile=1 -u $DATABASE_ROOT_USER -p$DATABASE_ROOT_PW --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp $DATABASE_NAME < ./reset_admin_password_strong.sql >> ../../$LOG_DIR/$LOG_FILE 2>&1
#		if [ $? -ne 0 ]; then
#		echo "Error! Exiting."
#			shutdown_database;
#			cd "$CURRENT_DIR"
#			exit 2
#		fi
#	fi

	# end
	cd "$OH_PATH"
}

###################################################################
function dump_database {
	# save OH database if existing
	if [ -x ./$MYSQL_DIR/bin/mysqldump ]; then
		mkdir -p "$OH_PATH/$BACKUP_DIR"
		echo "Dumping $MYSQL_NAME database..."
		./$MYSQL_DIR/bin/mysqldump --skip-extended-insert -u $DATABASE_USER --password=$DATABASE_PASSWORD --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp $DATABASE_NAME > ./$BACKUP_DIR/mysqldump_$DATE.sql
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
	echo "$MYSQL_NAME dump file $BACKUP_DIR/mysqldump_$DATE.sql completed!"
}

###################################################################
function shutdown_database {
	if [ "$OH_MODE" != "CLIENT" ]; then
		echo "Shutting down $MYSQL_NAME..."
		cd "$OH_PATH"
		./$MYSQL_DIR/bin/mysqladmin -u $DATABASE_ROOT_USER -p$DATABASE_ROOT_PW --host=$DATABASE_SERVER --port=$DATABASE_PORT --protocol=tcp shutdown >> ./$LOG_DIR/$LOG_FILE 2>&1
		# wait till the MySQL tcp port is closed
		until !( nc -z $DATABASE_SERVER $DATABASE_PORT ); do sleep 1; done
		echo "$MYSQL_NAME stopped!"
	else
		exit 1
	fi
}

###################################################################
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

###################################################################
function write_api_config_file {
	######## application.properties setup - OH API server
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$OH_DIR/rsc/$API_SETTINGS ]; then
		[ -f ./$OH_DIR/rsc/$API_SETTINGS ] && mv -f ./$OH_DIR/rsc/$API_SETTINGS ./$OH_DIR/rsc/$API_SETTINGS.old
		# generate OH API token and save to settings file
		# JWT_TOKEN_SECRET=`openssl rand -base64 64 | xargs`
		JWT_TOKEN_SECRET=`LC_ALL=C tr -dc A-Za-z0-9 </dev/urandom | head -c 66`
		echo "Writing OH API configuration file -> $API_SETTINGS..."
		sed -e "s/JWT_TOKEN_SECRET/"$JWT_TOKEN_SECRET"/g" -e "s&OH_API_PID&"$OH_API_PID"&g" ./$OH_DIR/rsc/$API_SETTINGS.dist > ./$OH_DIR/rsc/$API_SETTINGS
	fi
}

###################################################################
function copy_config_file {
	# function to copy a single configuration file with backup
	# usage: copy_config_file [file_name]
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$OH_DIR/rsc/$1 ]; then
		[ -f ./$OH_DIR/rsc/$1 ] && mv -f ./$OH_DIR/rsc/$1 ./$OH_DIR/rsc/$1.old
		echo "Writing OH configuration file -> $1..."
		cp ./$OH_DIR/rsc/$1.dist ./$OH_DIR/rsc/$1
	fi
}

###################################################################
function write_config_files {
	# set up configuration files
	echo "Checking for OH configuration files..."
	######## IMAGING / DICOM setup
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$OH_DIR/rsc/$IMAGING_SETTINGS ]; then
		[ -f ./$OH_DIR/rsc/$IMAGING_SETTINGS ] && mv -f ./$OH_DIR/rsc/$IMAGING_SETTINGS ./$OH_DIR/rsc/$IMAGING_SETTINGS.old
		echo "Writing OH configuration file -> $IMAGING_SETTINGS..."
		sed -e "s/DICOM_SIZE/$DICOM_MAX_SIZE/g" -e "s/OH_PATH_SUBSTITUTE/$OH_PATH_ESCAPED/g" \
		-e "s/DICOM_STORAGE/$DICOM_STORAGE/g" -e "s/DICOM_DIR/$DICOM_DIR_ESCAPED/g" ./$OH_DIR/rsc/$IMAGING_SETTINGS.dist > ./$OH_DIR/rsc/$IMAGING_SETTINGS
	fi
	######## LOG4J_SETTINGS setup
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$OH_DIR/rsc/$LOG4J_SETTINGS ]; then
		OH_LOG_DEST="$OH_PATH_ESCAPED/$LOG_DIR/$OH_LOG_FILE"
		[ -f ./$OH_DIR/rsc/$LOG4J_SETTINGS ] && mv -f ./$OH_DIR/rsc/$LOG4J_SETTINGS ./$OH_DIR/rsc/$LOG4J_SETTINGS.old
		echo "Writing OH configuration file -> $LOG4J_SETTINGS..."
		sed -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/" -e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		-e "s/DBNAME/$DATABASE_NAME/g" -e "s/LOG_LEVEL/$LOG_LEVEL/g" -e "s+LOG_DEST+$OH_LOG_DEST+g" \
		./$OH_DIR/rsc/$LOG4J_SETTINGS.dist > ./$OH_DIR/rsc/$LOG4J_SETTINGS
	fi
	######## DATABASE_SETTINGS setup 
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$OH_DIR/rsc/$DATABASE_SETTINGS ]; then
		[ -f ./$OH_DIR/rsc/$DATABASE_SETTINGS ] && mv -f ./$OH_DIR/rsc/$DATABASE_SETTINGS ./$OH_DIR/rsc/$DATABASE_SETTINGS.old
		echo "Writing OH database configuration file -> $DATABASE_SETTINGS..."
		sed -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/g" -e "s/DBNAME/$DATABASE_NAME/g" \
		-e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		./$OH_DIR/rsc/$DATABASE_SETTINGS.dist > ./$OH_DIR/rsc/$DATABASE_SETTINGS
	fi
	######## OH_SETTINGS setup
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f ./$OH_DIR/rsc/$OH_SETTINGS ]; then
		[ -f ./$OH_DIR/rsc/$OH_SETTINGS ] && mv -f ./$OH_DIR/rsc/$OH_SETTINGS ./$OH_DIR/rsc/$OH_SETTINGS.old
		echo "Writing OH configuration file -> $OH_SETTINGS..."
		sed -e "s/OH_MODE/$OH_MODE/g" -e "s/OH_LANGUAGE/$OH_LANGUAGE/g" -e "s&OH_DOC_DIR&../$OH_DOC_DIR&g" \
		-e "s/DEMODATA=off/"DEMODATA=$DEMO_DATA"/g" -e "s/YES_OR_NO/$OH_SINGLE_USER/g" \
		-e "s/PHOTO_DIR/$PHOTO_DIR_ESCAPED/g" -e "s/APISERVER=off/"APISERVER=$API_SERVER"/g" \
		./$OH_DIR/rsc/$OH_SETTINGS.dist > ./$OH_DIR/rsc/$OH_SETTINGS
	fi

	######## OH - Other settings setup
	copy_config_file $EXAMINATION_SETTINGS;
	copy_config_file $PRINTER_SETTINGS;
	copy_config_file $SMS_SETTINGS;
	copy_config_file $TELEMETRY_SETTINGS;
	copy_config_file $XMPP_SETTINGS;

	######## DEFAULT_CREDENTIALS_SETTINGS setup
	if [ "$OH_MODE" == "PORTABLE" ]; then
		copy_config_file $CRED_SETTINGS;
	fi
	if [ "$DEMO_DATA" = "on" ]; then
		cp ./$OH_DIR/rsc/$DEMO_CRED_SETTINGS.dist ./$OH_DIR/rsc/$CRED_SETTINGS
	fi
}

###################################################################
function clean_database {
	# remove socket and pid file
	echo "Removing socket and pid file..."
	rm -rf ./$TMP_DIR/*
	# remove database files
	echo "Removing databases..."
	# removing all databases under default data dir (prod / demo)
	rm -rf ./$DEFAULT_DATADIR/*
}

###################################################################
function clean_conf_files {
	# remove configuration files - leave only .dist files
	echo "Removing configuration files..."
	rm -f ./$CONF_DIR/$MYSQL_CONF_FILE
	rm -f ./$OH_DIR/rsc/$OH_SETTINGS
	rm -f ./$OH_DIR/rsc/$OH_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$DATABASE_SETTINGS
	rm -f ./$OH_DIR/rsc/$DATABASE_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$EXAMINATION_SETTINGS
	rm -f ./$OH_DIR/rsc/$EXAMINATION_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$IMAGING_SETTINGS
	rm -f ./$OH_DIR/rsc/$IMAGING_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$LOG4J_SETTINGS
	rm -f ./$OH_DIR/rsc/$LOG4J_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$PRINTER_SETTINGS
	rm -f ./$OH_DIR/rsc/$PRINTER_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$SMS_SETTINGS
	rm -f ./$OH_DIR/rsc/$SMS_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$TELEMETRY_SETTINGS
	rm -f ./$OH_DIR/rsc/$TELEMETRY_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$XMPP_SETTINGS
	rm -f ./$OH_DIR/rsc/$XMPP_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$API_SETTINGS
	rm -f ./$OH_DIR/rsc/$API_SETTINGS.old
	rm -f ./$OH_DIR/rsc/$CRED_SETTINGS
	rm -f ./$OH_DIR/rsc/$CRED_SETTINGS.old
}

###################################################################
function clean_log_files {
	# remove all log files
	echo "Removing log files..."
	rm -f ./$LOG_DIR/*
}

###################################################################
function start_gui {
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
}

###################################################################
function start_api_server {
	# check for application configuration files
	if [ ! -f ./$OH_DIR/rsc/$API_SETTINGS ]; then
		echo "Error: missing $API_SETTINGS settings file. Exiting"
		exit 1;
	fi
	
	########## WORKAROUND to kill existing API server process ##################
	########## TO BE REMOVED IN NEXT RELEASES
	##########
	# check for stale PID files
	if [ -f $OH_PATH/$TMP_DIR/$OH_API_PID ]; then
		API_PID_NUMBER=$(cat $OH_PATH/$TMP_DIR/$OH_API_PID)
		echo "Killing API server - process $API_PID_NUMBER..."
		kill $API_PID_NUMBER
	fi
	##########

	echo "------------------------"
	echo "---- EXPERIMENTAL ------"
	echo "------------------------"
	echo "Starting API server..."
	echo "Please wait, it might take some time..."
	echo ""
	echo "Connect to http://$OH_UI_URL for dashboard"
	echo ""
	
	cd "$OH_PATH/$OH_DIR" # workaround for hard coded paths
	$JAVA_BIN -client -Xms64m -Xmx1024m -cp "./bin/$OH_API_JAR:./rsc::./static" org.springframework.boot.loader.JarLauncher >> ../$LOG_DIR/$API_LOG_FILE 2>&1 &
	
	if [ $? -ne 0 ]; then
		echo "An error occurred while starting Open Hospital API. Exiting."
		shutdown_database;
		cd "$CURRENT_DIR"
		exit 4
	fi
	cd "$OH_PATH"
}

###################################################################
function start_ui {
	echo "Starting Open Hospital UI at $OH_UI_URL..."
	# OH UI launch
	if which gnome-open > /dev/null; then
		gnome-open $OH_UI_URL
	elif which xdg-open > /dev/null; then
		xdg-open $OH_UI_URL
	elif [ ! -n $BROWSER ]; then
		$BROWSER $OH_UI_URL
	else
		echo "Could not detect the web browser to use."
	fi
}

###################################################################
function check_latest_oh_version {
	echo "Checking online for Open Hospital latest version..."
	LATEST_OH_VERSION=$(curl -s -L https://api.github.com/repos/informatici/openhospital/releases/latest | grep tag_name  | cut -b16-22)
	echo "Latest OH version is" $LATEST_OH_VERSION
	echo ""
}

###################################################################
function parse_user_input {
	case $1 in
	###################################################
	A)	# toggle API server
		case "$API_SERVER" in
			*on*)
				API_SERVER="off";
			;;
			*off*)
				API_SERVER="on";
			;;
		esac
		#
		write_api_config_file;
		if (( $2==0 )); then API_SERVER="on"; fi
		option="Z";
		;;
	###################################################
	E)	# toggle EXPERT_MODE features
		case "$EXPERT_MODE" in
			*on*)
				EXPERT_MODE="off";
			;;
			*off*)
				EXPERT_MODE="on";
			;;
		esac
		#
		if (( $2==0 )); then EXPERT_MODE="on"; interactive_menu; fi
		option="Z";
		;;
	###################################################
	C)	# start in CLIENT mode
		OH_MODE="CLIENT"
		DEMO_DATA="off"
		set_oh_mode;
		echo ""
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	P)	# start in PORTABLE mode
		OH_MODE="PORTABLE"
		set_oh_mode;
		echo ""
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	S)	# start in SERVER mode
		OH_MODE="SERVER"
		set_oh_mode;
		echo ""
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	d)	# toggle debug mode 
		case "$LOG_LEVEL" in
			*INFO*)
				LOG_LEVEL="DEBUG";
			;;
			*DEBUG*)
				LOG_LEVEL="INFO";
			;;
		esac
		set_log_level;
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	D)	# demo mode
		echo ""
		# exit if OH is configured in CLIENT mode
		if [ "$OH_MODE" = "CLIENT" ]; then
			echo "Error - OH_MODE set to CLIENT mode. Cannot run with Demo data. Exiting."
			exit 1;
		fi	
		if (( $2==0 )); then DEMO_DATA="off"; fi # workaround for -D option

		# invert values if D is pressed
		case "$DEMO_DATA" in
				*on*)
					DEMO_DATA="off";
				;;
				*off*)
					DEMO_DATA="on";
				;;
		esac

		# update configuration settings
		set_demo_data;
		set_db_name;

		WRITE_CONFIG_FILES=on; write_config_files;

		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	G)	# set up GSM
		echo ""
		echo "Setting up GSM..."
		java_check;
		java_lib_setup;
		$JAVA_BIN -Djava.library.path=${NATIVE_LIB_PATH} -classpath "$OH_CLASSPATH" org.isf.utils.sms.SetupGSM "$@"
		echo "Done!"
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	h)	# help
		if (( $2==0 )); then
			script_menu;
			exit 0;
		fi
		cat $HELP_FILE | less;
		;;
	###################################################
	i)	# initialize/install OH database
		# set mode to CLIENT
		#OH_MODE="CLIENT"
		echo ""
		echo "*************************************************************"
		echo "***             Database installation wizard              ***"
		echo "*************************************************************"
		echo ""
		echo "Current database settings are:"
		echo ""
		echo " Database Server -> $DATABASE_SERVER"
		echo " TCP port -> $DATABASE_PORT" 
		echo " Database name -> $DATABASE_NAME"
		echo " Database user -> $DATABASE_USER"
		echo " Database password -> $DATABASE_PASSWORD"
		echo ""
		echo "-> To change this values select [m] option from main menu <-"
		echo ""
		echo "Do you want to initialize/install the [$DATABASE_NAME] database?"
		echo ""
		get_confirmation 1;
		initialize_dir_structure;
		check_language;
		set_language;
		mysql_check;
		echo "Do you want to create the [$DATABASE_USER] user and [$DATABASE_NAME] database on [$DATABASE_SERVER] server?"
		read -p "Press [y] to confirm: " choice
		if [ "$choice" = "y" ]; then
			# ask user for root database password
			read -p "Please insert the MariaDB / MySQL database root password [$DATABASE_ROOT_USER@$DATABASE_SERVER] -> " -s DATABASE_ROOT_PW
			echo ""
			create_database_user;
			create_database;
		fi
		# ask user for database password
		read -p "Please insert the MariaDB / MySQL database password for user [$DATABASE_USER@$DATABASE_SERVER] -> " -s DATABASE_PASSWORD
		echo ""
		echo "Do you want to install the [$DATABASE_NAME] database on [$DATABASE_SERVER]?"
		get_confirmation 1;
		test_database_connection;
		import_database;
		echo "Done!"
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	l)	# set language
		echo ""
		#WRITE_CONFIG_FILES="on"
		if (( $2==0 )); then
			OH_LANGUAGE="$OPTARG"
			option="Z";
		else
			echo ""
			echo "Available languages:"
			echo ""
			# show all available languages
			for ((i=0; i < ${#OH_LANGUAGE_LIST[@]}; i++ )); do echo " ${OH_LANGUAGE_LIST[$i]} - ${OH_LANGUAGE_LIST_INFO[$i]} "; done
			echo ""
			read -n 2 -p "Please select language [${OH_LANGUAGE_LIST[*]}] (default is en): " OH_LANGUAGE
		fi
		check_language;
		set_language;
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	m)	# configure OH database connection manually
		DEMO_DATA="off"
		#read -p "Please select Single user configuration (yes/no): " OH_SINGLE_USER
		###### OH_SINGLE_USER=${OH_SINGLE_USER:-Off} # set default # TBD
		echo ""
		echo "**************************************************************"
		echo "***         Database server configuration wizard           ***"
		echo "***   Enter settings and generate OH configuration files   ***"
		echo "**************************************************************"
		echo ""
		read -p "Enter database server IP address [DATABASE_SERVER]: " DATABASE_SERVER
		read -p "Enter database server TCP port [DATABASE_PORT]: " DATABASE_PORT
		read -p "Enter database database name [DATABASE_NAME]: " DATABASE_NAME
		read -p "Enter database user name [DATABASE_USER]: " DATABASE_USER
		read -p "Enter database password [DATABASE_PASSWORD]: " -s DATABASE_PASSWORD

		echo ""
		echo ""
		echo "-> Database settings <-"
		echo ""
		echo "-> DATABASE_SERVER=$DATABASE_SERVER"
		echo "-> DATABASE_PORT=$DATABASE_PORT"
		echo "-> DATABASE_NAME=$DATABASE_NAME"
		echo "-> DATABASE_USER=$DATABASE_USER"
		echo ""
		set_db_name;
		WRITE_CONFIG_FILES="on"; write_config_files;
		echo "Done!"
		echo ""
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	e)	# export/save database
		echo ""
		# check if mysql utilities exist
		mysql_check;
		# check if portable mode is on
		if [ "$OH_MODE" != "CLIENT" ]; then
			# check if database already exists
			if [ -d ./"$DATA_DIR" ]; then
				config_database;
				start_database;
			else
				echo ""
	        		echo "Error: no data found! Exiting."
				exit 1
			fi
		fi
		test_database_connection;
		echo "Saving Open Hospital database..."
		dump_database;
		if [ "$OH_MODE" != "CLIENT" ]; then
			shutdown_database;
		fi
		echo "Done!"
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	r)	# restore database
		echo ""
		echo "*************************************************************"
		echo "***               Database restore wizard                 ***"
		echo "*************************************************************"
		# check if local portable database exists
		if [ "$OH_MODE" != "CLIENT" ] && [ -d ./"$DATA_DIR" ]; then
			echo ""
			echo "Error: Portable database already present. Remove existing data before restoring.."
		else
			echo ""
			# ask user for database/sql script to restore
			read -p "Enter SQL dump/backup file that you want to restore - (in $SQL_DIR subdirectory) -> " DB_CREATE_SQL
			if [ ! -f $OH_PATH/$SQL_DIR/$DB_CREATE_SQL ]; then
				echo "Error: No SQL file found! Exiting."
			else
				echo "Found $DB_CREATE_SQL - are you sure you want to restore it on [$DATABASE_NAME@$DATABASE_SERVER] ?"
				get_confirmation 1;
				# check if mysql utilities exist
				mysql_check;
				if [ "$OH_MODE" != "CLIENT" ]; then
					set_db_name;
					config_database;
					initialize_dir_structure;
					initialize_database;
					start_database;
					set_database_root_pw;
					create_database_user;
					create_database;
				fi
				test_database_connection;
				import_database;
				if [ $OH_MODE != "CLIENT" ]; then
					shutdown_database;
				fi
		        	echo "Done!"
			fi
		fi
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	s)	# save / write config files
		echo ""
		echo "Do you want to save current settings to OH configuration files?"
		get_confirmation 1;
		# overwrite configuration files if existing
		WRITE_CONFIG_FILES=on; write_config_files;
		set_oh_mode;
		check_language;
		set_language;
		set_log_level;
		echo "Done!"
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	t)	# test database connection
		echo ""
		if [ "$OH_MODE" != "CLIENT" ]; then
			echo "Error: Only for CLIENT mode."
		else
			mysql_check;
			test_database_connection;
		fi
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	u)	# create Desktop shortcut
		echo ""
		create_desktop_shortcut;
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	v)	# display software version and configuration
		echo ""
		echo "--------- OH version ---------"
		echo "Open Hospital version: $OH_VERSION"
		echo ""
		echo "--------- Software versions ---------"
		echo "$MYSQL_NAME version: $MYSQL_DIR"
		echo "JAVA version: $JAVA_DISTRO"
		echo ""
		echo "--------- Script Configuration ---------"
		echo "Architecture is $ARCH"
		echo "Config file generation is set to $WRITE_CONFIG_FILES"
		echo ""
		echo "--------- OH default configuration ---------"
		echo "OH mode is set to $OH_MODE"
		echo "Language is set to $OH_LANGUAGE"
		echo "Demo data is set to $DEMO_DATA"
		echo ""
		echo "--- Database ---"
		echo "DATABASE_SERVER=$DATABASE_SERVER"
		echo "DATABASE_PORT=$DATABASE_PORT"
		echo "DATABASE_NAME=$DATABASE_NAME"
		echo "DATABASE_USER=$DATABASE_USER"
		echo ""
		echo "--- Imaging / Dicom ---"
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
		echo "PHOTO_DIR=$PHOTO_DIR"
		echo "BACKUP_DIR=$BACKUP_DIR"
		echo "LOG_DIR=$LOG_DIR"
		echo "SQL_DIR=$SQL_DIR"
		echo "TMP_DIR=$TMP_DIR"
		echo ""
		echo "---  Logging ---"
		echo "Log level is set to $LOG_LEVEL"
		echo "LOG_FILE=$LOG_FILE"
		echo "OH_LOG_FILE=$OH_LOG_FILE"
		echo ""
		
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	X)	# kill processes / clean installation
		echo ""
		# kill mariadb/mysqld processes
        	echo "Stopping Open Hospital..."
		echo "Warning: do you want to kill all java and mysql/mariadb processes?"
		read -p "Press [y] to confirm: " choice
		if [ "$choice" = "y" ]; then
			# kill mariadb/mysqld processes
			echo "Killing mariadb/mysql..."
			killall mariadbd
			echo "Killing java..."
			killall java
		fi
		########## WORKAROUND to kill existing API server process ##################
		########## TO BE REMOVED IN NEXT RELEASES
		##########
		# check for stale PID files
		if [ -f $OH_PATH/$TMP_DIR/$OH_API_PID ]; then
			API_PID_NUMBER=$(cat $OH_PATH/$TMP_DIR/$OH_API_PID)
			echo "Killing API server - process $API_PID_NUMBER..."
			kill $API_PID_NUMBER
			echo "Removing API server pid file $OH_API_PID..."
			rm -f $OH_PATH/$TMP_DIR/$OH_API_PID
		fi
		##########

		# cleaning files
        	echo "Cleaning Open Hospital installation..."
		echo "Warning: do you want to remove all existing log files?"
		read -p "Press [y] to confirm: " choice
		if [ "$choice" = "y" ]; then
			clean_log_files;
		fi
		echo "Warning: do you want to remove all existing configuration files?"
		read -p "Press [y] to confirm: " choice
		if [ "$choice" = "y" ]; then
			clean_conf_files;
		fi
		# remove local portable database
		if [ "$OH_MODE" != "CLIENT" ]; then
			echo "Warning: do you want to remove all existing data and databases?"
			read -p "Press [y] to confirm: " choice
			if [ "$choice" = "y" ]; then		
				echo "--->>> This operation cannot be undone"
				echo "--->>> Are you sure?"
				read -p "Press [y] to confirm: " choice
				if [ "$choice" = "y" ]; then		
					clean_database;
				fi
			fi
		fi
		echo "Warning: do you want to reset all existing configuration variables?"
		read -p "Press [y] to confirm: " choice
		if [ "$choice" = "y" ]; then
			# unset variables
			unset OH_MODE
			unset OH_LANGUAGE
			unset OH_SINGLE_USER
			unset LOG_LEVEL
			unset DEMO_DATA
			unset DB_CREATE_SQL
			unset EXPERT_MODE
			unset API_SERVER
			unset UI_INTERFACE
			echo ""
			echo "Warning: in order to reload database settings, exit script and relaunch."
			echo "Select [v] option from script menu to check current settings."
			echo ""
			# set variables to defaults
			set_defaults;
		fi
		echo "Done!"
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	q)	# quit
		echo "";
		echo "Quit pressed. Exiting.";
		exit 0
		;;
	###################################################
	Q)	# quit
		echo "";
		echo "Quit pressed. Exiting.";
		exit 0
		;;
	###################################################
	: )	# for -l option. If no lang argument is given, shows error
		echo "";
		echo "No language specified. See $SCRIPT_NAME -h for help"
		exit 3
		;;
	###################################################
	#"" )	# enter key
	#	option="Z"
	#	echo "";
	#	echo "Starting Open Hospital...";
	#	fi
	#	;;
	###################################################
	"V" )	# Check for latest OH version
		echo "";
		check_latest_oh_version;
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	"Z" )	# Z key
		option="Z";
		echo "";
		echo "Starting Open Hospital...";
		;;
	###################################################
	?)	# default
		echo ""
		if (( $2==0 )); then 
			echo "Invalid option: -${OPTARG}. See $SCRIPT_NAME -h for help"
			exit 0;
		else
			echo "Invalid option: ${option}. See $SCRIPT_NAME -h for help"
			echo "Press any key to continue";
			read;
		fi
		option="h";
		;;
	esac
}

######################## Script start ########################

######## Pre-flight checks

# check user running the script
if [ $(id -u) -eq 0 ]; then
	echo "Error - do not run this script as root. Exiting."
	exit 1
fi

######## Environment setup

set_path;
read_settings;
set_defaults;
set_db_name;

# set working dir to OH base dir
cd "$OH_PATH"

######## Parse user input

# reset in case getopts has been used previously in the shell
OPTIND=1 
# list of arguments expected in user input (- option)
# E is excluded from command line option
OPTSTRING=":AECPSdDGhil:msrtvequQXVZ?" 
COMMAND_LINE_ARGS=$@

# Parse arguments passed via command line / interactive input
if [[ ${#COMMAND_LINE_ARGS} -ne 0 ]]; then
	while getopts ${OPTSTRING} option; do
		parse_user_input $option 0; # non interactive
	done
else # If no arguments are passed via command line, show the interactive menu
	interactive_menu;
fi

#shift "$((OPTIND-1))"

######################## OH start ########################

echo ""

# check for demo mode
if [ "$DEMO_DATA" = "on" ]; then
	# exit if OH is configured in CLIENT mode
	if [[ "$OH_MODE" = "CLIENT" ]]; then
		echo "Error - OH_MODE set to $OH_MODE mode. Cannot run with Demo data. Exiting."
		exit 1;
	fi

	# set database name to demo
	DATABASE_NAME=$DEMO_DATABASE

	if [ -f ./$SQL_DIR/$DB_DEMO ]; then
		echo "Found SQL demo database, starting OH with Demo data..."
		DB_CREATE_SQL=$DB_DEMO
	else
		echo "Error: no $DB_DEMO found! Exiting."
		exit 1
	fi
	set_db_name;
fi

# display running configuration
echo "Write config files is set to $WRITE_CONFIG_FILES"
echo "Starting Open Hospital in $OH_MODE mode..."
echo "OH_PATH is set to $OH_PATH"

# display OH settings
echo "OH language is set to $OH_LANGUAGE";

# check for java
java_check;

# setup java lib
java_lib_setup;

# create directories
initialize_dir_structure;

######## Database setup

# start MariaDB/MySQL database server and create database
if [ "$OH_MODE" = "PORTABLE" ] || [ "$OH_MODE" = "SERVER" ] ; then
	# check for MariaDB/MySQL software
	mysql_check;
	# config database
	config_database;
	# check if OH database already exists
	if [ ! -d ./"$DATA_DIR" ]; then
		echo "OH database not found, starting from scratch..."
		# prepare database
		initialize_database;
		# start database
		start_database;	
		# set database root password
		set_database_root_pw;
		# create database user
		create_database_user;
		# create database 
		create_database;
		# load data
		import_database;
	else
	        echo "OH database found!"
		# start database
		start_database;
	fi
fi

######## OH startup

# test if database connection is working
test_database_connection;

# check for API server
if [ "$API_SERVER" = "on" ]; then
	start_api_server;
fi

# check for UI interface
if [ "$UI_INTERFACE" = "on" ]; then
	start_ui;
fi

# if SERVER mode is selected, wait for CTRL-C input to exit
if [ "$OH_MODE" = "SERVER" ]; then
	echo "Open Hospital - SERVER mode started"

	# show MariaDB/MySQL server running configuration
	echo "***************************************"
	echo "* Database server listening on:"
	echo ""
	cat ./$CONF_DIR/$MYSQL_CONF_FILE | grep bind-address
	cat ./$CONF_DIR/$MYSQL_CONF_FILE | grep port | head -1
	echo ""
	echo "***************************************"
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

	# generate config files if not existent
	write_config_files;

	# start OH gui
	start_gui;

	# Close and exit
	echo "Exiting Open Hospital..."
	shutdown_database;

	# go back to starting directory
	cd "$CURRENT_DIR"
fi

# Final exit
echo "Done!"
exit 0
