#!/bin/sh
javac -classpath .:../../lib/sftp.jar -d . ScpExample.java
java -cp .:../../lib/sftp.jar ScpExample
