#!/bin/sh
javac -classpath .:../../../lib/sftp.jar -d . FtpsExample.java
java -cp .:../../../lib/sftp.jar FtpsExample
