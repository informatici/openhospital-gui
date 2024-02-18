@echo off
REM # Open Hospital (www.open-hospital.org)
REM # Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
REM #
REM # Open Hospital is a free and open source software for healthcare data management.
REM #
REM # This program is free software: you can redistribute it and/or modify
REM # it under the terms of the GNU General Public License as published by
REM # the Free Software Foundation, either version 3 of the License, or
REM # (at your option) any later version.
REM #
REM # https://www.gnu.org/licenses/gpl-3.0-standalone.html
REM #
REM # This program is distributed in the hope that it will be useful,
REM # but WITHOUT ANY WARRANTY; without even the implied warranty of
REM # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM # GNU General Public License for more details.
REM #
REM # You should have received a copy of the GNU General Public License
REM # along with this program. If not, see <https://www.gnu.org/licenses/>.
REM #

REM ################### Script configuration ###################
REM
REM set LEGACYMODE=on to start with legacy oh.bat script
REM
REM launch oh.bat -h to see available options
REM 
REM -> default startup script called is oh.ps1 (powershell) <-
REM 

set LEGACYMODE="off"

REM ############################################################
REM check for legacy mode

if %LEGACYMODE%=="on" goto legacy

REM ###### Functions and script start
goto :init

:header
	echo.
	echo  Open Hospital startup script
	echo  %__BAT_NAME% v%__SCRIPTVERSION%
	echo.
	goto :eof

:usage
	echo USAGE:
	echo   %__BAT_NAME% [-option]
	echo.
	echo.  -h, -?, --help              shows this help
	echo.  -legacymode, --legacymode   start OH with legacy oh.bat
	goto :eof

:init
	set "__SCRIPTVERSION=1.0"
	set "__BAT_FILE=%~0"
	set "__BAT_PATH=%~dp0"
	set "__BAT_NAME=%~nx0"

:parse
	if "%~1"=="" goto :main

	if /i "%~1"=="/?"	call :header & call :usage & goto :end
	if /i "%~1"=="-?"	call :header & call :usage & goto :end
	if /i "%~1"=="-h"	call :header & call :usage & goto :end
	if /i "%~1"=="-help"	call :header & call :usage & goto :end
	if /i "%~1"=="--help"	call :header & call :usage & goto :end

	if /i "%~1"=="/legacymode"	call :legacy & goto :end
	if /i "%~1"=="-legacymode"	call :legacy & goto :end
	if /i "%~1"=="--legacymode"	call :legacy & goto :end

	shift
	goto :parse

:main
	REM ################### oh.ps1 ###################
	REM default startup script called: oh.ps1

	echo Starting OH with oh.ps1...

	REM launch powershell script
	powershell.exe  -ExecutionPolicy Bypass -File  ./oh.ps1
	goto end

:legacy
REM ############################# Legacy oh.bat ############################

echo Legacy mode - Starting OH with oh.bat...

REM ################### Open Hospital Configuration ###################
REM #                                                   
REM #                   ___Warning___                   
REM #
REM # __this configuration parameters work ONLY for legacy mode__
REM #                                                   
REM # _for normal startup, please edit oh.ps1__
REM #
REM ###################
set OH_PATH=%~dps0

REM ##########  set mode  #########
REM set OH_MODE = PORTABLE SERVER CLIENT
set OH_MODE="PORTABLE"

REM ##########  set language  #########
REM # Language setting - default set to en
REM set OH_LANGUAGE=en fr es it pt ar
set OH_LANGUAGE=en

REM # set log level to INFO | DEBUG - default set to INFO
set LOG_LEVEL=INFO

REM ##################### Database configuration #######################
set DATABASE_SERVER=localhost
set DATABASE_PORT=3306
set DATABASE_ROOT_PW=tmp2021oh111
set DATABASE_NAME=oh
set DATABASE_USER=isf
set DATABASE_PASSWORD=isf123

REM #######################  OH configuration  #########################
REM # path and directories
set OH_DIR="."
set OH_DOC_DIR="..\doc"
set OH_SINGLE_USER="no"
set CONF_DIR="data\conf"
set DATA_DIR="data\db"
set PHOTO_DIR="data\photo"
set LOG_DIR="data\log"
set SQL_DIR="sql"
set SQL_EXTRA="sql\extra"
set TMP_DIR="tmp"

REM # imagingin / dicom
set DICOM_MAX_SIZE="4M"
set DICOM_STORAGE="FileSystemDicomManager"
set DICOM_DIR="data\dicom_storage"

REM # logging
set LOG_FILE=startup.log
set OH_LOG_FILE=openhospital.log

