@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  simulator-ui startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and SIMULATOR_UI_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\simulator-ui-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\formsrt.jar;%APP_HOME%\lib\Jama-1.0.2.jar;%APP_HOME%\lib\jcommon-1.0.0.jar;%APP_HOME%\lib\jfreechart-1.0.1.jar;%APP_HOME%\lib\jgrapht-jdk1.5-0.7.3.jar;%APP_HOME%\lib\jmatio.jar;%APP_HOME%\lib\log4j-1.2.16.jar;%APP_HOME%\lib\qdox-1.6.3.jar;%APP_HOME%\lib\ssj.jar;%APP_HOME%\lib\swingx-all-1.6.4.jar;%APP_HOME%\lib\colt.jar;%APP_HOME%\lib\commons-collections-3.2.jar;%APP_HOME%\lib\itextpdf-5.3.4.jar;%APP_HOME%\lib\jayatana-1.2.4.jar;%APP_HOME%\lib\jbullet.jar;%APP_HOME%\lib\jnumeric-0.1.jar;%APP_HOME%\lib\jpct.jar;%APP_HOME%\lib\jung-1.7.6.jar;%APP_HOME%\lib\jython.jar;%APP_HOME%\lib\macify-1.4.jar;%APP_HOME%\lib\piccolo.jar;%APP_HOME%\lib\piccolox.jar;%APP_HOME%\lib\vecmath.jar;%APP_HOME%\lib\launch4j.jar;%APP_HOME%\lib\commons-beanutils.jar;%APP_HOME%\lib\commons-logging.jar;%APP_HOME%\lib\forms.jar;%APP_HOME%\lib\formsrt.jar;%APP_HOME%\lib\foxtrot.jar;%APP_HOME%\lib\looks.jar;%APP_HOME%\lib\xstream.jar;%APP_HOME%\lib\simulator-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\rosjava-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\hamcrest-core-1.1.jar;%APP_HOME%\lib\junit-4.10.jar;%APP_HOME%\lib\xml-apis-1.0.b2.jar;%APP_HOME%\lib\ws-commons-util-1.0.1.jar;%APP_HOME%\lib\com.springsource.org.apache.commons.logging-1.1.1.jar;%APP_HOME%\lib\com.springsource.org.apache.commons.codec-1.3.0.jar;%APP_HOME%\lib\com.springsource.org.apache.commons.httpclient-3.1.0.jar;%APP_HOME%\lib\apache_xmlrpc_common-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\apache_xmlrpc_client-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\com.springsource.org.apache.commons.net-2.0.0.jar;%APP_HOME%\lib\commons-pool-1.6.jar;%APP_HOME%\lib\jsr305-1.3.9.jar;%APP_HOME%\lib\guava-12.0.jar;%APP_HOME%\lib\com.springsource.org.apache.commons.lang-2.4.0.jar;%APP_HOME%\lib\com.springsource.org.apache.commons.io-1.4.0.jar;%APP_HOME%\lib\netty-3.5.2.Final.jar;%APP_HOME%\lib\rosjava_bootstrap-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\rosjava_messages-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\apache_xmlrpc_server-0.0.0-SNAPSHOT.jar;%APP_HOME%\lib\dnsjava-2.1.1.jar

@rem Execute simulator-ui
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SIMULATOR_UI_OPTS%  -classpath "%CLASSPATH%" ca.nengo.ui.NengoLauncher %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable SIMULATOR_UI_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%SIMULATOR_UI_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
