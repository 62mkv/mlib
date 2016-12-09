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
// ..\js\locale\Dispatch.js
//////////////////////
Ext.ns("RP.locale");

/**
 * @namespace RP.locale
 * @class Dispatch
 * @singleton
 * @extends none
 * API for internationalizing messages and date formats....
 */
RP.locale.Dispatch = function() {
    // Message dictionary: holds all messages for the current message; key = message name, value = message value
    var msgDict = {};
    
    // Locale Pack Definitions dictionary; key = locale pack name, value = Object { url }
    var localePacksDict = {};
    
    // URLs to append for locale definitions, used mainly for customization. key = locale pack name,
    // value = Array of String
    var urlAppendDict = {};
    var messageKeyEnabled = !Ext.isEmpty(Ext.util.Cookies.get("rp-showmessages")) && RP.globals.getValue("SERVER_TYPE") !== "production";
    //
    // Utility functions.
    //
    
    // Define a locale pack. Will overwrite a previous definition if they have the same name.
    var defineLocalePack = function(lp, override) {
        if (!override && localePacksDict[lp.name]) {
            return;
        }
        
        // Override means to clear everything, including 'append' URLs...
        if (override && urlAppendDict[lp.name]) {
            logger.logTrace("[Dispatch] Clearing 'append' URLs for locale pack '" + lp.name + "'");
            delete urlAppendDict[lp.name];
        }
        
        localePacksDict[lp.name] = {
            url: lp.url
        };
    };
    
    // Gets a locale pack definition's property.
    var getLocalePackProperty = function(localePackName, property) {
        var localePackDef = localePacksDict[localePackName];
        
        if (!localePackDef) {
            return null;
        }
        return localePackDef[property];
    };
    
    var getLocalePackURLs = function(name) {
        var url = getLocalePackProperty(name, "url");
        
        if (!url || url.length === 0) {
            return null;
        }
        
        if (!Ext.isArray(url)) {
            url = [url];
        }
        
        if (urlAppendDict[name]) {
            url = url.concat(urlAppendDict[name]);
        }
        
        var urls = [];
        Ext.each(url, function(u) {
            urls.push(u);
        });
        return urls;
    };
    
    // Tell browser to include a new script file
    var generateIncludeScriptHTML = function(url) {
        return "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
    };
    
    return {
        isMessageKeyEnabled: function() {
            return messageKeyEnabled;
        },

        /**
         * Define one or more locale packs.
         * @param (Object or array of Object) locale pack definition(s). Each object must have the 'name' and 'url' properties
         * @param (boolean) override Flag indicating whether or not to override existing message pack definition(s)
         */
        defineLocalePacks: function(defs, override) {
            if (Ext.isArray(defs)) {
                // Array of definitions.
                Ext.each(defs, function(lp) {
                    defineLocalePack(lp, override);
                });
            }
            else {
                // Single definition.
                defineLocalePack(defs, override);
            }
        },
        
        /**
         * Append one or more URLs to an existing locale pack definition. Definition may
         * be defined at a later point. These URLs will be loaded *after* the ones
         * defined in 'defineLocalePacks()'
         * @param {String} name The locale pack name
         * @param {String or Array of String} urls URL(s) to append
         */
        appendLocalePackURL: function(name, urls) {
            var d = urlAppendDict[name];
            
            if (!d) {
                d = [];
                urlAppendDict[name] = d;
            }
            
            if (!Ext.isArray(urls)) {
                urls = [urls];
            }
            
            Ext.each(urls, function(url) {
                logger.logTrace("[Dispatch] Adding to 'append' URLs for locale pack '" + name + "': " + url);
                d.push(url);
            });
        },
        
        /**
         * Download the named locale pack. This is done asynchronously, as it generates a script
         * tag to tell the browser to include a .js file with the locales we want. The .js file
         * with the locales should call this singleton's setLocales() method to store locales.
         * @param (String) name The locale pack name
         */
        includeLocalePack: function(name) {
            var urls = getLocalePackURLs(name);
            RP.util.ScriptLoader.loadSerial(urls, Ext.emptyFn, Ext.emptyFn);
        },
        
        /**
         * Set multiple message values
         * @param (String) nameSpace Common namespace for all messages in messageObj.
         * @param (Object) messagesObj An object with properties containing message values.
         */
        setMessages: function(nameSpace, messagesObj) {
            if (nameSpace === null) {
                nameSpace = "";
            }
            
            if (nameSpace.length > 0 && nameSpace.lastIndexOf('.') != nameSpace.length - 1) {
                nameSpace += ".";
            }
            
            // Loop through all of the object's properties, and for ones whose values are strings,
            // store them...
            var value;
            for (var property in messagesObj) {
                value = messagesObj[property];
                
                if (typeof(value) === "string") {
                    msgDict[nameSpace + property] = value;
                }
            }
        },
        
        /**
         * Returns a named message. If not found will return "{name}" where name is the message
         * name being passed in.
         * @param (String) name The message name
         * @return {String} The current locale's message value
         */
        getMessage: function(name) {
            if (typeof msgDict[name] === "undefined") {
                logger.logError("[Dispatch] Message not found: " + name);
            }
            
            var message = (msgDict[name] || name);
            if (messageKeyEnabled) {
                message = String.format('<span title="{0}">{1}</span>', name, message);
            }
            return message;
        },

         /**
         * Function will be called after 3 s or once message is in Dictionary
         * @param {String} name The message name
         * @param {Function} fn The function to be called
         * @param {Int} time How long to try to call waitForMessageLoaded before timeout
         */
        waitForMessageLoaded: function(msg, fn, time) {
            time = (time + 100) || 100;

            try {
                var message = this.getMessage(msg);

                if (message !== msg || time >= 3000) {
                    fn.call();
                    return;
                }
            } catch (e) {
            }

            Ext.defer(this.waitForMessageLoaded, 100, this, [msg, fn, time]);
        }
    };
}();


// Shortcuts.
RP.getMessage = RP.locale.Dispatch.getMessage;
//////////////////////
// ..\js\Ajax.js
//////////////////////
Ext.ns("RP");

RP.Ajax = Ext.Ajax; // backwards compatibility
/**
 * @class RP.Ajax
 * @extends Ext.data.Connection
 * @singleton
 *
 * Overrides to the standard Ext.Ajax class.
 */
Ext.apply(RP.Ajax, {
    /**
     * Remove the default headers from the request.
     * @private
     */
    defaultHeaders: null,
    
    /**
     * Provides a way to check for any ajax calls that are still being processed
     * @private
     */
    activeRequests: 0,
    
    /**
     * Display an exception dialog related to an ajax request.  This may be used
     * by applications to display a dialog on specific exceptions.
     *
     * @param {Number} status The status number returned by the REFSResponse.
     * @param {String} errorMessage The localized message that should be displayed to the user.
     */
    displayExceptionDialog: function(status, errorMessage) {
        var template = "{0}<br /><br /> {1}<br />({2}: {3})";
        
        // For apps not using the REFS backend...
        if (!RP.REFSExceptionCodes) {
            RP.REFSExceptionCodes = {
                OK: 0
            };
        }
        
        var dialogMessage = "";
        if (status > RP.REFSExceptionCodes.OK) {
            dialogMessage = RP.getMessage("rp.common.exception.dialogMessage");
        }
        else 
            if (status < RP.REFSExceptionCodes.OK) {
                dialogMessage = RP.getMessage("rp.common.exception.dialogUnexpectedMessage");
            }
        
        Ext.Msg.alert(RP.getMessage("rp.common.exception.dialogTitle"), String.format(template, dialogMessage, errorMessage, RP.getMessage("rp.common.exception.dialogStatusLabel"), status));
    },
    
    /**
     * Make a request using standard text form parameters.  To disable the default
     * error handling, specify 'disableExceptionHandling' as true.
     *
     * @param {Object} params The parameters object used to call {@link Ext.Ajax.request}
     */
    requestWithTextParams: function(params) {
        params.headers = RP.util.Object.mergeProperties(params.headers || {}, {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
        });
        Ext.Ajax.request(params);
    },
    
    /**
     * Make a request using JSON parameters.
     *
     * @param {Object} params The parameters object used to call {@link Ext.Ajax.request}
     */
    requestWithJSONParams: function(params) {
        params.headers = RP.util.Object.mergeProperties(params.headers || {}, {
            "Content-Type": "application/json; charset=UTF-8"
        });
        Ext.Ajax.request(params);
    }
});


RP.Ajax.addEvents(/**
 * @event applicationrequestexception
 * Fires as a result of an exception being thrown from the application server.
 * The response object should contain a reference in data of the response coming
 * from the server.
 *
 * The function handler may return a boolean flag that will stop any additional dialog
 * messages from being displayed to the user.  Indicating "false" is stating
 * that the error handling has been completed for the exception thrown.
 *
 * @param {Connection} conn This Connection object.
 * @param {Object} response The response object.
 * @param {Object} options The options config object passed to the {@link #request} method.
 * @return {Boolean} False to stop additional error handling.
 */
"applicationrequestexception", /**
 * @event sessionterminated
 *
 * Fires when the Ajax request senses that the user's session is deemed terminated.
 */
"sessionterminated");

// global registration of all Ajax calls that fail
Ext.Ajax.on('requestexception', function(conn, resp, options) {
    Ext.Ajax.activeRequests--;
    
    // If the user is leaving the page, swallow the exception since the exception came from
    // the browser killing the ajax requests during it's unload process.
    if (window.leavingPage) {
        return;
    }
    
    var urlMsg = options.CallMethod ? "Proxy to " + options.CallMethod : options.url;
    logger.logError(String.format("<br /><span style='padding-left: 3em'>Failed - URL: {0}</span><br /><span style='padding-left: 3em'> Parameters: {1}</span> <br /><span style='padding-left: 3em'>Status Text: {2}</span>", urlMsg, options.params, resp.statusText));
    
    // If disableExceptionHandling is set to true then we don't want to display a message to the user
    if (!Ext.isBoolean(options.disableExceptionHandling) || !options.disableExceptionHandling) {
        var exceptionMessageIdRoot = "rp.common.exception.";
        var exceptionMessage = RP.getMessage(exceptionMessageIdRoot + status);
        
        if (exceptionMessage === exceptionMessageIdRoot) {
            Ext.Ajax.displayExceptionDialog(resp.status, RP.getMessage(exceptionMessageIdRoot + "GENERAL_EXCEPTION"));
        }
        else {
            Ext.Ajax.displayExceptionDialog(resp.status, exceptionMessage);
        }
    }
});

// global registration of all Ajax requests before they go out
Ext.Ajax.on('beforerequest', function(conn, config) {
    Ext.Ajax.activeRequests++;
});

// global registration of all Ajax requests that have successfully completed
Ext.Ajax.on('requestcomplete', function(conn, response, options) {
    Ext.Ajax.activeRequests--;
    
    if (window.location.pathname.indexOf(RP.globals.getPath('LOGIN')) === -1 && Ext.isFunction(response.getResponseHeader) && response.getResponseHeader("authenticated") && response.getResponseHeader("authenticated") == "false") {
        Ext.Ajax.fireEvent("sessionterminated");
    }
    
    if (!RP.REFSExceptionCodes) {
        return;
    }
    
    // examine the response object and see if there were any problems
    try {
        var responseObj = Ext.util.JSON.decode(response.responseText);
        response.responseObject = responseObj;
        var status = responseObj.status;
        
        if (status !== RP.REFSExceptionCodes.OK) {
            logger.logError("Ajax response error: " + responseObj.message);
            
            // handleException is used in conjunction with the requestexception
            // event.  If the response for handling the event returns true, 
            // then the standard error dialog message will be displayed to
            // to the user.  If a false, is returned, then it will be assumed
            // that the exception was handled.
            var handleException = true;
            
            // if an exception occurred while retrieving the data 
            // from the application server then funnel it back to the
            // application handlers for requestexception.
            if (status === RP.REFSExceptionCodes.HTTP_PROXY_EXCEPTION) {
                handleException = Ext.Ajax.fireEvent("applicationrequestexception", conn, response, options);
            }
            
            // If disableExceptionHandling is set to true then we don't want to display a message to the user
            if (handleException &&
            (!Ext.isBoolean(options.disableExceptionHandling) || !options.disableExceptionHandling)) {
                var messageId = RP.REFSExceptionMessages[status];
                Ext.Ajax.displayExceptionDialog(status, RP.getMessage("rp.common.exception." + messageId));
            }
        }
    } 
    catch (e) {
        logger.logError("An error occurred while handling the response.");
        logger.logError(e);
    }
});
//////////////////////
// ..\js\Math.js
//////////////////////
/**
 * @class Math
 */
Ext.applyIf(Math, {
  /**
   * Round number cValue to iScale decimal places, return rounded number.
   * For example:   cValue  iScale  return
   *                1.04      1     1.1
   *                1.5       0     2
   *                0.285     2     0.29        special case
   * 0.285 is a special case where .285*100 = 28.499999999999996
   * which rounds incorrectly. To fix this, we scale by one more, and then divide by 10. Scaling by one
   * more results in an integer which takes away possible floating-point errors.
   * @param {Number} cValue value to round.
   * @param {Number} iScale scale to round to.
   * @return {Number} The rounded number.
   */
  roundDecimal: function(cValue, iScale)
  {
    var iMultiplier = Math.pow(10, iScale);
    cValue = (Math.round((cValue * (iMultiplier * 10)) / 10) / iMultiplier);
    return cValue;
  }
});
//////////////////////
// ..\js\String.js
//////////////////////
/**
 * @class String
 */
Ext.applyIf(String.prototype, {
  /**
   * Returns true if the string contains the specified value.
   * @param {String} value
   * @return {Boolean}
   */
  contains: function(value) {
    return this.indexOf(value) !== -1;
  }
});
//////////////////////
// ..\js\Ext.js
//////////////////////
Ext.applyIf(Ext, {
    
    /**
     * True if the detected browser is mobile safari (iPad, iPod, iPhone).
     * @type Boolean 
     */
    isMobileSafari: function() {
        return navigator.userAgent.toLowerCase().indexOf("mobile") !== -1 && Ext.isSafari;
    }(),
    
    /**
     * true if current userAgent is set to IE9 and Document Mode is IE9.
     * @type Boolean
     */
    isIE9: function() {
        return Ext.isIE && /msie 9/.test(navigator.userAgent.toLowerCase()) && document.documentMode == 9; 
    }(),
    
    /**
     * Checks the current Document Mode of IE. document.documentMode is a IE only attribute.
     * @param {Number} modeNumber The number of the document mode you wish to check
     * @return {Boolean} true if Document Mode is modeNumber
     */
    isIEDocMode: function(modeNumber) {
        return Ext.isIE && document.documentMode == modeNumber; 
    }
});

Ext.apply(Ext, {
    isIE6: function() {
        return Ext.isIE && /msie 6/.test(navigator.userAgent.toLowerCase());
    }()
});
//////////////////////
// ..\js\Overrides.js
//////////////////////
// Override, like a boss!

Ext.override(Ext.grid.GridPanel, {
    applyState: function(state) {
        var cm = this.colModel, cs = state.columns, store = this.store, s, c, colIndex;
        
        if (cs) {
            for (var i = 0, len = cs.length; i < len; i++) {
                s = cs[i];
                c = cm.getColumnById(s.id);
                if (c) {
                    colIndex = cm.getIndexById(s.id);
                    cm.setState(colIndex, {
                        hidden: s.hidden,
                        width: s.width,
                        sortable: s.sortable
                    });
                    if (colIndex != i) {
                        cm.moveColumn(colIndex, i);
                    }
                }
            }
        }
        if (store) {
            s = state.sort;
            if (s) {
                store[store.remoteSort ? 'setDefaultSort' : 'sort'](s.field, s.direction);
            }
            s = state.group;
            if (store.groupBy) {
                if (s) {
                    store.groupBy(s);
                }
                else {
                    store.clearGrouping();
                }
            }
            
        }
        var o = Ext.apply({}, state);
        delete o.columns;
        delete o.sort;
        Ext.grid.GridPanel.superclass.applyState.call(this, o);
    },
    
    getState: function() {
        var o = {
            columns: []
        }, store = this.store, ss, gs;
        
        for (var i = 0, c; (c = this.colModel.config[i]); i++) {
            o.columns[i] = {
                id: c.id,
                width: c.width
            };
            if (c.hidden) {
                o.columns[i].hidden = true;
            }
            if (c.sortable) {
                o.columns[i].sortable = true;
            }
        }
        if (store) {
            ss = store.getSortState();
            if (ss) {
                o.sort = ss;
            }
            if (store.getGroupState) {
                gs = store.getGroupState();
                if (gs) {
                    o.group = gs;
                }
            }
        }
        return o;
    }
});

(function() {
    var oldGetErrors = Ext.form.DateField.prototype.getErrors;
    Ext.override(Ext.form.DateField, {
    getErrors: function(value) {
        var errors = oldGetErrors.createDelegate(this, [value])();
        for(var i = 0; i < errors.length; i++) {
            errors[i] = errors[i].replace(this.format, Date.translateFormat(this.format));
        }
        return errors;
    }
});
})(); 

//RPWEB-5883
//This prevents some of the console errors of "El._flyweights is undefined" in firefox.
//This can be removed when we no longer user an iframe.
Ext.Element.fly = function(el, named){
    var ret = null;
    named = named || '_global';
    
    el = Ext.getDom(el);
    if (el) {
        if(Ext.Element._flyweights === undefined) {  //added this check
            Ext.Element._flyweights = {};
        }
        (Ext.Element._flyweights[named] = Ext.Element._flyweights[named] || new Ext.Element.Flyweight()).dom = el;
        ret = Ext.Element._flyweights[named];
    }
    return ret;
};
//////////////////////
// ..\js\Range.js
//////////////////////
/*
 * RPWEB-5683 Fix for IE9 to be able to use Ext.MessageBox.
 */
if ((typeof Range !== "undefined") && !Range.prototype.createContextualFragment) {
  Range.prototype.createContextualFragment = function (html) {
    var frag = document.createDocumentFragment(),
        div = document.createElement("div");
    frag.appendChild(div);
    div.outerHTML = html;
    return frag;
  };
}
//////////////////////
// ..\js\TabPanel.js
//////////////////////
/**
 * @class RP.TabPanel
 * @extends Ext.TabPanel
 *
 * A TabPanel override that automatically makes the tab
 * scrollers wider for mobile devices.
 */
Ext.override(Ext.TabPanel, {
    /**
     * @cfg {Boolean} disableWideScrollers
     * Set to true to not use wide scrollers on mobile devices.
     */
    disableWideScrollers: false,
    
    tabScrollerWidth: 18,
    
    // overridden because the WEbKit hack set the scrollerWidth manually.
    autoScrollTabs: function(){
        // automatically use wider tabs if it is a mobile device
        // may need to update the condition to account for more
        // devices in the future, such as android...
        if (this.disableWideScrollers !== true && Ext.isMobileSafari) {
            this.addClass("rp-mobile-tab-panel");
            this.tabScrollerWidth = 29;
        }
        
        this.pos = this.tabPosition == 'bottom' ? this.footer : this.header;
        var count = this.items.length, ow = this.pos.dom.offsetWidth, tw = this.pos.dom.clientWidth, wrap = this.stripWrap, wd = wrap.dom, cw = wd.offsetWidth, pos = this.getScrollPos(), l = this.edge.getOffsetsTo(this.stripWrap)[0] + pos;
        
        if (!this.enableTabScroll || cw < 20) { // 20 to prevent display:none issues
            return;
        }
        if (count === 0 || l <= tw) {
            // ensure the width is set if there's no tabs
            wd.scrollLeft = 0;
            wrap.setWidth(tw);
            if (this.scrolling) {
                this.scrolling = false;
                this.pos.removeClass('x-tab-scrolling');
                this.scrollLeft.hide();
                this.scrollRight.hide();
                // See here: http://extjs.com/forum/showthread.php?t=49308&highlight=isSafari
                if (Ext.isAir || Ext.isWebKit) {
                    wd.style.marginLeft = '';
                    wd.style.marginRight = '';
                }
            }
        }
        else {
            if (!this.scrolling) {
                this.pos.addClass('x-tab-scrolling');
                // See here: http://extjs.com/forum/showthread.php?t=49308&highlight=isSafari
                if (Ext.isAir || Ext.isWebKit) {
                    wd.style.marginLeft = this.tabScrollerWidth + 'px';
                    wd.style.marginRight = this.tabScrollerWidth + 'px';
                }
            }
            
            tw -= wrap.getMargins('lr');
            wrap.setWidth(tw > 20 ? tw : 20);
            if (!this.scrolling) {
                if (!this.scrollLeft) {
                    this.createScrollers();
                }
                else {
                    this.scrollLeft.show();
                    this.scrollRight.show();
                }
            }
            this.scrolling = true;
            if (pos > (l - tw)) { // ensure it stays within bounds
                wd.scrollLeft = l - tw;
            }
            else { // otherwise, make sure the active tab is still visible
                this.scrollToTab(this.activeTab, false);
            }
            this.updateScrollButtons();
        }
    }
});
//////////////////////
// ..\js\TimeSpan.js
//////////////////////
/**
 * @class RP.TimeSpan
 * @namespace RP
 * @method Constructor
 * @param {Object} ms The number of milliseconds in this span of time.
 */
RP.TimeSpan = function(ms) {
    this.ms = ms;
};

/**
 * @namespace RP
 * @method Adds this timespan to another, and returns a new timespan.
 * @param {Object} ts The timespan to add to this timespan.
 * @return {Object} a new TimeSpan instance
 */
RP.TimeSpan.prototype.plus = function(ts) {
    if (ts.ms) {
        return new RP.TimeSpan(this.ms + ts.ms);
    }
    return this;
};

/**
 * @namespace RP
 * @method Subtracts a timespan.
 * @param {Object} ts The timespan to subtract from this timespan.
 * @return {Object} a new TimeSpan instance
 */
RP.TimeSpan.prototype.minus = function(ts) {
    if (ts.ms) {
        return new RP.TimeSpan(this.ms - ts.ms);
    }
    return this;
};

/**
 * @namespace RP
 * @method Returns the number of milliseconds in this timespan.
 * @return {Number} number of milliseconds in this timespan.
 */
RP.TimeSpan.prototype.getTotalMilliseconds = function() {
    return this.ms;
};

/**
 * @namespace RP
 * @method Returns the total number of milliseconds represented by the time span.
 * @return {Number} the total number of milliseconds
 */
RP.TimeSpan.prototype.totalMilliseconds = function() {
    return this.getTotalMilliseconds();
};

/**
 * @namespace RP
 * @method Returns the total number of seconds represented by the time span.
 * @return {Number} the total number of seconds
 */
RP.TimeSpan.prototype.totalSeconds = function() {
    return this.getTotalMilliseconds() / 1000.0;
};

/**
 * @namespace RP
 * @method Returns the total number of minutes represented by the time span.
 * @return {Number} the total number of minutes
 */
RP.TimeSpan.prototype.totalMinutes = function() {
    return this.getTotalMilliseconds() / 60000.0;
};

/**
 * @namespace RP
 * @method Returns the total number of hours represented by the time span.
 * @return {Number} the total number of hours
 */
RP.TimeSpan.prototype.totalHours = function() {
    return this.getTotalMilliseconds() / 3600000.0;
};

/**
 * @namespace RP
 * @method Returns the total number of days represented by the time span.
 * @return {Number} the total number of days
 */
RP.TimeSpan.prototype.totalDays = function() {
    return this.getTotalMilliseconds() / 86400000.0;
};
// Shorthand.
// DEPRECATED
var TimeSpan = function(ms) {
    this.ms = ms;
};

RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "plus", "plus");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "minus", "minus");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "getTotalMilliseconds", "getTotalMilliseconds");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "totalMilliseconds", "totalMilliseconds");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "totalSeconds", "totalSeconds");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "totalMinutes", "totalMinutes");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "totalHours", "totalHours");
RP.util.DeprecationUtils.deprecatedInstanceMethod("TimeSpan", "RP.TimeSpan", "totalDays", "totalDays");
//////////////////////
// ..\js\FiscalDatePicker.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.FiscalDatePicker
 * @extends Ext.DatePicker
 * 
 * An extension that displays fiscal weeks.
 */
RP.FiscalDatePicker = Ext.extend(Ext.DatePicker, {
    
    initComponent: function() {
        if (Ext.isNumber(RP.globals.getValue("CALENDAR").startDayOfWeek)) {
            this.startDay = RP.globals.getValue("CALENDAR").startDayOfWeek;
        }
        
        RP.FiscalDatePicker.superclass.initComponent.call(this);
    },
    
    /**
     * Hook into the onRender template method to render the fiscal week column.
     * @private
     */
    onRender: function() {
        RP.FiscalDatePicker.superclass.onRender.apply(this, arguments);
        
        if (Ext.isEmpty(RP.globals.getValue("CALENDAR").fiscalPeriods)) {
            // if there is no data, there is nothing to render.  Skip the week rendering process.
            return;
        }
        
        this.addClass("rp-fiscal-date-picker"); // class providing adjusted widths/borders
        
        var table = this.el.child(".x-date-inner");
        
        // add the header row
        var headerMarkup = "<th><span>{0}</span></th>";
        var weekHeader = RP.getMessage("rp.common.components.fiscalDatePicker.weekHeader");
        var headerRow = table.child("tr");
        headerRow.insertHtml("afterBegin", String.format(headerMarkup, weekHeader));
        
        // add the regular rows
        var rowMarkup = '<td><a class="rp-fiscal-week" href="#" hidefocus="on" tabIndex="1"><em><span></span></em></a></td>';
        var rows = table.select("tbody>tr");
        
        // just for testing purposes...
        rows.each(function(row, composite, index) {
            row.insertHtml("afterBegin", rowMarkup);
        });
        
        // hold on the week cells for updating later
        this.weekCells = table.select(".rp-fiscal-week");
        
        this.mon(this.eventEl, "click", this.onFiscalWeekClick, this, {
            delegate: ".rp-fiscal-week"
        });
        
        // force the datepicker to recalculate its width to account for the extra column
        delete this.internalRender; // reset the flag used by update so that it recalculates
        this.update(this.getValue(), true);
    },
    
    update: function(date) {
        RP.FiscalDatePicker.superclass.update.apply(this, arguments);
        
        if (!this.weekCells) { return; }
        
        this.convertFiscalPeriodsToDates();
        
        var fiscalPeriods = RP.globals.getValue("CALENDAR").fiscalPeriods;
        
        this.renderWeekNumber(fiscalPeriods);
    },
    
    convertFiscalPeriodsToDates: function() {
        var fiscalPeriods = RP.globals.getValue("CALENDAR").fiscalPeriods;
        
        if (Ext.isDate(fiscalPeriods[0].startDate)) {
            return;
        }

        Ext.each(fiscalPeriods, function(fiscalPeriod) {
            fiscalPeriod.startDate = new Date(fiscalPeriod.startDate);
            fiscalPeriod.endDate = new Date(fiscalPeriod.endDate);
        });
    },
    
    /**
     * Determine the fiscal period based on the date passed.
     * 
     * @param {Object} date The current date.
     * @return {Object} Null if the fiscal period doesn't exist, or the index of the fiscal period.
     */
    findFiscalPeriod: function(fiscalPeriods, dates) {
        for (var i = 0; i < dates.getCount(); i++) {
            var date = new Date(dates.item(i).dom.dateValue);
            
            for(var j = 0; j < fiscalPeriods.length; j++) {
                if (date.between(fiscalPeriods[j].startDate, fiscalPeriods[j].endDate)) {
                    return {
                        fiscalPeriodIdx: j, 
                        beginWeek: date
                    };
                }
            }
        }
        
        return {
            fiscalPeriodIdx: null, 
            beginWeek: null
        };
    },
    
    /**
     * Render the week number into the week column.
     * 
     * @param {Object} fiscalPeriods The fiscal periods from the page context.
     */
    renderWeekNumber: function(fiscalPeriods) {
        for (var i = 0; i < this.weekCells.getCount(); i++) {
            var weekNumber = "&nbsp;";
            var weekCell = this.weekCells.item(i);
            var dates = weekCell.parent().parent().select('.x-date-date');
            
            // get the fiscal period in the context of the current date row.
            var fiscalContext = this.findFiscalPeriod(fiscalPeriods, dates);
            var beginOfWeek = fiscalContext.beginWeek;
            var fiscalPeriodIdx = fiscalContext.fiscalPeriodIdx;
            
            if (fiscalPeriodIdx !== null) {
                var fiscalPeriod = fiscalPeriods[fiscalPeriodIdx];
                var offset = fiscalPeriod.startDate.getDayOfYear();
                var weekOffset = this.determineBeginningOfYearWeekOffset(this.startDay, fiscalPeriod);
                
                var dayOfYear = beginOfWeek.getDayOfYear();
                if (dayOfYear < offset) {
                    // 365 is used in this adjustment to ensure that the dayOfYear is 
                    // greater than the beginning offset.
                    // For Example, Start Of Year is 10/1/2010, offset will be 273.
                    // If the current date is 12/01/2010, the dayOfYear is 334.  The difference will
                    // calculate out to be a positive number.
                    // In the case that the calendar rolls over to the next year, 1/1/2011, the dayOfYear is
                    // 0.  This case, the dayOfYear needs to be adjusted forward by a full year to ensure that the
                    // calculations are positive.
                    dayOfYear += 365 + (fiscalPeriod.startDate.isLeapYear() || fiscalPeriod.endDate.isLeapYear() ? 1 : 0);
                }
                
                // take the difference between the two and divide by 7 (number of days in a week.)
                // this roughly equates to the week number in 0-based form.  If the
                // beginning of the year is not the first day of the week, then the
                // weekOffset will take into account the adjustment to push the week count forward.
                weekNumber = Math.floor((dayOfYear - offset) / 7) + 1 + weekOffset;
                
                // if the fiscal period ends in this row and there is additional fiscal data, add
                // the /1 to the display to indicate that the beginning of the next week is in the same row. 
                if (this.endOfFiscalYearInRow(weekCell.parent(), fiscalPeriod) && this.startOfFiscalYearInRow(weekCell.parent(), fiscalPeriods[fiscalPeriodIdx + 1])) {
                    weekNumber += "/1";
                }
                else if (this.startOfFiscalYearInRow(weekCell.parent(), fiscalPeriods[fiscalPeriodIdx])) {
                    weekNumber = "1";
                }
            }
            
            weekCell.child("span").update(weekNumber.toString());
        }
    },
    
    /**
     * If the beginning of the year is in the middle of the week, the 
     * week offset should be pushed forward to indicate that the beginning
     * of the year has already been encountered.  The next week should be considered
     * week 2.
     * 
     * @param {Object} startDayOfWeek The start day of the calendar.
     * @param {Object} fiscalPeriod The fiscal period of the current context.
     */
    determineBeginningOfYearWeekOffset: function(startDayOfWeek, fiscalPeriod) {
        var dayOfWeek = fiscalPeriod.startDate.getDay();
        if (startDayOfWeek != dayOfWeek) {
            return 1;
       }
       return 0;
    },
    
    _checkFiscalYear: function(weekRow, fiscalPeriod, checkFn) {
        var dates = weekRow.parent().select('.x-date-date');

        if (fiscalPeriod) {
            for(var i = 0; i < dates.getCount(); i++) {
                var currentDateTime = dates.item(i).dom.dateValue;
                if(checkFn.call(this, fiscalPeriod, currentDateTime)) {
                    return true;
                }
            }
        }
        
        return false;
    },
    endOfFiscalYearInRow: function(weekRow, fiscalPeriod) {
        return this._checkFiscalYear(weekRow, fiscalPeriod, function(fiscalPeriod, dateTime) {
            if (fiscalPeriod.endDate.getTime() === dateTime) {
                return true;
            }
            return false;
        });
    },
    
    startOfFiscalYearInRow: function(weekRow, fiscalPeriod) {
        return this._checkFiscalYear(weekRow, fiscalPeriod, function(fiscalPeriod, dateTime) {
            if (fiscalPeriod.startDate.getTime() === dateTime) {
                return true;
            }
            return false;
        });
    },
    
    /**
     * Event handler for when a fiscal week is clicked.  Calls the standard handleDateClick
     * method inherited from Ext.DatePicker passing in the first day of the week.
     * @param {Ext.EventObject} event
     * @param {HtmlElement} target
     * @private
     */
    onFiscalWeekClick: function(event, target) {
        this.handleDateClick(event, Ext.fly(target).parent().next().down(".x-date-date").dom);
    }
});

Ext.reg("rpfiscaldatepicker", RP.FiscalDatePicker);
//////////////////////
// ..\js\ux\Exception.js
//////////////////////
/**
 * @class RP.Exception
 * @extends Ext.Error
 * A custom error class to be used inside of RPWEB.
 * @constructor
 * Create a new exception.
 * @param {String} message The exception message.
 * @param {Error} innerError (optional) An inner error to store with the exception.
 */
RP.Exception = Ext.extend(Ext.Error, {
  name: "RP.Exception",
  
  constructor: function(message, innerError) {
    Ext.apply(this, {
      message: message,
      innerError: innerError
    });
    
    RP.Exception.superclass.constructor.call(this);
  },
  
  /**
   * Gets the inner error, if any, associated with this exception.
   * @return {Error} The inner error.
   */
  getInnerError: function() {
    return this.innerError;
  }
});
//////////////////////
// ..\js\ux\ContainerLayout.js
//////////////////////
/*global Ext, RP */

/**
 * @namespace Ext.layout
 * @class ContainerLayout
 * @extends ux of Ext.layout.ContainerLayout
 * 
 * Override Ext.layout.ContainerLayout for additional functionality, such as notifying
 * the container it has just been assigned a layout manager.
 */

// Fire event "rpux-layoutassign" when a layout manager is assigned to a container.
Ext.layout.ContainerLayout.prototype.setContainer =
Ext.layout.ContainerLayout.prototype.setContainer.createSequence(function(ct)
{
  if (ct)
  {
    ct.fireEvent("rpux-layoutassign", this);
  }
});
//////////////////////
// ..\js\ux\DatePicker.js
//////////////////////
/*global Ext */

// This is to work around a bug in Ext where the DatePicker control's beforeDestory is being
// called twice when destroyed and blows up because this.keyNav is null the 2nd time around.
Ext.override(Ext.DatePicker,
{
  beforeDestroy: function()
  {
    if(this.rendered)
    {
      if (this.keyNav)    // added this check
      {
        this.keyNav.disable();
        this.keyNav = null;
      }
      
      Ext.destroy(
          this.leftClickRpt,
          this.rightClickRpt,
          this.monthPicker,
          this.eventEl,
          this.mbtn,
          this.todayBtn
      );
    }
  }
});
//////////////////////
// ..\js\ux\Store.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.ux");

/**
 * @namespace RP.ux
 * @class Store
 * Extends Ext.data.Store to provide additional functionality, such as maintaining a list
 * of deleted records.
 */
RP.ux.Store = Ext.extend(Ext.data.Store,
{
  /**
    * @property isLoaded
    * @type Boolean
    * True if the store has ever been loaded.
    */
  isLoaded: false,
  
  /**
  * Construct a Store using normal Ext.data.Store config.  Additional config options:
  * trackDeletedRecords: keep track of deleted records
  * @method constructor
  */  
  constructor: function(config)
  {
    RP.ux.Store.superclass.constructor.apply(this, arguments);  
    
    if (config.trackDeletedRecords)
    {
      this._deletedRecords = [];
  
      this.on("remove", function(store, rec, index)
      {
        this._deletedRecords.push(rec);
      }, this); 
        
      this.on("load", function()
      {
        delete this._deletedRecords;
        this._deletedRecords = [];        
      }, this);
    }
    
    this.on("load", function() {
      this.isLoaded = true;
    }, this);
  },
  
  
  /**
  * If 'trackDeletedRecords' config option is set to true, then this will return an
  * array of deleted records.
  * @method getDeletedRecords
  */
  getDeletedRecords: function()
  {
    ///<summary>
    /// Extension to get the current selected index.
    ///</summary>
    return this._deletedRecords;
  }
});
//////////////////////
// ..\js\ux\MixedCollection.js
//////////////////////
/**
 * @namespace Ext.util
 * @class MixedCollection
 * Added additional functionality to Ext's MixedCollection class
 */
Ext.override(Ext.util.MixedCollection,
{
  /**
  * Get the key of an item.  Note: the speed of this is O(N), so if your collection can get
  * quite large, use something else!
  * @method keyOf
  */
  keyOf: function(item)
  {
    for (var key in this.map)
    {
      if (this.map[key] === item)
      {
        return key;
      }
    }
    return null;
  },
  
  /**
  * Removes all items beginning at the specified index from the combo array.
  * @param {int} index The index to start removing
  */
  trimAt: function(index)
  {
    if (index < this.length && index >= 0)
    {
      var count = this.length - index;
      
      this.items.splice(index, count);
      this.length -= count;
      this.keys.splice(index, count);
    }
  }
});
//////////////////////
// ..\js\ux\IFrame.js
//////////////////////
/*global Ext */

Ext.ns("Ext.ux");

/**
 * @namespace Ext.ux
 * @class IFrameComponent
 * @extends Ext.BoxComponent
 *
 * Renders an IFRAME to display external web application
 */
Ext.ux.IFrameComponent = Ext.extend(Ext.BoxComponent, 
{
  onRender: function(ct, position)
  {
    this.el = ct.createChild({ tag: 'iframe', id: 'iframe-' + this.id, frameBorder: 0, src: this.url });
  }
});
//////////////////////
// ..\js\ux\Element.js
//////////////////////
Ext.Element.addMethods(function(){
  var defaultMaskColor = "#000";
  var defaultMaskOpacity = 0.97;
  var defaultZIndex = 50000;
  var oldSetOpacity = Ext.Element.prototype.setOpacity;
  
  return {
    /**
     * Set the opacity of the element
     * @param {Float} opacity The new opacity. 0 = transparent, .5 = 50% visibile, 1 = fully visible, etc
     * @param {Boolean/Object} animate (optional) a standard Element animation config object or <tt>true</tt> for
     * the default animation (<tt>{duration: .35, easing: 'easeIn'}</tt>)
     * @return {Ext.Element} this
     */
     setOpacity : function(opacity, animate){
        var s = this.dom.style;

        if((!animate || !this.anim) && Ext.isIE9){
            s.opacity = opacity;
            return this;
        }
        else{
            return oldSetOpacity.apply(this, arguments);
        }
    },

    /**
     * Extends the Ext Element.mask() function to provide options for mask color and opacity.
     * If color and opacity not specified, default is a very dark mask (85% black).
     * @param {String} msg (optional) A message to display in the mask
     * @param {String} msgCls (optional) A css class to apply to the msg element
     * @param {Float} opacity (optional) Mask opacity; default is 0.85
     * @param {String} color (optional) Mask color; default is '#000'
     * @return {Element} The mask element
     */
    maskEx: function(msg, msgCls, opacity, color){
      color = color || defaultMaskColor;
      opacity = opacity || defaultMaskOpacity;
      
      var m = this.mask(msg, msgCls);
      m.setStyle("background-color", color);
      m.setStyle("opacity", opacity);
      m.setStyle("z-index", defaultZIndex);
    },
    
    /**
     * Removes the class and style applied by the unselectable method. Allows a user to select elements.
     * Ex. The header of a panel by default applies x-unselectable.
     */
    selectable: function() {
      this.dom.unselectable = "off";
      this.removeClass('x-unselectable');
      this.setStyle({ 
        '-moz-user-select': '',
        '-khtml-user-select': ''
      });
    },
    
    /**
     * Checks to see if the Element's innerHTML is empty ("" or "&nbsp;").
     * @return {Boolean}
     */
    isEmpty: function() {
        var html = this.dom.innerHTML.trim();
        
        return html == "" || html == "&nbsp;";
    }
  };
}());

// hack to fix Ext.Element's getAttribute to work in IE9 -- this is fixed in Ext JS 4
// appears that IE9 follows the more standardized way of accessing dom attributes
// the internal code is a direct copy from the standard source except for the following:
// old: Ext.isIE ? function(name, ns){
// new: (Ext.isIE && !Ext.isIE9) ? function(name, ns){
Ext.Element.prototype.getAttribute = (Ext.isIE && !Ext.isIE9) ? function(name, ns){
    var d = this.dom,
        type = typeof d[ns + ":" + name];

    if(['undefined', 'unknown'].indexOf(type) == -1){
        return d[ns + ":" + name];
    }
    return d[name];
} : function(name, ns){
    var d = this.dom;
    return d.getAttributeNS(ns, name) || d.getAttribute(ns + ":" + name) || d.getAttribute(name) || d[name];
};
//////////////////////
// ..\js\ux\GridView.js
//////////////////////
Ext.override(Ext.grid.GridView, {
  // private
  renderRowsForExport : function(startRow, endRow, includeColumnDelegate) {
    // pull in all the crap needed to render rows
    var g = this.grid, cm = g.colModel, dataStore = g.store, stripe = g.stripeRows;
    var colCount = cm.getColumnCount();

    if(dataStore.getCount() < 1){
        return '';
    }
    
    var columnData = this.getColumnData();

    startRow = startRow || 0;
    endRow = !Ext.isDefined(endRow) ? dataStore.getCount()-1 : endRow;

    // records to render
    var records = dataStore.getRange(startRow, endRow);

    var pushColumn = true;
    var evaluateColumn = false;
    if (includeColumnDelegate !== null && typeof includeColumnDelegate === "function") {
      evaluateColumn = true;
    }
    
    // Used to handle group views. 
    var groupField;
    var groups = [];
    if (typeof dataStore.groupField !== "undefined") {
      groupField = dataStore.getGroupState();
      groups = [];
    }
      
    var rows = [];
    for (var j = 0; j < records.length; j++) {
      var record = records[j];
      
      var row = [];
      for (var i = 0; i < colCount; i++) {
        var column = columnData[i];
        if (evaluateColumn) {
          pushColumn = includeColumnDelegate.call(this, column);
        }

        if (pushColumn) {
          // Add the group to the returned results.
          if (typeof dataStore.groupField !== "undefined") {
            var groupValue = record.data[groupField];
            if (typeof groupValue !== "undefined" && groups.indexOf(groupValue) === -1) {
              groups.push(groupValue);
              rows.push(String.format("\"{0}\"", groupValue));
            }
          }
          var columnString = column.renderer.call(column.scope, record.data[column.name], column, record, j, i, dataStore);
          var div = document.createElement("div");
          div.innerHTML = columnString;
          columnString = div.textContent || div.innerText || "";
          if (typeof columnString === "string") {
            columnString = columnString.replace(/\"/g, "\\\"");
          }
          row.push(String.format("\"{0}\"", columnString));
        }
      }
      rows.push(row.join(","));
      
    }
    
    return rows.join("\n");
  },
  
  // patch added as part of RPWEB-4042, see the comment in the JIRA issue for more details
  isHideableColumn: function(c) {
    return !c.hidden && c.hideable !== false;
  }
});
//////////////////////
// ..\js\ux\Field.js
//////////////////////
Ext.data.Field = function(config){
  // extended object to handle ISO-8601 date strings and MS JSON formatted dates (\/Date(1069689066000)\/).
  if(typeof config === "string"){
    config = {name: config};
  }
  Ext.apply(this, config);
  
  var types = Ext.data.Types;
  if (this.type) {
    if (Ext.isString(this.type)) {
        this.type = Ext.data.Types[this.type.toUpperCase()] || types.AUTO;
    }
  }
  else {
      this.type = types.AUTO;
  }

  var st = Ext.data.SortTypes;
  // named sortTypes are supported, here we look them up
  if(typeof this.sortType === "string"){
    this.sortType = st[this.sortType];
  }
  
  // set default sortType for strings and dates
  if(!this.sortType){
    switch(this.type.type){
      case "string":
        this.sortType = st.asUCString;
        break;
      case "date":
        this.sortType = st.asDate;
        break;
      default:
        this.sortType = this.type.sortType;
    }
  }

  // define once
  var stripRe = /[\$,%]/g;
  var msdateRe = /\/Date\((-?\d+)\)\//; // MS JSON date format
  
  var convertStringToDate = function(v) {
    if(!v){
      return '';
    }
    if(v instanceof Date){
      return v;
    }
    
    // Parse ISO-8601 date, assuming rpcommon is loaded...
    if ((/T(\d{2}):(\d{2})/).test(v)) {
      return new Date(v);
    }
    
    var match = v.match(msdateRe);
    if( match !== null  && match.length > 1) {
      // expects MS JSON dates of the form \/Date(1069689066000)\/
      return new Date(parseInt(v.replace(msdateRe, '$1'),10));
    }
    if(dateFormat){
      if(dateFormat === "timestamp"){
        return new Date(v*1000);
      }
      if(dateFormat === "time"){
        return new Date(parseInt(v, 10));
      }
      return Date.parseTime(v, dateFormat);
    }
    var parsed = Date.parse(v);
    return parsed ? new Date(parsed) : null;
  };

  // prebuilt conversion function for this field, instead of
  // switching every time we're reading a value
  if(!this.convert){
    var cv, dateFormat = this.dateFormat;
    switch(this.type.type){
      case "":
      case "auto":
      case undefined:
        cv = function(v){ return v; };
        break;
      case "string":
        cv = function(v){ return (v === undefined || v === null) ? '' : String(v); };
        break;
      case "integer":
      case "int":
        cv = function(v){
          return v !== undefined && v !== null && v !== '' ?
               parseInt(String(v).replace(stripRe, ""), 10) : '';
          };
        break;
      case "float":
        cv = function(v){
          return v !== undefined && v !== null && v !== '' ?
               parseFloat(String(v).replace(stripRe, ""), 10) : '';
          };
        break;
      case "bool":
      case "boolean":
        cv = function(v){ return v === true || v === "true" || v === 1; };
        break;
      case "date":
        cv = function(v) {
          return convertStringToDate(v);
        };
       break;
     case "time":
      cv = function(v) {
        var dt = convertStringToDate(v);
        
        if (!dt || dt === '') {
          return '';
        }
        return dt.formatTime();
      };
      break;
      
    case "datestring":
      cv = function(v) {
        var dt = convertStringToDate(v);
        if (!dt || dt === '') {
          return '';
        }
        return dt.formatDate(RP.core.Formats.Date.FullDateTime);
      };
      break;
    default:
      cv = this.type.convert;
      break;
    }
    this.convert = cv;
  }
  
};

Ext.data.Field.prototype = {
    dateFormat: null,
    defaultValue: "",
    mapping: null,
    sortType : null,
    sortDir : "ASC"
};
//////////////////////
// ..\js\ux\Forms.js
//////////////////////
// Ext standard email mask does not match RFC for email addresses (missing
// several special characters).
Ext.form.VTypes.emailMask = /[a-z0-9_\.\-@\+!#\$\%\&\'\*\/=\?\^_`\{\|\}\~]/i;
//////////////////////
// ..\js\ux\Template.js
//////////////////////
/**
 * Extension to allow injection of custom function to be called prior
 * to calling base 'applyTemplates'.  This is used for example, to
 * provide a function to inject additional template values for
 * extensions, etc.. 
 */

Ext.Template.prototype.apply =
Ext.Template.prototype.apply.createInterceptor(function(values) {
  if (Ext.isFunction(this.rpBeforeApplyFn)) {
		this.rpBeforeApplyFn(values);
	}
  return true;
});
//////////////////////
// ..\js\core\History.js
//////////////////////
////////////////////////////////////////////////////////////////////////////
/////  This is to override the Ext.History example because it does not work 
/////  in IE 8 when the doctype tag is specified.
////////////////////////////////////////////////////////////////////////////

/*!
 * Ext JS Library 3.0.0
 * Copyright(c) 2006-2009 Ext JS, LLC
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/**
 * @class Ext.History
 * @extends Ext.util.Observable
 * History management component that allows you to register arbitrary tokens that signify application
 * history state on navigation actions.  You can then handle the history {@link #change} event in order
 * to reset your application UI to the appropriate state when the user navigates forward or backward through
 * the browser history stack.
 * @singleton
 */
Ext.History = (function() {
    //**************************************************************
    // this and it's usage should be the only changes.  It used to just check for IE
    function needIFrameHack() {
        return Ext.isIE && !Ext.isIE8;
    }
    
    var iframe, hiddenField;
    var ready = false;
    var currentToken;
    
    function getHash() {
        var href = window.location.href, i = href.indexOf("#");
        return i >= 0 ? href.substr(i + 1) : null;
    }
    
    function cleanHashMark(token) {
        var i = token.indexOf("#");
        return i >= 0 ? token.substr(i + 1) : token;
    }
    
    function doSave() {
        hiddenField.value = currentToken;
    }
    
    function handleStateChange(token) {
        currentToken = token;
        Ext.History.fireEvent('change', token);
    }
    
    function updateIFrame(token) {
        var html = ['<html><body><div id="state">', cleanHashMark(token), '</div></body></html>'].join('');
        try {
            var doc = iframe.contentWindow.document;
            doc.open();
            doc.write(html);
            doc.close();
            return true;
        } 
        catch (e) {
            logger.logError(new RP.Exception("[History] Write iframe failed", e));
            return false;
        }
    }
    
    function checkIFrame() {
        if (!iframe.contentWindow || !iframe.contentWindow.document) {
            setTimeout(checkIFrame, 10);
            return;
        }
        
        var doc = iframe.contentWindow.document;
        var elem = doc.getElementById("state");
        var token = elem ? elem.innerText : null;
        
        var hash = getHash();
        
        setInterval(function() {
            doc = iframe.contentWindow.document;
            elem = doc.getElementById("state");
            
            var newtoken = elem ? elem.innerText : null;
            
            var newHash = getHash();
            
            if (newtoken !== token) {
                token = newtoken;
                handleStateChange(token);
                window.location.hash = token;
                hash = token;
                doSave();
            }
            else if (newHash !== hash) {
                hash = newHash;
                updateIFrame(newHash);
            }
        }, 50);
        
        ready = true;
        
        Ext.History.fireEvent('ready', Ext.History);
    }
    
    function startUp() {
        currentToken = hiddenField.value ? hiddenField.value : getHash();
        
        if (needIFrameHack()) {
            checkIFrame();
        }
        else {
            var hash = getHash();
            setInterval(function() {
                var newHash = getHash();
                if (newHash !== hash) {
                    hash = newHash;
                    handleStateChange(hash);
                    doSave();
                }
            }, 50);
            ready = true;
            Ext.History.fireEvent('ready', Ext.History);
        }
    }
    
    return {
        /**
         * The id of the hidden field required for storing the current history token.
         * @type String
         * @property
         */
        fieldId: 'x-history-field',
        /**
         * The id of the iframe required by IE to manage the history stack.
         * @type String
         * @property
         */
        iframeId: 'x-history-frame',
        
        events: {},
        
        /**
         * Initialize the global History instance.
         * @param {Boolean} onReady (optional) A callback function that will be called once the history
         * component is fully initialized.
         * @param {Object} scope (optional) The callback scope
         */
        init: function(onReady, scope) {
            if (ready) {
                Ext.callback(onReady, scope, [this]);
                return;
            }
            if (!Ext.isReady) {
                Ext.onReady(function() {
                    Ext.History.init(onReady, scope);
                });
                return;
            }
            hiddenField = Ext.getDom(Ext.History.fieldId);
            if (needIFrameHack()) {
                iframe = Ext.getDom(Ext.History.iframeId);
            }
            this.addEvents('ready', 'change');
            if (onReady) {
                this.on('ready', onReady, scope, {
                    single: true
                });
            }
            startUp();
        },
        
        /**
         * Add a new token to the history stack. This can be any arbitrary value, although it would
         * commonly be the concatenation of a component id and another id marking the specifc history
         * state of that component.  Example usage:
         * <pre><code>
         // Handle tab changes on a TabPanel
         tabPanel.on('tabchange', function(tabPanel, tab){
         Ext.History.add(tabPanel.id + ':' + tab.id);
         });
         </code></pre>
         * @param {String} token The value that defines a particular application-specific history state
         * @param {Boolean} preventDuplicates When true, if the passed token matches the current token
         * it will not save a new history step. Set to false if the same state can be saved more than once
         * at the same history stack location (defaults to true).
         */
        add: function(token, preventDup) {
            if (preventDup !== false) {
                if (this.getToken() == token) {
                    return true;
                }
            }
            if (needIFrameHack()) {
                window.location.hash = token;
                return updateIFrame(token);
            }
            else {
                window.location.hash = token;
                return true;
            }
        },
        
        /**
         * Programmatically steps back one step in browser history (equivalent to the user pressing the Back button).
         */
        back: function() {
            history.go(-1);
        },
        
        /**
         * Programmatically steps forward one step in browser history (equivalent to the user pressing the Forward button).
         */
        forward: function() {
            history.go(1);
        },
        
        /**
         * Retrieves the currently-active history token.
         * @return {String} The token
         */
        getToken: function() {
            return ready ? currentToken : getHash();
        }
    };
})();
Ext.apply(Ext.History, new Ext.util.Observable());
//////////////////////
// ..\js\data\JSONProxy.js
//////////////////////
Ext.ns("RP.data");

/**
 * @class RP.data.JSONProxy
 * @extends Ext.data.HttpProxy
 * 
 * An HttpProxy extension that JSON encodes the parameters
 * and sets up the appropriate content type headers.
 */
RP.data.JSONProxy = Ext.extend(Ext.data.HttpProxy, {
  request: function(action, rs, params, reader, callback, scope, arg) {
    params = Ext.util.JSON.encode(params);
    callback = callback || this.loadResponse;
    
    this.conn.headers = RP.util.Object.mergeProperties(this.conn.headers || {}, {
      "Content-Type": "application/json; charset=UTF-8"
    });
    
    RP.data.JSONProxy.superclass.request.apply(this, arguments);
  },
  loadResponse: function(o, success, response) {
    if (response.status === 401) {
      window.location.reload();
    }
    RP.data.JSONProxy.superclass.loadResponse.apply(this, arguments);
  }
});

// backwards compatibility
Ext.data.AspNetAjaxHttpProxy = RP.data.JSONProxy;
//////////////////////
// ..\js\form\DateField.js
//////////////////////
Ext.ns("RP.form");

/**
 * @class RP.form.DateField 
 * @extends Ext.form.DateField
 * Defaults the invalidText configuration if it is not specified.  This is done to ensure
 * that the invalidText display uses a Locale Pack.
 * 
 * @cfg {Boolean} showFiscal Specify true to render the fiscal version of {@link RP.menu.DateMenu}.
 * Defaults to false.
 */
RP.form.DateField = Ext.extend(Ext.form.DateField, {
    initComponent: function() {
        // Set default invalidText if not part of initialConfig
        if (!this.initialConfig.invalidText) {
            Ext.apply(this, {
                // invalidText will be used in a String.format with {0} = value and {1} = format
                invalidText: RP.getMessage("rp.common.misc.InvalidDate")
            });
        }
        
        RP.form.DateField.superclass.initComponent.call(this);
    },
    
    /**
     * Hook into the trigger click lazy instantiation mechanism to inject our own
     * date menu, cascading the showFiscal configuration.
     * @private
     */
    onTriggerClick: function() {
        if (Ext.isEmpty(this.menu) && this.showFiscal === true) {
            this.menu = new RP.menu.DateMenu({
                showFiscal: this.showFiscal
            });
            
            // Preserve the start day from the DateMenu.
            // The DateField.superclass overwrites the menu.picker.startDay in the 
            // onTriggerClick funciton and sets it to Ext.form.DateField.startDay, which is 0. 
            // Here the menu.picker.startDay will be set correctly based on fiscal data, 
            // so by setting this.startDay to the picker's startDay it will use that 
            // value instead of 0.
            this.startDay = this.menu.picker.startDay;
        }
        
        RP.form.DateField.superclass.onTriggerClick.apply(this, arguments);
    }
});

// Remaining for backwards compatibility.
Ext.ns("RP.ui");
RP.ui.DateField = RP.form.DateField;
Ext.reg("rpdatefield", RP.form.DateField);
//////////////////////
// ..\js\form\DirectionalDateField.js
//////////////////////
Ext.ns("RP.form");

/**
 * @class RP.form.DirectionalDateField
 * @extends Ext.Container
 * <p>RP.form.DateField wrapper to implement a date picker.  We add the left and right
 * buttons to allow navigate the calendar forwards and backwards a customized # of days.</p>
 * The config object passed to this control will also be passed to the DateField control.
 *
 * @cfg {Number} dayInterval The number of days to skip via the left/right buttons.  Defaults to 1.
 * @cfg {Boolean} showFiscal Specify true to render the fiscal version of {@link RP.menu.DateMenu}.
 * @cfg {String} extraCls (optional) An extra CSS class that will be added to the container.
 * @cfg {function} forward (optional) For non-default functionality on a forward click
 * @cfg {function} backward (optional) For non-default functionality on a backward click
 * Defaults to false.
 */
RP.form.DirectionalDateField = Ext.extend(Ext.Container, {
    initComponent: function() {
        this._state = {};
        this._state.dayInterval = this.initialConfig.dayInterval || 1;
        
        this.addEvents(
            /**
             * @event select
             * Fires when a date is selected, the ENTER key is pressed, or the
             * left/right buttons are clicked.
             * @param {RP.form.DirectionalDateField} component The reference to this component.
             * @param {Date} date The selected date.
             */
            "select"
        );
        
        this._lastSelectedValue = this.initialConfig.value;
        this._state.dateField = this._createDateField();
        
        Ext.apply(this, {
            autoEl: {},
            cls: this.initialConfig.cls || 'rp-datepicker',
            disabledClass: "",
            width: this.initialConfig.width || 145,
            layout: "column",
            items: this._getDatePickerItems()
        });
        
        if (Ext.isDefined(this.extraCls)) {
            this.addClass(this.extraCls);
        }
        
        RP.form.DirectionalDateField.superclass.initComponent.call(this);
    },
    
    _createDateField: function() {
        var staticConfig = {
          xtype: this.initialConfig.dateFieldXtype || "rpdatefield",
            allowBlank: false,
            enableKeyEvents: true,
            value: this.initialConfig.value,
            disabled: this.initialConfig.disabled,
            maxValue: this.initialConfig.maxValue,
            emptyText: this.initialConfig.emptyText || RP.getMessage("rp.common.misc.EmptyDateText"),
          listeners: this._getDateFieldListeners()
      };
        
      // merge up the initial configuration with our static configuration
      // this allows us to configure the date picker per application bases
      Ext.applyIf(staticConfig, this.initialConfig);
      
      var datefield = Ext.ComponentMgr.create(staticConfig);
      datefield.blankText = RP.getMessage("rp.common.misc.MissingDate");
      
      datefield.on("afterrender", this.onAfterRender, this);
      datefield.originalOnTriggerClick = datefield.onTriggerClick;
      datefield.onTriggerClick = this.onNewTriggerClick.createDelegate(this);
      
      datefield.on("select", function(df, date) {
          var parsedDate = RP.Formatting.Dates.parse(this._state.dateField.getRawValue(), RP.core.Formats.Date.Medium);
          if ((typeof(date) === "object") || !isNaN(parsedDate)) {
              var newDate = this._state.dateField.getValue();
              
              if ((!Ext.isDate(this._lastSelectedValue) && Ext.isDate(newDate)) || (Ext.isDate(newDate) && newDate.getTime() !== this._lastSelectedValue.getTime())) {
                  this.fireEvent("select", this, newDate);
                  this._lastSelectedValue = newDate.clone();
              }
          }
      }, this);
      
      // RPWEB-3584
      // safeParse added because RP's Date.parseDate breaks when adding initTime to it. Using Date.extParseDate to
      // avoid the bug involving places that have DST at midnight.
      datefield.safeParse = function(value, format) {
          if (/[gGhH]/.test(format.replace(/(\\.)/g, ''))) {
              // if parse format contains hour information, no DST adjustment is necessary
              return Date.parseDate(value, format);
          }
          var parsedDate = Date.extParseDate(value + ' ' + this.initTime, format + ' ' + this.initTimeFormat);
          if (parsedDate) {
              return parsedDate.clearTime();
          }
      };
      
      return datefield;
    },
    
    _getDateFieldListeners: function() {
      return {
                "afterrender": function(comp) {
                    // RPWEB-2869 prevent the user from accidentally dragging the image into the text box
                    comp.el.next("img").on("mousedown", function(e) {
                        e.preventDefault();
                    });
                },
                "keydown": {
                    scope: this,
                    fn: function(datepicker, e) {
                        var key = e.getKey();
                        if (key === e.ENTER || key === e.TAB) {
                            //Added DateField.beforeBlur date preparing, because keydown didn't have similar 
                            // functionality and the day was not being parsed before being checked.
                            var enteredDate = this._state.dateField.parseDate(this._state.dateField.getRawValue());
                            if (enteredDate) {
                                this._state.dateField.setValue(enteredDate);
                            }
                            
                            if (this._checkDateValueChanged(this._lastSelectedValue, datepicker.getValue())) {
                                datepicker.fireEvent("select");
                                document.body.focus();
                            }
                        }
                    }
                },
                "blur": {
                    scope: this,
                    fn: function(datepicker, e) {
                        if (this._checkDateValueChanged(this._lastSelectedValue, datepicker.getValue())) {
                            datepicker.fireEvent("select");
                            document.body.focus();
                        }
                    }
                },
                "change": {
                    scope: this,
                    fn: function(datepicker, e) {
                        if (this._checkDateValueChanged(this._lastSelectedValue, datepicker.getValue())) {
                            datepicker.fireEvent("select");
                            document.body.focus();
                        }
                    }
                }
        };
    },
    
    disable: function() {
        RP.form.DirectionalDateField.superclass.disable.call(this);
        
        Ext.iterate(this.items.keys, function(key) {
            Ext.getCmp(key).disable();
        });
        this._state.dateField.disable();
    },
    
    enable: function() {
        RP.form.DirectionalDateField.superclass.enable.call(this);
        
        Ext.iterate(this.items.keys, function(key) {
            Ext.getCmp(key).enable();
        });
        this._state.dateField.enable();
    },
    
    /**
     * Programmatically moves the date picker forward in time.  The movement forward
     * is based on the configuration of dayInterval.
     */
    forward: function() {
        var date = this.getValue();
        if (typeof(date) === "object") {
            date.addDays(this._state.dayInterval);
            this.setValue(date);
            this.fireEvent("select", this, date);
        }
    },
    /**
     * Programmatically moves the date picker backward in time.  The movement backward
     * is based on the configuration of dayInterval.
     */
    backward: function() {
        var date = this.getValue();
        if (typeof(date) === "object") {
            date.addDays(-this._state.dayInterval);
            this.setValue(date);
            this.fireEvent("select", this, date);
        }
    },
    
    /**
     */
    getDateFieldComponent: function() {
        return this._state.dateField;
    },
    
    /**
     * @param {Date} val
     */
    setValue: function(val) {
        this._state.dateField.setValue(val);
        this._lastSelectedValue = val;
    },
    
    /**
     */
    getValue: function() {
        return this._state.dateField.getValue();
    },
    
    /**
     * @param {Object} originalDate
     * @param {Object} newDate
     * @private
     */
    _checkDateValueChanged: function(originalDate, newDate) {
        if (!Ext.isDate(originalDate)) {
            return true;
        }
        
        var originalMS = originalDate.getTime();
        var newDateMS = Ext.isDate(newDate) ? newDate.getTime() : originalDate.getTime();
        return originalMS - newDateMS !== 0;
    },
    
    /**
     * @private
     */
    _getDateField: function() {
        var field = {
            bodyBorder: false,
            width: this.initialConfig.width - 45 || 100,
            height: 22,
            layout: "fit",
            maskDisabled: false,
            items: this._state.dateField
        };
        return field;
    },
    
    /**
     * @private
     */
    _getDatePickerItems: function() {
        var items = [];
        
        if (this._useNextPreviousButtons()) {
            items.push(this._getNextPreviousButton('previous'));
        }
        
        items.push(this._getDateField());
        
        if (this._useNextPreviousButtons()) {
            items.push(this._getNextPreviousButton('next'));
        }
        
        return items;
    },
    
    /**
     * @private
     * @param {Object} direction
     */
    _getNextPreviousButton: function(direction) {
        var iconCls = direction === 'next' ? 'rp-next-btn' : 'rp-prev-btn';
        var handler = direction === 'next' ? this.forward : this.backward;
        
        var btn = {
            xtype: "button",
            iconCls: iconCls,
            cls: "rp-raised-btn",
            scope: this,
            handler: handler
        };
        
        return btn;
    },
    
    /**
     * @private
     */
    _useNextPreviousButtons: function() {
        if (typeof this.initialConfig.useNextPreviousButtons !== 'undefined') {
            if (!this.initialConfig.useNextPreviousButtons) {
                return false;
            }
        }
        return true;
    },
    
    /**
     * Handles the afterrender event so that the trigger of the datefield is accessible.
     * @private
     */
    onAfterRender: function() {
        this._state.dateField.trigger.on("mousedown", this.onTriggerMouseDown, this);
        
        // hack to fix weird display issue in IE7 (RPWEB-4288), we may be able to remove this in 4.0
        this.el.repaint();
    },
    
    
    /**
     * Handles the mousedown event on the trigger to determine if the date menu should appear.
     * @private
     */
    onTriggerMouseDown: function() {
        if (this._state.dateField.menu && this._state.dateField.menu.isVisible()) {
            this.hideDateMenu = true;
        }
    },
    
    /**
     * Determines if the date menu should appear or if the original onTriggerClick function executes.
     * @private
     */
    onNewTriggerClick: function() {
        if (!this.hideDateMenu) {
            this._state.dateField.originalOnTriggerClick();
        }
        else {
            this.hideDateMenu = false;
        }
    }
});

// Remaining for backwards compatibility.
Ext.ns("RP.ui");
RP.ui.DatePicker = RP.form.DirectionalDateField;
Ext.reg("rpdatepicker", RP.form.DirectionalDateField);
//////////////////////
// ..\js\menu\DateMenu.js
//////////////////////
Ext.ns("RP.menu");

/**
 * @class RP.menu.DateMenu
 * @extends Ext.menu.DateMenu
 * @cfg {Boolean} showFiscal Specify true to render an {@link RP.FiscalDatePicker}.
 * Defaults to false.
 */
RP.menu.DateMenu = Ext.extend(Ext.menu.DateMenu, {
    /**
     * Override the original DateMenu's initComponent to allow for the injection
     * of the {@link RP.FiscalDatePicker} if showFiscal is set to true.
     * @private
     */
    initComponent : function(){
        if (this.showFiscal === true) {
            // the code below is a direct copy/paste of the standard DateMenu's initComponent,
            // except for using the FiscalDatePicker and fixing a few JSLint warnings
            this.on('beforeshow', this.onBeforeShow, this);
            this.strict = (Ext.isIE7 && Ext.isStrict);
            if(this.strict) {
                this.on('show', this.onShow, this, {single: true, delay: 20});
            }
            Ext.apply(this, {
                plain: true,
                showSeparator: false,
                items: this.picker = new RP.FiscalDatePicker(Ext.applyIf({
                    internalRender: this.strict || !Ext.isIE,
                    ctCls: 'x-menu-date-item',
                    id: this.pickerId
                }, this.initialConfig))
            });
            this.picker.purgeListeners();
            Ext.menu.DateMenu.superclass.initComponent.call(this);
            /**
             * @event select
             * Fires when a date is selected from the {@link #picker Ext.DatePicker}
             * @param {DatePicker} picker The {@link #picker Ext.DatePicker}
             * @param {Date} date The selected date
             */
            this.relayEvents(this.picker, ['select']);
            this.on('show', this.picker.focus, this.picker);
            this.on('select', this.menuHide, this);
            if(this.handler){
                this.on('select', this.handler, this.scope || this);
            }
        }
        else {
            RP.menu.DateMenu.superclass.initComponent.call(this);
        }    
    }
});
//////////////////////
// ..\js\interfaces\ITaskContext.js
//////////////////////
Ext.ns("RP.interfaces");

/**
 * @namespace RP.interfaces
 * @class ITask
 */
RP.interfaces.ITaskContext =
{
  /**@private*/
  _name: "ITaskContext",    // required member to indicate interface name
  
  /**
   * Get the task configuration
   * @method
   * @return {Object} The task configuration object
   */
  getConfig: Ext.emptyFn,
  
  /**
   * Subscribe an event handler to the task's status change event.
   * @method
   * @param {Function} handler
   */
  subscribeStatusChange: Ext.emptyFn,
  
  /**
   * Unsubscribe an event handler from the task's status change event.
   * @method
   * @param {Function} handler
   */
  unsubscribeStatusChange: Ext.emptyFn,
  
  /**
   * Fires the task's status change event.
   * @method
   */
  fireStatusChange: Ext.emptyFn,
  
  /**
   * Gets the task's runtime "local context".  The "local context" is a glob
   * of data generated at runtime pertaining to this task
   * @method
   * @return {Object} local context object
   */
  getLocalContext: Ext.emptyFn
};
//////////////////////
// ..\js\interfaces\ITaskflow.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.interfaces");

/**
 * @namespace RP.interfaces
 * @class ITaskflow
 */
RP.interfaces.ITaskflow2 =
{
  /**@private*/
  _name: "ITaskflow2",    // required member to indicate interface name
  
  /**
   * Get the config object for the taskflow user interface.
   * @method
   */
  getTaskflowUIConfig: Ext.emptyFn,
  
  /**
   * addInitListener(cb)
   * Add a delegate to call when this taskflow has finished its base initialization. This is
   * basically your hook to do custom initialization.  'cb' has the following signature: 
   *    void delegate(Object this, Function continueFn)
   * Your handler can do stuff asynchronously and when done, call the continueFn delegate
   * to continue the taskflow.  continueFn takes no parameters.
   * @method
   */
  addInitListener: Ext.emptyFn,
  
  /**
   * addPostInitListener(cb)
   * Add a delegate to call when this taskflow is initialized.  'cb' has
   * the following signature: void delegate(Object this)
   * @method
   */
  addPostInitListener: Ext.emptyFn,
  
  /**
   * addTaskflowStartListener(cb)
   * Add a delegate to call when this taskflow after it's started to run.
   * 'cb' has the following signature: void delegate(Object this)
   * @method
   */
  addTaskflowStartListener: Ext.emptyFn,
  
  /**
   * addTaskCompletedListener(cb)
   * Add a delegate to call when any task in this taskflow is completed.
   * 'cb' has the following signature: void delegate(Object this, Object taskConfig)
   * @method
   */
  addTaskCompletedListener: Ext.emptyFn,
  
  /**
   * addTaskflowCompletedListener(cb)
   * Add a delegate to call when this taskflow has completed.  'cb' has 
   * the following signature: void delegate(Object this)
   * @method
   */
  addTaskflowCompletedListener: Ext.emptyFn,
  
  /**
   * setRebuildHandler(cb)
   * Sets the delegate to invoke to rebuild this taskflow
   */
  setRebuildHandler: Ext.emptyFn,
  
  /**
   * rebuild(config)
   * Rebuild this taskflow with a new configuration
   */
  rebuild: Ext.emptyFn,
  
  /**
   * Query if this taskflow has been initialized.  "Initialized" means tasks
   * have been loaded and getTaskByID() should work as expected.
   * @method
   */
  isInitialized: Ext.emptyFn,
  
  /**
   * Query if this taskflow has been completed.  "Completed" means all of the tasks in
   * this taskflow has signalled they have completed.
   * @method
   */
  isCompleted: Ext.emptyFn,
  
  /**
   * initTaskflow(initCompletedFn)
   * Called to initialize the taskflow
   * @method
   * @param {function} initCompletedFn The delegate you should call once you complete initialization
   */
  initTaskflow: Ext.emptyFn,
  
  /**
   * getTaskflowContext()
   * Called to get the taskflow context.  This context appears in the URL.
   * @method
   */
  getTaskflowContext: Ext.emptyFn,
  
  /**
   * mergeTaskflowContext(context)
   * Called to merge new values into the taskflow context
   * @method
   * @param {Object} context The taskflow context object to set to
   */
  mergeTaskflowContext: Ext.emptyFn,
  
  /**
   * Called to get this taskflow's description
   * @method
   * @return {String} Description text
   */
  getTaskflowDescription: Ext.emptyFn,
  
  /**
   * Sets this taskflow's description.  NOTE: for now, if the description changes after
   * the taskflow container is rendered, this will NOT re-render the description panel.
   * @method
   * @param {String} desc The description text/HTML
   */
  setTaskflowDescription: Ext.emptyFn,
  
  /**
   * Called to get the default taskId for a taskflow when there is no URL hash
   */
  getDefaultTaskId: Ext.emptyFn,
  
  /**
   * getTaskflowRoster(tfConfig)
   * Called to create this taskflow's list of widgets, where each widget corresponds to a task
   * @method
   * @param tfConfig  {Object} The taskflow config object
   * @return {array} Array of taskflow widgets.
   */
  getTaskflowRoster: Ext.emptyFn,
  
  /**
   * getTaskMetadata()
   * Returns the specified task's metadata, including the task config object and its local context
   * @param {String} taskID
   * @method
   */
  getTaskMetadata: Ext.emptyFn,
  
  /**
   * createTaskWidgetConfig(taskConfig)
   * Called to create a configuration object for a task widget.  You can do additional processing here, 
   * such as authorizing the user for this task, etc.  Return null to skip creating this task
   * @method
   * @param taskConfig  {Object} The task config object
   * @return {Ext.Component} Task widget.
   */
  createTaskWidgetConfig: Ext.emptyFn,
  
  /**
   * startTaskflow(tfConfig)
   * Called to start a taskflow after it has successfully initialized
   * @method
   * @param tfConfig  {Object} The taskflow config object
   */
  startTaskflow: Ext.emptyFn,
  
  /**
   * Called to update taskflow status 
   * @method
   */
  updateStatus: Ext.emptyFn,
  
  /**
   * isTaskCompleted(taskID)
   * Called to get a task's completion status. 
   * @method
   */
  isTaskCompleted: Ext.emptyFn,
  
  /**
   * setTaskCompleted(taskID, completedStatus)
   * Called to set a task's completion status.
   * @method
   */
  setTaskCompleted: Ext.emptyFn,
  
  /**
   * getTaskConfig(taskID)
   * Called to get a task config object, i.e., the roster item definition. 
   * @method
   */
  getTaskConfig: Ext.emptyFn
};
//////////////////////
// ..\js\interfaces\ITaskflowContainer2.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.interfaces");

/**
 * @namespace RP.interfaces
 * @class ITaskflowContainer
 */
RP.interfaces.ITaskflowContainer2 =
{
  /**@private*/
  _name: "ITaskflowContainer",    // required member to indicate interface name
  
  /**
   * Return the name of the taskflow contained within.  Typically this will be
   * name registered in the TaskflowRegistry.  This is used to uniquely identify
   * a taskflow for bookmarking, etc. 
   * @method
   * @return {Object} An object that implements RP.taskflow.ITaskflow
   */
  getTaskflowName: Ext.emptyFn,
  
  /**
   * Called by taskflow frame to ensure the underlying taskflow is initialized. 
   * @method
   */
  ensureTaskflowInitialized: Ext.emptyFn,
  
  /**
   * Called to get an instance of an object representing a taskflow business object.
   * Object must implement RP.taskflow.ITaskflow.
   * @method
   * @return {RP.taskflow.ITaskflow2} An object that implements RP.taskflow.ITaskflow2
   */
  getTaskflow: Ext.emptyFn,
  
  /**
   * Handler called when your container has been deactivated (collapsed) because
   * another task in another taskflow container is activated.  Example: TFC1 has
   * Task1 and TFC2 has Task2; Task1 is currently active; User clicks the Task2
   * widget => result is that TFC1.onDeactivated is invoked.  If user then switches
   * back to Task1, then TFC2.onDeactivated is invoked.
   * @method
   */
  onDeactivated: Ext.emptyFn,
  
  /**
   * Activate the default widget (task) in your taskflow container.  The taskflow should
   * be initialized if it hasn't yet.
   * @method
   */
  activateDefaultWidget: Ext.emptyFn,  
  
  /**
   * onWidgetClick(widget)
   * Handler for a widget (task) click.  'widget' is the widget being clicked.
   * @method
   */
  onWidgetClick: Ext.emptyFn,
  
  /**
   * Return a task matching the specified ID.  If taskflow hasn't been initialized,
   * this should return null.
   * @method
   * @param {string} ID The task ID
   */
  getWidgetByID: Ext.emptyFn,
  
  /**
   * The callback passed to this method must be called prior to activating a
   * task form.  This is to allow the TaskflowFrame to cancel activation.  If
   * the callback returns false, do not activate the task form.  Signature of
   * cb: boolean function(void)
   * setBeforeAppActivateHandler(cb)
   * @method
   */
  setBeforeAppActivateHandler: Ext.emptyFn,
  
  /**
   * addApplicationActivatedHandler(cb)
   * Add a delegate to call when an app in this taskflow is activated.  'cb' has
   * the following signature: void delegate(Ext.Component this, Ext.Component application)
   * @method
   */
  addApplicationActivatedHandler: Ext.emptyFn,
  
  /**
   * Add a listener to invoke when the actual taskflow object has been created.
   * @method
   * @param {Function} cb The callback function.  'cb' has the following signature:
   *     void delegate(Object taskflowContainer)
   */
  addTaskflowCreationListener: Ext.emptyFn,
  
  /**
   * Remove a listener added via addTaskflowCreationListener
   * @method
   * @param {Function} cb The callback function
   */
  removeTaskflowCreationListener: Ext.emptyFn,
  
  /**
   * Add a listener to invoke when this taskflow is aborted via a schedule check
   * @method
   * @param {Function} cb The callback function
   */
  addTaskflowAbortHandler: Ext.emptyFn,
  
  /**
   * Sets the delegate to invoke to rebuild this taskflow
   * @method
   * @param {Function} delegate The delegate to invoke in order to rebuild this
   * taskflow.  It takes a lone argument, which is either a config object
   * to create an ITaskflowContainer2 instance with, or an actual
   * ITaskflowContainer2 instance.
   */
  setRebuildTaskflowHandler: Ext.emptyFn,
  
  /**
   * Gets the active widget
   * @method
   */
  getActiveWidget: Ext.emptyFn
};
//////////////////////
// ..\js\interfaces\IPrintSource.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.interfaces");

/**
 * @class IPrintSource
 * @namespace RP.interfaces
 */
RP.interfaces.IPrintSource =
{
  /**@private*/
  _name: "IPrintSource",    // required member to indicate interface name
  
  /**
   * Return the HTML for your component for printing.
   * @method
   */
  getMarkupForPrinting: Ext.emptyFn
};
//////////////////////
// ..\js\interfaces\ITaskForm.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.interfaces");

/**
 * @namespace RP.interfaces
 * @class ITaskForm
 */
RP.interfaces.ITaskForm =
{
  /**@private*/
  _name: "ITaskForm",    // required member to indicate interface name
  
  /**
   * Called by TaskflowFrame to indicate this task form is about to be shown
   * @method
   */
  onActivate: Ext.emptyFn,
  
  /**
   * onBeforeDeactivate(cb)
   * Called by TaskflowFrame before this task form is about to be deactivated.
   * This task form can cancel deactivation by invoking the passed in callback
   * (cb) with a single argument with the value false.
   * @method
   */
  onBeforeDeactivate: Ext.emptyFn,
  
  /**
   * Called by TaskflowFrame to indicate this task form is about to be hidden
   * @method
   */
  onDeactivate: Ext.emptyFn,
  
  /**
   * setUrlHash(
   * Called by Taskflow Container to set the URL hash value used for history and bookmarking
   * @method
   */
  setUrlHash: Ext.emptyFn,
  
  /**
   * Called TaskflowFrame to get the URL hash used for history and bookmarking
   * @method
   * @return {string} The URL hash value
   */
  getUrlHash: Ext.emptyFn
};
//////////////////////
// ..\js\interfaces\ITaskWidget.js
//////////////////////
Ext.ns("RP.interfaces");

/**
 * @namespace RP.interfaces
 * @class ITaskWidget
 */
RP.interfaces.ITaskWidget = {
  _name: "ITaskWidget",
  
  /**
   * Sets the task ID to associate with this widget
   * @param {String} taskId The task ID 
   * @method
   */
  setTaskId: Ext.emptyFn,
  
  /**
   * Gets the task ID associated with this widget
   * @return {String} taskId The task ID 
   * @method
   */
  getTaskId: Ext.emptyFn,
  
  /**
   * Called by the taskflow engine to set the taskflow
   * @param {RP.interfaces.ITaskflow2} taskId The task ID 
   * @method
   */
  setTaskflow: Ext.emptyFn,
  
  /**
   * Return the taskflow reference set by setTaskflow()
   * @return {RP.interfaces.ITaskflow2} The taskflow 
   * @method
   */
  getTaskflow: Ext.emptyFn,
  
  /**
   * Initialize task local context.  This is called by the taskflow engine
   * to allow you to initialize the values of your task's local context at
   * runtime
   */
  initializeLocalContext: Ext.emptyFn,
  
  /**
   * Gets the enabled status of the widget.
   * @method
   * @return {Boolean} disabled
   */
  getEnabled: Ext.emptyFn,
  
  /**
   * Sets the enabled status of the widget.
   * @method
   */
  setEnabled: Ext.emptyFn,
  
  /**
   * Creates the widget's task form.
   * @method
   */
  createTaskForm: Ext.emptyFn,
  
  /**
   * Gets a reference to the widget's task form.
   * @return {Object}
   */
  getTaskForm: Ext.emptyFn,
  
  /**
   * Simulate a click on the widget, i.e., fire the widget click event.
   * @method
   */
  raiseWidgetClick: Ext.emptyFn,
  
  /**
   * Subscribe an event handler to the widget's click event.
   * @method
   * @param {Function} handler
   */
  subscribeWidgetClick: Ext.emptyFn,
  
  /**
   * Unsubscribe an event handler from the widget's click event.
   * @method
   * @param {Function} handler
   */
  unsubscribeWidgetClick: Ext.emptyFn,
  
  /**
   * Simulate a change on the widget, i.e., fire the widget change event.
   * @method
   */
  raiseWidgetChange: Ext.emptyFn,
  
  /**
   * Subscribe an event handler to the widget's change event.
   * @method
   * @param {Function} handler
   */
  subscribeWidgetChange: Ext.emptyFn,
  
  /**
   * Unsubscribe an event handler from the widget's change event.
   * @method
   * @param {Function} handler
   */
  unsubscribeWidgetChange: Ext.emptyFn,
  
  /**
   * Gets the task's context for the browser URL.
   * @method
   * @return {Object} The task's current context
   */
  getTaskContext: Ext.emptyFn,
  
  /**
   * Set the task context for the browser URL.
   * @param {Mixed} taskContext
   */
  setTaskContext: Ext.emptyFn,
  
  /**
   * Sets the active state of the widget.
   * @method
   * @param {Boolean}
   */
  setActive: Ext.emptyFn
};
//////////////////////
// ..\js\collections\MixedCollection.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.collections");

/**
 * @namespace RP.collections
 * @class MixedCollection
 * @extends Ext.util.MixedCollection
 * 
 * Extends Ext.util.MixedCollection for additional functionality, including the
 * ability to automatically trim the collection when it reaches a specified max
 * size (items added first are trimmed, like a queue)
 */
RP.collections.MixedCollection = Ext.extend(Ext.util.MixedCollection,
{
  maxSize: undefined,
  
  /**
  * @param {object} config Config object
  * @cfg {int} maxSize The maximum number of elements this collection holds.  When the size
  *  exceeds this value, the oldest item added is removed from the collection.
  * @constructor
  */
  constructor: function(config)
  {
    RP.collections.MixedCollection.superclass.constructor.apply(this, [config]); 
    
    if (config)
    {
      if (typeof config.maxSize !== "undefined")
      {
        this.maxSize = config.maxSize;
        this.on("add", function(index, o, key)
        {
          if (this.length > this.maxSize)
          {
            this.removeAt(0);
          }
        }, this);
      }
    }
  },
  
  /**
  * Removes all items beginning at the specified index from the combo array.
  * @param {int} index The index to start removing
  * @method trimAt
  */
  trimAt: function(index)
  {
    if (index < this.length && index >= 0)
    {
      var count = this.length - index;
      
      this.items.splice(index, count);
      this.length -= count;
      this.keys.splice(index, count);
    }
  }
});
//////////////////////
// ..\js\collections\Stack.js
//////////////////////
Ext.namespace("RP.collections");


/**
 * @namespace RP.collections
 * @class Stack
 * @extends Object
 * 
 * Common stack (LIFO list)
 */
RP.collections.Stack = function()
{
  this._s = [];
};

RP.collections.Stack.prototype =
{
  /**
   * Pushes item(s) onto the stack
   * @param {object} vargs Items to add to the stack
   * @method push
   * @return void
   */
  push: function(/* items... */)
  {
    // Pushes one or more items onto stack; last one pushed is on top of the stack...
    this._s.push.apply(this._s, arguments);
  },
  
  /**
   * Pop one item off the stack
   * @method pop
   * @return {object} The popped item
   */
  pop: function()
  {
    var v = this.popn(1);
    if (v.length > 0)
    {
      return v[0];
    }
    else
    {
      return null;
    }
  },
  
  /**
   * Pop an arbitrary number of items off the stack
   * @method popn
   * @return {Array} Items popped
   */
  popn: function(n)
  {
    n = (typeof n === "undefined")? 1 : n;    
    n = Math.min(n, this._s.length);
    
    if (n > 0)
    {
      return this._s.splice(this._s.length - n, n).reverse();
    }
    else
    {
      return [];
    }
  },
  
  /**
   * Peek at the item on top of the stack
   * @method peek
   * @return {object} Item at the top of the stack
   */
  peek: function()
  {
    var v = this.peekn(1);
    if (v.length > 0)
    {
      return v[0];
    }
    else
    {
      return null;
    }
  },
  
  /**
   * Peek at an arbitrary number of items on top of the stack
   * @method peekn
   * @return {Array} Items at the top of the stack
   */
  peekn: function(n)
  {
    n = (typeof n === "undefined")? 1 : n;
    n = Math.min(n, this._s.length);
    
    if (n > 0)
    {
      return this._s.slice(this._s.length - n, this._s.length).reverse();
    }
    else
    {
      return [];
    }
  },
  
  /**
   * Remove all items from the stack
   * @method clear
   * @return void
   */
  clear: function()
  {
    this._s = [];
  },
  
  /**
   * Count the number of items in the stack
   * @method count
   * @return {int} The number of items
   */
  count: function()
  {
    return this._s.length;
  },
  
  /**
   * Count the number of items to pop until the specified item is on top of the stack
   * @method countUntil
   * @param {object} item The item to look for
   * @return {int} The number of items or -1 if item not found
   */
  countUntil: function(item)
  {
    for (var i = this._s.length - 1; i >= 0; i--)
    {
      if (this._s[i] === item)
      {
        return(this._s.length - i - 1);
      }
    }
    
    return -1;
  },
  
  /**
   * Check if the specified item exists in the stack
   * @method contains
   * @param {object} item The item to look for
   * @return {bool} Does item exist?
   */
  contains: function(item)
  {
    return(this.countUntil(item) >= 0);
  }
};
//////////////////////
// ..\js\util\BreadCrumbTrail.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.BreadCrumbTrail
 * @extends Ext.util.Observable
 * <p>A utility class for building a breadcrumb trail.  This class is only a
 * collection of breadcrumbs... it is up to the consumer to listen for the
 * appropriate events and then render the breadcrumbs appropriately.</p>
 * <p>Any breadcrumb added to the trail must fire an "activate" event if any
 * action is to be taken as a result of a click/select/etc...  A "text" breadcrumb
 * (the last crumb in the trail) typically would not need to fire this event.</p>
 * @constructor
 * @param {Object} config
 */
RP.util.BreadCrumbTrail = Ext.extend(Ext.util.Observable, {
  constructor: function(config) {
    Ext.apply(this, config);
    
    this.breadCrumbs = new Ext.util.MixedCollection();
    
    this.addEvents(
      /**
       * @event add
       * Fires after a breadcrumb is added to the trail.
       * @param {RP.util.BreadCrumbTrail} this
       * @param {Object} breadCrumb The breadcrumb that was added.
       */
      "add",
      
      /**
       * @event clear
       * Fires after the breadcrumb trail has been cleared.
       * @param {RP.util.BreadCrumbTrail} this
       */
      "clear",
      
      /**
       * @event navigate
       * Fires when an individual breadcrumb is activated.
       * @param {RP.util.BreadCrumbTrail} this
       * @param {Object} breadCrumb The breadcrumb that was activated.
       */
      "navigate"
    );
    
    RP.util.BreadCrumbTrail.superclass.constructor.call(this, config);
  },
  
  /**
   * Adds a breadcrumb to the end of the breadcrumb trail.
   * @param {Object} breadCrumb The breadcrumb to add.
   */
  add: function(breadCrumb) {
    this.breadCrumbs.add(breadCrumb);
    
    breadCrumb.on("activate", function() {
      this.navigate(breadCrumb);
    }, this);
    
    this.fireEvent("add", this, breadCrumb);
  },
  
  /**
   * Handler for a breadcrumb's activate event.  Will remove
   * all other breadcrumbs behind the one that is activated.
   * 
   * @param {Object} breadCrumb
   * @private
   */
  navigate: function(breadCrumb) {
      var index = this.breadCrumbs.indexOf(breadCrumb);
      var last;
      
      // remove all trailing bread crumbs and their event listeners
      while ((last = this.breadCrumbs.last()) != breadCrumb) {
        last.un("activate", this.navigate, this);
        this.breadCrumbs.remove(last);
      }
      
      this.fireEvent("navigate", this, breadCrumb);
  },
  
  /**
   * Gets the number of breadcrumbs in the trail.
   * @return {Number}
   */
  getCount: function() {
    return this.breadCrumbs.getCount();
  },
  
  /**
   * Gets the trail's breadcrumbs in the form of a Ext.util.MixedCollection.
   * 
   * @return {Ext.util.MixedCollection}
   */
  getBreadCrumbs: function() {
    return this.breadCrumbs;
  },
  
  /**
   * Removes all breadcrumbs.
   */
  clear: function() {
    // remove the event listeners
    this.breadCrumbs.each(function(breadCrumb) {
      breadCrumb.un("activate", this.navigate, this);
    });
    
    this.breadCrumbs.clear();
    this.fireEvent("clear");
  }
});
//////////////////////
// ..\js\util\CardBreadCrumbTrail.js
//////////////////////
/**
 * @class RP.ui.CardBreadCrumbTrail
 * @extends RP.ui.BreadCrumbTrail
 * <p>An extension of BreadCrumbTrail that provides built-in functionality to
 * take care of the logic for adding/removing cards and also rendering the
 * breadcrumb trail.</p>
 * @cfg {Object} cardContainer The card container to be used to hold the bread crumbs
 */
RP.util.CardBreadCrumbTrail = Ext.extend(RP.util.BreadCrumbTrail, {
  /**
   * @cfg {Object} spacerCfg A DomHelper object spec that will be used as the
   * spacer between breadcrumbs.  Defaults to a span containing two arrows.
   */
  spacerCfg: {
    xtype: "box",
    autoEl: {
      tag: "span",
      html: "&nbsp;&gt;&gt;&nbsp;"
    }
  },
  
  /**
   * Override to additionally add the breadcrumb's card into the layout and
   * then set it as the active item.
   * @param {Object} breadCrumb
   */
  add: function(breadCrumb) {
    RP.util.CardBreadCrumbTrail.superclass.add.apply(this, arguments);
    
    var card = breadCrumb.getCard();
    var layout = this.cardContainer.getLayout();
    
    this.cardContainer.add(card);
    var index = this.cardContainer.items.indexOf(card);
    
    if (Ext.isString(layout)) {
      this.cardContainer.activeItem = index;
    }
    else {
      layout.setActiveItem(index);
    }
    
    this.renderBreadCrumbs(true);
  },
  
  /**
   * Override to also remove the cards that were associated with the
   * breadcrumbs that were removed.
   * @param {Object} breadCrumb
   * @private
   */
  navigate: function(breadCrumb) {
    RP.util.CardBreadCrumbTrail.superclass.navigate.apply(this, arguments);
    
    var card = breadCrumb.getCard();
    var last;
    
    // remove trailing cards in the layout
    while ((last = this.cardContainer.items.last()) != card) {
      this.cardContainer.remove(last);
    }
    
    var index = this.cardContainer.items.indexOf(card);
    this.cardContainer.getLayout().setActiveItem(index);
    this.renderBreadCrumbs(false);
  },
  
  /**
   * Renders the breadcrumb trail into the container specified
   * by "crumbContainer" config.
   * @private
   */
  renderBreadCrumbs: function(adding) {
    if (!this.crumbContainer.rendered) {
      this.crumbContainer.on("render", this.renderBreadCrumbs.createDelegate(this, [adding]));
      return;
    }
    
    if (adding) {
      if (this.breadCrumbs.getCount() > 1) {
        this.crumbContainer.add(this.spacerCfg);
      }
      
      this.crumbContainer.add(this.breadCrumbs.last());
    }
    else {
      var current;
      while ((current = this.crumbContainer.items.last()) != this.breadCrumbs.last()) {
        this.crumbContainer.remove(current);
      }
    }
    
    this.crumbContainer.doLayout();
    this.deactiveateLastCrumb();
  },
  
  /**
   * Activates all breadcrumbs except for the last one in the trail,
   * which instead gets deactivated.
   */
  deactiveateLastCrumb: function() {
    this.breadCrumbs.each(function(crumb) {
      if (crumb == this.breadCrumbs.last()) {
        crumb.deactivate();
      }
      else {
        crumb.reactivate();
      }
    }, this);
  }
});
//////////////////////
// ..\js\util\Object.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class Object
 * @singleton
 */
RP.util.Object = (function()
{
  return {
    mergeProperties: function(/*arguments*/)
    {
      ///<summary>
      /// Creates a new object with properties combined from all objects in objectsArray.
      /// Properties of objects in the array take precedence over objects that appear
      /// before them, i.e., if two objects have the same property, then the latter object's
      /// property value is used.  NOTE: All properties copied are shallow (i.e., referenced
      /// objects are not cloned).
      ///</summary>
      var obj = {};
      
      for (var i = 0; i < arguments.length; i++)
      {
        var currObj = arguments[i] || {};
        
        for (var property in currObj)
        {
          obj[property] = currObj[property];
        }
      }
      return obj;
    }
  }; // end return
})();

// Shortcut
RP.mergeProperties = RP.util.Object.mergeProperties;
//////////////////////
// ..\js\util\Dom.js
//////////////////////
/*global RP, Ext, document */

Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class DOM
 * @singleton
 */
RP.util.DOM = (function()
{
  return {
    // This is a workaround for getting the .outerHTML property which is in IE but not Firefox
    /**
     * @method
     * @param {Object} obj
     */
    getOuterHTML: function(obj)
    {
      var element;
      if (!obj)
      {
        return null;
      }
      element = document.createElement("div");
      element.appendChild(obj.cloneNode(true));
      return element.innerHTML;
    }
  }; // end return
})();
//////////////////////
// ..\js\util\Format.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.Format
 * @singleton
 */
RP.util.Format = function() {
  return {
    /**
     * Returns a custom renderer used to translate colvals into long descriptions.
     * @param {String} namespace
     * @param {String} colnam
     */
    longDescRenderer: function(namespace, colnam) {
      return function(value) {
        return RP.core.CodeTranslator.getLongDesc(namespace, colnam, value);
      };
    }
  };
}();
//////////////////////
// ..\js\util\Task.js
//////////////////////
Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class Task
 * @singleton
 */
RP.util.Task = (function() {
  var count = 0;
  var PIDs = {};
  
  return {
    // The array of elements to pass in to context.fn(array[i])
    // taskName is used to id the process, only one queue can be
    // running for a taskname.  Old queues are stopped when one
    // with the same taskName comes in.
    // config is batchSize, timeout (ms), successCB, and stoppedCB
    // returns the PID and callback methods get PID as a param
    processQueue: function(array, fn, context, taskName, config) {
      if (array.length < 1) {
        return;
      }
      
      Ext.applyIf(config, {
        batchSize: 1,
        timeout: 10,
        batchCompleteCB: Ext.emptyFn,
        successCB: Ext.emptyFn,
        stoppedCB: Ext.emptyFn
      });
      
      var a = array.concat();
      var idx = 0;
      var pid = (++count);
      
      PIDs[taskName] = pid;
      
      setTimeout(function() {
        // another task of the same task is running, so stop
        if (PIDs[taskName] !== pid) {
          config.stoppedCB.call(context, pid);
          return;
        }
        
        for (var i = 0; i < config.batchSize && a.length > 0; i++) {
          var item = a.shift();
          fn.call(context, item, idx++);
        }
        config.batchCompleteCB.call(context, pid);
        
        if (a.length > 0) {
          setTimeout(arguments.callee, config.timeout);
        }
        else {
          config.successCB.call(context, pid);
        }
      }, config.timeout);
      
      return pid;
    }
  }; // end return
})();
//////////////////////
// ..\js\util\Stopwatch.js
//////////////////////
/*
  Usage:
  // Using RP.util.Stopwatch.getWatch('loadTimer')
  RP.util.stopwatch.getWatch('timer', true, 'optional description'); // timer started
  // do something
  var sw = RP.util.Stopwatch.getWatch('timer'); // just gets the timer since it exists
  var html = sw.report(); // gives report for the 1 timer
  
  var sw = RP.util.Stopwatch.getWatch('stopped timer'); // creates the timer in a stopped state
  sw.start();
  // do something
  sw.stop();
  
  RP.util.Stopwatch.report('someDiv'); // writes a report to that div
  
*/

Ext.ns("RP.util");

RP.util.Stopwatch = (function()
{
  var _watches = [];
  var listeners = [];
  
  function invokeListeners() {
    Ext.each(listeners, function(d) {
      d();
    });
  }

  return {
    getWatch: function(id, startNow, desc)
    {
      var watch = _watches[id];
      if (!watch)
      {
        watch = new RP.util.Stopwatch.StopWatch(id, desc);
      }
      if (startNow)
      {
        watch.start();
      }
      return watch;
    },

    clearAll: function()
    {
      _watches = [];
      invokeListeners();
    },

    report: function(div)
    {
      var reportobj = [];
      for (var i in _watches)
      {
        if (typeof (_watches[i]) === 'function') { continue; }

        var child = _watches[i];
        reportobj.push(child.report(true));
      }
      var realdiv = document.getElementById(div);
      if (realdiv)
      {
        realdiv.innerHTML = "<table class='padded-table' border='1'><thead><tr><th>ID</th><th>Total (s)</th><th>Count</th><th>Description</th></tr></thead><tbody>" +
                            reportobj.join('') + "</tbody></table>";
      }
      else
      {
        return reportobj.join('');
      }
      return '';
    },

    StopWatch: function(id, desc)
    {
      if (_watches[id])
      {
        throw new RP.Exception(id + " already exists");
      }
      //var events = [];  //events can be used to break out individual times, but it's not needed now.
      var count = 0;
      var total = 0;
      desc = desc || 'N/A';

      var TimedEvent = function()
      {
        var start = new Date();

        return {
          stop: function()
          {
            var stop = new Date();
            this.duration = stop.getTime() - start.getTime();
            invokeListeners();
          }
        };
      };

      this.id = id;
      this.start = function()
      {
        if (this.current)
        {
          this.stop();
        }
        this.current = new TimedEvent();
      };
      this.stop = function()
      {
        if (this.current)
        {
          this.current.stop();
          //events.push(this.current);
          count++;
          total += this.current.duration;
          this.current = null;
          invokeListeners();
        }
      };
      this.report = function(forTbl)
      {
        if (forTbl)
        {
          return "<tr><td>" + id + "</td><td>" + total / 1000.0 + "</td><td>" + count + "</td><td>" + desc + "</td></tr>";
        }
        return "<div>StopWatch: <strong>" + id + "</strong> // " +
                    "Total Duration: <strong>" + total / 1000.0 + "</strong> // " +
                    "Count: <strong>" + count + "</strong></div>";
      };

      _watches[id] = this;

      return this;
    },
    
    /**
     * Add a delegate to invoke whenever the the log is updated (added, cleared, etc.)
     * @method
     * @param {Object} handler
     */
    addListener: function(handler) {
      listeners.push(handler);
    },
    
    /**
     * Remove a previously added listener
     * @method
     * @param {Object} handler
     */
    removeListener: function(handler) {
      listeners.remove(handler);
    }
  };
})();


// Shorthand.
// DEPRECATED
var stopwatch = {};
RP.util.DeprecationUtils.renameClass("stopwatch", "RP.util.Stopwatch");
//////////////////////
// ..\js\event\AppEventManager.js
//////////////////////
/*global Ext, RP, logger */

Ext.ns("RP.event");

/**
 * @namespace RP.event
 * @class AppEventManager
 * @singleton
 * @extends none
 * 
 * Manages well-known application-wide events.  Provides a way for UI components and business classes
 * alike to fire and/or listen to events.
 */
RP.event.AppEventManager = (
  function()
  {
    var evtPool = new Ext.util.Observable();
    
    // Need to call addEvents (albeit an empty list) to seed its events collection...
    evtPool.addEvents([]);
    
    Ext.util.Observable.capture(evtPool, function(evtName)
    {
      logger.logTrace("[AppEventManager] observed event fired: " + evtName);
      return true;
    },
    this);
    
    return {
      /**
       * Registers an application event handler.
	     * @method
	     * @param {string} evtName The event name
	     * @param {Function} handler The delegate to invoke when event is fired
	     * @param {Object} scope The object scope for the delegate
	     * @param {Object} options Options for the delegate
	     */
      register: function(evtName, handler, scope, options)
      {
        evtPool.on(evtName, handler, scope, options);
      },
      
      /**
       * Unregisters an application event handler.
	     * @method
	     * @param {string} evtName The event name
	     * @param {Function} handler The delegate to invoke when event is fired
	     */
      unregister: function(evtName, handler)
      {
        evtPool.un(evtName, handler);
      },
      
      /**
       * Fires the named event.  Caller can pass in as many arguments as necessary.
	     * @method
	     * @param {string} evtName The event name
	     * @param {Object} vargs Optional variable number of arguments
	     */
      fire: function(evtName /* , vargs... */)
      {
        // Insert evtName as the 1st argument...
        var args = [];
        
        args[0] = evtName;
        for (var i = 0; i < arguments.length; i++)
        {
          args[i+1] = arguments[i];
        }
        
        evtPool.fireEvent.apply(evtPool, args);
      },
      
      /**
       * Suspends firing of all events.
	     * @method
	     */
      suspendEvents: function()
      {
        logger.logTrace("[AppEventManager] Entering suspended state");
        evtPool.suspendEvents();
      },
      
      /**
       * Resumes ability to fire events.
	     * @method
	     */
      resumeEvents: function()
      {
        logger.logTrace("[AppEventManager] Leaving suspended state");
        evtPool.resumeEvents();
      }
    };
  }
)();
//////////////////////
// ..\js\event\AppEventProxy.js
//////////////////////
/*global Ext, RP, logger */

Ext.ns("RP.event");

/**
 * @namespace RP.event
 * @class AppEventProxy
 * @singleton
 * @extends none
 *
 * Acts as as proxy to AppEventManager for listeners that wish to have
 * a sleeping/awake state.  If the listener is in the awake state, events from
 * AppEventManager pass straight through to the listener.  If the listener is in the
 * sleeping state, events are queued up as they are received (a prior queued event
 * with the same name can be optionally discarded, i.e. replaced with the latest event).
 * When the listener changes from the sleeping state to the awake state, the queued events 
 * are relayed to the listener.
 */
RP.event.AppEventProxy = (
  function()
  {
    var evtListenerDict = {};   // event dictionary; key=event name, value=array of {listener, handler} instances
    var awakeListeners = [];    // array of listeners
    var asleepListeners = [];   // array of {listener, q} instances, where q is an array of {evtname, handler} instances
    
    var createSleepingListener = function(listener)
    {
      return {
        listener: listener,
        q: []
      };
    };
    
    var getOrCreateEventDictEntry = function(evtName) 
    {
      var e = {};
      
      e.eventListeners = evtListenerDict[evtName];
      
      if (!e.eventListeners)
      {
        e.isNew = true;
        e.eventListeners = [];
        evtListenerDict[evtName] = e.eventListeners;
      }
      
      return e;
    };
    
    var findAwakeListener = function(listener, remove)
    {
      for (var i = 0; i < awakeListeners.length; i++)
      {
        if (awakeListeners[i] === listener)
        {
          if (remove)
          {
            awakeListeners.splice(i, 1);
          }
          return listener;
        }
      }
      
      return null;
    };
    
    var findAsleepListener = function(listener, remove)
    {
      for (var i = 0; i < asleepListeners.length; i++)
      {
        if (asleepListeners[i].listener === listener)
        {
          var al = asleepListeners[i];
          
          if (remove)
          {
            asleepListeners.splice(i, 1);
          }
          return al;
        }        
      }
      
      return null;
    };
    
    var queueEvent = function(al, evtName, fn, removeDuplicateEvents)
    {
      var dupidx = -1;    // duplicate event index
      
      if (removeDuplicateEvents)
      {      
        for (var i = 0; i < al.q.length; i++)
        {
          var qevt = al.q[i];
          
          if (qevt.evtname === evtName)
          {
            dupidx = i;
            break;
          }
        }
        
        // Remove prior duplicate, if any, then add this event to the queue.
        if (dupidx >= 0)
        {
          al.q.splice(dupidx, 1);        
        }
      }
        
      al.q.push({evtname: evtName, handler: fn});
    };
    
    var processQueuedEvents = function(al)
    {
      // Loop thru queued events and relay them to the listener.
      Ext.each(al.q, function(qevt)
      {
        qevt.handler();
      },
      this);
    };
    
    var findQueuedEvent = function(al, evtName, remove)
    {
      var e = [];
      
      for (var i = 0; i < al.q.length; i++)
      {
        if (al.q[i].evtname === evtName)
        {
          e.push(al.q[i]);
          if (remove)
          {
            al.q.splice(i, 1);
            i--;    // compensate for outer loop incrementing i...
          }
          // do NOT break here because there may be multiple events with the same name
        }
      }
      return e;
    };
    
    var findEventListener = function(e, listener, remove)
    {
      for (var i = 0; i < e.eventListeners.length; i++)
      {
        if (e.eventListeners[i].listener === listener)
        {
          var el = e.eventListeners[i];
          if (remove)
          {
            e.eventListeners.splice(i, 1);
          }
          return el;
        }
      }
      return null;
    };
    
    // Event handler that interfaces with AppEventManager.  This contains the main
    // proxy functionality.
    var eventHandler = function(evtName /* , vargs... */)
    {
      var e = getOrCreateEventDictEntry(evtName);
      var origArgs = arguments;
            
      Ext.each(e.eventListeners,
        function(l)
        {
          var fn = l.handler.createDelegate(l.handler, origArgs, false);          
          
          if (findAwakeListener(l.listener, false))
          {
            // Relay event...
            fn();
          }
          else
          {
            // Find sleeping listener and queue the event...
            var al = findAsleepListener(l.listener, false);
            
            if (al)
            {
              queueEvent(al, evtName, fn, l.removeDups);
            }
          }
        },
        this);
    };
    
    return {
      /**
       * Registers an application event listener.
	     * @method
	     * @param {Object} listener The listener object
	     * @param {bool} isAwake Flag indicating the current listener state; True if awake, False if sleeping
	     */
      registerListener: function(listener, isAwake)
      {      
        if (findAsleepListener(listener, false) || findAwakeListener(listener, false))
        {
          throw new RP.Exception("Listener already registered.");
        }
        
        if (isAwake)
        {
          awakeListeners.push(listener);        
        }
        else
        {
          asleepListeners.push(createSleepingListener(listener));
        }
      },
      
      /**
       * Unregisters an application event listener.
	     * @method
	     * @param {Object} listener The object listening to the event
	     */
      unregisterListener: function(listener)
      {
        var l = findAsleepListener(listener, true);
        
        if (!l)
        {
          l = findAwakeListener(listener, true);
        }
        
        // Remove the listener from our internal event dictionary.
        Ext.iterate(evtListenerDict, function(evtName, la, o) {
          Ext.each(la, function(l) {
            if (l.listener === listener) {
              la.remove(l);
              return false;   // break the loop
            }
            return true;  // continue the loop
          }, this);
        }, this);        
      },
      
      /**
       * Subscribe to an application event.
	     * @method
	     * @param {string} evtName The event name
	     * @param {Object} listener The listener object
	     * @param {Function} handler The delegate to invoke when event is fired
	     * @param {bool} removeDuplicateEvents Flag indicating whether or not to remove previous
	     * events with the same name for this listener
	     */
      subscribe: function(evtName, listener, handler, removeDuplicateEvents)
      {
        // Keep track of the listener in the appropriate event list...
        var e = getOrCreateEventDictEntry(evtName);
        
        if (! findEventListener(e, listener, false))
        {
          e.eventListeners.push({listener: listener, handler: handler, removeDups: removeDuplicateEvents});
        }
        
        // Register the event for ourselves.
        if (e.isNew)
        {
          RP.event.AppEventManager.register(evtName, eventHandler, this);
        }
      },
      
      /**
       * Unsubscribe from an application event.
	     * @method
	     * @param {string} evtName The event name
	     * @param {Object} listener The listener object
	     */
      unsubscribe: function(evtName, listener)
      {
        // Removed queued events if applicable.
        var al = findAsleepListener(listener, false);
        if (al)
        {
          findQueuedEvent(al, evtName, true);        
        }
        
        // Remove listener from event dictionary.
        var e = getOrCreateEventDictEntry(evtName);
        findEventListener(e, listener, true);       
      },
      
      /**
       * Change the listener state
	     * @method
	     * @param {Object} listener The listener object
	     * @param {bool} isAwake Flag indicating whether or that the listener is in an awake state
	     */
      setState: function(listener, isAwake)
      {
        var i;
        
        if (isAwake)
        {
          // Move listener from sleeping state to awake state. Fire queued up events.
          var al = findAsleepListener(listener, true);
          
          if (al)
          {
            processQueuedEvents(al);
            awakeListeners.push(al.listener);
          }
        }
        else
        {
          // Move listener from awake state to sleeping state.
          var l = findAwakeListener(listener, true); 
          
          if (l)
          {         
            asleepListeners.push(createSleepingListener(l));          
          }
        }
      }
    };
  }
)();
//////////////////////
// ..\js\util\DebugConsole\Trigger.js
//////////////////////
var keyDown =  function(e) {
    if (e.ctrlKey && e.shiftKey) {
        if (e.getKey() === 76/* L */) {
        
            if (!Ext.getCmp("x-debug-browser") && !this.disableDiagnosticsWindow) {
                this.debugConsole = new RP.util.DebugConsole.Console();
            }
            else {
                this.debugConsole.destroy();
            }
            e.stopEvent();
        }
        
        if (e.getKey() === 83/* S */) {
            logger.toggleLogToServer();
            e.stopEvent();
        }
        
    }
};

Ext.EventManager.on(document, 'keydown', keyDown);

Ext.onReady(function(){
    
    // RPWEB-4338 Access to client side log
    // should be disabled when session expires
    RP.event.AppEventManager.register(RP.upb.AppEvents.SessionExpired, function() {
        Ext.EventManager.un(document, 'keydown', keyDown);
    }, this);
    
    RP.event.AppEventManager.register(RP.upb.AppEvents.ActiveAgain, function() {
        Ext.EventManager.on(document, 'keydown', keyDown);
    }, this);
    
  // In a production environment, disable the Diagnostics window when page inactivity
  // kicks in...
  if (RP.globals && RP.globals.SERVER_TYPE === "production") {
    if (RP.upb && RP.upb.AppEvents) {
      RP.event.AppEventManager.register(RP.upb.AppEvents.Inactive, function(){
        this.disableDiagnosticsWindow = true;
        if (Ext.getCmp("x-debug-browser")) {
          Ext.getCmp("x-debug-browser").destroy();
        }
      }, window.document);
      
      RP.event.AppEventManager.register(RP.upb.AppEvents.ActiveAgain, function(){
        this.disableDiagnosticsWindow = false;
      }, window.document);
    }
  }
  
  // Setup Ext log and timer
  Ext.apply(Ext, {
    log: function(){
      var debugConsole = Ext.getCmp("x-debug-browser");
      if (!debugConsole) {
        debugConsole = new RP.util.DebugConsole.Console();
      }
      debugConsole.logView.log.apply(debugConsole.logView, arguments);
    },
    
    logf: function(format, arg1, arg2, etc){
      Ext.log(String.format.apply(String, arguments));
    },
    
    dump: function(o){
      if (typeof o == 'string' || typeof o == 'number' || typeof o == 'undefined' || Ext.isDate(o)) {
        Ext.log(o);
      }
      else if (!o) {
        Ext.log("null");
      }
      else if (typeof o != "object") {
        Ext.log('Unknown return type');
      }
      else if (Ext.isArray(o)) {
        Ext.log('[' + o.join(',') + ']');
      }
      else {
        var b = ["{\n"];
        for (var key in o) {
          var to = typeof o[key];
          if (to != "function" && to != "object") {
            b.push(String.format("  {0}: {1},\n", key, o[key]));
          }
        }
        var s = b.join("");
        if (s.length > 3) {
          s = s.substr(0, s.length - 2);
        }
        Ext.log(s + "\n}");
      }
    },
    
    _timers: {},
    
    time: function(name){
      name = name || "def";
      Ext._timers[name] = new Date().getTime();
    },
    
    timeEnd: function(name, printResults){
      var t = new Date().getTime();
      name = name || "def";
      var v = String.format("{0} ms", t - Ext._timers[name]);
      Ext._timers[name] = new Date().getTime();
      if (printResults !== false) {
        Ext.log('Timer ' + (name == "def" ? v : name + ": " + v));
      }
      return v;
    }
  });
  
  // Setup console.log for browser that don't support it.
  if (typeof console === 'undefined') {
    console = {
      log: Ext.log,
      error: Ext.log
    };
  }
});
//////////////////////
// ..\js\util\DebugConsole\Console.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.Console
 * @extends Ext.Panel
 */
RP.util.DebugConsole.Console = Ext.extend(Ext.Panel, {
  constructor: function() {
    Ext.apply(this, {
      id: 'x-debug-browser'
    });
    RP.util.DebugConsole.Console.superclass.constructor.apply(this, arguments);
  },
  
  initComponent: function() {
    var console = this.createConsole();
    
    Ext.apply(this, {
        title: 'Console',
        renderTo: Ext.getBody(),
        collapsible: true,
        animCollapse: false,
        style: 'position:absolute;left:0;bottom:0;z-index:101',
        height:200,
        logView: console.logView,
        layout: 'fit',

        tools:[{
            id: 'close',
            scope: this,
            handler: function(){
                this.destroy();
                Ext.EventManager.removeResizeListener(this.handleResize, this);
            }
        }],

        items: console.tabPanel
    });
    
    RP.util.DebugConsole.Console.superclass.initComponent.call(this);
    
    this.on("afterlayout", function() {
      this.resizer = new Ext.Resizable(this.el, {
          minHeight:50,
          handles: "n",
          pinned: true,
          transparent:true,
          console: this,
          resizeElement : function(){
              var box = this.proxy.getBox();
              this.proxy.hide();
              this.console.setHeight(box.height);
              return box;
          }
      });
      
      Ext.EventManager.onWindowResize(this.handleResize, this);
      this.handleResize();
    }, this);
  },
  
  createConsole: function() {

    var scriptPanel = new RP.util.DebugConsole.ScriptsPanel();
    var logView = new RP.util.DebugConsole.LogPanel();
    var tree = new RP.util.DebugConsole.DomTree();
    var compInspector = new RP.util.DebugConsole.ComponentInspector();
    var compInfoPanel = new RP.util.DebugConsole.ComponentInfoPanel();
    var storeInspector = new RP.util.DebugConsole.StoreInspector();
    var objInspector = new RP.util.DebugConsole.ObjectInspector();
    var logViewer = new RP.util.DebugConsole.LogViewer();
    var stopWatch = new RP.util.DebugConsole.StopWatchesPanel();
    var tabPanel = new Ext.TabPanel({
        activeTab: 0,
        border: false,
        tabPosition: 'bottom',
        items: [{
            title: 'Log Viewer',
            layout: 'fit',
            items: [logViewer]
        },{
            title: 'Debug Console',
            layout:'border',
            items: [logView, scriptPanel]
        },{
            title: 'HTML Inspector',
            layout:'border',
            items: [tree]
        },{
            title: 'Component Inspector',
            layout: 'border',
            items: [compInspector,compInfoPanel]
        },{
            title: 'Object Inspector',
            layout: 'border',
            items: [objInspector]
        },{
            title: 'Data Stores',
            layout: 'border',
            items: [storeInspector]
        },{
            title: 'Stop Watch',
            layout: 'fit',
            items: [stopWatch]
        }]
    });
    
    return {
      logView: logView,
      tabPanel: tabPanel
    };
  },
  
  handleResize: function() {
        var b = Ext.getBody();
        var size = b.getViewSize();
        if(size.height < b.dom.scrollHeight) {
            size.width -= 18;
        }
        this.setWidth(size.width);
  }
});
//////////////////////
// ..\js\util\DebugConsole\ColumnNodeUI.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.ColumnNodeUI
 * @extends Ext.tree.TreeNodeUI
 */
RP.util.DebugConsole.ColumnNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    focus: Ext.emptyFn, // prevent odd scrolling behavior

    renderElements : function(n, a, targetNode, bulkRender){
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : '';

        var t = n.getOwnerTree();
        var cols = t.columns;
        var bw = t.borderWidth;
        var c = cols[0];

        var buf = [
             '<li class="x-tree-node"><div ext:tree-node-id="',n.id,'" class="x-tree-node-el x-tree-node-leaf ', a.cls,'">',
                '<div class="x-tree-col" style="width:',c.width-bw,'px;">',
                    '<span class="x-tree-node-indent">',this.indentMarkup,"</span>",
                    '<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow"/>',
                    '<img src="', a.icon || this.emptyIcon, '" class="x-tree-node-icon',(a.icon ? " x-tree-node-inline-icon" : ""),(a.iconCls ? " "+a.iconCls : ""),'" unselectable="on"/>',
                    '<a hidefocus="on" class="x-tree-node-anchor" href="',a.href ? a.href : "#",'" tabIndex="1" ',
                    a.hrefTarget ? ' target="'+a.hrefTarget+'"' : "", '>',
                    '<span unselectable="on">', n.text || (c.renderer ? c.renderer(a[c.dataIndex], n, a) : a[c.dataIndex]),"</span></a>",
                "</div>"];
         for(var i = 1, len = cols.length; i < len; i++){
             c = cols[i];

             buf.push('<div class="x-tree-col ',(c.cls?c.cls:''),'" style="width:',c.width-bw,'px;">',
                        '<div class="x-tree-col-text">',(c.renderer ? c.renderer(a[c.dataIndex], n, a) : a[c.dataIndex]),"</div>",
                      "</div>");
         }
         buf.push(
            '<div class="x-clear"></div></div>',
            '<ul class="x-tree-node-ct" style="display:none;"></ul>',
            "</li>");

        if(bulkRender !== true && n.nextSibling && n.nextSibling.ui.getEl()){
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin",
                                n.nextSibling.ui.getEl(), buf.join(""));
        }else{
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf.join(""));
        }

        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.firstChild.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        this.anchor = cs[3];
        this.textNode = cs[3].firstChild;
    }
});
//////////////////////
// ..\js\util\DebugConsole\ComponentInfoPanel.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.ComponentInfoPanel
 * @extends Ext.Panel
 */
RP.util.DebugConsole.ComponentInfoPanel = Ext.extend(Ext.Panel,{
    id:'x-debug-compinfo',
    region: 'east',
    minWidth: 200,
    split: true,
    width: 350,
    border: false,
    autoScroll: true,
    layout:'anchor',
    style:'border-width:0 0 0 1px;',

    initComponent: function() {
        this.watchBox = new Ext.form.Checkbox({
            id: 'x-debug-watchcomp',
            boxLabel: 'Watch ComponentMgr',
            listeners: {
                check: function(cb, val) {
                    if (val) {
                        Ext.ComponentMgr.all.on('add', this.onAdd, this);
                        Ext.ComponentMgr.all.on('remove', this.onRemove, this);
                    } else {
                        Ext.ComponentMgr.all.un('add', this.onAdd, this);
                        Ext.ComponentMgr.all.un('remove', this.onRemove, this);
                    }
                },
                scope: this
            }
        });

        this.tbar = new RP.ui.Toolbar({
          leftItems: [{
            text: 'Clear',
            handler: this.clear,
            scope: this
          }, '->', this.watchBox]
        });
        RP.util.DebugConsole.ComponentInfoPanel.superclass.initComponent.call(this);
    },

    onAdd: function(i, o, key) {
        var markup = ['<div style="padding:5px !important;border-bottom:1px solid #ccc;">',
                    'Added: '+o.id,
                    '</div>'].join('');
        this.insertMarkup(markup);
    },

    onRemove: function(o, key) {
        var markup = ['<div style="padding:5px !important;border-bottom:1px solid #ccc;">',
                    'Removed: '+o.id,
                    '</div>'].join('');
        this.insertMarkup(markup);
    },

    message: function(msg) {
        var markup = ['<div style="padding:5px !important;border-bottom:1px solid #ccc;">',
                    msg,
                    '</div>'].join('');
        this.insertMarkup(markup);
    },
    insertMarkup: function(markup) {
        this.body.insertHtml('beforeend', markup);
        this.body.scrollTo('top', 100000);
    },
    clear : function(){
        this.body.update('');
        this.body.dom.scrollTop = 0;
    }
});
//////////////////////
// ..\js\util\DebugConsole\ComponentInspector.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/**
 * Modified ext debug console.
 * @author Kevin Rice
 */
Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.ComponentInspector
 * @extends Ext.tree.TreePanel
 */
RP.util.DebugConsole.ComponentInspector = Ext.extend(Ext.tree.TreePanel, {
  enableDD: false,
  lines: false,
  rootVisible: false,
  animate: false,
  hlColor: 'ffff9c',
  autoScroll: true,
  region: 'center',
  border: false,
  initComponent: function() {
    this.bbar = new RP.ui.Toolbar({
      leftItems: [{
        text: 'Refresh',
        handler: this.refresh,
        scope: this
      }],
      centerItems: [{
        xtype: 'label',
        text: 'Component Path: '
      }, {
        xtype: 'textfield',
        itemId: 'componentPath',
        width: 200
      }, {
        xtype: 'button',
        itemId: 'validateButton',
        text: 'Validate Component Path',
        scope: this,
        handler: this.validateComponentPath
      }],
      rightItems: [{
        xtype: 'label',
        text: 'JavaScript Path: '
      }, {
        xtype: 'textfield',
        itemId: 'extPath',
        width: 200
      }],
      centerWidth: 500
    });
    RP.util.DebugConsole.ComponentInspector.superclass.initComponent.call(this);
    
    this.root = this.setRootNode(new Ext.tree.TreeNode({
      text: 'Ext Components',
      component: Ext.ComponentMgr.all,
      leaf: false
    }));
    this.parseRootNode();
    
    this.on('dblclick', this.onDblClick, this);
    this.on('click', this.onClick, this);
    this.on('expandnode', this.onExpand, this);
  },
  
  createNode: function(n, c) {
    var leaf = (c.items && c.items.length > 0);
    return n.appendChild(new Ext.tree.TreeNode({
      text: c.getItemId() + (c.getXType() ? ' [ ' + c.getXType() + ' ]' : ''),
      component: c,
      leaf: !leaf
    }));
  },
  
  createToolbarNode: function(n, c, text, accessorMethod) {
    var leaf = true;
    return n.appendChild(new Ext.tree.TreeNode({
      text: text + (c.getXType() ? ' [ ' + c.getXType() + ' ]' : ''),
      accessorMethod: accessorMethod,
      component: c,
      leaf: !leaf
    }));
  },
  
  parseChildItems: function(n) {
    var cn = n.attributes.component.items;
    if (cn) {
      for (var i = 0; i < cn.length; i++) {
        if (!Ext.isFunction(cn.get)) {
          continue;
        }
        var c = cn.get(i);
        if (c.id != this.id && c.id != this.bottomToolbar.id) {
          var newNode = this.createNode(n, c);
          if (!newNode.leaf) {
            this.parseChildItems(newNode);
          }
        }
      }
    }
    var comp = n.attributes.component;
    var newBarNode = null;
    if (comp.tbar) {
      newBarNode = this.createToolbarNode(n, comp.getTopToolbar(), "tbar", ".getTopToolbar()");
      if (!newBarNode.leaf) {
        this.parseChildItems(newBarNode);
      }
    }
    if (comp.bbar) {
      newBarNode = this.createToolbarNode(n, comp.getBottomToolbar(), "bbar", ".getBottomToolbar()");
      if (!newBarNode.leaf) {
        this.parseChildItems(newBarNode);
      }
    }
    if (comp.fbar) {
      newBarNode = this.createToolbarNode(n, comp.getFooterToolbar(), "fbar", ".getFooterToolbar()");
      if (!newBarNode.leaf) {
        this.parseChildItems(newBarNode);
      }
    }
  },
  
  parseRootNode: function() {
    var n = this.root;
    var cn = n.attributes.component.items;
    for (var i = 0, c; cn[i]; i++) {
      c = cn[i];
      if (c.id != this.id && c.id != this.bottomToolbar.id) {
        if (!c.ownerCt) {
          var newNode = this.createNode(n, c);
          if (!newNode.leaf) {
            this.parseChildItems(newNode);
          }
        }
      }
    }
  },
  
  onDblClick: function(node, e) {
    var oi = Ext.getCmp('x-debug-objinspector');
    oi.refreshNodes(node.attributes.component);
    oi.ownerCt.show();
  },
  
  onClick: function(node, event) {
    if (node.attributes.component.el) {
      node.attributes.component.el.frame("FF0000");
    }
    var componentPath = this.getComponentPath(node);
    this.getBottomToolbar().getCenterBar().getComponent('componentPath').setValue(componentPath);
    var scriptPath;
    try {
      scriptPath = RP.util.DebugConsole.Path.parse(componentPath);
    } 
    catch (exception) {
      scriptPath = exception.message;
    }
    this.getBottomToolbar().getRightBar().getComponent('extPath').setValue(scriptPath);
  },
  
  onExpand: function(node) {
    if (node.hasChildNodes() && node.childNodes.length === 1) {
      node.firstChild.expand();
    }
  },
  
  validateComponentPath: function() {
    var path = this.getBottomToolbar().getCenterBar().getComponent('componentPath').getValue();
    var component = undefined;
    var scriptPath;
    try {
      scriptPath = RP.util.DebugConsole.Path.parse(path);
      component = eval(scriptPath);
    } 
    catch (exception) {
      component = undefined;
    }
    if (!(component instanceof Ext.Component)) {
      this.showInvalidComponentBox("Invalid Component Path", "The path you entered does not return a component");
    }
    else {
      // Set the ext path to the javascript version of the path.
      this.getBottomToolbar().getRightBar().getComponent('extPath').setValue(scriptPath);
      var oi = Ext.getCmp('x-debug-objinspector');
      oi.refreshNodes(component);
      if (component.el !== undefined && !component.el.hasFxBlock()) {
        component.el.frame("FF0000");
      }
    }
  },
  
  refresh: function() {
    while (this.root.firstChild) {
      this.root.removeChild(this.root.firstChild);
    }
    this.parseRootNode();
    var ci = Ext.getCmp('x-debug-compinfo');
    if (ci) {
      ci.message('refreshed component tree - ' + Ext.ComponentMgr.all.length);
    }
  },
  
  getComponentPath: function(treeNode) {
    if (treeNode.isRoot) {
      return "";
    }
    else if (treeNode.parentNode.isRoot) {
      return treeNode.attributes.component.getItemId();
    }
    else if (treeNode.attributes.component.id.toLowerCase().indexOf("ext-comp") < 0) {
      return treeNode.attributes.component.id;
    }
    else if (treeNode.attributes.component.itemId) {
      return this.getComponentPath(treeNode.parentNode) + '/' + treeNode.attributes.component.getItemId();
    }
    else if (treeNode.attributes.accessorMethod) {
      return this.getComponentPath(treeNode.parentNode) + '/' + treeNode.attributes.accessorMethod;
    }
    else {
      var parentPath = this.getComponentPath(treeNode.parentNode);
      if (parentPath.substr(parentPath.length - 1) === "*") {
        return parentPath;
      }
      else {
        return parentPath + '/*';
      }
    }
  },
  
  showInvalidComponentBox: function(title, message) {
  
    var msgBox = Ext.MessageBox.show({
      title: title,
      msg: message,
      buttons: false,
      icon: Ext.MessageBox.INFO,
      closable: false
    });
    msgBox.getDialog().el.shadow.el.pause(1.2).fadeOut({
      remove: true
    });
    msgBox.getDialog().el.pause(1.2).fadeOut({
      callback: function() {
        msgBox.hide();
      }
    });
  }
});
//////////////////////
// ..\js\util\DebugConsole\ComponentNodeUI.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.ComponentNodeUI
 * @extends Ext.tree.TreeNodeUI
 */
RP.util.DebugConsole.ComponentNodeUI = Ext.extend(Ext.tree.TreeNodeUI,{
    onOver : function(evt){
        RP.util.DebugConsole.ComponentNodeUI.superclass.onOver.call(this);
        var cmp = this.node.attributes.component;
        if (cmp.el && cmp.el.mask && cmp.id !='x-debug-browser') {
            try { // Oddly bombs on some elements in IE, gets any we care about though
                cmp.el.mask();
            } catch(e) {}
        }
    },

    onOut : function(evt){
        RP.util.DebugConsole.ComponentNodeUI.superclass.onOut.call(this);
        var cmp = this.node.attributes.component;
        if (cmp.el && cmp.el.unmask && cmp.id !='x-debug-browser') {
            try {
                cmp.el.unmask();
            } catch(e) {}
        }
    }
});
//////////////////////
// ..\js\util\DebugConsole\DomTree.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.DomTree
 * @extends Ext.tree.TreePanel
 */
RP.util.DebugConsole.DomTree = Ext.extend(Ext.tree.TreePanel, {
    enableDD:false ,
    lines:false,
    rootVisible:false,
    animate:false,
    hlColor:'ffff9c',
    autoScroll: true,
    region:'center',
    border:false,

    initComponent : function(){


        RP.util.DebugConsole.DomTree.superclass.initComponent.call(this);

        // tree related stuff
        var styles = false, hnode;
        var nonSpace = /^\s*$/;
        var html = Ext.util.Format.htmlEncode;
        var ellipsis = Ext.util.Format.ellipsis;
        var styleRe = /\s?([a-z\-]*)\:([^;]*)(?:[;\s\n\r]*)/gi;

        function findNode(n){
            if(!n || n.nodeType != 1 || n == document.body || n == document){
                return false;
            }
            var pn = [n], p = n;
            while((p = p.parentNode) && p.nodeType == 1 && p.tagName.toUpperCase() != 'HTML'){
                pn.unshift(p);
            }
            var cn = hnode;
            for(var i = 0, len = pn.length; i < len; i++){
                cn.expand();
                cn = cn.findChild('htmlNode', pn[i]);
                if(!cn){ // in this dialog?
                    return false;
                }
            }
            cn.select();
            var a = cn.ui.anchor;
            this.getTreeEl().dom.scrollTop = Math.max(0 ,a.offsetTop-10);
            //treeEl.dom.scrollLeft = Math.max(0 ,a.offsetLeft-10); no likey
            cn.highlight();
            return true;
        }

        function nodeTitle(n){
            var s = n.tagName;
            if(n.id){
                s += '#'+n.id;
            }else if(n.className){
                s += '.'+n.className;
            }
            return s;
        }

        this.loader = new Ext.tree.TreeLoader();
        this.loader.load = function(n, cb){
            var isBody = n.attributes.node == document.body;
            var cn = n.attributes.node.childNodes;
            for(var i = 0, c;cn[i]; i++){
                c = cn[i];
                if(isBody && c.id == 'x-debug-browser'){
                    continue;
                }
                if(c.nodeType == 1){
                    n.appendChild(new RP.util.DebugConsole.HtmlNode({
                      node: c
                    }));
                }else if(c.nodeType == 3 && !nonSpace.test(c.nodeValue)){
                    n.appendChild(new Ext.tree.TreeNode({
                        text:'<em>' + ellipsis(html(String(c.nodeValue)), 35) + '</em>',
                        cls: 'x-tree-noicon'
                    }));
                }
            }
            cb();
        };

        //tree.getSelectionModel().on('selectionchange', onNodeSelect, null, {buffer:250});

        this.root = this.setRootNode(new Ext.tree.TreeNode('Ext'));

        hnode = this.root.appendChild(new RP.util.DebugConsole.HtmlNode({
          node: document.getElementsByTagName('html')[0]
        }));

    }
});
//////////////////////
// ..\js\util\DebugConsole\HtmlNode.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.HtmlNode
 * @extends Ext.tree.AsyncTreeNode
 */
RP.util.DebugConsole.HtmlNode = Ext.extend(Ext.tree.AsyncTreeNode, {
    cls: 'x-tree-noicon',
    preventHScroll: true,

    constructor: function() {
        var leaf = !this.hasChild(arguments[0].node);
        var attr = {
            text : this.renderNode(arguments[0].node, leaf),
            leaf : leaf,
            cls: 'x-tree-noicon'
        };
        
        Ext.apply(this, {
          htmlNode: arguments[0].node,
          tagName: arguments[0].node.tagName.toLowerCase()
        });
        
        RP.util.DebugConsole.HtmlNode.superclass.constructor.call(this, attr);
        this.attributes.node = arguments[0].node; // for searching
        
        if(!leaf){
            this.on('expand', this.onExpand,  this);
            this.on('collapse', this.onCollapse,  this);
        }
    },
    html: Ext.util.Format.htmlEncode,
    ellipsis: Ext.util.Format.ellipsis,
    nonSpace: /^\s*$/,

    attrs: [
        {n: 'id', v: 'id'},
        {n: 'className', v: 'class'},
        {n: 'name', v: 'name'},
        {n: 'type', v: 'type'},
        {n: 'src', v: 'src'},
        {n: 'href', v: 'href'}
    ],

    hasChild: function(n){
        for(var i = 0, c; n.childNodes[i]; i++){
            c = n.childNodes[i];
            if(c.nodeType == 1){
                return true;
            }
        }
        return false;
    },

    renderNode: function(n, leaf){
        var tag = n.tagName.toLowerCase();
        var s = '&lt;' + tag;
        for(var i = 0, len = this.attrs.length; i < len; i++){
            var a = this.attrs[i];
            var v = n[a.n];
            if(v && !this.nonSpace.test(v)){
                s += ' ' + a.v + '=&quot;<i>' + this.html(v) +'</i>&quot;';
            }
        }
        var style = n.style ? n.style.cssText : '';
        if(style){
            s += ' style=&quot;<i>' + this.html(style.toLowerCase()) +'</i>&quot;';
        }
        if(leaf && n.childNodes.length > 0){
            s+='&gt;<em>' + this.ellipsis(this.html(String(n.innerHTML)), 35) + '</em>&lt;/'+tag+'&gt;';
        }else if(leaf){
            s += ' /&gt;';
        }else{
            s += '&gt;';
        }
        return s;
    },

    refresh : function(highlight){
        var leaf = !hasChild(this.htmlNode);
        this.setText(renderNode(this.htmlNode, leaf));
        if(highlight){
            Ext.fly(this.ui.textNode).highlight();
        }
    },

    onExpand : function(){
        if(!this.closeNode && this.parentNode){
            this.closeNode = this.parentNode.insertBefore(new Ext.tree.TreeNode({
                text:'&lt;/' + this.tagName + '&gt;',
                cls: 'x-tree-noicon'
            }), this.nextSibling);
        }else if(this.closeNode){
            this.closeNode.ui.show();
        }
    },

    onCollapse : function(){
        if(this.closeNode){
            this.closeNode.ui.hide();
        }
    },

    render : function(bulkRender){
        RP.util.DebugConsole.HtmlNode.superclass.render.call(this, bulkRender);
    },

    highlightNode : function(){
        //Ext.fly(this.htmlNode).highlight();
    },

    highlight : function(){
        //Ext.fly(this.ui.textNode).highlight();
    },

    frame : function(){
        this.htmlNode.style.border = '1px solid #0000ff';
        //this.highlightNode();
    },

    unframe : function(){
        //Ext.fly(this.htmlNode).removeClass('x-debug-frame');
        this.htmlNode.style.border = '';
    }
});
//////////////////////
// ..\js\util\DebugConsole\LogPanel.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.LogPanel
 * @extends Ext.Panel
 */
RP.util.DebugConsole.LogPanel = Ext.extend(Ext.Panel, {
    autoScroll: true,
    region: 'center',
    border: false,
    style:'border-width:0 1px 0 0',

    initComponent: function() {
      Ext.apply(this, {
        renderTo: Ext.getBody()
      });
      
      RP.util.DebugConsole.LogPanel.superclass.initComponent.call(this);
    },
    
    log : function(){
        var markup = [  '<div style="padding:5px !important;border-bottom:1px solid #ccc;">',
                    Ext.util.Format.htmlEncode(Array.prototype.join.call(arguments, ', ')).replace(/\n/g, '<br/>').replace(/\s/g, '&#160;'),
                    '</div>'].join(''),
            bd = this.body.dom;

        this.body.insertHtml('beforeend', markup);
        bd.scrollTop = bd.scrollHeight;
    },

    clear : function(){
        this.body.update('');
        this.body.dom.scrollTop = 0;
    }
});
//////////////////////
// ..\js\util\DebugConsole\LogViewer.js
//////////////////////
Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.LogViewer
 * @extends Ext.Panel
 * 
 * The 'Log Viewer' tab of the debugging console.
 */
RP.util.DebugConsole.LogViewer = Ext.extend(Ext.Panel, {
  initComponent: function(){
    var tpl = new Ext.XTemplate('<tpl for=".">', '<div class="rp-log {cls}">{message}</div>', '</tpl>');
    
    // Setup button pressed state
    this.pressedButtons = new Ext.util.MixedCollection();
    this.pressedButtons.addAll({
      TRACE: 'TRACE',
      DEBUG: 'DEBUG',
      WARNING: 'WARNING',
      INFO: 'INFO',
      ERROR: 'ERROR',
      FATAL: 'FATAL'
    });
    
    // Prevent new log messages getting dumped into the log panel until Refresh has been clicked.
    logger.getStore().suspendEvents(false);
    
    Ext.apply(this, {
      itemId: 'logsTab',
      frame: false,
      autoScroll: true,
      bbar: new RP.ui.Toolbar(this.buildBottomBarSettings()),
      items: [{
        xtype: 'dataview',
        itemSelector: "div.rp-log",
        itemId: 'logMessagesPanel',
        store: logger.getStore(),
        emptyText: "No log messages to display",
        reserveScrollOffset: true,
        tpl: tpl,
        layout: 'fit',
        id: 'logger_out',
        frame: false,
        border: false
      }]
    });
    
    RP.util.DebugConsole.LogViewer.superclass.initComponent.call(this);
    
    this.mon(logger, "logtoservertoggled", this.logToServerHandler, this);
    
    this.on('afterlayout', function(){

      // The user can only log messages to the server after a session had been established. So, we need to
      // disable the checkbox to for logging to server until the session has been established.
      if (logger.getLogToServerURL()) {
        this.getBottomToolbar().getRightBar().getComponent("logToServer").enable();
      }
      else {
        logger.logTrace('[Diagnostic] getLogToServerURL not set');
        this.getBottomToolbar().getRightBar().getComponent("logToServer").disable();
      }
      
      this.scrollToBottom();
    }, this);
    
    logger.getStore().on("add", function() {
      this.scrollToBottom();
    }, this);
  },
  
  logToServerHandler: function(){
    
    var button =  this.getBottomToolbar().getRightBar().getComponent("logToServer");
    button.toggle(logger.getIsLogToServer(), true);
    
    if (logger.getIsLogToServer()) {
      button.setIconClass('rp-debug-icon-check');
    }
    else {
      button.setIconClass('rp-debug-icon-x');
    }

  },
  
  buildBottomBarSettings: function(){
    var settingItems = {
      centerWidth: 375,
      leftItems: ["-", {
        xtype: 'button',
        itemId: 'refresh',
        text: 'Refresh',
        iconCls: 'rp-debug-icon-x',
        scope: this,
        enableToggle: true,
        toggleHandler: function(button, pressed){
          this.onToggleRefreshHandler(button, pressed);
        }
      }, "-", {
        xtype: 'button',
        itemId: 'clear',
        text: 'Clear',
        scope: this,
        handler: function() {
          this.onClear();
        }
      }, "-"],
      centerItems: ["-", {
        xtype: 'button',
        text: '<div class="rp-log-t">Trace</div>',
        pressed: true,
        itemId: 'trace',
        iconCls: 'rp-debug-icon-check',
        enableToggle: true,
        listeners: {
          scope: this,
          toggle: function(button, pressed) {
            this.onToggle(button, pressed);
          }
        }
      }, "-", {
        xtype: 'button',
        text: '<div class="rp-log-d">Debug</div>',
        pressed: true,
        itemId: 'debug',
        iconCls: 'rp-debug-icon-check',
        enableToggle: true,
        listeners: {
          scope: this,
          toggle: function(button, pressed) {
            this.onToggle(button, pressed);
          }
        }
      }, "-", {
        xtype: 'button',
        text: '<div class="rp-log-i">Info</div>',
        pressed: true,
        itemId: 'info',
        iconCls: 'rp-debug-icon-check',
        enableToggle: true,
        listeners: {
          scope: this,
          toggle: function(button, pressed) {
            this.onToggle(button, pressed);
          }
        }
      }, "-", {
        xtype: 'button',
        text: '<div class="rp-log-w">Warning</div>',
        pressed: true,
        itemId: 'warning',
        iconCls: 'rp-debug-icon-check',
        enableToggle: true,
        listeners: {
          scope: this,
          toggle: function(button, pressed) {
            this.onToggle(button, pressed);
          }
        }
      }, "-", {
        xtype: 'button',
        text: '<div class="rp-log-e">Error</div>',
        pressed: true,
        itemId: 'error',
        iconCls: 'rp-debug-icon-check',
        enableToggle: true,
        listeners: {
          scope: this,
          toggle: function(button, pressed) {
            this.onToggle(button, pressed);
          }
        }
      }, "-", {
        xtype: 'button',
        text: '<div class="rp-log-f">Fatal</div>',
        pressed: true,
        itemId: 'fatal',
        iconCls: 'rp-debug-icon-check',
        enableToggle: true,
        listeners: {
          scope: this,
          toggle: function(button, pressed) {
            this.onToggle(button, pressed);
          }
        }
      }, "-"],
      rightItems: ["-", {
          xtype: "button",
          text: "Show Message IDs",
          itemId: "showMsgBtn",
          enableToggle: true,
          hidden: RP.globals.getValue("SERVER_TYPE") === "production",
          pressed: RP.locale.Dispatch.isMessageKeyEnabled(),
          listeners: {
            scope: this,
            toggle: this.onShowMessageButtonToggle
          }
        }, {
        xtype: 'button',
        text: 'Log to Server',
        align: 'left',
        itemId: 'logToServer',
        buttonAlign: 'left',
        enableToggle: true,
        pressed: logger.getIsLogToServer(),
        iconCls: this.getLogToServerIcon(),
        listeners: {
          toggle: function(button, pressed){
            if (pressed) {
              button.setIconClass('rp-debug-icon-check');
            }
            else {
              button.setIconClass('rp-debug-icon-x');
            }
            logger.toggleLogToServer();
          }
        }
      }, "-"]
    };
    
    return settingItems;
  },
  
  onShowMessageButtonToggle: function(button, pressed) {
    if(pressed) {
      // Set the path to null otherwise the clear function can't clear the cookie.
      // http://www.sencha.com/forum/showthread.php?98070-CLOSED-Ext.util.Cookies.clear%28%29-not-working
      Ext.util.Cookies.set("rp-showmessages", true, null, null);
    }
    else {
      Ext.util.Cookies.clear("rp-showmessages");
    }
    RP.util.Helpers.reload();
  },
  
  getLogToServerIcon: function() {
    var iconCls = 'rp-debug-icon-x';
    if (logger.getIsLogToServer()) {
      iconCls = 'rp-debug-icon-check';
    }
    return iconCls;
  },
  
  onToggle: function(button, pressed) {
    if (pressed) {
      this.pressedButtons.add(button.itemId.toUpperCase(), button.itemId.toUpperCase());
      button.setIconClass('rp-debug-icon-check');
    }
    else {
      this.pressedButtons.removeKey(button.itemId.toUpperCase());
      button.setIconClass('rp-debug-icon-x');
    }
    
    this.updateLoggerFilter(this.pressedButtons.getRange());
  },
  
  /**
   * Handle when happens when the refresh button is clicked
   * @param {Object} button refresh
   * @param {Boolean} pressed 
   */
  onToggleRefreshHandler: function(button, pressed) {
    // When the Refresh button is pressed then Live Refresh should be enabled and the
    // window will automatically update has new log message or stop watches have been added.
    if (pressed === true) {
      logger.getStore().resumeEvents();
      this.updateLoggerFilter(this.pressedButtons.getRange());
      this.scrollToBottom();
      
      button.setIconClass('rp-debug-icon-check');
    }
    else {
      logger.getStore().suspendEvents(false);
      
      button.setIconClass('rp-debug-icon-x');
    }
  },
  
  /**
   * Handles the click event on the clear button. Clears the contents of the
   * Log panel.
   */
  onClear: function(){
    if (logger) {
      logger.clearAll();
    }
  },
  
  updateLoggerFilter: function(levels) {
    logger.filterBy(levels);
    
    // If the refresh button is unchecked then events are
    // suspended for the store and nothing will be updated
    // until an event is fired.
    if (logger.getStore().eventsSuspended) {
      logger.getStore().resumeEvents();
      logger.getStore().fireEvent("datachanged");
      logger.getStore().suspendEvents(false);
    }
  },
  
  /**
   * Scroll to the bottom of the current log panel.
   */
  scrollToBottom: function(){
    var content = this.body;
    content.dom.scrollBottom = content.dom.scrollHeight - content.dom.offsetHeight;
    content.scroll("b", content.dom.scrollBottom, true);
  }
});
//////////////////////
// ..\js\util\DebugConsole\ObjectInspector.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.ObjectInspector
 * @extends Ext.tree.TreePanel
 * 
 * A TreePanel extension that can be used to inspect the properties of an
 * object.  This component is also used by the 'Data Stores' tab as well.
 */
RP.util.DebugConsole.ObjectInspector = Ext.extend(Ext.tree.TreePanel, {
    id: 'x-debug-objinspector',
    enableDD:false ,
    lines:false,
    rootVisible:false,
    animate:false,
    hlColor:'ffff9c',
    autoScroll: true,
    region:'center',
    border:false,
    borderWidth: Ext.isBorderBox ? 0 : 2, // the combined left/right border for each cell
    cls:'x-column-tree',

    initComponent : function(){
        this.showFunc = false;
        this.toggleFunc = function() {
            this.showFunc = !this.showFunc;
            this.refreshNodes(this.currentObject);
        };
        this.bbar = new RP.ui.Toolbar({
          leftItems: [{
            text: 'Show Functions',
            enableToggle: true,
            pressed: false,
            handler: this.toggleFunc,
            scope: this
          }]
        });

        Ext.apply(this,{
            title: ' ',
            loader: new Ext.tree.TreeLoader(),
            columns:[{
                header:'Property',
                width: 300,
                dataIndex:'name'
            },{
                header:'Value',
                width: 900,
                dataIndex:'value'
            }]
        });

        RP.util.DebugConsole.ObjectInspector.superclass.initComponent.call(this);

        this.root = this.setRootNode(new Ext.tree.TreeNode({
            text: 'Dummy Node',
            leaf: false
        }));

        if (this.currentObject) {
            this.parseNodes();
        }
    },

    refreshNodes: function(newObj) {
        this.currentObject = newObj;
        var node = this.root;
        while(node.firstChild){
            node.removeChild(node.firstChild);
        }
        this.parseNodes();
    },

    parseNodes: function() {
        for (var o in this.currentObject) {
            if (!this.showFunc) {
                if (Ext.isFunction(this.currentObject[o])) {
                    continue;
                }
            }
            this.createNode(o);
        }
    },

    createNode: function(o) {
        return this.root.appendChild(new Ext.tree.TreeNode({
            name: o,
            value: this.currentObject[o],
            uiProvider:RP.util.DebugConsole.ColumnNodeUI,
            iconCls: 'x-debug-node',
            leaf: true
        }));
    },

    onRender : function(){
        RP.util.DebugConsole.ObjectInspector.superclass.onRender.apply(this, arguments);
        this.headers = this.header.createChild({cls:'x-tree-headers'});

        var cols = this.columns, c;
        var totalWidth = 0;

        for(var i = 0, len = cols.length; i < len; i++){
             c = cols[i];
             totalWidth += c.width;
             this.headers.createChild({
                 cls:'x-tree-hd ' + (c.cls?c.cls+'-hd':''),
                 cn: {
                     cls:'x-tree-hd-text',
                     html: c.header
                 },
                 style:'width:'+(c.width-this.borderWidth)+'px;'
             });
        }
        this.headers.createChild({cls:'x-clear'});
        // prevent floats from wrapping when clipped
        this.headers.setWidth(totalWidth);
        this.innerCt.setWidth(totalWidth);
    }
});
//////////////////////
// ..\js\util\DebugConsole\Path.js
//////////////////////
Ext.ns("RP.util.DebugConsole");

RP.util.DebugConsole.Path = function () {
    var doParse = function(path) {
        var chunks = path.split("/");
        
        pathPieces.push("window.Ext");
        addId(chunks[0]);
        
        for (var i = 1; i < chunks.length; i++) {
            if (chunks[i] === "*") {
                if(i + 1 < chunks.length && chunks[i + 1].indexOf("=") === -1) {
                    i++;
                    addWildCardItemId(chunks[i]);
                }
                else if(i + 1 >= chunks.length) {
                    throw new Error("Parse Error: Path cannot end with a '*' wildcard.");
                }
            }
            // a period followed by one or more characters
            else if (chunks[i].match("^\\..+")) {
                addLiteral(chunks[i]);
            }
            else if(chunks[i].match("^css=.+")) {
                addCss(chunks[i]);
            }
            else if(chunks[i].match("^xtype=.+")) {
                addXtype(chunks[i]);
            }
            else {
                addItemId(chunks[i]);
            }
        }
        
        return pathString();
    };
    
    var pathString = function() {
        return pathPieces.join("");
    };
    
    var addId = function(id) {
        if (id.charAt(0) !== ".") {
            pathPieces.push(".getCmp('" + id  + "')");
        }
        else {
            pathPieces.push(id);
        }
    };
    
    var addItemId = function(itemId) {
        pathPieces.push(".getComponent('" + itemId  + "')");
    };
    
    var addLiteral = function(literal) {
        pathPieces.push(literal);
    };
    
    var addCss = function(cssSelector) {
        cssSelector = extractValue(cssSelector);
        var pathSoFar = pathString();
        var cssString = pathSoFar + ".el.child('" + cssSelector + "').id";

        // we don't need the previous pieces since we're going to key off of
        // an actual DOM id.
        pathPieces = [];
        pathPieces.push("window.Ext.getCmp(" + cssString + ")");
    };
    
    var addWildCardItemId = function(itemId) {
        pathPieces.push(".find('itemId', '" + itemId + "')[0]");
    };
    
    var addXtype = function(xtype) {
        xtype = extractValue(xtype);
        pathPieces.push(".findByType('" + xtype + "')[0]");
    };
    
    var extractValue = function(typeValuePair) {
        return typeValuePair.substring(typeValuePair.indexOf("=") + 1);
    };
    
    var pathPieces = [];
    
    return {
        parse: function(path) {
            pathPieces = [];
            return doParse(path);
        }
    };
}();
//////////////////////
// ..\js\util\DebugConsole\ScriptPanel.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.ScriptsPanel
 * @extends Ext.Panel
 * 
 * A debugging tool that acts similar to the console tab of Firebug.  This
 * can be used to evaluate/inject custom javascript at runtime in browsers such
 * as IE.
 */
RP.util.DebugConsole.ScriptsPanel = Ext.extend(Ext.Panel, {
    id:'x-debug-scripts',
    region: 'east',
    minWidth: 200,
    split: true,
    width: 350,
    border: false,
    layout:'anchor',
    style:'border-width:0 0 0 1px;',

    initComponent : function(){

        this.scriptField = new Ext.form.TextArea({
            anchor: '100% -26',
            style:'border-width:0;'
        });

        this.trapBox = new Ext.form.Checkbox({
            id: 'console-trap',
            boxLabel: 'Trap Errors',
            checked: true
        });

        this.toolbar = new RP.ui.Toolbar({
            leftItems: [
            {
                xtype: 'button',
                text: 'Run',
                scope: this,
                handler: this.evalScript
            },{
                xtype: 'button',
                text: 'Clear',
                scope: this,
                handler: this.clear
            },
            '->',
            this.trapBox,
            ' ', ' ']
        });

        this.items = [this.toolbar, this.scriptField];

        RP.util.DebugConsole.ScriptsPanel.superclass.initComponent.call(this);
    },

    evalScript : function(){
        var s = this.scriptField.getValue();
        var rt;
        if(this.trapBox.getValue()){
            try{
                rt = eval(s);
                Ext.dump(rt === undefined? '(no return)' : rt);
            }catch(e){
                Ext.log(e.message || e.descript);
            }
        }else{
            rt = eval(s);
            Ext.dump(rt === undefined? '(no return)' : rt);
        }
    },

    clear : function(){
        this.scriptField.setValue('');
        this.scriptField.focus();
    }
});
//////////////////////
// ..\js\util\DebugConsole\StopWatchesPanel.js
//////////////////////
Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.StopWatchesPanel
 * @extends Ext.Panel
 *
 * The Stop Watch view inside the debugging console.
 */
RP.util.DebugConsole.StopWatchesPanel = Ext.extend(Ext.Panel, {

    initComponent: function() {
        Ext.apply(this, {
            itemId: 'stopWatchesTab',
            frame: false,
            bbar: new RP.ui.Toolbar(this.buildBottomBarSettings()),
            items: [{
                xtype: 'panel',
                itemId: 'stopWatchesMessagesPanel',
                layout: 'fit',
                id: 'stopwatch_out',
                frame: false,
                border: false
            }]
        });

        RP.util.DebugConsole.StopWatchesPanel.superclass.initComponent.call(this);

        this.on('afterlayout', function() {
            this.updateStopwatch();
        }, this);
    },
    buildBottomBarSettings: function() {
        return {
            leftItems: ["-",{
                xtype: 'button',
                itemId: 'refresh',
                text: 'Refresh',
                iconCls: 'rp-debug-icon-x',
                scope: this,
                enableToggle: true,
                toggleHandler: function(button, pressed) {
                    this.onToggleRefreshHandler(button, pressed);
                }
            }, "-",{
                xtype: 'button',
                itemId: 'clear',
                text: 'Clear',
                scope: this,
                handler: function() {
                    this.onClear();
                }
            }, "-"]
        };
    },
    
    /**
     * Handle when happens when the refresh button is clicked
     * @param {Object} button refresh
     * @param {Boolean} pressed
     */
    onToggleRefreshHandler: function(button, pressed) {
        // When the Refresh button is pressed then Live Refresh should be enabled and the
        // window will automatically update has new log message or stop watches have been added.
        if (pressed === true) {
            this.updateStopwatch();
            RP.util.stopwatch.addListener(this.updateStopwatch.createDelegate(this, [true], false));
            button.setIconClass('rp-debug-icon-check');
        }
        else {
            this.updateStopwatch();
            RP.util.stopwatch.removeListener(this.updateStopwatch.createDelegate(this, [true], false));
            button.setIconClass('rp-debug-icon-x');
        }
    },
    
    /**
     * Handles the click event on the clear button. Clears the contents of the
     * stopwatch panel.
     */
    onClear: function() {
        if (RP.util.stopwatch) {
            RP.util.stopwatch.clearAll();
            Ext.get('stopwatch_out').update('');
        }
    },
    /**
     * Calls out to the stopwatch to refresh the data in the stopwatch panel
     */
    updateStopwatch: function() {
        if (RP.util.stopwatch) {
            RP.util.stopwatch.report('stopwatch_out');
        }
    }
});
//////////////////////
// ..\js\util\DebugConsole\StoreInspector.js
//////////////////////
/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

/**
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.StoreInspector
 * @extends Ext.tree.TreePanel
 * 
 * An inspector that looks through all Ext.data.Stores that are registered
 * within the Ext.StoreMgr.  This can then be used to view a store's fields
 * directly through the debug console.
 */
RP.util.DebugConsole.StoreInspector = Ext.extend(Ext.tree.TreePanel, {
    enableDD:false ,
    lines:false,
    rootVisible:false,
    animate:false,
    hlColor:'ffff9c',
    autoScroll: true,
    region:'center',
    border:false,

    initComponent: function() {
        this.bbar = new RP.ui.Toolbar({
          leftItems: [{
            text: 'Refresh',
            handler: this.refresh,
            scope: this
          }]
        });
        RP.util.DebugConsole.StoreInspector.superclass.initComponent.call(this);

        this.root = this.setRootNode(new Ext.tree.TreeNode({
            text: 'Data Stores',
            leaf: false
        }));
        this.on('click', this.onClick, this);
        
        this.parseStores();
    },

    parseStores: function() {
        var cn = Ext.StoreMgr.items;
        for (var i = 0,c;cn[i];i++) {
            c = cn[i];
            
            var childNode = this.root.appendChild(new Ext.tree.TreeNode({
                text: c.storeId + ' - ' + c.totalLength + ' records',
                component: c,
                leaf: true
            }), this);
        }
    },

    onClick: function(node, e) {
        var oi = Ext.getCmp('x-debug-objinspector');
        oi.refreshNodes(node.attributes.component);
        oi.ownerCt.show();
    },
    
    onExpand: function(node, event) {
        node.attribute.component.each(function(record) {
              childNode.appendChild({
                text: record.data,
                leaf: true
              });
            }, this);
    },

    refresh: function() {
        while (this.root.firstChild) {
            this.root.removeChild(this.root.firstChild);
        }
        this.parseStores();
    }
});
//////////////////////
// ..\js\util\Grid.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class Grid
 */
RP.util.Grid = (function()
{
  return {
    /**
     * @deprecated
     * @method
     * @param {Object} cmodel The column model configuration of the grid.
     * @param {Object} config A grid configuration (Unused).
     * @return {Object} Column model configuration.
     */
    processColumnModel: function(cmodel, config)
    {
      logger.logWarning("The method processColumnModel is deprecated and should not be used.");
      // A copy is necessary if you instantiate two or more of the same grid.  If you overwrite
      // cm, then the next instance will inherit this and the UI doesn't behave properly.
      var cm = [];
      Ext.each(cmodel, function(c)
      {
        cm.push(RP.util.Object.mergeProperties(c));
      }, this);

      if (cm && cm.length)
      {
        for (var i = 0, len = cm.length; i < len; i++)
        {
          var col = cm[i];
          if (!col.name)
          {
            col.name = 'col_' + i;
          }
          col.id = col.name;
          col.dataIndex = col.name;

          // eval renderer to fn
          if (col.renderer)
          {
            col.renderer = eval(col.renderer);
          }

          if (col.editor && typeof (col.editor) === "string")
          {
            col.editor = eval(col.editor);
          }
        }
      }
      return cm;
    }
  };
})();
//////////////////////
// ..\js\util\Helpers.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.Helpers
 * @singleton
 */
RP.util.Helpers = function() {
    var sessionExpired = false;

    Ext.onReady(function(){
        // When the session has become expired we change the sessionExpired flag true. After the
        // user has reauthenciated the sessionExpired will be changed back to false.
        RP.event.AppEventManager.register(RP.upb.AppEvents.SessionReauthenticated, function() {
            sessionExpired = false;
        });

        RP.event.AppEventManager.register(RP.upb.AppEvents.SessionExpired, function(){
            sessionExpired = true;
        });
    });

    return {
        /**
         * Logs out the user and redirects them back to the login page.
         */
        logout: function() {
            RP.util.Helpers.redirect(RP.globals.getFullyQualifiedPath("LOGOUT"));
        },

        /**
         * Redirects the page to the specified url.
         * @param {Object} url
         * @param {Object} millis (Optional) The number of milliseconds for the setTimeout call.
         */
        redirect: function(url, millis) {
            (function() {
                try {
                    window.location.assign(url);
                }
                catch (e) {
                } /* Catching IE onbeforeunload Unspecified error */
            }).defer(millis || 0);
        },

        /**
         * Redirects the browser to the login URL.
         */
        redirectToLogin: function() {
            RP.util.Helpers.redirect(RP.globals.getFullyQualifiedPath("LOGIN"));
        },

        isReverseProxy: function() {
            return RP.util.Helpers.isExternalAuthentication();
        },

        isExternalAuthentication: function() {
            return RP.upb.PageBootstrapper.isExternallyAuthenticated();
        },

        isNativeLogin: function() {
            return RP.upb.PageBootstrapper.isNativeLogin();
        },

        /**
         * Reloads the current page.
         * @param {Object} millis (Optional) The number of milliseconds for the setTimeout call.
         */
        reload: function(millis) {
            (function() {
                window.location.reload();
            }).defer(millis || 0);
        },

        /**
         * Marks the current session for requiring re-authentication.
         */
        markSessionForReAuthentication: function() {
            Ext.Ajax.requestWithTextParams({
                url: RP.globals.getFullyQualifiedPath('REAUTHENTICATE')
            });
        },

        /**
         * Keep the current session alive by making a heart beat Ajax request to the server.
         * @param {Number} lastActivityMillis Milliseconds since epoch of the last activity the browser detected, defaults to now if null
         */
        keepSessionAlive: function(lastActivityMillis) {
            Ext.Ajax.requestWithTextParams({
                url: RP.buildDataServiceUrl("rp", "admin/sessionKeepAlive"),
                disableExceptionHandling: true,
                params: {
                    lastActivity: lastActivityMillis || new Date().getTime()
                },
                success: function() {
                    RP.event.AppEventManager.fire(RP.upb.AppEvents.KeepAliveSuccess, {});
                },
                failure: function() {
                    RP.event.AppEventManager.fire(RP.upb.AppEvents.KeepAliveFailure, {});
                }
            });
            logger.logInfo("[Helpers] keepSessionAlive with lastActivityDate: " + lastActivityMillis);
            // fire app event for interested listeners
            RP.event.AppEventManager.fire(RP.upb.AppEvents.KeepAlive, {});
        },

        /**
         * Checks for the last activity of the user in the browser as saved by the server
         * @param {Object} successFn callback function on success
         * @param {Object} failureFn callback function on failure
         * @param {Object} scope scope of the callbacks
         */
        isUserActive: function(successFn, failureFn, scope) {
            Ext.Ajax.requestWithTextParams({
                url: RP.buildDataServiceUrl("rp", "admin/isUserActive"),
                failure: failureFn,
                success: successFn,
                scope: scope
            });
        },

        /**
         * Checks to see if the users session has expired. Returns true if the session is expired.
         * @return {Boolean} True if session is expired.
         */
        isSessionExpired: function() {
            return sessionExpired;
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
        }
    };
}();
//////////////////////
// ..\js\util\CSSLoader.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class Task
 * @singleton
 */
RP.util.CSSLoader = (function() {
    var downloaded = [];
    
    var includeCSS = function(url) {
        if (Ext.isEmpty(downloaded[url])) {
            var h = document.getElementsByTagName("HEAD")[0];
            var css = document.createElement("link");
            css.type = "text/css";
            css.href = url;
            css.rel = "stylesheet";
            h.appendChild(css);
            
            logger.logInfo("[CSSLoader] Loading CSS: " + url);
            
            downloaded[url] = 1;
        }
    };
    
    /**
     * Fixes URLs to load properly
     * @param {String} url The URL to be transformed
     * @param {Boolean} prefix Adds static URL if it would result in a valid url
     */
    var urlTransform = function(url, prefix) {
        prefix = prefix && RP.globals && !Ext.isEmpty(RP.globals.getPath("STATIC"));
        
        if (prefix && (url.indexOf("://") === -1) && url.indexOf(RP.globals.getPath("STATIC")) !== 0) {
            url = RP.globals.getPath("STATIC") + url;
        }
        
        //Replace {client-mode} with the client mode, except for src which is replaced
        //with debug because src mode is not currently supported on css files
        if (url && url.indexOf("{client-mode}" >= 0)) {
            url = url.replace(/\{client-mode\}/g, (RP.globals.getValue("CLIENTMODE") === "src" ? "debug" : RP.globals.getValue("CLIENTMODE")));
        }
        
        return url;
    };
    
    return {
        /**
         * Downloads CSS files asynchronously
         * @param {Array or String} urls
         * @param {Boolean} prefixStaticURL Prefix relative URLs with RP.globals.getPath("STATIC")
         */
        load: function(urls, prefixStaticURL) {
            if (Ext.isEmpty(urls)) {
                return;
            }
            
            if (!Ext.isArray(urls)) {
                urls = [urls];
            }
            
            Ext.each(urls, function(url) {
                url = urlTransform(url, prefixStaticURL);
                includeCSS(url);
            });
        }
    };
})();
//////////////////////
// ..\js\util\DataStore.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class DataStore
 * <p>
 * Utilities for Ext stores.  The 'join' method joins one or more stores into an
 * existing store using single column keyed PKs.  Example:
 <pre><code>
var englishStore = new Ext.data.ArrayStore({
	fields:
	[
		{ name: "number", type: "int" },
		{ name: "word" }
	],
	data:
	[
		[ 0, "zero" ],
		[ 1, "one" ]
	]
});

var spanishStore = new Ext.data.ArrayStore({
	fields:
	[
		{ name: "numero", type: "int" },
		{ name: "wordo" }
	],
	data:
	[
		[ 1, "uno" ],
		[ 2, "dos" ]
	]
});

var joinedStore = new Ext.data.ArrayStore({
	fields:
	[
		{ name: "number", type: "int" },
		{ name: "englishSpelling" },
		{ name: "spanishSpelling" }
	]
});

RP.util.DataStore.join(
	[
		// source store #1
		{
			store: englishStore,
			pk: "number",
			map: [ { src: "word", dest: "englishSpelling" } ]
		},
		// source store #2
		{
			store: spanishStore,
			pk: "numero",
			map: [ { src: "wordo", dest: "spanishSpelling" } ]
		}
	],
	{ // destination store
		store: joinedStore,
		pk: "number"
	}
);
	
// This will produce the following:
number   englishSpelling  spanishSpelling
------   ---------------  ---------------
0        zero             [undefined]
1        one              uno
2        [undefined]      dos
</code></pre>
 * 
 * The 'transpose' method transposes a table (i.e., turn rows into columns, and columns into rows).
 * The source store values may also be optionally transformed (if original data should be kept 
 * intact, make a copy before calling this function!), as well as the destination table.  
 * Transformation on the source store takes place before transposition, while transformation on 
 * the destination store takes place afterwards. Example: 
 <pre><code>
Suppose you have the following table that lists your sales for the week:

Date		Sales		Cost
---------	--------	---------
1/1/10		100,000		65,000
1/2/10		105,000		67,000
1/3/10		110,000		69,000
1/4/10		98,000		63,000
1/5/10		118,000		70,000
1/6/10		112,000		65,000
1/7/10		125,000		71,000

... and you want to generate a report that transposes the data into this:

Metric      Day1    Day2    Day3    Day4    Day5    Day6    Day7    Total
----------  ----    ----    ----    ----    ----    ----    ----    --------
Sales (k)   100     105     110     98      118     112     125     768
Cost (k)    65      67      69      63      70      65      71      470

(Note: the Sales values were transformed into units of thousands, and we added a Total column.)

... this is the code that does it:

var srcStore = new Ext.data.ArrayStore({
	fields:
	[
		{ name: "Date", type: "date" },
		{ name: "Sales", type: "float" },
		{ name: "Cost", type: "float" }
	]
});

var destStore = new Ext.data.ArrayStore({
	fields:
	[
		{ name: "Metric" },
		{ name: "Day1", type: "float" },
		{ name: "Day2", type: "float" },
		{ name: "Day3", type: "float" },
		{ name: "Day4", type: "float" },
		{ name: "Day5", type: "float" },
		{ name: "Day6", type: "float" },
		{ name: "Day7", type: "float" },
		{ name: "Total", type: "float" }
	]
});

var startDate = new Date("1/1/2010 0:00:00");

RP.util.DataStore.transpose(
{
	source:
	{
		store: srcStore,
		pivotColumn: "Date",
		pivotMap: function(value)
		{
		  var span = new TimeSpan(value - startDate);
		  return("Day" + (span.totalDays() + 1).toString());        
		},
		transforms:
		[
		  {	// divides by 1,000
		    target: "Sales",
		    fn: function(recData) { return recData.Sales / 1000; }			        
		  },
		  {	// divides by 1,000
		    target: "Cost",
		    fn: function(recData) { return recData.Cost / 1000; }			        
		  }
		]
	},
	dest:
	{
		store: destStore,
		pivotColumn: "Metric",
		pivots: 
		[
		  { s: "Sales",		d: "Sales (k)"   renderer: blahblahblah, metaData: {} },  // optional renderer and metadata can be specified
		  { s: "Cost",      d: "Cost (k)" }
		],
		transforms:
		[
		  {	// sums the specified columns
		    target: "Total",
		    fn: "sum",  // or function(recData, transform, metaData) {}
		    columns: ["Day1", "Day2", "Day3", "Day4", "Day5", "Day6", "Day7" ]
		  }
		]
	}          
});
 </code></pre>
 </p>
 * @singleton
 */
RP.util.DataStore = (function() {
	var prebuiltTransforms = {};
	
	prebuiltTransforms.sum = function(r, t) {
		var sum = 0;
		Ext.each(t.columns, function(c) {
			sum += r[c];
		}, this);              
		return sum;
	};
	
	prebuiltTransforms.average = function(r, t) {
		var sum = 0;
		var cnt = 0;
		
		Ext.each(t.columns, function(c) {
			sum += r[c];
			cnt++;
		}, this);
		
		if (cnt === 0) {
			return NaN;
		}
		return(sum / cnt);
	};
	
	var createTransforms = function(tDefs) {
		var tArr = [];
		
		Ext.each(tDefs, function(t) {
			if (Ext.isString(t.fn)) {
				t.fn = prebuiltTransforms[t.fn];
			}
			          
			tArr.push(function(r) {
				r[t.target] = t.fn(r, t, r._metaData);
			});
		});
		return tArr;
  };
  
    return {
		/**
		 * Joins one or more stores into an existing store.
		 * @method join
		 * @param {Array} srcStoreCfgs Array of stores to join 
		 * @param {Object} destStoreCfg Destination store
		 *		 
		 */
		join: function(srcStoreCfgs, destStoreCfg) {
			var store;
			var recs = new Ext.util.MixedCollection();
      var jrec, rec;
      
      var mapFn = function(r) {
        rec = r;
        var pk = rec.get(srcStoreCfgs[i].pk);
        var jidx = recs.indexOfKey(pk.toString());
        
        
        if (jidx >= 0) {
          jrec = recs.itemAt(jidx);
        }
        else {
          // PK not found - add to joined store
          var data = {};
          
          data[destStoreCfg.pk] = pk;
          jrec = new destStoreCfg.store.recordType(data);
          recs.add(pk.toString(), jrec);
        }
        
        // Copy mapped fields.
        Ext.each(srcStoreCfgs[i].map, copyFn, this);
      };
      
      var copyFn = function(m) {
        jrec.set(m.dest, rec.get(m.src));
      };
			
			for (var i = 0; i < srcStoreCfgs.length; i++) {
				store = srcStoreCfgs[i].store;
				
				// Loop thru each record in current store and:
				// - add if its PK isn't found in joined store
				// - set mapped values if it is found in joined store
				store.each(mapFn, this);
			}
			
			destStoreCfg.store.add(recs.getRange());
		},
		
		/**
		 * Transposes a store into another existing store.
		 * @method transpose
		 * @param {Object} cfg {Object source, Object dest, [Array transforms]}
		 */
		transpose: function(cfg) {		
			var transforms;
		  
			// Perform source store transformations.
			if (Ext.isArray(cfg.source.transforms)) {
				transforms = createTransforms(cfg.source.transforms);        
				cfg.source.store.each(function(rec) {
					Ext.each(transforms, function(tFn) {
						tFn(rec.data);
					}, this);
				}, this);      
			}
			
			// Create objects for destination rows.  Only the destination pivot column is
			// set at this point...
			var recObjs = new Ext.util.MixedCollection();
			Ext.each(cfg.dest.pivots, function(p) {
				var rec = {};        
				rec[cfg.dest.pivotColumn] = p.d;
				if (p.renderer) {
					rec.renderer = p.renderer;
				}
				if (p.metaData) {
				  rec._metaData = p.metaData;
				}
				recObjs.add(p.s, rec);
			}, this);
			
			// Populate the non-pivot values for objects in destination rows.
			cfg.source.store.each(function(d) {
				var destCol = cfg.source.pivotMap(d.data[cfg.source.pivotColumn]);
				for (var srcColName in d.data) {
					if (srcColName !== cfg.source.pivotColumn) {
						var rowObj = recObjs.key(srcColName);
						if (rowObj) {
						  rowObj[destCol] = d.data[srcColName];
						}
					}
				}
			}, this);
			
			// Perform dest store transformations.
			transforms = undefined;
			if (Ext.isArray(cfg.dest.transforms)) {
				transforms = createTransforms(cfg.dest.transforms); 
			}
			
			// Turn rec objects into data records.
			var recs = [];
			recObjs.each(function(r) {
				if (transforms) {
					Ext.each(transforms, function(tFn) {
						tFn(r);
					}, this);
				}
				
				recs.push(new cfg.dest.store.recordType(r));
			}, this);
			
			cfg.dest.store.add(recs);
		},
		
		/**
		 * Get a prebuilt transform function.
		 * @method getTransformFunction
		 * @param {String} name One of the following: "sum", "average"
		 */
		getTransformFunction : function(name) {
			return prebuiltTransforms[name];
		}	
	};
})();
//////////////////////
// ..\js\util\PageInactivityChecker.js
//////////////////////
Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class PageInactivityChecker
 * @singleton
 *
 * A singleton to check for web page inactivity and calls handler(s) when an inactivity timeout occurs.
 */
RP.util.PageInactivityChecker = (function() {
    var handlers = []; //functions called when inactive triggered
    var pollerID;
    var lastActivityTime; //last time activity detected in browser
    var timeOutSecs; //seconds until user is considered inactive
    var lastSessionKeepAlive; //timer used to keep track of the last time sessionKeepAlive was called
    var sessionKeepAliveIntervalMillis = 60000; //sessionKeepAlive polling interval
    var checkingUserActivity = false; // whether or not there is an Ajax request currently checking to see if the user is active
    var inactive = false;
    
    Ext.onReady(function(){
        // When the window becomes active again we set the inactive flag back to false.
        RP.event.AppEventManager.register(RP.upb.AppEvents.ActiveAgain, function() {
            inactive = false;
        }); 
        
        RP.event.AppEventManager.register(RP.upb.AppEvents.Inactive, function(){
            inactive = true;
        }); 
    });
    
    var resetLastActivityTime = function() {
        lastActivityTime = new Date();
    };
    
    var resetLastSessionKeepAlive = function() {
        lastSessionKeepAlive = new Date();
    };
    
    var activityEventNames = ['keydown', 'mouseover', 'mousemove'];
    
    var setHooks = function(timeOutInSecs) {
        timeOutSecs = timeOutInSecs;
        lastActivityTime = new Date();
        lastSessionKeepAlive = new Date();
        
        Ext.each(activityEventNames, function(evtName) {
            Ext.EventManager.on(document, evtName, resetLastActivityTime);
        });
        
        pollerID = window.setInterval(pollerFn, 1000);
        logger.logInfo("[PageInactivityChecker] Inactivity checker started with timeout of " + timeOutSecs + " seconds.");
    };
    
    var releaseHooks = function() {
        if (pollerID) {
            Ext.each(activityEventNames, function(evtName) {
                Ext.EventManager.un(document, evtName, resetLastActivityTime);
            });
            
            window.clearInterval(pollerID);
            pollerID = undefined;
            logger.logInfo("[PageInactivityChecker] Inactivity checker stopped.");
        }
    };
    
    var triggerInactivityDialog = function() {
        checkingUserActivity = false;
        logger.logInfo("[PageInactivityChecker] Inactivity timeout occurred.");
        releaseHooks();
        
        Ext.each(handlers, function(h) {
            h();
        });
    };
    
    var handleInactivityResponse = function(response, options) {
        checkingUserActivity = false;
        var result;
        try {
            result = Ext.util.JSON.decode(response.responseText);
        } 
        catch (ex) {
            //JSON error which means it is probably the login page
            releaseHooks();
            return;
        }
        if (result && Ext.isNumber(result.data)) {
            var serverLastBrowserActivity = parseInt(result.data, 10);
            var serverLastBrowserActivityElapsed = (new Date()) - serverLastBrowserActivity;
            
            if (serverLastBrowserActivityElapsed >= (timeOutSecs * 1000)) {
                triggerInactivityDialog();
                return;
            }
            
            if (lastActivityTime < serverLastBrowserActivity) {
                lastActivityTime = serverLastBrowserActivity;
            }
        }
        
        resetLastSessionKeepAlive();
    };
    
    var pollerFn = function() {
        var lastActivityElapsed = (new Date()) - lastActivityTime;
        var lastSessionKeepAliveElapsed = (new Date()) - lastSessionKeepAlive;
        if (lastActivityElapsed >= (timeOutSecs * 1000) && checkingUserActivity === false) {
            RP.util.Helpers.isUserActive(handleInactivityResponse, triggerInactivityDialog, this);
            checkingUserActivity = true;
        }
        else 
            if (lastSessionKeepAliveElapsed >= sessionKeepAliveIntervalMillis && lastActivityElapsed <= sessionKeepAliveIntervalMillis) {
                resetLastSessionKeepAlive();
                
                // execute a request against the user's session to inform the 
                // server side session that the user is still active.
                RP.util.Helpers.keepSessionAlive(lastActivityTime.getTime());
            }
    };
    
    return {
        /**
         * @private
         *
         * This method will set the interval millis for the session keep alive.
         * @param {Object} intervalMillis The interval millis.
         */
        _setSessionKeepAliveIntervalMillis: function(intervalMillis) {
            sessionKeepAliveIntervalMillis = intervalMillis;
        },
        
        /**
         * Returns true if the page is inactive.
         * @return {Boolean} True if inactive
         */
        isInactive: function() {
            return inactive;
        },
        
        /**
         * Add a new handler to invoke when the inactivity timeout occurs.
         * @param {Function} h
         */
        addHandler: function(h) {
            handlers.push(h);
        },
        
        /**
         * Retrieve the number of seconds of the interval to time out
         * the client side user session.
         */
        getTimeOutInSeconds: function() {
            return timeOutSecs;
        },
        
        /**
         * Removes a previously added handler for inactivity timeout.
         * @param {Function} h
         */
        removeHandler: function(h) {
            handlers.remove(h);
        },
        
        /**
         * Start checking for page inactivity.
         * @param {Number} timeOutInSecs
         */
        start: function(timeOutInSecs) {
            if (pollerID) {
                logger.logInfo("[PageInactivityChecker] Checker already started.");
                return false;
            }
            
            setHooks(timeOutInSecs);
            return true;
        },
        
        /**
         * Stop checking for page inactivity.
         */
        stop: function() {
            releaseHooks();
        }
    };
})();
//////////////////////
// ..\js\layout\CenteredFormLayout.js
//////////////////////
Ext.ns("RP.layout");

/**
 * @class RP.layout.CenteredFormLayout
 * @extends Ext.layout.FormLayout
 * 
 * A FormLayout extension that uses auto width labels and center
 * aligns each x-form-item horizontally.  Labels are bold by default.
 */
RP.layout.CenteredFormLayout = Ext.extend(Ext.layout.FormLayout, {
    /**
     * Override of the default fieldTpl from the container layout.  This template
     * renders each form item in a 2 column, single row table that is horizontally centered.
     * @type Ext.Template
     * @property
     */
    fieldTpl: (function() {
        var t = new Ext.Template(
            '<div class="x-form-item {itemCls}" tabIndex="-1"><table style="margin: 0 auto;"><tr>',
                '<td><label for="{id}" style="{labelStyle}" class="x-form-item-label">{label}{labelSeparator}</label></td>',
                '<td><div class="x-form-element" id="x-form-el-{id}" style="{elementStyle}"></td>',
            '</tr></table></div>'
        );
        t.disableFormats = true;
        return t.compile();
    })(),
    
    /**
     * Override to ensure that the field labels are auto width and have no left padding.
     * @param {Ext.Container} ct The container the layout is being applied to.
     * @private
     */
    setContainer : function(ct){
        RP.layout.CenteredFormLayout.superclass.setContainer.call(this, ct);

        if(!ct.hideLabels){
            Ext.apply(this, {
                labelStyle: 'width:auto;',
                labelAdjust: 0,
                elementStyle: 'padding-left:0;'
            });
        }
    }
});

// set the shortcut so the layout can be lazy instantiated from a string
Ext.Container.LAYOUTS.centeredform = RP.layout.CenteredFormLayout;
//////////////////////
// ..\js\core\ClassOperations.js
//////////////////////
Ext.ns("RP.core");

/**
 * @class RP.core.ClassOperations
 * <p>Provides utilities to support the abstract class pattern in JavaScript.</p>
 * @singleton
 */
RP.core.ClassOperations = function() {
  var errorFormatStr = "The type {0} must implement the inherited abstract method {1}()";
  
  return {
    /**
     * Used to define an abstract method inside an abstract class.  This
     * is similar to Ext.emptyFn, in that it defines a simple place holder
     * function, but this will throw an exception if called directly.
     * @shortcut RP.abstractFn
     */
    abstractFn: function() {
      throw new RP.Exception("RP.abstractFn() called directly.");
    },
    
    /**
     * Custom extension of Ext.extend that also checks to make sure the
     * derived class has correctly implemented all abstract methods; will
     * throw an exception for any instances of RP.abstractFn that are not overridden.
     * @shortcut RP.extend
     * @param {Object} baseClass
     * @param {Object} overrides
     * @return {Function} derivedClass
     */
    extend: function(baseClass, overrides) {
      var derivedClass = Ext.extend(baseClass, overrides);
      var derivedPrototype = derivedClass.prototype;
      
      // check for any missed implementations of RP.abstractFn
      for (var field in derivedPrototype) {
        if (derivedPrototype[field] === RP.abstractFn) {
          var name = derivedPrototype._name || "[Unknown Class]";
          var msg = String.format(errorFormatStr, name, field);
          
          logger.logError(msg);
          throw new RP.Exception(msg);
        }
      }
      
      return derivedClass;
    }
  };
}();

// shortcuts
RP.abstractFn = RP.core.ClassOperations.abstractFn;
RP.extend = RP.core.ClassOperations.extend;
//////////////////////
// ..\js\core\CodeTranslator.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class CodeTranslator
 * @singleton
 * @extends none
 *
 * Singleton for loading/accessing code translations
 */
RP.core.CodeTranslator = function(){

  // default reader used by global stores created
  var reader = new Ext.data.JsonReader({
    root: ''
  }, [{
    name: 'colnam'
  }, {
    name: 'colval'
  }, {
    name: 'lngdsc'
  }, {
    name: 'short_dsc'
  }]);
  
  return {
    /**
     * Performs a lookup on the specified global store and returns the
     * matching row's field, otherwise value
     * @param {String} namespace
     * @param {String} colnam
     * @param {String} value
     * @param {String} field
     */
    getRowValue: function(namespace, colnam, value, field){
      var store = Ext.StoreMgr.get(namespace + "." + colnam);
      var index = store.findExact('colval', value);
      
      if (index > -1) {
        value = store.getAt(index).get(field);
      }
      
      return value;
    },
    
    /**
     * Loads an array of objects into global stores, also creating
     * the stores if necessary
     * @param {String} namespace
     * @param {Array of Object} data
     */
    loadData: function(namespace, data){
      Ext.each(data, function(item){
        var storeId = namespace + "." + item.colnam;
        var store = Ext.StoreMgr.get(storeId);
        
        // check if the global store exists
        if (typeof store === "undefined") {
          store = new Ext.data.Store({
            storeId: storeId,
            reader: reader
          });
        }
        
        store.loadData([item], true);
      });
    },
    
    // TODO use ScriptLoader
    loadCodePack: function(url) {
      document.write('<script type="text/javascript" src="' + url + '"></script>');
    },
    
    // shortcuts
    getLongDesc: function(namespace, colnam, value) {
      return this.getRowValue(namespace, colnam, value, "lngdsc");
    },
    getShortDesc: function(namespace, colnam, value) {
      return this.getRowValue(namespace, colnam, value, "short_desc");
    },
    getStore: function(namespace, colnam) {
      return Ext.StoreMgr.get(namespace + "." + colnam);
    }
  };
}();
//////////////////////
// ..\js\core\Sundial.js
//////////////////////
/*global Ext, RP, window, redprairie */

RP.core.Sundial = {
  _offset: 0,
  
  now: function() {
    return new Date(new Date().getTime() + this._offset);
  },
  
  setServerTime: function(offsetDate) {
    this._offset = offsetDate.getTime() - (new Date()).getTime();
  },
  
  hasOffset: function() {
    return (this._offset !== 0);
  }
};

// Shorthand.
// DEPRECATED
var Sundial = {};
RP.util.DeprecationUtils.renameClass("Sundial", "RP.core.Sundial");
//////////////////////
// ..\js\core\Interface.js
//////////////////////
Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Interface
 * @singleton
 * @extends none
 *
 * Javascript doesn't have interfaces like OO languages, so this is one way of implementing
 * such a feature.  The inspiration for this came from <a href="http://knol.google.com/k/glen-ford/programming-to-the-interface-in/27lm3zg1hrg7v/7#">here</a>.
 */
RP.core.Interface = (function() {
  var checkInterface = function(obj, theInterface, map) {
    var omember;
    
    map = map || {};
    for (var member in theInterface) {
      if (member !== "_name") // don't check interface name
      {
        omember = map[member] || member;
        if (typeof obj[omember] !== typeof theInterface[member]) // verify type of member
        {
          throw new RP.Exception("Member '" + member + "' not implemented or implemented incorrectly in interface '" + theInterface._name + "'");
        }
      }
    }
    return true;
  };
  
  var extractInterface = function(obj, iobj) {
    var i = {};
    var theInterface = iobj.i;
    var map = iobj.map;
    var omember;
    
    for (var member in theInterface) {
      if (member !== "_name") {
        omember = map[member] || member;
        if (typeof obj[omember] === "function") {
          i[member] = obj[omember].createDelegate(obj);
        }
        else {
          i[member] = obj[omember];
        }
      }
    }
    return i;
  };
  
  return {
    /**
     * Registers an object as implementing a certain interface.  <br/><b>Shorthand</b>: RP.iimplement()
     * @method
     * @param {Object} obj The object
     * @param {Object} theInterface The interface it implements
     * @param {Object} map Optional map for interface member name to actual object member name
     *     (used for multiple interfaces member name collisions)
     * @return void RP.Exception is thrown if object does not fully implement the interface
     */
    implement: function(classRef, theInterface, map) {
      if (!theInterface._name) {
        throw new RP.Exception("Missing _name member in interface definition.");
      }
      
      if ((typeof classRef.prototype.__rpInterfaces !== "undefined") &&
          (typeof classRef.prototype.__rpInterfaces[theInterface._name] !== "undefined")) {
        throw new RP.Exception(theInterface._name + " has already been implemented for class " + classRef.toString());
      }
      
      if (!checkInterface(classRef.prototype, theInterface, map)) {
        throw new RP.Exception("Interface not implemented.");
      }
      
      if (!classRef.prototype.__rpInterfaces) {
        classRef.prototype.__rpInterfaces = {};
      }
      
      classRef.prototype.__rpInterfaces[theInterface._name] = {
        i: theInterface,
        map: Ext.apply({}, map)
      };
    },
    
    /**
     * Gets a certain interface that an object implements.  <br/><b>Shorthand</b>: RP.iget()
     * @method
     * @param {Object} obj The object
     * @param {Object} theInterface The interface definition object of the
     *         interface you are expecting
     * @return {Object} interface with scoping set to the object, or null
     *         if the ojbect does not implement said interface
     */
    get: function(obj, theInterface) {
      if (!obj || !obj.__rpInterfaces) {
        return undefined;
      }
      
      var i = obj.__rpInterfaces[theInterface._name];
      
      if (!i) {
        return undefined;
      }
      
      // Copy interface members.
      // The reason this is done every time (instead of once inside implement())
      // is because interfaces that contain value type properties won't be up-to-date.
      return extractInterface(obj, i);
    }
  };
})();

// Shorthands.
RP.iimplement = RP.core.Interface.implement;
RP.iget = RP.core.Interface.get;

//////////////////////
// ..\js\core\IntervalJN.js
//////////////////////
Ext.ns("RP.core");

RP.core.IntervalJN = {
    /*  To improve performance, we'll assume the caller is passing in valid intervals...  We'll
     let our unit tests track down bugs...
     _isValidInterval: function(arg, errorMsg) {
     if (!this._isStartValidObject(arg)) {
     errorMsg += "Object not an interval - ";
     throw new RP.Exception(String.format(errorMsg + "Start object not a Date object"));
     }
     if (!this._isEndValidObject(arg)) {
     errorMsg += "Object not an interval - ";
     throw new RP.Exception(String.format(errorMsg + "End object not a Date object"));
     }
     return true;
     },
     _isStartValidObject: function(arg) {
     return (this._isDateObject(arg.Start));
     },
     _isEndValidObject: function(arg) {
     return (this._isDateObject(arg.End));
     },
     _isNullOrUndefined: function(arg) {
     return (typeof arg === "undefined") ||
     (arg === null);
     },
     _isDateObject: function(arg, throwErrorMsg) {
     if (this._isNullOrUndefined(arg)) {
     throw new RP.Exception(String.format("{0} is Null or undefined", arg));
     }
     if (arg instanceof Date) {
     return true;
     }
     if (this._isNullOrUndefined(throwErrorMsg)) {
     return false;
     }
     else {
     throw new RP.Exception(String.format("{0} {1} not a Date object", throwErrorMsg, arg));
     }
     },
     */
    getDuration: function(intervalJN) {
        return intervalJN.End.deltaT(intervalJN.Start);
    },
    
    getCenter: function(intervalJN) {
        return intervalJN.Start.clone().addMilliseconds((intervalJN.End.deltaT(intervalJN.Start).totalMilliseconds() / 2));
    },
    
    containsTime: function(intervalJN, timePoint) {
        //this._isValidInterval(intervalJN, "Invalid 1st Argument: ");
        //this._isDateObject(timePoint, "Invalid 2nd Argument: ");
        
        return (timePoint.getTime() >= intervalJN.Start.getTime()) &&
        (timePoint.getTime() <= intervalJN.End.getTime());
    },
    
    containsInterval: function(outerIntervalJN, innerIntervalJN2) {
        return (RP.core.IntervalJN.containsTime(outerIntervalJN, innerIntervalJN2.Start)) &&
        (RP.core.IntervalJN.containsTime(outerIntervalJN, innerIntervalJN2.End));
    },
    
    isOverlapping: function(intervalJN1, intervalJN2) {
        return (intervalJN1.Start.isBefore(intervalJN2.End)) && (intervalJN1.End.isAfter(intervalJN2.Start));
    },
    
    isCoincident: function(intervalJN1, intervalJN2) {
        return (intervalJN1.Start.equals(intervalJN2.Start) && intervalJN1.End.equals(intervalJN2.End));
    },
    
    isContinuous: function(intervalJN1, intervalJN2) {
        return (intervalJN1.Start.equals(intervalJN2.End) || intervalJN1.End.equals(intervalJN2.Start));
    },
    
    isPoint: function(interval) {
        return interval.Start.equals(interval.End);
    },
    
    shiftByMinutes: function(intervalJN, minutesStart, minutesEnd) {
        intervalJN.Start.addMinutes(minutesStart);
        
        if ((typeof minutesEnd === "undefined") || (minutesEnd === null)) {
            intervalJN.End.addMinutes(minutesStart);
        }
        else {
            intervalJN.End.addMinutes(minutesEnd);
        }
    },
    
    canScaleByMinutes: function(intervalJN, durationDiffMinutes) {
        var origDuration = RP.core.IntervalJN.getDuration(intervalJN).totalMinutes();
        
        return (origDuration + durationDiffMinutes > 0);
    },
    
    scaleByMinutes: function(intervalJN, durationDiffMinutes) {
        if (!RP.core.IntervalJN.canScaleByMinutes(intervalJN, durationDiffMinutes)) {
            throw new RP.Exception("Invalid duration");
        }
        
        RP.core.IntervalJN.shiftByMinutes(intervalJN, 0, durationDiffMinutes);
    },
    
    cloneInterval: function(intervalJN) {
        return {
            Start: intervalJN.Start.clone(),
            End: intervalJN.End.clone()
        };
    },
    
    formatInterval: function(interval, formatObj) {
        if (interval === null || interval.Start === null || interval.End === null) {
            return "";
        }
        
        if (!formatObj) {
            formatObj = RP.core.Formats.Time.Default;
        }
        
        return String.format("{0} - {1}", interval.Start.formatDate(formatObj), interval.End.formatDate(formatObj));
    },
    
    parseTimeAfterBase: function(strTime, baseDate) {
        var amPmSpec = this._parseAmPmSpec(strTime);
        var retDate = RP.core.IntervalJN.parseTime(strTime, baseDate, true);
        
        var expectedHours = retDate.getHours();
        
        while (baseDate.getTime() > retDate.getTime() ||
        !this._validateAmPmSpec(amPmSpec, retDate)) {
            var hoursToAdd = 12;
            if ( RP.Formatting.Dates.is12HLocale() === false ) {
                hoursToAdd = 24;
            }
            retDate.setHours(retDate.getHours() + hoursToAdd);
        }
        
        this.adjustForDST(retDate, expectedHours);
        
        return retDate;
    },
    
    parseTime: function(strTime, datePart, accept2400) {
        var nextDay = false; // detect 24:00 as next day
        // Do not allow minutes if 24:xx
        var ind = strTime.indexOf(":");
        
        var hhmm = [];
        
        if (ind !== -1) {
            hhmm = strTime.split(":");
            if (hhmm.length > 1) {
                if (hhmm[0] === "24" && (parseInt(hhmm[1], 10) !== 0)) {
                    throw new RP.Exception("Invalid time text");
                }
            }
            
            // Do not allow anything over hour 24
            if (parseInt(hhmm[0], 10) > 24) {
                throw new RP.Exception("Invalid time text");
            }
            
            // Convert 24:00 to 0:00 and set it to next calendar day
            if (hhmm[0] === "24") {
                if (accept2400 === true) {
                    nextDay = true;
                    strTime = "0:00";
                }
                else {
                    throw new RP.Exception("24:00 cannot be used as the time");
                }
            }
        }
        
        var retDate = RP.core.Format.parseTime(strTime);
        if (datePart instanceof Date) {
            var newDate = datePart.clone();
            newDate.setHours(retDate.getHours(), retDate.getMinutes(), retDate.getSeconds());
            newDate.apExists = retDate.apExists;
            retDate = newDate;
        }
        
        if (nextDay) {
            retDate.addDays(1);
        }
        return retDate;
    },
    
    _parseAmPmSpec: function(timeStr) {
        var strTime = timeStr.toLowerCase();
        var meridiem = RP.Formatting.Dates.getUniqueLettersOfMeridiems();
        if(meridiem) {
            if (strTime.indexOf(meridiem.am) !== -1) {
                return meridiem.am;
            }
            else if (strTime.indexOf(meridiem.pm) !== -1) {
                return meridiem.pm;
            }
        }
        return null;
    },
    
    _validateAmPmSpec: function(amPmSpec, time) {
        var meridiem = RP.Formatting.Dates.getUniqueLettersOfMeridiems();
        if(meridiem) {
            if ((amPmSpec === meridiem.am) && time.getHours() >= 12) {
                return false;
            }
            if ((amPmSpec === meridiem.pm) && time.getHours() < 12) {
                return false;
            }
        }
        return true;
    },

    parseTimeInterval: function(strInterval, datePart) {
        var intervalText = String(strInterval);
        var times = intervalText.split("-");
        
        if (times.length !== 2 ||
        times[0].trim().length === 0 ||
        times[1].trim().length === 0) {
            throw new RP.Exception("Invalid interval text specified");
        }
        
        var endAmPmSpec = this._parseAmPmSpec(times[1]);
        
        var retInterval = {
            Start: RP.core.IntervalJN.parseTime(times[0], datePart),
            End: RP.core.IntervalJN.parseTime(times[1], datePart, true)
        };
        
        var expectedHours = RP.core.IntervalJN.parseTime(times[1], datePart, true).getHours();
        
        if ((retInterval.Start !== null) && (retInterval.End !== null)) {
            while (retInterval.Start.getTime() >= retInterval.End.getTime() ||
            !this._validateAmPmSpec(endAmPmSpec, retInterval.End)) {
                retInterval.End.addHours(12);
            }
            
            this.adjustForDST(retInterval.End, expectedHours);
        }
        else {
            throw new RP.Exception("Invalid interval text specified");
        }
        
        return retInterval;
    },
    
    parseBusinessDayTimeInterval: function(strInterval, dateTimeStart) {
        var retInterval = RP.core.IntervalJN.parseTimeInterval(strInterval, dateTimeStart);
        
        if (retInterval.Start < dateTimeStart && dateTimeStart.getHours() === 0) {
            retInterval.Start.addDays(1);
            retInterval.End.addDays(1);
        }
        return retInterval;
    },
    
    adjustInterval: function(interval, offsetMinutes, offsetDuration) {
        if (RP.core.IntervalJN.isValidOffset(offsetMinutes)) {
            RP.core.IntervalJN.shiftByMinutes(interval, offsetMinutes);
        }
        if (RP.core.IntervalJN.isValidOffset(offsetDuration)) {
            RP.core.IntervalJN.scaleByMinutes(interval, offsetDuration);
        }
    },
    
    isValidOffset: function(number) {
        return ((typeof number !== "undefined") &&
        (number !== null) &&
        !isNaN(parseInt(number, 10)));
    },
    
    compareDate: function(dt1, dt2) {
        return dt1.getTime() - dt2.getTime();
    },
    
    compareStart: function(interval1, interval2) {
        return (interval1.Start.getTime() - interval2.Start.getTime());
    },
    
    compareEnd: function(interval1, interval2) {
        return (interval1.End.getTime() - interval2.End.getTime());
    },
    
    compareCenter: function(interval1, interval2) {
        return (RP.core.IntervalJN.getCenter(interval1) - RP.core.IntervalJN.getCenter(interval2));
    },
    
    compareDuration: function(interval1, interval2) {
        return (RP.core.IntervalJN.getDuration(interval1).getTotalMilliseconds() - RP.core.IntervalJN.getDuration(interval2).getTotalMilliseconds());
    },
    
    setParent: function(interval, parentSequence) {
        interval.__parent = parentSequence;
    },
    
    getParent: function(interval) {
        return interval.__parent;
    },
    
    setRule: function(interval, iRule) {
        interval.__rule = iRule;
    },
    
    getRule: function(interval) {
        return interval.__rule;
    },
    
    adjustForDST: function(endDateTime, expectedHours) {
        //Normalized for 24-hour issues
        var endHours = endDateTime.getHours() >= 12 ? endDateTime.getHours() - 12 : endDateTime.getHours();
        expectedHours = expectedHours >= 12 ? expectedHours - 12 : expectedHours;
        
        // Compensate for DST
        if (endHours !== expectedHours) {
            if (endHours < expectedHours) {
                endDateTime.addHours(1);
            }
            else {
                endDateTime.addHours(-1);
            }
        }
    }
};

// Shorthand.
// DEPRECATED
var IntervalJN = {};
RP.util.DeprecationUtils.renameClass("IntervalJN", "RP.core.IntervalJN");
//////////////////////
// ..\js\core\SequenceJN.js
//////////////////////
Ext.ns("RP.core");

RP.core.SequenceJN = {
    addInterval : function(sequence, interval, iRule) {
        // return sequence contains the chop up of the interval that was applied
        // to the sequence
        // this includes all the splits that were done but no merges with
        // existing sequence data
        var retSeq = []; // TOOD: do we really need to return retSeq out of
        // this method?

        // trap illegal add to a bounded sequence
        var isDisplacmentAdd = false;

        if (sequence.Start && sequence.End) {
            isDisplacmentAdd = !RP.core.IntervalJN.containsInterval(sequence,
            interval);
        }

        if (sequence.__isBounded && isDisplacmentAdd) {
            throw new RP.Exception("interval is out of bounds for the bounded sequence");
        }

        // negotiate parent displacement
        if (RP.core.IntervalJN.getParent(sequence) && isDisplacmentAdd) {
            RP.core.SequenceJN.removeInterval(RP.core.IntervalJN.getParent(sequence),
            sequence);
        }

        // brand the sequence so we can tell it form other things
        sequence.__isSequence = true;

        // brand the interval so we know it's parent and rule
        RP.core.IntervalJN.setParent(interval, sequence);
        RP.core.IntervalJN.setRule(interval, iRule);

        // find where to begin
        var startIndex = RP.core.SequenceJN.getStartIndex(sequence, interval);

        // starting at the begining index, we are going to walk forward
        // splititng or merging as we go
        var splitPair;

        for (var index = startIndex; index <= sequence.length; index++) {
            splitPair = null;

            // the loop above allows us to come in one more time than there are
            // entries in the
            // sequence if we are there then nothing in interval can possibly
            // overlap
            // just insert him and we're done

            // direct insert
            if (index === sequence.length) {
                RP.core.SequenceJN._insertIntoSequence(sequence, interval, index);
                // TOOD: do we really need to return retSeq out of this method?
                retSeq.push(interval);
                break;
            }

            var curInterval = sequence[index];
            var curRule = RP.core.IntervalJN.getRule(curInterval);
            var nextInterval = this.getNextIntervalInSequence(sequence, index);
            var prevInterval = this.getPrevIntervalInSequence(sequence, index);

            var istart = interval.Start.getTime();

            if (istart >= curInterval.End.getTime()) {
                continue;
            }

            // TODO: if (curInterval.canMerge(interval))

            // if the new begins before the current...
            var cstart = curInterval.Start.getTime();
            if (istart < cstart) {
                // ...and if no overlap...
                if (interval.End.getTime() <= cstart) {
                    // direct insert
                    RP.core.SequenceJN._insertIntoSequence(sequence, interval, index);
                    retSeq.splice(index, 0, interval);
                    break;
                }
                else if (iRule.canExtend && iRule.canExtend(null, interval, curInterval)) {
                    interval = iRule.extend(null, interval, curInterval);

                    // direct insert
                    RP.core.SequenceJN._insertIntoSequence(sequence, interval, index);
                    retSeq.splice(index, 0, interval);
                    break;
                }
                else if (iRule.canExtend && iRule.canExtend(prevInterval, interval, null)) {
                    interval = iRule.extend(prevInterval, interval, null);
                    index--;
                }
                else if (curRule.canExtend && curRule.canExtend(interval, curInterval, null)) {
                    curInterval = curRule.extend(interval, curInterval, null);

                    sequence[index] = curInterval;
                    retSeq.splice(index, 0, curInterval);
                    index--;
                }
                else if (RP.core.SequenceJN._isUnboundedSequence(interval)) {
                    splitPair = this._splitSetParentAndRule(curRule,
                    curInterval, interval.End, sequence);

                    interval = iRule.merge(interval, splitPair.first);

                    sequence.splice(index, 1);
                    RP.core.SequenceJN._insertIntoSequence(sequence, interval, index);
                    sequence.splice(index + 1, 0, splitPair.last);

                    break;
                }
                else {
                    // there is overlap so split the new interval
                    // add the no overlapping first part to the set
                    // save the last part as the newInterval going forward

                    splitPair = this._splitSetParentAndRule(iRule, interval,
                    curInterval.Start, sequence);

                    // direct insert
                    RP.core.SequenceJN._insertIntoSequence(sequence, splitPair.first,
                    index);
                    retSeq.splice(index, 0, splitPair.first);

                    interval = splitPair.last;
                }
            }
            // so new doesn't start before cur, maybe they start at the same
            // time
            // or current is a sequence. We shunt Sequences here because they do
            // not
            // need to be split ( as will happen in the next section )
            else if (RP.core.IntervalJN.compareStart(interval, curInterval) === 0 || curInterval.__isSequence) // keep as generic sequence
            // test
            {
                var compareEnd = RP.core.IntervalJN.compareEnd(interval, curInterval);

                // if it's a point just let it insert
                if (RP.core.IntervalJN.compareDate(interval.Start, interval.End) === 0) {
                    // direct insert
                    RP.core.SequenceJN._insertIntoSequence(sequence, interval, index);
                    retSeq.splice(index, 0, interval);
                    break;
                }
                // if it is coincident with current then just merge and go o
                else if (compareEnd === 0) {
                    curInterval = curRule.merge(curInterval, interval);

                    sequence[index] = curInterval;
                    retSeq.splice(index, 0, interval);
                    break;

                }
                else if (iRule.canExtend && iRule.canExtend(curInterval, interval, null)) {
                    interval = iRule.extend(curInterval, interval, null);
                }
                else if (iRule.canExtend && iRule.canExtend(null, interval, nextInterval)) {
                    interval = iRule.extend(null, interval, nextInterval);
                    index--;
                }
                else if (curRule.canExtend && curRule.canExtend(interval, curInterval, null)) {
                    curInterval = curRule.extend(interval, curInterval, null);

                    sequence[index] = curInterval;
                    retSeq.splice(index, 0, curInterval);
                    index--;
                }
                else if (curRule.canExtend && curRule.canExtend(null, curInterval, interval)) {
                    curInterval = curRule.extend(null, curInterval, interval);

                    sequence[index] = curInterval;
                    retSeq.splice(index, 0, curInterval);
                }
                // since they don't overlapp the question now is which to split
                else if (compareEnd < 0 && !curInterval.__isSequence) {
                    // new is shorter than current so split the current
                    // and merge new with first part before putitng both back in
                    // queue
                    // and we're done
                    splitPair = curRule.split(curInterval, interval.End);

                    retSeq.splice(index, 0, splitPair.first);

                    splitPair.first = curRule.merge(splitPair.first, interval);

                    splitPair = this._setParentAndRule(splitPair, sequence,
                    curRule);

                    sequence[index] = splitPair.first;
                    sequence.splice(index + 1, 0, splitPair.last);
                    retSeq.splice(index + 1, 0, splitPair.last);

                    splitPair = undefined;
                    break;
                }
                else if (compareEnd < 0 && curInterval.__isSequence) {
                    var r = curRule.merge(curInterval, interval);
                    retSeq.splice(index, 0, r);

                    break;

                }
                else if (compareEnd > 0 && RP.core.SequenceJN._isUnboundedSequence(interval)) {
                    interval = iRule.merge(interval, curInterval);

                    sequence[index] = interval;
                    retSeq.splice(index, 0, interval);

                    if (index < sequence.length - 1 && RP.core.IntervalJN.isOverlapping(interval,
                    sequence[index + 1])) {
                        curInterval = sequence[index + 1];
                        curRule = RP.core.IntervalJN.getRule(curInterval);

                        splitPair = this._splitSetParentAndRule(curRule,
                        curInterval, interval.End, sequence);

                        interval = iRule.merge(interval, splitPair.first);
                        sequence[index + 1] = splitPair.last;
                    }
                    break;
                }
                else {
                    // new is longer than current so split new, merge the first
                    // half with current
                    // and repalce current with that, then replace interval with
                    // second half of
                    // the split and continue on.
                    splitPair = this._splitSetParentAndRule(iRule, interval,
                    curInterval.End, sequence);

                    retSeq.splice(index, 0, splitPair.first);

                    splitPair.first = curRule.merge(curInterval,
                    splitPair.first);

                    sequence[index] = splitPair.first;

                    interval = splitPair.last;

                }
            }
            else if (RP.core.SequenceJN._isUnboundedSequence(curInterval) && RP.core.IntervalJN.compareEnd(interval, curInterval) < 0) {
                retSeq.splice(index, 0, curRule.merge(curInterval, interval));

                break;
            }
            else if (iRule.canExtend && iRule.canExtend(curInterval, interval, null)) {
                interval = iRule.extend(curInterval, interval, null);
            }
            else if (iRule.canExtend && iRule.canExtend(null, interval, nextInterval)) {
                interval = iRule.extend(null, interval, nextInterval);
                index--;
            }
            else if (curRule.canExtend && curRule.canExtend(interval, curInterval, null)) {
                curInterval = curRule.extend(interval, curInterval, null);

                sequence[index] = curInterval;
                retSeq.splice(index, 0, curInterval);
                index--;
            }

            else {
                // i.e., if new begins later than the current...
                splitPair = this._splitSetParentAndRule(curRule, curInterval,
                interval.Start, sequence);

                sequence[index] = splitPair.first;
                sequence.splice(index + 1, 0, splitPair.last);
            }
        }

        // update sequence Interval properties
        RP.core.SequenceJN.updateSequenceProperties(sequence);

        // put us back into parent if we were taken out
        if (RP.core.IntervalJN.getParent(sequence) && isDisplacmentAdd) {
            RP.core.SequenceJN.addInterval(RP.core.IntervalJN.getParent(sequence),
            sequence, RP.core.IntervalJN.getRule(sequence));
        }

        return retSeq;
    },
    getNextIntervalInSequence : function(sequence, currIndex) {
        var newIndex = currIndex + 1;
        if (sequence.length <= newIndex) {
            var currInterval = sequence[currIndex];
            return {
                Start : currInterval.End,
                End : currInterval.End
            };
        }
        return sequence[newIndex];
    },
    getPrevIntervalInSequence : function(sequence, currIndex) {
        var newIndex = currIndex - 1;
        if (newIndex < 0) {
            var currInterval = sequence[currIndex];
            return {
                Start : currInterval.Start,
                End : currInterval.Start
            };
        }
        return sequence[newIndex];
    },
    getSubSequence : function(sequence, interval, retSequence) {
        if (retSequence === undefined) {
            retSequence = [];
        }

        // find where to begin
        var startIndex = RP.core.SequenceJN.getStartIndex(sequence, interval);

        // starting at the begining index, we are going to walk forward
        for (var indx = startIndex; indx < sequence.length; indx++) {
            // get the current interval
            var curInterval = sequence[indx];

            // out actual terminus condition is when start of current
            // is greater than end of range
            if (curInterval.Start >= interval.End) {
                break;
            }

            var splitPair;
            // check for begin range split
            if (RP.core.IntervalJN.containsTime(curInterval, interval.Start) && interval.Start > curInterval.Start) {
                splitPair = RP.core.IntervalJN.getRule(curInterval).split(
                curInterval, interval.Start);
                curInterval = splitPair.last;
            }

            // check for end range split
            if (RP.core.IntervalJN.containsTime(curInterval, interval.End) && interval.End < curInterval.End) {
                splitPair = RP.core.IntervalJN.getRule(curInterval).split(
                curInterval, interval.End);
                curInterval = splitPair.first;
            }

            // spool out whatever we have at the moment.
            RP.core.SequenceJN.addInterval(retSequence, RP.core.SequenceJN._clone(curInterval));
            splitPair = undefined;
        }

        if (sequence.__isBounded) {
            RP.core.SequenceJN.setBounds(retSequence, interval);
        }

        return retSequence;
    },
    _insertIntoSequence : function(sequence, interval, index) {
        var sRule = RP.core.IntervalJN.getRule(sequence);
        if (sRule && sRule.insert) {
            sRule.insert(sequence, interval, index);
        }
        else {
            sequence.splice(index, 0, interval);
        }
    },
    _isUnboundedSequence : function(interval) {
        return interval.__isSequence && !interval.__isBounded;
    },
    _clone : function(interval) {
        var rule = RP.core.IntervalJN.getRule(interval);
        var ret = rule.clone(interval);
        // Copy Interval stuff
        RP.core.IntervalJN.setRule(ret, rule);
        if (RP.core.IntervalJN.getParent(interval) !== null) {
            RP.core.IntervalJN.setParent(ret, RP.core.IntervalJN
            .getParent(interval));
        }

        // Copy Sequnce stuff
        if (interval.__isSequence) {
            ret.__isSequence = true;
            if (interval.__isBounded) {
                ret.__isBounded = interval.__isBounded;
            }
        }
        return ret;
    },
    _splitSetParentAndRule : function(rule, interval, date, sequence) {
        var splitPair = rule.split(interval, date);

        return this._setParentAndRule(splitPair, sequence, rule);
    },
    _setParentAndRule : function(splitPair, sequence, rule) {
        RP.core.IntervalJN.setParent(splitPair.first, sequence);
        RP.core.IntervalJN.setRule(splitPair.first, rule);
        RP.core.IntervalJN.setParent(splitPair.last, sequence);
        RP.core.IntervalJN.setRule(splitPair.last, rule);
        return splitPair;
    },
    removeInterval : function(sequence, interval) {
        sequence.remove(interval);
        RP.core.SequenceJN.updateSequenceProperties(sequence);

    },
    updateSequenceProperties : function(sequence) {
        if (sequence.__isBounded) {
            return;
        }
        if (sequence.length === 0) {
            sequence.Start = null;
            sequence.End = null;
        }
        else {
            sequence.Start = sequence[0].Start.clone();
            sequence.End = sequence[sequence.length - 1].End.clone();
        }
    },
    getStartIndex : function(sequence, interval) {
        // Check for 0 index conditon
        if ((sequence.length === 0) || (sequence[0].Start.getTime() >= interval.End.getTime())) {
            return 0;
        }

        // Check for end index conditon
        var intervalInSet = sequence[sequence.length - 1];

        if (intervalInSet.End.getTime() < interval.Start.getTime()) {
            return sequence.length;
        }

        var startIndex = RP.core.SequenceJN.binarySearch(sequence, interval);

        intervalInSet = sequence[startIndex - 1];

        if ((startIndex !== 0) && (intervalInSet.End.getTime() > interval.Start.getTime())) {
            startIndex--;
        }

        return startIndex;
    },
    binarySearch : function(sequence, interval) {
        var length = sequence.length;
        var lower_bound = 0;
        var upper_bound = length - 1;
        var s1, s2 = interval.Start.getTime();

        while (lower_bound <= upper_bound) {
            var middle = (lower_bound + upper_bound) >> 1;

            s1 = sequence[middle].Start.getTime();
            if (s1 === s2) {
                return middle;
            }
            else if (s1 < s2) {
                lower_bound = middle + 1;
            }
            else {
                upper_bound = middle - 1;
            }
        }

        return lower_bound;
    },
    binarySearchForSequenceThatContainsTime : function(sequence, time) {
        var length = sequence.length;
        var lower_bound = 0;
        var upper_bound = length - 1;
        var interval;

        while (lower_bound <= upper_bound) {
            var middle = (lower_bound + upper_bound) >> 1;
            interval = sequence[middle];
            if (RP.core.IntervalJN.containsTime(interval, time)) {
                return middle;
            }
            else if (interval.Start.compareTo(time) < 0) {
                lower_bound = middle + 1;
            }
            else {
                upper_bound = middle - 1;
            }
        }
        return -1;
    },
    coalesce : function(sequence) {

        // starting at the 0 index, we are going to walk forward coalescing as
        // we go
        for (var index = 0; index < sequence.length - 1; index++) {
            var curInterval = sequence[index];
            if (curInterval.__isSequence) {
                RP.core.SequenceJN.coalesce(curInterval);
            }

            var curRule = RP.core.IntervalJN.getRule(curInterval);
            var nextInterval = sequence[index + 1];
            if (curRule.canJoin && 
                curRule.canJoin(curInterval, nextInterval) && 
                RP.core.IntervalJN.compareDate(curInterval.End, nextInterval.Start) === 0) {
                
                var joinedInterval = curRule.join(curInterval, nextInterval);
                RP.core.IntervalJN.setParent(joinedInterval, RP.core.IntervalJN
                .getParent(curInterval));
                RP.core.IntervalJN.setRule(joinedInterval, curRule);

                sequence[index] = joinedInterval;
                sequence.splice(index + 1, 1);
                index--;
            }
        }

        if (sequence.length === 1 && sequence[0].__isSequence) {
            RP.core.SequenceJN.coalesce(sequence[0]);
        }

    },
    fillSparceInterval : function(sequence) {
        var i;
        // fix spaces between sequences.
        for (i = 0; i < sequence.length; i++) {
            var rule = RP.core.IntervalJN.getRule(sequence[i]);
            var prev = (i === 0 ? sequence : sequence[i - 1]);
            var next = (i >= sequence.length - 1 ? sequence : sequence[i + 1]);

            if (rule.canExtend && rule.canExtend(prev, sequence[i], next)) {
                rule.extend(prev, sequence[i], next);
            }
        }

    },
    setBounds : function(sequence, interval) {
        sequence.__isSequence = true;
        sequence.__isBounded = true;

        sequence.Start = interval.Start;
        sequence.End = interval.End;

    },
    formatSequence : function(sequence) {
        if (!sequence.__isSequence) {
            return "Not a Sequence";
        }

        if (sequence.length === 0) {
            return "Empty Sequence";
        }

        var retText = [];
        retText.push(String.format("Start Date: {0}", sequence.Start.formatDate(RP.core.Formats.Date.Medium)));
        retText.push(String.format("End Date: {0}", sequence.End.formatDate(RP.core.Formats.Date.Medium)));

        for (var i = 0; i < sequence.length; i++) {
            retText.push(String.format("[{0}] {1}", i, IntervalJN.formatInterval(sequence[i])));
        }

        return retText.join('\r\n');
    }
};

// Shorthand.
// DEPRECATED
var SequenceJN = {};
RP.util.DeprecationUtils.renameClass("SequenceJN", "RP.core.SequenceJN");
//////////////////////
// ..\js\core\IntervalRule.js
//////////////////////
Ext.ns("RP.core");
///////////////////////////////////////////////////////////////////////
// Script IntervalRule
/**
 * @class RP.core.IntervalRule
 */
RP.core.IntervalRule = {

    canSplit : function(intervalJN, splitAt) {

    },
    canJoin : function(intervalJN1, intervalJN2) {

    },
    canMerge : function(intervalJN1, intervalJN2) {

    },
    split : function(intervalJN, splitAt) {

    },
    join : function(intervalJN1, intervalJN2) {

    },
    merge : function(intervalJN1, intervalJN2) {

    }
};

// Shorthand.
// DEPRECATED
var IntervalRule = {};
RP.util.DeprecationUtils.renameClass("IntervalRule", "RP.core.IntervalRule");
//////////////////////
// ..\js\core\ComponentMgr.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class ComponentMgr
 * @singleton
 * @extends none
 * Extends Ext.ComponentMgr to register xtypes, with added features such as parameter handling
 */
RP.core.ComponentMgr = function() {
    // This holds the in/out parameters of a component
    var rpTypeParams = {};
    
    return {
        /**
         * Registers a new xtype
         * @param (String) xtypeName The xtype name
         * @param (Class) classRef The class
         * @param {Array} paramArray Signature (in out parameters, etc.)
         * @return {CompositeElement} this
         */
        register: function(xtypeName, classRef, paramArray) {
            if (xtypeName.toLowerCase() !== xtypeName) {
                throw new RP.Exception("rp reg xtypes must be all lower case - " + xtypeName);
            }
            
            Ext.reg(xtypeName, classRef);
            
            //TODO: site_id might not belong...
            // add site_id as first "default" param for everything
            paramArray = [{
                name: 'site_id',
                type: 'number',
                description: 'Site ID',
                direction: 'in'
            }].concat(paramArray);
            
            rpTypeParams[xtypeName] = paramArray;
        },
        
        /**@private*/
        createConfig: function(xtypeName, paramValueArray, hash) {
            if (!rpTypeParams[xtypeName] && Ext.ComponentMgr.isRegistered(xtypeName)) {
                paramValueArray.xtype = xtypeName;
                return paramValueArray;
            }
            else 
                if (!rpTypeParams[xtypeName]) {
                    throw new RP.Exception("Component is not registered: " + xtypeName + ".");
                }
            var newType = {};
            if (Ext.isArray(paramValueArray)) {
                var paramArray = rpTypeParams[xtypeName];
                
                if (paramArray) {
                    var paramPosition = 0;
                    for (var n = 0; n < paramArray.length; n++) {
                        var param = paramArray[n];
                        if (paramValueArray.length <= paramPosition) {
                            break;
                        }
                        if (param.direction && param.direction.search(/in/i) === -1) {
                            continue;
                        }
                        var paramValue = paramValueArray[paramPosition];
                        newType[param.name ? param.name : param] = paramValue;
                        paramPosition++;
                        
                    }
                }
            }
            else {
                newType = paramValueArray;
            }
            
            newType.hash = hash;
            newType.xtype = xtypeName;
            return newType;
        },
        
        /**
         * Creates a Component from the specified xtype
         * @param (String) xtypeName The xtype name
         * @param (Array) paramValueArray The parameter values
         * @param {String/Array} hash Optional hash value
         * @return {Component} this
         */
        create: function(xtypeName, paramValueArray, hash) {
            var newType = this.createConfig(xtypeName, paramValueArray, hash);
            return Ext.ComponentMgr.create(newType);
        },
        
        /**
         * Gets the component's signature (in out parameters, etc.)
         * @param (String) xtype The xtype name
         * @return {Object} Signature object
         */
        getSignature: function(xtype) {
            var retobj = {};
            Ext.each(rpTypeParams[xtype], function(parameter) {
                if (parameter.name) {
                    var newParameter = Ext.apply({}, parameter);
                    delete newParameter.name;
                    retobj[parameter.name] = newParameter;
                }
            }, this);
            
            return retobj;
        }
    };
}();
//////////////////////
// ..\js\core\CommonExtensions.js
//////////////////////
/*global Ext, RP, window, redprairie */

Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class CommonExtensions
 * @singleton
 * @extends none
 *
 * Singleton for accessing common Ext extensions for our components
 */
RP.core.CommonExtensions = function()
{
  var printChildren = function(config, options)
  {
    var sb = [];
    
    // Check to see if windowOptions was passed into the config.
    config.windowOptions = config.windowOptions || {};
    
    var stashClientMode = RP.globals.getValue("STASH_CLIENTMODE");
    var buildString = RP.stash.getVersion("rpcommon").build;
    if(buildString !== "") {
        buildString = "-" + buildString;
    }
    sb.push("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
    sb.push("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
    sb.push("<head>");
    sb.push("<title>" + config.title + "</title>");
    sb.push(String.format('<link rel="stylesheet" type="text/css" media="all" href="{0}rpcommon-css.{1}{2}.css" />', RP.stash.DEPLOYED_ROOT, stashClientMode, buildString));
    sb.push(String.format('<link rel="stylesheet" type="text/css" media="all" href="{0}rpcommon-print-css.{1}{2}.css" />', RP.stash.DEPLOYED_ROOT, stashClientMode, buildString));
    
    // Add custom .css references.
    if (config.cssURLs)
    {
      Ext.each(config.cssURLs, function(cssUrl)
      {
        sb.push(String.format("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"{0}\" />", cssUrl));
      }, this);
    }
    
    sb.push("</head>");
    sb.push("<body onLoad='javascript:self.print();'>");

    // Append markup of all printable children.
    var items = (config.items instanceof Array) ? config.items : [config.items];
    Ext.each(items,
      function(item, index)
      {
        if (!item.rendered)
        {
          return;
        }

        var childFilter;
        if (config.printChildFilter && !(childFilter = config.printChildFilter(item)))
        {
          return;
        }

        if (typeof childFilter === "object")
        {
          item = childFilter;
        }
        
        var iPrintMarkup = RP.iget(item, RP.interfaces.IPrintSource);

        sb.push("<div>");
        if (iPrintMarkup)
        {
          var markup = iPrintMarkup.getMarkupForPrinting(options);
          sb.push(markup);
        }
        else
        {
          var dom = item.getEl().dom;
          sb.push(dom.outerHTML || RP.util.DOM.getOuterHTML(dom));
        }

        sb.push("</div>");
      });

    sb.push("</body>");
    sb.push("</html>");

    var printWin = window.open("", "PrintWindow",
      "toolbar="    + (config.windowOptions.toolbar     || 'yes') +
     ",location="   + (config.windowOptions.location    || 'no') +
     ",directories="+ (config.windowOptions.directories || 'no') +
     ",status="     + (config.windowOptions.status      || 'no') +
     ",menubar="    + (config.windowOptions.menubar     || 'yes') +
     ",scrollbars=" + (config.windowOptions.scrollbars  || 'yes') +
     ",resizable="  + (config.windowOptions.resizable   || 'yes') +
     ",copyhistory="+ (config.windowOptions.copyhistory || 'yes') +
     ",width="      + (config.windowOptions.width       || 400) +
     ",height="     + (config.windowOptions.height      || 400));

    // join and strip all <a href...> </a> tags
    var html = sb.join('\n').replace(/<a[^>]*>([^<]*)<\/a>/gi, "$1");
    printWin.document.write(html);
    printWin.document.close();
  };
  
  var showIcon = function(iconParent, id, show)
  {
    var tools = iconParent.tools || [];
      
    if (tools[id])
    {      
      if (show)
      {
        tools[id].show();
      }
      else
      {
        tools[id].hide();
      }
    }
  };

  return {
    /**
    * Adds a print tool icon and handler to an ExtJS component.  Must be called prior to being rendered.
    * <ul>
    * <li>&#187; If a control in the 'items' list implements RP.interfaces.IPrintSource, then
    *    that interface is used to generate the markup for printing; otherwise, the 
    *    outerHTML of the child element is used</li>
    * <li>&#187; Only children with 'rendered' set to true will be printed</li>
    * @method
    * @param {object} config An object with the following properties:
    * <ul>
    *  <li><b>title</b>: (string) Title of printed page.</li>
    *  <li><b>iconParent</b>: (Ext.Component) Control with a toolbar to place the Print icon into
    *  <li><b>items</b>: a control or array of controls to print.</li>
    *  <li><b>printChildFilter</b>: ([optional] Function) A callback function to filter which
    *    children to print; if not supplied, all children are printed.
    *    Callback returns true/false to indicate whether or not to print
    *    the child passed in as the sole argument.  The callback may also
    *    instead return a Component to print (usually a child of the item.)</li>
    *  <li><b>cssURLs</b>: ([optional] string[]) An array of URLs (string) of CSS files to
    *    include in the HTML header for printing</li>
    *  <li><b>printOptionsDelegate</b>: A delegate that gets called before actual printing</li>
    *  <li><b>windowOptions</b>: ([optional]) A list of options for the print window that pops up
    *      <ul>
    *       <li><b>toolbar</b>: yes/no (Defaults to yes)</li>
    *       <li><b>location</b>: yes/no (Defaults to no)</li>
    *       <li><b>directories</b>: yes/no (Defaults to no)</li>
    *       <li><b>status</b>: yes/no (Defaults to no)</li>
    *       <li><b>menubar</b>: yes/no (Defaults to yes)</li>
    *       <li><b>scrollbars</b>: yes/no (Defaults to yes)</li>
    *       <li><b>resizable</b>: yes/no (Defaults to yes)</li>
    *       <li><b>copyhistory</b>: yes/no (Defaults to yes)</li>
    *       <li><b>width</b>: A number (Defaults to 400)</li>
    *       <li><b>height</b>: A number (Defaults to 400)</li>
    *       </ul>
    *    </li>
    * </ul>
    */
    addPrintHandler: function(config)
    {
      // Append print button to the control's toolbar.
      var tools = config.iconParent.tools || [];

      tools.push(
          {
            id: 'print',
            handler:
              function()
              {
                if (typeof(config.printOptionsDelegate) === "function")
                {
                  var fn = function(options)
                  {
                    printChildren(config, options);
                  };
                  
                  config.printOptionsDelegate(fn);
                }
                else
                {
                  printChildren(config, null);
                }
              }
          });

      config.iconParent.tools = tools;
    },
    
    /**
    * Pops up a window with support for special rendering for printing, and display
    * the Print dialog.  Exactly the same functionality as addPrintHandler() without
    * adding the print UI (tool icon).
    * <ul>
    * <li>&#187; If a control in the 'items' list implements RP.interfaces.IPrintSource, then
    *    that interface is used to generate the markup for printing; otherwise, the 
    *    outerHTML of the child element is used</li>
    * <li>&#187; Only children with 'rendered' set to true will be printed</li>
    * @method
    * @param {object} config An object with the following properties:
    * <ul>
    *  <li><b>title</b>: (string) Title of printed page.</li>
    *  <li><b>items</b>: a control or array of controls to print.</li>
    *  <li><b>printChildFilter</b>: ([optional] Function) A callback function to filter which
    *    children to print; if not supplied, all children are printed.
    *    Callback returns true/false to indicate whether or not to print
    *    the child passed in as the sole argument.  The callback may also
    *    instead return a Component to print (usually a child of the item.)</li>
    *  <li><b>cssURLs</b>: ([optional] string[]) An array of URLs (string) of CSS files to
    *    include in the HTML header for printing</li>
    *  <li><b>printOptionsDelegate</b>: A delegate that gets called before actual printing</li>
    *  <li><b>windowOptions</b>: ([optional]) A list of options for the print window that pops up
    *      <ul>
    *       <li><b>toolbar</b>: yes/no (Defaults to yes)</li>
    *       <li><b>location</b>: yes/no (Defaults to no)</li>
    *       <li><b>directories</b>: yes/no (Defaults to no)</li>
    *       <li><b>status</b>: yes/no (Defaults to no)</li>
    *       <li><b>menubar</b>: yes/no (Defaults to yes)</li>
    *       <li><b>scrollbars</b>: yes/no (Defaults to yes)</li>
    *       <li><b>resizable</b>: yes/no (Defaults to yes)</li>
    *       <li><b>copyhistory</b>: yes/no (Defaults to yes)</li>
    *       <li><b>width</b>: A number (Defaults to 400)</li>
    *       <li><b>height</b>: A number (Defaults to 400)</li>
    *       </ul>
    *    </li>
    * </ul>
    */
    printComponents: function(config, options)
    {
      printChildren(config, options);
    },
    
    /**
    * Adds a Reload tool icon to your container's title bar
    * @method
    * @param {object} config An object with the following properties:
    * <ul>
    *  <li><b>iconParent</b>: (Ext.Component) Control with a toolbar to place the icon into
    *  <li><b>reloadDelegate</b>: (Function) The delegate to call when the icon is clicked.</li>
    * </ul>
    */
    addReloadHandler: function(config)
    {
      // Append reload button to the control's toolbar.
      var tools = config.iconParent.tools || [];

      tools.push(
          {
            id: 'refresh',
            handler:
              function()
              {
                config.reloadDelegate();
              }
          });

      config.iconParent.tools = tools;
    },
    
    /**
    * Show/hide the Reload icon
    * @method
    * @param {Ext.Component} iconParent The container with the title bar
    * @param {bool} show True to show, False to hide the icon
    */
    showReloadIcon: function(iconParent, show)
    {
      showIcon(iconParent, "refresh", show);
    },
    
    /**
    * Show/hide the Print icon
    * @method
    * @param {Ext.Component} iconParent The container with the title bar
    * @param {bool} show True to show, False to hide the icon
    */
    showPrintIcon: function(iconParent, show)
    {
      showIcon(iconParent, "print", show);
    }
  };
}();
//////////////////////
// ..\js\ui\AbstractSuggest.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @namespace RP.ui
 * @extends Ext.Container
 * 
 * @class AbstractSuggest
 * A simple abstract class that will provide basic functionality for suggest box with an action button.
 * A concrete implementation must provide at least the createDataStore method to define the data store
 * behind the search box.  All other functionality can be taken as is, but may be overridden.
 * @constructor
 * Creates a new implementation of the AbstractSuggest component.
 * @param {Object} config The configuration for the container and live search box component.
 */
RP.ui.AbstractSuggest = Ext.extend(Ext.Container, {
  /**
   * @private
   * Defines a name for the abstract class implementation.  Override with concrete
   * class name (namespace.class).
   */
  _name: "RP.ui.AbstractSuggest",
  
  /**
   * @cfg {Number} width
   * Set the width property of the component.
   */
  width: 250,
  
  /**
   * @cfg {Number} height
   * Set the height property of the component.
   */
  height: 20,
  
  /**
   * @cfg {String} layout 
   * Set the layout of the component.  Defaulted to 'border' to properly layout
   * the control components from west to east.
   */
  layout: 'border',
  
  /**
   * @cfg {Number} minChars
   * Minimum number of characters that are required before
   * the search box 
   */
  minChars: 2,

  /**
   * @private
   * Data store used for searching for entries that match the user's input.
   */
  dataStore: null,
  
  /**
   * @private
   * Reference to the search entry field.
   */  
  searchField: null,
   
  initComponent: function() {
    this.dataStore = this.createDataStore();
    
    Ext.apply(this, {
      items: this.createItems()
    });
    
    RP.ui.AbstractSuggest.superclass.initComponent.call(this);
  },
  
    /**
   * @method
   * Clears the value in the search box
   */
  clearSearchBoxValue: function() {
    this.searchField.clearValue();
  },
  
  /**
   * @method
   * Create a new search entry field.  This method creates a object
   * reference using the xtype livesearchbox.
   * 
   * @return {Object} The search box configuration.
   */
  createSearchEntryField: function() {
    return {
      xtype: 'livesearchbox',
      region: 'center',
      layout: 'fit',
      queryParam: "filter",
      store: this.dataStore,
      minChars: this.minChars
    };
  },
  
  /**
   * @method
   * Base configuration implementation for search entry box.  May be overriden
   * to implement a custom set of action buttons that are handled by implementation 
   * specific overrides.
   * 
   * @return {Object} The object composing the actions available to the user.
   */
  createActionButtons: function() {
    return {
      xtype: 'button',
      text: RP.getMessage("rp.common.misc.SearchButton"),
      width: 50,
      cls: 'rp-ui-abstract-suggest-button',
      region: 'east',
      margins: '0 0 0 5'
    };
  },
  
  /**
   * @abstract 
   * Abstract method for creating the set of items that make up the live search box.
   * The item list is primarily composed of the seraach entry field and action buttons.
   *
   * @return {Object} The list of items composing the search entry box.
   */
  createItems: function() {
    this.searchField = Ext.ComponentMgr.create(this.createSearchEntryField());
    
    return [
      this.searchField,
      this.createActionButtons()
    ];
  },
  
  /**
   * @abstract
   * Abstract method to create the data store.  Must be implemented by the concrete
   * class to properly configure the search box.
   * 
   * @return {Object} The newly created data store.
   */
  createDataStore: RP.abstractFn,
  
  /**
   * Retrieve the selected record from th search box field.  This method implies
   * that the internal representation of the search box is composed with a combo box.
   * 
   * @return {Ext.data.Record} The data record from the data store.
   */
  getSelectedRecord: function() {
    return this.searchField.getSelectedRecord();
  },
  
  /**
   * Retrieve the search box from the control to gain access to the internal
   * fields methods.
   * 
   * @return {RP.ui.LiveSearchBox} The search box control.
   */
  getSearchBox: function() {
    return this.searchField;
  }
});
//////////////////////
// ..\js\ui\Hyperlink.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.Hyperlink
 * Simple Hyperlink class.
 * 
 * @extends Ext.BoxComponent
 * @xtype hyperlink
 *
 */
RP.ui.Hyperlink = Ext.extend(Ext.BoxComponent, {
  /**
   * @cfg {String} text (Required) The text of the link, can include HTML.
   */
  
  /**
   * @cfg {Function} handler (Optional) A function to be called
   * when the link is clicked.
   */
  
  /**
   * @cfg {String} href (Optional) The text that is placed in the href instead of javascript:void(0).
   */
  href: ["javascript", ":void(0);"].join(""),   // "javascript:void(0);" will cause a jslint error
  
  /**
   * @cfg {Object} scope (Optional) The scope to call the handler from.
   */
	
  /**
   * @private
   */
  initComponent: function() {
    this.handlers = [];
    
    if (this.handler) {
      this.handlers.push(this.handler);
    }
    
    this.autoEl = {
      tag: "a",
      href: this.href, 
      html: this.text
    };
    
    RP.ui.Hyperlink.superclass.initComponent.call(this);
    
    this.addEvents("click");
  },
  
  /**
   * Override to attach the click handler to the component's Ext.Element.
   * @private
   */
  afterRender: function() {
    RP.ui.Hyperlink.superclass.afterRender.call(this);
    
    this.mon(this.el, "click", function() {
      Ext.each(this.handlers, function(handler) {
        handler.call(this.scope || this);
      }, this);
      
      this.fireEvent("click", this);
    }, this);
  },
  
  /**
   * Updates the text of the hyperlink.
   * @method
   * @param {String} text
   */
  setText: function(text) {
    this.update(text);
  },
  
  /**
   * Gets the text of the hyperlink.
   * @method
   * @return {String}
   */
  getText: function() {
    return this.el.dom.innerHTML;
  },
  
  /**
   * Adds an additional click handler.
   * @method
   * @param {Function} handler
   */
  addHandler: function(handler) {
    this.handlers.push(handler);
  }
});

Ext.reg("hyperlink", RP.ui.Hyperlink);
//////////////////////
// ..\js\ui\HyperlinkBreadCrumb.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.HyperlinkBreadCrumb
 * A hyperlink displayed in navigational breadcrumbs. Contain references
 * to cards prior to the current page in a navigational hierarchy.
 *
 * @extends RP.ui.Hyperlink
 * @cfg {Object} card The card to be shown when the breadcrumb is activated.
 */
RP.ui.HyperlinkBreadCrumb = Ext.extend(RP.ui.Hyperlink, {
  cls: "rp-breadcrumb",
  
  /**
   * @private
   */
  initComponent: function() {
    this.addEvents(
      /**
       * @event activate
       * Fired when the breadcrumb is clicked and is not deactivated.
       * @param {RP.ui.HyperlinkBreadCrumb} this
       */
      "activate"
    );
    
    RP.ui.HyperlinkBreadCrumb.superclass.initComponent.call(this);
  },
  
  /**
   * A default handler that simply fires the 'activate' event.
   * @method
   */
  handler: function() {
    this.fireEvent("activate", this);
  },
  
  /**
   * Deactivates the breadcrumb, preventing it from firing its activate event.
   * Also gives it the appearance that it is plain text.
   * @method
   */
  deactivate: function() {
    this.suspendEvents(false);
    this.addClass("deactivated");
  },
  
  /**
   * Does the inverse of deactivate.
   * @method
   */
  reactivate: function() {
    this.resumeEvents();
    this.removeClass("deactivated");
  },
  
  /**
   * Gets the card associated with this breadcrumb.
   * @method
   * @return {Object} The card associated with the current breadcrumb.
   */
  getCard: function() {
    return this.card;
  }
});
//////////////////////
// ..\js\ui\AccordionScrollPanel.js
//////////////////////
/*global Ext, RP, window */

Ext.ns("RP.ui");

/**
 * @namespace RP.ui
 * @class AccordionScrollPanel
 * @extends Ext.Panel
 * Accordion scroll panel with up and down arrows at the top and bottom.
 */
RP.ui.AccordionScrollPanel = Ext.extend(Ext.Panel, {
  constructor: function(config) {
    this._topToolbar = new Ext.Toolbar({
      height: 22,
      cls: 'rp-scrollbar-up'
    });
    this._bottomToolbar = new Ext.Toolbar({
      height: 22,
      cls: 'rp-scrollbar-down'
    });
    
    this._count = 0;    
    this._timeInterval = null;
    
    var originalRenderListener = Ext.emptyFn;
    var originalRenderScope = window;
    
    if (config.listeners && config.listeners.render) {
      originalRenderListener = Ext.isFunction(config.listeners.render) ? config.listeners.render : (Ext.isFunction(config.listeners.render.fn) ? config.listeners.render.fn : Ext.emptyFn);
      originalRenderScope = config.listeners.render.scope || config.listeners.scope || window;
    }
    
    var registered = false;
    var cfg = {
      layout: 'anchor',
      tbar: this._topToolbar,
      bbar: this._bottomToolbar,
      listeners: {
        render: {
          fn: function(panel) {
            if (!registered) {
              var dir = '';
              var interval = null;
              var fn = function() {
                if (dir && panel.body.isScrollable()) {
                  panel.body.scroll(dir, 20);
                }
                else {
                  window.clearInterval(interval);
                  interval = null;
                }
              };
              panel.tbar.dom.onmouseover = function() {
                dir = 'up';
                if(!interval){
                    interval = window.setInterval(fn, 100);
                }
                el = Ext.fly(panel.tbar.dom);
                el.addClass('rp-scrollbar-over');
              };
              panel.bbar.dom.onmouseover = function() {
                dir = 'down';
                if(!interval){
                    interval = window.setInterval(fn, 100);
                }
                el = Ext.fly(panel.bbar.dom);
                el.addClass('rp-scrollbar-over');
              };
              panel.tbar.dom.onmouseout = function() {
                dir = '';
                el = Ext.fly(panel.tbar.dom);
                el.removeClass('rp-scrollbar-over');
              };
              panel.bbar.dom.onmouseout = function() {
                dir = '';
                el = Ext.fly(panel.bbar.dom);
                el.removeClass('rp-scrollbar-over');
              };
              
              /*ipad events to allow for scrolling the widgets*/
              panel.tbar.dom.onmouseup = panel.tbar.dom.onmouseout;
              panel.bbar.dom.onmouseup = panel.bbar.dom.onmouseout;
              
              panel.tbar.dom.ontouchstart = panel.tbar.dom.onmouseover;
              panel.bbar.dom.ontouchstart = panel.bbar.dom.onmouseover;
              panel.tbar.dom.ontouchend = panel.tbar.dom.onmouseup;
              panel.bbar.dom.ontouchend = panel.bbar.dom.onmouseup;
              
              this._topToolbar.container.addClass('x-hide-visibility');
              this._bottomToolbar.container.addClass('x-hide-visibility');
              registered = true;
            }
            
            originalRenderListener.call(originalRenderScope);
          },
          scope: this
        },
        resize: {
          fn: this.fixScroll,
          scope: this
        },
        expand: {
          fn: this.fixScroll,
          scope: this
        }
      }
    };
    
    Ext.apply(config, cfg);
    
    RP.ui.AccordionScrollPanel.superclass.constructor.apply(this, [config]);
  },
  
  fixScroll: function() {
    if (this.isDestroyed || this.collapsed || !this.getEl() || !this.getEl().getBox()) {
      return;
    }
    
    if (!this.items || this.items.length === 0) {
      if (this._count < 1000) {
        // client and scroll heights aren't set right at this point, so need to delay
        if (this._timeInterval) {
          window.clearTimeout(this._timeInterval);
        }
        this._timeInterval = window.setTimeout(this.fixScroll.createDelegate(this, [this]), 100);
        this._count++;
      }
      return;
    }
    
    this._count = 0;
    
    var origHeight = this.getEl().getBox().height;
    
    if (this.body.isScrollable()) {
      this._topToolbar.container.removeClass('x-hide-visibility');
      this._bottomToolbar.container.removeClass('x-hide-visibility');
      this.tbar.removeClass("hide");
      this.bbar.removeClass("hide");
    }
    else {
      this.tbar.addClass("hide");
      this.bbar.addClass("hide");
      this.body.scrollTo('top');
    }
    
    var newHeight = this.getEl().getBox().height;
    if (origHeight !== newHeight) {
      this.setHeight(origHeight);
    }
  }
});

Ext.reg("rpaccordionscrollpanel", RP.ui.AccordionScrollPanel);
//////////////////////
// ..\js\ui\ChangePassword.js
//////////////////////
Ext.ns("RP.ui");

// TODO: client-side standards for checking password strength?

RP.ui.ChangePassword = Ext.extend(Ext.Window, {
  id: "changePassword",
  
  success: false,
  
  /**
   * @private
   */
  initComponent: function() {
  
    // allows enter key to submit the login form
    var enterKeyFn = (function(field, e) {
      if (e.getKey() == e.ENTER) {
        e.stopEvent();
        submitFn.call(this);
      }
    }).createDelegate(this);
    
    var submitFn = function() {
      var form = this.getComponent("form");
      var results = form.getForm().getValues();
      var myMask = new Ext.LoadMask(document.body, { msg: RP.getMessage("rp.common.misc.SavingMaskText") });
      myMask.show();
      
      var msgBoxOptions = {
        modal: true,
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.ERROR,
        minWidth: 300,
        title: RP.getMessage("rp.common.changepassword.ErrorTitle")
      };
      
      if (Ext.isEmpty(results.curPwd)) {
        form.getComponent("curPwd").markInvalid(RP.getMessage("rp.common.changepassword.EmptyError"));
        myMask.hide();
        return;  
      }
      if (Ext.isEmpty(results.newPwd)) {
        form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.EmptyError"));
        myMask.hide();
        return;
      }
      
      if (results.newPwd !== results.confirmPwd) {
        form.getComponent("confirmPwd").markInvalid(RP.getMessage("rp.common.changepassword.NewPasswordsDoNotMatch"));
        myMask.hide();
        return;
      }
      
      Ext.Ajax.requestWithTextParams({
        url: RP.globals.getFullyQualifiedPath("PATH_TO_DATA_ROOT") + "changePassword",
        disableExceptionHandling: true,
        method: "POST",
        params: Ext.urlEncode({
          oldPwd: results.curPwd,
          newPwd: results.newPwd
        }),
        scope: this,
        callback: function(options, success, response) {
          var result = Ext.util.JSON.decode(response.responseText).status;
          myMask.hide();
          switch (result) {
              case RP.REFSExceptionCodes.OK:
                logger.logTrace("[ChangePassword] Password change successful.");

                this.success = true;
                this.newPassword = results.newPwd;
                
                Ext.MessageBox.alert(RP.getMessage("rp.common.changepassword.FormTitle"), RP.getMessage("rp.common.changepassword.Success"), function(){
                    this.destroy();
                    this.newPassword = null;
                }, this);
                
                break;
            case RP.REFSExceptionCodes.PASSWORD_OLD_PASSWORD_INCORRECT:
                logger.logError("[ChangePassword] Old password was incorrect.");
                form.getComponent("curPwd").markInvalid(RP.getMessage("rp.common.changepassword.OldPasswordInvalid"));
                break;
                
            case RP.REFSExceptionCodes.PASSWORD_TOO_SHORT:
                logger.logError("[ChangePassword] Password too short.");
                form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.TooShortError"));
                break;
                
            case RP.REFSExceptionCodes.PASSWORD_MISSING_UPPER:
                logger.logError("[ChangePassword] Password missing uppercase character.");
                form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.MissingUpperCaseError"));
                break;
                
            case RP.REFSExceptionCodes.PASSWORD_MISSING_NONALPHA:
                logger.logError("[ChangePassword] Password missing non-alphanumeric character.");
                form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.MissingNonAlphaError"));
                break;
                
            case RP.REFSExceptionCodes.PASSWORD_CONTAINS_USERID:
                logger.logError("[ChangePassword] Password contains user ID.");
                form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.ContainsUserNameError"));
                break;
                
            case RP.REFSExceptionCodes.PASSWORD_DUPLICATE_OF_PREVIOUS:
                logger.logError("[ChangePassword] Password contains old password.");
                form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.ContainsOldPasswordError"));
                break;

            case RP.REFSExceptionCodes.INVALID_LOGIN:
                logger.logError("[ChangePassword] Invalid login.");
                Ext.Msg.show(Ext.apply(msgBoxOptions, {
                    msg: RP.getMessage("rp.common.changepassword.InvalidLogin")
                }));
                break;

            case RP.REFSExceptionCodes.PASSWORD_ACCOUNT_LOCKED:
                logger.logError("[ChangePassword] Account is locked.");
                Ext.Msg.show(Ext.apply(msgBoxOptions, {
                    msg: RP.getMessage("rp.common.changepassword.AccountLocked")
                }));
                break;

            case RP.REFSExceptionCodes.PASSWORD_EXPIRED_EXCEPTION:
                logger.logError("[ChangePassword] Password is expired.");
                Ext.Msg.show(Ext.apply(msgBoxOptions, {
                    msg: RP.getMessage("rp.common.changepassword.Expired")
                }));
                break;

            case RP.REFSExceptionCodes.PASSWORD_CHANGE_REQUIRED:
                logger.logError("[ChangePassword] Password change required.");
                Ext.Msg.show(Ext.apply(msgBoxOptions, {
                    msg: RP.getMessage("rp.common.changepassword.ChangeRequired")
                }));
                break;

            case RP.REFSExceptionCodes.PASSWORD_SAME_CASE:
                logger.logError("[ChangePassword] Password cannot be entirely the same case.");
                form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.SameCase"));
                break;

            default:
                Ext.Msg.show(Ext.apply(msgBoxOptions, {
                    msg: RP.getMessage("rp.common.changepassword.GenericServerError")
                }));
            }
        }
      });
    };
    
    Ext.apply(this, {
      title: this.initialConfig.formTitle || RP.getMessage("rp.common.changepassword.FormTitle"),
      width: 450,
      draggable: false,
      resizable: false,
      closable: false,
      modal: true,
      padding: 20,
      bodyStyle: "background-color: #fff",
      items: [{
        xtype: "box",
        html: this.initialConfig.formIntro || RP.getMessage("rp.common.changepassword.FormIntro")
      }, {
        xtype: "form",
        itemId: "form",
        style: "margin: 20px auto",
        labelAlign: "right",
        labelWidth: 125,
        defaults: {
          anchor: "99%",
          labelStyle: "font-weight: bold",
          inputType: "password",
          listeners: {
            specialkey: enterKeyFn
          },
          msgTarget: "under"
        },
        width: "80%",
        border: false,
        items: [{
          xtype: "textfield",
          itemId: "curPwd",
          name: "curPwd",
          fieldLabel: RP.getMessage("rp.common.changepassword.OldPasswordLabel")
        }, {
          xtype: "textfield",
          itemId: "newPwd",
          name: "newPwd",
          fieldLabel: RP.getMessage("rp.common.changepassword.NewPasswordLabel")
        }, {
          xtype: "textfield",
          itemId: "confirmPwd",
          name: "confirmPwd",
          fieldLabel: RP.getMessage("rp.common.changepassword.ConfirmPasswordLabel")
        }]
      }, {
        itemId: "pnlButtons",
        border: false,
        buttonAlign: "center",
        buttons: [{
          itemId: "btnSubmit",
          text: RP.getMessage("rp.common.changepassword.SubmitButton"),
          scope: this,
          handler: submitFn
        }, {
          itemId: "btnCancel",
          text: RP.getMessage("rp.common.changepassword.CancelButton"),
          scope: this,
          handler: this.destroy
        }]
      }]
    });
    
    RP.ui.ChangePassword.superclass.initComponent.call(this);

    // attempt to focus the userName field after the form is shown
    this.on("show", function() {
      this.getComponent("form").getComponent("curPwd").focus(false, 500);
    }, this);
    
    // show the dialog immediately after initialization.
    this.show();
    
    var inactiveFn = (function() {
      this.destroy();
    }).createDelegate(this);
    
    RP.util.PageInactivityChecker.addHandler(inactiveFn);
    
    this.on("destroy", function() {
      RP.util.PageInactivityChecker.removeHandler(inactiveFn);
    }, this);
  },
  
  /**
   * Retrieve the new password
   * @return {String}
   */
  getNewPassword: function(){
    return this.newPassword;
  }
});
//////////////////////
// ..\js\ui\ClickableToolTip.js
//////////////////////
Ext.ns("RP.ui");


/**
 * @class RP.ui.ClickableToolTip
 * @extends Ext.ToolTip
 *
 * A ToolTip extension allowing user interactivity.  The Tooltip
 * is only shown after a click event on an appropriate delegate
 * within the ToolTip's target.  Once shown, the ToolTip is hidden
 * by clicking the close button or clicking outside the ToolTip area.
 *
 * Since Ext.ToolTip is a descendant of Ext.Container, it supports
 * the items config, allowing child components.
 */
RP.ui.ClickableToolTip = Ext.extend(Ext.ToolTip, {
  /**
   * @cfg {String} delegate (Required) See Ext.ToolTip.delegate for details.
   */
  
  /**
   * @cfg {Function} handler (Required) See Ext.ToolTip.delegate for details.
   */
  
  autoWidth: true,
  
  initComponent: function() {
    Ext.apply(this, {
      closable: true,
      autoHide: false,
      showDelay: 0,
      mouseDownXY: [null, null]
    });
    
    RP.ui.ClickableToolTip.superclass.initComponent.call(this);
  },
  
  /**
   * Custom override to try and keep the ToolTip within the screen.
   * @private
   */
  show: function() {
    RP.ui.ClickableToolTip.superclass.show.apply(this, arguments);
    
    var box = this.getBox();
    if (box.x + box.width > Ext.getBody().getWidth()) {
      var x = Ext.getBody().getWidth() - (box.width + 10);
      
      this.setPagePosition(x, box.y);
    }
  },
  
  /**
   * @override
   * @param {Mixed} target
   * @private
   */
  initTarget: function(target) {
    var t;
    if ((t = Ext.get(target))) {
      // remove existing listener
      if (this.target) {
        var tg = Ext.get(this.target);
        this.mun(tg, "mousedown", this.onTargetMouseDown, this);
        this.mun(tg, "mouseup", this.onTargetMouseUp, this);
      }
      // set the new one
      this.mon(t, "mousedown", this.onTargetMouseDown, this);
      this.mon(t, "mouseup", this.onTargetMouseUp, this);
      this.target = t;
    }
  },
  
  /**
   * True if the event should be ignored... based on if the ToolTip is
   * disabled or if the event is not correctly within the target.
   * @param {Ext.EventObject} e
   * @return {Boolean}
   */
  ignoreEvent: function(e) {
    return this.disabled || e.within(this.target.dom, true);
  },
  
  /**
   * Used to record the original x/y coords of the click process.
   * @param {Ext.EventObject} e
   * @private
   */
  onTargetMouseDown: function(e) {
    if (!this.ignoreEvent(e)) {
      this.mouseDownXY = e.getXY();
    }
  },
  
  /**
   * Shows the ToolTip if the component is not disabled, the event is valid,
   * and if the x/y coords have not changed since the mousedown.
   * @param {Ext.EventObject} e
   * @private
   */
  onTargetMouseUp: function(e) {
    var newXY = e.getXY();
    var oldXY = this.mouseDownXY;
    
    this.mouseDownXY = [null, null];
    
    if (this.ignoreEvent(e) || !(oldXY[0] == newXY[0] && oldXY[1] == newXY[1]) ) {
      return;
    }
    
    var t = e.getTarget(this.delegate);
    if (t) {
      this.triggerElement = t;
      this.targetXY = newXY;
      this.show();
    }
  },
  
  /**
   * @param {Ext.EventObject} e
   * @private
   * @override
   */
  onDocMouseDown: function(e) {
    if (!e.within(this.el.dom)) {
      this.hide();
    }
  }
});
//////////////////////
// ..\js\ui\ComponentDataView.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.ComponentDataView
 * @extends Ext.DataView
 * @xtype compdataview
 *
 * DataView extension that supports rendering Components internally.  Due
 * to the nature of how dataview's work, the Components will be destroyed
 * and then recreated after key actions (refresh, update, add, etc...),
 * but this component ensures correct destruction of the components in
 * order to avoid memory leaks.
 *
 * Also requires the same required config options as the normal Ext.DataView.
 *
 * This component is slight extension from the one found here:
 * http://www.extjs.com/forum/showthread.php?79210-ComponentDataView-Ext-components-inside-a-dataview-or-listview
 */
RP.ui.ComponentDataView = Ext.extend(Ext.DataView, {
  defaultType: "textfield",
  
  /**
   * Default class for the table
   */
  cls: "comp-data-view",
  
  /**
   * @cfg {Array} items (Required) Array of Component object configs.
   * 
   * A renderTarget or applyTarget can be optionally specified to
   * choose where inside the row markup the component should be rendered.
   * 
   * If the component has a setValue method and an applyValue config is
   * specified, the component will be automatically store bound, and any changes
   * to the component's value will update the store when the component's 'blur'
   * event fires.
   * 
   * An optional renderer can be included, which will be called with the
   * scope of the dataview or passed in scope config, along with the following arguments:
   * (component, target, record, rowIndex, colIndex, store).
   */
  initComponent: function() {
    RP.ui.ComponentDataView.superclass.initComponent.call(this);
    this.components = new Ext.util.MixedCollection();
  },
  
  refresh: function() {
    this.components.clear();
    RP.ui.ComponentDataView.superclass.refresh.call(this);
    this.renderItems(0, this.store.getCount() - 1);
  },
  
  selectRow: function(index) {
    var selectedItem = Ext.query(".comp-data-view-row-selected", this.getTemplateTarget().dom)[0];
    if (selectedItem) {
        Ext.fly(selectedItem).removeClass("comp-data-view-row-selected");
    }
    var item = Ext.query(".comp-data-view-row", this.getTemplateTarget().dom)[index];
    Ext.fly(item).addClass("comp-data-view-row-selected");
  },
  
  // private
  onUpdate: function(ds, record) {
    var index = ds.indexOf(record);
    if (index > -1) {
      this.destroyItems(index);
    }
    RP.ui.ComponentDataView.superclass.onUpdate.apply(this, arguments);
    if (index > -1) {
      this.renderItems(index, index);
    }
  },
  
  // private
  onAdd: function(ds, records, index) {
    var count = this.all.getCount();
    RP.ui.ComponentDataView.superclass.onAdd.apply(this, arguments);
    if (count !== 0) {
      this.renderItems(index, index + records.length - 1);
    }
  },
  
  onClick: function(e) {
    var item = e.getTarget(this.itemSelector, this.getTemplateTarget());
    if(item){
      var index = this.indexOf(item);
      
      // Get rid of any selected row styling and apply to newly clicked row
      var selectedItem = Ext.query(".comp-data-view-row-selected", this.getTemplateTarget().dom)[0];
      if (selectedItem) {
        Ext.fly(selectedItem).removeClass("comp-data-view-row-selected");
      }
      Ext.fly(item).addClass("comp-data-view-row-selected");
      
      if(this.onItemClick(item, index, e) !== false){
        this.fireEvent("click", this, index, item, e);
      }
    }else{
      if(this.fireEvent("containerclick", this, e) !== false){
        this.onContainerClick(e);
      }
    }
  },
  
  // private
  onRemove: function(ds, record, index) {
    this.destroyItems(index);
    RP.ui.ComponentDataView.superclass.onRemove.apply(this, arguments);
  },
  
  // private
  onDestroy: function() {
    RP.ui.ComponentDataView.onDestroy.call(this);
    this.components.clear();
  },
  
  // private
  renderItems: function(startIndex, endIndex) {
    var ns = this.all.elements; // array of rows for this data view
    
    var blurFn = function(f) {
      this.store.getAt(this.index).data[this.dataIndex] = f.getValue();
    };
    
    for (var i = startIndex; i <= endIndex; i++) {
      // append new element array to args containing this row's components
      var r = [];
      
      for (var items = this.items, j = 0, len = items.length, c; j < len; j++) {
        c = items[j].render ? c = items[j].cloneConfig() : Ext.create(items[j], this.defaultType);
        c.itemId = "cdv-" + i + "-" + j;
        r[j] = c;
        
        var target = ns[i];
        
        if (c.renderTarget) {
          target = Ext.DomQuery.selectNode(c.renderTarget, ns[i]);
          c.render(target);
        }
        else if (c.applyTarget) {
          target = Ext.DomQuery.selectNode(c.applyTarget, ns[i]);
          c.applyToMarkup(target);
        }
        else {
          c.render(target);
        }
        
        if (Ext.isFunction(c.setValue) && c.applyValue) {
          c.setValue(this.store.getAt(i).get(c.applyValue));
          c.on('blur', blurFn, {
            store: this.store,
            index: i,
            dataIndex: c.applyValue
          });
        }
        
        // call the custom renderer
        if (Ext.isFunction(c.renderer)) {
          c.renderer.call(c.scope || this, c, target, this.store.getAt(i), i, j, this.store);
        }
      }
      this.components.insert(i, r);
    }
  },
  
  // private
  destroyItems: function(index) {
    var componentToDestroy =  this.components.removeAt(index);
    Ext.destroy(componentToDestroy);
  }
});

Ext.reg("compdataview", RP.ui.ComponentDataView);
//////////////////////
// ..\js\ui\ComponentDataViewGrid.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.ComponentDataViewGrid
 * @extends RP.ui.ComponentDataView
 * @xtype compdataviewgrid
 *
 * ComponentDataView extension that supports rendering Components automatically
 * in a grid format.  Similar to how the ComponentDataView works the Components 
 * will be destroyed and then recreated after key actions (refresh, update, add, 
 * etc...), but this component ensures correct destruction of the components in
 * order to avoid memory leaks.
 *
 * Also requires the same required config options as the normal RP.ui.ComponentDataViewGrid
 * and Ext.DataView.
 */
RP.ui.ComponentDataViewGrid = Ext.extend(RP.ui.ComponentDataView, {
  
  /**
   * Default class for the table
   */
  cls: "comp-data-view-grid",
  
  initComponent: function(){
    var tempate = new Ext.Template(
      '<table cellpadding="0" cellspacing="0">' +
        '<thead>' + 
          '<tr>' +
            '{templateHeader}' +
          '</tr>' +
        '</thead>' +
        '<tbody>' +
          '<tpl for=".">' +
            '<tr class="comp-data-view-row">' +
              '{templateBody}' +
            '</tr>' +
          '</tpl>' +
        '</tbody>' +
      '</table>'
    );
    
    var headerTemplate = new Ext.Template(
      '<th style="white-space:nowrap;{style}"><div {tooltip}>{headerData}</div></th>'
    );
    
    var rowTemplate = new Ext.Template(
      '<td class="col{columnNum}" width="{width}" style="{style}">{rowData}</td>'
    );
    
    this.itemSelector = 'tr.comp-data-view-row';
    var columns = this.initialConfig.items;
    var tplHeader = '';
    var tplBody = '';
    
    for (ii=0; ii<columns.length; ii++) {
      if (columns[ii].hidden) {
        continue;
      }
      
      var tooltip = "";
      if (columns[ii].tooltip) {
        tooltip = "ext:qtip='" + columns[ii].tooltip + "'";
      }
      
      tpldata = '';
      dataIndex = columns[ii].dataIndex;
      if (dataIndex) {
        columns[ii].xtype = "label";
      }
      tplHeader += headerTemplate.apply({
        headerData: columns[ii].header,
        style: columns[ii].style,
        tooltip: tooltip
      });
      
      if (this.initialConfig.autoExpandColumn === columns[ii].colID) {
        tplBody += rowTemplate.apply({
          columnNum: ii,
          width: "100%",
          style: columns[ii].style,
          rowData: tpldata 
        });
      }
      else {
        tplBody += rowTemplate.apply({
          columnNum: ii,
          style: columns[ii].style,
          rowData: tpldata 
        });
      }
      
      columns[ii].renderTarget = "td.col" + ii;
    }
    
    this.tpl = tempate.apply({
      templateHeader: tplHeader,
      templateBody: tplBody
    });
    
    RP.ui.ComponentDataViewGrid.superclass.initComponent.call(this);
  }
  
});

Ext.reg("compdataviewgrid", RP.ui.ComponentDataViewGrid);
//////////////////////
// ..\js\ui\ImageButton.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class ImageButton
 * A button with an image as its background.
 * 
 * @namespace RP.ui
 * @extends Ext.Button
 * @param {Object} config
 */
RP.ui.ImageButton = Ext.extend(Ext.Button, {
  /**
   * @private
   */
  initComponent: function(){
    var config = this.initialConfig;
    
    config.iconWidth = config.iconWidth || 110;
    config.iconHeight = config.iconHeight || 46;
    config.iconFontColor = config.iconFontColor || "white";
    config.iconFontWeight = config.iconFontWeight || "bold";
    
    // set the background icon to use when the button is disabled
    config.disabledIcon = config.disabledIcon || config.icon;
    config.disabledText = config.disabledText || config.text;
    
    // applies override styles found in rp-buttons.css
    Ext.apply(this, {
      cls: "rp-img-button",
      scale: "" // prevent Ext from sizing the button with a default size
    });    
    
    RP.ui.ImageButton.superclass.initComponent.call(this);
    
    this.on("render", function(){
      this.setIconSize(this.initialConfig.iconWidth, this.initialConfig.iconHeight);
      this.btnEl.setStyle("color", this.initialConfig.iconFontColor);
      this.btnEl.setStyle("font-weight", this.initialConfig.iconFontWeight);
    }, this);
  },
  
  /**
   * Changes the button's image to a disabled button style.
   * @method
   */
  disable: function(){
    this.setIcon(this.initialConfig.disabledIcon);
    this.setText(this.initialConfig.disabledText);
    RP.ui.ImageButton.superclass.disable.call(this);
    this.removeClass("x-item-disabled");
  },
  
  /**
   * Changes the button's image to an enabled button style.
   * @method
   */
  enable: function(){
    this.setIcon(this.initialConfig.icon);
    this.setText(this.initialConfig.text);
    RP.ui.ImageButton.superclass.enable.call(this);
  },
  
  /**
   * Changes the button's icon size.
   * @method
   * @param {Number} width The desired width of the button icon in pixels.
   * @param {Number} height The desired height of the button icon in pixels.
   */
  setIconSize: function(width, height){
    this.btnEl.setStyle("width", width + "px");
    this.btnEl.setStyle("height", height + "px");
  }
});

Ext.reg("rpimagebutton", RP.ui.ImageButton);
//////////////////////
// ..\js\ui\LiveSearchBox.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.LiveSearchBox
 *
 * Override of Ext.form.ComboBox which defaults and sets many of the fields needed to
 * implement the live search feature. By default the control will send the following
 * <p>arguments to the server-side:</p>
 * <p>query - the field to filter by.</p>
 * <p>start - the start index (used for paging).</p>
 * <p>limit - number of results to return (can also be used with paging).</p>
 * 
 * This component is slight extension from the one found here:
 * http://www.extjs.com/deploy/dev/examples/form/forum-search.html
 * 
 * @extends Ext.form.ComboBox
 * @xtype livesearchbox
 */
RP.ui.LiveSearchBox = Ext.extend(Ext.form.ComboBox, {
  
  /**
   * @private {String} CSS selector for the item used to display
   * the data for the drop down. Defaults to div.livesearch-item
   */
  itemSelector: "div.livesearch-item",
  
  /**
   * @private {Number} The minimum number of characters needed for 
   * autoComplete and typeAhead to activate. Defaults to 2.
   */
  minChars: 2,
  
  /**
   * @cfg {String/Ext.XTemplate} The template string, or Ext.XTemplate instance 
   * to use to display each item in the dropdown list.
   */
  tpl: '<tpl for="."><div class="livesearch-item">{item}</div></tpl>',
  
  /**
   * @private
   */
  initComponent: function() {
    this.addEvents(
      /**
       * @event update
       * Fired when there is a new selection.
       * @param {LiveSearchBox} this
       * @param {Ext.data.Record}
       */
      "update"
    );
    
    // force these settings...
    Ext.apply(this, {
      enableKeyEvents: true,
      hideTrigger: true,
      typeAhead: false
    });
    
    this.loadingText = this.loadingText ? this.loadingText : RP.getMessage("rp.common.misc.LoadingMaskText");
    
    // if using a pager, default the width
    if (this.initialConfig.pageSize) {
      this.pageSize = this.initialConfig.pageSize;
      this.width = this.initialConfig.width ? this.initialConfig.width : 230;
    }
    
    // hide the search results when the query is deleted
    this.on("keyup", function() {
      if (this.getValue() === null || this.getValue() === "") {
        this.collapse();
      }
    }, this);
    
    RP.ui.LiveSearchBox.superclass.initComponent.call(this);
  },
  
  // private
  /**
   * Sets the LiveSearchBox's value to the item selected by the user
   * @private
   * @param {Ext.data.Record} record The record selected
   */
  onSelect: function(record) {
    // only set the selected row if the value has not changed
    if (this.lastQuery == this.getValue()) {
      this.setValue(record.data.item);
      this.collapse();
      this.fireEvent("update", this, record);
    }
  },
  
  /**
   * Retrieve the selected record from the search selection.
   * @method
   * @return {Ext.data.Record/null} The record object currently selected or null if no selection present.
   */
  getSelectedRecord: function() {
    var rows = this.view.getSelectedRecords();
    
    if (rows.length > 0) {
      return rows[0];
    }
    return null;
  }
});

Ext.reg("livesearchbox", RP.ui.LiveSearchBox);
//////////////////////
// ..\js\ui\TimeField.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class TimeField
 * <p>An Ext.form.TextField extension for entering time values.</p>
 * <p>Will automatically attempt to parse and format the time value
 * on the blur event.</p>
 *
 * @namespace RP.ui
 * @extends Ext.form.TextField
 * @constructor
 * @param {Object} config
 */
RP.ui.TimeField = Ext.extend(Ext.form.TextField, {

    /**
     * @cfg {Object} format An RP.core.Formats.Time value to use for formatted
     * the entered time values with. Defaults to RP.core.Formats.Time.Default.
     */
    /**
     * @private
     */
    initComponent: function() {
        this._format = this.initialConfig.format || RP.core.Formats.Time.Default;
        this._theDate = (new Date()).clearTime();
        this.typedValue = null;
        
        this._roundMinutes = Ext.value(this.initialConfig.roundMinutes, 1);
        
        if (!this.initialConfig.emptyText) {
            Ext.apply(this, {
                emptyText: RP.getMessage("rp.common.misc.EmptyTimeText")
            });
        }
        
        this.addEvents("beforechange");
        this.on("change", function(e) {
            this.fireEvent("beforechange");
        });
        
        RP.ui.TimeField.superclass.initComponent.call(this);
    },
    
    /**
     * Override to format the entered value into the field's time format.
     * @private
     */
    beforeBlur: function() {
        var v = this.parseValue(this.getRawValue());
        if (v) {
            this.typedValue = this.getRawValue();
            this.setRawValue(this.formatTime(v));
        }
    },
    
    /**
     * Sets the field's internal base date.
     * @method
     * @param {Date} dt The date to set the field's internal base date to.
     */
    setTheDate: function(dt) {
        if (!Ext.isDate(dt)) {
            dt = RP.core.Sundial.now();
        }
        
        this._theDate = dt.clone().clearTime();
        
        if (this.getRawValue()) {
            this.setValue(this.getRawValue());
        }
    },
    
    /**
     * Sets the field's value to the given time
     * @method
     * @param {Date} dt The time to set the field's value to.
     */
    setTheTime: function(dt) {
        this.setValue(this.formatTime(dt));
    },
    
    /**
     * Sets the field's internal base date and time to the given value
     * @method
     * @param {Date} dt The Date and time to set the field's internal base date to.
     */
    setDateTime: function(dt) {
        this.setTheDate(dt);
        this.setTheTime(dt);
    },
    
    /**
     * Gets the field's formatted time value.
     * @method
     * @return {String} The formatted time.
     */
    getValue: function() {
        var v = RP.ui.TimeField.superclass.getValue.call(this);
        return this.formatTime(this.parseValue(v)) || '';
    },
    
    /**
     * Gets the Date object the field, created by combining the field's
     * date object with specified time value.
     * @method
     * @return {Date}
     */
    getDateTime: function(raw) {
        if (raw) {
            return this.parseValue(this.getRawValue());
        }
        
        return this.parseValue(this.getValue());
    },
    
    /**
     * Combines the specified time value with field's internal date.
     * @method
     * @param {String/Number} value The value to combine.
     * @return {Date} A Date object with the specified time value.
     */
    parseValue: function(value) {
        if (typeof(value) === "string") {
            if (value && value.length > 0) {
                try {
                    var date = RP.core.IntervalJN.parseTimeAfterBase(value, this._theDate);
                    if (this._roundMinutes !== 0) {
                        date.round(this._roundMinutes);
                    }
                    
                    return date;
                } 
                catch (e) {
                    logger.logError(new RP.Exception("[TimeField] parseValue failed for: " + value, e));
                    //this.markInvalid();
                }
                return null;
            }
            else {
                return null;
            }
        }
        else 
            if (!Ext.isEmpty(value)) {
                return value.round(this._roundMinutes);
            }
            else {
                return null;
            }
    },
    
    /**
     * Formats the time using the TimeField's internal format.
     * @method
     * @param {Object} t The Date object.
     * @return {String} The formatted string, empty if an invalid date was passed.
     */
    formatTime: function(t) {
        if (t) {
            return t.clone().formatTime(this._format);
        }
        else {
            return "";
        }
    }
});

Ext.reg("rpuitimefield", RP.ui.TimeField);
//////////////////////
// ..\js\ui\SessionExpiredDialog.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.SessionExpiredDialog
 * @extends Ext.Window
 * 
 * A dialog window that is displayed when the user's session has
 * expired.  The user then has the ability to either go back to the
 * login page, or attempt to log back in.  Instantiation of this
 * component will automatically mark the user's session as requiring
 * re-authentication.
 */
RP.ui.SessionExpiredDialog = Ext.extend(Ext.Window, {
    id: "sessionExpiredDialog",
    
    /**
     * @private
     */
    initComponent: function() {
        
        // Fire session expired event
        RP.event.AppEventManager.fire(RP.upb.AppEvents.SessionExpired, {});
        
        // allows enter key to submit the login form
        var enterKeyFn = (function(field, e) {
            if (e.getKey() == e.ENTER) {
                this.submit();
            }
        }).createDelegate(this);
        
        this.msgBoxOptions = this._createMsgBoxOptions();
        
        // handler used to submit the login form    
        Ext.apply(this, {
            title: RP.getMessage("rp.common.login.InactivityTimeoutTitle"),
            width: 450,
            draggable: false,
            closable: false,
            resizable: false,
            padding: 20,
            bodyStyle: "background-color: #fff",
            items: [{
                xtype: "box",
                style: "font-size: 12px;",
                html: RP.getMessage("rp.common.login.InactivityTimeout")
            }, {
                xtype: "form",
                itemId: "form",
                style: "margin: 20px auto",
                labelWidth: 75,
                defaults: {
                    labelStyle: "font-weight: bold"
                },
                width: "60%",
                border: false,
                items: [{
                    xtype: "textfield",
                    itemId: "userName",
                    fieldLabel: RP.getMessage("rp.common.login.Username"),
                    listeners: {
                        "specialkey": enterKeyFn
                    },
                    style: {
                        marginTop: "1px"
                    },
                    anchor: "99%"
                }, {
                    xtype: "textfield",
                    itemId: "password",
                    fieldLabel: RP.getMessage("rp.common.login.Password"),
                    inputType: "password",
                    listeners: {
                        "specialkey": enterKeyFn
                    },
                    style: {
                        marginTop: "1px"
                    },
                    anchor: "99%"
                }]
            }, {
                itemId: "pnlButtons",
                border: false,
                buttonAlign: "center",
                buttons: [{
                    itemId: 'btnLogin',
                    text: RP.getMessage("rp.common.login.LoginButton"),
                    scope: this,
                    handler: this.submit.createDelegate(this)
                }, {
                    itemId: 'btnCancel',
                    text: RP.getMessage("rp.common.login.CancelButton"),
                    handler: RP.util.Helpers.logout
                }]
            }]
        });
        
        RP.ui.SessionExpiredDialog.superclass.initComponent.call(this);
    },
    
    destroy: function() {
        Ext.EventManager.removeResizeListener(this.center, this);
        
        RP.ui.SessionExpiredDialog.superclass.destroy.apply(this, arguments);
    },
    
    show: function() {
        Ext.EventManager.onWindowResize(this.center, this);
        
        RP.ui.SessionExpiredDialog.superclass.show.apply(this, arguments);
        
        // attempt to focus the userName field after the form is shown
        this.getComponent("form").getComponent("userName").focus(false, 100);
    },
    
    /**
     * Submits login credentials entered by the user to re-authenticate a session.
     * @method
     */
    submit: function() {
        var userName = this.getComponent("form").getComponent("userName").getValue();
        var password = this.getComponent("form").getComponent("password").getValue();
        
        if (Ext.isEmpty(userName) || Ext.isEmpty(password)) {
            this._displayAlertMessageBox(RP.getMessage("rp.common.login.LoginFailed"));
            return;
        }
        
        if (userName.toUpperCase() !== RP.globals.getValue("USERNAME").toUpperCase()) {
            this._displayAlertMessageBox(RP.getMessage("rp.common.login.InvalidUser"));
            return;
        }
        
        // show a load mask while the ajax call is running
        this.loadMask = new Ext.LoadMask(this.body, {
            msg: RP.getMessage("rp.common.misc.PleaseWait")
        });
        
        this.loadMask.show();
        
        // perform a request to attempt to re-authenticate the user
        Ext.Ajax.requestWithTextParams({
            url: RP.core.PageContext.getPageUrl() + RP.globals.getPath("REAUTHENTICATE"),
            params: Ext.urlEncode({
                loginName: userName,
                password: password
            }),
            method: "POST",
            scope: this,
            callback: this._loginCallback
        });
    },
    
    /**
     * Creates the message box's options to be used for the Invalid User/Login Failed alerts.
     * @private
     */
    _createMsgBoxOptions: function() {
        return {
            modal: true,
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.ERROR,
            minWidth: 300
        };
    },
    
    /**
     * Displays an alert message box to the user.
     * @param {String} message The message to display in the message box.
     * @private
     */
    _displayAlertMessageBox: function(message) {
        Ext.Msg.show(Ext.apply(this.msgBoxOptions, {
            msg: message,
            cls: "aboveExpiredMask"
        }));
    },
    
    /**
     * Displays an alert message box with a message appropriate to the exception.
     * @param {Number} status The status number of the exception.
     * @private
     */
    _displayExceptionMessage: function(status) {
        var message = "";
        
        switch(status) {
            case RP.REFSExceptionCodes.REAUTH_USERNAME_MISMATCH_EXCEPTION:
                message = RP.getMessage("rp.common.login.InvalidUser"); break;
            case RP.REFSExceptionCodes.NOT_AUTHENTICATION_EXCEPTION:
                message = RP.getMessage("rp.common.login.LoginFailed"); break;
            case RP.REFSExceptionCodes.LOGIN_ATTEMPTS_EXCEEDED_EXCEPTION:
                message = RP.getMessage("rp.common.exception.LOGIN_ATTEMPTS_EXCEEDED_EXCEPTION"); break;
            case RP.REFSExceptionCodes.SERVER_COMMUNICATION_FAILURE:
                message = RP.getMessage("rp.common.exception.SERVER_COMMUNICATION_FAILURE"); break;
            case RP.REFSExceptionCodes.SITE_NOT_ACTIVE_EXCEPTION:
                message = RP.getMessage("rp.common.exception.SITE_NOT_ACTIVE_EXCEPTION"); break;
            default:
                message = RP.getMessage("rp.common.login.LoginFailed"); 
        }
        
        logger.logInfo("[SessionExpiredLoginDialog] Exception caught trying to re-authenticate user; response status: " + status);
        this._displayAlertMessageBox(message);
    },
    
    /**
     * Handles a result from a request for reauthentication.
     * @private
     */

    _loginCallback: function(options, success, response) {
        // parse the response
        var relogin = false;
        
        try {
            var r = Ext.util.JSON.decode(response.responseText); 
            this.loadMask.hide();
            if (r.status === RP.REFSExceptionCodes.OK) {
                this.destroy();
            }
            else {
                this._displayExceptionMessage(r.status);
            }
        } 
        catch (err) {
            // Stuff coming back isn't JSON (i.e., is Login page HTML)...
            
            // This most likely as a result of the REFS session expiring, so
            // try to authenticate again using the same credentials.  This
            // would create a new REFS session, and the need to reload the page.
            if (RP.globals.getPath("LOGIN_SERVICE")) {
                this._reauthenticateUsingLoginPage();
            }
            else {
                relogin = true;
            }
        }
        
        if (relogin) {
            alert(RP.getMessage("rp.common.login.SessionError")); // don't use Ext.Msg.alert(), as it returns right away...
            RP.util.Helpers.redirectToLogin();
            return;
        }
        
        // Fire session reauthenticated event
        RP.event.AppEventManager.fire(RP.upb.AppEvents.SessionReauthenticated, {});
    },
    
    /**
     * Submits for reauthentication using the Login Service.
     * @private
     */
    _reauthenticateUsingLoginPage: function() {
        var userName = this.getComponent("form").getComponent("userName").getValue();
        var password = this.getComponent("form").getComponent("password").getValue();
        var params = {
            loginName: userName,
            password: password
        };
        
        Ext.Ajax.requestWithTextParams({
            url: RP.globals.getFullyQualifiedPath('LOGIN_SERVICE'),
            method: "POST",
            params: params,
            scope: this,
            disableExceptionHandling: true,
            callback: this._reauthUsingLoginPageCallback
        });
    },
    
    /**
     * Handles a reathentication request from the Login Service.
     * @private
     */
    _reauthUsingLoginPageCallback: function(options, success, response) {
        this.loadMask.hide();
        var result;
        try {
            if(Ext.isEmpty(response.responseText)) {
                RP.throwError("Empty responseText");
            }
            result = Ext.util.JSON.decode(response.responseText);
        }
        catch(ex) {
            this._displayAlertMessageBox(RP.getMessage("rp.common.login.InvalidResponse"));
            return;
        }
        if (result.status === RP.REFSExceptionCodes.OK && success) {
            // eliminate the task dirty check to avoid
            // stopping the user from starting a new session
            // and continue on with establishing a brand new session
            window.onbeforeunload = null;
            RP.util.Helpers.redirectToLogin();
        }
        else {
            this._displayExceptionMessage(result.status);
        }
    }
});
//////////////////////
// ..\js\ui\SmartTimeLogic.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class SmartTimeLogic
 * <p> This is designed to handle certain logic based on having two associated start and stop
 * TimeFields. The stop TimeField will be updated based on the start TimeField. The main case
 * that this handles is if the end time will cross from AM to PM or vice versa.</p>
 *
 * <p> Here are some examples:
 * <ul>
 * <li> Start time: 8a, end time: 5 -> end time changes to 5pm</li>
 * <li> Start time: 8p, end time: 5 -> end time changes to 5am</li>
 * <li> Start time: 1a, end time: 10 -> end time changes to 10am</li>
 * <li> Start time: 1p, end time: 10 -> end time changes to 10pm</li>
 * <li> Start time: 10a, end time: 10 -> end time changes to 10pm</li>
 * <li> Start time: 10p, end time: 10 -> end time changes to 10am</li>
 * <li> Start time: 10a, end time: 10:15 -> end time changes to 10:15am</li>
 * </ul>
 * </p>
 *
 * @namespace RP.ui
 */
RP.ui.SmartTimeLogic = function(startTimeField, stopTimeField) {

    stopTimeField.on("beforechange", function() {
        if (!hasMeridiemIdentifier(stopTimeField.typedValue)) {
            calculateStopTime();
        }
    });
    
    /**
     * Calculates the stop time based on what is given as the start time. This is dependant
     * on the fact that the RP.ui.TimeField's parsing will default a raw number to the AM value.
     * @method
     */
    calculateStopTime = function() {
        var startTime = startTimeField.getDateTime();
        var stopTime = stopTimeField.getDateTime();
        
        if (Ext.isEmpty(startTime) || Ext.isEmpty(stopTime)) {
            return;
        }
        
        var MERIDIEM_BOUNDARY = 12;
        var startHours = startTime.getHours();
        var stopHours = stopTime.getHours();
        var startMinutes = startTime.getMinutes();
        var stopMinutes = stopTime.getMinutes();
        
        // Check if hours are equal or are 12 hours apart
        if (startHours == stopHours || startHours - MERIDIEM_BOUNDARY == stopHours) {
            if (startMinutes == stopMinutes && isAM(startHours, MERIDIEM_BOUNDARY)) {
                stopTimeField.setTheTime(stopTime.clone().addHours(MERIDIEM_BOUNDARY));
            }
            // Check to see if there are minutes separating the hours and the hours
            // and it's in the PM, we care about this because the TimeField will default
            // to AM so we need to correct it to PM. 
            else 
                if (stopMinutes > startMinutes && isPM(startHours, MERIDIEM_BOUNDARY)) {
                    stopTimeField.setTheTime(stopTime.clone().addHours(MERIDIEM_BOUNDARY));
                }
        }
        // Check for case when we want to adjust from AM to PM (e.g. start: 8am, end: 5(am))
        else 
            if (stopHours < startHours) {
                if (isAM(startHours, MERIDIEM_BOUNDARY)) {
                    stopTimeField.setTheTime(stopTime.clone().addHours(MERIDIEM_BOUNDARY));
                }
                // Check if we aren't crossing an AM/PM boundary when in the PM 
                // (e.g. start: 1pm, end 10(am) -> 10pm)
                else 
                    if (startHours - MERIDIEM_BOUNDARY < stopHours) {
                        stopTimeField.setTheTime(stopTime.clone().addHours(MERIDIEM_BOUNDARY));
                    }
            }
    };
    
    /**
     * Check to see if a timeString has an AM/PM indicator (e.g. 10a, 4pm, etc)
     * @method
     * @param {Object} timeString A string that could contain an AM/PM indicator.
     * @return {Boolean}
     */
    hasMeridiemIdentifier = function(timeString) {
        var hasIdentifier = false;
        if (Ext.isEmpty(timeString)) {
            return hasIdentifier;
        }
        timeString = timeString.toLowerCase();
        hasIdentifier = (timeString.indexOf("a") !== -1) || (hasIdentifier = timeString.indexOf("p") !== -1) ? true : false;
        return hasIdentifier;
    };
    
    /**
     * Determines if the specified hour is AM
     * @method
     * @param {Number} hour The hour value (0-23)
     * @param {Number} meridiemBoundary The number of hours marking the difference
     * between am and pm.
     * @return {Boolean}
     */
    isAM = function(hour, meridiemBoundary) {
        return hour < meridiemBoundary;
    };
    
    /**
     * Determines if the specified hour is PM
     * @method
     * @param {Number} hour The hour value (0-23)
     * @param {Number} meridiemBoundary The number of hours marking the difference
     * between am and pm.
     * @return {Boolean}
     */
    isPM = function(hour, meridiemBoundary) {
        return hour >= meridiemBoundary;
    };
};
//////////////////////
// ..\js\ui\StoreBoundComponents\TimeTextField.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @namespace RP.ui
 * @class StoreBoundTimeTextField
 * @extends Ext.form.TextField
 */
RP.ui.StoreBoundTimeTextField = Ext.extend(Ext.form.TextField,
{

  /**
   * @method
   */
  initComponent: function()
  {
    var config = this.initialConfig;

    this.on("change", function(field, newVal, oldVal)
    {
      var baseDateDelegate = config.baseDateHandler.createDelegate(config.scope || this, [config.record, config.fieldName]);
      var baseDate = baseDateDelegate();

      var fieldDate = null;
      if (newVal)
      {
        try
        {
          fieldDate = IntervalJN.parseTimeAfterBase(newVal, baseDate);
        }
        catch (e)
        {
          Ext.Msg.alert('', 'Please enter a valid time.');
          this.setRawValue(oldVal);
          return;
        }
      }

      var dummyDate = new Date();
      var curDate = config.record.get(config.fieldName);
      
      if (newVal === '' && !curDate)
      {
       this.setRawDate(null, this.initialConfig.emptyText);
       return;
      }
      
      // find out if the field is different from what's in the store.
      if (!(curDate || dummyDate).equals(fieldDate || dummyDate))
      {
        config.record.set(config.fieldName, fieldDate);
        this.addClass("rp-dirty-cell");
      }

      this.setRawDate(fieldDate);
    }, this);

    Ext.apply(this, config);
    
    if (!config.emptyText) {
      config.emptyText = RP.getMessage("rp.common.misc.EmptyTimeText");
    }

    RP.ui.StoreBoundTimeTextField.superclass.initComponent.call(this);

  },

  /**
   * @method
   * @param {Object} ct
   * @param {Object} position
   */
  onRender: function(ct, position)
  {
    RP.ui.StoreBoundTimeTextField.superclass.onRender.call(this, ct, position);
  },

  /**
   * @method
   * @param {Object} v
   */
  setValue: function(v)
  {
    RP.ui.StoreBoundTimeTextField.superclass.setValue.apply(this, arguments);
    this.fireEvent('change', this, v);
  },

  /**
   * @method
   * @param {Object} fieldDate
   * @param {Object} defaultText
   */
  setRawDate: function(fieldDate, defaultText)
  {
    if (fieldDate)
    {
      this.setRawValue(fieldDate.formatTime(this.initialConfig.timeFormat));
    }
    else if (defaultText)
    {
      this.setRawValue(defaultText);
    }
    else
    {
      this.setRawValue("");
    }
  }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\StoreBinder.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class StoreBinder
 * @namespace RP.ui
 * @extends Ext.Component
 */
RP.ui.StoreBinder = Ext.extend(Ext.Component, {
    _store: null,
    _record: null,
    _valueField: null,
    _emptyText: null,
    _control: null,
    
    _dirtyCellCls: "rp-dirty-cell",
    
    /**
     * @private
     */
    initComponent: function() {
        var storeBindings = this.initialConfig.storeBindings;
        this._store = storeBindings.store || null;
        this._record = storeBindings.record || null;
        this._valueField = storeBindings.valueField;
        this._emptyText = storeBindings.emptyText;
        this._control = this.initialConfig.control;
        
        if (!this._valueField) {
            if (this.name) {
                this._valueField = this.name;
            }
            else {
                throw new RP.Exception('fieldname is required for a store bound control.');
            }
        }
        
        if (!this._store && !this._record) {
            throw new RP.Exception('either store or record must be provided for a store bound control');
        }
        
        if (this._store) {
            this._store.on('load', this.onStoreLoaded, this);
            // reg failure event
        }
        
        if (!this._control) {
            throw new RP.Exception('store bound control requires a control to be set');
        }
        
        this._oldControlSetValue = this._control.setValue;
        this._control.setValue = function(newVal) {
            this._storeBinder.setValue(newVal);
        };
        
        if (Ext.isFunction(this._control.initValue)) {
            // This is so that the dirty flag isn't potentially set on initial value set
            // on prepopulated stores...
            this._control.initValue = this._control.initValue.createSequence(function() {
                this.removeClass(this._dirtyCellCls);
            }, this._control);
        }
        
        this._oldControlClearValue = this._control.clearValue;
        this._control.clearValue = function() {
            this._storeBinder.clearValue();
        };
        
        this._control.on('change', this.onControlChange, this);
        
        if (this._store.getCount() > 0) {
            this.onStoreLoaded();
        }
        
        RP.ui.StoreBinder.superclass.initComponent.call(this);
    },
    
    /**
     * @method
     */
    getStoreValue: function() {
        return this._record.get(this._valueField);
    },
    
    /**
     * @method
     * @param {Object} newVal
     */
    setValue: function(newVal) {
        this.onControlChange(this._control, newVal, this._control.getValue());
    },
    
    /**
     * @method
     */
    clearValue: function() {
        if (this._oldControlClearValue) {
            this._oldControlClearValue.apply(this._control, []);
        }
        this._control.setRawValue('');
        this._control.removeClass(this._dirtyCellCls);
    },
    
    /**
     * @method
     */
    onStoreLoaded: function(/*store, records, options*/) {
        if (this._store.getCount()) {
            this._record = this._store.getAt(0);
            this.setValue(this._record.get(this._valueField));
            this._control.removeClass(this._dirtyCellCls);
        }
    },
    
    /**
     * @method
     * @param {Object} field
     * @param {Object} newVal
     * @param {Object} oldVal
     */
    onControlChange: function(field, newVal, oldVal) {
        var formattedVal = newVal;
        var curVal = this._record.get(this._valueField);
        
        if (this._control.formatValue) {
            try {
                formattedVal = this._control.formatValue(newVal);
            } 
            catch (ex) {
                Ext.Msg.alert('', ex.message);
                this._control.setRawValue(curVal);
                return;
            }
        }
        
        var parsedVal = newVal;
        if (this._control.parseValue) {
            parsedVal = this._control.parseValue(newVal);
        }
        
        var areDifferent = true;
        
        if (Ext.isDate(curVal)) {
            if (Ext.isString(parsedVal)) {
                parsedVal = new Date(parsedVal);
            }
            areDifferent = !curVal.equals(parsedVal);
        }
        else {
            areDifferent = curVal !== parsedVal;
        }
        
        if (this._record && areDifferent) {
            this._record.set(this._valueField, parsedVal);
            if (formattedVal !== oldVal) {
                this._control.addClass(this._dirtyCellCls);
            }
        }
        
        if (formattedVal === "" && this._emptyText) {
            formattedVal = this._emptyText;
        }
        
        this._oldControlSetValue.apply(this._control, [formattedVal]);
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\TimeField.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.StoreBoundTimeField
 * @extends RP.ui.TimeField
 */
RP.ui.StoreBoundTimeField = Ext.extend(RP.ui.TimeField,
{
  _storeBinder: null,

  initComponent: function()
  {
    this.initialConfig.control = this;
    this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);

    this.initialConfig.storeBindings.store.on("load", function(store, recs)
    {
      this.setTheDate(this._storeBinder.getStoreValue());
    }, this);


    RP.ui.StoreBoundTimeField.superclass.initComponent.call(this);
  },

  formatValue: function(newVal)
  {
    if (typeof (newVal) === "string")
    {
      return newVal;
    }

    if (newVal)
    {
      return newVal.clone().formatTime(this._format);
    }
    else
    {
      return "";
    }
  }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\TextField.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.StoreBoundTextField
 *
 * 
 * 
 * @namespace RP.ui
 * @extends Ext.form.TextField
 */
RP.ui.StoreBoundTextField = Ext.extend(Ext.form.TextField, {
    _storeBinder: null,
    
    /**
     * @private
     */
    initComponent: function() {
        this.initialConfig.control = this;
        this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);
        
        RP.ui.StoreBoundTextField.superclass.initComponent.call(this);
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\TextArea.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.StoreBoundTextArea
 * @extends Ext.form.TextArea 
 */
RP.ui.StoreBoundTextArea = Ext.extend(Ext.form.TextArea,
{
  _storeBinder: null,

  initComponent: function()
  {
    this.initialConfig.control = this;
    this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);

    RP.ui.StoreBoundTextArea.superclass.initComponent.call(this);
  }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\DateField.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.StoreBoundDateField
 * @extends Ext.form.DateField
 */
RP.ui.StoreBoundDateField = Ext.extend(Ext.form.DateField,
{
  _storeBinder: null,

  initComponent: function()
  {
    this.initialConfig.control = this;
    this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);

    this._format = this.initialConfig.format || RP.core.Formats.Date.Medium;
    
    if (!this.initialConfig.emptyText) {
      Ext.apply(this, {
        emptyText: RP.getMessage("rp.common.misc.EmptyDateText")
      });
    }

    RP.ui.StoreBoundDateField.superclass.initComponent.call(this);
  },

  formatValue: function(newVal)
  {
    if (Ext.isString(newVal))
    {
      if (Ext.isEmpty(newVal))
      {
        return "";
      }
      return((new Date(newVal)).clearTime().formatDate(this._format));
    }
    else if (newVal)
    {
      return newVal.clone().clearTime().formatDate(this._format);
    }
    else
    {
      return "";
    }
  }

});
//////////////////////
// ..\js\ui\StoreBoundComponents\DisplayField.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.StoreBoundDisplayField
 * @extends Ext.form.DisplayField
 */
RP.ui.StoreBoundDisplayField = Ext.extend(Ext.form.DisplayField,
{
  _storeBinder: null,

  initComponent: function()
  {
    this.initialConfig.control = this;
    this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);

    RP.ui.StoreBoundDisplayField.superclass.initComponent.call(this);
  }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\ComboBoxStoreBinder.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.ComboBoxStoreBinder
 * @extends RP.ui.StoreBinder
 */
RP.ui.ComboBoxStoreBinder = Ext.extend(RP.ui.StoreBinder,
{
  _displayField: null,

  initComponent: function()
  {
    var storeBindings = this.initialConfig.storeBindings;
    this._displayField = storeBindings.displayField || null;

    RP.ui.ComboBoxStoreBinder.superclass.initComponent.call(this);
  },

  onControlChange: function(field, newVal, oldVal)
  {
    RP.ui.ComboBoxStoreBinder.superclass.onControlChange.call(this, field, newVal, oldVal);

    if (this._displayField && this._control.store && this._control.valueField && this._control.displayField)
    {
      var displayVal = null;
      
      if (this._control.store.getCount() === 0)
      {
        displayVal = this._record.get(this._displayField);
        this._control.setRawValue(displayVal);
        this._control.lastSelectionText = displayVal;
        return;
      }

      var recIndex = this._control.store.find(this._control.valueField, newVal);
      if (recIndex >= 0)
      {
        var rec = this._control.store.getAt(recIndex);
        displayVal = rec.get(this._control.displayField);
        this._record.set(this._displayField, displayVal);
      }
    }
  }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\ComboBox.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.StoreBoundComboBox
 * @extends Ext.form.ComboBox
 */
RP.ui.StoreBoundComboBox = Ext.extend(Ext.form.ComboBox,
{
  _storeBinder: null,

  previousSelectedValue: null,

  initComponent: function()
  {
    this.initialConfig.control = this;
    if (this.initialConfig.storeBindings)
    {
      this._storeBinder = new RP.ui.ComboBoxStoreBinder(this.initialConfig);

      var okToLookup = false;
      if (this.initialConfig.storeBindings.store)
      {
        this.initialConfig.storeBindings.store.on("load",
        function(store, recs)
        {
          if (recs.length > 0)
          {
            this.previousSelectedValue = recs[0].get(this.initialConfig.valueField);
            okToLookup = false;
          }
        },
        this);
      }
    }
    
    this.on("beforeselect",
      function(combo, rec, index)
      {
        if (okToLookup)
        {
          this.previousSelectedValue = this.getValue();
        }
        else
        {
          okToLookup = true;
        }
      },
      this);

    RP.ui.StoreBoundComboBox.superclass.initComponent.call(this);
  }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\StoreBoundXTypes.js
//////////////////////
/*
 * Bind xtypes to the actual classes implementing the Store Bound Control Components.
 */
RP.core.ComponentMgr.register('rpstorebounddatefield', RP.ui.StoreBoundDateField);
RP.core.ComponentMgr.register('rpstoreboundcombobox', RP.ui.StoreBoundComboBox);
RP.core.ComponentMgr.register('rpstoreboundtextarea', RP.ui.StoreBoundTextArea);
RP.core.ComponentMgr.register('rpstoreboundtimefield', RP.ui.StoreBoundTimeField);
RP.core.ComponentMgr.register('rpstoreboundtextfield', RP.ui.StoreBoundTextField);
RP.core.ComponentMgr.register('rpstoreboundtimetextfield', RP.ui.StoreBoundTimeTextField);
RP.core.ComponentMgr.register('rpstorebounddisplayfield', RP.ui.StoreBoundDisplayField);
//////////////////////
// ..\js\ui\GridRowStatus.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.ui");

/**
 * @class GridRowStyle
 * Used by RP.ui.GridView to style a grid row and/or to display an icon to
 * indicate a row status. Essentially an Enumeration class for Grid Row Status.
 * 
 * @namespace RP.ui
 * @singleton
 */
RP.ui.GridRowStyle = function(){
  var classMap = {
    None: {
      status: 0,
      cls: ""
    },
    Resolved: {
      status: 1,
      cls: "rp-grid-row-resolved"
    },
    Deleted: {
      status: 2,
      cls: "rp-grid-row-deleted"
    },
    Edited: {
      status: 3,
      cls: "rp-grid-row-edited"
    },
    Important: {
      status: 4,
      cls: "rp-grid-row-important"
    },
    UserDefinedBegin: {
      status: 100,
      cls: ""
    }
  };
  
  return {
    /**
     * Returns a numerical representation of the name
     * of the status passed in by the user.
     * @method
     * @param {String} name The status for which to get the numerical value.
     * @return {Number} The numerical representation of the status name.
     */
    getStatus: function(name){
      return classMap[name].status;
    },
    
    /**
     * Returns the numerical representation of the default
     * status.
     * @method
     * @return {Number} The numerical representation of the default status.
     */
    getDefaultStatus: function(){
      return classMap.None.status;
    },
    
    /**
     * Returns a css class name of the status passed in by the user.
     * @method
     * @param {Number} status The status for which to get the css class.
     * @return {String} The css class of the status.
     */
    getClassByStatus: function(status){
      for (var name in classMap) {
        if (classMap[name].status == status) {
          return classMap[name].cls;
        }
      }
      return classMap.None.cls;
    },
    
    /**
     * Returns the class name of the status passed in by the user.
     * @method
     * @param {String} name The status for which to get the class name.
     * @return {String} The class name of the status.
     */
    getClassByName: function(name){
      if (classMap[name]) {
        return classMap[name].cls;
      }
      else {
        return classMap.None.cls;
      }
    }
  };
}();

// 
RP.ui.GridRowStatus = {
  None: RP.ui.GridRowStyle.getStatus("None"),
  Resolved: RP.ui.GridRowStyle.getStatus("Resolved"),
  Deleted: RP.ui.GridRowStyle.getStatus("Deleted"),
  Edited: RP.ui.GridRowStyle.getStatus("Edited"),
  Important: RP.ui.GridRowStyle.getStatus("Important"),
  
  UserDefinedBegin: 100
};
//////////////////////
// ..\js\ui\GridView.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.ui");

/**
 * @class GridView
 * Extends the standard Ext GridView to perform additional things such as
 * styling a row to indicate it as was changed, etc.
 *
 * @namespace RP.ui
 * @extends Ext.grid.GridView
 */
RP.ui.GridView = Ext.extend(Ext.grid.GridView, {
  /**
   * @method
   * @param {Object} config The configuration object.
   */
  constructor: function(config) {
    this.config = config ||
    {};
    RP.ui.GridView.superclass.constructor.apply(this, arguments);
  },
  
  /**
   * Override private function that's called when a reference to the grid is available.
   * @method
   */
  init: function() {
    RP.ui.GridView.superclass.init.apply(this, arguments);
    
    if (this.config.statusIcon === true) {
      this._attachIconColumn();
      
      // Listen to reconfigure event to add icon column again if cm changes.   
      this.grid.on("reconfigure", this._attachIconColumn, this);
    }
  },
  
  /**
   * Override to apply empty text if store is already loaded...  The base 'deferEmptyText'
   * option could be used, but that doesn't take into account whether or not the store
   * is already loaded...
   * @method
   */
  afterRender: function() {
    RP.ui.GridView.superclass.afterRender.apply(this, arguments);
    
    if (this.grid.store.isLoaded) {
      this.applyEmptyText();
    }
    
    // for state stuff.
    this.grid.initState();
    
  },
  
  /**
   * Returns the css class for a row in a grid
   * @method
   * @param {Object} record The record for which row to get the css class from.
   * @param {int} index The index of the record to the css class from.
   * @param {Object} rowParams Parameters for the row.
   * @param {Object} store The store which the record pertains to.
   */
  getRowClass: function(record, index, rowParams, store) {
    var defaultRowStyleClassFn = function(status, record) {
      return RP.ui.GridRowStyle.getClassByStatus(status);
    };
    
    var status = this.config.getRowStatusFn ? this.config.getRowStatusFn(record, index) : RP.ui.GridRowStyle.getDefaultStatus();
    var cls = (this.config.getRowStyleClassFn || defaultRowStyleClassFn)(status, record);
    
    return cls + (this.config.extraRowCls ? (" " + this.config.extraRowCls) : "");
  },
  
  /**
   * @private
   * Attaches an icon column to the column model.
   */
  _attachIconColumn: function() {
    var cm = this.grid.colModel;
    var newCols = [{ // A column styled with the "rp-view-grid-detail" class is required for grid row indicators
      dataIndex: "__statusiconplaceholder__",
      header: " ",
      id: "iconColumn",
      menuDisabled: true,
      width: 22,
      sortable: false,
      hidden: false,
      hideable: false,
      editable: false,
      renderer: function() {
        return '<div class="rp-view-grid-detail">&nbsp;</div>';
      },
      destroy: Ext.emptyFn // nothing to do - no editor or other resources to destroy
    }];
    
    cm.setConfig(cm.config.concat(newCols), true);
    
  },
  
  /**
   * Override of parent function. Hides the header if there is no text to display.
   * @private
   */
  applyEmptyText: function() {
    RP.ui.GridView.superclass.applyEmptyText.apply(this, arguments);
    
    if (this.emptyText && !this.hasRows()) {
      this.mainHd.hide(false);
    }
    else {
      this.mainHd.show(false);
    }
  }
});
//////////////////////
// ..\js\ui\GridPanel.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class RP.ui.GridPanel
 * @extends Ext.grid.GridPanel
 * <p>An RP extension of the {@link Ext.grid.GridPanel} providing additional functionality
 * such as printing, export, and tooltips.</p>
 * <p><b><u>Cell Tooltips</u></b></p>
 * <p>Tooltip support has been added, which is enabled on a per-column basis.  To enable,
 * set <pre><code>cellTooltip: true</code></pre> on each column in the column model that
 * should display a tooltip.</p>
 * <p>Next, listen for the grid's <b>beforetooltipshow</b> event, and update the grid's tooltip
 * property reference to display the desired contents/markup.  Example:<pre><code>
myGrid.on("beforetooltipshow", function(grid, row, cell) {
    var record = grid.getStore().getAt(row);
    
    grid.tooltip.update("<-- " + record.get("some_field"));
}, this);
 * </code></pre></p>
 * <p>Additional configurations, such as CSS classes, can be added to the grid's internal
 * tooltip via the {@link #tooltipConfig} option.</p>
 * @constructor
 * @param {Object} config The config object
 * @xtype rpgridpanel
 */
RP.ui.GridPanel = Ext.extend(Ext.grid.GridPanel,
{
  /**
   * @cfg {Boolean} stripeRows See {@link Ext.grid.GridPanel#stripeRows}, defaulted to true.
   */
  stripeRows: true,
  
  /**
   * @cfg {Boolean} enableExport True to enable CSV export (default is false).
   */
  enableExport: false,
  
  /**
   * @cfg {Boolean} enableExportPdf True to enable PDF export (default is false).
   */
  enableExportPdf: false,
  
  /**
   * @cfg {Boolean} enablePrint True to enable printing functionality (default is false).
   */
  enablePrint: false,
  
  /**
   * @cfg {Boolean} enableRefresh True to enable store reload functionality (default is false).
   */
  enableRefresh: false,
  
  /**
   * @cfg {Function} printOptionsDelegate A delegate that gets called before actual printing.
   */
  
  /**
   * @cfg {String} printTitle The page title for printing the grid (default is "").
   */
  printTitle: "",
  
  /**
   * @cfg {Object} tooltipConfig An optional configuration object used when creating the grid's
   * internal tooltip property, supports all configurations specified by {@link Ext.ToolTip}.
   */
  
  // private
  initComponent: function() {
    var toolbar = this.tbar;
    
    // If Exports, Print, or Refresh enabled...
    if (this.enableExport || this.enableExportPdf || this.enablePrint || this.enableRefresh) {
      // Create the necessary tools (icons in toolbar).
      var tools = [];
      
      if (this.enableRefresh)
      {
        tools.push({id: "refresh", handler: this.reloadGrid, scope: this });
      }
      
      if (this.enableExport)
      {
        tools.push({id: "save", handler: this.exportGridAsCSV, scope: this });
      }

      if (this.enableExportPdf)
      {
        tools.push({id: "pdf", handler: this.exportGridAsPDF, scope: this });
      }
      
      if (this.enablePrint)
      {
        tools.push({id: "print", handler: this.printGrid, scope: this });
      }
      
      // Create panel to hold tools.
      var toolbarPanel = {
        xtype: "panel",
        headerCfg: {}, // this will get rid of all borders, etc.
        bodyCfg: {},
        tools: tools,
        width: tools.length * 20 // provides horizontal space so tools do not stack vertically
      };
      
      // If the grid already has a toolbar, add it to the "Right" panel, else create
      // a brand new toolbar to host the tools.
      if (toolbar)
      {
        // Verify it is an instance of RP.ui.Toolbar and add the tool(s) to the right.
        if (typeof(toolbar.addItemsToRightBar) !== "function")
        {
          throw new RP.Exception("Toolbar type is not supported.  Toolbar must have 'addItemsToRightBar' function.");
        }
        toolbar.addItemsToRightBar([toolbarPanel]);
      }
      else
      {
        toolbar = new RP.ui.Toolbar(
        {
          rightItems: [toolbarPanel]
        });
      }
    }
    
    Ext.applyIf(this, {
      tbar: toolbar
    });
    
    // since Ext.grid.GridPanel's prototype sets the view to null, applyIf
    // thinks it's already defined, so this has to happen separate.
    if (this.view === null) {
      // gridView and gridViewConfig are supported here for backwards compatibility
      this.view = this.gridView || new RP.ui.GridView(this.gridViewConfig || this.viewConfig);
    }
    
    RP.ui.GridPanel.superclass.initComponent.call(this);
  },
  
  onRender: function()
  {
    RP.ui.GridPanel.superclass.onRender.apply(this, arguments);

    /*
    Add Ext.toolTip support for grid cells. To use, set cellTooltip to true on
    columnModel and handle beforetooltipshow event with this signature: function(grid, row, col)
    */
    this.addEvents(
      /**
       * @event beforetooltipshow
       * Fires before the grid's ToolTip is shown.  Listen for this event and update the
       * grid's tooltip appropriately.  This is used in conjunction with "cellTooltip: true"
       * on columns that should display a ToolTip.
       * @param {RP.ui.GridPanel} this
       * @param {Number} row The row index.
       * @param {Number} cell The cell index.
       */
      "beforetooltipshow"
    );
    
    var defaultTooltipConfig = {
        renderTo: Ext.getBody(),
        target: this.view.mainBody,
        delegate: this.view.cellSelector,
        dismissDelay: 0,
        trackMouse: true,
        anchor: "top",
        listeners: {
            beforeshow: this._renderTooltip,
            scope: this
        }
    };
    
    /**
     * The grid's ToolTip, this property is not available until after the grid has been rendered.
     * @type Ext.ToolTip
     * @property
     */
    this.tooltip = new Ext.ToolTip(Ext.apply({}, this.tooltipConfig, defaultTooltipConfig));
  },
  
  _renderTooltip: function(tt) {
    var v = this.getView();
    var row = v.findRowIndex(tt.triggerElement);
    var cell = v.findCellIndex(tt.triggerElement);
    var colConfig = this.colModel.config[cell];

    if (colConfig !== undefined && colConfig.cellTooltip === true) {
      this.fireEvent("beforetooltipshow", this, row, cell);
      if (tt.body.dom.innerHTML === "") {
        tt.hide();
        return false;
      }
    }
    else {
      tt.hide();
      return false;
    }
  },

  /**
   * RP.interfaces.IPrintSource implementation that creates printable
   * markup from the grid's contents.
   * @return {String} HTML of printable grid.
   */
  getMarkupForPrinting: function()
  {
    var view = this.getView();
    var ts = view.templates;

    // backup the templates, so they can be reset after getting the print table
    var bak = {};
    bak.body = ts.body;
    bak.master = ts.master;
    bak.header = ts.header;
    bak.hcell = ts.hcell;
    bak.row = ts.row;
    bak.cell = ts.cell;
    
    var grpBak = {};
    grpBak.startGroup = view.startGroup;
    grpBak.endGroup = view.endGroup;

    ts.master = new Ext.Template('<table>',
                                  '{header}',
                                  '<tbody>',
                                    '{body}',
                                    '{summary}',
                                  '</tbody>',
                                '</table>');
    ts.header = new Ext.Template('<thead><tr>{cells}</tr></thead>\n');

    ts.hcell = new Ext.Template('<td style="{style}">', '{value}', "</td>");
    ts.row = new Ext.Template('<tr class="{alt}">{cells}</tr>\n');
    ts.cell = new Ext.Template('<td style="{style}" {cellAttr}>', '{value}', "</td>");

    var colCount = this.colModel.getColumnCount();
    view.startGroup = view.rpPrintGroupStart || new Ext.XTemplate('<tr><td class="rp-print-grid-group" colspan="' + colCount + '"><div>',
                    view.groupTextTpl,
                    '</div></td></tr>');
    view.endGroup = "";
    
    var cb = this._exportHeaderRow(true, ts.hcell);
    var header = ts.header.apply({cells: cb.join("")});
    var rows = this.view.renderRows();
    var body = ts.body.apply({ rows: rows });
    var summary = "";
    if (view.summary && view.summary.dom)
    {
      summary = this._getSummaryHTML(view.summary.dom, view);
    }

    var html = ts.master.apply(
    {
      header: header,
      body: body,
      summary: summary
    });

    view.templates = bak;
    view.startGroup = grpBak.startGroup;
    view.endGroup = grpBak.endGroup;

    return html;
  },
  
  /**
  * Pops up a window with the grid in printable format and display the Print dialog.
  */
  printGrid: function()
  {
    if (typeof(this.initialConfig.printOptionsDelegate) === "function")
    {
      var fn = function(options)
      {
        RP.core.CommonExtensions.printComponents(
        {
          title: this.printTitle,
          items: this
        },
        options);
      };
      
      this.initialConfig.printOptionsDelegate(fn.createDelegate(this));
    }
    else
    {
      RP.core.CommonExtensions.printComponents(
      {
        title: this.printTitle,
        items: this
      });
    }
  },
  
  /**
  * Reloads the data in the grid.
  */
  reloadGrid: function()
  {
    this.getStore().reload();
  },
  
  exportGridAsCSV: function() {
    var exportData = this._exportAsCSV();
    this._exportGrid(exportData, "CSV");
  },
  exportGridAsPDF: function() {
    var exportData = this._exportAsCSV();
    this._exportGrid(exportData, "PDF");
  },
  
  /**
   * Export the grid contents to server and have it handle sending
   * the data back to the browser in a digestable format that the user
   * requested.
   * @private
   */
  _exportGrid: function(exportData, type)
  {
    var postExportUrl = RP.globals.getValue('POST_EXPORT_URL');
    var getExportUrl = RP.globals.getValue('GET_EXPORT_URL');
    
    if (typeof(postExportUrl) === "undefined")
    {
      logger.logError("[GridPanel] RP.globals.POST_EXPORT_URL not defined");
      return;
    }
    
    if (typeof(getExportUrl) === "undefined")
    {
      logger.logError("[GridPanel] RP.globals.GET_EXPORT_URL not defined");
      return;
    }
    
    var baseUrl = RP.globals.getPath('BASE_URL');
    
    Ext.Ajax.request({
      url: String.format("{0}?type={1}", baseUrl + postExportUrl, type),
      method: 'POST',
      params: exportData.data,
      scope: this,
      headers: { 'Content-Type': exportData.contentType },
      success: function(res)
      {
        var key = res.responseText;
        window.open(baseUrl + getExportUrl + "/" + key, "rpexport", "width=20,height=20,resizable=yes");
      },
      failure: function(e)
      {
        //TODO
        //alert('Export failed.  Unable to post export data.');
        logger.logError("Export failed");
      }
    });
  },
  
  /**
   * Retrieve the data from the grid in CSV format to send
   * back to the server
   * @private
   */
  _exportAsCSV: function()
  {
    var headerRow = this._exportHeaderRow(true);

    var header = this._escapeColumns(headerRow); 
    var rows = this.getView().renderRowsForExport(0, this.getStore().getCount(), this._includeColumn);

    var summary = "";
    if (this.getView().summary && this.getView().summary.dom)
    {
      summary = this._getSummaryCSV(this.getView().summary.dom, this.getView());
    }

    return {
      data: String.format("{0}\n{1}\n{2}", header, rows, summary),
      contentType: "text/plain; charset=utf-8"
    };
  },

  /**
   * If the column is considered hidden, the column should not
   * be exported back to the server.
   * @private
   */
  _includeColumn: function(column) {
    if (typeof column.style === "string" && column.style.indexOf("display:none;") > -1) {
      return false;
    }
    else if (typeof column.style === "object" && column.style.display === "none") {
      return false;
    }
    
    return true;
  },
  
  /**
   * Export the header row columns into a simple array data structure.
   * @private
   */
  _exportHeaderRow: function(checkVisibility, cellTemplate) {
    var columnModel = this.colModel;
    var len = columnModel.getColumnCount();
    
    // loop through each column in the header.
    var cols = [];
    for (var i = 0; i < len; i++) {
      var column = {};
      column.id = columnModel.getColumnId(i) || "&nbsp;";
      column.value = columnModel.getColumnHeader(i) || " ";
      column.style = this.getView().getColumnStyle(i, true);
      
      if (cellTemplate) {
        cols.push(cellTemplate.apply(column));
      }
      else {
        cols.push(column);
      }
    }
    
    return this._exportRow(cols);
  },
  
  _escapeColumns: function(row) {
    var rowString =  row.join("\",\"");
    rowString = "\"" + rowString + "\"";
    
    return rowString;
  },
  
  _getSummaryHTML: function(summaryDOM, view)
  {
    var masterTemplate = new Ext.Template('<tr><table>','{body}','</table></tr>');
    var rowTemplate = "<tr>{0}</tr>";

    var rows = Ext.query("div.rp-grid3-summary-row tr", summaryDOM);
    var cols, rowHTML, colHTML, colStyle, colValue, i;
    rowHTML= [];
    for(var row = 0; row < rows.length; row++) {
      cols = Ext.query("div.x-grid3-cell-inner", rows[row]);
      colHTML = [];
      for (i = 0; i < cols.length; i++)
      {
        colStyle = view.getColumnStyle(i, true);
        colValue = cols[i].innerHTML;
        colHTML.push(String.format('<td style="{0}">{1}</td>', colStyle, colValue));
      }
      rowHTML.push(String.format(rowTemplate, colHTML.join("")));
    }

    return masterTemplate.apply({ body: rowHTML.join("") });
  },
  
  _getSummaryCSV: function(summaryDOM, view)
  {
    var masterTemplate = new Ext.Template('{body}');

    var cols = Ext.query("div.x-grid3-cell-inner", summaryDOM);
    var row = this._exportRow(cols);
    
    return masterTemplate.apply({body: this._escapeColumns(row)});
  },

  _exportRow: function(cols) {
    var row = [];
    
    for (var i = 0; i < cols.length; i++) {
      if (this._includeColumn(cols[i])) {
        if (cols[i].innerHTML) {
          row.push(cols[i].innerHTML);
        }
        else if (cols[i].value) {
          row.push(cols[i].value);
        }
        else {
          row.push(cols[i]);
        }
      }
    }
    
    return row;
  }
});

RP.iimplement(RP.ui.GridPanel, RP.interfaces.IPrintSource);
    
Ext.reg("rpgridpanel", RP.ui.GridPanel);
//////////////////////
// ..\js\ui\Panel.js
//////////////////////
/*global Ext, RP, logger, window, document */

Ext.ns('RP.ui');

/**
 * @class Panel
 * @namespace RP.ui
 * @extends Ext.Panel
 * @param {Object} config The configuration for the panel.
 */
RP.ui.Panel = Ext.extend(Ext.Panel, {
    /**
     * This method is used to retrieve the footer bar of the panel
     * @method 
     * @return {Element} Footer bar object
     */
    getFooterBar: function() {
        return this.fbar;
    }
});


Ext.reg("panel", RP.ui.Panel);
//////////////////////
// ..\js\ui\GridRowGroup.js
//////////////////////
/*global Ext, RP, window */

Ext.namespace("RP.ui");

/**
 * @classRP.ui.GridRowGroup
 * <p>An extension of Ext.grid.GroupingView that differs in the way
 * the grouping row is rendered.  The Ext grouping row is a separate single element that
 * takes up the entire width of the grid, and uses a grouping template to render itself.
 * This extension takes ONE row out of the group and renders it as the grouping header,
 * preserving all the columns of the grid and its UI state (sorting, show/hide of columns,
 * column ordering, etc.)</p>
 *
 * <p>By default, the first [sorted or unsorted] row of a group becomes the group header.
 * A config option is provided to allow the owner to override that behavior and specify the
 * row in the group to use as the grouping row.  In addition, another config option allows
 * the owner to hide certain columns of the groups' child rows.</p>
 *
 * <p>Depreciated config options (from Ext.grid.GroupingView):
 *   groupTextTpl</p>
 *   
 * @namespace RP.ui
 * @extends Ext.grid.GroupingView
 */
RP.ui.GridRowGroup = Ext.extend(Ext.grid.GroupingView, {
    //==============================================================================
    // Config options
    //==============================================================================
    
    /**
     * @cfg {Function} groupRowSelector This config option overrides the default behavior in which the first row in
     * a group becomes the group header row.  If you allow sorting in any column, then
     * chances are you'll need to provide this option to properly select the correct
     * row to use as the grouping row.  Function will be passed the following parameters:
     * * rs: Array of records in the group
     * Function should return the index of the record in 'rs' to use as the grouping row
     */
    groupRowSelector: null,
    
    /**
     * @cfg {Array} hideChildRowColumns This config specifies the column names (dataIndex value of grid's column model) to hide
     * the values of the groups' child rows.  For example, your grouping row may have a common
     * value such as Date which you may want to not repeat in the child rows.
     */
    hideChildRowColumns: null,
    
    
    //==============================================================================
    // Overrides
    //==============================================================================
    
    initTemplates: function() {
        RP.ui.GridRowGroup.superclass.initTemplates.call(this);
        
        this.startGroup = new Ext.XTemplate('<div id="{groupId}" class="x-grid-group {cls}">', '<div id="{groupId}-hd" class="{groupCssClass}" style="{style}">', '{groupHeaderContent}', '</div>', '<div id="{groupId}-bd" class="x-grid-group-body">');
        this.startGroup.compile();
        
        this.templates.cell = new Ext.Template('<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css} {customCSS}" style="{style}" tabIndex="0" {cellAttr}>', '<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>', '</td>');
        
        this.rpPrintGroupStart = new Ext.XTemplate('{groupHeaderContent}');
        this.rpPrintGroupStart.compile();
    },
    
    processEvent: function(name, e) {
        
        // Toggle the group if the event is a mouse down and clicked on the group row.
        var hd = e.getTarget('.x-grid-group-hd-rp', this.mainBody);
        if (hd && name === "mousedown" && e.button === 0) {
            var noChildren = !Ext.get(hd).hasClass("x-grid-group-hd-rp");
            if (noChildren) {
                return;
            }
            
            e.stopEvent();
            this.toggleGroup(hd.parentNode);
        }
        else {
            // Other wise pass the event through.
            RP.ui.GridRowGroup.superclass.processEvent.call(this, name, e);
        }
    },
    
    doRender: function(cs, rs, ds, startRow, colCount, stripe) {
        if (rs.length < 1) {
            return '';
        }
        var groupField = this.getGroupField();
        var colIndex = this.cm.findColumnIndex(groupField);
        
        this.enableGrouping = !!groupField;
        
        if (!this.enableGrouping || this.isUpdating) {
            return RP.ui.GridRowGroup.superclass.doRender.apply(this, arguments);
        }
        
        var gstyle = 'width:' + this.getTotalWidth() + ';';
        
        var gidPrefix = this.grid.getGridEl().id;
        var cfg = this.cm.config[colIndex];
        var groupRenderer = cfg.groupRenderer || cfg.renderer;
        var prefix = this.showGroupName ? (cfg.groupName || cfg.header) + ': ' : '';
        
        // Separate the records into groups.
        var groups = [], curGroup, i, len, gid;
        for (i = 0, len = rs.length; i < len; i++) {
            var rowIndex = startRow + i;
            var r = rs[i], gvalue = r.data[groupField], g = this.getGroup(gvalue, r, groupRenderer, rowIndex, colIndex, ds);
            if (!curGroup || curGroup.group !== g) {
                gid = gidPrefix + '-gp-' + groupField + '-' + Ext.util.Format.htmlEncode(g);
                
                var isCollapsed = typeof this.state[gid] !== 'undefined' ? !this.state[gid] : this.startCollapsed;
                var gcls = isCollapsed ? 'x-grid-group-collapsed' : '';
                curGroup = {
                    group: g,
                    gvalue: gvalue,
                    text: prefix + g,
                    groupId: gid,
                    startRow: rowIndex,
                    rs: [r],
                    cls: gcls,
                    style: gstyle
                };
                groups.push(curGroup);
            }
            else {
                curGroup.rs.push(r);
            }
            r._groupId = gid;
        }
        
        // Records are now separated into 'groups'
        
        // If 'groupRowSelector' config option present, call it to select the group header row.
        if (this.groupRowSelector) {
            for (i = 0, len = groups.length; i < len; i++) {
                var gr = groups[i];
                var hdrIndex = this.groupRowSelector(gr.rs) || 0;
                
                if (hdrIndex > 0) {
                    var hdrRow = gr.rs[hdrIndex];
                    gr.rs.splice(hdrIndex, 1);
                    gr.rs.unshift(hdrRow);
                }
            }
        }
        
        // Process 'hideChildRowColumns' option.
        var childrenCs = cs;
        if (this.hideChildRowColumns && this.hideChildRowColumns.length) {
            childrenCs = [];
            
            var emptyRender = function() {
                return "";
            };
            
            Ext.each(cs, function(c) {
                var childCs = Ext.apply({}, c);
                childrenCs.push(childCs);
            }, this);
            
            Ext.each(childrenCs, function(c) {
                for (var hidx = 0; hidx < this.hideChildRowColumns.length; hidx++) {
                    if (c.name === this.hideChildRowColumns[hidx]) {
                        c.renderer = emptyRender;
                    }
                }
            }, this);
        }
        
        var buf = [];
        var hdrCols = [];
        var col = 0;
        
        Ext.each(cs, function(c) {
            var cfg = Ext.apply({}, c);
            if (!this.firstVisibleColID && !this.cm.isHidden(col)) {
                this.firstVisibleColID = c.id;
            }
            hdrCols.push(cfg);
            col++;
        }, this);
        
        var getGroupRowClass, origGetRowClass;
        
        if (this.getRowClass) {
            origGetRowClass = this.getRowClass;
            
            getGroupRowClass = function(r, rowIndex, rp, ds) {
                return (origGetRowClass(r, rowIndex, rp, ds) + " rp-grid-row-group");
            };
        }
        else {
            getGroupRowClass = function(r, rowIndex, rp, ds) {
                return "rp-grid-row-group";
            };
        }
        
        for (i = 0, len = groups.length; i < len; i++) {
            var grp = groups[i];
            
            // Generate the row content for this group.
            this.getRowClass = getGroupRowClass;
            var origCellApply = this.templates.cell.rpBeforeApplyFn;
            this.templates.cell.rpBeforeApplyFn = this.rpBeforeApplyFn.createDelegate(this);
            grp.groupHeaderContent = Ext.grid.GroupingView.superclass.doRender.call(this, hdrCols, grp.rs.slice(0, 1), ds, grp.startRow, colCount, stripe);
            this.templates.cell.rpBeforeApplyFn = origCellApply;
            
            this.getRowClass = origGetRowClass;
            
            if (grp.rs.length >= 2) {
                grp.groupCssClass = "x-grid-group-hd-rp";
            }
            
            this.doGroupStart(buf, grp, cs, ds, colCount);
            
            buf[buf.length] = Ext.grid.GroupingView.superclass.doRender.call(this, childrenCs, grp.rs.slice(1), ds, grp.startRow + 1, colCount, stripe);
            
            this.doGroupEnd(buf, grp, cs, ds, colCount);
        }
        return buf.join('');
    },
    
    rpBeforeApplyFn: function(values) {
        values.customCSS = (values.id === this.firstVisibleColID) ? "rp-grid-row-group-first-cell" : "";
    },
    
    processRows: function(startRow, skipStripe) {
        if (!this.ds || this.ds.getCount() < 1) {
            return;
        }
        
        var rows = this.getRows();
        skipStripe = skipStripe || !this.grid.stripeRows;
        startRow = startRow || 0;
        Ext.each(rows, function(row, idx) {
            row.rowIndex = idx;
            row.className = row.className.replace(this.rowClsRe, ' ');
            if (!skipStripe && (idx + 1) % 2 === 0) {
                row.className += ' x-grid3-row-alt';
            }
        });
        
        // add first/last-row classes
        if (rows.length > 0) {
            if (startRow === 0) {
                Ext.fly(rows[0]).addClass(this.firstRowCls);
            }
            Ext.fly(rows[rows.length - 1]).addClass(this.lastRowCls);
        }
    },
    
    resolveCell: function(row, col, hscroll) {
        try {
            if (this.getRows().length === 0) {
                return null;
            }
            
            return RP.ui.GridRowGroup.superclass.resolveCell.call(this, row, col, hscroll);
        } 
        catch (e) {
            logger.logError(new RP.Exception("[GridRowGroup] resolveCell failed", e));
            return null;
        }
    },
    
    updateGroupWidths: function() {
        if (!this.enableGrouping || !this.hasRows()) {
            return;
        }
        
        var tw = this.cm.getTotalWidth() + 'px';
        
        var gs = this.getGroups();
        for (var i = 0, len = gs.length; i < len; i++) {
            gs[i].firstChild.style.width = tw;
            gs[i].firstChild.firstChild.style.width = tw;
            gs[i].firstChild.firstChild.firstChild.style.width = tw;
        }
        
        var colCount = gs[0].firstChild.firstChild.firstChild.rows[0].childNodes.length;
        
        Ext.each(gs, function(g) {
            var firstVisibleColFound = false;
            for (var i = 0; i < colCount; i++) {
                if (firstVisibleColFound) {
                    if (this.cm.config[i].hidden === true) {
                        g.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.display = "none";
                    }
                    else {
                        g.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.display = "";
                    }
                }
                
                var w = parseInt(this.getColumnWidth(i), 10);
                
                if (!firstVisibleColFound &&
                g.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.display !== "none") {
                    firstVisibleColFound = true;
                    
                    var pl = parseInt(g.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.paddingLeft, 10);
                    var pr = parseInt(g.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.paddingRight, 10);
                    if (!isNaN(pl)) {
                        w -= pl;
                    }
                    if (!isNaN(pr)) {
                        w -= pr;
                    }
                }
                
                g.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.width = w.toString() + "px";
            }
        }, this);
    },
    
    // Override of superclass private member.
    applyEmptyText: function() {
        RP.ui.GridView.superclass.applyEmptyText.apply(this, arguments);
        
        if (this.emptyText && !this.hasRows()) {
            this.mainHd.hide(false);
        }
        else {
            this.mainHd.show(false);
        }
    }
});
//////////////////////
// ..\js\ui\SummaryGroupingView.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.SummaryGroupingView
 * @namespace RP.ui
 * @extends Ext.grid.GroupingView
 *
 * This grouping view can be used to create a group view with specifying summary types to show in the group section.
 * The summary type is specified on the Ext.grid.ColumnModel for the grid, use the property summaryType on the column
 * model to specify a function for that summary column. The function has passed an array or Ext.data.Record objects for the column
 * as well as the dataIndex of the column:
 *
 * <p>
 * summaryType: String/Function
 *
 * <br><br>
 * <b>Parameters:</b>
 * <br>
 * <ul>
 * <li>records : Array or Ext.data.Record
 *  <br> The array of records for that row.</li>
 * <li>dataIndex: String
 *  <br> The dataIndex of the column from the columnModel
 * </li>
 * </ul>
 *
 * Aside from specifying a custom function for the summaryType there are also some default types:
 * <ul>
 *  <li> sum </li>
 *  <li> count </li>
 *  <li> min </li>
 *  <li> max </li>
 *  <li> average </li>
 * </ul>
 *
 * <p>
 * If you specify a groupTextTpl it will be used instead of the summary style group row, no column will use the
 * summaryTypes. The groupTextTpl works exactly as the groupTextTpl on the Ext.grid.GroupingView.
 *
 * This class also affords added CSS stylings for the grouping headers and the group body sections that aren't
 * provided by the base Ext.grid.GroupingView.
 *
 */
RP.ui.SummaryGroupingView = Ext.extend(Ext.grid.GroupingView, {

    /**
     * @cfg {String} customHeaderCls Custom CSS class to style the whole group.
     */
    customGroupCls: "rp-summary-group",
    
    
    /**
     * @cfg {String} defaultHeaderCls CSS class to style the group headers portions.
     */
    defaultHeaderCls: "rp-summary-group-header",
    
    /**
     * @cfg {String} customHeaderCls CSS class to style custom header. Added when the default template is not used.
     */
    customHeaderCls: "rp-summary-group-header-custom",
    
    /**
     * @cfg {String} customBodyCls Custom CSS class to style the body area of the grid view. This will affect all of
     * the rows in the grid, for all groups.
     */
    customBodyCls: "",
    
    /**
     * @private
     */
    defaultBodyCls: "rp-summary-group-view-body",
    
    /**
     * @cfg {String} groupTextTpl By default this will be used for the summary functionality of the grouping view.
     * Summary options are specified on the column model (See Ext.grid.ColumnModel). This can still be used to
     * specify a non-summary style grouping as sell, See Ext.grid.GroupingView for details.
     */
    groupTextTpl: "{groupHeaderContent}",
    
    /**
     * @private
     *
     * Sets up the template section for the group section and adds the custom styles in.
     */
    initTemplates: function() {
        RP.ui.SummaryGroupingView.superclass.initTemplates.call(this);
        
        this.startGroup = new Ext.XTemplate('<div id="{groupId}" class="x-grid-group {cls} ' + this.customGroupCls + '">', '<div id="{groupId}-hd" class="x-grid-group-hd x-grid-group-title ' + this.defaultHeaderCls + (!this.isTemplateDefault() ? ' ' + this.customHeaderCls : '') + '" style="{style}"><div class="x-grid-group-title">', this.groupTextTpl, '</div></div>', '<div id="{groupId}-bd" class="x-grid-group-body ' + this.defaultBodyCls + " " + this.customBodyCls + '">');
        this.startGroup.compile();
        
    },
    
    /**
     * @private
     *
     * A majority of this is copied directly from Ext.grid.GroupingView.
     *
     * @param {Object} cs
     * @param {Object} rs
     * @param {Object} ds
     * @param {Object} startRow
     * @param {Object} colCount
     * @param {Object} stripe
     */
    doRender: function(cs, rs, ds, startRow, colCount, stripe) {
        if (rs.length < 1) {
            return '';
        }
        
        if (!this.canGroup() || this.isUpdating) {
            return Ext.grid.GroupingView.superclass.doRender.apply(this, arguments);
        }
        
        var groupField = this.getGroupField(), colIndex = this.cm.findColumnIndex(groupField), g, gstyle = 'width:' + this.getTotalWidth() + ';', cfg = this.cm.config[colIndex], groupRenderer = cfg.groupRenderer || cfg.renderer, prefix = this.showGroupName ? (cfg.groupName || cfg.header) + ': ' : '', groups = [], curGroup, i, len, gid;
        
        for (i = 0, len = rs.length; i < len; i++) {
            var rowIndex = startRow/* + i*/, r = rs[i], gvalue = r.data[groupField];    
            //RPWEB-3502 removed +i so that every group start with a white row
            //not sure if this breaks anything in GroupingView or GridView
            
            g = this.getGroup(gvalue, r, groupRenderer, rowIndex, colIndex, ds);
            if (!curGroup || curGroup.group != g) {
                gid = this.constructId(gvalue, groupField, colIndex);
                // if state is defined use it, however state is in terms of expanded
                // so negate it, otherwise use the default.
                this.state[gid] = !(Ext.isDefined(this.state[gid]) ? !this.state[gid] : this.startCollapsed);
                curGroup = {
                    group: g,
                    gvalue: gvalue,
                    text: prefix + g,
                    groupId: gid,
                    startRow: rowIndex,
                    rs: [r],
                    cls: this.state[gid] ? '' : 'x-grid-group-collapsed',
                    style: gstyle
                };
                groups.push(curGroup);
            }
            else {
                curGroup.rs.push(r);
            }
            r._groupId = gid;
        }
        
        var buf = [];
        var hdrCols = [];
        
        // Get the configs for each of the columns
        Ext.each(cs, function(c) {
            var cfg = Ext.apply({}, c);
            hdrCols.push(cfg);
        }, this);
        
        // Loop through the groups and add the summaries.
        Ext.each(groups, function(g) {
            
            // If a groupTextTpl is specified other than the summary type, just render it like you normally would.
            if (this.isTemplateDefault()) {
                // Grab the data record setup to use for the group headers.
                var groupHeaderRs = [g.rs.slice(0, 1)[0].copy()];
                
                // Update the groupSummary based on the summaryTypes from the columnModel.
                Ext.each(this.cm.config, function(cfg) {
                    var newData = "";
                    if (!Ext.isEmpty(cfg.summaryType)) {
                        if (Ext.isFunction(cfg.summaryType)) {
                            newData = cfg.summaryType.call(this, g.rs, cfg.dataIndex);
                        }
                        else {
                            newData = this.determineSummaryFunction(cfg.summaryType, g.rs, cfg.dataIndex);
                        }
                    }
                    groupHeaderRs[0].data[cfg.dataIndex] = newData;
                }, this);
                
                
                // Use the GroupingView's superclass (Ext.grid.GridView) to render the summary like a row, with column
                // like positioning.
                g.groupHeaderContent = Ext.grid.GroupingView.superclass.doRender.call(this, hdrCols, groupHeaderRs, ds, g.startRow, colCount, false);
                
                // removed class to prevent text odd borders in the cells. This will also make the 
                // expander button work correctly.
                g.groupHeaderContent = g.groupHeaderContent.replace("x-grid3-row", "");
            }
            
            this.doGroupStart(buf, g, cs, ds, colCount);
            buf[buf.length] = Ext.grid.GroupingView.superclass.doRender.call(this, cs, g.rs, ds, g.startRow, colCount, stripe);
            
            this.doGroupEnd(buf, g, cs, ds, colCount);
            
        }, this);
        
        return buf.join('');
    },
    
    /**
     * @private
     *
     * This will update the columns in the group section to match resizing the columns on the grid.
     */
    updateGroupWidths: function() {
        // If a groupTextTpl is specified other than the summary type just render it like you
        // normally would.
        if (!this.isTemplateDefault()) {
            RP.ui.SummaryGroupingView.superclass.updateGroupWidths.apply(this, arguments);
            return;
        }
        
        if (!this.enableGrouping || !this.hasRows()) {
            return;
        }
        
        var tw = Math.max(this.cm.getTotalWidth(), this.el.dom.offsetWidth - this.getScrollOffset()) + "px";
        
        var gs = this.getGroups();
        for (var i = 0, len = gs.length; i < len; i++) {
            gs[i].firstChild.style.width = tw;
            gs[i].firstChild.firstChild.style.width = tw;
            gs[i].firstChild.firstChild.firstChild.style.width = tw;
            gs[i].firstChild.firstChild.firstChild.firstChild.style.width = tw; //table
        }
        
        var colCount = gs[0].firstChild.firstChild.firstChild.firstChild.rows[0].childNodes.length;
        
        
        Ext.each(gs, function(g) {
            var firstVisibleColFound = false;
            for (var i = 0; i < colCount; i++) {
                if (firstVisibleColFound) {
                    if (this.cm.config[i].hidden === true) {
                        g.firstChild.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.display = "none";
                    }
                    else {
                        g.firstChild.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.display = "";
                    }
                }
                
                var w = parseInt(this.getColumnWidth(i), 10);
                
                if (!firstVisibleColFound &&
                g.firstChild.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.display !== "none") {
                    firstVisibleColFound = true;
                    
                    var pl = parseInt(g.firstChild.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.paddingLeft, 10);
                    var pr = parseInt(g.firstChild.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.paddingRight, 10);
                    if (!isNaN(pl)) {
                        w -= pl;
                    }
                    if (!isNaN(pr)) {
                        w -= pr;
                    }
                }
                
                g.firstChild.firstChild.firstChild.firstChild.rows[0].childNodes[i].style.width = w.toString() + "px";
            }
        }, this);
    },
    
    /**
     * @private
     *
     * This function will determine the value based on a preset summary type.
     *
     * @param {String} summaryType String name of the basic summary types.
     * @param {Array} rs Array for Ext.data.Record for all the rows for that column.
     * @param {String} dataIndex Data index for the column.
     */
    determineSummaryFunction: function(summaryType, rs, dataIndex) {
        var summaryValue = 0;
        var data = {};
        
        // Setup the data array for doing the loop with the RP.ui.GridSummary.Calculations
        data[dataIndex] = 0;
        
        // If it's a min or max we need to set it to the first record from the record set since
        // the 0 could skew result. (e.g. all positive numbers in our set, and 0 is the start, 
        // then 0 will, be the min, not the smallest record.)
        if (summaryType === "min" || summaryType === "max") {
            data[dataIndex] = rs[0].get(dataIndex);
        }
        
        // This is here because RP.ui.GridSummary.Calculations is assuming that it will be used to
        // loop over a store where as this we are looping over an array of records. 
        var fakeRecord = {
            store: {
                getCount: function() {
                    return rs.length;
                }
            }
        };
        
        Ext.each(rs, function(record, index) {
            data[dataIndex] = RP.ui.GridSummary.Calculations[summaryType](record.get(dataIndex), fakeRecord, dataIndex, data, index);
        });
        return data[dataIndex];
    },
    
    /**
     * Boolean function to determine if the groupTextTpl is default, or has changed.
     * @return {Boolean} True if groupTextTpl is the default template "{groupHeaderContent}".
     */
    isTemplateDefault: function () {
        return this.groupTextTpl === "{groupHeaderContent}";
    }
});
//////////////////////
// ..\js\ui\GridSummary.js
//////////////////////
/*global Ext, RP */

/*
JH 9/26/2008
Found the code that does a ONE LINE summary here: http://extjs.com/forum/showthread.php?t=21331 (post #9)
I modified it to support multiple summary rows
*/

Ext.ns('RP.ui');

/**
 * @class GridSummary
 * Grid summary row class. Supports the display of multiple summary rows.
 * 
 * @namespace RP.ui
 * @extends Ext.util.Observable
 * @param {Object} config The configuration object.
 */
RP.ui.GridSummary = function(config) {
  Ext.apply(this, config);

  if (!config.numSummaryRows || config.numSummaryRows <= 0) {
    throw new RP.Exception("Invalid numRows config parameter");
  }

  // 'bottom' || 'trailing'
  // bottom is at the bottom of the grid (after blank rows)
  // trailing is inside the scroll panel following the last row
  this.summaryAlignment = config.summaryAlignment || 'trailing';

  this.numSummaryRows = config.numSummaryRows;
  this.hideIfEmpty = config.hideIfEmpty;
  this.buttons = [];
};

Ext.extend(RP.ui.GridSummary, Ext.util.Observable, {
  
  init: function(grid) {
    this.grid = grid;
    this.cm = grid.getColumnModel();
    this.view = grid.getView();

    var v = this.view;

    // override GridView's onLayout() method
    v.onLayout = this.onLayout.createDelegate(this);

    v.afterMethod('render', this.refreshSummary, this);
    v.afterMethod('refresh', this.refreshSummary, this);
    v.afterMethod('syncScroll', this.syncSummaryScroll, this);
    v.afterMethod('onColumnWidthUpdated', this.doWidth, this);
    v.afterMethod('onAllColumnWidthsUpdated', this.doAllWidths, this);
    v.afterMethod('onColumnHiddenUpdated', this.doHidden, this);

    // update summary row on store's add/remove/clear/update events
    grid.store.on({
      add: this.refreshSummary,
      remove: this.refreshSummary,
      clear: this.refreshSummary,
      update: this.refreshSummary,
      scope: this
    });
    
    this.addEvents('refresh');
    this.on('refresh', function() {
      // Loop over all of the buttons and add listeners
      Ext.each(this.buttons, function(button) {
        var el = Ext.get(button.buttonId);
        if(el) {
          el.on(button.buttonListeners);
        }
      }, this);
    }, this);

    if (!this.summaryTableTpl){
      this.summaryTableTpl = new Ext.Template(
              '<div class="rp-grid3-summary-row x-grid3-gridsummary-row-offset">',
                    '{rows}',
                '</div>'
            );
    }
    this.summaryTableTpl.disableFormats = true;
    
    var innerText = [
            '<table class="x-grid3-summary-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
                 '<tbody>',
                    '<tr>{cells}</tr>',
                 '</tbody>',
            '</table>'
        ].join("");

    if (!this.rowTpl){
      this.rowTpl = new Ext.Template('<div style="{tstyle}">' + innerText + '</div>');
      this.rowTpl.disableFormats = true;
    }
    this.rowTpl.compile();

    if (!this.cellTpl){
      this.cellTpl = new Ext.Template(
                '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}">',
                    '<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>',
                "</td>"
            );
      this.cellTpl.disableFormats = true;
    }
    this.cellTpl.compile();
    
    if (!this.buttonTpl) {
        this.buttonTpl = new Ext.Template('<td style="{style}"><button type="button" id="{buttonId}" style="{style} padding:0">{value}</button></td>');
        
        this.buttonTpl.disableFormats = true;
    }
    
    this.buttonTpl.compile();
    
  },

  /**
   * Loops through a record set in the store and calculates a summary
   * based on the summary type.
   * @method
   * @param {Number} summaryRowIndex The index of the summary row.
   * @param {Object/Array} rs The record set to loop through.
   * @param {Ext.grid.ColumnModel} cm The column model of the store.
   */
  calculate: function(summaryRowIndex, rs, cm) {
    var data = {}, cfg = cm.config;
    for (var i = 0, len = cfg.length; i < len; i++)
    { // loop through all columns in ColumnModel
      var cf = cfg[i], // get column's configuration
                cname = cf.dataIndex; // get column dataIndex

      // initialize grid summary row data for
      // the current column being worked on
      data[cname] = 0;

      var summaryType = this.getSummaryType(cf, summaryRowIndex);
      
      if (summaryType) {
        if (typeof (summaryType) === "function") {
          data[cname] = summaryType(rs);
        }
        else {
          for (var j = 0, jlen = rs.length; j < jlen; j++)
          {
            var r = rs[j]; // get a single Record
            var val;

            if (cf.summaryValue) {
              val = cf.summaryValue(r, summaryRowIndex);
            }
            else {
              val = r.get(cname);
            }
            
            if (RP.ui.GridSummary.Calculations[summaryType]) {
              data[cname] = RP.ui.GridSummary.Calculations[summaryType](val, r, cname, data, j);
            }
            else {
              data[cname] = "NaN";
            }
          }
        }
      }
    }

    return data;
  },

  /**
   * Handler for the layout event. Sets the height and width for the view.
   * @method
   * @param {Number} vw width of the view.
   * @param {Number} vh height of the view.
   */
  onLayout: function(vw, vh) {
    if (this.grid.store.getTotalCount() === 0 && this.hideIfEmpty)
    {
      return;
    }

    if (Ext.type(vh) !== 'number') { // handles grid's height:'auto' config
      return;
    }

    // Removed because it was causing unwanted white space at the end
    // of grids with summary.  
    // note: this method is scoped to the GridView
    //if (!this.grid.getGridEl().hasClass('x-grid-hide-gridsummary'))
    //{
    //  // readjust gridview's height only if grid summary row is visible
    //  if (this.view.summary)
    //  {
    //    this.view.scroller.setHeight(vh - this.view.summary.getHeight());
    //  }
    //}
  },

  /**
   * @private
   */
  syncSummaryScroll: function() {
    if (this.grid.store.getTotalCount() === 0 && this.hideIfEmpty) {
      return;
    }

    var mb = this.view.scroller.dom;

    this.view.summaryWrap.dom.scrollLeft = mb.scrollLeft;
    this.view.summaryWrap.dom.scrollLeft = mb.scrollLeft; // second time for IE (1/2 time first fails, other browsers ignore)
  },

    /**
     * @private
     * @param col
     * @param w
     * @param tw 
     */
    doWidth: function(col, w, tw)
    {
        if (this.grid.store.getTotalCount() === 0 && this.hideIfEmpty){
            return;
        }

        var s = this.view.summary.dom;

        for (var i = 0; i < s.childElementCount; i++)
        {
            s.childNodes[i].firstChild.style.width = tw;
            s.childNodes[i].firstChild.rows[0].childNodes[col].style.width = w;
        }
    },

    /**
     * @private
     * @param ws
     * @param tw
     */
    doAllWidths: function(ws, tw)
    {    
        if (this.grid.store.getTotalCount() === 0 && this.hideIfEmpty){
            return;
        }

        var s = this.view.summary.dom, wlen = ws.length;

        for (var i = 0; i < s.childElementCount; i++)
        {
            s.childNodes[i].firstChild.style.width = tw;
            var cells = s.childNodes[i].firstChild.rows[0].childNodes;

            for (var j = 0; j < wlen; j++){
                cells[j].style.width = ws[j];
            }
        }
  },

  /**
   * @private
   * @param col
   * @param hidden
   * @param tw
   */
  doHidden: function(col, hidden, tw){
      var s = this.view.summary.dom,
            display = hidden ? 'none' : '';

      for(var i = 0; i < s.childElementCount; i++)
      {
          s.childNodes[i].firstChild.style.width = tw;
          s.childNodes[i].firstChild.rows[0].childNodes[col].style.display = display;
      }
  },

  /**
   * Renders the summary row.
   * @private
   * @param summaryRowIndex
   * @param o
   * @param cs
   * @param cm 
   */
  renderSummary: function(summaryRowIndex, o, cs, cm){
    if (this.grid.store.getTotalCount() === 0 && this.hideIfEmpty){
      return "";
    }

    cs = cs || this.view.getColumnData();
    var cfg = cm.config,
            buf = [],
            last = cs.length - 1;

    for (var i = 0, len = cs.length; i < len; i++){
      var c = cs[i], cf = cfg[i], p = {};

      p.id = c.id;
      p.style = c.style;
      p.css = i === 0 ? 'x-grid3-cell-first ' : (i === last ? 'x-grid3-cell-last ' : '');
      var summaryType = this.getSummaryType(cf, summaryRowIndex);
      
      if (summaryType || (cf.summaryRenderer && cf.summaryRenderer(summaryRowIndex))){
        p.value = (((cf.summaryRenderer && cf.summaryRenderer(summaryRowIndex)) ? cf.summaryRenderer(summaryRowIndex) : null) || c.renderer)(o.data[c.name], p, o, summaryRowIndex, i);
      }
      else{
        p.value = '';
      }
      if (p.value === undefined || p.value === ""){
        p.value = "";
      }
      
      // If summary rows column is of type button then we use the button template.
      // Otherwise, we use the default cell template.
      if (summaryType === "button") {
        // Generate id for the button so we can access it to setup listeners
        var buttonId = Ext.id();
        var button = {};
        var buttonListeners = cf.buttonListeners;
       
        button.buttonId = buttonId;
        p.buttonId = buttonId;
        
        button.buttonListeners = buttonListeners;
        
        // Keep track of the buttons so we can add listeners later.
        this.buttons.push(button);
        
        buf[buf.length] = this.buttonTpl.apply(p);
      }
      else {
        buf[buf.length] = this.cellTpl.apply(p);
      }
    }

    return this.rowTpl.apply({ cells: buf.join(''), tstyle: 'width: ' + this.view.getTotalWidth()});
  },

  /**
   * Refreshes the summary row.
   * @method
   */
  refreshSummary: function(){
    var hidden = (this.grid.store.getTotalCount() === 0 && this.hideIfEmpty);
    var rowsBuf = [];

    for (var i = 0; i < this.numSummaryRows; i++) {
      var g = this.grid, ds = g.store,
            cs = this.view.getColumnData(),
            cm = this.cm,
            rs = ds.getRange(),
            data = this.calculate(i, rs, cm),
            buf = this.renderSummary(i, { data: data }, cs, cm);

      rowsBuf.push(buf);
    }

    var summaryBuf = this.summaryTableTpl.apply({
      tstyle: 'width:' + this.view.getTotalWidth() + ';',
      rows: rowsBuf.join('')
    });

    if (!this.view.summaryWrap) {
      var fn = Ext.DomHelper.append;
      if (this.summaryAlignment === 'bottom') {
        fn = Ext.DomHelper.insertAfter;
      }
      this.view.summaryWrap = fn.apply(Ext.DomHelper, [this.view.scroller, {
        tag: 'div',
        cls: 'x-grid3-gridsummary-row-inner'
      }, true]);
    }

    this.view.summaryWrap[hidden ? 'addClass' : 'removeClass']("x-hidden");
    this.view.summary = this.view.summaryWrap.update(summaryBuf).first();
    this.fireEvent("refresh");
  },

  /**
   * Toggles the display of the summary row.
   * @method
   * @param {Boolean} visible A boolean to determine whether the summary
   * row is visible or not. Defaults to false.
   */
  toggleSummary: function(visible) { // true to display summary row
    var el = this.grid.getGridEl();

    if (el) {
      if (visible === undefined) {
        visible = el.hasClass('x-grid-hide-gridsummary');
      }
      el[visible ? 'removeClass' : 'addClass']('x-grid-hide-gridsummary');

      this.view.layout(); // readjust gridview height
    }
  },

  /**
   * Returns the summary row
   * @method
   * @return {Object} The summary row.
   */
  getSummaryNode: function() {
    return this.view.summary;
  },
  
  getSummaryType: function(config, summaryRowIndex) {
    var type = config.summaryType;
    if (type && Ext.isFunction(type)) {
      type = type(summaryRowIndex);
    }
    
    return type;
  }
});
Ext.reg('gridsummary', RP.ui.GridSummary);

/*
 * all Calculation methods are called on each Record in the Store
 * with the following 5 parameters:
 *
 * v - cell value
 * record - reference to the current Record
 * colName - column name (i.e. the ColumnModel's dataIndex)
 * data - the cumulative data for the current column + summaryType up to the current Record
 * rowIdx - current row index
 */
RP.ui.GridSummary.Calculations = {
  sum: function(v, record, colName, data, rowIdx) {
    return data[colName] + Ext.num(v, 0);
  },

  count: function(v, record, colName, data, rowIdx) {
    return rowIdx + 1;
  },

  max: function(v, record, colName, data, rowIdx) {
    return Math.max(Ext.num(v, 0), data[colName]);
  },

  min: function(v, record, colName, data, rowIdx) {
    return Math.min(Ext.num(v, 0), data[colName]);
  },

  average: function(v, record, colName, data, rowIdx) {
    var t = data[colName] + Ext.num(v, 0), count = record.store.getCount();
    return rowIdx === count - 1 ? (t / count) : t;
  }
};
//////////////////////
// ..\js\ui\InactivityWarningDialog.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class InactivityWarningDialog
 * This is a dialog window that will warn the user that their
 * session is about to expire due to inactivity.  The user can then
 * either Continue or Logout.  If the user does neither, and the
 * expiration time runs out, the session will automatically expire
 * and a RP.ui.SessionExpiredDialog will be displayed.
 *
 * @namespace RP.ui
 * @extends Ext.Window
 */
RP.ui.InactivityWarningDialog = Ext.extend(Ext.Window, {

    /**
     * @cfg {Number} secondsUntilExpire (Optional) <p>The number of seconds
     * until the session will automatically expire. Defaults to 60 seconds.</p>
     */
    secondsUntilExpire: 60,
    
    /**
     * @private {Object} timeoutTask
     * The timeout task created by the TaskMgr to count down seconds
     * until expiration.  Only used internally.
     */
    timeoutTask: {},
    
    /**
     * @private {String} id
     * The id for the InactivityWarningDialog component
     */
    id: "inactivityWarningDialog",
    
    /**
     * @private {Boolean} a toggle to prevent isUserActive from firing constantly. Only used internally.
     */
    isAjaxRequestExecuting: false,
    
    /**
     * @private
     */
    initComponent: function() {
        this.expireCountDown = this.secondsUntilExpire;
        Ext.apply(this, {
            title: RP.getMessage("rp.common.login.InactivityWarningTitle"),
            width: 450,
            draggable: false,
            closable: false,
            resizable: false,
            padding: 20,
            bodyStyle: "background-color: #fff",
            items: [{
                xtype: "box",
                itemId: "message",
                style: "font-size: 12px; margin-bottom: 30px;",
                html: this.getDisplayMessage()
            }, {
                itemId: "pnlButtons",
                border: false,
                buttonAlign: "center",
                buttons: [{
                    text: RP.getMessage("rp.common.login.ContinueButton"),
                    id: 'btnContinue',
                    scope: this,
                    handler: function() {
                        RP.util.Helpers.keepSessionAlive();
                        this.destroy();
                    }
                }, {
                    text: RP.getMessage("rp.common.login.LogoutButton"),
                    scope: this,
                    itemId: 'btnLogout',
                    handler: function() {
                        this.destroy();
                        RP.util.Helpers.logout();
                    }
                }]
            }]
        });
        
        RP.ui.InactivityWarningDialog.superclass.initComponent.call(this);
    },
    
    /**
     * Shows the dialog and starts a count down timer to automatically
     * display seconds until logout and then call expireSession.
     */
    show: function() {
        Ext.getBody().maskEx();
        
        Ext.EventManager.onWindowResize(this.center, this);
        
        RP.ui.InactivityWarningDialog.superclass.show.apply(this, arguments);
        
        // start a countdown to display the seconds until logout
        // at 0, stop the countdown and display the expired dialog
        this.expireCountDown = this.secondsUntilExpire;
        var runFn = function() {
            this.getComponent("message").update(this.getDisplayMessage());
            
            if (this.expireCountDown-- <= 0 && !this.isAjaxRequestExecuting) {
                this.isAjaxRequestExecuting = true;
                RP.util.Helpers.isUserActive(this.userActiveSuccess, this.expireSession, this);
            }
        };
        
        var taskCfg = {
            run: runFn,
            interval: 1000,
            scope: this
        };
        
        this.timeoutTask = Ext.TaskMgr.start(taskCfg);
    },
    
    /**
     * Handler for successfully getting a response from RP.util.Helpers.isUserActive.
     */
    userActiveSuccess: function(response, options) {
        var intervalMillis = (this.secondsUntilExpire + RP.util.PageInactivityChecker.getTimeOutInSeconds()) * 1000;
        this.isAjaxRequestExecuting = false;
        try {
            var result = Ext.util.JSON.decode(response.responseText);
            if (Ext.isNumber(result.data)) {
                var serverLastBrowserActivity = parseInt(result.data, 10);
                var serverLastBrowserActivityElapsed = (new Date()) - serverLastBrowserActivity;
                if (serverLastBrowserActivityElapsed < intervalMillis) {
                    this.destroy();
                    return;
                }
            }
            this.expireSession();
        }
        catch(ex) {
            // If we do not get JSON back it probably is the login page, expire the session
            this.expireSession();
        } 
    },
    
    /**
     * Stops the count down task.
     */
    stopCountdown: function() {
        this.isAjaxRequestExecuting = false;
        Ext.TaskMgr.stop(this.timeoutTask);
    },
    
    /**
     * Removes the mask hiding the body and destroys the window.
     */
    destroy: function() {
        this.stopCountdown();
        
        Ext.getBody().unmask();
        
        Ext.EventManager.removeResizeListener(this.center, this);
        
        RP.event.AppEventManager.fire(RP.upb.AppEvents.ActiveAgain, {});
        
        RP.ui.InactivityWarningDialog.superclass.destroy.call(this);
    },
    
    /**
     * Marks the session as requiring re-authentication and then displays the
     * appropriate dialog depending on how the user was authenticated.
     */
    expireSession: function() {
        this.stopCountdown();
        this.hide();
        
        RP.event.AppEventManager.fire(RP.upb.AppEvents.BeforeSessionExpired, {});
        
        RP.util.Helpers.markSessionForReAuthentication();
        
        if (!Ext.getCmp("sessionExpiredDialog") && !Ext.getCmp("externalAuthenticationExpiredDialog")) {
            var dialog;
            if (!RP.util.Helpers.isNativeLogin()) {
                dialog = new RP.ui.ExternalAuthenticationExpiredDialog();
            }
            else {
                dialog = new RP.ui.SessionExpiredDialog();
            }
            
            dialog.on("destroy", this.destroy, this);
            dialog.show();
        }
    },
    
    /**
     * Returns a string value containing the description and number
     * of seconds until automatic logout.
     * @return {String} A formatted String displaying the number of
     * seconds until automatic logout.
     */
    getDisplayMessage: function() {
        return String.format(RP.getMessage("rp.common.login.InactivityWarning"), Math.max(this.expireCountDown, 0));
    }
});
//////////////////////
// ..\js\ui\ExternalAuthenticationExpiredDialog.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.ExternalAuthenticationExpiredDialog
 * @extends Ext.Window
 * 
 * A dialog window that is displayed when the user's session has
 * expired.  The user then has the ability to either go back to the
 * login page, or attempt to log back in.  Instantiation of this
 * component will automatically mark the user's session as requiring
 * re-authentication.
 */
RP.ui.ExternalAuthenticationExpiredDialog = Ext.extend(Ext.Window, {
    id: "externalAuthenticationExpiredDialog",
    
    /**
     * @private
     */
    initComponent: function() {
        
        // Fire session expired event
        RP.event.AppEventManager.fire(RP.upb.AppEvents.SessionExpired, {});
        
        Ext.apply(this, {
            title: RP.getMessage("rp.common.login.InactivityTimeoutTitle"),
            width: 450,
            draggable: false,
            closable: false,
            resizable: false,
            padding: 20,
            bodyStyle: "background-color: #fff",
            items: [{
                xtype: "box",
                style: "font-size: 12px;",
                html: RP.getMessage("rp.common.login.ExternalAuthSessionExpired")
            }, {
                itemId: "pnlButtons",
                border: false,
                buttonAlign: "center",
                buttons: [{
                    itemId: "btnOk",
                    text: Ext.MessageBox.buttonText.ok,
                    handler: RP.util.Helpers.logout
                }]
            }]
        });
        
        RP.ui.ExternalAuthenticationExpiredDialog.superclass.initComponent.call(this);
    }
});
//////////////////////
// ..\js\ui\LoadMask.js
//////////////////////
/**
 * @class LoadMask
 * A simple utility class for generically masking elements while loading data.
 * Adds a hidden property to determine if the LoadMask is being shown or not.
 *
 * @namespace Ext
 * @extends Ext.LoadMask
 *
 */
Ext.apply(Ext.LoadMask.prototype, {

    /**
     * Shows a LoadMask over the configured Element.
     * @method
     */
    show: function() {
        this.onBeforeLoad();
        this.hidden = false;
    },
    
    /**
     * Called from hide(). Sets the LoadMask's hidden property to true when it is no longer needed.
     * @method
     */
    onLoad: function() {
        this.el.unmask(this.removeMask);
        this.hidden = true;
    }
});
//////////////////////
// ..\js\ui\TimeRange.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class TimeRange
 *
 * @namespace RP.ui
 * @extends Ext.Container
 */
RP.ui.TimeRange = Ext.extend(Ext.Container, {
    /**
     * @cfg {String/Object} timeFormat The time format to use inside the
     * time field controls, defaults to RP.core.Formats.Time.Default.
     */
    timeFormat: RP.core.Formats.Time.Default,
    
    /**
     * @cfg {Number} offsetMintues The number of minutes to offset the
     * endtime from the start time
     */
    offsetMinutes: 60,
    
    /**
     * @cfg (Date) startDate The start date of the time range
     */
    startDate: new Date(),
    
    /**
     * @private
     */
    initComponent: function() {
        this.addEvents(        
        /**
         * @event change
         * Triggered when following a change to the date or time fields.
         */
        "change");
        
        var now = this.startDate.roundBackToHour();
        
        Ext.applyIf(this, {
            cls: "rp-ui-time-range",
            startDateTime: now,
            endDateTime: now.clone().addMinutes(this.offsetMinutes),
            layout: "table",
            layoutConfig: {
                columns: 5
            }
        });
        
        this._setLastStartDate(this.startDateTime);
        
        Ext.apply(this, {
            items: this._createItems()
        });
        
        this.createListeners();
        
        RP.ui.TimeRange.superclass.initComponent.call(this);
    },
    
    /**
     * Creates and returns the time controls by calling
     * createStartDateTime() and createEndDateTime().
     * @private
     * @return {Array/Component} The time controls.
     */
    _createItems: function() {
        var start = this.createStartDateTime();
        Ext.apply(this, start);
        
        var end = this.createEndDateTime();
        Ext.apply(this, end);
        
        return [this.startDateField, this.startTimeField, {
            xtype: "label",
            cls: "x-form-item",
            text: RP.getMessage("rp.common.components.ui.timeRange.toLabel")
        }, this.endTimeField, this.endDateField];
    },
    
    /**
     * Creates the start date/time controls.
     * @method
     * @return {Object} An object with the keys startDateField and
     * startTimeField.
     */
    createStartDateTime: function() {
        return {
            startDateField: new RP.ui.DateField({
                value: this._cloneAndClear(this.startDateTime)
            }),
            startTimeField: new RP.ui.TimeField({
                value: this.startDateTime.formatTime(this.timeFormat)
            })
        };
    },
    
    /**
     * Creates the end date/time controls.
     * @method
     * @return {Object} An object with the keys endDateField and
     * endTimeField.
     */
    createEndDateTime: function() {
        return {
            endDateField: new RP.ui.DateField({
                value: this._cloneAndClear(this.endDateTime)
            }),
            endTimeField: new RP.ui.TimeField({
                value: this.endDateTime.formatTime(this.timeFormat)
            })
        };
    },
    
    /**
     * Gets the combined start date and time.
     * @method
     * @return {Date} The start date.
     */
    getStartDateTime: function() {
        this.startTimeField.setTheDate(this.startDateField.getValue());
        
        return Ext.isEmpty(this.startTimeField.getDateTime()) ? null : this.startTimeField.getDateTime().clone();
    },
    
    /**
     * Gets the combined end date and time.
     * @method
     * @return {Date} The end date.
     */
    getEndDateTime: function() {
        this.endTimeField.setTheDate(this.endDateField.getValue());
        
        return Ext.isEmpty(this.endTimeField.getDateTime()) ? null : this.endTimeField.getDateTime().clone();
    },
    
    /**
     * Set the start date and time.
     * @method
     * @param {Date} startDate The start date.
     */
    setStartDateTime: function(startDate) {
        this.startDateField.setValue(this._cloneAndClear(startDate));
        this.startTimeField.setValue(startDate.formatTime(this.timeFormat));
    },
    
    /**
     * Set the end date and time.
     * @method
     * @param {Date} endDate The end date.
     */
    setEndDateTime: function(endDate) {
        this.endDateField.setValue(this._cloneAndClear(endDate));
        this.endTimeField.setValue(endDate.formatTime(this.timeFormat));
    },
    
    /**
     * Create the event listeners on the time controls.
     * @method
     */
    createListeners: function() {
        this.startDateField.on({
            "select": this.onStartDateFieldUpdate,
            "change": this.onStartDateFieldUpdate,
            scope: this
        });
        
        this.endDateField.on("change", this.onEndDateFieldChange, this);
        this.startTimeField.on("change", this.onStartTimeFieldChange, this);
        this.endTimeField.on("change", this.onEndTimeFieldChange, this);
        
        this.startTimeField.on("blur", this.validateDuration, this);
        this.startDateField.on("blur", this.validateDuration, this);
        this.endTimeField.on("blur", this.validateDuration, this);
        this.endDateField.on("blur", this.validateDuration, this);
    },
    
    /**
     * Only used to fire the change event.
     * @private
     */
    onEndDateFieldChange: function() {
        this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
    },
    
    /**
     * When start date changes, update the end date to match.
     * @method
     * @param {Object} field The field that contains the start date.
     * @param {Date} newStartDate The new start date value.
     */
    onStartDateFieldUpdate: function(field, newStartDate) {
        var endTime = this.getEndDateTime();
        if (!Ext.isEmpty(endTime)) {
            var deltaMS = this.getDurationDeltaMillis(this.lastStartDate, newStartDate);
            
            endTime.addMilliseconds(deltaMS);
            
            this.endTimeField.setValue(endTime.formatTime(this.timeFormat));
            this.endDateField.setValue(endTime);
            this._setLastStartDate(newStartDate);
            this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
        }
    },
    
    /**
     * When start time changes, maintain old duration between start/end time.
     * @private
     * @param {Object} field
     * @param {String} newValue
     * @param {String} oldValue
     */
    onStartTimeFieldChange: function(field, newValue, oldValue) {
        if (Ext.isEmpty(newValue)) {
            this.startTimeField.setValue(null);
        }
        else {
            this.startTimeField.setValue(newValue);
            var endTime = this.getEndDateTime();
            if (Ext.isEmpty(oldValue) && !Ext.isEmpty(endTime)) {
                oldValue = this.getStartDateTime().format("g:i a");
            }
            else 
                if (Ext.isEmpty(oldValue) && Ext.isEmpty(endTime)) {
                    oldValue = this.getStartDateTime().add(Date.MINUTE, -this.offsetMinutes).format("g:i a");
                }
            if (Ext.isEmpty(endTime)) {
                endTime = this.getStartDateTime();
            }
            var newStartTime = this.getStartDateTime();
            var oldStartTime = IntervalJN.parseTimeAfterBase(oldValue, this._cloneAndClear(newStartTime));
            var deltaMS = this.getDurationDeltaMillis(oldStartTime, newStartTime);
            
            endTime.addMilliseconds(deltaMS);
            
            this.endTimeField.setValue(endTime.formatTime(this.timeFormat));
            this.endTimeField.setTheDate(endTime);
            this.endDateField.setValue(this._cloneAndClear(endTime));
            this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
        }
    },
    
    /**
     * Gets the delta milliseconds between two date objects.
     * @private
     * @param {Date} oldDate
     * @param {Date} newDate
     */
    getDurationDeltaMillis: function(oldDate, newDate) {
        return newDate.deltaT(oldDate).ms;
    },
    
    /**
     * When end time changes, check if end time is before the start time...
     * if it is, increment the end date assuming the end time has crossed midnight
     * @private
     * @param {Object} field
     * @param {String} newValue
     * @param {String} oldValue
     */
    onEndTimeFieldChange: function(field, newValue, oldValue) {
        if (Ext.isEmpty(newValue)) {
            this.endTimeField.setValue(null);
        }
        else {
            var startTimeTest;
            if (Ext.isEmpty(this.getStartDateTime())) {
                startTimeTest = this.getEndDateTime();
            }
            else {
                startTimeTest = IntervalJN.parseTimeAfterBase(this.startTimeField.getValue(), this.startDateField.getValue().clone());
            }
            var endTimeTest = IntervalJN.parseTimeAfterBase(this.endTimeField.getValue(), this.endDateField.getValue().clone());
            var endTime = this.getEndDateTime();
            
            // if raw dates are the same, but the end time is before the start time, increment the end date
            if (this._cloneAndClear(startTimeTest).compareTo(this._cloneAndClear(endTimeTest)) === 0 &&
            endTime < startTimeTest) {
                endTime.addDays(1).clearTime();
                
                this.endTimeField.setTheDate(endTime);
                this.endDateField.setValue(endTime.clone());
            }
            this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
        }
    },
    
    /**
     * Validates the duration, making sure the end date is not earlier
     * than the start date.
     * @method
     */
    validateDuration: function() {
        var startTime = this.getStartDateTime();
        var endTime = this.getEndDateTime();
        
        if (endTime < startTime) {
            this.endTimeField.markInvalid(RP.getMessage("rp.common.components.ui.timeRange.invalidDuration"));
            this.endDateField.markInvalid(RP.getMessage("rp.common.components.ui.timeRange.invalidDuration"));
        }
        else {
            this.endTimeField.clearInvalid();
            this.endDateField.clearInvalid();
        }
    },
    
    /**
     * Sets the last start date, used to maintain duration when the
     * start date changes.
     * @private
     * @param {Date} date
     */
    _setLastStartDate: function(date) {
        this.lastStartDate = this._cloneAndClear(date);
    },
    
    /**
     * Takes a date object and performs a clone() and clearTime() on it.
     * @private
     * @param {Date} date
     */
    _cloneAndClear: function(date) {
        return date.clone().clearTime();
    }
});
//////////////////////
// ..\js\ui\TimeRangeDropDown.js
//////////////////////
Ext.ns("RP.ui");


/**
 * @class TimeRangeDropDown
 *
 * @namespace RP.ui
 * @extends RP.ui.TimeRange
 *
 * <p>A RP.ui.TimeRange extension for using drop-downs for the time values.</p>
 * @constructor
 * @param {Object} config
 */
RP.ui.TimeRangeDropDown = Ext.extend(RP.ui.TimeRange, {
    /**
     * @cfg {Number} offsetMinutes The number of minutes to use for the interval
     * between each time in the drop-down list of a TimeCombo.
     */
    offsetMinutes: 15,
    
    /**
     * @private
     */
    initComponent: function() {
        this.offsetMinutes = this.initialConfig.offsetMinutes;
        RP.ui.TimeRangeDropDown.superclass.initComponent.call(this);
    },
    
    /**
     * Creates the start date/time controls.
     * @method
     * @return {Object} An object with the keys startDateField and
     * startTimeField.
     */
    createStartDateTime: function() {
        return {
            startDateField: new RP.ui.DateField({
                maxValue: this.initialConfig.maxValue,
                value: this._cloneAndClear(this.startDateTime)
            }),
            startTimeField: new RP.ui.TimeCombo({
                value: this.startDateTime.formatTime(this.timeFormat),
                format: this.timeFormat,
                offsetMinutes: this.offsetMinutes
            })
        };
    },
    
    /**
     * Creates the end date/time controls.
     * @method
     * @return {Object} An object with the keys endDateField and
     * endTimeField.
     */
    createEndDateTime: function() {
        return {
            endDateField: new RP.ui.DateField({
                maxValue: this.initialConfig.maxValue,
                value: this._cloneAndClear(this.endDateTime)
            }),
            endTimeField: new RP.ui.TimeCombo({
                value: this.endDateTime.formatTime(this.timeFormat),
                format: this.timeFormat,
                offsetMinutes: this.offsetMinutes
            })
        };
    },
    
    /**
     * Creates the event listeners on the time controls.
     * @method
     */
    createListeners: function() {
        RP.ui.TimeRangeDropDown.superclass.createListeners.call(this);
        this.startTimeField.on("focus", function() {
            this.endTimeField.collapse();
        }, this);
        this.endTimeField.on("focus", function() {
            this.startTimeField.collapse();
        }, this);
    }
});

Ext.reg("rpuitimerangedropdown", RP.ui.TimeRangeDropDown);
//////////////////////
// ..\js\ui\TimeCombo.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class TimeCombo
 *
 * <p>An Ext.form.ComboBox extension for entering time values.</p>
 * <p>Will automatically attempt to parse and format the time value
 * on the blur event.</p>
 *
 * @namespace RP.ui
 * @extends Ext.form.ComboBox
 * @constructor
 * @param {Object} config
 */
RP.ui.TimeCombo = Ext.extend(Ext.form.ComboBox, {
    /**
     * @cfg {Number} offsetMinutes The number of minutes to use for the interval
     * between each time in the drop-down list.
     */
    offsetMinutes: 15,
    
    /**
     * @cfg {Number} hoursToBoundary The number of hours to show before and after the selected
     * time in the drop-down list.
     */
    hoursToBoundary: 12,
    
    /**
     * @cfg {Boolean} enableAutocomplete Whether or not autocomplete is enabled when typing
     */
    enableAutoComplete: false,
    
    /**
     * @private
     */
    initComponent: function() {
        // TimeCombo inherits from ComboBox and partially inherits from TimeField
        Ext.applyIf(RP.ui.TimeCombo.prototype, RP.ui.TimeField.prototype);
        RP.ui.TimeField.prototype.initComponent.call(this);
        this.offsetMinutes = this.initialConfig.offsetMinutes || this.offsetMinutes;
        this.hoursToBoundary = this.initialConfig.hoursToBoundary || this.hoursToBoundary;
        this.enableAutoComplete = this.initialConfig.enableAutoComplete || this.enableAutoComplete;
        
        this.autoSelect = false;
        this.enableKeyEvents = true;
        this.store = this.createTimes(new Date());
        this.doneInitialSelection = false;
        
        // Expands the ComboBox drop-down on TextField click
        this.on("render", function(cmp) {
            cmp.getEl().on("click", function() {
                cmp.expand();
                if (!this.doneInitialSelection) {
                    // For unknown reasons later code unselects the text in IE, so defer the selectText call
                    if (Ext.isIE) {
                        this.selectText.defer(30, this);
                    }
                    else {
                        this.selectText();
                    }
                    this.doneInitialSelection = true;
                }
            }, this);
        }, this);
        
        this.on("keydown", function(field, event) {
            if (!this.enableAutoComplete) {
                // Disable ComboBox expansion when a key is pressed
                this.collapse();
                // minChars is reset to 0 for unknown reasons when handling the keydown so minChars is set here
                this.minChars = Number.MAX_VALUE;
            }
        }, this);
        
        // Update the ComboBox's store on expansion and select the appropriate value if applicable
        this.on("expand", function() {
            this.store.loadData(this.createTimes(this.getDateTime(true)));
            if (!this.enableAutoComplete) {
                this.setTheTime(this.getDateTime(true));
            }
            // The next 3 lines take care of selecting the current value in the drop-down and centering
            // it in the viewable drop-down area
            this.selectByValue(this.getRawValue());
            this.select(this.selectedIndex + 7); // Select a value a bit further down
            // Select the correct value again without scrolling so that it is toward the middle
            this.selectByValue(this.getRawValue(), false);
        }, this);
        RP.ui.TimeCombo.superclass.initComponent.call(this);
    },
    
    /**
     * Creates the times in the ComboBox's drop-down
     * @method
     * @param {Object} time The base time to add other times based off of
     * @return An array of times that can be used for user selection
     */
    createTimes: function(time) {
        if (Ext.isEmpty(time)) {
            time = new Date().clearTime();
        }
        var times = [];
        var startDate = time.add(Date.HOUR, -this.hoursToBoundary);
        var date;
        for (date = startDate; date < startDate.add(Date.HOUR, this.hoursToBoundary * 2);) {
            date = date.add(Date.MINUTE, this.offsetMinutes);
            times.push(date.formatTime(this._format));
        }
        return times;
    },
    
    beforeBlur: function() {
        RP.ui.TimeField.prototype.beforeBlur.apply(this, arguments); // Needed for the parse functionality of RP.ui.TimeField
        RP.ui.TimeCombo.superclass.beforeBlur.apply(this, arguments);
    },
    
    /**
     * Handles the onFocus event. Expands the ComboBox.
     * @method
     */
    onFocus: function() {
        if (!Ext.isOpera) { // don't touch in Opera
            this.el.addClass(this.focusClass);
        }
        if (!this.hasFocus) {
            this.hasFocus = true;
            this.startValue = this.getRawValue();
            this.fireEvent("focus", this);
            if (!this.triggerCollapse) {
                this.expand();
            }
            this.triggerCollapse = false;
        }
        this.selectText();
    },
    
    /**
     * Handles the onSelect event.
     * @method
     * @param {Object} record The record selected
     * @param {Object} index The index selected
     */
    onSelect: function(record, index) {
        RP.ui.TimeCombo.superclass.onSelect.apply(this, arguments);
        this.hasFocus = true;
        var value = this.store.getAt(index).json[0];
        if (String(value) !== String(this.startValue)) {
            this.setRawValue(value);
            this.fireEvent('change', this, value, this.startValue);
            this.startValue = value;
        }
    },
    
    /**
     * Handles the onBlur event
     * @method
     */
    onBlur: function() {
        this.beforeBlur();
        this.el.removeClass(this.focusClass);
        this.hasFocus = false;
        this.doneInitialSelection = false;
        if (this.validationEvent !== false && this.validateOnBlur && this.validationEvent != "blur") {
            this.validate();
        }
        var value = this.getRawValue();
        if (String(value) !== String(this.startValue)) {
            // The stock ComboBox control does not fire the change event if the text is changed by typing
            // rather than selection hence the need to do this
            this.fireEvent('change', this, value, this.startValue);
            this.startValue = value;
        }
        this.fireEvent("blur", this);
        this.applyEmptyText(); // Using this instead of postBlur keeps IE and Chrome from messing up
    },
    
    /** 
     * Handles the onTriggerClick event. Does not call doQuery like in Ext.form.ComboBox because
     * that is handled by the expand event handler and we do not want duplicate queries.
     * @method
     */
    onTriggerClick: function() {
        if (this.readOnly || this.disabled) {
            return;
        }
        if (this.isExpanded()) {
            this.collapse();
            this.triggerCollapse = true;
            this.el.focus();
        }
        else {
            this.onFocus({});
            this.el.focus();
        }
    }
});

Ext.reg("rpuitimecombo", RP.ui.TimeCombo);
//////////////////////
// ..\js\ui\Toolbar.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class Toolbar
 *
 *
 * <PRE>
 * Description/Configuration
 *
 * [Description]
 *
 * A common, mostly pre-configured toolbar that supports easily assigning items to the left, center, right
 * or any combination of the three
 *
 *
 * [Config Properties]
 *
 * Name                Req?    Type     Default   Description
 * ==================  ======  =======  ========  =============================================================
 * leftItems           no      array    N/A       Items to be placed on the left side of the toolbar
 * centerItems         no      array    N/A       Items to be placed on the center of the toolbar
 * rightItems          no      array    N/A       Items to be placed on the right of the toolbar
 * centerWidth         no      int      N/A       Allows you to override the width of the center portion of
 *                                                  the toolbar.  Is only utilized if there are left or
 *                                                  right items passed in.  When only centerItems are configured
 *                                                  this value is ignored and the entire toolbar is utilized.
 *                                                  This property is useful for shrinking the center, allowing
 *                                                  more space for left/right items.  If no centerWidth is
 *                                                  configured, toolbar spaced will be split equally among the
 *                                                  left, center, and right.
 * backgroundCls       no      string   N/A       String representing the css background style to use. A default
 *                                                  background is used when one isn't configured.
 *
 *
 * [Notes]
 *
 * --By default any configured areas share equal space. i.e. if only left and right items are configured
 *   then both the "left" toolbar and "right" toolbar will be the same size.
 * --leftItems are left justified but may be right justified with the use of the regular Ext fill component (shorthand: "->")
 * --rightItems are right justified.  Currently this cannot be overridden but a future update will allow you to override this behavior
 * }
 * </PRE>
 *
 * @namespace RP.ui
 * @extends Ext.Container
 */
RP.ui.Toolbar = Ext.extend(Ext.Container, {

    /**
     * @private
     */
    initComponent: function() {
        var items = [];
        
        this._leftBar = null;
        this._rightBar = null;
        this._centerBar = null;
        this._containerCreated = false;
        
        // Configure left toolbar if needed
        if (this._hasLeftItems() || (this._hasCenterItems() && !this._hasOnlyCenterItems())) {
            this._createLeftBar(this.initialConfig.leftItems || []);
            
            items = items.concat(this.getLeftBar());
        }
        
        // Configure center toolbar if needed
        if (this._hasCenterItems()) {
            this._createCenterBar(this.initialConfig.centerItems || []);
            
            items = items.concat(this.getCenterBar());
        }
        
        // Configure right toolbar if needed
        if (this._hasRightItems() || (this._hasCenterItems() && !this._hasOnlyCenterItems())) {
            this._createRightBar(this.initialConfig.rightItems || []);
            
            items = items.concat(this.getRightBar());
        }
        
        var containerConfig = {
            layout: 'hbox',
            cls: 'rp-tbar-container ' + this._getToolbarBackground(),
            hideBorders: true,
            items: items
        };
        
        Ext.apply(this, Ext.apply(this.initialConfig, containerConfig));
        //Ext.apply(this, config);
        
        RP.ui.Toolbar.superclass.initComponent.call(this);
        
        this._containerCreated = true;
    },
    
    // Public
    
    /**
     * Returns the left region of the toolbar.
     * @method
     * @return {Ext.ToolBar} The left region of the toolbar.
     */
    getLeftBar: function() {
        return this._leftBar;
    },
    
    /**
     * Returns the center region of the toolbar.
     * @method
     * @return {Ext.ToolBar} The center region of the toolbar.
     */
    getCenterBar: function() {
        return this._centerBar;
    },
    
    /**
     * Returns the right region of the toolbar.
     * @method
     * @return {Ext.ToolBar} The right region of the toolbar.
     */
    getRightBar: function() {
        return this._rightBar;
    },
    
    /**
     * Adds items to the left region of the toolbar.
     * @method
     * @param {Array} items An array of components to add to
     * the left region of the toolbar.
     */
    addItemsToLeftBar: function(items) {
        if (this._leftBar === null) {
            this._createLeftBar(items);
        }
        else {
            this._leftBar.add(items);
        }
    },
    
    /**
     * Adds items to the center region of the toolbar.
     * @method
     * @param {Array} items An array of components to add to
     * the center region of the toolbar.
     */
    addItemsToCenterBar: function(items) {
        if (this._centerBar === null) {
            this._createCenterBar(items);
        }
        else {
            this._centerBar.add(items);
        }
    },
    
    /**
     * Adds items to the right region of the toolbar.
     * @method
     * @param {Array} items An array of components to add to
     * the right region of the toolbar.
     */
    addItemsToRightBar: function(items) {
        if (this._rightBar === null) {
            this._createRightBar(items);
        }
        else {
            this._rightBar.add(items);
        }
    },
    
    // Private
    
    /**
     * @private
     */
    _hasLeftItems: function() {
        return this.initialConfig.leftItems ? true : false;
    },
    
    /**
     * @private
     */
    _hasCenterItems: function() {
        return this.initialConfig.centerItems ? true : false;
    },
    
    /**
     * @private
     */
    _hasRightItems: function() {
        return this.initialConfig.rightItems ? true : false;
    },
    
    /**
     * @private
     */
    _hasOnlyCenterItems: function() {
        return (this._hasCenterItems() && !this._hasLeftItems() && !this._hasRightItems()) ? true : false;
    },
    
    /**
     * @private
     */
    _createLeftBar: function(items) {
        //make sure we don't have one already
        if (this._leftBar !== null) {
            return;
        }
        
        this._leftBar = new Ext.Toolbar({
            cls: 'rp-tbar',
            items: items,
            flex: 1
        });
        
        if (this._isContainerAlreadyCreated()) {
            this.insert(0, this._leftBar);
        }
    },
    
    /**
     * @private
     */
    _createCenterBar: function(items) {
        //make sure we don't have one already
        if (this._centerBar !== null) {
            return;
        }
        
        var centerConfig = {
            layout: 'hbox',
            layoutConfig: {
                pack: 'center',
                align: 'middle'
            },
            cls: 'rp-tbar',
            items: items
        };
        
        if (this.initialConfig.centerWidth && !this._hasOnlyCenterItems()) {
            Ext.apply(centerConfig, {
                width: this.initialConfig.centerWidth
            });
        }
        else {
            Ext.apply(centerConfig, {
                flex: 1
            });
        }
        
        this._centerBar = new Ext.Toolbar(centerConfig);
        
        if (this._isContainerAlreadyCreated()) {
            this.insert(1, this._centerBar);
        }
    },
    
    /**
     * @private
     */
    _createRightBar: function(items) {
        //make sure we don't have one already
        if (this._rightBar !== null) {
            return;
        }
        
        this._rightBar = new Ext.Toolbar({
            cls: 'rp-tbar',
            items: ["->", items],
            flex: 1
        });
        
        if (this._isContainerAlreadyCreated()) {
            this.insert(2, this._rightBar);
        }
    },
    
    /**
     * @private
     */
    _getToolbarBackground: function() {
        return this.initialConfig.backgroundCls || 'rp-tbar-background';
    },
    
    /**
     * @private
     */
    _isContainerAlreadyCreated: function() {
        return this._containerCreated;
    }
});

Ext.reg("rptoolbar", RP.ui.Toolbar);
//////////////////////
// ..\js\ui\TriCheckbox.js
//////////////////////
Ext.ns('RP.ui');

/**
 * @class TriCheckbox
 * This is a checkbox with the following three states: indeterminate, unchecked, and checked.
 * The values are null, false, and true respectively.
 * @namespace RP.ui
 * @extends Ext.form.Checkbox
 * @author Kevin Rice
 *
 *
 *
 * @cfg {Array} values The values of the check states.
 * @cfg {Array} checkboxCls The classes for each checkbox state.
 * @cfg {Array} validValues By default is equal to value but can be overridden. Should be
 *                          used to prevent a user from being able to click a checkbox to
 *                          a value specfic value.
 * @cfg {Object} defaultAutoCreate The auto create settings for the checkbox.
 */
RP.ui.TriCheckbox = Ext.extend(Ext.form.Checkbox, {
    // These values map by state.
    values: [null, false, true],
    checkboxCls: ['rp-ui-tri-checkbox-grayed', null, 'rp-ui-tri-checkbox-checked'],
    validValues: this.values,
    
    defaultAutoCreate: {
        tag: 'input',
        type: 'hidden',
        autocomplete: 'off'
    },
    
    /**
     * Handles rendering the checkbox.
     * @method
     * @param {Object} ct The container
     * @param {Object} position The position of the container
     */
    onRender: function(ct, position) {
        Ext.form.Checkbox.superclass.onRender.call(this, ct, position);
        
        this.innerWrap = this.el.wrap({
            tabIndex: this.tabIndex,
            cls: this.baseCls + '-wrap-inner'
        });
        this.wrap = this.innerWrap.wrap({
            cls: this.baseCls + '-wrap'
        });
        
        if (this.boxLabel) {
            this.labelEl = this.innerWrap.createChild({
                tag: 'label',
                htmlFor: this.el.id,
                cls: 'x-form-cb-label',
                html: this.boxLabel
            });
        }
        
        this.imageEl = this.innerWrap.createChild({
            tag: 'img',
            src: Ext.BLANK_IMAGE_URL,
            cls: 'x-checkbox'
        }, this.el);
        
        this.imageEl.on("click", this.onClick, this);
        
        if ((typeof this.value === "object" && this.value === null) || this.value === true) {
            this.setValue(this.value);
        }
        else {
            this.setValue(false);
        }
        this.originalValue = this.value;
    },
    
    /**
     * Gets the value of the checkbox.
     * @method
     * @return {Object} The value from the checkbox.
     */
    getValue: function() {
        return Ext.form.Checkbox.superclass.getValue.call(this);
    },
    
    /**
     * Sets the value of the checkbox. The values are checked against
     * the valid values. Values that are not in valid values need to be
     * set programmatically using set setCheckState or setCheckValueByIndex.
     * @method
     * @param {Object} value The value of the checkbox to be set.
     */
    setValue: function(value) {
        if (!(typeof value == "object" && value === null) && value !== true) {
            value = false;
        }
        if (this.validValue(value)) {
            Ext.form.Checkbox.superclass.setValue.call(this, value);
            this.updateCheckCls();
        }
        else {
            this.setCheckValueByIndex(this.getCheckIndex() + 1);
            this.setValue(this.getNextCheckState());
        }
    },
    
    /**
     * Checks to see if the value specified is in the validValues config.
     * @method
     * @param {Object} value The value to be checked to see if it is valid.
     * @return {Boolean} boolean
     */
    validValue: function(value) {
        var valid = false;
        for (var ii = 0; ii < this.validValues.length; ii++) {
            var validValue = this.validValues[ii];
            if (value === validValue) {
                valid = true;
                break;
            }
        }
        return valid;
    },
    
    /**
     * Toggles to the next value when the checkbox is clicked.
     * @method
     */
    onClick: function() {
        this.toggleValue();
    },
    
    /**
     * Gets the index of the checkbox's value.
     * @method
     * @param {Object} value The value to get the index of.
     * @return {Number} The index of the passed in value.
     */
    getCheckIndexByValue: function(value) {
        for (var i = 0; i < this.values.length; i++) {
            if (value === this.values[i]) {
                return i;
            }
        }
        return 0;
    },
    
    /**
     * Gets the current values index.
     * @method
     * @return {Number} The current index value.
     */
    getCheckIndex: function() {
        return this.getCheckIndexByValue(this.value);
    },
    
    /**
     * Sets the value of the checkbox based on an index value.
     * @param {Number} index The index to the value within values config.
     */
    setCheckValueByIndex: function(index) {
        this.value = this.values[index];
    },
    
    /**
     * Sets the check state based on a value or index passed in. This
     * will also update the class of the checkbox.
     * @method
     * @param {Object} state An index or value of the checkbox to be set
     */
    setCheckState: function(state) {
        if (typeof state === "number") {
            this.setCheckValueByIndex(state);
        }
        else {
            var index = this.getCheckIndexByValue(state);
            this.setCheckValueByIndex(index);
        }
        
        this.updateCheckCls();
    },
    
    /**
     * Updates the check class of the check box. This uses the current set index
     * and gets the class from the checkboxCls config.
     * @method
     */
    updateCheckCls: function() {
        if (!this.wrap) {
            return;
        }
        var cls = this.checkboxCls[this.getCheckIndex()];
        this.wrap.replaceClass(this._checkCls, cls);
        this._checkCls = cls;
    },
    
    /**
     * Gets the next check state of the check box.
     * @method
     * @return {Object} Value from the values array.
     */
    getNextCheckState: function() {
        return this.values[(this.getCheckIndex() + 1) % this.values.length];
    },
    
    /**
     * Switches the checkbox to the next checkstate
     * @method
     */
    toggleValue: function() {
        if (!this.disabled && !this.readOnly) {
            this.setValue(this.getNextCheckState());
        }
    },
    
    /**
     * Handles after render.
     * @method
     */
    afterRender: function() {
        Ext.form.Checkbox.superclass.afterRender.call(this);
    }
});
Ext.reg('rpuitricheckbox', RP.ui.TriCheckbox);
//////////////////////
// ..\js\ui\DatePickerToolbar.js
//////////////////////
Ext.ns("RP.ui");

RP.ui.DatePickerToolbar = Ext.extend(RP.ui.Toolbar,
{
  initComponent: function()
  {
    var config = {
      leftItems: this.initialConfig.leftItems || [],
      centerItems: this.initialConfig.datePicker,
      rightItems: this.initialConfig.rightItems || [],
      centerWidth: this.initialConfig.centerWidth || 150
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    
    RP.ui.DatePickerToolbar.superclass.initComponent.call(this);
  }
});

Ext.reg("rpdatepickertoolbar", RP.ui.DatePickerToolbar);
//////////////////////
// ..\js\help\RoboHelpLink.js
//////////////////////
/*global Ext, RP, window */

Ext.ns("RP.help");

/**
 * RoboHelp hyperlink
 * @namespace RP.help
 * @class RoboHelpLink
 * @singleton
 */
RP.help.RoboHelpLink = (function()
{
  var mainUrl;
  
  var showHelp = function(target, urlHashTranslatorFn)
  {
    var w, urlHash = window.location.hash;
    
    if (urlHash)
    {
      // Context-sensitive help.
      var mapped = urlHashTranslatorFn(urlHash);
      var url = mapped;
      
      if (mapped !== null) {
        w = window.open(url, target, "height=680,left=700,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,titlebar=no,toolbar=no,top=20,width=500,zoominherit=no");
        w.focus();
      }
      else{
        Ext.Msg.show({
          title: RP.getMessage("rp.common.misc.NoHelpFoundTitle"),
          msg: RP.getMessage("rp.common.misc.NoHelpFoundText"),
          buttons: Ext.Msg.OK,
          icon: Ext.MessageBox.WARNING
        });
      }
    }
    else
    {
      // Main help.
      w = window.open(mainUrl, target, 
                  "height=680,left=400,top=20,width=800,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,titlebar=no,toolbar=no,zoominherit=no");                  
      w.focus();
    }
  }; 
  
  return {
    /**
     * Creates a hyperlink control for RoboHelp-help implementations.
     * @method
     * @param {Object} config Config object for the hyperlink Element
     * @param {string} mainURL The RoboHelp main URL
     * @param {string} helpLabel The text label for the Help link
     * @param {string} windowTarget The browser target window name to display Help
     * @param {Function} urlHashTranslatorFn The function to call to map the 
     * URL Hash to the appropriate Help topic
     * @return {RP.ui.Hyperlink}
     */
    createHelpLink: function(config, mainURL, helpLabel, windowTarget, urlHashTranslatorFn)
    {
      mainUrl = mainURL;
            
      return Ext.ComponentMgr.create(RP.util.Object.mergeProperties(
      {
        id: 'roboHelpLink',
        xtype: "hyperlink",
        text: helpLabel,
        handler: function()
        {
          showHelp(windowTarget, urlHashTranslatorFn);
        }
      }, config));
    }
  };
})();
//////////////////////
// ..\js\help\HelpRegistry.js
//////////////////////
RP.help.HelpRegistry = (function() {
  var appIdMap = {};
  var defaultHelpURL = "";
  
  return {
    /**
     * Sets the default Help URL.  This URL is used if no Help map is found for a particular application.
     * @param {String} url
     */
    setDefaultHelpURL: function(url) {
      defaultHelpURL = url;
    },
    
    /**
     * 
     * @param {String} appId
     * @param {String} rootHelpUrl The root URL of help files; Help URLs specified in tasks are relative to this root URL
     */
    register: function(appId, rootHelpUrl) {
      appIdMap[appId] = rootHelpUrl;      
    },
    
    /**
     * Returns the Help mapper function for an app
     * @param {Object} appId
     */
    getHelpRootURL: function(appId) {
      if (appIdMap[appId]) {
        return appIdMap[appId];       
      }
      else {
        return null;
      }      
    }
  };
})();
//////////////////////
// ..\js\taskflow\BaseTaskflowWidget.js
//////////////////////
/*global Ext, RP */

Ext.ns('RP.taskflow');

/**
 * @namespace RP.taskflow
 * @class BaseTaskflowWidget
 * @extends Ext.Panel
 */
RP.taskflow.BaseTaskflowWidget = Ext.extend(Ext.Panel, {
  /**
   * Widget is disabled?
   * @type Boolean
   * @property disabledWidget
   */
  disabledWidget: false,

  /**
   * The CSS class used for displaying non-status widgets.  This should probably be the
   * standard CSS class applied to widgets, and it should be removed/overriden for status
   * widgets as this behavior seems to be backwards.  This change would require a fairly
   * substantial refactoring of the rp-taskflow.css file.
   * @type String
   * @property hyperlinkCls
   */
  hyperlinkCls: "rp-widget-hyperlink",

  /**
   * Read-only. The reference to the task form
   * @type Object
   */
  taskForm: null,

  initComponent: function(){
    this._taskContext = this.createDefaultTaskContext();

    if (this.initialConfig.xtype) {
      var signature = RP.core.ComponentMgr.getSignature(this.initialConfig.xtype);

      for (var property in signature) {
        var sig = signature[property];
        if (sig && sig.required && sig.direction.search(/in/i) !== -1 && !this.initialConfig[property]) {
          logger.logDebug("[BaseTaskflowWidget] Required input config property: " +
          property +
          " is missing on Widget: " +
          this._getWidgetTitle());
        }
      }

    }

    // try and default to rp-widget-hyperlink because it applies the styles to correctly
    // display the right edge of the widget...  this style then messed up status widgets,
    // so they can pass in "" for extraCls to prevent it from happening. RPWEB-2844
    var extraCls = Ext.value(this.initialConfig.extraCls, this.hyperlinkCls, true);
    extraCls += (this.initialConfig.hasPanel ? "" : " rp-widget-no-panel");

    this.initialConfig.cls = "rp-vantage-item" + (extraCls ? (" " + extraCls) : "");
    this.initialConfig.anchor = "100%";

    this.initialConfig.frame = true;
    this.title = this._getWidgetTitle();

    Ext.apply(this, this.initialConfig);
    RP.taskflow.BaseTaskflowWidget.superclass.initComponent.call(this);

    if (this.disabledWidget) {
      this.addClass("rp-disabled-widget");
    }

    this.addEvents("rpext-widgetclick", "rpext-widgetchange");

    if (this.initialConfig.appEventHandlers) {
      RP.event.AppEventProxy.registerListener(this, true);

      Ext.each(this.initialConfig.appEventHandlers, function(eh){
        RP.event.AppEventProxy.subscribe(eh.event, this, eh.handler, eh.removeDuplicates);
      }, this);
    }

    this.on("destroy", function(){
      this._taskflow = null; // dereference
    }, this);
  },

  render: function(){
    RP.taskflow.BaseTaskflowWidget.superclass.render.apply(this, arguments);
    this.onRendered();
  },

  onRendered: function(c){
    this.mon(this.header, "click", this.onClick, this);
    this.mon(this.body, "click", this.onClick, this);

    if (this.onPostRender) {
      this.onPostRender();
    }
  },

  onPostRender: function(){
    this.setActive(this.activeWidget);
    this._writeLine();
    this.el.addClassOnClick("rp-pressed-item");
    this.el.addClassOnOver("rp-hover-item");
  },

  /**
   * Overrideable method to create default task context if the URL doesn't specify a
   * task context.
   */
  createDefaultTaskContext: function(){
    return {};
  },

  /**
   * Gets the local context object
   */
  getLocalContext: function(){
    return this.getTaskflow().getTaskMetadata(this.getTaskId()).getLocalContext();
  },

  /**
   * Gets the value of a local context variable
   * @param {String} name
   */
  getLocalContextValue: function(name){
    return this.getLocalContext()[name];
  },

  /**
   * Gets the value of a local context variable
   * @param {String} name
   * @param {Mixed} value
   */
  setLocalContextValue: function(name, value){
    var lc = this.getTaskflow().getTaskMetadata(this.getTaskId()).getLocalContext();
    lc[name] = value;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * Called by the taskflow engine to set the taskflow
   * @param {RP.interfaces.ITaskflow2} taskId The task ID
   * @method
   */
  setTaskflow: function(tf){
    this._taskflow = tf;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * Return the taskflow reference set by setTaskflow()
   * @return {RP.interfaces.ITaskflow2} The taskflow
   * @method
   */
  getTaskflow: function(){
    return this._taskflow;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * Sets the task ID to associate with this widget
   * @param {String} taskId The task ID
   * @method
   */
  setTaskId: function(taskId){
    this._taskId = taskId;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * Gets the task ID associated with this widget
   * @return {String} taskId The task ID
   * @method
   */
  getTaskId: function(){
    return this._taskId;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * Initialize the task's local context at runtime.  Designed to be overriden
   * by subclasses.
   * @method
   */
  initializeLocalContext: Ext.emptyFn,

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @return {Boolean}
   */
  getEnabled: function(){
    return !this.disabledWidget;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @param {Boolean} enabled
   */
  setEnabled: function(enabled){
    this.disabledWidget = !enabled;
    if (this.disabledWidget) {
      this.addClass("rp-disabled-widget");
    }
    else {
      this.removeClass("rp-disabled-widget");

      // This is necessary in IE.  For some reason, the disabled attribute doesn't go away...
      if (this.rendered) {
        this.header.dom.disabled = false;
      }
    }
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   *
   * This needs to be overridden if you want to present a UI for the task.
   * @method
   */
  createTaskForm: Ext.emptyFn,

  /**
   * RP.interfaces.ITaskWidget implementation
   * @return {Object}
   */
  getTaskForm: function(){
    return this.taskForm;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   */
  raiseWidgetClick: function(){
    if (!this.disabledWidget) {
      this.fireEvent("rpext-widgetclick", this);
    }
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @param {Function} delegate
   */
  subscribeWidgetClick: function(delegate){
    this.mon(this, "rpext-widgetclick", delegate);
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @param {Function} delegate
   */
  unsubscribeWidgetClick: function(delegate){
    this.mun(this, "rpext-widgetclick", delegate);
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   */
  raiseWidgetChange: function(){
    this.fireEvent("rpext-widgetchange");
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @param {Function} delegate
   */
  subscribeWidgetChange: function(delegate){
    this.mon(this, "rpext-widgetchange", delegate);
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @param {Function} delegate
   */
  unsubscribeWidgetChange: function(delegate){
    this.mun(this, "rpext-widgetchange", delegate);
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * Gets the task's context.
   * @method
   * @return {Object} The task's current context
   */
  getTaskContext: function(){
    return this._taskContext;
  },

  /**
   * RP.interfaces.ITaskWidget implementation
   * @param {Object} taskContext
   */
  setTaskContext: function(taskContext){
    var oldCtx = RP.core.PageContext.encodeContextAsHashValue(this._taskContext);
    var newCtx = RP.core.PageContext.encodeContextAsHashValue(taskContext);

    if (oldCtx !== newCtx) {
      if (RP.globals.CURRENT_HASH.taskID === this.getTaskId()) {
        RP.core.PageContext.updateTaskContext(taskContext);
      }

      this._taskContext = taskContext;
      this.onTaskContextChanged();
    }
  },

  /**
   * Overridable method for subclasses to handle task context changes.  Use
   * getTaskContext() to retrieve the current task context.
   * @private
   * @method
   */
  onTaskContextChanged: Ext.emptyFn,

  /**
   * RP.interfaces.ITaskWidget implementation
   * @method
   * @param {Boolean} isActive
   */
  setActive: function(isActive){
    if (isActive) {
      this.activeWidget = true;
      this.addClass("rp-active-item");
    }
    else {
      this.activeWidget = false;
      this.removeClass("rp-active-item");
    }
  },

  onClick: function(ev, target, options){
    if (ev) {
      ev.stopEvent();
      target.blur();
    }

    this.raiseWidgetClick();
  },

  _getWidgetTitle: function(){
    // this.uiTitle comes from derived class code, the rest comes from widgetCfg config option from either
    // task registration or taskflow roster item.
    return (this.initialConfig.title || this.initialConfig.uiTitle || this.title || this.uiTitle || "&nbsp;");
  },

  _writeLine: function(){
    if (this.disabledWidget) {
      this.header.dom.disabled = true;
    }

    this.header.dom.innerHTML = String.format("<div class='x-panel-header-text rp-ellipsis'>{0}</div>", RP.getMessage(this.title));

    this._addWidgetQtip();
  },

  _addWidgetQtip: function(){
    var element = this.header.first();
    var textWidth = element.getTextWidth();
    var containerWidth = element.getWidth();

    if(textWidth > containerWidth){
      element.set({
        qtip: RP.getMessage(this.title)
      });
    }
  },

  _getTaskFormItemId: function(){
    return this.initialConfig.itemId + "-taskform";
  }
});


RP.iimplement(RP.taskflow.BaseTaskflowWidget, RP.interfaces.ITaskWidget);
//////////////////////
// ..\js\taskflow\TaskflowWidgetRegistry.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.taskflow");


/**
 * @namespace RP.taskflow
 * @class TaskflowWidgetRegistry
 * @singleton
 * @extends none
 * Maintains a registry of taskflow widgets
 */
RP.taskflow.TaskflowWidgetRegistry = function() {
  function xtypeFn(appId, xtype) {
    return appId + "-" + xtype;
  }
  
  var registeredWidgets = {};
  
  return {
    /**
     * Registers a new xtype
     * @param (Object) config Widget configuration object.
     * <b>Required:</b><ul>
     * <li>appId: {string} Application identifier; must be lower-case
     * <li>description: {string} Description of this widget
     * <li>xtype: {string} Widget xtype; must be lower-case and unique in the appId space; used as xtype name, where full xtype name = [appId]-[xtype]
     * <li>classRef: {Constructor} Type associated with this widget
     * </ul><b>Optional:</b><ul>
     * <li>paramArray: {array of Object} Input/output parameters list of xtype.
     * </ul>
     * @return nothing
     */
    register: function(config) {
      // Enforce naming conventions.
      if ((config.appId.length === 0) || (config.appId.toLowerCase() !== config.appId)) {
        throw new RP.Exception("appId must be lower-case and not empty - " + config.appId);
      }
      
      if ((config.xtype.length === 0) || (config.xtype.toLowerCase() !== config.xtype)) {
        throw new RP.Exception("xtype must be lower-case and not empty - " + config.xtype);
      }
      
      logger.logTrace("[TaskflowWidgetRegistry] Adding to registry. appId: " + config.appId + "; xtype: " + config.xtype);
      
      var xtypeName = xtypeFn(config.appId, config.xtype);
      
      //RPWEB-3410 creates empty array if paramArray field not passed in.
      if(config.paramArray === undefined || config.paramArray === null) {
          config.paramArray = [];
      }
      
      // Register as xtype.
      RP.core.ComponentMgr.register(xtypeName, config.classRef, config.paramArray);
      
      registeredWidgets[config.xtype] = Ext.apply({}, config);
    },
    
    /**
     * Creates a new config object with the appropriate xtype and merged config object
     * @param (string) appId Application identifier
     * @param (string) xtype Widget xtype name within its application scope
     * @param (Object) config Optional config object to merge
     * @return {Object} A merged config object with at least 'xtype' specified.
     */
    getWidgetXtype: function(appId, xtype, config) {
      var cfg = config ||
      {};
      
      cfg.xtype = xtypeFn(appId, xtype);
      return cfg;
    },
    
    /**
     * Gets all registered widgets
     */
    getAll: function() {
      var defs = [];
      
      Ext.iterate(registeredWidgets, function(name, def) {        
        defs.push(Ext.apply({}, def));
      });
      
      return defs;
    }
  };
}();

RP.registerWidget = RP.taskflow.TaskflowWidgetRegistry.register;
RP.getWidgetXtype = RP.taskflow.TaskflowWidgetRegistry.getWidgetXtype;
//////////////////////
// ..\js\taskflow\BaseTaskflowWidgetWithPanel.js
//////////////////////
/*global Ext, RP */

Ext.ns('RP.taskflow');

/**
 * @namespace RP.taskflow
 * @class BaseTaskflowWidgetWithPanel
 * @extends RP.taskflow.BaseTaskflowWidgetWithCount
 *
 * Extends the "count" widget to have a panel below the widget to display additional information.
 */
RP.taskflow.BaseTaskflowWidgetWithPanel = Ext.extend(RP.taskflow.BaseTaskflowWidget, {
  /**
   * @cfg {Ext.Component} panel The panel to include as part of the widget
   */
  /**
   * @cfg {Number} panelHeight The height of the panel to create.  Default is 50.
   */
  initComponent: function() {
    var config = this.initialConfig;
    var bodyCfg = config.bodyCfg || {};
    
    config.bodyCfg = RP.util.Object.mergeProperties({
      height: bodyCfg.height || config.panelHeight || 50,
      cls: "rp-widget-panel"
    }, bodyCfg);
    
    config.hasPanel = true;
    config.extraCls = (config.extraCls || "") + " rp-widget-with-panel";
    config.layout = config.layout || "fit";
    config.items = config.panel;
    
    RP.taskflow.BaseTaskflowWidgetWithPanel.superclass.initComponent.call(this);
  },
  
  onPostRender: function() {
    this.setActive(this.activeWidget);
    this._writeLine();
    this.el.addClassOnClick("rp-pressed-panel");
    this.el.addClassOnOver("rp-hover-item");
  }
});
//////////////////////
// ..\js\taskflow\BaseTaskflowHyperlinkWidget.js
//////////////////////
/*global Ext, RP */

Ext.ns('RP.taskflow');

/**
 * @namespace RP.taskflow
 * @class BaseTaskflowHyperlinkWidget
 * @extends RP.taskflow.BaseTaskflowWidget
 * A widget implemented as an HTML hyperlink (so you get the browser's context menu to open
 * it in a new tab or page
 */
RP.taskflow.BaseTaskflowHyperlinkWidget = Ext.extend(RP.taskflow.BaseTaskflowWidget, {
  initComponent: function() {
    Ext.apply(this, {
      extraCls: "rp-widget-hyperlink"
    });
    
    RP.taskflow.BaseTaskflowHyperlinkWidget.superclass.initComponent.call(this);
  },
  
  _writeLine: function() {
    if (this.disabledWidget) {
      this.header.dom.disabled = true;
    }
    
    this.header.dom.innerHTML = String.format("<div class='x-panel-header-text'>{0}</div>", this.title);    
  },
  
  _addParamToUrl: function(p) {
    var param = this.initialConfig[p];
    
    if (Ext.isDate(this.initialConfig[p])) {
      param = Date.encodeDateToURL(this.initialConfig[p]);
    }
    
    return param;
  }
  
});
//////////////////////
// ..\js\taskflow\ExternalAppWidget.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class ExternalAppWidget
 * @extends RP.taskflow.BaseTaskflowWidget
 */
RP.taskflow.ExternalAppWidget = Ext.extend(RP.taskflow.BaseTaskflowWidget,
{
  /**
   * @cfg {String} uiTitle
   * The widget's title to display in the UI
   */
   
  /**
   * @cfg {String} url
   * The URL to navigate to
   */
   
  /**
   * @cfg {String} frameTitle
   * Header title for the task form; if not specified, then the header will not be displayed
   */
   
  createTaskForm: function()
  {
    this.taskForm = new RP.taskflow.ExternalAppTaskForm(
    {
      url: this.initialConfig.url, 
      frameTitle: this.initialConfig.frameTitle,
      itemId: this._getTaskFormItemId()
    });
  }
});

RP.registerWidget(
{
  appId: "stash",
  description: "External app widget",
  xtype: "external-app",
  classRef: RP.taskflow.ExternalAppWidget,
  paramArray: []
});
//////////////////////
// ..\js\taskflow\BaseTaskForm.js
//////////////////////
/*global Ext, RP */

Ext.ns('RP.taskflow');

/**
 * @namespace RP.taskflow
 * @class BaseTaskForm
 * @extends Ext.Panel
 * Used as a base class for task forms that are launched by the taskflow widgets.  This
 * class adds Application Event handling for a panel.  TaskflowFrame interfaces with
 * this to automatically set this panel to the Awake or Asleep state when it switches
 * task forms.  A task form configures Application Events to listen to by setting the
 * "appEventHandlers" config option.
 */
RP.taskflow.BaseTaskForm = Ext.extend(Ext.Panel, {
  /**
   * @cfg {Boolean} noHeader
   * True to render without the title bar
   */
  noHeader: false,
  
  /**
   * @method
   */
  initComponent: function() {
    logger.logTrace("[BaseTaskForm] initComponent itemId: " + this.itemId);
    
    // Register this container as a listener to App Events.
    RP.event.AppEventProxy.registerListener(this, false);
    if (this.initialConfig.appEventHandlers) {
      Ext.each(this.initialConfig.appEventHandlers, function(eh) {
        logger.logTrace("[BaseTaskForm] Subscribing to app event: " + eh.event);
        RP.event.AppEventProxy.subscribe(eh.event, this, eh.handler, eh.removeDuplicates);
      }, this);
    }
    
    Ext.apply(this, {
      frame: !this.noHeader
    });
    
    RP.taskflow.BaseTaskForm.superclass.initComponent.call(this);
  },
  
    
  /**
   * @property
   * @type String
   * The message that should be displayed when a dirty state is encountered.  
   * Override this message with a message id to customize this to an application specific message.
   */
  dirtyWarningMessageId: "rp.common.misc.DirtyWarningMessage",
  
  /**
   * isDirty provides a hook by which changing pages when the taskform is
   * considered dirty should alert the user that an unsaved change is pending.
   * 
   * Implement this method to return true when the taskform is considered dirty and stop
   * the user from leaving the current page.  This functionality is in addition to the 
   * onBeforeDeactivate feature.  Returning true will alert the user of unsaved changes.
   * 
   * @return {Boolean} True if dirty state, false otherwise.
   */
  isDirty: Ext.emptyFn,
  
  /**
   * Override base destroy() to unhook app events
   * @method
   */
  destroy: function() {
    logger.logTrace("[BaseTaskForm] destroyed itemId: " + this.itemId);
    
    // Unhook myself from AppEventProxy.
    RP.event.AppEventProxy.unregisterListener(this);
    
    RP.taskflow.BaseTaskForm.superclass.destroy.call(this);
  },
  
  /**
   * RP.interfaces.ITaskForm implementation
   * @method onActivate
   */
  onActivate: function() {
    logger.logTrace("[BaseTaskForm] onActivate itemId: " + this.itemId);
    
    window.onbeforeunload = function() {
        if(Ext.isFunction(this.isDirty) && this.isDirty() === true) {
            // Check to see if the user is "actually" leaving the page.
            this.checkLeavePage.defer(3000);
            return RP.getMessage(this.dirtyWarningMessageId);
        }
        // The page isn't dirty, since the onbeforeload is firing and 
        // the page isn't dirty, we can safely say the user is leaving the page.
        window.leavingPage = true;

    }.createDelegate(this);
    
    // Resume my events (any previously queued events will now fire)
    RP.event.AppEventProxy.setState(this, true);
  },
  
  checkLeavePage: function() {
      // Check to see if there are AJAX requests, if so we may be leaving the page.
      if(Ext.Ajax.activeRequests > 0) {
          window.leavingPage = true;
      }
      // Hang out for 3 seconds, if we are still on the page, clear the leaving page flag.
      var reset = function() {
          window.leavingPage = false;
      }.defer(3000);
  },
  
  /**
   * RP.interfaces.ITaskForm implementation
   * Return false to cancel deactivation of this task
   * @method onBeforeDeactivate
   */
  onBeforeDeactivate: function(cb) {
    cb(true);
  },
  
  /**
   * RP.interfaces.ITaskForm implementation
   * @method onDeactivate
   */
  onDeactivate: function() {
    logger.logTrace("[BaseTaskForm] onDeactivate itemId: " + this.itemId);
    
    // Suspend my events for now (actually queue events while I'm sleeping)...
    RP.event.AppEventProxy.setState(this, false);
  },
  
  /**
   * RP.interfaces.ITaskForm implementation
   * @method getUrlHash
   */
  getUrlHash: function() {
    if (!this.__urlHash) {
      this._urlHash = Ext.id();
    }
    return this.__urlHash;
  },
  
  /**
   * RP.interfaces.ITaskForm implementation
   * @method setUrlHash
   */
  setUrlHash: function(hash) {
    this.__urlHash = hash;
  }
});

RP.iimplement(RP.taskflow.BaseTaskForm, RP.interfaces.ITaskForm);

RP.core.ComponentMgr.register("rptaskform", RP.taskflow.BaseTaskForm, []);
//////////////////////
// ..\js\taskflow\BaseTaskFormWithDateMenu.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @class RP.taskflow.BaseTaskFormWithDateMenu
 * @extends RP.taskflow.BaseTaskForm
 *
 * <p>A simple extension that allows you to add a date menu in the header area.  It
 * is important to note that this component does not display the traditional header,
 * instead it displays a top toolbar that just looks like a header.  Do not use this
 * if you need to use the tbar configuration.</p>
 *
 * <p>It is important to note that in Ext JS 4 there will be native support for rendering
 * components in the header, so this will be refactored then.</p>
 *
 * @cfg {Object} dateMenuConfig (required) The configuration object of the {@link RP.ui.DirectionalDateField}.
 */
RP.taskflow.BaseTaskFormWithDateMenu = Ext.extend(RP.taskflow.BaseTaskForm, {
    // private
    initComponent: function() {
        if (!this.dateMenuConfig) {
            throw new RP.Exception("Missing required dateMenuConfig.");
        }
        
        this._dateMenu = new RP.form.DirectionalDateField(this.dateMenuConfig);
        
        Ext.apply(this, {
            noHeader: true,
            header: false,
            tbar: this.createHeaderToolbar()
        });
        
        RP.taskflow.BaseTaskFormWithDateMenu.superclass.initComponent.apply(this, arguments);
    },
    
    /**
     * Retrieves the date menu reference for external access purposes.
     * @return {RP.form.DirectionalDateField} The date picker of this panel.
     */
    getDateMenu: function() {
        return this._dateMenu;
    },
    
    /**
     * Creates the header toolbar.
     */
    createHeaderToolbar: function() {
        return {
            // divides the horizontal space up into 3 even sections
            // uses the pack: "center" configuration to center align the date picker
            xtype: "container",
            layout: {
                type: "hbox",
                align: "middle"
            },
            cls: "rp-header-toolbar",
            defaults: {
                flex: 1
            },
            items: [{
                xtype: "box",
                html: this.title
            }, {
                xtype: "container",
                layout: {
                    type: "hbox",
                    pack: "center"
                },
                items: this.getDateMenu()
            }, {
                xtype: "box"
            }]
        };
    }
});
//////////////////////
// ..\js\taskflow\ExternalAppTaskForm.js
//////////////////////
/*global Ext, RP */

Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class ExternalAppTaskForm
 * @extends RP.taskflow.BaseTaskForm
 *
 * Implements task form that renders an IFRAME for external web apps
 */
RP.taskflow.ExternalAppTaskForm = Ext.extend(RP.taskflow.BaseTaskForm,
{
  /**
   * @cfg {String} url
   * The URL to navigate to
   */
   
  /**
   * @cfg {frameTitle} String
   * Header title; if not specified, then the header will not be displayed
   */
  
  initComponent: function()
  {
    var iframe = new Ext.ux.IFrameComponent(
    {
      url: this.initialConfig.url || "about:blank"
    });
    
    Ext.apply(this,
    {
      items: iframe,
      layout: "fit",
      header: this.initialConfig.frameTitle? true : false,
      noHeader: this.initialConfig.frameTitle? false : true,
      title: this.initialConfig.frameTitle
    });
    
    RP.taskflow.ExternalAppTaskForm.superclass.initComponent.call(this);
  }
});

RP.core.ComponentMgr.register("rpexternalapp", RP.taskflow.ExternalAppTaskForm, []);
//////////////////////
// ..\js\taskflow\TaskContext.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class TaskContext
 * @extends Ext.util.Observable
 *
 * Task context business object.  Contains task configuration as well as runtime data, etc.
 */
RP.taskflow.TaskContext = Ext.extend(Ext.util.Observable, {
  constructor: function(config) {
    this._taskCfg = config.taskCfg;
    this._localContext = Ext.apply({}, config.initialContext);
    
    this.addEvents("statuschanged", "localcontextchanged");
    
    RP.taskflow.TaskContext.superclass.constructor.call(this);
  },
  
  /**
   * Get the task configuration
   * @method
   * @return {Object} The task configuration object
   */
  getConfig: function() {
    return this._taskCfg;
  },
  
  /**
   * RP.interfaces.ITaskContext implementation.
   * Subscribe an event handler to the task's status change event.
   * @method
   * @param {Function} handler
   */
  subscribeStatusChange: function(handler) {    
    this.on("statuschanged", handler);
  },
  
  /**
   * RP.interfaces.ITaskContext implementation.
   * Unsubscribe an event handler from the task's status change event.
   * @method
   * @param {Function} handler
   */
  unsubscribeStatusChange: function(handler) {    
    this.un("statuschanged", handler);
  },
  
  /**
   * RP.interfaces.ITaskContext implementation.
   * Fires the task's status change event.
   * @method
   */
  fireStatusChange: function() {    
    this.fireEvent("statuschanged", this);
  },
  
  /**
   * RP.interfaces.ITaskContext implementation.
   * Gets the task's runtime "local context".  The "local context" is a glob
   * of data generated at runtime pertaining to this task
   * @method
   * @return {Object} local context object
   */
  getLocalContext: function(handler) {    
    return this._localContext;
  }
});

RP.iimplement(RP.taskflow.TaskContext, RP.interfaces.ITaskContext);
//////////////////////
// ..\js\taskflow\Taskflow.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class Taskflow
 * @extends Ext.util.Observable
 *
 * Taskflow business object.  This class translates a taskflow in JSON to a taskflow
 * instance hostable by the taskflow engine.
 */
RP.taskflow.Taskflow = Ext.extend(Ext.util.Observable, {
    /**
     * @cfg {Object} listeners
     * <p>An object containing delegates to handle taskflow life-cycle events.  Consists of the
     * following properties:</p>
     * <p><b>scope</b>: {Object} The scope of all delegates
     * <p><b>init</b>: {Function} Delegate to invoke to initialize taskflow. void delegate(Object taskflow, Function continueFn)
     * <p><b>afterInit</b>: {Function} Delegate to invoke after initialization (for customer use only). void delegate(Object taskflow)
     * <p><b>beforeStart</b>: {Function} Delegate to invoke before taskflow is run. void delegate(Object taskflow)
     * <p><b>complete</b>: {Function} Delegate to invoke after taskflow completes. void delegate(Object taskflow)
     */
    /**
     * @cfg {Object} uiConfig
     * <p>An object containing options for the user interface.  Consists of the following properties:</p>
     * <p><b>disableOnComplete</b>: {Boolean} Disable the taskflow upon completion?
     * <p><b>description</b>: {String} HTML to display in the taskflow's description area
     */
    /**
     * @cfg {Array of Object} roster
     * <p>An array of task objects.  Each task object has the following required properties:</p>
     * <p><b>id</b>: {String} Globally unique task ID
     * <p><b>widget</b>: {Object} Config object to create the widget with. If you wish to reference a registered widget, use the RP.getWidgetXtype()
     * Additionally, each task object may have the following optional properties:</p>
     * <p><b>statusCalculation</b>: {Function} boolean delegate(Object localContext, Object flowContext)
     * <ul>
     * <li>Return boolean flag indicating whether or not this task is completed
     * <li>If not specified, the default implementation returns 'true'
     * <li>localContext is the task's context object, and flowContext is the taskflow's context object
     * </ul>
     * <p><b>inParameterMapping</b>: {String}
     * <ul>
     * <li>Map the taskflow's flowContext values to the task's localContext
     * <li>Used to retrieve taskflow values
     * <li>The property name is the flowContext name, and the value (string) is the localContext name
     * </ul>
     * <p><b>outParameterMapping</b>: {String}
     * <ul>
     * <li>Map the task's localContext to the taskflow's flowContext
     * <li>Used to save this task's values to the taskflow
     * <li>The property name is the flowContext name, and the value (string) is the localContext name
     * </ul>
     * <p><b>dependencies</b>: {Array of String}
     * <ul>
     * <li>Used to specify other task(s) that must be completed before this task can execute
     * <li>Each array element is the ID value of the other tasks in the containing taskflow
     * <li>A taskflow cannot reference another taskflow, so dependencies must reference tasks in the same taskflow!
     * </ul>
     */
    /**
     * The taskflow's context.  Contains values accumulated/updated during execution of this taskflow.
     * @type Object
     * @property flowContext
     */
    /**
     * The taskflow's copy of its initial configuration object
     * @type Object
     * @property initialConfig
     */
    constructor: function(config) {
        RP.taskflow.Taskflow.superclass.constructor.call(this, null);
        
        // Set up properties.
        this.initialConfig = Ext.apply({}, config);
        this.flowContext = Ext.apply(config.initialContext || {}, {});
        
        // initialContext should affect this.flowContext only, and not affect this._context...
        //this._context = RP.mergeProperties(config.initialContext || {}); 
        this._context = {};
        
        this._initialized = false;
        this._completed = false;
        this._roster = null;
        
        this.addEvents(        /**
         * @event rpext-init
         * Fires after this taskflow has finished its base initialization.
         * @param {RP.taskflow.Taskflow} this
         * @param {Function} continueFn
         */
        "rpext-init",        /**
         * @event rpext-initialized
         * Fires after this taskflow has initialized
         * @param {RP.taskflow.Taskflow} this
         */
        "rpext-initialized",        /**
         * @event rpext-starting
         * Fires before this taskflow runs
         * @param {RP.taskflow.Taskflow} this
         */
        "rpext-starting",        /**
         * @event rpext-task-completed
         * Fires when a task in this taskflow is completed
         * @param {RP.taskflow.Taskflow} this
         * @param {Object} taskConfig
         */
        "rpext-task-completed",        /**
         * @event rpext-completed
         * Fires after this taskflow has completed
         * @param {RP.taskflow.Taskflow} this
         */
        "rpext-completed");
        
        if (Ext.isObject(config.listeners)) {
            var l = config.listeners;
            var scope = l.scope || this;
            
            if (l.init) {
                this.addInitListener(l.init.createDelegate(scope));
            }
            
            if (l.afterInit) {
                this.addPostInitListener(l.afterInit.createDelegate(scope));
            }
            
            if (l.beforeStart) {
                this.addTaskflowStartListener(l.beforeStart.createDelegate(scope));
            }
            
            if (l.complete) {
                this.addTaskflowCompletedListener(l.complete.createDelegate(scope));
            }
        }
    },
    
    /**
     * getTaskflowContext()
     * Called to get the taskflow context
     * @method
     */
    getTaskflowContext: function() {
        return this._context;
    },
    
    /**
     * mergeTaskflowContext(context)
     * Called to merge new values into the taskflow context
     * @method
     * @param {Object} context The taskflow context object to set to
     */
    mergeTaskflowContext: function(context) {
        this._context = RP.mergeProperties(this._context, context);
        RP.core.PageContext.updateTaskflowContext(context);
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation.
     * @method
     */
    getTaskflowUIConfig: function() {
        return this.initialConfig.uiConfig;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation.
     * @method
     */
    addInitListener: function(cb) {
        this.on("rpext-init", cb);
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation.
     * @method
     */
    addPostInitListener: function(cb) {
        this.on("rpext-initialized", cb);
    },
    
    /**
     * addTaskflowStartListener(cb)
     * Add a delegate to call when this taskflow after it's started to run.
     * @method
     */
    addTaskflowStartListener: function(cb) {
        this.on("rpext-starting", cb);
    },
    
    addTaskCompletedListener: function(cb) {
        this.on("rpext-task-completed", cb);
    },
    
    /**
     * addTaskflowCompletedListener(cb)
     * Add a delegate to call when this taskflow has completed.
     * @method
     */
    addTaskflowCompletedListener: function(cb) {
        this.on("rpext-completed", cb);
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    setRebuildHandler: function(cb) {
        this._rebuildHandler = cb;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    rebuild: function(config) {
        this._rebuildHandler(config);
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    isInitialized: function() {
        return this._initialized;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    isCompleted: function() {
        return this._completed;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * An overridden implementation must call this superclass method last.
     * @method
     */
    initTaskflow: function(initCompletedFn) {
        this._completed = false;
        this._roster = new Ext.util.MixedCollection();
        
        var evtName = "rpext-init";
        
        if (Ext.isObject(this.events[evtName])) {
            var count = this.events[evtName].listeners.length;
            
            var continueFn = function() {
                count--;
                if (count === 0) {
                    this._signalInitialized(initCompletedFn);
                }
            };
            
            this.fireEvent(evtName, this, continueFn.createDelegate(this));
        }
        else {
            this._signalInitialized(initCompletedFn);
        }
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    getTaskflowDescription: function() {
        if (this.initialConfig.uiConfig) {
            return this.initialConfig.uiConfig.description;
        }
        return null;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    setTaskflowDescription: function(desc) {
        this.initialConfig.uiConfig = this.initialConfig.uiConfig || {};
        this.initialConfig.uiConfig.description = desc;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    getDefaultTaskId: function() {
        return this.initialConfig.defaultTaskId;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    getTaskflowRoster: function(tfConfig) {
        return this.initialConfig.roster;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    createTaskWidgetConfig: function(taskConfig) {
        if (!taskConfig.id) {
            logger.logDebug("[Taskflow] Roster item missing 'ID' member.");
            return null;
        }
        
        var signature = RP.core.ComponentMgr.getSignature(taskConfig.widget.xtype);
        var tfCtx = this._mapFlowToLocalContext(taskConfig.inParameterMapping, {}, signature);
        
        var widgetConfig = RP.mergeProperties(taskConfig.widget, tfCtx);
        
        this._roster.add(taskConfig.id, {
            taskContext: this._createTaskMetadata(taskConfig, tfCtx),
            widgetCfg: widgetConfig,
            completed: null
        });
        
        return widgetConfig;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    getTaskMetadata: function(taskID) {
        return this._roster.get(taskID).taskContext;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * An overridden implementation must call this method after it has done its custom handling.
     * @method
     */
    startTaskflow: function(tfConfig) {
        this.fireEvent("rpext-starting", this);
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    updateStatus: function() {
        if (this._completed) {
            return;
        }
        
        // Copy output parameters' values from each task into the flow context.
        this._roster.each(function(rosterItem) {
            var taskContext = rosterItem.taskContext;
            var localContext = RP.mergeProperties(rosterItem.widgetCfg, taskContext.getLocalContext());
            var signature = RP.core.ComponentMgr.getSignature(localContext.xtype);
            var taskCfg = taskContext.getConfig();
            
            this._mapLocalToFlowContext(taskCfg.outParameterMapping, localContext, signature);
        }, this);
        
        // Copy flow context values to each task's input parameters, and update each task's status.
        var waiter = null;
        var waitObjs = [];
        
        var waitedReturnFn = function(rosterItem) {
            if (Ext.isObject(waiter)) { // if everything was synchronous, the waiter object won't be instantiated
                waiter.remove(rosterItem);
            }
            else {
                waitObjs.remove(rosterItem);
            }
        };
        
        this._roster.each(function(rosterItem) {
            var taskContext = rosterItem.taskContext;
            var localContext = RP.mergeProperties(rosterItem.widgetCfg, taskContext.getLocalContext());
            var signature = RP.core.ComponentMgr.getSignature(localContext.xtype);
            var taskCfg = taskContext.getConfig();
            
            this._mapFlowToLocalContext(taskCfg.inParameterMapping, taskContext.getLocalContext(), signature);
            
            // NOTE: the following is not needed if all apps stop using the widget's initialConfig
            // as the local context.  This is here only to maintain backwards compatibility (e.g.,
            // for SiteMgr).  rosterItem.widgetCfg === widget.initialConfig
            this._mapFlowToLocalContext(taskCfg.inParameterMapping, rosterItem.widgetCfg, signature);
            
            // Query this task's status (which it can process asynchronously...)
            this._calculateTaskStatus(rosterItem, waitObjs, waitedReturnFn);
        }, this);
        
        var tfCompletedCalcFn = (function() {
            var taskflowComplete = true;
            
            this._roster.each(function(rosterItem) {
                taskflowComplete = taskflowComplete && rosterItem.completed;
            }, this);
            
            if (taskflowComplete) {
                this._completed = true;
                this.fireEvent("rpext-completed", this);
            }
        }).createDelegate(this);
        
        if (Ext.isEmpty(waitObjs)) {
            tfCompletedCalcFn();
        }
        else {
            waiter = new RP.util.Waiter({
                objs: waitObjs,
                handler: tfCompletedCalcFn
            });
        }
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    isTaskCompleted: function(taskID) {
        var rosterItem = this._roster.get(taskID);
        return rosterItem.completed;
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    setTaskCompleted: function(taskID, completedStatus) {
        this._setTaskCompleted(this._roster.get(taskID), completedStatus);
        this.updateStatus();
    },
    
    /**
     * RP.interfaces.ITaskflow2 implementation
     * @method
     */
    getTaskConfig: function(taskID) {
        var rosterItem = this._roster.get(taskID);
        return rosterItem.taskContext.getConfig();
    },
    
    /** @private */
    _createTaskMetadata: function(taskCfg, initialContext) {
        return new RP.taskflow.TaskContext({
            taskCfg: Ext.apply({}, taskCfg),
            initialContext: initialContext
        });
    },
    
    /** @private */
    _setTaskCompleted: function(rosterItem, completedStatus) {
        // If this task has just switched to completed, fire event...
        var fire = (completedStatus && !rosterItem.completed);
        
        rosterItem.completed = completedStatus;
        if (fire) {
            this.fireEvent("rpext-task-completed", this, rosterItem.taskContext.getConfig());
        }
    },
    
    /** @private */
    _mapLocalToFlowContext: function(map, localContext, signature) {
        if (map) {
            for (var property in map) {
                var localName = map[property];
                
                if (!signature[localName] || signature[localName].direction.search(/out/i) === -1) {
                    logger.logDebug("[Taskflow] Parameter " + localName + " doesn't exist");
                }
                this.flowContext[property] = localContext[localName];
            }
        }
        return this.flowContext;
    },
    
    /** @private */
    _mapFlowToLocalContext: function(map, localContext, signature) {
        if (map) {
            for (var property in map) {
                var localName = map[property];
                
                if (!signature[localName] || signature[localName].direction.search(/in/i) === -1) {
                    logger.logDebug("[Taskflow] Parameter " + localName + " doesn't exist");
                }
                localContext[localName] = this.flowContext[property];
            }
        }
        return localContext;
    },
    
    /** @private */
    _calculateTaskStatus: function(rosterItem, waitObjs, waitedResultFn) {
        var taskContext = rosterItem.taskContext;
        var taskCfg = taskContext.getConfig();
        var statusCalculation = taskCfg.statusCalculation;
        var localContext = RP.mergeProperties(rosterItem.widgetCfg, taskContext.getLocalContext());
        
        var statusResultFn = (function(result) {
            this._setTaskCompleted(rosterItem, result);
            waitedResultFn(rosterItem);
        }).createDelegate(this);
        
        if (taskCfg.completeTaskOnClick) {
            return rosterItem.completed;
        }
        else 
            if (!Ext.isDefined(statusCalculation)) {
                this._setTaskCompleted(rosterItem, true); // this will cause the "task completed" event to fire...
            }
            else 
                if (Ext.isString(statusCalculation)) {
                    logger.logTrace(String.format("[Taskflow] Performing eval() on '{0}' to set statusCalculation for '{1}'.", statusCalculation, localContext.itemId));
                    statusCalculation = eval(statusCalculation);
                }
        
        if (Ext.isFunction(statusCalculation)) {
            waitObjs.push(rosterItem);
            statusCalculation.call({}, localContext, this.flowContext, statusResultFn);
        }
    },
    
    /**
     * @protected
     * Signal that initialization has completed.
     */
    _signalInitialized: function(initCompletedFn) {
        this._initialized = true;
        initCompletedFn();
        
        this.fireEvent("rpext-initialized", this);
    }
});

// Validate interface.
RP.iimplement(RP.taskflow.Taskflow, RP.interfaces.ITaskflow2);
//////////////////////
// ..\js\taskflow\TaskflowContainer.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.taskflow");

/**
 * This is a base class that contains a taskflow.  A taskflow includes a list of tasks, each
 * of which is represented in the UI by a widget (RP.taskflow.BaseTaskflowWidget base class).
 * The responsibility of this class is to handle the user-interface aspect of a taskflow.
 * The business logic of taskflows is handled by this class's ITaskflow instance returned by
 * getTaskflow().
 *
 * @namespace RP.taskflow
 * @class TaskflowContainer
 * @extends RP.ui.AccordionScrollPanel
 */
RP.taskflow.TaskflowContainer = Ext.extend(RP.ui.AccordionScrollPanel, {
  /**
   * @cfg {String or Object} taskflow (Required) <p>The taskflow xtype name or object.  Must implement
   * RP.interfaces.ITaskflow2</p>
   */
  /**
   * @cfg {String} title (Optional) <p>The taskflow title</p>
   */
  /** @private */
  initComponent: function(){
    logger.logTrace("[TaskflowContainer] initComponent; taskflow: " + this.initialConfig.taskflow + "; itemId: " + this.itemId);
    
    this._taskflow = null;
    this._initializing = false;
    this._widgets = null;
    this._activeWidget = null;
    
    if (!this.initialConfig.title) {
      this.title = "[Untitled Task Flow]";
    }
    else if (this.initialConfig.isTitleMessageName) {
      this.title = RP.getMessage(this.title);
    }
    
    Ext.apply(this, {
      border: false,
      headerAsText: false,
      headerCfg: {
        tag: "div",
        cls: "x-panel-header rp-taskflow-header",
        children: [{
          tag: "div",
          cls: "left",
          children: [{
            tag: "div",
            cls: "right",
            children: [{
              tag: "div",
              cls: "middle",
              children: [{
                tag: "div",
                cls: "rp-ellipsis",
                children: [{
                  tag: "span",
                  cls: "x-panel-header-text"
                }]
              }]
            }]
          }]
        }]
      }
    });
    
    RP.taskflow.TaskflowContainer.superclass.initComponent.call(this);
    
    this.addEvents(  /**
     * @event rpext-appactivated
     * Fires after an app in this taskflow is activated.
     * @param {Ext.Component} this
     * @param {Ext.Component} application component
     */
    "rpext-appactivated",  /**
     * @event rpext-tf-created
     * Fires after the taskflow object has been created
     * @param {Ext.Component} this
     */
    "rpext-tf-created",  /**
     * @event rpext-tf-aborted
     * Fires after the taskflow has aborted (failed to download its task code, etc.)
     * @param (Ext.Component) this
     */
    "rpext-tf-aborted");
    
    this.mon(this, "expand", this._onExpand, this);
    
    this.on("destroy", this._onDestroy, this);
  },
  
  /**
   * Slight override of Ext.Panel's setTitle so that it's also not checking headerAsText.
   * @param {String} title
   * @param {String} iconCls
   */
  setTitle: function(title, iconCls) {
    this.title = title;
    if (this.header) {
      this.header.child('span').update(title);
    }
    if (iconCls) {
      this.setIconClass(iconCls);
    }
    this.fireEvent('titlechange', this, title);
    return this;
  }, 
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation
   * @method
   */
  getTaskflowName: function() {
    return this.initialConfig.taskflow;
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation
   * @method
   */
  getTaskflow: function() {
    return this._taskflow;
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation
   * @method
   */
  onDeactivated: function() {
    this._deactivateCurrentWidget();
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation
   * @method
   */
  activateDefaultWidget: function() {
    if (!this.rendered || !this._taskflow) {
      this._activateDefaultWidget = true;
      return;
    }
    
    var activateFn = (function() {
      logger.logTrace("[TaskflowContainer] Activating widget...");
      
      this.ensureTaskflowInitialized();
      // determine index of default widget to activate
      var idx = 0;
      var defaultWidget = this.getWidgetByID(this._taskflow.getDefaultTaskId());
      
      if (defaultWidget) {
        idx = this._widgets.indexOf(defaultWidget);
      }
      var widget = this._widgets.itemAt(idx);
      while (widget.disabledWidget) {
          widget = this._widgets.itemAt(++idx);
      }
      
      this.onWidgetClick(widget, undefined);
    }).createDelegate(this);
    
    if (this._widgets && this._widgets.getCount() > 0) {
      activateFn();
    }
    else 
      if (!this._started) {
        this._taskflow.addPostInitListener(activateFn);
      }
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation
   * @method
   */
  onWidgetClick: function(widget) {
    if ((widget !== this._activeWidget)) {
      var fn = function(result) {
        if (result) {
          this._widgetClickHandler(widget);
        }
        else {
          logger.logTrace("[TaskflowContainer] onWidgetClick widget canceled; itemId: " + widget.itemId);
        }        
      };
      
      this._beforeAppActivateHandler(fn.createDelegate(this));
    }
    else {
      this._widgetClickHandler(widget);
    }    
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * Get the task widget component by task ID (roster element ID member)
   * @param {string} id The taskflow task ID
   * @method
   * @return {RP.taskflow.BaseTaskflowWidget} The widget
   */
  getWidgetByID: function(id) {
    if (this._widgets) {
      return this._widgets.get(id);
    }
    return null;
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * @method
   */
  setBeforeAppActivateHandler: function(cb) {
    this._beforeAppActivateHandler = cb;
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * @method
   */
  addApplicationActivatedHandler: function(cb) {
    this._addEventHandler("rpext-appactivated", cb);
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * @method
   */
  addTaskflowCreationListener: function(cb) {
    this.on("rpext-tf-created", cb);
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * @method
   */
  removeTaskflowCreationListener: function(cb) {
    this.un("rpext-tf-created", cb);
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * @method
   */
  addTaskflowAbortHandler: function(cb) {
    this.on("rpext-tf-aborted", cb);
  },
  
  /**
   * RP.interfaces.ITaskflowContainer2 implementation.
   * Sets the delegate to invoke to rebuild this taskflow
   * @method
   * @param {Function} delegate The delegate to invoke in order to rebuild this
   * taskflow.  It takes a lone argument, which is either a config object
   * to create an ITaskflowContainer2 instance with, or an actual
   * ITaskflowContainer2 instance.
   */
  setRebuildTaskflowHandler: function(delegate) {
    this._rebuildHandler = delegate;
  },
  
  /**
   * Rebuild this taskflow.
   * @param {Object} config New taskflow config
   */
  rebuild: function(config) {
    this._rebuildHandler(this.getTaskflow(), config.title, config);
  },
  
  getActiveWidget: function() {
    return this._activeWidget;
  },
  
  /** @private */
  _addEventHandler: function(evtName, cb) {
    if (this.rendered) {
      this.mon(this, evtName, cb);
    }
    else {
      this.on("render", function() {
        this.mon(this, evtName, cb);
      });
    }
  },
  
  /** @private */
  _removeEventHandler: function(evtName, cb) {
    this.mun(this, evtName, cb);
  },
  
  /** @private */
  _onExpand: function() {
    this.ensureTaskflowInitialized();
  },
  
  /** @private */
  _onDestroy: function() {
    if (this._widgets) {
      this._widgets.each(function(widget) {
        var taskForm = widget.getTaskForm();
        if (taskForm) {
          taskForm.destroy();
        }
      }, this);
    }
  },
  
  /** @private */
  ensureTaskflowInitialized: function() {
    var itf;    
    
    if (this._taskflow || this._initializing) {
      return;
    }
    
    this._initializing = true;
    
    var initProgressMask = new Ext.LoadMask(this.getEl(), {
      msg: RP.getMessage("rp.common.misc.LoadingMaskText")
    });
    
    initProgressMask.show.defer(5, initProgressMask);
    
    logger.logTrace("[TaskflowContainer] ensureTaskflowInitialized() starting");
    var steps = new RP.util.FunctionQueue();
    
    if (!this._taskflow) {
      steps.add(this._loadTaskflowCodeStep.createDelegate(this));
    }
    steps.add(this._initTaskflowStep.createDelegate(this));
            
    steps.execute((function() {
      initProgressMask.hide();
      this._initializing = false;
      this.doLayout();
      logger.logTrace("[TaskflowContainer] ensureTaskflowInitialized() completed successfully");
    }).createDelegate(this), (function() {
      if (window.leavingPage) {
          return;
      }
      initProgressMask.hide();
      this._initializing = false;
      this.doLayout();
      logger.logFatal("[TaskflowContainer] ensureTaskflowInitialized() completed with ERROR");
      
      Ext.Msg.alert(RP.getMessage("rp.common.misc.Error"), RP.getMessage("rp.common.misc.TaskflowInitFailed"));
    }).createDelegate(this));  
  },
  
  /** @private */
  _loadTaskflowCodeStep: function(successFn, errorFn) {    
    var createTFSuccessFn = (function(tf) {
      this._taskflow = tf;
      
      this.fireEvent("rpext-tf-created", this);
      
      var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
      
      if (!itf) {
        logger.logDebug("[TaskflowContainer] Taskflow does not implement the RP.interfaces.ITaskflow2 interface");
        errorFn();
        return;
      }
      
      itf.addTaskCompletedListener(this._onTaskCompleted.createDelegate(this));
      itf.addTaskflowCompletedListener(this._onTaskflowCompleted.createDelegate(this));
      itf.setRebuildHandler(this.rebuild.createDelegate(this));
      
      if (this._activateDefaultWidget) {
        this.activateDefaultWidget();
      }
      
      successFn();
    }).createDelegate(this);
    
    var createTFFailedFn = (function(tfDef) {
      this.fireEvent("rpext-tf-aborted", this);
      errorFn();
    }).createDelegate(this);
    
    RP.createTaskflow(this.initialConfig.taskflow, this.initialConfig.initialContext, createTFSuccessFn, createTFFailedFn);
  },
  
  /** @private */
  _initTaskflowStep: function(successFn, errorFn) {
    var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
    
    if (!itf.isInitialized()) {
      try {
        itf.initTaskflow((function() {
          this._loadWidgets(successFn, errorFn);
          this.fixScroll.defer(10, this);
        }).createDelegate(this));
      }
      catch (e) {
        logger.logError(e);
        errorFn();
        
        if (RP.globals.SERVER_TYPE === "development") {
          throw e;
        }
      }
    }
  },
  
  /** @private */
  _loadWidgets: function(successFn, errorFn) {
    var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
    var tfConfig = Ext.apply({}, this.initialConfig);
    var roster = itf.getTaskflowRoster(tfConfig);    
    var widgetContainer = new Ext.Container();
    
    this._widgets = new Ext.util.MixedCollection();
    
    Ext.each(roster, function(item) {
      var widgetCfg = itf.createTaskWidgetConfig(item);
      
      if (typeof widgetCfg.itemId === "undefined") {
        widgetCfg.itemId = item.id;
      }
      
      if (widgetCfg) {
        var widget = Ext.ComponentMgr.create(widgetCfg);
        var iwidget = RP.iget(widget, RP.interfaces.ITaskWidget);
        
        iwidget.setTaskflow(this._taskflow);
        iwidget.setTaskId(item.id);
        iwidget.initializeLocalContext();
        iwidget.subscribeWidgetClick(this.onWidgetClick.createDelegate(this));
        iwidget.subscribeWidgetChange(this._onWidgetChange.createDelegate(this));
        
        this._widgets.add(item.id, widget);
        
        widget.failedDependencies = [];
        widget.on("render", function() {
          if (Ext.isIE) {
            this.mon(widget.el, "mouseenter", this._handleMouseOver, widget);
            this.mon(widget.el, "mouseleave", this._handleMouseOut, widget);
          }
          else {
            this.mon(widget.el, "mouseover", this._handleMouseOver, widget);
            this.mon(widget.el, "mouseout", this._handleMouseOut, widget);
          }
        }, this);
      }
    }, this);
    
    // Add Description panel.
    var descriptionText = itf.getTaskflowDescription();
    var uicfg = itf.getTaskflowUIConfig();
    var descriptionComp;
    
    if (uicfg) {
      if (uicfg.createDescriptionCompFn) {
        descriptionComp = new Ext.Panel({
          itemId: "description",
          cls: "rp-taskflow-description",
          layout: "fit",
          items: uicfg.createDescriptionCompFn(this._taskflow, descriptionText)
        });
      }
      else 
        if (descriptionText) {
          descriptionComp = new Ext.Panel({
            itemId: "description",
            html: descriptionText,
            cls: "rp-taskflow-description"
          });
        }
      
      if (descriptionComp) {
        this.add(descriptionComp);
      }
    }
    
    widgetContainer.add(this._widgets.getRange());
    this.add(widgetContainer);
    
    itf.startTaskflow();
    
    // Update status and UI...
    this._onWidgetChange();
    successFn();
  },
  
  /** @private */
  _widgetClickHandler: function(widget) {
    logger.logTrace("[TaskflowContainer] Widget clicked itemId: " + widget.itemId);
    
    var newInst = false;
    
    if (!widget.getTaskForm()) {
      widget.createTaskForm();
      if (!widget.getTaskForm()) {
        return;
      }
      
      if (!widget.taskForm.itemId) {
        logger.logWarning("[TaskflowContainer] Task form for widget with itemId '" + widget.itemId + "' is missing itemId.  Use BaseTaskflowWidget._getTaskFormItemId() to generate default itemId");
      }
      newInst = true;
    }
    
    var taskID;
    var itf = RP.iget(widget.taskForm, RP.interfaces.ITaskForm);

    if (newInst) {
      if (!itf) {
        logger.logDebug("[TaskflowContainer] Your taskflow app needs to implement the RP.interfaces.ITaskForm interface");
        return;
      }
      
      taskID = this._widgets.keyOf(widget);
      if (taskID) {
        itf.setUrlHash(RP.core.PageContext.getTaskHash(this.getTaskflowName(), taskID, 
                this._taskflow.getTaskflowContext(), widget.getTaskContext()));
      }
    }
    else if (widget.taskForm) {
      taskID = this._widgets.keyOf(widget);
      itf.setUrlHash(RP.core.PageContext.getTaskHash(this.getTaskflowName(), taskID, 
                this._taskflow.getTaskflowContext(), widget.getTaskContext()));
    }
    
    if (widget !== this._activeWidget) {
      this._deactivateCurrentWidget();
      
      var activeTaskID = this._widgets.keyOf(widget);
      var taskCfg = this._taskflow.getTaskConfig(activeTaskID);
      if (taskCfg.completeTaskOnClick) {
        // Mark the task that is represented by this widget as 'complete'.
        var itf2 = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
        itf2.setTaskCompleted(activeTaskID, true);
      }
      
      this._activeWidget = widget;
      widget.setActive(true);
    }
    
    this.fireEvent("rpext-appactivated", this, widget.taskForm);
  },
  
  /** @private */
  _deactivateCurrentWidget: function() {
    if (this._activeWidget) {
      this._activeWidget.setActive(false);
      this._activeWidget = null;
    }
  },
  
  /** @private */
  _handleMouseOver: function(ev, t) {
    if (this.failedDependencies) {
      Ext.each(this.failedDependencies, function(dependency) {
        dependency.addClass("rp-widget-dependency");
      }, this);
    }
  },
  
  /** @private */
  _handleMouseOut: function(ev, t) {
    if (this.failedDependencies) {
      Ext.each(this.failedDependencies, function(dependency) {
        dependency.removeClass("rp-widget-dependency");
      }, this);
    }
  },
  
  /** @private */
  _onWidgetChange: function() {
    var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
    
    itf.updateStatus();
    if (itf.isCompleted()) {
      return;
    }
    
    this._widgets.each(function(widget) {
      var failedDependecies = [];
      var taskID = this._widgets.keyOf(widget);
      var taskCfg = this._taskflow.getTaskConfig(taskID);
      
      if (taskCfg.dependencies) {
        Ext.each(taskCfg.dependencies, function(dependency) {
          if (!itf.isTaskCompleted(dependency)) {
            failedDependecies.push(this._widgets.get(dependency));
          }
        }, this);
      }
      
      widget.setEnabled(failedDependecies.length === 0);
      widget.failedDependencies = failedDependecies;
    }, this);
  },
  
  /** @private */
  _onTaskCompleted: function(tf, taskConfig) {
    logger.logInfo("[TaskflowContainer] Task completed.  Taskflow name: " + tf.initialConfig.name + "; roster item id: " + taskConfig.id);
    var widget = this.getWidgetByID(taskConfig.id);
    
    if (Ext.isFunction(widget.onTaskCompleted)) {
      widget.onTaskCompleted.call(widget);
    }
  },
  
  /** @private */
  _onTaskflowCompleted: function(tf) {
    logger.logInfo("[TaskflowContainer] Taskflow completed: " + this.initialConfig.taskflow);
    
    var itf = RP.iget(tf, RP.interfaces.ITaskflow2);
    var uiCfg = itf.getTaskflowUIConfig();
    
    if (uiCfg && uiCfg.disableOnComplete) {
      this._widgets.each(function(widget) {
        widget.setEnabled(false);
      }, this);
    }
  }
});

// Validate interface.
RP.iimplement(RP.taskflow.TaskflowContainer, RP.interfaces.ITaskflowContainer2);

// Register xtype.
RP.core.ComponentMgr.register("rptfcontainer", RP.taskflow.TaskflowContainer, []);
//////////////////////
// ..\js\taskflow\TaskRegistry.js
//////////////////////
/*global RP, Ext */

Ext.ns("RP.taskflow");


/**
 * @namespace RP.taskflow
 * @class TaskRegistry
 * @singleton
 * @extends none
 * Maintains a registry of definitions for tasks that the current user can execute.  A
 * task is instantiated from a definition in this registry, therefore, a definition is
 * just the data for constructing a task plus other metadata.
 */
RP.taskflow.TaskRegistry = function()
{
    var defsByApp = {};
    var scriptsByApp = {};
    var cssByApp = {};
    var properties = ["appId", "taskId", "widgetXtype", "widgetCfg", "stashLibs", "scriptUrls", "cssUrls", "tags", "desc", "helpUrl", "helpMapperFn", "title"];

    function setDef(def) {   
        logger.logTrace("[TaskRegistry] Adding to registry. appId: " + def.appId + "; widge xtype: " + def.widgetXtype);
        
        var tDef = Ext.copyTo({}, def, properties);
        var appTasks = defsByApp[def.appId];
        
        if (Ext.isEmpty(appTasks)) {
            appTasks = {};
            defsByApp[def.appId] = appTasks;
        }
        
        appTasks[def.taskId] = tDef;
    }
    
    function getDef(appId, taskId, throwOnError) {
        var defs = defsByApp[appId];
        var scriptUrls = [];
        var cssUrls = [];
            
        if (Ext.isEmpty(defs)) {
            if (throwOnError) {
                throw new RP.Exception("No tasks with appId '" + appId + "' have been registered!");
            }
            return undefined;
        }
        
        var def = Ext.copyTo({}, defs[taskId], properties);

        if (Ext.isEmpty(def)) {
            if (throwOnError) {
                throw new RP.Exception("No tasks with appId '" + appId + "' and taskId '" + taskId + "' registered!");
            }
            return undefined;
        }

        if (!Ext.isEmpty(scriptsByApp[appId]) && !Ext.isEmpty(scriptsByApp[appId][taskId])) {
            def.scriptUrls = def.scriptUrls.concat(scriptsByApp[appId][taskId]);
        }
        if (!Ext.isEmpty(cssByApp[appId]) && !Ext.isEmpty(cssByApp[appId][taskId])) {
            def.cssUrls = def.cssUrls.concat(cssByApp[appId][taskId]);
        }
        
        return def;
    }
    
    function appendURLs(appId, taskId, dict, urls) {
        if (!dict[appId]) {
            dict[appId] = {};
            dict[appId][taskId] = [];
        }
        var tasks = dict[appId];
        var urlArray = tasks[taskId];
        
        if (!Ext.isArray(urls)) {
            urls = [urls];
        }
        
        Ext.each(urls, function(url){
            logger.logTrace("[TaskRegistry] Appending to task '" + appId + "." + taskId + "': " + url);
            urlArray.push(url);
        });
    }
    
    return {
        /**
        * Registers a new task definition (or an array of task definitions).  Registering a task
        * with the same name overwrites the previous definition.
        * @param (Object) def Task configuration object or an array of task configuration objects.
        * <b>Required:</b>
        * <ul>
        *   <li>appId: (String) The application id (e.g., "sitemgr")
        *   <li>taskId: (String) The task id (e.g., "inbox"); must be unique in the application space
        *   <li>widgetXtype: (String) The widget's xtype registered via TaskflowWidgetRegistry
        * </ul>
        * <b>Optional:</b>
        * <ul>
        *   <li>widgetCfg: (Object) Configuration to instantiate widget with
        *   <li>stashLibs: (Object or array of Objects) {name: String, version: String}
        *   <li>scriptUrls: (String or Array of String) Script URLs to download to load this task's code
        *   <li>cssUrls: (String or Array of String) CSS URLs to download to load this task's code
        *   <li>tags: (String) Comma-separated list of tags
        *   <li>desc: (String) Description of what this task does
        *   <li>helpUrl: (String) The URL (relative to your application root) of the Help file for this task
        *   <li>helpMapperFn: (Function) A function to dynamically return the Help URL for this task 
        * </ul>
        * @return nothing
        */
        register: function(def) {
            if (Ext.isArray(def)) {
                Ext.each(def, function(d) {
                    setDef(d);
                });
            }
            else {
                setDef(def);
            }
        },
        
        /**
         * Gets a registered task's properties
         * @param {String} appId The registered task's application ID
         * @param {String} taskId The registered task's task ID
         */
        get: function(appId, taskId) {
            return getDef(appId, taskId, true);
        },
        
        /**
         * Appends custom javascript files to load for a named app and task.  This is used
         * mainly as a customization hook to inject custom code for a given task.
         * @method
         * @param {string} appId App Id 
         * @param {string} taskId Task Id 
         * @param {string or array of string} urls URLs to append to the named task
         */
        appendScriptUrls: function(appId, taskId, urls) {
            appendURLs(appId, taskId, scriptsByApp, urls);
        },
        
        /**
         * Appends custom CSS files to load for a named app and task.  This is used
         * mainly as a customization hook to inject custom CSS for a given task.
         * @method
         * @param {string} appId App Id 
         * @param {string} taskId Task Id 
         * @param {string or array of string} urls URLs to append to the named task
         */
        appendCSSUrls: function(appId, taskId, urls) {
             appendURLs(appId, taskId, cssByApp, urls);
        }
    };
}();

RP.registerTask = RP.taskflow.TaskRegistry.register;
RP.getTask = RP.taskflow.TaskRegistry.get;
//////////////////////
// ..\js\taskflow\TaskflowRegistry.js
//////////////////////
/* global RP, Ext */

Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class TaskflowRegistry
 * @singleton
 * @extends none Maintains a registry of definitions for tasksflows that the
 *          current user can execute. A taskflow is instantiated from a
 *          definition in this regisry, therefore, a definition is just the data
 *          for constructing a taskflow plus other metadata.
 */
RP.taskflow.TaskflowRegistry = function() {
    var defs = {};
    var initialContexts = {};
    var appendScriptURLs = {}; // key = taskflow name, value = [String]
    var appendCssURLs = {}; // key = taskflow name, value = [String]

    function set(def) {
        // if (Ext.isEmpty(def.name) || Ext.isEmpty(def.tasks)) {
        // throw new RP.Exception("Missing required parameters.");
        // }
        logger.logTrace("[TaskflowRegistry] Adding to registry: " + def.name);

        // Add in the taskflow's scriptUrls to the list of dependent files
        // before
        // ScriptLoader.onReady fires.
        if (def.scriptUrls) {
            RP.util.ScriptLoader.load(def.scriptUrls, Ext.emptyFn, Ext.emptyFn);
        }

        // Add in the taskflow's cssUrls to the list of dependent files
        if (def.cssUrls) {
            RP.util.CSSLoader.load(def.cssUrls, RP.globals.paths.STATIC);
        }

        var tfDef = Ext.copyTo({}, def, ["name", "title", "isTitleMessageName",
                        "tasks", "listeners", "uiConfig", "desc",
                        "defaultTaskId", "scheduleCheckFn"]);
        defs[def.name] = tfDef;
    }

    function get(name, throwOnError) {
        var def = defs[name];

        if (Ext.isEmpty(def)) {
            logger.logError("Taskflow definition with name '" + name + "' not registered!");
            if (throwOnError) {
                throw new RP.Exception("Taskflow definition with name '" + name + "' not registered!");
            }
            return undefined;
        }
        return def;
    }

    function createTaskflow(def, initialContext, successFn, errorFn) {
        var roster = [];
        var stashLibs = [];
        var scriptUrls = [];
        var cssUrls = [];

        logger.logTrace("[TaskflowRegistry] createTaskflow name: " + def.name);

        Ext.each(def.tasks, function(t) {
            var taskDef = RP.getTask(t.appId, t.taskId);

            if (Ext.isDefined(taskDef.title)) {

                if (t.widgetCfg) {
                    t.widgetCfg = {};
                }

                t.widgetCfg = {
                    title : taskDef.title
                };

            }

            logger.logTrace("[TaskflowRegistry] Adding task. appId: " + t.appId + "; widget xtype: " + taskDef.widgetXtype);
            roster.push({
                        appId : t.appId,
                        id : t.id,
                        widget : RP.getWidgetXtype(t.appId,
                                taskDef.widgetXtype, RP.util.Object
                                        .mergeProperties(
                                                taskDef.widgetCfg,
                                                t.widgetCfg)),
                        statusCalculation : t.statusCalculation,
                        inParameterMapping : t.inParameterMapping ? Ext.apply({}, t.inParameterMapping) : undefined,
                        outParameterMapping : t.outParameterMapping ? Ext.apply({}, t.outParameterMapping) : undefined,
                        dependencies : t.dependencies ? t.dependencies.concat() : undefined,
                        helpUrl : taskDef.helpUrl,
                        helpMapperFn : taskDef.helpMapperFn,
                        completeTaskOnClick : t.completeTaskOnClick
                    });

            // Stash libs.
            if (Ext.isArray(taskDef.stashLibs)) {
                stashLibs = stashLibs.concat(taskDef.stashLibs);
            } else if (Ext.isDefined(taskDef.stashLibs)) {
                stashLibs.push(taskDef.stashLibs);
            }

            // css urls
            if (Ext.isArray(taskDef.cssUrls)) {
                cssUrls = cssUrls.concat(taskDef.cssUrls);
            } else if (Ext.isDefined(taskDef.cssUrls)) {
                cssUrls.push(taskDef.cssUrls);
            }

            // script urls
            if (Ext.isArray(taskDef.scriptUrls)) {
                scriptUrls = scriptUrls.concat(taskDef.scriptUrls);
            } else if (Ext.isDefined(taskDef.scriptUrls)) {
                scriptUrls.push(taskDef.scriptUrls);
            }
        });

        // Add 'append' (custom) URLs...
        if (appendCssURLs[def.name]) {
            cssUrls = cssUrls.concat(appendCssURLs[def.name]);
        }

        if (appendScriptURLs[def.name]) {
            scriptUrls = scriptUrls.concat(appendScriptURLs[def.name]);
        }

        //
        // Load Stash libraries, CSS, then scripts...
        //
        var queue = new RP.util.FunctionQueue();

        var loadSuccessFn = function() {
            logger
                    .logTrace("[TaskflowRegistry] Finished downloading tasks script files (if any)...");

            // check if any event listeners on the taskflow are plain strings
            // yet... if any are
            // then perform an eval on them now since the dependent script urls
            // should have been loaded.
            if (def.listeners) {
                Ext.iterate(def.listeners, function(key, value) {
                    if (Ext.isString(value)) {
                        logger.logTrace(String.format(
                                                "[TaskflowRegistry] Performing eval() on '{0}' to set event listener for '{1}'.",
                                                value, key));
                        def.listeners[key] = eval(value);
                    }
                });
            }

            successFn(new RP.taskflow.Taskflow({
                        name : def.name,
                        initialContext : initialContext,
                        defaultTaskId : def.defaultTaskId,
                        roster : roster,
                        listeners : def.listeners,
                        uiConfig : def.uiConfig
                    }));
        };

        var loadErrorFn = function() {
            logger.logError("[RP.taskflow.TaskflowRegistry] Aborting creation of taskflow '" + def.name + "'because script file failed to download...");
            errorFn();
        };

        var createStashLib = function(sl) {
            return function(succFn, errFn) {
                RP.util.ScriptLoader.loadStashLibrary(sl.name, sl.version,
                        succFn, errFn);
            };
        };

        if (stashLibs.length > 0) {
            Ext.each(stashLibs, function(sl) {
                queue.add(createStashLib(sl));
                logger
                        .logTrace("[TaskflowRegistry] Added loading of stash lib to queue: " + sl.name);
            });
        }

        if (cssUrls.length > 0) {
            queue.add(function(succFn, errFn) {
                RP.util.CSSLoader.load(cssUrls, true);
                succFn();
            });
            logger.logTrace("[TaskflowRegistry] Added downloading of CSS file(s) to queue");
        }

        if (scriptUrls.length > 0) {
            queue.add(function(succFn, errFn) {
                RP.util.ScriptLoader.load(scriptUrls, succFn, errFn);
            });
            logger.logTrace("[TaskflowRegistry] Added loading of script file(s) to queue");
        }

        // Add one more function to the queue, which waits until the JS
        // ScriptLoader
        // finishes loading of ALL other files before continuing..
        queue.add(function(succFn, errFn) {
            RP.util.ScriptLoader.onReady(function() {
                logger.logTrace("[TaskflowRegistry] AT THIS POINT, SCRIPT LOADING HAS FINISHED.");
                succFn();
            });
        });

        queue.execute(loadSuccessFn, loadErrorFn);
    }

    function setInitContext(tfName, initialContext) {
        initialContexts[tfName] = Ext.apply({}, initialContext);
    }

    function getInitContext(tfName) {
        return initialContexts[tfName];
    }

    function appendURLs(tfName, dict, urls) {
        var d = dict[tfName];

        if (!d) {
            d = [];
            dict[tfName] = d;
        }

        if (!Ext.isArray(urls)) {
            urls = [urls];
        }

        Ext.each(urls, function(url) {
            logger.logTrace("[TaskflowRegistry] Appending to taskflow '" + tfName + "': " + url);
            d.push(url);
        });
    }

    return {
        /**
         * Registers a taskflow defintion (or an array of taskflow definitions).
         * Registering a taskflow definition with the same name overwrites the
         * previous definition.
         * 
         * @param (Object)
         *            def Taskflow configuration object or an array of taskflow
         *            configuration objects. <b>Required:</b>
         *            <ul>
         *            <li>name: (String) Globally unique taskflow name
         *            <li>title: (String) Taskflow title
         *            <li>tasks: (Array) Array of Object containining these
         *            properties:
         *            <ul>
         *            <li>id: (String) Roster item id; must be unique amongst
         *            the tasks in this taskflow
         *            <li>appId: (String) The application ID (e.g., "sitemgr")
         *            <li>taskId: (String) The registered task id (e.g.,
         *            "inbox")
         *            <li>statusCalculation: (Function) Optional function to
         *            calculate this task's status within the scope of the
         *            taskflow
         *            <li>inParameterMapping: (Array) Optional array of input
         *            parameter mappings
         *            <li>outParameterMapping: (Array) Optional array of output
         *            parameter mappings
         *            <li>dependencies: (Array of String) Optional array of
         *            other task ids in this taskflow that must be completed
         *            before this task can execute
         *            <li>completeTaskOnClick: (Boolean) If 'true' the task
         *            will be marked as complete when clicked; takes precedence
         *            over statusCalculation
         *            </ul>
         *            The appId and taskId properties are used to query the
         *            TaskRegistry for the task definition. The appId and
         *            widgetXtype properties are used to query the
         *            TaskflowWidgetRegistry for the corresponding widget.
         *            </ul>
         *            <b>Optional:</b>
         *            <li>isTitleMessageName: (Boolean) If true, title property
         *            actually is a message name; default is 'false'
         *            <li>desc: (String) (metadata) Description of what this
         *            taskflow is about
         *            <li>scheduleCheckFn: (Function) A function to invoke to
         *            check if this taskflow should be enabled based on current
         *            time
         *            <li>listeners: (Object) The 'listeners' config property
         *            of RP.taskflow.Taskflow
         *            <li>uiConfig: (Object) The 'uiConfig' config property of
         *            RP.taskflow.Taskflow
         *            <ul>
         *            </ul>
         * @return nothing
         */
        register : function(def) {
            if (Ext.isArray(def)) {
                Ext.each(def, function(d) {
                    set(d);
                });
            } else {
                set(def);
            }
        },

        /**
         * Gets the definition of a previously registered taskflow.
         * 
         * @param {String}
         *            name The registered name of the taskflow
         */
        get : function(name) {
            return get(name, false);
        },

        /**
         * Gets all taskflows currently registered within the system.
         */
        getAll : function() {
            var tfDefs = [];

            Ext.iterate(defs, function(name, tfDef) {
                tfDefs.push(Ext.apply({}, tfDef));
            });

            return tfDefs;
        },

        /**
         * Creates a new taskflow from a previous taskflow registration
         * 
         * @param {String}
         *            name The name of the registered taskflow
         * @param {Object}
         *            initialContext An object to populate the initial taskflow
         *            context with
         * @param {Function}
         *            successFn The function to invoke with the created taskflow
         * @param {Function}
         *            errorFn The function to invoke if failed to create
         *            taskflow
         */
        create : function(name, initialContext, successFn, errorFn) {
            var def = get(name, true);
            var tfInitContext = RP.util.Object.mergeProperties(initialContext,
                    getInitContext(name));
            
            createTaskflow(def, tfInitContext, successFn, errorFn);
        },

        /**
         * Sets the initial flow context for the named taskflow
         * 
         * @param {String}
         *            name
         * @param {Object}
         *            initialContext
         */
        setInitialContext : function(name, initialContext) {
            setInitContext(name, initialContext);
        },

        /**
         * Gets the initial flow context for the named taskflow
         * 
         * @param {Object}
         *            name
         */
        getInitialContext : function(name) {
            return getInitContext(name);
        },

        /**
         * Appends custom javascript files to load for a named taskflow. This is
         * used mainly as a customization hook to inject custom code for a given
         * taskflow.
         * 
         * @method
         * @param {string}
         *            name Taskflow name
         * @param {string
         *            or array of string} urls URLs to append to the named
         *            taskflow
         */
        appendScript : function(name, urls) {
            appendURLs(name, appendScriptURLs, urls);
        },

        /**
         * Appends custom CSS files to load for a named taskflow. This is used
         * mainly as a customization hook to inject custom CSS for a given
         * taskflow.
         * 
         * @method
         * @param {string}
         *            name Taskflow name
         * @param {string
         *            or array of string} urls URLs to append to the named
         *            taskflow
         */
        appendCSS : function(name, urls) {
            appendURLs(name, appendCssURLs, urls);
        }
    };
}();

RP.registerTaskflow = RP.taskflow.TaskflowRegistry.register;
RP.createTaskflow = RP.taskflow.TaskflowRegistry.create;
//////////////////////
// ..\js\taskflow\ModuleRegistry.js
//////////////////////
/* global RP, Ext */

Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class ModuleRegistry
 * @singleton
 * @extends none Maintains a registry of modules available to the current user.
 *          A Module is instantiated from a definition in this regisry,
 *          therefore, a definition is just the data for constructing module
 *          plus other metadata.
 */
RP.taskflow.ModuleRegistry = (function() {
    var defs = {};

    var set = function(def) {
        logger.logTrace("[ModuleRegistry] Adding to registry: " + def.name);

        var mdef = Ext.copyTo({}, def, ["name", "label", "icon", "iconCls", "sortOrder", "initFn", "scriptUrls", "cssUrls", "taskflows"]);
        defs[def.name] = mdef;

        if (mdef.scriptUrls) {
            RP.util.ScriptLoader.load(mdef.scriptUrls, Ext.emptyFn, Ext.emptyFn);
        }
        // Load all css files associated with the modules so we can apply an
        // icon class to them immediately.
        if (Ext.isArray(mdef.cssUrls) && mdef.cssUrls.length > 0) {
            RP.util.CSSLoader.load(mdef.cssUrls, true);
            logger.logTrace("[ModuleRegistry] Downloading CSS files for module: " + mdef.name);
        }
    };

    var _get = function(name, throwOnError) {
        var def = defs[name];

        if (Ext.isEmpty(def)) {
            logger.logError("Module definition with name '" + name + "' not registered!");
            if (throwOnError) {
                throw new RP.Exception("Module definition with name '" + name + "' not registered!");
            }
            return undefined;
        }
        return def;
    };

    return {
        register : function(def) {
            if (Ext.isArray(def)) {
                Ext.each(def, function(d) {
                            set(d);
                        });
            } else {
                set(def);
            }
        },

        get : function(name) {
            return _get(name, false);
        },

        /**
         * Retrieve all of the modules sorted in based on 
         * the sort order of the properties.
         */
        getAllSorted : function() {
            var list = [];

            Ext.each(RP.upb.modules, function(d) {
                list.push(_get(d));
            });

            return list;
        }
    };
})();

RP.registerModule = RP.taskflow.ModuleRegistry.register;
//////////////////////
// ..\js\taskflow\TaskflowFrame.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @namespace RP.taskflow
 * @class TaskflowFrame
 * @extends Ext.Panel
 */
RP.taskflow.TaskflowFrame = Ext.extend(Ext.Panel,
{
  /**
   * @cfg {Array} taskflows (Required) <p>Array of taskflows to include</p>
   */
   
   /**
   * @cfg {Array} modules (Required) <p>Array of modules to include</p>
   */
   
  /**
   * @cfg {String} navRegion (Optional) <p>The taskflow navigation panel placement - "west" or "east"</p>
   */
   
  /**
   * @cfg {Integer} navInitialSize (Optional) <p>The taskflow navigation panel width; default is 200</p>
   */
   
  /**
   * @cfg {Ext.Component} navArea (Optional) <p>Panel or component to put in the north region of the
   * navigation panel</p>
   */
  
  /**
   * @cfg (Number) navAreaHeight (Optional) <p>navArea height.  Defaults to navArea.height, or 50 if neither specified</p>
   */
   
  /**
   * @cfg {Boolean} hideAccordion (Optional) <p>For single taskflow, hide accordion in the
   * navigation panel</p>
   */
  
  /**
   * @cfg {Ext.Component} blankForm (Optional) <p>Panel or component to initially render in the task form area.  This
   * will only display if there are no taskflows</p>
   */
  

  
  activeWidget: undefined,
  
  //maximizeTasks: false,
   
  /** @private */
  initComponent: function()
  {
    logger.logTrace("[TaskflowFrame] initComponent; itemId: " + this.itemId);
    
    this._onAppActivatedFn = this._onAppActivated.createDelegate(this);
    this._rebuildTaskflowFn = this.rebuildTaskflow.createDelegate(this);
    this._tfAbortHandlerFn = this._tfAbortHandler.createDelegate(this);
    this._beforeAppActivatedFn = this._beforeAppActivatedHandler.createDelegate(this);
    
    this._initializeHistory();
    
    this._rpViewPanel = new Ext.Panel({
      id: '_rp_view_panel',
      region: "center",
      layout: "card",
      style: "zIndex: 0 ",
      bodyCssClass: "rp-ttf-app",
      border: false,
      layoutConfig: {
        deferredRender: true
      }
    });

    var t = this._createTaskflows();
    this._rpTaskflowsContainer = this._createTaskflowContainer();
     
    this._rpTaskflowNav = new Ext.Panel({
      region: 'center',
      layout: 'border',
      //title: this.initialConfig.navTitle,
      items: [
        this._rpTaskflowsContainer
      ]
    });
    
    this._rpModuleNav = this._createModuleNavigation();

    if (this.initialConfig.navArea) {
      this._rpTaskflowNav.add(new Ext.Panel({
        region: "north",
        layout: "fit",
        height: this.initialConfig.navAreaHeight || this.initialConfig.navArea.height || 50,
        items: this.initialConfig.navArea
      }));

      this._rpTaskflowNav.doLayout();
    }
    
    this._rpTaskflowNavContainer = new Ext.Panel({
      border: false,
      bodyBorder: false,
      region: this.initialConfig.navRegion || "east",
      collapseMode: 'mini',
      collapsedCls: 'rp-taskflow-splitbar',
      hideCollapseTool: true,
      split: true,
      margins:{
        top: 0,
        right: 0,
        bottom: 0,
        left: 0
      },
      itemId: "taskflowNavContainer",
      collapsible: true,
      layout: "border",
      cls: "rp-vantage",
      width: this.initialConfig.navInitialSize || 200,
      minSize: this.initialConfig.navInitialSize || 200,
      maxSize: this.initialConfig.navInitialSize || 200,
      items: [
        this._rpModuleNav,
        this._rpTaskflowNav
        ]
    });
    this.on("afterlayout", function() {
       var layout = this.layout.east ? this.layout.east : this.layout.west;
       var splitterBar = layout.getSplitBar();
       var splitterProxy = Ext.get(splitterBar.proxy);
       
       splitterBar.el.addClass("rp-taskflow-splitbar");
       splitterProxy.addClass("rp-taskflow-splitbar");
       
    }, this);

    Ext.apply(this, {
      layout: "border",
      border: false,
      itemId: this.initialConfig.itemId || "taskflowFrame",
      items: [
        this._rpTaskflowNavContainer,
        this._rpViewPanel
      ]
    });

    RP.taskflow.TaskflowFrame.superclass.initComponent.apply(this);
    
    if (t.waiter) {
      t.waiter.resume();
    }
    else {
      this._addTaskflowContainers(t.taskflows);
    }
  },
  
  buildModules: function(modules) {
    var items = [];
    
    if (!Ext.isEmpty(modules)) {
      Ext.each(modules, function(module, index) {
        items.push({
          title: String.format('<img src="{0}" /><br/>{1}', RP.globals.paths.STATIC + module.icon, RP.getMessage(module.label)),
          module: module,
          layout: 'fit',
          itemId: module.name,
          items: module.items
        });
        if (module.name == RP.globals.CURRENT_MODULE.name) {
          this._rpViewPanel.add(this._createLandingPage(module.landingPage)); 
          this._moduleIndex = index;
        }
      }, this);
    }
    else {
        this._rpViewPanel.add(this._createLandingPage());
    }

    return items;
  },
  
  /**
   * Toggle the visibility state of the taskflow navigation panel (accordion panel containing taskflows)
   * @method toggleNavPanel
   */
  toggleNavPanel: function() {
    this._rpTaskflowNav.toggleCollapse();
  },
  
  /**
   * Hide the taskflow navigation panel (accordion panel containing taskflows)
   * @method showNavPanel
   */
  hideNavPanel: function() {
    this._rpTaskflowNav.hide();
  },
  
  /**
   * Show the taskflow navigation panel (accordion panel containing taskflows)
   * @method showNavPanel
   */
  showNavPanel: function() {
    this._rpTaskflowNav.show();
  },
  
  /**
   * Activate a taskflow programatically.
   * 
   * @method
   * @param {Mixed} tf The taskflow name or RP.interfaces.ITaskflow instance
   * @param {Boolean} activateDefaultWidget Activate the default widget.  Defaulted to true.
   * @param {Function} activatedFn Function to call on activation.
   */
  activateTaskflow: function(tf, activateDefaultWidget, activatedFn) {
    var tfc = this._getTaskflowContainer(tf);
    if(Ext.isEmpty(activateDefaultWidget)) {
        activateDefaultWidget = true;
    }
    this._activateTaskflow(tfc, activateDefaultWidget, activatedFn);
  },
  
  /**
   * Rebuild the specified taskflow by getting rid of it and creating another
   * instance with new context
   * @method
   * @param {Mixed} tf The taskflow name or RP.interfaces.ITaskflow instance
   * @param {String} title Optional new title (null to inherit the current title)
   * @param {Object} initialContext (optional) New initialContext to create the taskflow with 
   */
  rebuildTaskflow: function(tf, title, initialContext) {
    var tfc = this._getTaskflowContainer(tf);
    if (!tfc) {
      logger.logError("[TaskflowFrame] Cannot find taskflow container");
      return;
    }
    
    // The events of the taskflowContainter will resume to allow onRender to fire.
    // This will only matter if initialConfig.taskflowsEnabled is set to false.
    tfc.resumeEvents();
    
    RP.core.PageContext.updateTaskflowContext(initialContext);
    
    var expandNewTfc = !tfc.collapsed;
    var itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
    var tfCfg = this._createTaskflowContainerConfig({
      itemId: tfc.itemId,
      taskflow: itfc.getTaskflowName(),
      title: title || tfc.title,
      isTitleMessageName: false,
      initialContext: initialContext
    });
    var idx = this._rpTaskflowsContainer.items.indexOf(tfc);    
    var newTfc = Ext.ComponentMgr.create(tfCfg);
    
    this._rpTaskflowsContainer.remove(tfc, true);
    this._rpTaskflowsContainer.insert(idx, newTfc);    
    this._rpTaskflowsContainer.doLayout();
    
    if (expandNewTfc) {
      var hash = window.location.hash;
      this._activateTaskflow(tfc, Ext.isEmpty(hash) || hash === ":", (function(taskFlowContainer) {
          this.navigateToURLHash(hash);
        }).createDelegate(this));
    }
    
    // Destroy the discarded TaskflowContainer, which should also destroy all taskforms
    // its widgets contain.
    tfc.destroy();
  },
  
  /**
   * Add a new taskflow.
   * @method addTaskflow
   * @param {Ext.Component} tfc The taskflow container
   * @param {boolean} activateTF Make the new taskflow active?
   * @param {boolean} activateDefaultWidget Activate the default widget? Applicable only if activateTF is true
   * @param {Function} activatedFn Delegate to invoke after taskflow has activated.  Applicable only if activateTF is true
   */
  addTaskflow: function(tfc, activateTF, activateDefaultWidget, activatedFn) {
    logger.logInfo("[TaskflowFrame] addTaskflow called with itemId: " + tfc.initialConfig.itemId);
    
    if (typeof activateDefaultWidget === "undefined") {
      activateDefaultWidget = true;
    }
    
    if (!RP.iget(tfc, RP.interfaces.ITaskflowContainer2)) {
      logger.logDebug("[TaskflowFrame] RP.interfaces.ITaskflowContainer2 interface not implemented.");
    }
    
    this._setupHandlersForTaskflowContainer(tfc);
    
    this._rpTaskflowsContainer.add(tfc);
    this._rpTaskflowsContainer.doLayout();
    
    if (activateTF) {
      this._activateTaskflow(tfc, activateDefaultWidget, activatedFn);
    }
  },

  /**
   * Remove a taskflow.
   * @method removeTaskflow
   * @param {Ext.Component} tfc The taskflow container
   * @param {boolean} deferLayout Defer refreshing the layout?
   */
  removeTaskflow: function(tfc, deferLayout) {
    logger.logInfo("[TaskflowFrame] removeTaskflow called with itemId: " + tfc.initialConfig.itemId);
    
    //remove from container, doLayout(), etc...
    this._rpTaskflowsContainer.remove(tfc);
    tfc.destroy();
  },

  /**
   * Navigate to a specific task in the current module.
   * @param {String} urlHash The URL hash containing module, taskflow, and task names
   */
  navigateToURLHash: function(urlHash) {  
    logger.logTrace("[TaskflowFrame] navigateToHash() called: " + urlHash);
    
    if (this._rpTaskflowsContainer.items.getCount() === 0) {
      logger.logTrace("[TaskflowFrame] navigateToHash() - no taskflows, so ignoring...");
      return;
    }
    
    this._activateDefaultWidget.defer(100, this, [urlHash]);
  },
  
  /**
   * Returns the TaskflowContainer containing the specified taskflow
   * @private
   * @param {Mixed} tf An RP.interfaces.ITaskflow instance or taskflow name
   */
  _getTaskflowContainer: function(tf) {
    if (typeof tf === "string") {
      return this._findTaskflowContainer(function(itfc) {
        return (itfc.getTaskflowName() === tf);
      });
    }
    else {
      return this._findTaskflowContainer(function(itfc) {
        return (itfc.getTaskflow() === tf);
      });
    }
  },
  
  /**
   * Find a taskflow container using a custom delegate
   * @private
   * @param {Function} matchFn Matcher function that returns boolean value indicating a match
   */
  _findTaskflowContainer: function(matchFn) {
    var tfc = null;
    
    this._rpTaskflowsContainer.items.each(function(c) {
      var itfc = RP.iget(c, RP.interfaces.ITaskflowContainer2);
      if (matchFn(itfc)) {      
        tfc = c;
        return false;
      }
    }, this);
    return tfc;
  },
  
  /**
   * Do preprocessing of a taskflow given its config object, such as executing
   * its scheduleCheckFn, etc.
   * @private
   * @param {Object} tfConfig
   * @param {Function} beginDeferHandler Delegate invoked if deferred processing is required
   * @param {Function} endDeferHandler Delegate invoked after deferred processing has completed
   */
  _preprocessTaskflow: function(tfConfig, beginDeferHandler, endDeferHandler) {
    if (typeof tfConfig.itemId === "undefined") {
      logger.logWarning("[TaskflowFrame] The 'itemId' config attribute for a taskflow is not specified.  Taskflow title: " + tfConfig.title);
    }

    var scheduleCheckFn = RP.taskflow.TaskflowRegistry.get(tfConfig.taskflow).scheduleCheckFn;

    // if the scheduleCheckFn is a string, attempt to evaluate it into a function
    if (Ext.isString(scheduleCheckFn)) {
      logger.logTrace('[TaskflowFrame] String scheduleCheckFn detected, performing eval.');

      var evaluatedFn = eval(scheduleCheckFn);

      if (Ext.isFunction(evaluatedFn)) {
        scheduleCheckFn = evaluatedFn;
      }
      else {
        logger.logError(String.format('[TaskflowFrame] Taskflow "{0}" has a String scheduleCheckFn of "{1}" that did not actually evaluate to a function that exists, so it will be processed as normal."',
                                      tfConfig.taskflow, scheduleCheckFn));
      }
    }
    
    if (Ext.isFunction(scheduleCheckFn)) {
      logger.logTrace("[TaskflowFrame] Checking schedule for taskflow: " + tfConfig.taskflow);
      
      beginDeferHandler(tfConfig);

      var resultFn = (function(result) {
        logger.logTrace("[TaskflowFrame] Schedule check result for taskflow '" + tfConfig.taskflow + "': " + result);
        // the handler must be executed asyncrhonously because otherwise if the taskflow's schedule check
        // executes synchronously, the taskflow will be removed (unsuccessfully) before it was ever added
        endDeferHandler.defer(1, this, [tfConfig, !!result]);
      }).createDelegate(this);
      
      var tfInitContext = RP.util.Object.mergeProperties(tfConfig.initialContext, 
            RP.taskflow.TaskflowRegistry.getInitialContext(tfConfig.taskflow));
      
      scheduleCheckFn(resultFn, tfInitContext);
    }
  },
  
  /**
   * Create the module navigation.
   * @private
   */
  _createModuleNavigation: function () {
    var moduleNav = new Ext.TabPanel({
      border: false,
      bodyBorder: false,
      region: 'north',
      enableTabScroll: true,
      cls: 'rp-taskflow-module-tabs',
      layoutOnTabChange: true,
      itemId: "moduleNav",
      items: this.buildModules(this.initialConfig.modules),
      itemTpl: this._getModuleTabItemTpl()
    });

    // Ensure that the dirty state of the task form is accounted
    // for prior to switching the active tab.  Reported on RPWEB-4080
    // where a different module was selected, and the user decided
    // to stay on the page, which resulted the wrong tab shown as active.
    moduleNav.on("beforetabchange", function(tabPanel, panel, selectedPanel) {
        if (panel.module.name !== selectedPanel.module.name) {
            RP.core.PageContext.setActiveModule(panel.module.name);
        }
        return false;
    });
    
    // Suspend events and set the correct active tab based on the current module
    moduleNav.on("afterrender", function() {
      if (this._moduleIndex !== undefined && this._moduleIndex !== null) {
        this._rpModuleNav.suspendEvents(false);
        this._rpModuleNav.setActiveTab(this._moduleIndex, false);
        
        if (this._setEmptyHeight() === true) {
          this._rpModuleNav.setHeight(0);
        }
        this._rpModuleNav.resumeEvents();
      }
    }, this);
    
    return moduleNav;
  },
  
  /**
     * A simple method to overwrite the item template to change its 
     * href from "#" to "javascript:void(0)"
     * http://jira.redprairie.com/browse/RPWEB-3934
     * @private
     */
  _getModuleTabItemTpl: function() {
    var tabTemplate = new Ext.Template('<li class="{cls}" id="{id}"><a class="x-tab-strip-close"></a>', 
      '<a class="x-tab-right" href="javascript:void(0)"><em class="x-tab-left">', 
      '<span class="x-tab-strip-inner"><span class="x-tab-strip-text {iconCls}">{text}</span></span>', 
      '</em></a></li>');
    tabTemplate.disableFormats = true;
    tabTemplate.compile();
    return tabTemplate;
  },
  
  /**
   * Determines the height of the module navigation height.
   * @return {Number} Returns a 0 zero height if there are no module items, empty otherwise.
   */
  _setEmptyHeight: function() {
    if (this._rpModuleNav.getActiveTab().items.getCount() === 0) {
      return true;
    }
    
    return false;
  },
  
  /**
   * Create the taskflow container.
   * @private
   */
  _createTaskflowContainer: function() {
    return Ext.ComponentMgr.create({
      id: "_rp_taskflow_container",
      xtype: "panel",
      style: this.initialConfig.hideAccordion? "padding-top: 12px" : "",
      border: false,
      header: false,
      region: "center",
      layout: this.initialConfig.hideAccordion? "fit" : "accordion",
      collapsible: true,
      initialSize: this.initialConfig.navInitialSize || 200,
      items: []
    });
  },
  
  /**
   * Create taskflows from initialConfig
   * @private
   */
  _createTaskflows: function() {
    var t = { taskflows: [] };
    var beginDeferHandler = (function(tfConfig) {
      if (!t.waiter) {
        t.waiter = new RP.util.Waiter({
          handler: this._addTaskflowContainers.createDelegate(this, [t.taskflows])
        });
        t.waiter.suspend();
      }
      t.waiter.add(tfConfig);
    }).createDelegate(this);
    
    var endDeferHandler = function(tfConfig, result) {
      if (!result) {
        t.taskflows.remove(tfConfig);
      }
      t.waiter.remove(tfConfig);      
    };
    
    Ext.each(this.initialConfig.taskflows, function(tfConfig) {
      this._preprocessTaskflow(tfConfig, beginDeferHandler, endDeferHandler);
      t.taskflows.push(tfConfig);
    }, this);
    
    return t;
  },
  
  /** @private */
  _initializeHistory: function() {
    // Add history form
    var html = [];
    // Ext history managment requires this form
    html.push('<form id="history-form" class="x-hidden">');
    html.push('<input type="hidden" id="x-history-field" />');
    if (Ext.isIE)
    {
      html.push('<iframe id="x-history-frame"></iframe>');
    }
    html.push('</form>');

    Ext.DomHelper.insertHtml('beforeEnd', document.body, html.join());
    
    Ext.History.init();

    Ext.History.on("change", function(token) {
      logger.logTrace("[TaskflowFrame] History change detected, token: " + token);
      
      var hash = this._parseHistoryToken(token);
        
      // When the users session is expired we don't allow the user to navigate via the url. Instead we send them
      // to the main login page.
      if (RP.util.Helpers.isSessionExpired()) {
        var newUrl = RP.core.PageContext.getPageURL(RP.globals.SITEID, RP.core.PageContext.getActiveModuleName(), hash.tfName, hash.taskID, hash.moduleContext, hash.taskflowContext, hash.taskContext);
        RP.util.Helpers.redirect(newUrl);
        RP.util.Helpers.reload();
        return;
      }
      
      if (RP.util.PageInactivityChecker.isInactive()) {
          RP.util.Helpers.keepSessionAlive();
          Ext.getCmp("inactivityWarningDialog").destroy();
      }
      
      var me = this;
      var activateWidgetFn = function(itfc, tfc, hash) {

        var widget = itfc.getWidgetByID(hash.taskID);
        
        if (!widget) {
          itfc.activateDefaultWidget();
          return;
        }
        
        if (hash.taskContext) {
          widget.setTaskContext(hash.taskContext);
        }
          
        if (widget !== this.activeWidget) {
          this.activeWidget = widget;
          
          var tfCompleted = itfc.getTaskflow().isCompleted();
        
          if (widget && (widget.getEnabled() || tfCompleted)) {      
            widget.raiseWidgetClick();
          } 
          else {
            itfc.activateDefaultWidget();
          }
          
          me._rpTaskflowsContainer.doLayout();
        }
      };
      
      // Find taskflow container.
      var found = false;
      var waitObj;
      
      // Update global variable with URL hash properties.
      RP.globals.CURRENT_HASH = hash; 
      
      this._rpTaskflowsContainer.items.each(function(c) {
        itfc = RP.iget(c, RP.interfaces.ITaskflowContainer2);
        if (itfc.getTaskflowName() === hash.tfName) {
          if (!itfc.getTaskflow() || !itfc.getTaskflow().isInitialized()) {
            waitObj = { itfc: itfc, tfc: c };
          }
          else {
            found = true;
            activateWidgetFn.call(me, itfc, c, hash);
          }
          return false;
        }
      }, this);

      if (!found && !waitObj && this._rpTaskflowsContainer.items.getCount() > 0) {
        // URL is missing taskflow name or specifies an invalid one.  Default to the first
        // taskflow...
        var c = this._rpTaskflowsContainer.items.first();
        waitObj = {
          itfc: RP.iget(c, RP.interfaces.ITaskflowContainer2),
          tfc: c	
        };
      }
        
      if (!found && waitObj && this.taskflowsEnabled) {
        // Found the taskflow container, but its taskflow hasn't been initialized yet.  Wait
        // for the taskflow to initialize then activate the widget then...
        var itfc = waitObj.itfc;
        var tfc = waitObj.tfc;
        var tfCreateListener = null;
        
        
        logger.logTrace("[TaskflowFrame] Waiting for tfc itemId: " + tfc.initialConfig.itemId);

        tfc.expand(false);
        tfc.fireEvent("expand", tfc);

        var postCreationFn = function(listenPostInit) {
          logger.logTrace("[TaskflowFrame] postInit for tfc itemId: " + tfc.initialConfig.itemId);

          if (tfCreateListener) {
            itfc.removeTaskflowCreationListener(tfCreateListener);
          }
          
          itfc.getTaskflow().mergeTaskflowContext(hash.taskflowContext);

          if (listenPostInit) {
            itfc.getTaskflow().addPostInitListener((function(tf) {
              activateWidgetFn.call(me, itfc, tfc, hash);
            }).createDelegate(this));
          }
        };
        
        var tf = itfc.getTaskflow();
        if (tf) {
          var itf = RP.iget(tf, RP.interfaces.ITaskflow2);
          
          if (itf.isInitialized()) {
            postCreationFn(false);
            activateWidgetFn.call(me, itfc, tfc, hash);
          }
          else { 
            postCreationFn(true);
          }
        }
        else {
          if (!tfCreateListener) {
            tfCreateListener = function() {
              postCreationFn(true);
            };
          }
      
          itfc.addTaskflowCreationListener(tfCreateListener);
        }
      }
    }, this);
  },
  
  _parseHistoryToken: function(token) {
    logger.logTrace("[TaskflowFrame] Parsing history token: " + token);    
    return RP.core.PageContext.parseHash(token);
  },

  /** @private */
  _activateTaskflow: function(tfc, activateDefaultWidget, activatedFn) {
    logger.logTrace("[TaskflowFrame] Expanding taskflow container with itemId: " + tfc.initialConfig.itemId);
    this.taskflowsEnabled = true;
    tfc.expand(true);
    tfc.fireEvent("expand", tfc); // apparently expand() doesn't cause "expand" event to fire...
    
    var unhook = false;
    var tfCreatedFn = function() {
      var itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
      
      if (activateDefaultWidget) {
        itfc.activateDefaultWidget();
      }
      
      if (unhook) {
        itfc.removeTaskflowCreationListener(tfCreatedFn);
      }
      
      if (Ext.isFunction(activatedFn)) {
        activatedFn(tfc);
      }
    };
    
    var itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
    if (itfc.getTaskflow()) {      
      tfCreatedFn();
    }
    else {
      unhook = true;
      itfc.addTaskflowCreationListener(tfCreatedFn);      
    }
  },

/**
 * Create a xtype config for a taskflow container given a taskflow definition
 * @private
 * @param {Object} tfDef
 */  
  _createTaskflowContainerConfig: function(tfcCfg) {
    var tfcxtype = this.initialConfig.tfContainerXtype || "rptfcontainer";
    
    return RP.mergeProperties({
        xtype: tfcxtype,
        listeners: {
          render: {
            scope: this,
            fn: function() {
              var tfc = this._rpTaskflowsContainer.getComponent(tfcCfg.itemId);
              this._setupHandlersForTaskflowContainer(tfc);
            }
          }
        }      
      }, tfcCfg);
  },
  
  _setupHandlersForTaskflowContainer: function(tfc) {
    var itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
    itfc.addTaskflowAbortHandler(this._tfAbortHandlerFn);
    itfc.addApplicationActivatedHandler(this._onAppActivatedFn);
    itfc.setRebuildTaskflowHandler(this._rebuildTaskflowFn);
    itfc.setBeforeAppActivateHandler(this._beforeAppActivatedFn);
  },
  
  /** @private */
  _addTaskflowContainers: function(taskflows) {
    // Create components from list of taskflow configs.
    var tfcs = [];
    
    Ext.each(taskflows, function(tfDef) {
      var tfCfg = this._createTaskflowContainerConfig(tfDef);
      
      logger.logTrace("[TaskflowFrame] Creating taskflow container with itemId: " + tfDef.itemId + "; tfContainerXtype: " + tfCfg.xtype);
      tfcs.push(Ext.ComponentMgr.create(tfCfg));
    }, this);
    
    var addToContainerFn = (function() {
      this._rpTaskflowsContainer.add(tfcs);
      
      logger.logTrace("[TaskflowFrame] Taskflow containers added and laid out.");
        
      this._activateDefaultWidget(window.location.hash);
      // Navigate to specified task as indicated by URL hash...
      if (this.initialConfig.taskflowsEnabled && tfcs.length > 0) {
        this._rpTaskflowNavContainer.doLayout();
      }
      else {
        
        // Taskflows have been disabled, so suspend all events for the taskflowContainer.
        // This will prevent the onRender and doLayout events from firing, which
        // Will cause the taskflows to render despite the initialConfig.taskflowsEnabled value.
        this._rpTaskflowsContainer.suspendEvents(false);
        this._rpViewPanel.getLayout().setActiveItem(this._rpViewPanel.items.first());
      }
    }).createDelegate(this);
    
    addToContainerFn.defer(100);
  },
    
  /** @private */
  _onAppActivated: function(tfc, taskForm) {
    logger.logTrace("[TaskflowFrame] _onAppActivated; tfc itemId: " + tfc.initialConfig.itemId + "; taskForm itemId: " + taskForm.itemId);
    
    var itfc;
    var layout = this._rpViewPanel.getLayout();   // the Card layout
    var activeItem = layout.activeItem;
    
    if (activeItem === taskForm) {
      return;
    }
    
    var hasOldActiveTaskForm = false;
    if (activeItem) {
      if (Ext.isDefined(RP.iget(activeItem, RP.interfaces.ITaskForm))) {
        hasOldActiveTaskForm = true;
      }      
    }
    
    // Notify previous active taskflow container that he's being deactivated.
    if (tfc !== this._activeTFC) {
      if (this._activeTFC) {
        itfc = RP.iget(this._activeTFC, RP.interfaces.ITaskflowContainer2);
        itfc.onDeactivated();
      }
      this._activeTFC = tfc;      
    }

    // Force a create of a task form.
    if (taskForm.xtype) {
      taskForm = RP.core.ComponentMgr.create(taskForm.xtype, taskForm);
    }

    // Sets the active task form in the card layout:
    // - Deactivate previous active task form
    // - Activate current active task form.

    // Add task form to card container if not already added...
    var found = false;
    this._rpViewPanel.items.each(function(p) {
      if (p === taskForm) {
        found = true;
        return false;
      }
    }, this);

    if (!found) {
      this._rpViewPanel.add(taskForm);
    }
    
    if (hasOldActiveTaskForm && activeItem) {
      RP.iget(activeItem, RP.interfaces.ITaskForm).onDeactivate();
    }
    
    layout.setActiveItem(this.getComponentId(taskForm));
    activeItem = layout.activeItem;
    var itf = RP.iget(activeItem, RP.interfaces.ITaskForm);

    itf.onActivate();
    var newHash = itf.getUrlHash();

    if (newHash) {
      Ext.History.add(newHash, true);
    }
    
    this._currentHash = newHash || "";

    this._rpViewPanel.doLayout();
    
    // Update global variable pointing to the active task.
    itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
    var taskId = itfc.getActiveWidget().getTaskId();
    var taskCfg = itfc.getTaskflow().getTaskMetadata(taskId).getConfig();
    RP.globals.CURRENT_TASK = Ext.apply({}, {
      appId: taskCfg.appId,
      taskId: taskCfg.id,
      taskConfig: taskCfg,
      taskForm: itfc.getActiveWidget().taskForm
    }); 
  },
  
  /** @private */
  _activateDefaultWidget: function(hash) {
    if (hash) {
      var loadFn = function(urlHash) {
        Ext.History.fireEvent("change", urlHash);
      };
      if(typeof hash === "string" && hash.charAt(0) === '#') {
          hash = hash.slice(1);
      }
      loadFn.defer(10, this, [hash]);
    }
    else {
      // Expand the first taskflow container and ask it to activate the default widget.
      var tfc = this._rpTaskflowsContainer.items.first();
      if (tfc) {
        var fn = function() {
          tfc.fireEvent("expand", tfc);

          // Activate default widget in first taskflow after layout.  Do it only once,
          // so unregister "afterlayout" event right away (afterlayout is generated
          // everytime this taskflow container is resized, etc.)
          var activateDefaultWidgetFn = function() {
            this.un("afterlayout", activateDefaultWidgetFn, this);

            var itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
            itfc.activateDefaultWidget();
          };

          tfc.on("afterlayout", activateDefaultWidgetFn, tfc);
        };
        
        if (tfc.rendered) {
          fn();
        }
        else {
          tfc.on("render", fn, this);
        }
      }
    }    
  },
  
  /** @private */
  _tfAbortHandler: function(tfContainer) {
    logger.logTrace("[TaskflowFrame] Aborting taskflow container with itemId: " + tfContainer.initialConfig.itemId);
    
    if (this._rpTaskflowsContainer.items.getCount() > 1) {
      var idx = this._rpTaskflowsContainer.items.indexOf(tfContainer);
      this._rpTaskflowsContainer.getLayout().setActiveItem((idx === 0)? 1 : 0);
      this._rpTaskflowsContainer.remove(tfContainer);
      this._rpTaskflowsContainer.doLayout();
      Ext.History.fireEvent("change", window.location.hash);
    }
    else {
      // No taskflows available, show blank form.
      if (this.initialConfig.blankForm) {
        this._rpViewPanel.getLayout().setActiveItem(this.initialConfig.blankForm);
      }  
    }
  },
  
  /** @private */
  _beforeAppActivatedHandler: function(resultFn) {
    var layout = this._rpViewPanel.getLayout();   // the Card layout
    var activeItem = layout.activeItem;
    
    var tf = RP.iget(activeItem, RP.interfaces.ITaskForm);
    if (Ext.isDefined(tf)) {
      tf.onBeforeDeactivate(resultFn);
    }
    else {
      resultFn(true);
    }
  },

  /** @private */
  _createLandingPage: function(landingPage) {
    if (!landingPage) {
      var messageId = "rp.common.misc.NoTaskflows";
      var titleId = "rp.common.misc.NoTaskflowsTitle";
      
      if (this.initialConfig.modules.length === 0) {
          messageId = "rp.common.misc.NotAuthorized";
          titleId = "rp.common.misc.NotAuthorizedTitle";
      }
      
      landingPage = {
        xtype: "panel",
        itemId: "landingPage",
        layout: {
            type: "vbox",
            align: "center"
        },
        title: RP.getMessage(titleId),
        items: [
          {
            xtype: "box",
            flex: 0.5
          },
          {
            xtype: "panel",
            bodyCfg: {
              cls: "rp-taskflow-blank-form-text",
              align: "center"
            },
            border: false,
            layout: "fit",
            itemId: "landingPageText",
            html: RP.getMessage(messageId)
          },
          {
            xtype: "button",
            itemId: "continueButton",
            cls: "rp-taskflow-blank-form-button",
            text: RP.getMessage("rp.common.login.ContinueButton"),
            flex: 0,
            autoWidth: false,
            scale: "medium",
            listeners: {
                click: RP.util.Helpers.logout
            }
            
          },
          {
            xtype: "box",
            flex: 0.5
          }
        ]
      };
    }
    
    return Ext.ComponentMgr.create(landingPage);
  }
});

// Register xtype.
RP.core.ComponentMgr.register("rptfframe", RP.taskflow.TaskflowFrame, []);
//////////////////////
// ..\js\taskflow\widgets\WidgetWithState.js
//////////////////////
Ext.ns("RP.taskflow.widgets");

/**
 * @class RP.taskflow.widgets.WidgetWithState
 * @extends RP.taskflow.BaseTaskflowWidget
 * Representation of a widget that keeps track of its current state.
 * The possible states include:
 * <div class="mdetail-params"><ul>
 * <li>FAILED</li>
 * <li>NORMAL</li>
 * <li>SUCCESSFUL</li>
 * <li>WARNING</li>
 * <li>WORKING</li>
 * </ul></div>
 */
RP.taskflow.widgets.WidgetWithState = Ext.extend(RP.taskflow.BaseTaskflowWidget, {
  /**
   * A pseudo-enum representing the various states that the widget can have.  Subclasses can
   * add additional states.  The values of the object are implemented as strings instead of
   * integers because keeping track of integer values can be error prone if there are multiple
   * subclasses.
   * @type Object
   * @property widgetStates
   */
  widgetStates: {
    FAILED: "failed",
    NORMAL: "normal",
    SUCCESSFUL: "successful",
    WARNING: "warning",
    WORKING: "working"
  },
  
  /**
   * The CSS class of the vantage status div.
   * @type String
   * @property vantageCls
   */
  vantageCls: "rp-vantage-status",
  
  /**
   * The element representing the right side of the widget when a non-standard state
   * is being displayed, which is a div containing the class 'rp-vantage-status'.
   * @type Ext.Element
   * @property vantageStatus
   */
  /**
   * The HTML markup used to render a status icon in the widget's vantageStatus element.
   * @type String
   * @property statusImageHTML
   */
  statusImageHTML: '<img width="21" height="21" src="{0}" class="{1}">',
  
  initComponent: function() {
    if (!this.widgetState) {
      this.showNormal();
    }
    
    this.initialConfig.extraCls = "rp-widget-state";
    
    RP.taskflow.widgets.WidgetWithState.superclass.initComponent.call(this);
  },
  
  /**
   * Returns a boolean specifying if the widget is currently in the failed state
   * @return {Boolean} True if in the failed state else false
   */
  isFailed: function() {
    return this.widgetState === this.widgetStates.FAILED;
  },

  /**
   * Returns a boolean specifying if the widget is currently in the normal state
   * @return {Boolean} True if in the normal state else false
   */  
  isNormal: function() {
    return this.widgetState === this.widgetStates.NORMAL;
  },
  
  /**
   * Returns a boolean specifying if the widget is currently in the successful state
   * @return {Boolean} True if in the successful state else false
   */
  isSuccessful: function() {
    return this.widgetState === this.widgetStates.SUCCESSFUL;
  },

  /**
   * Returns a boolean specifying if the widget is currently in the warning state
   * @return {Boolean} True if in the warning state else false
   */
  isWarning: function() {
    return this.widgetState === this.widgetStates.WARNING;
  },

  /**
   * Returns a boolean specifying if the widget is currently in the working state
   * @return {Boolean} True if in the working state else false
   */
  isWorking: function() {
    return this.widgetState === this.widgetStates.WORKING;
  },
  
  /**
   * Updates the widget to show in the failed state
   */
  showFailed: function() {
    this.widgetState = this.widgetStates.FAILED;
    this.backgroundCls = "rp-widget-failed-item";
    this.imgCls = "rp-widget-failed";
    this.applyIconClasses();
  },
  
  /**
   * Updates the widget to display nothing in the status area.
   */
  showNormal: function() {
    this.widgetState = this.widgetStates.NORMAL;
    this.backgroundCls = "";
    this.imgCls = "";
    this.applyIconClasses();
  },
  
  /**
   * Updates the widget to display a green check mark in the status area.
   */
  showSuccessful: function() {
    this.widgetState = this.widgetStates.SUCCESSFUL;
    this.backgroundCls = "rp-widget-done-item";
    this.imgCls = "rp-widget-checked";
    this.applyIconClasses();
  },
  
  /**
   * Displays a warning icon in the widget's status area.
   */
  showWarning: function() {
    this.widgetState = this.widgetStates.WARNING;
    this.backgroundCls = "";
    this.imgCls = "rp-widget-warning";
    this.applyIconClasses();
  },
  
  /**
   * Updates the widget to show that it is in a working/loading state.
   */
  showWorking: function() {
    this.widgetState = this.widgetStates.WORKING;
    this.backgroundCls = "";
    this.imgCls = "rp-widget-loading";
    this.applyIconClasses();
  },
  
  /**
   * Creates the vantage status on the right side of the widget.
   * Checks if the widget is rendered and will not create it if it already exists.
   * @private
   */
  createVantageStatus: function() {
    if (this.rendered && !this.vantageStatus) {
      this.vantageStatus = this.header.createChild({
        cls: this.vantageCls
      });
    }
  },
  
  /**
   * Removes the vantage status on the right side of the widget.
   * @private
   */
  removeVantageStatus: function() {
    if (this.rendered && this.vantageStatus) {
      Ext.removeNode(this.vantageStatus.dom);
      delete this.vantageStatus;
    }
  },
  
  /**
   * Applies the background class and image class to the widget.
   * @private
   */
  applyIconClasses: function() {
    if (!this.rendered || this.isDestroyed) {
      return;
    }
    
    if (this.widgetState === this.widgetStates.NORMAL) {
      this.addClass(this.hyperlinkCls);
      this.removeVantageStatus();
    }
    else {
      // TODO this block will also run for the WidgetWithCount.  this component could
      // potentially be further refactored to follow the state pattern more closely.
      // for example, WidgetWithCount will by default have/keep the "normal" state,
      // which can be a problem if you call showSuccessful() before the componenet
      // has rendered because that state will then be overridden with a loading mask
      // by the WidgetWithCount's onPostRender call to displayCount(this._count).
      this.removeClass(this.hyperlinkCls);
      
      this.createVantageStatus();
      this.vantageStatus.dom.innerHTML = String.format(this.statusImageHTML, Ext.BLANK_IMAGE_URL, this.imgCls);
    }
    
    this.el.replaceClass(this.lastBackgroundCls, this.backgroundCls);
    this.lastBackgroundCls = this.backgroundCls;
  },
  
  /**
   * A post-render hook to apply the widget state.
   * @private
   */
  onPostRender: function() {
    RP.taskflow.widgets.WidgetWithState.superclass.onPostRender.apply(this, arguments);
    this.applyIconClasses();
  }
});
//////////////////////
// ..\js\taskflow\widgets\WidgetWithCount.js
//////////////////////
Ext.ns("RP.taskflow.widgets");

/**
 * @class RP.taskflow.widgets.WidgetWithCount
 * @extends RP.taskflow.widgets.WidgetWithState
 * Representation of a widget with a numeric count on the right side.
 */
RP.taskflow.widgets.WidgetWithCount = Ext.extend(RP.taskflow.widgets.WidgetWithState, {
  /**
   * An HTML fragment used to render the widget's status count in.
   * @type String
   * @property countHTML
   */
  countHTML: '<span class="{0}">{1}</span>',
  
  /**
   * Displays a count in the widget.  If the count is 0, a green checkmark
   * will be shown.  For anything else, the count will be displayed as passed in.
   * @param {Number/String} count
   */
  displayCount: function(count) {
    this._count = count;
    
    if (count === 0) {
      this.showSuccessful();
    }
    else {
      this.showNormal();
      
      if (!this.rendered) {
        return;
      }
          
      var statusCls = !Ext.isEmpty(count) && count.toString().length <= 3 ? "" : "rp-vantage-sum-small";
      
      this.createVantageStatus();
      this.removeClass(this.hyperlinkCls);
      this.vantageStatus.dom.innerHTML = String.format(this.countHTML, statusCls, count || "");
    }
  },
  
  /**
   * Gets the widget's current count value.
   * @return {Number}
   */
  getCount: function() {
    return this._count;
  },
  
  /**
   * A post-render hook to display the count when in the normal state.
   * @private
   */
  onPostRender: function() {
    RP.taskflow.widgets.WidgetWithCount.superclass.onPostRender.apply(this, arguments);
    
    // only if the widget is in the normal state display the count, this check
    // is needed to preserve any other state that might've been set
    if (this.widgetState === this.widgetStates.NORMAL) {
      this.displayCount(this._count);
    }
  }
});

// backwards compatibility
RP.taskflow.BaseTaskflowWidgetWithCount = RP.taskflow.widgets.WidgetWithCount;

(function() {
  var proto = RP.taskflow.widgets.WidgetWithCount.prototype;
  
  Ext.apply(proto, {
    renderCount: proto.displayCount,
    writeLine: proto.displayCount
  });
})();
//////////////////////
// ..\js\login\Login.js
//////////////////////
Ext.ns("RP.login");

Ext.QuickTips.init();

/**
 * @class RP.login.LoginForm
 * @extends Ext.Viewport
 *
 * Implements a common login form.
 */
RP.login.LoginForm = Ext.extend(Ext.Viewport, {
    authenticatedStatus: 0,
    passwordExpiredStatus: 1201,

    /**
     * @cfg {String} customerLogoSrc
     * URL of a custom image that renders in the page's upper-left corner by default.
     */
    
    /**
     * @cfg {Boolean} [externalAuthentication=false]
     * Specify true to hide the login controls and have the login automatically
     * submit (used for ReverseProxy/FederatedSSO authentication types).
     */
    externalAuthentication: false,
    
    /**
     * @cfg {Object} loginLocales 
     * An optional object map of iso locale names to display names
     * used for displaying additional locales to be used by the login page.
     */

    /**
     * @cfg {Boolean} [showJDALogo=true]
     * True to display JDA logo / branding on the login page.
     */
    showJDALogo: true,

    /**
     * @private
     */
    initComponent: function() {
        var formTitle = RP.getMessage("rp.common.login.FormTitle"),
            formPanel = {
                header: false,
                cls: "loginFormPanel",
                xtype: "form",
                id: "loginForm",
                height: 250,
                width: 300,
                hideBorders: true,
                labelAlign: 'top',
                labelPad: 8,
                labelWidth: 225,
                layoutConfig: {
                    labelSeparator: ""
                },
                defaultType: 'textfield',
                items: this._buildFormItems()
            },
            logoUrl = RP.stash.DEPLOYED_ROOT + 'images/login/logo-small.png',
            loginCt, loginItems;

        if(formTitle === "null" || formTitle === "rp.common.login.FormTitle") {
            formTitle = "<br>";
        }

        loginItems = [{
            xtype: "box",
            cls: "login_ProdName",
            html: formTitle
            //display nothing unless something is specified (through rpweb config)
        }, formPanel];

        if (this.showJDALogo || this.replaceLogoWithCustomerLogo) {
            if(this.customerLogoSrc !== undefined && this.replaceLogoWithCustomerLogo) {
                logoUrl = this.customerLogoSrc;
            }

            loginItems.splice(0, 0, {
                xtype: 'container',
                cls: 'jdaLogo',
                style: 'background:url(' + logoUrl + ') no-repeat right center;',
                width: 600,
                height: 80
            });
        }

        loginCt = Ext.ComponentMgr.create({
            cls: 'loginCt',
            items: loginItems
        }, 'container');
        
        this.createCustomLogo();
        
        Ext.apply(this, {
            items: [this.createLogoStrip(), loginCt]
        });
        
        RP.login.LoginForm.superclass.initComponent.apply(this);
    },

    /**
     * @private
     */
    _buildFormItems: function() {
        var errorHidden = !this._hasError();
        
        var formItems = [{
            fieldLabel: RP.getMessage("rp.common.login.Username"),
            id: "userName",
            cls: "rp-login-label",
            itemCls: 'loginUsername',
            hidden: this.externalAuthentication,
            width: 171,
            autoCreate: {
                tag: "input",
                type: "text",
                autocomplete: "on"
            }
        }, {
            fieldLabel: RP.getMessage("rp.common.login.Password"),
            id: "password",
            cls: "rp-login-label",
            itemCls: 'loginPassword',
            width: 171,
            hidden: this.externalAuthentication,
            inputType: "password"
        }, {
            xtype: "panel",
            id: "warning",
            cls: "loginErrorMsg",
            header: false,
            height: 30,
            hidden: errorHidden,
            html: RP.getMessage("rp.common.login.LoginFailed"),
            layout: 'fit'
        }, {
            xtype: 'panel',
            cls: 'rp-login-button-panel',
            height: 35,
            items: [{
                id: "loginBtn",
                xtype: "button",
                hidden: this.externalAuthentication,
                cls: "rp-login-button",
                height: 32,
                text: RP.getMessage("rp.common.login.LoginButtonUpper"),
                handler: this.submit,
                scope: this
            }]
        }];

        if(this.loginLocales) {
            formItems.push(this._buildLocaleCombo());
        }

        return formItems;
    },

    /**
     * @private
     */
    _buildLocaleCombo: function() {
        var localeData = [],
            currentLocale = this._getCurrentLocale();

        Ext.iterate(this.loginLocales, function(locale, localeMessage) {
            localeData.push([locale, localeMessage]);
        });

        return new Ext.form.ComboBox({
            displayField: 'localeMessage',
            editable: false,
            hideLabel: true,
            itemCls: 'locale-selector',
            lazyRender:true,
            listClass: 'locale-selector-list',
            listeners: {
                select: this.onLocaleSelect,
                scope: this
            },
            mode: 'local',
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: [
                    'locale',
                    'localeMessage'
                ],
                data: localeData
            }),
            triggerAction: 'all',
            triggerConfig: {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger"},
            typeAhead: true,
            value: currentLocale,
            valueField: 'locale',
            width: 170
        });
    },

    createLogoStrip: function() {
        return new Ext.Container({
            cls: 'loginLogoStrip',
            height: 111, // Includes 1px for top border
            items: [{
                xtype: 'box',
                cls: 'bottomBorder'
            }, {
                xtype: 'box',
                cls: 'worldMap',
                autoEl: {
                    tag: 'img',
                    src: RP.stash.DEPLOYED_ROOT + 'images/login/map.png'
                }
            }]
        });
    },

    createCustomLogo: function() {
        if(this.customerLogoSrc !== undefined) {
            var logoUrl = this.customerLogoSrc;
            if(this.replaceLogoWithCustomerLogo) {
                logoUrl = RP.stash.DEPLOYED_ROOT + 'images/login/logo-blue.png';
            }

            var externalLogoContainer = new Ext.Container({
                cls: 'jda-custom-logo',
                renderTo: Ext.getBody(),
                height: 102,
                width: 600,
                html: '',
                style: 'background:url(' + logoUrl + ') no-repeat left center;'
            });
        }
    },

    _getCurrentQueryObject: function() {
        return Ext.urlDecode(window.location.search.replace("?", ""));
    },

    _getCurrentLocale: function() {
        var queryObject = this._getCurrentQueryObject();
        return Ext.isEmpty(queryObject.locale) ? RP.globals.getValue("BASE_LOCALE") : queryObject.locale;
    },

    onLocaleSelect: function(combo) {
        var currentSelection = combo.getValue();
        if(this._getCurrentLocale() !== currentSelection) {
            var URLQueryObject = this._getCurrentQueryObject();
            URLQueryObject.locale = currentSelection;
            var urlBase = String.format("{0}//{1}{2}", window.location.protocol, window.location.host, window.location.pathname);
            window.location = urlBase + "?" + Ext.urlEncode(URLQueryObject);
        }
    },
    
    _hasError: function() {
        var querystring = window.location.search.substring(1);
        if (querystring) {
            var qsConfig = Ext.urlDecode(querystring);
            if (qsConfig && qsConfig.reason) {
                return true;
            }
        }
        
        return false;
    },
    
    /**
     * Submits a login request
     * @method submit
     */
    submit: function() {
        var results = Ext.getCmp("loginForm").getForm().getValues();
        
        if (results.userName && results.password) {
            return this.login(results.userName, results.password);
        }
        else if (this.externalAuthentication) {
            return this.login();
        }
        else {
            Ext.getCmp("warning").show();
        }
    },
    
    /**
     * @private
     */
    login: function(userId, password) {
        logger.logTrace("[Login] Submitting credentials...");
        
        var myMask = new Ext.LoadMask(document.body, {
            msg: RP.getMessage("rp.common.misc.PleaseWait")
        });
        myMask.show();
        this._disableFields();
        
        var params = {
            loginName: userId,
            password: password
        };
        
        return Ext.Ajax.requestWithTextParams({
            url: this.initialConfig.securityServiceURL,
            method: "POST",
            params: params,
            scope: this,
            disableExceptionHandling: true,
            callback: function(options, success, response) {
                // if the response times out, we won't try to cast the response text
                if (response.status === -1) {
                    logger.logFatal("[Login] Request timed out.");
                    myMask.hide();
                    this._enableFields();
                    Ext.getCmp("warning").show();
                }
                else {
                    var result;
                    try {
                        result = Ext.util.JSON.decode(response.responseText);
                        logger.logTrace("[Login] Got result from server: " + result.message);
                    }
                    catch (ex) {
                        result = {};
                        result.message = "Received an invalid response from server.";
                    }
                    
                    if (result.status === this.passwordExpiredStatus && !this.externalAuthentication) {
                        Ext.getCmp("loginForm").getComponent("password").reset();
                        myMask.hide();
                        this._enableFields();
                        
                        logger.logTrace("[Login] Displaying ChangePassword dialog.");
                        
                        var changePassword = new RP.ui.ChangePassword({
                            formTitle: RP.getMessage("rp.common.resetpassword.FormTitle"),
                            formIntro: RP.getMessage("rp.common.resetpassword.FormIntro")
                        });
                        
                        changePassword.on("destroy", function(comp) {
                            if (changePassword.success === true) {
                                var newPassword = changePassword.getNewPassword();
                                this.login(userId, newPassword);
                            }
                        }, this);
                    }
                    else if (result.status === this.authenticatedStatus) {
                        var redirectUrl = this.onAuthenticatedHandler(window.location.href);
                        if (redirectUrl) {
                            RP.util.Helpers.redirect(redirectUrl);
                        }
                        else {
                            RP.core.PageContext.setInitialURL(result.data.siteId, result.data.module);
                        }
                    }
                    else if (this.externalAuthentication && result.status !== 0) {
                        myMask.hide();
                        this._enableFields();
                        
                        var config = {
                            title: RP.getMessage("rp.common.misc.Error"),
                            msg: RP.getMessage("rp.common.login.ExternalLoginFailed"),
                            icon: Ext.MessageBox.WARNING,
                            cls: "rp-login-external-failed-msg",
                            closable: false,
                            buttons: {}
                        };
                        
                        var closeUrl = RP.globals.getPath("SSO_CLOSE_URL");
                        if (!Ext.isEmpty(closeUrl)) {
                            delete config.buttons;
                            
                            Ext.apply(config, {
                                fn: this.onExternalAuthenticationFailure,
                                buttons: {
                                    ok: RP.getMessage("rp.common.login.ContinueButton")
                                }
                            });
                        }
                        
                        Ext.MessageBox.show(config);
                    }
                    else if (!this.externalAuthentication && result.status === RP.REFSExceptionCodes.LOGIN_ATTEMPTS_EXCEEDED_EXCEPTION) {
                            logger.logError("[Login] " + result.message);
                            myMask.hide();
                            this._enableFields();
                            Ext.MessageBox.show({
                                title: RP.getMessage("rp.common.misc.Error"),
                                msg: RP.getMessage("rp.common.exception.LOGIN_ATTEMPTS_EXCEEDED_EXCEPTION"),
                                icon: Ext.MessageBox.ERROR,
                                buttons: Ext.MessageBox.OK
                            });
                    }
                    else {
                        logger.logFatal("[Login] " + result.message);
                        myMask.hide();
                        this._enableFields();
                        Ext.getCmp("warning").show();
                    }
                }
            }
        });
    },
    
    /**
     * Handler to determine where to go following a successful login.
     * If the redirectUrl is specified in the URL and fully specifies the module,
     * taskflow and/or task parameters redirect to there.  Otherwise, take the module
     * passed in as the location to go to.
     *
     * @param {String} href The url href.
     * @return {String} An string containing the redirectUrl.
     * @private
     */
    onAuthenticatedHandler: function(href) {
        var queryString = href.match(/\?(.+)$/);
        
        var redirectUrl;
        if (!Ext.isEmpty(queryString)) {
            var params = Ext.urlDecode(queryString[1]);
            redirectUrl = params.redirectUrl;
        }
        
        // Reload, and this time, it will go through to the redirectUrl URL...
        if (!Ext.isEmpty(redirectUrl) && !redirectUrl.match(/\/rp\/?$/)) {
            return redirectUrl;
        }
        
        return undefined;
    },
    
    /**
     * @private
     */
    afterRender: function() {
        Ext.EventManager.on(document, "keydown", this.keyHandler, this);
        var task = new Ext.util.DelayedTask(function() {
            Ext.getCmp("userName").focus();
        }, this);
        task.delay(200);
        RP.login.LoginForm.superclass.afterRender.apply(this);
    },
    
    /**
     * @private
     */
    beforeDestroy: function() {
        Ext.EventManager.removeListener(document, "keydown", this.keyHandler);
        RP.login.LoginForm.superclass.beforeDestroy.apply(this);
    },
    
    /**
     * @private
     */
    keyHandler: function(ev, target, opt) {
        if (Ext.MessageBox.isVisible()) {
            Ext.getCmp("warning").hide();
            Ext.getCmp("loginForm").getComponent("password").focus(false, 500);
            return;
        }
        if ((ev.keyCode === ev.ENTER) && (target.type != "button")) {
            this.submit();
        }
    },
    
    /**
     * @private
     * @param {Object} buttonId
     * @param {Object} text
     */
    onExternalAuthenticationFailure: function(buttonId, text) {
        RP.util.Helpers.redirect(RP.globals.getPath("SSO_CLOSE_URL"));
    },
    
    _disableFields: function() {
        Ext.getCmp('userName').disable();
        Ext.getCmp('password').disable();
    },
    
    _enableFields: function() {
        Ext.getCmp('userName').enable();
        Ext.getCmp('password').enable();
    }
});
//////////////////////
// ..\js\logout\LogoutForm.js
//////////////////////
Ext.ns("RP.logout");

/**
 * @class RP.logout.LogoutForm
 * @extends Ext.Viewport
 * 
 * A simple logout form that will display a generic message to the user
 * saying they've been logged out, and if an SSOCloseUrl is configured,
 * it will redirect to it after the configured time.
 */
RP.logout.LogoutForm = Ext.extend(Ext.Viewport, {
    /**
     * @cfg {Number} secondsBeforeRedirect The number of seconds to wait
     * before redirecting the user when an SSOCloseUrl is available.  Defaults to 0.
     */
    secondsBeforeRedirect: 0,
    
    initComponent: function() {
        var closeURL = RP.globals.getPath("SSO_CLOSE_URL"), text;
        
        if (!Ext.isEmpty(closeURL)) {
            text = String.format(RP.getMessage("rp.common.logout.Redirect"), closeURL);
            
            RP.util.Helpers.redirect(closeURL, this.secondsBeforeRedirect * 1000);
        }
        else {
            text = RP.getMessage("rp.common.logout.SessionEnded");
        }
        
        Ext.apply(this, {
            items: {
                xtype: "box",
                style: "padding-top: 15px; text-align: center;",
                html: text
            }
        });
        
        RP.logout.LogoutForm.superclass.initComponent.call(this);
    }
});
//////////////////////
// ..\js\formats\ExtFormats.js
//////////////////////
/**
 * Ext Formats file. This file is for all cultures.
 *
 * Globalize provides the dataformatting and ext.xml provides the messages.
 */
RP.locale.Dispatch.waitForMessageLoaded('rp.common.ext.blankTextOne', function() {
  Ext.UpdateManager.defaults.indicatorText = '<div class="loading-indicator">'+RP.getMessage("rp.common.ext.loadingText")+'</div>';

  if(Ext.DataView){
    Ext.DataView.prototype.emptyText = "";
  }

  if(Ext.grid.GridPanel){
    Ext.grid.GridPanel.prototype.ddText = RP.getMessage("rp.common.ext.ddText");
  }

  if(Ext.LoadMask){
    Ext.LoadMask.prototype.msg = RP.getMessage("rp.common.ext.loadingText");
  }


  Date.monthNames = RP.Formatting.Dates.getMonthNames();

  Date.getShortMonthName = function(month) {
    return RP.Formatting.Dates.getShortMonthNames()[month];
  }; 

  Date.monthNumbers = {
    Jan : 0,
    Feb : 1,
    Mar : 2,
    Apr : 3,
    May : 4,
    Jun : 5,
    Jul : 6,
    Aug : 7,
    Sep : 8,
    Oct : 9,
    Nov : 10,
    Dec : 11
  };

  Date.getMonthNumber = function(name) {
    return Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
  };

  Date.dayNames = RP.Formatting.Dates.getDayNames();

  Date.getShortDayName = function(day) {
    return RP.Formatting.Dates.getShortDayNames()[day];
  };

  Date.parseCodes.S.s = RP.getMessage("rp.common.ext.ordinalNumber");

  if(Ext.MessageBox){
    Ext.MessageBox.buttonText = {
      ok     : RP.getMessage("rp.common.ext.ok"),
      cancel : RP.getMessage("rp.common.ext.cancel"),
      yes    : RP.getMessage("rp.common.ext.yes"),
      no     : RP.getMessage("rp.common.ext.no")
    };
  }

  if(Ext.util.Format){
    Ext.util.Format.date = function(value, format){
      if(!value) {
        return "";
      }
      if(!Ext.isDate(value)) {
        value = new Date(Date.parse(value));
      }
      return value.dateFormat(format || RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium));
    };
  }

  if(Ext.DatePicker){
    Ext.apply(Ext.DatePicker.prototype, {
      todayText         : RP.getMessage("rp.common.ext.todayText"),
      minText           : RP.getMessage("rp.common.ext.minDateText"),
      maxText           : RP.getMessage("rp.common.ext.maxDateText"),
      disabledDaysText  : "",
      disabledDatesText : "",
      monthNames        : Date.monthNames,
      dayNames          : Date.dayNames,
      nextText          : RP.getMessage("rp.common.ext.nextMonthText"),
      prevText          : RP.getMessage("rp.common.ext.prevMonthText"),
      monthYearText     : RP.getMessage("rp.common.ext.monthYearText"),
      todayTip          : RP.getMessage("rp.common.ext.todayTip"),
      ariaTitleDateFormat: RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.FullDateWithoutDayName),
      format            : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium),
      longDayFormat     : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.FullDateWithoutDayName),
      okText            : "&#160;OK&#160;",
      cancelText        : RP.getMessage("rp.common.ext.cancel"),
      startDay          : RP.Formatting.Dates.getWeekStartDay()
    });
  }

  if(Ext.PagingToolbar){
    Ext.apply(Ext.PagingToolbar.prototype, {
      beforePageText : RP.getMessage("rp.common.ext.beforePageText"),
      afterPageText  : RP.getMessage("rp.common.ext.afterPageText"),
      firstText      : RP.getMessage("rp.common.ext.firstPageText"),
      prevText       : RP.getMessage("rp.common.ext.prevPageText"),
      nextText       : RP.getMessage("rp.common.ext.nextPageText"),
      lastText       : RP.getMessage("rp.common.ext.lastPageText"),
      refreshText    : RP.getMessage("rp.common.ext.refreshText"),
      displayMsg     : RP.getMessage("rp.common.ext.displayMsg"),
      emptyMsg       : RP.getMessage("rp.common.ext.emptyMsg")
    });
  }

  if(Ext.form.BasicForm){
      Ext.form.BasicForm.prototype.waitTitle = RP.getMessage("rp.common.ext.waitTitle");
  }

  if(Ext.form.Field){
    Ext.form.Field.prototype.invalidText = RP.getMessage("rp.common.ext.invalidFieldText");
  }

  if(Ext.form.TextField){
    Ext.apply(Ext.form.TextField.prototype, {
      minLengthText : RP.getMessage("rp.common.ext.minLengthText"),
      maxLengthText : RP.getMessage("rp.common.ext.maxLengthText"),
      blankText     : RP.getMessage("rp.common.ext.blankText"),
      regexText     : "",
      emptyText     : null
    });
  }

  if(Ext.form.NumberField){
    Ext.apply(Ext.form.NumberField.prototype, {
      decimalSeparator : RP.Formatting.Numbers.getDecimalSeparator(),
      decimalPrecision : 2,
      minText : RP.getMessage("rp.common.ext.minValText"),
      maxText : RP.getMessage("rp.common.ext.maxValText"),
      nanText : RP.getMessage("rp.common.ext.nanValText")
    });
  }

  if(Ext.form.DateField){
    Ext.apply(Ext.form.DateField.prototype, {
      disabledDaysText  : RP.getMessage("rp.common.ext.disabledText"),
      disabledDatesText : RP.getMessage("rp.common.ext.disabledText"),
      minText           : RP.getMessage("rp.common.ext.minDateText"),
      maxText           : RP.getMessage("rp.common.ext.maxDateText"),
      invalidText       : RP.getMessage("rp.common.ext.invalidDateText"),
      format            : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium),
      altFormats        : RP.Formatting.Dates.createAltString()
    });
  }

  if(Ext.form.ComboBox){
    Ext.apply(Ext.form.ComboBox.prototype, {
      loadingText       : RP.getMessage("rp.common.ext.loadingText"),
      valueNotFoundText : undefined
    });
  }

  if(Ext.form.VTypes){
    Ext.apply(Ext.form.VTypes, {
      emailText    : RP.getMessage("rp.common.ext.emailText"),
      urlText      : RP.getMessage("rp.common.ext.urlText"),
      alphaText    : RP.getMessage("rp.common.ext.alphaText"),
      alphanumText : RP.getMessage("rp.common.ext.alphanumText")
    });
  }

  if(Ext.form.HtmlEditor){
    Ext.apply(Ext.form.HtmlEditor.prototype, {
      createLinkText : 'Please enter the URL for the link:',
      buttonTips : {
        bold : {
          title : RP.getMessage("rp.common.ext.boldTitle"),
          text  : RP.getMessage("rp.common.ext.boldText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        italic : {
          title : RP.getMessage("rp.common.ext.italicTitle"),
          text  : RP.getMessage("rp.common.ext.italicText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        underline : {
          title : RP.getMessage("rp.common.ext.underlineTitle"),
          text  : RP.getMessage("rp.common.ext.underlineText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        increasefontsize : {
          title : RP.getMessage("rp.common.ext.growTitle"),
          text  : RP.getMessage("rp.common.ext.growText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        decreasefontsize : {
          title : RP.getMessage("rp.common.ext.shrinkTitle"),
          text  : RP.getMessage("rp.common.ext.shrinkText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        backcolor : {
          title : RP.getMessage("rp.common.ext.highlightTitle"),
          text  : RP.getMessage("rp.common.ext.highlightText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        forecolor : {
          title : RP.getMessage("rp.common.ext.fontColorTitle"),
          text  : RP.getMessage("rp.common.ext.fontColorText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        justifyleft : {
          title : RP.getMessage("rp.common.ext.leftTitle"),
          text  : RP.getMessage("rp.common.ext.leftText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        justifycenter : {
          title : RP.getMessage("rp.common.ext.centerTitle"),
          text  : RP.getMessage("rp.common.ext.centerText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        justifyright : {
          title : RP.getMessage("rp.common.ext.rightTitle"),
          text  : RP.getMessage("rp.common.ext.rightText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        insertunorderedlist : {
          title : RP.getMessage("rp.common.ext.bulletTitle"),
          text  : RP.getMessage("rp.common.ext.bulletText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        insertorderedlist : {
          title : RP.getMessage("rp.common.ext.numberedTitle"),
          text  : RP.getMessage("rp.common.ext.numberedText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        createlink : {
          title : RP.getMessage("rp.common.ext.hyperLinkTitle"),
          text  : RP.getMessage("rp.common.ext.hyperLinkText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        },
        sourceedit : {
          title : RP.getMessage("rp.common.ext.sourceTitle"),
          text  : RP.getMessage("rp.common.ext.sourceText"),
          cls   : Ext.baseCSSPrefix + 'html-editor-tip'
        }
      }
    });
  }

  if(Ext.grid.GridView){
    Ext.apply(Ext.grid.GridView.prototype, {
      sortAscText  :RP.getMessage("rp.common.ext.sortAscText"),
      sortDescText :RP.getMessage("rp.common.ext.sortDescText"),
      columnsText  :RP.getMessage("rp.common.ext.columnsText")
    });
  }

  if(Ext.grid.GroupingView){
    Ext.apply(Ext.grid.GroupingView.prototype, {
      emptyGroupText :RP.getMessage("rp.common.ext.emptyGroupText"),
      groupByText    :RP.getMessage("rp.common.ext.groupByText"),
      showGroupsText :RP.getMessage("rp.common.ext.showGroupsText")
    });
  }

  if(Ext.grid.PropertyColumnModel){
    Ext.apply(Ext.grid.PropertyColumnModel.prototype, {
      nameText   :RP.getMessage("rp.common.ext.nameText"),
      valueText  :RP.getMessage("rp.common.ext.valueText"),
      dateFormat : RP.Formatting.Dates.getExtShortDatePattern(),
      falseText:RP.getMessage("rp.common.ext.falseText")
    });
  }

  if(Ext.grid.BooleanColumn){
     Ext.apply(Ext.grid.BooleanColumn.prototype, {
        trueText  :RP.getMessage("rp.common.ext.trueText"),
        falseText :RP.getMessage("rp.common.ext.falseText"),
        undefinedText: '&#160;'
     });
  }

  if(Ext.grid.NumberColumn){
      Ext.apply(Ext.grid.NumberColumn.prototype, {
          format : String.format("0{0}000{1}00", RP.Formatting.Numbers.getThousandSeparator(), RP.Formatting.Numbers.getDecimalSeparator())
      });
  }

  if(Ext.grid.DateColumn){
      Ext.apply(Ext.grid.DateColumn.prototype, {
          format : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium)
      });
  }

  if(Ext.layout.BorderLayout && Ext.layout.BorderLayout.SplitRegion){
    Ext.apply(Ext.layout.BorderLayout.SplitRegion.prototype, {
      splitTip            : RP.getMessage("rp.common.ext.splitTip"),
      collapsibleSplitTip : RP.getMessage("rp.common.ext.collapsibleSplitTip")
    });
  }

  if(Ext.form.TimeField){
    Ext.apply(Ext.form.TimeField.prototype, {
      minText     : RP.getMessage("rp.common.ext.minTimeText"),
      maxText     : RP.getMessage("rp.common.ext.maxTimeText"),
      invalidText : RP.getMessage("rp.common.ext.invalidTimeText"),
      format      : RP.Formatting.Times.getExtPattern(RP.core.Formats.Time.Default),
      altFormats  : "g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H"
    });
  }

  if(Ext.form.CheckboxGroup){
    Ext.apply(Ext.form.CheckboxGroup.prototype, {
      blankText : RP.getMessage("rp.common.ext.blankTextAtLeast")
    });
  }

  if(Ext.form.RadioGroup){
    Ext.apply(Ext.form.RadioGroup.prototype, {
      blankText : RP.getMessage("rp.common.ext.blankTextOne")
    });
  }
});
//////////////////////
// ..\js\..\..\stashLibEndLoad.js
//////////////////////
RP.stash.api.endLoadLib();
