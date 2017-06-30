include $(MLIBDIR)/makefiles/StandardHeader.mk

OFILES=argmap.o \
       dbcalls.o \
       extract.o \
       initialize.o \
       jnilib.o \
       mcccalls.o \
       miscalls.o \
       native.o \
       oscalls.o \
       results.o \
       server.o \
       srvcalls.o 

EXTRACFLAGS=$(JCOPTIONS)

include $(MLIBDIR)/makefiles/Library.mk
include $(MLIBDIR)/makefiles/StandardFooter.mk

results.o: results.c results.h

native.o: native.c native.h

javalib.o: javalib.c

results.h: $(MLIBDIR)/target/mlib.jar
	$(JAVA_HOME)/bin/javah -classpath "$(MLIBDIR)/build/core/classes:$(MLIBDIR)/build/native/classes:$(MLIBDIR)/build/server/classes:$(MLIBDIR)/target/mlib.jar" -jni -o results.h com.sam.moca.server.legacy.WrappedResults

native.h: $(MLIBDIR)/target/mlib.jar
	$(JAVA_HOME)/bin/javah -classpath "$(MLIBDIR)/build/core/classes:$(MLIBDIR)/build/native/classes:$(MLIBDIR)/build/server/classes:$(MLIBDIR)/target/mlib.jar" -jni -o native.h com.sam.moca.server.legacy.InternalNativeProcess
