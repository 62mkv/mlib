include $(MOCADIR)/makefiles/StandardHeader.mk

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

include $(MOCADIR)/makefiles/Library.mk
include $(MOCADIR)/makefiles/StandardFooter.mk

results.o: results.c results.h

native.o: native.c native.h

javalib.o: javalib.c

results.h: $(MOCADIR)/lib/moca-server.jar
	$(JAVA_HOME)/bin/javah -classpath "$(MOCADIR)/build/core/classes:$(MOCADIR)/build/native/classes:$(MOCADIR)/build/server/classes:$(MOCADIR)/lib/moca-core.jar:$(MOCADIR)/lib/moca-native.jar:$(MOCADIR)/lib/moca-server.jar" -jni -o results.h com.redprairie.moca.server.legacy.WrappedResults

native.h: $(MOCADIR)/lib/moca-server.jar
	$(JAVA_HOME)/bin/javah -classpath "$(MOCADIR)/build/core/classes:$(MOCADIR)/build/native/classes:$(MOCADIR)/build/server/classes:$(MOCADIR)/lib/moca-core.jar:$(MOCADIR)/lib/moca-native.jar:$(MOCADIR)/lib/moca-server.jar" -jni -o native.h com.redprairie.moca.server.legacy.InternalNativeProcess
