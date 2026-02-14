@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script, version 3.3.2
@REM ----------------------------------------------------------------------------
@echo off
setlocal EnableDelayedExpansion

set BASE_DIR=%~dp0
set WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

if "%JAVA_HOME%"=="" (
  set JAVA_CMD=java
) else (
  set JAVA_CMD=%JAVA_HOME%\bin\java
)

if not exist "%WRAPPER_JAR%" (
  if not exist "%WRAPPER_PROPERTIES%" (
    echo Missing %WRAPPER_PROPERTIES%
    exit /b 1
  )

  for /f "usebackq tokens=1,* delims==" %%A in (`findstr /b /c:"wrapperUrl=" "%WRAPPER_PROPERTIES%"`) do set WRAPPER_URL=%%B

  if "%WRAPPER_URL%"=="" (
    echo wrapperUrl not set in %WRAPPER_PROPERTIES%
    exit /b 1
  )

  if exist "%SystemRoot%\System32\curl.exe" (
    "%SystemRoot%\System32\curl.exe" -fsSL "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
  ) else if exist "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" (
    "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
  ) else (
    echo Neither curl nor powershell is available to download Maven Wrapper.
    exit /b 1
  )
)

set HAS_RUN=0
set HAS_TARGET=0
for %%A in (%*) do (
  if /I "%%~A"=="spring-boot:run" set HAS_RUN=1
  if /I "%%~A"=="-f" set HAS_TARGET=1
  if /I "%%~A"=="--file" set HAS_TARGET=1
  if /I "%%~A"=="-pl" set HAS_TARGET=1
  if /I "%%~A"=="--projects" set HAS_TARGET=1
)

if "!HAS_RUN!"=="1" if "!HAS_TARGET!"=="0" (
  "%JAVA_CMD%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" org.apache.maven.wrapper.MavenWrapperMain -f "%BASE_DIR%\modules\app-api\pom.xml" %*
) else (
  "%JAVA_CMD%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
)
endlocal
