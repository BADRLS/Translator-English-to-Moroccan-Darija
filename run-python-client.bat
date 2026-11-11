@echo off
setlocal
cd /d "%~dp0clients\python-client"

echo ===================================================
echo  Starting Moroccan Darija Python Desktop Client...
echo ===================================================
python translator_client.py
pause

