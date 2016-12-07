#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpExample.java
java -cp .:../../../lib/sftp.jar FtpExample