set DB_CREATE_SQL=create_all_en.sql
REM #-> DB_CREATE_SQL default is set to create_all_en.sql - set to "create_all_demo.sql" for demo or create_all_[lang].sql for language

REM ######## Architecture
REM # ARCH can be set to i686 (32 bit) or x64 (64bit)
REM set ARCH=i686
set ARCH=x64

REM ######## MySQL Software
REM # MariaDB 64bit download URL
REM https://archive.mariadb.org/mariadb-10.6.16/winx64-packages/mariadb-10.6.16-winx64.zip

REM # MariaDB 32bit download  URL
REM https://archive.mariadb.org/mariadb-10.6.5/win32-packages/mariadb-10.6.5-win32.zip

REM set MYSQL_DIR=mariadb-10.6.5-win%ARCH%
set MYSQL_DIR=mariadb-10.6.16-win%ARCH%

REM ####### JAVA Software
REM # JRE 11 64bit - x86_64 - openjdk
REM set JAVA_URL="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/"
REM set JAVA_DISTRO="OpenJDK11U-jre_x64_windows_hotspot_11.0.11_9.zip"

REM # JRE 8 32bit - i686 - zulu
REM set JAVA_DIR=zulu8.60.0.21-ca-jre8.0.322-win_i686
REM set JAVA_BIN=%OH_PATH%\%JAVA_DIR%\bin\java.exe

REM # JRE 11 32bit - i686 - zulu - default
set JAVA_DIR=zulu17.48.15-ca-jre17.0.10-win_%ARCH%
set JAVA_BIN=%OH_PATH%\%JAVA_DIR%\bin\java.exe

set REPLACE_PATH=%OH_PATH%\%MYSQL_DIR%\bin

REM ######## Script start

REM set path variable with / in place of \ for configuration file
set OH_PATH_SUBSTITUTE=%OH_PATH:\=/%

echo Configuring Open Hospital...

REM # Set mysql TCP port
set startPort=%DATABASE_PORT%
:searchport
netstat -o -n -a | find "LISTENING" | find ":%startPort% " > NUL
if "%ERRORLEVEL%" equ "0" (
	echo TCP port %startPort% unavailable
	set /a startPort +=1
	goto :searchport
) ELSE (
	echo TCP port %startPort% available
	set DATABASE_PORT=%startPort%
	goto :foundport
)

:foundport
echo Found TCP port %DATABASE_PORT% for MySQL !

REM # Create tmp and log dir
mkdir "%OH_PATH%\%TMP_DIR%"
mkdir "%OH_PATH%\%LOG_DIR%"

echo Generating MySQL config file...
REM ### Setup MySQL configuration
echo f | xcopy %OH_PATH%\%CONF_DIR%\my.cnf.dist %OH_PATH%\%CONF_DIR%\my.cnf /y > "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe OH_PATH_SUBSTITUTE %OH_PATH_SUBSTITUTE% -- %OH_PATH%\%CONF_DIR%\my.cnf  >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DATABASE_SERVER %DATABASE_SERVER% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DATABASE_PORT %DATABASE_PORT% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe MYSQL_DISTRO %MYSQL_DIR% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DICOM_SIZE %DICOM_MAX_SIZE% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe TMP_DIR %TMP_DIR% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DATA_DIR %DATA_DIR% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe LOG_DIR %LOG_DIR% -- %OH_PATH%\%CONF_DIR%\my.cnf >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1

echo Generating OH configuration files...
REM ### Setup dicom.properties
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\dicom.properties.dist %OH_PATH%\%OH_DIR%\rsc\dicom.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe OH_PATH_SUBSTITUTE %OH_PATH_SUBSTITUTE% -- %OH_PATH%\%OH_DIR%\rsc\dicom.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DICOM_SIZE %DICOM_MAX_SIZE% -- %OH_PATH%\%OH_DIR%\rsc\dicom.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DICOM_STORAGE %DICOM_STORAGE% -- %OH_PATH%\%OH_DIR%\rsc\dicom.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DICOM_DIR %DICOM_DIR% -- %OH_PATH%\%OH_DIR%\rsc\dicom.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1

REM ### Setup database.properties
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\database.properties.dist %OH_PATH%\%OH_DIR%\rsc\database.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBSERVER %DATABASE_SERVER% -- %OH_PATH%\%OH_DIR%\rsc\database.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBPORT %DATABASE_PORT% -- %OH_PATH%\%OH_DIR%\rsc\database.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBUSER %DATABASE_USER% -- %OH_PATH%\%OH_DIR%\rsc\database.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBPASS %DATABASE_PASSWORD% -- %OH_PATH%\%OH_DIR%\rsc\database.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBNAME %DATABASE_NAME% -- %OH_PATH%\%OH_DIR%\rsc\database.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1

