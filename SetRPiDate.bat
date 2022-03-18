echo on
SET theDateTime=sudo date -s "%DATE% %TIME%"
REM echo %theDateTime%
echo %theDateTime% >%TEMP%\DT.txt
REM type %TEMP%\DT.txt
"C:\Program Files\PuTTY\PUTTY.EXE" -ssh wpilibpi.local -l pi -pw raspberry -m %TEMP%\DT.txt
del %TEMP%\DT.txt
REM "C:\Program Files\PuTTY\plink.exe" -ssh -pw raspberry pi@wpilibpi.local %theDateTime%
REM plink.exe" -ssh -pw raspberry pi@wpilibpi.local 'sudo date -s ...........
