@echo off
setlocal enabledelayedexpansion

echo =====================================================================
echo  VEHICLE SERVICE CENTRE MANAGEMENT SYSTEM
echo  Build and Run Utility
echo =====================================================================
echo.

:: 1. Check Java JDK
javac -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] 'javac' compiler was not found in your PATH!
    echo Please install Java JDK 11 or higher and add it to your System Environment PATH.
    echo Download JDK from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [1/3] Java JDK detected successfully:
java -version
echo.

:: 2. Check if Maven is installed
call mvn -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [2/3] Building application using Apache Maven...
    call mvn clean compile
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Maven compilation failed.
        pause
        exit /b 1
    )
    echo.
    echo [3/3] Launching Vehicle Service Centre Management System...
    call mvn exec:java -Dexec.mainClass="Main"
    goto :end
)

:: 3. Fallback to direct javac / java compilation if Maven is not installed
echo [2/3] Maven not found in PATH; compiling using direct JDK javac...

if not exist "target\classes" mkdir target\classes

:: Copy resources
if exist "src\main\resources" (
    xcopy /E /I /Y "src\main\resources\*" "target\classes\" >nul
)

:: Compile all Java sources
dir /s /B src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d target\classes @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Check errors above.
    pause
    exit /b 1
)

echo.
echo [3/3] Launching Desktop Application...
java -cp "target\classes" Main

:end
echo Application exited.
pause
