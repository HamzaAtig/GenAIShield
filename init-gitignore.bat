@echo off
echo Creating .gitignore ...

(
echo # =========================
echo # Build / Maven
echo # =========================
echo target/
echo */target/
echo *.log
echo *.class
echo
echo # =========================
echo # IDE
echo # =========================
echo .idea/
echo *.iml
echo *.ipr
echo *.iws
echo .vscode/
echo
echo # =========================
echo # OS
echo # =========================
echo .DS_Store
echo Thumbs.db
echo
echo # =========================
echo # Environment variables
echo # =========================
echo .env
echo .env.*
echo !.env.example
echo
echo # =========================
echo # Logs
echo # =========================
echo logs/
echo *.log
echo
echo # =========================
echo # Docker
echo # =========================
echo docker-data/
echo
echo # =========================
echo # Testcontainers
echo # =========================
echo .testcontainers.properties
echo
echo # =========================
echo # Misc
echo # =========================
echo node_modules/
echo coverage/
echo build/
) > .gitignore

echo .gitignore created successfully.
pause
