Requirements:
  at least JDK 1.6

  To compile and run the movie package you need the Xuggler libraries
  (see http://www.xuggle.com/xuggler/).

Setup:
  - create a folder with the contents of the installation archive
  - set the environment variable JAVA_HOME accordingly.
  - set the environment variable MANDEL_HOME to the installation folder.

  - the file .mandtool in the installation folder contains all
    necessary settings

Calling:
  The bin folder contains the startup script mand.bat
  - to call the GUI:  mand
  - to call the calculation server: mand -c Mand -s
  - to call any mandel command use mand -c <cmd> <options>

  As long as the database localized in MANDEL_HOME is used the commands
  can be called from any folder. If you want to use another database
  on your machine the the cammand should be started from the
  appropriate root folder. For the UI, the root folder can be passed
  as argument to the explorer command.

Native GMP Support:
  - Only available for MS Windows 32 bit platforms, not for 64 bit.
  - Please install the MS Redistributable Package.
  - You can verify, whether the DLL works correctly with the command
       bin\mand -c com.mandelsoft.mand.MandIter
    It should not provide an error and notify, that the DLL is used.

NEW: Support of superfractalthing maths
  - The calculation tool (mand) now supports the option `-o` to enable
    the superfractalthing maths pixel iterator.
  - it replaces the GMP iterator (if present)
  - it completely based on double arithmetik plus some reference point
    iteration based on BigDecimals
  - it therefore runs on all platforms and does not require a native 
    library
  - it requires a huge amount of memory for large iteration depths,
    therefore the use of the 64 bit version of Java is strongly recommended.
