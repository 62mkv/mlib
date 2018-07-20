mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\mad\mad-1.0.3.jar" -DgroupId=sni.mlib -DartifactId=mad -Dversion=1.0.3 -Dpackaging=jar;
mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\jscape\lib\sftp.jar" -DgroupId=sni.mlib -DartifactId=sftp -Dversion=1.0 -Dpackaging=jar;
mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\oracle\ojdbc6.jar" -DgroupId=sni.mlib -DartifactId=oracle-ojdbc -Dversion=12.1.0.1 -Dpackaging=jar;
mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\el4j\module-xml_merge-common-3.1.jar" -DgroupId=sni.mlib -DartifactId=module-xml_merge-common -Dversion=3.1 -Dpackaging=jar;
mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\mad\metrics-core-3.0.0-SNAPSHOT.jar" -DgroupId=sni.mlib -DartifactId=metrics-core -Dversion=3.0.0 -Dpackaging=jar;
mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\junit\junit-4.10.jar" -DgroupId=sni.mlib -DartifactId=junit -Dversion=4.10 -Dpackaging=jar;
mvn install:install-file -Dfile="%MLIBDIR%\3rdparty\hibernate\hibernate-core-4.1.10.Final.jar" -DgroupId=sni.mlib -DartifactId=hibernate-core -Dversion=4.1.10 -Dpackaging=jar;
