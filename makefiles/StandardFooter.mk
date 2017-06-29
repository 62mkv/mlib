debug: all

install: all INSTALL-SOFTWARE

dinstall: all INSTALL-SOFTWARE

nodebug:
	$(MAKE) DEBUG=-O

hinstall: $(IFILES)
	@if [ -n "$(IFILES)" ]; then $(INSTALL) $(INCMODE) $(MLIBDIR)/include $?; fi

.SUFFIXES : .cpp

.cpp.o:
	$(CCC) $(CCFLAGS) -c $<
