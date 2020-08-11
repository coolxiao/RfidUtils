@echo off
set /p arg=请将欲签名的apk拉入该区域:
cd %~dp0  
call java SignApk  platform.x509.pem platform.pk8 %arg% %arg:~0,-4%Test.apk
echo 签名成功!
pause