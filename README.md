# DMS

You need to install the jars as maven dependencies. run these commands in the libs directory

mvn install:install-file -Dfile=chemistry-opencmis-client-api-1.0.0.jar -DgroupId=org.apache.chemistry.opencmis -DartifactId=chemistry-opencmis-client-api -Dversion=1.1.0 -Dpackaging=jar

mvn install:install-file -Dfile=chemistry-opencmis-client-bindings-1.0.0.jar -DgroupId=org.apache.chemistry.opencmis -DartifactId=chemistry-opencmis-client-bindings -Dversion=1.1.0 -Dpackaging=jar

mvn install:install-file -Dfile=chemistry-opencmis-client-impl-1.0.0.jar -DgroupId=org.apache.chemistry.opencmis -DartifactId=chemistry-opencmis-client-impl -Dversion=1.1.0 -Dpackaging=jar

mvn install:install-file -Dfile=chemistry-opencmis-commons-api-1.0.0.jar -DgroupId=org.apache.chemistry.opencmis -DartifactId=chemistry-opencmis-commons-api -Dversion=1.1.0 -Dpackaging=jar

mvn install:install-file -Dfile=chemistry-opencmis-commons-impl-1.0.0.jar -DgroupId=org.apache.chemistry.opencmis -DartifactId=chemistry-opencmis-commons-impl -Dversion=1.1.0 -Dpackaging=jar
