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
// ..\js\core\ErrorUtils.js
//////////////////////
/**
 * @member RP
 * @method throwError
 * Throws an Error with the specified message and the current stack trace
 * @param {String} message The message to display
 */
RP.throwError = (function() {
    var truncateLength = 80;
    var maxStackTraceDepth = 25;
    var anonymousString = "(anonymous)";

    /**
     * @member RP
     * @method truncateFunction
     * Function to print (after remove multiple spaces from) a function
     * @param {Function} fn The function to stringify
     * @return {String} String version of the function
     */
    var truncateFunction = function(fn) {
        var functionString = fn.toString();
        return functionString.replace(/\s+/g, " ");
    };

    /**
     * @member RP
     * @method printObject
     * Function to stringify an object
     * @param {Object} obj The object to stringify
     * @return {String} String version of the object
     */
    var printObject = function(obj) {
        var result = "";
        // Prevent Ext from being printed, since its Date property cannot be printed as of Ext JS 4.1
        if (obj == Ext) {
            return result;
        }
        for (var property in obj) {
            var value = obj[property];
            if (Ext.isString(value)) {
                value = "'" + value + "'";
            }
            else if (Ext.isArray(value)) {
                value = "[ " + value + " ]";
            }
            else if (Ext.isFunction(value)) {
                value = truncateFunction(value);
            }
            result += property + " : " + value + ", ";
        }
        return "{" + result.replace(/, $/, "") + "}";
    };

    /**
     * @member RP
     * @method prettyPrintArguments
     * Function to print something in a more readable fashion
     * @param {Array} args A list of things to print
     * @return {Array} An array of stringified versions of the arguments
     */
    var prettyPrintArguments = function(args) {
        var prettyArgs = Ext.Array.map(args, function(value) {
            if (value) {
                if (Ext.isFunction(value)) {
                    return truncateFunction(value);
                }
                else if (Ext.isObject(value)) {
                    return printObject(value);
                }
                else if (value.getValue) {
                    return value.getValue();
                }
            }
        });
        return prettyArgs;
    };

    /**
     * @member RP
     * @method getStackTrace
     * Print the stack trace of a specified method
     * @param {Function} method The method to be stack traced
     * @return {String} A string version of the stack trace
     */
    var getStackTrace = function(method) {
        var stackTrace = [];
        var counter = 0;
        while (method && counter < maxStackTraceDepth) {
            var stackPiece = anonymousString;
            if (method.$owner && method.$owner.$className) {
                stackPiece = method.$owner.$className;
            }
            var argArr = prettyPrintArguments(Ext.Array.toArray(method["arguments"]));
            if (method.$name) {
                stackPiece += "." + method.$name + "(" + argArr.join(",") + ")";
            }

            if (stackPiece === anonymousString) {
                stackPiece += " " + truncateFunction(method);
            }

            if (stackPiece.length > truncateLength) {
                stackPiece = stackPiece.substring(0, truncateLength) + "...";
            }

            stackTrace.push("\tat " + stackPiece);

            // check for more levels of hierarchy
            if (method.caller) {
                method = method.caller;
            }
            else {
                method = null;
            }
            counter++;
        }

        if (counter == maxStackTraceDepth) {
            stackTrace.push("and more ...");
        }

        return stackTrace.join("\n");
    };

    return function(message) {
        var method = this.throwError.caller;
        var newMessage = message + ":\n" + getStackTrace(method);
        
        // Old versions of Safari don't have window.onerror, compensate.
        if (Ext.isSafari && Ext.safariVersion < 5.1) {
            RP.showErrorDialog(newMessage);
        }
        throw new Error(newMessage);
    };
}());

/**
 * @member RP
 * @method showErrorDialog
 * A method designed for unified display of error messages
 * @param {String} message The message to display in the box
 */
RP.showErrorDialog = function(message) {
    Ext.MessageBox.setAutoScroll(true);
    Ext.MessageBox.show({
        title: "Error",
        msg: message.replace(/\n/g, "<br/>").replace(/\t/g, "&#160;&#160;&#160;&#160;"),
        buttons: Ext.MessageBox.OK,
        width: 650,
        fn: function() {
            Ext.MessageBox.setAutoScroll(false);
        }
    });
};
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
    RP.locale.Dispatch.includeLocalePack("dateranges");
});
//////////////////////
// ..\js\core\ApplicationSites.js
//////////////////////
Ext.ns("RP.core");

/**
 * Serves as an API for interacting with the user's available sites.
 *
 * @class RP.core.ApplicationSites
 * @singleton
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
     * Finds the client site for the site within the specified application
     * @param {String} applicationId The application id.
     * @param {String} siteId The site id of the client site you are trying to get.
     * @return {Object} The client site object if found, null otherwise.
     */
    function getClientSite(applicationId, siteId) {
        var application = appMetaData[applicationId];

        if (Ext.isArray(application)) {
            for(var i = 0; i < application.length; i++) {
                var clientSite = application[i];

                if(clientSite.siteId === siteId) {
                    return clientSite;
                }
            }
        }

        return null;
    }

    /**
     * Lexicographically compares the values of two strings ("A" < "B", "C" > "A")
     *@param {String} stringA The primary string
     *@param {String} stringB The string being compared
     *@return {Number} 0 if equal, -1 if a is "less" than b, 1 if a is "greater" than b
     */
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
         * @param {Object} applicationMetaData The application meta data information.
         */
        setApps: function(applicationMetaData) {
            appMetaData = applicationMetaData;
        },

        /**
         * Gets a sorted list of all the available sites.
         * @return {Array} The array of all site IDs.
         */
        getAllSiteIds: function() {
            var clientSites,
                siteId,
                siteIds = [],
                uniqueSiteIds = {}; // used to avoid slow indexOf checks (causes issues in IE7)

            // avoiding function-based iteration for performance
            for (var application in appMetaData) {
                clientSites = appMetaData[application];

                if (Ext.isArray(clientSites)) {
                    for (var i=0; i < clientSites.length; i++) {
                        siteId = clientSites[i].siteId;

                        if (!uniqueSiteIds[siteId]) {
                            siteIds.push(siteId);
                        }

                        uniqueSiteIds[siteId] = true;
                    }
                }
            }

            return this.sortSiteIds(siteIds);
        },

        /**
         * Returns an ordered list of site IDs such that
         * ['site100', 'siteB', 'siteA', 'site100A'] would yield
         * ['site100', 'site100A', 'siteA', 'siteB']
         * @param {Array} uniqueSites A list of sites to compare
         * @return {Array} An ordered list of site IDS corresponding to the site list passed in
         */
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
         * @param {String} applicationId The application the site is in
         * @param {String} siteId The site whose Native ID your are seeking
         * @return {String} The native site ID, or null if not found.
         */
        getNativeId: function(applicationId, siteId) {
            var clientSite = getClientSite(applicationId, siteId);

            if (clientSite !== null) {
                return clientSite.nativeId;
            }

            return null;
        },
        
        getSiteLocale: function(siteID) {
            var app, site;
            
            for (var appindex in appMetaData) {
                app = appMetaData[appindex];
                
                for (var siteindex in app) {
                    site = app[siteindex];
                    
                    if (site.siteId === siteID && site.locale !== '')
                    {
                        return site.locale;
                    }
                }
            }

            return null;
        },

        /**
         * Builds a url to the Session Keep Alive service
         * used for legacy applications for managing a user's session lifetime.
         */
        getSessionKeepAliveUrl: function() {
            logger.logTrace("[Application Site] Finding Keep Alive URL");

            // Page URL addended with /rp/admin/sessionKeepAlive
            return RP.core.PageContext.getPageUrl() + this.buildDataServiceUrl("rp", "admin/sessionKeepAlive");
        },

        /**
         * Allows for an application to verify that a backend application has been registered and can
         * be used for data requests.
         * 
         * @param {String} appId The application id of the web service being verified.
         * @return {Boolean} whether or not the application matching the appId exists.
         */
        isAppInstalled: function(appId) {
            var application = getApplication(appId);
            return application !== undefined;
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
            logger.logTrace("[Application Site] Creating a fully qualified data service URL");
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

            if (RP.core.Sundial.hasOffset()) {
                params.demoDate = Ext.Date.format(RP.core.Sundial.now(), "c");
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
            logger.logTrace("[Application Site] Creating a web folder URL");
            //If the path contains an intial "/" remove it because it will be added in later
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }

            return RP.globals.paths.STATIC + appId + "/" + path;
        }
    };
}();

// Shortcuts
RP.isAppInstalled = RP.core.ApplicationSites.isAppInstalled;
RP.buildDataServiceUrl = RP.core.ApplicationSites.buildDataServiceUrl;
RP.buildWebUrl = RP.core.ApplicationSites.buildWebUrl;
RP.getSessionKeepAliveUrl = RP.core.ApplicationSites.getSessionKeepAliveUrl;
//////////////////////
// ..\js\core\PageContext.js
//////////////////////
Ext.ns("RP.core");

/**
 * Interface for dealing with the Page/Taskflow/Task Contexts in both Object and String form
 *
 * @class RP.core.PageContext
 */
