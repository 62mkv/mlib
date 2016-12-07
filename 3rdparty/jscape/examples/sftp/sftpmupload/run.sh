#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . SFtpMuploadExample.java
java -cp .:../../../lib/sftp.jar SFtpMuploadExample
