@echo off
setlocal

set DIR=%~dp0
set WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
set WRAPPER_PROPERTIES=%DIR%gradle\wrapper\gradle-wrapper.properties

java -cp "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
