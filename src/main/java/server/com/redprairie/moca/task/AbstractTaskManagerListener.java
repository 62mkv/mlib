/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.task;

/**
 * Abstract TaskManagerListener which provides noop implementations of every
 * method on the {@link TaskManagerListener} interface. Subclasses should
 * implement the hook methods which they're interested in.
 */
public abstract class AbstractTaskManagerListener implements TaskManagerListener {

    @Override
    public void onStart(TaskManager manager) {}

    @Override
    public void onStop(TaskManager manager) {}

    @Override
    public void onRestart(TaskManager manager) {}

    @Override
    public void onTaskAdded(TaskManager manager, TaskDefinition task) {}

    @Override
    public void onTaskChanged(TaskManager manager, TaskDefinition oldTask,
                              TaskDefinition newTask) {}

    @Override
    public void onTaskRemoved(TaskManager manager, TaskDefinition task) {}

    @Override
    public void onTaskStarted(TaskManager manager, TaskDefinition task) {}

    @Override
    public void onTaskStopped(TaskManager manager, TaskDefinition task) {}

}
