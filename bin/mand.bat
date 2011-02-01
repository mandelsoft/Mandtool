@echo on
if "%MANDEL_HOME%" == "" goto error1
if "%JAVA_HOME%" == "" goto error2
if "%*" == "" goto splash

set SPLASH=
goto cont

:splash
set SPLASH="-splash:%MANDEL_HOME%\bin\images\splash.png"

:cont
"%JAVA_HOME%\bin\java" %SPLASH% -Djava.library.path="%MANDEL_HOME%\lib\win" -Xmx1024M -classpath "%MANDEL_HOME%\lib\Mandelbrot.jar" com.mandelsoft.mand.tools.Cmd %*
 
goto end
 
:error1
echo Error: MANDEL_HOME not set 
goto end
 
:error2
echo Error: JAVA_HOME not set 
goto end

:end

 

