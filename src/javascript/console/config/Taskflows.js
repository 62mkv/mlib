RP.registerTaskflow({  
    name: "moca.console.taskflow",
    title: "Menu",
    tasks:[{
        appId: "moca.console.app",
        id: "sessions",
        taskId: "sessions"
    }, {
        appId: "moca.console.app",
        id: "sessionKeys",
        taskId: "sessionKeys"
    }, {
        appId: "moca.console.app",
        id: "nativeProcesses",
        taskId: "nativeProcesses"
    }, {
        appId: "moca.console.app",
        id: "databaseConnections",
        taskId: "databaseConnections"
    }, {
        appId: "moca.console.app",
        id: "jobs",
        taskId: "jobs"
    }, {
        appId: "moca.console.app",
        id: "tasks",
        taskId: "tasks"
    }, {
        appId: "moca.console.app",
        id: "jobHistory",
        taskId: "jobHistory",
        widgetCfg: {
            id: "jobHistoryWidget"
        }
    }, {
        appId: "moca.console.app",
        id: "taskHistory",
        taskId: "taskHistory",
        widgetCfg: {
            id: "taskHistoryWidget"
        }
    }, {
        appId: "moca.console.app",
        id: "asyncExecutor",
        taskId: "asyncExecutor"
    }, {
        appId: "moca.console.app",
        id: "clusterAsyncExecutor",
        taskId: "clusterAsyncExecutor",
         // This is defined so we can later extract the id using 
        // Ext.getComponent to hide this widget if clustering is not
        // currently configured
        widgetCfg: {
            id: "clusteringAsyncWidget"
        }
    }, {
        appId: "moca.console.app",
        id: "registry",
        taskId: "registry"
    }, {
        appId: "moca.console.app",
        id: "componentLibraries",
        taskId: "componentLibraries"
    }, {
        appId: "moca.console.app",
        id: "environmentVariables",
        taskId: "environmentVariables"
    }, {
        appId: "moca.console.app",
        id: "systemProperties",
        taskId: "systemProperties"
    }, {
        appId: "moca.console.app",
        id: "resourceUsage",
        taskId: "resourceUsage"
    }, {
        appId: "moca.console.app",
        id: "commandProfile",
        taskId: "commandProfile"
    }, {
        appId: "moca.console.app",
        id: "logFiles",
        taskId: "logFiles"
    }, {
        appId: "moca.console.app",
        id: "clustering",
        taskId: "clustering",
        // This is defined so we can later extract the id using 
        // Ext.getComponent to hide this widget if clustering is not
        // currently configured
        widgetCfg: {
            id: "clusteringWidget"
        }
    }],
    listeners: {},
    uiConfig: {}
});
 
RP.registerTask([{
    appId: "moca.console.app",
    taskId: "sessions",
    widgetXtype: "moca.console.sessions.xtype"
}, {
    appId: "moca.console.app",
    taskId: "databaseConnections",
    widgetXtype: "moca.console.dbconns.xtype"
}, {
    appId: "moca.console.app",
    taskId: "jobs",
    widgetXtype: "moca.console.jobs.xtype"
}, {
    appId: "moca.console.app",
    taskId: "tasks",
    widgetXtype: "moca.console.tasks.xtype"
}, {
    appId: "moca.console.app",
    taskId: "asyncExecutor",
    widgetXtype: "moca.console.asyncexecutor.xtype"
}, {
    appId: "moca.console.app",
    taskId: "clusterAsyncExecutor",
    widgetXtype: "moca.console.clusterasyncexecutor.xtype"
}, {
    appId: "moca.console.app",
    taskId: "registry",
    widgetXtype: "moca.console.registry.xtype"
}, {
    appId: "moca.console.app",
    taskId: "componentLibraries",
    widgetXtype: "moca.console.complibs.xtype"
}, {
    appId: "moca.console.app",
    taskId: "environmentVariables",
    widgetXtype: "moca.console.envvars.xtype"
}, {
    appId: "moca.console.app",
    taskId: "systemProperties",
    widgetXtype: "moca.console.sysprops.xtype"
}, {
    appId: "moca.console.app",
    taskId: "resourceUsage",
    widgetXtype: "moca.console.resource.xtype"
}, {
    appId: "moca.console.app",
    taskId: "sessionKeys",
    widgetXtype: "moca.console.sessionkeys.xtype"
}, {
    appId: "moca.console.app",
    taskId: "commandProfile",
    widgetXtype: "moca.console.cmdprofile.xtype"
}, {
    appId: "moca.console.app",
    taskId: "nativeProcesses",
    widgetXtype: "moca.console.nativeprocs.xtype"
}, {
    appId: "moca.console.app",
    taskId: "logFiles",
    widgetXtype: "moca.console.logfiles.xtype"
}, {
    appId: "moca.console.app",
    taskId: "clustering",
    widgetXtype: "moca.console.clustering.xtype"
}, {
    appId: "moca.console.app",
    taskId: "jobHistory",
    widgetXtype: "moca.console.jobhistory.xtype"
}, {
    appId: "moca.console.app",
    taskId: "taskHistory",
    widgetXtype: "moca.console.taskhistory.xtype"
}]);
