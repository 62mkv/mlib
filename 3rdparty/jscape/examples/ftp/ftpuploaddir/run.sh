#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpUploadDirExample.java
java -cp .:../../../lib/sftp.jar FtpUploadDirExample
