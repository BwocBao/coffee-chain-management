@echo off
setlocal EnableExtensions EnableDelayedExpansion
echo Starting Coffee Chain Backend...

if exist ".env" (
    echo Loading environment from .env
    for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
        set "env_key=%%A"
        set "env_value=%%B"
        if not "!env_key!"=="" if not "!env_key:~0,1!"=="#" set "!env_key!=!env_value!"
    )
) else (
    echo .env not found. Using application.yaml defaults and system environment.
)

mvn spring-boot:run
pause
endlocal
