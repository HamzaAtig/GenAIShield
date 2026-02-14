@echo off

echo Creating package structure for GenAIShield...

REM Base paths
set CORE=modules\app-core\src\main\java\org\hat\genaishield\core
set INFRA=modules\app-infra\src\main\java\org\hat\genaishield\infra
set API=modules\app-api\src\main\java\org\hat\genaishield\api
set TESTS=modules\app-tests\src\test\java\org\hat\genaishield\tests

REM CORE packages
mkdir %CORE%\domain
mkdir %CORE%\ports\in
mkdir %CORE%\ports\out
mkdir %CORE%\usecase
mkdir %CORE%\policy

REM INFRA packages
mkdir %INFRA%\config
mkdir %INFRA%\adapters\in
mkdir %INFRA%\adapters\out
mkdir %INFRA%\security

REM API packages
mkdir %API%\controller
mkdir %API%\dto
mkdir %API%\mapper
mkdir %API%\error

REM TEST packages
mkdir %TESTS%\integration
mkdir %TESTS%\security
mkdir %TESTS%\redteam

echo Package structure created successfully.
pause