RP.core.PageContext = (function() {
    var CONTEXT_TOKEN = ":";

    // by default the browser should refresh on module context change.
    var refreshOnModuleContextChange = true;

    /**
     * Finds the Base URL of the page
     * @return {String} The Base URL string
     * @private
     */
    function getRootUrl() {
        var baseUrl = RP.globals.getPath("BASE_URL"); 
        if (!Ext.isEmpty(baseUrl)) {
            return baseUrl;
        }

        return Ext.String.format("{0}//{1}", 
            window.location.protocol, 
            window.location.host);
    }

    /**
     * Finds the Root path of the app
     * @param {String} moduleName The name of the Module
     * @param {String} siteId The ID of the active site
     * @return {String} The Root path string
     * @private
     */
    function getPageRootPath(moduleName, siteId) {
        // ensure the siteId is defaulted if not specified
        if (!Ext.isDefined(siteId)) {
            siteId = RP.globals.getValue("SITEID");
        }

        var siteIdStr = Ext.String.format("/{0}", encodeURIComponent(siteId));

        var rootPath = Ext.String.format("{0}{1}/{2}",
            RP.globals.getFullyQualifiedPath("PB_ROOT"),
            siteIdStr, 
            moduleName);

        if (rootPath.indexOf("http") !== 0) {
            rootPath = getRootUrl() + rootPath;
        }

        return rootPath; 
    }

    /**
     * Redirects the browser to a new Module
     * @param {String} moduleName The new module being switched to
     * @param {Object} moduleContext The context (hash) to be used within that new module
     * @private
     */
    function setLocationToModule(moduleName, moduleContext) {
        var moduleContextStr = moduleContext ? Ext.urlEncode(moduleContext) : "";
        RP.util.Helpers.redirect(Ext.String.format("{0}#/{1}", getPageRootPath(moduleName), moduleContextStr));
    }

    /**
     * Takes an array of objts and returns the primitives in the set
     * @param {Array} objs The set of objects to iterate over
     * @return {Array} An array of key/value context objects, where the values are primitives
     * @private
     */
    function getValidContextProperties(objs) {
        var ret = {};

        // Retrieve only properties that are primitive value types...
        Ext.iterate(objs, function(key, value) {
            if (Ext.isPrimitive(value)) {
                ret[key] = value;
            }
        });

        return ret;
    }

    /**
     * Turn the context of a module and the active task and taskflow into a URL
     * @param {Object} moduleContext The object representation of the module's context
     * @param {Object} taskflowContext The object representation of the taskflow's context
     * @param {Object} taskContext The object representation of the task's context
     * @return {String} A stringified version of the context, or an empty string if there is no context
     * @private
     */
    function getContextAsUrl(moduleContext, taskflowContext, taskContext) {
        var moduleCtx = moduleContext ? Ext.urlEncode(getValidContextProperties(moduleContext)) : "";
        var taskflowCtx = taskflowContext ? Ext.urlEncode(getValidContextProperties(taskflowContext)) : "";
        var taskCtx = taskContext ? Ext.urlEncode(getValidContextProperties(taskContext)) : "";

        if (!Ext.isEmpty(moduleCtx) || !Ext.isEmpty(taskflowCtx) || !Ext.isEmpty(taskCtx)) {
            return Ext.String.format("/{0}/{1}/{2}", moduleCtx, taskflowCtx, taskCtx);
        }

        // if there is no context set don't fake one.
        return "";
    }

    /**
     * Generates the context from the hash out of a URL
     * @param {String} hash The hashed version of the context in the form of <taskflow_name>:<task_id>/<module_context>/<taskflow_context>/<task_context>
     * @return {Object} An object representing the context specified by the string
     * @private
     */
    function parseUrl(hash) {

        // Format of 'hash': <taskflow_name>:<task_id>/<module_context>/<taskflow_context>/<task_context>
        if (!Ext.isDefined(hash) || hash === null) {
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

    /**
     * Creates the string version of the module context
     * @param {String} taskflowName The active taskflow
     * @param {String} taskName The active task
     * @param {Object} moduleContext The context of the active module
     * @param {Object} taskflowContext The context of the active taskflow
     * @param {Object} taskContext The context of the active task
     * @return {String} The URL string version of the current context
     * @private
     */
    function createUrlHash(taskflowName, taskName, moduleContext, taskflowContext, taskContext) {

        // get the appropriate values to set the URL and default to empty string on null.
        var ctxUrl = getContextAsUrl(moduleContext, taskflowContext, taskContext) || "";
        taskName = taskName || "";
        taskflowName = taskflowName || "";

        return Ext.String.format("{0}{1}{2}{3}", taskflowName, CONTEXT_TOKEN, taskName, ctxUrl);
    }

    return {

        /**
         * Returns whether or not the Page is refreshed on a change of the module context
         * @return {Boolean} Whether or not the module should be reloaded when the module context changes. 
         */
        isRefreshOnModuleContextChange: function() {
            return refreshOnModuleContextChange;
        },

        /**
         * Converts a context object into a String representation (so it can be used as part of a URL hash, for example)
         * @param {Object} ctx The context
         * @return {String} The string version of the context passed in
         */
        encodeContextAsHashValue: function(ctx) {
          return Ext.urlEncode(getValidContextProperties(ctx));
        },

        /**
         * Parses a URL hash to get taskflow name, task ID, and various contexts. Context values are parsed to JSON objects.
         * @param {String} hash The string hashed version of the context
         * @return {Object} Object with the following properties: tfName, taskID, moduleContext, taskflowContext, and taskContext
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
         * @return {String} The module name or null if the module name doesn't match up
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
         * This should not be called directly. PageBootstrapper calls this to determine the
         * module to load by inspecting the URL for the module name, and if not found,
         * sets it to the first sorted module.
         * @param {String} moduleName The name of the active module
         * @return {String} The active module name
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
         * @param {String} moduleName The name of the module
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
         * @param {String} siteId The id of the default site
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
         * Returns the URL hash to use if you were to change the taskflow and/or task in the current module
         * @param {String} taskflowName The name of the active taskflow
         * @param {String} taskID The ID of the active task
         * @param {Object} taskflowContext The context of the active taskflow
         * @param {Object} taskContext The context of the active task
         * @return {String} The URL String version of the active task's context
         */
        getTaskHash: function(taskflowName, taskID, taskflowContext, taskContext) {
            // Get the taskflow and task context with the current module context...
            var currCtx = parseUrl(window.location.hash);
            var newHash = createUrlHash(taskflowName, taskID, currCtx.moduleContext, taskflowContext, taskContext);

            return newHash;
        },

        /**
         * Returns the current module context as specified in the current URL as a JSON object
         * @return {Object} The module context
         */
        getModuleContext: function() {
            var currCtx = parseUrl(window.location.hash);
            return currCtx.moduleContext;
        },

        /**
         * This should not be called directly.  This is designed to be called once by
         * the page builder to set the module context at page load time
         * @param {Object} moduleContext The context of the module
         * @param {Boolean} [refreshBrowser] Whether or not to refresh the browser
         */
        updateModuleContext: function(moduleContext, refreshBrowser) {
            if(!Ext.isEmpty(refreshBrowser)) {
                refreshOnModuleContextChange = refreshBrowser;
            }

            // Update the module context, but preserve the taskflow and task context...
            var currCtx = parseUrl(window.location.hash);
            var newHash = createUrlHash(currCtx.tfName, currCtx.taskID, moduleContext, currCtx.taskflowContext, currCtx.taskContext);

            Ext.util.History.add(newHash, true);
        },

        /**
         * This should not be called directly.  This is designed to be called once when instantiating a taskflow.
         * @param {Object} taskflowContext The taskflows context
         */
        updateTaskflowContext: function(taskflowContext) {
          // Update the taskflow context, but preserve the module and task context...
          var currCtx = parseUrl(window.location.hash);
          var newHash = createUrlHash(currCtx.tfName, currCtx.taskID, currCtx.moduleContext, taskflowContext, currCtx.taskContext);

          Ext.util.History.add(newHash, true);
        },

        /**
         * Update the task context.
         * @param {Object} taskContext The new task context
         */
        updateTaskContext: function(taskContext) {
            // Update the task context, but preserve the module and taskflow context...
            var currCtx = parseUrl(window.location.hash);
            var newHash = createUrlHash(currCtx.tfName, currCtx.taskID, currCtx.moduleContext, currCtx.taskflowContext, taskContext);
          
            if (newHash) {
                Ext.util.History.add(newHash, true);
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
         * @return {String} The URL built up from the context passed in
         */
        getPageURL: function(siteId, moduleName, taskflowName, taskName, moduleCtx, taskflowCtx, taskCtx) {
            return Ext.String.format("{0}#{1}", 
                getPageRootPath(moduleName, siteId),
                createUrlHash(taskflowName, taskName, moduleCtx, taskflowCtx, taskCtx));
        },

        /**
         * Switches the user to a different site.
         * @param {String} siteId The site Id to switch to.
         * @param {Boolean} openInNewWindow Whether to open the new site in a new window.
         */
        switchSite: function(siteId, openNewWindow) {
            var url = RP.core.PageContext.getPageURL(siteId, 
                RP.core.PageContext.getActiveModuleName()
            );

            if (openNewWindow === true) {
                RP.core.PageContext.openWindow(url);
            }
            else {
                RP.util.Helpers.redirect(url);
            }
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
                opt.push(Ext.String.format("{0}={1}", key, value ? 1 : 0));
            });

            // Open the new window with the options in a comma separated string
            return window.open(url, name, opt.toString());
        }
    };
})();
//////////////////////
// ..\js\core\SessionSecurity.js
//////////////////////
Ext.ns("RP.core");

/**
 * Hooks all the important pieces of the security mechanisms to secure the user's session.
 * *Needs refactor*
 * @class RP.core.SessionSecurity
 */
RP.core.SessionSecurity = (function() {
    /**
     * @private
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
        logger.logTrace("[Session Security] User inactive, starting expiration countdown");
        RP.event.AppEventManager.fire(RP.upb.AppEvents.Inactive, {});

        var dlg = Ext.getCmp("inactivityWarningDialog");
        if (!dlg) {
            dlg = new RP.ui.InactivityWarningDialog();
        }

        RP.event.AppEventManager.register(RP.upb.AppEvents.ActiveAgain, startTimerFn);

        dlg.show();
    }

    /**
     * @private
     * Display the session expired dialog or the external authentication expired dialog to
     * the user to force them to provide their credentials again.
     */
    function terminatedSession() {
        logger.logTrace("[Session Security] Session expired, displaying dialog");
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
     * @private
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
Ext.ns("RP.upb");
/**
 * @class RP.upb.AppEvents
 * Enumerated type for event names. All events will be passed
 * the event name as the first argument
 */
RP.upb.AppEvents = {

	/**
	 * @property {String} Inactive
	 * User-inactivity triggered
	 */
    Inactive: "rp.upb.inactive",

    /**
     * @property {String} ActiveAgain
     * User-inactivity warning, or re-authorize dialog, dismissed
     */
    ActiveAgain: "rp.upb.activeAgain",

    /**
     * @property {String} ModuleContextChanged
     * The module context has been changed
     */
    ModuleContextChanged: "rp.ub.moduleCtxChanged",

    /**
     * @property {String} BeforeSessionExpired
     * BeforeSessionExpired occurs after the inactivity warning dialog box has
     * counted down and ended, but before the session is marked as requiring
     * new authentication.
     */
    BeforeSessionExpired: "rp.upb.beforeSession",

    /**
     * @property {String} SessionExpired
     * Session has completely expired; the expired dialog box appears.
     */
    SessionExpired: "rp.upb.sessionExpired",

    /**
     * @property {String} SessionReauthenticated
     * Occurs when the session expired and the user reauthenticates
     */
    SessionReauthenticated: "rp.upb.sessionReauthenticated",

    /**
     * @property {String} KeepAlive
     * Occurs when the session keep alive heartbeat is sent out
     */
    KeepAlive: "rp.upb.keepAlive",
    
    /**
     * @property {String} KeepAliveSuccess
     * Occurs when the session keep alive heartbeat request succeeds upon return 
     */
    KeepAliveSuccess: "rp.upb.keepAliveSuccess",
    
    /**
     * @property {String} KeepAliveSuccess
     * Occurs when the session keep alive heartbeat request fails upon return 
     */
    KeepAliveFailure: "rp.upb.keepAliveFailure"
};
//////////////////////
// ..\js\upb\ChangeSiteWindow.js
//////////////////////
/**
 * An internal class, site model, used by the {@link RP.upb.ChangeSiteWindow}.
 */
Ext.define("RP.upb.ChangeSiteWindow.SiteModel", {
    extend: "Ext.data.Model",
    fields: ["siteId"]
});

/**
 * A Window that allows a user to change sites (e.g., BU, warehouse) in RPWEB.
 */
Ext.define("RP.upb.ChangeSiteWindow", {
    extend: "Ext.window.Window",
    alias: "widget.changesitewindow",
    width: 450,
    closeAction: "hide",
    draggable: false,
    resizable: false,
    closable: false,
    modal: true,
    bodyPadding: 20,
    bodyStyle: "background-color: #fff",
    
    initComponent: function() {
        Ext.apply(this, {
            title: RP.getMessage("rp.common.changesite.FormTitle"),
            items: this.createItems()
        });
        
        this.callParent();
    },
    
    /**
     * Creates the Window's items.
     */
    createItems: function() {
        var store = new Ext.data.Store({
            model: "RP.upb.ChangeSiteWindow.SiteModel"
        });
        
        // create models from the array of sites
        Ext.each(RP.core.ApplicationSites.getAllSiteIds(), function(site) {
            store.add({
                siteId: site
            });
        });
        
        return [{
            xtype: "panel",
            itemId: "changeSitePanel",
            height: 90, // height seems to be required for the vbox layout
            border: false,
            layout: {
                type: "vbox",
                align: "center"
            },
            items: [{
                xtype: "combo",
                itemId: "chooseSite",
                value: RP.globals.SITEID,
                labelAlign: "right",
                fieldLabel: RP.getMessage("rp.common.changesite.CurrentSite"),
                store: store,
                displayField: "siteId",
                valueField: "siteId",
                typeAhead: true,
                queryMode: "local",
                forceSelection: true,
                triggerAction: "all",
                selectOnFocus: true,
                labelStyle: "font-weight: bold",
                listeners: {
                    specialkey: this.onSpecialKey,
                    scope: this
                }
            }, {
                xtype: "checkboxfield",
                boxLabel: RP.getMessage("rp.common.changesite.OpenInNewWindow")
            }],
            bbar: this.createBottomToolbar()
        }];
    },
    
    /**
     * Creates the bottom toolbar containing the buttons.
     */
    createBottomToolbar: function() {
        return {
            xtype: "toolbar",
            dock: "bottom",
            ui: "footer",
            layout: {
                type: "hbox",
                pack: "center"
            },
            defaults: {
                xtype: "button",
                minWidth: this.minButtonWidth
            },
            items: [{
                itemId: "btnSubmit",
                text: RP.getMessage("rp.common.changesite.SelectButton"),
                handler: this.openSelectedSite,
                scope: this
            }, {
                itemId: "btnCancel",
                text: RP.getMessage("rp.common.changesite.CancelButton"),
                handler: this.close,
                scope: this
            }]
        };
    },
    
    /**
     * Performs a site change using the selected form values. Closes the 
     * current site.
     * @param {String} siteId The site Id to switch to.
     * @param {Boolean} openNewWindow Whether to open the new site in a new window.
     */
    changeSite: function(siteId, openNewWindow) {
        RP.core.PageContext.switchSite(siteId, openNewWindow);
        this.close();
    },
    
    /**
     * Opens the selected new Site, using {@link #changeSite}.
     */
    openSelectedSite: function() {
        var siteId = this.down("combobox").getValue();
        var openNewWindow = this.down("checkboxfield").getValue();
        this.changeSite(siteId, openNewWindow);
    },
    
    /**
     * Shortcut for using the 'ENTER' key inside the site combo box to change
     * the site.
     * @param {Ext.form.Field} field Field that the cursor was on when the key
     * was registered
     * @param {Ext.EventObject} e The event of the special key
     */
    onSpecialKey: function(field, e) {
        if (e.getKey() == e.ENTER  && field.findRecordByValue(field.getValue())) {
            e.stopEvent();
            this.openSelectedSite();
        }
    }
});
//////////////////////
// ..\js\upb\UnstableEnvironmentWindow.js
//////////////////////
/**
 * A Window that shows a message indicating the environment is unstable.
 *
 * This class is a wrapper around the message box for the sake of the namespacing
 * the unstable window. Also, it's used in multiple locations.
 * @singleton
 */
Ext.define("RP.upb.UnstableEnvironmentWindow", {
    extend: "Ext.Component",
    singleton: true,
    alias: "widget.unstableenvironmentwindow",
    id: "_rp-unstable-environment-window",

    show: function() {
        Ext.MessageBox.show({
            itemId: this.id,
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.WARNING,
            title: RP.getMessage("rp.common.misc.UnstableEnvironmentMessageTitle"), 
            msg: RP.getMessage("rp.common.misc.UnstableEnvironmentMessageDescription")
        });
    },

    isVisible: function() {
        if (Ext.MessageBox.cfg.itemId === this.id) {
            return Ext.MessageBox.isVisible();
        }
    },

    close: function() {
        if (Ext.MessageBox.cfg.itemId === this.id) {
            Ext.MessageBox.close();
        }
    }
});
//////////////////////
// ..\js\upb\HeaderPanel.js
//////////////////////
/**
 *  A header panel object, shown at the top of every page. 
 */
Ext.define("RP.upb.HeaderPanel", {

    extend: "Ext.panel.Panel",
    alias: "widget.rpheaderpanel",
    
    /**
     * @cfg {Boolean} externalAuthentication Specify true to configure the 
     * HeaderPanel to display in reverse-proxy mode. This disables the change
     * password menu and the Logout link will instead appear as a Close link.
     */
    
    initComponent: function() {
        var demoDateHtml = this._getDemoDateHtml();
        
        var html = Ext.String.format('<div class="rp-header">' +
        '<div class="rp-header-logo"></div>' +
        '<div class="rp-header-nav">' +
        '<div class="rp-header-nav-top">' +
        '</div>' +
        demoDateHtml +
        '</div>' +
        '</div>');
        
        var appHeader = new Ext.panel.Panel({
            border: false,
            html: html
        });
        
        var exitLink;
        var exitLinkId = "_rpExitLink";
        
        if (RP.upb.PageBootstrapper.isNativeLogin() === false) {
            exitLink = new Ext.Component({
                autoEl: {
                    tag: "a",
                    href: RP.globals.getFullyQualifiedPath("LOGOUT"),
                    html: RP.getMessage("rp.common.misc.Close")
                },
                cls: "rp-header-nav-link",
                id: exitLinkId
            });
        }
        else {
            exitLink = new Ext.Component({
                autoEl: {
                    tag: "a",
                    href: RP.globals.getFullyQualifiedPath("LOGOUT"),
                    html: RP.getMessage("rp.common.misc.Logout")
                },
                cls: "rp-header-nav-link",
                id: exitLinkId
            });
        }
        
        var mainHelpURL = RP.globals.getPath('BASE_URL') + RP.globals.getValue('PATH_TO_ROOT') + "web/refs/help/main.htm";
        
        RP.help.HelpRegistry.setDefaultHelpURL(mainHelpURL);
        
        var helpLink = RP.help.RoboHelpLink.createHelpLink(
            {cls: "rp-header-nav-link"}, 
            mainHelpURL, // Main URL
            RP.getMessage("rp.common.misc.HelpLinkLabel"), // Help hyperlink label
            "rphelpwindow", // window target name
            this.helpMapper // Help mapper function
        );
        
        Ext.apply(this, {
            border: false,
            layout: "fit",
            items: [appHeader]
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
        
        this.callParent(arguments);
    },
    
    /** 
     * Generate the demo date html to display below
     * the site/logout/help links.
     * @private 
     */
    _getDemoDateHtml: function() {
        var demoDateHtml = "";
    
        var sundial = RP.core.Sundial;
        if (sundial.hasOffset()) {
            var demoDateStr = RP.Date.formatDate(sundial.now(), RP.core.Formats.Date.Long);
      
            demoDateHtml = 
                '<div class="rp-header-nav-bottom">' +
                '<span class="rp-header-demo-time">' + RP.getMessage("rp.common.misc.DemoDateLabel") + 
                demoDateStr + '</span>' + '</div>';
        }
        
        return demoDateHtml;
    },
    
    /**
     * Adds a notification to the HeaderPanel by using {@link #addHeaderComponent}. 
     * @param {String} renterToId Id given to a new {@link RP.upb.HeaderNotification}
     */
    displayHeaderNotification: function(renderToId) {
        if (RP.globals.getValue("UNSTABLE") === "true") {
            this.addHeaderComponent(new RP.upb.HeaderNotification({id: renderToId}));
        }
    },
    
    
    /**
     * Adds a separator and then renders the component into the HeaderPanel.
     * @param {Ext.Component} component A component to add to the HeaderPanel.
     */
    addHeaderComponent: function(component) {
        if (!Ext.isEmpty(component)) {
            var domQuery = Ext.core.DomQuery;
            var headerNav =domQuery.selectNode(".rp-header .rp-header-nav-top");
            var separatorHtml = '<span class="rp-header-nav-separator">|</span>';
            
            if (domQuery.selectNode(".rp-header .rp-header-nav-top:has(*)")) {
                Ext.core.DomHelper.insertHtml("beforeEnd", headerNav, separatorHtml);
            }
            
            component.render(headerNav);
        }
    },
    
    /**
     * Getter function for the components of the site. Returns a Hyperlink,
     * unless there is only 1 available site from 
     * {@link RP.core.ApplicationSites#getAllSiteIds}. If that is the case, 
     * returns a Label. If the only available site is DEFAULT_NONE, null.
     * @return {RP.ui.Hyperlink/Ext.form.Label} site component
     */
    getSiteComponent: function() {
        var sites = RP.core.ApplicationSites.getAllSiteIds();
        var siteId = RP.globals.getValue("SITEID");
        
        if (!Ext.isEmpty(sites)) {
            if (sites.length > 1) {
                return new RP.ui.Hyperlink({
                    text: siteId,
                    cls: "rp-header-nav-link",
                    id: "_rp-siteSelectLink",
                    scope: this,
                    handler: function() {
                        if (!this.changeSiteWindow) {
                            this.changeSiteWindow = new RP.upb.ChangeSiteWindow();
                        }
                        
                        this.changeSiteWindow.show();
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
    
    /**
     * Gets the help mapper function of the current task. If it is not defined
     * by CURRENT_TASK.taskConfig.helpMapperFn, it constructs one from
     * taskConfig.helpUrl and {@link RP.help.HelpRegistry#getHelpRootURL}.
     * @return {Function} help mapper function
     */
    helpMapper: function() {
        // this value is updated at runtime by the taskflow engine, so getValue doesn't work
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
//////////////////////
// ..\js\upb\HeaderNotification.js
//////////////////////
/**
 * A class for notifications in {@link RP.upb.HeaderPanel}. They are by default 
 * links that create a Ext.MessageBox informing the user that the environment 
 * is unstable.
 */
Ext.define("RP.upb.HeaderNotification", {
    extend: "Ext.Component",
    alias: "widget.rpheadernotification",
    
    initComponent: function() {
        Ext.apply(this, {
            autoEl: {
                tag: "a",
                href: ["javascript", ":void(0);"].join(""),
                html: RP.getMessage("rp.common.misc.UnstableEnvironmentWarning")
            }
        });
        
        this.on("afterrender", this.attachClickHandler, this);
        this.callParent();
    },
    
    /**
     * Attach the default click handler to the element so that the 
     * appropriate message is displayed. In other words, attaches 
     * {@link #displayMessage} as an on-click event. This function is 
     * called after rendering by default.
     */
    attachClickHandler: function() {
        this.getEl().on("click", this.displayMessage, this);
    },

    /**
     * Display a message to the user indicating that the environment is unstable.
     */
    displayMessage: function() {
        RP.upb.UnstableEnvironmentWindow.show();
    }
});
//////////////////////
// ..\js\upb\UserComponent.js
//////////////////////
/**
 * Provides the basic UI component around the user and user options
 * in the header panel.
 */
Ext.define("RP.upb.UserComponent", {
    extend: "Ext.Component",
    
    /**
     * @cfg {Boolean} externalAuthentication Specify true to restrict the menu
     * from being added, and instead, the down arrow is removed.
     */
    
    initComponent: function(){
        var userFullName = RP.globals.getValue("USER_FULL_NAME");
        Ext.apply(this, {
            id: "_rp-userName",
            autoEl: {
                tag: "span",
                html: RP.globals.getValue("DISPLAY_USER_FULLNAME") && !Ext.isEmpty(userFullName) ? userFullName : RP.globals.getValue("USERNAME")
            }
        });
        
        this.on("afterrender", this.onAfterRender, this);
        this.callParent();
    },
    
    /**
     * Handle post rendering operations.
     * @private
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
     * Creates a menu on the user's name, with the singular option to change
     * password. This functionality is supported by 
     * {@link RP.ui.ChangePassword}.
     */
    addMenu: function() {
        var menu = new Ext.menu.Menu({
            id: "_rpUserMenu"
        });
        
        menu.add({
            itemId: "changePassword",
            text: RP.getMessage("rp.common.changepassword.FormTitle"),
            handler: function(){
                var changePassword = new RP.ui.ChangePassword();
                changePassword.show();
            }
        });
    
        // allow the user's name to display a menu
        this.getEl().on("click", function(){
            menu.showBy(this, "bl");
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
/**
 * A class which represents the view of a page. This view is composed of the 
 * North panel and the Center panel. The north panel holds only the 
 * {@link RP.upb.HeaderPanel}, and the center panel holds the rest of the 
 * page's content.
 */
Ext.define("RP.upb.Viewport", {
    extend: "Ext.container.Viewport",
    
    /**
     * @cfg {Number} northHeight Height of the north panel.
     */
    northHeight: 55,
    
    /**
     * @cfg {Boolean} externalAuthentication Specify true to configure the 
     * HeaderPanel to display in reverse-proxy mode. This disables the change
     * password menu and the Logout link will instead appear as a Close link.
     */

    /**
     * @cfg {Object} centerItems Items which should be added to the Center
     * Panel.
     */
    
    initComponent: function() {
    
        var centerPanel = this.createCenterPanel();
        var northPanel = this.createNorthPanel();
        
        Ext.apply(this, {
            layout: 'border',
            items: [northPanel, centerPanel]
        });
        
        this.callParent(arguments);
    },
    
    /**
     * Creates the center panel of the Viewport
     * @return {Ext.panel.Panel} Panel for the content which appears in the 
     * center. Items are instantiated to {@link #centerItems}.
     */
    createCenterPanel: function() {
        return Ext.create("Ext.panel.Panel", {
            itemId: "_rp_center_panel",
            cls: "rp-center-panel",
            region: 'center',
            border: false,
            layout: "fit",
            items: this.centerItems
        });
    },
    
    /**
     * Creates the upper panel of the Viewport. The single item is a 
     * {@link RP.upb.HeaderPanel}.
     * @return {Object} Panel containing the HeaderPanel
     */
    createNorthPanel: function() {
        return {
            region: 'north',
            split: false,
            height: this.northHeight,
            border: false,
            collapsible: false,
            layout: 'fit',
            items: [{
                id: "_rp_header_panel",
                xtype: 'rpheaderpanel',
                externalAuthentication: this.externalAuthentication
            }]
        };
    }
});
//////////////////////
// ..\js\upb\PageBootstrapper.js
//////////////////////
Ext.ns("RP.upb");

/**
 * @class RP.upb.PageBootstrapper
 *
 * Provides the API to bootstrap the page, sort of like the main() function.
 * Called by the Unified PageBuilder in REFS.
 */
RP.upb.PageBootstrapper = (function() {

    /**
     * @cfg {String} viewPortId
     * Used to set the the ID of the Ext component at creation time. It should
     * be used to gain access to other sub components that are rendered within
     * the page.
     */
    var viewPortId = "rpViewport";

    /**
     * @cfg {Boolean} externalAuthentication Specify true to configure the
     * HeaderPanel to display in reverse-proxy mode. This disables the change
     * password menu and the Logout link will instead appear as a Close link.
     */
    var externalAuthentication = false;

    /**
     * @cfg {Boolean} nativeLogin Tells whether login should be native.
     */
    var nativeLogin = true;

    /**
     * @cfg {String} taskflowFrameItemId
     * taskflowFrameItemId is used to define the taskflow frame item id.
     * The combination of the viewPortId and the taskflowFrameItemId should
     * give a proper path to someone manipulating the taskflow state.
     */
    var taskflowFrameItemId = "taskflowFrame";

    /**
     * @cfg {Boolean} logToServerFlag  True to initially log to
     * server
     */

    /**
     * @cfg {Number} logToServerInterval The number of milliseconds
     * to wait to post client-side logs to the server.  Default is 5000.
     */

    /**
     * @cfg {Number} logMaxEntries The max number of log messages to
     * display in the UI.  Note: does not affect the number of logs sent to
     * server.  Default is 1000.
     */

    function renderPage(config) {
        externalAuthentication = config.externalAuthentication || false;
        nativeLogin = Ext.valueFrom(config.nativeLogin, true);

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
                        isTitleMessageName: tfDef.isTitleMessageName,
                        widgetContainerXtype: tfDef.widgetContainerXtype
                    });
                }
            });
        }

        var createViewportFn = function() {
            var viewport;
            
            //Hack
            if (RP.taskflow.prototype && RP.taskflow.prototype.TaskflowFrame) {
                viewport = new RP.upb.prototype.Viewport({
                    id: viewPortId,
                    taskflowFrameItemId: taskflowFrameItemId,
                    externalAuthentication: externalAuthentication,
                    taskflows: taskflows,
                    taskflowsEnabled: taskflowsEnabled,
                    modules: modules
                });
            }
            else {
                viewport = new RP.upb.Viewport({
                    id: viewPortId,
                    externalAuthentication: externalAuthentication,
                    centerItems: [new RP.taskflow.TaskflowFrame({
                        itemId: taskflowFrameItemId,
                        navRegion: RP.globals.NAV_REGION,
                        navTitle: activeModule ? RP.getMessage(activeModule.label) : "",
                        taskflows: taskflows,
                        taskflowsEnabled: taskflowsEnabled,
                        modules: modules
                    })]
                });
            }
            
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

    /**
     * Logs messages appropriately, given message, file, and line number where
     * an error occured.
     * @param {String} message The error message
     * @param {String} file (Optional) The file that the error occured in
     * @param {Number/String} line (Optional) The line number of the error
     */
    var onErrorHandler = function(message, file, line) {
        var msg = "An error occured ";
        if(message.indexOf("nsIXMLHttpRequest.getAllResponseHeaders") >= 0) {
            return;
        }
        if (file) {
            msg += "in file " + file;
        }
        if (line) {
            msg += " at line " + line;
        }
        msg += "\n\n";
        // Check for cross site scripting issue.
        if (message == "Script error.") {
            message += "\n\nThe message for this error may have been lost due to your browser being at a different domain than the stashURL and staticURL in your rpweb.xml config file.";
        }
        try {
            //HACK.  In order for Ext.MessageBox to render correctly, layouts
            //cannot be suspended.
            while(Ext.AbstractComponent.layoutSuspendCount > 0){
                Ext.resumeLayouts(true);
            }
        
            logger.logError(msg + message);
            Ext.defer(RP.showErrorDialog, 1, RP, [msg + message]);
        }
        catch (e) {
        }
        //return false so that the browser prints the error message to the console
        return false;
    };

    return {

        /**
         * Bootstraps a page load. Determines the active module and taskflow,
         * and activates them.
         * @param {Object} config Options which were initialized beyond the
         * default values.
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

            var scriptUrls = [];
            // first add all module scripts
            Ext.Array.each(RP.taskflow.ModuleRegistry.getAllSorted(), function(node) {
                Ext.Array.each(node.scriptUrls, function(scriptUrl) {
                    scriptUrls.push(scriptUrl);
                });
            });
            // now add all taskflow scripts for the active module
            Ext.each(RP.taskflow.TaskflowRegistry.getAll(), function(node) {
                Ext.each(node.scriptUrls, function(scriptUrl) {
                    scriptUrls.push(scriptUrl);
                });
            });
            // load the scripts
            if (!Ext.isEmpty(scriptUrls)) {
                RP.util.ScriptLoader.load(scriptUrls, Ext.emptyFn, Ext.emptyFn);
            }

            Ext.onReady(function() {
                window.onerror = onErrorHandler;

                // Make the components stateful.
                Ext.state.Manager.setProvider(new Ext.state.CookieProvider({
                    expires: new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 365))
                }));

                // This is to allow for possibly other dynamic scripts getting loaded (message packs
                // and other types of scripts) as part of the stash library load)...
                RP.util.ScriptLoader.onReady(function() {
                    renderPage(config);

                    // Warn about possible cross-site scripting issues
                    if (RP.globals.getValue("SERVER_TYPE") !== "production" && window.console && window.console.warn) {
                        var host = window.location.host;
                        var staticUrl = RP.globals.getPath("STATIC");
                        var stashUrl = RP.stash.STASH_ROOT;
                        if (!RP.String.contains(staticUrl.toLowerCase(), host.toLowerCase())) {
                            console.warn("Your staticURL: " + staticUrl + " is a different host that your browser is at: " + host + ". This can cause odd cross-site scripting issues.");
                        }
                        if (!RP.String.contains(stashUrl.toLowerCase(), host.toLowerCase())) {
                            console.warn("Your stashURL: " + stashUrl + " is a different host that your browser is at: " + host + ". This can cause odd cross-site scripting issues.");
                        }
                    }

                    if (RP.globals.getValue("SERVER_TYPE") === "production" && RP.globals.getValue("UNSTABLE") === "true") {
                        RP.upb.UnstableEnvironmentWindow.show();
                    }

                    RP.event.AppEventManager.register(RP.upb.AppEvents.ModuleContextChanged, function() {
                        RP.util.Helpers.reload(1);
                    });
                });
            });
        },

        /**
         *  used to retrieve a reference to the taskflow frame such that
         *  additional component operations may be invoked. Uses
         *  {@link #viewPortId} and {@link Ext#getCmp}.
         */
        getTaskflowFrame: function() {
            return Ext.getCmp(viewPortId).down("#" + taskflowFrameItemId);
        },

        /**
         * Tells whether external authentication is set to true
         * @return {Boolean} Returns {@link #externalAuthentication}.
         */
        isExternallyAuthenticated: function() {
            return externalAuthentication || false;
        },

        /**
         * Tells whether native login is set as true.
         * @return {Boolean} Returns {@link #nativeLogin}
         */
        isNativeLogin: function() {
            return nativeLogin;
        }
    };
})();

/**
 * @member RP
 * @method getTaskflowFrame
 * Shorthand for {@link RP.upb.PageBootstrapper#getTaskflowFrame}
 */
RP.getTaskflowFrame = RP.upb.PageBootstrapper.getTaskflowFrame;
//////////////////////
// ..\js\..\..\stashLibEndLoad.js
//////////////////////
RP.stash.api.endLoadLib();
