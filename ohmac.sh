#!/bin/bash
#
# Open Hospital (www.open-hospital.org)
# Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

#######################  OH configuration  #########################
# path and directories
TMP_DIR="tmp"
LOG_DIR="data/log"
DICOM_DIR="data/dicom_storage"
PHOTO_DIR="data/photo"
BACKUP_DIR="data/dump"

OH_DIR="oh"
OH_DOC_DIR="doc"
CONF_DIR="data/conf"
DATA_DIR="data/db"
SQL_DIR="sql"
SQL_EXTRA_DIR="sql/extra"
API_SERVER="off"
WRITE_CONFIG_FILES="on"

# OH jar bin files
OH_GUI_JAR="OH-gui.jar"
OH_API_JAR="openhospital-api-0.1.0.jar"
OH_API_WAR="openhospital-api-0.1.0.war"

# OH configuration files
OH_SETTINGS="settings.properties"
DATABASE_SETTINGS="database.properties"
EXAMINATION_SETTINGS="examination.properties"
IMAGING_SETTINGS="dicom.properties"
LOG4J_SETTINGS="log4j2-spring.properties"
PRINTER_SETTINGS="txtPrinter.properties"
SMS_SETTINGS="sms.properties"
TELEMETRY_SETTINGS="telemetry.properties"
XMPP_SETTINGS="xmpp.properties"
API_SETTINGS="application.properties"
CRED_SETTINGS="default_credentials.properties"
DEMO_CRED_SETTINGS="default_demo_credentials.properties"
HELP_FILE="OH-readme.txt"

# logging
LOG_FILE="startup.log"
OH_LOG_FILE="openhospital.log"
API_LOG_FILE="api.log"

# imaging / dicom
DICOM_MAX_SIZE="4M"
DICOM_STORAGE="FileSystemDicomManager" # SqlDicomManager
DICOM_DIR="data/dicom_storage"

# set escaped path (/ in place of \)
OH_PATH_ESCAPED=$(echo $OH_PATH | sed -e 's/\//\\\//g')
DICOM_DIR_ESCAPED=$(echo $DICOM_DIR | sed -e 's/\//\\\//g')
PHOTO_DIR_ESCAPED=$(echo $PHOTO_DIR | sed -e 's/\//\\\//g')
LOG_DIR_ESCAPED=$(echo $LOG_DIR | sed -e 's/\//\\\//g')
TMP_DIR_ESCAPED=$(echo $TMP_DIR | sed -e 's/\//\\\//g')

##################### Java configuration #######################
JAVA_URL="https://cdn.azul.com/zulu/bin"
JAVA_DISTRO="zulu17.60.17-ca-jre17.0.16-macosx_aarch64"
JAVA_DIR=$JAVA_DISTRO
JAVA_ARCH="arm64"
EXT="tar.gz"

##################### Database configuration #######################
DATABASE_SERVER="localhost"
DATABASE_PORT="3306"
DATABASE_ROOT_PW="tmp2021oh111"
DATABASE_NAME="oh"
DATABASE_USER="isf"
DATABASE_PASSWORD="isf123"
DB_CREATE_SQL="create_all_en.sql"

# seconds to wait for the database to start listening, or to release its port on shutdown
DATABASE_WAIT_TIMEOUT=90

# demo data - the demo branch below uses both of these
DEMO_DATABASE="ohdemo"
DB_DEMO="create_all_demo.sql"

# OH API server and web interface - same names and values as oh.sh
OH_API_PROD="oh-api"
OH_API_HOST="localhost"
OH_API_PORT="8080"

OH_UI_HOST="localhost"
OH_UI_PORT="8080"
OH_UI_PROD="oh-ui"
# no /$OH_UI_PROD here: the bundled artifact is started directly and its template sets
# server.servlet.context-path=/, where oh.sh deploys a Tomcat webapp of that name
OH_UI_URL="http://$OH_UI_HOST:$OH_UI_PORT"

# empty as in oh.sh, where the assignment is commented out
OH_API_PID=""

# activate expert mode - set to "on" to enable advanced functions - use at your own risk!
EXPERT_MODE="off"

