@echo off
set /p arg=�뽫��ǩ����apk���������:
cd %~dp0  
call java SignApk  platform.x509.pem platform.pk8 %arg% %arg:~0,-4%Test.apk
echo ǩ���ɹ�!
pause