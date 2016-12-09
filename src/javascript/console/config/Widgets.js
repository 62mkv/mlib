// Sessions
Ext.define("RP.Moca.SessionsWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Sessions';
        this.uiTitle = this.headerText;
        this.itemId = "sessionsWidget";

        this.callParent(arguments);
    },

    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Sessions.SessionsViewer({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.sessions.xtype",
    classRef: RP.Moca.SessionsWidget,
    paramArray: []
});
 
// Native Processes
Ext.define("RP.Moca.NativeProcessesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Native Processes';
        this.uiTitle = this.headerText;
        this.itemId = "nativeprocessesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.NativeProcesses.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.nativeprocs.xtype",
    classRef: RP.Moca.NativeProcessesWidget,
    paramArray: []
});
 
// Database Connections
Ext.define("RP.Moca.DatabaseConnectionsWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Database Connections';
        this.uiTitle = this.headerText;
        this.itemId = "dbconnsWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.DatabaseConnections.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.dbconns.xtype",
    classRef: RP.Moca.DatabaseConnectionsWidget,
    paramArray: []
});
 
// Jobs
Ext.define("RP.Moca.JobsWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Jobs';
        this.uiTitle = this.headerText;
        this.itemId = "jobsWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Jobs.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.jobs.xtype",
    classRef: RP.Moca.JobsWidget,
    paramArray: []
});
 
// Tasks
Ext.define("RP.Moca.TasksWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Tasks';
        this.uiTitle = this.headerText;
        this.itemId = "tasksWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Tasks.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.tasks.xtype",
    classRef: RP.Moca.TasksWidget,
    paramArray: []
});

// Async Executor
Ext.define("RP.Moca.AsyncExecutorWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget", 
 
    initComponent: function() {
        this.headerText = 'Asynchronous Executor';
        this.uiTitle = this.headerText;
        this.itemId = "asyncexecutorWidget";
       
        RP.Moca.AsyncExecutorWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.AsyncExecutor.View({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.asyncexecutor.xtype",
    classRef: RP.Moca.AsyncExecutorWidget,
    paramArray: []
});

// Cluster Async Executor
Ext.define("RP.Moca.ClusterAsyncExecutorWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget", 
 
    initComponent: function() {
        this.headerText = 'Cluster Async Executor';
        this.uiTitle = this.headerText;
        this.itemId = "clusterasyncexecutorWidget";
       
        RP.Moca.ClusterAsyncExecutorWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.ClusterAsyncExecutor.View({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.clusterasyncexecutor.xtype",
    classRef: RP.Moca.ClusterAsyncExecutorWidget,
    paramArray: []
});

// Registry
Ext.define("RP.Moca.RegistryWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Registry';
        this.uiTitle = this.headerText;
        this.itemId = "registryWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Registry.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.registry.xtype",
    classRef: RP.Moca.RegistryWidget,
    paramArray: []
});
 
// Component Libraries
Ext.define("RP.Moca.ComponentLibrariesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Component Libraries';
        this.uiTitle = this.headerText;
        this.itemId = "componentLibrariesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.ComponentLibraries.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.complibs.xtype",
    classRef: RP.Moca.ComponentLibrariesWidget,
    paramArray: []
});
 
// Environment Variables
Ext.define("RP.Moca.EnvironmentVariablesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Environment Variables';
        this.uiTitle = this.headerText;
        this.itemId = "environmentVariablesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.EnvironmentVariables.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.envvars.xtype",
    classRef: RP.Moca.EnvironmentVariablesWidget,
    paramArray: []
});
 
// System Properties
Ext.define("RP.Moca.SystemPropertiesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'System Properties';
        this.uiTitle = this.headerText;
        this.itemId = "systemPropertiesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.SystemProperties.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.sysprops.xtype",
    classRef: RP.Moca.SystemPropertiesWidget,
    paramArray: []
});
 
// Resource Usage
Ext.define("RP.Moca.ResourceUsageWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Resource Usage';
        this.uiTitle = this.headerText;
        this.itemId = "resourceUsageWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.ResourceUsage.View({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.resource.xtype",
    classRef: RP.Moca.ResourceUsageWidget,
    paramArray: []
});
 
// Session Keys
Ext.define("RP.Moca.SessionKeysWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Connected Users';
        this.uiTitle = this.headerText;
        this.itemId = "sessionKeysWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.SessionKeys.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.sessionkeys.xtype",
    classRef: RP.Moca.SessionKeysWidget,
    paramArray: []
});
 
// Command Profile
Ext.define("RP.Moca.CommandProfileWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Command Profile';
        this.uiTitle = this.headerText;
        this.itemId = "commandProfileWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.CommandProfile.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.cmdprofile.xtype",
    classRef: RP.Moca.CommandProfileWidget,
    paramArray: []
});
 
// Log Files
Ext.define("RP.Moca.LogFilesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Log Files';
        this.uiTitle = this.headerText;
        this.itemId = "logFilesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.LogFiles.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.logfiles.xtype",
    classRef: RP.Moca.LogFilesWidget,
    paramArray: []
});

// Clustering
Ext.define("RP.Moca.ClusteringWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Clustering';
        this.uiTitle = this.headerText;
        this.itemId = "clusteringWidget";
       
        RP.Moca.ClusteringWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Clustering.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.clustering.xtype",
    classRef: RP.Moca.ClusteringWidget,
    paramArray: []
});

// Job History
Ext.define("RP.Moca.JobHistoryWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Job Execution History';
        this.uiTitle = this.headerText;
        this.itemId = "jobHistoryWidget";
       
        RP.Moca.JobHistoryWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.JobHistory.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.jobhistory.xtype",
    classRef: RP.Moca.JobHistoryWidget,
    paramArray: []
});

// Task History
Ext.define("RP.Moca.TaskHistoryWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Task Execution History';
        this.uiTitle = this.headerText;
        this.itemId = "taskHistoryWidget";
       
        RP.Moca.TaskHistoryWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.TaskHistory.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.taskhistory.xtype",
    classRef: RP.Moca.TaskHistoryWidget,
    paramArray: []
});
