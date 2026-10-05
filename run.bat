@echo off
REM ==========================================================
REM  Personal Knowledge Vault - compile and run
REM  Just double-click this file.
REM ==========================================================
cd /d "%~dp0"

if not exist out mkdir out

echo Compiling Java files...
javac -encoding UTF-8 -cp "lib/*" -d out src\*.java
if errorlevel 1 (
    echo.
    echo Compile failed. Please check the messages above.
    pause
    exit /b 1
)

echo Starting the program...
java -cp "out;lib/*" Main
pause
