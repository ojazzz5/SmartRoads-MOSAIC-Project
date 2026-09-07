@ECHO OFF
REM Set your JAVA_HOME and MOSAIC path before running
SET JAVA_HOME=C:\path\to\your\jdk
SET PATH=%JAVA_HOME%\bin;C:\path\to\mosaic;%PATH%
echo Starting S1 - ITS (Variable Message Signs)...
cd /d C:\path\to\mosaic
call mosaic.bat -c "%~dp0S1_ITS\scenario_config.json" -w 0
pause