OH_LANGUAGE_LIST="en|fr|es|it|pt|ar"
OH_LANGUAGE="en" # default
OH_MODE="PORTABLE"
LOG_LEVEL="INFO"
DEMO_DATA="off"
USER=`whoami`;


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
	echo "   -l    [ $OH_LANGUAGE_LIST ] -> set language"
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
	# only the options this script implements; oh.sh lists more
	echo "   -------------------------------- "
	echo "    EXPERT MODE - advanced options"
	echo ""
	echo "   -A  toggle API server - EXPERIMENTAL		| -d  toggle log level INFO/DEBUG"
	echo "   -i  initialize/install OH database		| -G  setup GSM"
	echo "   -U  enable UI web interface"
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
function check_latest_oh_version {
	echo "Checking online for Open Hospital latest version..."
	LATEST_OH_VERSION=$(curl -s -L https://api.github.com/repos/informatici/openhospital/releases/latest | grep tag_name  | cut -b16-22)
	echo "Latest OH version is" $LATEST_OH_VERSION
	echo ""
}

###################################################################
function set_oh_mode {
	# if $OH_SETTINGS is present set OH mode
	if [ -f ./$OH_DIR/rsc/$OH_SETTINGS ]; then
		echo "Configuring OH mode..."
		######## $OH_SETTINGS OH mode configuration
		echo "Setting OH mode to $OH_MODE in OH configuration file -> $OH_SETTINGS..."        
        sed -e "s/^MODE=.*/MODE=$OH_MODE/" -i '' ./$OH_DIR/rsc/$OH_SETTINGS
		#sed -e "/^"MODE="/c\"MODE=$OH_MODE"" -i ./$OH_DIR/rsc/$OH_SETTINGS
	else 
		echo ""
		echo ""
		echo "Warning: $OH_SETTINGS file not found."
	fi
	echo "OH mode set to $OH_MODE"
}

###################################################################
function set_log_level {
	if [ -f ./$OH_DIR/rsc/$LOG4J_SETTINGS ]; then
		echo ""        
		######## $LOG4J_SETTINGS log_level configuration
		echo "Setting log level to $LOG_LEVEL in OH configuration file -> ./$OH_DIR/rsc/$LOG4J_SETTINGS..."
		case "$LOG_LEVEL" in
			*INFO*)
				sed -e "s/DEBUG/$LOG_LEVEL/g" -i '' "./$OH_DIR/rsc/$LOG4J_SETTINGS"
			;;
			*DEBUG*)
				sed -e "s/INFO/$LOG_LEVEL/g" -i '' "./$OH_DIR/rsc/$LOG4J_SETTINGS" 
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
function java_check {
# check if JAVA_BIN is already set and it exists
echo ""
echo "is java installed?"
# Point at the bundled JRE unless a usable one was already given. The download below puts the JRE
# exactly at this path, so it has to be set before that too: leaving the variable empty would make
# the launch command start with its first argument instead of the java binary.
if [ -z "${JAVA_BIN:-}" ] || [ ! -x "$JAVA_BIN" ]; then
	JAVA_BIN="./$OH_DIR/$JAVA_DIR/bin/java"
fi

# if JAVA_BIN is not found download JRE
if [ ! -x "$JAVA_BIN" ]; then
	echo ">no"
	if [ ! -f "./$JAVA_DISTRO.$EXT" ]; then
		echo "  Warning - JAVA not found. Do you want to download it?"
		get_confirmation;
		# download java binaries
		echo "  Download $JAVA_DISTRO..."
        echo curl -o ./$OH_DIR/$JAVA_DISTRO.$EXT $JAVA_URL/$JAVA_DISTRO.$EXT		

		curl -o ./$OH_DIR/$JAVA_DISTRO.$EXT $JAVA_URL/$JAVA_DISTRO.$EXT		
	fi
	echo "  Unpacking $JAVA_DISTRO..."
	tar xf ./$OH_DIR/$JAVA_DISTRO.$EXT -C ./$OH_DIR
	if [ $? -ne 0 ]; then
		echo "  Error unpacking Java. Exiting."
		exit 1
	fi
	echo "  JAVA unpacked successfully!"
	echo "  Removing downloaded file..."
	rm ./$OH_DIR/$JAVA_DISTRO.$EXT
	echo "  Done!"
fi

if [ ! -x "$JAVA_BIN" ]; then
	echo "Error: no usable Java found at $JAVA_BIN. Exiting."
	exit 1
fi

echo ">Using $JAVA_BIN"
}
###################################################################
function java_lib_setup {
	# NATIVE LIB setup
	case $JAVA_ARCH in
        arm64)
        NATIVE_LIB_PATH="./$OH_DIR/lib/native/macOS/arm64"
        ;;
	esac

	# macOS tags everything extracted from a downloaded archive with a quarantine
	# flag. The bundled OpenCV native is only ad-hoc signed, so Gatekeeper refuses
	# to load it and warns the user that it may contain malware. Clear the flag on
	# the native libraries, before the JVM tries to load them: once a load has been
	# blocked, clearing it afterwards no longer helps.
	# Nothing happens when the flag is absent, e.g. when running from a checkout.
	if [ -d "$NATIVE_LIB_PATH" ] && ls "$NATIVE_LIB_PATH"/*.dylib >/dev/null 2>&1 &&
		xattr "$NATIVE_LIB_PATH"/*.dylib 2>/dev/null | grep -q "com.apple.quarantine"; then
		echo "Removing the macOS quarantine flag from the bundled native libraries..."
		xattr -dr com.apple.quarantine "$NATIVE_LIB_PATH"
	fi

	# CLASSPATH setup
	# include OH jar file
	OH_CLASSPATH=$OH_DIR/bin/$OH_GUI_JAR
	
	# include all needed directories
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/bundle
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rpt_base
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rpt_extra
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rpt_stat
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rsc
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rsc/images
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/lib

	# include all jar files under lib/
	DIRLIBS=./$OH_DIR/lib/*.jar
	for i in ${DIRLIBS}
	do
		OH_CLASSPATH="$i":$OH_CLASSPATH
	done
}

###################################################################
function write_api_config_file {
	######## application.properties setup - OH API server
	SET_FILE=./$OH_DIR/rsc/$API_SETTINGS
	# the API templates only ship in the package that bundles the API server
	if [ ! -f "$SET_FILE.dist" ]; then
		echo "Warning: $API_SETTINGS.dist not found, this package does not include the API server."
		return
	fi
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f "$SET_FILE" ]; then
		[ -f "$SET_FILE" ] && mv -f "$SET_FILE" "$SET_FILE.old"
		# generate OH API token and save to settings file
		JWT_TOKEN_SECRET=`LC_ALL=C tr -dc A-Za-z0-9 </dev/urandom | head -c 66`

		echo ">Writing OH API configuration file -> $API_SETTINGS..."
		# read the template and write the copy: editing it in place would consume the placeholders
		# in the shipped file, leaving the next run nothing to replace
		sed -e "s/JWT_TOKEN_SECRET/$JWT_TOKEN_SECRET/g" \
			-e "s&OH_API_PID&$OH_API_PID&g" \
			-e "s&UI_HOST&$OH_UI_HOST&g" \
			-e "s&UI_PORT&$OH_UI_PORT&g" \
			-e "s&API_HOST&$OH_API_HOST&g" \
			-e "s&API_PORT&$OH_API_PORT&g" \
			-e "s&API_URL&$OH_API_PROD&g" \
			"$SET_FILE.dist" > "$SET_FILE"
	fi
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
function set_language {
	# check for valid language selection
	case "$OH_LANGUAGE" in 
		en|fr|it|es|pt|ar) # TBD: language array direct check
			# set localized database creation script
			DB_CREATE_SQL="create_all_$OH_LANGUAGE.sql"
			;;
		*)
			echo "Invalid language option: $OH_LANGUAGE. Exiting."
			exit 1
		;;
	esac

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
function install_brew {
	if command -v brew &> /dev/null; then
		echo "Homebrew is already installed."
	else
		# If brew is not installed, proceed with the installation
		echo "Homebrew is not installed. Do you want to download it?"
		get_confirmation;
		/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
		
		# Check if the installation was successful
		if [ $? -eq 0 ]; then
			echo "Homebrew has been installed successfully."
		else
			echo "An error occurred during Homebrew installation."
		fi
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
function install_db {
    install_brew;
    if brew list --formula | grep -q mariadb; then
        echo "MariaDB is already installed."
    else
        echo "MariaDB is not installed. Do you want to download it?"
        get_confirmation;
        brew install mariadb;
        brew services start mariadb >/dev/null;
        create_db;
    fi
    return
}

###################################################################
function find_database_client {
	# The macOS package does not bundle a database client, so there may well be
	# none available: callers must cope with an empty result.
	if command -v mysql >/dev/null 2>&1; then
		DATABASE_CLIENT="mysql"
	elif command -v mariadb >/dev/null 2>&1; then
		DATABASE_CLIENT="mariadb"
	else
		DATABASE_CLIENT=""
	fi
}

###################################################################
function test_db_connection {
	# test if a database client is available
	find_database_client;
	if [ -z "$DATABASE_CLIENT" ]; then
		# Without a client the check cannot run. That is not a reason to refuse
		# to start: the application connects through JDBC, not through this tool.
		echo "Can't test database connection: no MySQL/MariaDB client found."
		return
	fi

	# The caller says which question to ask. Before the database is installed only the server can be
	# reached, so asking for the [$DATABASE_NAME] database there would refuse to start the very
	# installation that is about to create it.
	if [ "$1" = "server" ]; then
		STATEMENT="SELECT 1"
	else
		STATEMENT="USE $DATABASE_NAME"
	fi

	echo "Testing database connection..."
	if $DATABASE_CLIENT --user="$DATABASE_USER" --password="$DATABASE_PASSWORD" \
		--host="$DATABASE_SERVER" --port="$DATABASE_PORT" --protocol=tcp \
		-e "$STATEMENT" >/dev/null 2>&1; then
		echo ">Database connection successfully established!"
	else
		echo "!Error: can't connect to database! Exiting."
		exit 2
	fi
}

###################################################################
function import_db {
    start_db;
	
    SCRIPTDIR="./$SQL_DIR"
    SCRIPT="$DB_CREATE_SQL"

	
	TABLE_NAME="OH_VERSION"		
	echo mysql -u $USER -h$DATABASE_SERVER -p$DATABASE_ROOT_PW -e "DESCRIBE $DATABASE_NAME.$TABLE_NAME;"
	if mysql -u $USER -h$DATABASE_SERVER -p$DATABASE_ROOT_PW -e "DESCRIBE $DATABASE_NAME.$TABLE_NAME;"; then	
		echo " >db already imported"
	else
		echo " >Checking for SQL creation script... $SCRIPT in $SCRIPTDIR"
		# check for database creation script
		if [ -f "$SCRIPTDIR/$SCRIPT" ]>/dev/null; then
			echo "  >Using SQL file $SCRIPT..."
		else
			echo "  >Error: No SQL file found! Exiting."
			stop_db;
			exit 2
		fi

		# create OH database structure
		echo "  >Importing database [$DATABASE_NAME] with user [$DATABASE_USER@$DATABASE_SERVER]..."    

		CURRPATH=`pwd`
		cd "$SCRIPTDIR"
		# --abort-source-on-error: the client otherwise carries on after a failed statement inside a sourced file and still exits 0
		mysql --abort-source-on-error --local-infile=1 -u $USER $DATABASE_NAME < $SCRIPT
		if [ $? -ne 0 ]; then
			echo "  >Error: Database not imported! Exiting."
			stop_db;
			cd "$CURRENT_DIR"
			exit 2
		fi
		
		cd "$CURRPATH"
		echo " >db successfully imported"
	fi
}

###################################################################
function create_db {
    echo "is needed to create db?" 

    if brew list | grep -q mariadb >/dev/null; then
        echo " >mariadb running"			
    else
        install_db;     
        brew services start mariadb >/dev/null;
    fi
    
    CMD="USE $DATABASE_NAME"
	echo " (connection test: mysql -u $USER -h$DATABASE_SERVER -p$DATABASE_ROOT_PW -e "$CMD")"
    if mysql -u $USER -h$DATABASE_SERVER -p$DATABASE_ROOT_PW -e "$CMD">/dev/null 2>&1; then    
        echo " >db connection ok"
    else 
		echo " >START DB"                
		echo " >Creating database [$DATABASE_NAME]..."   
		mysql -u $USER -e "CREATE USER '$DATABASE_USER'@'$DATABASE_SERVER' IDENTIFIED BY '$DATABASE_PASSWORD'; GRANT ALL PRIVILEGES ON *.* TO '$DATABASE_USER'@'$DATABASE_SERVER' WITH GRANT OPTION; FLUSH PRIVILEGES;"        
		mysql -u $USER -e "ALTER USER '$DATABASE_USER'@'$DATABASE_SERVER' IDENTIFIED BY '$DATABASE_ROOT_PW';"    
		mysql -u $USER -e "CREATE DATABASE $DATABASE_NAME CHARACTER SET utf8; GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$DATABASE_USER'@'$DATABASE_SERVER';"
		mysql -u $USER -e "ALTER USER '$DATABASE_USER'@'$DATABASE_SERVER' IDENTIFIED BY '$DATABASE_PASSWORD'; GRANT ALL PRIVILEGES ON *.* TO '$DATABASE_USER'@'$DATABASE_SERVER' WITH GRANT OPTION; FLUSH PRIVILEGES;"        

        if [ $? -ne 0 ]; then
            echo "Error: Database user creation failed! Exiting."
            #stop_db;
            exit 2
        fi
    fi	
}
###################################################################
# Probed with bash's own /dev/tcp rather than `nc`, which a stock macOS does not have.
function database_port_open {
	(exec 3<>/dev/tcp/$DATABASE_SERVER/$DATABASE_PORT) > /dev/null 2>&1
}

###################################################################
# `brew services start` returns when launchd accepts the job, not when the server is ready, and
# the next thing this script runs is a mysql client - on a cold start that client was refused.
function wait_for_database {
	WAITED=0
	until database_port_open; do
		if [ $WAITED -ge $DATABASE_WAIT_TIMEOUT ]; then
			echo "Error: MariaDB is not listening on $DATABASE_SERVER:$DATABASE_PORT after $DATABASE_WAIT_TIMEOUT seconds."
			echo "Check it with 'brew services info mariadb'. Exiting."
			exit 2
		fi
		if [ $WAITED -gt 0 ] && [ $((WAITED % 10)) -eq 0 ]; then
			echo "Still waiting for MariaDB to listen on $DATABASE_SERVER:$DATABASE_PORT ($WAITED s)..."
		fi
		sleep 1
		WAITED=$((WAITED+1))
	done
}

###################################################################
function wait_for_database_stopped {
	WAITED=0
	while database_port_open; do
		if [ $WAITED -ge $DATABASE_WAIT_TIMEOUT ]; then
			echo "Warning: MariaDB is still listening on $DATABASE_SERVER:$DATABASE_PORT after $DATABASE_WAIT_TIMEOUT seconds."
			echo "Check it with 'brew services info mariadb'."
			return
		fi
		if [ $WAITED -gt 0 ] && [ $((WAITED % 10)) -eq 0 ]; then
			echo "Still waiting for MariaDB to release $DATABASE_SERVER:$DATABASE_PORT ($WAITED s)..."
		fi
		sleep 1
		WAITED=$((WAITED+1))
	done
}

###################################################################
function start_db {
    create_db;
    brew services start mariadb >/dev/null;
    wait_for_database;
}
###################################################################
function stop_db {
    brew services stop mariadb
    wait_for_database_stopped;
}

###################################################################
function remove_db {
    stop_db;
    brew uninstall mariadb 
    rm -rf /opt/homebrew/var/aria_*
    rm -rf /opt/homebrew/var/cache
    rm -rf /opt/homebrew/var/ib*
    rm -rf /opt/homebrew/var/sys
    rm -rf /opt/homebrew/var/mysql
    rm -rf /opt/homebrew/var/undo*
    rm -rf /opt/homebrew/var/maria*
}

###################################################################
function copy_config_file {
	# create a configuration file from its template if it is not there yet
	# usage: copy_config_file [file_name]
	#
	# Only when the file is missing, never over an existing one. Unlike settings.properties and
	# database.properties below, nothing in these files is derived from the launcher, so rewriting
	# them on every run would achieve nothing while silently discarding what the site configured -
	# vital sign ranges, SMS gateway credentials. Nor would a backup copy save it: WRITE_CONFIG_FILES
	# is fixed to "on" in this script with no way to turn it off, so the second run would overwrite
	# the copy the first run had just made.
	if [ ! -f ./$OH_DIR/rsc/$1 ]; then
		echo ">Writing OH configuration file -> $1..."
		cp ./$OH_DIR/rsc/$1.dist ./$OH_DIR/rsc/$1
	fi
}

###################################################################
function write_config_files {
	# set up configuration files
	echo "Checking for OH configuration files..."
	######## DICOM setup
	IMAGING_FILE=./$OH_DIR/rsc/$IMAGING_SETTINGS
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f $IMAGING_FILE ]; then
		[ -f $IMAGING_FILE ] && mv -f $IMAGING_FILE $IMAGING_FILE.old
		echo ">Writing OH configuration file -> $IMAGING_SETTINGS..."
		cp $IMAGING_FILE.dist $IMAGING_FILE
		sed -i '' -e "s/DICOM_SIZE/$DICOM_MAX_SIZE/g" -e "s/OH_PATH_SUBSTITUTE/$OH_PATH_ESCAPED/g" \
		-e "s/DICOM_STORAGE/$DICOM_STORAGE/g" -e "s/DICOM_DIR/$DICOM_DIR_ESCAPED/g" $IMAGING_FILE		
	fi
	######## $LOG4J_SETTINGS setup
	LOG4J_FILE=./$OH_DIR/rsc/$LOG4J_SETTINGS
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f $LOG4J_FILE ]; then
		OH_LOG_DEST="./$LOG_DIR/$OH_LOG_FILE"
		[ -f $LOG4J_FILE ] && mv -f $LOG4J_FILE $LOG4J_FILE.old
		echo ">Writing OH configuration file -> $LOG4J_SETTINGS..."
		cp $LOG4J_FILE.dist $LOG4J_FILE
		sed -i '' -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/" -e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		-e "s/DBNAME/$DATABASE_NAME/g" -e "s/LOG_LEVEL/$LOG_LEVEL/g" -e "s+LOG_DEST+$OH_LOG_DEST+g" \
		$LOG4J_FILE		
	fi
	######## $DATABASE_SETTINGS setup
	# written only when absent, unlike the files around it: where the installation keeps its data is
	# the installer's decision, and rewriting it on every run tied macOS to isf@localhost
	DB_FILE=./$OH_DIR/rsc/$DATABASE_SETTINGS
	if [ ! -f $DB_FILE ]; then
		echo ">Writing OH database configuration file -> $DATABASE_SETTINGS..."
		cp $DB_FILE.dist $DB_FILE
		sed -i '' -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/g" -e "s/DBNAME/$DATABASE_NAME/g" \
		-e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		$DB_FILE	
	fi
	######## $OH_SETTINGS setup
	SETTINGS_FILE=./$OH_DIR/rsc/$OH_SETTINGS
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f $SETTINGS_FILE ]; then
		[ -f  $SETTINGS_FILE ] && mv -f $SETTINGS_FILE $SETTINGS_FILE.old
		echo ">Writing OH configuration file -> $OH_SETTINGS..."
		cp $SETTINGS_FILE.dist $SETTINGS_FILE
		# persisted so that -U survives a restart; anchored: UI_INTERFACE is a substring of GUI_INTERFACE
		sed -i '' -e "s/OH_MODE/$OH_MODE/g" -e "s/OH_LANGUAGE/$OH_LANGUAGE/g" -e "s&OH_DOC_DIR&../$OH_DOC_DIR&g" \
		-e "s/DEMODATA=off/"DEMODATA=$DEMO_DATA"/g" -e "s/YES_OR_NO/$OH_SINGLE_USER/g" \
		-e "s/PHOTO_DIR/$PHOTO_DIR_ESCAPED/g" \
		-e "s/^APISERVER=off/APISERVER=$API_SERVER/" \
		-e "s/^GUI_INTERFACE=on/GUI_INTERFACE=$GUI_INTERFACE/" \
		-e "s/^UI_INTERFACE=off/UI_INTERFACE=$UI_INTERFACE/" \
		$SETTINGS_FILE
	fi
	######## OH - other settings, copied as they are
	# sms.properties and telemetry.properties are declared as @PropertySource in the core, so a
	# missing file stops the application from starting instead of disabling a feature. The other
	# three are read through the properties bundle and fall back to defaults, but they are written
	# here as well so that a site finds the whole set in place, as it does on the other platforms.
	copy_config_file $EXAMINATION_SETTINGS;
	copy_config_file $PRINTER_SETTINGS;
	copy_config_file $SMS_SETTINGS;
	copy_config_file $TELEMETRY_SETTINGS;
	copy_config_file $XMPP_SETTINGS;

	######## default credentials
	if [ "$OH_MODE" == "PORTABLE" ]; then
		copy_config_file $CRED_SETTINGS;
	fi
	if [ "$DEMO_DATA" = "on" ]; then
		cp ./$OH_DIR/rsc/$DEMO_CRED_SETTINGS.dist ./$OH_DIR/rsc/$CRED_SETTINGS
	fi
}

function read_settings {
    CURRENT_DIR=$PWD
	echo "./$OH_DIR/rsc/version.properties"
	# check and read OH version file
	if [ -f ./$OH_DIR/rsc/version.properties ]; then
		source "./$OH_DIR/rsc/version.properties"
		OH_VERSION=$VER_MAJOR.$VER_MINOR.$VER_RELEASE
	else
		echo "Error: Open Hospital non found! Exiting."
		exit 1;
	fi

	# check for OH settings file and read values, as oh.sh does. Without this every run starts from
	# the defaults at the top of this script, so the choices the user persisted in the settings file
	# - the API server above all - are silently dropped on the next launch.
	if [ -f ./$OH_DIR/rsc/$OH_SETTINGS ]; then
		echo "Reading OH settings file..."
		. ./$OH_DIR/rsc/$OH_SETTINGS

		OH_MODE=$MODE
		OH_LANGUAGE=$LANGUAGE
		OH_SINGLE_USER=$SINGLE_USER
		OH_DOC_DIR=$OH_DOC_DIR
		DEMO_DATA=$DEMODATA
		API_SERVER=$APISERVER
		GUI_INTERFACE=$GUI_INTERFACE
		UI_INTERFACE=$UI_INTERFACE
	fi

	# check for database settings file and read values, as oh.sh does. Without this the connection
	# details below stay at their defaults whatever the installation was configured with, so the
	# database test and the generated files all talk about a server nobody asked for.
	if [ -f ./$OH_DIR/rsc/$DATABASE_SETTINGS ]; then
		echo "Reading database settings file..."
		DATABASE_SERVER=$(cat ./$OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.url" | cut -d"/" -f3 | cut -d":" -f1)
		DATABASE_PORT=$(cat ./$OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.url" | cut -d"/" -f3 | cut -d":" -f2)
		DATABASE_NAME=$(cat ./$OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.url" | cut -d"/" -f4)
		DATABASE_USER=$(cat ./$OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.username" | cut -d"=" -f2)
		DATABASE_PASSWORD=$(cat ./$OH_DIR/rsc/$DATABASE_SETTINGS | grep "jdbc.password" | cut -d"=" -f2)
	else
		echo "Warning: configuration file $DATABASE_SETTINGS not found."
	fi

    ARCH=`uname -m`
	}

###################################################################
function start_api_server {
	# The packages that carry the API ship a self-contained Spring Boot artifact, not the Tomcat
	# layout oh.sh starts, and the client package carries none at all. Where it cannot be started
	# this says so and returns: until now the call failed with "command not found" and the run
	# carried on, so refusing to start Open Hospital at all would take away something that works.
	API_ARTIFACT=""
	for candidate in "./$OH_DIR/bin/$OH_API_JAR" "./$OH_DIR/bin/$OH_API_WAR"; do
		[ -f "$candidate" ] && API_ARTIFACT="$candidate" && break
	done
	if [ -z "$API_ARTIFACT" ]; then
		echo "Warning: no API server found in ./$OH_DIR/bin, this package does not include it."
		return
	fi
	if [ ! -f ./$OH_DIR/rsc/$API_SETTINGS ]; then
		echo "Warning: missing $API_SETTINGS settings file, the API server will not be started."
		return
	fi

	echo "------------------------"
	echo "---- EXPERIMENTAL ------"
	echo "------------------------"
	echo "Starting API server..."
	echo "Please wait, it might take some time..."
	echo ""
	echo "Connect to $OH_UI_URL for OH web interface"
	echo ""

	case "$API_ARTIFACT" in
		*.war) LAUNCHER="org.springframework.boot.loader.launch.WarLauncher" ;;
		*)     LAUNCHER="org.springframework.boot.loader.launch.JarLauncher" ;;
	esac
	$JAVA_BIN -client -Xms64m -Xmx1024m \
		-cp "$API_ARTIFACT:./$OH_DIR/rsc:./$OH_DIR/static" $LAUNCHER >> ./$LOG_DIR/$API_LOG_FILE 2>&1 &

	if [ $? -ne 0 ]; then
		echo "An error occurred while starting the Open Hospital API server. Exiting."
		stop_db;
		cd "$CURRENT_DIR"
		exit 4
	fi
}

###################################################################
function start_ui {
	echo "Starting Open Hospital UI at $OH_UI_URL..."
	# OH UI launch - `open` is the macOS equivalent of the xdg-open oh.sh uses
	open "$OH_UI_URL" || echo "Could not open a web browser, please go to $OH_UI_URL yourself."
}

###################################################################
function start_gui {
	echo "Starting Open Hospital GUI..."
	# OH GUI launch	
	
	$JAVA_BIN -client --add-opens java.desktop/javax.imageio.stream=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED -Xms64m -Xmx1024m -Dsun.java2d.dpiaware=false -Djava.library.path=${NATIVE_LIB_PATH} -classpath $OH_CLASSPATH org.isf.Application >> ./$LOG_DIR/$LOG_FILE 2>&1

	if [ $? -ne 0 ]; then
		echo "An error occurred while starting Open Hospital. Exiting."
		stop_db;
		cd "$CURRENT_DIR"
		exit 4
	fi
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
		echo ""
		write_api_config_file;
		if (( $2==0 )); then API_SERVER="on"; fi
		echo "Press any key to continue"; 
		read;
		;;
	###################################################
	U)	# toggle UI Interface
		case "$UI_INTERFACE" in
			*on*)
				UI_INTERFACE="off";
				GUI_INTERFACE="on";
			;;
			*off*)
				UI_INTERFACE="on";
				GUI_INTERFACE="off";
			;;
		esac
		#
		if (( $2==0 )); then UI_INTERFACE="on"; interactive_menu; fi
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
		# create config files if not present
		#write_config_files;
		set_log_level;
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	#D)	# demo mode
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
		set_language;
		install_db;
		echo "Do you want to create the [$DATABASE_USER] user and [$DATABASE_NAME] database on [$DATABASE_SERVER] server?"
		read -p "Press [y] to confirm: " choice
		if [ "$choice" = "y" ]; then
			# ask user for root database password
			read -p "Please insert the MariaDB / MySQL database root password [$DATABASE_ROOT_USER@$DATABASE_SERVER] -> " -s DATABASE_ROOT_PW
			echo ""				
			create_db;
		fi
		# ask user for database password
		read -p "Please insert the MariaDB / MySQL database password for user [$DATABASE_USER@$DATABASE_SERVER] -> " -s DATABASE_PASSWORD
		echo ""
		echo "Do you want to install the [$DATABASE_NAME] database on [$DATABASE_SERVER]?"
		get_confirmation 1;
		test_db_connection server;
		import_db;
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
			read -n 2 -p "Please select language [$OH_LANGUAGE_LIST]: " OH_LANGUAGE
		fi
		set_language;
		if (( $2==0 )); then option="Z"; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	#m)	# configure OH database connection manually
	#e)	# export/save database
	#r)	# restore database
	#s)	# save / write config files
	#t)	# test database connection
	#u)	# create Desktop shortcut
	#v)	# display software version and configuration
	#X)	# kill processes / clean installation
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
	#: )	# for -l option. If no lang argument is given, shows error
	###################################################
	V )	# Check for latest OH version
		echo "";
		check_latest_oh_version;
		if (( $2==0 )); then exit 0; else echo "Press any key to continue"; read; fi
		;;
	###################################################
	"Z" )	# Z key
	#?)	# default
		option="Z";
		echo "";
		echo "Starting Open Hospital...";
		;;
	esac
}


###################################################################
function set_defaults {
	# Fill in the values read_settings did not find, as oh.sh does. The settings file is not
	# required to carry every key, and an absent key leaves its variable empty rather than at the
	# default set at the top of this script, so the defaults have to be reapplied afterwards.

	# EXPERT_MODE features - set default to off
	if [ -z "$EXPERT_MODE" ]; then
		EXPERT_MODE="off"
	fi

	# API server - set default to off
	if [ -z "$API_SERVER" ]; then
		API_SERVER="off"
	fi

	# GUI interface - set default to on
	if [ -z "$GUI_INTERFACE" ]; then
		GUI_INTERFACE="on"
	fi

	# UI interface - set default to off
	if [ -z "$UI_INTERFACE" ]; then
		UI_INTERFACE="off"
	fi
}

read_settings;
set_defaults;

#remove_db;
#import_db;

# reset in case getopts has been used previously in the shell
OPTIND=1 
# list of arguments expected in user input (- option)
# E is excluded from command line option
OPTSTRING=":AECPSdDGhil:msrtvequQXUVZ?"
COMMAND_LINE_ARGS=$@

# Parse arguments passed via command line / interactive input
if [[ ${#COMMAND_LINE_ARGS} -ne 0 ]]; then
	while getopts ${OPTSTRING} option; do
		parse_user_input $option 0; # non interactive
	done
else # If no arguments are passed via command line, show the interactive menu
	interactive_menu;
fi


function demo_mode(){
	echo "is a demo mode?"
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
	else
		echo ">no"
	fi
}
######################## OH start ########################
echo ""
demo_mode;

# display running configuration
echo ""
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
	import_db;
fi

test_db_connection;

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
			stop_db;		
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
	stop_db;

	# go back to starting directory
	cd "$CURRENT_DIR"
fi