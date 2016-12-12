VPATH=.:$(MOCADIR)/lib

include $(MOCADIR)/makefiles/ApplicationLibs.mk

default: $(APPNAME)

clean:
	-@rm -f $(APPNAME) *.o

$(APPNAME): $(OFILES)
	$(CC) $(CFLAGS) $(LDFLAGS) $(OFILES) $(LIBS) $(LIBOPTS) -o $@

INSTALL-SOFTWARE:
	$(INSTALL) $(APPMODE) $(BINDIR) $(APPNAME)

