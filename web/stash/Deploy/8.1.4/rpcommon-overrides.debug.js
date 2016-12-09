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
// ..\js\overrides\..\..\..\stashLibBeginLoad.js
//////////////////////
RP.globals = RP.globals || {};
RP.stash.api.beginLoadLib();
//////////////////////
// ..\js\overrides\AbstractComponent.js
//////////////////////
/*
 * As of Ext JS 4.0.2, all components are now stateful by default.
 * Initially, this was causing the following error within TaskflowFrame:
 * 
 * me["get" + layout.perpendicularPrefixCap] is not a function
 * 
 * This was also causing many extra state cookies to be created for components we
 * don't really care about, such as help and logout links only to name a couple.
 */
Ext.override(Ext.AbstractComponent, {
    stateful: false
});
//////////////////////
// ..\js\overrides\Component.js
//////////////////////
Ext.override(Ext.Component, {
    /**
     * @override
     * @version 4.0.2a - 4.1.0
     * http://www.sencha.com/forum/showthread.php?141176-Loading-mask-does-not-stay-centered-when-the-window-is-resized
     */
    initComponent: function() {
        this.callOverridden(arguments);

        this.on('afterrender', function() {
            this.on('resize', function() {
                // In addition to returning a boolean (which we don't actually care about),
                // isMasked() centers the load mask if it exists and is visible.
                this.el.isMasked();
            }, this);
        }, this, { single: true });
    }
});
//////////////////////
// ..\js\overrides\Element.js
//////////////////////
Ext.Element.addMethods(function() {
    var defaultMaskColor = "#000";
    var defaultMaskOpacity = 0.85;
    var defaultZIndex = 50000;
    
    return {
        /**
         * @method
         * @member Ext.Element
         * 
         * Extends the Ext Element.mask() function to provide options for mask
         * color and opacity. If color and opacity not specified, default is a
         * very dark mask (85% black).
         * @param {String} msg (optional) A message to display in the mask
         * @param {String} msgCls (optional) A css class to apply to the msg
         * element
         * @param {Float} opacity (optional) Mask opacity; default is 0.85
         * @param {String} color (optional) Mask color; default is '#000'
         * @return {Element} The mask element
         */
        maskEx: function(msg, msgCls, opacity, color) {
            color = color || defaultMaskColor;
            opacity = opacity || defaultMaskOpacity;
            
            var m = this.mask(msg, msgCls);
            m.setStyle("background-color", color);
            m.setStyle("opacity", opacity);
            m.setStyle("z-index", defaultZIndex);
        },
        
        /**
         * @method
         * @member Ext.Element
         * 
         * Checks to see if the Element's innerHTML is empty ("" or "&nbsp;").
         * @return {Boolean}
         */
        isEmpty: function() {
            var html = Ext.String.trim(this.dom.innerHTML);
            
            return html == "" || html == "&nbsp;";
        }
    };
}());
//////////////////////
// ..\js\overrides\DateField.js
//////////////////////
Ext.override(Ext.form.field.Date, {
    getErrors: function(value) {
        var errors = this.callOverridden(arguments);
        for (var i = 0; i < errors.length; i++) {
            errors[i] = errors[i].replace(this.format, RP.Date.translateFormat(this.format));
        }
        return errors;
    }
});
//////////////////////
// ..\js\overrides\DateOverrides.js
//////////////////////
Ext.ns("RP.date");

(function() {
    RP.date.NativeDate = Date;
    RP.date.DateOverride = function() {
        var date = new RP.date.NativeDate();

        // Look for an ISO date string in the constructor argument, and if
        // exists, construct
        // using Ext's parseDate method...
        if (arguments.length === 1 && typeof arguments[0] === "string" && arguments[0].match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/)) {
            date = RP.Date.extParseDate(arguments[0], "c");

            // Get the server time zone off of the date string
            // and store it in decimal form on the date object.
            // this is used for RP.core.Format.formatDateNoTranslation
            var isoDateNoOffset = arguments[0].replace(/(\+|-)(\d?\d:?\d\d)/, "");
            if (!Ext.isEmpty(isoDateNoOffset)) {
                date.isoDateNoOffset = isoDateNoOffset;
            }
        }
        else if (arguments.length === 0) {
            return new RP.date.NativeDate();
        }
        else if (arguments.length === 1) {
            // Work around weird Firefox behavior where if the string is any 4
            // digit number,
            // it translates the date to Dec. 31 7PM and the number as the
            // year... Workaround
            // is to simply insert a colon after the first two digits, so e.g.,
            // "1400" becomes
            // 14:00, which is 2:00 PM
            var str;
            if (Ext.isGecko && Ext.isString(arguments[0]) && arguments[0].match(/^\d{4}$/)) {
                str = arguments[0].substr(0, 2) + ":" + arguments[0].substr(2);
            }
            // Work around for Chrome trying to compensate for a time being
            // invalid.
            // When a time with minutes greater than 59 is parsed by Date's
            // constructor, Chrome
            // assumes that the number is meant for the years of the date. For
            // example,
            // new Date("6:70") will create a date with the year = 1970 and the
            // hour = 6, but the
            // minutes will be 00. We're expecting Chrome to behave like Firefox
            // here and return an
            // "Invalid Date", but it does not.
            else if (Ext.isChrome && Ext.isString(arguments[0]) && arguments[0].match(/:\d+/)) {
                var testDate = new RP.date.NativeDate(arguments[0]);
                var minuteInt = parseInt(arguments[0].match(/:(\d+)/)[1], 10);

                if (minuteInt >= 60 && testDate && !isNaN(testDate.getFullYear())) {
                    return new RP.date.NativeDate("Invalid date");
                }
                str = arguments[0];
            }
            else {
                str = arguments[0];
            }
            date = new RP.date.NativeDate(str);
        }
        else if (arguments.length > 1) {
            date = new RP.date.NativeDate(arguments[0], arguments[1], arguments[2], arguments[3] || 0, arguments[4] || 0, arguments[5] || 0, arguments[6] || 0);

            if (date.toString() === "Invalid Date" && arguments.length === 1) {
                date = new RP.date.NativeDate(arguments[0]);
            }
        }

        if(arguments.length >= 1 && arguments[0] instanceof Date) {
            if(arguments[0].isoDateNoOffset !== undefined) {
                date.isoDateNoOffset = arguments[0].isoDateNoOffset;
            }
        }

        return date;
    };

    // Add functionality from the native Date object to support
    // all base/Ext functionality in addition to our new stuff.
    for (var prop in RP.date.NativeDate) {
        if (prop !== "constructor") {
            RP.date.DateOverride[prop] = RP.date.NativeDate[prop];
        }
    }
    RP.date.DateOverride.prototype = RP.date.NativeDate.prototype;

    // Replace the native Date object with our new and improved version.
    Date = RP.date.DateOverride;
    Date.parse = RP.date.NativeDate.parse;
    Date.UTC = RP.date.NativeDate.UTC;
})();

(function() {
    /*
     * This method will set the time value (milliseconds|seconds|minutes) of a date object to the passed in value.
     *
     * @param {Date} date The date to set the time value of.
     * @param {Number} newValue The new time value to set the date to.
     * @param {String} timeUnitGetterFnName The function that will be used to get
     * the value of the proper unit that you are modifying (ex. getMilliseconds,
     * getSeconds).
     * @param {Number} multiplier The number that is used to convert the time
     * unit to milliseconds.
     */
    var _setTimeValue = function(date, newValue, timeUnitGetterFnName, multiplier) {
        var currentTimeValue = date[timeUnitGetterFnName]();
        date.setTime((date.getTime() - currentTimeValue * multiplier) + newValue * multiplier);
    };

    var _fixDstCompensation = function(date, isMonthMovingForward, fn, args) {
        // call the setter function in the scope of the date using the supplied arguments
        fn.apply(date, args);

        var beforeOffset = date.getTimezoneOffset();
        var afterOffset = Ext.Date.add(date, Ext.Date.HOUR, 1).getTimezoneOffset();
        var dstShift = Math.abs(afterOffset - beforeOffset);
        var isInDstTimeRange = beforeOffset < afterOffset;
        if (isInDstTimeRange && isMonthMovingForward === false) {
            date.setTime(Ext.Date.add(date, Ext.Date.MINUTE, dstShift).getTime());
        }
    };

    var nativeSetMonth = Date.prototype.setMonth;

    Ext.apply(Date.prototype, {
        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setMillisecond which
         * will incorrectly set the Time Zone when dealing with the hour between
         * 1:00 am and 1:59 am on a Daylight Savings Day.
         *
         * <p>
         * This method will set the milliseconds of the date object to the given
         * value.
         *
         * @param {Number} value The new milliseconds value to set the date to.
         */
        setMilliseconds: function(milliseconds) {
            _setTimeValue(this, milliseconds, "getMilliseconds", 1);
        },

        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setSeconds which will
         * incorrectly set the Time Zone when dealing with the hour between 1:00
         * am and 1:59 am on a Daylight Savings Day.
         *
         * <p>
         * This method will set the seconds of the date object to the given
         * value.
         *
         * @param {Number} value The new seconds value to set the date to.
         */
        setSeconds: function(seconds, milliseconds) {
            _setTimeValue(this, seconds, "getSeconds", 1000);
            if (!Ext.isEmpty(milliseconds)) {
                this.setMilliseconds(milliseconds);
            }
        },

        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setMinutes which will
         * incorrectly set the Time Zone when dealing with the hour between 1:00
         * am and 1:59 am on a Daylight Savings Day.
         *
         * This method will set the minutes of the date object to the given
         * value.
         * @param {Number} value The new minutes value to set the date to.
         */
        setMinutes: function(minutes, seconds, milliseconds) {
            _setTimeValue(this, minutes, "getMinutes", 60000);
            if (!Ext.isEmpty(seconds) || !Ext.isEmpty(milliseconds)) {
                this.setSeconds(seconds, milliseconds);
            }
        },

        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setHours which will
         * incorrectly set the Time Zone when dealing with the hour between 1:00
         * am and 1:59 am on a Daylight Savings Day.
         *
         * This method will set the hours of the date object to the given value.
         * @param {Number} value The new hours value to set the date to.
         */
        setHours: function(hours, minutes, seconds, milliseconds) {
            var beforeIsDst = Ext.Date.isDST(this);
            var beforeTimeZoneOffset = this.getTimezoneOffset();

            // call the setter function in the scope of the date using the supplied arguments
            _setTimeValue(this, hours, "getHours", 3600000);
            if (!Ext.isEmpty(minutes) || !Ext.isEmpty(seconds) || !Ext.isEmpty(milliseconds)) {
                this.setMinutes(minutes, seconds, milliseconds);
            }

            var afterIsDst = Ext.Date.isDST(this);
            var afterTimeZoneOffset = this.getTimezoneOffset();
            var dstShift = Math.abs(afterTimeZoneOffset - beforeTimeZoneOffset);

            // Compensate for dst
            if (beforeIsDst === false && afterIsDst === true && hours !== 2) { // we've moved into DST and it's not the 2:00 hour
                this.setTime(Ext.Date.add(this, Ext.Date.MINUTE, -dstShift).getTime());
            }
            else if (beforeIsDst === true && afterIsDst === false) { // we've moved out of DST
                this.setTime(Ext.Date.add(this, Ext.Date.MINUTE, dstShift).getTime());
            }

        },

        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setDate which will
         * incorrectly set the Time Zone when dealing with the hour between 1:00
         * am and 1:59 am on a Daylight Savings Day.
         *
         * This method will set the day of the date object to the given value.
         * @param {Number} value The new day value to set the date to.
         */
        setDate: function(day) {
            var beforeIsDst = Ext.Date.isDST(this);
            var beforeTimeZoneOffset = this.getTimezoneOffset();

            // call the setter function in the scope of the date using the supplied arguments
            _setTimeValue(this, day, "getDate", 86400000);

            var afterIsDst = Ext.Date.isDST(this);
            var afterTimeZoneOffset = this.getTimezoneOffset();
            var dstShift = Math.abs(afterTimeZoneOffset - beforeTimeZoneOffset);

            // Compensate for dst
            if (beforeIsDst === false && afterIsDst === true) { // we've moved into DST
                this.setTime(Ext.Date.add(this, Ext.Date.MINUTE, -dstShift).getTime());
            }
            else if (beforeIsDst === true && afterIsDst === false) { // w've moved out of DST
                this.setTime(Ext.Date.add(this, Ext.Date.MINUTE, dstShift).getTime());
            }
        },

        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setMonth which will
         * incorrectly set the Time Zone when dealing with the hour between 1:00
         * am and 1:59 am on a Daylight Savings Day.
         *
         * This method will set the month of the date object to the given value.
         * @param {Number} value The new month value to set the date to.
         */
        setMonth: function(month, date) {
            var currentMonth = this.getMonth();
            var monthsToAdd = month - currentMonth;
            var day = this.getDate();
            if (currentMonth == month && Ext.isEmpty(date)) {
                return;
            }

            // Check and see if we are moving forward or backward in months.
            var isMonthMovingForward = currentMonth < month;

            // Set the date to the 1st of the month just so it's in a date that exists in all months.
            // We need to account for dst because some times setting the date to 1 will cross the DST threshold.
            _fixDstCompensation(this, isMonthMovingForward, this.setDate, [1]);

            // Add the months
            _fixDstCompensation(this, isMonthMovingForward, nativeSetMonth, [currentMonth + monthsToAdd]);

            // Set the date back to the right date and compensate for DST
            // Set the date to the 1st of the month just so it's in a date that exists in all months.
            // We need to account for DST because sometimes setting the date to 1 will cross the DST threshold.
            _fixDstCompensation(this, isMonthMovingForward, this.setDate,
                [Math.min(day, Ext.Date.getDaysInMonth(new Date(this.getFullYear(), month, 1)))]);

            if (!Ext.isEmpty(date)) {
                this.setDate(date);
            }
        },

        /**
         * @method
         * @member Date
         *
         * <p>
         * A method is used to set time values on a date object with correct
         * Daylight Savings Time.
         *
         * <p>
         * This is a fix for the default Date.prototype.setFullYear which will
         * incorrectly set the Time Zone when dealing with the hour between 1:00
         * am and 1:59 am on a Daylight Savings Day.
         *
         * This method will set the full year of the date object to the given value.
         * @param {Number} value The new year value to set the date to.
         */
        setFullYear: function(year, month, date) {
            var yearsToAdd = year - this.getFullYear();
            this.setMonth(yearsToAdd * 12 + this.getMonth());

            if (!Ext.isEmpty(month) || !Ext.isEmpty(date)) {
                this.setMonth(month, date);
            }
        }
    });
})();
//////////////////////
// ..\js\overrides\JSON.js
//////////////////////
Ext.JSON.extDecode = Ext.JSON.decode;

/**
 * This override provides a default operation for all decodes to ensure
 * safe parsing is carried if not otherwise specified.  This will eliminate
 * errors that are seen when decoding HTML responses when a session has terminated
 * in a way that is uncaught to the average observer.  
 */
Ext.JSON.decode =  function() {
    if(arguments.length === 2) {
        return Ext.JSON.extDecode.apply(Ext.JSON, arguments);
    }
    else {
        return Ext.JSON.extDecode.call(Ext.JSON, arguments[0], true);
    }
};
Ext.decode = Ext.JSON.decode;
//////////////////////
// ..\js\overrides\core\History.js
//////////////////////
// Overrides the add method to fix a quirky Internet Explorer bug.
(function() {
    var oldAdd = Ext.util.History.add;
    
    Ext.apply(Ext.util.History, {
        add: function(token, preventDup){
            oldAdd.apply(this, arguments);
            if (this.oldIEMode) {
                window.top.location.hash = token;
            }
        },
        
        cleanHashMark: function(token){
            var i = token.indexOf("#");
            return i >= 0 ? token.substr(i + 1) : token;
        },
        
        updateIFrame: function(token){
            var html = '<html><body><div id="state">' +
            Ext.util.Format.htmlEncode(this.cleanHashMark(token)) +
            '</div></body></html>';
            
            try {
                var doc = this.iframe.contentWindow.document;
                doc.open();
                doc.write(html);
                doc.close();
                return true;
            } 
            catch (e) {
                return false;
            }
        }
    });
})();
//////////////////////
// ..\js\overrides\ux\Types.js
//////////////////////
(function() {
    // define once
    var stripRe = /[\$,%]/g;
    var msdateRe = /\/Date\((-?\d+)\)\//; // MS JSON date format
    var convertStringToDate = function(v) {
        if (!v) {
            return '';
        }
        if (v instanceof Date) {
            return v;
        }
        
        // Parse ISO-8601 date, assuming rpcommon is loaded...
        if ((/T(\d{2}):(\d{2})/).test(v)) {
            return new Date(v);
        }
        
        if (v.match) {
            var match = v.match(msdateRe);
            if (match !== null && match.length > 1) {
                // expects MS JSON dates of the form \/Date(1069689066000)\/
                return new Date(parseInt(v.replace(msdateRe, '$1'), 10));
            }
        }
        var dateFormat = this.dateFormat;
        if (dateFormat) {
            if (dateFormat === "timestamp") {
                return new Date(v * 1000);
            }
            if (dateFormat === "time") {
                return new Date(parseInt(v, 10));
            }
            return Date.parseTime(v, dateFormat);
        }
        var parsed = Ext.isNumeric(v) ? v : Date.parse(v);
        return parsed ? new Date(parsed) : null;
    };
    
    Ext.data.Types.DATE = {
        convert: function(v) {
            return convertStringToDate(v);
        },
        sortType: Ext.data.SortTypes.asDate,
        type: "date"
    };
    
    Ext.data.Types.TIME = {
        convert: function(v) {
            var dt = convertStringToDate(v);
            
            if (!dt || dt === '') {
                return '';
            }
            return dt.formatTime();
        },
        sortType: Ext.data.SortTypes.asDate,
        type: "time"
    };
    
    Ext.data.Types.DATESTRING = {
        convert: function(v) {
            var dt = convertStringToDate(v);
            if (!dt || dt === '') {
                return '';
            }
            return dt.formatDate(RP.core.Formats.Date.FullDateTime);
        },
        sortType: Ext.data.SortTypes.asDate,
        type: "datestring"
    };
})();
//////////////////////
// ..\js\overrides\data\Types.js
//////////////////////
/**
 * @ignore
 */
