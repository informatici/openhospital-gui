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

# OH configuration files
OH_SETTINGS="settings.properties"
DATABASE_SETTINGS="database.properties"
IMAGING_SETTINGS="dicom.properties"
LOG4J_SETTINGS="log4j.properties"
API_SETTINGS="application.properties"
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
JAVA_DISTRO="zulu11.64.19-ca-jre11.0.19-macosx_aarch64"
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
if [ -e "./$OH_DIR/$JAVA_DIR/bin/java" ]; then
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

echo ">Using $JAVA_BIN"
}
###################################################################
function java_lib_setup {
	# NATIVE LIB setup
	case $JAVA_ARCH in
        arm64)
        NATIVE_LIB_PATH="./$OH_DIR/lib/native/Linux/arm64"
        ;;
		64)
		NATIVE_LIB_PATH="./$OH_DIR/lib/native/Linux/amd64"
		;;
		32)
		NATIVE_LIB_PATH="./$OH_DIR/lib/native/Linux/i386"
		;;
	esac

	# CLASSPATH setup
	# include OH jar file
	OH_CLASSPATH=$OH_DIR/bin/$OH_GUI_JAR
	
	# include all needed directories
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/bundle
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rpt_base
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rpt_extra
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rpt_stat
	OH_CLASSPATH=$OH_CLASSPATH:$OH_DIR/rsc
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
	SET_FILE=$OHDIR/rsc/$API_SETTINGS
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f SET_FILE ]; then
		[ -f $SET_FILE ] && mv -f $SET_FILE $SET_FILE.old		
		# generate OH API token and save to settings file		
		JWT_TOKEN_SECRET=`LC_ALL=C tr -dc A-Za-z0-9 </dev/urandom | head -c 66`

		echo "Writing OH API configuration file -> $API_SETTINGS..."
		sed -i '' -e "s/JWT_TOKEN_SECRET/"$JWT_TOKEN_SECRET"/g" -e "s&OH_API_PID&"$OH_API_PID"&g" $SET_FILE.dist
		cp -f $SET_FILE.dist $SET_FILE	
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
function test_db_connection {
    # test if mysql client is available			
    echo "Testing database connection..."	
	

	CMD="exit"		
    if mysql -u $USER -h$DATABASE_SERVER -p$DATABASE_ROOT_PW -e "$CMD">/dev/null 2>&1; then
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
		mysql --local-infile=1 -u $USER $DATABASE_NAME < $SCRIPT
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
function start_db {    
    create_db;
    brew services start mariadb >/dev/null;
}
###################################################################
function stop_db {
    brew services stop mariadb    
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
		OH_LOG_DEST="./$OH_DIR/$LOG_DIR/$OH_LOG_FILE"
		[ -f $LOG4J_FILE ] && mv -f $LOG4J_FILE $LOG4J_FILE.old
		echo ">Writing OH configuration file -> $LOG4J_SETTINGS..."
		cp $LOG4J_FILE.dist $LOG4J_FILE
		sed -i '' -e "s/DBSERVER/$DATABASE_SERVER/g" -e "s/DBPORT/$DATABASE_PORT/" -e "s/DBUSER/$DATABASE_USER/g" -e "s/DBPASS/$DATABASE_PASSWORD/g" \
		-e "s/DBNAME/$DATABASE_NAME/g" -e "s/LOG_LEVEL/$LOG_LEVEL/g" -e "s+LOG_DEST+$OH_LOG_DEST+g" \
		$LOG4J_FILE		
	fi
	######## $DATABASE_SETTINGS setup 
	DB_FILE=./$OH_DIR/rsc/$DATABASE_SETTINGS
	if [ "$WRITE_CONFIG_FILES" = "on" ] || [ ! -f $DB_FILE ]; then
		[ -f $DB_FILE ] && mv -f $DB_FILE $DB_FILE.old
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
		sed -i '' -e "s/OH_MODE/$OH_MODE/g" -e "s/OH_LANGUAGE/$OH_LANGUAGE/g" -e "s&OH_DOC_DIR&../$OH_DOC_DIR&g" \
		-e "s/DEMODATA=off/"DEMODATA=$DEMO_DATA"/g" -e "s/YES_OR_NO/$OH_SINGLE_USER/g" \
		-e "s/PHOTO_DIR/$PHOTO_DIR_ESCAPED/g" -e "s/APISERVER=off/"APISERVER=$API_SERVER"/g" \
		$SETTINGS_FILE	
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

    ARCH=`uname -m`
	}

###################################################################
function start_gui {
	echo "Starting Open Hospital GUI..."
	# OH GUI launch	
	
	$JAVA_BIN -client -Xms64m -Xmx1024m -Dsun.java2d.dpiaware=false -Djava.library.path=${NATIVE_LIB_PATH} -classpath $OH_CLASSPATH org.isf.menu.gui.Menu >> $OH_DIR/$LOG_DIR/$LOG_FILE 2>&1

	if [ $? -ne 0 ]; then
		echo "An error occurred while starting Open Hospital. Exiting."
		shutdown_database;
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
	#E)	# toggle EXPERT_MODE features
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
		test_db_connection;
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


read_settings;
 
#remove_db;
#import_db;

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
		set_db_name;
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