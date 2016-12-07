#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpsDownloadDirExample.java
java -cp .:../../../lib/sftp.jar FtpsDownloadDirExample
