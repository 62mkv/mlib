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
INSTALL=$(MLIBDIR)/makefiles/install.sh

INCMODE=644
LIBMODE=755
APPMODE=755

INCDIR=$(MLIBDIR)/include
LIBDIR=$(MLIBDIR)/lib
BINDIR=$(MLIBDIR)/bin

####################################################################
# Next Section: Platform specific definitions...
####################################################################

include $(MLIBDIR)/makefiles/Config.mk

INCLUDES=-I$(MLIBDIR)/include

LDFLAGS=$(LDOPTS) $(EXTRALDFLAGS) -L$(MLIBDIR)/lib 
SHLDFLAGS=$(SHLDOPTS) $(EXTRALDFLAGS) -L$(MLIBDIR)/lib 

CFLAGS=$(WARN) $(DEBUG) $(COPTIONS) $(DEFINES) $(INCLUDES) \
       $(EXTRACFLAGS) $(JAVACFLAGS)

CCFLAGS=$(WARN) $(DEBUG) $(CCOPTIONS) $(DEFINES) $(INCLUDES) \
	$(EXTRACCFLAGS) $(JAVACFLAGS)
