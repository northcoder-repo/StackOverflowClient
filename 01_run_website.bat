@echo off

set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-20.0.1.9-hotspot"

cd target

%JAVA_HOME%\bin\java ^
  -cp StackOverflowClient-1.0.0.jar;lib\* ^
  org.northcoder.soclient.App
