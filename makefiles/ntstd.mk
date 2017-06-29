###########################################################################
#
# Standard MOCA Makefile for Windows NT/MSVC environment.
#
###########################################################################

!include $(MLIBDIR)\makefiles\Config.mk

MOCAOBJDIR = $(MLIBDIR)\downloads\components
COMPCSVDIR = $(MLIBDIR)\db\data\load\base\safetoload\comp_ver


!ifndef NODEBUG
DEBUG=d
DEBUGFLAG=-Zi -Fd$(TARGET)
LINKDEBUG=-debug
!else
DEBUGFLAG=-O2
!endif

!ifdef MAPFILE
MAPFLAG=-map
!endif

!ifdef JAVA_HOME
JAVACFLAGS="-I$(JAVA_HOME)\include" "-I$(JAVA_HOME)\include\win32"
!endif

WARNFLAG=-W3

.SUFFIXES : .cpp

CFLAGS=-nologo -GF -MD -DWIN32 -DWIN32_LEAN_AND_MEAN $(JAVACFLAGS) $(WARNFLAG) $(DEBUGFLAG) $(EXTRACFLAGS) -I$(MLIBDIR)\include \
       
LINKFLAGS=-incremental:no -fixed:no -MACHINE:X86 $(LINKDEBUG) $(EXTRALINKFLAGS) $(MAPFLAG)

!ifndef NOMOCALIBS
MOCALIBS=MOCA.lib
!endif

STDLIBS=kernel32.lib user32.lib gdi32.lib advapi32.lib ws2_32.lib wininet.lib

all: $(TARGET).$(TARGETTYPE)

cut:
!ifdef VERSION_FILE
	perl $(MLIBDIR)\client\scripts\CppSetVersion.pl $(VERSION_FILE) $(MOCAMAJOR) $(MOCAMINOR) $(MOCAREV)
	perl $(MLIBDIR)\client\scripts\CppUpdateProgIdText.pl $(C_RGS_FILE)
!endif

fix: $(FIXTARGET).$(FIXTARGETTYPE)

$(FIXTARGET).$(FIXTARGETTYPE): $(FIXDEPENDS)
!ifdef VERSION_FILE
	echo Updating $(TARGET).$(TARGETTYPE)...
	@perl $(MLIBDIR)\client\scripts\CppBumpver.pl $(VERSION_FILE) 
	@perl $(MLIBDIR)\client\scripts\CppUpdateProgIdText.pl $(C_RGS_FILE)
!endif

clean:
	-@del $(TARGET).exe $(TARGET).dll $(TARGET).lib $(TARGET)-static.lib
	-@del $(TARGET).exp *.obj *.pdb *.ilk *.manifest
!if "$(TEMPFILES)" != ""
	-del $(TEMPFILES)
!endif

hinstall:

install: all hinstall i-install $(POST_INSTALL)

i-install:
!ifndef NOINSTALL
	@perl $(MLIBDIR)\scripts\install.pl $(TARGET).$(TARGETTYPE) $(MLIBDIR)\bin
	@perl $(MLIBDIR)\scripts\install.pl $(TARGET).pdb $(MLIBDIR)\bin
!endif
!if "$(TARGETTYPE)" != "exe"
	@perl $(MLIBDIR)\scripts\install.pl $(TARGET).lib $(MLIBDIR)\lib
!if "$(MAKESTATIC)" == "yes"
	@perl $(MLIBDIR)\scripts\install.pl $(TARGET)-static.lib $(MLIBDIR)\lib
!endif
!endif
!ifdef VERSION_FILE
	@perl $(MLIBDIR)\client\scripts\CppCompver.pl $(VERSION_FILE) $(C_RGS_FILE) $(TARGET) > $(TARGET).csv
	@$(INSTALL) $(TARGET).$(TARGETTYPE) $(MOCAOBJDIR)
	@$(INSTALL) $(TARGET).csv $(COMPCSVDIR)
!endif



hinstall: $(IFILES)
!ifdef IFILES
	@$(INSTALL) $** $(MLIBDIR)\include
!endif

$(TARGET).lib: $(PREREQS) $(OFILES)
	lib -nologo -out:$@ $(OFILES)

$(TARGET).exe: $(PREREQS) $(OFILES) $(LIBDEPEND) $(VBP) $(FORMS)
!ifdef VBP
	vb6 -make -out make.log $(TARGET)
!else
	$(CC) $(CFLAGS) $(OFILES) -link -libpath:$(MLIBDIR)\lib \
	$(STDLIBS) $(MOCALIBS) $(EXTRALIBS) $(LINKFLAGS) -out:$@ 
	mt -nologo -manifest $(TARGET).exe.manifest -outputresource:$(TARGET).exe;#1
!endif

$(TARGET).dll: $(PREREQS) $(OFILES) $(LIBDEPEND) $(DEFFILE) makestatic
	$(CC) $(CFLAGS) -LD$(DEBUG) $(DEFFILE) $(OFILES) -link \
	-libpath:$(MLIBDIR)\lib $(STDLIBS) \
	$(EXTRALIBS) $(LINKFLAGS) -dll -implib:$*.lib -out:$@
	mt -nologo -manifest $(TARGET).dll.manifest -outputresource:$(TARGET).dll;#2

makestatic: force
!if "$(MAKESTATIC)" == "yes"
	lib -nologo -out:$(TARGET)-static.lib $(OFILES)
!endif

.c.obj:
	$(CC) $(CFLAGS) -c $*.c -Fo$@

.cpp.obj:
	$(CC) $(CFLAGS) -c $*.cpp -Fo$@

force:
