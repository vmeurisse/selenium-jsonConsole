selenium-jsonConsole
====================

[![Build status](https://travis-ci.org/vmeurisse/selenium-jsonConsole.svg?branch=master)](http://travis-ci.org/vmeurisse/selenium-jsonConsole)

Usage
-----

### Build

You can build this provect as any maven project
````
mvn install
````

The result file will be in `target/jsonConsole-1.0-SNAPSHOT.jar`

### Start selenium Grid

You can now start your selenium grid using this command

````
java -cp jsonConsole-1.0-SNAPSHOT.jar;selenium-server-standalone-2.37.0.jar org.openqa.grid.selenium.GridLauncher -role hub -port 4444 -servlets org.meurisse.selenium.jsonConsole.JsonConsole
````

### Usage

You can now access the new console at `http://localhost:4444/grid/admin/JsonConsole`.

License
-------

This project is licensed under the [MIT License](http://en.wikipedia.org/wiki/MIT_License). See LICENSE.txt for details.
