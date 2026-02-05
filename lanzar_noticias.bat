@echo off
title Pipeline de Despliegue - Noticias App
setlocal

echo.
echo [1/3] Compilando Backend (API)...
cd /d "C:\Users\mucho\OneDrive\Documenten\Contenedores\noticias_app\noticias_api"
call mvn clean package
if %ERRORLEVEL% NEQ 0 (echo ERROR en API & pause & exit)

echo.
echo [2/3] Compilando Frontend (Web)...
cd /d "C:\Users\mucho\OneDrive\Documenten\Contenedores\noticias_app\noticias_web"
call mvn clean package
if %ERRORLEVEL% NEQ 0 (echo ERROR en WEB & pause & exit)

echo.
echo [3/3] Levantando Contenedores con Docker...
cd /d "C:\Users\mucho\OneDrive\Documenten\Contenedores\noticias_app"
docker compose up --build -d

echo.
echo ==========================================
echo    PROCESO COMPLETADO CON EXITO
echo ==========================================
pause