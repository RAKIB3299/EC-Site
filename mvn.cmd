@echo off
set "MAVEN_PROJECTBASEDIR=%~dp0"
"%~dp0.tools\apache-maven-3.9.16\bin\mvn.cmd" "-Dmaven.repo.local=%~dp0.m2\repository" %*
