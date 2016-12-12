all: $(SUBDIR)

$(SUBDIR): FRC
	cd ./$@; $(MAKE) $(TARGET)

clean:
	$(MAKE) TARGET=clean

install: hinstall
	$(MAKE) TARGET=install

hinstall:
	$(MAKE) TARGET=hinstall

FRC:

