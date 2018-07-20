##START#########################################################################
#
# $URL: https://athena.redprairie.com/svn/prod/moca/branches/vs2005/config/Config.mk.win32-x86 $
# $Revision: 120417 $
# $Author: mlange $
#
# Description: Configure makefile for MOCA.
#
# $Copyright-Start$
#
# Copyright (c) 2002-2009
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

MOCA_PLATFORM = win32-x86

#
# Set the release flag.
#

RELEASE   = 2013.2.2.15
MOCAVER   = 20132215
MOCAREV   = 2
MOCAMAJOR = 2013
MOCAMINOR = 2

#
# Java Configuration
#

JAVACFLAGS = "-I$(JAVA_HOME)\include" "-I$(JAVA_HOME)\include\win32"
