include $(MOCADIR)/makefiles/LibraryLibs.mk

default: $(PREREQS) $(OFILES) $(LIBNAME)

clean:
	-@rm -f $(LIBNAME)$(SHLIBEXT) *.o *.a

INSTALL-SOFTWARE:
	$(INSTALL) $(LIBMODE) $(LIBDIR) $(INSTALL-FILES)

$(LIBDEPEND) dummy::
	-@for i in `ls $(MOCADIR)/src/libsrc/$@/*.o`; do \
		rm -f `basename $$i`; \
	done
	ln -sf $(MOCADIR)/src/libsrc/$@/*.o .

$(OFILES):

$(LIBNAME) dummy:: $(LIBDEPEND) 
	$(LD) $(SHLDFLAGS) $(OFILES) $(LIBS) -lc -o $(LIBNAME)$(SHLIBEXT)
