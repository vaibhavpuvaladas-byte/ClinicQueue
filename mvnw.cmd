@echo off
@REM ClinicQueue Maven Wrapper for Windows

setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

@REM Ensure Java 21+ is used if available
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java" -version 2>&1 | findstr /i "17\." >nul
    if not errorlevel 1 (
        if exist "C:\Program Files\Java\jdk-25" (
            set "JAVA_HOME=C:\Program Files\Java\jdk-25"
        ) else if exist "C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot" (
            set "JAVA_HOME=C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot"
        )
    )
) else (
    if exist "C:\Program Files\Java\jdk-25" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25"
    ) else if exist "C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot" (
        set "JAVA_HOME=C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot"
    )
)

@REM Check if local portable maven exists in .tools
set LOCAL_MVN=%DIRNAME%\.tools\apache-maven-3.9.9\bin\mvn.cmd

if exist "%LOCAL_MVN%" (
    call "%LOCAL_MVN%" %*
) else (
    where mvn >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        call mvn %*
    ) else (
        echo [ERROR] Maven not found! Please ensure Apache Maven is installed or located in .tools.
        exit /b 1
    )
)
