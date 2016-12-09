/*
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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
//////////////////////
// ..\js\..\..\stashLibBeginLoad.js
//////////////////////
RP.globals = RP.globals || {};
RP.stash.api.beginLoadLib();
//////////////////////
// ..\js\CompatibilityLayer.js
//////////////////////
/**
 * This library should include any required backwards compatibility 
 * functionality to ensure that older versions of rpcommon are functional
 * without error.  These should be migrated out of the core at some point
 * once they are no longer supported in the released versions.
 */
 
function printStackTrace() {
    // this method was used in 3.3.16 and was provided by a third party
    // library (javascript-stackTrace) that was removed in version 3.3.17.
    // For more details see the JIRA issue, RPWEB-4232.
    return [];
}
//////////////////////
// ..\js\LoadLocalePacks.js
//////////////////////
// load the locale packs for the stash, both formats and actual messages
// this relies on the locale packs being defined before rpcore is loaded
Ext.onReady(function() {
    RP.locale.Dispatch.includeLocalePack("rpext");
    RP.locale.Dispatch.includeLocalePack("ext");
    RP.locale.Dispatch.includeLocalePack("components");
    RP.locale.Dispatch.includeLocalePack("exceptions");
    RP.locale.Dispatch.includeLocalePack("login");
    RP.locale.Dispatch.includeLocalePack("misc");
});
//////////////////////
// ..\js\core\ApplicationSites.js
//////////////////////
Ext.ns("RP.core");

/**
 * @class RP.core.ApplicationSites
 * @singleton
 *
 * Serves as an API for interacting with the user's available sites.
 */
RP.core.ApplicationSites = function() {
    var appMetaData = {};
    

    /**
     * Performs a case-insensitive lookup for an application object, returning
     * the first one found.
     * @param {String} applicationId
     * @return {Object/undefined} The application object if found, otherwise undefined.
     */
    function getApplication(applicationId) {
        for (var app in appMetaData) {
            if (app.toLowerCase() === applicationId.toLowerCase()) {
                return appMetaData[app];
            }
        }
        return undefined;
    }
    
    /**
     *
     * @param {String} applicationId The application id.
     * @param {String} siteId The site id of the client site you are trying to get.
     * @return {Object/null} The client site object if found, null otherwise.
     */
    function getClientSite(applicationId, siteId) {
        var application = appMetaData[applicationId];
        
        if (Ext.isArray(application)) {
            for (var i = 0; i < application.length; i++) {
                var clientSite = application[i];
                
                if (clientSite.siteId === siteId) {
                    return clientSite;
                }
            }
        }
        return null;
    }
    
    function compareString(stringA, stringB) {
        var a = stringA.toLowerCase();
        var b = stringB.toLowerCase();
        if (a < b) {
            return -1;
        }
        else if (a > b) {
            return 1;
        }
        return 0;
    }
    
    return {
        /**
         * Takes a JSON object of all applications with their configured sitenames
         * @param {Object} The application meta data information.
         */
        setApps: function(applicationMetaData) {
            appMetaData = applicationMetaData;
        },
        
        /**
         * Gets a sorted list of all the available sites.
         * @return {Array} The array of all site IDs.
         */
        getAllSiteIds: function() {
            var allSiteIds = [];
            var clientSites;
            
            // avoiding function-based iteration for performance
            for (var application in appMetaData) {
                clientSites = appMetaData[application];
                
                if (Ext.isArray(clientSites)) {
                    for (var i = 0; i < clientSites.length; i++) {
                        allSiteIds.push(clientSites[i].siteId);
                    }
                }
            }
            
            // split the sites into two groups (numbers and strings)
            return this.sortSiteIds(Ext.unique(allSiteIds));
        },
        
        sortSiteIds: function(uniqueSites) {
            var numberSites = [], stringSites = [];
            
            for (var j = 0; j < uniqueSites.length; j++) {
                var site = uniqueSites[j];
                
                if (Ext.isNumber(parseInt(site, 10))) {
                    numberSites.push(site);
                }
                else {
                    stringSites.push(site);
                }
            }
            
            // sort the groups in ascending order
            numberSites.sort(function(a, b) {
                var parsedA = parseInt(a, 10), parsedB = parseInt(b, 10);
                
                // kinda hacky... check if the parsed numbers are equal but the sources are different
                // and then give true numbers a lower order than sites that are only prefixed with a number
                // ex: "122" should come before "122-foo"
                if (parsedA === parsedB && a !== b) {
                    return compareString(a.replace(parsedA.toString(), ""), b.replace(parsedB.toString(), ""));
                }
                
                return parsedA - parsedB;
            });
            
            stringSites.sort(compareString);
            
            return numberSites.concat(stringSites);
        },
        
        /**
         * Gets the application-specific native site ID for the given siteId.
         * @param {String} applicationId
         * @param {String} siteId
         * @return {String} The native site ID, or null if not found.
         */
        getNativeId: function(applicationId, siteId) {
            var clientSite = getClientSite(applicationId, siteId);
            
            if (clientSite !== null) {
                return clientSite.nativeId;
            }
            return null;
        },
        
        /**
         * Builds a url to the Session Keep Alive service
         * used for legacy applications for managing a user's session lifetime.
         */
        getSessionKeepAliveUrl: function() {
            var pageUrl = RP.core.PageContext.getPageUrl();
            
            return pageUrl + this.buildDataServiceUrl("rp", "admin/sessionKeepAlive");
        },
        
        
        
        /**
         * Builds a full url to be used in a data service call
         * The built URL is as follows:
         * /data/<derivedSiteName>/<bean>?<params>
         * @param {String} appId The application id of the web service being called.
         * @param {String} bean The web service bean
         * @param {Object} params Parameters to be sent with the data service call
         * @return {String} url The url that is built up for the request.
         */
        buildDataServiceUrl: function(appId, bean, params) {
            var globalSiteId = RP.globals.getValue("SITEID");
            
            var serverName = RP.location.DEFAULT_NONE;
            var siteId = RP.location.DEFAULT_NONE;

            var application = getApplication(appId);

            if (Ext.isDefined(application)) {
                var clientSite = getClientSite(appId, globalSiteId);

                var potentialSiteId = RP.core.ApplicationSites.getNativeId(appId, globalSiteId);
                if (Ext.isDefined(potentialSiteId)) {
                    siteId = potentialSiteId;
                }

                if (clientSite !== null && siteId !== null) {
                    serverName = clientSite.server;
                }
            }

            params = params || {};
            
            var siteName = (serverName !== RP.location.DEFAULT_NONE ? serverName : globalSiteId);
            var url = RP.globals.getFullyQualifiedPath("PATH_TO_DATA_ROOT");
            // Reserved word "rp" for internal app requests, use that as our site name.
            if (appId === "rp") {
                url += appId;
            }
            // Site Name specified as a query parameter gets next precedence
            else if (params.siteName) {
                url += params.siteName;
            }
            // Append the derived site name.
            else {
                url += siteName;
            }
            
            url += "/" + bean;
            if (!params.siteId && siteId !== RP.location.DEFAULT_NONE) {
                params.siteId = siteId;
            }
            
            // added for backwards compat...RPWEB-4053
            var sundial = RP.core.Sundial || Sundial;
            if (sundial.hasOffset()) {
                params.demoDate = sundial.now().format("c");
            }
            
            url = Ext.urlAppend(url, Ext.urlEncode(params));
            return url;
        },
        
        /**
         * Builds a full url used to access the web folder
         * @param {String} appId The application id
         * @param {String} path The relative path to the resource.
         * @return {String} url The full url to the resource.
         */
        buildWebUrl: function(appId, path) {
            //If the path contains an intial "/" remove it because it will
            //be added in later
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }
            var url = RP.globals.paths.STATIC + appId + "/" + path;
            
            return url;
        }
    };
}();

