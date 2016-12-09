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
 * API for internationalizing messages and date formats
 *
 * @class RP.locale.Dispatch
 * @singleton
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
    
    // Define a locale pack.    Will overwrite a previous definition if they have the same name.
    var defineLocalePack = function(lp, override) {
        logger.logDebug("[Dispatch] Adding locale pack definition: " + lp.name);
        
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
    
    // Gets a locale pack's URL.    Locale-sensitive.
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
    
    return {
        isMessageKeyEnabled: function() {
            return messageKeyEnabled;
        },

        /**
         * Define one or more locale packs.
         * @param {Object/Object[]} locale pack definition(s).    Each
         * object must have the 'name' and 'url' properties
         * @param {Boolean} override Flag indicating whether or not to override
         * existing message pack definition(s)
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
         * Append one or more URLs to an existing locale pack definition.
         * Definition may be defined at a later point. These URLs will be loaded
         * *after* the ones defined in {@link #defineLocalePacks}.
         * @param {String} name The locale pack name
         * @param {String/String[]} urls URL(s) to append
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
         * Download the named locale pack.    This is done asynchronously, as it generates a script
         * tag to tell the browser to include a .js file with the locales we want.    The .js file
         * with the locales should call this singleton's setLocales() method to store locales.
         * @param {String} name The locale pack name
         * @param {Function} callback (optional) A callback function to be called when the locale pack has finished loading.
         * @param {Object} scope (optional) The scope in which the callback is executed.
         */
        includeLocalePack: function(name, callback, scope) {
            logger.logDebug("[Dispatch] Including locale pack: " + name);
            
            var urls = getLocalePackURLs(name);
            
            if (Ext.isFunction(callback)) {
                    var bindedCallback = Ext.bind(callback, scope);
                    
                    RP.util.ScriptLoader.load(urls, bindedCallback, bindedCallback);
            }
            else {
                    RP.util.ScriptLoader.load(urls, Ext.emptyFn, Ext.emptyFn);
            }
        },
        
        /**
         * Set multiple message values
         * @param {String} nameSpace Common namespace for all messages in messageObj.
         * @param {Object} messagesObj An object with properties containing message values.
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
         * Returns a named message.    If not found will return "{name}" where name 
         * is the message name being passed in.
         * @param {String} name The message name
         * @return {String} The current locale's message value
         */
        getMessage: function(name) {
            if (typeof msgDict[name] === "undefined") {
                logger.logError("[Dispatch] Message not found: " + name);
                
                if (Ext.isDefined(RP.globals.getValue("SERVER_TYPE")) && RP.globals.getValue("SERVER_TYPE").toUpperCase() === "TEST") {
                        RP.throwError("Message ID " + name + " was not found in the loaded message packs.");
                }
            }
            var message = (msgDict[name] || name);
            if (messageKeyEnabled) {
                message = Ext.String.format('<span title="{0}">{1}</span>', name, message);
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


/**
 * @member RP
 * @method getMessage
 * Shorthand for {@link RP.locale.Dispatch#getMessage}.
 */
RP.getMessage = RP.locale.Dispatch.getMessage;
//////////////////////
// ..\js\Ajax.js
//////////////////////
Ext.ns("RP");

RP.Ajax = Ext.Ajax; // backwards compatibility

/**
 * @class RP.Ajax
 * @extends Ext.Ajax
 * @singleton
 *
 * Overrides to the standard {@link Ext.Ajax} class.
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
    displayExceptionDialog: function(status, errorMessage, callback) {
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
        else if (status < RP.REFSExceptionCodes.OK) {
            dialogMessage = RP.getMessage("rp.common.exception.dialogUnexpectedMessage");
        }
        
        Ext.Msg.alert(RP.getMessage("rp.common.exception.dialogTitle"), Ext.String.format(template, dialogMessage, errorMessage, RP.getMessage("rp.common.exception.dialogStatusLabel"), status), callback);
    },
    
    /**
     * Make a request using standard text form parameters.  To disable the default
     * error handling, specify 'disableExceptionHandling' as true.
     *
     * @param {Object} params The parameters object used to call {@link Ext.Ajax#request}
     * @deprecated
     */
    requestWithTextParams: function(params) {
        RP.util.DeprecationUtils.logStackTrace("[DEPRECATED] RP.Ajax.requestWithTextParams is deprecated. Please use RP.Ajax.request(params);");
        Ext.Ajax.request(params);
    },
    
    /**
     * Make a request using JSON parameters.
     *
     * @param {Object} params The parameters object used to call {@link Ext.Ajax#request}
     * @deprecated
     */
    requestWithJSONParams: function(params) {
        RP.util.DeprecationUtils.logStackTrace("[DEPRECATED] RP.Ajax.requestWithJSONParams is deprecated. Please use RP.Ajax.request(params);");
        Ext.Ajax.request(params);
    },
    
    /**
     * 
     * @param {Object} options Object containing the following properties:<ul>
     * <li><b>disableExceptionHandling</b>: {Boolean}<br />Whether to disable
     * exception handling</li>
     * <li><b>operation</b>: {Object}<br />Object containing the following
     * property:<ul><li><b>disableExceptionHandling</b>: {Boolean}<br />
     * Whether to disable exception handling</li></li></ul> 
     * @return {Boolean} Whether user is handling exceptions
     * @private
     */
    isUserHandlingException: function(options) {
        if (options.disableExceptionHandling === true) {
            return true;
        }
        
        if (options.operation && options.operation.disableExceptionHandling === true) {
            return true;
        }
        
        return false;
    }
});

RP.Ajax.addEvents(
    /**
     * @event applicationrequestexception
     * Fires as a result of an exception being thrown from the application server.
     * The response object should contain a reference in data of the response coming
     * from the server.
     *
     * The function handler may return a boolean flag that will stop any additional dialog
     * messages from being displayed to the user.  Indicating "false" is stating
     * that the error handling has been completed for the exception thrown.
     *
     * @param {Ext.data.Connection} conn This Connection object.
     * @param {Object} response The response object.
     * @param {Object} options The options config object passed to the {@link #request} method.
     * @return {Boolean} False to stop additional error handling.
     */
    "applicationrequestexception",

    /**
     * @event sessionterminated
     *
     * Fires when the Ajax request senses that the user's session is deemed terminated.
     */
    "sessionterminated"
);

// global registration of all Ajax calls that fail
Ext.Ajax.on('requestexception', function(conn, response, options) {
    Ext.Ajax.activeRequests--;
    
    // If the user is leaving the page, swallow the exception since the exception came from
    // the browser killing the ajax requests during it's unload process.
    if (window.leavingPage) {
        return;
    }
    
    var urlMsg = options.CallMethod ? "Proxy to " + options.CallMethod : options.url;
    logger.logError(Ext.String.format("<br /><span style='padding-left: 3em'>Failed - URL: {0}</span><br /><span style='padding-left: 3em'> Parameters: {1}</span> <br /><span style='padding-left: 3em'>Status Text: {2}</span>", urlMsg, options.params, response.statusText));
    
    // If disableExceptionHandling is set to true then we don't want to display a message to the user
    var responseObject = Ext.JSON.decode(response.responseText, true);
    var status = response.status;
    if (!Ext.isEmpty(responseObject) && !Ext.isEmpty(responseObject.status)) {
        status = responseObject.status;
    }
    var exceptionMessageIdRoot = "rp.common.exception.";
    var messageId = RP.REFSExceptionMessages[status];
    if (Ext.isEmpty(messageId)) {
        messageId = "GENERAL_EXCEPTION";
    }
    
    if (status === RP.REFSExceptionCodes.CONNECTION_SESSION_REVOKED_EXCEPTION) {
        var callback = RP.util.Helpers.logout;
        Ext.Msg.alert(RP.getMessage("rp.common.login.SessionErrorTitle"), RP.getMessage(exceptionMessageIdRoot + messageId), callback);
    }
    else if (!Ext.Ajax.isUserHandlingException(options)) {
        Ext.Ajax.displayExceptionDialog(status, RP.getMessage(exceptionMessageIdRoot + messageId));
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
        var responseObj = Ext.JSON.decode(response.responseText, true);
        if (responseObj === null) {
            logger.logError("An error occurred trying to parse the response as JSON.");
        }
        else {
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
                if (handleException && !Ext.Ajax.isUserHandlingException(options)) {
                    var messageId = RP.REFSExceptionMessages[status];
                    Ext.Ajax.displayExceptionDialog(status, RP.getMessage("rp.common.exception." + messageId));
                }
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
 * A Math API supplying what the native JavaScript one does not.
 */
Ext.define("RP.Math", {
  singleton: true,
  
  /**
   * Rounds a number (cValue) to (iScale) decimal places, returning the rounded number.
   * For example:   
   *                cValue  iScale  return
   *                1.04      1     1.1
   *                1.5       0     2
   *                0.285     2     0.29        special case
   * 0.285 is a special case where .285*100 = 28.499999999999996
   * which rounds incorrectly. To fix this, we scale by one more, and then divide by 10. Scaling by one
   * more results in an integer which takes away possible floating-point errors.
   * @param {Number} cValue value to round.
   * @param {Number} iScale number of decimal places to round to.
   * @return {Number} The rounded number.
   */
  roundDecimal: function(cValue, iScale) {
    var iMultiplier = Math.pow(10, iScale);
    cValue = (Math.round((cValue * (iMultiplier * 10)) / 10) / iMultiplier);
    return cValue;
  }
});
//////////////////////
// ..\js\String.js
//////////////////////
/**
 * A String API supplying what the native JavaScript one does not.
 */
Ext.define("RP.String", {
    singleton: true,
    
    /**
     * Returns true if the string contains the specified substring.
     * @param {String} string The string to perform the contains operation on.
     * @param {String} value The value to look for in the string.
     * @return {Boolean} Return is true if value is a substring of string
     */
    contains: function(string, value) {
        return string.indexOf(value) !== -1;
    }
});
//////////////////////
// ..\js\Ext.js
//////////////////////
Ext.applyIf(Ext, {
    /**
     * @method isMobileSafari
     * @member Ext
     * 
     * True if the detected browser is mobile safari (iPad, iPod, iPhone).
     * @type Boolean
     */
    isMobileSafari: function() {
        return navigator.userAgent.toLowerCase().indexOf("mobile") !== -1 && Ext.isSafari;
    }(),
    
    /**
     * @method isMobileSafari
     * @member Ext
     * 
     * Checks the current Document Mode of IE. document.documentMode is a IE only attribute.
     * @param {Number} modeNumber The number of the document mode you wish to check
     * @return {Boolean} true if Document Mode is modeNumber
     */
    isIEDocMode: function(modeNumber) {
        return Ext.isIE && document.documentMode == modeNumber; 
    }
});
//////////////////////
// ..\js\FiscalDatePicker.js
//////////////////////
/**
 * @class RP.FiscalDatePicker
 * @extends Ext.picker.Date
 * 
 * An extension that displays fiscal weeks.
 */
 Ext.define("RP.FiscalDatePicker", {
    extend: "Ext.picker.Date",
    alias: "widget.rpfiscaldatepicker",
    
    // override of the width set on Ext.picker.Date's prototype (178) to
    // account for the additional 34 pixels of the 'Wk' column.
    width: 212,
    
    initComponent: function() {
        // add a safety check around this...
        if (Ext.isNumber(RP.globals.getValue("CALENDAR").startDayOfWeek)) {
            this.startDay = RP.globals.getValue("CALENDAR").startDayOfWeek;
        }
        
        this.callParent();
    },
    
    /**
     * Hook into the onRender template method to render the fiscal week column.
     * @private
     */
    onRender: function() {
        this.callParent(arguments);
        
        if (Ext.isEmpty(RP.globals.getValue("CALENDAR").fiscalPeriods)) {
            // if there is no data, there is nothing to render.  Skip the week rendering process.
            return;
        }
        
        this.addCls("rp-fiscal-date-picker"); // class providing adjusted widths/borders
        
        var table = this.el.down(".x-datepicker-inner");
        
        // add the header row
        var headerMarkup = "<th><span>{0}</span></th>";
        var weekHeader = RP.getMessage("rp.common.components.fiscalDatePicker.weekHeader");
        var headerRow = table.down("tr");
        headerRow.insertHtml("afterBegin", Ext.String.format(headerMarkup, weekHeader));
        
        
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
        }, this);
    },
    
    update: function(date) {
        this.callParent(arguments);
        
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
                if (RP.Date.between(date, fiscalPeriods[j].startDate, fiscalPeriods[j].endDate)) {
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
            var dates = weekCell.parent().parent().select("." + this.baseCls + "-date");
            
            // get the fiscal period in the context of the current date row.
            var fiscalContext = this.findFiscalPeriod(fiscalPeriods, dates);
            var beginOfWeek = fiscalContext.beginWeek;
            var fiscalPeriodIdx = fiscalContext.fiscalPeriodIdx;
            
            if (fiscalPeriodIdx !== null) {
                var fiscalPeriod = fiscalPeriods[fiscalPeriodIdx];
                var offset = Ext.Date.getDayOfYear(fiscalPeriod.startDate);
                var weekOffset = this.determineBeginningOfYearWeekOffset(this.startDay, fiscalPeriod);
                var isLeapYear = Ext.Date.isLeapYear;
                
                var dayOfYear = Ext.Date.getDayOfYear(beginOfWeek);
                if (dayOfYear < offset) {
                    // 365 is used in this adjustment to ensure that the dayOfYear is 
                    // greater than the beginning offset.
                    // For Example, Start Of Year is 10/1/2010, offset will be 273.
                    // If the current date is 12/01/2010, the dayOfYear is 334.  The difference will
                    // calculate out to be a positive number.
                    // In the case that the calendar rolls over to the next year, 1/1/2011, the dayOfYear is
                    // 0.  This case, the dayOfYear needs to be adjusted forward by a full year to ensure that the
                    // calculations are positive.
                    dayOfYear += 365 + (isLeapYear(fiscalPeriod.startDate) || isLeapYear(fiscalPeriod.endDate) ? 1 : 0);
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
            
            weekCell.down("span").update(weekNumber.toString());
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
        var dates = weekRow.parent().select("." + this.baseCls + "-date");

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
     * method inherited from Ext.picker.Date, but passing in the first day of the week.
     * @param {Ext.EventObject} event
     * @param {HtmlElement} target
     * @private
     */
    onFiscalWeekClick: function(event, target) {
        this.handleDateClick(event, Ext.fly(target).parent().next().down("." + this.baseCls + "-date").dom);
    }
});
//////////////////////
// ..\js\TimeSpan.js
//////////////////////
/**
 * A simple class representing a span of time. Construct using a number of milliseconds.
 * 
 * @class RP.TimeSpan
 * @method constructor
 * @param {Number/Object} ms The number of milliseconds in this span of time.
 */
RP.TimeSpan = function(ms) {
    this.ms = ms;
};

/**
 * Returns a new TimeSpan, sum of the this and ts
 * @param {RP.TimeSpan} ts The TimeSpan to add to this.
 * @return {RP.TimeSpan} The sum of this and ts
 */
RP.TimeSpan.prototype.plus = function(ts) {
    if (ts.ms) {
        return new RP.TimeSpan(this.ms + ts.ms);
    }
    return this;
};

/**
 * Returns a new TimeSpan, difference of this and ts.
 * @param {RP.TimeSpan} ts The TimeSpan to subtract from this.
 * @return {RP.TimeSpan} The difference of this and ts.
 */
RP.TimeSpan.prototype.minus = function(ts) {
    if (ts.ms) {
        return new RP.TimeSpan(this.ms - ts.ms);
    }
    return this;
};

/**
 * Returns the number of milliseconds in this TimeSpan.
 * @return {Number} The number of milliseconds in this TimeSpan.
 */
RP.TimeSpan.prototype.getTotalMilliseconds = function() {
    return this.ms;
};

/**
 * Returns the total number of milliseconds represented by this TimeSpan.
 * Dependent on {@link #getTotalMilliseconds}.
 * @return {Number} The total number of milliseconds
 */
RP.TimeSpan.prototype.totalMilliseconds = function() {
    return this.getTotalMilliseconds();
};

/**
 * Returns the total number of seconds represented by the TimeSpan.
 * @return {Number} The total number of seconds
 */
RP.TimeSpan.prototype.totalSeconds = function() {
    return this.getTotalMilliseconds() / 1000.0;
};

/**
 * Returns the total number of minutes represented by the TimeSpan.
 * @return {Number} The total number of minutes
 */
RP.TimeSpan.prototype.totalMinutes = function() {
    return this.getTotalMilliseconds() / 60000.0;
};

/**
 * Returns the total number of hours represented by the TimeSpan.
 * @return {Number} The total number of hours
 */
RP.TimeSpan.prototype.totalHours = function() {
    return this.getTotalMilliseconds() / 3600000.0;
};

/**
 * Returns the total number of days represented by the time span.
 * @return {Number} the total number of days
 */
RP.TimeSpan.prototype.totalDays = function() {
    return this.getTotalMilliseconds() / 86400000.0;
};
//////////////////////
// ..\js\ux\Store.js
//////////////////////
/**
 * Extends Ext.data.Store to provide additional functionality, such as maintaining a list
 * of deleted records.
 */
Ext.define("RP.ux.Store", {
    extend: "Ext.data.Store",
    
    /**
     * @property {Boolean} isLoaded
     * True if the store has ever been loaded.
     */
    isLoaded: false,
    
    /**
     * @cfg {Boolean} trackDeletedRecords
     * Specify true to store deleted records, retrievable through 
     * {@link RP.ux.Store#getDeletedRecords}.
     */
    
    /**
     * Construct a Store using normal Ext.data.Store config
     * @private
     */
    constructor: function(config) {
        this.callParent(arguments);
        
        if (config.trackDeletedRecords) {
            this._deletedRecords = [];
            
            this.on("remove", function(store, rec, index) {
                this._deletedRecords.push(rec);
            }, this);
            
            this.on("load", function() {
                delete this._deletedRecords;
                this._deletedRecords = [];
            }, this);
        }
        
        this.on("load", function() {
            this.isLoaded = true;
        }, this);
    },
    
    
    /**
     * Returns the array of deleted records. Will return null if
     * {@link #trackDeletedRecords} was not set to true.
     * 
     * @return {Ext.data.Model[]} Records which were deleted
     * at some point.
     */
    getDeletedRecords: function() {
        return this._deletedRecords;
    }
});
//////////////////////
// ..\js\ux\IFrame.js
//////////////////////
/**
 * @class Ext.ux.IFrameComponent
 * @extends Ext.Component
 * <p>An simple Component that can be used to dipslay an external web
 * application by rendering it inside an iframe.</p.
 * @cfg {String} url The iframe "src" URL.
 */
Ext.define("Ext.ux.IFrameComponent", { // TODO Ext JS 4 evaluate moving/renaming this component...
    extend: "Ext.Component",
    
    initComponent: function() {
        this.autoEl = {
            tag: "iframe",
            frameborder: 0,
            src: this.url
        };
        
        this.callParent();
    },

    load: function(url) {
        this.getEl().dom.src = url || this.url;
    }
});
//////////////////////
// ..\js\form\DateField.js
//////////////////////
/**
 * <p>Presents a new DateField. Optionally, shows a fiscal version. If invalid text is given,
 * invalidText is defaulted to ensure use of a Locale Pack.</p>
 * 
 * @cfg {Boolean} showFiscal Specify true to render the fiscal version of {@link RP.menu.DateMenu}.
 * defaults to false
 * @cfg {Object} invalidText Text to display when given an invalid entry
 */
Ext.define("RP.form.DateField", {
    extend: "Ext.form.field.Date",
    
    alias: ["widget.rpdatefield", "RP.ui.DateField"],
    
    initComponent: function() {
        // Set default invalidText if not part of initialConfig
        if (!this.initialConfig.invalidText) {
            Ext.apply(this, {
                // invalidText will be used in a Ext.String.format with {0} = value and {1} = format
                invalidText: RP.getMessage("rp.common.misc.InvalidDate")
            });
        }
        
        this.callParent();
    },
    
    /**
     * Creates and returns a Picker, either standard {@link Ext.form.field.Picker} or 
     * fiscal {@link RP.FiscalDatePicker}.
     * @return {Ext.form.field.Picker/RP.FiscalDatePicker}  Picker object created
     */
    createPicker: function() {
        var picker = this.callParent();
        
        if (this.showFiscal === true) {
            picker = Ext.create("RP.FiscalDatePicker", picker.initialConfig);
        }
        
        return picker;
    }
});
//////////////////////
// ..\js\form\DirectionalDateField.js
//////////////////////
/**
 * <p>Wraps {@link RP.form.DateField} to implement a date picker. Left and right buttons 
 * are added to allow navigation of the calendar. Navigation can be done in a custom number of days
 * </p>
 * <p>The config will also be passed to {@link RP.form.DateField}.</p>
 *
 * @cfg {Number} dayInterval The number of days to skip via the left/right buttons.  Defaults to 1.
 * @cfg {String} extraCls An extra CSS class that will be added to the container. (optional) 
 * @cfg {Boolean} showFiscal Specify true to render the fiscal version of {@link RP.menu.DateMenu}.
 * Defaults to false.
 */
Ext.define("RP.form.DirectionalDateField", {
    extend: "Ext.form.FieldContainer",
    
    alias: ["widget.rpdatepicker", "RP.ui.DatePicker"],
    
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
        
        var staticConfig = {
            xtype: "rpdatefield",
            width: 120, 
            allowBlank: false,
            hideLabel: true,
            enableKeyEvents: true,
            checkChangeEvents: ["change"],
            value: this.initialConfig.value,
            disabled: this.initialConfig.disabled,
            maxValue: this.initialConfig.maxValue,
            emptyText: this.initialConfig.emptyText || RP.getMessage("rp.common.misc.EmptyDateText"),
            listeners: {
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
            }
        };
        
        // merge up the initial configuration with our static configuration
        // this allows us to configure the date picker per application basis
        Ext.applyIf(staticConfig, this.initialConfig);
        
        this._state.dateField = Ext.ComponentMgr.create(staticConfig);
        this._state.dateField.blankText = RP.getMessage("rp.common.misc.MissingDate");
        
        this._state.dateField.on("select", function(df, date) {
            var parsedDate = RP.Formatting.Dates.parse(this._state.dateField.getRawValue(), RP.core.Formats.Date.Medium);
            if ((typeof(date) === "object") || !isNaN(parsedDate)) {
                var newDate = this._state.dateField.getValue();
                
                if ((!Ext.isDate(this._lastSelectedValue) && Ext.isDate(newDate)) || (Ext.isDate(newDate) && newDate.getTime() !== this._lastSelectedValue.getTime())) {
                    this.fireEvent("select", this, newDate);
                    this._lastSelectedValue = RP.Date.clone(newDate);
                }
            }
        }, this);
        
        // RPWEB-3584
        // safeParse added because RP's Date.parseDate breaks when adding initTime to it. Using Date.extParseDate to
        // avoid the bug involving places that have DST at midnight.
        this._state.dateField.safeParse = function(value, format) {
            if (/[gGhH]/.test(format.replace(/(\\.)/g, ''))) {
                // if parse format contains hour information, no DST adjustment is necessary
                return RP.Date.parseDate(value, format);
            }
            var parsedDate = Ext.Date.parse(value + ' ' + this.initTime, format + ' ' + this.initTimeFormat);
            if (parsedDate) {
                return RP.Date.clearTime(parsedDate);
            }
        };
        
        // Hack for the FieldContainer layout engine to ensure that
        // it is rendered appropriately when the label is not specified.
        if (!Ext.isDefined(this.fieldLabel)) {
            this.width = 144;
        }
        
        Ext.apply(this, {
            cls: this.initialConfig.cls || 'rp-datepicker',
            disabledCls: "",
            layout: "hbox",
            items: this._getDatePickerItems()
        });
        
        if (Ext.isDefined(this.extraCls)) {
            this.addClass(this.extraCls);
        }
        
        this.callParent();
    },
    
    /**
     * Disables the DirectionalDateField, and internal DateField, also.
     */
    disable: function() {
        this.callParent();
        
        Ext.iterate(this.items.keys, function(key) {
            Ext.getCmp(key).disable();
        });
        this._state.dateField.disable();
    },
    
    /**
     * Enables the DirectionalDateField and internal DateField, also.
     */
    enable: function() {
        this.callParent();
        
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
        if (Ext.isDate(date)) {
            date = RP.Date.addDays(date, this._state.dayInterval);
            this.setValue(date);
        }
    },
    
    /**
     * Programmatically moves the date picker backward in time.  The movement backward
     * is based on the configuration of dayInterval.
     */
    backward: function() {
        var date = this.getValue();
        if (Ext.isDate(date)) {
            date = RP.Date.addDays(date, -this._state.dayInterval);
            this.setValue(date);
        }
    },
    
    /**
     * Getter function for the internal {@link RP.form.DateField}.
     */
    getDateFieldComponent: function() {
        return this._state.dateField;
    },
    
    /**
     * Setter function for the current {@link Date}.
     * @param {Date} val
     */
    setValue: function(val) {
        this._state.dateField.setValue(val);
        this._lastSelectedValue = val;
    },
    
    /**
     * Getter function for the current {@link Date}.
     */
    getValue: function() {
        return this._state.dateField.getValue();
    },
    
    /**
     * Private method to check whether newDate is unequal to originalDate
     * @param {Object} originalDate The original date to be tested
     * @param {Object} newDate The new date to be tested
     * @return {Boolean} return is true if the newDate and originalDate 
     * aren't the same
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
     * Gets an array of items relevant to the DatePicker.
     * @return {Object[]} return is size 3 iff {@link #_useNextPreviousButtons},
     * else size 1 with only the middle dateField element
     * @return {Object} return[0] Previous 
     * @return {RP.form.DateField} return[1] internal dateField object
     * @return {Object} return[2] Next 
     * @private
     */
    _getDatePickerItems: function() {
        var items = [];
        
        if (this._useNextPreviousButtons()) {
            items.push(this._getNextPreviousButton('previous'));
        }
        
        items.push(this._state.dateField);
        
        if (this._useNextPreviousButtons()) {
            items.push(this._getNextPreviousButton('next'));
        }
        
        return items;
    },
    
    /**
     * Returns the appropriate Object direction based on string
     * @param {String} direction
     * - next
     * -previous
     * @return {Object} button which is appropriately next of previous
     * @private
     */
    _getNextPreviousButton: function(direction) {
        var iconCls = direction === 'next' ? 'rp-next-btn' : 'rp-prev-btn';
        var handler = direction === 'next' ? this.forward : this.backward;
        
        return {
            xtype: "button",
            width: 22,
            iconCls: iconCls,
            cls: "rp-raised-btn",
            scope: this,
            handler: handler
        };
    },
    
    /**
     * Reports whether or not to use the next and previous buttons
     * @return {Boolean} return is true if you are using next/prev buttons
     * @private
     */
    _useNextPreviousButtons: function() {
        if (typeof this.initialConfig.useNextPreviousButtons !== 'undefined') {
            if (!this.initialConfig.useNextPreviousButtons) {
                return false;
            }
        }
        return true;
    }
});
//////////////////////
// ..\js\data\model\Format.js
//////////////////////
/**
 * This model represents an export format for use with the RP.ui.Mixins.Exportable mixin and
 * RP.ui.ExportablePlugin plugin.
 * @author Jeff Gitter
 */
Ext.define('RP.data.model.Format', {
    extend: 'Ext.data.Model',
    fields: ['format', 'display']
});
//////////////////////
// ..\js\menu\DateMenu.js
//////////////////////
Ext.ns("RP.menu");

/**
 * An extension of {@link #picker Ext.menu.DatePicker} with the ability to add in an {@link RP.FiscalDatePicker}
 *
 * @class RP.menu.DateMenu
 * @extends Ext.menu.DatePicker
 * @cfg {Boolean} showFiscal Specify true to render an {@link RP.FiscalDatePicker}. Defaults to false.
 */
Ext.define("RP.menu.DateMenu", {
    extend: "Ext.menu.DatePicker",

    /**
     * Override the original DateMenu's initComponent to allow for the injection
     * of the {@link RP.FiscalDatePicker} if showFiscal is set to true.  This method
     * is basically identical except for the showFiscal piece.
     * @private
     */
    initComponent : function() {
        var me = this;

        Ext.apply(me, {
            showSeparator: false,
            plain: true,
            border: false,
            bodyPadding: 0, // remove the body padding from the datepicker menu item so it looks like 3.3
            items: Ext.applyIf({
                cls: Ext.baseCSSPrefix + 'menu-date-item',
                id: me.pickerId,
                xtype: this.showFiscal === true ? 'rpdatepicker' : 'datepicker'
            }, me.initialConfig)
        });

        me.callParent(arguments);

        me.picker = me.down('datepicker');
        /**
         * @event select
         * Fires when a date is selected from the {@link #picker Ext.menu.DatePicker}
         * @param {Ext.menu.DatePicker} picker The {@link #picker Ext.menu.DatePicker}
         * @param {Date} date The selected date
         */
        me.relayEvents(me.picker, ['select']);

        if (me.hideOnClick) {
            me.on('select', me.hidePickerOnSelect, me);
        }
    }
});
//////////////////////
// ..\js\interfaces\ITaskContext.js
//////////////////////
Ext.ns("RP.interfaces");

/**
 * @class RP.interfaces.ITask
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
Ext.ns("RP.interfaces");

/**
 * @class RP.interfaces.ITaskflow
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
Ext.ns("RP.interfaces");

/**
 * @class RP.interfaces.ITaskflowContainer
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
Ext.ns("RP.interfaces");

/**
 * @class RP.interfaces.IPrintSource
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
Ext.ns("RP.interfaces");

/**
 * @class RP.interfaces.ITaskForm
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
 * @class RP.interfaces.ITaskWidget
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
// ..\js\layout\Flow.js
//////////////////////
// note : we can get away with this because all browsers we support also support inline-block
/**
 * This is a very simple layout that flows child items left to right using inline-block instead of float.
 *
 * The width and height of each item is independent.  Each item will flow left to right until it hits the edge of the
 * screen and will then jump to the next inline-block row.  Example:
        Ext.create('Ext.panel.Panel', {
            layout: 'rp.flow',
            items: [{
                xtype: 'container',
                height: 200,
                width: 300
            }, {
                xtype: 'container',
                height: 100,
                width: 100
            }, {
                xtype: 'container',
                height: 200,
                width: 350
            }, {
                xtype: 'container',
                height: 100,
                width: 200
            }]
        });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.layout.Flow', {
    extend: 'Ext.layout.container.Auto',
    alias: 'layout.rp.flow',
    itemCls: 'rp-flowlayout-item',

    // inherit docs
    initLayout: function() {
        this.callParent(arguments);
        this.owner.addCls('rp-flowlayout');
    }
});
//////////////////////
// ..\js\layout\Anchor.js
//////////////////////
// note : we can get away with this because all browsers we support also support inline-block
/**
 * This is a very simple layout that flows child items left to right using inline-block instead of float.
 * This layout allows you to specify anchors for your child items.  Using this, if you create six child
 * items that each have an anchor of '33% 50%', they would all take up equal portions of the screen.
 *
 * If you want to use static widths, you should use the {@link RP.layout.Flow flow layout}.
 *
 * This example creates 6 items with 2 on top and 4 on the bottom but filling the whole taskform.
        Ext.create('Ext.panel.Panel', {
            layout: 'rp.anchor',
            items: [{
                xtype: 'container',
                anchor: '50% 50%'
            }, {
                xtype: 'container',
                anchor: '50% 50%'
            }, {
                xtype: 'container',
                anchor: '25% 50%'
            }, {
                xtype: 'container',
                anchor: '25% 50%'
            },{
                xtype: 'container',
                anchor: '25% 50%'
            }, {
                xtype: 'container',
                anchor: '25% 50%'
            }]
        });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.layout.Anchor', {
    extend: 'Ext.layout.container.Anchor',
    alias: 'layout.rp.anchor',
    itemCls: 'rp-flowlayout-item',

    // inherit docs
    initLayout: function() {
        this.callParent(arguments);
        this.owner.addCls('rp-flowlayout');
    }
});
//////////////////////
// ..\js\layout\Table.js
//////////////////////
// note : we can get away with this because all browsers we support also support inline-block
/**
 * This layout functions very similar to the table layout except that rowspans are ignored.  It uses
 * anchors to achieve the specified number of columns across the width of the container.
 *
 * This example shows how to use this layout to achieve a dashboard like layout with 3 columns across
 * using the minWidth config.
 *
        Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'rp.table',
                columns: 3,
                minWidth: 200
            },
            items: [{
                xtype: 'some.widget',
                width: 250              // this will be ignored and reset by the layout
            }, {
                xtype: 'cooler.widget',
                anchor: '0 20%'         // width anchor is ignored, but height anchor is respected
            }, {
                xtype: 'coolest.widget',
                height: 250             // this will be respected
            }, {
                xtype: 'another.widget',
                colspan: 2,             // span 2 columns with this widget
            }, {
                xtype: 'yet.another.widget',
                colspan: 3              // span the whole taskform with this widget
            }, {
                xtype: 'cool.widget',
                colspan: 4              // this will be corrected to 3 due to the constrainColumns config
            }]
        });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.layout.Table', {
    extend: 'RP.layout.Anchor',
    alias: 'layout.rp.table',

    /**
     * @cfg {Integer} columns
     * Number of columns to create in the table
     */
    columns: 1,

    /**
     * @cfg {Integer} minWidth
     * Minimum number of pixels for a column to take up.
     */

    /**
     * @cfg {Boolean} [constrainColumns=true]
     * True to constrain the colspan of items to the columns set for this layout, otherwise
     * you can intentionally set colspan higher to achieve greater width than 100%.
     */
    constrainColumns: true,


    /**
     * Set the number of columns programatically
     * 
     */
    setColumns: function(columns) {
        this.columns = columns;
        this.owner.doLayout();
    },

    /**
     * @cfg {Integer} colspan
     * This should be applied to child items of this layout. Number of columns this 
     * item should consume.
     */

    // inherit docs
    initLayout: function() {
        this.callParent(arguments);
        this.calculatedColumns = this.columns;
    },

    // inherit docs
    renderChildren: function() {
        if (!Ext.isEmpty(this.minWidth)) {
            this.calculatedColumns = this._calculateColumns();
        }
        this.callParent(arguments);
    },

    // inherit docs
    configureItem: function(item) {
        item.anchor = this._getAnchor(item.colspan || 1, item.anchor);
        this.callParent(arguments);
    },

    // inheritdocs
    calculate: function(ownerContext) {
        // If the calculated columns have changed we need to recalculate the anchors
        if (!Ext.isEmpty(this.minWidth) && this._calculateColumns() !== this.calculatedColumns) {
            ownerContext.invalidate();
        }
        this.callParent(arguments);
    },

    /**
     * @private
     * Calculate the number of columns based on the current width and minWidth of children
     * @return {Integer} Number of columns to draw
     */
    _calculateColumns: function() {
        return Ext.Number.constrain(Math.floor(this.owner.getWidth() / this.minWidth), 1, this.columns);
    },

    /**
     * @private
     * Retrieve the anchor value for a child item based on the colspan and calculated columns.
     * @param {Integer} colspan The number of columns this item spans
     * @param {String} current The current anchor value - any height anchor will be preserved
     * @return {String} The new anchor value
     */
    _getAnchor: function(colspan, current) {
        if (this.constrainColumns) {
            colspan = Math.min(colspan, this.calculatedColumns);
        }
        var anchor = Math.floor((100 * colspan) / this.calculatedColumns) + '%';
        if (!Ext.isEmpty(current)) {
            var terms = current.split(' ');
            if (!Ext.isEmpty(terms[1])) {
                anchor += ' ' + terms[1];
            }
        }
        return anchor;
    }
});
//////////////////////
// ..\js\collections\MixedCollection.js
//////////////////////
Ext.ns("RP.collections");

/**
 * @class RP.collections.MixedCollection
 * @extends Ext.util.MixedCollection
 *
 * An extension of {@link Ext.util.MixedCollection} with additional functionality, including the
 * ability to automatically trim the collection when it reaches a specified max
 * size (items added first are trimmed, like a queue)
 */
Ext.define("RP.collections.MixedCollection", {
    extend: "Ext.util.MixedCollection",

    maxSize: undefined,

    /**
     * @param {Object} config Config object
     * @cfg {Number} maxSize The maximum number of elements this collection holds. When the size
     * exceeds this value, the oldest item added is removed from the collection.
     */
    constructor: function(config) {
        this.callParent(arguments);

        if (config) {
            if (typeof config.maxSize === "number") {
                this.maxSize = config.maxSize;
                this.on("add", function(index, o, key) {
                    if (this.length > this.maxSize) {
                        this.removeAt(0);
                    }
                }, this);
            }
        }
    },

    /**
     * Get the key of an item. Note: the speed of this is O(N), so if your collection can get
     * quite large, use something else!
     * @param {Object} item The item whose key you want to look up
     * @return {Object} The key for the item you were looking for, or null if it doesn't exist in the collection
     */
    keyOf: function(item) {

        // Checks through each item in the map
        // Returns the key if it finds it
        for (var key in this.map) {
            if (this.map[key] === item) {
                return key;
            }
        }

        return null;
    },

    /**
     * Removes all items beginning at the specified index from the combo array.
     * @param {Number} index The index to start removing
     */
    trimAt: function(index) {
        if (index < this.length && index >= 0) {
            var count = this.length - index;

            this.items.splice(index, count);
            this.keys.splice(index, count);
            this.length -= count;
        }
    }
});
//////////////////////
// ..\js\collections\Stack.js
//////////////////////
Ext.namespace("RP.collections");

/**
 * Common stack (LIFO list)
 *
 * @class RP.collections.Stack
 * @deprecated 5.0.0
 */
RP.collections.Stack = function() {
    this._s = [];
};

RP.collections.Stack.prototype = {
    /**
     * Pushes item(s) onto the stack
     * @param {Array} items Items to add to the stack
     */
    push: function(/* items... */) {
        // Pushes one or more items onto stack; last one pushed is on top of the stack...
        this._s.push.apply(this._s, arguments);
    },

    /**
     * Pop one item off the stack
     * @return {Object} The popped item
     */
    pop: function() {
        var v = this.popn(1);
        if (v.length > 0) {
            return v[0];
        }

        return null;
    },

    /**
     * Pop an arbitrary number of items off the stack
     * @param {Number} n The number of items to pop
     * @return {Array} The items popped
     */
    popn: function(n) {
        n = n || 1;
        n = Math.min(n, this._s.length);

        if (n > 0) {
            return this._s.splice(this._s.length - n, n).reverse();
        }

        return [];
    },

    /**
     * Peek at the item on top of the stack
     * @return {Object} Item at the top of the stack
     */
    peek: function() {
        var v = this.peekn(1);
        if (v.length > 0) {
            return v[0];
        }

        return null;
    },

    /**
     * Peek at an arbitrary number of items on top of the stack
     * @param {Number} n The number of items to peek at
     * @return {Array} Items at the top of the stack
     */
    peekn: function(n) {
        n = n || 1;
        n = Math.min(n, this._s.length);

        if (n > 0) {
            return this._s.slice(this._s.length - n, this._s.length).reverse();
        }

        return [];
    },

    /**
     * Remove all items from the stack
     */
    clear: function() {
        this._s = [];
    },

    /**
     * Count the number of items in the stack
     * @return {Number} The number of items
     */
    count: function() {
        return this._s.length;
    },

    /**
     * Count the number of items to pop until the specified item is on top of the stack
     * @param {Object} item The item to look for
     * @return {Number} The number of items or -1 if item not found
     */
    countUntil: function(item) {
        for (var i = this._s.length - 1; i >= 0; i--) {
            if (this._s[i] === item) {
                return (this._s.length - i - 1);
            }
        }

        return -1;
    },

    /**
     * Check if the specified item exists in the stack
     * @param {Object} item The item to look for
     * @return {Boolean} Does item exist?
     */
    contains: function(item) {
        return (this.countUntil(item) >= 0);
    }
};
//////////////////////
// ..\js\panel\plugin\HeaderComponent.js
//////////////////////
/**
 * A Plugin which allows for easily embedding a Component into a Panel's Header.
 */
Ext.define('RP.panel.plugin.HeaderComponent', {
    extend: 'Ext.AbstractPlugin',
    
    /**
     * @cfg {Boolean} center True to keep the item centered in the middle of the Panel's
     * Header.  Defaults to true.
     */
    center: true,
    
    /**
     * @cfg {Boolean} removeTopBottomPadding True to remove top/bottom padding of the Panel's
     * Header to ensure the Header itself retains a relatively close total height without
     * embedded components.  Defaults to true.
     */
    removeTopBottomPadding: true,
    
    /**
     * @cfg {Boolean} adjusting
     * Used to prevent recursion in adjustSpacer
     */
    adjusting: false,
    
    /**
     * @cfg {Ext.AbstractComponent} item (required) 
     * The item to add into a Panel's Header.
     */
    
    constructor: function(config) {
        Ext.apply(this, config);
        
        if (!this.item) {
            RP.throwError('An item must be configured.');
        }
        
        // turn config object into a valid component
        this.item = Ext.ComponentManager.create(this.item);
    },
    
    init: function(panel) {
        this.cmp = panel;
        panel.on('afterrender', this.addItemToHeader, this);
    },
    
    /**
     * Returns the item that the Plugin was configured with.
     * @return {Ext.AbstractComponent} The item which was embedded
     */
    getItem: function() {
        return this.item;
    },
    
    /**
     * Adds the item to the Panel's Header.
     * @private
     */
    addItemToHeader: function(panel) {
        var header = panel.header;
        
        if (this.center) {
            this.spacer = new Ext.Component();
            
            header.on('resize', this.adjustSpacer, this);
            
            header.insert(1, [this.item, this.spacer]);
        }
        else {
            header.add(this.item);
        }
        
        // TODO does not work in legacy IE
        if (this.removeTopBottomPadding) {
            header.el.setStyle('padding-top', 0);
            header.el.setStyle('padding-bottom', 0);
        }
    },
    
    /**
     * Adjusts the spacer component to take up enough space so that the item is centered.
     * @private
     */
    adjustSpacer: function(header, width, height, options) {
        if (this.adjusting === false) {
            this.adjusting = true;
            
            var actualWidth = header.el.getWidth(true); // exclude padding/borders
            
            var spacerSize = actualWidth / 2;
            spacerSize -= this.getTotalToolWidth(header);
            spacerSize -= this.item.el.getWidth() / 2;
            
            this.spacer.setWidth(spacerSize); // triggers an additional layout to occur
            
            this.adjusting = false;
        }
    },
    
    /**
     * Retrieves the total computed width of space occupied by Tools.
     * @return Number
     * @private
     */
    getTotalToolWidth: function(header) {
        var total = 0;
        
        header.items.each(function(tool) {
            if (tool instanceof Ext.panel.Tool) {
                // also adding 2px of "fake" padding not accounted for by the width
                // seems to come from either the box layout or the tool
                total += tool.getWidth() + 2;
            }
        });
        
        return total;
    },
    
    /**
     * Destroys this componenet, and the reference to the item.
     */
    destroy: function() {
        this.cmp.un('afterrender', this.addItemToHeader, this);
        this.cmp.header.un('resize', this.adjustSpacer, this);
        
        this.cmp = null;
        this.item = null;
    }
});
//////////////////////
// ..\js\util\BreadCrumbTrail.js
//////////////////////
Ext.ns('RP.util');

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
Ext.define('RP.util.BreadCrumbTrail', {

    mixins: {
        observable: 'Ext.util.Observable'
    },

    constructor: function(config) {
        Ext.apply(this, config);

        this.breadCrumbs = new RP.collections.MixedCollection();

        this.mixins.observable.constructor.call(this, config);

        this.addEvents(        /**
         * @event add
         * Fires after a breadcrumb is added to the trail.
         * @param {RP.util.BreadCrumbTrail} this
         * @param {Object} breadCrumb The breadcrumb that was added.
         */
        'add',
        /**
         * @event clear
         * Fires after the breadcrumb trail has been cleared.
         * @param {RP.util.BreadCrumbTrail} this
         */
        'clear',
        /**
         * @event navigate
         * Fires when an individual breadcrumb is activated.
         * @param {RP.util.BreadCrumbTrail} this
         * @param {Object} breadCrumb The breadcrumb that was activated.
         */
        'navigate',
        /**
         * @event beforenavigate
         * @param {RP.util.BreadCrumbTrail} this The breadcrumb trail that
         * fired the event.
         * @param {Object} target The crumb that is being navigated to.
         * @param {Object[]} trailingCrumbs The crumbs that are after the target
         * crumb in reverse order. Example:
         *   bread > crumb > trail > to > some > form
         *             |     [form, some, to, trail]
         *             |                 |
         *           target        trailing crumbs
         */
        'beforenavigate');
    },

    /**
     * Adds a breadcrumb to the end of the breadcrumb trail.
     * @param {Object} breadCrumb The breadcrumb to add.
     */
    add: function(breadCrumb) {
        this.breadCrumbs.add(breadCrumb);

        breadCrumb.on('activate', function() {
            this.navigate(breadCrumb);
        }, this);

        this.fireEvent('add', this, breadCrumb);
    },

    /**
     * Handler for a breadcrumb's activate event.  Will remove
     * all other breadcrumbs behind the one that is activated.
     *
     * @param {Object} breadCrumb
     * @private
     */
    navigate: function(breadCrumb) {
        var idx = this.breadCrumbs.getCount() - 1,
            limit = this.breadCrumbs.indexOf(breadCrumb),
            toRemove = [];

        // Push the bread crumbs that trail the target crumb
        // into toRemove in reverse order.
        for (; idx > limit; --idx) {
            toRemove.push(this.breadCrumbs.getAt(idx));
        }

        // Fire the before navigate event to allow any listeners to cancel
        // the navigation if they wish.
        if (this.fireEvent('beforenavigate', this, breadCrumb, toRemove) === false) {
            return;
        }

        for (idx = 0, limit = toRemove.length; idx < limit; ++idx) {
            toRemove[idx].un('activate', this.navigate, this);
            this.breadCrumbs.remove(toRemove[idx]);
        }

        this.fireEvent('navigate', this, breadCrumb);
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
            breadCrumb.un('activate', this.navigate, this);
        });

        this.breadCrumbs.clear();
        this.fireEvent('clear');
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
Ext.define("RP.util.CardBreadCrumbTrail", {
    
    extend: "RP.util.BreadCrumbTrail",
    
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
        this.callParent(arguments);
        
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
        this.callParent(arguments);
        
        var card = breadCrumb.getCard();
        
        // remove trailing cards in the layout
        var lastIndex = this.cardContainer.items.length - 1;
        while (this.cardContainer.items.get(lastIndex - 1) != card) {
            lastIndex--;
        }
        
        var index = this.cardContainer.items.indexOf(card);
        this.cardContainer.getLayout().setActiveItem(index);
        
        while(lastIndex < this.cardContainer.items.length) {
            this.cardContainer.remove(this.cardContainer.items.get(lastIndex));
        }
        
        this.renderBreadCrumbs(false);
    },
    
    /**
     * Renders the breadcrumb trail into the container specified
     * by "crumbContainer" config.
     * @private
     */
    renderBreadCrumbs: function(adding) {
        if (!this.crumbContainer.rendered) {
            this.crumbContainer.on("render", Ext.bind(this.renderBreadCrumbs, this, [adding]));
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
// ..\js\util\Component.js
//////////////////////
Ext.define('RP.util.Component', {
    singleton: true,

    /**
     * This utility method takes a component and returns its top-level container.  If the component
     * isn't in a container then null will be returned.
     * @param {Ext.Component} cmp The component
     * @return {Ext.container.Container} The top-most container containing cmp, else null
     */
    getTopLevelContainer: function(cmp) {
        if (!cmp.ownerCt) {
            return null;
        }
        var top = cmp;
        for(;;) {
            if (!top.ownerCt) {
                break;
            } else {
                top = top.ownerCt;
            }
        }
        return top;
    },

    /**
     * Try to get a component reference on from an element.
     * This method will find the closest ancestor element
     * that is a component and return that component. The
     * passed in element tself may be a component. If this
     * is the case, that component will be returned.
     * @param  {Ext.Element} el 
     */
    getComponentFromElement: function(el) {
        var component;
        while (!component && el) {
            component = Ext.getCmp(el.id);
            if (!component) {
                el = el.up('[id]');
            }
        }
        return component;
    }
});
//////////////////////
// ..\js\util\Object.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.Object
 * @singleton
 */
RP.util.Object = (function() {
  return {
	  
	/**
	 * Creates a new object with properties combined. Properties of objects in
	 * the array take precedence over objects that appear before them, i.e., 
	 * if two objects have the same property, then the latter object's property
	 * value is used.  NOTE: All properties copied are shallow (i.e., 
	 * referenced objects are not cloned).
	 */
    mergeProperties: function() {
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

/**
 * @member RP
 * @method mergeProperties
 * Shorthand for {@link RP.util.Object#mergeProperties}
 */
RP.mergeProperties = RP.util.Object.mergeProperties;
//////////////////////
// ..\js\util\Dom.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.DOM
 * @singleton
 */
RP.util.DOM = (function() {
  return {
    /**
     * A workaround for the .outerHTML property. This exists in IE, but not in
     * Firefox. 
     * @param {Object} obj Object for which the outer HTML is needed
     */
    getOuterHTML: function(obj) {
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
     * Returns a custom renderer used to translate colvals into long 
     * descriptions. Using this custom renderer is longhand for 
     * {@link RP.core.CodeTranslator#getLongDesc}.
     * @param {String} namespace Namespace of the data
     * @param {String} colnam Specific name inside the namespace
     * @return {String} Long description
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
 * @class RP.util.Task
 * @singleton
 */
RP.util.Task = (function() {
  var count = 0;
  var PIDs = {};
  
  return {
    
    /**
     * Processes a task. Specifically, calls a function for each element in an
     * array, given a context. Also requires a task name for this series of 
     * calls.
     * 
     * @param {Object[]} array Elements to call the function on
     * @param {Function} fn Function to call using the arguments 
     * (context, array[i], i)
     * @param {Object} context 'this' reference
     * @param {String} taskName Name to assign to this task
     * @param {Object} config Config options which can be set. Note: batchSize,
     * timeout, batchCompleteCB, successCB, and stoppedCB are all overriden.
     */
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
Ext.ns("RP.util");

/**
 * A representation of a Stopwatch. Stopwatches have the ability to start, stop
 * and print. This class maintains a list of all the registered Stopwatches,
 * and new Stopwatches are always registered to it. Thus, Stopwatch is a 
 * pseudo-singleton; it cannot be instantiated directly, but new instances of
 * Stopwatch can be created, and all new ones have a reference maintained in the
 * class.
 * 
 * 
 * Example usage for creating a Stopwatch with the id 'timer' and starting it now.
 * 
 *      RP.util.stopwatch.getWatch('timer', true, 'optional description'); // timer started
 * 
 * Example usage for getting an already existing Stopwatch, and saving the report
 * 
 *      var sw = RP.util.Stopwatch.getWatch('timer'); // just gets the timer since it exists
 *      var html = sw.report(); // gives report for the 1 timer
 * 
 * Example usage for getting a timer, and starting it when desired
 * 
 *      var sw = RP.util.Stopwatch.getWatch('stopped timer'); // creates the timer in a stopped state
 *      sw.start();
 * 
 * Example usage for reporting a stopwatch to a div
 * 
 *      RP.util.Stopwatch.report('someDiv'); // writes a report to that div
 * 
 * @singleton
 */
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
    /**
     * Gets a stopwatch from the already registered ones. If the ID is not
     * already registered, a new one is created.
     * 
     * @param {String} id ID of an already registered stopwatch
     * @param {Boolean} startNow Weather to start the stopwatch while getting
     * it
     * @param {String} desc (Optional) Description of the Stopwatch
     */
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

    /**
     * Clears all of the registered stopwatches, and alerts all of the
     * registered listeners. 
     * 
     * NOTE: Listeners are not cleared.
     */
    clearAll: function()
    {
      _watches = [];
      invokeListeners();
    },

    /**
     * Attempts to report the stopwatch to the specified div class. If that
     * div can not be found, returns a string version of the report.
     * 
     * @param {String} div (optional) The div class to write the report to
     * @return {String} The report, if the div class could not be found
     */
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

    /**
     * Creates a new stopwatch from the ID and description. If a stopwatch 
     * already exists with the same ID, then calls {@link RP#throwError}. 
     * 
     * NOTE: {@link #getWatch} should be used in most cases.
     * 
     * @method StopWatch
     * @param {String} id ID of an already registered stopwatch
     * @param {String} desc (Optional) Description of the Stopwatch
     */
    StopWatch: function(id, desc)
    {
      if (_watches[id])
      {
        RP.throwError(id + " already exists");
      }
      //var events = [];  //events can be used to break out individual times, but it's not needed now.
      var count = 0;
      var total = 0;
      desc = desc || 'N/A';

      var TimedEvent = function()
      {
        var start = new Date();

        return {
          /**
           * @method stop
           * Stops this Stopwatch
           */
          stop: function()
          {
            var stop = new Date();
            this.duration = stop.getTime() - start.getTime();
            invokeListeners();
          }
        };
      };

      this.id = id;
      
      /**
       * @method start
       * Starts this Stopwatch
       */
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
     * 
     * @param {Function} handler Function to add
     */
    addListener: function(handler) {
      listeners.push(handler);
    },
    
    /**
     * Remove a previously added listener
     * 
     * @param {Function} handler Handler function to remove 
     */
    removeListener: function(handler) {
      Ext.Array.remove(listeners, handler);
    }
  };
})();
//////////////////////
// ..\js\util\Grid.js
//////////////////////
Ext.ns("RP.util");

/**
 * The sole method for this class has been marked deprecated. This class should
 * also be considered as such.
 */
RP.util.Grid = (function() {
  return {
    /**
     * @deprecated
     * @method
     * @param {Object} cmodel The column model configuration of the grid.
     * @param {Object} config A grid configuration (Unused).
     * @return {Object} Column model configuration.
     */
    processColumnModel: function(cmodel, config) {
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
 * A singleton class which houses helper methods for various RP processes
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
         * @param {String} url Target page to redirect to
         * @param {Number} (Optional) The number of milliseconds for 
         * the setTimeout call. Uses 0 by default.
         */
        redirect: function(url, millis) {
            Ext.defer(function() {
                try {
                    window.location.assign(url);
                } 
                catch (e) {
                } /* Catching IE onbeforeunload Unspecified error */
            }, millis || 0);
        },
        
        /**
         * Redirects the browser to the login URL.
         */
        redirectToLogin: function() {
            RP.util.Helpers.redirect(RP.globals.getFullyQualifiedPath("LOGIN"));
        },
        
        /**
         * Calls RP.upb.PageBootstrapper.isExternallyAuthenticated
         */
        isExternalAuthentication: function() {
            return RP.upb.PageBootstrapper.isExternallyAuthenticated();
        },
        
        /**
         * Calls RP.upb.PageBootstrapper.isNativeLogin
         */
        isNativeLogin: function() {
            return RP.upb.PageBootstrapper.isNativeLogin();
        },
        
        /**
         * Reloads the current page.
         * @param {Number} (Optional) The number of milliseconds for the setTimeout call.
         */
        reload: function(millis) {
            Ext.defer(function() {
                window.location.reload();
            }, millis || 0);
        },
        
        /**
         * Marks the current session for requiring re-authentication.
         */
        markSessionForReAuthentication: function() {
            Ext.Ajax.request({
                url: RP.globals.getFullyQualifiedPath('REAUTHENTICATE')
            });
        },
        
        /**
         * Keep the current session alive by making a heart beat Ajax request
         * to the server.
         * @param {Number} (Optional) lastActivityMillis Milliseconds since 
         * the last activity the browser detected, defaults to now.
         */
        keepSessionAlive: function(lastActivityMillis) {
            Ext.Ajax.request({
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
         * Checks for the last activity of the user in the browser as saved by
         * the server
         * @param {Function} successFn callback function on success
         * @param {Function} failureFn callback function on failure
         * @param {Object} scope scope of the callbacks
         */
        isUserActive: function(successFn, failureFn, scope) {
            Ext.Ajax.request({
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
         * @param {String} url The url the window should be set to.
         * @param {Object} name The name of the new window.
         * @param {Object} options Additional options for opening the window.
         * @return {Object} Reference to the newly opened window
         */
        openWindow: function(url, name, options) {
            logger.logInfo("[Helpers] Opening window to: " + url);
            
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
Ext.ns("RP.util");

/**
 * A singleton class used to load CSS files
 * 
 * @singleton
 */
RP.util.CSSLoader = (function() {
    var downloaded = [];
    
    /**
     * Includes a CSS file into the current document
     * 
     * @param {String} url URL of the stylesheet
     */
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
         * @param {String/String[]} urls URL(s) of css files to be downloaded
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
// ..\js\util\PageInactivityChecker.js
//////////////////////
Ext.ns("RP.util");

/**
 * A singleton to check for web page inactivity and calls handler(s) when an inactivity timeout occurs.
 * 
 * @class RP.util.PageInactivityChecker
 * @singleton
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
    var activityEventNames = ['keydown','mouseover','mousemove'];
    
    Ext.onReady(function(){
        // When the window becomes active again we set the inactive flag back to false.
        RP.event.AppEventManager.register(RP.upb.AppEvents.ActiveAgain, function() {
            logger.logInfo("[PageInactivityChecker] Setting inactive flag to false.");
            
            inactive = false;
        }); 
        
        RP.event.AppEventManager.register(RP.upb.AppEvents.Inactive, function() {
            logger.logInfo("[PageInactivityChecker] Setting inactive flag to true.");
            
            inactive = true;
        }); 
    });
    
    var resetLastActivityTime = function() {
        lastActivityTime = new Date();
    };
    
    var resetLastSessionKeepAlive = function() {
        lastSessionKeepAlive = new Date();
    };
    
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
        var result = Ext.JSON.decode(response.responseText, true);
        if (result === null) {
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
         * This method will set the interval millis for the session keep alive.
         * 
         * @param {Number} intervalMillis The interval millis.
         * @private
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
         * @param {Function} h Handler function to be added
         */
        addHandler: function(h) {
            logger.logTrace("[PageInactivityChecker] Adding page inactivity handler.");
            handlers.push(h);
        },
        
        /**
         * Retrieve the number of seconds of the interval to time out
         * the client side user session.
         * @return {Number} Number of seconds until the user is considered inactive
         */
        getTimeOutInSeconds: function() {
            return timeOutSecs;
        },
        
        /**
         * Removes a previously added handler for inactivity timeout.
         * @param {Function} h Handler to be removed
         */
        removeHandler: function(h) {
            logger.logTrace("[PageInactivityChecker] Removing page inactivity handler.");
            Ext.Array.remove(handlers, h);
        },
        
        /**
         * Start checking for page inactivity.
         * @param {Number} timeOutInSecs Number of seconds that users can wait
         * before becoming inactive
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
// ..\js\util\ConstructedMixin.js
//////////////////////
/**
 * This class is intended to be extended to create a mixin that latches onto the constructor of the
 * class it is being mixed into.  This prevents situations where you have to manually construct or
 * initialize mixin properties such as with the Observable mixin.  If mixed into a plugin, this will
 * not happen and the mixin must be constructed manually.
 * 
 * @author Jeff Gitter
 */
Ext.define('RP.util.ConstructedMixin', {
    onClassMixedIn: function(cls) {
        if (!(cls.prototype instanceof Ext.AbstractPlugin)) {
            cls.prototype.constructor = (cls.prototype.constructor ?
                Ext.Function.createSequence(cls.prototype.constructor, this.prototype.constructor) : this.prototype.constructor);
        }
    }
});
//////////////////////
// ..\js\core\ClassOperations.js
//////////////////////
Ext.ns("RP.core");

/**
 * @class RP.core.ClassOperations
 * Provides utilities to support the abstract class pattern in JavaScript.
 * @singleton
 */
RP.core.ClassOperations = {

    /**
     * Used to define an abstract method inside an abstract class.  This
     * is similar to {@link Ext#emptyFn}, in that it defines a simple place holder
     * function, but this will throw an exception if called directly. This is
     * a dummy function.
     */
    abstractFn: function() {
        RP.throwError("RP.abstractFn called directly.");
    }
};

/**
 * @member RP
 * @method abstractFn
 * Shorthand for {@link RP.core.ClassOperations#abstractFn}.
 */
RP.abstractFn = RP.core.ClassOperations.abstractFn;
//////////////////////
// ..\js\core\CodeTranslator.js
//////////////////////
Ext.ns("RP.core");

/**
 * @class RP.core.CodeTranslator
 * @singleton
 *
 * Singleton for loading/accessing code translations, which maintains an 
 * internal {@link Ext.data.Model}. 
 */
RP.core.CodeTranslator = function(){

  // default model used by global stores created
  Ext.define("RP.core.CodeTranslation", {
      extend: "Ext.data.Model",
      fields: [
        { name: 'colnam' },
        { name: 'colval' },
        { name: 'lngdsc' },
        { name: 'short_dsc' }
      ]
  });
  
  return {
  
    /**
     * Performs a lookup on the specified global store and returns the
     * matching row's field, otherwise value. Namespace and colnam are used
     * with {@link Ext.StoreManager#get}. Value is used with 
     * {@link Ext.data.Store#findExact}. Field is the field inside the Store,
     * using the index determined by value.
     * @param {String} namespace Namespace of the data. 
     * @param {String} colnam Specific name inside the namespace
     * @param {String} value The value to match the field against
     * @param {String} field Field sought.
     * @return {Object} The value
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
     * @param {String} namespace Namespace of the data.
     * @param {Object[]} data Array of objects to add to the namespace
     */
    loadData: function(namespace, data){
      Ext.each(data, function(item){
        var storeId = namespace + "." + item.colnam;
        var store = Ext.StoreMgr.get(storeId);
        
        // check if the global store exists
        if (typeof store === "undefined") {
          store = new Ext.data.Store({
            storeId: storeId,
            model: "RP.core.CodeTranslation",
            reader: "json"
          });
        }
        
        store.loadData([item], true);
      });
    },
    
    // TODO use ScriptLoader
    loadCodePack: function(url) {
      document.write('<script type="text/javascript" src="' + url + '"></script>');
    },
    
    /**
     * Gets the long description for a value. See {@link #getRowValue}
     * @param {String} namespace Namespace of the data. 
     * @param {String} colnam Specific name inside the namespace
     * @param {String} value The value to match the field against
     * @return {String} Long description
     */
    getLongDesc: function(namespace, colnam, value) {
      return this.getRowValue(namespace, colnam, value, "lngdsc");
    },
    
    /**
     * Gets the short description for a value. See {@link #getRowValue}
     * @param {String} namespace Namespace of the data. 
     * @param {String} colnam Specific name inside the namespace
     * @param {String} value The value to match the field against
     * @return {String} Short description
     */
    getShortDesc: function(namespace, colnam, value) {
      return this.getRowValue(namespace, colnam, value, "short_desc");
    },
    /**
     * Gets the store associated with a namespace and column.
     * See {@link Ext.data.StoreManager#get}
     * @param {String} namespace Namespace of the data. 
     * @param {String} colnam Specific name inside the namespace
     * @return {Object} The item if it is found
     */
    getStore: function(namespace, colnam) {
      return Ext.StoreMgr.get(namespace + "." + colnam);
    }
  };
}();
//////////////////////
// ..\js\core\Sundial.js
//////////////////////
/**
 * @class RP.core.Sundial
 * A simple class which more or less emulates a Sundial; It maintains an offset
 * time measurement. This class is not graphical, and does not extend
 * {@link Ext.Component}. 
 */
RP.core.Sundial = {
  _offset: 0,
  
  /**
   * Gets the current time according to this Sundial
   * @return {Date} The current date and time
   */
  now: function() {
    return new Date(new Date().getTime() + this._offset);
  },
  
  /**
   * Sets the offset for this Sundial. Offsets cannot exceed 24hrs. 
   * @param {Date} offsetDate Date to pull the time from, to offset this time
   */
  setServerTime: function(offsetDate) {
    this._offset = offsetDate.getTime() - (new Date()).getTime();
  },
  
  /**
   * Returns whether the Sundial has an offset
   * @return {Boolean} True if the Sundial has an offset
   */
  hasOffset: function() {
    return (this._offset !== 0);
  }
};
//////////////////////
// ..\js\core\Interface.js
//////////////////////
Ext.ns("RP.core");

/**
 * @class RP.core.Interface
 * @singleton
 *
 * Javascript doesn't have interfaces like OOP languages, so this is one way
 * of implementing such a feature. The inspiration for this came from
 * <a href="http://knol.google.com/k/glen-ford/programming-to-the-interface-in/27lm3zg1hrg7v/7#">here</a>.
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
                    RP.throwError("Member '" + member + "' not implemented or implemented incorrectly in interface '" + theInterface._name + "'");
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
                    i[member] = Ext.bind(obj[omember], obj);
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
         * Registers an object as implementing a certain interface. Exception
         * is thrown if object does not fully implement the interface
         * @param {Object} obj The object
         * @param {Object} theInterface The interface it implements
         * @param {Object} map (Optional) Map for interface member name to
         * actual object member name (used for multiple interfaces member name collisions)
         */
        implement: function(classRef, theInterface, map) {
            if (!theInterface._name) {
                RP.throwError("Missing _name member in interface definition.");
            }
            
            if ((typeof classRef.prototype.__rpInterfaces !== "undefined") &&
            (typeof classRef.prototype.__rpInterfaces[theInterface._name] !== "undefined")) {
                RP.throwError(theInterface._name + " has already been implemented for class " + classRef.toString());
            }
            
            if (!checkInterface(classRef.prototype, theInterface, map)) {
                RP.throwError("Interface not implemented.");
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
         * Gets a certain interface that an object implements.
         * @param {Object} obj The object
         * @param {Object} theInterface The interface definition object of the
         * interface you are expecting
         * @return {Object} interface with scoping set to the object, or null
         * if the object does not implement said interface
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

/**
 * @member RP
 * @method iimplement
 * Shorthand for {@link RP.core.Interface#implement}
 */
RP.iimplement = RP.core.Interface.implement;

/**
 * @member RP
 * @method iget
 * Shorthand for {@link RP.core.Interface#get}
 */
RP.iget = RP.core.Interface.get;

//////////////////////
// ..\js\core\IntervalJN.js
//////////////////////
Ext.ns("RP.core");

/**
 * A static class which manipulates time interval (IntervalJN) objects. These
 * objects contain the following keys:
 * 
 * <ul><li><b>End</b>: {Date} The end time.</li>
 * <li><b>Start</b>: {Date} The start time.</li></ul>
 * 
 * @class RP.core.IntervalJN
 * @singleton
 */
RP.core.IntervalJN = {
    /*  To improve performance, we'll assume the caller is passing in valid intervals...  We'll
     let our unit tests track down bugs...
     _isValidInterval: function(arg, errorMsg) {
     if (!this._isStartValidObject(arg)) {
     errorMsg += "Object not an interval - ";
     RP.throwError(Ext.String.format(errorMsg + "Start object not a Date object"));
     }
     if (!this._isEndValidObject(arg)) {
     errorMsg += "Object not an interval - ";
     RP.throwError(Ext.String.format(errorMsg + "End object not a Date object"));
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
     RP.throwError(Ext.String.format("{0} is Null or undefined", arg));
     }
     if (arg instanceof Date) {
     return true;
     }
     if (this._isNullOrUndefined(throwErrorMsg)) {
     return false;
     }
     else {
     RP.throwError(Ext.String.format("{0} {1} not a Date object", throwErrorMsg, arg));
     }
     },
     */
    
    /**
     * Reports the duration of an object by directly calling
     * {@link RP.Date#deltaT}.
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The date to subtract from.</li>
     * <li><b>Start</b>: {Date} The date to subtract.</li></ul>
     * @return {RP.TimeSpan} The time span between dates
     */
    getDuration: function(intervalJN) {
        return RP.Date.deltaT(intervalJN.End, intervalJN.Start);
    },
    
    /**
     * Reports the center of an object containing time keys
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Date} A Date object which is between the Start and End
     */
    getCenter: function(intervalJN) {
        return RP.Date.addMilliseconds(intervalJN.Start, (RP.Date.deltaT(intervalJN.End, intervalJN.Start).totalMilliseconds() / 2));
    },
    
    /**
     * Reports whether the time point is between the Start and End of an
     * Date-containing Object.
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Date} timePoint Point in time to check for
     * @return {Boolean} True if timePoint exists between the Start and End
     */
    containsTime: function(intervalJN, timePoint) {
        //this._isValidInterval(intervalJN, "Invalid 1st Argument: ");
        //this._isDateObject(timePoint, "Invalid 2nd Argument: ");
        
        return (timePoint.getTime() >= intervalJN.Start.getTime()) &&
        (timePoint.getTime() <= intervalJN.End.getTime());
    },
    
    /**
     * Reports whether one time interval exists completely inside another. 
     * 
     * @param {Object} outerIntervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} innerIntervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Boolean} True if the time interval in innerIntervalJN is also
     * inside outerIntervalJN. Thus, 
     *     
     *     outerIntervalJN >= innerIntervalJN2
     */
    containsInterval: function(outerIntervalJN, innerIntervalJN2) {
        return (RP.core.IntervalJN.containsTime(outerIntervalJN, innerIntervalJN2.Start)) &&
        (RP.core.IntervalJN.containsTime(outerIntervalJN, innerIntervalJN2.End));
    },
    
    /**
     * Reports whether two objects "overlap" each other. Overlap is defined as:
     * 
     *      1. intervalJN1 starts before intervalJN2 ends
     *      2. intervalJN1 ends after invervalJN2 starts
     *      
     * Therefore, {@link #containsInterval} and isOverlapping will never be
     * true for the same time 
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Boolean} True if the two overlap
     */
    isOverlapping: function(intervalJN1, intervalJN2) {
        return (RP.Date.isBefore(intervalJN1.Start, intervalJN2.End)) && (RP.Date.isAfter(intervalJN1.End, intervalJN2.Start));
    },
    
    /**
     * Reports whether two IntervalJN objects are equivalent.
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Boolean} True if the two objects are equivalent
     */
    isCoincident: function(intervalJN1, intervalJN2) {
        return (RP.Date.equals(intervalJN1.Start, intervalJN2.Start) && RP.Date.equals(intervalJN1.End, intervalJN2.End));
    },
    
    /**
     * Reports whether the addition of the two intervals would result in a 
     * continuous interval of time. Specifically, tells whether either of these
     * conditions are met:
     *      
     *      1. intervalJN1 starts when intervalJN2 ends
     *      OR
     *      2. intervalJN1 ends when intervalJN2 starts
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Boolean} True if these two intervals make up a continuous unit
     */
    isContinuous: function(intervalJN1, intervalJN2) {
        return (RP.Date.equals(intervalJN1.Start, intervalJN2.End) || RP.Date.equals(intervalJN1.End, intervalJN2.Start));
    },
    
    /**
     * Reports whether this interval is a point in time. Specifically, if the
     * start and end time are equivalent. This is done by calling
     * {@link RP.Date#equals}.
     * 
     * @param {Object} interval An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Boolean} True if the start and end time match
     */
    isPoint: function(interval) {
        return RP.Date.equals(interval.Start, interval.End);
    },
    
    /**
     * Shifts a time interval a specified number of minutes
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Number} minutesStart Number of minutes to add to the start time
     * @param {Number} minutesEnd (optional) Number of minutes to add to the end time
     */
    shiftByMinutes: function(intervalJN, minutesStart, minutesEnd) {
        intervalJN.Start = RP.Date.addMinutes(intervalJN.Start, minutesStart);
        
        if ((typeof minutesEnd === "undefined") || (minutesEnd === null)) {
            intervalJN.End = RP.Date.addMinutes(intervalJN.End, minutesStart);
        }
        else {
            intervalJN.End = RP.Date.addMinutes(intervalJN.End, minutesEnd);
        }
    },
    
    /**
     * Reports whether the intervalJN object can be scaled by minutes.
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Number} durationDiffMinutes Offset number of minutes
     * @return {Boolean} True if it is valid to call {@link #scaleByMinutes}
     */
    canScaleByMinutes: function(intervalJN, durationDiffMinutes) {
        var origDuration = RP.core.IntervalJN.getDuration(intervalJN).totalMinutes();
        
        return (origDuration + durationDiffMinutes > 0);
    },
    
    /**
     * Scales an intervalJN object by the specified number of minutes.
     * The object's End is extended by the specified amount. See also
     * {@link #shiftByMinutes}.
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Number} durationDiffMinutes Number of minutes to extend
     */
    scaleByMinutes: function(intervalJN, durationDiffMinutes) {
        if (!RP.core.IntervalJN.canScaleByMinutes(intervalJN, durationDiffMinutes)) {
            RP.throwError("Invalid duration");
        }
        
        RP.core.IntervalJN.shiftByMinutes(intervalJN, 0, durationDiffMinutes);
    },
    
    /**
     * Returns a clone of the passed-in IntervalJN.
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Object} An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     */
    cloneInterval: function(intervalJN) {
        return {
            Start: RP.Date.clone(intervalJN.Start),
            End: RP.Date.clone(intervalJN.End)
        };
    },
    
    /**
     * Returns a String representation of the intervalJN object
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} formatObj (optional) Object to format the Dates inside
     * intervalJN
     * @return {String} String representation of the interval
     */
    formatInterval: function(interval, formatObj) {
        if (interval === null || interval.Start === null || interval.End === null) {
            return "";
        }
        
        if (!formatObj) {
            formatObj = RP.core.Formats.Time.Default;
        }
        
        return Ext.String.format("{0} - {1}", RP.Date.formatDate(interval.Start, formatObj), RP.Date.formatDate(interval.End, formatObj));
    },
    
    /**
     * Returns a new Date object with a specific time, which occurs
     * strictly after a specified Date object.
     * 
     * If the Date object contains the time 3:00PM, and the passed in strTime
     * is 2:00PM, then the returned object will be 23-hours after the passed in 
     * Date. Note that if 2:00 were the time, the new Date object would contain
     * 2:00AM, the following morning.
     * 
     * @param {String} strTime String representation of the time.
     * @param {Date} baseDate Date object containing the correct date, and a
     * time indicative of the minimum acceptable value.
     * @return {Date}
     */
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
    
    /**
     * Parses a string representation of the time and a Date object into
     * another date object, containing the correct time.
     * 
     * @param {String} strTime A string representation of the time. Must
     * contain a ':'
     * @param {Date} datePart A date object containing the correct date
     * @param {Boolean} accept2400 Whether to allow '24:00' as a valid time. If
     * '24:00' is passed in, and this is not specified true, an exception is
     * thrown.
     * @return {Date} A newly created Date object
     */
    parseTime: function(strTime, datePart, accept2400) {
        var nextDay = false; // detect 24:00 as next day
        // Do not allow minutes if 24:xx
        var ind = strTime.indexOf(":");
        
        var hhmm = [];
        
        if (ind !== -1) {
            hhmm = strTime.split(":");
            if (hhmm.length > 1) {
                if (hhmm[0] === "24" && (parseInt(hhmm[1], 10) !== 0)) {
                    RP.throwError("Invalid time text");
                }
            }
            
            // Do not allow anything over hour 24
            if (parseInt(hhmm[0], 10) > 24) {
                RP.throwError("Invalid time text");
            }
            
            // Convert 24:00 to 0:00 and set it to next calendar day
            if (hhmm[0] === "24") {
                if (accept2400 === true) {
                    nextDay = true;
                    strTime = "0:00";
                }
                else {
                    RP.throwError("24:00 cannot be used as the time");
                }
            }
        }
        
        var retDate = RP.core.Format.parseTime(strTime);
        if (datePart instanceof Date) {
            var newDate = RP.Date.clone(datePart);
            newDate.setHours(retDate.getHours(), retDate.getMinutes(), retDate.getSeconds());
            newDate.apExists = retDate.apExists;
            retDate = newDate;
        }
        
        if (nextDay) {
            retDate = RP.Date.addDays(retDate, 1);
        }
        return retDate;
    },
    
    /**
     * Reports whether the string has an "a" or "p". Not case-sensitive.
     * 
     * @param {String} timeStr String representation of the time
     * @return {String} "a" if AM, or "p" if PM. Null if neither exist.
     * @private
     */
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
    
    /**
     * Validates the passed in time against 24hr-format time.
     * 
     * @param {String} amPmSpec "a" or "p" to specify AM or PM
     * @param {Date} time Date containing a time in 24hr format 
     * @return {Boolean} True if AM/PM are properly set for the time
     * @private
     */
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

    /**
     * Creates a new IntervalJN by specifying the interval of time, and the
     * date.
     * 
     * @param {String} strInterval A string of the form 'X:XX-YY:YY' 
     * @param {Date} datePart Date for the interval
     * @return {Object} an IntervalJN object
     */
    parseTimeInterval: function(strInterval, datePart) {
        var intervalText = String(strInterval);
        var times = intervalText.split("-");
        
        if (times.length !== 2 ||
        Ext.String.trim(times[0]).length === 0 ||
        Ext.String.trim(times[1]).length === 0) {
            RP.throwError("Invalid interval text specified");
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
                retInterval.End = RP.Date.addHours(retInterval.End, 12);
            }
            
            this.adjustForDST(retInterval.End, expectedHours);
        }
        else {
            RP.throwError("Invalid interval text specified");
        }
        
        return retInterval;
    },
    
    /**
     * Creates a new IntervalJN by specifying the interval of time, and the 
     * date. If the date contains 0 hours, the returned date is set forward one
     * day.
     * 
     * @param {String} strInterval A string of the form 'X:XX-YY:YY' 
     * @param {Date} datePart Date for the interval
     * @return {Object} An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     */
    parseBusinessDayTimeInterval: function(strInterval, dateTimeStart) {
        var retInterval = RP.core.IntervalJN.parseTimeInterval(strInterval, dateTimeStart);
        
        if (retInterval.Start < dateTimeStart && dateTimeStart.getHours() === 0) {
            retInterval.Start = RP.Date.addDays(retInterval.Start, 1);
            retInterval.End = RP.Date.addDays(retInterval.End, 1);
        }
        return retInterval;
    },
    
    /**
     * Adjusts an IntervalJN object
     * 
     * @param {Object} interval An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Number} offsetMinutes Number of minutes to shift by
     * @param {Number} offsetDuration Number of minutes to extend the duration
     */
    adjustInterval: function(interval, offsetMinutes, offsetDuration) {
        if (RP.core.IntervalJN.isValidOffset(offsetMinutes)) {
            RP.core.IntervalJN.shiftByMinutes(interval, offsetMinutes);
        }
        if (RP.core.IntervalJN.isValidOffset(offsetDuration)) {
            RP.core.IntervalJN.scaleByMinutes(interval, offsetDuration);
        }
    },
    
    /**
     * Reports whether the passed in String can be parsed to an integer
     * 
     * @param {String} number a String representation of a number
     * @return {Boolean} True if number can be parsed to an int
     */
    isValidOffset: function(number) {
        return ((typeof number !== "undefined") &&
        (number !== null) &&
        !isNaN(parseInt(number, 10)));
    },
    
    /**
     * Returns the different in time between the first date and second
     * 
     * @param {Date} dt1 Time to subtract from
     * @param {Date} dt2 Time to subtract
     * @return {Number} Difference between the times
     */
    compareDate: function(dt1, dt2) {
        return dt1.getTime() - dt2.getTime();
    },
    
    /**
     * Compares the start times of two time intervals
     * 
     * @param {Object} interval1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} interval2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Number} The difference from start1 - start2
     */
    compareStart: function(interval1, interval2) {
        return (interval1.Start.getTime() - interval2.Start.getTime());
    },
    
    /**
     * Compares the end times of two time intervals
     * 
     * @param {Object} interval1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} interval2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Number} The difference from end1 -end2
     */
    compareEnd: function(interval1, interval2) {
        return (interval1.End.getTime() - interval2.End.getTime());
    },
    
    /**
     * Compares the median times of two intervals. Directly calls
     * {@link #getCenter}, so the return type is {@link Date}
     * 
     * @param {Object} interval1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} interval2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Date} The difference from middle1 - middle2
     */
    compareCenter: function(interval1, interval2) {
        return (RP.core.IntervalJN.getCenter(interval1) - RP.core.IntervalJN.getCenter(interval2));
    },
    
    /**
     * Compares the duration of two time intervals
     * 
     * @param {Object} interval1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} interval2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Number} The difference from duration1 - duration2 in milliseconds
     */
    compareDuration: function(interval1, interval2) {
        return (RP.core.IntervalJN.getDuration(interval1).getTotalMilliseconds() - RP.core.IntervalJN.getDuration(interval2).getTotalMilliseconds());
    },
    
    /**
     * Sets the parent reference for an interval.
     * 
     * NOTE: Parental status is not preserved across cloning.
     * 
     * @param {Object} interval An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} parentSequence Parent object
     */
    setParent: function(interval, parentSequence) {
        interval.__parent = parentSequence;
    },
    
    /**
     * Getter for the parent reference for this interval.
     * 
     * NOTE: If this object was cloned from an interval that had a parent, the
     * parent reference no longer exists.
     * 
     * @param {Object} interval An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Object} Parent object
     */
    getParent: function(interval) {
        return interval.__parent;
    },
    
    /**
     * Sets the rule reference for an interval
     * 
     * @param {Object} interval An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} iRule Rule object
     */
    setRule: function(interval, iRule) {
        interval.__rule = iRule;
    },
    
    /**
     * Getter for the rule reference for this interval.
     * 
     * @param {Object} interval An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Object} Rule object
     */
    getRule: function(interval) {
        return interval.__rule;
    },
    
    /**
     * Corrects endDateTime to properly match expectedHours. Either can be in
     * 24-hr format, and they do not have to match. If endDateTime, adjusted,
     * is less than the expected time, then an hour is added. Else, an hour is
     * subtracted. 
     * 
     * NOTE: If endDateTime MATCHES expectedHours, then endDateTime will be
     * reduced by 1 hour.
     * 
     * NOTE: As JavaScript is pass-by-value, the current implementation of this
     * function actually does <b>nothing</b>. {@link RP.Date#addHours} returns a
     * <i>copy</i> of the date. Thus, the following statement has no
     * repurcussions outside of this function scope:
     * 
     *      endDateTime = RP.Date.addHours(...);
     * 
     * @param {Date} endDateTime Date object containing the time to adjust. 
     * Either 12hr or 24hr format.
     * @param {Number} expectedHours Expected number of hours. Either 12hr or
     * 24hr format.
     */
    adjustForDST: function(endDateTime, expectedHours) {
        //Normalized for 24-hour issues
        var endHours = endDateTime.getHours() >= 12 ? endDateTime.getHours() - 12 : endDateTime.getHours();
        expectedHours = expectedHours >= 12 ? expectedHours - 12 : expectedHours;
        
        // Compensate for DST
        if (endHours !== expectedHours) {
            if (endHours < expectedHours) {
                endDateTime = RP.Date.addHours(endDateTime, 1);
            }
            else {
                endDateTime = RP.Date.addHours(endDateTime, -1);
            }
        }
    }
};
//////////////////////
// ..\js\core\SequenceJN.js
//////////////////////
Ext.ns("RP.core");

RP.core.SequenceJN = {
    addInterval: function(sequence, interval, iRule) {
        // return sequence contains the chop up of the interval that was applied
        // to the sequence
        // this includes all the splits that were done but no merges with
        // existing sequence data
        var retSeq = []; // TOOD: do we really need to return retSeq out of
        // this method?
        
        // trap illegal add to a bounded sequence
        var isDisplacmentAdd = false;
        
        if (sequence.Start && sequence.End) {
            isDisplacmentAdd = !RP.core.IntervalJN.containsInterval(sequence, interval);
        }
        
        if (sequence.__isBounded && isDisplacmentAdd) {
            RP.throwError("interval is out of bounds for the bounded sequence");
        }
        
        // negotiate parent displacement
        if (RP.core.IntervalJN.getParent(sequence) && isDisplacmentAdd) {
            RP.core.SequenceJN.removeInterval(RP.core.IntervalJN.getParent(sequence), sequence);
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
                    splitPair = this._splitSetParentAndRule(curRule, curInterval, interval.End, sequence);
                    
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
                    
                    splitPair = this._splitSetParentAndRule(iRule, interval, curInterval.Start, sequence);
                    
                    // direct insert
                    RP.core.SequenceJN._insertIntoSequence(sequence, splitPair.first, index);
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
                    
                    splitPair = this._setParentAndRule(splitPair, sequence, curRule);
                    
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
                    
                    if (index < sequence.length - 1 &&
                    RP.core.IntervalJN.isOverlapping(interval, sequence[index + 1])) {
                        curInterval = sequence[index + 1];
                        curRule = RP.core.IntervalJN.getRule(curInterval);
                        
                        splitPair = this._splitSetParentAndRule(curRule, curInterval, interval.End, sequence);
                        
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
                    splitPair = this._splitSetParentAndRule(iRule, interval, curInterval.End, sequence);
                    
                    retSeq.splice(index, 0, splitPair.first);
                    
                    splitPair.first = curRule.merge(curInterval, splitPair.first);
                    
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
                splitPair = this._splitSetParentAndRule(curRule, curInterval, interval.Start, sequence);
                
                sequence[index] = splitPair.first;
                sequence.splice(index + 1, 0, splitPair.last);
            }
        }
        
        // update sequence Interval properties
        RP.core.SequenceJN.updateSequenceProperties(sequence);
        
        // put us back into parent if we were taken out
        if (RP.core.IntervalJN.getParent(sequence) && isDisplacmentAdd) {
            RP.core.SequenceJN.addInterval(RP.core.IntervalJN.getParent(sequence), sequence, RP.core.IntervalJN.getRule(sequence));
        }
        
        return retSeq;
    },
    getNextIntervalInSequence: function(sequence, currIndex) {
        var newIndex = currIndex + 1;
        if (sequence.length <= newIndex) {
            var currInterval = sequence[currIndex];
            return {
                Start: currInterval.End,
                End: currInterval.End
            };
        }
        return sequence[newIndex];
    },
    getPrevIntervalInSequence: function(sequence, currIndex) {
        var newIndex = currIndex - 1;
        if (newIndex < 0) {
            var currInterval = sequence[currIndex];
            return {
                Start: currInterval.Start,
                End: currInterval.Start
            };
        }
        return sequence[newIndex];
    },
    getSubSequence: function(sequence, interval, retSequence) {
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
                splitPair = RP.core.IntervalJN.getRule(curInterval).split(curInterval, interval.Start);
                curInterval = splitPair.last;
            }
            
            // check for end range split
            if (RP.core.IntervalJN.containsTime(curInterval, interval.End) && interval.End < curInterval.End) {
                splitPair = RP.core.IntervalJN.getRule(curInterval).split(curInterval, interval.End);
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
    _insertIntoSequence: function(sequence, interval, index) {
        var sRule = RP.core.IntervalJN.getRule(sequence);
        if (sRule && sRule.insert) {
            sRule.insert(sequence, interval, index);
        }
        else {
            sequence.splice(index, 0, interval);
        }
    },
    _isUnboundedSequence: function(interval) {
        return interval.__isSequence && !interval.__isBounded;
    },
    _clone: function(interval) {
        var rule = RP.core.IntervalJN.getRule(interval);
        var ret = rule.clone(interval);
        // Copy Interval stuff
        RP.core.IntervalJN.setRule(ret, rule);
        if (RP.core.IntervalJN.getParent(interval) !== null) {
            RP.core.IntervalJN.setParent(ret, RP.core.IntervalJN.getParent(interval));
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
    _splitSetParentAndRule: function(rule, interval, date, sequence) {
        var splitPair = rule.split(interval, date);
        
        return this._setParentAndRule(splitPair, sequence, rule);
    },
    _setParentAndRule: function(splitPair, sequence, rule) {
        RP.core.IntervalJN.setParent(splitPair.first, sequence);
        RP.core.IntervalJN.setRule(splitPair.first, rule);
        RP.core.IntervalJN.setParent(splitPair.last, sequence);
        RP.core.IntervalJN.setRule(splitPair.last, rule);
        return splitPair;
    },
    removeInterval: function(sequence, interval) {
        Ext.Array.remove(sequence, interval);
        RP.core.SequenceJN.updateSequenceProperties(sequence);
        
    },
    updateSequenceProperties: function(sequence) {
        if (sequence.__isBounded) {
            return;
        }
        if (sequence.length === 0) {
            sequence.Start = null;
            sequence.End = null;
        }
        else {
            sequence.Start = RP.Date.clone(sequence[0].Start);
            sequence.End = RP.Date.clone(sequence[sequence.length - 1].End);
        }
    },
    getStartIndex: function(sequence, interval) {
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
    binarySearch: function(sequence, interval) {
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
    binarySearchForSequenceThatContainsTime: function(sequence, time) {
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
    coalesce: function(sequence) {
    
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
                RP.core.IntervalJN.setParent(joinedInterval, RP.core.IntervalJN.getParent(curInterval));
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
    fillSparceInterval: function(sequence) {
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
    setBounds: function(sequence, interval) {
        sequence.__isSequence = true;
        sequence.__isBounded = true;
        
        sequence.Start = interval.Start;
        sequence.End = interval.End;
        
    },
    formatSequence: function(sequence) {
        if (!sequence.__isSequence) {
            return "Not a Sequence";
        }
        
        if (sequence.length === 0) {
            return "Empty Sequence";
        }
        
        var retText = [];
        retText.push(Ext.String.format("Start Date: {0}", RP.Date.formatDate(sequence.Start, RP.core.Formats.Date.Medium)));
        retText.push(Ext.String.format("End Date: {0}", RP.Date.formatDate(sequence.End, RP.core.Formats.Date.Medium)));
        
        for (var i = 0; i < sequence.length; i++) {
            retText.push(Ext.String.format("[{0}] {1}", i, RP.core.IntervalJN.formatInterval(sequence[i])));
        }
        
        return retText.join('\r\n');
    }
};
//////////////////////
// ..\js\core\IntervalRule.js
//////////////////////
Ext.ns("RP.core");

/**
 * Operations which can also be performed on {@link RP.core.IntervalJN} objects.
 * 
 * NOTE: This is an interface class. No methods are implemented.
 * 
 * @class RP.core.IntervalRule
 */
RP.core.IntervalRule = {

    /**
     * Reports whether it is valid to split an intervalJN at the split Date
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The date to subtract from.</li>
     * <li><b>Start</b>: {Date} The date to subtract.</li></ul>
     * @param {Date} splitAt Date which is between the time interval
     * @return {Boolean} True if it is valid to call {@link #split} 
     */
    canSplit : function(intervalJN, splitAt) {
        
    },
    
    /**
     * Reports whether it is valid to join two time intervals.
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Boolean} Whether it is valid to call {@link #join}
     */
    canJoin : function(intervalJN1, intervalJN2) {

    },
    
    /**
     * Reports whether it is valid to merge two time intervals.
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Object} Whether it is valid to call {@link #merge}
     */
    canMerge : function(intervalJN1, intervalJN2) {

    },
    
    /**
     * Splits an intervalJN at the split Date
     * 
     * @param {Object} intervalJN An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The date to subtract from.</li>
     * <li><b>Start</b>: {Date} The date to subtract.</li></ul>
     * @param {Date} splitAt Date which is between the time interval
     * @return {Object[]} Two IntervalJNs, such that the first one spans the 
     * original start time to the split, and the second spans the split to the
     * original ending time.
     */
    split : function(intervalJN, splitAt) {

    },
    
    /**
     * Joins two time intervals
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Object} An object which is the joined product of intervalJN1
     * and intervalJN2
     */
    join : function(intervalJN1, intervalJN2) {

    },
    
    /**
     * Merges two time intervals
     * 
     * @param {Object} intervalJN1 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @param {Object} intervalJN2 An object containing the following keys:<ul>
     * <li><b>End</b>: {Date} The end time.</li>
     * <li><b>Start</b>: {Date} The start time.</li></ul>
     * @return {Object} An object which is the merged product of intervalJN1
     * and intervalJN2
     */
    merge : function(intervalJN1, intervalJN2) {

    }
};
//////////////////////
// ..\js\core\ComponentMgr.js
//////////////////////
Ext.ns("RP.core");

/**
 * @class RP.core.ComponentMgr
 * @singleton
 * Extends Ext.ComponentMgr to register xtypes, with added features such as parameter handling
 */
RP.core.ComponentMgr = function() {
    // This holds the in/out parameters of a component
    var rpTypeParams = {};
    
    return {
        /**
         * Registers a new xtype
         * @param {String} xtypeName The xtype name
         * @param {Ext.Class} classRef The class
         * @param {Array} paramArray Signature (in out parameters, etc.)
         * @return {Ext.CompositeElement} this
         */
        register: function(xtypeName, classRef, paramArray) {
            if (xtypeName.toLowerCase() !== xtypeName) {
                RP.throwError("rp reg xtypes must be all lower case - " + xtypeName);
            }
            
            // verify the alias can actually be set
            if (Ext.isFunction(classRef) && Ext.isEmpty(classRef.$className)) {
                var message = Ext.String.format("Widget Class being registered under '{0}' must be created using Ext.define instead of Ext.extend", xtypeName);
                logger.logError(message);
                RP.throwError(message);
            }
            
            Ext.ClassManager.setAlias(classRef, 'widget.' + xtypeName);
            
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
                    RP.throwError("Component is not registered: " + xtypeName + ".");
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
         * @param {String} xtypeName The xtype name
         * @param {Object[]} paramValueArray The parameter values
         * @param {String} hash Optional hash value
         * @return {Ext.Component} The newly instantiated Component
         */
        create: function(xtypeName, paramValueArray, hash) {
            var newType = this.createConfig(xtypeName, paramValueArray, hash);
            return Ext.ComponentMgr.create(newType);
        },
        
        /**
         * Gets the component's signature (in out parameters, etc.)
         * @param {String} xtype The xtype name
         * @return {Object} Signature object, containing each parameter at the
         * key of the parameter name
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
Ext.ns("RP.core");

/**
 * @class RP.core.CommonExtensions
 * @singleton
 *
 * Singleton for accessing common Ext extensions for our components
 */
RP.core.CommonExtensions = function() {
    var printChildren = function(config, options) {
        var sb = [];
        
        // Check to see if windowOptions was passed into the config.
        config.windowOptions = config.windowOptions || {};
        
        var stashClientMode = RP.globals.getValue("STASH_CLIENTMODE");
        var buildString = RP.stash.getVersion("rpcommon").build;
        if (buildString !== "") {
            buildString = "-" + buildString;
        }
        
        sb.push("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        sb.push("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        sb.push("<head>");
        sb.push("<title>" + config.title + "</title>");

        // push in all of the css from the current page
        Ext.Array.each(document.styleSheets, function(sheet) {
            if (!Ext.isEmpty(sheet.href)) {
                sb.push(Ext.String.format('<link rel="stylesheet" type="text/css" media="all" href="{0}" />', sheet.href));
            }
        });

        // add the print page css
        sb.push(Ext.String.format('<link rel="stylesheet" type="text/css" media="all" href="{0}rpcommon-print-css.{1}{2}.css" />', RP.stash.DEPLOYED_ROOT, stashClientMode, buildString));
        
        // Add custom .css references.
        if (config.cssURLs) {
            Ext.each(config.cssURLs, function(cssUrl) {
                sb.push(Ext.String.format("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"{0}\" />", cssUrl));
            }, this);
        }
        
        sb.push("</head>");
        sb.push("<body onLoad='javascript:self.print();'>");
        
        // Append markup of all printable children.
        var items = (config.items instanceof Array) ? config.items : [config.items];
        Ext.each(items, function(item, index) {
            if (!item.rendered) {
                return;
            }
            
            var childFilter;
            if (config.printChildFilter && !(childFilter = config.printChildFilter(item))) {
                return;
            }
            
            if (typeof childFilter === "object") {
                item = childFilter;
            }
            
            var iPrintMarkup = RP.iget(item, RP.interfaces.IPrintSource);
            
            sb.push("<div>");
            if (iPrintMarkup) {
                var markup = iPrintMarkup.getMarkupForPrinting(options);
                sb.push(markup);
            }
            else {
                var dom = item.getEl().dom;
                sb.push(dom.outerHTML || RP.util.DOM.getOuterHTML(dom));
            }
            
            sb.push("</div>");
        });
        
        sb.push("</body>");
        sb.push("</html>");
        
        var printWin = window.open("", "PrintWindow",
            "toolbar="     + (config.windowOptions.toolbar     || 'yes') +
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
    
    var showIcon = function(iconParent, type, show) {
        var typeSelector = Ext.String.format('[type="{0}"]', type);
        var tool = iconParent.down(typeSelector);
        
        if (tool) {
            if (show) {
                tool.show();
            }
            else {
                tool.hide();
            }
        }
    };
    
    return {
        /**
          * Adds a print tool icon and handler to an ExtJS component.  Must be called prior to being rendered.
          * <ul>
          * <li>If a control in the 'items' list implements RP.interfaces.IPrintSource, then
          *    that interface is used to generate the markup for printing; otherwise, the 
          *    outerHTML of the child element is used</li>
          * <li>Only children with 'rendered' set to true will be printed</li>
          * @param {Object} config An object with the following properties:
          * <ul>
          *  <li><b>title</b>: {@link String} Title of printed page.</li>
          *  <li><b>iconParent</b>: {@link Ext.Component} Control with a toolbar to place the Print icon into
          *  <li><b>items</b>: a control or array of controls to print.</li>
          *  <li><b>printChildFilter</b>: [{@link Function}] (Optional) A callback function to filter which
          *    children to print; if not supplied, all children are printed.
          *    Callback returns true/false to indicate whether or not to print
          *    the child passed in as the sole argument.  The callback may also
          *    instead return a Component to print (usually a child of the item.)</li>
          *  <li><b>cssURLs</b>: [{@link String}[]] (Optional) An array of URLs (string) of CSS files to
          *    include in the HTML header for printing</li>
          *  <li><b>printOptionsDelegate</b>: {@link Function} A delegate that gets called before actual printing</li>
          *  <li><b>windowOptions</b>: [{@link Object}] (Optional) A list of options for the print window that pops up
          *      <ul>
          *       <li><b>toolbar</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>location</b>: {@link String} yes/no (Defaults to no)</li>
          *       <li><b>directories</b>: {@link String} yes/no (Defaults to no)</li>
          *       <li><b>status</b>: {@link String} yes/no (Defaults to no)</li>
          *       <li><b>menubar</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>scrollbars</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>resizable</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>copyhistory</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>width</b>: {@link Number} A number (Defaults to 400)</li>
          *       <li><b>height</b>: {@link Number} A number (Defaults to 400)</li>
          *       </ul>
          *    </li>
          * </ul>
          */
        addPrintHandler: function(config) {
            // Append print button to the control's toolbar.
            var tools = config.iconParent.tools || [];
            
            tools.push({
                type: 'print',
                handler: function() {
                    if (typeof(config.printOptionsDelegate) === "function") {
                        var fn = function(options) {
                            printChildren(config, options);
                        };
                        
                        config.printOptionsDelegate(fn);
                    }
                    else {
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
          * <li>If a control in the 'items' list implements RP.interfaces.IPrintSource, then
          *    that interface is used to generate the markup for printing; otherwise, the 
          *    outerHTML of the child element is used</li>
          * <li>Only children with 'rendered' set to true will be printed</li>
          * @param {Object} config An object with the following properties:
          * <ul>
          *  <li><b>title</b>: {@link String} Title of printed page.</li>
          *  <li><b>items</b>: a control or array of controls to print.</li>
          *  <li><b>printChildFilter</b>: [{@link Function}] (Optional) A callback function to filter which
          *    children to print; if not supplied, all children are printed.
          *    Callback returns true/false to indicate whether or not to print
          *    the child passed in as the sole argument.  The callback may also
          *    instead return a Component to print (usually a child of the item.)</li>
          *  <li><b>cssURLs</b>: [{@link String}[]] (Optional) An array of URLs (string) of CSS files to
          *    include in the HTML header for printing</li>
          *  <li><b>printOptionsDelegate</b>: {@link Function} A delegate that gets called before actual printing</li>
          *  <li><b>windowOptions</b>: [{@link Object}] (Optional) A list of options for the print window that pops up
          *      <ul>
          *       <li><b>toolbar</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>location</b>: {@link String} yes/no (Defaults to no)</li>
          *       <li><b>directories</b>: {@link String} yes/no (Defaults to no)</li>
          *       <li><b>status</b>: {@link String} yes/no (Defaults to no)</li>
          *       <li><b>menubar</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>scrollbars</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>resizable</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>copyhistory</b>: {@link String} yes/no (Defaults to yes)</li>
          *       <li><b>width</b>: {@link Number} A number (Defaults to 400)</li>
          *       <li><b>height</b>: {@link Number} A number (Defaults to 400)</li>
          *       </ul>
          *    </li>
          * </ul>
          */
        printComponents: function(config, options) {
            printChildren(config, options);
        },
        
        /**
          * Adds a Reload tool icon to your container's title bar
          * @param {Object} config An object with the following properties:
          * <ul>
          *  <li><b>iconParent</b>: {@link Ext.Component} Control with a toolbar to place the icon into
          *  <li><b>reloadDelegate</b>: {@link Function} The delegate to call when the icon is clicked.</li>
          * </ul>
          */
        addReloadHandler: function(config) {
            // Append reload button to the control's toolbar.
            var tools = config.iconParent.tools || [];
            
            tools.push({
                type: 'refresh',
                handler: function() {
                    config.reloadDelegate();
                }
            });
            
            config.iconParent.tools = tools;
        },
        
        /**
          * Show/hide the Reload icon
          * @param {Ext.Component} iconParent The container with the title bar
          * @param {Boolean} show True to show, False to hide the icon
          */
        showReloadIcon: function(iconParent, show) {
            showIcon(iconParent, "refresh", show);
        },
        
        /**
          * Show/hide the Print icon
          * @param {Ext.Component} iconParent The container with the title bar
          * @param {Boolean} show True to show, False to hide the icon
          */
        showPrintIcon: function(iconParent, show) {
            showIcon(iconParent, "print", show);
        }
    };
}();
//////////////////////
// ..\js\event\AppEventManager.js
//////////////////////
Ext.ns("RP.event");

/**
 * @class RP.event.AppEventManager
 * 
 * Manages well-known application-wide events, i.e. {@link RP.upb.AppEvents}.  
 * Provides a way for UI components and business classes alike to fire 
 * and/or listen to events.
 * @singleton
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
       * @param {String} evtName The event name
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
       * @param {String} evtName The event name
       * @param {Function} handler The delegate to invoke when event is fired
       */
      unregister: function(evtName, handler)
      {
        evtPool.un(evtName, handler);
      },
      
      /**
       * Fires the named event.  Caller can pass in as many arguments as necessary.
       * @param {String} evtName The event name
       * @param {Object...} vargs (Optional) Variable number of arguments
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
       * Suspends all events from firing
       */
      suspendEvents: function()
      {
        logger.logTrace("[AppEventManager] Entering suspended state");
        evtPool.suspendEvents();
      },
      
      /**
       * Resumes ability to fire events.
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
Ext.ns("RP.event");

/**
 * @class RP.event.AppEventProxy
 *
 * Acts as as proxy to AppEventManager for listeners that wish to have
 * a sleeping/awake state.  If the listener is in the awake state, events from
 * AppEventManager pass straight through to the listener.  If the listener is in the
 * sleeping state, events are queued up as they are received (a prior queued event
 * with the same name can be optionally discarded, i.e. replaced with the latest event).
 * When the listener changes from the sleeping state to the awake state, the queued events 
 * are relayed to the listener.
 * 
 * @singleton
 */
RP.event.AppEventProxy = (
  function() 
  {
    var evtListenerDict = {};   // event dictionary; key=event name, value=array of {listener, handler} instances
    var awakeListeners = [];    // array of listeners
    var asleepListeners = [];   // array of {listener, q} instances, where q is an array of {evtname, handler} instances
    
    /**
     * Creates a new set of [listener, q], where q is an array of 
     * {evtname, handler} instances. This can be thought of as a sleeping
     * listener object.
     * @param {Object} listener A listener which is supposed to be added, in the 
     * sleep state
     * @return {Object} A sleeping listener entry
     * @private
     */
    var createSleepingListener = function(listener)
    {
      return {
        listener: listener,
        q: []
      };
    };
    
    /**
     * If evtName doesn't exist in the event dictionary, it is added as a 
     * new entry.
     * @param {String} evtName Key name in the event dictionary
     * @return {Object} An event, containing an isNew boolean and an array,
     * eventListeners
     * @private
     */
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
    
    /**
     * Looks through the array of awake listeners to find the param listener.
     * If it isn't found, return is null. 
     * @param {Object} listener The listener to look for
     * @param {Boolean} remove Specify true to remove the listener if found
     */
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
    
    /**
     * Looks through the array of asleep listeners to find the param listener.
     * If it isn't found, return is null. 
     * @param {Object} listener The listener to look for
     * @param {Boolean} remove Specify true to remove the listener if found
     * @private
     */
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
    
    /**
     * Adds an event to the queue. 
     * @param {Array} al Array of {listener, q} instances, where q is an array 
     * of {evtname, handler} instances. This represents all asleep listeners.
     * @param {String} evtName Key name in al
     * @param {Function} fn Handler function for the event
     * @param {Boolean} removeDuplicateEvents Removes all events with the same 
     * evtName
     * @private
     */
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
    
    /**
     * Calls the handlers for all queued events
     * @param {Object} al Array of {listener, q} instances, where q is an array 
     * of {evtname, handler} instances. This represents all asleep listeners.
     * @private
     */
    var processQueuedEvents = function(al)
    {
      // Loop thru queued events and relay them to the listener.
      Ext.each(al.q, function(qevt)
      {
        qevt.handler();
      },
      this);
    };
    
    /**
     * Looks through queued events to find all entries with the same name
     * @param {Object} al Array of {listener, q} instances, where q is an array 
     * of {evtname, handler} instances. This represents all asleep listeners.
     * @param {String} evtName Key name in al
     * @param {Boolean} remove Specify true to remove all such elements
     * @return {Array} An array of {evtname, handler} instances. 
     * @private
     */
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
    
    /**
     * Finds an event listener, listener, in e. Returns null if it doesn't
     * exist in e.
     * @param {Object} e Contains an array, eventListeners
     * @param {Object} listener The listener to search for
     * @param {Boolean} remove Specify true to remove the listener, if found
     * @private
     */
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
    
    /**
     * Event handler that interfaces with AppEventManager.  This contains the 
     * main proxy functionality.
     * @param {String} evtName Key name in the event dictionary
     */
    var eventHandler = function(evtName /* , vargs... */)
    {
      var e = getOrCreateEventDictEntry(evtName);
      var origArgs = arguments;

      Ext.each(e.eventListeners,
        function(l)
        {
          var fn = Ext.bind(l.handler, l.handler, origArgs, false);

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
       * @param {Object} listener The listener object
       * @param {Boolean} isAwake Flag indicating the current listener state;
       * True for awake, False for sleeping
       */
      registerListener: function(listener, isAwake)
      {      
        if (findAsleepListener(listener, false) || findAwakeListener(listener, false))
        {
          RP.throwError("Listener already registered.");
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
              Ext.Array.remove(la, l);
              return false;   // break the loop
            }
            return true;  // continue the loop
          }, this);
        }, this);        
      },
      
      /**
       * Subscribe to an application event.
       * @param {String} evtName The event name
       * @param {Object} listener The listener object
       * @param {Function} handler The delegate to invoke when event is fired
       * @param {Boolean} removeDuplicateEvents Flag indicating whether or not to remove previous
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
       * @param {String} evtName The event name
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
       * @param {Object} listener The listener object
       * @param {Boolean} isAwake Flag indicating whether or that the listener is in an awake state
       */
      setState: function(listener, isAwake)
      {

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
// ..\js\ui\Mixins\Exportable.js
//////////////////////
/**
 * This class is intended to be used as a mixin.
 *
 * An exportable component can be exported via the basetaskform export action.  In order for this to
 * work, you need to supply a back-end method for consuming your serialized component data and
 * transforming it into the desired format.  Common formats for export are pdf and csv, but there are
 * no limitations to the number or type of formats.
 *
 * When you instantiate an exportable component, it will immediately attempt to set up the linkage to
 * the export event of the top-level taskform container.  If the component is not currently contained by
 * a taskform (which it usually isn't at creation time), listeners will be set up to wait for it to be
 * added to a taskform and the linkage will be set up at that time.
 *
 * Following is an example of typical usage.
 *
 *      Ext.define('ExportableThing', {
 *          extend: 'Ext.Component',
 *          alias: ['widget.exportable-thing'],
 *          mixins: {
 *              exportable: 'RP.ui.Mixins.Exportable'
 *          },
 *          exportFormats: [
 *              new RP.data.model.Format({
 *                  format: 'pdf',
 *                  display: 'PDF'
 *              }),
 *              new RP.data.model.Format({
 *                  format: 'csv',
 *                  display: 'CSV'
 *              })
 *          ],
 *          performExport: function(exportables) {
 *              var format = exportables.get(this.id);
 *              if (format === false) {
 *                  // user chose not to export this component
 *                  return;
 *              } else if (format === true) {
 *                  // default the format
 *                  format = 'pdf';
 *              }
 *              // call your backend export method sending whatever serialized data you need
 *              // to perform the export
 *              this.callBackendExportUtility(this.serializeData(), format);
 *          },
 *          ...
 *      });
 *
 *      // once the taskform is displayed, just clicking the export button will kick off
 *      // the export action, calling performExport on your ExportableThing instance.
 *      Ext.define('sample.widget', {
 *          extend: 'RP.taskflow.BaseTaskflowWidget',
 *          createTaskForm: function() {
 *              this.taskForm = Ext.ComponentMgr.create({
 *                  xtype: 'rptaskform',
 *                  allowExport: true,
 *                  items: [{
 *                      xtype: 'exportable-thing'
 *                  }],
 *              });
 *          }
 *      });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.ui.Mixins.Exportable', {
    extend: 'RP.util.ConstructedMixin',

    /**
     * @cfg {RP.data.model.Format[]} exportFormats
     * This array of Format models is used to supply the user with choices and determines what
     * code is added to the exportables map when the export event is fired.  If no export formats
     * are supplied, it is assumed that there is only one default format that can be exported to.
     */
    exportFormats: [],

    constructor: function() {
        Ext.applyIf(this, {
            _exportable: true
        });
        this._setupExportableListeners();
    },

    /**
     * Turn on or off the exportable behavior.
     * @param {Boolean} enable false to disable, else enable
     */
    setExportable: function(enable) {
        this._exportable = (enable !== false);
    },

    /**
     * @private
     * Setup the export event listener.  If this component isn't part of a taskform yet, listen to it's
     * top-level parent's added event and attempt to rerun.
     */
    _setupExportableListeners: function() {
        var taskform = (this instanceof RP.taskflow.BaseTaskForm ? this : this.up('rptaskform'));

        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this) || this;
            top.on('added', this._setupExportableListeners, this, { single: true});
            return;
        }

        taskform.on('export', this.performExport, this);
    },

    /**
     * @method performExport
     * Perform the export action when the export event is fired by the taskform.
     * This method *must* be implemented or an error will be thrown when an export
     * is performed on the taskform.
     * @param {Ext.util.MixedCollection} exportables A map containing the selection value
     *      (false, true or string format) keyed by the exportable component's id.  Listeners should run
     *      exportables.get(this.id) to determine if they should export and in what format.  False
     *      indicates that the user chose not to export that component, true to export in default
     *      format (no {@link RP.ui.Mixins.Exportable#exportFormats} were configured), and a
     *      string value indicates the format the user selected.
     */
    performExport: RP.abstractFn
});
//////////////////////
// ..\js\ui\Mixins\Printable.js
//////////////////////
/**
 * This class is intended to be used as a mixin.
 *
 * A printable component can be printed via the basetaskform print action.  You are free to implement
 * a print method of your own.  The RP.core.CommonExtensions.printComponents method can print one or more
 * components into a single document if you would like to make use of that.
 *
 * When you instantiate a printable component, it will immediately attempt to set up the linkage to
 * the print event of the top-level taskform container.  If the component is not currently contained by
 * a taskform (which it usually isn't at creation time), listeners will be set up to wait for it to be
 * added to a taskform and the linkage will be set up at that time.
 *
 * Following is an example of typical usage.
 *
 *      Ext.define('PrintableThing', {
 *          extend: 'Ext.Component',
 *          alias: ['widget.printable-thing'],
 *          mixins: {
 *              printable: 'RP.ui.Mixins.Printable'
 *          },
 *          performPrint: function(printables) {
 *              if (printables.get(this.id) === true) {
 *                  RP.core.CommonExtensions.printComponents({items: [this]});
 *              }
 *          },
 *          ...
 *      });
 *
 *      // once the taskform is displayed, just clicking the print button will kick off
 *      // the print action, calling performPrint on your PrintableThing instance.
 *      Ext.define('sample.widget', {
 *          extend: 'RP.taskflow.BaseTaskflowWidget',
 *          createTaskForm: function() {
 *              this.taskForm = Ext.ComponentMgr.create({
 *                  xtype: 'rptaskform',
 *                  allowExport: true,
 *                  items: [{
 *                      xtype: 'printable-thing'
 *                  }],
 *              });
 *          }
 *      });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.ui.Mixins.Printable', {
    extend: 'RP.util.ConstructedMixin',
    constructor: function() {
        Ext.applyIf(this, {
            _printable: true
        });
        this._setupPrintableListeners();
    },

    /**
     * Turn on or off the printable behavior.
     * @param {Boolean} enable false to disable, else enable
     */
    setPrintable: function(enable) {
        this._printable = (enable !== false);
    },

    /**
     * @private
     * Setup the print event listener.  If this component isn't part of a taskform yet, listen to it's
     * top-level parent's added event and attempt to rerun.
     */
    _setupPrintableListeners: function() {
        var taskform = (this instanceof RP.taskflow.BaseTaskForm ? this : this.up('rptaskform'));

        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this) || this;
            top.on('added', this._setupPrintableListeners, this, { single: true});
            return;
        }

        taskform.on('print', this.performPrint, this);
    },

    /**
     * @method performPrint
     * Perform the print action when the print event is fired by the taskform.
     * This method *must* be implemented or an error will be thrown when an export
     * is performed on the taskform.
     * @param {Ext.util.MixedCollection} printables A map containing the selection value
     *      (true or false) keyed by the printable component's id.  Listeners should run
     *      printables.get(this.id) to determine if they should print.  False indicates that the
     *      user chose not to print that component, true indicates a print should take place.
     */
    performPrint: RP.abstractFn
});
//////////////////////
// ..\js\ui\Mixins\Refreshable.js
//////////////////////
/**
 * This class is intended to be used as a mixin.
 *
 * A refreshable component can be refreshed via the basetaskform refresh action.  You must implement
 * implement the performRefresh method on a refreshable component.  If you wish to use loading masks,
 * it is the responsibility of the refresh method to take care of that.
 *
 * When you instantiate a refreshable component, it will immediately attempt to set up the linkage to
 * the refresh event of the top-level taskform container.  If the component is not currently contained by
 * a taskform (which it usually isn't at creation time), listeners will be set up to wait for it to be
 * added to a taskform and the linkage will be set up at that time.
 *
 * Following is an example of typical usage.
 *
 *      Ext.define('RefreshableThing', {
 *          extend: 'Ext.grid.Panel',
 *          alias: ['widget.refreshable-thing'],
 *          mixins: {
 *              refreshable: 'RP.ui.Mixins.Refreshable'
 *          },
 *          performRefresh: function() {
 *              this.getStore().reload();
 *          },
 *          ...
 *      });
 *
 *      // once the taskform is displayed, just clicking the refresh button will kick off
 *      // the refresh action, calling performRefresh on your RefreshableThing instance.
 *      Ext.define('sample.widget', {
 *          extend: 'RP.taskflow.BaseTaskflowWidget',
 *          createTaskForm: function() {
 *              this.taskForm = Ext.ComponentMgr.create({
 *                  xtype: 'rptaskform',
 *                  allowExport: true,
 *                  items: [{
 *                      xtype: 'refreshable-thing',
 *                      ...
 *                  }],
 *              });
 *          }
 *      });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.ui.Mixins.Refreshable', {
    extend: 'RP.util.ConstructedMixin',
    constructor: function() {
        Ext.applyIf(this, {
            _refreshable: true
        });
        this._setupRefreshableListeners();
    },

    /**
     * Turn on or off the refreshable behavior.
     * @param {Boolean} enable false to disable, else enable
     */
    setRefreshable: function(enable) {
        this._refreshable = (enable !== false);
    },

    /**
     * @private
     * Setup the refresh event listener.  If this component isn't part of a taskform yet, listen to it's
     * top-level parent's added event and attempt to rerun.
     */
    _setupRefreshableListeners: function() {
        var taskform = (this instanceof RP.taskflow.BaseTaskForm ? this : this.up('rptaskform'));

        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this) || this;
            top.on('added', this._setupRefreshableListeners, this, { single: true});
            return;
        }

        taskform.on('refresh', this.performRefresh, this);
    },

    /**
     * @method performRefresh
     * Perform the refresh action when a refresh event is fired on the taskform.
     * This method must be implemented or an error will be thrown.
     */
    performRefresh: RP.abstractFn
});
//////////////////////
// ..\js\ui\ExportablePlugin.js
//////////////////////
/**
 * This plugin makes any component exportable via the taskform export button.
 *
 * An implementation for performExport must be supplied.  This must be on the plugin itself,
 * not on the component that is being plugged into.  This can be done ad-hoc on the plugin,
 * or the plugin can be extended to supply an implmentation for a specific component or family
 * of components.
 *
 * See {@link RP.taskflow.BaseTaskForm#allowExport} and {@link RP.ui.Mixins.Exportable}
 *
 * Example ad-hoc usage:
 *
 *      Ext.create('Ext.Component', {
 *          plugins: [new RP.ui.ExportablePlugin({
 *              exportFormats: [
 *                  new RP.data.model.Format({
 *                      format: 'pdf',
 *                      display: 'PDF'
 *                  }),
 *                  new RP.data.model.Format({
 *                      format: 'csv',
 *                      display: 'CSV'
 *                  })
 *              ],
 *              performExport: function(exportables) {
 *                  // value is keyed by the plugin's component, not the plugin itself
 *                  var value = exportables.get(this.cmp.id);
 *                  if (value !== false) {
 *                      var format = (value === true ? 'pdf' : value);
 *                      this.cmp.callBackendExportMethod(this.cmp.serializeData(), format);
 *                  }
 *              }
 *          })]
 *      });
 *
 * Example usage of plugin extension:
 *
 *      // exportable plugin specific to form panels
 *      Ext.define('exportable.form.plugin', {
 *          extend: 'RP.ui.ExportablePlugin',
 *          performExport: function(exportables) {
 *              if (exportables.get(this.cmp) !== false) {
 *                  ...  // perform export specific to form panels
 *              }
 *          }
 *      });
 *      Ext.create('Ext.form.Panel', {
 *          plugins: [new exportable.form.plugin({})],
 *          ...
 *      });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.ui.ExportablePlugin', {
    extend: 'Ext.AbstractPlugin',
    mixins: {
        exportMixin: 'RP.ui.Mixins.Exportable'
    },

    /**
     * @cfg {Object} scope
     * Scope to run the handler method in.  Defaults to this plugin object.
     */
    
    /**
     * @cfg {String} entity
     * The exporting entity on the server
     */

    init: function(cmp) {
        var me = this;
        me.cmp = cmp;
        me.cmp._exportable = true;
        me.cmp.exportFormats = me.exportFormats;
        /* add a method to the component to retrieve this plugin */
        me.cmp.getExportablePlugin = function() {
            return me;
        };
        if (me.disabled === false) {
            me.enable();
        }

        this.downloadFrame = new Ext.ux.IFrameComponent({
            hidden: true,
            renderTo: Ext.getBody()
        });

        this.cmp.exportFormats = [
            new RP.data.model.Format({
                format: 'pdf',
                display: RP.getMessage('rpux.fileformat.pdf')
            }), new RP.data.model.Format({
                format: 'csv',
                display: RP.getMessage('rpux.fileformat.csv')
            })
        ];
    },

    performExport: function(exportables) {
        var exportable = exportables.get(this.cmp.id);
        if (exportable) {
            this.makeExportRequest(Ext.String.capitalize(exportable));
        }
    },

    serialize: RP.AbstractFn,

    /**
     * This method enables the plugin and sets up the listener to the taskform export event.
     */
    enable: function() {
        this.callParent();
        var taskform = (this.cmp instanceof RP.taskflow.BaseTaskForm ? this.cmp : this.cmp.up('rptaskform'));

        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this.cmp) || this.cmp;
            top.on('added', this.enable, this, { single: true});
            return;
        }

        taskform.on('export', this.performExport, this.scope || this);
    },

    /**
     * This method disables the plugin and removes the listener to the taskform print event.
     */
    disable: function() {
        this.callParent();
        var taskform = (this.cmp instanceof RP.taskflow.BaseTaskForm ? this.cmp : this.cmp.up('rptaskform'));
        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this.cmp) || this.cmp;
            top.un('added', this.enable, this);
        } else {
            taskform.un('export', this.performExport, this.scope || this);
        }
    },

    /**
     * make the export ajax request
     * @param  {String} format the format for the 
     * export
     */
    makeExportRequest: function(format) {
        Ext.Ajax.request({
            url: RP.globals.POST_EXPORT_URL,
            method: 'POST',
            params: {
                format: format,
                entity: this.entity,
                encoding: 'json'
            },
            jsonData: this.serialize(format),
            headers: {
                'Content-Type': 'text/plain; charset=utf-8'
            },
            scope: this,
            success: this._onExportSuccess,
            failure: this._onExportFailure
        });
    },

    _onExportSuccess: function(res) {
        var key = res.responseText;
        var url = Ext.String.format('{0}/{1}/?filename={2}',
                RP.globals.GET_EXPORT_URL,
                key,
                encodeURI(this.buildTitle())
            );

        this.downloadFrame.load(url);
    },

    /**
     * @protected
     */
    getTitle: function() {
        return this.title || this.cmp.title || 'export-' + new Date().getTime();
    },

    /**
     * @protected
     */
    buildTitle: function() {
        var title = this.getTitle();
        return title.trim().replace(new RegExp(' ', 'g') , '_');
    },

    _onExportFailure: function(e) {
        logger.logError("[Exportable] Export failed");
    }
});
//////////////////////
// ..\js\ui\PrintablePlugin.js
//////////////////////
/**
 * This plugin makes any component printable via the taskform print button.
 *
 * An implementation for performPrint must be supplied.  This must be on the plugin itself,
 * not on the component that is being plugged into.  This can be done ad-hoc on the plugin,
 * or the plugin can be extended to supply an implmentation for a specific component or family
 * of components.
 *
 * See {@link RP.taskflow.BaseTaskForm#allowPrint} and {@link RP.ui.Mixins.Printable}
 *
 * Example ad-hoc usage:
 *
 *      Ext.create('Ext.Component', {
 *          plugins: [new RP.ui.PrintablePlugin({
 *              performPrint: function(printables) {
 *                  // value is keyed by the plugin's component, not the plugin itself
 *                  if (printables.get(this.cmp.id) !== false) {
 *                      RP.core.CommonExtensions.printComponents({items: [this]});
 *                  }
 *              }
 *          })]
 *      });
 *
 * Example usage of plugin extension:
 *
 *      // printable plugin specific to panels
 *      Ext.define('printable.panel.plugin', {
 *          extend: 'RP.ui.PrintablePlugin',
 *          performPrint: function(printables) {
 *              if (printables.get(this.cmp) !== false) {
 *                  RP.core.CommonExtensions.printComponents({
 *                      items: [this],
 *                      ... // special panel printing options
 *                  });
 *              }
 *          }
 *      });
 *      Ext.create('Ext.panel.Panel', {
 *          plugins: [new printable.panel.plugin({})],
 *          ...
 *      });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.ui.PrintablePlugin', {
    extend: 'Ext.AbstractPlugin',
    mixins: {
        printMixin: 'RP.ui.Mixins.Printable'
    },

    /**
     * @cfg {Object} scope
     * Scope to run the handler method in.  Defaults to this plugin object.
     */

    init: function(cmp) {
        var me = this;
        me.callParent(arguments);
        me.cmp = cmp;
        me.cmp._printable = true;
        /* add a method to the component to retrieve this plugin */
        me.cmp.getPrintablePlugin = function() {
            return me;
        };
        if (me.disabled === false) {
            me.enable();
        }
    },

    /**
     * This method enables the plugin and sets up the listener to the taskform print event.
     */
    enable: function() {
        this.callParent();
        var taskform = (this.cmp instanceof RP.taskflow.BaseTaskForm ? this.cmp : this.cmp.up('rptaskform'));

        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this.cmp) || this.cmp;
            top.on('added', this.enable, this, { single: true});
            return;
        }

        taskform.on('print', this.performPrint, this.scope || this);
    },

    /**
     * This method disables the plugin and removes the listener to the taskform print event.
     */
    disable: function() {
        this.callParent();
        var taskform = (this.cmp instanceof RP.taskflow.BaseTaskForm ? this.cmp : this.cmp.up('rptaskform'));
        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this.cmp) || this.cmp;
            top.un('added', this.enable, this);
        } else {
            taskform.un('refresh', this.performPrint, this.scope || this);
        }
    }
});
//////////////////////
// ..\js\ui\RefreshablePlugin.js
//////////////////////
/**
 * This plugin makes any component refreshable via the taskform refresh button.
 *
 * An implementation for performRefresh must be supplied.  This must be on the plugin itself,
 * not on the component that is being plugged into.  This can be done ad-hoc on the plugin,
 * or the plugin can be extended to supply an implmentation for a specific component or family
 * of components.
 *
 * See {@link RP.taskflow.BaseTaskForm#allowRefresh} and {@link RP.ui.Mixins.Refreshable}
 *
 * Example ad-hoc usage:
 *
 *      Ext.create('Ext.grid.Panel', {
 *          plugins: [new RP.ui.RefreshablePlugin({
 *              performRefresh: function() {
 *                  this.cmp.getStore().reload();
 *              }
 *          })],
 *          ...
 *      });
 *
 * Example usage of plugin extension:
 *
 *      // printable plugin specific to charts
 *      Ext.define('refreshable.chart.plugin', {
 *          extend: 'RP.ui.RefreshPlugin',
 *          performRefresh: function() {
 *              this.cmp.getStore().reload();
 *          }
 *      });
 *      Ext.create('Ext.panel.Panel', {
 *          plugins: [new refreshable.chart.plugin({})],
 *          ...
 *      });
 *
 * @author Jeff Gitter
 */
Ext.define('RP.ui.RefreshablePlugin', {
    extend: 'Ext.AbstractPlugin',
    mixins: {
        refreshMixin: 'RP.ui.Mixins.Refreshable'
    },

    /**
     * @cfg {Object} scope
     * Scope to run the handler method in.  Defaults to this plugin object.
     */

    init: function(cmp) {
        var me = this;
        me.callParent(arguments);
        me.cmp = cmp;
        me.cmp._refreshable = true;
        me.cmp.getRefreshablePlugin = function() {
            return me;
        };
        if (me.disabled === false) {
            me.enable();
        }
    },

    /**
     * This method enables the plugin and sets up the listener to the taskform refresh event.
     */
    enable: function() {
        this.callParent();
        var taskform = (this.cmp instanceof RP.taskflow.BaseTaskForm ? this.cmp : this.cmp.up('rptaskform'));

        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this.cmp) || this.cmp;
            top.on('added', this.enable, this, { single: true});
            return;
        }

        taskform.on('refresh', this.performRefresh, this.scope || this);
    },

    /**
     * This method disables the plugin and removes the listener to the taskform refresh event.
     */
    disable: function() {
        this.callParent();
        var taskform = (this.cmp instanceof RP.taskflow.BaseTaskForm ? this.cmp : this.cmp.up('rptaskform'));
        if (!taskform) {
            var top = RP.util.Component.getTopLevelContainer(this.cmp) || this.cmp;
            top.un('added', this.enable, this);
        } else {
            taskform.un('refresh', this.performRefresh, this.scope || this);
        }
    }
});
//////////////////////
// ..\js\ui\Mixins\TimeControl.js
//////////////////////
/**
 * A mixin to provide logic for parsing time in the RP specific way.
 */
Ext.define("RP.ui.Mixins.TimeControl", {

    extend: "Ext.Base",
    
    /**
     * @cfg {Object} format An RP.core.Formats.Time value to use for formatted
     * the entered time values with. Defaults to RP.core.Formats.Time.Default.
     */
    constructor: function(config) {
        this.addEvents("parseerror");
        this._theDate = RP.Date.clearTime(config.initDate || RP.core.Sundial.now());
        this._format = config.format || RP.core.Formats.Time.Default;
        this._roundMinutes = Ext.value(this.config.roundMinutes, 1);
        this.callParent(arguments);
    },
    
    /**
     * Combines the specified time value with the control's date.
     * @method
     * @param {String/Number} time The time value to combine with the control's date.
     * @return {Date} A Date object with the specified time value and the control's date.
     */
    parseValue: function(time) {
        if (typeof(time) === "string") {
            if (time && time.length > 0) {
                try {
                    var date = RP.core.IntervalJN.parseTimeAfterBase(time, this._theDate);
                    if (this._roundMinutes !== 0) {
                        date = RP.Date.round(date, this._roundMinutes);
                    }
                    
                    return date;
                } 
                catch (e) {
                    logger.logError("[ParseTime] parseValue failed for: " + time);
                    this.fireEvent("parseerror", this, time);
                }
                return null;
            }
            else {
                return null;
            }
        }
        else if (!Ext.isEmpty(time)) {
            return RP.Date.round(time, this._roundMinutes);
        }
        else {
            return null;
        }
    },
    
    /**
     * Formats the time using the TimeField's internal format.
     * @method
     * @param {Object} date The Date object.
     * @return {String} The formatted string, empty string if undefined or null was passed.
     */
    formatTime: function(date) {
        if (date) {
            return RP.Date.formatTime(date, this._format);
        }
        else {
            return "";
        }
    },
    
    /**
     * Sets the field's internal base date.
     * @method
     * @param {Date} date The date to set the field's internal base date to.
     */
    setTheDate: function(date) {
        if (!Ext.isDate(date)) {
            date = this.initialConfig.initDate || RP.core.Sundial.now();
        }
        
        this._theDate = RP.Date.clearTime(date);
        
        if (this.getRawValue()) {
            this.setValue(this.getRawValue());
        }
    },
    
    /**
     * Sets the field's value to the given time
     * @method
     * @param {Date} date The time to set the field's value to.
     */
    setTheTime: function(date) {
        this.setValue(this.formatTime(date));
    },
    
    /**
     * Sets the field's internal base date and time to the given value
     * @method
     * @param {Date} date The Date and time to set the field's internal base date to.
     */
    setDateTime: function(date) {
        this.setTheDate(date);
        this.setTheTime(date);
    },
    
    /**
     * Gets the field's formatted time value.
     * @method
     * @return {String} The formatted time.
     */
    getValue: function() {
        var date = this.callParent(arguments);
        return this.formatTime(this.parseValue(date, this._theDate)) || '';
    },
    
    /**
     * Gets the Date object the field, created by combining the field's
     * date object with specified time value.
     * @method
     * @return {Date}
     */
    getDateTime: function(raw) {
        if (raw) {
            return this.parseValue(this.getRawValue(), this._theDate);
        }
        
        return this.parseValue(this.getValue(), this._theDate);
    }
    
    
});
//////////////////////
// ..\js\ui\AbstractSuggest.js
//////////////////////
/**
 * A simple abstract class that will provide basic functionality for suggest box with an action button.
 * A concrete implementation must provide at least the createDataStore method to define the data store
 * behind the search box.  All other functionality can be taken as is, but may be overridden.
 * @constructor
 * Creates a new implementation of the AbstractSuggest component.
 * @param {Object} config The configuration for the container and live search box component.
 */
Ext.define("RP.ui.AbstractSuggest", {
    
    extend: "Ext.container.Container",
    
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
        
        this.callParent(arguments);
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
        
        return [this.searchField, this.createActionButtons()];
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
/**
 * @class RP.ui.Hyperlink
 * Simple Hyperlink class.
 * 
 * @extends Ext.BoxComponent
 * @xtype hyperlink
 *
 */
Ext.define("RP.ui.Hyperlink", {
  extend: "Ext.Component",
  alias: "widget.hyperlink",

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
    
    this.callParent();

    this.addEvents("click");
  },
  
  /**
   * Override to attach the click handler to the component's Ext.Element.
   * @private
   */
  afterRender: function() {
    this.callParent();
    
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
Ext.define("RP.ui.HyperlinkBreadCrumb", {
    
    extend: "RP.ui.Hyperlink",
    
    cls: "rp-breadcrumb",
    
    /**
     * @private
     */
    initComponent: function() {
        this.addEvents(        /**
         * @event activate
         * Fired when the breadcrumb is clicked and is not deactivated.
         * @param {RP.ui.HyperlinkBreadCrumb} this
         */
        "activate");
        
        this.callParent(arguments);
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
        this.addCls("deactivated");
    },
    
    /**
     * Does the inverse of deactivate.
     * @method
     */
    reactivate: function() {
        this.resumeEvents();
        this.removeCls("deactivated");
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
/**
 * @class RP.ui.AccordionScrollPanel
 * @extends Ext.panel.Panel
 * Accordion scroll panel with up and down arrows at the top and bottom.
 */
Ext.define("RP.ui.AccordionScrollPanel", {
    extend: "Ext.panel.Panel",
    alias: "widget.rpaccordionscrollpanel",

    constructor: function(config) {
        this._topToolbar = new Ext.Toolbar({
            dock: "top",
            itemId: "topToolbar",
            height: 22,
            cls: 'rp-scrollbar-up'
        });
        this._bottomToolbar = new Ext.Toolbar({
            dock: "bottom",
            itemId: "bottomToolbar",
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
            //tbar: this._topToolbar,
            //bbar: this._bottomToolbar,
            listeners: {
                render: function(panel) {
                    if (!registered) {
                        var dir = '';
                        var interval = null;
                        var tbar = this._topToolbar;
                        var bbar = this._bottomToolbar;

                        var fn = function() {
                            if (dir && panel.body.isScrollable()) {
                                panel.body.scroll(dir, 20);
                            }
                            else {
                                window.clearInterval(interval);
                                interval = null;
                            }
                        };
                        
                        // TODO evaluate if this still works correctly... after removing/adding back in
                        // probably a different el property?  unless the render method fires again
                        var attachTopToolbarListeners = function() {
                            tbar.el.dom.onmouseover = function() {
                                dir = 'up';
                                if(!interval) {
                                    interval = window.setInterval(fn, 100);
                                }
                                tbar.el.addCls('rp-scrollbar-over');
                            };

                            tbar.el.dom.onmouseout = function() {
                                dir = '';
                                tbar.el.removeCls('rp-scrollbar-over');
                            };

                            // iPad events to allow for scrolling the widgets
                            tbar.el.dom.onmouseup = tbar.el.dom.onmouseout;
                            tbar.el.dom.ontouchstart = tbar.el.dom.onmouseover;
                            tbar.el.dom.ontouchend = tbar.el.dom.onmouseup;
                        };
                        
                        var attachBottomToolbarListeners = function() {

                            bbar.el.dom.onmouseover = function() {
                                dir = 'down';
                                if(!interval) {
                                    interval = window.setInterval(fn, 100);
                                }
                                bbar.el.addCls('rp-scrollbar-over');
                            };

                            bbar.el.dom.onmouseout = function() {
                                dir = '';
                                bbar.el.removeCls('rp-scrollbar-over');
                            };

                            // iPad events to allow for scrolling the widgets
                            bbar.el.dom.onmouseup = bbar.el.dom.onmouseout;
                            bbar.el.dom.ontouchstart = bbar.el.dom.onmouseover;
                            bbar.el.dom.ontouchend = bbar.el.dom.onmouseup;
                        };

                        // Wait for the tbar to be rendered
                        if(!tbar.rendered) {
                            tbar.on("render", attachTopToolbarListeners);
                        }
                        else {
                            attachTopToolbarListeners();
                        }
                        
                        // Wait for the bbar to be rendered
                        if(!bbar.rendered) {
                            bbar.on("render", attachBottomToolbarListeners);
                        }
                        else {
                            attachBottomToolbarListeners();
                        }

                        registered = true;
                    }

                    originalRenderListener.call(originalRenderScope);
                    
                    // TODO Ext JS 4 evaluate this... needed to make scrollers appear on inital page
                    // load if the area is small enough for them to be required off the bat
                    this.setHeight(this.getHeight());
                },
                resize: this.fixScroll,
                expand: this.fixScroll,
                scope: this
            }
        };

        Ext.apply(config, cfg);
        
        this.callParent([config]);
        
    },
    
    fixScroll: function() {
        if (this.isDestroyed || this.collapsed || !this.getEl() || !this.getEl().getBox()) {
            return;
        }

        if (!this.items || this.items.length === 0) {
            if (this._count < 1000) { // WHAT??!?!
                // client and scroll heights aren't set right at this point, so need to delay
                if (this._timeInterval) {
                    window.clearTimeout(this._timeInterval);
                }
                this._timeInterval = window.setTimeout(Ext.bind(this.fixScroll, this, [this]), 100);
                this._count++;
            }
            return;
        }

        this._count = 0;

        var origHeight = this.getEl().getBox().height;

        if (this.body.isScrollable()) {
            if (!this._topToolbar.ownerCt) {
                this.addDocked(this._topToolbar);
            }
            if (!this._bottomToolbar.ownerCt) {
                this.addDocked(this._bottomToolbar);
            }
        }
        else {
            if (this._topToolbar.ownerCt === this) {
                this.removeDocked(this._topToolbar, false);
            }
            if (this._bottomToolbar.ownerCt === this) {
                this.removeDocked(this._bottomToolbar, false);
            }
            this.body.scrollTo('top');
        }

        var newHeight = this.getEl().getBox().height;
        if (origHeight !== newHeight) {
            this.setHeight(origHeight);
        }
    }

});
//////////////////////
// ..\js\ui\ChangePassword.js
//////////////////////
// TODO: client-side standards for checking password strength?

/**
 * @class RP.ui.ChangePassword
 * @extends Ext.window.Window
 * A Window which allows the user to change their password.
 * 
 * @cfg {String} formTitle The title of the form.
 */
Ext.define("RP.ui.ChangePassword", {
    extend: "Ext.window.Window",
    
    width: 450,
    draggable: false,
    resizable: false,
    closable: false,
    modal: true,
    border: false,
    bodyPadding: 20,
    
    /**
     * Whether or not the password change was successful.
     * @type Boolean
     * @property success
     */
    success: false,
    
    /**
     * @private
     */
    initComponent: function() {
        
        Ext.apply(this, {
            title: this.formTitle || RP.getMessage("rp.common.changepassword.FormTitle"),
            items: [{
                xtype: "component",
                html: this.initialConfig.formIntro || RP.getMessage("rp.common.changepassword.FormIntro")
            }, {
                xtype: "form",
                itemId: "form",
                style: "margin: 20px auto",
                fieldDefaults: {
                    labelAlign: "right",
                    labelStyle: "font-weight: bold",
                    labelWidth: 125
                },
                defaults: {
                    anchor: "100%",
                    inputType: "password",
                    listeners: {
                        specialkey: this.onSpecialKey,
                        scope: this
                    }
                },
                width: 320,
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
                    handler: this.onSubmit,
                    scope: this
                }, {
                    itemId: "btnCancel",
                    text: RP.getMessage("rp.common.changepassword.CancelButton"),
                    handler: this.close,
                    scope: this
                }]
            }]
        });
        
        this.callParent();
        
        this.on("show", this.focusCurrentPassword, this);
        
        var inactiveFn = Ext.bind(this.destroy, this);
        
        RP.util.PageInactivityChecker.addHandler(inactiveFn);
        
        this.on("destroy", function() {
            RP.util.PageInactivityChecker.removeHandler(inactiveFn);
        }, this);
    },
    
    /**
     * Retrieve the new password
     * @return {String}
     */
    getNewPassword: function() {
        return this.newPassword;
    },
    
    /**
     * Focuses the current password field.
     */
    focusCurrentPassword: function() {
        this.getComponent("form").getComponent("curPwd").focus(false, 500);
    },
    
    /**
     * @private
     */
    onSpecialKey: function(field, e) {
        if (e.getKey() == e.ENTER) {
            e.stopEvent();
            this.onSubmit();
        }
    },
    
    /**
     * Allows the enter key to submit the form.
     * @private
     */
    onSubmit: function() {
        var form = this.getComponent("form");
        var results = form.getForm().getValues();
        Ext.getBody().mask(RP.getMessage("rp.common.misc.SavingMaskText"));
        
        if (Ext.isEmpty(results.curPwd)) {
            form.getComponent("curPwd").markInvalid(RP.getMessage("rp.common.changepassword.EmptyError"));
            Ext.getBody().unmask();
            return;
        }
        if (Ext.isEmpty(results.newPwd)) {
            form.getComponent("newPwd").markInvalid(RP.getMessage("rp.common.changepassword.EmptyError"));
            Ext.getBody().unmask();
            return;
        }
        
        if (results.newPwd !== results.confirmPwd) {
            form.getComponent("confirmPwd").markInvalid(RP.getMessage("rp.common.changepassword.NewPasswordsDoNotMatch"));
            Ext.getBody().unmask();
            return;
        }
        
        logger.logTrace("[ChangePassword] Changing user password.");
        
        Ext.Ajax.request({
            url: RP.globals.getFullyQualifiedPath("PATH_TO_DATA_ROOT") + "changePassword",
            disableExceptionHandling: true,
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
            },
            params: Ext.urlEncode({
                oldPwd: results.curPwd,
                newPwd: results.newPwd
            }),
            scope: this,
            callback: this.onChangePasswordCallback
        });
    },
    
    onChangePasswordCallback: function(options, success, response) {
        var form = this.getComponent("form");
        var result = Ext.JSON.decode(response.responseText).status;
        var results = form.getForm().getValues();
        Ext.getBody().unmask();
        
        var msgBoxOptions = {
            modal: true,
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.ERROR,
            minWidth: 300,
            title: RP.getMessage("rp.common.changepassword.ErrorTitle")
        };
        
        switch (result) {
            case RP.REFSExceptionCodes.OK:
                logger.logTrace("[ChangePassword] Password change successful.");
                
                this.success = true;
                this.newPassword = results.newPwd;
                
                Ext.MessageBox.alert(RP.getMessage("rp.common.changepassword.FormTitle"), RP.getMessage("rp.common.changepassword.Success"), function() {
                    this.close();
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
                logger.logError("[ChangePassword] Unexpected error result: " + result);
                Ext.Msg.show(Ext.apply(msgBoxOptions, {
                    msg: RP.getMessage("rp.common.changepassword.GenericServerError")
                }));
        }
    }
});
//////////////////////
// ..\js\ui\CheckButtonItem.js
//////////////////////
Ext.define('RP.ui.CheckButtonItem', {
    extend: 'Ext.menu.CheckItem',
    alias: ['widget.menucheckbuttonitem'],

    renderTpl: [
        '<a id="{id}-itemEl" class="' + Ext.baseCSSPrefix + 'menu-item-link" href="{href}" <tpl if="hrefTarget">target="{hrefTarget}"</tpl> hidefocus="true" unselectable="on">',
            '<img id="{id}-iconEl" src="{icon}" class="' + Ext.baseCSSPrefix + 'menu-item-icon {iconCls}" />',
            '<span id="{id}-textEl" class="' + Ext.baseCSSPrefix + 'menu-item-text" <tpl if="arrowCls">style="margin-right: 17px;"</tpl> >{text}</span>',
            '<span id="{id}-buttonEl" class="' + Ext.baseCSSPrefix + 'menu-item-buttons"></span>',
            '<img id="{id}-arrowEl" src="{blank}" class="{arrowCls}" />',
        '</a>'
    ],

    initComponent: function() {
        this.callParent();
        this.addChildEls('buttonEl');
        this.on('afterrender', this._afterRender, this);
    },

    onClick: function(e) {
        if (e.getTarget('.x-btn') === null) {
            this.callParent([e]);
        }
    },

    _afterRender: function() {
        if (this.buttonsRendered !== true) {
            var items = [], pressed = true, id = this.getId();
            Ext.Array.each(this.buttons, function(btn) {
                items.push(Ext.apply(btn, {
                    xtype: 'button',
                    enableToggle: true,
                    toggleGroup: id + '-toggleGroup',
                    allowDepress: false,
                    parentOptionId: id,
                    pressed: pressed,
                    margin: '0 0 0 5'
                }));
                pressed = false;
            });
            if (items.length > 0) {
                Ext.create('Ext.container.Container', {
                    autoEl: 'span',
                    items: items,
                    renderTo: this.buttonEl,
                    autoRender: true
                });
            }
            this.buttonsRendered = true;
        }
    },

    getPressedButton: function() {
        var result = null,
            // DOMQuery is required since a checkitem isn't a container
            buttons = Ext.query('div:has(button)', this.buttonEl.dom),
            id = this.getId();
        Ext.each(buttons, function(btn) {
            var cmp = Ext.getCmp(btn.id);
            if (cmp.pressed === true) {
                result = cmp;
                return false;
            }
        });
        return result;
    }
});
//////////////////////
// ..\js\ui\ClickableToolTip.js
//////////////////////
/**
 * @class RP.ui.ClickableToolTip
 * @extends Ext.ToolTip
 * @deprecated
 *
 * A ToolTip extension allowing user interactivity.  The Tooltip
 * is only shown after a click event on an appropriate delegate
 * within the ToolTip's target.  Once shown, the ToolTip is hidden
 * by clicking the close button or clicking outside the ToolTip area.
 *
 * Since Ext.ToolTip is a descendant of Ext.Container, it supports
 * the items config, allowing child components.
 */
Ext.define("RP.ui.ClickableToolTip", {
    extend: "Ext.tip.ToolTip",
    
    /**
     * @cfg {String} delegate (Required) See Ext.ToolTip.delegate for details.
     */
    /**
     * @cfg {Function} handler (Required) See Ext.ToolTip.delegate for details.
     */
    autoWidth: true,
    
    initComponent: function() {
        RP.util.DeprecationUtils.logStackTrace(
            "[DEPRECATED] RP.ui.ClickableToolTip is deprecated. Please use RP.ui.ToolTip instead.");
        
        Ext.apply(this, {
            closable: true,
            autoHide: false,
            showDelay: 0,
            mouseDownXY: [null, null]
        });
        
        this.callParent(arguments);
    },
    
    /**
     * Custom override to try and keep the ToolTip within the screen.
     * @private
     */
    show: function() {
        this.callParent(arguments);
        
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
    setTarget: function(target) {
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
        
        if (this.ignoreEvent(e) || !(oldXY[0] == newXY[0] && oldXY[1] == newXY[1])) {
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
Ext.define("RP.ui.ComponentDataView", {
    extend: "Ext.view.View",
    alias: "widget.compdataview",
    
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
        this.callParent();
        this.components = new RP.collections.MixedCollection();
    },
    
    refresh: function() {
        this.components.clear();
        this.callParent();
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
        this.callParent(arguments);
        if (index > -1) {
            this.renderItems(index, index);
        }
    },
    
    // private
    onAdd: function(ds, records, index) {
        var count = this.all.getCount();
        this.callParent(arguments);
        if (count !== 0) {
            this.renderItems(index, index + records.length - 1);
        }
    },
    
    onClick: function(e) {
        var item = e.getTarget(this.itemSelector, this.getTemplateTarget());
        if (item) {
            var index = this.indexOf(item);
            
            // Get rid of any selected row styling and apply to newly clicked row
            var selectedItem = Ext.query(".comp-data-view-row-selected", this.getTemplateTarget().dom)[0];
            if (selectedItem) {
                Ext.fly(selectedItem).removeClass("comp-data-view-row-selected");
            }
            Ext.fly(item).addClass("comp-data-view-row-selected");
            
            if (this.onItemClick(item, index, e) !== false) {
                this.fireEvent("click", this, index, item, e);
            }
        }
        else {
            if (this.fireEvent("containerclick", this, e) !== false) {
                this.onContainerClick(e);
            }
        }
    },
    
    // private
    onRemove: function(ds, record, index) {
        this.destroyItems(index);
        this.callParent(arguments);
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
                c = items[j].render ? c = items[j].cloneConfig() : Ext.ComponentManager.create(items[j], this.defaultType);
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
        var componentToDestroy = this.components.removeAt(index);
        Ext.destroy(componentToDestroy);
    }
});
//////////////////////
// ..\js\ui\ComponentDataViewGrid.js
//////////////////////
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
Ext.define("RP.ui.ComponentDataViewGrid", {
    extend: "RP.ui.ComponentDataView",
    alias: "widget.compdataviewgrid",
    
    defaultType: "textfield",
    
    /**
     * Default class for the table
     */
    cls: "comp-data-view-grid",
    
    getLabelRenderer: function(dataIndex) {
        return function(component, target, record, rowIndex, colIndex, store) {
            component.setText(record.get(dataIndex));
        };
    },
    
    initComponent: function() {
        var template = new Ext.Template(
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
        '</table>');
        
        var headerTemplate = new Ext.Template('<th style="white-space:nowrap;{style}"><div {tooltip}>{headerData}</div></th>');
        var rowTemplate = new Ext.Template('<td class="col{columnNum}" width="{width}" style="{style}">{rowData}</td>');
        this.itemSelector = 'tr.comp-data-view-row';
        var columns = this.initialConfig.items, tplHeader = '', tplBody = '';
        
        for (var i = 0; i < columns.length; i++) {
            var column = columns[i];
            if (column.hidden) {
                continue;
            }
            
            var tooltip = "";
            if (column.tooltip) {
                tooltip = "ext:qtip='" + column.tooltip + "'";
            }
            
            var tpldata = '';
            if (column.dataIndex) {
                column.xtype = "label";
                if (!column.renderer) {
                    column.renderer = this.getLabelRenderer(column.dataIndex);
                }
            }
            
            tplHeader += headerTemplate.apply({
                headerData: column.header,
                style: column.style,
                tooltip: tooltip
            });
            
            if (this.initialConfig.autoExpandColumn === column.colID) {
                tplBody += rowTemplate.apply({
                    columnNum: i,
                    width: "100%",
                    style: column.style,
                    rowData: tpldata
                });
            }
            else {
                tplBody += rowTemplate.apply({
                    columnNum: i,
                    style: column.style,
                    rowData: tpldata
                });
            }
            
            column.renderTarget = "td.col" + i;
        }
        
        this.tpl = template.apply({
            templateHeader: tplHeader,
            templateBody: tplBody
        });
        
        this.callParent();
    }
    
});
//////////////////////
// ..\js\ui\LiveSearchBox.js
//////////////////////
/**
 * Override of {@link Ext.form.ComboBox} which defaults and sets many of the 
 * fields needed to implement the live search feature. By default the control
 * will send the following arguments to the server-side:<ul>
 * <li>query - the field to filter by.</li>
 * <li>start - the start index (used for paging).</li>
 * <li>limit - number of results to return (can also be used with paging).</li>
 *
 * This component is slight extension from the one found [here][1].
 * 
 * [1]: http://www.extjs.com/deploy/dev/examples/form/forum-search.html
 *
 * @xtype livesearchbox
 */
Ext.define("RP.ui.LiveSearchBox", {
    extend: "Ext.form.ComboBox",
    alias: "widget.livesearchbox",
    
    listConfig: {
        /**
         * @cfg {String} itemSelector CSS selector for the item used to display
         * the data for the drop down. Defaults to div.livesearch-item
         * @private
         */
        itemSelector: "div.livesearch-item"
    },
    
    /**
     * @cfg {Number} minChars The minimum number of characters needed for
     * autoComplete and typeAhead to activate. Defaults to 2.
     */
    minChars: 2,
    
    /**
     * Getter for the inner template
     * 
     * @return {String/Ext.XTemplate} The template string, or Ext.XTemplate instance
     * to use to display each item in the dropdown list.
     */
    getInnerTpl: function() {
        return '<tpl for="."><div class="livesearch-item">{item}</div></tpl>';
    },
    
    /**
     * @private
     */
    initComponent: function() {
        this.addEvents(        
        /**
         * @event update
         * Fired when there is a new selection.
         * @param {RP.ui.LiveSearchBox} this
         * @param {Ext.data.Record} record
         */
        "update");
        
        // force these settings...
        Ext.apply(this, {
            enableKeyEvents: true,
            hideTrigger: true,
            typeAhead: false,
            triggerAction: "query"
        });
        
        this.listConfig.loadingText = this.listConfig.loadingText ? this.listConfig.loadingText : RP.getMessage("rp.common.misc.LoadingMaskText");
        
        // if using a pager, default the width
        if (this.initialConfig.pageSize) {
            this.pageSize = this.initialConfig.pageSize;
            this.width = this.initialConfig.width ? this.initialConfig.width : 230;
        }
        
        // hide the search results when the query is deleted
        this.on("keyup", function() {
            if (Ext.isEmpty(this.getRawValue())) {
                this.collapse();
            }
        }, this);
        
        this.callParent();
    },

    /**
     * Sets the LiveSearchBox's value to the item selected by the user
     * 
     * @param {Ext.data.Record} record The record selected
     * @private
     */
    onSelect: function(record) {
        // only set the selected row if the value has not changed
        if (this.lastQuery == this.getRawValue()) {
            this.setValue(record.data.item);
            this.collapse();
            this.fireEvent("update", this, record);
        }
    },
    
    /**
     * Retrieve the selected record from the search selection.
     * 
     * @return {Ext.data.Record} The record object currently selected or null
     * if no selection present.
     */
    getSelectedRecord: function() {
        var rows = this.view.getSelectedRecords();
        
        if (rows.length > 0) {
            return rows[0];
        }
        return null;
    }
});
//////////////////////
// ..\js\ui\TimeField.js
//////////////////////
/**
 * An Ext.form.TextField extension for entering time values. Will automatically 
 * attempt to parse and format the time value on the beforeBlur event.
 */
Ext.define("RP.ui.TimeField", {
    extend: "Ext.form.field.Text",
    
    alias: "widget.rpuitimefield",
    
    mixins: {
        timeControl: "RP.ui.Mixins.TimeControl"
    },
    
    
    initComponent: function() {
        
        // initialize the mixins
        this.mixins.timeControl.constructor.call(this, this.initialConfig);
        this.typedValue = null;
        
        if (!this.initialConfig.emptyText) {
            Ext.apply(this, {
                emptyText: RP.getMessage("rp.common.misc.EmptyTimeText")
            });
        }
        
        this.addEvents(
        /**
         * @event beforechange
         * Triggered at the same time as "blur". 
         */
        "beforechange");
        this.on("blur", function(e) {
            this.fireEvent("beforechange");
        });
        
        this.callParent(arguments);
    },
    
    /**
     * Parses the entered value. Override to change how the entered value 
     * is entered into the field's time format.
     */
    beforeBlur: function() {
        var value = this.parseValue(this.getRawValue());
        if (value) {
            this.typedValue = this.getRawValue();
            this.setRawValue(this.formatTime(value));
        }
    }
    
});
//////////////////////
// ..\js\ui\SessionExpiredDialog.js
//////////////////////
/**
 * @class RP.ui.SessionExpiredDialog
 * @extends Ext.Window
 *
 * A dialog window that is displayed when the user's session has
 * expired.  The user then has the ability to either go back to the
 * login page, or attempt to log back in.
 */
Ext.define("RP.ui.SessionExpiredDialog", {
    extend: "Ext.window.Window",
    
    id: "sessionExpiredDialog",
    
    /**
     * @private
     */
    initComponent: function() {
        // Fire session expired event
        RP.event.AppEventManager.fire(RP.upb.AppEvents.SessionExpired, {});
        
        // allows enter key to submit the login form
        var enterKeyFn = Ext.bind(function(field, e) {
            if (e.getKey() == e.ENTER) {
                this.submit();
            }
        }, this);
        
        this.msgBoxOptions = this._createMsgBoxOptions();
        
        // handler used to submit the login form
        Ext.apply(this, {
            title: RP.getMessage("rp.common.login.InactivityTimeoutTitle"),
            width: 450,
            draggable: false,
            closable: false,
            resizable: false,
            bodyPadding: 20,
            bodyStyle: "background-color: #fff",
            items: [{
                xtype: "box",
                style: "font-size: 12px;",
                html: RP.getMessage("rp.common.login.InactivityTimeout")
            }, {
                xtype: "form",
                itemId: "form",
                style: "margin: 20px auto",
                fieldDefaults: {
                    labelWidth: 75
                },
                defaults: {
                    labelStyle: "font-weight: bold"
                },
                width: 270,
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
                    handler: Ext.bind(this.submit, this)
                }, {
                    itemId: 'btnCancel',
                    text: RP.getMessage("rp.common.login.CancelButton"),
                    handler: RP.util.Helpers.logout
                }]
            }]
        });
        
        this.callParent(arguments);
    },
    
    destroy: function() {
        Ext.EventManager.removeResizeListener(this.center, this);
        
        this.callParent(arguments);
    },
    
    show: function() {
        Ext.EventManager.onWindowResize(this.center, this);
        
        this.callParent(arguments);
        
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
            msg: RP.getMessage("rp.common.misc.PleaseWait"),
            style: {
                'z-index' : '100000 !important'
            }
        });
        
        this.loadMask.show();
        
        logger.logTrace("[SessionExpiredLoginDialog] Submitting reauthentication request.");
        
        // perform a request to attempt to re-authenticate the user
        Ext.Ajax.request({
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
            var r = Ext.JSON.decode(response.responseText);
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
                logger.logTrace("[SessionExpiredLoginDialog] Reauthentication failed, attempting to use the standard login service instead.");
                
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
        
        Ext.Ajax.request({
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
            result = Ext.JSON.decode(response.responseText);
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
            RP.util.Helpers.reload();
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
 * @class RP.ui.SmartTimeLogic
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
                stopTimeField.setTheTime(RP.Date.addHours(stopTime, MERIDIEM_BOUNDARY));
            }
            // Check to see if there are minutes separating the hours and the hours
            // and it's in the PM, we care about this because the TimeField will default
            // to AM so we need to correct it to PM. 
            else 
                if (stopMinutes > startMinutes && isPM(startHours, MERIDIEM_BOUNDARY)) {
                    stopTimeField.setTheTime(RP.Date.addHours(stopTime, MERIDIEM_BOUNDARY));
                }
        }
        // Check for case when we want to adjust from AM to PM (e.g. start: 8am, end: 5(am))
        else 
            if (stopHours < startHours) {
                if (isAM(startHours, MERIDIEM_BOUNDARY)) {
                    stopTimeField.setTheTime(RP.Date.addHours(stopTime, MERIDIEM_BOUNDARY));
                }
                // Check if we aren't crossing an AM/PM boundary when in the PM 
                // (e.g. start: 1pm, end 10(am) -> 10pm)
                else 
                    if (startHours - MERIDIEM_BOUNDARY < stopHours) {
                        stopTimeField.setTheTime(RP.Date.addHours(stopTime, MERIDIEM_BOUNDARY));
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
/**
 * A store-bound TimeTextField.
 */
Ext.define("RP.ui.StoreBoundTimeTextField", {
    alias: "rpstoreboundtimetextfield",
    extend: "Ext.form.TextField",
    
    initComponent: function() {
        var config = this.initialConfig;
        
        this.on("change", function(field, newVal, oldVal) {
            var baseDateDelegate = Ext.bind(config.baseDateHandler, config.scope || this, [config.record, config.fieldName]);
            var baseDate = baseDateDelegate();
            
            var fieldDate = null;
            if (newVal) {
                try {
                    fieldDate = IntervalJN.parseTimeAfterBase(newVal, baseDate);
                } 
                catch (e) {
                    Ext.Msg.alert('', 'Please enter a valid time.');
                    this.setRawValue(oldVal);
                    return;
                }
            }
            
            var dummyDate = new Date();
            var curDate = config.record.get(config.fieldName);
            
            if (newVal === '' && !curDate) {
                this.setRawDate(null, this.initialConfig.emptyText);
                return;
            }
            
            // find out if the field is different from what's in the store.
            if (!RP.Date.equals((curDate || dummyDate), (fieldDate || dummyDate))) {
                config.record.set(config.fieldName, fieldDate);
                this.addCls("rp-dirty-cell");
            }
            
            this.setRawDate(fieldDate);
        }, this);
        
        Ext.apply(this, config);
        
        if (!config.emptyText) {
            config.emptyText = RP.getMessage("rp.common.misc.EmptyTimeText");
        }
        
        this.callParent();
    },
    
    /**
     * Sets the new value.
     * @param {Object} v
     * @return {Number}
     */
    setValue: function(v) {
        this.callParent(arguments);
        this.fireEvent('change', this, v);
    },
    
    /**
     * Sets the raw date.
     * @param {Object} fieldDate
     * @param {Object} defaultText
     */
    setRawDate: function(fieldDate, defaultText) {
        if (fieldDate) {
            this.setRawValue(RP.Date.formatTime(fieldDate, this.initialConfig.timeFormat));
        }
        else if (defaultText) {
            this.setRawValue(defaultText);
        }
        else {
            this.setRawValue("");
        }
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\StoreBinder.js
//////////////////////
/**
 * The StoreBinder used internally by the StoreBoundComponents.
 */
Ext.define("RP.ui.StoreBinder", {
    extend: "Ext.Component",
    
    _store: null,
    _record: null,
    _valueField: null,
    _emptyText: null,
    _control: null,
    
    _dirtyCellCls: "rp-dirty-cell",
    
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
                RP.throwError('fieldname is required for a store bound control.');
            }
        }
        
        if (!this._store && !this._record) {
            RP.throwError('either store or record must be provided for a store bound control');
        }
        
        if (this._store) {
            this._store.on('load', this.onStoreLoaded, this);
            // reg failure event
        }
        
        if (!this._control) {
            RP.throwError('store bound control requires a control to be set');
        }
        
        this._oldControlSetValue = this._control.setValue;
        this._control.setValue = function(newVal) {
            this._storeBinder.setValue(newVal);
        };
        
        if (Ext.isFunction(this._control.initValue)) {
            // This is so that the dirty flag isn't potentially set on initial value set
            // on pre-populated stores...
            this._control.initValue = Ext.Function.createSequence(this._control.initValue, this.removeDirtyFlag, this);
        }
        
        this._oldControlClearValue = this._control.clearValue;
        this._control.clearValue = function() {
            this._storeBinder.clearValue();
        };
        
        this._control.on('change', this.onControlChange, this);
        
        if (this._store.getCount() > 0) {
            this.onStoreLoaded();
        }
        
        this.callParent(arguments);
    },
    
    getStoreValue: function() {
        return this._record.get(this._valueField);
    },
    
    /**
     * Sets the StoreBinder's value.
     * @param {Object} newVal The new value.
     */
    setValue: function(newVal) {
        this.onControlChange(this._control, newVal, this._control.getValue());
    },
    
    clearValue: function() {
        if (this._oldControlClearValue) {
            this._oldControlClearValue.apply(this._control, []);
        }
        this._control.setRawValue('');
        this.removeDirtyFlag();
    },
    
    onStoreLoaded: function(/*store, records, options*/) {
        if (this._store.getCount()) {
            this._record = this._store.getAt(0);
            this.setValue(this._record.get(this._valueField));
            this.removeDirtyFlag();
        }
    },
    
    /**
     * @private
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
            areDifferent = !RP.Date.equals(curVal, parsedVal);
        }
        else {
            areDifferent = curVal !== parsedVal;
        }
        
        if (this._record && areDifferent) {
            this._record.set(this._valueField, parsedVal);
            if (formattedVal !== oldVal) {
                this.addDirtyFlag();
            }
        }
        
        if (formattedVal === "" && this._emptyText) {
            formattedVal = this._emptyText;
        }
        
        this._oldControlSetValue.apply(this._control, [formattedVal]);
    },
    
    addDirtyFlag: function() {
        if (this._control.inputEl) {
            this._control.inputEl.addCls(this._dirtyCellCls);
        }
    },
    
    removeDirtyFlag: function() {
        if (this._control.inputEl) {
            this._control.inputEl.removeCls(this._dirtyCellCls);
        }
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\TimeField.js
//////////////////////
Ext.define("RP.ui.StoreBoundTimeField", {
    alias: "widget.rpstoreboundtimefield",
    extend: "RP.ui.TimeField",
    
    _storeBinder: null,
    
    initComponent: function() {
        this.initialConfig.control = this;
        this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);
        
        this.initialConfig.storeBindings.store.on("load", function(store, recs) {
            this.setTheDate(this._storeBinder.getStoreValue());
        }, this);
        
        this.callParent();
    },
    
    formatValue: function(newVal) {
        if (typeof(newVal) === "string") {
            return newVal;
        }
        
        if (newVal) {
            return RP.Date.formatTime(newVal, this._format);
        }
        else {
            return "";
        }
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\TextField.js
//////////////////////
Ext.define("RP.ui.StoreBoundTextField", {
    alias: "rpstoreboundtextfield",
    extend: "Ext.form.TextField",
    
    _storeBinder: null,
    
    initComponent: function() {
        this.initialConfig.control = this;
        this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);
        
        this.callParent();
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\TextArea.js
//////////////////////
Ext.define("RP.ui.StoreBoundTextArea", {
    alias: "widget.rpstoreboundtextarea",
    extend: "Ext.form.TextArea",
    
    _storeBinder: null,
    
    initComponent: function() {
        this.initialConfig.control = this;
        this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);
        
        this.callParent();
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\DateField.js
//////////////////////
Ext.define("RP.ui.StoreBoundDateField", {
    alias: "widget.rpstorebounddatefield",
    extend: "Ext.form.field.Date",
    
    _storeBinder: null,
    
    initComponent: function() {
        this.initialConfig.control = this;
        this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);
        
        this._format = this.initialConfig.format || RP.core.Formats.Date.Medium;
        
        if (!this.initialConfig.emptyText) {
            Ext.apply(this, {
                emptyText: RP.getMessage("rp.common.misc.EmptyDateText")
            });
        }
        
        this.callParent();
    },
    
    formatValue: function(newVal) {
        if (Ext.isString(newVal)) {
            if (Ext.isEmpty(newVal)) {
                return "";
            }
            return RP.Date.formatDate(RP.Date.clearTime(new Date(newVal)), this._format);
        }
        else if (newVal) {
            return RP.Date.formatDate(RP.Date.clearTime(newVal), this._format);
        }
        else {
            return "";
        }
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\DisplayField.js
//////////////////////
Ext.define("RP.ui.StoreBoundDisplayField", {
    alias: "rpstorebounddisplayfield",
    extend: "Ext.form.DisplayField",
    
    _storeBinder: null,
    
    initComponent: function() {
        this.initialConfig.control = this;
        this._storeBinder = new RP.ui.StoreBinder(this.initialConfig);
        
        this.callParent();
    }
});
//////////////////////
// ..\js\ui\StoreBoundComponents\ComboBoxStoreBinder.js
//////////////////////
Ext.define("RP.ui.ComboBoxStoreBinder", {
    extend: "RP.ui.StoreBinder",
    
    _displayField: null,
    
    initComponent: function() {
        var storeBindings = this.initialConfig.storeBindings;
        this._displayField = storeBindings.displayField || null;
        
        this.callParent(arguments);
    },
    
    onControlChange: function(field, newVal, oldVal) {
        this.callParent(arguments);
        
        if (this._displayField && this._control.store && this._control.valueField && this._control.displayField) {
            var displayVal = null;
            
            if (this._control.store.getCount() === 0) {
                displayVal = this._record.get(this._displayField);
                this._control.setRawValue(displayVal);
                this._control.lastSelectionText = displayVal;
                return;
            }
            
            var recIndex = this._control.store.find(this._control.valueField, newVal);
            if (recIndex >= 0) {
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
Ext.define("RP.ui.StoreBoundComboBox", {
    alias: "widget.rpstoreboundcombobox",
    extend: "Ext.form.ComboBox",
    
    _storeBinder: null,
    
    previousSelectedValue: null,
    
    initComponent: function() {
        this.initialConfig.control = this;
        if (this.initialConfig.storeBindings) {
            this._storeBinder = new RP.ui.ComboBoxStoreBinder(this.initialConfig);
            
            var okToLookup = false;
            if (this.initialConfig.storeBindings.store) {
                this.initialConfig.storeBindings.store.on("load", function(store, recs) {
                    if (recs.length > 0) {
                        this.previousSelectedValue = recs[0].get(this.initialConfig.valueField);
                        okToLookup = false;
                    }
                }, this);
            }
        }
        
        this.on("beforeselect", function(combo, rec, index) {
            if (okToLookup) {
                this.previousSelectedValue = this.getValue();
            }
            else {
                okToLookup = true;
            }
        }, this);
        
        this.callParent();
    }
});
//////////////////////
// ..\js\ui\ToolTip.js
//////////////////////
/**
 * @class RP.ui.ToolTip
 * @extends Ext.tip.ToolTip
 *
 * A ToolTip extension allowing more flexibility when it comes to user interactivity.  
 * Adds a mode where the Tooltip is only shown after a click event on an appropriate 
 * delegate within the ToolTip's target.  Once shown, the ToolTip is hidden
 * by clicking the close button or clicking outside the ToolTip area.
 */
Ext.define("RP.ui.ToolTip", {
    extend: "Ext.tip.ToolTip",

    /**
     * @cfg {Number} anchorOffset 
     * When used in conjunction with useArrow, sets the anchor arrow's offset 
     * from the tooltip's left edge. This option is disabled by default, with the
     * anchor being dynamically positioned based on the position of the tooltip
     * and its target. To enable manual, fixed offsets, useDynamicOffset must be false.
     */

    /**
     * @cfg {String / String[]} mode
     * Array detailing the way(s) in which users can interact with the tooltip. Acceptable values are:
     *
     *   - **`'hover'`** : Default behavior inherited from Ext.tip.ToolTip. The tooltip
     *   will display when the cursor hovers over target element(s) on the page and
     *   disappear on mouse out.
     *
     *   - **`'click'`** : The tooltip will only display when a user clicks on the
     *   target element(s). In this case, the tooltip will appear as a closable panel
     *   and will remain visible until the panel's close button is clicked or a click
     *   is registered outside of the tooltip container and its target element(s).
     */
    mode: ['hover'],

    /**
     * @cfg {Boolean} useArrow
     * True to display an anchored arrow, false to hide it.
     */
    useArrow: true,

    /**
     * @cfg {Boolean} useDynamicOffset
     * Determines whether the tooltip's anchor is positioned dynamically based
     * on the position of the tooltip and its target element. If true, any
     * manually set value of anchorOffset will be ignored.
     */
    useDynamicOffset: true,

    buttonAlign: 'center',
    maxWidth: undefined,
    shadow: false,
    
    initComponent: function() {
        // An assumption is being made here that if items are passed in then
        // less padding is needed.  The design pattern is if there is a grid
        // in the tip then a small amount of padding is needed.  If the tip
        // is composed of more like a sentence or something like that then
        // we want a larger set of padding.  Likely, the sentence scenario
        // will use html or tpl to configure which is our hint to make less
        // padding.
        if(this.items && this.items.length > 0) {
            this.addCls('complex-tip');
        } else {
            this.addCls('simple-tip');
        }

        this._setMode(this.mode);
        this._setUseArrow(this.useArrow);
        this.callParent(arguments);

        this.on('show', this._onTooltipShow, this);
        this.on('hide', this._onTooltipHide, this);
    },

    _setUseArrow: function(useArrow) {
        this.useArrow = useArrow;
        if (this.useArrow) {
            this.on('show', this._onBeforeShowSingle, this, { single: true });
        }
        else {
            this.un('show', this._onBeforeShowSingle, this, { single: true });
        }
    },

    _setMode: function(mode) {
        if(!Ext.isArray(mode)) {
            this.mode = [mode];
        }

        if(this.supportsMode('click')) {
            Ext.apply(this, {
                mouseDownXY: [null, null],
                showDelay: 0,
                trackMouse: false
            });

            if(!this.supportsMode('hover')) {
                Ext.apply(this, {
                    autoHide: false
                });
            }
        }
    },

    _onBeforeShowSingle: function() {
        if(this.anchorEl) {
            this.anchorEl.setStyle({
                visibility: 'visible'
            });
        }
    },
    
    /**
     * Handler for this component's show event.
     * Masks the background (avoiding an issue with clicking away 
     * causing new click activity and scrolling issues),
     * and keeps the ToolTip within the screen.
     * Exists as a listener instead of a show() override so that beforeshow
     * will still be respected. (i.e. We don't want to mask if the show is canceled.)
     */
    _onTooltipShow: function() {
        var body = Ext.getBody();
        // Mask the background, and use the mask's zIndex to
        // make sure the tooltip will be in the foreground.
        var maskZIndex = body.mask().addCls('x-transparent-mask').getPositioning()['z-index'],
            currentZIndex = this.getEl().getStyle('z-index'),
            newIdx = Ext.Number.from(maskZIndex) + 1;

        if (newIdx > currentZIndex) {
            // Update the seed used by the zIndexManager so that any subsequent attempts to bring
            // the tooltip or its children to the front will assign the correct zIndex.
            this.zIndexManager.zseed = newIdx;
            // Update the tooltip's z-index to be in front of the body mask.
            this.getEl().setStyle('z-index', newIdx);
        }

        // Adjust positioning if off-screen
        var box = this.getBox();
        if (box.x + box.width > body.getWidth()) {
            var x = body.getWidth() - (box.width + 10);
            this.setPagePosition(x, box.y);
        }
    },

    _onTooltipHide: function() {
        Ext.getBody().unmask();
    },

    /**
     * @private
     * Removed left / right support. Tooltips are now restricted to rendering
     * either above or below the target.
     */
    getTargetXY: function() {
        var me = this,
            mouseOffset,
            offsets, xy, dw, dh, de, bd, scrollX, scrollY, axy, sz, constrainPosition;
        if (me.delegate) {
            me.anchorTarget = me.triggerElement;
        }
        if (me.anchor) {
            me.targetCounter++;
            offsets = me.getOffsets();
            xy = (me.anchorToTarget && !me.trackMouse) ? me.el.getAlignToXY(me.anchorTarget, me.getAnchorAlign()) : me.targetXY;
            dw = Ext.Element.getViewWidth() - 5;
            dh = Ext.Element.getViewHeight() - 5;
            de = document.documentElement;
            bd = document.body;
            scrollX = (de.scrollLeft || bd.scrollLeft || 0) + 5;
            scrollY = (de.scrollTop || bd.scrollTop || 0) + 5;
            axy = [xy[0] + offsets[0], xy[1] + offsets[1]];
            sz = me.getSize();
            constrainPosition = me.constrainPosition;

            me.anchorEl.removeCls(me.anchorCls);

            if (me.targetCounter < 2 && constrainPosition) {
                // OVERRIDE - Removed conditional checks for left / right tooltip alignment.
                if (axy[1] < scrollY) {
                    if (me.anchorToTarget) {
                        me.defaultAlign = 't-b';
                        if (me.mouseOffset) {
                            me.mouseOffset[1] *= -1;
                        }
                    }
                    me.anchor = 'top';
                    return me.getTargetXY();
                }
                if (axy[1] + sz.height > dh) {
                    if (me.anchorToTarget) {
                        me.defaultAlign = 'b-t';
                        if (me.mouseOffset) {
                            me.mouseOffset[1] *= -1;
                        }
                    }
                    me.anchor = 'bottom';
                    return me.getTargetXY();
                }
            }

            me.anchorCls = Ext.baseCSSPrefix + 'tip-anchor-' + me.getAnchorPosition();
            me.anchorEl.addCls(me.anchorCls);
            me.targetCounter = 0;

            if(me.renderTo) {
                var cushion = 8;

                // make sure that the tooltip isn't aligned to the left edge of the render element.
                var offsetLeft = me.renderTo.dom.offsetLeft + cushion;
                if(offsetLeft && axy[0] <= offsetLeft) {
                    axy[0] = offsetLeft;
                }

                // make sure that the tooltip isn't aligned to the right edge of the render element.
                var rightEdgeRenderTo = me.renderTo.dom.offsetLeft + me.renderTo.dom.offsetWidth - cushion;
                var rightEdgeTip = me.getWidth() + axy[0];
                if(rightEdgeRenderTo < rightEdgeTip) {
                    axy[0] = rightEdgeRenderTo - me.getWidth();
                }
            }

            return axy;
        } else {
            mouseOffset = me.getMouseOffset();
            return (me.targetXY) ? [me.targetXY[0] + mouseOffset[0], me.targetXY[1] + mouseOffset[1]] : mouseOffset;
        }
    },

    /**
     * @private
     * The tooltip is always too high when it's above or below a row so we are pushing it down by 3 pixels.
     */
    getOffsets: function() {
        var offsets = this.callParent(arguments);

        offsets[1] = offsets[1] + 3;

        return offsets;
    },

    /**
     * @private
     * Tooltips are centered by default, instead of aligning to corners.
     * Removed left / right support.
     */
    getAnchorAlign: function() {
        return (this.anchor === 'top') ? 't-b' : 'b-t';
    },

    /**
     * @private
     * Overrides base Ext method so that the anchor position is dynamic based
     * on the target element's location.
     */
    syncAnchor: function() {
        var targetEl = Ext.get(this.anchorTarget),
            min, max, offset;

        if(this.useDynamicOffset) {
            // Ext adds a magic 20 to center the arrow, but our align
            // of t-b and b-t above means that 20 is no longer needed.
            offset = targetEl.getX() + (targetEl.getWidth() / 2) - (20 + this.el.getX());
            // reserve 40px for arrow plus 4px for rounded corners
            max = this.el.getWidth() - 44; 
            // reserve 4px for rounded corner
            min = 4;

            // We don't want to ever show the arrow even partially disconnected from
            // the tooltip so we have to make sure the offset is never large or small
            // enough to have that happen. Ext will add 20 in the superclass sync
            // logic to account for the arrows width. We don't need that since we
            // are already centering everything.
            this.anchorOffset = Ext.Number.constrain(offset, min, max);
        }
        
        this.callParent(arguments);
    },

    supportsMode: function(mode) {
        return this.mode.indexOf(mode) !== -1;
    },
    
    /**
     * @param {Mixed} target
     * @private
     */
    setTarget: function(target) {
        if(this.supportsMode('hover')) {
            this.callParent(arguments);

            if(!this.supportsMode('click')) {
                return;
            }
        }

        var t;
        if ((t = Ext.get(target))) {

            if(this.target !== t) {
                this.targetChanged = true;
            }

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
            this.anchorTarget = t;
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
        
        if (this.ignoreEvent(e) || !(oldXY[0] == newXY[0] && oldXY[1] == newXY[1])) {
            return;
        }
        
        var t = e.getTarget(this.delegate);
        if (t) {
            var targetEl = Ext.get(t);

            this.triggerElement = t;
            this.anchorTarget = this.anchorTarget || targetEl;
            this.targetXY = newXY;

            // Toggle the tooltip so the click target causes the tip to show/hide
            if(this.isHidden()) {
                this.show();
            } else {
                this.hide();
            }
        }
    },
    
    /**
     * @param {Ext.EventObject} e
     * @private
     */
    onDocMouseDown: function(e) {
        if (!this.eventWithinTooltip(e)) {
            this.disable();
            Ext.defer(this.doEnable, 100, this);
        }
    },

    /**
     * Checks if the event occurred within the tooltip or any components contained
     * within the tooltip. This should also detect events that occur within floating
     * elements that are owned by components contained within the tooltip.
     *
     * @param {Ext.EventObject} event The event
     * @return {Boolean} TRUE if the event occured somewhere within the tooltip or
     * one of it's nested components. FALSE otherwise.
     */
    eventWithinTooltip: function(event) {
        var owningComponent;

        if (event.within(this.el.dom)) {
            return true;
        }

        owningComponent = RP.util.Component.getComponentFromElement(
            Ext.get(event.getTarget()));
        
        if (owningComponent) {
            return owningComponent.up('[id=' + this.id + ']') || owningComponent.floatParent === this;
        }

        return false;
    },

    /**
     * Show as if the event is its own target click.  This
     * can be used instead of defining targets and delegates
     * for a tooltip to just show from a particular "click" event.
     * @param event [description]
     */
    showFromClickEvent: function(event) {
        var target = event.getTarget();

        this.setTarget(target);
        this.mouseDownXY = event.getXY();
        this.anchorTarget = target;

        if(this.targetChanged) {
            this.onTargetMouseUp(event);
        }

        this.targetChanged = false;
    }
});
//////////////////////
// ..\js\ui\GridRowStatus.js
//////////////////////
Ext.ns("RP.ui");

/**
 * @class RP.ui.GridRowStyle
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
/**
 * Extends the standard Ext GridView to perform additional things such as
 * styling a row to indicate it as was changed, etc.
 */
Ext.define("RP.ui.GridView", {
    extend: "Ext.grid.View",
    alias: "widget.rpgridview",
    
    /**
     * Override to apply empty text if store is already loaded...  The base 'deferEmptyText'
     * option could be used, but that doesn't take into account whether or not the store
     * is already loaded...
     * @method
     */
    afterRender: function() {
        this.callParent(arguments);
        
        if (this.getStore().isLoaded) {
            this.applyEmptyText();
        }
        
        // for state stuff.
        this.up("gridpanel").initState(); // grid!!!!
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
        
        var status = this.getRowStatusFn ? this.getRowStatusFn(record, index) : RP.ui.GridRowStyle.getDefaultStatus();
        var cls = (this.getRowStyleClassFn || defaultRowStyleClassFn)(status, record);
        
        return cls + (this.extraRowCls ? (" " + this.extraRowCls) : "");
    },
    
    
    /**
     * Override of parent function. Hides the header if there is no text to display.
     * @private
     */
    applyEmptyText: function() {
        this.callParent(arguments);
        
        if (this.emptyText && !this.hasRows()) {
            this.mainHd.hide(false); //exist??
        }
        else {
            this.mainHd.show(false);
        }
    },
    
    // private
    renderRowsForExport: function(startRow, endRow, includeColumnDelegate) {
        // pull in all the crap needed to render rows
        var dataStore = this.getStore(), stripe = this.stripeRows, headerCt = this.getHeaderCt(), colCount = headerCt.getColumnCount();
        
        if (dataStore.getCount() < 1) {
            return '';
        }
        
        var columnData = this.getGridColumns();
        
        startRow = startRow || 0;
        endRow = !Ext.isDefined(endRow) ? dataStore.getCount() - 1 : endRow;
        
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
        if(Ext.isDefined(dataStore.groupField)) {
            groupField = dataStore.groupers.get(0).property;
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
                    if (Ext.isDefined(dataStore.groupField)) {
                        var groupValue = record.data[groupField];
                        if (Ext.isDefined(groupValue) && groups.indexOf(groupValue) === -1) {
                            groups.push(groupValue);
                            rows.push(Ext.String.format("\"{0}\"", groupValue));
                        }
                    }
                    var columnString = record.data[column.dataIndex];
                    if (typeof column.renderer === "function") {
                        columnString = column.renderer.call(column.scope, record.data[column.dataIndex], column, record, i, dataStore, this);
                        var div = document.createElement("div");
                        div.innerHTML = columnString;
                        columnString = div.textContent || div.innerText || "";
                    }
                    if (typeof columnString === "string") {
                        columnString = columnString.replace(/\"/g, "\\\"");
                    }
                    row.push(Ext.String.format("\"{0}\"", columnString));
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
// ..\js\ui\GridPanel.js
//////////////////////
/**
 * @class RP.ui.GridPanel
 * @extends Ext.grid.GridPanel
 * <p>An RP extension of the {@link #Ext.grid.GridPanel} providing additional functionality
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
Ext.define("RP.ui.GridPanel", {
    extend: "Ext.grid.Panel",
    
    alias: "widget.rpgridpanel",
    
    /**
     * @cfg {Boolean} stripeRows See {@link Ext.grid.GridPanelstripeRows}, defaulted to true.
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
     * @cfg todo
     */
    viewType: "rpgridview",
    
    /**
     *
     */
     tooltipClass: "Ext.ToolTip",
    
    /**
     * @cfg {Object} tooltipConfig An optional configuration object used when creating the grid's
     * internal tooltip property, supports all configurations specified by {@link Ext.ToolTip}.
     */
    // private
    initComponent: function() {

        // Store off a reference to the original column config since the Ext.grid.Table will overwrite it
        // with the headerCnt.items.items...
        if(this.columns) {
            this.columnConfig = this.columns;
        }
        
        if (this.statusIcon === true) {
            this._attachIconColumn();
            
            // Listen to reconfigure event to add icon column again if cm changes.   
            this.on("reconfigure", this._attachIconColumn, this);
            
        }
        
        var toolbar = this.tbar;
        
        // gridView and gridViewConfig are supported here for backwards compatibility
        this.viewConfig = this.gridViewConfig || this.viewConfig || {};
        Ext.applyIf(this.viewConfig, {
            store: this.store
        });
        
        // If Exports, Print, or Refresh enabled...
        if (this.enableExport || this.enableExportPdf || this.enablePrint || this.enableRefresh) {
            // Create the necessary tools (icons in toolbar).
            var tools = [];
            
            if (this.enableRefresh) {
                tools.push({
                    xtype: "tool",
                    type: "refresh",
                    handler: this.reloadGrid,
                    scope: this
                });
            }
            
            if (this.enableExport) {
                tools.push({
                    xtype: "tool",
                    type: "save",
                    handler: this.exportGridAsCSV,
                    scope: this
                });
            }
            
            if (this.enableExportPdf) {
                tools.push({
                    xtype: "tool",
                    type: "pdf",
                    handler: this.exportGridAsPDF,
                    scope: this
                });
            }
            
            if (this.enablePrint) {
                tools.push({
                    xtype: "tool",
                    type: "print",
                    handler: this.printGrid,
                    scope: this
                });
            }
            
            // If the grid already has a toolbar, add it to the "Right" panel, else create
            // a brand new toolbar to host the tools.
            if (toolbar) {
                // Verify it is an instance of RP.ui.Toolbar and add the tool(s) to the right.
                if (typeof(toolbar.addItemsToRightBar) !== "function") {
                    RP.throwError("Toolbar type is not supported.  Toolbar must have 'addItemsToRightBar' function.");
                }
                toolbar.addItemsToRightBar(tools);
            }
            else {
                toolbar = new RP.ui.Toolbar({
                    right: {
                        items: tools
                    }
                });
            }
        }
        
        Ext.applyIf(this, {
            tbar: toolbar
        });
        
        // since Ext.grid.GridPanel's prototype sets the view to null, applyIf
        // thinks it's already defined, so this has to happen separate.
        if (Ext.isEmpty(this.view)) {
            this.view = this.gridView;
        }
        
        this.callParent(arguments);
    },
    
    onRender: function() {
        this.callParent(arguments);
        
        /*
         Add Ext.toolTip support for grid cells. To use, set cellTooltip to true on
         columnModel and handle beforetooltipshow event with this signature: function(grid, row, col)
         */
        this.addEvents(        /**
         * @event beforetooltipshow
         * Fires before the grid's ToolTip is shown.  Listen for this event and update the
         * grid's tooltip appropriately.  This is used in conjunction with "cellTooltip: true"
         * on columns that should display a ToolTip.
         * @param {RP.ui.GridPanel} this
         * @param {Number} row The row index.
         * @param {Number} cell The cell index.
         */
        "beforetooltipshow");
        
        var defaultTooltipConfig = {
            renderTo: RP.getTaskflowFrame()._rpViewPanel.el,
            target: this.getEl(),
            delegate: this.getView().cellSelector,
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
        this.tooltip = Ext.create(this.tooltipClass, Ext.apply({}, this.tooltipConfig, defaultTooltipConfig));
    },

    /**
     * Override reconfigure to reset columnConfig, needed for injecting the status column.
     */
    reconfigure: function(store, columns) {
        this.columnConfig = columns;
        this.callParent(arguments);
    },
    
    /**
     * @private
     * Attaches an icon column to the column model.
     */
    _attachIconColumn: function() {
        var columns = this.columnConfig;
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
        
        this.columns = columns.concat(newCols);
        if(this.rendered) {
            this.suspendEvents(false);
            this.reconfigure(null, this.columns);
            this.resumeEvents();
        }
        
    },
    
    _renderTooltip: function(tt) {
        var v = this.getView();
        // HACK!!!
        var rowElement = v.findItemByChild(tt.triggerElement);
        if(!Ext.isEmpty(rowElement) && !Ext.fly(rowElement).hasCls("x-grid-summary-row")) {
            var row = v.indexOf(rowElement);
            //        var cell = v.findCellIndex(tt.triggerElement); TODO FIX THIS!!!! WE need to get the cell index to pass to the event
            var cell = -1;
            Ext.iterate(v.findItemByChild(tt.triggerElement).cells, function(item, index) {
                if (item === tt.triggerElement) {
                    cell = index;
                    return false;
                }
            });
        // END HACK!!
            
            var colConfig = this.columns[cell];
            
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
        // HACK
        }
        else {
            return false;
        }
        // END HACK
    },
    
    /**
     * RP.interfaces.IPrintSource implementation that creates printable
     * markup from the grid's contents.
     * @return {String} HTML of printable grid.
     */
    getMarkupForPrinting: function() {
        var view = this.getView();
        var ts = view.tpl;
        
        // backup the templates, so they can be reset after getting the print table
        var bak = {};
        bak.body = ts.body;
        bak.master = ts.master;
        bak.header = ts.header;
        bak.hcell = ts.hcell;
        bak.row = ts.row;
        bak.cell = ts.cell;
        
        ts.master = new Ext.Template('<table>', '{header}', '<tbody>', '{body}', '{summary}', '</tbody>', '</table>');
        ts.header = new Ext.Template('<thead><tr>{cells}</tr></thead>\n');
        
        ts.hcell = new Ext.Template('<td class="rp-print-header-cell" style="{style}">', '{value}', "</td>");
        ts.row = new Ext.Template('<tr class="{alt}">{cells}</tr>\n');
        ts.cell = new Ext.Template('<td style="{style}" {cellAttr}>', '{value}', "</td>");
        
        var cb = this._exportHeaderRow(true, ts.hcell);
        var header = ts.header.apply({
            cells: cb.join("")
        });
        var body = this.body.down("tbody").dom.innerHTML;
        
        var html = ts.master.apply({
            header: header,
            body: body
        });
        
        view.templates = bak;
        
        return html;
    },
    
    /**
     * Pops up a window with the grid in printable format and display the Print dialog.
     */
    printGrid: function() {
        if (typeof(this.initialConfig.printOptionsDelegate) === "function") {
            var fn = function(options) {
                RP.core.CommonExtensions.printComponents({
                    title: this.printTitle,
                    items: this
                }, options);
            };
            
            this.initialConfig.printOptionsDelegate(Ext.bind(fn, this));
        }
        else {
            RP.core.CommonExtensions.printComponents({
                title: this.printTitle,
                items: this
            });
        }
    },
    
    /**
     * Reloads the data in the grid.
     */
    reloadGrid: function() {
        this.getStore().load();
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
    _exportGrid: function(exportData, type) {
        var postExportUrl = RP.globals.getValue('POST_EXPORT_URL');
        var getExportUrl = RP.globals.getValue('GET_EXPORT_URL');
        
        if (typeof(postExportUrl) === "undefined") {
            logger.logError("[GridPanel] RP.globals.POST_EXPORT_URL not defined");
            return;
        }
        
        if (typeof(getExportUrl) === "undefined") {
            logger.logError("[GridPanel] RP.globals.GET_EXPORT_URL not defined");
            return;
        }
        
        var baseUrl = RP.globals.getPath('BASE_URL');
        
        Ext.Ajax.request({
            url: Ext.String.format("{0}?type={1}", baseUrl + postExportUrl, type),
            method: 'POST',
            params: exportData.data,
            scope: this,
            headers: {
                'Content-Type': exportData.contentType
            },
            success: function(res) {
                var key = res.responseText;
                window.open(baseUrl + getExportUrl + "/" + key, "rpexport", "width=20,height=20,resizable=yes");
            },
            failure: function(e) {
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
    _exportAsCSV: function() {
        var headerRow = this._exportHeaderRow(true);
        
        var header = this._escapeColumns(headerRow);
        var rows = this.getView().renderRowsForExport(0, this.getStore().getCount(), this._includeColumn);
        
        var summary = "";
        if (this.getView().summary && this.getView().summary.dom) {
            summary = this._getSummaryCSV(this.getView().summary.dom, this.getView());
        }
        
        return {
            data: Ext.String.format("{0}\n{1}\n{2}", header, rows, summary),
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
        var headerCt = this.getView().getHeaderCt();
        var len = headerCt.getColumnCount();
        var gridColumns = headerCt.getGridColumns();
        
        // loop through each column in the header.
        var cols = [];
        for (var i = 0; i < len; i++) {
            var column = {};
            column.id = gridColumns[i].dataIndex || "&nbsp;";
            column.value = gridColumns[i].text || " ";
            column.style = gridColumns[i].style || " ";
            
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
        var rowString = row.join("\",\"");
        rowString = "\"" + rowString + "\"";
        
        return rowString;
    },
    
    _getSummaryHTML: function(summaryDOM, view) {
        var masterTemplate = new Ext.Template('<tr>', '{body}', '</tr>');
        
        var cols = Ext.query("div.x-grid3-cell-inner", summaryDOM);
        var rowHTML = [];
        for (var i = 0; i < cols.length; i++) {
            var colStyle = view.getColumnStyle(i, true);
            var colValue = cols[i].innerHTML;
            rowHTML.push(Ext.String.format('<td style="{0}">{1}</td>', colStyle, colValue));
        }
        return masterTemplate.apply({
            body: rowHTML.join("")
        });
    },
    
    _getSummaryCSV: function(summaryDOM, view) {
        var masterTemplate = new Ext.Template('{body}');
        
        var cols = Ext.query("div.x-grid3-cell-inner", summaryDOM);
        var row = this._exportRow(cols);
        
        return masterTemplate.apply({
            body: this._escapeColumns(row)
        });
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
//////////////////////
// ..\js\ui\Panel.js
//////////////////////
/**
 * An extension of the regular {@link Ext.panel.Panel} class to add some more
 * functionality.
 */
Ext.define("RP.ui.Panel", {
    extend: "Ext.panel.Panel",

    alias: "widget.rpuipanel",
  
    /**
     * Scroll to the bottom of the current log panel.
     * @param {Boolean} animate Flag the scroll method to animate the content scroll.
     */
    scrollToBottom: function(animate) {
        var content = this.body;

        content.scroll("b", content.dom.scrollHeight, animate);
    }
});
//////////////////////
// ..\js\ui\InactivityWarningDialog.js
//////////////////////
/**
 * This is a dialog window that will warn the user that their
 * session is about to expire due to inactivity.  The user can then
 * either Continue or Logout.  If the user does neither, and the
 * expiration time runs out, the session will automatically expire
 * and a RP.ui.SessionExpiredDialog will be displayed.
 */
Ext.define("RP.ui.InactivityWarningDialog", {
    
    extend: "Ext.window.Window",

    /**
     * @cfg {Number} secondsUntilExpire <p>The number of seconds
     * until the session will automatically expire. Defaults to 60 seconds.</p>
     */
    secondsUntilExpire: 60,
    
    /**
     * @cfg {Object} timeoutTask
     * The timeout task created by the TaskMgr to count down seconds
     * until expiration.  Only used internally.
     * @private
     */
    timeoutTask: {},
    
    /**
     * @cfg {String} id
     * The id for the InactivityWarningDialog component
     * @private
     */
    id: "inactivityWarningDialog",
    
    /**
     * @cfg {Boolean} isAjaxRequestExecuting
     * A toggle to prevent isUserActive from firing constantly. Only used internally.
     * @private
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
            bodyPadding: 20,
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
        
        this.callParent(arguments);
    },
    
    /**
     * Shows the dialog and starts a count down timer to automatically
     * display seconds until logout and then call expireSession.
     */
    show: function() {
        Ext.getBody().maskEx();
        
        Ext.EventManager.onWindowResize(this.center, this);
        
        this.callParent(arguments);
        
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
        
        this.timeoutTask = Ext.TaskManager.start(taskCfg);
    },
    
    /**
     * Handler for successfully getting a response from {@link RP.util.Helpers#isUserActive}.
     */
    userActiveSuccess: function(response, options) {
        var intervalMillis = (this.secondsUntilExpire + RP.util.PageInactivityChecker.getTimeOutInSeconds()) * 1000;
        this.isAjaxRequestExecuting = false;
        try {
            var result = Ext.JSON.decode(response.responseText);
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
        catch (ex) {
            // If we do not get JSON back it probably is the login page, expire the session
            this.expireSession();
        }
    },
    
    /**
     * Stops the count down task.
     */
    stopCountdown: function() {
        this.isAjaxRequestExecuting = false;
        Ext.TaskManager.stop(this.timeoutTask);
    },
    
    /**
     * Stops the count down task, removes the mask hiding the body, and 
     * destroys the window.
     */
    destroy: function() {
        this.stopCountdown();
        
        Ext.getBody().unmask();
        
        Ext.EventManager.removeResizeListener(this.center, this);
        
        RP.event.AppEventManager.fire(RP.upb.AppEvents.ActiveAgain, {});
        
        this.callParent();
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
        return Ext.String.format(RP.getMessage("rp.common.login.InactivityWarning"), Math.max(this.expireCountDown, 0));
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
Ext.define("RP.ui.ExternalAuthenticationExpiredDialog", {
    extend: "Ext.window.Window",
    
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
            bodyPadding: 20,
            bodyStyle: "background-color: #fff",
            items: [{
                xtype: "box",
                style: "font-size: 12px;",
                html: RP.getMessage("rp.common.login.InactivityTimeout")
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
        
        this.callParent(arguments);
    }
});
//////////////////////
// ..\js\ui\TimeRange.js
//////////////////////
/**
 * A control used to enter a time range.
 */
Ext.define("RP.ui.TimeRange", {
    extend: "Ext.container.Container",
    
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
     * @cfg {Date} startDate The start date of the time range
     */
    startDate: new Date(),
    
    initComponent: function() {
        this.addEvents(
        /**
         * @event change
         * Triggered when following a change to the date or time fields.
         */
        "change");
        
        var now = RP.Date.roundBackToHour(this.startDate);
        
        Ext.applyIf(this, {
            cls: "rp-ui-time-range",
            startDateTime: now,
            endDateTime: RP.Date.addMinutes(RP.Date.clone(now), this.offsetMinutes),
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
        
        this.callParent(arguments);
    },
    
    /**
     * Creates and returns the time controls by calling
     * createStartDateTime() and createEndDateTime().
     * @return {Array/Ext.Component} The time controls.
     * @private
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
     * @return {Object} An object with the following controls
     * @return {RP.form.DateField} return.startDateField A clone of the endDateTime
     * @return {RP.ui.TimeField} return.startTimeField A formatted version of endDateTime
     */
    createStartDateTime: function() {
        return {
            startDateField: new RP.ui.DateField({
                value: this._cloneAndClear(this.startDateTime)
            }),
            startTimeField: new RP.ui.TimeField({
                value: RP.Date.formatTime(this.startDateTime, this.timeFormat)
            })
        };
    },
    
    /**
     * Creates the end date/time controls.
     * @return {Object} An object with the following controls
     * @return {RP.form.DateField} return.endDateField A clone of the endDateTime
     * @return {RP.ui.TimeField} return.endTimeField A formatted version of endDateTime
     */
    createEndDateTime: function() {
        return {
            endDateField: new RP.ui.DateField({
                value: this._cloneAndClear(this.endDateTime)
            }),
            endTimeField: new RP.ui.TimeField({
                value: RP.Date.formatTime(this.endDateTime, this.timeFormat)
            })
        };
    },
    
    /**
     * Gets the combined start date and time from startDateField
     * @return {Date} A clone of the start date and time.
     */
    getStartDateTime: function() {
        this.startTimeField.setTheDate(this.startDateField.getValue());
        
        return Ext.isEmpty(this.startTimeField.getDateTime()) ? null : RP.Date.clone(this.startTimeField.getDateTime());
    },
    
    /**
     * Gets the combined end date and time from endDateField.
     * @return {Date} A clone of the end date and time.
     */
    getEndDateTime: function() {
        this.endTimeField.setTheDate(this.endDateField.getValue());
        
        return Ext.isEmpty(this.endTimeField.getDateTime()) ? null : RP.Date.clone(this.endTimeField.getDateTime());
    },
    
    /**
     * Set the start date and time to a custom value.
     * @param {Date} startDate The start date.
     */
    setStartDateTime: function(startDate) {
        this.startDateField.setValue(this._cloneAndClear(startDate));
        this.startTimeField.setValue(RP.Date.formatTime(startDate, this.timeFormat));
    },
    
    /**
     * Set the end date and time to a custom value.
     * @param {Date} endDate The end date.
     */
    setEndDateTime: function(endDate) {
        this.endDateField.setValue(this._cloneAndClear(endDate));
        this.endTimeField.setValue(RP.Date.formatTime(endDate, this.timeFormat));
    },
    
    /**
     * Create the event listeners on the time controls.
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
     * Only used to fire the change event. Calls {@link #getStartDateTime} and
     * {@link #getEndDateTime}.
     * @private
     */
    onEndDateFieldChange: function() {
        this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
    },
    
    /**
     * Called by the change event. When start date changes, update the end date to match.
     * @param {Object} field Unused EventObject passed in by Ext.Element
     * @param {Date} newStartDate The new start date value.
     */
    onStartDateFieldUpdate: function(field, newStartDate) {
        var endTime = this.getEndDateTime();
        if (!Ext.isEmpty(endTime)) {
            var deltaMS = this.getDurationDeltaMillis(this.lastStartDate, newStartDate);
            
            endTime.addMilliseconds(deltaMS);
            
            this.endTimeField.setValue(RP.Date.formatTime(endTime, this.timeFormat));
            this.endDateField.setValue(endTime);
            this._setLastStartDate(newStartDate);
            this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
        }
    },
    
    /**
     * When start time changes, maintain old duration between start/end time.
     * @param {Object} field Unused EventObject passed in by Ext.Element
     * @param {String} newValue The new value of time for startTimeField
     * @param {String} oldValue The old value, used to maintain duration
     * @private
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
            else if (Ext.isEmpty(oldValue) && Ext.isEmpty(endTime)) {
                oldValue = this.getStartDateTime().add(Date.MINUTE, -this.offsetMinutes).format("g:i a");
            }
            if (Ext.isEmpty(endTime)) {
                endTime = this.getStartDateTime();
            }
            var newStartTime = this.getStartDateTime();
            var oldStartTime = RP.core.IntervalJN.parseTimeAfterBase(oldValue, this._cloneAndClear(newStartTime));
            var deltaMS = this.getDurationDeltaMillis(oldStartTime, newStartTime);
            
            endTime.addMilliseconds(deltaMS);
            
            this.endTimeField.setValue(RP.Date.formatTime(endTime, this.timeFormat));
            this.endTimeField.setTheDate(endTime);
            this.endDateField.setValue(this._cloneAndClear(endTime));
            this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
        }
    },
    
    /**
     * Gets the change in milliseconds between newDate and oldDate.
     * @param {Date} oldDate The old date/time
     * @param {Date} newDate The date/time to subtract from
     * @private
     */
    getDurationDeltaMillis: function(oldDate, newDate) {
        return newDate.deltaT(oldDate).ms;
    },
    
    /**
     * When end time changes, check if end time is before the start time...
     * if it is, increment the end date assuming the end time has crossed midnight
     * @param {Object} field Unused EventObject passed in by Ext.Element
     * @param {String} newValue The new value of time for the endTimeField
     * @param {String} oldValue The old value of time, used to judge against 
     * startTimeField for correctness
     * @private
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
                startTimeTest = RP.core.IntervalJN.parseTimeAfterBase(this.startTimeField.getValue(), this.startDateField.getValue().clone());
            }
            var endTimeTest = RP.core.IntervalJN.parseTimeAfterBase(this.endTimeField.getValue(), this.endDateField.getValue().clone());
            var endTime = this.getEndDateTime();
            
            // if raw dates are the same, but the end time is before the start time, increment the end date
            if (this._cloneAndClear(startTimeTest).compareTo(this._cloneAndClear(endTimeTest)) === 0 &&
            endTime < startTimeTest) {
                RP.Date.clearTime(endTime.addDays(1));
                
                this.endTimeField.setTheDate(endTime);
                this.endDateField.setValue(endTime.clone());
            }
            this.fireEvent("change", this.getStartDateTime(), this.getEndDateTime());
        }
    },
    
    /**
     * Validates the duration, making sure the end date is not earlier
     * than the start date. Marks or clears markings of 
     * validation or invalidation.
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
     * Sets the lastStartDate, which is used to maintain duration when the 
     * start date changes. Also clears the date.
     * @param {Date} date Date to clear and set as lastStartDate
     * @private
     */
    _setLastStartDate: function(date) {
        this.lastStartDate = this._cloneAndClear(date);
    },
    
    /**
     * Takes a date object and performs a clone() and clearTime() on it.
     * @param {Date} date The date object to clear
     * @return {Date} Clone of this Date object.
     * @private
     */
    _cloneAndClear: function(date) {
        return RP.Date.clearTime(date);
    }
});
//////////////////////
// ..\js\ui\TimeRangeDropDown.js
//////////////////////
/**
 * A RP.ui.TimeRange extension for using drop-downs for the time values.
 */
Ext.define("RP.ui.TimeRangeDropDown", {
    extend: "RP.ui.TimeRange",
    
    alias: "widget.rpuitimerangedropdown",
    
    /**
     * @cfg {Number} offsetMinutes The number of minutes to use for the interval
     * between each time in the drop-down list of a TimeCombo.
     */
    offsetMinutes: 15,
    
    initComponent: function() {
        this.offsetMinutes = this.initialConfig.offsetMinutes;
        this.callParent();
    },
    
    /**
     * Creates the start date/time controls.
     * @return {Object} An object with the following controls
     * @return {RP.form.DateField} return.startDateField A clone of the endDateTime
     * @return {RP.ui.TimeField} return.startTimeField A formatted version of endDateTime
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
     * @return {Object} An object with the following controls
     * @return {RP.form.DateField} return.endDateField A clone of the endDateTime
     * @return {RP.ui.TimeField} return.endTimeField A formatted version of endDateTime
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
     */
    createListeners: function() {
        this.callParent();
        this.startTimeField.on("focus", function() {
            this.endTimeField.collapse();
        }, this);
        this.endTimeField.on("focus", function() {
            this.startTimeField.collapse();
        }, this);
    }
});
//////////////////////
// ..\js\ui\TimeCombo.js
//////////////////////
/**
 * A ComboBox extension for entering time values.
 * Will automatically attempt to parse and format the time value on the blur event.
 *
 * TODO: Evaluate whether this component is still necessary in 4.x.  What does it
 * offer that can't be achieved through Ext.form.field.Time?
 */
Ext.define("RP.ui.TimeCombo", {
    extend: "Ext.form.field.ComboBox",
    
    alias: "widget.rpuitimecombo",
    
    mixins: {
        timeControl: "RP.ui.Mixins.TimeControl"
    },
    
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
     * @cfg {Boolean} fixedTimesInDropdown Whether or not the times in the drop-down remain stationary or update based on the entered value
     */
    fixedTimesInDropdown: true,
    
    /**
     * @private
     */
    initComponent: function() {
        // initialize the mixins
        this.initDate = this.initialConfig.initDate = new Date(2008, 0, 1);
        this.mixins.timeControl.constructor.call(this, this.initialConfig);
        
        this.store = new Ext.data.ArrayStore({
            fields: ["key", "value"]
        });
        
        this.store.loadData(this.createTimes(this.fixedTimesInDropdown ? null : this.getDateTime(true)));
        
        Ext.apply(this, {
            autoSelect: false,
            enableKeyEvents: true,
            queryMode: "local",
            valueField: "value",
            displayField: "key",
            triggerAction: "all"
        });
        
        if (!this.initialConfig.emptyText) {
            Ext.apply(this, {
                emptyText: RP.getMessage("rp.common.misc.EmptyTimeText")
            });
        }
        
        this.on("render", this.onRenderHandler, this);
        this.on("keydown", this.onKeyDownHandler, this);
        this.on("expand", this.onExpandHandler, this);
        this.on("focus", this.onFocusHandler, this);
        this.callParent(arguments);
    },
    
    /**
     * Handles the render event.
     * @method
     */
    onRenderHandler: function(cmp) {
        cmp.getEl().on("click", this.onElementClickHandler, this);
    },
    
    /**
     * Handles the click event on the input element.
     * @method
     */
    onElementClickHandler: function() {
        this.expand();
    },
    
    /**
     * Handler for the keydown event.
     * @method
     */
    onKeyDownHandler: function(field, event) {
        if (!this.enableAutoComplete) {
            // Disable ComboBox expansion when a key is pressed
            this.collapse();
            // minChars is reset to 0 for unknown reasons when handling the keydown so minChars is set here
            this.minChars = Number.MAX_VALUE;
        }
    },
    
    /**
     * Override of onTriggerClick. Provides behavior for when the trigger is clicked.
     * @method
     */
    onTriggerClick: function() {
        this.reloadStore();
        this.callParent();
    },
    
    /**
     * Handles the ComboBox expand event. This occurs later than onTriggerClick.
     * @method
     */
    onExpandHandler: function() {
        // Ensure the store is reloaded on expand if the current value is not in the current list
        // because the list will not display if the store reloads when the current
        // value is in the list.
        this.reloadStore();
        
        var record = this.store.find("key", this.getRawValue());
        if (record == -1 && this.fixedTimesInDropdown) {
            record = this.getNearestValue();
        }
        var boundList = this.getPicker();
        var itemNode = boundList.getNode(record);
        var midPointItemsDropDown = 7;
        var indexToScrollTo = (record + midPointItemsDropDown > this.store.getCount()) ? this.store.getCount() - 1 : record + midPointItemsDropDown;
        var itemNodeOffset = boundList.getNode(indexToScrollTo);
        if (itemNode) {
            // highlight the current value
            boundList.highlightItem(itemNode);
            // scroll the current value toward the center of the view
            boundList.listEl.scrollChildIntoView(itemNodeOffset, false);
        }
    },
    
    /**
     * Returns the nearest value in the list of times that is less than the current raw value time in the ComboBox.
     */
    getNearestValue: function() {
        var currentRawValue = new Date(this.initDate.toDateString() + " " + this.getRawValue());
        for (var index = 0; index < this.store.getCount(); index++) {
            if (this.store.getAt(index).get("value") > currentRawValue) {
                return index === 0 ? 0 : index - 1;
            }
        }
        return this.store.getCount() - 1;
    },
        
    /**
     * Creates the times in the ComboBox's drop-down
     * @method
     * @param {Object} time The base time to add other times based off of
     * @return An array of times that can be used for user selection
     */
    createTimes: function(time) {
        if (Ext.isEmpty(time)) {
            var twelveHours = 43200000;
            var oneMinute = 60000;
            var currentDate = RP.Date.clearTime(this.initDate);
            time = new Date(currentDate.getTime() + twelveHours - this.offsetMinutes * oneMinute);
        }
        var times = [];
        var startDate = Ext.Date.add(time, Ext.Date.HOUR, -this.hoursToBoundary);
        var date;
        for (date = startDate; date < Ext.Date.add(startDate, Ext.Date.HOUR, this.hoursToBoundary * 2);) {
            date = Ext.Date.add(date, Ext.Date.MINUTE, this.offsetMinutes);
            times.push({
                "key": RP.Date.formatTime(date, this._format),
                "value": date
            });
        }
        return times;
    },
    
    /**
     * Override to format the entered value into the field's time format.
     * @private
     */
    beforeBlur: function() {
        var value;

        try {
            value = RP.Date.parseTime(this.getRawValue(), RP.core.Formats.Time.Default);
        }
        catch(e) {

        }

        if (value) {
            this.typedValue = this.getRawValue();
            this.setRawValue(this.formatTime(value));
        }
        this.callParent(arguments);
    },

    /**
     * @override
     * Mostly copy-pasted from Ext.form.field.Time.
     */
    getErrors: function(value) {
        var format = Ext.String.format,
            errors = this.callParent(arguments),
            minValue = this.minValue,
            maxValue = this.maxValue,
            time;

        if (value === null || value.length < 1) { // if it's blank and textfield didn't flag it then it's valid
             return errors;
        }

        try {
            time = RP.Date.parseTime(this.getRawValue(), this._format);
        }
        catch(e) {
            // Handled below
        }

        if (!time) {
            errors.push(format(this.invalidText, value, this._format));
            return errors;
        }

        return errors;
    },
    
    /**
     * Handles the focus event.
     * @method
     */
    onFocusHandler: function() {
        this.expand();
    },
    
    /**
     * Reloads the store used by the TimeCombo if necessary.
     * @method
     */
    reloadStore: function() {
        var isInList = (this.store.find("key", this.getRawValue()) >= 0);
        if (Ext.isEmpty(this.getRawValue()) || !isInList) {
            this.store.loadData(this.createTimes(this.fixedTimesInDropdown ? null : this.getDateTime(true)));
            if (!this.enableAutoComplete) {
                this.setTheTime(this.getDateTime(true));
            }
        }
    }
});
//////////////////////
// ..\js\ui\Toolbar.js
//////////////////////
/**
 * A common, mostly pre-configured toolbar that supports easily assigning items to the left, center, right
 * or any combination of the three
 * 
 * --By default any configured areas share equal space. i.e. if only left and right items are configured
 *   then both the "left" toolbar and "right" toolbar will be the same size.<br /><br />
 * --leftItems are left justified but may be right justified with the use of the regular Ext fill component (shorthand: "->")<br /><br />
 * --rightItems are right justified.  Currently this cannot be overridden but a future update will allow you to override this behavior
 */
Ext.define("RP.ui.Toolbar", {
    extend: "Ext.Container",
    
    /**
     * @cfg {Ext.Component[]} leftItems
     * Items to be placed on the left side of the toolbar
     */
    
    /**
     * @cfg {Ext.Component[]} centerItems
     * Items to be placed on the center of the toolbar
     */
    
    /**
     * @cfg {Ext.Component[]} rightItems
     * Items to be placed on the right of the toolbar
     */
    
    /**
     * @cfg {Number} centerWidth
     * Allows you to override the width of the center portion of the toolbar.
     * Is only utilized if there are left or right items passed in.  When only
     * centerItems are configured this value is ignored and the entire toolbar
     * is utilized. This property is useful for shrinking the center, allowing
     * more space for left/right items. If no centerWidth is configured,
     * toolbar spaced will be split equally among the left, center, and right.
     */
    
    /**
     * @cfg {String} backgroundCls
     * String representing the css background style to use. A default
     * background is used when one isn't configured.
     */
    
    alias: "widget.rptoolbar",

    initComponent: function() {
        var items = [];
        
        this._leftBar = null;
        this._rightBar = null;
        this._centerBar = null;
        this._containerCreated = false;
        
        // Configure left toolbar if needed
        if (this._hasLeftItems() || (this._hasCenterItems() && !this._hasOnlyCenterItems())) {
            this._createLeftBar();
            
            items = items.concat(this.getLeftBar());
        }
        
        // Configure center toolbar if needed
        if (this._hasCenterItems()) {
            this._createCenterBar();
            
            items = items.concat(this.getCenterBar());
        }
        
        // Configure right toolbar if needed
        if (this._hasRightItems() || (this._hasCenterItems() && !this._hasOnlyCenterItems())) {
            this._createRightBar();
            
            items = items.concat(this.getRightBar());
        }
        
        var containerConfig = {
            layout: 'hbox',
            cls: 'rp-tbar-container ' + this._getToolbarBackground(),
            items: items
        };
        
        Ext.apply(this, containerConfig);
        
        this.callParent(arguments);
        
        this._containerCreated = true;
    },

    /**
     * Returns the left region of the toolbar.
     * 
     * @return {Ext.toolbar.Toolbar} The left region of the toolbar.
     */
    getLeftBar: function() {
        return this._leftBar;
    },
    
    /**
     * Returns the center region of the toolbar.
     * 
     * @return {Ext.toolbar.Toolbar} The center region of the toolbar.
     */
    getCenterBar: function() {
        return this._centerBar;
    },
    
    /**
     * Returns the right region of the toolbar.
     * 
     * @return {Ext.toolbar.Toolbar} The right region of the toolbar.
     */
    getRightBar: function() {
        return this._rightBar;
    },
    
    /**
     * Adds items to the left region of the toolbar.
     * 
     * @param {Ext.Component[]} items An array of components to add to
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
     * 
     * @param {Ext.Component[]} items An array of components to add to
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
     * 
     * @param {Ext.Component[]} items An array of components to add to
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
        return Ext.isDefined(this.left) && Ext.isDefined(this.left.items);
    },
    
    /**
     * @private
     */
    _hasCenterItems: function() {
        return Ext.isDefined(this.center) && Ext.isDefined(this.center.items);
    },
    
    /**
     * @private
     */
    _hasRightItems: function() {
        return Ext.isDefined(this.right) && Ext.isDefined(this.right.items);
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
    _createLeftBar: function() {
        //make sure we don't have one already
        if (this._leftBar !== null) {
            return;
        }
        
        this._leftBar = Ext.widget('toolbar', 
            Ext.applyIf(this.left, {
                cls: 'rp-tbar',
                style: "border: none",
                flex: 1
            })
        );
        
        if (this._isContainerAlreadyCreated()) {
            this.insert(0, this._leftBar);
        }
    },
    
    /**
     * @private
     */
    _createCenterBar: function() {
        //make sure we don't have one already
        if (this._centerBar !== null) {
            return;
        }
        
        this._centerBar = Ext.widget("toolbar", 
            Ext.applyIf(this.center, {
                layout: {
                    type: 'hbox',
                    pack: 'center',
                    align: 'middle'
                },
                cls: 'rp-tbar',
                style: "border: none",
                flex: 1
            })
        );
        
        if (this._isContainerAlreadyCreated()) {
            this.insert(1, this._centerBar);
        }
    },
    
    /**
     * @private
     */
    _createRightBar: function() {
        //make sure we don't have one already
        if (this._rightBar !== null) {
            return;
        }
        this.right.items.unshift("->");
        
        this._rightBar = Ext.widget('toolbar', 
            Ext.applyIf(this.right, {
                cls: 'rp-tbar',
                style: "border: none",
                flex: 1
            })
        );
        
        if (this._isContainerAlreadyCreated()) {
            this.insert(2, this._rightBar);
        }
    },
    
    /**
     * @private
     */
    _getToolbarBackground: function() {
        return this.backgroundCls || 'rp-tbar-background';
    },
    
    /**
     * @private
     */
    _isContainerAlreadyCreated: function() {
        return this._containerCreated;
    }
});
//////////////////////
// ..\js\ui\TriCheckbox.js
//////////////////////
/**
 * A checkbox class with the following three states: indeterminate, unchecked, and checked.
 * The values are null, false, and true respectively.
 * @author Kevin Rice
 *
 * @cfg {Array} values The values of the check states.
 * @cfg {Array} checkboxCls The classes for each checkbox state. 
 * Classes must be synchronous with validValues
 * @cfg {Array} validValues By default is equal to values but can be overridden. Should be 
 * used to prevent a user from being able to click a checkbox to 
 * a value specfic value.
 * @cfg {Object} defaultAutoCreate The auto create settings for the checkbox.
 *              KEY             DEFAULT VALUE
 *              tag             'input'
 *              type            'hidden'
 *              autocomplete    'off'
 */
Ext.define("RP.ui.TriCheckbox", {
    extend: "Ext.form.Checkbox",
    
    alias: "widget.rpuitricheckbox",
    
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
     * @param {Object} ct The container
     * @param {Object} position The position of the container
     * @private
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
     * Getter function for the value of the checkbox.
     * @return {Object} The value from the checkbox.
     */
    getValue: function() {
        return Ext.form.Checkbox.superclass.getValue.call(this);
    },
    
    /**
     * Sets the value of the checkbox. The values are checked against
     * the validValues. 
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
     * Checks to see if the value specified exists in validValues.
     * @param {Object} value The value to be checked to see if it is valid.
     * @return {Boolean} return is true if value exists in validValues
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
     * Dependent on {@link #toggleValue}.
     * @private
     */
    onClick: function() {
        this.toggleValue();
    },
    
    /**
     * Gets the index of the passed in value in the validValues array
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
     * Getter function for the index of the current value in validValues.
     * Dependent on {@link #getCheckIndexByValue}.
     * @return {Number} The current index value.
     */
    getCheckIndex: function() {
        return this.getCheckIndexByValue(this.value);
    },
    
    /**
     * Sets the value of the checkbox, based on an index value of validValues
     * @param {Number} index The index to the value within validValues
     */
    setCheckValueByIndex: function(index) {
        this.value = this.values[index];
    },
    
    /**
     * Sets the check state based on a value or index passed in. This
     * will also update the class of the checkbox. Dependent on 
     * {@link #setCheckValueByIndex}.
     * @param {Number/Object} state An index in validValues, or value itself,
     * of the checkbox to be set
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
     * and gets the class from checkboxCls.
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
     * @return {Object} Value from the values array.
     */
    getNextCheckState: function() {
        return this.values[(this.getCheckIndex() + 1) % this.values.length];
    },
    
    /**
     * Switches the checkbox to the next state.
     */
    toggleValue: function() {
        if (!this.disabled && !this.readOnly) {
            this.setValue(this.getNextCheckState());
        }
    },
    
    /**
     * Handles after render.
     * @private
     */
    afterRender: function() {
        Ext.form.Checkbox.superclass.afterRender.call(this);
    }
});
//////////////////////
// ..\js\ui\DatePickerToolbar.js
//////////////////////
/**
 * An RP.ui.Toolbar extension that centers a given date field in the toolbar.
 * @cfg {Ext.form.field.Date} datePicker A date field to center in the toolbar.
 */
Ext.define("RP.ui.DatePickerToolbar", {
    extend: "RP.ui.Toolbar",
    
    alias: "widget.rpdatepickertoolbar",
    
    initComponent: function() {
        
        if (!Ext.isDefined(this.datePicker)) {
            RP.throwError(this.$className + " requires that a 'datePicker' be configured.");
        }
        
        Ext.apply(this, {
            left: this.left || {},
            center: {
                items: this.datePicker,
                width: 150
            },
            right: this.right || {}
        });
        
        this.callParent();
    }
});
//////////////////////
// ..\js\help\RoboHelpLink.js
//////////////////////
Ext.ns("RP.help");

/**
 * @class RP.help.RoboHelpLink
 * @singleton
 * RoboHelp hyperlink
 */
RP.help.RoboHelpLink = (function() {
    var mainUrl;

    /**
     * @param {String} target The location to open
     * @param {Function} A helper function for opening the RoboHelp
     * @private
     */
    var showHelp = function(target, urlHashTranslatorFn) {
        var w, urlHash = window.location.hash;

        if (urlHash) {
            // Context-sensitive help.
            var url = urlHashTranslatorFn(urlHash);

            if (url) {
                w = window.open(url, target, "height=680,left=700,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,titlebar=no,toolbar=no,top=20,width=500,zoominherit=no");
                w.focus();
            }
            else {
                Ext.Msg.show({
                    title: RP.getMessage("rp.common.misc.NoHelpFoundTitle"),
                    msg: RP.getMessage("rp.common.misc.NoHelpFoundText"),
                    buttons: Ext.Msg.OK,
                    icon: Ext.MessageBox.WARNING
                });
            }
        }
        else {
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
         * @param {String} mainURL The RoboHelp main URL
         * @param {String} helpLabel The text label for the Help link
         * @param {String} windowTarget The browser target window name to display Help
         * @param {Function} urlHashTranslatorFn The function to call to map the  URL Hash to the appropriate Help topic
         * @return {RP.ui.Hyperlink} A hyperlink to RoboHelp
         */
        createHelpLink: function(config, mainURL, helpLabel, windowTarget, urlHashTranslatorFn) {
            mainUrl = mainURL;

            return Ext.ComponentMgr.create(RP.util.Object.mergeProperties({
                id: 'roboHelpLink',
                xtype: "hyperlink",
                text: helpLabel,
                handler: function() {
                    showHelp(windowTarget, urlHashTranslatorFn);
                }
            }, config));
        }
    };
})();
//////////////////////
// ..\js\help\HelpRegistry.js
//////////////////////
/**
 * @class RP.help.HelpRegistry
 * @singleton
 */
RP.help.HelpRegistry = (function() {
  var appIdMap = {};
  var defaultHelpURL = "";
  
  return {
    /**
     * Sets the default Help URL.  This URL is used if no Help map is found 
     * for a particular application.
     * @param {String} url New default help URL
     */
    setDefaultHelpURL: function(url) {
      defaultHelpURL = url;
    },
    
    /**
     * 
     * @param {String} appId Key to store the rootHelpUrl
     * @param {String/Function} rootHelpUrl The root URL of help files/Help
     * mapper function for an app
     */
    register: function(appId, rootHelpUrl) {
      appIdMap[appId] = rootHelpUrl;      
    },
    
    /**
     * Returns the root URL of help files/Help mapper function for an app. 
     * Corresponds to whatever was {@link #register}ed. 
     * @param {String} appId Key where the data was stored
     * @return {String/Function} Whatever was registered originally
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
// ..\js\state\AbstractProvider.js
//////////////////////
/**
 * @class RP.state.AbstractProvider
 * @extends Ext.state.Provider
 *
 * This abstract implementation of the state provider makes use of embedded state data for state retrieval.
 * In order to use this you must set stateful to true on your component, specify a stateId, and if it
 * doesn't exist already, an implementation for that component's {@link Ext.state.Stateful#getState} and
 * {@link Ext.state.Stateful#applyState} methods.
 *
 * Note that this will not provide meaningful state data unless the application server back-end supplies
 * storage and retrieval of the state data and the connection object implements the getPersistedState method.
 *
 * This class should not be used directly as it doesn't supply an implementation for saving or clearing 
 * state data.  See {@link RP.state.AjaxProvider} and {@link RP.state.RestProvider}.
 *
 * @author Jeff Gitter
 * @docauthor Jeff Gitter
 */
Ext.define('RP.state.AbstractProvider', {
    extend: 'Ext.state.Provider',

    inheritableStatics: {
        /**
         * @static
         * @property {Enumeration} operation The enumeration of operations
         * @property {Integer} operation.SET The set operation
         * @property {Integer} operation.CLEAR The clear operation
         */
        operation: { SET: 1, CLEAR: 2 }
    },

    /**
     * @private @property {Boolean} _initialized
     * Indicates whether the store has loaded the embedded state data
     */
    _initialized: false,

    /**
     * @cfg {String} saveUrl
     * The url used for saving state.  To allow for the inclusion of url path variables, you can specify 
     * module, taskflow, task, and stateId in your url and a variable replacement will be done for you.  For example: 
     * 
     *      Ext.state.StateManager.setProvider(new RP.state.AjaxProvider({
     *          saveUrl: '/persistence/{module}/{taskflow}/{task}/{stateId}'
     *      }));
     *
     * This makes it easy to configure your urls via Spring Controller however you want them.
     */
    /**
     * @cfg {String} clearUrl
     * The url used for clearing state.  To allow for the inclusion of url path variables, you can specify 
     * module, taskflow, task, and stateId in your url and a variable replacement will be done for you.  For example: 
     * 
     *      Ext.state.StateManager.setProvider(new RP.state.AjaxProvider({
     *          clearUrl: '/persistence/{module}/{taskflow}/{task}/{stateId}'
     *      }));
     *
     * This makes it easy to configure your urls via Spring Controller however you want them.
     */
    /**
     * @cfg {String} url
     * A url used for clearing or saving state.  Ignored if saveUrl/clearUrl are set.
     */

    constructor: function(config) {
        this.callParent(arguments);

        this.addEvents(
            /**
             * @event savesuccess
             * Fires after the state has been successfully saved to server
             * @param {Provider} this
             * @param {RP.state.State} record The state record that was saved or cleared
             * @param {RP.state.AbstractProvider.operation} operation The operation that succeeded
             */
            'savesuccess',
            /**
             * @event savefailure
             * Fires when an error is thrown during state save
             * @param {Provider} this
             * @param {RP.state.State} record The state record that failed to save or clear
             * @param {RP.state.AbstractProvider.operation} operation The operation that failed
             */
            'savefailure'
        );

        if (!this.saveUrl && !this.url) {
            RP.throwError('Either saveUrl or url is required.');
        }
        if (!this.clearUrl && !this.url) {
            RP.throwError('Either clearUrl or url is required.');
        }
        if (this.saveUrl && !this.saveUrl.match(".*/$")) {
            this.saveUrl += "/";
        }
        if (this.clearUrl && !this.clearUrl.match(".*/$")) {
            this.clearUrl += "/";
        }
        if (this.url && !this.url.match(".*/$")) {
            this.url += "/";
        }

        this.state = Ext.create(Ext.data.Store, {
            model: 'RP.state.State',
            proxy: {
                type: 'memory',
                data: RP.MODULE_STATE,
                reader: 'json'
            }
        });

        this.tasks = new Ext.util.MixedCollection();
        this._writer = new Ext.data.writer.Json();
    },

    initialize: function() {
        if (!this._initalized) {
            this.state.getProxy().data = RP.MODULE_STATE;
            this.state.load();
            this._initalized = true;
        }
    },

    /**
     * Returns the current state saved for the given stateId.
     *
     * @param {String} stateId The stateId to fetch
     * @return {Object} The state object or undefined if none are found
     */
    get: function(stateId) {
        this.initialize();

        var record = this._getState(stateId);

        if (record) {
            return this.decodeValue(record.get('data'));
        } else {
            return undefined;
        }
    },

    // @inheritdoc
    set: function(stateId, state) {
        this.initialize();

        if (state === undefined || state === null) {
            return this.clear(stateId);
        }

        var record = this._setState(stateId, state);
        if (record) {
            this._queueChange(record, this.self.operation.SET);
        }
    },

    // @inheritdoc
    clear: function(stateId) {
        this.initialize();

        var record = this._clearState(stateId);
        if (record) {
            this._queueChange(record, this.self.operation.CLEAR);
        }
    },

    /**
     * @private
     *
     * Queue up a request to prevent sending unnecessary requests.
     *
     * @param {Ext.data.Model} state The state model for this operation
     * @param {RP.state.AbstractProvider.operation} operation The type of operation being performed
     */
    _queueChange: function(state, operation) {
        if (!this.tasks.get(state)) {
            this.tasks.add(state, new Ext.util.DelayedTask());
        }

        this.tasks.get(state).delay(750, this._sendRequest, this, [state, operation]);
    },

    /**
     * @private
     *
     * Get the state record for the given state id.
     *
     * @param {String} stateId The stateId to fetch
     */
    _getState: function(stateId) {
        var index = this._getIndex(stateId);

        if (index >= 0) {
            return this.state.getAt(index);
        } else {
            return undefined;
        }
    },

    /**
     * @private
     *
     * Get the state record for the given state id.
     *
     * @param {String} stateId The stateId to fetch
     * @param {Object} state The state being set
     * @return {RP.state.State} The record to send or nothing if no change was made.
     */
    _setState: function(stateId, state) {
        var encodedState = this.encodeValue(state);
        var index = this._getIndex(stateId);
        var record;

        if (index < 0) {
            record = this._createRecord(stateId, encodedState);
            this.state.add(record);
        } else if (encodedState !== this.state.getAt(index).get('data')) {
            record = this.state.getAt(index);
            record.set('data', encodedState);
        } else if (this.state.getAt(index).get('global') === true) {
            record = this.state.getAt(index);
        }

        return record;
    },

    /**
     * @private
     * 
     * Create a new State record
     * 
     * @param {String} stateId The stateId to fetch
     * @param {Object} state The state being set
     * @return {RP.state.State} The new state record
     */
    _createRecord: function(stateId, state) {
        return new RP.state.State({
            module: this._getModule(),
            taskflow: this._getTaskflow(),
            task: this._getTask(),
            stateId: stateId,
            data: state
        });
    },

    /**
     * @private
     *
     * Clear the state for the given stateId.
     * 
     * @param {String} stateId The stateId to clear
     * @return {RP.state.State} The record being cleared or nothing if it doesn't exist.
     */
    _clearState: function(stateId) {
        var record, index = this._getIndex(stateId);
        if (index >= 0) {
            record = this.state.getAt(index);
            this.state.removeAt(index);
        }

        return record;
    },

    /**
     * @private
     *
     * Retrieve the index for a specific stateId.
     * 
     * @param {String} stateId The stateId to locate
     * @return {Integer} The numeric index of the stateId if it exists, else -1
     */
    _getIndex: function(stateId) {
        return this.state.findBy(function(record) {
            return (record.get('module') === this._getModule() &&
                    record.get('taskflow') === this._getTaskflow() &&
                    record.get('task') === this._getTask() &&
                    record.get('stateId') === stateId);
        }, this);
    },

    /**
     * @private
     * Send the ajax request to the configured URL
     * @param {Ext.data.Model} state The state that is being updated on the server
     * @param {RP.state.AbstractProvider.operation} operation The operation being performed
     */
    _sendRequest: function(state, operation) {
        Ext.Ajax.request(this._buildRequest(state, operation));
    },

    /**
     * @private
     * Build the request to send to the server with the configured url(s).  Each extending class should
     * provide an implementation for this method.
     * @param {Ext.data.Model} state The state record for which the request is being built
     * @param {RP.state.AbstractProvider.operation} operation The operation being performed
     * @return {Object} The request object for use with {@link Ext.Ajax#request}
     */
    _buildRequest: RP.abstractFn,

    /**
     * @private
     * Callback for the save and clear requests.
     * @param {Object} opts The request options
     * @param {Boolean} success True if request was successful, else false
     * @param {Object} response The response object
     */
    _onSaveCallback: function(opts, success, response) {
        if (success !== true) {
            RPUX.Util.logError(response.responseText);
            this.fireEvent('savefailure', this, opts.record, opts.operation);
        } else {
            this._invalidatePageCache();
            this.fireEvent('savesuccess', this, opts.record, opts.operation);
        }
    },

    /**
     * @private
     * @method _invalidatePageCache
     * Invalidate the REFS page cache.
     */
    _invalidatePageCache: (function() {
        var request = {
            url: '/data/cache',
            params: {
                action: 'invalidatePageCache'
            }
        };

        return function() {
            Ext.Ajax.request(request);
        };
    })(),

    /**
     * @private
     * Get the current module.
     * @return {String} The current module
     */
    _getModule: function() {
        if (RP.CURRENT_LOCATION && RP.CURRENT_LOCATION.module) {
            return RP.CURRENT_LOCATION.module;
        } else {
            return RP.core.PageContext.getActiveModuleName();
        }
    },

    /**
     * @private
     * Get the current taskflow.
     * @return {String} The current taskflow
     */
    _getTaskflow: function() {
        if (RP.CURRENT_LOCATION && RP.CURRENT_LOCATION.taskflow) {
            return RP.CURRENT_LOCATION.taskflow;
        } else {
            return RP.core.PageContext.getActiveTaskflowName();
        }
    },

    /**
     * @private
     * Get the current task.
     * @return {String} The current task
     */
    _getTask: function() {
        if (RP.CURRENT_LOCATION && RP.CURRENT_LOCATION.task) {
            return RP.CURRENT_LOCATION.task;
        } else {
            return RP.core.PageContext.getActiveTaskName();
        }
    }
});
//////////////////////
// ..\js\state\RestProvider.js
//////////////////////
/**
 * @class RP.state.RestProvider
 * @extends RP.state.AbstractProvider
 *
 * This implementation of the state provider makes use of embedded state data for state retrieval.  It
 * uses a configured url to make RESTful requests to the server for saving and clearing state data
 * for a specific stateId.
 *
 * In order to use this you must set stateful to true on your component, specify a stateId, and if it
 * doesn't exist already, an implementation for that component's {@link Ext.state.Stateful#getState} and
 * {@link Ext.state.Stateful#applyState} methods.
 *
 * Note that this will not provide meaningful state data unless the application server back-end supplies
 * storage and retrieval of the state data and the connection object implements the getPersistedState method.
 *
 * @author Jeff Gitter
 * @docauthor Jeff Gitter
 */
Ext.define('RP.state.RestProvider', {
    extend: 'RP.state.AbstractProvider',

     // @inheritdoc
    _buildRequest: function(record, operation) {
        var options = {
            scope: this,
            callback: this._onSaveCallback,
            jsonData: [this._writer.getRecordData(record)],
            params: {},
            operation: operation,
            record: record,
            pathVars: {
                module: this._getModule(),
                taskflow: this._getTaskflow(),
                task: this._getTask(),
                stateId: record.get('stateId')
            }
        };

        if (operation === this.self.operation.SET) {
            Ext.apply(options, {
                url: this.saveUrl || this.url,
                method: 'POST'
            });

        } else if (operation === this.self.operation.CLEAR) {
            Ext.apply(options, {
                url: this.clearUrl || this.url,
                method: 'DELETE'
            });
        }

        return RPUX.buildRequest(options);
    }
});
//////////////////////
// ..\js\state\AjaxProvider.js
//////////////////////
/**
 * @class RP.state.AjaxProvider
 * @extends RP.state.AbstractProvider
 *
 * This implementation of the state provider makes use of embedded state data for state retrieval.  It
 * uses configured urls to make ajax requests to the server for saving and clearing state data
 * for a specific stateId.
 *
 * In order to use this you must set stateful to true on your component, specify a stateId, and if it
 * doesn't exist already, an implementation for that component's {@link Ext.state.Stateful#getState} and
 * {@link Ext.state.Stateful#applyState} methods.
 *
 * Note that this will not provide meaningful state data unless the application server back-end supplies
 * storage and retrieval of the state data and the connection object implements the getPersistedState method.
 *
 * @author Jeff Gitter
 * @docauthor Jeff Gitter
 */
Ext.define('RP.state.AjaxProvider', {
    extend: 'RP.state.AbstractProvider',

     // @inheritdoc
    _buildRequest: function(record, operation) {
        var options = {
            scope: this,
            callback: this._onSaveCallback,
            method: 'POST',
            record: record,
            operation: operation,
            jsonData: [this._writer.getRecordData(record)],
            params: {},
            pathVars: {
                module: this._getModule(),
                taskflow: this._getTaskflow(),
                task: this._getTask(),
                stateId: record.get('stateId')
            }
        };

        if (operation === this.self.operation.SET) {
            Ext.apply(options, {
                url: this.saveUrl || this.url
            });
        } else if (operation === this.self.operation.CLEAR) {
            Ext.apply(options, {
                url: this.clearUrl || this.url
            });
        }

        return RPUX.buildRequest(options);
    }
});
//////////////////////
// ..\js\state\State.js
//////////////////////
/**
 * @private
 * This model defines the state of a component on a specific module, taskflow and task.
 * @author Jeff Gitter
 */
Ext.define('RP.state.State', {
    extend: 'Ext.data.Model',

    fields: ['module', 'taskflow', 'task', 'stateId', 'data', {
        name: 'global',
        type: 'boolean'
    }],
    validations: [
        {type: 'presence', field: 'module'},
        {type: 'presence', field: 'taskflow'},
        {type: 'presence', field: 'task'},
        {type: 'presence', field: 'stateId'},
        {type: 'presence', field: 'data'}
    ]
});
//////////////////////
// ..\js\taskflow\Actions.js
//////////////////////
/**
 * @class RP.taskflow.Actions
 * @extends Ext.container.Container
 * @private
 *
 * A collection of buttons that will be added to the BaseTaskForm's header
 * and allow taskform-wide actions to be undertaken. (e.g. Page refresh,
 * component print / export)
 */
Ext.define('RP.taskflow.Actions', {
    extend: 'Ext.container.Container',
    alias: 'widget.rptaskflowactions',

    /**
     * @cfg {Boolean} allowExport
     * True to add an export button.
     */
    allowExport: false,

    /**
     * @cfg {Boolean} allowPrint
     * True to add a print button.
     */
    allowPrint: false,

    /**
     * @cfg {Boolean} allowRefresh
     * True to add a refresh button.
     */
    allowRefresh: false,

    cls: 'rp-taskflow-actions-ct',

    /**
     * @cfg {Integer} defaultRefreshInterval
     * By default, when refresh intervals are enabled, the page loads with refresh off until the
     * user chooses a refresh time.  If you set this to one of the refreshIntervals values, the
     * auto-refresh will be turned on and set to that time when the page loads.  It must match
     * one of the refreshIntervals or it will be ignored.
     */
    defaultRefreshInterval: -1,

    /**
     * @cfg {Boolean} enableRefreshInterval
     * Turns on the refresh intervals and freshness timestamp.  This is ignored if allowRefresh is false.
     */
    enableRefreshInterval: false,

    /**
     * @cfg {RP.taskflow.BaseTaskForm} ownerTaskform
     * A reference to the taskform to which this container is attached
     */

    /**
     * @cfg {Integer[]} refreshIntervals=[5,10,15,30,60]
     * Sets up the possible interval lengths for auto-refresh (in seconds).  This is
     * ignored if enableRefreshInterval is false.
     */
    refreshIntervals: [5, 10, 15, 30, 60],

    /**
     * @cfg {String} [timestampCls='rp-taskflow-basetaskform-refresh-timestamp']
     * CSS class to apply to the refresh timestamp container.
     * Only used if {@link RP.taskflow.Actions#enableRefreshInterval} is true
     */
    timestampCls: 'rp-taskflow-basetaskform-refresh-timestamp',

    initComponent: function() {
        Ext.apply(this, {
            items: this.createItems()
        });

        Ext.applyIf(this, {
            layout: 'hbox'
        });

        this.callParent(arguments);
    },

    createItems: function() {
        var items = [];

        if (this.allowExport) {
            this.exportButton = this._createExportButton();
            items.splice(0, 0, this.exportButton);
        }
        if (this.allowPrint) {
            this.printButton = this._createPrintButton();
            items.splice(0, 0, this.printButton);
        }
        if (this.allowRefresh) {
            this.refreshButton = this._createRefreshButton();

            if (this.enableRefreshInterval) {
                items.splice(0, 0, this._createRefreshInterval());
            } else {
                items.splice(0, 0, this.refreshButton);
            }
        }

        return items;
    },

    _createExportButton: function() {
        return Ext.ComponentManager.create({  
            cls: 'rp-taskflow-basetaskform-export-button',
            height: 24,
            itemId: 'exportAction',
            menu: this._getExportMenu(),
            menuAlign: 'tr-br?',
            tooltip: RP.getMessage('rp.common.components.BaseTaskForm.export'),
            width: 34,
            getSplitCls: function() { return ''; }
        }, 'button');
    },

    _createPrintButton: function() {
        return Ext.ComponentManager.create({            
            cls: 'rp-taskflow-basetaskform-print-button',
            height: 24,
            itemId: 'printAction',
            menu: this._getPrintMenu(),
            menuAlign: 'tr-br?',
            tooltip: RP.getMessage('rp.common.components.BaseTaskForm.print'),
            width: 34,
            getSplitCls: function() { return ''; }
        }, 'button');
    },

    _createRefreshButton: function() {
        return Ext.ComponentManager.create({  
            cls: 'rp-taskflow-basetaskform-refresh-button',
            height: 24,
            itemId: 'refreshAction',
            tooltip: RP.getMessage('rp.common.components.BaseTaskForm.refresh'),
            scope: this,
            width: 34,
            handler: this._fireRefreshEvent
        }, 'button');
    },

    _createRefreshInterval: function() {
        this._refreshIntervalTask = Ext.util.TaskManager.newTask({
            run: this._fireRefreshEvent,
            scope: this
        });

        this.refreshButton.cls += ' rp-taskflow-basetaskform-refresh-embedded';

        this.refreshTimestamp = new Ext.container.Container({
            cls: this.timestampCls + ' rp-taskflow-basetaskform-refresh-wrap',
            height: 24,
            width: 80,
            listeners: {
                afterrender: this._refreshStamp,
                scope: this
            }
        });
        this.on('refresh', this._refreshStamp, this);

        this.refreshIntervalButton = new Ext.button.Button({
            border: '1px 1px 1px 0',
            cls: 'rp-taskflow-basetaskform-refresh-interval',
            height: 24,
            itemId: 'refreshIntervalButton',
            menu: this._getIntervalMenu(),
            menuAlign: 'tr-br?',
            tooltip: RP.getMessage('rp.common.components.BaseTaskForm.refreshInterval'),
            width: 17,
            getSplitCls: function() { return ''; }
        });

        return Ext.ComponentManager.create({
            cls: 'rp-taskflow-basetaskform-refresh-wrap',
            items: [ this.refreshTimestamp, this.refreshButton, this.refreshIntervalButton ]
        }, 'container');
    },

    /**
     * @private
     * Build the export menu object
     * @return {Ext.menu.Menu} The export menu object
     */
    _getExportMenu: function() {
        var menu = new Ext.menu.Menu({
            items: [{
                xtype: 'container', hidden: 'true'
            }],
            showSeparator: false
        });

        menu.on('beforeshow', function(me) {
            var list = this._queryExportables();
            //. if there's only one exportable and it only has one format available
            if (list.length === 1 && list[0].exportFormats.length <= 1) {
                var collection = new Ext.util.MixedCollection();
                collection.add(list[0].id, list[0].exportFormats.length === 1 ? list[0].exportFormats[0] : true);
                this._fireExportEvent(collection);
                return false;
            } else {
                me.removeAll(true);
            }
        }, this);

        menu.on('show', function(me) {
            me.alignTo(me.ownerButton, me.ownerButton.menuAlign);
            me.getEl().mask(RP.getMessage('rp.common.misc.LoadingMaskText'));
            var list = this._queryExportables();

            Ext.suspendLayouts();
            if (list.length > 0) {
                if (list.length > 1) {
                    me.add(this._createSelectAllMenuItem());
                }
                Ext.Array.each(list, function(cmp) {
                    var buttons = [];
                    Ext.each(cmp.exportFormats, function(format) {
                        buttons.push({
                            code: format.get('format'),
                            text: format.get('display')
                        });
                    });
                    me.add({
                        xtype: 'menucheckbuttonitem',
                        text: cmp.optionTitle || cmp.title,
                        registeredCmp: cmp,
                        buttons: buttons
                    });
                }, me);
                me.add({
                    xtype: 'button',
                    text: RP.getMessage('rp.common.components.BaseTaskForm.export'),
                    handler: function() {
                        var collection = new Ext.util.MixedCollection();
                        Ext.Array.each(me.query('menucheckitem'), function(item) {
                            if (item._allCheckItem) {
                                return true;
                            }
                            // set value to the value of item.checked.  If item.checked is true,
                            // use the format record instead.
                            var value = item.checked;
                            if (item.checked === true && item.getPressedButton() !== null) {
                                value = item.getPressedButton().code;
                            }
                            collection.add(item.registeredCmp.id, value);
                        });
                        me.hide();
                        this._fireExportEvent(collection);
                    },
                    scope: this
                });
            } else {
                me.add({
                    xtype: 'container',
                    html: RP.getMessage('rp.common.components.BaseTaskForm.noOptions')
                });
            }
            Ext.resumeLayouts(true);

            me.getEl().unmask();
        }, this);

        return menu;
    },

    /**
     * @private
     * Build the interval menu object
     * @return {Ext.menu.Menu} The interval menu object
     */
    _getIntervalMenu: function() {
        this.intervalMenu = new RP.menu.BoundMenu({
            cls: 'rp-taskflow-basetaskform-interval-menu',
            displayField: 'display',
            minWidth: 119,
            showSeparator: false,
            store: this._buildIntervalStore(),
            valueField: 'value'
        });

        this.intervalMenu.on('show', function(me) {
            var max = 0;
            me.items.each(function(item) {
                max = item.getWidth();
            });
            me.minWidth = max;
            me.setWidth(this.refreshIntervalButton.ownerCt.getWidth());
        }, this, { single: true });

        this.intervalMenu.on('change', function(me, value, old, item, oldItem) {
            this._updateRefreshTask(value);
            if (oldItem) {
                oldItem.removeCls('selected');
            }
            item.addCls('selected');
        }, this);

        this.setRefreshInterval(this.defaultRefreshInterval);
        return this.intervalMenu;
    },

    /**
     * @private
     * Update the interval task for the new interval value and restart it
     * @param {Integer} interval length in minutes
     */
    _updateRefreshTask: function(value) {
        this._refreshIntervalTask.stop();
        if (value !== -1) {
            this._refreshIntervalTask.start(value * 60 * 1000);
        }
    },

    /**
     * Set the refresh interval for this taskform.
     * @param {Integer} interval The interval value to set. Ignored if it isn't in the refreshIntervals config.
     */
    setRefreshInterval: function(interval) {
        if (this.enableRefreshInterval) {
            this.intervalMenu.setValue(Ext.Array.contains(this.refreshIntervals, interval) ? interval : -1);
        }
    },

    /**
     * @private
     * Build the store containing intervals for the interval menu
     * @return {Ext.data.Store} The interval store
     */
    _buildIntervalStore: function() {
        var data = [];
        Ext.Array.each(this.refreshIntervals, function(interval) {
            var value = Math.ceil(interval);
            data.push([this._parseInterval(value), value]);
        }, this);

        data.push([RP.getMessage('rp.common.misc.off'), -1]);

        return new Ext.data.Store({
            fields: [{
                name: 'display',
                type: 'string'
            }, {
                name: 'value',
                type: 'integer'
            }],
            data: data,
            proxy: {
                type: 'memory',
                reader: 'array'
            }
        });
    },

    /**
     * @private
     * Turn an integer representing minutes into a locale-based string parsed into
     * hours and minutes.
     * @param {Integer} interval
     * @return {String} Display string based on locale
     */
    _parseInterval: function(interval) {
        var minutes = interval % 60;
        var hours = (interval - minutes) / 60;
        return Ext.String.trim(Ext.String.format((hours > 0 ? '{2} {0}' : '') + (minutes > 0 ? ' {3} {1}' : ''),
                RP.getMessage('rp.common.misc.hour_abbr'),
                RP.getMessage('rp.common.misc.minute_abbr'),
                hours, minutes));
    },

    /**
     * @private
     * Build the print menu object
     * @return {Ext.menu.Menu} The print menu object
     */
    _getPrintMenu: function() {
        var menu = new Ext.menu.Menu({
            items: [{
                xtype: 'container', hidden: 'true'
            }],
            showSeparator: false
        });

        menu.on('beforeshow', function(me) {
            var list = this._queryPrintables();
            if (list.length === 1) {
                var collection = new Ext.util.MixedCollection();
                collection.add(list[0].id, true);
                this._firePrintEvent(collection);
                return false;
            } else {
                me.removeAll(true);
            }
        }, this);

        menu.on('show', function(me) {
            me.alignTo(me.ownerButton, me.ownerButton.menuAlign);
            me.getEl().mask(RP.getMessage('rp.common.misc.LoadingMaskText'));
            var list = this._queryPrintables();

            Ext.suspendLayouts();
            if (list.length > 0) {
                me.add(this._createSelectAllMenuItem());
                Ext.Array.each(list, function(cmp) {
                    me.add({
                        xtype: 'menucheckitem',
                        text: cmp.optionTitle || cmp.title,
                        registeredCmp: cmp
                    });
                }, me);
                me.add({
                    xtype: 'button',
                    text: RP.getMessage('rp.common.components.BaseTaskForm.print'),
                    handler: function() {
                        var collection = new Ext.util.MixedCollection();
                        Ext.Array.each(me.query('menucheckitem'), function(item) {
                            if (item._allCheckItem) {
                                return true;
                            }
                            collection.add(item.registeredCmp.id, item.checked);
                        });
                        me.hide();
                        this._firePrintEvent(collection);
                    },
                    scope: this
                });
            } else {
                me.add({
                    xtype: 'container',
                    html: RP.getMessage('rp.common.components.BaseTaskForm.noOptions')
                });
            }
            Ext.resumeLayouts(true);

            me.getEl().unmask();
        }, this);

        return menu;
    },

    /**
     * @private
     * Builds a menu check item that, when checked, selects
     * every other check item in the same menu.
     */
    _createSelectAllMenuItem: function() {
        return Ext.ComponentManager.create({   
            _allCheckItem: true,
            text: RP.getMessage('rp.common.components.BaseTaskForm.alloptions'),
            listeners: {
                checkchange: function(cmp, checked) {
                    Ext.Array.each(cmp.parentMenu.query('menucheckitem'), function(cmp) {
                        cmp.setChecked(checked);
                    });
                }
            }
        }, 'menucheckitem');
    },

    /**
     * @private
     * Query for all components that are exportable
     */
    _queryExportables: function() {
        return this._query('component[_exportable]', '_exportable', 'getExportablePlugin');
    },

    /**
     * @private
     * Query for all components that are printable
     */
    _queryPrintables: function() {
        return this._query('component[_printable]', '_printable', 'getPrintablePlugin');
    },
    
    /**
     * @private
     * Generic query for all components with a particular property set
     */
    _query: function(selector, propertyName, methodName) {
        var list = [],
            components = this.ownerTaskform.query(selector);

        if (this.ownerTaskform[propertyName] === true) {
            Ext.Array.insert(components, 0, [this]);
        }

        Ext.Array.each(components, function(cmp) {
            if (!cmp[methodName] || cmp[methodName]().disabled === false) {
                list.push(cmp);
            }
        });

        return list;
    },

    /**
     * Fire the export event
     * @param {Ext.util.MixedCollection} The map of printable components to selection value
     */
    _fireExportEvent: function(collection) {
        this.fireEvent('export', collection);
    },

    /**
     * Fire the print event
     * @param {Ext.util.MixedCollection} The map of printable components to selection value
     */
    _firePrintEvent: function(collection) {
        this.fireEvent('print', collection);
    },

    /**
     * Fire the refresh event
     */
    _fireRefreshEvent: function() {
        this.fireEvent('refresh');
    },

    /**
     * @private
     * Refresh the interval timestamp with the current time
     */
    _refreshStamp: function() {
        this.refreshTimestamp.update(
            RP.core.Format.formatDateInUserTimeZone(new Date(), RP.core.Formats.Time.Short));
    }
});
//////////////////////
// ..\js\taskflow\BaseTaskflowWidget.js
//////////////////////
/**
 * The default class for creating taskflows that have widgets.
 */
Ext.define("RP.taskflow.BaseTaskflowWidget", {
    extend: "Ext.panel.Panel",

    margin: '0 0 0 0',

    /**
     * @cfg {Boolean} true to hide the widget if it is the only one
     * in the taskflow.
     */
    hideIfOnlyWidget: true,

    /**
     * @cfg {Boolean} disabledWidget
     * Widget is disabled
     */
    disabledWidget: false,

    width: 216,

    ui: '',

    activeCls: 'rp-widget-prototype-side-active',

    overCls: 'rp-widget-prototype-side-hover',

    cls: 'rp-taskflow-prototype-basetaskflow-widget',


    // exact copy of the normal AbstractPanel renderTpl, but adds in left, right, and middle divs for styling purposes

    // TODO: Refactor styling for docked items so that they render properly without this override

    renderTpl: [
      '<div id="{id}-body" class="{baseCls}-body<tpl if="bodyCls"> {bodyCls}</tpl>',
      ' {baseCls}-body-{ui}<tpl if="uiCls">',
      '<tpl for="uiCls"> {parent.baseCls}-body-{parent.ui}-{.}</tpl>',
      '</tpl>"<tpl if="bodyStyle"> style="{bodyStyle}"</tpl>>',
      '</div>'
    ],

    // These components are docked to the side of the panel so that shadow styles can be applied
    dockedItems: [{
      xtype: 'component',
      dock: 'right',
      cls: 'rp-widget-panel-center-right'
    }, {
      xtype: 'component',
      dock: 'left',
      cls: 'rp-widget-panel-center-left'
    }],

    /**
     * @cfg {String} hyperlinkCls
     * The CSS class used for displaying non-status widgets.  This should probably be the
     * standard CSS class applied to widgets, and it should be removed/overridden for status
     * widgets as this behavior seems to be backwards.  This change would require a fairly
     * substantial refactoring of the rp-taskflow.css file.
     */
    hyperlinkCls: "rp-widget-hyperlink",

    /**
     * Read-only. The reference to the task form
     * @type Object
     */
    taskForm: null,

    titleAlign: (RP.globals.NAV_REGION === 'east' ? 'right' : 'left'),

    // Get rid of styling applied by default
    // Don't set border: false here, or we'll inherit a bunch of
    // !important CSS that we'd rather not deal with. Let the
    // borderCls defined above handle the border styles.
    frame: false,
    shadow: false,

    initComponent: function() {
          this.addEvents(        /**
         * @event rpext-widget-setEnabled
         * Fires a widget has been setEnabled
         * @param {Ext.Component} this
         * @param {Boolean} enabled
         */
        "rpext-widget-setEnabled");

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

        this.title = this._getWidgetTitle();

        Ext.apply(this, this.initialConfig);

        Ext.apply(this, {
            header:  {xtype: 'rpBaseTaskflowWidgetHeader'}
        });


        this.callParent(arguments);

        if (this.disabledWidget) {
            this.addCls("rp-disabled-widget");
        }

        this.addEvents("rpext-widgetclick", "rpext-widgetchange");

        if (this.initialConfig.appEventHandlers) {
            RP.event.AppEventProxy.registerListener(this, true);

            Ext.each(this.initialConfig.appEventHandlers, function(eh) {
                RP.event.AppEventProxy.subscribe(eh.event, this, eh.handler, eh.removeDuplicates);
            }, this);
        }

        this.on("destroy", function() {
            this._taskflow = null; // dereference
        }, this);

        this.on('afterrender', this._onAfterRender, this);
    },

    /**
     * Function to call when render completes
     * @private
     */
    _onAfterRender: function() {
        this.mon(this.header, "click", this.onClick, this);
        this.mon(this.body, "click", this.onClick, this);

        if (this.onPostRender) {
            this.onPostRender();
        }
    },

    /**
     * Function to call after render is done
     * @private
     */
    onPostRender: function() {
        this.setActive(this.activeWidget);
        this._writeLine();
        this.el.addClsOnClick("rp-pressed-item");
        this.el.addClsOnOver("rp-hover-item");
    },

    /**
     * Overrideable method to create default task context if the URL doesn't specify a
     * task context.
     */
    createDefaultTaskContext: function() {
        return {};
    },

    /**
     * Gets the local context object
     */
    getLocalContext: function() {
        return this.getTaskflow().getTaskMetadata(this.getTaskId()).getLocalContext();
    },

    /**
     * Gets the value of a local context variable
     * @param {String} name The value of [name] in the local context
     */
    getLocalContextValue: function(name) {
        return this.getLocalContext()[name];
    },

    /**
     * Sets the value of a local context variable
     * @param {String} name Name of the variable to set
     * @param {Object/Object[]} value Value to set it to
     */
    setLocalContextValue: function(name, value) {
        var lc = this.getTaskflow().getTaskMetadata(this.getTaskId()).getLocalContext();
        lc[name] = value;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Called by the taskflow engine to set the taskflow
     * @param {RP.interfaces.ITaskflow} taskId The task ID
     */
    setTaskflow: function(tf) {
        this._taskflow = tf;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Return the taskflow reference set by setTaskflow()
     * @return {RP.interfaces.ITaskflow} The taskflow
     */
    getTaskflow: function() {
        return this._taskflow;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Sets the task ID to associate with this widget
     * @param {String} taskId The task ID
     */
    setTaskId: function(taskId) {
        this._taskId = taskId;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Gets the task ID associated with this widget
     * @return {String} taskId The task ID
     */
    getTaskId: function() {
        return this._taskId;
    },

    /**
     * @method initializeLocalContext
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Initialize the task's local context at runtime. Designed to be overriden
     * by subclasses.
     */
    initializeLocalContext: Ext.emptyFn,

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * @return {Boolean} True if this is enabled
     */
    getEnabled: function() {
        return !this.disabledWidget;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Sets this taskflow and widget to be enabled or disabled
     * @param {Boolean} enabled If this should be enabled
     */
    setEnabled: function(enabled) {
        this.disabledWidget = !enabled;
        if (this.disabledWidget) {
            this.addCls("rp-disabled-widget");
        }
        else {
            this.removeCls("rp-disabled-widget");

            // This is necessary in IE.  For some reason, the disabled attribute doesn't go away...
            if (this.header && this.header.rendered) {
                this.header.el.dom.disabled = false;
            }
        }
    },

    /**
     * @method createTaskForm
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * This needs to be overridden if you want to present a UI for the task.
     */
    createTaskForm: Ext.emptyFn,

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * @return {Object} The taskform object
     */
    getTaskForm: function() {
        return this.taskForm;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Fires the widget click event. See also {@link #subscribeWidgetClick}.
     */
    raiseWidgetClick: function() {
        if (!this.disabledWidget) {
            this.fireEvent("rpext-widgetclick", this);
        }
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Applies a callback function to listen to clicks against this
     * widget. See also {@link #raiseWidgetClick} and
     * {@link #unsubscribeWidgetClick}.
     * @param {Function} delegate Function to call on widget click
     */
    subscribeWidgetClick: function(delegate) {
        this.mon(this, "rpext-widgetclick", delegate);
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Removes a callback function which listened to clicks against the widget.
     * See also {@link #subscribeWidgetClick}.
     * @param {Function} delegate Function to remove
     */
    unsubscribeWidgetClick: function(delegate) {
        this.mun(this, "rpext-widgetclick", delegate);
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Fires the widget change event. See also {@link #subscribeWidgetChange}.
     */
    raiseWidgetChange: function() {
        this.fireEvent("rpext-widgetchange");
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Applies a callback function to listen to changes in this widget.
     * See also {@link #raiseWidgetChange} and {@link #unsubscribeWidgetChange}.
     * @param {Function} delegate Function to call on widget change
     */
    subscribeWidgetChange: function(delegate) {
        this.mon(this, "rpext-widgetchange", delegate);
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Removes a callback function which listened to changes in the widget.
     * See also {@link #subscribeWidgetChange}.
     * @param {Function} delegate Function to remove
     */
    unsubscribeWidgetChange: function(delegate) {
        this.mun(this, "rpext-widgetchange", delegate);
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Gets the task's context.
     *
     * @return {Object} The task's current context
     */
    getTaskContext: function() {
        return this._taskContext;
    },

    /**
     * {@link RP.interfaces.ITaskWidget} implementation
     * @param {Object} taskContext The task's context
     */
    setTaskContext: function(taskContext) {
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
     * @method onTaskContextChanged
     * Overridable method for subclasses to handle task context changes. Use
     * {@link #getTaskContext} to retrieve the current task context.
     */
    onTaskContextChanged: Ext.emptyFn,

    /**
     * {@link RP.interfaces.ITaskWidget} implementation.<br />
     * Sets this widget as active or inactive.
     * @param {Boolean} isActive Activity to set
     */
    setActive: function(isActive) {
        if (isActive) {
            this.activeWidget = true;
            this.addCls(this.activeCls);
        }
        else {
            this.activeWidget = false;
            this.removeCls(this.activeCls);
        }
    },

    /**
     * Handles click events
     *
     * @param {Ext.EventObject} ev Event object to response to
     * @private
     */
    onClick: function(ev) {
        if (ev && ev.stopEvent) {
            ev.stopEvent();
            ev.getTarget().blur();
        }

        this.raiseWidgetClick();
    },

    /**
     * Gets the title for the widget
     * @return {String} The title of the widget
     * @private
     */
    _getWidgetTitle: function() {
        // this.uiTitle comes from derived class code, the rest comes from widgetCfg config option from either
        // task registration or taskflow roster item.
        return (this.initialConfig.title || this.initialConfig.uiTitle || this.title || this.uiTitle || "&nbsp;");
    },

    /**
     * Waits until the header is rendered, then checks whether the widget should
     * be disabled, applying changes as necessary.
     * @private
     */
    _writeLine: function() {
        if (!this.header.rendered) {
            this.header.on("render", this._writeLine, this);
        }
        else if (this.disabledWidget) {
            this.header.el.dom.disabled = true;
        }
    },

    /**
     * Reports the initialConfig.itemId value, appended with '-taskform'
     * @return {String}  The taskform item ID
     * @private
     */
    _getTaskFormItemId: function() {
        return this.initialConfig.itemId + "-taskform";
    }
});

RP.iimplement(RP.taskflow.BaseTaskflowWidget, RP.interfaces.ITaskWidget);
//////////////////////
// ..\js\taskflow\BaseTaskflowWidgetHeader.js
//////////////////////
/**
 * @private
 */
Ext.define("RP.taskflow.BaseTaskflowWidgetHeader", {
    extend: "Ext.panel.Header",
    alias: 'widget.rpBaseTaskflowWidgetHeader',
    
    cls: 'rp-taskflow-prototype-basetaskflow-widget-header',
    //+1 adds on for padding
    height: 33,
    
    padding: 0,
    
    arrowConfig: {
        height: 25,
        margin: '0 0 5 0',
        width: 11,
        cls: 'rp-widget-header-arrow'
    },
    
    initComponent: function() {
        //hack 
        //this is better than copying a 150+ initComponent
        this.callParent();
        this.removeAll(false);
        this.add(this.titleCmp);
        this.insert(RP.globals.NAV_REGION === 'east' ? 0 : undefined, this._createArrowComponent());
    },
    
    //private
    _createArrowComponent: function() {
        return new Ext.Component(Ext.apply({}, this.arrowConfig));
    }
});
//////////////////////
// ..\js\taskflow\TaskflowWidgetRegistry.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @class RP.taskflow.TaskflowWidgetRegistry
 * @singleton
 * Maintains a registry of taskflow widgets.
 */
RP.taskflow.TaskflowWidgetRegistry = function() {
  function xtypeFn(appId, xtype) {
    return appId + "-" + xtype;
  }
  
  var registeredWidgets = {};
  
  return {
    /**
     * Registers a new xtype
     * @param {Object} config Widget configuration object.
     * <br /><b>Required:</b><ul>
     * <li>appId: {String} Application identifier; must be lower-case
     * <li>description: {String} Description of this widget
     * <li>xtype: {String} Widget xtype; must be lower-case and unique in the appId space; used as xtype name, where full xtype name = [appId]-[xtype]
     * <li>classRef: {Function/String} Constructor type associated with this widget
     * </ul><b>Optional:</b><ul>
     * <li>paramArray: {Object[]} Input/output parameters list of xtype.
     * </ul>
     */
    register: function(config) {
      // Enforce naming conventions.
      if ((config.appId.length === 0) || (config.appId.toLowerCase() !== config.appId)) {
        RP.throwError("appId must be lower-case and not empty - " + config.appId);
      }
      
      if ((config.xtype.length === 0) || (config.xtype.toLowerCase() !== config.xtype)) {
        RP.throwError("xtype must be lower-case and not empty - " + config.xtype);
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
     * @param {String} appId Application identifier
     * @param {String} xtype Widget xtype name within its application scope
     * @param {Object} config Optional config object to merge
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
     * @return {Object[]} Contains the definitions of the registered widgets
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

/**
 * @member RP
 * @method registerWidget
 * Shorthand for {@link RP.taskflow.TaskflowWidgetRegistry#register}.
 */
RP.registerWidget = RP.taskflow.TaskflowWidgetRegistry.register;

/**
 * @member RP
 * @method getWidgetXtype
 * Shorthand for {@link RP.taskflow.TaskflowWidgetRegistry#getWidgetXtype}.
 */
RP.getWidgetXtype = RP.taskflow.TaskflowWidgetRegistry.getWidgetXtype;
//////////////////////
// ..\js\taskflow\BaseTaskForm.js
//////////////////////
/**
 * @class RP.taskflow.BaseTaskForm
 * @extends Ext.panel.Panel
 *
 * Used as a base class for task forms that are launched by the taskflow widgets.  This
 * class adds Application Event handling for a panel.  TaskflowFrame interfaces with
 * this to automatically set this panel to the Awake or Asleep state when it switches
 * task forms.  A task form configures Application Events to listen to by setting the
 * "appEventHandlers" config option.
 *
 * Although this component extends Ext.panel.Panel, it does not support the standard
 * {@link Ext.panel.Panel#header} config. If one is passed in, it will be ignored.
 * Header text can still be set with {@link Ext.panel.Panel#title}, and items can still 
 * be added to the header using the {@link RP.taskflow.BaseTaskForm#centerTools} and 
 * {@link Ext.panel.Panel#tools} configs.
 *
 * Although not listed as configuration options of BaseTaskForm, this component
 * accepts config options supported by the {@link RP.taskflow.Actions} container and will
 * pass them along when that component is created and added to the taskform header.
 */
Ext.define("RP.taskflow.BaseTaskForm", {
    extend: "Ext.panel.Panel",
    alias: "widget.rptaskform",

    actionConfigs: [
        'allowExport',
        'allowPrint',
        'allowRefresh',
        'defaultRefreshInterval',
        'enableRefreshInterval',
        'refreshIntervals',
        'timestampCls'
    ],

    /**
     * @cfg {Object[]} centerTools
     * A collection of components or config objects which will be inserted
     * into the middle of the panel's header, between the title and any other tools (e.g. print / export)
     */
    centerTools: undefined,

    headerCls: 'rp-taskflow-basetaskform-header',
    headerHeight: (64 - 22 + 16),

    marginString: RP.globals.NAV_REGION === 'west' ? '0 16 10 12' : '0 12 10 16',
    
    /**
     * @cfg {Boolean} noHeader
     * @deprecated Use {@link #noGutter} instead
     * True to render without the 20px gutter
     */

    /**
     * @cfg {Boolean} noGutter
     * True to render the taskform with no additional margins.
     * (i.e. No space between the taskform, taskflow bar (top edge),
     * and widget container (left / right edge, depending on nav region))
     */
    noGutter: false,

    /**
     * @cfg {Boolean}
     * When the taskform is shown show it with the full screen (hide the widget container)
     */
    fullScreen: false,
    
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
            margin: (this.noGutter || this.noHeader) ? '' : this.marginString,
            bodyStyle: 'border-width: 0px;',
            border: false,
            header: this._getHeaderConfig()
        });

        this._maybeEnableFullScreen();

        
        this.callParent(arguments);

        this.addEvents(
            /**
             * @event refresh
             * This event is fired to signal listening components
             * that it is time to refresh.
             */
            'refresh',
            /**
             * @event print
             * This event is fired to signal listening components
             * that it is time to print.
             * @param {Ext.util.MixedCollection} printables A map containing the selection value
             *      (true or false) keyed by the component itself.  Listeners should run
             *      printables.get(this) to determine if they should print.  False indicates the
             *      user chose not to print that component, true indicates a print should take place.
             */
            'print',
            /**
             * @event export
             * This event is fired to signal listening components
             * that it is time to export their data.
             * @param {Ext.util.MixedCollection} exportables A map containing the selection value
             *      (false, true or string format) keyed by the component itself.  Listeners should run
             *      exportables.get(this) to determine if they should export and in what format.  False
             *      indicates that the user chose not to export that component, true to export in default
             *      format (no {@link RP.ui.Mixins.Exportable#exportFormats} were configured), and a
             *      string value indicates the format the user selected.
             */
            'export'
        );

        if (this.actionContainer) {
            this.relayEvents(this.actionContainer, ['export', 'print', 'refresh']);
        }
    },

    /**
     * @cfg {String}
     * The message that should be displayed when a dirty state is encountered.
     * Override this message with a message id to customize this to an application specific message.
     */
    dirtyWarningMessageId: "rp.common.misc.DirtyWarningMessage",

    /**
     * @method isDirty
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
     * Overrides base {@link Ext.AbstractComponent#destroy} method to unhook
     * app events.
     */
    destroy: function() {
        logger.logTrace("[BaseTaskForm] destroyed itemId: " + this.itemId);
        
        // Unhook myself from AppEventProxy.
        RP.event.AppEventProxy.unregisterListener(this);
        
        this.callParent();
    },

    /**
     * {@link RP.interfaces.ITaskForm} implementation.<br />
     * Handler function for the onActivate event
     * @private
     */
    onActivate: function() {
        logger.logTrace("[BaseTaskForm] onActivate itemId: " + this.itemId);
        
        window.onbeforeunload = Ext.bind(function() {
            if (Ext.isFunction(this.isDirty) && this.isDirty() === true) {
                // Check to see if the user is "actually" leaving the page.
                Ext.defer(this.checkLeavePage, 3000);
                return RP.getMessage(this.dirtyWarningMessageId);
            }
            // The page isn't dirty, since the onbeforeload is firing and 
            // the page isn't dirty, we can safely say the user is leaving the page.
            window.leavingPage = true;
            
        }, this);
        
        // Resume my events (any previously queued events will now fire)
        RP.event.AppEventProxy.setState(this, true);
    },

    /**
     * Checks for active AJAX requests. If any exist, the window is flagged for
     * leavingPage. After 3 seconds, the flag is removed.
     */
    checkLeavePage: function() {
        // Check to see if there are AJAX requests, if so we may be leaving the page.
        if (Ext.Ajax.activeRequests > 0) {
            window.leavingPage = true;
        }
        // Hang out for 3 seconds, if we are still on the page, clear the leaving page flag.
        Ext.defer(function() {
            window.leavingPage = false;
        }, 3000);
    },

    /**
     * {@link RP.interfaces.ITaskForm} implementation.<br />
     * Return false to cancel deactivation of this task
     * @private
     */
    onBeforeDeactivate: function(cb) {
        cb(true);
    },

    /**
     * {@link RP.interfaces.ITaskForm} implementation.<br />
     * Handler function for the onDeactivated event
     * @private
     */
    onDeactivate: function() {
        logger.logTrace("[BaseTaskForm] onDeactivate itemId: " + this.itemId);
        
        // Suspend my events for now (actually queue events while I'm sleeping)...
        RP.event.AppEventProxy.setState(this, false);
    },

    /**
     * {@link RP.interfaces.ITaskForm} implementation.<br />
     * Gets the URL hash
     * @param {String} URL hash
     */
    getUrlHash: function() {
        if (!this.__urlHash) {
            this._urlHash = Ext.id();
        }
        return this.__urlHash;
    },

    /**
     * {@link RP.interfaces.ITaskForm} implementation.<br />
     * Sets the URL hash to the specified hash
     * @param {String} hash New URL hash to set
     */
    setUrlHash: function(hash) {
        this.__urlHash = hash;
    },
    
    /**
     * @private
     * Get the configuration object for the header container of the task form
     * @return {Object} The config object
     */
    _getHeaderConfig: function() {
        var headerTools = [];

        if (this.centerTools) {
            headerTools = [{
                xtype: 'toolbar',
                style: 'border-width: 0px;',
                flex: 1,
                items: this.centerTools,
                // Apparently, toolbar's enableOverflow config only works when
                // a layout is not provided, so we'll have to pass in an explicit
                // overflowHandler to get the behavior that we want.
                layout: {
                    type: 'hbox',
                    overflowHandler: 'Menu',
                    pack: 'center'
                }
            }];
        }

        if (this.tools) {
            headerTools = headerTools.concat(this.tools);
        }

        var config = {
            xtype: 'header',
            height: this.headerHeight,
            baseCls: '',
            cls: this.headerCls,
            layout: 'column',
            tools: headerTools,
            listeners: {
                beforerender: function(header) {
                    if(this.centerTools) {
                        // Ext puts a flex on the title by default.  For centerTools to
                        // actually be centered between the text and any right-aligned tools, 
                        // the title's width should be fixed.
                        delete header.titleCmp.flex;
                    }
                },
                scope: this
            }
        };

        if (this.allowExport || this.allowPrint || this.allowRefresh) {
            this.actionContainer = this._createActionContainer();
            config.tools.push(this.actionContainer);
        }

        return config;
    },

    _createActionContainer: function() {
        var cfg = { ownerTaskform: this },
            props = this.actionConfigs,
            len = props.length,
            i = 0,
            prop;

        for (; i < len; ++i) {
            prop = props[i];
            
            if (prop in this) {
                cfg[prop] = this[prop];
            }
        }

        return Ext.create('RP.taskflow.Actions', cfg);
    },

    /**
     * If the refresh button exists, enable it
     */
    enableRefresh: function() {
        var btn = this.getRefreshButton();
        if (btn) {
            btn.enable();
        }
    },

    /**
     * If the refresh button exists, disable it
     */
    disableRefresh: function() {
        var btn = this.getRefreshButton();
        if (btn) {
            btn.disable();
        }
    },

    /**
     * Get a reference the refresh button
     * @return {Ext.button.Button} The refresh button or null if it isn't there
     */
    getRefreshButton: function() {
        if (!this.actionContainer || !this.actionContainer.refreshButton) {
            return null;
        }

        return this.actionContainer.refreshButton;
    },

    /**
     * If the print button exists, enable it
     */
    enablePrint: function() {
        var btn = this.getPrintButton();
        if (btn) {
            btn.enable();
        }
    },

    /**
     * If the print button exists, disable it
     */
    disablePrint: function() {
        var btn = this.getPrintButton();
        if (btn) {
            btn.disable();
        }
    },

    /**
     * Get a reference the print button
     * @return {Ext.button.Button} The print button or null if it isn't there
     */
    getPrintButton: function() {
        if (!this.actionContainer || !this.actionContainer.printButton) {
            return null;
        }
        
        return this.actionContainer.printButton;
    },

    /**
     * If the export button exists, enable it
     */
    enableExport: function() {
        var btn = this.getExportButton();
        if (btn) {
            btn.enable();
        }
    },

    /**
     * If the export button exists, disable it
     */
    disableExport: function() {
        var btn = this.getExportButton();
        if (btn) {
            btn.disable();
        }
    },

    /**
     * Get a reference to the Export button
     * @return {Ext.button.Button} The export button or null if it isn't there
     */
    getExportButton: function() {
        if (!this.actionContainer || !this.actionContainer.exportButton) {
            return null;
        }
        
        return this.actionContainer.exportButton;
    },

    _maybeEnableFullScreen: function() {
        if (this.fullScreen) {
            this.on('activate', this._onFullScreenActivate, this);
            this.on('deactivate', this._onFullScreenDeactivate, this);
        }
    },

    _onFullScreenActivate: function() {
        RP.getTaskflowFrame().getWidgetContainer().hide();
    },

    _onFullScreenDeactivate: function() {
        RP.getTaskflowFrame().getWidgetContainer().show();
    }
});

RP.iimplement(RP.taskflow.BaseTaskForm, RP.interfaces.ITaskForm);

RP.core.ComponentMgr.register("rptaskform", "RP.taskflow.BaseTaskForm", []);
//////////////////////
// ..\js\taskflow\BaseTaskflowWidgetWithPanel.js
//////////////////////
/**
 * Extends {@link RP.taskflow.BaseTaskflowWidget} to add a panel below the
 * widget which can display additional information.
 */
Ext.define("RP.taskflow.BaseTaskflowWidgetWithPanel", {
    extend:"RP.taskflow.BaseTaskflowWidget",
    
    // These components are docked to the side of the panel so that shadow styles can be applied
    dockedItems: [{
      xtype: 'component',
      dock: 'right',
      cls: 'rp-widget-panel-center-right'
    }, {
      xtype: 'component',
      dock: 'left',
      cls: 'rp-widget-panel-center-left'
    }],
    
    bodyCls: 'rp-taskflow-prototype-basetaskflow-widget-with-panel-widget-body',

    /**
     * @cfg {Ext.Component} panel The panel to include as part of the widget
     */
    
    initComponent: function() {
        var config = this.initialConfig;
        
        config.defaults = Ext.applyIf(config.defaults || {}, {
            border: false,
            width: 180
        });
        config.hasPanel = true;
        config.extraCls = (config.extraCls ? config.extraCls + " " : "") + "rp-widget-with-panel";
        config.items = config.panel;
        
        this.bbar = {
            xtype: "component",
            html: ['<div class="rp-widget-panel-bottom-left">',
                        '<div class="rp-widget-panel-bottom-right">',
                            '<div class="rp-widget-panel-bottom-center"></div>',
                        '</div>',
                    '</div>'].join("")
        };
        
        this.callParent();
        
        // after created, reduce the header height for this widget type
     //   this.header.setHeight(25);
    }
});
//////////////////////
// ..\js\taskflow\ExternalAppWidget.js
//////////////////////
/**
 * A Widget implementation that can be used to embed an external web
 * application or page internally inside an iframe, by creating an
 * {@link RP.taskflow.ExternalAppTaskForm}. 
 */
Ext.define("RP.taskflow.ExternalAppWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
    
    /**
     * @cfg {String} url 
     * The URL to embed inside the iframe.
     */
    
    /**
     * @cfg {String} frameTitle 
     * The title to use for the task form. If not configured then the header of
     * the task form will not be displayed.
     */
    
    createTaskForm: function() {
        this.taskForm = new RP.taskflow.ExternalAppTaskForm({
            url: this.url,
            title: this.frameTitle,
            itemId: this._getTaskFormItemId()
        });
    }
});

RP.registerWidget({
    appId: "stash",
    description: "External app widget",
    xtype: "external-app",
    classRef: "RP.taskflow.ExternalAppWidget"
});
//////////////////////
// ..\js\taskflow\Taskflow.js
//////////////////////
/**
 * Taskflow business object.  This class translates a taskflow in JSON to a taskflow
 * instance hostable by the taskflow engine.
 */
Ext.define("RP.taskflow.Taskflow", {

    mixins: {
        observable: "Ext.util.Observable"
    },
    
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
     * @cfg {Object[]} roster
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
     * <p><b>dependencies</b>: {String[]}
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
        Ext.apply(this, config);
        this.mixins.observable.constructor.call(this);
        
        // Set up properties.
        this.initialConfig = Ext.apply({}, config);
        this.flowContext = Ext.apply(config.initialContext || {}, {});
        
        // initialContext should affect this.flowContext only, and not affect this._context...
        //this._context = RP.mergeProperties(config.initialContext || {}); 
        this._context = {};
        
        this._initialized = false;
        this._completed = false;
        this._roster = null;
        
        this.addEvents(
        /**
         * @event rpextinit
         * Fires when this taskflow has begun initialization. Full name "rpext-init"
         * @param {RP.taskflow.Taskflow} this
         * @param {Function} continueFn
         */
        "rpext-init",
        /**
         * @event rpextinitialized
         * Fires after this taskflow has initialized. Full name "rpext-initialized"
         * @param {RP.taskflow.Taskflow} this
         */
        "rpext-initialized",
        /**
         * @event rpextstarting
         * Fires before this taskflow runs. Full name "rpext-starting"
         * @param {RP.taskflow.Taskflow} this
         */
        "rpext-starting",
        /**
         * @event rpexttaskcompleted
         * Fires when a task in this taskflow is completed. Full name "rpext-task-completed"
         * @param {RP.taskflow.Taskflow} this
         * @param {Object} taskConfig
         */
        "rpext-task-completed",
        /**
         * @event rpextcompleted
         * Fires after this taskflow has completed. Full name "rpext-completed"
         * @param {RP.taskflow.Taskflow} this
         */
        "rpext-completed");
        
        if (Ext.isObject(config.listeners)) {
            var l = config.listeners;
            var scope = l.scope || this;
            
            if (l.init) {
                this.addInitListener(Ext.bind(l.init, scope));
            }
            
            if (l.afterInit) {
                this.addPostInitListener(Ext.bind(l.afterInit, scope));
            }
            
            if (l.beforeStart) {
                this.addTaskflowStartListener(Ext.bind(l.beforeStart, scope));
            }
            
            if (l.complete) {
                this.addTaskflowCompletedListener(Ext.bind(l.complete, scope));
            }
        }
    },
    
    /**
     * Called to get the taskflow context
     * 
     * @return {Object} The context of this taskflow
     */
    getTaskflowContext: function() {
        return this._context;
    },
    
    /**
     * Called to merge new values into the taskflow context. 
     * See {@link RP.core.PageContext#updateTaskflowContext}.
     * 
     * @param {Object} context The taskflow context object to set to
     */
    mergeTaskflowContext: function(context) {
        this._context = RP.mergeProperties(this._context, context);
        RP.core.PageContext.updateTaskflowContext(context);
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    getTaskflowUIConfig: function() {
        return this.initialConfig.uiConfig;
    },
    
    /** Add a delegate to call when this task flow is beginning to initialize
     * {@link RP.interfaces.ITaskflow} implementation
     * 
     * @param {Function} cb Function to call on rpext-init
     */
    addInitListener: function(cb) {
        this.on("rpext-init", cb);
    },
    
    /**
     * Add a delegate to call when this task flow has initialized
     * {@link RP.interfaces.ITaskflow} implementation
     * 
     * @param {Function} cb Function to call on rpext-initialized
     */
    addPostInitListener: function(cb) {
        this.on("rpext-initialized", cb);
    },
    
    /**
     * Add a delegate to call when this taskflow is started to run.
     * 
     * @param {Function} cb Function to call on rpext-starting
     */
    addTaskflowStartListener: function(cb) {
        this.on("rpext-starting", cb);
    },
    
    /**
     * Add a delegate to call when this task has completed
     * 
     * @param {Function} cb Function to call on rpext-task-completed
     */
    addTaskCompletedListener: function(cb) {
        this.on("rpext-task-completed", cb);
    },
    
    /**
     * Add a delegate to call when this taskflow has completed.
     * 
     * @param {Function} cb Function to call on rpext-completed
     */
    addTaskflowCompletedListener: function(cb) {
        this.on("rpext-completed", cb);
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    setRebuildHandler: function(cb) {
        this._rebuildHandler = cb;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    rebuild: function(config) {
        this._rebuildHandler(config);
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    isInitialized: function() {
        return this._initialized;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    isCompleted: function() {
        return this._completed;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     * An overridden implementation must call this superclass method last.
     */
    initTaskflow: function(initCompletedFn) {
        this._completed = false;
        this._roster = new RP.collections.MixedCollection();
        
        var evtName = "rpext-init";
        
        if (Ext.isObject(this.events[evtName])) {
            var count = this.events[evtName].listeners.length;
            
            var continueFn = function() {
                count--;
                if (count === 0) {
                    this._signalInitialized(initCompletedFn);
                }
            };
            
            this.fireEvent(evtName, this, Ext.bind(continueFn, this));
        }
        else {
            this._signalInitialized(initCompletedFn);
        }
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    getTaskflowDescription: function() {
        if (this.initialConfig.uiConfig) {
            return this.initialConfig.uiConfig.description;
        }
        return null;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    setTaskflowDescription: function(desc) {
        this.initialConfig.uiConfig = this.initialConfig.uiConfig || {};
        this.initialConfig.uiConfig.description = desc;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    getDefaultTaskId: function() {
        return this.initialConfig.defaultTaskId;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     */
    getTaskflowRoster: function(tfConfig) {
        return this.initialConfig.roster;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
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
     * {@link RP.interfaces.ITaskflow} implementation
     */
    getTaskMetadata: function(taskID) {
        return this._roster.get(taskID).taskContext;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
     * An overridden implementation must call this method after it has done its custom handling.
     */
    startTaskflow: function(tfConfig) {
        this.fireEvent("rpext-starting", this);
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
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
                Ext.Array.remove(waitObjs, rosterItem);
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
        
        var tfCompletedCalcFn = Ext.bind(function() {
            var taskflowComplete = true;
            
            this._roster.each(function(rosterItem) {
                taskflowComplete = taskflowComplete && rosterItem.completed;
            }, this);
            
            if (taskflowComplete) {
                this._completed = true;
                this.fireEvent("rpext-completed", this);
            }
        }, this);
        
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
     * {@link RP.interfaces.ITaskflow} implementation
     */
    isTaskCompleted: function(taskID) {
        var rosterItem = this._roster.get(taskID);
        return rosterItem.completed;
    },
    
    /**
     * {@link RP.interfaces.ITaskflow} implementation
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
        
        var statusResultFn = Ext.bind(function(result) {
            this._setTaskCompleted(rosterItem, result);
            waitedResultFn(rosterItem);
        }, this);
        
        if (taskCfg.completeTaskOnClick) {
            return rosterItem.completed;
        }
        else if (!Ext.isDefined(statusCalculation)) {
            this._setTaskCompleted(rosterItem, true); // this will cause the "task completed" event to fire...
        }
        else if (Ext.isString(statusCalculation)) {
            logger.logTrace(Ext.String.format("[Taskflow] Performing eval() on '{0}' to set statusCalculation for '{1}'.", statusCalculation, localContext.itemId));
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
// ..\js\taskflow\BaseTaskFormWithDateMenu.js
//////////////////////
/**
 * A simple extension of {@link RP.taskflow.BaseTaskForm} that allows you to
 * add a date menu in the header area.
 *
 * @cfg {Object} dateMenuConfig The configuration object of the {@link RP.ui.DirectionalDateField}.
 */
Ext.define("RP.taskflow.BaseTaskFormWithDateMenu", {
    extend: "RP.taskflow.BaseTaskForm",

    /**
     * @cfg {Object} dateMenuConfig The configuration object of the
     * {@link RP.form.DirectionalDateField}.
     */

    initComponent: function() {
        if (!Ext.isDefined(this.dateMenuConfig)) {
            this.dateMenuConfig = {};
        }

        this._dateMenu = new RP.form.DirectionalDateField(this.dateMenuConfig);

        this.plugins = this.plugins || [];

        this.plugins.push(new RP.panel.plugin.HeaderComponent({
            item: this._dateMenu
        }));

        this.callParent();
    },

    /**
     * Retrieves the date menu reference for external access purposes.
     * @return {RP.form.DirectionalDateField} The date picker of this panel.
     */
    getDateMenu: function() {
        return this._dateMenu;
    }
});
//////////////////////
// ..\js\taskflow\ExternalAppTaskForm.js
//////////////////////
/**
 * Implements a task form that renders an IFRAME for external web apps, using
 * {@link Ext.ux.IFrameComponent}.
 * 
 */
Ext.define("RP.taskflow.ExternalAppTaskForm", {
    extend: "RP.taskflow.BaseTaskForm",

    /**
     * @cfg {String} url 
     * The URL to embed inside the iframe.
     */

    initComponent: function() {
        var iframe = new Ext.ux.IFrameComponent({
            url: this.url || "about:blank"
        });

        Ext.apply(this, {
            items: iframe,
            noHeader: this.title ? false : true,
            layout: "fit"
        });

        this.callParent();
    }
});

RP.core.ComponentMgr.register("rpexternalapp", "RP.taskflow.ExternalAppTaskForm", []);
//////////////////////
// ..\js\taskflow\TaskContext.js
//////////////////////
/**
 * Task context business object.  Contains task configuration as well as 
 * runtime data, etc.
 */
Ext.define("RP.taskflow.TaskContext", {

    mixins: {
        observable: "Ext.util.Observable"
    },
    
    constructor: function(config) {
        this._taskCfg = config.taskCfg;
        this._localContext = Ext.apply({}, config.initialContext);
        
        this.addEvents(
               /**
                 * @event statuschanged
                 * Fired when the status of this taskflow changes
                 */
                "statuschanged",
                /**
                 * @event localcontextchanged
                 * Fired when the local context of this taskflow changes
                 */
                "localcontextchanged"
        );
        
        this.callParent(arguments);
    },
    
    /**
     * Get the task configuration
     * @return {Object} The task configuration object
     */
    getConfig: function() {
        return this._taskCfg;
    },
    
    /**
     * RP.interfaces.ITaskContext implementation.
     * Subscribe an event handler to the task's status change event.
     * @param {Function} handler Handler function for the status change event
     */
    subscribeStatusChange: function(handler) {
        this.on("statuschanged", handler);
    },
    
    /**
     * RP.interfaces.ITaskContext implementation.
     * Unsubscribe an event handler from the task's status change event.
     * @param {Function} handler Handler function to remove from the listeners
     */
    unsubscribeStatusChange: function(handler) {
        this.un("statuschanged", handler);
    },
    
    /**
     * RP.interfaces.ITaskContext implementation.
     * Fires the task's status change event.
     */
    fireStatusChange: function() {
        this.fireEvent("statuschanged", this);
    },
    
    /**
     * RP.interfaces.ITaskContext implementation.
     * Gets the task's runtime "local context".  The "local context" is a glob
     * of data generated at runtime pertaining to this task
     * @param {Function} handler Unused handler function.
     * @return {Object} local context object
     */
    getLocalContext: function(handler) {
        return this._localContext;
    }
});

RP.iimplement(RP.taskflow.TaskContext, RP.interfaces.ITaskContext);
//////////////////////
// ..\js\taskflow\TaskflowFrame.js
//////////////////////
/**
 * @class RP.taskflow.TaskflowFrame
 * @extends Ext.panel.Panel
 */
Ext.define("RP.taskflow.TaskflowFrame", {
    extend: "Ext.panel.Panel",

    /**
     * @cfg {Array} taskflows (Required) <p>Array of taskflows to include</p>
     */
    /**
     * @cfg {Array} modules (Required) <p>Array of modules to include</p>
     */
    /**
     * @cfg {String} navRegion <p>The taskflow navigation panel placement - "west" or "east"</p>
     */
    /**
     * @cfg {Number} navInitialSize <p>The taskflow navigation panel width; default is 200</p>
     */
    /**
     * @cfg {Ext.Component} navArea <p>Panel or component to put in the north region of the
     * navigation panel</p>
     */
    /**
     * @cfg {Number} navAreaHeight <p>navArea height.  Defaults to navArea.height, or 50 if neither specified</p>
     */
    /**
     * @cfg {Boolean} hideAccordion <p>For single taskflow, hide accordion in the
     * navigation panel</p>
     */
    /**
     * @cfg {Ext.Component} blankForm <p>Panel or component to initially render in the task form area.  This
     * will only display if there are no taskflows</p>
     */
    activeWidget: undefined,

    
    //maximizeTasks: false,
    
    initComponent: function() {
        logger.logTrace("[TaskflowFrame] initComponent; itemId: " + this.itemId);
        
        this._onAppActivatedFn = Ext.bind(this._onAppActivated, this);
        this._rebuildTaskflowFn = Ext.bind(this.rebuildTaskflow, this);
        this._tfAbortHandlerFn = Ext.bind(this._tfAbortHandler, this);
        this._beforeAppActivatedFn = Ext.bind(this._beforeAppActivatedHandler, this);
        
        this._initializeHistory();
        
        this._viewContainer = this._createViewContainer();
        
        var t = this._createTaskflows();
        
        this._rpTaskflowsContainer = this._createTaskflowContainer();
        
        Ext.apply(this, {
            bodyStyle: 'border: 0;',
            itemId: this.itemId || "taskflowFrame",
            layout: {
                type: 'border',
                targetCls: 'rp-transparent-panel'
            },
            items: this._createItems()
        });
        
        this.buildModules(this.modules); 
        
        this.callParent(arguments);
        
        if (t.waiter) {
            t.waiter.resume();
        }
        else {
            this._addTaskflowContainers(t.taskflows);
        }
        
        this._customTaskflowViews = {};
    },
    
    _createItems: RP.abstractFn,
    
    _getViewPanelLayoutConfig: function() {
        return {
            type: "card",
            deferredRender: true
        };
    },
    
    /**
     * Builds all of the modules from {@link #modules}. 
     * @param {Object[]} modules Module from {@link RP.taskflow.ModuleRegistry}
     */
    buildModules: function(modules) {
        var items = [];
        
        if (!Ext.isEmpty(modules)) {
            Ext.each(modules, function(module, index) {
                var icon = RP.globals.paths.STATIC + module.icon;
                
                if (Ext.isEmpty(module.icon)) {
                    logger.logError("[TaskflowFrame] No module icon configured for: " + module.name);
                    icon = Ext.BLANK_IMAGE_URL;
                }
                
                items.push({
                    title: Ext.String.format('<img src="{0}" /><br/>{1}', icon, RP.getMessage(module.label)),
                    module: module,
                    layout: "anchor",
                    cls: "rp-module-contents-panel",
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
     * Activate a taskflow programatically.
     *
     * @param {String/RP.interfaces.ITaskflow} tf The taskflow name or 
     * RP.interfaces.ITaskflow instance
     * @param {Boolean} activateDefaultWidget Activate the default widget.
     * Defaulted to true.
     * @param {Function} activatedFn Function to call on activation.
     */
    activateTaskflow: function(tf, activateDefaultWidget, activatedFn) {
        var tfc = this._getTaskflowContainer(tf);
        if (Ext.isEmpty(activateDefaultWidget)) {
            activateDefaultWidget = true;
        }
        this._activateTaskflow(tfc, activateDefaultWidget, activatedFn);
    },
    
    /**
     * Rebuild the specified taskflow by getting rid of it and creating another
     * instance with new context
     * @param {String/RP.interfaces.ITaskflow} tf The taskflow name or 
     * RP.interfaces.ITaskflow instance
     * @param {String} title (Optional) New title. If null, inherits current
     * title
     * @param {Object} initialContext (Optional) New initialContext to create
     * the taskflow with
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
            isTitleMessageName: tfc.isTitleMessageName || false,
            initialContext: initialContext
        });
        var idx = Ext.Array.indexOf(this._rpTaskflowsContainer.items, tfc);
        var newTfc = Ext.ComponentMgr.create(tfCfg);
        
        this._rpTaskflowsContainer.remove(tfc, true);
        this._rpTaskflowsContainer.insert(idx, newTfc);
        
        if (expandNewTfc) {
            var hash = window.location.hash;
            this._activateTaskflow(tfc, Ext.isEmpty(hash) || hash === ":", Ext.bind(function(taskFlowContainer) {
                this.navigateToURLHash(hash);
            }, this));
        }
        
        // Destroy the discarded TaskflowContainer, which should also destroy all taskforms
        // its widgets contain. It does not, however, destroy the widget container itself, so we
        // need to do that manually. newTfc has its own widget container; we don't want to leave
        // this one lying around.
        tfc.destroy();
        tfc._widgetContainer.destroy();
    },
    
    /**
     * Add a new taskflow.
     * @param {Ext.Component} tfc The taskflow container
     * @param {Boolean} activateTF Make the new taskflow active?
     * @param {Boolean} activateDefaultWidget Activate the default widget? 
     * Applicable only if activateTF is true
     * @param {Function} activatedFn Delegate to invoke after taskflow has 
     * activated.  Applicable only if activateTF is true
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
        
        if (activateTF) {
            this._activateTaskflow(tfc, activateDefaultWidget, activatedFn);
        }
    },
    
    /**
     * Remove a taskflow.
     * @param {Ext.Component} tfc The taskflow container
     * @param {Boolean} deferLayout (Unused) Specifies whether refreshing
     * should be deferred.
     */
    removeTaskflow: function(tfc, deferLayout) {
        logger.logInfo("[TaskflowFrame] removeTaskflow called with itemId: " + tfc.initialConfig.itemId);
        
        //remove from container
        this._rpTaskflowsContainer.remove(tfc);
        tfc.destroy();
    },
    
    /**
     * Navigate to a specific task in the current module.
     * @param {String} urlHash The URL hash containing module, taskflow, and
     * task names
     */
    navigateToURLHash: function(urlHash) {
        logger.logTrace("[TaskflowFrame] navigateToHash() called: " + urlHash);
        
        if (this._rpTaskflowsContainer.items.getCount() === 0) {
            logger.logTrace("[TaskflowFrame] navigateToHash() - no taskflows, so ignoring...");
            return;
        }
        
        Ext.defer(this._activateDefaultWidget, 100, this, [urlHash]);
    },
    
    /**
     * Returns the TaskflowContainer containing the specified taskflow
     * @private
     * @param {String/RP.interfaces.ITaskflow} tf An RP.interfaces.ITaskflow instance or taskflow name
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
                logger.logError(Ext.String.format('[TaskflowFrame] Taskflow "{0}" has a String scheduleCheckFn of "{1}" that did not actually evaluate to a function that exists, so it will be processed as normal."',
                                              tfConfig.taskflow, scheduleCheckFn));
            }
        }
        
        if (Ext.isFunction(scheduleCheckFn)) {
            logger.logTrace("[TaskflowFrame] Checking schedule for taskflow: " + tfConfig.taskflow);
            
            beginDeferHandler(tfConfig);
            
            var resultFn = Ext.bind(function(result) {
                logger.logTrace("[TaskflowFrame] Schedule check result for taskflow '" + tfConfig.taskflow + "': " + result);
                // the handler must be executed asyncrhonously because otherwise if the taskflow's schedule check
                // executes synchronously, the taskflow will be removed (unsuccessfully) before it was ever added
                Ext.defer(endDeferHandler, 1, this, [tfConfig, !!result]);
            }, this);
            
            var tfInitContext = RP.util.Object.mergeProperties(tfConfig.initialContext, RP.taskflow.TaskflowRegistry.getInitialContext(tfConfig.taskflow));
            
            scheduleCheckFn(resultFn, tfInitContext);
        }
    },
    
    getModules: function(){
        return this._modules;
    },
    
    /**
     * A simple method to overwrite the item template to change its
     * href from "#" to "javascript:void(0)"
     * http://jira.redprairie.com/browse/RPWEB-3934
     * @private
     */
    _getModuleTabItemTpl: function() {
        var tabTemplate = new Ext.Template('<li class="{cls}" id="{id}"><a class="x-tab-strip-close"></a>', '<a class="x-tab-right" href="javascript:void(0)"><em class="x-tab-left">', '<span class="x-tab-strip-inner"><span class="x-tab-strip-text {iconCls}">{text}</span></span>', '</em></a></li>');
        tabTemplate.disableFormats = true;
        tabTemplate.compile();
        return tabTemplate;
    },
    
    /**
     * Create the taskflow container.
     * @private
     */
    _createTaskflowContainer: function() {
        return Ext.ComponentMgr.create({
            id: "_rp_taskflow_container",
            xtype: "panel",
            style: this.initialConfig.hideAccordion ? "padding-top: 12px" : "",
            border: false,
            header: false,
            region: "center",
            layout: {
                type: this.hideAccordion ? "fit" : "accordion",
                animate: false
            },
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
        var t = {
            taskflows: []
        };
        var beginDeferHandler = Ext.bind((function(tfConfig) {
            if (!t.waiter) {
                t.waiter = new RP.util.Waiter({
                    handler: Ext.bind(this._addTaskflowContainers, this, [t.taskflows])
                });
                t.waiter.suspend();
            }
            t.waiter.add(tfConfig);
        }), this);
        
        var endDeferHandler = function(tfConfig, result) {
            if (!result) {
                Ext.Array.remove(t.taskflows, tfConfig);
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
        logger.logInfo("[TaskflowFrame] Initializing history.");
        
        // Add history form
        var html = [];
        // Ext history management requires this form
        html.push('<form id="history-form" class="x-hide-display">');
        html.push('<input type="hidden" id="x-history-field" />');
        if (Ext.isIE) {
            html.push('<iframe id="x-history-frame"></iframe>');
        }
        html.push('</form>');
        
        Ext.core.DomHelper.insertHtml('beforeEnd', document.body, html.join());
        
        Ext.util.History.init();
        
        Ext.util.History.on("change", this._handleHistoryChange, this);
    },
    
    /**
     * @private
     */
    _handleHistoryChange: function(token) {
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
        
        // Check for the inactivity dialog, destroying it if visible, then keep the session alive
        if (RP.util.PageInactivityChecker.isInactive()) {
            RP.util.Helpers.keepSessionAlive();
            Ext.getCmp("inactivityWarningDialog").destroy();
        }
        
        // handle module context change
        if (RP.globals.CURRENT_HASH) {
            // If module context changed, reload page...
            var oldModuleCtxStr = Ext.urlEncode(RP.globals.CURRENT_HASH.moduleContext);
            var newHash = RP.core.PageContext.parseHash(token);
            if ((Ext.urlEncode(newHash.moduleContext) !== oldModuleCtxStr) && RP.core.PageContext.isRefreshOnModuleContextChange()) {
                RP.event.AppEventManager.fire(RP.upb.AppEvents.ModuleContextChanged);
            }
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
            
            if ((this._currentHash !== token)) {
                this.activeWidget = widget;
                
                var tfCompleted = itfc.getTaskflow().isCompleted();
                
                if (widget && (widget.getEnabled() || tfCompleted)) {
                    widget.raiseWidgetClick();
                }
                else {
                    itfc.activateDefaultWidget();
                }
            }
        };
        
        // Find taskflow container.
        var found = false;
        var waitObj;
        
        // Update global variable with URL hash properties.
        RP.globals.CURRENT_HASH = hash;
        
        this._rpTaskflowsContainer.items.each(function(c) {
            itfc = RP.iget(c, RP.interfaces.ITaskflowContainer2);
            if (itfc && itfc.getTaskflowName() === hash.tfName) {
                if (!itfc.getTaskflow() || !itfc.getTaskflow().isInitialized()) {
                    waitObj = {
                        itfc: itfc,
                        tfc: c
                    };
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
            var c = this._rpTaskflowsContainer.down(' > [activateTasks]');
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
            
            tfc.activateTasks();
            
            var postCreationFn = function(listenPostInit) {
                logger.logTrace("[TaskflowFrame] postInit for tfc itemId: " + tfc.initialConfig.itemId);
                
                if (tfCreateListener) {
                    itfc.removeTaskflowCreationListener(tfCreateListener);
                }
                
                itfc.getTaskflow().mergeTaskflowContext(hash.taskflowContext);
                
                if (listenPostInit) {
                    itfc.getTaskflow().addPostInitListener(Ext.bind(function(tf) {
                        activateWidgetFn.call(me, itfc, tfc, hash);
                    }, this));
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
    },
    
    _parseHistoryToken: function(token) {
        logger.logTrace("[TaskflowFrame] Parsing history token: " + token);
        return RP.core.PageContext.parseHash(token);
    },
    
    /** @private */
    _activateTaskflow: function(tfc, activateDefaultWidget, activatedFn) {
        logger.logTrace("[TaskflowFrame] Expanding taskflow container with itemId: " + tfc.initialConfig.itemId);
        this.taskflowsEnabled = true;
        
        tfc.activateTasks();
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
        var tfcxtype = this.tfContainerXtype || "rptfcontainer";
        
        return RP.mergeProperties({
            xtype: tfcxtype,
            taskflowFrame: this,
            widgetContainerXtype: tfcCfg.widgetContainerXtype,
            taskflowFrameWidgetContainer: this._widgetContainer,
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
    
    /**
     * @private
     */
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
            
            // If the taskflows are not enabled, we should hide the taskflows initially.
            if(!this.initialConfig.taskflowsEnabled) {
                tfCfg.hidden = true;
            }
            
            logger.logTrace("[TaskflowFrame] Creating taskflow container with itemId: " + tfDef.itemId + "; tfContainerXtype: " + tfCfg.xtype);
            tfcs.push(Ext.ComponentManager.create(tfCfg));
        }, this);
        
        this._rpTaskflowsContainer.add(tfcs);
        logger.logTrace("[TaskflowFrame] Taskflow containers added and laid out.");
        
        this._activateDefaultWidget(window.location.hash);
        
        // Navigate to specified task as indicated by URL hash...
        if (!(this.initialConfig.taskflowsEnabled && tfcs.length > 0)) {
            // Taskflows have been disabled, so suspend all events for the taskflowContainer.
            // This will prevent the onRender and doLayout events from firing, which
            // Will cause the taskflows to render despite the initialConfig.taskflowsEnabled value.

            this._rpViewPanel.getLayout().setActiveItem(this._rpViewPanel.items.first());
            this._rpViewPanel.show();
        }
    },
    
    /** @private */
    _onAppActivated: function(tfc, taskForm) {
        //Hide it until the first activation
        if (!this._hasBeenActivated) {
            this._hasBeenActivated = true;
            this._rpViewPanel.show();
        }
        
        logger.logTrace("[TaskflowFrame] _onAppActivated; tfc itemId: " + tfc.initialConfig.itemId + "; taskForm itemId: " + taskForm.itemId);
        
        var itfc;
        var layout = this._rpViewPanel.getLayout(); // the Card layout
        var activeItem = layout.activeItem;
        
        
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
        
        if (hasOldActiveTaskForm && activeItem) {
            RP.iget(activeItem, RP.interfaces.ITaskForm).onDeactivate();
        }
        
        layout.setActiveItem(taskForm);
        
        activeItem = layout.activeItem;
        var itf = RP.iget(activeItem, RP.interfaces.ITaskForm);
        
        itf.onActivate();
        var newHash = itf.getUrlHash();
        
        if (newHash) {
            Ext.History.add(newHash, true);
        }
        
        this._currentHash = newHash || "";
        
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

        if(this._helpRPContainer && this._helpRPContainer.isVisible()) {
            this._helpRPContainer.hide();
        }
    },
    
    /** @private */
    _activateDefaultWidget: function(hash) {
        if (hash) {
            var loadFn = function(urlHash) {
                Ext.History.fireEvent("change", urlHash);
            };
            if (typeof hash === "string" && hash.charAt(0) === '#') {
                hash = hash.slice(1);
            }
            Ext.defer(loadFn, 10, this, [hash]);
        }
        else {
            // Expand the first taskflow container and ask it to activate the default widget.
            var tfc = this._rpTaskflowsContainer.items.first();
            if (tfc) {
                var fn = function() {
                    
                    tfc.activateTasks();
                    
                    var itfc = RP.iget(tfc, RP.interfaces.ITaskflowContainer2);
                    itfc.activateDefaultWidget();
                    
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
            this._rpTaskflowsContainer.getLayout().setActiveItem((idx === 0) ? 1 : 0);
            this._rpTaskflowsContainer.remove(tfContainer);
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
        var layout = this._rpViewPanel.getLayout(); // the Card layout
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
                items: [{
                    xtype: "box",
                    flex: 0.5
                }, {
                    xtype: "panel",
                    bodyCfg: {
                        cls: "rp-taskflow-blank-form-text",
                        align: "center"
                    },
                    border: false,
                    layout: "fit",
                    itemId: "landingPageText",
                    html: RP.getMessage(messageId)
                }, {
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
                
                }, {
                    xtype: "box",
                    flex: 0.5
                }]
            };
        }
        
        return Ext.ComponentMgr.create(landingPage);
    },
    
    /**
     * @private Create the view container
     */
    _createViewContainer: function() {
        var viewPanel;
        this._rpViewPanel = viewPanel = this._createViewPanel();
        
        var viewContainer = Ext.create('RP.taskflow.prototype.ViewContainer', {
            navRegion: RP.globals.NAV_REGION,
            itemId: "viewContainer",
            region: 'center',
            viewPanel: viewPanel
        });
        
        this._widgetContainer = viewContainer.getWidgetContainer();
        
        return viewContainer;
    },
    
    /**
     * Creates a new {@link Ext.panel.Panel} to be used as the view panel
     * @return {Ext.panel.Panel} new panel
     * 
     * @param hidden, true to have the panel intially hidden,
     * this is used to not have the view show empty on intial page load.
     */
    _createViewPanel: function() {
        return new Ext.panel.Panel({
            itemId: "_rp_view_panel",
            hidden: true,
            layout: this._getViewPanelLayoutConfig(),
            style: 'zIndex: 0; background-color: white',
            bodyCls: "rp-ttf-app",
            bodyStyle: 'border: none;',
            border: false
        });
    },
    
    getActiveTaskform: function() {
        return this._rpViewPanel.getLayout().getActiveItem();
    },
    
    getWidgetContainer: function() {
        return this._widgetContainer;
    },
    
    getRPHelpContainer: function(){
        if (!this._helpRPContainer) {
            this._helpRPContainer = Ext.create('RP.help.Container', {
                alignComponent: this._rpViewPanel
            });
        }
        
        return this._helpRPContainer;
    }
});

// Register xtype.
RP.core.ComponentMgr.register("rptfframe", "RP.taskflow.TaskflowFrame", []);
//////////////////////
// ..\js\taskflow\TaskflowRegistry.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @class RP.taskflow.TaskflowRegistry
 * @singleton
 * Maintains a registry of definitions for tasksflows that the
 * current user can execute. A taskflow is instantiated from a
 * definition in this regisry, therefore, a definition is just the data
 * for constructing a taskflow plus other metadata.
 */
RP.taskflow.TaskflowRegistry = function() {
    var defs = {};
    var initialContexts = {};
    var appendScriptURLs = {}; // key = taskflow name, value = [String]
    var appendCssURLs = {}; // key = taskflow name, value = [String]

    function set(def) {
        // if (Ext.isEmpty(def.name) || Ext.isEmpty(def.tasks)) {
        // RP.throwError("Missing required parameters.");
        // }
        logger.logTrace("[TaskflowRegistry] Adding to registry: " + def.name);

        // Add in the taskflow's cssUrls to the list of dependent files
        if (def.cssUrls) {
            RP.util.CSSLoader.load(def.cssUrls, RP.globals.paths.STATIC);
        }

        var tfDef = Ext.copyTo({}, def, ["name", "title", "isTitleMessageName",
                        "tasks", "listeners", "uiConfig", "desc",
                        "defaultTaskId", "scheduleCheckFn", "scriptUrls", "widgetContainerXtype"]);
        defs[def.name] = tfDef;
    }

    function get(name, throwOnError) {
        var def = defs[name];

        if (Ext.isEmpty(def)) {
            logger.logError("Taskflow definition with name '" + name + "' not registered!");
            if (throwOnError) {
                RP.throwError("Taskflow definition with name '" + name + "' not registered!");
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

        var loadSuccessFn = function() {
            logger.logTrace("[TaskflowRegistry] Finished downloading tasks script files (if any)...");

            // check if any event listeners on the taskflow are plain strings
            // yet... if any are
            // then perform an eval on them now since the dependent script urls
            // should have been loaded.
            if (def.listeners) {
                Ext.iterate(def.listeners, function(key, value) {
                    if (Ext.isString(value)) {
                        logger.logTrace(Ext.String.format(
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

        Ext.each(stashLibs, function(sl) {
            RP.util.ScriptLoader.loadStashLibrary(sl.name, sl.version);
            logger.logTrace("[TaskflowRegistry] Added loading of stash lib to queue: " + sl.name);
        });

        if (cssUrls.length > 0) {
            RP.util.CSSLoader.load(cssUrls, true);
            logger.logTrace("[TaskflowRegistry] Added downloading of CSS file(s) to queue");
        }

        if (scriptUrls.length > 0) {
            RP.util.ScriptLoader.load(scriptUrls);
            logger.logTrace("[TaskflowRegistry] Added loading of script file(s) to queue");
        }

        RP.util.ScriptLoader.onReady(function() {
            logger.logTrace("[TaskflowRegistry] AT THIS POINT, SCRIPT LOADING HAS FINISHED.");
            loadSuccessFn();
        });
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
         * @param {Object/Object[]} def Taskflow configuration object(s).
         * Contains the following properties :
         *   <ul>
         *   <li>name: (String) Globally unique taskflow name
         *   <li>title: (String) Taskflow title
         *   <li>tasks: (Object[]) Objects in the array contain these
         *   properties:
         *   <ul>
         *   <li>id: (String) Roster item id; must be unique amongst the
         *   tasks in this taskflow.
         *   <li>appId: (String) The application ID (e.g., "sitemgr")
         *   <li>taskId: (String) The registered task id (e.g., "inbox")
         *   <li>statusCalculation: (Function) Optional function to calculate
         *   this task's status within the scope of the taskflow.
         *   <li>inParameterMapping: (Array) Optional array of input
         *   parameter mappings
         *   <li>outParameterMapping: (Array) Optional array of output
         *   parameter mappings
         *   <li>dependencies: (String[]) Optional array of other task ids in
         *   this taskflow that must be completed before this task executes
         *   <li>completeTaskOnClick: (Boolean) If 'true' the task will be
         *   marked as complete when clicked; takes precedence over
         *   statusCalculation
         *   </ul>
         *   The appId and taskId properties are used to query the TaskRegistry
         *   for the task definition. The appId and widgetXtype properties are
         *   used to query the TaskflowWidgetRegistry for the corresponding
         *   widget.
         *   </ul>
         *   <ul>Optional:
         *   <li>isTitleMessageName: (Boolean) If true, title property actually
         *    is a message name; default is 'false'
         *   <li>desc: (String) (metadata) Description of what this Taskflow
         *   is about
         *   <li>scheduleCheckFn: (Function) A function to invoke to check if
         *   this taskflow should be enabled based on current time.
         *   <li>listeners: (Object) The 'listeners' config property of
         *   RP.taskflow.Taskflow
         *   <li>uiConfig: (Object) The 'uiConfig' config property of
         *   RP.taskflow.Taskflow
         *   </ul>
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
         * @param {String} name The registered name of the taskflow
         * @return {RP.taskflow.Taskflow} The taskflow sought.
         */
        get : function(name) {
            return get(name, false);
        },

        /**
         * Gets all taskflows currently registered within the system.
         * @return {RP.taskflow.Taskflow[]} All of the registered taskflows
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
         * @param {String} name The name of the registered taskflow
         * @param {Object} initialContext An object to populate the initial
         * taskflow context with
         * @param {Function} successFn The function to invoke with the created
         * taskflow.
         * @param {Function} errorFn The function to invoke if failed to create
         * taskflow.
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
         * @param {String} name Taskflow to apply context to
         * @param {Object} initialContext Context to apply
         */
        setInitialContext : function(name, initialContext) {
            setInitContext(name, initialContext);
        },

        /**
         * Gets the initial flow context for the named taskflow
         *
         * @param {Object} name Name of the taskflow
         * @return {Object} Initial context applied earlier
         */
        getInitialContext : function(name) {
            return getInitContext(name);
        },

        /**
         * Appends custom javascript files to load for a named taskflow. This
         * is used mainly as a customization hook to inject custom code for a
         * given taskflow.
         *
         * @param {String} name Taskflow name
         * @param {String/String[]} urls URLs to append to the named taskflow
         */
        appendScript : function(name, urls) {
            appendURLs(name, appendScriptURLs, urls);
        },

        /**
         * Appends custom CSS files to load for a named taskflow. This is used
         * mainly as a customization hook to inject custom CSS for a given
         * taskflow.
         *
         * @param {String} name Taskflow name
         * @param {String/String[]} urls URLs to append to the named taskflow
         */
        appendCSS : function(name, urls) {
            appendURLs(name, appendCssURLs, urls);
        }
    };
}();

/**
 * @member RP
 * @method registerTaskflow
 * Shorthand for {@link RP.taskflow.TaskflowRegistry#register}
 */
RP.registerTaskflow = RP.taskflow.TaskflowRegistry.register;

/**
 * @member RP
 * @method createTaskflow
 * Shorthand for {@link RP.taskflow.TaskflowRegistry#create}
 */
RP.createTaskflow = RP.taskflow.TaskflowRegistry.create;
//////////////////////
// ..\js\taskflow\widgets\WidgetWithState.js
//////////////////////
/**
 * Representation of a widget that keeps track of its current state.
 * The possible states include:<ul>
 * <li>FAILED</li>
 * <li>NORMAL</li>
 * <li>SUCCESSFUL</li>
 * <li>WARNING</li>
 * <li>WORKING</li></ul>
 */
Ext.define("RP.taskflow.widgets.WidgetWithState", {
    extend: "RP.taskflow.BaseTaskflowWidget",
    
    /**
     * A pseudo-enum representing the various states that the widget can have.  Subclasses can
     * add additional states.  The values of the object are implemented as strings instead of
     * integers because keeping track of integer values can be error prone if there are multiple
     * subclasses.
     * @type Object[]
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
     */
    vantageCls: "rp-vantage-status",
    
    /**
     * @cfg {Ext.Element} vantageStatus 
     * The element representing the right side of the widget when a non-standard state
     * is being displayed, which is a component containing the class 'rp-vantage-status'.
     */
    
    /**
     * @cfg {String} statusImageHTML
     * The HTML markup used to render a status icon in the widget's vantageStatus element.
     */
    statusImageHTML: '<img width="19" height="19" src="{0}" class="{1}">',
    
    initComponent: function() {
        if (!this.widgetState) {
            this.showNormal();
        }
        this.initialConfig.extraCls = (this.initialConfig.extraCls ? this.initialConfig.extraCls + " " : "") + "rp-widget-with-state";

        this.createVantageStatus();

        this.callParent();
    },
    
    /**
     * Reports whether the widget is currently in the failed state
     * @return {Boolean} True if this is in the failed state
     */
    isFailed: function() {
        return this.widgetState === this.widgetStates.FAILED;
    },
    
    /**
     * Reports whether the widget is currently in the normal state
     * @return {Boolean} True if this is in the normal state
     */
    isNormal: function() {
        return this.widgetState === this.widgetStates.NORMAL;
    },
    
    /**
     * Reports whether the widget is currently in the successful state
     * @return {Boolean} True if this is in the successful state
     */
    isSuccessful: function() {
        return this.widgetState === this.widgetStates.SUCCESSFUL;
    },
    
    /**
     * Reports whether the widget is currently in the warning state
     * @return {Boolean} True if this is in the warning state
     */
    isWarning: function() {
        return this.widgetState === this.widgetStates.WARNING;
    },
    
    /**
     * Reports whether the widget is currently in the working state
     * @return {Boolean} True if this is in the working state
     */
    isWorking: function() {
        return this.widgetState === this.widgetStates.WORKING;
    },
    
    /**
     * Updates the widget to show in the failed state
     */
    showFailed: function() {
        this.widgetState = this.widgetStates.FAILED;
        this.backgroundCls = "rp-widget-failed-status";
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
        this.backgroundCls = "rp-widget-success-status";
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
        if (!this.vantageStatus) {
            this.vantageStatus = this.vantageStatusWrapper = Ext.create("Ext.Component", {
                cls: 'rp-prototype-widget-vantage-middle',
                height: 21,
                padding: '0 7.5',
                minWidth: 17,
                html: Ext.String.format(this.statusImageHTML, Ext.BLANK_IMAGE_URL, this.imgCls)
            });

            if (this.header) {
                if (!this.header.isComponent) {
                    this.updateHeader();
                }

                // Because of the nature of the basetaskflowwidgetheader's items
                // (title + arrow component), the status container will always be inserted
                // at index 1 (between the two), regardless of whether the nav container
                // is rendered east or west.
                this.header.insert(1, this.vantageStatusWrapper);
            }
        }
    },
    
    /**
     * Returns the container that contains the status of the widget.
     */
    getStatusContainer: function() {
        return this.vantageStatus;
    },
    
    /**
     * Removes the vantage status on the right side of the widget.
     * @private
     */
    removeVantageStatus: function() {
        if (this.rendered && this.header.isComponent && this.vantageStatus) {
            this.header.remove(this.vantageStatus);
            delete this.vantageStatus;
        }
    },
    
    /**
     * Applies the background class and image class to the widget.
     * @private
     */
    applyIconClasses: function() {
        if (!this.header || this.isDestroyed) {
            return;
        }
        
        if (this.widgetState === this.widgetStates.NORMAL) {
            this.addCls(this.hyperlinkCls);
            this.removeVantageStatus();
        }
        else {
            // TODO this block will also run for the WidgetWithCount.  this component could
            // potentially be further refactored to follow the state pattern more closely.
            // for example, WidgetWithCount will by default have/keep the "normal" state,
            // which can be a problem if you call showSuccessful() before the componenet
            // has rendered because that state will then be overridden with a loading mask
            // by the WidgetWithCount's onPostRender call to displayCount(this._count).
            this.removeCls(this.hyperlinkCls);

            this.removeVantageStatus();
            this.createVantageStatus();
        }

        this.removeCls(this.lastBackgroundCls);
        this.addCls(this.backgroundCls);
        this.lastBackgroundCls = this.backgroundCls;
    },
    
    /**
     * A post-render hook to display the count when in the normal state.
     * @private
     */
    onPostRender: function() {
        this.callParent(arguments);
        this.applyIconClasses();
    }
});
//////////////////////
// ..\js\taskflow\widgets\WidgetWithCount.js
//////////////////////
/**
 * Representation of a widget with a numeric count on the right side.
 */
Ext.define("RP.taskflow.widgets.WidgetWithCount", {
    extend: "RP.taskflow.widgets.WidgetWithState",

    /**
     * @cfg countCls
     * Class to be applied to the count's div
     */
    countCls: 'rp-vantage-count',

    _borderOffset: 2,
    
    /**
     * Displays a count in the widget.  If the count is 0, a green checkmark
     * will be shown. For anything else, the count will be displayed as passed in.
     * If a string is passed in, it will simply be displayed instead of a number.
     * @param {Number/String} count Number (or text) to show as the count.
     */
    displayCount: function(count) {
        this._count = count;
        
        if (count === 0) {
            this.showSuccessful();
        }
        else {
            this.showNormal();
            
            if (!this.header || !this.header.isComponent) {
                return;
            }
            
            var statusCls = !Ext.isEmpty(count) && count.toString().length <= 3 ? "" : "rp-vantage-sum-small";

            this.createVantageStatus();
            this.removeCls(this.hyperlinkCls);
            this.vantageStatus.update(Ext.DomHelper.markup({
                style: 'line-height:' + (this.vantageStatus.height - this._borderOffset) +'px;',
                cls: this.countCls + " " + statusCls,
                html: count || ""
            }));
        }
    },
    
    /**
     * Gets the widget's current count value.
     * @return {Number} Count associated with this widget
     */
    getCount: function() {
        return this._count;
    },

    updateHeader: function(){
        this.callParent();
        this.header.on('afterrender', this._onHeaderRender, this);
    },
    
    /**
     * A post-render hook to display the count when in the normal state.
     * @private
     */
    _onHeaderRender: function() {
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
// ..\js\taskflow\TaskRegistry.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * @class RP.taskflow.TaskRegistry
 * @singleton
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
                RP.throwError("No tasks with appId '" + appId + "' have been registered!");
            }
            return undefined;
        }
        
        var def = Ext.copyTo({}, defs[taskId], properties);

        if (Ext.isEmpty(def)) {
            if (throwOnError) {
                RP.throwError("No tasks with appId '" + appId + "' and taskId '" + taskId + "' registered!");
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
        * @param {Object/Object[]} def Task configuration object or an array of task configuration objects.
        * <br /><b>Required:</b>
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
         * @param {String} appId App Id 
         * @param {String} taskId Task Id 
         * @param {String/String[]} urls URLs to append to the named task
         */
        appendScriptUrls: function(appId, taskId, urls) {
            appendURLs(appId, taskId, scriptsByApp, urls);
        },
        
        /**
         * Appends custom CSS files to load for a named app and task.  This is used
         * mainly as a customization hook to inject custom CSS for a given task.
         * @param {String} appId App Id 
         * @param {String} taskId Task Id 
         * @param {String/String[]} urls URLs to append to the named task
         */
        appendCSSUrls: function(appId, taskId, urls) {
             appendURLs(appId, taskId, cssByApp, urls);
        }
    };
}();

/**
 * @member RP
 * @method registerTask
 * Shorthand for {@link RP.taskflow.TaskRegistry#register}
 */
RP.registerTask = RP.taskflow.TaskRegistry.register;

/**
 * @member RP
 * @method getTask
 * Shorthand for {@link RP.taskflow.TaskRegistry#get}
 */
RP.getTask = RP.taskflow.TaskRegistry.get;
//////////////////////
// ..\js\taskflow\ModuleRegistry.js
//////////////////////
Ext.ns("RP.taskflow");

/**
 * Maintains a registry of modules available to the current user. A Module is
 * instantiated from a definition in this registry, therefore, a definition is
 * just the data for constructing module plus other metadata.
 * @singleton
 */
RP.taskflow.ModuleRegistry = (function() {
    var defs = {};

    var set = function(def) {
        logger.logTrace("[ModuleRegistry] Adding to registry: " + def.name);
        
        //Keep track of all of the properties
        var mdef = def;
        defs[def.name] = mdef;

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
                RP.throwError("Module definition with name '" + name + "' not registered!");
            }
            return undefined;
        }
        return def;
    };

    return {
        /**
         * Registers a module against the ModuleRegistry
         *
         * @param {Object} def Definition of the Module. This is an object with
         * the following keys: <ul>
         * <li><b>name</b>: {String} Name of the module</li>
         * <li><b>label</b>: {String} Label for the module</li>
         * <li><b>icon</b>: {String} Class which represents an icon</li>
         * <li><b>sortOrder</b>: {Object} </li>
         * <li><b>initFn</b>: {Function} Function to call on initialization</li>
         * <li><b>scriptUrls</b>: {String[]} URLs of custom scripts to add</li>
         * <li><b>cssUrls</b>: {String[]} URLs of custom CSS to add</li>
         * <li><b>taskflows</b>: {Object[]} Array of Taskflows that this module
         * is associated with</li></ul>
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
        * Gets the definition of a module based on the name
        *
        * @param {String} name Name to look up
        * @return {Object} Definition of the module
        */
        get : function(name) {
            return _get(name, false);
        },

        /**
         * Retrieve all of the modules sorted in based on
         * the sort order of the properties.
         * @return {Object[]} Array of sorted modules
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

/**
 * @member RP
 * @method registerModule
 * Shorthand for {@link RP.taskflow.ModuleRegistry#register}.
 */
RP.registerModule = RP.taskflow.ModuleRegistry.register;
//////////////////////
// ..\js\qtip\GridTip.js
//////////////////////
/**
 * @deprecated
 *
 * This is a tooltip that contains an inner panel, such as a grid panel.
 * This is accomplished by overriding the max width that tooltips
 * normally possess.  Specify a component to be displayed via the 
 * "displayedPanel" config.  The component will be wrapped in a panel
 * that will have "panelConfig" applied to it.  By default, this tooltip
 * will have autohide: false, trackmouse: false and closable: true, and
 * will be aligned to the location of the mouse pointer when it is rendered.
 */
Ext.define('RP.qtip.GridTip', {
    extend: 'Ext.tip.ToolTip',
    alternateClassName: 'RP.GridTip',
    alias: ['gridtip', 'widget.gridtip'],
    
    /**
     * @cfg {Number} Because ToolTips are not capable of managing thier children's widths, we need a
     * width value to set for this gridtip.
     */
    width: 500,
    
    /**
     * @cfg {Ext.Component} displayedPanel A required component to be displayed on the tooltip.  
     */
     
    /**
     * @cfg {object} panelConfig.  An optional set of css options to be applied to the wrapping
     * panel.
     */  
     
    /**
     * @cfg {String} The Css class to be applied to the gridtip. 
     * The default class (rp-gridtip) adds styles for a contained
     * grid.
     */
    cls: 'rp-gridtip',
    
    /**
     * @cfg {Boolean} True to display an anchored arrow, false
     * to hide it.  Defaults to true.
     */
    arrow: true,

    /**
     * @cfg {Number} anchor offset Sets the offset of the anchor arrow from the left
     * of the gridtip.  defaults to the center.
     */
     
    initComponent: function() {
        RP.util.DeprecationUtils.logStackTrace(
            "[DEPRECATED] RP.qtip.GridTip is deprecated. Please use RP.ui.ToolTip instead.");

        Ext.apply(this, {
            items: this._createPanel(),
            autoHide: false,
            trackMouse: false,
            buttonAlign: 'center',
            closable: true,
            //override the default value of 300
            maxWidth: this.width + 1,
            //set the correct anchorOffset
            anchorOffset: this.anchorOffset ? this.anchorOffset : (this.width / 2 - 20),
            //align to the mouse pointer
            anchorToTarget: false
        });

        if (this.arrow) {
            this.on('beforeshow', function(){
                this.anchorEl.setStyle({visibility: 'visible'});
            }, this, {single: true});
        }
        
        this.callParent();
    },    
    
    /**
     * Hold the contained panel.
     */
    _createPanel: function() { 
        var panel = Ext.create('Ext.panel.Panel', Ext.apply({}, this.panelConfig, {
            items: this.displayedPanel,
            layout: 'fit'
        }));
        
        return panel;
    }
});
//////////////////////
// ..\js\picker\Month.js
//////////////////////
/**
 * @private
 * A month picker component. This class is used by the {@link Ext.picker.Date Date picker} class
 * to allow browsing and selection of year/months combinations.
 */
Ext.define('RP.picker.Month', {
    extend: 'Ext.container.Container',

    /**
     * @cfg {String} [baseCls='x-monthpicker']
     *  The base CSS class to apply to the picker element.
     */
    baseCls: Ext.baseCSSPrefix + 'monthpicker',


    initComponent: function() {
        this.addEvents(
            /**
             * @event okclick
             * Fires when the ok button is pressed.
             * @param {Ext.picker.Month} this
             * @param {Array} value The current value
             */
            'okclick',

            /**
             * @event cancelclick
             * Fires when the cancel button is pressed.
             * @param {Ext.picker.Month} this
             */
            'cancelclick'
        );

        Ext.apply(this, {
            items: this._createItems()
        });

        this.callParent();
    },

    setValue: function(value){
        this._monthCombo.setValue(value.getMonth());
        this._yearField.setValue(value.getFullYear());
    },

    _onOkClick: function() {
        this.fireEvent('okclick', this, this.getValue());
    },

    /**
     * Return the date that was picked by drawing the month
     * from the month combo and the year
     * from the year combo.
     */
    getValue: function(){
        var month = this._monthCombo.getValue();
        var year = this._yearField.getValue();

        return [month, year];
    },

    /**
     * Returns true if either combobox is expanded,
     * else false
     */
    isAnythingExpanded: function(){
        return this._monthCombo.isExpanded || this._yearField.isExpanded;
    },

    _createItems: function(){
        this._monthCombo = this._createMonthCombo();

        this._yearField = this._createYearField();

        this._okButton = this._createOkButton();

        return [this._monthCombo, this._yearField, this._okButton];
    },

    _createYearField: function() {
        var yearField = Ext.create('Ext.form.field.Number', {
            fieldLabel: RP.getMessage("rp.common.dateranges.year"),
            hideTrigger: true,
            requiredIndicator: '',
            allowBlank: false,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            labelAlign: 'top',
            minValue: 1,
            maxValue: 9999
        });

        yearField.on('validitychange', this._onYearFieldValiditychange, this);

        return yearField;
    },

    _onYearFieldValiditychange: function(field, isValid) {
        var enableFn = isValid ? 'enable' : 'disable';
        this._okButton[enableFn]();
    },

    _createMonthCombo: function() {
        return Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: RP.getMessage("rp.common.dateranges.month"),
            store: this._createMonthStore(),
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            forceSelection: true,
            labelAlign: 'top'
        });
    },

    _createMonthStore: function() {
        var months = Ext.create('Ext.data.Store', {
            fields: ['abbr', 'name'],
            data : [
                {"abbr":0, "name":Ext.Date.monthNames[0]},
                {"abbr":1, "name":Ext.Date.monthNames[1]},
                {"abbr":2, "name":Ext.Date.monthNames[2]},
                {"abbr":3, "name":Ext.Date.monthNames[3]},
                {"abbr":4, "name":Ext.Date.monthNames[4]},
                {"abbr":5, "name":Ext.Date.monthNames[5]},
                {"abbr":6, "name":Ext.Date.monthNames[6]},
                {"abbr":7, "name":Ext.Date.monthNames[7]},
                {"abbr":8, "name":Ext.Date.monthNames[8]},
                {"abbr":9, "name":Ext.Date.monthNames[9]},
                {"abbr":10, "name":Ext.Date.monthNames[10]},
                {"abbr":11, "name":Ext.Date.monthNames[11]}
            ]
        });

        return months;
    },

    _createOkButton: function() {
        return new Ext.button.Button({
            margin: '5 0 0 0',
            handler: this._onOkClick,
            scope: this,
            text: RP.getMessage('rp.common.misc.ok')
        });
    }
    
});

//////////////////////
// ..\js\login\Login.js
//////////////////////
Ext.QuickTips.init();

/**
 * Implements a common login form.
 *
 * @class RP.login.LoginForm
 * @extends Ext.Viewport
 */
Ext.define("RP.login.LoginForm", {
    extend: "Ext.container.Viewport",

    authenticatedStatus: 0,

    passwordExpiredStatus: 1201,

    /**
     * @cfg {Object} loginLocales An optional object map of iso locale names to display names
     * used for displaying additional locales to be used by the login page.
     */

    /**
     * Extends base initComponent to add elements
     * @private
     */
    initComponent: function() {
        var errorHidden = !this._hasError();

        var formPanel = {
            header: false,
            cls: "loginFormPanel",
            xtype: "form",
            id: "loginForm",
            hideBorders: true,
            fieldDefaults: {
                labelAlign: "right",
                labelWidth: 300,
                labelPad: 20
            },
            layoutConfig: {
                labelSeparator: ":"
            },
            defaultType: "textfield",
            items: [{
                xtype: "box",
                id: "loginLogo"
            }, {
                xtype: "box",
                cls: "login_ProdName",
                html: RP.getMessage("rp.common.login.FormTitle")
            }, {
                fieldLabel: RP.getMessage("rp.common.login.Username"),
                itemId: "userName",
                inputId: "userName",
                autoCreate: {
                    tag: "input",
                    type: "text",
                    autocomplete: "on"
                },
                width: 450
            }, {
                fieldLabel: RP.getMessage("rp.common.login.Password"),
                itemId: "password",
                inputId: "password",
                inputType: "password",
                width: 450
            }, {
                xtype: "panel",
                layout: "fit",
                height: 30,
                items: [{
                    xtype: "panel",
                    id: "warning",
                    styleHtmlContent: true,
                    styleHtmlCls: "loginErrorMsg",
                    hidden: errorHidden,
                    header: false,
                    html: RP.getMessage("rp.common.login.LoginFailed")
                }]
            }],
            dockedItems: [{
                xtype: "toolbar",
                dock: "bottom",
                ui: "footer",
                defaults: {minWidth: 75},
                layout: {
                    type: "hbox",
                    align: "center"
                },
                items: [{
                    xtype: "tbspacer",
                    width: 265
                },
                { 
                    id: "loginBtn",
                    xtype: 'button',
                    text: RP.getMessage("rp.common.login.LoginButton"),
                    handler: this.submit,
                    scope: this
                }]
            }]
        };

        var html = "<br/>";
        var URLQueryObject = Ext.urlDecode(window.location.search.replace("?", ""));
        var urlBase = Ext.String.format("{0}//{1}{2}", window.location.protocol, window.location.host, window.location.pathname);
        var currentLocale = Ext.isEmpty(URLQueryObject.locale) ? RP.globals.getValue("BASE_LOCALE") : URLQueryObject.locale;

        Ext.iterate(this.loginLocales, function(key, value) {
            URLQueryObject.locale = key;
            if (key == currentLocale) {
                html += Ext.String.format("<span id=\"{0}\">{1}</span>", key, value);
            }
            else {
                html += Ext.String.format('<span id=\"{0}\"><a href="{1}">{2}</a></span>', key, urlBase + "?" + Ext.urlEncode(URLQueryObject), value);
            }
        });

        var containerPanel = new Ext.Panel({
            bodyStyle: "background-color: transparent;",
            border: false,
            cls: "loginBGBox",
            height: 414,
            items: [formPanel, {
                width: 350,
                cls: "login-locale-links",
                xtype: "container",
                html: html
            }]
        });

        Ext.apply(this, {
            cls: "loginBody",
            items: [containerPanel]
        });

        this.callParent(arguments);
    },

    /**
     * Checks to see if an error exists
     * @return {Boolean} Whether or not an error is present
     * @private
     */
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
     */
    submit: function() {
        var results = Ext.getCmp("loginForm").getForm().getValues();

        if (results.userName && results.password) {
            this.login(results.userName, results.password);
        }
        else {
            Ext.getCmp("warning").show();
        }
    },

    /**
     * Makes the actual backend login call
     * @private
     */
    login: function(userId, password) {
        logger.logTrace("[Login] Submitting credentials...");

        var myMask = new Ext.LoadMask(Ext.getBody(), {
            msg: RP.getMessage("rp.common.misc.PleaseWait")
        });
        myMask.show();
        this._disableFields();

        var params = {
            loginName: userId,
            password: password
        };

        Ext.Ajax.request({
            url: this.initialConfig.securityServiceURL,
            method: "POST",
            params: params,
            scope: this,
            disableExceptionHandling: true,
            callback: function(options, success, response) {
                var result = Ext.JSON.decode(response.responseText);

                // If result came back as Password expired, user must enter a new password
                if (result && result.status === this.passwordExpiredStatus) {
                    Ext.getCmp("loginForm").getComponent("password").reset();
                    myMask.hide();
                    this._enableFields();

                    logger.logTrace("[Login] Displaying ChangePassword dialog.");

                    var changePassword = new RP.ui.ChangePassword({
                        formTitle: RP.getMessage("rp.common.resetpassword.FormTitle"),
                        formIntro: RP.getMessage("rp.common.resetpassword.FormIntro")
                    });

                    changePassword.show();

                    changePassword.on("destroy", function(comp) {
                        if (changePassword.success === true) {
                            var newPassword = changePassword.getNewPassword();
                            this.login(userId, newPassword);
                        }
                    }, this);
                }

                // If result came back as authenticated, redirect user through to their destination
                else if (result && result.status === this.authenticatedStatus) {
                    var redirectUrl = this.onAuthenticatedHandler(window.location.href);
                    if (redirectUrl) {
                        RP.util.Helpers.redirect(redirectUrl);
                    }
                    else {
                        RP.core.PageContext.setInitialURL(result.data.siteId, result.data.module);
                    }
                }

                // If a login attempt failed from too many login attempts
                else if (result && result.status === RP.REFSExceptionCodes.LOGIN_ATTEMPTS_EXCEEDED_EXCEPTION) {
                    logger.logError("[Login] " + result.message);
                    myMask.hide();
                    this._enableFields();
                    Ext.MessageBox.show({
                        title: RP.getMessage("rp.common.misc.Error"),
                        msg: RP.getMessage("rp.common.exception.LOGIN_ATTEMPTS_EXCEEDED_EXCEPTION"),
                        icon: Ext.MessageBox.Error,
                        buttons: Ext.MessageBox.OK
                    });
                }

                // Any other failures just fail...
                else {
                    if (result) {
                        logger.logFatal("[Login] " + result.message);
                    } else {
                        logger.logFatal("[Login] Received an invalid response from server.");
                    }
                        
                    myMask.hide();
                    this._enableFields();
                    Ext.getCmp("warning").show();
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
     * @return {String/undefined} An string containing the redirectUrl or undefined.
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
     * Overrides default afterrender function to add key listening
     * @private
     */
    afterRender: function() {
        Ext.EventManager.on(document, "keydown", this.keyHandler, this);
        var task = new Ext.util.DelayedTask(function() {
            this.down("#userName").focus();
        }, this);
        task.delay(200);
        this.callParent(arguments);
    },

    /**
     * Overrides default beforedestroy function to remove key listening
     * @private
     */
    beforeDestroy: function() {
        Ext.EventManager.removeListener(document, "keydown", this.keyHandler);
        this.callParent(arguments);
    },

    /**
     * Function to handle "Enter" key press
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
    
    _disableFields: function() {
        this.down("#userName").disable();
        this.down("#password").disable();
    },
    
    _enableFields: function() {
        this.down("#userName").enable();
        this.down("#password").enable();
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
Ext.define("RP.logout.LogoutForm", {
    extend: "Ext.Viewport",
    
    /**
     * @cfg {Number} secondsBeforeRedirect The number of seconds to wait
     * before redirecting the user when an SSOCloseUrl is available. Defaults to 0.
     */
    secondsBeforeRedirect: 0,

    /**
     * Override the initComponent to add the text box
     * @private
     */
    initComponent: function() {
        var closeURL = RP.globals.getPath("SSO_CLOSE_URL"),
            text = "";

        if (!Ext.isEmpty(closeURL)) {
            text = Ext.String.format(RP.getMessage("rp.common.logout.Redirect"), closeURL);

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

        this.callParent(arguments);
    }
});
//////////////////////
// ..\js\util\DebugConsole\Trigger.js
//////////////////////
Ext.EventManager.on(document, 'keydown', function(e){
  if (e.ctrlKey &&
      e.shiftKey) {
        if (e.getKey() === 76/* L */) {
        
          if (!Ext.getCmp("x-debug-browser") && !this.disableDiagnosticsWindow) {
            this.debugConsole = new RP.util.DebugConsole.Console();
          }
          else {
            if (this.debugConsole && this.debugConsole.isVisible()) {
                if(this.debugConsole.isPinned()){
                    this.debugConsole.unpin();
                }   
         
                this.debugConsole.hide();
            }
            else {
                this.debugConsole.show();
            }
          }
          e.stopEvent();
        }
        
        if (e.getKey() === 85 /* U */) {
            if (this.debugConsole && this.debugConsole.isVisible() && this.debugConsole.getCompInspector().isVisible(true)) {
                this.debugConsole.getCompInspector().toggleUiSpy();
            }
            e.stopEvent();
        }
        
        if (e.getKey() === 83/* S */) {
             logger.toggleLogToServer();
             e.stopEvent();
        }
    
  }
});



Ext.onReady(function(){
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
    
    logf: function(format, arg1, arg2, etc){
      Ext.log(Ext.String.format.apply(String, arguments));
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
            b.push(Ext.String.format("  {0}: {1},\n", key, o[key]));
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
      var v = Ext.String.format("{0} ms", t - Ext._timers[name]);
      Ext._timers[name] = new Date().getTime();
      if (printResults !== false) {
        Ext.log('Timer ' + (name == "def" ? v : name + ": " + v));
      }
      return v;
    }
  });
  
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

/*
 * Modified ext debug console.
 * @author Kevin Rice
 */

Ext.define("RP.util.DebugConsole.Console", {
    extend: 'Ext.Panel', 
  constructor: function() {
    Ext.apply(this, {
      id: 'x-debug-browser'
    });
    this.callParent(arguments);
  },
  
  config: {
	  compInspector: null // Trigger.js needs to check if the component inspector is visible for UI Spy
  },
  
  initComponent: function() {
    var console = this.createConsole();
    this.pinButton = Ext.create('Ext.panel.Tool', {
        type: 'unpin',
            scope: this,
            handler: function(){
                // this is pinned, so unpin it
                if(this.isPinned()){    
                    this.unpin();
                }else{      //this is unpinned, so pin it
                    this.pin();     
                }
                RP.getTaskflowFrame().forceComponentLayout();
            }
      });

    this.wasPinned = false; 
     
    Ext.apply(this, {
        title: 'Console',
        renderTo: Ext.getBody(),
        collapsible: true,
        animCollapse: false,
        style: 'position:absolute;left:0;bottom:0;z-index:101',
        height: 200,
        layout: 'fit',
        dock: 'bottom',
        //region: 'south',
        resizable: true,
        resizeHandles: "n",
        tools:[this.pinButton, {
            type: 'close',
            scope: this,
            handler: function(){
                if(this.isPinned()){
                    this.unpin();
                }
                this.hide();
            }
        }],

        items: console
    });
    
    this.callParent();
    
    Ext.EventManager.onWindowResize(this.handleResize, this);
    this.on('resize', this.onConsoleResize, this);
    this.on('collapse', this.onConsoleCollapse, this);
    this.on('expand', this.onConsoleExpand, this);
    this.on('destroy', this.onConsoleResize, this);
    console.on('tabchange', this.onClick, this);
    this.on('collapse', this.stopUiSpy, this);

    this.handleResize();
  },
 
  isPinned: function(){
    return RP.getTaskflowFrame().getDockedComponent('x-debug-browser') === this;
  },
  
  pin: function(){
    RP.getTaskflowFrame().insertDocked(0, this);
    

    this.pinButton.setType('pin');
  },
  
  unpin: function(){
        RP.getTaskflowFrame().removeDocked(this, false);

        Ext.getBody().appendChild(this.getEl());
        this.getEl().alignTo(RP.getTaskflowFrame().el, 'bl-bl');
        this.setWidth(RP.getTaskflowFrame().getEl().getViewSize().width);
                    
        this.pinButton.setType('unpin');   
  },
  
  forceWidth: function(){
    var b = RP.getTaskflowFrame().getEl();
    var size = b.getViewSize();
    
    this.setWidth(size.width);    
  },
  
  onConsoleResize: function(){
      
    if(!this.isPinned() && this.rendered){
        this.getEl().alignTo(RP.getTaskflowFrame().getEl(), 'bl-bl');      
    }
    
    this.forceWidth();
    RP.getTaskflowFrame().forceComponentLayout();
  },
  
  onConsoleCollapse: function(){
    this.wasPinned = this.isPinned();
    
    if(!this.wasPinned){
        this.pin();
    }
    
    this.pinButton.disable();
    
    //this.onConsoleResize();
  },
  
  onConsoleExpand: function(){
    
    if(!this.wasPinned){
        this.unpin();
    }
    
    this.pinButton.enable();
    
    //this.onConsoleResize();
  },
  
  createConsole: function() {

    this.setCompInspector(new RP.util.DebugConsole.ComponentInspector());
    var logViewer = new RP.util.DebugConsole.LogViewer();
    var stopWatch = new RP.util.DebugConsole.StopWatchesPanel();
    var layoutGuide = new RP.util.DebugConsole.LayoutGuide();

    var items = [{
        title: 'Log Viewer',
        layout: 'fit',
        items: [logViewer]
    }, {
        title: 'Component Inspector',
        layout: 'fit',
        items: [this.getCompInspector()]
    }, {
        title: 'Stop Watch',
        layout: 'fit',
        items: [stopWatch]
    }];

    var listeners;

    // Only show the layout guide when the server isn't in production mode.
    // This is a tool just for developers.
    if (Ext.isDefined(RP.globals.getValue("SERVER_TYPE")) &&
            RP.globals.getValue("SERVER_TYPE").toUpperCase() !== "PRODUCTION") {
        items.push({
            title: 'Layout Guide',
            layout: 'fit',
            items: [layoutGuide]
        });
        listeners = {
            tabchange: function(owner, newCard) {
                if (newCard.getComponent(layoutGuide)) {
                    layoutGuide.refreshStore();
                }
            }
        };
    }

    var tabPanel = Ext.createWidget('tabpanel', {
        activeTab: 0,
        border: false,
        tabPosition: 'bottom',
        items: items,
        listeners: listeners
    });
    
    return tabPanel;
  },
  
  handleResize: function() {
        var b = Ext.getBody();
        var size = b.getViewSize();
        if(size.height < b.dom.scrollHeight) {
            size.width -= 18;
        }
        this.setWidth(size.width);

        this.onConsoleResize();
  },
  
  onClick: function() {
	  if(!this.getCompInspector().isVisible(true)){
		  this.getCompInspector().enableUiSpy();
	  }
  },
  
  stopUiSpy: function() {
	  this.getCompInspector().disableUiSpy();
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
Ext.define("RP.util.DebugConsole.ComponentInspector", {
    extend: "Ext.tree.Panel",
    region: 'center',
    pathToStartNode: ["rpViewport","_rp_center_panel [ container ]", "taskflowFrame", 
                      "viewContainer", "_rp_view_panel [ panel ]"],
    uiSpyButton: null,
    uiSpyShowInTreeCheckbox: null,
    uiSpyOutlineComponent: null,
    uiSpyUnderComponent: null,
    uiSpyOnFirstComponent: false,
    uiSpyNoItemIdWarning: "No item id!",
    uiSpyOKColor: "#00FF00",
    uiSpyNoItemIdColor: "#FFFF00",
    
    
    config: {
        uiSpy: false
    },
    
    initComponent: function() {
        this.uiSpyButton = new Ext.button.Button();
        this.uiSpyButton.setText("UI Spy");
        this.uiSpyButton.on('click', this.toggleUiSpy, this);
        
        this.uiSpyShowInTreeCheckbox = Ext.create('Ext.form.field.Checkbox', {
            fieldLabel: 'Show result in tree',
            value: false,
            disabled: true,
            labelStyle: {
                'vertical-align': 'super'
            }
        });
        
        this.uiSpyOutlineComponent = Ext.create('Ext.Component', {
            itemId: "uiSpyBorderCmp",
            floating: true,
            style: {
                "background-color" : this.uiSpyOKColor,
                "opacity" : "0.4",
                "filter" : "alpha(opacity=40)",
                "pointer-events" : "none"
            },
            visible: false
        });
        
        this.bbar = this.createBottomBar();
        
        Ext.apply(this, {
            fields: [
                {name: 'text', type: 'string'},
                {name: 'component'}
            ],
            layout: 'fit',
            rootVisible: true,
            root: {
                text: 'Ext Components',
                component: Ext.ComponentManager.all,
                leaf: false
            },
            viewConfig: {
                loadMask: false
            }
        });
        
        this.callParent();
        this.buildBranch(Ext.ComponentManager.all.getValues(), this.getRootNode(), true);
        
        this.on('itemclick', this.onClick, this);
        this.on('beforeitemexpand', this.onBeforeExpandNode, this);
        this.startNode = this.expandToNode(this.getRootNode(), 0);
        Ext.EventManager.on(document, 'click', this.uiSpyClick, this);
        Ext.EventManager.on(document, "mousemove", this.uiSpyMouseMove, this);
    },
    
    /**
     * Gets the outermost Ext.Component, going up from the given element
     */
    getClosestExtComponent: function(element) {
        var genIdRegExp = new RegExp("-[0-9]{4}");
        var genIdExtraStuffRegExp = new RegExp("-[0-9]{4}.");

        target = element;
        targetId = target.id;
        targetCmp = Ext.getCmp(targetId);
        while (target && (!targetCmp || !(targetCmp.itemId || !genIdExtraStuffRegExp.test(targetCmp.id)))) {
            target = target.parent();
            if (target) {
                targetId = target.id;
                  targetCmp = Ext.getCmp(targetId);
            }
        }
        if(targetCmp && !targetCmp.itemId && genIdRegExp.test(targetCmp.id) && !genIdExtraStuffRegExp.test(targetCmp.id)){
            this.uiSpyOutlineComponent.show();
            this.uiSpyOutlineComponent.getEl().applyStyles("background-color:" + this.uiSpyNoItemIdColor);
        }
        else {
            this.uiSpyOutlineComponent.show();
            this.uiSpyOutlineComponent.getEl().applyStyles("background-color:" + this.uiSpyOKColor);

        }
        return targetCmp;
    },
    
    /**
     * @private
     * Create the options bar display options specific to the component inspector.
     * @return {Toolbar} The toolbar of options.
     */
    createBottomBar: function() {
        return {
            xtype: "rptoolbar",
            itemId: "optionsBar",
            left: {
                items: [{
                    text: 'Refresh',
                    handler: this.refresh,
                    scope: this
                }, 
                this.uiSpyButton,
                this.uiSpyShowInTreeCheckbox
                ]
            },
            center: {
                flex: 2,
                items: [{
                    xtype: 'label',
                    text: 'Component Path: '
                }, {
                    xtype: 'textfield',
                    itemId: 'componentPath',
                    selectOnFocus: true
                }, {
                    xtype: 'button',
                    itemId: 'validateButton',
                    text: 'Validate Component Path',
                    scope: this,
                    handler: this.validateComponentPath
                }]
            },
            right: {
                items: [{
                    xtype: 'label',
                    text: 'JavaScript Path: '
                }, {
                    xtype: 'textfield',
                    itemId: 'extPath',
                    selectOnFocus: true
                }]
            }
        };
    },
    
    createNode: function(node, child) {
        var hasChildren = (child.items && child.items.length > 0) || (child.getDockedItems && child.getDockedItems().length > 0);
        return node.appendChild({
            text: child.getItemId() + (child.getXType() ? ' [ ' + child.getXType() + ' ]' : ''),
            component: child,
            leaf: !hasChildren
        });
    },
    
    buildBranch: function(items, node, isRoot) {
        // make sure the grab the items from the passed in items if it
        // contains a subset of items, generally from a component
        items = items.items || items;
        
        Ext.each(items, function(child) {
            if (child.id !== this.id) {
                if (!child.ownerCt || (child.ownerCt && child.ownerCt.id !== this.id)) {
                    if ((isRoot && !child.ownerCt) || !isRoot) {
                        this.createNode(node, child);
                    }
                }
            }
        }, this);
    },
    
    /**
     * @private
     * Click handler to make the component obvious to the user
     * and set the component and ext path fields in the toolbar.
     * @param {Object} tree The tree.
     * @param {Object} node The node clicked.
     */
    onClick: function(tree, node) {
    
        if (node.data.component.el) {
            node.data.component.el.frame("#ff0000", 1, {
                duration: 1000
            });
        }
        
        var componentPath = this.getComponentPathFromTreeNode(node);
        this.getDockedComponent("optionsBar").down('#componentPath').setValue(componentPath);
        
        var scriptPath;
        try {
            scriptPath = RP.util.DebugConsole.Path.parse(componentPath);
        } 
        catch (exception) {
            scriptPath = exception.message;
        }
        this.getDockedComponent("optionsBar").down('#extPath').setValue(scriptPath);
    },
    
    /**
     *
     * @param {Object} node
     */
    onBeforeExpandNode: function(node) {
        node.data.loading = true;
        if (!node.data.leaf && node.parentNode !== null) {
            var component = node.data.component;
            var items = component.items;
            this.buildBranch(items, node, false);
            if(Ext.isDefined(component.dockedItems)) {
                this.buildBranch(component.dockedItems, node, false);
            } 
        }
        node.data.loading = false;
    },
    
    validateComponentPath: function() {
        var path = this.getDockedItems("[dock=bottom]")[0].down("#componentPath").getValue();
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
            this.getDockedItems("[dock=bottom]")[0].down('#extPath').setValue(scriptPath);
            if (component.el !== undefined) {
                component.el.frame("#ff0000", 1, {
                    duration: 1000
                });
            }
        }
    },
    
    refresh: function() {
        var root = this.getRootNode();
        while (root.firstChild) {
            root.removeChild(root.firstChild);
        }
        
        this.buildBranch(Ext.ComponentManager.all.getValues(), root, true);
        this.startNode = this.expandToNode(this.getRootNode(), 0);
        this.selectPath(this.startNode.getPath("text"), "text");
        this.onClick(this, this.startNode);
    },
    
    getComponentPath: function(component) {
        var genIdRegExp = new RegExp("-[0-9]{4}");
        
        var componentPath = "";
        
        var uiSpyOnFirstComponentLocal = this.uiSpyOnFirstComponent;
        this.uiSpyOnFirstComponent = false;
        
        if (!component) {
            componentPath = "";
        }
        else if (!component.up()) {
            var returnString = "";
            if(component instanceof Ext.window.Window) {
                returnString += "window=";
            }
            returnString += component.getItemId();
            componentPath = returnString;
        }
        else if (!(component instanceof Ext.AbstractComponent) || (Ext.isDefined(component.initialConfig.id) && !genIdRegExp.test(component.initialConfig.id) )) {
            componentPath = component.id;
        }
        else if (component.itemId) {
            componentPath = this.getComponentPath(component.up()) + '/' + component.getItemId();
        }
        else if (Ext.isDefined(component.dock)) {
            var dockedItems = component.ownerCt.getDockedItems("[dock=" + component.dock + "]");
            var i;
            for(i = 0; i < dockedItems.length; i++) {
                if (dockedItems[i].getId() === component.getId()) {
                    break;
                }
            }
            componentPath = this.getComponentPath(component.up()) + "/.getDockedItems('[dock=" + component.dock + "]')[" + i + "]";
        }
        else {
            if(uiSpyOnFirstComponentLocal){
                componentPath = this.uiSpyNoItemIdWarning;
            }
            else{
                var parentPath = this.getComponentPath(component.up());
                if (parentPath.substr(parentPath.length - 1) === "*") {
                    componentPath = parentPath;
                }
                else {
                    componentPath = parentPath + '/*';
                }
            }
        }
        return componentPath;
    },
    
    getComponentPathFromTreeNode: function(treeNode) {
        var component = treeNode.data.component;
        if (component instanceof Ext.Component){
            return this.getComponentPath(component);
        } else {
            return "";
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
        var task = new Ext.util.DelayedTask(function(){
            msgBox.hide();
        });
        task.delay(1000);
    },

    expandToNode: function(node, pathIndex) {
        if (node) {
            node.expand();
        }
    
        if(Ext.isEmpty(node) || node.data.text === this.pathToStartNode[this.pathToStartNode.length - 1]) {
            return node;
        }
        
        var nextNode = node.findChild("text", this.pathToStartNode[pathIndex]);
        return this.expandToNode(nextNode, ++pathIndex);
    },
    
    toggleUiSpy: function() {
        if(this.getUiSpy()){
            this.disableUiSpy();
        } else {
            this.enableUiSpy();
        }
    },
    
    enableUiSpy: function() {
        this.setUiSpy(true);
        this.uiSpyButton.toggle(this.getUiSpy());
        this.uiSpyShowInTreeCheckbox.enable();
    },
    
    disableUiSpy: function() {
        this.setUiSpy(false);
        if(this.uiSpyButton) {
            this.uiSpyButton.toggle(this.getUiSpy());
        }
        if(this.uiSpyShowInTreeCheckbox && this.uiSpyShowInTreeCheckbox.isVisible(true)) {
            this.uiSpyShowInTreeCheckbox.disable();
        }
        this.uiSpyOutlineComponent.hide();
    },
    
    getCorrespondingTreeNode: function(component) {
        var rootNode = this.getRootNode();
        var returnNode = rootNode.findChildBy(
                function(node) {
                    if(this.getComponentFromTreeNode(node) === component){
                        return true;
                    } else {
                        node.expand();
                        return false;
                    }
                },
                this,
                true);
        if(returnNode) {
            return returnNode;
        } else {
            return rootNode;
        }
    },
    
    getComponentFromTreeNode: function(treeNode) {
        return treeNode.get("component");
    },
    
    uiSpyClick: function(e) {
        if (this.isVisible(true) && this.uiSpy) {
            e.stopEvent();
            //if we have an element with an item id that is not in the console
            if (this.uiSpyUnderComponent && 
                    this.uiSpyUnderComponent.getItemId() && 
                    !this.uiSpyUnderComponent.isDescendantOf(Ext.getCmp("x-debug-browser")) && 
                    (this.uiSpyUnderComponent !== Ext.getCmp("x-debug-browser")) &&
                    this.uiSpyOutlineComponent.isVisible(true)) {
                this.uiSpyOnFirstComponent = true;
                componentPath = this.getComponentPath(this.uiSpyUnderComponent);
                this.getDockedComponent("optionsBar").down('#componentPath').setValue(componentPath);
                
                var scriptPath;
                try {
                    if(componentPath === this.uiSpyNoItemIdWarning){
                        scriptPath = this.uiSpyNoItemIdWarning;
                    } else {
                        scriptPath = RP.util.DebugConsole.Path.parse(componentPath);
                    }
                } 
                catch (exception) {
                    scriptPath = exception.message;
                }
                this.getDockedComponent("optionsBar").down('#extPath').setValue(scriptPath);
                
                if(this.uiSpyShowInTreeCheckbox.getValue()){
                    correspondingNode = this.getCorrespondingTreeNode(this.uiSpyUnderComponent);
                    this.getSelectionModel().select([correspondingNode]);
                }
            }
        }
    },
    
    uiSpyMouseMove: function(e) {
        if (!this.isVisible(true)) {
            this.disableUiSpy();
        } else if (this.uiSpy) {
            this.uiSpyOutlineComponent.hide();
            var el = document.elementFromPoint(e.getPageX(), e.getPageY());
            if(el){
                el = Ext.get(el);
                targetCmp = this.getClosestExtComponent(el);
                if (targetCmp && !targetCmp.isDescendantOf(Ext.getCmp("x-debug-browser")) && (targetCmp !== Ext.getCmp("x-debug-browser"))){
                    this.uiSpyUnderComponent = targetCmp;
                    this.uiSpyOutlineComponent.setSize(targetCmp.getWidth(), targetCmp.getHeight());
                    this.uiSpyOutlineComponent.showAt([targetCmp.getBox().x, targetCmp.getBox().y]);
                } else {
                    this.uiSpyOutlineComponent.hide();
                }
            }
        }
    }
});
//////////////////////
// ..\js\util\DebugConsole\LayoutGuide.js
//////////////////////
/**
 * This is an addition to the DebugConsole that allows the user to select
 * components that are on the page in a grid and display a layout guide over
 * top the compoenents.
 *
 * @author Nate Nichols
 */
Ext.define('RP.util.DebugConsole.LayoutGuide', {
    extend: 'Ext.grid.Panel',

    layoutGuideCls: 'rp-layout-guide',
    layoutGuideOverCls: 'rp-layout-guide-over',

    initComponent: function() {
        Ext.apply(this, {
            bbar: this.buildBottomBar(),
            store: this.buildStore(),
            columns: this.buildColumns(),
            selModel: {
                allowDeselect: true,
                mode: 'SINGLE'
            },
            listeners: {
                itemmouseenter: this.onMouseOverComponent,
                itemmouseleave: this.onMouseLeaveComponent,
                selectionchange: this.onSelectionChange,
                scope: this
            }
        });

        this.callParent(arguments);
    },

    buildBottomBar: function() {
        this.selectorField = Ext.ComponentManager.create({
            xtype: 'textfield',
            fieldLabel: 'Component Selector',
            labelWidth: 110,
            width: 300,
            emptyText: 'container'
        });

        this.xoffsetField = Ext.ComponentManager.create({
            xtype: 'numberfield',
            fieldLabel: 'X Offset',
            labelWidth: 60,
            value: 0,
            width: 125
        });

        this.yoffsetField = Ext.ComponentManager.create({
            xtype: 'numberfield',
            fieldLabel: 'Y Offset',
            labelWidth: 60,
            value: 0,
            width: 125
        });

        return [this.selectorField, {
            xtype: 'button',
            text: 'Refresh',
            handler: this.refreshStore,
            scope: this
        }, '-', this.xoffsetField, this.yoffsetField, {
            xtype: 'button',
            text: 'Reset Offsets',
            handler: this.resetOffsets,
            scope: this
        }];
    },

    buildStore: function() {
        return Ext.create('Ext.data.Store', {
            fields: ['itemId', 'id', 'area', 'title'],
            proxy: {
                type: 'memory',
                data: this.getComponents(),
                reader: {
                    type: 'json'
                }
            },
            sorters: [{
                property:'area',
                direction: 'DESC'
            }, {
                property: 'title',
                direction: 'ASC'
            }, {
                property: 'itemId',
                direction: 'ASC'
            }, {
                property: 'id',
                direction: 'ASC'
            }]
        });
    },

    buildColumns: function() {
        return [{
            dataIndex: 'title',
            text: 'Title',
            width: 200
        }, {
            dataIndex: 'itemId',
            text: 'Item Id',
            width: 200
        }, {
            dataIndex: 'id',
            text: 'Id',
            width: 100
        }, {
            dataIndex: 'area',
            text: 'Area (pixels)',
            align: 'right',
            width: 100,
            renderer: function(value) {
                return value + ' px';
            }
        }];
    },

    refreshStore: function() {
        this.store.loadData(this.getComponents());
    },

    resetOffsets: function() {
        this.xoffsetField.setValue(0);
        this.yoffsetField.setValue(0);
    },

    getComponents: function() {
        var root = RP.globals.CURRENT_TASK ?
                        RP.globals.CURRENT_TASK.taskForm :
                        (RP.getTaskflowFrame ? RP.getTaskflowFrame() : null);
        
        if (root) {
            return this.processComponents([root].concat(
                root.query(this.selectorField.getValue() || 'container')));
        }
        else {
            return [];
        }
    },

    processComponents: function(components) {
        return Ext.Array.map(components, function(c) {
            return {
                itemId: c.itemId,
                id: c.id,
                title: c.title || '**UNTITLED**',
                area: c.rendered ? c.getHeight() * c.getWidth() : 0
            };
        });
    },

    onSelectionChange: function(selModel, selected) {
        var cmp;
        if (this.lastSelected) {
            cmp = Ext.getCmp(this.lastSelected.get('id'));
            this.unmask(cmp, this.self.LAYOUT_GUIDE, this.layoutGuideCls);
        }
        
        this.lastSelected = selected = selected[0];
        
        if (selected) {
            cmp = Ext.getCmp(this.lastSelected.get('id'));
            this.mask(cmp, this.self.LAYOUT_GUIDE, this.layoutGuideCls);
        }
    },

    onMouseOverComponent: function(view, record, item, index, e, opts) {
        var cmp = Ext.getCmp(record.get('id'));
        this.mask(cmp, this.self.COMPONENT_BORDER, this.layoutGuideOverCls);
    },

    onMouseLeaveComponent: function(view, record, item, index, e, opts) {
        var cmp = Ext.getCmp(record.get('id'));
        this.unmask(cmp, this.self.COMPONENT_BORDER);
    },

    // This is mostly pulled from Ext.Element's mask method.
    mask: function(cmp, maskName, cls) {
        var el = cmp.el, dom, mask,
            xoff = 0, yoff = 0, setExpression, dh = Ext.DomHelper,
            data;

        if (!el) {
            return;
        }

        data = (el.$cache || el.getCache()).data;

        dom = el.dom;
        setExpression = dom.style.setExpression;
        
        // If the element is already masked with the specified mask, we're done
        if (data[maskName]) {
            return;
        }

        if (maskName === this.self.LAYOUT_GUIDE) {
            xoff = this.xoffsetField.getValue() || 0;
            yoff = this.yoffsetField.getValue() || 0;
        }

        if (!(/^body/i.test(dom.tagName) && el.getStyle('position') == 'static')) {
            el.addCls('x-masked-relative');
        }

        mask = dh.append(dom, {
            cls: cls,
            style: Ext.String.format('left:{0}px;top:{1}px;', xoff, yoff)
        }, true);

        data[maskName] = mask;

        el.addCls('x-masked');
        mask.setDisplayed(true);

        // NOTE: CSS expressions are resource intensive and to be used only as a last resort
        // These expressions are removed as soon as they are no longer necessary - in the unmask method.
        // In normal use cases an element will be masked for a limited period of time.
        // Fix for https://sencha.jira.com/browse/EXTJSIV-19.
        // IE6 strict mode and IE6-9 quirks mode takes off left+right padding when calculating width!
        if (!Ext.supports.IncludePaddingInWidthCalculation && setExpression) {
            mask.dom.style.setExpression('width', 'this.parentNode.clientWidth + "px"');
        }

        // Some versions and modes of IE subtract top+bottom padding when calculating height.
        // Different versions from those which make the same error for width!
        if (!Ext.supports.IncludePaddingInHeightCalculation && setExpression) {
            mask.dom.style.setExpression('height', 'this.parentNode.' + (dom == DOC.body ? 'scrollHeight' : 'offsetHeight') + ' + "px"');
        }
        // ie will not expand full height automatically
        else if (Ext.isIE && !(Ext.isIE7 && Ext.isStrict) && me.getStyle('height') == 'auto') {
            mask.setSize(undefined, elHeight || me.getHeight());
        }
        return mask;
    },

    // Mostly pulled from Ext.Element's unmask method.
    unmask: function(cmp, maskName, name) {
        var el = cmp.el,
            data, dom, mask;

        if (!el) {
            return;
        }

        data = (el.$cache || el.getCache()).data;
        dom = el.dom;
        mask = data[maskName];

        if (mask) {
            // Remove resource-intensive CSS expressions as soon as they are not required.
            if (mask.dom.style.clearExpression) {
                mask.dom.style.clearExpression('width');
                mask.dom.style.clearExpression('height');
            }

            mask.remove();
            delete data[maskName];
            
            // If we just removed the last mask, none of the following properties on data will
            // have a value. If this is the case, we need to make sure that the mask classes are
            // removed.
            if (!data[this.self.LAYOUT_GUIDE] && !data[this.self.COMPONENT_BORDER] && !data.maskEl) {
                el.removeCls(['x-masked', 'x-masked-relative']);
            }
        }
    },

    statics: {
        LAYOUT_GUIDE: 'layoutGuide',
        COMPONENT_BORDER: 'cmpBorder'
    }

});
//////////////////////
// ..\js\util\DebugConsole\LogViewer.js
//////////////////////
/**
 * @class RP.util.DebugConsole.LogViewer
 * @extends Ext.Panel
 *
 * The 'Log Viewer' tab of the debugging console.
 */
Ext.define("RP.util.DebugConsole.LogViewer", {
    extend: "RP.ui.Panel",
    initComponent: function() {
        var tpl = new Ext.XTemplate('<tpl for=".">', '<div class="rp-log {cls}">{message}</div>', '</tpl>');
        
        // Setup button pressed state
        this.pressedButtons = ['TRACE', 'DEBUG', 'WARNING', 'INFO', 'ERROR', 'FATAL'];
        
        var loggerStore = logger.getStore();
        
        Ext.apply(this, {
            itemId: 'logsTab',
            frame: false,
            autoScroll: true,
            bbar: this.createBottomToolbar(),
            items: [{
                xtype: 'dataview',
                itemSelector: "div.rp-log",
                itemId: 'logMessagesPanel',
                store: loggerStore,
                emptyText: "No log messages to display",
                tpl: tpl,
                frame: false,
                border: false,
                listeners: {
                    refresh: this.scrollToBottom,
                    scope: this
                }
            }]
        });
        
        this.callParent();
        
        this.mon(logger, "logtoservertoggled", this.logToServerHandler, this);
        this.on('afterlayout', this.initializeLoggerState, this, { single: true });
        // loggerStore.on("datachanged", this.scrollToBottom, this, { delay: 10 });
    },
    logToServerHandler: function() {
        var button = this.getLogToServerButton();
        button.toggle(logger.getIsLogToServer(), true);
        
        if (logger.getIsLogToServer()) {
            button.setIconCls('rp-approve-btn');
        }
        else {
            button.setIconCls('rp-decline-btn');
        }
    },
    createBottomToolbar: function() {
        var filterButtonTemplate = {
            xtype: 'button',
            pressed: true,
            iconCls: 'rp-approve-btn',
            enableToggle: true,
            listeners: {
                scope: this,
                toggle: this.onToggle
            }
        };
        
        return {
            xtype: "rptoolbar",
            itemId: "optionsBar",
            left: {
                items: [{
                    xtype: 'button',
                    itemId: 'refresh',
                    text: 'Refresh',
                    iconCls: 'rp-approve-btn',
                    pressed: true,
                    scope: this,
                    enableToggle: true,
                    toggleHandler: function(button, pressed) {
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
                }]
            },
            center: {
                flex: 2,
                items: [Ext.apply({
                        text: '<div class="rp-log-t">Trace</div>',
                        itemId: 'trace'
                    }, filterButtonTemplate), "-", Ext.apply({
                        text: '<div class="rp-log-d">Debug</div>',
                        itemId: 'debug'
                    }, filterButtonTemplate), "-", Ext.apply({
                        text: '<div class="rp-log-i">Info</div>',
                        itemId: 'info'
                    }, filterButtonTemplate), "-", Ext.apply({
                        text: '<div class="rp-log-w">Warning</div>',
                        itemId: 'warning'
                    }, filterButtonTemplate), "-", Ext.apply({
                        text: '<div class="rp-log-e">Error</div>',
                        itemId: 'error'
                    }, filterButtonTemplate), "-", Ext.apply({
                        text: '<div class="rp-log-f">Fatal</div>',
                        itemId: 'fatal'
                    }, filterButtonTemplate)
                ]
            },
            right: {
                items: [{
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
                        toggle: function(button, pressed) {
                            if (pressed) {
                                button.setIconCls('rp-approve-btn');
                            }
                            else {
                                button.setIconCls('rp-decline-btn');
                            }
                            logger.toggleLogToServer();
                        }
                    }
                }]
            }
        };
    },

    onShowMessageButtonToggle: function(button, pressed) {
        if(pressed) {
            Ext.util.Cookies.set("rp-showmessages", true);
        }
        else {
            Ext.util.Cookies.clear("rp-showmessages");
        }
        RP.util.Helpers.reload();
    },
    
    initializeLoggerState: function() {
        this.updateLoggerFilter();
        
        // The user can only log messages to the server after a session had been established. So, we need to
        // disable the checkbox to for logging to server until the session has been established.
        if (logger.getLogToServerURL()) {
            this.getLogToServerButton().enable();
        }
        else {
            logger.logTrace('[Diagnostic] getLogToServerURL not set');
            this.getLogToServerButton().disable();
        }
        
        this.isRefreshEnabled = true;
        this.scrollToBottom();
        this.isRefreshEnabled = false;
    },
    getLogToServerIcon: function() {
        var iconCls = 'rp-decline-btn';
        if (logger.getIsLogToServer()) {
            iconCls = 'rp-approve-btn';
        }
        return iconCls;
    },
    getLogToServerButton: function() {
        return this.getDockedComponent("optionsBar").down("#logToServer");
    },
    onToggle: function(button, pressed) {
        if (pressed) {
            this.pressedButtons.push(button.itemId.toUpperCase());
            button.setIconCls('rp-approve-btn');
        }
        else {
            Ext.Array.remove(this.pressedButtons, button.itemId.toUpperCase());
            button.setIconCls('rp-decline-btn');
        }
        
        this.updateLoggerFilter();
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
            this.updateLoggerFilter();
            
            button.setIconCls('rp-approve-btn');
        }
        else {
            logger.getStore().suspendEvents(false);
            
            button.setIconCls('rp-decline-btn');
        }
    },
    /**
     * Handles the click event on the clear button. Clears the contents of the
     * Log panel.
     */
    onClear: function() {
        if (logger) {
            logger.clearAll();
        }
    },
    updateLoggerFilter: function() {
        var store = logger.getStore();
        var previouslySuspended = store.eventsSuspended;
        
        store.resumeEvents();
        logger.filterBy(this.pressedButtons);
        
        if (previouslySuspended) {
            store.suspendEvents(false);
        }
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

        if(chunks[0].match("^window=.+")) {
            addWindow(chunks[0]);
        } else {
            addId(chunks[0]);
        }
        
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
    
    var addWindow = function(window) {
        var value = extractValue(window);
        pathPieces.push(".WindowManager.getBy(function(w) { return w.itemId == '" + value + "'; })[0]");
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
        pathPieces.push(".down('[itemId=\"" + itemId + "\"]')");
    };
    
    var addXtype = function(xtype) {
        xtype = extractValue(xtype);
        pathPieces.push(".down('." + xtype + "')");
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
// ..\js\util\DebugConsole\StopWatchesPanel.js
//////////////////////
Ext.ns("RP.util.DebugConsole");

/**
 * @class RP.util.DebugConsole.StopWatchesPanel
 * @extends Ext.Panel
 *
 * The Stop Watch view inside the debugging console.
 */
Ext.define("RP.util.DebugConsole.StopWatchesPanel", {
    
    extend: "Ext.panel.Panel",

    initComponent: function() {
        Ext.apply(this, {
            itemId: 'stopWatchesTab',
            frame: false,
            bbar: new RP.ui.Toolbar(this.buildBottomBarSettings()),
            layout: 'fit',
            items: [{
                xtype: 'panel',
                itemId: 'stopWatchesMessagesPanel',
                id: 'stopwatch_out',
                frame: false,
                border: false
            }]
        });
        
        this.callParent(arguments);
        
        this.on('afterlayout', function() {
            this.updateStopwatch();
        }, this);
    },
    buildBottomBarSettings: function() {
        return {
            left: {
                items: ["-", {
                    xtype: 'button',
                    itemId: 'refresh',
                    text: 'Refresh',
                    iconCls: 'rp-decline-btn',
                    scope: this,
                    enableToggle: true,
                    toggleHandler: function(button, pressed) {
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
                }, "-"]
            }
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
            RP.util.Stopwatch.addListener(Ext.bind(this.updateStopwatch, this, [true], false));
            button.setIconCls('rp-approve-btn');
        }
        else {
            this.updateStopwatch();
            RP.util.Stopwatch.removeListener(Ext.bind(this.updateStopwatch, this, [true], false));
            button.setIconCls('rp-decline-btn');
        }
    },
    
    /**
     * Handles the click event on the clear button. Clears the contents of the
     * stopwatch panel.
     */
    onClear: function() {
        if (RP.util.Stopwatch) {
            RP.util.Stopwatch.clearAll();
            Ext.get('stopwatch_out').update('');
        }
    },
    /**
     * Calls out to the stopwatch to refresh the data in the stopwatch panel
     */
    updateStopwatch: function() {
        if (RP.util.Stopwatch) {
            RP.util.Stopwatch.report('stopwatch_out');
        }
    }
});
//////////////////////
// ..\js\util\date\RangeBuilder.js
//////////////////////
/**
 * This object is responsible for building date ranges. The
 * ranges supported are specified by methods in the {@link
 * #ranges} property. Some range methods take arguments such as
 * the length of the range, while others are completely self contained.
 *
 * @author Nate Nichols
 */
Ext.define('RP.util.date.RangeBuilder', {
    singleton: true,

    /**
     * Helper function: The current Date/Time
     * @return {Date}
     */
    now: function() {
        return new Date();
    },

    /**
     * Helper function: Today at midnight
     * @return {Date}
     */
    today: function() {
        return Ext.Date.clearTime(new Date());
    },

    /**
     * Helper function: Today at midnight offset by a specified
     * number of days.
     * @param {Number} offset The day offset. Negative numbers move
     * the date back in time.
     * @return {Date}
     */
    offsetToday: function(offset) {
        return RP.Date.addDays(this.today(), offset);
    },

    /**
     * Helper function: This week sunday, at midnight.
     * @return {Date}
     */
    weekStart: function() {
        var today = this.today(),
            days = today.getDay() * -1;
        return RP.Date.addDays(today, days);
    },

    /**
     * Helper function: The first day of this month at midnight.
     * @return {Date}
     */
    monthStart: function() {
        var today = this.today(),
            days = (today.getDate() - 1) * -1;
        return RP.Date.addDays(today, days);
    },

    /**
     * Helper function: The first day of this year at midnight.
     * @return {Date}
     */
    yearStart: function() {
        var today = this.today(),
            days = Ext.Date.getDayOfYear(today) * -1;
        return RP.Date.addDays(today, days);
    },

    /**
     * Helper function: This week sunday at midnight, offset by a
     * specified number of weeks
     * @param {Number} offset The week offset. Negative numbers move
     * the date back in time.
     * @return {Date}
     */
    offsetWeekStart: function(offset) {
        var start = this.weekStart();
        return RP.Date.addWeeks(start, offset);
    },

    /**
     * Helper function: The first day of this month at midnight, offset
     * by a specified number of months
     * @param {Number} offset The month offset. Negative numbers move
     * the date back in time.
     * @return {Date}
     */
    offsetMonthStart: function(offset) {
        var start = this.monthStart();
        return RP.Date.addMonths(start, offset);
    },


    /**
     * Helper function: The first day of this year at midnight, offset
     * by a specified number of years
     * @param {Number} offset The year offset. Negative numbers move
     * the date back in time.
     * @return {Date}
     */
    offsetYearStart: function(offset) {
        var start = this.yearStart();
        return RP.Date.addYears(start, offset);
    },

    // @ignore
    constructor: function() {
        /**
         * @property {Object} ranges A map of date range building functions
         */
        this.ranges = {
            //-------------------------//
            //        Day ranges       //
            //-------------------------//
            /**
             * Create a single day range, based on the start date passed in.
             * @param {Date} startDate The start date.
             * @return {Object}
             */
            singleDay: Ext.bind(this.buildSingleDayRange, this),
            /**
             * Create a date range from today at midnight to tomorrow
             * at midnight.
             * @return {Object}
             */
            today: Ext.Function.pass(this.buildDayRange, [1, 0], this),
            /**
             * Create a date range from yesterday at midnight to today
             * at midnight
             * @return {Object}
             */
            yesterday: Ext.Function.pass(this.buildDayRange, [1, -1], this),
            /**
             * Create a date range from tomorrow at midnight to the day
             * after at midnight.
             * @return {Object}
             */
            tomorrow: Ext.Function.pass(this.buildDayRange, [1, 1], this),

            //--------------------------//
            //    Ranges to/from now    //
            //--------------------------//

            /**
             * Create a date range starting now and ending after X minutes
             * where X is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            nextXMinutes: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addMinutes, RP.Date], true),
            /**
             * Create a date range starting now and ending after X hours
             * where X is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            nextXHours: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addHours, RP.Date], true),
            /**
             * Create a date range starting now and ending after X days
             * where X is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            nextXDays: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addDays, RP.Date], true),
            /**
             * Create a date range starting now and ending after X weeks
             * where X is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            nextXWeeks: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addWeeks, RP.Date], true),
            /**
             * Create a date range starting now and ending after X months
             * where X is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            nextXMonths: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addMonths, RP.Date], true),
            /**
             * Create a date range starting now and ending after X years
             * where X is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            nextXYears: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addYears, RP.Date], true),

            /**
             * Create a date range starting X minutes ago and ending now where X
             * is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            lastXMinutes: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addMinutes, RP.Date, -1], true),
            /**
             * Create a date range starting X hours ago and ending now where X
             * is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            lastXHours: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addHours, RP.Date, -1], true),
            /**
             * Create a date range starting X days ago and ending now where X
             * is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            lastXDays: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addDays, RP.Date, -1], true),
            /**
             * Create a date range starting X weeks ago and ending now where X
             * is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            lastXWeeks: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addWeeks, RP.Date, -1], true),
            /**
             * Create a date range starting X months ago and ending now where X
             * is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            lastXMonths: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addMonths, RP.Date, -1], true),
            /**
             * Create a date range starting X years ago and ending now where X
             * is passed in the period param.
             * @param {Number} period The length of the range
             * @return {Object}
             */
            lastXYears: Ext.Function.bind(
                this.buildNowBasedRange, this, [RP.Date.addYears, RP.Date, -1], true),

            //------------------------------//
            //       Relative ranges        //
            //------------------------------//
            /**
             * Create a date range for last week
             * @return {Object}
             */
            lastWeek: Ext.Function.pass(this.buildWeekRange, [1, 0, -1], this),
            /**
             * Create a date range for this week
             * @return {Object}
             */
            thisWeek: Ext.Function.pass(this.buildWeekRange, [1, 0], this),
            /**
             * Create a date range for next week
             * @return {Object}
             */
            nextWeek: Ext.Function.pass(this.buildWeekRange, [1, 1], this),

            /**
             * Create a date range for last month
             * @return {Object}
             */
            lastMonth: Ext.Function.pass(this.buildMonthRange, [1, 0, -1], this),
            /**
             * Create a date range for this month
             * @return {Object}
             */
            thisMonth: Ext.Function.pass(this.buildMonthRange, [1, 0], this),
            /**
             * Create a date range for next month
             * @return {Object}
             */
            nextMonth: Ext.Function.pass(this.buildMonthRange, [1, 1], this),
            
            /**
             * Create a date range for last year
             * @return {Object}
             */
            lastYear: Ext.Function.pass(this.buildYearRange, [1, 0, -1], this),
            /**
             * Create a date range for this year
             * @return {Object}
             */
            thisYear: Ext.Function.pass(this.buildYearRange, [1, 0], this),
            /**
             * Create a date range for next year
             * @return {Object}
             */
            nextYear: Ext.Function.pass(this.buildYearRange, [1, 1], this),

            //--------------------------//
            //         Weekdays         //
            //--------------------------//
            /**
             * Create a date range for this sunday
             * @return {Object}
             */
            sunday: Ext.Function.pass(this.buildWeekDayRange, [0, 0], this),
            /**
             * Create a date range for this monday
             * @return {Object}
             */
            monday: Ext.Function.pass(this.buildWeekDayRange, [1, 0], this),
            /**
             * Create a date range for this tuesday
             * @return {Object}
             */
            tuesday: Ext.Function.pass(this.buildWeekDayRange, [2, 0], this),
            /**
             * Create a date range for this wednesday
             * @return {Object}
             */
            wednesday: Ext.Function.pass(this.buildWeekDayRange, [3, 0], this),
            /**
             * Create a date range for this thursday
             * @return {Object}
             */
            thursday: Ext.Function.pass(this.buildWeekDayRange, [4, 0], this),
            /**
             * Create a date range for this firday
             * @return {Object}
             */
            friday: Ext.Function.pass(this.buildWeekDayRange, [5, 0], this),
            /**
             * Create a date range for this saturday
             * @return {Object}
             */
            saturday: Ext.Function.pass(this.buildWeekDayRange, [6, 0], this),
            /**
             * Create a date range for this sunday
             * @return {Object}
             */
            thisSunday: Ext.Function.pass(this.buildWeekDayRange, [0, 0], this),
            /**
             * Create a date range for this monday
             * @return {Object}
             */
            thisMonday: Ext.Function.pass(this.buildWeekDayRange, [1, 0], this),
            /**
             * Create a date range for this tuesday
             * @return {Object}
             */
            thisTuesday: Ext.Function.pass(this.buildWeekDayRange, [2, 0], this),
            /**
             * Create a date range for this wednesday
             * @return {Object}
             */
            thisWednesday: Ext.Function.pass(this.buildWeekDayRange, [3, 0], this),
            /**
             * Create a date range for this thursday
             * @return {Object}
             */
            thisThursday: Ext.Function.pass(this.buildWeekDayRange, [4, 0], this),
            /**
             * Create a date range for this friday
             * @return {Object}
             */
            thisFriday: Ext.Function.pass(this.buildWeekDayRange, [5, 0], this),
            /**
             * Create a date range for this saturday
             * @return {Object}
             */
            thisSaturday: Ext.Function.pass(this.buildWeekDayRange, [6, 0], this),
            /**
             * Create a date range for next sunday
             * @return {Object}
             */
            nextSunday: Ext.Function.pass(this.buildWeekDayRange, [0, 1], this),
            /**
             * Create a date range for next monday
             * @return {Object}
             */
            nextMonday: Ext.Function.pass(this.buildWeekDayRange, [1, 1], this),
            /**
             * Create a date range for next tuesday
             * @return {Object}
             */
            nextTuesday: Ext.Function.pass(this.buildWeekDayRange, [2, 1], this),
            /**
             * Create a date range for next wednesday
             * @return {Object}
             */
            nextWednesday: Ext.Function.pass(this.buildWeekDayRange, [3, 1], this),
            /**
             * Create a date range for next thursday
             * @return {Object}
             */
            nextThursday: Ext.Function.pass(this.buildWeekDayRange, [4, 1], this),
            /**
             * Create a date range for next friday
             * @return {Object}
             */
            nextFriday: Ext.Function.pass(this.buildWeekDayRange, [5, 1], this),
            /**
             * Create a date range for next saturday
             * @return {Object}
             */
            nextSaturday: Ext.Function.pass(this.buildWeekDayRange, [6, 1], this),
            /**
             * Create a date range for last sunday
             * @return {Object}
             */
            lastSunday: Ext.Function.pass(this.buildWeekDayRange, [0, -1], this),
            /**
             * Create a date range for last monday
             * @return {Object}
             */
            lastMonday: Ext.Function.pass(this.buildWeekDayRange, [1, -1], this),
            /**
             * Create a date range for last tuesday
             * @return {Object}
             */
            lastTuesday: Ext.Function.pass(this.buildWeekDayRange, [2, -1], this),
            /**
             * Create a date range for last wednesday
             * @return {Object}
             */
            lastWednesday: Ext.Function.pass(this.buildWeekDayRange, [3, -1], this),
            /**
             * Create a date range for last thursday
             * @return {Object}
             */
            lastThursday: Ext.Function.pass(this.buildWeekDayRange, [4, -1], this),
            /**
             * Create a date range for last friday
             * @return {Object}
             */
            lastFriday: Ext.Function.pass(this.buildWeekDayRange, [5, -1], this),
            /**
             * Create a date range for last saturday
             * @return {Object}
             */
            lastSaturday: Ext.Function.pass(this.buildWeekDayRange, [6, -1], this)
        };

        this.callParent(arguments);
    },

    /**
     * Helper function to build a range based on the current time.
     * @param {Number} period The length of the range
     * @param {Function} addFn The function to use to add the period
     * to the start date.
     * @param {Object} addFnScope The scope to call the addFn in.
     * @param {Number} [multiplier=undefined] The period value will be
     * multiplied by the multiplier and then added to the date. Pass
     * -1 do make a range go backward from the start date.
     * @return {Object} A date range object. If the startDate will be
     * greater than the end date (negative multiplier) The start date and
     * end date are flipped.
     */
    buildNowBasedRange: function(period, addFn, addFnScope, multiplier) {
        return this.buildDateRange(period, this.now(), addFn, addFnScope, multiplier);
    },

    /**
     * Helper function to build a day range.
     * @param {Number} period The length of the range
     * @param {Number} offset The number of days to offset from today
     * @param {Number} [multiplier=undefined] The period value will be
     * multiplied by the multiplier and then added to the date. Pass
     * -1 do make a range go backward from the start date.
     * @return {Object} A date range object. If the startDate will be
     * greater than the end date (negative multiplier) The start date and
     * end date are flipped.
     */
    buildDayRange: function(period, offset, multiplier) {
        return this.buildDateRange(period, this.offsetToday(offset),
            RP.Date.addDays, RP.Date, multiplier);
    },

    /**
     * Helper function to build a week range.
     * @param {Number} period The length of the range
     * @param {Number} offset The number of weeks to offset from the start
     * of this week.
     * @param {Number} [multiplier=undefined] The period value will be
     * multiplied by the multiplier and then added to the date. Pass
     * -1 do make a range go backward from the start date.
     * @return {Object} A date range object. If the startDate will be
     * greater than the end date (negative multiplier) The start date and
     * end date are flipped.
     */
    buildWeekRange: function(period, offset, multiplier) {
        return this.buildDateRange(period, this.offsetWeekStart(offset),
            RP.Date.addWeeks, RP.Date, multiplier);
    },

    /**
     * Helper function to build a month range.
     * @param {Number} period The length of the range
     * @param {Number} offset The number of months to offset from the start
     * of this month.
     * @param {Number} [multiplier=undefined] The period value will be
     * multiplied by the multiplier and then added to the date. Pass
     * -1 do make a range go backward from the start date.
     * @return {Object} A date range object. If the startDate will be
     * greater than the end date (negative multiplier) The start date and
     * end date are flipped.
     */
    buildMonthRange: function(period, offset, multiplier) {
        return this.buildDateRange(period, this.offsetMonthStart(offset),
            RP.Date.addMonths, RP.Date, multiplier);
    },

    /**
     * Helper function to build a year range.
     * @param {Number} period The length of the range
     * @param {Number} offset The number of years to offset from the start
     * of this year.
     * @param {Number} [multiplier=undefined] The period value will be
     * multiplied by the multiplier and then added to the date. Pass
     * -1 do make a range go backward from the start date.
     * @return {Object} A date range object. If the startDate will be
     * greater than the end date (negative multiplier) The start date and
     * end date are flipped.
     */
    buildYearRange: function(period, offset, multiplier) {
        return this.buildDateRange(period, this.offsetYearStart(offset),
            RP.Date.addYears, RP.Date, multiplier);
    },

    /**
     * Helper function to build a weekday range, ie: 'tuesday'.
     * @param {Number} day The day of the week. Sunday = 0.
     * @param {Number} offset The number of weeks to offset from the start
     * of this week.
     * @return {Object} The date range.
     */
    buildWeekDayRange: function(day, offset) {
        var startOfWeek = this.offsetWeekStart(offset);
        return this.buildDateRange(1, RP.Date.addDays(startOfWeek, day),
            RP.Date.addDays, RP.Date);
    },

    /**
     * Helper function to build a single day range, ie: '6-6-2006'.
     * @param {Date} start The start date.
     * @return {Object} The date range.
     */
    buildSingleDayRange: function(start) {
        return this.buildDateRange(1, start, RP.Date.addDays, RP.Date);
    },

    /**
     * Helper function to build a date range.
     * @param {Number} period The length of the range
     * @param {Date} start The startDate of the range
     * @param {Function} addFn The function to use to add the period to the
     * start date.
     * @param {Object} addFnScope The scope to call the addFn in
     * @param {Number} [multiplier=undefined] The period value will be
     * multiplied by the multiplier and then added to the date. Pass
     * -1 do make a range go backward from the start date.
     * @return {Object} A date range object. If the startDate will be
     * greater than the end date (negative multiplier) The start date and
     * end date are flipped.
     */
    buildDateRange: function(period, start, addFn, addFnScope, multiplier) {

        multiplier = multiplier || 1;

        var end = addFn.apply(addFnScope, [start, multiplier * period]),
            tmp;

        if (start > end) {
            tmp = start;
            start = end;
            end = tmp;
        }

        return {
            startDate: start,
            endDate: end
        };
    },

    /**
     * Takes two date range objects and merges them into one. The
     * startRange's startDate and the endRange's endDate will be used
     * for the merged range's endpoints, unless the startDate is
     * greater than the endDate. If this is the case, the ranges will
     * be flipped.
     *
     * @param {Object} startRange The starting date range object.
     * @param {Object} endRange The ending date range object.
     * @return {Object} The merged date range.
     */
    mergeRanges: function(startRange, endRange) {
        // Ensure the startDate is always less than or equal
        // to the endDate.
        if (startRange.startDate > endRange.endDate) {
            return {
                startDate: endRange.startDate,
                endDate: startRange.endDate
            };
        }
        else {
            return {
                startDate: startRange.startDate,
                endDate: endRange.endDate
            };
        }
    }
});
//////////////////////
// ..\js\util\date\RangeParser.js
//////////////////////
/**
 * This object is responsible for parsing strings into date range
 * values. Besides your typical date strings (6/10/2012, 6-10-2012),
 * many other local specific strings are supported. See {@link
 * #basicRangeStrings} {@link #singleDayStrings}, and {@link
 * #advancedRangeStrings} for the untranslated strings.
 *
 * These untranslated strings are used as identifiers in the rp.common.daterange
 * message pack to pull the locale specific translations.
 *
 * This singleton supports a variety of date formats. The formats
 * that are supported may be customized by changing the following
 * properties: {@link #supportedDateFormats}, {@link #partialToFullFormats},
 * and {@link #regexToFormat} as well as the {@link #defaultDateFormat}.
 *
 * @author Nate Nichols
 */
Ext.define('RP.util.date.RangeParser', {
    singleton: true,

    /**
     * @property {String[]} supportedDateFormats A list of supported date formats.
     * Override this property to customize which formats are supported.
     */
    supportedDateFormats: [
        'm/d/Y', 'm/d/y', 'm/d/', 'm/d',
        'm-d-Y', 'm-d-y', 'm-d-', 'm-d',
        'n/j/Y', 'n/j/y', 'n/j/', 'n/j',
        'n-j-Y', 'n-j-y', 'n-j-', 'n-j',
        'Y-m-d', 'Y-n-j',
        'F j Y', 'F j', 'F', 
        'M j Y', 'M j', 'M'
    ],

    /**
     * @property {Object} partialToFullFormats This object maps partial formats
     * to a full format. This map is used to support the user typing in just
     * part of a date and have the parser fill in the rest automatically. For
     * example: 'm/d/' is a partial format that maps to 'm/d/Y'. If a date is
     * parsed with a partial format, it is formatted as a string using the
     * matching full format.
     *
     * Any partial formats specified in {@link #supportedDateFormats} must be
     * mapped to a full format here.
     */
    partialToFullFormats: {
        'm/d/': 'm/d/Y', 
        'm-d-': 'm-d-Y', 
        'n/j/': 'n/j/Y', 
        'n-j-': 'n-j-Y', 
        'm/d': 'm/d/Y',
        'm-d': 'm-d-Y',
        'n/j': 'n/j/Y',
        'n-j': 'n-j-Y',
        'F j': 'F j Y',
        'M j': 'M j Y',
        'F': 'F j Y',
        'M': 'M j Y'
    },

    /**
     * @property {Object} regexToFormat This property is similar to {@link
     * #partialToFullFormats}. It maps unique format prefixes with all the
     * valid formats that have that prefix. This property is used when
     * attempting to format the second date in a date range, to ensure that
     * the same format group is used for both dates.
     */
    regexToFormat: {
        '^m/': ['m/d/Y','m/d/y', 'm/d', 'm/d/', 'm/', 'm'],
        '^m-': ['m-d-y','m-d-Y', 'm-d', 'm-d-', 'm-', 'm'],
        '^n/': ['n/j/Y','n/j/y', 'n/j', 'n/j/', 'n/', 'n'],
        '^n-': ['n-j-y','n-j-Y', 'n-j', 'n-j-', 'n-', 'n'],
        '^Y-m': ['Y-m-d', 'Y-m', 'Y'],
        '^Y-n': ['Y-n-j', 'Y-n', 'Y'],
        '^F': ['F j Y', 'F j', 'F'],
        '^M': ['M j Y', 'M j', 'M']
    },

    /**
     * @property {String[]} basicRangeStrings The untranslated names
     * for basic range names. The {@link RP.util.date.RangeBuilder#ranges}
     * property of RP.util.date.RangeBuilder has a matching function for
     * each of these range names. The locale translations for these names
     * are used when actually parsing the string. If the translated string
     * is found, the RangeBuilder is used to create the appropriate range
     * object.
     */
    basicRangeStrings: [
        'thisWeek', 'thisMonth', 'thisYear',
        'lastWeek', 'lastMonth', 'lastYear',
        'nextWeek', 'nextMonth', 'nextYear'
    ],

    /**
     * @property {String[]} singleDayStrings The untranslated names for single day
     * ranges. The {@link RP.util.date.RangeBuilder#ranges} property of
     * RP.util.date.RangeBuilder has a matching function for each of these range
     * names. The locale translations for these names are used when actually
     * parsing the string. If the translated string is found, the RangeBuilder
     * is used to create the appropriate range object.
     *
     * Note: single day strings are still parsed into a range object with a
     * start and end date. The end date will be midnight of the next day.
     */
    singleDayStrings: [
        'today', 'tomorrow', 'yesterday',
        'sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday',
        'thisSunday', 'thisMonday', 'thisTuesday', 'thisWednesday', 'thisThursday', 'thisFriday', 'thisSaturday',
        'lastSunday', 'lastMonday', 'lastTuesday', 'lastWednesday', 'lastThursday', 'lastFriday', 'lastSaturday',
        'nextSunday', 'nextMonday', 'nextTuesday', 'nextWednesday', 'nextThursday', 'nextFriday', 'nextSaturday'
    ],

    /**
     * @property {String[]} singleDayStrings The untranslated names for single day
     * ranges. The {@link RP.util.date.RangeBuilder#ranges} property of
     * RP.util.date.RangeBuilder has a matching function for each of these range
     * names. The locale translations for these names are used when actually
     * parsing the string. If the translated string is found, the RangeBuilder
     * is used to create the appropriate range object.
     *
     * These ranges are more complex as they take an arbitrary integer specifying
     * the length of the range. For example: "next 10 days". The locale translation
     * must follow a specific format using {0} to indicate where the number must
     * appear in the range. For the range above, the nextXDays message pack translation
     * must be 'next {0} days'.
     */
    advancedRangeStrings: [
        'nextXMinutes', 'nextXHours', 'nextXDays', 'nextXWeeks', 'nextXMonths', 'nextXYears',
        'lastXMinutes', 'lastXHours', 'lastXDays', 'lastXWeeks', 'lastXMonths', 'lastXYears'
    ],

    /**
     * @property {String} [defaultDateFormat='m/d/Y'] The default Date format to use.
     */
    defaultDateFormat: 'm/d/Y',

    /**
     * @private
     * @property {Object} translations An object containing translated strings.
     * @property {Object} translations.basicRanges A map of translated strings to
     * range names.
     * @property {Object} translations.singleDayRanges A map of translated Strings
     * to range names.
     * @property {Object} translations.advancedRanges A map of range names to
     * advanced range data.
     * @property {Object[String[]]} translations.rangeIndicators An array of arrays
     * of strings. Each outer array represents a range indicator string. The inner
     * arrays contain the the parts of the range indicator. ie. ' thru ' -> [' thru',
     * ' thr', ' th', ' t'].
     */

    /**
     * Get the translations for the user's locale. The translation itself is only done
     * one time, the first time the a date is parsed. This logic cannot run at class
     * creation time since we cannot guaranty the message packs have been loaded at
     * that point.
     * @return {Object}
     */
    getTranslations: function() {
        if (!this.translations) {
            this.translations = this.translateStrings();
        }
        return this.translations;
    },

    /**
     * Translate everything that needs to be translated and return back an object
     * containing those translations.
     * @return {Object}
     */
    translateStrings: function() {
        var translations = this.translateDateStrings(),
            msgPath = 'rp.common.dateranges.rangeIndicators',
            rangeIndicators = RP.getMessage(msgPath);

        if (msgPath === rangeIndicators) {
            rangeIndicators = ['-'];
        }
        else {
            rangeIndicators = rangeIndicators.split(',');
        }

        translations.rangeIndicators = Ext.Array.map(rangeIndicators, function(ind) {
            // ensure there are spaces around the range indicator
            ind = Ext.String.format(' {0} ', ind.trim());

            var partials = [],
                len = ind.length;

            for (; len > 1; --len) {
                partials.push(ind.slice(0, len));
            }
            return partials;
        });

        return translations;
    },

    /**
     * Translate the different classes of range names and return an object
     * containing all the translations.
     * @return {Object}
     */
    translateDateStrings: function() {
        var translate = function(rangeStrings) {
                var results = {};
                Ext.Array.each(rangeStrings, function(rangeName) {
                    var msgPath = 'rp.common.dateranges.' + rangeName,
                        translation = RP.getMessage(msgPath);
                    if (translation !== msgPath) {
                        // We found a translation, so add the range.
                        results[translation] = rangeName;
                    }
                });
                return results;
            },
            basicRanges = translate(this.basicRangeStrings),
            singleDayRanges = translate(this.singleDayStrings),
            advancedRanges = translate(this.advancedRangeStrings);

        return {
            basicRanges: basicRanges,
            singleDayRanges: singleDayRanges,
            advancedRanges: this.processAdvancedRanges(advancedRanges)
        };
    },

    /**
     * Advanced ranges are those which accept arbitrary numerical input
     * from the user to specify the length of the range. This function
     * processes the translated ranges by splitting the translation on
     * '{0}' and replacing it with a regex to match numbers.
     * @param {Object} ranges an object containing mapped range translations.
     * @return {Object}
     */
    processAdvancedRanges: function(ranges) {
        var processed = {},
            regex = /\d+/;

        Ext.Object.each(ranges, function(translation, rangeName) {
            var parts = translation.trim().split('{0}');
            if (!parts[0]) {
                // ["", <some text>] {0} must have been at the start of the string
                parts[0] = regex;
            }
            else if (!parts[1]) {
                // [<some text>, ""] {0} must have been at the end of the string
                parts[1] = regex;
            }
            else {
                // [<some>, <text>] {0} must have been in between parts 1 and 2.
                parts = [parts[0], regex, parts[1]];
            }
            if (Ext.Array.every(parts, function(p) {return p;})) {
                // If all the parts are defined, we can add the range.
                processed[rangeName] = {
                    parts: parts,
                    fullString: translation
                };
            }
        });

        return processed;
    },

    /**
     * Parse a string and return a list of range strings that can match
     * the passed in range string.
     * @return {String[]} an array of range strings.
     */
    suggestRangeText: function(text) {
        return Ext.Array.map(this.parseRange(text), function(result) {
            return result.text;
        });
    },


    /**
     * Parse all the possible date ranges from the passed in text.
     * @param {String} text The string to parse
     * @param {String/String[]} [forceFormat=null] An optional format
     * string or array of format strings to use. If nothing is passed,
     * the {@link #defaultDateFormat} and {@link #supportedDateFormats}
     * will be used instead
     * @return {Object[]} An array of date range objects.
     */
    parseDate: function(text, forceFormat) {
        var matches, date, dates, matchingFmt, fullFmt;
            defaultFormat = this.defaultDateFormat;
            formats = forceFormat ? 
                Ext.Array.from(forceFormat) :
                [defaultFormat].concat(this.supportedDateFormats);

        text = text.trim();

        Ext.Array.some(formats, function(fmt) {
            date = Ext.Date.parse(text, fmt);
            if (date) {
                matchingFmt = fmt;
                fullFmt = this.partialToFullFormats[fmt] || fmt;
                return true;
            }
        }, this);

        if (date) {
            matches = [{
                text: Ext.Date.format(date, fullFmt),
                dateRange: RP.util.date.RangeBuilder.ranges.singleDay(date),
                format: fullFmt
            }];
        }
        else {
            // Test the string for single day ranges.
            dates = this.parseRangeString(text);
            if (dates) {
                matches = (matches || []).concat(dates);
            }
        }
        return matches;
    },

    /**
     * Parse the passed in string looking for date range translation
     * matches.
     * @param {String} text The string to parse
     * @return {Object[]} An array of ranges that match the text.
     */
    parseRangeString: function(text) {
        var rangeMatches = [],
            translations = this.getTranslations(),
            parseSimpleRanges = function(translation, rangeName) {
                if (translation.slice(0, text.length) === text) {
                    rangeMatches.push({
                        text: translation,
                        dateRange: RP.util.date.RangeBuilder.ranges[rangeName]()
                    });
                }
            };

        text = text.trim();


        Ext.Object.each(translations.singleDayRanges, parseSimpleRanges);
        Ext.Object.each(translations.basicRanges, parseSimpleRanges);

        rangeMatches = rangeMatches.concat(this.parseAdvancedRange(text));

        return rangeMatches.length ? rangeMatches : null;
    },

    /**
     * Parse the passed in string looking for advanced date range translation
     * matches. (Ones with arbitrary user specified range periods like 'next 10 days').
     * @param {String} text The string to parse
     */
    parseAdvancedRange: function(text) {
        var matches = [];
        text = text.trim();

        Ext.Object.each(this.getTranslations().advancedRanges,
            function(rangeName, rangeData) {
                var rangeText = text.slice(0),
                    translation = rangeData.fullString,
                    parts = rangeData.parts,
                    period = null, textLen, i, partial, partialLen;

                for (i = 0; i < parts.length; ++i) {
                    partial = parts[i];
                    textLen = rangeText.length;

                    if (Ext.isString(partial)) {
                        // We aren't looking at a regex.
                        partialLen = partial.length;

                        if (textLen > partialLen) {
                            // The text length is greater than this part
                            // of the range translation. If this is the case
                            // the rangeText must start with the exact partial
                            // string for us too keep trying to match the range.
                            if (rangeText.match('^' + partial)) {
                                // We had an exact match so we can keep trying
                                // to match the remaining portion of the range
                                // text.
                                rangeText = rangeText.slice(partialLen);
                            }
                            else {
                                // Break out of the each call. The string can't
                                // possibly match.
                                return true;
                            }
                        }
                        else {
                            // The text length is less than or equal to this
                            // part of the range translation.
                            if (rangeText === partial.slice(0, textLen) &&
                                    period !== null) {
                                // The rest of the range text matched the
                                // rest some or all of the translated text
                                // and we already have a time period. This
                                // means we can construct a range object.
                                matches.push(this._buildAdvancedRangeMatch(
                                    rangeName, translation, period));
                            }
                            // Break out of the each call. The string has been
                            // consumed. If we found a match, it was pushed into
                            // matches.
                            return true;
                        }
                    }
                    else {
                        // partial is a regex used to grab the period (number).
                        period = rangeText.match(partial);
                        if (period) {
                            period = period[0];
                            rangeText = rangeText.slice(period.length);

                            if (rangeText.length === 0) {
                                matches.push(this._buildAdvancedRangeMatch(
                                    rangeName, translation, period));
                            }
                        }
                        else {
                            // Break. We didn't find a number, so this string can't
                            // match this range. We're done here.
                            return true;
                        }
                    }
                }

            }, this);

        return matches;

    },

    /**
     * Parse the passed in string looking for matching date ranges.
     * @param {String} text The text to parse.
     * @return {Object[]} An array of valid ranges for the text.
     */
    parseRange: function(text) {
        text = text.trim();
        var results = this.parseDate(text),
            rangeText = {};

        if (Ext.isEmpty(results)) {
            results = this.parseDateRange(text);
        }
        return Ext.Array.filter(results || [], function(result) {
            var range = result.dateRange,
                keep = range.startDate < range.endDate && !rangeText[result.text];
            rangeText[result.text] = true;
            return keep;
        });
    },

    /**
     * Parse the passed in string looking for date ranges that are
     * use {@link #rangeIndicators range indicators}. ('6/12 - 6/15',
     * 'today - friday', 'tuesday - next 7 days').
     * @param {String} text The text to parse
     * @return {Object[]} An Array of valid ranges for the text.
     */
    parseDateRange: function(text) {
        text = text.trim();

        var rangeIndicators = this.getTranslations().rangeIndicators,
            matches = [];

        Ext.Array.each(rangeIndicators, function(indicatorArray) {
            var chunks, firstHalf, secondHalf,
                firstResults;

            Ext.Array.each(indicatorArray, function(indicator) {
                chunks = text.split(indicator);
                if (chunks.length !== 2) {
                    // The range indicator was found more than once or not at all
                    // so this is not a valid date range. try the next indicator.
                    return true;
                }

                firstHalf = chunks[0];
                secondHalf = chunks[1];
                firstResults = this.parseDate(firstHalf);

                if (!firstResults || Ext.isEmpty(firstResults)) {
                    // The first half isn't a date of any sort. This can't be
                    // a date range. break out of the each and move to the next
                    // indicator array.
                    return false;
                }

                Ext.Array.each(firstResults, function(leftObj) {
                    var leftText = leftObj.text,
                        leftRange = leftObj.dateRange,
                        startDate = leftRange.startDate,
                        fullFormat = leftObj.format || this.defaultDateFormat,
                        formats, secondResults;

                    if (Ext.isEmpty(secondHalf)) {
                        matches.push({
                            text: leftText + indicatorArray[0] +
                                Ext.Date.format(leftRange.endDate, fullFormat),
                            dateRange: {
                                startDate: startDate,
                                endDate: RP.Date.addDays(startDate, 1)
                            },
                            format: fullFormat
                        });
                    }
                    else {
                        formats = leftObj.format ?
                            this.getMatchingFormats(leftObj.format) : 
                            null;
                        secondResults = this.parseDate(secondHalf, formats);

                        if (!secondResults || Ext.isEmpty(secondResults)) {
                            // The second half isn't a date of any sort. This
                            // can't possibly be a date range so break out
                            // of the each.
                            return false;
                        }

                        Ext.Array.each(secondResults, function(rightObj) {
                            var rightRange = rightObj.dateRange;
                            if (RP.Date.compare(rightRange.startDate, leftRange.startDate) !== 0 ||
                                    RP.Date.compare(rightRange.endDate, leftRange.endDate) !== 0) {
                                matches.push({
                                    text: leftText + indicatorArray[0] + rightObj.text,
                                    dateRange: {
                                        startDate: startDate,
                                        endDate: rightRange.endDate
                                    },
                                    format: rightObj.format || leftObj.format
                                });
                            }
                        }, this);
                    }
                }, this);
            }, this);
        }, this);

        return matches;

    },

    /**
     * @private
     * Build the object for an advanced range match.
     */
    _buildAdvancedRangeMatch: function(rangeName, translation, period) {
        return {
            text: Ext.String.format(translation, period),
            dateRange: RP.util.date.RangeBuilder.ranges[rangeName](period)
        };
    },

    /**
     * Get the date formats that belong to the same group as the passed in format.
     * The {@link #defaultDateFormat} is always added to the list of matching
     * formats.
     * @param {String} fmt A date format string
     * @return {String[]} An array of date formats.
     */
    getMatchingFormats: function(fmt) {
        var matchingFormats;
        Ext.Object.each(this.regexToFormat, function(regex, formats) {
            if (fmt.match(regex)) {
                matchingFormats = formats;
                return false; // break.
            }
        }, this);
        return [this.defaultDateFormat].concat(matchingFormats);
    },

    /**
     * Parse multiple ranges in a single string.
     * @param {String} text The text to parse.
     * @return {Object[]} An array containing date range objects and/or strings.
     * If part of the string cannot be parsed into a range, it will simply be
     * returned as a string. This method will only return date ranges with exact
     * matches to the strings being considered. So 't - t' will not be treated
     * as a range though it could be expanded out to 'today - tomorrow'.
     */
    parseRanges: function(text) {
        text = text.trim();
        var chunks = text.split(/\s+/),
            sliceLen, considering, range,
            results = [];

        while (chunks.length > 0) {
            sliceLen = chunks.length;

            while (sliceLen > 0) {
                considering = chunks.slice(0, sliceLen).join(' ');
                range = this.isExactRange(considering);

                if (range) {
                    results.push(range);
                    chunks = chunks.slice(sliceLen);
                    break;
                }
                else {
                    --sliceLen;
                    if (!sliceLen) {
                        results.push(chunks.shift());
                    }
                }
            }
        }

        return results;
    },

    /**
     * Test if the text can be parsed into a date range. This
     * method will only return a date range object if the string
     * passed in exactly matches a date range. So 't - t' will
     * return false, even though it could be expanded out to 'today -
     * tomorrow'. This method will also only return a range if
     * just one range is found to be an exact match.
     * 
     * @param {String} text The text to test
     * @return {Object/false} False if the string does not exactly represent
     * a range or multiple ranges are represented exactly. A date
     * range object otherwise.
     */
    isExactRange: function(text) {
        text = text.trim();
        var ranges = this.parseRange(text);

        if (ranges.length !== 1) {
            return false;
        }

        return ranges[0].text === text ? ranges[0] : false;
    }
});
//////////////////////
// ..\js\taskflow\widgetContainer\Empty.js
//////////////////////
/**
 * Component that can be used as custom widgetViewXtype to get 
 * a full screen look.
 */
Ext.define('RP.taskflow.widgetContainer.Empty', {
    extend: 'Ext.Component',
    alias: 'widget.rp-empty-widget-container',
    width: 0 
});
//////////////////////
// ..\js\taskflow\button\Button.js
//////////////////////
Ext.define('RP.taskflow.button.Button', {
    extend: 'Ext.button.Button',
    _cls: 'rp-taskform-button',
    cls: '',
    textAlign: 'left',

    paddingOffset: 0,

    initComponent: function() {
        this.callParent();
        this.addCls(this._cls);
        if (this.menu) {
            this.menu.addCls(this._cls);
            this.menu.showSeparator = false;
            this.menu.on('boxready', this._onBoxReady, this);
        }
    },

    /**
     * @private
     * If the menu is smaller than the buton stretch
     * it to the width of the button and remove the rounded edge.
     */
    _onBoxReady: function() {
        var menu = this.menu,
            buttonWidth = this.getWidth(),
            menuWidth = menu.getWidth();

        if(menuWidth <= buttonWidth) {
            menu.addCls('rp-btn-larger-menu');
            //menu is a stretchmax layout, set the item width
            //not the menu width so the seperator extend
            //across the menu
            menu.items.first().setWidth(buttonWidth - this.paddingOffset);
        }
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

    if(Ext.data.Types){
      var thousandSeparator = '';
        if(RP.Formatting.Numbers.getThousandSeparator() === ',')
        {
          thousandSeparator = RP.Formatting.Numbers.getThousandSeparator();
        }
        Ext.data.Types.stripRe = new RegExp(Ext.String.format("[\\{0}{1}%]", RP.Formatting.Currencies.getCurrencySymbol(), thousandSeparator), "g");
    }
    
    if(Ext.Date) {
        Ext.Date.monthNames = RP.Formatting.Dates.getMonthNames();

        Ext.Date.getShortMonthName = function(month) {
          return RP.Formatting.Dates.getShortMonthNames()[month];
        }; 

        Ext.Date.monthNumbers = {
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

        Ext.Date.getMonthNumber = function(name) {
          return Ext.Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
        };

        Ext.Date.dayNames = RP.Formatting.Dates.getDayNames();

        Ext.Date.getShortDayName = function(day) {
          return RP.Formatting.Dates.getShortDayNames()[day];
        };

        Ext.Date.parseCodes.S.s = RP.getMessage("rp.common.ext.ordinalNumber");
    }
    
    if(Ext.MessageBox){
      Ext.MessageBox.buttonText = {
        ok     : RP.getMessage("rp.common.ext.ok"),
        cancel : RP.getMessage("rp.common.ext.cancel"),
        yes    : RP.getMessage("rp.common.ext.yes"),
        no     : RP.getMessage("rp.common.ext.no")
      };
    }

    if(Ext.util.Format){
        Ext.apply(Ext.util.Format, {
            thousandSeparator: RP.Formatting.Numbers.getThousandSeparator(),
            decimalSeparator: RP.Formatting.Numbers.getDecimalSeparator(),
            currencySign: RP.Formatting.Currencies.getCurrencySymbol(),
            dateFormat: RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium),
            date: function(value, format) {
              if(!value) {
                return "";
              }
              if(!Ext.isDate(value)) {
                value = new Date(Date.parse(value));
              }
              return RP.Date.dateFormat(value, (format || RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium)));
            }
        });
    }
    if(Ext.grid.PropertyColumnModel){
      Ext.apply(Ext.grid.PropertyColumnModel.prototype, {
        nameText   : RP.getMessage("rp.common.ext.nameText"),
        valueText  : RP.getMessage("rp.common.ext.valueText"),
        dateFormat : RP.Formatting.Dates.getExtShortDatePattern(),
        falseText  : RP.getMessage("rp.common.ext.falseText")
      });
    }

    if(Ext.grid.BooleanColumn){
       Ext.apply(Ext.grid.BooleanColumn.prototype, {
          trueText  : RP.getMessage("rp.common.ext.trueText"),
          falseText : RP.getMessage("rp.common.ext.falseText"),
          undefinedText: '&#160;'
       });
    }

    if(Ext.grid.NumberColumn){
        Ext.apply(Ext.grid.NumberColumn.prototype, {
            format : Ext.String.format("0{0}000{1}00", RP.Formatting.Numbers.getThousandSeparator(), RP.Formatting.Numbers.getDecimalSeparator())
        });
    }

    if(Ext.grid.DateColumn){
        Ext.apply(Ext.grid.DateColumn.prototype, {
            format : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium)
        });
    }
    if (Ext.Updater) {
        Ext.Updater.defaults.indicatorText = '<div class="loading-indicator">'+RP.getMessage("rp.common.ext.loadingText")+'</div>';
    }

    if(Ext.view.View){
      Ext.view.View.prototype.emptyText = "";
    }

    if(Ext.grid.Panel){
      Ext.grid.Panel.prototype.ddText = RP.getMessage("rp.common.ext.ddText");
    }

    if(Ext.LoadMask){
      Ext.LoadMask.prototype.msg = RP.getMessage("rp.common.ext.loadingText");
    }
    if(Ext.layout.BorderLayout && Ext.layout.BorderLayout.SplitRegion){
      Ext.apply(Ext.layout.BorderLayout.SplitRegion.prototype, {
        splitTip            : RP.getMessage("rp.common.ext.splitTip"),
        collapsibleSplitTip : RP.getMessage("rp.common.ext.collapsibleSplitTip")
      });
    }

    if(Ext.form.field.Time){
      Ext.apply(Ext.form.field.Time.prototype, {
        minText     : RP.getMessage("rp.common.ext.minTimeText"),
        maxText     : RP.getMessage("rp.common.ext.maxTimeText"),
        invalidText : RP.getMessage("rp.common.ext.invalidTimeText"),
        format      : RP.Formatting.Times.getExtPattern(RP.core.Formats.Time.Short),
        altFormats  :"g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H"
      });
    }
    if(Ext.picker.Date){
      Ext.apply(Ext.picker.Date.prototype, {
        todayText         : RP.getMessage("rp.common.ext.todayText"),
        minText           : RP.getMessage("rp.common.ext.minDateText"),
        maxText           : RP.getMessage("rp.common.ext.maxDateText"),
        disabledDaysText  : "",
        disabledDatesText : "",
        monthNames        : Ext.Date.monthNames,
        dayNames          : Ext.Date.dayNames,
        nextText          : RP.getMessage("rp.common.ext.nextMonthText"),
        prevText          : RP.getMessage("rp.common.ext.prevMonthText"),
        monthYearText     : RP.getMessage("rp.common.ext.monthYearText"),
        todayTip          : RP.getMessage("rp.common.ext.todayTip"),
        ariaTitleDateFormat: RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.FullDateWithoutDayName),
        format            : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium),
        longDayFormat     : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.FullDateWithoutDayName),
        startDay          : RP.Formatting.Dates.getWeekStartDay()
      });
    }

    if(Ext.picker.Month) {
      Ext.apply(Ext.picker.Month.prototype, {
          okText            :"&#160;OK&#160;",
          cancelText        : RP.getMessage("rp.common.ext.cancel")
      });
    }

    if(Ext.toolbar.Paging){
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

    if(Ext.form.Basic){
        Ext.form.Basic.prototype.waitTitle = RP.getMessage("rp.common.ext.waitTitle");
    }

    if(Ext.form.field.Base){
      Ext.form.field.Base.prototype.invalidText = RP.getMessage("rp.common.ext.invalidFieldText");
    }

    if(Ext.form.field.Text){
      Ext.apply(Ext.form.field.Text.prototype, {
        minLengthText : RP.getMessage("rp.common.ext.minLengthText"),
        maxLengthText : RP.getMessage("rp.common.ext.maxLengthText"),
        blankText     : RP.getMessage("rp.common.ext.blankText"),
        regexText     : "",
        emptyText     : null
      });
    }

    if(Ext.form.field.Number){
      Ext.apply(Ext.form.field.Number.prototype, {
        decimalSeparator : RP.Formatting.Numbers.getDecimalSeparator(),
        decimalPrecision : 2,
        minText          : RP.getMessage("rp.common.ext.minValText"),
        maxText          : RP.getMessage("rp.common.ext.maxValText"),
        nanText          : RP.getMessage("rp.common.ext.nanValText")
      });
    }

    if(Ext.form.field.Date){
      Ext.apply(Ext.form.field.Date.prototype, {
        disabledDaysText  : RP.getMessage("rp.common.ext.disabledText"),
        disabledDatesText : RP.getMessage("rp.common.ext.disabledText"),
        minText           : RP.getMessage("rp.common.ext.minDateText"),
        maxText           : RP.getMessage("rp.common.ext.maxDateText"),
        invalidText       : RP.getMessage("rp.common.ext.invalidDateText"),
        format            : RP.Formatting.Dates.getExtPattern(RP.core.Formats.Date.Medium),
        altFormats        : RP.Formatting.Dates.createAltString()
      });
    }

    if(Ext.form.field.ComboBox){
      Ext.apply(Ext.form.field.ComboBox.prototype, {
        loadingText       : RP.getMessage("rp.common.ext.loadingText"),
        valueNotFoundText : undefined
      });
    }

    if(Ext.form.field.VTypes){
      Ext.apply(Ext.form.field.VTypes, {
        emailText    : RP.getMessage("rp.common.ext.emailText"),
        urlText      : RP.getMessage("rp.common.ext.urlText"),
        alphaText    : RP.getMessage("rp.common.ext.alphaText"),
        alphanumText : RP.getMessage("rp.common.ext.alphanumText")
      });
    }

    if(Ext.form.field.HtmlEditor){
      Ext.apply(Ext.form.field.HtmlEditor.prototype, {
        createLinkText : RP.getMessage("rp.common.ext.createLinkText"),
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

    if(Ext.grid.header.Container){
      Ext.apply(Ext.grid.header.Container.prototype, {
        sortAscText  : RP.getMessage("rp.common.ext.sortAscText"),
        sortDescText : RP.getMessage("rp.common.ext.sortDescText"),
        columnsText  : RP.getMessage("rp.common.ext.columnsText")
      });
    }

    if(Ext.grid.GroupingFeature){
      Ext.apply(Ext.grid.GroupingFeature.prototype, {
        emptyGroupText : RP.getMessage("rp.common.ext.emptyGroupText"),
        groupByText    : RP.getMessage("rp.common.ext.groupByText"),
        showGroupsText : RP.getMessage("rp.common.ext.showGroupsText")
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
// ..\js\upb\Viewport.js
//////////////////////
/**
 * A class which represents the view of a page. This view is composed of the 
 * North panel and the Center panel. The north panel holds only the 
 * {@link RP.upb.HeaderPanel}, and the center panel holds the rest of the 
 * page's content.
 */
Ext.define("RP.upb.prototype.Viewport", {
    extend: "Ext.container.Viewport",
    
    /**
     * @cfg {Number} northHeight Height of the north panel.
     */
    northHeight: 54,
    
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
    
        var centerPanel = this.createCenterPanel(),
        northPanel = this.createNorthPanel();
        Ext.apply(this, {
            cls: (RP.globals.NAV_REGION === 'east' ? 'rp-viewport-nav-east' : 'rp-viewport-nav-west'),
            layout: {
                type: 'border',
                targetCls: ' '
            },
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
        return Ext.create("Ext.container.Container", {
            itemId: "_rp_center_panel",
            region: 'center',
            layout: {
                type: 'fit',
                targetCls: ''
            },
            items: this._createCenterItems()
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
                height: this.northHeight,
                modules: this.modules,
                id: "_rp_header_panel",
                xtype: 'rpPrototypeHeaderContainer'
        };
    },
    
    _createCenterItems: function() {
        var taskflowFrame =  Ext.create('RP.taskflow.prototype.TaskflowFrame', {
            itemId: this.taskflowFrameItemId,
            taskflows: this.taskflows,
            taskflowsEnabled: this.taskflowsEnabled,
            modules: this.modules
        });
        
        return taskflowFrame;
    }
});
//////////////////////
// ..\js\upb\HeaderContainer.js
//////////////////////
/**
 *  @class RP.upb.prototype.HeaderContainer
 */
Ext.define('RP.upb.prototype.HeaderContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.rpPrototypeHeaderContainer',
    cls: 'rp-header-prototype',

    /**
     * @cfg {String} This is the css class to be applied to the
     * buttons
     */     
    buttonCls: 'rp-header-menu-btn',

    searchbarWidth: 406,
    
    initComponent: function() {
        Ext.apply(this, {
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            defaults: {
                margin: '0 10'
            },
            items: this._createItems()
        });

        this.on('afterrender', this._afterHeaderRender, this);
        
        this.callParent();
    },

    addHeaderComponent: function(component) {
        this.insert(3, component);
    },

    _afterHeaderRender: function(headerCt) {
        // Mostly a stylistic choice.
        headerCt.el.unselectable();
    },
    
    _createItems: function() {
        this.searchBar = this._createSearchBar();

        var items = [this._createRpLogoContainer(),  this.searchBar, {
            xtype: 'component',
            flex: 1
        }, this._createUserButton(), this._createSiteButton()];

        return this._mayberReorderForNavRegion(items);
    },

    _mayberReorderForNavRegion: function(arr) {
        if(RP.globals.NAV_REGION === 'east'){
            arr.reverse();
        }

        return arr;
    },
    
    _createRpLogoContainer: function() {
        return Ext.create('Ext.container.Container', {
            height: this.height,
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            margin: '0',
            width: RP.taskflow.prototype.TaskflowFrame.prototype.modulesWidth,
            items: this._createRpLogoContainerItems()
        });
    },

    _createRpLogoContainerItems: function() {
        var logo = Ext.create('Ext.container.Container', {
            flex: 1,
            items: [{
                xtype: 'component',
                cls: 'rp-redprairie-logo'
            }]
        }),
            spacer = {
                xtype: 'component',
                width: 16
            }, items = [spacer, logo];

        return this._mayberReorderForNavRegion(items);
        },

    _createSearchBar: function() {
        // Set up any custom boolean mappings used by global search.
        RP.search.BooleanParser.requestTranslations();

        return Ext.create('RP.search.field.ComboBox', {
            itemId: 'searchBar',
            hidden: !this._isSearchable(),
            baseCls: 'rp-search-combo-box',
            clearOnSearch: true,
            fieldCls: 'rp-header-search-form-field',
            margin: '0',
            triggerCls: 'rp-search-btn',
            triggerWrapCls: 'rp-search-btn-triggerWrap',
            width: this.searchbarWidth,
            focusWidth: false,
            listeners: {
                search: this._onSearch,
                requestsearchapi: this._showSearchAPI,
                scope: this
            }
        });
    },
    
    _isSearchable: function(){
        return Ext.Array.some(this.modules, function(module){
            return module.searchable;
        });
    },

    _showSearchAPI: function() {
        var taskflowFrame = RP.upb.PageBootstrapper.getTaskflowFrame();

        if(!this.searchAPIPanel) {
            this.searchAPIPanel = Ext.create('RP.upb.prototype.SearchAPIPanel', {});

            taskflowFrame._rpViewPanel.add(this.searchAPIPanel);
        }

        var activeTaskform = taskflowFrame.getActiveTaskform();

        // We never want to set the search result panel itself as its own previousViewItem,
        // or else the panel's close button won't do anything when clicked.
        if(activeTaskform !== this.searchPanel && activeTaskform !== this.searchAPIPanel) {
            this.searchAPIPanel.previousViewItem = activeTaskform;
        } else if(activeTaskform === this.searchPanel) {
            this.searchAPIPanel.previousViewItem = this.searchPanel.previousViewItem;
        }
        
        taskflowFrame._rpViewPanel.getLayout().setActiveItem(this.searchAPIPanel);
    },

    _onSearch: function(combo, records) {
        var searchValue,
            category;
        if(records && records[0] && records[0].store.model.$className === 'RP.search.model.SearchHistory') {
            searchValue = records[0].get('query');
            category = records[0].get('category');
        } else {
            searchValue = combo.getValue();
            if(records && records[0]) {
                category = records[0].get('categoryType');
            }
        }

        if(Ext.isEmpty(searchValue) || searchValue.length < 3) {
            this.searchBar.focus();
            return;
        }

        var taskflowFrame = RP.upb.PageBootstrapper.getTaskflowFrame();

        if(!this.searchPanel) {
            this.searchPanel = Ext.create('RP.upb.prototype.SearchResultPanel');

            this.searchPanel.on('requestsearchapi', this._showSearchAPI, this);

            taskflowFrame._rpViewPanel.add(this.searchPanel);
        }

        this.searchBar._onComboBlur();

        this.searchPanel.searchCombo.focus(false, 10);
        this.searchPanel.search(category, searchValue, true);

        var activeTaskform = taskflowFrame.getActiveTaskform();

        // We never want to set the search result panel itself as its own previousViewItem,
        // or else the panel's close button won't do anything when clicked.
        if(activeTaskform !== this.searchPanel && activeTaskform !== this.searchAPIPanel) {
            this.searchPanel.previousViewItem = activeTaskform;
        } else if(activeTaskform === this.searchAPIPanel) {
            this.searchPanel.previousViewItem = this.searchAPIPanel.previousViewItem;
        }
        
        taskflowFrame._rpViewPanel.getLayout().setActiveItem(this.searchPanel);
    },

    _createUserButton: function() {
        //determine if we should display the username or the full name
        var userButtonText = RP.globals.DISLAY_USER_FULLNAME ?
                RP.globals.USER_FULL_NAME : RP.globals.USERNAME,
            btn;

        btn = this._createButton({
            itemId: 'user-button',
            text: userButtonText,
            menu: this._createUserMenu()
        });

        return btn;
    },

    _createSiteButton: function() {
        var sites = RP.core.ApplicationSites.getAllSiteIds(),
            handler, cls;

        if(sites.length > 1 ) {
            handler = this._onSelectSiteClick;
        }
        else {
            cls = this.buttonCls + ' rp-btn-no-pointer';
        }

        return this._createButton({
            itemId: 'site-button',
            text: RP.globals.SITEID,
            handler: handler,
            cls: cls,
            scope: this
        });
    },

    _createButton: function(cfg) {
        var config = Ext.applyIf(cfg, {
            height: 23,
            cls: this.buttonCls,
            minWidth:  75,
            menuAlign: 'tl-bl'
        });

        return Ext.create('RP.taskflow.button.Button', config);
    },
    
    _createUserMenu: function() {
        var items = [{
            text: RP.getMessage('rp.common.changepassword.FormTitle'),
            listeners: {
                scope: this,
                click: this._onChangePasswordClick
            },
            plain: true
        }, {
            id: '_rpExitLink',
            text: RP.upb.PageBootstrapper.isNativeLogin() === false ?
                    RP.getMessage("rp.common.misc.Close") :
                    RP.getMessage("rp.common.misc.Logout"),
            listeners: {
                scope: this,
                click: this._onExitClick
            },
            plain: true
        }];

        return Ext.create('Ext.menu.Menu', {
            minWidth: 0,
            items: items,
            lastChildCls: 'rp-last-child'
        });
    },

    _onChangePasswordClick: function() {
        if (!this.changePasswordWindow) {
            this.changePasswordWindow = Ext.create('RP.ui.ChangePassword', {
                closeAction: 'hide'
            });
        }

        this.changePasswordWindow.show();
    },

    _onExitClick: function() {
        RP.util.Helpers.logout();
    },

    _onSelectSiteClick: function() {
        if(!this.changeSiteWindow) {
            this.changeSiteWindow = new RP.upb.ChangeSiteWindow();
        }
        this.changeSiteWindow.show();
    }
});
//////////////////////
// ..\js\upb\SearchAPIPanel.js
//////////////////////
/**
 *  @class RP.upb.prototype.SearchResultPanel
 */
Ext.define('RP.upb.prototype.SearchAPIPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rpPrototypeSearchAPIPanel',

    initComponent: function() {
        this.buildAPIGrid();

        Ext.apply(this, {
            margin: RP.taskflow.BaseTaskForm.prototype.marginString,
            bodyStyle: 'border-width: 0px;',
            cls: 'rp-search-api',
            border: false,
            items: [ this.searchAPIGrid ],
            layout: 'fit',
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 36,
                cls: 'rp-search-api-header',
                items: [
                    Ext.create('Ext.container.Container', { 
                        html: RP.getMessage('rp.common.search.HelpTitle'),
                        cls: 'rp-search-api-title'
                    }),
                    '->',
                    { 
                        html: '',
                        cls: 'rp-search-results-close-btn rp-close-x-btn',
                        listeners: {
                            click: {
                                element: 'el', //bind to the underlying el property on the panel
                                fn: this._onCloseClick
                            },
                            scope: this
                        }
                    }
                ]
            }],
            header: false
        });
        
        this.callParent();

        this.on('deactivate', this._onSearchAPIDeactivate, this);
        this.on('activate', this._onSearchAPIActivate, this);
    },

    _onSearchAPIDeactivate: function(oldCard, newCard) {
        RP.upb.PageBootstrapper.getTaskflowFrame().getWidgetContainer().show();
    },

    _onSearchAPIActivate: function() {
        RP.upb.PageBootstrapper.getTaskflowFrame().getWidgetContainer().hide();
    },

    buildAPIGrid: function() {
        if(this.searchAPIGrid === undefined) {
            this.searchAPIStore = Ext.create('Ext.data.Store', {
                model: 'RP.search.model.SearchAPIField',
                sorters: [{
                    property: 'fieldLabel',
                    direction: 'ASC'
                }],
                autoLoad: true
            });

            this.searchAPIGrid = Ext.create('Ext.grid.Panel', {
                store: this.searchAPIStore,
                columns: [
                    {
                        text: RP.getMessage('rp.common.search.Type'),
                        dataIndex: 'fieldLabel',
                        width: 200
                    }, { 
                        text: RP.getMessage('rp.common.search.Resources'),
                        xtype:'templatecolumn',
                        tpl:'<tpl for="resources"><div>{resourceLabel}</div></tpl>',
                        width: 200
                    }, { 
                        text: RP.getMessage('rp.common.search.Description'),
                        dataIndex: 'fieldDescription',
                        flex: 1,
                        renderer: function columnWrap(val){
                            return '<div style="white-space:normal;">'+ val +'</div>';
                        }
                    }, {
                        text: RP.getMessage('rp.common.search.Example'),
                        dataIndex: 'fieldExample',
                        width: 200,
                        renderer: function columnWrap(val){
                            return '<div style="white-space:normal;">'+ val +'</div>';
                        }
                    }
                ],
                dockedItems: [{
                    xtype: 'toolbar',
                    dock: 'top',
                    height: 36,
                    items: ['->', 
                        {
                            xtype: 'textfield',
                            emptyText: RP.getMessage('rp.common.search.Filter'),
                            margin: '0 8 0 0',
                            listeners: {
                                change: this.onChangeFilterStore,
                                scope: this
                            }
                        }
                    ]
                }]
            });
        }
    },

    onChangeFilterStore: function(field, newValue) {
        //Split what the user typed in by space
        var tempFilterTokens = [];
        if(newValue) {
            tempFilterTokens = newValue.split(" ");
        }
        var filterTokens = [];

        // Scrub the tokens to make sure they are valid.
        // Currently only rule is must be greater than 2 characters (each token)
        Ext.each(tempFilterTokens, function(token) {
            if(!Ext.isEmpty(token) && token.length > 2) {
                filterTokens.push(token.toLowerCase());
            }
        }, this);

        if(filterTokens.length === 0) {
            //There are no valid searches so clear filters and refresh the view
            this.searchAPIStore.clearFilter(true);
            this.searchAPIGrid.getView().refresh();
            return;
        }

        var records = this.searchAPIStore.snapshot === undefined ? this.searchAPIStore.getRange() : this.searchAPIStore.snapshot.items;
        var matches = false;
        Ext.each(records, function(record) {
            var score = 0;
            Ext.each(record.fields.keys, function(key) {
                var value = record.get(key);
                if(Ext.isString(value)) {
                    Ext.each(filterTokens, function(token) {
                        if(value.toLowerCase().indexOf(token) >= 0) {
                            score++;
                            matches = true;
                        }
                    }, this);
                }
            }, this);
            if(score > 0) {
                console.log(new Date() + " - " + score);
            }
            record.set('__filter_score', score);
            record.set('__filter_match', score > 0);
        }, this);

        this.searchAPIStore.clearFilter(true);
        this.searchAPIStore.filter('__filter_match', true);
        this.searchAPIStore.sort('__filter_score', 'DESC');
    },

    _onCloseClick: function() {
        var taskflowFrame = RP.upb.PageBootstrapper.getTaskflowFrame(),
            viewPanelLayout = taskflowFrame._rpViewPanel.getLayout();

        taskflowFrame.getWidgetContainer().show();
        viewPanelLayout.setActiveItem(this.previousViewItem || this._rpViewPanel.items.first());
    }
});
//////////////////////
// ..\js\upb\SearchResultPanel.js
//////////////////////
/**
 *  @class RP.upb.prototype.SearchResultPanel
 */
Ext.define('RP.upb.prototype.SearchResultPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rpPrototypeSearchResultPanel',

    /**
     * @cfg {String} categoryCls
     * A CSS class to apply to search result category blobs.
     */
    categoryCls: 'rp-search-category',
    
    /**
     * @cfg {String} checkboxCls
     * A CSS class to apply to selection checkboxes that appear to the left of each search result row.
     */
    checkboxCls: 'rp-search-checkbox',
    
    /**
     * @cfg {String} dataviewCls
     * A top-level CSS class to apply to the search results data view
     */
    dataviewCls: 'rp-search-dataview',
    
    /**
     * @cfg {String} headerCls
     * A CSS class to apply to search result header rows
     */
    headerCls: 'rp-search-category-header',

    /**
     * @cfg {String} rowCls
     * A CSS class to apply to search result rows
     */
    rowCls: 'rp-search-category-row',

    /**
     * @cfg {Ext.panel.Panel} previousViewItem
     * (optional) The panel that was visible before the search
     * result panel was loaded, that should be displayed if
     * search results are closed.
     */

    initComponent: function() {
        this.addEvents(
            'requestsearchapi'
        );

        this.searchResultStore = Ext.create('Ext.data.Store', {
            model: 'RP.search.model.SearchCategory',
            sorters: [{
                property: 'categoryType',
                direction: 'ASC'
            }],
            autoLoad: false,
            listeners: {
                load: this.onSearchResultLoad,
                scope: this
            }
        });

        Ext.apply(this, {
            margin: RP.taskflow.BaseTaskForm.prototype.marginString,
            bodyStyle: 'border-width: 0px;',
            cls: 'rp-search',
            itemId: 'search-result-panel',
            border: false,
            items: [this.buildResultsPanel()],
            layout: 'fit'
        });
        
        this.callParent();

        this.on('deactivate', this._onSearchResultDeactivate, this);
        this.on('activate', this._onSearchResultActivate, this);
    },

    _onSearchResultDeactivate: function(oldCard, newCard) {
        RP.upb.PageBootstrapper.getTaskflowFrame().getWidgetContainer().show();
    },

    _onSearchResultActivate: function() {
        RP.upb.PageBootstrapper.getTaskflowFrame().getWidgetContainer().hide();
    },

    /**
     * @private
     * Builds a button whose menu contains navigation
     * options for selected search results.
     */
    _buildNavButton: function() {
        return Ext.create('Ext.button.Button', {
            disabled: true,
            minWidth: 80,
            textAlign: 'left',
            menu: {
                listeners: {
                    scope: this,
                    click: function(menu, item, e, eOpts) {
                        // Button menus are styled with a top toolbar.
                        // We don't want to attempt any flow logic if the toolbar is clicked.
                        if (item instanceof Ext.menu.Item) {
                            if (item.moduleName) {
                                this._flowToApplication(item.moduleName, item.taskflowName, 
                                    item.taskId, item.moduleContext, item.taskflowContext, item.taskContext);
                            }
                            else {
                                var rowEls = this.resultsView.getEl().select('.' + this.resultsView.selectedRowCls),
                                    record = this.resultsView.getSelectionModel().getSelection()[0],
                                    rowIdx, data;

                                if (record) {
                                    Ext.suspendLayouts();
                                    Ext.Array.each(rowEls.elements, function(row, index) {
                                        // Don't try to create a details tab for the header row.
                                        if(Ext.fly(row).hasCls(this.rowCls)) {
                                            rowIdx = row.getAttribute('data-index');
                                            data = record.get('data')[rowIdx];

                                            this._addDetailsTab(item, data);
                                        }
                                    }, this);

                                    Ext.resumeLayouts(true);
                                    this.resultsPanel.setActiveTab(this.resultsPanel.items.getCount() - 1);
                                }
                            }
                        }
                    },
                    show: this._onNavMenuShow
                }
            },
            text: RP.getMessage('rp.common.misc.Open'),
            width: 60
        });
    },

    _onNavMenuShow: function(menu) {
        var navItems = menu.items.getRange();

        Ext.Array.each(navItems, function(item, index) {
            var visFn = !this._isNavItemValidForSelections(
                            this._getSelectedRowItems(), item.validation) ? 'hide' : 'show';
            item[visFn]();
        }, this);
    },

    /**
     * @private
     * Grabs an array of data objects representing the data view's currently selected rows.
     */
    _getSelectedRowItems: function() {
        var rowNodes = this.resultsView.getEl().select('.' + this.resultsView.selectedRowCls).elements,
            selectedRecord = this.resultsView.getSelectionModel().getSelection()[0],
            rows = [];

        Ext.Array.each(rowNodes, function(node) {
            if(!Ext.fly(node).hasCls(this.headerCls)) {
                rows.push(selectedRecord.get('data')[node.getAttribute('data-index')]);
            }
        }, this);

        return rows;
    },

    /**
     * @private
     * Determines whether the currently selected rows should have
     * visibility to an item in the navigation button.
     */
    _isNavItemValidForSelections: function(selectedRowRecords, validations) {
        var isValid = true;

        Ext.Array.each(selectedRowRecords, function(row) {
            Ext.Array.each(validations, function(validation) {
                if((validation.equals && validation.equals.indexOf(row[validation.variablePath]) === -1) ||
                        (validation.notEquals && validation.notEquals.indexOf(row[validation.variablePath]) >= 0)) {
                    isValid = false;
                    return false;
                }
            }, this);

            if(!isValid) {
                return false;
            }
        }, this);

        return isValid;
    },

    /**
     * @private
     * Creates a new tab for displaying detailed information about a search result item.
     */
    _addDetailsTab: function(item, rowData) {
        var newNavButton = Ext.create('Ext.button.Button', {
            menu: {
                listeners: {
                    scope: this,
                    click: function(menu, item, e, eOpts) {
                        if (item instanceof Ext.menu.Item) {
                            if (item.moduleName) {
                                this._flowToApplication(item.moduleName, item.taskflowName, 
                                    item.taskId, item.moduleContext, item.taskflowContext, item.taskContext);
                            }
                        }
                    }
                }
            },
            text: RP.getMessage('rp.common.misc.Open'),
            width: 60
        });

        this.insertNavItems(newNavButton, item.rec, false);

        var tabId = rowData[item.rec.get('idField')],
            newTab = this.resultsPanel.items.findBy(function(item, key) {
                return item.itemId === tabId;
            }, this);

        if (newTab) {
            this.resultsPanel.setActiveTab(newTab);
            return;
        }

        newTab = Ext.create('Ext.panel.Panel', {
            closable: true,
            itemId: tabId,
            title: tabId,
            tpl: item.rec.get('detailsTemplate'),
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [ newNavButton ]
            }]
        });

        newTab.on('activate', this._onTabActivate, this, { single: true, detailsUrl: item.rec.get('detailsUrl') });

        this.resultsPanel.add(newTab);
    },

    /**
     * @private
     * Handler for each details tab's activate event.
     */
    _onTabActivate: function(newCard, oldCard, eOpts) {
        if (!Ext.isEmpty(eOpts.detailsUrl)) {
            Ext.Ajax.request({
                method: 'GET',
                url: eOpts.detailsUrl,
                success: function(response) {
                    newCard.update(newCard.tpl.apply(response.responseObject.data[0]));
                }
            });
        }
        else {
            logger.logDebug('Global search details page not loaded - No detailsUrl provided.');
        }
    },

    _onCloseClick: function() {
        var taskflowFrame = RP.upb.PageBootstrapper.getTaskflowFrame(),
            viewPanelLayout = taskflowFrame._rpViewPanel.getLayout();

        taskflowFrame.getWidgetContainer().show();
        viewPanelLayout.setActiveItem(this.previousViewItem || this._rpViewPanel.items.first());
    },

    buildResultsPanel: function() {
        this.searchCombo = Ext.create('RP.search.field.ComboBox', {
            itemId: 'search-result-list-combo',
            cls: 'search-results-search-combo',
            emptyText: RP.getMessage("rp.common.misc.Search"),
            hideTrigger: false,
            matchFieldWidth: true,
            triggerCls: '',
            width: 300,
            height: 24,
            margin: '8 0 0 8',
            listeners: {
                scope: this,
                search: this._onSearchComboSearch,
                expand: this._onSearchComboExpand
            }
        });

        this.resultCountContainer = Ext.create('Ext.container.Container', { 
            html: '',
            margin: '0 0 0 8',
            width: 173
        });

        this._resultsNavButton = this._buildNavButton();

        this.resultsPanel = Ext.create('Ext.tab.Panel', {
            itemId: 'search-result-tabs',
            items:[{
                title: RP.getMessage('rp.common.misc.Search'),
                itemId: 'search-result-list',
                xtype: 'panel',
                autoScroll: true,
                cls: 'search-result-list',
                items: this.buildResultsDataView(),
                dockedItems: [{
                    xtype: 'toolbar',
                    dock: 'top',
                    cls: 'rp-inset-toolbar',
                    itemId: 'search-result-list-header',
                    height: 32,
                    items: [
                        this.resultCountContainer,
                        '->',
                        {
                            xtype: 'container',
                            cls: 'rp-search-api-help',
                            margin: '0 8 0 0',
                            html: RP.getMessage("rp.common.search.HelpText"),
                            listeners: {
                                click: {
                                    element: 'el', //bind to the underlying el property on the panel
                                    fn: function() {
                                        this.fireEvent('requestsearchapi', this, undefined);
                                    }
                                },
                                scope: this
                            }
                        }
                    ]
                }, {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    height: 56,
                    padding: '18 0 0 0',
                    items: [ this._resultsNavButton ]
                }],
                tabConfig: {
                    // We don't want the first tab to appear in the tab bar's innerCt
                    // because it needs to be locked in place (in the event of overflow).
                    // A separate, fixed-position div will pushed into the tab bar later
                    // that will emulate this hidden tab's behavior.
                    hidden: true
                }
            }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                itemId: 'search-result-list-toolbar',
                cls: 'rp-toolbar-header',
                height: 36,
                items: [
                    this.searchCombo
                ]
            }],
            layout: 'fit',
            header: false
        });

        this.resultsPanel.on('afterrender', this._afterResultsPanelRender, this, { single: true });

        return this.resultsPanel;
    },

    /**
     * @private
     * Handler for the results panel's afterrender event.
     */
    _afterResultsPanelRender: function(tabPanel) {
        var tabbar = tabPanel.getTabBar(),
            barLayout = tabbar.getLayout();

        barLayout.overflowHandler.handleOverflow = Ext.bind(this.handleTabBarOverflow, this);

        // This replaces the results panel's hidden first tab element.
        // It exists outside of the innerCt so that it is always visible, even when
        // the other tabs overflow and scroll arrows are added. 
        this.mainTab = tabbar.body.createChild({
            cls: 'rp-search-results-header-tab',
            html: RP.getMessage('rp.common.misc.Search')
        }, tabbar.body.child('.' + Ext.baseCSSPrefix + 'box-scroller-left'));
        this.mainTab.on('click', function() {
            this.resultsPanel.setActiveTab(0);
        }, this);

        // The panel's close button also needs to be visible all the time,
        // regardless of how many tabs are currently open.
        this.closeButton = tabbar.body.createChild({
            cls: 'rp-search-results-close-btn rp-close-x-btn'
        }, tabbar.body.child('.' + Ext.baseCSSPrefix + 'box-scroller-right'));
        this.closeButton.on('click', this._onCloseClick, this);

        // Forces the tabbar to reserve space for the header tab & close button.
        barLayout.availableSpaceOffset = this.mainTab.getWidth() + this.closeButton.getWidth();

        // Add a style for when the data view is scrolled.  In this case I am using
        // it to show a box shadow at the top of the container when it is scrolled
        // down at all.
        this.resultsView.getEl().up('.x-panel-body').on('scroll', function(e, t, eOpts) {
            if(Ext.isIE) {
                return;
            }
            var extEl = Ext.get(t.id);

            var scroll = extEl.getScroll();
            if(scroll.top > 0) {
                extEl.addCls('scrolled-vertical');
            } else {
                extEl.removeCls('scrolled-vertical');
            }

            if(scroll.left > 0) {
                extEl.addCls('scrolled-horizontal');
            } else {
                extEl.removeCls('scrolled-horizontal');
            }
        }, this);
    },

    /**
     * Overrides a method on the results panel's tab bar layout, ensuring
     * that the tab bar always reserves space for the header & close button.
     */
    handleTabBarOverflow: function() {
        var layout = this.resultsPanel.getTabBar().getLayout(),
            result = Ext.getClass(layout.overflowHandler).prototype.handleOverflow.apply(layout.overflowHandler, arguments);

        result.reservedSpace += this.mainTab.getWidth() + this.closeButton.getWidth();
        return result;
    },

    _onSearchComboSearch: function(combo, records) {
        var category;
        if (records && records[0] && records[0].data) {
            category = records[0].data.categoryType;
            if (category === undefined) {
                category = this.previousCategory;
            }
        } else {
            category = this.previousCategory;
        }

        this._doSearch(category, combo.getValue());
    },

    _onSearchComboExpand: function() {
        if (this.skipSearchDropdown === true) {
            this.skipSearchDropdown = undefined;
            this.searchCombo.collapse();
        }
    },

    buildResultsDataView: function() {
        var me = this,
            selectedRowCls = 'rp-search-category-row-selected',
            selectChangeFn = Ext.bind(me._onSelectionModelChange, me);
        
        me.resultsView = Ext.create('Ext.view.View', {
            itemId: 'searchResultDataView',
            store: this.searchResultStore,
            itemSelector: '.rp-selectable-search-result',
            autoScroll: true,
            cls: this.dataviewCls,
            selectedRowCls: selectedRowCls,
            selModel: {
                selectedRowCls: selectedRowCls,
                listeners: {
                    selectionchange: selectChangeFn
                }
            },
            _areRowsSelected: Ext.bind(me._areRowsSelected, me),
            _selectRows: Ext.bind(me._selectRows, me),
            _flowToApplication: Ext.bind(me._flowToApplication, me),
            processItemEvent: me._processDataViewItemEvent,
            // Override so that data content rows show selected state instead of whole category "grids."
            updateIndexes : function(startIndex, endIndex) {
                var ns = this.all.elements,
                    records = this.store.getRange(),
                    i;

                startIndex = startIndex || 0;
                endIndex = endIndex || ((endIndex === 0) ? 0 : (ns.length - 1));
                for (i = startIndex; i <= endIndex; i++) {
                    var idx = Ext.fly(ns[i]).up('.rp-search-category').getAttribute('data-index');

                    ns[i].viewIndex = i;

                    ns[i].viewRecordId = records[idx].internalId;
                    if (!ns[i].boundView) {
                        ns[i].boundView = this.id;
                    }
                }
            },
            tpl: me._buildResultsTemplate(),
            listeners: {
                afterrender: function() {
                    this.el.on('click', me.onCategoryMoreClick, me, {delegate: '.rp-search-category-more-link'});
                },
                beforeselect: function() {
                    if (this.resultsView.cancelSelect === true) {
                        delete this.resultsView.cancelSelect;
                        return false;
                    }
                },
                refresh: function() {
                    this.searchResultStore.each(function(category) {
                        var plugins = category.get('plugins');
                        if (plugins) {
                            var categoryData = category.get('data');
                        }
                    }, this);
                },
                scope: me
            }
        });

        return me.resultsView;
    },

    /**
     * @private
     * Builds the template used by the search results data view
     * and any relevant utility methods used by it.
     */
    _buildResultsTemplate: function() {
        return Ext.create('Ext.XTemplate', 
                '<tpl for=".">' +
                    '<table class="rp-search-category {[xindex === xcount ? "last" : ""]}" data-index="{[xindex - 1]}">' +
                        '<tr class="rp-search-category-header rp-selectable-search-result">' +
                            '<tpl for="data">' +
                                '<tpl if="xindex == 1">' +
                                    '<td class="{[this._getCheckboxCls(parent.categoryType, parent.supportMultiSelect)]}"></td>' +
                                    '<td class="rp-search-header-content" colspan="2">{[this.applyDataTemplate(parent.headerTemplate, parent)]}</td>' +
                                '</tpl>' +
                            '</tpl>' + 
                        '</tr>' +
                        '<tpl for="data">' +
                            '<tr class="rp-selectable-search-result rp-search-category-row {[xindex === xcount ? "last" : ""]}" data-index="{[xindex - 1]}">' +
                                '<td class="{[this._getCheckboxCls(parent.categoryType, parent.supportMultiSelect)]}"></td>' +
                                '<td class="rp-search-category-data-content-cell span10">{[this.applyDataTemplate(parent.bodyTemplate, parent.data[xindex - 1])]}</td>' +
                                '<td class="right-spacer"></td>' +
                            '</tr>' +
                        '</tpl>' + 
                    '</table>' +
                    '<tpl if="totalCount &gt; resultCount">' +
                        '<div class="rp-search-category-more-link" data-index="{[xindex]}">{totalCount} results found in {[values.categoryLabel.toLowerCase()]}</div>' +
                    '</tpl>' +
                '</tpl>',
            {
                applyDataTemplate: function(template, data) {
                    var dataTemplate = Ext.create('Ext.XTemplate', template, {
                        _format: this._format,
                        formatCurrency: this.formatCurrency,
                        formatDate: this.formatDate,
                        formatDateTime: this.formatDateTime,
                        formatPercentage: this.formatPercentage,
                        formatTime: this.formatTime,
                        formatDefaultNumber: this.formatDefaultNumber
                    });
                    return dataTemplate.apply(data);
                },
                /**
                 * @private
                 */
                _getCheckboxCls: function(categoryType, isMultiSelectSupported) {
                    var cls = 'rp-search-checkbox';

                    if(isMultiSelectSupported === false || categoryType === 'module') {
                        cls += ' rp-search-checkbox-hidden';
                    }

                    return cls;
                },
                /**
                 * @private
                 * Formats a general item based on a provided format type
                 */
                _format: function(type, value, format) {
                    if (value === undefined) {
                        return;
                    }

                    try {
                        switch(type) {
                            case 'currency':
                                return RP.core.Format.formatCurrency(value,
                                        format || RP.core.Formats.Currency.Default);
                            case 'date':
                                return RP.Date.formatDate(value,
                                        format || RP.core.Formats.Date.Medium);
                            case 'datetime':
                                return RP.Date.formatDate(value,
                                        format || RP.core.Formats.Date.Long);
                            case 'percent':
                                return RP.core.Format.formatPercentage(value,
                                        format || RP.core.Formats.Percent.MediumPrecision);
                            case 'time':
                                if(!Ext.isDate(value)) {
                                    value = new Date(value);
                                }
                                return RP.Date.formatTime(value,
                                        format || RP.core.Formats.Time.Short);
                        }
                    }
                    catch(error) {}
                },
                formatCurrency: function(currency) {
                    return this._format('currency', currency);
                },
                formatDate: function(date) {
                    return this._format('date', date);
                },
                formatDateTime: function(dateTime) {
                    return this._format('datetime', dateTime);
                },
                formatPercentage: function(percent) {
                    return this._format('percent', percent);
                },
                formatTime: function(time) {
                    return this._format('time', time);
                },
                formatDefaultNumber: function(number) {
                    return RP.core.Format.formatNumber(number, RP.core.Formats.Number.Default);
                }
            }
        );
    },

    /**
     * Handler for the data view's selectionchange event
     * in charge of pushing navigation items into the _resultsNavButton's menu.
     */
    _onSelectionModelChange: function(selModel, records) {
        // Multi-category selection is not allowed, so we can safely grab the first record.
        var selectedRecord = records[0];

        this._resultsNavButton.menu.removeAll();

        // Modules should not be selectable.
        // The current design mandates that they be direct hyperlinks
        // instead, so they require special treatment.
        if (records.length === 0 || selectedRecord.get('categoryType') === 'module') {
            this._resultsNavButton.disable();
            return;
        }

        this._resultsNavButton.enable();
        
        this.insertNavItems(this._resultsNavButton, selectedRecord, !Ext.isEmpty(records));

        var visFn = selectedRecord && !Ext.isEmpty(selectedRecord.get('detailsTemplate')) &&
                        !Ext.isEmpty(selectedRecord.get('detailsUrl')) ? 'show' : 'hide';

        this._resultsNavButton.menu.findItem('detailsItem')[visFn]();
    },

    /**
     * @param {Object[]} flows
     * Pushes menu items into the nav button
     */
    insertNavItems: function(btn, record, includeDetailsItem) {
        var flows = record.get('flow');

        Ext.Array.each(flows, function(item, index) {
            btn.menu.add(Ext.create('Ext.menu.Item', Ext.apply({
                moduleContext: item.moduleContext,
                taskContext: item.taskContext,
                taskflowContext: item.taskflowContext,
                text: item.taskLabel
            }, item)));
        }, this);

        if (includeDetailsItem) {
            btn.menu.add(Ext.create('Ext.menu.Item', {
                itemId: 'detailsItem',
                text: RP.getMessage('rp.common.misc.SearchDetailsMenuItem'),
                rec: record
            }));
        }
    },

    /**
     * Override for the dataview's processItemEvent method that handles "row"
     * selection logic on mousedown.
     */
    _processDataViewItemEvent: function(record, item, index, e) {
        if (e.type == "mousedown" && e.button === 0) {
            var selectedRecords = this.getSelectionModel().getSelection();

            // Users are only allowed to have records from a single category
            // selected at a time.
            if (!Ext.isEmpty(selectedRecords) && selectedRecords[0] !== record && this._areRowsSelected() &&
                    record.get('categoryType') !== 'module') {
                // Interrupts default select logic until user has selected
                // something from the message box.
                this.cancelSelect = true;

                Ext.Msg.show({
                    title: RP.getMessage('rp.common.misc.ChangeSearchCategoryTitle'),
                    msg: RP.getMessage('rp.common.misc.ChangeSearchCategory'),
                    buttons: Ext.Msg.OKCANCEL,
                    fn: function(buttonId, text, obj) {
                        if (buttonId === 'ok') {
                            this._selectRows(record, item, index, e, true);
                            delete this.cancelSelect;
                            this.select(record);
                        }
                    },
                    icon: Ext.MessageBox.QUESTION,
                    scope: this
                });
            }
            else {
                this._selectRows(record, item, index, e, false);
            }
        }
    },

    /**
     * @private
     * Indicates whether any rows in the resultsView are currently selected.
     * We can't just look at the data view's selection model because it doesn't
     * deselect a category record when all rows within that category are unchecked.
     */
    _areRowsSelected: function() {
        var rows = this.resultsView.getEl().select('.' + this.resultsView.selectedRowCls);

        return rows.elements.length > 0;
    },

    /**
     * @private
     * Applies selected styles to search result rows based on
     * click target and existing selections.
     */
    _selectRows: function(record, item, index, e, deselectAll) {
        var itemEl = Ext.get(item),
            categoryTable = itemEl.up('.' + this.categoryCls),
            checkbox = itemEl.down('.' + this.checkboxCls),
            rows, styleFn, 
            allRowsChecked = true,
            view = this.resultsView,
            isMultiSelectable = record.get('supportMultiSelect');

        if (deselectAll && view.getSelectionModel().getSelection()[0]) {
            this._deselectItems(itemEl, view);
        }

        // By design, modules are not directly selectable. Clicking one
        // should just take the user to that module.
        if(record.get('categoryType') === 'module') {
            return;
        }

        if (itemEl.hasCls(this.headerCls)) {
            // As with true grids using the checkbox selection model,
            // clicking on the header's checkbox should select / deselect
            // all records within a category.
            if (isMultiSelectable !== false && e.within(checkbox)) {
                rows = categoryTable.select('.' + this.rowCls);
                styleFn = itemEl.hasCls(view.selectedRowCls) ? 'removeCls' : 'addCls';

                itemEl[styleFn](view.selectedRowCls);

                Ext.Array.each(rows.elements, function(row, index) {
                    Ext.fly(row)[styleFn](view.selectedRowCls);
                }, this);
            }
        }
        else {
            rows = itemEl.parent().select('.' + this.rowCls);

            if (isMultiSelectable !== false && e.within(checkbox)) {
                var headerEl = categoryTable.down('.' + this.headerCls);
                styleFn = itemEl.hasCls(view.selectedRowCls) ? 'removeCls' : 'addCls';

                itemEl[styleFn](view.selectedRowCls);

                if (styleFn === 'addCls') {
                    Ext.Array.each(rows.elements, function(row, index) {
                        if (!Ext.fly(row).hasCls(view.selectedRowCls)) {
                            allRowsChecked = false;
                            return false; // Break
                        }
                    }, this);

                    if (allRowsChecked) {
                        headerEl.addCls(view.selectedRowCls);
                    }
                }
                else {
                    headerEl.removeCls(view.selectedRowCls);
                }
            }
            else {
                Ext.Array.each(rows.elements, function(row, index) {
                    styleFn = (row === item) ? 'addCls' : 'removeCls';

                    Ext.fly(row)[styleFn](view.selectedRowCls);
                }, this);
            }
        }
    },

    /**
     * @private
     * Deselect all row / header checkboxes that do not belong
     * to the same category as the passed item.
     */
    _deselectItems: function(item, view) {
        var dataViewEl = item.up('.' + this.dataviewCls),
            categoryEl = item.up('.' + this.categoryCls),
            allRows = dataViewEl.select('.' + this.rowCls),
            allHeaders = dataViewEl.select('.' + this.headerCls),
            categoryRows = categoryEl.select('.' + this.rowCls),
            categoryHeader = categoryEl.down('.' + this.headerCls);

        Ext.Array.each(allRows.elements, function(row, index) {
            if (categoryRows.elements.indexOf(row) === -1) {
                Ext.fly(row).removeCls(view.selectedRowCls);
            }
        }, this);

        Ext.Array.each(allHeaders.elements, function(row, index) {
            if (row !== categoryHeader) {
                Ext.fly(row).removeCls(view.selectedRowCls);
            }
        }, this);
    },

    /**
     * Called externally to perform a search action
     */
    search: function(category, query, limit) {
        // Set a flag so the dropdown can be kept from showing,
        // but only if there's a query.
        if (!Ext.isEmpty(query)) {
            this.skipSearchDropdown = true;
        }
        
        var comboValue = this.searchCombo.getValue();

        if (comboValue !== query) {
            // Make sure the searchCombo reflects what was entered into
            // the global search bar, but don't run any extra logic.
            // (setValue has a bunch of extra baggage that we don't want here.)
            this.searchCombo.setRawValue(query);
        }

        this._doSearch(category, query, limit);
    },

    /**
     * @private
     * Used internally to perform a search action.
     */
    _doSearch: function(category, query, limit) {
        this.previousCategory = category;
        this.previousQuery = query;

        if (limit !== undefined) {
            this.previousLimit = limit;
        } else if (limit !== false) {
            limit = this.previousLimit;
        }

        this.setSearchResultTitle(query);

        this.lastParams = {
            query: RP.search.StringParser.parseSearchString(query),
            rawQuery: query,
            category: category,
            queryLimit: limit
        };

        this.searchResultStore.load({
            params: this.lastParams
        });

        this.resultsPanel.setActiveTab(0);
    },

    onSearchResultLoad: function(store, groupers, eOpts) {
        var query = this.lastParams.rawQuery,
            category = this.lastParams.category,
            count = 0;

        store.data.each(function(item, index, self) {
            count += item.data.totalCount;
        }, this);

        // Assuming the history for this search is already there,
        // we will update the count to reflect the actual results.
        Ext.each(RP.search.field.ComboBox.searchHistory, function(data) {
            if (data.query === query && data.category === category) {
                data.totalCount = count;
            }
        });

        this.setSearchResultTitle(query, count, store.count());

        this.searchCombo.focus(false);
    },

    setSearchResultTitle: function(query, queryResultCount, categoryCount) {
        var inMsg = RP.getMessage('rp.common.misc.in'),
            resultMsg = RP.getMessage('rp.common.search.Result'),
            resultsMsg = RP.getMessage('rp.common.search.Results'),
            categoryMsg = RP.getMessage('rp.common.search.Category'),
            categoriesMsg = RP.getMessage('rp.common.search.Categories');

        var titleString = ' ';
        if(queryResultCount !== undefined) {
            titleString = queryResultCount + ' ';
            titleString += queryResultCount === 1 ? resultMsg : resultsMsg;
            if(categoryCount !== undefined) {
                titleString += ' ' + inMsg + ' ' + categoryCount + ' ';
                titleString += categoryCount === 1 ? categoryMsg : categoriesMsg;
            }
        }

        this.resultCountContainer.update('<div class="query-result-count">' + titleString + '</div>');
    },

    /**
     * @private
     * Click handler for each category's more link.
     */
    onCategoryMoreClick: function(event, element, eOpts){
        var categoryIndex = element.getAttribute('data-index'),
            categoryRow = this.searchResultStore.getAt(categoryIndex).data,
            category = categoryRow.categoryType,
            query = this.lastParams.rawQuery;

        this.search(category, query, false);
    },

    /**
     * @private
     * Navigates to a page, given module / taskflow / task data.
     */
    _flowToApplication: function(moduleName, taskflowName, taskId, moduleContext, taskflowContext, taskContext) {
        var pageContext = RP.core.PageContext,
            url = pageContext.getPageURL(
                RP.globals.getValue("SITEID"),
                moduleName,
                taskflowName,
                taskId,
                moduleContext,
                taskflowContext,
                taskContext);
        RP.util.Helpers.redirect(url);
    }
});
//////////////////////
// ..\js\upb\ChatMenu.js
//////////////////////
/**
 *  @class RP.upb.prototype.ChatMenu
 *  This class represents the drop down menu that comes down
 *  from the chat button.  Through config it can also be used 
 *  as a task menu.
 */
Ext.define('RP.upb.prototype.ChatMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.rpPrototypeHeaderChatMenu',

    /**
     * @cfg {String} This is the cls that is applied to the
     * body of this chatmenu's underlying menu element.
     */    
    chatMenuCls: 'rp-header-chatmenu',

    /**
     * @cfg {String} This is the css that is applied to the
     * text that is on the header of this menu
     */    
    chatTextCls: 'rp-chat-header-text',

    /**
     * @cfg {String} This is the css that is applied
     * to the header container itself
     */    
    chatHeaderCls: 'rp-chat-header',

    /**
     * @cfg {String} The css class that is applied to the
     * new button in this chat menu's header
     */    
    chatBtnCls: 'rp-new-btn',

    /**
     * @cfg {String} The css class that is applied to the 
     * new button's icon in the header
     */    
    chatBtnIconCls: 'rp-new-btn-icon',
    
    /**
     * @cfg {String} menuTitle:  the title to be displayed on the menu header
     * defaults to "Messages"
     */
    menuTitle: "Messages",
    
    /**
     * @cfg (Boolean} useCheckboxes: true to apply show checkboxes
     * for all children, false to hide them
     * defaults to false
     */
    useCheckboxes: false,
    
    initComponent: function() {
        Ext.apply(this, {
            bodyCls: this.chatMenuCls,
            items: this._createItems(),
            width: 285,
            defaults: {
                width: '100%',
                height: 70
            }
            
        });
        this.callParent();
    },
    
    /**
     * @private
     *  add the header to the front of the presupplied list 
     *  of items. 
     *  We want it to be at the top of the container.     
     */
    _createItems: function() {     
        if(this.items){
            this.items.unshift(this._createHeader());
        }
        
        return this.items;
    },
    
    /**
     * @private
     * this creates the toolbar that is rendered in the panel
     * which is docked to the top of the menu.
     **/
    _createHeaderToolbar: function() {
        return Ext.create('Ext.toolbar.Toolbar', {
            items: [{
                xtype: 'label',
                text: this.menuTitle,
                cls: this.chatTextCls
            },{
                xtype: 'tbfill'
            },{
                xtype: 'button',
                baseCls: this.chatBtnCls,
                iconCls: this.chatBtnIconCls,
                text: RP.getMessage("rp.common.misc.NewBtn"),
                textAlign: 'left',
                margin: '3px 3px 0 0',
                handler: this._newBtnClickHandler,
                scope: this
            }],
            baseCls: this.chatHeaderCls,
            height: '100%'
        });
    
    
    },
    
    /**
     *  Dummy implementation of this for now; should later 
     *  bring up a prompt to ask for information about the new message
     */
    _newBtnClickHandler: function(){
        var newMessage = Ext.create('RP.upb.prototype.ChatMessage', {
            showCheckbox: this.useCheckboxes });
        
        this.add(newMessage);
    },
    
    /**
     * @private
     * this creates the header that conttains the header 
     * toolbar. 
     */
    _createHeader: function() {
        return Ext.create('Ext.panel.Panel', {
            height: 35,
            dock: "top",
            tbar: this._createHeaderToolbar()
        });
    
    }
    
});
//////////////////////
// ..\js\upb\ChatMessage.js
//////////////////////
/**
 *  @class RP.upb.prototype.ChatMessage
 *
 *  All attributes can be accessed publicly after creation.
 *
 *  nameContainer = where "senderName" is applied; the title 
 *                  container of the message.
 *  timeContainer = where the time is displayed; a small 
 *                  container in the upper right
 *  ordnumButton = the optional button that displays the 
 *                 order number and can link to a certain 
 *                 order. NOTE: this will be null if 
 *                 showOrderNumber was false upon creation
 *  arrowContainer = the lower right container that displays 
 *                   the arrow graphic
 *  messageContainer = the main container of the panel; 
 *                     displays the contents of message. 
 */
Ext.define('RP.upb.prototype.ChatMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rpPrototypeHeaderChatMessage',
    
    /**
     * @cfg {String} senderName the name to be displayed with the message 
     */
    senderName: "Default Name",
    
    /**
     * @cfg {String} message the message to be displayed
     */
    message: "Default Message",
    
    /**
     * @cfg {Number} orderNumber A refrenced order number
     */
    orderNumber: 0,
    
    /**
     * @cfg {Number/String} time the time this message was 
     * created/sent 
     */
    time: 0,
    
    /**
      * @cfg {Boolean} showOrderNumber determines if this 
      * message will link to an order or not. 
      */
    showOrderNumber: false,
    
    /**
      * @cfg {string} this is the css that will be applied to the 
      * time display.
      */
    messageTimeCls: 'rp-header-timecontainer-text',
    
    /**
     * @cfg {String} This is the cls that is applied to this 
     * message for the background color and text wrapping.
     */
    messageCls: 'rp-header-chatmessage',
    
    /**
     * @cfg {String} This cls is is the css that is applied
     to the text message header, that is, the text that
     displays the sender's name.
     */    
    messageTextCls: 'rp-header-chatmessage-text',

    /**
     * @cfg {String} This is the css that is applied to the optional
     * checkbox btn that can be shown with the tasks/messages
     */    
    checkboxBtnCls: 'rp-chatmessage-checkbox-btn',

    /**
     * @cfg {String} this is the css class that styles
     * the button when it is pressed
     */    
    checkboxBtnPressedCls: 'rp-chatmessage-checkbox-btn-pressed',

    /**
     * @cfg {String} A css class that styles the ordnum btn
     * that is optional to display at the end of the message.
     */    
    ordnumBtnCls: 'rp-chatmenu-ordnum-btn',

    /**
     * @cfg {String} This is the css class that is applied
     * to the container that shows the arrow on the right side
     * of a message.
     */    
    arrowCls: 'rp-chatmessage-icon',
    
    /**
     * @cfg {String} showCheckboxes determines if this message will have a checkbox next to it.
     * This can be used in the event that this message is a task.
     */
    showCheckbox: false,
    
    initComponent: function() {
        Ext.apply(this, {
            baseCls:  this.messageCls,
            items: this._createContainers(),
            defaults: {
               //make sure all messages keep text away from
               //thier sides
               padding: '4px 0px 0px 8px'
            },
            layout: 'column',
            autoScroll: false
           
        });
        
        this.callParent();

    },
    
    
    _createContainers: function(){
    
        var objects = [ this._createNameContainer(), 
        this._createTimeContainer(),
        this._createMessageContainer(), 
        this._createArrowContainer()];
    
        //if we need a checkbox for this menu, add it to the
        //front of the list of items
        if(this.showCheckbox){
            objects.unshift(this._createCheckboxContainer());
        }
    
        return objects;
    
    
    },
    
    /**
     * @private
     * This creates the container that the senderName is 
     * displayed in.  It is rendered at the top of the 
     * container.
     */
    _createNameContainer: function(){
        this.nameContainer = Ext.create('Ext.container.Container', {
            height: 20,
            columnWidth: 0.85,
            layout: 'fit',
            items: [{
                xtype: 'label',
                text: this.senderName,
                cls: this.messageTextCls
            }]
        });
    
        return this.nameContainer;
    
    },
    
    /**
     * @private
     * This creates the checkbox container that is docked to 
     * the left of each individual chat message if that option
     * config is true
     */
    _createCheckboxContainer: function(){
        this.checkBoxContainer = Ext.create('Ext.container.Container', {
            dock: 'left',
            height: 70,
            buttonAlign: 'center',
            items: [{
                xtype: 'container',
                //keep the checkbox centered vertically
                height: 20
            },{
                xtype: 'button',
                cls: this.checkboxBtnCls,
                pressedCls: this.checkboxBtnPressedCls,
                hoverCls: this.checkboxBtnCls,
                height: 15,
                width: 15,
                enableToggle: true
            }]
        
        });
        
        return this.checkBoxContainer;
    },
    
    /**
     * @private
     * This will create the main message container for this 
     * message that displays the message text.  If
     * showOrderNumber is true, then a button containing
     * a link to a specified order number will also be placed.
     **/
    _createMessageContainer: function(){
        //we need to limit the length of the string to prevent
        //the container from scrolling.  If it gets too long,
        //add a ... to show that there are more contents
        var shortenedMessage = this.message;
        var threshold = 140;
        if(shortenedMessage.length > threshold){
            shortenedMessage = shortenedMessage.substring(0, threshold) + "...";
        }
    
        this.messageContainer = Ext.create('Ext.container.Container', {
            height: 50,
            width: '100%',
            columnWidth: 0.97,
            items: [{
                xtype: 'label',
                text: shortenedMessage
            }]
        });
        
        if(this.showOrderNumber){
            this.messageContainer.add(this._createOrdnumButton());
        }
    
        return this.messageContainer;
    },
    
    /**
     * @private
     * This will create the optional button that can link to
     * a message.
     */
    _createOrdnumButton: function(){
        this.ordnumButton = Ext.create('Ext.button.Button', {
            text: 'ORD' + this.orderNumber,
            cls: this.ordnumBtnCls,
            height: 16
        });
    
        return this.ordnumButton;
    },
    
    /**
     * @private
     * This will create the container that contains the time
     * of this message. It is rendered in the top right of
     * the message container.
     */
    _createTimeContainer: function(){
        this.timeContainer = Ext.create('Ext.container.Container', {
            //unformatted time for now; dummy data
            html: '' + this.time,
            cls: this.messageTimeCls,
            style: {
                //keep it away from the right of the panel a bit
                margin: '0 3px 0 0'
            },
            columnWidth: 0.15,
            height: 20
        });
    
        return this.timeContainer;
    },
    
    /**
     * @private
     * This will create the container that is styled to display
     * an arrow docked on the right side of the container.  
     */
    _createArrowContainer: function(){
        this.arrowContainer = Ext.create('Ext.container.Container', {
            cls: this.arrowCls,
            height: 50,
            columnWidth: 0.03
        });
        
        return this.arrowContainer;
    },
    
    /**
     * @return {String} The message content of this chatMessage
     */
    getMessage: function(){
        return this.message;
    },
    
    /**
     * Set the message of this chatMessage
     * @param message {String} the new message
     */
    setMessage: function(message){
        this.message = message;
    },
    
    /**
     * @return {String/Number} The time this chatMessage was created
     */
    getTime: function(){
        return this.time;
    },
    
    /**
     * @return {String} The the sender of this Message
     */
    getSender: function(){
        return this.senderName;
    },
    
    _shortenMessage: function(message){
        
    }
    
    
    
    
});
//////////////////////
// ..\js\..\..\stashLibEndLoad.js
//////////////////////
RP.stash.api.endLoadLib();
