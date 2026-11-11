@echo off
setlocal
cd /d "%~dp0backend"

set MVN_CMD="%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\439sdfsg2nbdob9ciift5h5nse\apache-maven-3.9.6\bin\mvn.cmd"
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 set MVN_CMD=mvn

echo =======================================================
echo  Starting Moroccan Darija LLM Translator Backend...
echo =======================================================
call %MVN_CMD% exec:java -Dexec.mainClass="com.darija.translator.ServerMain"
pause

