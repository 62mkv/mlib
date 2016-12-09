include $(MOCADIR)/makefiles/StandardHeader.mk

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

include $(MOCADIR)/makefiles/Library.mk
include $(MOCADIR)/makefiles/StandardFooter.mk
