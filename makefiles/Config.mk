##START#########################################################################
#
# $URL: https://athena.redprairie.com/svn/prod/moca/branches/vs2005/config/Config.mk.linux-x86 $
# $Revision: 120395 $
# $Author: mlange $
#
# Description: Configure makefile for MOCA.
#
# $Copyright-Start$
#
# Copyright (c) 2007
# RedPrairie Corporation
# All Rights Reserved
#
# This software is furnished under a corporate license for use on a
# single computer system and can be copied (with inclusion of the
# above copyright) only for use on such a system.
#
# The information in this document is subject to change without notice
# and should not be construed as a commitment by RedPrairie Corporation.
#
# RedPrairie Corporation assumes no responsibility for the use of the
# software described in this document on equipment which has not been
# supplied or approved by RedPrairie Corporation.
#
# $Copyright-End$
#
##END###########################################################################

MOCA_PLATFORM = linux-x64

CC         = gcc
CCC        = g++
LD         = ld
WARN       =
DEFINES    =
COPTIONS   = -m32 -fPIC
CCOPTIONS  = -m32 -fPIC
JCOPTIONS  =
LIBOPTS    = -ldl -lrt
#LDOPTS     = -melf_i386
SHLDOPTS   = -melf_i386 -shared -E -lrt
SHLIBEXT   = .so

ICONVLIB =

RANLIB = ranlib

JAVACFLAGS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