REM ### Setup settings.properties
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\settings.properties.dist %OH_PATH%\%OH_DIR%\rsc\settings.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe OH_MODE %OH_MODE% -- %OH_PATH%\%OH_DIR%\rsc\settings.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe OH_LANGUAGE %OH_LANGUAGE% -- %OH_PATH%\%OH_DIR%\rsc\settings.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe OH_DOC_DIR %OH_DOC_DIR% -- %OH_PATH%\%OH_DIR%\rsc\settings.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe PHOTO_DIR %PHOTO_DIR% -- %OH_PATH%\%OH_DIR%\rsc\settings.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe YES_OR_NO %OH_SINGLE_USER% -- %OH_PATH%\%OH_DIR%\rsc\settings.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1

REM ### Setup log4j.properties
REM # replace backslash with slash
set OH_LOG_DIR=%LOG_DIR:\=/%
set OH_LOG_DEST=../%OH_LOG_DIR%/%OH_LOG_FILE%
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\log4j.properties.dist %OH_PATH%\%OH_DIR%\rsc\log4j.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBSERVER %DATABASE_SERVER% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBPORT %DATABASE_PORT% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBUSER %DATABASE_USER% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties  >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBPASS %DATABASE_PASSWORD% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe DBNAME %DATABASE_NAME% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe LOG_LEVEL %LOG_LEVEL% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
%REPLACE_PATH%\replace.exe LOG_DEST %OH_LOG_DEST% -- %OH_PATH%\%OH_DIR%\rsc\log4j.properties >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1

REM ### Setup other OH property files
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\examination.properties.dist %OH_PATH%\%OH_DIR%\rsc\examination.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\sms.properties.dist %OH_PATH%\%OH_DIR%\rsc\sms.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\txtPrinter.properties.dist %OH_PATH%\%OH_DIR%\rsc\txtPrinter.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\telemetry.properties.dist %OH_PATH%\%OH_DIR%\rsc\telemetry.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\xmpp.properties.dist %OH_PATH%\%OH_DIR%\rsc\xmpp.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\default_credentials.properties.dist %OH_PATH%\%OH_DIR%\rsc\default_credentials.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
REM echo f | xcopy %OH_PATH%\%OH_DIR%\rsc\default_demo_credentials.properties.dist %OH_PATH%\%OH_DIR%\rsc\default_demo_credentials.properties /y >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1

