#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpsMuploadExample.java
java -cp .:../../../lib/sftp.jar FtpsMuploadExample
