@echo off

cd %~dp0  
call javac SignApk.java
echo 重新生成成功!请再次点击singed.bat进行签名
pause