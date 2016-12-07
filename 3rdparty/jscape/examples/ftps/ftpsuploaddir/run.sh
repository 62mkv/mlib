#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpsUploadDirExample.java
java -cp .:../../../lib/sftp.jar FtpsUploadDirExample
