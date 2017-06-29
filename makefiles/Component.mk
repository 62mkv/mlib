include $(MLIBDIR)/makefiles/ComponentLibs.mk

default: $(LIBNAME)

clean:
	-@rm -f $(LIBNAME)$(SHLIBEXT) *.o

INSTALL-SOFTWARE:
	$(INSTALL) $(LIBMODE) $(LIBDIR) $(LIBNAME)$(SHLIBEXT)

$(LIBNAME): $(OFILES)
	$(LD) $(SHLDFLAGS) $(OFILES) $(LIBS) -lc -o $(LIBNAME)$(SHLIBEXT)
