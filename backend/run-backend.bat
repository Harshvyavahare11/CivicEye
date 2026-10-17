@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Program Files\apache-maven-3.10.0\bin;%PATH%"
cd /d "%~dp0"
echo Starting CivicEye Backend with Java 21...
"C:\Program Files\apache-maven-3.10.0\bin\mvn.cmd" spring-boot:run
