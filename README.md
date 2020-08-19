# OpenHospital-gui
[![Java CI](https://github.com/informatici/openhospital-gui/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/informatici/openhospital-gui/actions?query=workflow%3A%22Java+CI+with+Maven%22)

**OpenHospital-core**
You need the openhospital-core in order to run the gui.

* clone [openhospital-core](https://github.com/informatici/openhospital-core)
* follow the instructions in the related README.md

**How to build with Maven:**

    mvn clean install
    
**How to create the DataBase**:

You need a local (or remote) MySQL server where to run the script in mysql/db/ folder

    create_all_en.sql
	
For remote MySQL server you need to change IP (localost) and PORT (3306) in rsc/applicationContext.properties:

    <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/oh" />

**With docker compose**

Simply run (it will run on localhost:3306):

    docker-compose up 

**How to launch the software**:

Use scripts startup.sh (Linux) or startup.cmd (Windows)

**Other info**

Please read Admin and User manuals in doc/ folder

# How to contribute

Please read the OpenHospital [Wiki](https://openhospital.atlassian.net/wiki/display/OH/Contribution+Guidelines)

See the Open Issues on [Jira](https://openhospital.atlassian.net/issues/)
