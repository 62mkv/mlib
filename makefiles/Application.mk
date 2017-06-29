VPATH=.:$(MLIBDIR)/lib

include $(MLIBDIR)/makefiles/ApplicationLibs.mk

default: $(APPNAME)

clean:
	-@rm -f $(APPNAME) *.o

$(APPNAME): $(OFILES)
	$(CC) $(CFLAGS) $(LDFLAGS) $(OFILES) $(LIBS) $(LIBOPTS) -o $@

INSTALL-SOFTWARE:
	$(INSTALL) $(APPMODE) $(BINDIR) $(APPNAME)