REM ### Setup database if not already existing
if not EXIST %OH_PATH%\%DATA_DIR%\%DATABASE_NAME% (
 	REM # Remove database files
	echo Removing data...
	rmdir /s /q %OH_PATH%\%DATA_DIR%
	REM # Create directories
	mkdir "%OH_PATH%\%DATA_DIR%"
	mkdir "%OH_PATH%\%TMP_DIR%"
	mkdir "%OH_PATH%\%LOG_DIR%"
	mkdir "%OH_PATH%\%DICOM_DIR%"
	mkdir "%OH_PATH%\%PHOTO_DIR%"
	del /s /q %OH_PATH%\%TMP_DIR%\*

	if %MYSQL_DIR:~0,5% == maria (
		echo Initializing MariaDB...
		start /b /min /wait %OH_PATH%\%MYSQL_DIR%\bin\mysql_install_db.exe --datadir=%OH_PATH%\%DATA_DIR% --password=%DATABASE_ROOT_PW%  >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
	)
	if %MYSQL_DIR:~0,5% == mysql (
		echo Initializing MySQL...
		start /b /min /wait %OH_PATH%\%MYSQL_DIR%\bin\mysqld.exe --initialize-insecure --console --basedir="%OH_PATH%\%MYSQL_DIR%" --datadir="%OH_PATH%\%DATA_DIR%"
	)
	if ERRORLEVEL 1 (goto error)

	echo Starting MySQL server on port %DATABASE_PORT%...
	start /b /min %OH_PATH%\%MYSQL_DIR%\bin\mysqld.exe --defaults-file=%OH_PATH%\%CONF_DIR%\my.cnf --tmpdir=%OH_PATH%\%TMP_DIR% --standalone --console
	if ERRORLEVEL 1 (goto error)
	timeout /t 2 /nobreak >nul

	REM # If using MySQL root password need to be set
	if %MYSQL_DIR:~0,5% == mysql (
		echo Setting MySQL root password...
		start /b /min /wait %OH_PATH%\%MYSQL_DIR%\bin\mysql.exe -u root --skip-password --host=%DATABASE_SERVER% --port=%DATABASE_PORT% -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '%DATABASE_ROOT_PW%';" >> %OH_PATH%\%LOG_DIR%\%LOG_FILE% 2>&1
		if ERRORLEVEL 1 (goto error)
	)

	echo Creating database...

	start /b /min /wait %OH_PATH%\%MYSQL_DIR%\bin\mysql.exe -u root -p%DATABASE_ROOT_PW% --host=%DATABASE_SERVER% --port=%DATABASE_PORT% -e "CREATE USER '%DATABASE_USER%'@'localhost' IDENTIFIED BY '%DATABASE_PASSWORD%'; CREATE DATABASE %DATABASE_NAME% CHARACTER SET utf8; GRANT ALL PRIVILEGES ON %DATABASE_NAME%.* TO '%DATABASE_USER%'@'localhost' IDENTIFIED BY '%DATABASE_PASSWORD%';" >> %OH_PATH%\%LOG_DIR%\%LOG_FILE% 2>&1
 	if ERRORLEVEL 1 (goto error)

	echo Importing database %DATABASE_NAME% with user %DATABASE_USER%@%DATABASE_SERVER%...
	cd /d %OH_PATH%\%SQL_DIR%
	start /b /min /wait %OH_PATH%\%MYSQL_DIR%\bin\mysql.exe --local-infile=1 -u %DATABASE_USER% -p%DATABASE_PASSWORD% --host=%DATABASE_SERVER% --port=%DATABASE_PORT% %DATABASE_NAME% < "%OH_PATH%\sql\%DB_CREATE_SQL%"  >> "%OH_PATH%\%LOG_DIR%\%LOG_FILE%" 2>&1
	if ERRORLEVEL 1 (goto error)
	cd /d %OH_PATH%
	echo Database imported!
) else (
	echo Database already initialized, trying to start...
	echo Starting MySQL server on port %DATABASE_PORT%...
	start /b /min %OH_PATH%\%MYSQL_DIR%\bin\mysqld.exe --defaults-file=%OH_PATH%\%CONF_DIR%\my.cnf --tmpdir=%OH_PATH%\%TMP_DIR% --standalone --console
	if ERRORLEVEL 1 (goto error)
)

REM ###### Setup CLASSPATH #####
set CLASSPATH=%OH_PATH%\%OH_DIR%\lib

SETLOCAL ENABLEDELAYEDEXPANSION

REM Include all jar files under lib\
for %%A IN (%OH_PATH%\%OH_DIR%\lib\*.jar) DO (
	set CLASSPATH=!CLASSPATH!;%%A
)
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\bundle
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\rpt_base
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\rpt_extra
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\rpt_stat
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\rsc
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\rsc\images
REM set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\rsc\icons REM hardcoded
set CLASSPATH=%CLASSPATH%;%OH_PATH%\%OH_DIR%\bin\OH-gui.jar

REM # Setup native_lib_path for current architecture
if %PROCESSOR_ARCHITECTURE%==AMD64 if not %ARCH%==32 (
	set NATIVE_LIB_PATH=%OH_PATH%\%OH_DIR%\lib\native\Win64
) else (
	set NATIVE_LIB_PATH=%OH_PATH%\%OH_DIR%\lib\native\Windows
)

REM ###### Start Open Hospital #####

echo Starting Open Hospital GUI...

cd /d %OH_PATH%\%OH_DIR%
%JAVA_BIN% -client -Xms64m -Xmx1024m -Dsun.java2d.dpiaware=false -Djava.library.path=%NATIVE_LIB_PATH% -cp %CLASSPATH% org.isf.menu.gui.Menu

REM # Shutdown MySQL
echo Shutting down MySQL...
start /b /min /wait %OH_PATH%\%MYSQL_DIR%\bin\mysqladmin --user=root --password=%DATABASE_ROOT_PW% --host=%DATABASE_SERVER% --port=%DATABASE_PORT% shutdown >> %OH_PATH%\%LOG_DIR%\%LOG_FILE% 2>&1

REM # Exit
echo Exiting Open Hospital...
cd /d %OH_PATH%
echo Done !

goto end

:error
	echo Error starting Open Hospital, exiting.
	cd /d %OH_PATH%
	goto end

:end
	call :cleanup
	exit /B

:cleanup
	REM The cleanup function is only really necessary if you
	REM are _not_ using SETLOCAL.
	set "__SCRIPTVERSION="
	set "__BAT_FILE="
	set "__BAT_PATH="
	set "__BAT_NAME="
	set "LEGACYMODE="

	goto :eof

