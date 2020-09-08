# OpenHospital-gui
[![Java CI](https://github.com/informatici/openhospital-gui/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/informatici/openhospital-gui/actions?query=workflow%3A%22Java+CI+with+Maven%22)

**Translations**

[![Transifex](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/content/)

[![ar](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Far%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/ar/)
[![cs](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fcs%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/cs/)
[![de](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fde%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/de/)
[![el](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fel%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/el/)
[![es](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fes%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/es/)
[![fr](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Ffr%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/fr/)
[![hi_IN](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fhi_IN%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/hi_IN/)
[![it](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fit%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/it/)
[![pt](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fpt%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/pt/)
[![sq](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fsq%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/sq/)
[![sw](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fsw%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/sw/)
[![ta](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fta%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/ta/)
[![zh_CN](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Ftransifex-open-api.herokuapp.com%2Fbadge%2Finformatici-senza-frontiere-onlus%2Fproject%2Fopenhospital%2Flanguage%2Fzh_CN%2Ftranslated.json)](https://www.transifex.com/informatici-senza-frontiere-onlus/openhospital/language/zh_CN/)

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

## Code style

This project uses a consistent code style and provides definitions for use in both IntelliJ and Eclipse IDEs.

<details><summary>IntelliJ IDEA instructions</summary>

For IntelliJ IDEA the process for importing the code style is:

* Select *Settings* in the *File* menu
* Select *Editor*
* Select *Code Style*
* Expand the menu item and select *Java*
* Go to *Scheme* at the top, click on the setting button by the side of the drop-down list
* Select *Import Scheme*
* Select *IntelliJ IDE code style XML*
* Navigate to the location of the file which relative to the project root is:  `.ide-settings/idea/OpenHospital-code-style-configuration.xml`
* Select *OK* 
* At this point the code style is stored as part of the IDE and is used for **all** projects opened in the editor.  To restrict the settings to just this project again select the setting button by the side of the *Scheme* list and select *Copy to Project...*. If successful a notice appears in the window that reads: *For current project*.

</details>

<details><summary>Eclipse instructions</summary>

For Eclipse the process requires loading the formatting style and the import order separately.

* Select *Preferences* in the *Window* menu
* Select *Java*
* Select *Code Style* and expand the menu
* Select *Formatter*
* Select the *Import...* button
* Navigate to the location of the file which relative to the project root is:  `.ide-settings/eclipse/OpenHospital-Java-CodeStyle-Formatter.xml`
* Select *Open*
* At this point the code style is stored and is applicable to all projects opened in the IDE.  To restrict the settings just to this project select *Configure Project Specific Settings...* in the upper right.  In the next dialog select the *openhospital* repository and select *OK*.  In the next dialog select the *Enable project specific settings* checkbox.  Finally select *Apply and Close*.
* Back in the *Code Style* menu area, select *Organize Imports*
* Select *Import...*
* Navigate to the location of the file which relative to the project root is:  `.ide-settings/eclipse/OpenHospital.importorder`
* Select *Open*
* As with the formatting styles the import order is applicable to all projects.  In order to change it just for this project repeat the same steps as above for *Configure Project Specific Settings...*
 
</details> 

Please read the OpenHospital [Wiki](https://openhospital.atlassian.net/wiki/display/OH/Contribution+Guidelines)

See the Open Issues on [Jira](https://openhospital.atlassian.net/issues/)
