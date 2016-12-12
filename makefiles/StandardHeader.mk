#
# Global makefile header
#
# Prior to using,
#  $SHELL must be /bin/ksh
#  $PATH must include /bin:/usr/bin
#

all: default

DEBUG=-g

####################################################################
# Next Section: Directories, scripts, modes...
####################################################################
INSTALL=$(MOCADIR)/makefiles/install.sh

INCMODE=644
LIBMODE=755
APPMODE=755

INCDIR=$(MOCADIR)/include
LIBDIR=$(MOCADIR)/lib
BINDIR=$(MOCADIR)/bin

####################################################################
# Next Section: Platform specific definitions...
####################################################################

include $(MOCADIR)/makefiles/Config.mk

INCLUDES=-I$(MOCADIR)/include

LDFLAGS=$(LDOPTS) $(EXTRALDFLAGS) -L$(MOCADIR)/lib 
SHLDFLAGS=$(SHLDOPTS) $(EXTRALDFLAGS) -L$(MOCADIR)/lib 

CFLAGS=$(WARN) $(DEBUG) $(COPTIONS) $(DEFINES) $(INCLUDES) \
       $(EXTRACFLAGS) $(JAVACFLAGS)

CCFLAGS=$(WARN) $(DEBUG) $(CCOPTIONS) $(DEFINES) $(INCLUDES) \
	$(EXTRACCFLAGS) $(JAVACFLAGS)
