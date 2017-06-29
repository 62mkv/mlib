include $(MLIBDIR)/makefiles/StandardHeader.mk

LIBNAME=libMOCA

OFILES=*.o

LIBDEPEND=dblib \
	  evtlib \
	  jnilib \
	  madlib \
	  mcclib \
	  mislib \
	  mxmllib \
	  oslib \
	  sqllib \
	  srvlib 

INSTALL-FILES=$(LIBNAME)$(SHLIBEXT)

EXTRALIBS=$(ICONVLIB) $(JAVA_VMLIB) -lm -lpthread

include $(MLIBDIR)/makefiles/Library.mk
include $(MLIBDIR)/makefiles/StandardFooter.mk
