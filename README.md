# Open Hospital - GUI
[![Java CI](https://github.com/informatici/openhospital-gui/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/informatici/openhospital-gui/actions?query=workflow%3A%22Java+CI+with+Maven%22)

This is the GUI component of [Open Hospital][openhospital]: it contains a graphical user interface (GUI) made with Java Swing. 
This project depends on the [Core component][openhospital-core] for the business logic and the data abstraction layer. 
An alternative user interface based on React and currently still work-in-progress is available [here][openhospital-ui].

## How to build

To build this project you'll need Java JDK 17+ and Maven (or using the provided Maven Wrapper `mvnw`)
Additionally, you'll need to build and install locally the [Core component][openhospital-core] of Open Hospital.
Once you do that, to build this project just issue:

  mvn package
  
To run the tests simply issue:

  mvn test
  
## How to launch Open Hospital

To launch Open Hospital GUI, use the scripts `oh.sh` (on Linux) or `oh.bat` (on Windows) from the maven `target` folder.
You will need a MySQL database running locally (e.g. the Docker container available in the Core project),
or any similar SQL database (e.g. MariaDB).

### Launch within IDE

Be sure to have configured properly the classpath for the project (see [5 Installing Open Hospital 1.14.0 in Eclipse EE](https://github.com/informatici/openhospital-doc/blob/develop/doc_admin/AdminManual.adoc#5-installing-open-hospital-1-14-0-in-eclipse-ee))

Before running the application, you should generate the config files with the `g)` option, or manually copying and renaming the files `*.dist` files in `rsc/` folder and edit them accordingly:

| Dist file                | Property file       | Properties to fill in                                         |
|--------------------------|---------------------|---------------------------------------------------------------|
| database.properties.dist | database.properties | DBSERVER, DBPORT, DBNAME, DBUSER, DBPASS                      |
| dicom.properties.dist    | dicom.properties    | OH_PATH_SUBSTITUTE/DICOM_DIR, DICOM_SIZE                      |
| log4j.properties.dist    | log4j.properties    | LOG_DEST, DBSERVER, DBPORT, DBNAME, DBUSER, DBPASS, LOG_LEVEL |
| settings.properties.dist | settings.properties | OH_LANGUAGE,(SINGLEUSER=)YES_OR_NO, PHOTO_DIR, OH_DOC_DIR     |

*For further information, please refer to the Admin and User manuals in the [Doc project][openhospital-doc].*

## How to contribute

You can find the contribution guidelines in the [Open Hospital wiki][contribution-guide]. 
A list of open issues is available on [Jira][jira].

## Community

You can reach out to the community of contributors by joining 
our [Slack workspace][slack] or by subscribing to our [mailing list][ml].

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
* Navigate to the location of the file which relative to the project root is: `.ide-settings/idea/OpenHospital-code-style-configuration.xml`
* Select *OK* 
* At this point the code style is stored as part of the IDE and is used for **all** projects opened in the editor. To restrict the settings to just this project again select the setting button by the side of the *Scheme* list and select *Copy to Project...*. If successful a notice appears in the window that reads: *For current project*.

</details>

<details><summary>Eclipse instructions</summary>

For Eclipse the process requires loading the formatting style and the import order separately.

* Select *Preferences* in the *Window* menu
* Select *Java*
* Select *Code Style* and expand the menu
* Select *Formatter*
* Select the *Import...* button
* Navigate to the location of the file which relative to the project root is: `.ide-settings/eclipse/OpenHospital-Java-CodeStyle-Formatter.xml`
* Select *Open*
* At this point the code style is stored and is applicable to all projects opened in the IDE. To restrict the settings just to this project select *Configure Project Specific Settings...* in the upper right. In the next dialog select the *openhospital* repository and select *OK*. In the next dialog select the *Enable project specific settings* checkbox. Finally select *Apply and Close*.
* Back in the *Code Style* menu area, select *Organize Imports*
* Select *Import...*
* Navigate to the location of the file which relative to the project root is: `.ide-settings/eclipse/OpenHospital.importorder`
* Select *Open*
* As with the formatting styles the import order is applicable to all projects. In order to change it just for this project repeat the same steps as above for *Configure Project Specific Settings...*
 
</details> 

 [openhospital]: https://www.open-hospital.org/
 [openhospital-core]: https://github.com/informatici/openhospital-core
 [openhospital-ui]: https://github.com/informatici/openhospital-ui
 [openhospital-doc]: https://github.com/informatici/openhospital-doc
 [contribution-guide]: https://openhospital.atlassian.net/wiki/display/OH/Contribution+Guidelines
 [jira]: https://openhospital.atlassian.net/jira/software/c/projects/OP/issues/
 [database.prop]: https://github.com/informatici/openhospital-core/blob/develop/src/test/resources/database.properties
 [slack]: https://join.slack.com/t/openhospitalworkspace/shared_invite/enQtOTc1Nzc0MzE2NjQ0LWIyMzRlZTU5NmNlMjE2MDcwM2FhMjRkNmM4YzI0MTAzYTA0YTI3NjZiOTVhMDZlNWUwNWEzMjE5ZDgzNWQ1YzE
 [ml]: https://sourceforge.net/projects/openhospital/lists/openhospital-devel
