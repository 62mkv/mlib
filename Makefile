all: install 

install: FRC
	cd ./src; $(MAKE) install

server: install

test: FRC
	ant test
	cd ./test/src; $(MAKE) install

config: FRC
	cd ./config; $(MAKE) 

clean: FRC
	cd ./config; $(MAKE) clean
	cd ./src; $(MAKE) clean
	cd ./test/src; $(MAKE) clean

superclean: clean
	rm -f bin/*
	rm -f include/*
	rm -f lib/*
	rm -f javalib/*

rebuild: clean install

FRC:

help:
	@echo
	@echo "All tagging must be done on a Win32 platform."
	@echo
	@echo "Do a 'make help' on a Win32 platform and follow the steps."
	@echo
