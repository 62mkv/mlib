#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpsMdownloadExample.java
java -cp .:../../../lib/sftp.jar FtpsMdownloadExample
