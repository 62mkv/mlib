#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . SFtpUploadDirExample.java
java -cp .:../../../lib/sftp.jar SFtpUploadDirExample
