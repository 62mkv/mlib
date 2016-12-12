DIRS=../bin \
     ../include \
     ../javalib \
     ../lib \
     ../downloads \
     ../downloads/components 

DEVDIRS=../log \
        ../temp

default: $(DIRS) $(DEVDIRS) clean config

devdirs: $(DEVDIRS)

clean:
	@rm -f config.cache config.log config.status

config:
	@./configure 

$(DIRS):
	mkdir $@

$(DEVDIRS):
	mkdir $@
