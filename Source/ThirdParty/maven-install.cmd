@echo off
pushd "%~dp0"
call mvn install:install-file -Dfile=spring-security\spring-security-core-4.0.0.M2.jar -DgroupId=org.springframework.security -DartifactId=spring-security-core -Dversion=4.0.0.M2 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-security\spring-security-web-4.0.0.M2.jar -DgroupId=org.springframework.security -DartifactId=spring-security-web -Dversion=4.0.0.M2 -Dpackaging=jar
popd
