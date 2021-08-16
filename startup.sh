#!/bin/sh
#
# Open Hospital (www.open-hospital.org)
# Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

######## Environment check:

# SET JAVA_BIN or JAVA_HOME
JAVA_BIN=`which java`
#JAVA_HOME=/usr/lib/jvm/java-14-openjdk-amd64/

# check if java home exists

if [ -z $JAVA_BIN ]; then
  echo "Warning - JAVA_BIN not found. Please set it up properly."
if [ -z $JAVA_HOME ]; then
  echo "Error - JAVA_HOME not found. Please set it up properly."
  exit
  fi
  # set JAVA_BIN
  JAVA_BIN=$JAVA_HOME/bin/java
fi

######## OPEN HOSPITAL Configuration
# OPENHOSPITAL_HOME is the directory where OpenHospital files are located
#OPENHOSPITAL_HOME=/usr/local/OpenHospital

# set current dir
CURRENT_DIR=$PWD

if [ -z $OPENHOSPITAL_HOME ]; then
  echo "Warning - OPENHOSPITAL_HOME not found. Please set it up properly."
  OPENHOSPITAL_HOME=$PWD
fi

######################## DO NOT EDIT BELOW THIS LINE ########################

DIRLIBS=${OPENHOSPITAL_HOME}/bin/*.jar
for i in ${DIRLIBS}
do
  if [ -z "$OPENHOSPITAL_CLASSPATH" ] ; then
    OPENHOSPITAL_CLASSPATH=$i
  else
    OPENHOSPITAL_CLASSPATH="$i":$OPENHOSPITAL_CLASSPATH
  fi
done

DIRLIBS=${OPENHOSPITAL_HOME}/lib/*.jar
for i in ${DIRLIBS}
do
  if [ -z "$OPENHOSPITAL_CLASSPATH" ] ; then
    OPENHOSPITAL_CLASSPATH=$i
  else
    OPENHOSPITAL_CLASSPATH="$i":$OPENHOSPITAL_CLASSPATH
  fi
done

DIRLIBS=${OPENHOSPITAL_HOME}/rsc/*.properties
for i in ${DIRLIBS}
do
  if [ -z "$OPENHOSPITAL_CLASSPATH" ] ; then
    OPENHOSPITAL_CLASSPATH=$i
  else
    OPENHOSPITAL_CLASSPATH="$i":$OPENHOSPITAL_CLASSPATH
  fi
done

DIRLIBS=${OPENHOSPITAL_HOME}/rsc/SmsGateway/*.properties
for i in ${DIRLIBS}
do
  if [ -z "$OPENHOSPITAL_CLASSPATH" ] ; then
    OPENHOSPITAL_CLASSPATH=$i
  else
    OPENHOSPITAL_CLASSPATH="$i":$OPENHOSPITAL_CLASSPATH
  fi
done

OPENHOSPITAL_CLASSPATH="${OPENHOSPITAL_HOME}/bundle":$OPENHOSPITAL_CLASSPATH
OPENHOSPITAL_CLASSPATH="${OPENHOSPITAL_HOME}/rpt":$OPENHOSPITAL_CLASSPATH
OPENHOSPITAL_CLASSPATH="${OPENHOSPITAL_HOME}/rsc":$OPENHOSPITAL_CLASSPATH
OPENHOSPITAL_CLASSPATH="${OPENHOSPITAL_HOME}":$OPENHOSPITAL_CLASSPATH

ARCH=$(uname -m)
case $ARCH in
	x86_64|amd64|AMD64)
		NATIVE_LIB_PATH=${OPENHOSPITAL_HOME}/lib/native/Linux/amd64
		;;
	i[3456789]86|x86|i86pc)
		NATIVE_LIB_PATH=${OPENHOSPITAL_HOME}/lib/native/Linux/i386
		;;
	*)
		echo "Unknown architecture $(uname -m)"
		;;
esac

######### OPEN HOSPITAL STARTUP

cd $OPENHOSPITAL_HOME&& $JAVA_BIN -Dsun.java2d.dpiaware=false -Djava.library.path=${NATIVE_LIB_PATH} -classpath "$OPENHOSPITAL_CLASSPATH" org.isf.menu.gui.Menu "$@"

# go back to working directory
cd $CURRENT_DIR