Ext.define('Ext.data.overrides.Types', {
    override: 'Ext.data.Types',
    singleton: true
}, function() {
    var st = Ext.data.SortTypes;

    Ext.apply(Ext.data.Types, {
        /**
         * @property {Object} AUTO
         * This data type means that no conversion is applied to the raw data before it is placed into a Record.
         */
        AUTO: {
            // OVERRIDE - Added missing convert function
            // TODO: Should be fixed in 4.1.1. Re-evaluate then.
            convert: function(v) {
                return v;
            },
            // END OVERRIDE
            sortType: st.none,
            type: 'auto'
        }
    });
});
//////////////////////
// ..\js\overrides\data\reader\Json.js
//////////////////////
Ext.override(Ext.data.reader.Json, {
    getResponseData: function() {
        try {
            return this.callOverridden(arguments);
        }
        catch(error) {
            // don't log an error to the console, but at least log something
            logger.logError(error.message);
        }
    }
});
//////////////////////
// ..\js\overrides\data\reader\Reader.js
//////////////////////
/**
 * This is to fix an issue with sencha's approach to trying to put the nullResultSet on the
 * Reader object. We have brought up this issue to them and the forum post can be found here:
 * http://www.sencha.com/forum/showthread.php?158151-nullResultSet-is-not-defined-on-the-Ext.data.reader.Reader-s-prototype
 * 
 * This will resolve an uncaught error that is thrown when the data response on an Ajax
 * request results in a null object.  Ext then attempts to return the nullResultSet (as defined below)
 * back to the calling method.  However, it doesn't exist as initially thought based on their 
 * use of the callback method on Ext.define.
 */
Ext.apply(Ext.data.reader.Reader.prototype, {
    nullResultSet: Ext.create('Ext.data.ResultSet', {
        total  : 0,
        count  : 0,
        records: [],
        success: true
    })
});
//////////////////////
// ..\js\overrides\..\..\..\stashLibEndLoad.js
//////////////////////
RP.stash.api.endLoadLib();
