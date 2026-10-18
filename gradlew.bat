@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
@rem Check for wrapper jar, else fallback to system gradle
set APP_HOME=%DIRNAME%

set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if exist "%WRAPPER_JAR%" (
    java -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
) else (
    echo Wrapper jar not found - using system gradle. To generate wrapper run: gradle wrapper --gradle-version 8.4
    gradle %*
)
