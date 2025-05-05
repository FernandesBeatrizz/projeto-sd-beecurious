@echo off
javac -cp "./main/jsoup-1.19.1.jar" main/java/search/*.java
if %errorlevel% equ 0 (
    echo Compilação concluída com sucesso!
) else (
    echo Erro durante a compilação.
)
pause