// Shortcuts
RP.buildDataServiceUrl = RP.core.ApplicationSites.buildDataServiceUrl;
RP.buildWebUrl = RP.core.ApplicationSites.buildWebUrl;
RP.getSessionKeepAliveUrl = RP.core.ApplicationSites.getSessionKeepAliveUrl;
//////////////////////
// ..\js\core\PageContext.js
//////////////////////
Ext.ns("RP.core");

RP.core.PageContext = (function() {
  var CONTEXT_TOKEN = ":";
  
  // by default the browser should refresh on module context change.
  var refreshOnModuleContextChange = true;
  
  function getRootUrl() {
        var baseUrl = RP.globals.getPath("BASE_URL"); 
        if (!Ext.isEmpty(baseUrl)) {
            return baseUrl;
        }

        return String.format("{0}//{1}", 
            window.location.protocol, 
            window.location.host);
  }
  
  function getPageRootPath(moduleName, siteId) {
    // ensure the siteId is defaulted if not specified
    if (!Ext.isDefined(siteId)) {
        siteId = RP.globals.getValue("SITEID");
    }
    
    var siteIdStr = String.format("/{0}", encodeURIComponent(siteId));

    var rootPath = String.format("{0}{1}/{2}",
        RP.globals.getFullyQualifiedPath("PB_ROOT"),
        siteIdStr, 
        moduleName);
        
    if (rootPath.indexOf("http") !== 0) {
        rootPath = getRootUrl() + rootPath;
    }
    
    return rootPath; 
  }
  
  function setLocationToModule(moduleName, moduleContext) {
    var moduleContextStr = moduleContext? Ext.urlEncode(moduleContext) : "";
    RP.util.Helpers.redirect(String.format("{0}#/{1}", getPageRootPath(moduleName), moduleContextStr));
  }
  
  function getValidContextProperties(obj) {
    var ret = {};
    
    // Retrieve only properties that are primitive value types...
    Ext.iterate(obj, function(key, value) {
      if (Ext.isPrimitive(value)) {
        ret[key] = value;
      }
    });
    
    return ret;
  }
  
  function getContextAsUrl(moduleContext, taskflowContext, taskContext) {
    var moduleCtx = moduleContext? Ext.urlEncode(getValidContextProperties(moduleContext)) : "";
    var taskflowCtx = taskflowContext? Ext.urlEncode(getValidContextProperties(taskflowContext)) : "";
    var taskCtx = taskContext? Ext.urlEncode(getValidContextProperties(taskContext)) : "";
    
    if (!Ext.isEmpty(moduleCtx) || !Ext.isEmpty(taskflowCtx) || !Ext.isEmpty(taskCtx)) {
      return String.format("/{0}/{1}/{2}", moduleCtx, taskflowCtx, taskCtx);
    }
    // if there is no context set don't fake one.
    return "";
  }
  
  function parseUrl(hash) {
    // Format of 'hash': <taskflow_name>:<task_id>/<module_context>/<taskflow_context>/<task_context>
    
    if(!Ext.isDefined(hash) || hash === null) {
        hash = "";
    }
    
    var parts = hash.split("/");
    var tfNav = parts[0].split(CONTEXT_TOKEN);
    var moduleCtx = {}, taskflowCtx = {}, taskCtx = {};
    
    // Parse module/taskflow/task context out of 2nd part...
    if (!Ext.isEmpty(parts[1])) {
      moduleCtx = Ext.urlDecode(parts[1]);
    }
    
    if (parts.length > 2) {
      taskflowCtx = Ext.urlDecode(parts[2]);
    }
    
    if (parts.length > 3) {
      taskCtx = Ext.urlDecode(parts[3]);
    }
    
    return {
      tfName: tfNav[0] || "",
      taskID: tfNav[1] || "",
      moduleContext: moduleCtx,
      taskflowContext: taskflowCtx,
      taskContext: taskCtx
    };
  }
  
  function createUrlHash(taskflowName, taskName, 
          moduleContext, taskflowContext, taskContext) {
    
    // get the appropriate values to set the URL and default to empty string on null.
    var ctxUrl = getContextAsUrl(moduleContext, taskflowContext, taskContext) || "";
    taskName = taskName || "";
    taskflowName = taskflowName || "";

    return String.format("{0}{1}{2}{3}", taskflowName, CONTEXT_TOKEN, taskName, ctxUrl);
    
  }
  
  return {
      
    
    /**
     * Return whether or not the module should be reloaded when the module context changes. 
     */
    isRefreshOnModuleContextChange: function() {
        return refreshOnModuleContextChange;
    },
      
    /**
     * Converts a context object into a String representation (so it can be used as part
     * of a URL hash, for example)
     * @param {Object} ctx
     */
    encodeContextAsHashValue: function(ctx) {
      return Ext.urlEncode(getValidContextProperties(ctx));
    },
    
    /**
     * Parses a URL hash to get taskflow name, task ID, and various contexts.  Context values
     * are parsed to JSON objects.
     * @method
     * @param {String} hash
     * @return {Object} Object with the following properties: tfName, taskID, moduleContext, 
     * taskflowContext, and taskContext
     */
    parseHash: function(hash) {
      return parseUrl(hash);
    },
    
    /**
     * Get path to the page root (protocol://host/rp)
     * @return {String} The root url of the RPWEB instance.
     */
    getPageUrl: function() {
        return getRootUrl();
    },
    
    /**
     * Gets the currently active module name
     * @method
     * @return {String} The module name
     */
    getActiveModuleName: function() {
      var urlPathName = window.location.pathname;
      
      var urlPath = urlPathName.split("/");
      var moduleName = urlPath[urlPath.length - 1];
      
      if (moduleName !== "rp" && moduleName !== RP.globals.SITEID) {
        return moduleName;
      }
      
      return null;
    },
    
    /**
     * This should not be called directly.  PageBootstrapper calls this to determine the
     * module to load by inspecting the URL for the module name, and if not found,
     * sets it to the first sorted module.
     * @private
     * @method
     * @param {Object} moduleName
     */
    determineModule: function(moduleName) {
      // Load modules from module registry...
      var modules = RP.taskflow.ModuleRegistry.getAllSorted();
      
      var activeModuleName = this.getActiveModuleName();
      if (!moduleName && activeModuleName) {
        moduleName = activeModuleName;
      }
      // If module specified in URL is not specified or invalid, select the first one.
      else if (!moduleName && modules.length > 0) {
        moduleName = modules[0].name;
      }
      
      var activeModule = RP.taskflow.ModuleRegistry.get(moduleName);
      if(activeModule === undefined && modules.length > 0) {
        activeModule = RP.taskflow.ModuleRegistry.get(modules[0].name);
        var url = RP.core.PageContext.getPageURL(RP.globals.SITEID, modules[0].name);
        RP.util.Helpers.redirect(url);
      }
      RP.globals.CURRENT_MODULE = activeModule;
      return activeModuleName;
    },
    
    /**
     * Reloads a module
     * @param {String} moduleName
     * @param {Object} moduleContext Optional module context; if not specified and 
     * reloading the same module, it will preserve the existing module context
     */
    setActiveModule: function(moduleName, moduleContext) {
      var currentModuleName = RP.globals.CURRENT_MODULE.name;
      var activeModuleName = this.determineModule(moduleName);
      
      // If loading same module, and moduleContext is not specified,
      // preserve the module context.
      if (!moduleContext && 
          RP.globals.CURRENT_MODULE && 
          moduleName === currentModuleName &&
          RP.globals.CURRENT_HASH && RP.globals.CURRENT_HASH.moduleContext) {
        moduleContext = RP.globals.CURRENT_HASH.moduleContext;
      }
      
      if (Ext.isDefined(activeModuleName) && activeModuleName != moduleName) {
        setLocationToModule(moduleName, moduleContext);
      }
      else {
        RP.util.Helpers.reload(100);
      }
    },
    
    /**
     * Set the initial URL after authentication on login.
     * @param {Object} siteId The id of the default site
     * @param {Object} moduleName The first default module.
     */
    setInitialURL: function(siteId, moduleName) {
      RP.globals.SITEID = siteId;
      RP.location = {};
      RP.location.DEFAULT_NONE = 'NONE';
      RP.util.Helpers.redirect(getPageRootPath(moduleName, siteId));
    },
    
    /**
     * Returns the active taskflow name
     * @method
     * @return {String} The taskflow name
     */
    getActiveTaskflowName: function() {
      var urlTokens = window.location.hash.split(CONTEXT_TOKEN);
      
      if (urlTokens.length > 0) {
        return urlTokens[0].replace(/^#/, "");
      }
      
      return null;
    },
    
    /**
     * Returns the active task name
     * @method
     * @return {String} The task ID
     */
    getActiveTaskName: function() {
      var urlTokens = window.location.hash.split(CONTEXT_TOKEN);
      
      if (urlTokens.length > 1) {
        return urlTokens[1].replace(/\/.*$/,"");
      }
      
      return null;
    },
    
    /**
     * Returns the URL hash to use if you were to change the taskflow and/or task
     * in the current module
     * @param {String} taskflowName
     * @param {String} taskID
     * @param {Object} taskflowContext
     * @param {Object} taskContext
     * @return {String} the URL hash
     */
    getTaskHash: function(taskflowName, taskID, taskflowContext, taskContext) {
      // Get the taskflow and task context with the current module context...
      var currCtx = parseUrl(window.location.hash);
      var newHash = createUrlHash(taskflowName, taskID,
                currCtx.moduleContext, taskflowContext, taskContext);
                
      return newHash;
    },
    
    /**
     * Returns the current module context as specified in the current URL as a JSON object
     * @method
     * @return {Object} The module context
     */
    getModuleContext: function() {
      var currCtx = parseUrl(window.location.hash);
      return currCtx.moduleContext;
    },
    
    /**
     * This should not be called directly.  This is designed to be called once by
     * the page builder to set the module context at page load time
     * @method
     * @private
     * @param {Object} moduleContext
     */
    updateModuleContext: function(moduleContext, refreshBrowser) {
      if(!Ext.isEmpty(refreshBrowser)) {
          refreshOnModuleContextChange = refreshBrowser;
      }
      
      // Update the module context, but preserve the taskflow and task context...
      var currCtx = parseUrl(window.location.hash);
      var newHash = createUrlHash(currCtx.tfName, currCtx.taskID,
                moduleContext, currCtx.taskflowContext, currCtx.taskContext);
                
      Ext.History.add(newHash, true);
    },
    
    /**
     * This should not be called directly.  This is designed to be called once when
     * instantiating a taskflow.
     * @method
     * @private
     * @param {Object} taskflowContext
     */
    updateTaskflowContext: function(taskflowContext) {
      // Update the taskflow context, but preserve the module and task context...
      var currCtx = parseUrl(window.location.hash);
      var newHash = createUrlHash(currCtx.tfName, currCtx.taskID,
                currCtx.moduleContext, taskflowContext, currCtx.taskContext);
      
      Ext.History.add(newHash, true);
    },
    
    /**
     * Update the task context.
     * @method
     * @param {Object} taskContext The new task context
     */
    updateTaskContext: function(taskContext) {
      // Update the task context, but preserve the module and taskflow context...
      var currCtx = parseUrl(window.location.hash);
      var newHash = createUrlHash(currCtx.tfName, currCtx.taskID,
                currCtx.moduleContext, currCtx.taskflowContext, taskContext);
      
      if (newHash) {
        Ext.History.add(newHash, true);
      }
    },
    
    /**
     * Form the URL to access a specific task in a specific taskflow and module 
     * @param {String} siteId The site id
     * @param {String} moduleName The registered module name
     * @param {String} taskflowName The registered taskflow name
     * @param {String} taskName The registered task name
     * @param {Object} moduleCtx Optional module context
     * @param {Object} taskflowCtx Optional taskflow context
     * @param {Object} taskCtx Optional task context
     */
    getPageURL: function(siteId, moduleName, taskflowName, taskName, moduleCtx, taskflowCtx, taskCtx) {
      return String.format("{0}#{1}", 
                getPageRootPath(moduleName, siteId),
                createUrlHash(taskflowName, taskName, moduleCtx, taskflowCtx, taskCtx));
    },
    
    /**
     * Open a native browser window.
     * @param {Object} url The url the window should be set to.
     * @param {Object} name The name of the new window.
     * @param {Object} options Additional options for opening the window.
     */
    openWindow: function(url, name, options) {
      var opt = [];
      options = options || {};
      // If the name isn't provided generate a unique name.
      name = name || new Date().getTime().toString();
      
      // Convert the object into an array ["toolbar=1", "menubar=0", ....]
      Ext.iterate(options, function(key, value) {
        opt.push(String.format("{0}={1}", key, value ? 1 : 0));
      });
      
      // Open the new window with the options in a comma separated string
      return window.open(url, name, opt.toString());
    },
    
    /**
     * Redirects the page to the specified url.
     * 
     * @param {Object} url
     * @param {Object} millis (Optional) The number of milliseconds to wait
     */
    redirect: function(url, millis) {
      RP.util.Helpers.redirect(url, millis);
    }
  };
})();
//////////////////////
// ..\js\core\SessionSecurity.js
//////////////////////
Ext.ns("RP.upb");

/**
 * @namespace RP.core
 * @class RP.core.SessionSecurity
 *
 * Hooks all the important pieces of the security mechanisms to secure the user's session.
 */
RP.core.SessionSecurity = (function() {
    /**
     * Start the PageInactivityChecker.  This is the initial point of
     * firing the pollers that will trigger the various checks and dialogs
     * to warn the user of session inactivity, expiration and termination.
     */
    function startTimerFn() {
        RP.util.PageInactivityChecker.start(60 * RP.globals.getValue("INACTIVITY_TIMEOUT_MINUTES"));
    }
    
    /**
     * @private
     * Handle the inactivity warning when the user becomes inactive.
     */
    function inactivityWarningFn() {
        RP.event.AppEventManager.fire(RP.upb.AppEvents.Inactive, {});
        
        var dlg = new RP.ui.InactivityWarningDialog();
        
        RP.event.AppEventManager.register(RP.upb.AppEvents.ActiveAgain, startTimerFn);
        
        dlg.show();
    }
    
    /**
     * @private
     *
     * Display the session expired dialog or the external authentication expired dialog to
     * the user to force them to provide their credentials again.
     */
    function terminatedSession() {
        if (!Ext.getCmp("sessionExpiredDialog") && !Ext.getCmp("externalAuthenticationExpiredDialog")) {
            Ext.getBody().maskEx();
            
            var dialog;
            if (!RP.util.Helpers.isNativeLogin()) {
                dialog = new RP.ui.ExternalAuthenticationExpiredDialog();
            }
            else {
                dialog = new RP.ui.SessionExpiredDialog();
            }
            
            dialog.on("destroy", function() {
                Ext.getBody().unmask();
            });
            
            dialog.show();
        }
    }
    
    /** 
     * Clear window.onbeforeunload handler to ensure that the user
     * is not stopped and asked whether the leave the page.  The user's session
     * has terminated and cannot be recovered.
     */
    function clearDirtyPageChecks() {
        if (Ext.isFunction(window.onbeforeunload)) {
            window.onbeforeunload = null;
        }
    }
    
    // bootstrap the session security hooks.
    Ext.Ajax.on("sessionterminated", terminatedSession, this);
    Ext.Ajax.on("sessionterminated", clearDirtyPageChecks, this);
    
    if (Ext.isString(RP.globals.paths.REAUTHENTICATE) &&
    Ext.isNumber(parseInt(RP.globals.INACTIVITY_TIMEOUT_MINUTES, 10)) &&
    RP.globals.INACTIVITY_TIMEOUT_MINUTES > 0) {
    
        // hook up the page inactivity checker.
        RP.util.PageInactivityChecker.addHandler(inactivityWarningFn);
        startTimerFn();
    }
})();
//////////////////////
// ..\js\upb\AppEvents.js
//////////////////////
Ext.namespace("RP.upb");

//
// AppEvents - well-known event names enumeration.  NOTE: all events will be passed
// the event name as the first argument.
//
RP.upb.AppEvents = {
    //====================================================
    // Inactive event
    //====================================================
    // desc: user inactivity triggered (right before inactivity warning dialog appears)
    // arg1: event name
    // arg2: none
    Inactive: "rp.upb.inactive",

    //====================================================
    // Active Again event
    //====================================================
    // desc: user inactivity warning or reauthorize dialog dismissed
    // arg1: event name
    // arg2: none
    ActiveAgain: "rp.upb.activeAgain",

    //====================================================
    // Module Context Changed event
    //====================================================
    // desc: user inactivity warning or reauthorize dialog dismissed
    // arg1: event name
    // arg2: none
    ModuleContextChanged: "rp.ub.moduleCtxChanged",

    //====================================================
    // Before Session Expired event
    //====================================================
    // desc: occurs after the inactivity warning dialog box countdown ends
    //       but before the session is marked as requiring reauthentication
    // arg1: event name
    // arg2: none
    BeforeSessionExpired: "rp.upb.beforeSessionExpired",

    //====================================================
    // Session Expired event
    //====================================================
    // desc: occurs when the session expired dialog box appears
    // arg1: event name
    // arg2: none
    SessionExpired: "rp.upb.sessionExpired",

    //====================================================
    // Session Reauthenticated event
    //====================================================
    // desc: occurs when the session expired and the user reauthenticates
    // arg1: event name
    // arg2: none 
    SessionReauthenticated: "rp.upb.sessionReauthenticated",

    //====================================================
    // Session Keep Alive event
    //====================================================
    // desc: occurs when the session keep alive heartbeat is sent out
    // arg1: event name
    // arg2: none
    KeepAlive: "rp.upb.keepAlive",
    
    //====================================================
    // Session Keep Alive Success event
    //====================================================
    // desc: occurs when the session keep alive heartbeat request succeeds upon return
    // arg1: event name
    // arg2: none
    KeepAliveSuccess: "rp.upb.keepAliveSuccess",
    
    //====================================================
    // Session Keep Alive Failure event
    //====================================================
    // desc: occurs when the session keep alive heartbeat request fails upon return
    // arg1: event name
    // arg2: none
    KeepAliveFailure: "rp.upb.keepAliveFailure"
};
//////////////////////
// ..\js\upb\ChangeSiteWindow.js
//////////////////////
Ext.ns("RP.upb");

/**
 * @class RP.upb.ChangeSiteWindow
 * @extends Ext.Window
 *
 * The window used to change sites within a connection inside RPWEB.  This
 * window will automatically close itself if the user selects the 'Cancel'
 * button.  If a site is selected, the page will redirect or launch a new window.
 */
RP.upb.ChangeSiteWindow = Ext.extend(Ext.Window, {
    initComponent: function() {
        this.siteStore = this.createSiteStore();
        
        this.siteComboBox = this.createSiteComboBox();
        
        this.siteComboBox.setValue(RP.globals.SITEID);
        
        Ext.apply(this, {
            title: RP.getMessage("rp.common.changesite.FormTitle"),
            width: 450,
            draggable: false,
            resizable: false,
            closable: false,
            modal: true,
            padding: 20,
            items: [{
                xtype: "panel",
                layout: "centeredform",
                itemId: "form",
                bodyStyle: "background-color: #fff",
                border: false,
                items: [this.siteComboBox, {
                    xtype: "checkbox",
                    itemId: "chkNewWindow",
                    boxLabel: RP.getMessage("rp.common.changesite.OpenInNewWindow")
                }],
                buttonAlign: "center",
                buttons: [{
                    itemId: "btnSelect",
                    text: RP.getMessage("rp.common.changesite.SelectButton"),
                    handler: this.onSelect,
                    scope: this
                }, {
                    itemId: "btnCancel",
                    text: RP.getMessage("rp.common.changesite.CancelButton"),
                    handler: this.close,
                    scope: this
                }]
            }]
        });
        
        RP.upb.ChangeSiteWindow.superclass.initComponent.call(this);
    },
    
    /**
     * Creates the store used by the site combo box.
     */
    createSiteStore: function() {
        var sites = RP.core.ApplicationSites.getAllSiteIds();
        var data = [];
        
        Ext.each(sites, function(site) {
            data.push([site]);
        });
        
        return new Ext.data.ArrayStore({
            fields: ["siteId"],
            data: data
        });
    },
    
    /**
     * Creates the site combo box.
     */
    createSiteComboBox: function() {
        return new Ext.form.ComboBox({
            xtype: "combo",
            fieldLabel: RP.getMessage("rp.common.changesite.CurrentSite"),
            store: this.siteStore,
            displayField: "siteId",
            valueField: "siteId",
            itemId: "currentSite",
            typeAhead: true,
            mode: "local",
            forceSelection: true,
            triggerAction: "all",
            selectOnFocus: true,
            labelStyle: "font-weight: bold",
            listeners: {
                specialkey: this.onSpecialKey,
                scope: this
            }
        });
    },
    
    /**
     * Performs a site change.
     * @param {String} siteId The new siteId.
     * @param {Boolean} newWindow True to open a new window.
     */
    changeSite: function(siteId, newWindow) {
        var url = RP.core.PageContext.getPageURL(siteId, RP.core.PageContext.getActiveModuleName());
        
        if (newWindow === true) {
            RP.core.PageContext.openWindow(url);
        }
        else {
            RP.util.Helpers.redirect(url);
        }
        
        this.close();
    },
    
    /**
     * Handler for when the 'Select' button is clicked.
     * @private
     */
    onSelect: function() {
        var siteId = this.siteComboBox.getValue();
        var newWindow = this.getComponent("form").getComponent("chkNewWindow").getValue();
        
        this.changeSite(siteId, newWindow);
    },
    
    /**
     * Shortcut for using the 'ENTER' key inside the site combo box to call onSelect.
     * @param {Ext.form.Field} field
     * @param {Ext.EventObject} e
     * @private
     */
    onSpecialKey: function(field, e) {
        if (e.getKey() == e.ENTER && field.findRecord(field.valueField, field.getValue())) {
            e.stopEvent();
            this.onSelect();
        }
    }
});
//////////////////////
// ..\js\upb\FormTitle.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.upb");

/**
 * @namespace RP.upb
 * @class FormTitle
 * 
 * Sets the page title for the form in the viewport. Called by HeaderPanel.
 */

/**
 * FormTitle Constuctor. Takes a form configuration object and appends the titleText to the form's title.
 * @method FormTitle
 * @param {Object} config The form's configuration.
 */
RP.upb.FormTitle = function(config) {
  if (!config.titleText) {
    return;
  }

  function createHTML(title) {
    return String.format('<div class="rp-header-page-title"><div class="rp-h2bullet"></div><div id="__pageTitle">{0}</div></div>',
                title);
  }

  var html = createHTML(config.titleText);

  RP.upb.FormTitle.superclass.constructor.call(this, {
      border: false,
      html: html
  });

  this.setTitleHTML = function(html)  {
    var lbl = Ext.get("__pageTitle");
    if (lbl) {
        lbl.dom.innerHTML = html;
    }
    else {
      this.html = createHTML(html);
    }
  };
};

Ext.extend(RP.upb.FormTitle, Ext.Panel);
//////////////////////
// ..\js\upb\UnstableEnvironmentWindow.js
//////////////////////
Ext.ns("RP.upb");

/**
 * A Window that shows a message indicating the environment is unstable.
 *
 * This class is a wrapper around the message box for the sake of the namespacing
 * the unstable window. Also, it's used in multiple locations.
 * @singleton
 */
RP.upb.UnstableEnvironmentWindow = Ext.extend(Ext.Component, {
    id: "_rp-unstable-environment-window",

    show: function() {
        Ext.MessageBox.show({
            title: RP.getMessage("rp.common.misc.UnstableEnvironmentMessageTitle"), 
            msg: RP.getMessage("rp.common.misc.UnstableEnvironmentMessageDescription"),
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.WARNING
        });
    },

    isVisible: function() {
        if (Ext.MessageBox.getDialog().title === RP.getMessage("rp.common.misc.UnstableEnvironmentMessageTitle")) {
            return Ext.MessageBox.isVisible();
        }
    },

    close: function() {
        if (Ext.MessageBox.getDialog().title === RP.getMessage("rp.common.misc.UnstableEnvironmentMessageTitle")) {
            Ext.MessageBox.hide();
        }
    }
});

RP.upb.UnstableEnvironmentWindow = new RP.upb.UnstableEnvironmentWindow();
//////////////////////
// ..\js\upb\HeaderPanel.js
//////////////////////
Ext.ns("RP.upb");

/**
 * @class RP.upb.HeaderPanel
 * @extends Ext.Panel
 */
RP.upb.HeaderPanel = Ext.extend(Ext.Panel, {

  initComponent: function() {
    var demoDateHtml = this._getDemoDateHtml();
    
    var html = 
      '<div class="rp-header">' +
        '<div class="rp-header-logo"></div>' +
        '<div class="rp-header-nav">' +
          '<div class="rp-header-nav-top">' +
          '</div>' +
          demoDateHtml +
        '</div>' +
      '</div>';
    
    this._formTitle = new RP.upb.FormTitle(this.initialConfig);
    
    var titleHtml = this._formTitle.html;
    var appHeader = new Ext.Panel({ border: false, html: html });
    var extraPanel = new Ext.Panel({ border: false, cls: "rp-form-title-extra", items: this.initialConfig.items });
    
    var exitLink;
    var exitLinkId = "_rpExitLink";
    
    if (RP.upb.PageBootstrapper.isNativeLogin() === false) {
      exitLink = new Ext.BoxComponent({
            autoEl: { tag: "a", href: RP.globals.getFullyQualifiedPath("LOGOUT"), html: RP.getMessage("rp.common.misc.Close") },
            cls: "rp-header-nav-link",
            id: exitLinkId
      });
    }
    else {
        exitLink = new Ext.BoxComponent({
            autoEl: { tag: "a", href: RP.globals.getFullyQualifiedPath("LOGOUT"), html: RP.getMessage("rp.common.misc.Logout") },
            cls: "rp-header-nav-link",
            id: exitLinkId
        });
    }
    
    var helpLink = this.createHelpLink();
    
    Ext.apply(this, {
      border: false,
      cls: "rp-header-outer",
      items: [appHeader, extraPanel, this._formTitle]
    });
    
    // add additional UI controls after component has finished rendering
    appHeader.on("afterrender", function() {
      
      this.displayHeaderNotification("_rp-headerNotification");
      this.addHeaderComponent(new RP.upb.UserComponent({
          externalAuthentication: this.externalAuthentication
      }));
      this.addHeaderComponent(this.getSiteComponent());
      this.addHeaderComponent(exitLink);
      this.addHeaderComponent(helpLink);
    }, this);
    
    RP.upb.HeaderPanel.superclass.initComponent.call(this);
  },
  
    /** 
     * Generate the demo date html to display below
     * the site/logout/help links.
     * @private 
     */
    _getDemoDateHtml: function() {
        var demoDateHtml = "";
    
        // added for backwards compat...RPWEB-4053
        var sundial = RP.core.Sundial || Sundial;
        if (sundial.hasOffset()) {
            var demoDateStr = sundial.now().formatDate(RP.core.Formats.Date.Long);
      
            demoDateHtml = 
                '<div class="rp-header-nav-bottom">' +
                '<span class="rp-header-demo-time">' + RP.getMessage("rp.common.misc.DemoDateLabel") + 
                demoDateStr + '</span>' + '</div>';
        }
        
        return demoDateHtml;
    },
  
  displayHeaderNotification: function(renderToId) {
      if (RP.globals.getValue("UNSTABLE") === "true") {
          this.addHeaderComponent(new RP.upb.HeaderNotification({id: renderToId}));
      }
  },
  
  setPageTitle: function(titleHTML) {
    this._formTitle.setTitleHTML(titleHTML);
  },
  
  /**
   * Adds a separator and then renders the component into the HeaderPanel.
   * @param {Ext.Component} component A component to add to the HeaderPanel.
   */
  addHeaderComponent: function(component) {
    if (!Ext.isEmpty(component)) {
      var headerNav = Ext.DomQuery.selectNode(".rp-header .rp-header-nav-top");
      var separatorHtml = '<span class="rp-header-nav-separator">|</span>';
      
      if (Ext.DomQuery.selectNode(".rp-header .rp-header-nav-top:has(*)")) {
          Ext.DomHelper.insertHtml("beforeEnd", headerNav, separatorHtml);
      }
      
      component.render(headerNav);
    }
  },
  
  createHelpLink: function() {
      var mainHelpURL = RP.globals.getPath('BASE_URL') + RP.globals.getValue('PATH_TO_ROOT') + "web/refs/help/main.htm";
      RP.help.HelpRegistry.setDefaultHelpURL(mainHelpURL);
      
      return RP.help.RoboHelpLink.createHelpLink(
        { cls: "rp-header-nav-link" },
        mainHelpURL,                                   // Main URL
        RP.getMessage("rp.common.misc.HelpLinkLabel"), // Help hyperlink label
        "rphelpwindow",                                // window target name
        this.helpMapper // Help mapper function
      );
  },
  
  getSiteComponent: function() {
    var sites = RP.core.ApplicationSites.getAllSiteIds();
    var siteId = RP.globals.getValue("SITEID");
    
    if (!Ext.isEmpty(sites)) {
      if (sites.length > 1) {
        return new RP.ui.Hyperlink({
          text: siteId,
          cls: "rp-header-nav-link",
          scope: this,
          handler: function() {
                // the window is a modal so we don't need to hang on to a reference
                // the window closes itself if 'Cancel' is clicked
                var changeSiteWindow = new RP.upb.ChangeSiteWindow().show();
          }
        });
      }
      else if (siteId !== RP.location.DEFAULT_NONE) {
        return new Ext.form.Label({
          text: siteId
        });
      }
    }
    return null;
  },
  
    helpMapper: function() {
        var currTask = RP.globals.CURRENT_TASK;
        
        if (!Ext.isDefined(currTask)) {
            return null;
        }
        
        var currTaskConfig = currTask.taskConfig;

        // Help map function takes precedence over everything else.
        if (Ext.isFunction(currTaskConfig.helpMapperFn)) {
            var taskMappedHelpUrl = currTaskConfig.helpMapperFn();

            if (!Ext.isEmpty(taskMappedHelpUrl)) {
                return taskMappedHelpUrl;
            }
        }

        // Get the components of the Help URL.
        var taskHelpUrl = currTaskConfig.helpUrl;
        var helpRootUrl = RP.help.HelpRegistry.getHelpRootURL(currTask.appId);

        // if the taskHelpUrl is a full URL, return it instead of appending the the helpRootUrl.
        if (taskHelpUrl.indexOf("http://") === 0 || taskHelpUrl.indexOf("https://") === 0) {
            return taskHelpUrl;
        }

        // If any Help URL components is empty, default to Main Help...
        if (Ext.isEmpty(taskHelpUrl) || Ext.isEmpty(helpRootUrl)) {
            return null;
        }

        // Valid Help URL components, so combine them for full URL...
        return (helpRootUrl + taskHelpUrl);
    }
});

Ext.reg("rpheaderpanel", RP.upb.HeaderPanel);
//////////////////////
// ..\js\upb\HeaderNotification.js
//////////////////////
Ext.ns("RP.upb");

/**
 * @class RP.upb.HeaderNotification
 * @extends Ext.Container
 */
RP.upb.HeaderNotification = Ext.extend(Ext.Component, {
    initComponent: function() {
        Ext.apply(this, {
            autoEl: {
                tag: "a",
                href: ["javascript", ":void(0);"].join(""),
                html: RP.getMessage("rp.common.misc.UnstableEnvironmentWarning")
            }
        });
        
        if (RP.globals.getValue("SERVER_TYPE") === "production") {
            this.displayMessage();
        }
        
        this.on("afterrender", this.attachClickHandler, this);
        RP.upb.HeaderNotification.superclass.initComponent.call(this);
    },
    
    /**
     * Attach the click handler to the element so that the appropriate message is displayed.
     */
    attachClickHandler: function() {
        this.getEl().on("click", this.displayMessage, this);
    },

    /**
     * Display the message to the user indicating the environment is unstable.
     */
    displayMessage: function() {
        RP.upb.UnstableEnvironmentWindow.show();
    }
});

Ext.reg("rpheadernotification", RP.upb.HeaderNotification);
//////////////////////
// ..\js\upb\UserComponent.js
//////////////////////
Ext.ns("RP.upb");

/**
 * @class RP.upb.UserComponent
 * Provides the basic UI component around the user and user options
 * in the header panel.
 */
RP.upb.UserComponent = Ext.extend(Ext.BoxComponent, {
    initComponent: function(){
        var userFullName = RP.globals.getValue("USER_FULL_NAME");
        Ext.apply(this, {
            id: "_rp-userName",
            autoEl: "span",
            html: RP.globals.getValue("DISPLAY_USER_FULLNAME") && !Ext.isEmpty(userFullName) ? userFullName : RP.globals.getValue("USERNAME")
        });
        
        this.on("afterrender", this.onAfterRender, this);
        
        
        RP.upb.UserComponent.superclass.initComponent.call(this);
    },
    
    /**
     * Handle post rendering operations.
     */
    onAfterRender: function() {
        if (!this.externalAuthentication) {
            this.addMenu();
        }
        else {
            this.removeDownArrow();
        }
    },
    
    /**
     * Create the menu for the
     */
    addMenu: function() {
        var menu = new Ext.menu.Menu({
            id: "_rpUserMenu"
        });
        
        menu.addMenuItem({
            itemId: "changePassword",
            text: RP.getMessage("rp.common.changepassword.FormTitle"),
            handler: function(){
                var x = new RP.ui.ChangePassword();
            }
        });
    
        // allow the user's name to display a menu
        this.getEl().on("click", function(){
            menu.show(this, "bl");
        });
    },
    
    /**
     * Removes the down arrow directly to the right of the user's login.
     */
    removeDownArrow: function() {
        this.getEl().set({
            id: ""
        }); 
    }
});
//////////////////////
// ..\js\upb\Viewport.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.upb");

/**
 * @namespace RP.upb
 * @class Viewport
 */
RP.upb.Viewport = Ext.extend(Ext.Viewport, {
  initComponent: function(){
    var center = {
      itemId: "_rp_center_panel",
      region: 'center',
      border: false,
      layout: "fit",
      items: this.items,
      cls: 'rp-center-panel'
    };
    
    var headerConfig = this.initialConfig.headerConfig;
    var title = (headerConfig.title) ? headerConfig.title : "RedPrairie"; //TODO
    var headerPanel = {
      id: "_rp_header_panel",
      xtype: 'rpheaderpanel',
      titleText: title,
      externalAuthentication: this.initialConfig.externalAuthentication
    };
    
    Ext.apply(headerPanel, this.initialConfig.headerConfig);
    
    var northHeight = 55;
    var north = {
      id: '_rp_north_panel',
      region: 'north',
      split: false,
      height: northHeight,
      border: false,
      collapsible: false,
      layout: 'fit',
      layoutConfig: {
        animate: true
      },
      items: [{
        border: false,
        iconCls: 'nav',
        items: [headerPanel]
      }]
    };
    
    var viewportItems = [north, center];
    var northHidden = false;
    var northPanel = null;
    
    this.toggleHeaderPanel = function(){
      if (!northPanel) {
        northPanel = Ext.getCmp("_rp_north_panel");
      }
      
      if (northHidden) {
        northPanel.setHeight(northHeight);
        northPanel.getEl().removeClass("viewinactive");
      }
      else {
        northPanel.getEl().addClass("viewinactive");
      }
      northHidden = !northHidden;
    };
    
    Ext.apply(this, {
      layout: 'border',
      items: viewportItems
    });
    
    RP.upb.Viewport.superclass.initComponent.call(this);
  }
});
//////////////////////
// ..\js\upb\PageBootstrapper.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.upb");

/**
 * @namespace RP.upb
 * @class PageBootstrapper
 * 
 * Provides the API to bootstrap the page, sort of like the main() function.  Called
 * by the Unified PageBuilder in REFS.
 */
RP.upb.PageBootstrapper = (function() {
  /**
   * viewPortId is used to set the the ID of the Ext component
   * at creation time.  It should be used to gain access to other sub
   * components that are rendered within the page.
   */
  var viewPortId = "rpViewport";
  
  var externalAuthentication = false;
  var nativeLogin = true;
  
  /**
   * taskflowFrameItemId is used to define the taskflow frame item id. 
   * The combination of the viewPortId and the taskflowFrameItemId should
   * give a proper path to someone manipulating the taskflow state.
   */
  var taskflowFrameItemId = "taskflowFrame";
  
  function renderPage(config) {
    externalAuthentication = config.externalAuthentication || false;
    nativeLogin = Ext.value(config.nativeLogin, true);
    
    var currentModule = config.currentModule || "";
    var modules = RP.taskflow.ModuleRegistry.getAllSorted();

    if (!Ext.isEmpty(currentModule) && Ext.isString(currentModule)) {
      RP.globals.CURRENT_MODULE = RP.taskflow.ModuleRegistry.get(currentModule);
    }
    else {
      RP.core.PageContext.determineModule();
    }
    
    var activeModule = RP.globals.CURRENT_MODULE;
    
    // Load taskflows from active module and scriptUrls.
    var taskflows = [];
    if (activeModule) {
      logger.logInfo("[PageBootstraper] Loading module '" + activeModule.name + "'");
      
      var taskflowsEnabled = true;
      if (activeModule.taskflows.names) {
        taskflowsEnabled = activeModule.taskflows.enabled;
        activeModule.taskflows = activeModule.taskflows.names;
      }

      // Load the active modules taskflows.        
      Ext.each(activeModule.taskflows, function(tfName) {
        var tfDef = RP.taskflow.TaskflowRegistry.get(tfName);
        if (tfDef) {
          taskflows.push({
            itemId: tfName,
            taskflow: tfName,
            title: tfDef.title,
            isTitleMessageName: tfDef.isTitleMessageName
          });
        }
      });
    }        
    
    var createViewportFn = function(){
      var viewport = new RP.upb.Viewport({
        id: viewPortId,
        externalAuthentication: externalAuthentication,
        //items: { xtype: "panel", html: 'The Stash <br /><img src="http://i14.photobucket.com/albums/a304/Meglicious/Rollie_Fingers_list_view.jpg" alt="The Stash" /><br />[Placeholder]' },
        items: [new RP.taskflow.TaskflowFrame({
          itemId: taskflowFrameItemId,
          navRegion: RP.globals.NAV_REGION,
          navTitle: activeModule? RP.getMessage(activeModule.label) : "",
          taskflows: taskflows,
          taskflowsEnabled: taskflowsEnabled,
          modules: modules
        })],
        headerConfig: {}
      });
      
      viewport.render(document.body);
    };
    
    var moduleInitFn = Ext.emptyFn;
    if (activeModule) {
        moduleInitFn = activeModule.initFn;
    }
    // Call the module's init method.
    if (Ext.isString(moduleInitFn)) {
      var tmpInitFn = eval(moduleInitFn);
      if (Ext.isFunction(tmpInitFn)) {
        moduleInitFn = tmpInitFn;
      }
    }
    
    if (Ext.isDefined(activeModule) && Ext.isFunction(moduleInitFn)) {
      moduleInitFn.call(activeModule, createViewportFn);
    }
    else {
      createViewportFn();
    }
  }
  
  return {
    
    
    /**
     * @cfg {Boolean} externalAuthentication (Optional) True to configure the HeaderPanel to display
     * in reverse-proxy mode.  This will disable the change password menu and the Logout
     * link will instead appear as a Close link.
     */
    
    /**
     * @cfg {Boolean} logToServerFlag (Optional) True to initially log to server 
     */
    
    /**
     * @cfg {Number} logToServerInterval (Optional) The number of milliseconds to wait to post client-side
     * logs to the server.  Default is 5000.
     */
    
    /**
     * @cfg {Number} logMaxEntries (Optional) The max number of log messages to display in the UI.  Note: does
     * not affect the number of logs sent to server.  Default is 1000.
     */
    
    /**
     * Bootstraps a page load.  Determines the active module and taskflow and activates them.
     * @method bootstrap
     * @param {Object} config
     */
	bootstrap: function(config) {
      // Set up logger.
      // Set the data service URL for posting client-side logs.
      logger.setLogToServerURL(RP.globals.getFullyQualifiedPath("PATH_TO_DATA_ROOT") + "logToServer");
      
      if (config.logToServerInterval) {
        logger.setLogToServerInterval(config.logToServerInterval);
      }
      
      if (config.logToServerFlag) {
        logger.setLogToServer(true, false);
      }
      
      if (config.logMaxEntries) {
        logger.setUIMaxEntries(config.logMaxEntries);
      }
      
      logger.logTrace("[PageBootstrapper] bootstrap()...");
            
      Ext.onReady(function() {
          
        // Make the components stateful.
        Ext.state.Manager.setProvider(new Ext.state.CookieProvider({
            expires: new Date(new Date().getTime()+(1000*60*60*24*365))
        }));

        // This is to allow for possibly other dynamic scripts getting loaded (message packs
        // and other types of scripts) as part of the stash library load)...
        RP.util.ScriptLoader.onReady(function() {
          renderPage(config);
              
          RP.event.AppEventManager.register(RP.upb.AppEvents.ModuleContextChanged, function() {
            RP.util.Helpers.reload(1);
          });
        });
      }); 
    },
    
    getTaskflowFrame: function() {
      return Ext.getCmp(viewPortId).find('itemId', taskflowFrameItemId)[0];
    },
    
    isExternallyAuthenticated: function() {
        return externalAuthentication;
    },
    isNativeLogin: function() {
        return nativeLogin;
    }
  }; 
})();

// This needs to load before the one in TaskflowFrame.js.  It relies on
// TaskflowFrame to set RP.globals.CURRENT_HASH, which is used to
// determine the previous hash.
Ext.History.on("change", function(strHash) {
  logger.logTrace("[PageContext] location change detected: " + strHash);
  
  if (RP.globals.CURRENT_HASH) {
    // If module context changed, reload page...
    var oldModuleCtxStr = Ext.urlEncode(RP.globals.CURRENT_HASH.moduleContext);
    var newHash = RP.core.PageContext.parseHash(strHash);
    if ((Ext.urlEncode(newHash.moduleContext) !== oldModuleCtxStr) && RP.core.PageContext.isRefreshOnModuleContextChange()) {
      RP.event.AppEventManager.fire(RP.upb.AppEvents.ModuleContextChanged);
    }
  }
});

/**
 * getTaskflowFrame
 * @namespace RP
 * The method getTaskflowFrame is used to retrieve a reference to the
 * taskflow frame such that additional component operations may be invoked.
 * 
 * This is a shortcut to the RP.upb.PageBootstrapper.getTaskflowFrame method.
 */
 RP.getTaskflowFrame = RP.upb.PageBootstrapper.getTaskflowFrame;
//////////////////////
// ..\js\..\..\stashLibEndLoad.js
//////////////////////
RP.stash.api.endLoadLib();
