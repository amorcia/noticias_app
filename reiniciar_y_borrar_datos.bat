@echo off
title Limpiar Datos y Reiniciar - Noticias App
echo.
echo ===================================================
echo   ATENCION: ESTO BORRARA TODOS LOS DATOS (BD)
echo ===================================================
echo.
echo Deteniendo contenedores...
docker compose down -v
echo.
echo Contenedores y volumenes eliminados.
echo Ahora se recrearan con el esquema sin tildes.
echo.
pause
echo Iniciando despliegue...
call lanzar_noticias.bat
