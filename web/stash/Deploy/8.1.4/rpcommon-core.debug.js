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
// ..\js\Button.js
//////////////////////
/**
 * @ignore
 *
 * Override to put the default stylings on buttons
 */
Ext.define('Ext.button.overrides.Button', {
    override: 'Ext.button.Button',

    initComponent: function(c) {
        this.callParent(arguments);
        
        /**
         * This override is to fix button style issues on a tablet.  The buttons were
         * left in a focused state.
         * The problem was that the mouse over method was called when the button is clicked
         * but the mouse out was not called.  This makes it soe the mouse events
         * don't do anything.
         */
        if(Ext.is.Tablet || Ext.is.Android) {
            this.handleMouseEvents = false;
        }
    }
});
//////////////////////
// ..\js\Date.js
//////////////////////
/**
 * @singleton
 * A singleton class which provides additional methods for handling
 * {@link Date} objects.
 */
RP.Date = {

    /**
     * Creates a copy of the passed in date, with the time reset to 12:00 AM
     * (00:00), which is the start of the day.
     * 
     * @param {Date} date The date to clear the time of.
     * @return {Date} A new date with the same date as the one passed in, with the time cleared.
     */
    clearTime: function(date) {
        if (date.toString() === "Invalid Date") {
            return;
        }

        var cloneDate = this.clone(date);

        // get current date before clearing time
        var d = cloneDate.getDate();

        // clear time
        cloneDate.setHours(0);
        cloneDate.setMinutes(0);
        cloneDate.setSeconds(0);
        cloneDate.setMilliseconds(0);

        if (cloneDate.getDate() != d) { // account for DST (i.e. day of month changed when setting hour = 0)
            // note: DST adjustments are assumed to occur in multiples of 1 hour (this is almost always the ca
            // refer to http://www.timeanddate.com/time/aboutdst.html for the (rare) exceptions to this rule
            // increment hour until cloned date == current date
            // http://www.sencha.com/forum/showthread.php?62250-CLOSED-35-3.x-2.x-DatePicker-Picks-wrong-date/page6&highlight=safeParse
            // for this DST adjust to work for places that change time at midnight, the Date's time needs to be set (to noon or something)
            for (var hr = 1, c = Ext.Date.add(cloneDate, Ext.Date.HOUR, hr); c.getDate() != d; hr++, c = Ext.Date.add(cloneDate, Ext.Date.HOUR, hr)) {
            }
            cloneDate.setDate(d);
            cloneDate.setHours(c.getHours());
        }

        return cloneDate;
    },
    
    /**
     * Compares the first date to the second date, returning a number
     * indicating the difference between the two.
     * 
     * @param {Date} date1 First Date object to compare.
     * @param {Date} date2 Second Date object to compare.
     * @return {Number}  
     *      -1 if date1 < date2. 
     *       0 if the dates are equivalent
     *       1 if date1 > date2.
     */
    compare: function(date1, date2) {
        if (isNaN(date1) || isNaN(date2)) {
            RP.throwError(date1 + " - " + date2);
        }
        else if ((date1 instanceof Date) && (date2 instanceof Date)) {
            return (date1 < date2) ? -1 : (date1 > date2) ? 1 : 0;
        }
        else {
            throw new TypeError(date1 + " - " + date2);
        }
    },
    
    /**
     * Returns a new Date object that is an exact date and time copy of the
     * original instance.
     * 
     * @param {Date} date Date that you want to clone.
     * @return {Date} A new Date instance
     */
    clone: function(date) {
        return new Date(date.getTime());
    },
    
    /**
     * Decides if two date objects are equivalent by calling {@link #compare}.
     * 
     * @param {Date} date1 First Date object to compare.
     * @param {Date} date2 (optional) Second date object to compare. If null,
     * new Date() [now] is used.
     * @return {Boolean} True if dates are equal.
     */
    equals: function(date1, date2) {
        return this.compare(date1, date2 || new Date()) === 0;
    },
    
    /**
     * Determines if the given time is between a range of two dates or equal
     * to either the start or end dates.
     * 
     * @param {Date} date The date object containing the time in question.
     * @param {Date} start Start of range.
     * @param {Date} end End of range.
     * @return {Boolean} True if time is inside the range
     */
    between: function(date, start, end) {
        return date.getTime() >= start.getTime() && date.getTime() <= end.getTime();
    },
    
    /**
     * Decides if one date follows another by calling {@link #compare}.
     * 
     * @param {Date} date1 The date to check.
     * @param {Date} date2 (optional) Date object to compare to. If no date is
     * supplied, new Date() [now] is used.
     * @return {Boolean} True if date1 occurs after date2
     */
    isAfter: function(date1, date2) {
        return this.compare(date1, date2 || new Date()) === 1;
    },
    
    /**
     * Decides if one date is the same as or follows another by calling
     * {@link #compare}.
     * 
     * @param {Date} date1 The date to check.
     * @param {Date} date2 (optional) Date object to compare to. If no date is
     * supplied, new Date() [now] is used.
     * @return {Boolean} True if date1 is equivalent to or occurs after date2
     */
    isOnOrAfter: function(date1, date2) {
        return this.compare(date1, date2 || new Date()) >= 0;
    },
    
    /**
     * Decides if one date precedes another by calling {@link #compare}
     * 
     * @param {Date} date1 The date to check.
     * @param {Date} date2 (optional) Date object to compare to. If no date is
     * supplied, new Date() [now] is used.
     * @return {Boolean} True if date1 occurs before date2
     */
    isBefore: function(date1, date2) {
        return this.compare(date1, date2 || new Date()) === -1;
    },
    
    /**
     * Decides if one date precedes or is equivalent to another by calling
     * {@link #compare}.
     * 
     * @param {Date} date1 The date to check.
     * @param {Date} date2 (optional) Date object to compare to. If no date is
     * supplied, new Date() [now] is used.
     * @return {Boolean} True if date1 is equivalent to or occurs before date2
     */
    isOnOrBefore: function(date1, date2) {
        return this.compare(date1, date2 || new Date()) <= 0;
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of
     * milliseconds added or subtracted.
     * 
     * @param {Date} date The date to add milliseconds to.
     * @param {Number} value The number of milliseconds to add (or subtract).
     * @return {Date} A copy of the date with the milliseconds added to it.
     */
    addMilliseconds: function(date, value) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        cloneDate.setTime(cloneDate.getTime() + value);
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of
     * seconds added or subtracted by calling {@link #addMilliseconds}.
     * 
     * @param {Date} date The date to add seconds to.
     * @param {Number} value The number of seconds to add (or subtract).
     * @return {Date} A copy of the date with the seconds added to it.
     */
    addSeconds: function(date, value) {
        return this.addMilliseconds(date, value * 1000);
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of
     * minutes added or subtracted by calling {@link #addMilliseconds}.
     * 
     * @param {Date} date The date to add minutes to.
     * @param {Number} value The number of minutes to add (or subtract).
     * @return {Date} A copy of the date with the minutes added to it.
     */
    addMinutes: function(date, value) {
        return this.addMilliseconds(date, value * 60000); /* 60*1000 */
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of hours
     * added or subtracted by calling {@link #addMilliseconds}.
     * 
     * @param {Date} date The date to add hours to.
     * @param {Number} value The number of hours to add (or subtract).
     * @return {Date} A copy of the date with the hours added to it.
     */
    addHours: function(date, value) {
        return this.addMilliseconds(date, value * 3600000); /* 60*60*1000 */
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of days
     * added or subtracted by calling {@link #addMilliseconds}.
     * 
     * @param {Date} date The date to add days to.
     * @param {Number} value The number of days to add (or subtract).
     * @return {Date} A copy of the date with the days added to it.
     */
    addDays: function(date, value) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        var expectedHours = cloneDate.getHours(), expectedMinutes = cloneDate.getMinutes();
        cloneDate.setTime(cloneDate.getTime() + value * 86400000); /* 24*60*60*1000 */
        this.adjustForDST(cloneDate, expectedHours, expectedMinutes);
        return cloneDate;
    },

    /**
     * Creates a copy of the passed in date, and adjusts it by either adding 1
     * hour or 30 minutes so that hour or minutes will be the same numerically.
     * 
     * @param {Date} date The date to adjust for DST.
     * @param {Object} expectedHours the hour that is expected
     * @param {Object} expectedMinutes the minutes that is expected
     * @return {Date} A copy of the date adjusted for DST.
     */
    adjustForDST: function(date, expectedHours, expectedMinutes) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        var currentMinutes = cloneDate.getMinutes(), currentHours = cloneDate.getHours();
        
        if (currentHours == expectedHours && currentMinutes == expectedMinutes) { //if same, no DST
            return cloneDate;
        }
        
        var vectorHours = currentHours - expectedHours, //1-2   normal dst
 deltaHours = Math.abs(vectorHours), deltaMinutes = Math.abs(currentMinutes - expectedMinutes); //30-00
        if (Math.abs((currentHours + 24) - expectedHours) < deltaHours) { //0-23
            vectorHours = (currentHours + 24) - expectedHours;
            deltaHours = Math.abs(vectorHours);
        }
        if (Math.abs(currentHours - (expectedHours + 24)) < deltaHours) { //23-0
            vectorHours = currentHours - (expectedHours + 24);
            deltaHours = Math.abs(vectorHours);
        }
        
        if (deltaMinutes > 0) {
            cloneDate = this.addMinutes(cloneDate, -deltaMinutes * (vectorHours / Math.abs(vectorHours)));
        }
        else {
            cloneDate = this.addHours(cloneDate, -vectorHours);
        }
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of weeks
     * added or subtracted by calling {@link #addDays}.
     * 
     * @param {Date} date The date to add weeks to.
     * @param {Number} value The number of weeks to add (or subtract).
     * @return {Date} A copy of the date with the weeks added to it.
     */
    addWeeks: function(date, value) {
        return this.addDays(date, value * 7);
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of
     * months added or subtracted.
     * 
     * @param {Date} date The date to add months to.
     * @param {Number} value The number of months to add (or subtract).
     * @return {Date} A copy of the date with the months added to it.
     */
    addMonths: function(date, value) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        var n = cloneDate.getDate();
        cloneDate.setDate(1);
        cloneDate.setMonth(cloneDate.getMonth() + value);
        cloneDate.setDate(Math.min(n, this.getDaysInMonth(cloneDate.getFullYear(), cloneDate.getMonth())));
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, with the specified number of years
     * added or subtracted by calling {@link #addMonths}.
     * 
     * @param {Date} date The date to add years to.
     * @param {Number} value The number of years to add (or subtract).
     * @return {Date} A copy of the date with the years added to it.
     */
    addYears: function(date, value) {
        return this.addMonths(date, value * 12);
    },
    
    /**
     * Decodes a date string (that uses dashes) such as 5-18-2008 to a date
     * object.
     * 
     * NOTE: Will not handle times.
     * 
     * @param {String} date as a string (mm-dd-yyyy)
     * @return {Date/Object} new date
     */
    decodeDateInURL: function(val) {
        if (!val) {
            return null;
        }
        
        if (typeof(val) === "string") {
            var parts = val.split("-");
            return new Date(parseInt(parts[2], 10), parseInt(parts[0], 10) - 1, parseInt(parts[1], 10));
        }
        else {
            return val;
        }
    },
    
    /**
     * Encodes a {@link Date} object to a string (that uses dashes).
     * 
     * @param {Date} date Date object to encode
     * @return {String} date formatted as (mm-dd-yyyy)
     */
    encodeDateToURL: function(date) {
        return Ext.String.format("{0}-{1}-{2}", date.getMonth() + 1, date.getDate(), date.getFullYear());
    },
    
    /**
     * Determines if the passed in year is a leap year.
     * 
     * @param {Number} year The year.
     * @return {Boolean} True if the year is a leap year.
     */
    isLeapYear: function(year) {
        return ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0);
    },
    
    /**
     * Gets the number of days in the month, given a year and month value.
     * Automatically corrects for LeapYear.
     * 
     * @param {Number} year The year.
     * @param {Number} month The month (0-11).
     * @return {Number} The number of days in the month.
     */
    getDaysInMonth: function(year, month) {
        return [31, (this.isLeapYear(year) ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
    },
    
    /**
     * Formats a {@link Date} object by either calling
     * {@link RP.core.FormatEngine#formatDate} or {@link Ext.Date#format}.
     * 
     * @param {Date} date The date that you want to format.
     * @param {String} format (optional) If the format is available, this will
     * use {@link Ext.Date#format}. Else,
     * {@link RP.core.FormatEngine#formatDate} will be used.
     * @return {String} Formatted version of the date
     */
    formatDate: function(date, format) {
        if (format && !RP.Formatting.Dates.isValidFormat(format))
        {
            return Ext.Date.format.apply(Ext.Date, arguments);
        }
        return RP.core.Format.formatDate(date, format);
    },
    
    /**
     * Wrapper for calling {@link #formatDate}. A format object is created
     * using a second argument which is passed in.
     * 
     * @param {Date} date Date object to format
     * @return {String} Formatted version of the date
     */
    dateFormat: function(date) {
        return this.formatDate.apply(date, [date, arguments[1], true]);
    },

    /**
     * A wrapper to call {@link RP.core.FormatEngine#formatTime}.
     * 
     * @param {Date} date The time that you want to format.
     * @param {Object} formatObj The format of the time.
     * @return {String} A formatted version of the time
     */
    formatTime: function(date, formatObj) {
        return RP.core.Format.formatTime(date, formatObj);
    },
    
    /**
     * @method extParseDate
     * A wrapper for {@link Ext.Date#parse}.
     */
    extParseDate: Ext.Date.parse,
    
    /**
     * Attempts to parse a string into a date object, returning null if the
     * parse failed.
     * 
     * @param {String} date String representation of the date
     * @param {Object/String} format An RP.core.Formats.Date format or an Ext format string.
     */
    parseDate: function(date, format) {
        if (arguments.length > 1 && (format.length > 1 || format.length === 1 && date.length < 3)) {
                return Ext.Date.parse.apply(Date, arguments);
        }
        return RP.core.Format.parseDateTime(date, format);
    },
    
    /**
     * A wrapper for {@link RP.core.FormatEngine#parseTime}
     * 
     * @param {Object} time String representation of the date
     * @param {Object} format (optional) Format object for the date
     * @return {Date} Date object
     */
    parseTime: function(time, format) {
        return RP.core.Format.parseTime(time, format);
    },
    
    /**
     * Returns the difference in time between date1 and date2.
     * 
     * @param {Date} date1 The date to subtract from.
     * @param {Date} date2 The date to subtract.
     * @returns {RP.TimeSpan} the time span between dates
     */
    deltaT: function(date1, date2) {
        return new RP.TimeSpan(date1.getTime() - date2.getTime());
    },
    
    /**
     * Creates a copy of the passed in date, which is rounded to a specified 
     * number of minutes.
     * 
     * @param {Date} date The Date whose minutes will be rounded.
     * @param {Number} roundToMinutes (optional) The number of minutes to round to.
     * Defaults to 15.
     * @return {Date} A copy of the date with the minutes rounded.
     */
    round: function(date, roundToMinutes) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        // Default to 15 minute rounding if not specified.
        if (roundToMinutes === undefined) {
            roundToMinutes = 15;
        }
        
        var m = cloneDate.getMinutes() % roundToMinutes;
        
        if (m > 0) {
            var halfRound = parseInt(roundToMinutes / 2, 10);
            if (m <= halfRound) {
                cloneDate = this.addMinutes(cloneDate, -1 * m);
            }
            else {
                cloneDate = this.addMinutes(cloneDate, roundToMinutes - m);
            }
        }
        
        cloneDate.setSeconds(0);
        cloneDate.setMilliseconds(0);
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, with Banker's rounding: the minute
     * is rounded to the nearest even number.
     * 
     * @param {Date} date The Date whose minutes will be rounded.
     * @return A copy of the date rounded to nearest minute.
     */
    roundToNearestMinute: function(date) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        if (cloneDate.getSeconds() >= 30) {
            cloneDate = this.addMinutes(cloneDate, 1);
        }
        
        cloneDate.setSeconds(0);
        cloneDate.setMilliseconds(0);
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, which is rounded to the nearest second.
     * 
     * @param {Date} date The Date whose seconds will be rounded.
     * @param {Number} secondsInterval (optional) The number of seconds to round to.
     * @return {Date} A copy of the date with the seconds rounded.
     */
    roundToNearestSeconds: function(date, secondsInterval) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        if (secondsInterval === undefined) {
            secondsInterval = 15;
        }
        
        var seconds = cloneDate.getSeconds() % secondsInterval;
        
        if (seconds > 0) {
            var halfRound = parseInt(secondsInterval / 2, 10);
            if (seconds <= halfRound) {
                cloneDate = this.addSeconds(cloneDate, -1 * seconds);
            }
            else {
                cloneDate = this.addSeconds(cloneDate, secondsInterval - seconds);
            }
        }
        
        cloneDate.setMilliseconds(0);
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, which has had the minutes, hours,
     * and seconds rolled back to 0.
     * 
     * @param {Date} date The Date whose hours will be rounded.
     * @return {Date} A copy of the date with evenly rounded hours.
     */
    roundBackToHour: function(date) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        cloneDate.setMinutes(0);
        cloneDate.setSeconds(0);
        cloneDate.setMilliseconds(0);
        
        return cloneDate;
    },
    
    /**
     * Creates a copy of the passed in date, and applies the time values of
     * another date object to it.
     * 
     * @param {Date} date The date object containing the date values.
     * @param {Date} time The date object containing the time values.
     * @return {Date} A copy of the date with the original calendar date but
     * with the time parameter's time component.
     */
    addTime: function(date, time) {
        // cloned the passed in date
        var cloneDate = this.clone(date);
        
        cloneDate.setHours(time.getHours());
        cloneDate.setMinutes(time.getMinutes());
        cloneDate.setSeconds(time.getSeconds());
        cloneDate.setMilliseconds(time.getMilliseconds());
        return cloneDate;
    },
    
    /**
     * Formats the given date in ISO-8601 with no timezone on it for use with
     * non-translation dates. 
     * 
     * @param {Date} date Date to format.
     * @return {String} ISO-8601 string of the date.
     */
    getISO8601StringNoTranslation: function(date) {
        return RP.core.Format.formatDate(date, RP.core.Format.ISO8601NoOffset);
    },
    
    /**
     * Formats the given date in ISO-8601 translated into the user's preferred
     * timezone.
     * @param {Date} date Date to format.
     * @return {String} ISO-8601 string of the date in the user's timezone.
     */
    getISO8601StringInUserTimeZone: function(date) {
        var userTimeZoneOffset = Ext.Date.isDST(date) ? RP.globals.getValue("USER_TIME_ZONE").daylightTimeOffset : RP.globals.getValue("USER_TIME_ZONE").standardTimeOffset;
        var minDecimal = userTimeZoneOffset % 1;
        var hours = userTimeZoneOffset - minDecimal;
        
        var min = Math.round(minDecimal * 60);
        var sign = userTimeZoneOffset >= 0 ? "+" : "-";
        
        hours = Math.abs(hours);
        
        hours = (hours.toString().length === 1 ? "0" : "") + hours;
        min = (min.toString().length === 1 ? "0" : "") + min;
        var sFormatVal = sign + hours + ":" + min;
        
        
        return RP.core.Format.formatDate(date, RP.core.Format.ISO8601NoOffset) + sFormatVal;
    },
    
    /**
     * Translates an Ext date format to something more human readable.
     * @param {String} format Ext date formatted string
     * @return {String} Formatted version
     */
    translateFormat : function(format) {
        return format.replace(/d/, "dd")
              .replace(/D/, "DDD")
              .replace(/j/, "d")
              .replace(/m/, "mm")
              .replace(/M/, "MMM")
              .replace(/n/, "m")
              .replace(/y/, "yy")
              .replace(/Y/, "yyyy")
              .replace(/a/, "am/pm")
              .replace(/A/, "AM/PM")
              .replace(/h/, "hh")
              .replace(/H/, "HH")
              .replace(/g/, "h")
              .replace(/G/, "H")
              .replace(/i/, "mm")
              .replace(/s/, "ss")
              .replace(/u/g, "s");
    }
};
//////////////////////
// ..\js\Table.js
//////////////////////
Ext.define('Ext.view.overrides.Table', {
    override: 'Ext.view.Table',

    initComponent: function() {
        this.callParent(arguments);

        if(Ext.is.Tablet || Ext.is.Android) {
            this.trackOver = false;
        }
    }
});
//////////////////////
// ..\js\util\DeprecationUtils.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.DeprecationUtils
 *
 * This class of methods assists with deprecated functionality that
 * is used in existing application code.
 */
RP.util.DeprecationUtils = function() {

    // This is used to keep track of whether we've notified the user of deprecation for a 
    // specific class.method. It will notify only once per call to a unique class.method.
    var deprecationMessageTracker = {};

    /**
     * Converts the namespace into the space from the window
     * object by correctly navigating the object hierarchy.
     * @param {String} namespace The namespace to resolve on the window object.
     */
    function convertNameSpaceToWindowNameSpace(namespace) {
        var contexts = namespace.split(".");
        var baseContext = window;
        
        for (var i = 0; i < contexts.length; i++) {
            if (!Ext.isDefined(baseContext)) {
                return undefined;
            }
            baseContext = baseContext[contexts[i]];
        }
        
        return baseContext;
    }

    /**
     * Replaces the property of one object with the property of another.
     * 
     * @param {Object} oldObject Object to have something added to
     * @param {String} oldName Old name prefix of the property
     * @param {Object} newObject Object to pull the property from
     * @param {String} deprecationMessage Message to display if this property is deprecated
     * @param {Object[]} arguments (optional) Arguments which can be added to the new object
     * @private
     */
    function replaceProperty(oldObject, oldName, newObject, property, deprecationMessage) {
        if (Ext.isFunction(newObject[property])) {
            deprecationMessageTracker[oldName + "." + property] = false;
            oldObject[property] = function() {
                if (deprecationMessageTracker[oldName + "." + property] === false) {
                    RP.util.DeprecationUtils.logStackTrace(deprecationMessage);
                    deprecationMessageTracker[oldName + "." + property] = true;
                }
                
                return newObject[property].apply(newObject, arguments);
            };
        }
        else {
            oldObject[property] = newObject[property];
        }
    }

    return {
        /**
         * This renameClass will take functionality from the new class and wrap
         * the methods with a new function that will indicate the deprecation
         * of the old class and identify the new class.<br /><br />The two 
         * parameters should be fully qualified from the window object.
         * 
         * @param {String} oldName The old class name reference.
         * @param {String} newName The new name of the class.
         */
        renameClass: function(oldName, newName) {
            var newClass = convertNameSpaceToWindowNameSpace(newName);
            var oldClass = convertNameSpaceToWindowNameSpace(oldName);
            var deprecationMessage = Ext.String.format("[DEPRECATED] The use of the {0} shorthand is deprecated.  Please use {1}.", oldName, newName);
            
            if (!Ext.isDefined(oldClass)) {
                oldClass = Ext.define(oldName, {});
            }
            // check to see if it is an instantiable class
            if (newClass.prototype) {
                // copy prototype methods
                for (var property in newClass.prototype) {
                    replaceProperty(oldClass.prototype, oldName, newClass.prototype, property, deprecationMessage);
                }
                
                // copy static methods
                for (var prop in newClass) {
                    if (!Ext.isDefined(newClass.prototype[prop])) {
                        replaceProperty(oldClass, oldName, newClass, prop, deprecationMessage);
                    }
                }
            }
            else {
                Ext.iterate(newClass, function(property) {
                    replaceProperty(oldClass, oldName, newClass, property, deprecationMessage); // check prototype first
                });
            }
        },

        /**
         * Use this method to create warnings and stack trace in regards
         * to a deprecated configuration property.
         *
         * @param {String} className The class name
         * @param {String} oldName The config option name
         * @param {String} optionalMessage (Optional) Message to be appended to
         * the deprecation message
         */
        deprecatedConfigOption: function(className, oldName, optionalMessage) {
            if (!Ext.isDefined(optionalMessage)) {
                optionalMessage = "";
            }
            var deprecationMessage = Ext.String.format("The configuration option, {0}, is no longer supported by the {1} class. {2}", oldName, className, optionalMessage);
            RP.util.DeprecationUtils.logStackTrace(deprecationMessage);
        },

        /**
         * Use this method to create warnings and stack trace in regards
         * to a deprecated instance method.<br /><br />The
         * newClass.prototype[newMethodName].apply will fail if the new method
         * uses a property available to the newClass that is not available to
         * the oldClass since *this* points to an instantiated oldClass
         *
         * @param {String} oldClassName The old class name.
         * @param {String} newClassName The new class name.
         * @param {String} oldMethodName The old method name.
         * @param {String} newMethodName The new method name.
         * @param {String} optionalMessage An optional message with more information.
         */
        deprecatedInstanceMethod: function(oldClassName, newClassName, oldMethodName, newMethodName, optionalMessage) {
            var oldClass = convertNameSpaceToWindowNameSpace(oldClassName);
            var newClass = convertNameSpaceToWindowNameSpace(newClassName);
            if (!Ext.isDefined(optionalMessage)) {
                optionalMessage = "";
            }
            var deprecatedMsg = Ext.String.format("[DEPRECATED] The use of {0}.{1} is deprecated.  Please use {2}.{3} {4}.", oldClassName, oldMethodName, newClassName, newMethodName, optionalMessage);
            
            deprecationMessageTracker[oldClassName + "." + oldMethodName] = false;
            oldClass.prototype[oldMethodName] = function() {
                if (deprecationMessageTracker[oldClassName + "." + oldMethodName] === false) {
                    RP.util.DeprecationUtils.logStackTrace(deprecatedMsg);
                    deprecationMessageTracker[oldClassName + "." + oldMethodName] = true;
                }
                
                return newClass.prototype[newMethodName].apply(this, arguments);
            };
        },

        /**
         * This function is used when you are deprecating one static method
         * from one class to another static method on a different class.
         * 
         * @param {Object} oldClassName The old class fully qualified name.
         * @param {Object} newClassName The new class fully qualified name.
         * @param {Object} oldMethodName The old static method name.
         * @param {Object} newMethodName The new static method name.
         * @param {Object} optionalMessage And optional message to be displayed.
         */
        deprecatedStaticMethodToStaticMethod: function(oldClassName, newClassName, oldMethodName, newMethodName, optionalMessage) {
            var oldClass = convertNameSpaceToWindowNameSpace(oldClassName);
            var newClass = convertNameSpaceToWindowNameSpace(newClassName);
            if (!Ext.isDefined(optionalMessage)) {
                optionalMessage = "";
            }
            var deprecatedMsg = Ext.String.format("[DEPRECATED] The use of {0}.{1} is deprecated.  Please use {2}.{3} {4}.", oldClassName, oldMethodName, newClassName, newMethodName, optionalMessage);
            
            deprecationMessageTracker[oldClassName + "." + oldMethodName] = false;
            oldClass[oldMethodName] = function() {
                if (deprecationMessageTracker[oldClassName + "." + oldMethodName] === false) {
                    RP.util.DeprecationUtils.logStackTrace(deprecatedMsg);
                    deprecationMessageTracker[oldClassName + "." + oldMethodName] = true;
                }
                
                return newClass[newMethodName].apply(newClass, arguments);
            };
        },

        /**
         * This function is used when you are deprecating one class (instance)
         * method from one class to another <b>static</b> method on a different
         * class.<br /><br />This will assume that the new static method will
         * have the instance passed in as the first parameter to the static 
         * method.<br /><br />e.g.
         * <pre>Date.prototype.clearTime(clone) -> RP.Date.clearTime(date, clone)
         * </pre>
         *
         * @param {Object} oldClassName The old class fully qualified name.
         * @param {Object} newClassName The new class fully qualified name.
         * @param {Object} oldMethodName The old static method name.
         * @param {Object} newMethodName The new static method name.
         * @param {Object} optionalMessage And optional message to be displayed.
         */
        deprecatedPrototypeMethodToStaticMethod: function(oldClassName, newClassName, oldMethodName, newMethodName, optionalMessage) {
            var oldClass = convertNameSpaceToWindowNameSpace(oldClassName);
            var newClass = convertNameSpaceToWindowNameSpace(newClassName);
            if (!Ext.isDefined(optionalMessage)) {
                optionalMessage = "";
            }
            
            var deprecatedMsg = Ext.String.format("[DEPRECATED] The use of {0}.prototype.{1} is deprecated.  Please use {2}.{3} {4}", oldClassName, oldMethodName, newClassName, newMethodName, optionalMessage);
            
            deprecationMessageTracker[oldClassName + "." + oldMethodName] = false;
            oldClass.prototype[oldMethodName] = function() {
                if (deprecationMessageTracker[oldClassName + "." + oldMethodName] === false) {
                    RP.util.DeprecationUtils.logStackTrace(deprecatedMsg);
                    deprecationMessageTracker[oldClassName + "." + oldMethodName] = true;
                }
                
                // Put the object as the first argument.
                var newArgs = Ext.Array.clone(arguments);
                newArgs.unshift(this);
                
                return newClass[newMethodName].apply(newClass, newArgs);
            };
        },

        /**
         * This function is used when you are deprecating a static method and
         * replacing it with another method that is a on the prototype of a
         * new class.<br /><br />This will assume that the old static method's
         * first parameter is what the new class will be. It will be used as 
         * the caller for the prototype method.<br /><br />e.g.
         * <pre>RP.util.Date.addMilliseconds(date, millis) -> date.addMilliseconds(millis)
         * </pre>
         *
         * @param {Object} oldClassName The old class fully qualified name.
         * @param {Object} newClassName The new class fully qualified name.
         * @param {Object} oldMethodName The old static method name.
         * @param {Object} newMethodName The new static method name.
         * @param {Object} optionalMessage And optional message to be displayed.
         */
        deprecatedStaticMethodToPrototypeMethod: function(oldClassName, newClassName, oldMethodName, newMethodName, optionalMessage) {
            var oldClass = convertNameSpaceToWindowNameSpace(oldClassName);
            var newClass = convertNameSpaceToWindowNameSpace(newClassName);
            if (!Ext.isDefined(optionalMessage)) {
                optionalMessage = "";
            }
            
            var deprecatedMsg = Ext.String.format("[DEPRECATED] The use of {0}.{1} is deprecated.  Please use {2}.prototype.{3} {4}", oldClassName, oldMethodName, newClassName, newMethodName, optionalMessage);
            
            deprecationMessageTracker[oldClassName + "." + oldMethodName] = false;
            oldClass[oldMethodName] = function() {
                if (deprecationMessageTracker[oldClassName + "." + oldMethodName] === false) {
                    RP.util.DeprecationUtils.logStackTrace(deprecatedMsg);
                    deprecationMessageTracker[oldClassName + "." + oldMethodName] = true;
                }
                
                // Put the object as the first argument.
                var newArgs = Ext.Array.clone(arguments);
                var object = newArgs.shift();
                
                return newClass.prototype[newMethodName].apply(object, newArgs);
            };
        },

        /**
         * Log a warning and stack trace to the appropriate consoles.
         * 
         * @param {String} msg The message to display in the warning.
         */
        logStackTrace: function(msg) {
            if (RP.globals.getValue("SERVER_TYPE") !== "production") {
                if (window.console && console.error) {
                    console.error(msg);
                }
                
                logger.logWarning(msg + "  See the browser console for detailed information.");
            }
        }
    };
}();
//////////////////////
// ..\js\core\FormatEngine.js
//////////////////////
Ext.ns("RP.core");

/**
 * An engine for formatting many parts of RPWeb
 * 
 * @class RP.core.FormatEngine
 * @singleton
 *
 * @param {Object} formats
 * @param {Object} constants
 */
RP.core.FormatEngine = function(formats, constants) {
    var FORMAT_ISO8601_WITH_OFFSET = "yyyy\u0027-\u0027MM\u0027-\u0027ddTHH:mm:sszzz";
    
    var FORMAT_ISO8601_WITHOUT_OFFSET = "S";
    
    //used to hold information changed per public function call
    var _FormatInfo = {};
    
    /**
     * Sets important formatting info
     *
     * @param {String} String which contains info as to which globalize format is used
     * @param {String} sFormatType Type of formatting. One of {"number", 
     * "currency", "date", "time", "string", "timespan"}
     * @param {String} sCategory Either "p" or "f" to denote parsing or
     * formatting
     * @param {String} sParseValue (Optional) Sets the parse string of the 
     * format info
     * @private
     */
    function _SetFormatInfo(formatObj, sFormatType)
    //set or reset the member variables
    //should be called first inside every public function
    {
        _FormatInfo = {};
        _FormatInfo.FormatString = Ext.String.trim(formatObj.formatstring);
    }

    function _IsValidInputToFormat(input) {
        var bValid = true;
        var re = /^\d+$/; //only number input should be formatted here
        if (!re.test(input)) {
            bValid = false;
        }
        return bValid;
    }

    /**
     * Formats properly formatted body text. 
     * 
     * @param {String} value Properly formatted body text
     * @return {String} Properly formatted string
     * @private
     */
    function _TransStringFormat(value) { //this function is called after body text is replace by <<__BodyTextX>>
        //need to fill '0' for digits
        var sFormatString = _FormatInfo.FormatString;
        var sOutput = "";
        var sChar = "";
        var j = value.length - 1;
        var bBodyText = false;
        //start from rightmost 0 to replace with a digit, as in Excel
        for (var i = sFormatString.length - 1; i >= 0; i--) {
            sChar = sFormatString.charAt(i);
            if (sChar === "0") //can't be <<__BodyText0>>
            {
                if (j < 0) {
                    break;
                } //jump out the loop if finish looping value
                sOutput = value.charAt(j) + sOutput;
                j--;
            }
            else //copy char
            {
                if (sChar === "'") //ignore single quote to encapsulate the body text
                {
                    if (!bBodyText) {
                        bBodyText = true;
                    }
                    else {
                        if (sFormatString.charAt(i - 1) === "'") {
                            sOutput = sChar + sOutput;
                        }
                        //check whether consecutive single quotes
                        if (sFormatString.charAt(i + 1) !== "'") {
                            bBodyText = false;
                        }
                    }
                }
                else //copy body text
                {
                    if (bBodyText) {
                        sOutput = sChar + sOutput;
                    }
                }
            }
        }
        return sOutput;
    }
    
    /**
     * @method formatDate
     * Formats a date.
     * 
     * @param {Number/String/Date} dtValue Date to format
     * @param {String} String (Optional) Format object to use
     * @return {String} Formatted version of the date
     */
    function _FormatDate(date, format) {
      if (!format) {
        format = RP.core.Formats.Date.Default;
      }
      return RP.Formatting.Dates.format(date, format);
    }

    /**
     * @method formatTime
     * Formats a time.
     * 
     * @param {Number/String/Date} dtValue Date to format
     * @param {String} String (Optional) Format object to use
     * @return {String} Formatted version of the time
     */
    function _FormatTime(time, format) {
      if (!format) {
        format = RP.core.Formats.Time.Default;
      }
      return RP.Formatting.Times.format(time, format);
    }

    
    /**
     * @method formatTimeSpan
     * Formats a timespan.
     * 
     * @param {RP.TimeSpan} Timespan to format
     * @param {String} String Format object to use. <ul>Must contain one of:
     * <li><b>formatstring</b>: {String} String to use to create a template</li>
     * <li><b>template</b>: {Ext.Template} Template to use for formatting</li></ul>
     * @return {String} an HTML fragment of the template. See {@link Ext.Template#apply}.
     */
    function _FormatTimeSpan(value, formatObj) {
        var s = value.totalSeconds();
        var m = Math.floor(s / 60) % 60;
        var h = Math.floor(s / 3600);
        
        var hh = Ext.String.leftPad(h, 2, "0");
        var mm = Ext.String.leftPad(m, 2, "0");
        var ss = Ext.String.leftPad(Math.round(s % 60), 2, "0");
        
        if (!formatObj.template) {
            formatObj.template = new Ext.Template(formatObj.formatstring);
            formatObj.template.compile();
        }
        
        return formatObj.template.apply({
            h: h,
            hh: hh,
            mm: mm,
            ss: ss
        });
    }
    
    /**
     * @method formatCurrency
     * Formats a currency.
     * 
     * @param {Number/String} cValue Currency to format
     * @param {String} String (Optional) Format object to use
     * @return {String} Formatted version of the string
     */
    function _FormatCurrency(number, format) {
      if (!format) {
        format = RP.core.Formats.Currency.Default;
      }
      return RP.Formatting.Currencies.format(number, format);
    }
    
    /**
     * @method formatNumber
     * Formats a number.
     * 
     * @param {Number/String} cValue Currency to format
     * @param {String} String (Optional) Format object to use
     * @return {String} Formatted version of the string
     */
    function _FormatNumber(number, format, precision) {
      if (!format) {
        format = RP.core.Formats.Number.Default;
      }
      if (format === RP.core.Formats.Number.CustomPrecision) {
        return RP.Formatting.Numbers.format(number, format, precision);
      }
      if (format.indexOf("p") > -1) {
        return RP.Formatting.Percentages.format(number, format, precision);
      }
      else if (format.indexOf("c") !== -1) {
        return RP.Formatting.Currencies.format(number, format);
      }
      return RP.Formatting.Numbers.format(number, format, precision);
    }
    
    /**
     * Formats a string. Directly calls _FormatBasedOnType
     * 
     * @param {String} cValue Currency to format
     * @param {String} String (Optional) Format object to use
     * @return {String} Formatted version of the string
     * @private
     */
    function _FormatString(value, formatObj) {
        if (!_IsValidInputToFormat(value)) {
            RP.throwError("Invalid input: " + value.toString() + " to format as: String");
        }
        if (!formatObj) {
            RP.throwError("Must specify format type object");
        }
        _SetFormatInfo(formatObj, "string");
        //as of Excel, only numbers will be formatted
        return _FormatBasedOnType("string", value, formatObj);
    }
    
    /**
     * @method parseDate
     * Parses a string into a date object.
     * 
     * @param {String} sValue String representation of the date
     * @param {String} String (Optional) Format object for the date
     * @return {Date} Date object
     */
    function _ParseDate(date, format) {
        var parsedDate = RP.Formatting.Dates.parse(date, format);
        if (parsedDate) {
          return RP.Date.clearTime(parsedDate);
        }
        return parsedDate;
    }
    
    /**
     * @method parseTime
     * Parses a string (in 24hr format) into a date object with appropriate time.
     * 
     * @param {String} sValue String representation of the date
     * @param {String} String (Optional) Format object for the date
     * @return {Date} Date object
     */

    function _ParseTime(time, format) {
        return RP.Formatting.Times.parse(time, format);
    }
    /**
     * @method parseDateTime
     * Used to parse a string into a date object with both date and time
     * preserved.
     * 
     * @param {String} String representation
     * @param {String} Format String to use
     * @return {Date} Date object with both date and time
     */
    function _ParseDateWithTime(datetime, format) {
        return RP.Formatting.Dates.parse(datetime, format);
    }
    
    /**
     * @method parseCurrency
     * Parses a currency from a string
     * 
     * @param {String} sValue String representation of the currency
     * @param {Object} String Format object to use
     * @return {Number} Formatted version of the string
     */
    function _ParseCurrency(number) {
        if (number === "") {
            return 0;
        }
        try {
            return RP.Formatting.Currencies.parse(number);
        }
        catch(e) {
            return NaN;
        }
    }
    
    /**
     * @method parseNumber
     * Parses a correctly formatted number from a string
     * 
     * @param {String} sValue String representation of the number
     * @param {String} String Format object to use
     * @return {Number} Correctly formatted number
     */
    function _ParseNumber(number, format) {
        return RP.Formatting.Numbers.parse(number, format);
    }
    
    /**
     * Parses a number from a string
     * @param {String} sValue String value to evaluate
     * @return {Number} Number which was represented by the string
     * @private
     */
    function _ParseString(sValue) {
        var sOutput = "";
        //gather the digits from input string 
        if (sValue) {
            var re = /\d+/;
            while (re.test(sValue)) {
                sOutput = sOutput + RegExp.lastMatch;
                sValue = RegExp.rightContext;
            }
        }
        return sOutput;
    }
    
    /**
     * This method is used to convert a given date into the user's preferred time zone.
     *
     * @param {Date} date The Date that you want to convert.
     * @return {Date} The converted date object
     * @private
     */
    function _ConvertDateToUserTimeZone(date) {
        // If the authentication connection hasn't implemented getUserPreferredTimeZone throw and erro and return the original date.
        if (Ext.isEmpty(RP.globals.getValue("USER_TIME_ZONE").standardTimeOffset) || Ext.isEmpty(RP.globals.getValue("USER_TIME_ZONE").daylightTimeOffset)) {
            throw new Error("The authentication connection does not have user preferred time zone implemented");
        }
        var userTimeZoneOffset = Ext.Date.isDST(date) ? RP.globals.getValue("USER_TIME_ZONE").daylightTimeOffset : RP.globals.getValue("USER_TIME_ZONE").standardTimeOffset;
        var timezoneDifference = (date.getTimezoneOffset() / 60) + userTimeZoneOffset;
        return RP.Date.addHours(date, timezoneDifference);
    }
    
    /**
     * @method formatDateInUserTimeZone
     * This method will format the given date to the Date format passed in.
     * 
     * @param {Date} date The date that should be converted.
     * @param {Object} format The RPFormat Date format the date should be formatted as.
     * @return {Date} Formatted date object
     */
    function _FormatDateInUserTimeZone(date, format) {
        return _FormatDate(_ConvertDateToUserTimeZone(date), format);
    }
    
    /**
     * @method formatTimeInUserTimeZone
     * This method will format the given date to the Time format passed in.
     * 
     * @param {Date} date The date that should be converted.
     * @param {Object} format The RPFormat Time format the date should be formatted as.
     * @return {Date} Formatted date object
     */
    function _FormatTimeInUserTimeZone(date, format) {
        return _FormatTime(_ConvertDateToUserTimeZone(date), format);
    }
    
    /**
     * This method will offset a date to the same timezone as the server
     * 
     * @param {Date} date Date to offset
     * @return {Date} Offset date
     * @private
     */
    function _ConvertDateToServerTimeZone(date) {
        if (Ext.isEmpty(date.isoDateNoOffset)) {
            throw new Error("This date can't be displayed with no translation, the date must be created as an ISO-8601 String.");
        }
        return new Date(date.isoDateNoOffset);
    }
    
    /**
     * @method formatDateNoTranslation
     * Formats a date without offsetting it.
     * 
     * @param {Date} date Date to format
     * @param {Object} format (Optional) Format object to use
     * @return {Date} Formatted date
     */
    function _FormatDateNoTranslation(date, format) {
        return _FormatDate(_ConvertDateToServerTimeZone(date), format);
    }
    
    /**
     * @method formatTimeNoTranslation
     * Formats a time without offsetting it.
     * 
     * @param {Date} date Time to format
     * @param {Object} format (Optional) Format object to use
     * @return {Date} Formatted time
     */
    function _FormatTimeNoTranslation(date, format) {
        return _FormatTime(_ConvertDateToServerTimeZone(date), format);
    }
    
    return {
        ISO8601: FORMAT_ISO8601_WITH_OFFSET,
        ISO8601NoOffset: FORMAT_ISO8601_WITHOUT_OFFSET,
        formatCurrency: _FormatCurrency,
        formatDate: _FormatDate,
        formatDateInUserTimeZone: _FormatDateInUserTimeZone,
        formatDateNoTranslation: _FormatDateNoTranslation,
        formatNumber: _FormatNumber,
        formatTime: _FormatTime,
        formatTimeInUserTimeZone: _FormatTimeInUserTimeZone,
        formatTimeNoTranslation: _FormatTimeNoTranslation,
        formatTimeSpan: _FormatTimeSpan,
        parseCurrency: _ParseCurrency,
        parseDate: _ParseDate,
        parseDateTime: _ParseDateWithTime,
        parseTime: _ParseTime,
        parseNumber: _ParseNumber
    };
};
//////////////////////
// ..\js\formats\RPFormats.js
//////////////////////
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: {  Long:                  "y",  //"MM/dd/yyyy hh:mm tt"
                             Default:               "J",  //"MM/dd/yyyy"
                             Medium:                "J",  //"MM/dd/yyyy"
                             TimeStamp:             "x",  //"MM/dd/yyyy hh:mm:ss tt" 
                             MediumWithHours:       "K",  //"MM/dd/yyyy h t",
                             FullDateOnly:          "D",  //"dddd, MMMM dd, yyyy"
                             FullDateTime:          "v",  //"dddd, MMMM dd, yyyy hh:mm tt"
                             POSDate:               "S",  //"yyyy'-'MM'-'dd'T'HH':'mm':'ss"
                             MediumDateExp:         "b",  //"MMM dd" 
                             WeekdayDate:           "w",  //"ddd, MM/dd"
                             Month:                 "MMM",
                             FullMonth:             "MMMM",
                             LongYear:              "yyyy",
                             ShortWeekday:          "ddd",
                             Weekday:               "dddd",
                             Short:                 "A",  //"M/d/yy"
                             ShortLongYear:         "d",  //"M/d/yyyy"
                             MonthYear:             "B",  //"MMM yyyy"
                             FullMonthYear:         "C",  //"MMMM yyyy"
                             MonthDate:             "E",  //"MM/dd"
                             FullMonthDate:         "M",  //"MMMM dd"
                             FullDateWithoutDayName:"G",  //"MMMM dd, yyyy"
                             MonthDateDay:          "I",  //"MM/dd ddd"
                             ShortDateTime:         "H",  //"MMM dd, hh:mm tt"
                             ShortWeekdayMedium:    "q",  //"ddd MM/dd/yyyy"
                             AbbrFull:              "a",  //"ddd, MMM dd, yyyy"
                             ISO2014:               "yyyy\u0027-\u0027MM\u0027-\u0027dd",
                             ISO8601:               "yyyy\u0027-\u0027MM\u0027-\u0027ddTHH:mm:sszzz",
                             AbbrFullDateTime:      "z",  //"ddd, MMM dd, yyyy hh:mm tt"
                             WeekDayTime:           "k"   //"ddd, MM/dd h:mm t"
                    },
                    Time: { Default:              "m",  //"hh:mm tt"
                            ShortAMPM:            "s",  //"h:mm t"
                            Long:                 "l",  //"hh:mm:ss tt"
                            Military:             "HH:mm",  //"HH:mm"
                            MilitaryWithSeconds:  "HH:mm:ss",
                            HourOnly:             "h",  //"h t"
                            MilitaryShort:        "H:mm",  //"H:mm"
                            Short:                "t",  //"h:mm tt"
                            TrimHoursLong:        "T"   //""h:mm:ss tt"
                    },
                    Number: { Default:               "n",  //2 significant digits
                              HighPrecision:         "n4",  //4 significant digits
                              MediumPrecision:       "n2",  //2 significant digits
                              MediumLowPrecision:    "n1",  //1 significant digits
                              LowPrecision:          "n0",  //no significant digits
                              ScheduleTotal:         "r",   //2 significant digits, no thousand separator - Only being maintained for backwards compatability
                              MediumPrecisionRaw:    "r",   //2 significant digits, no thousand separator
                              MinusSign:             "o",   //2 significant digits, minus sign instead of brackets
                              CustomPrecision:       "precision" //pass significant digits as third parameter
                    },
                    Currency: { Default:         "c",  //significant digits is defind by locale
                                HighPrecision:   "c",  //significant digits is defind by locale - Only being maintained for backwards compatability
                                MediumPrecision: "c",  //significant digits is defind by locale - Only being maintained for backwards compatability
                                LowPrecision:    "c0"  //no significant digits
                    },
                    Percent: { Default:            "p",   //2 significant digits
                               HighPrecision:      "p4",  //4 significant digits
                               MediumPrecision:    "p2",  //2 significant digits
                               MediumLowPrecision: "p1",  //1 significant digits
                               LowPrecision:       "p0",  //no significant digits
                               CustomPrecision:    "precision" //pass significant digits as third parameter (this can only be used using RP.Formatting.Percentages)
                    },
                    TimeSpan: {
                        Short: {
                            type: "timespan",
                            formatstring: "{h}:{mm}",
                            template: undefined    // compiled Ext template
                        },
                        Medium: {
                            type: "timespan",
                            formatstring: "{h}:{mm}:{ss}",
                            template: undefined    // compiled Ext template
                        },
                        Long: {
                            type: "timespan",
                            formatstring: "{hh}:{mm}:{ss}",
                            template: undefined    // compiled Ext template
                        } 
                    }
};

/**
 * @namespace RP.core
 * @class FormatConstants
 * @singleton
 */
RP.core.FormatConstants =  { Weekdays: RP.Formatting.Dates.getShortDayNames(),
                             FullWeekdays: RP.Formatting.Dates.getDayNames(),
                             Months: RP.Formatting.Dates.getShortMonthNames(),
                             FullMonths: RP.Formatting.Dates.getMonthNames()
};

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);
//////////////////////
// ..\js\util\logger.js
//////////////////////
Ext.ns("RP.util");

/**
 * A class which represents a log message. This is a model of a log message, 
 * containing a message, level, and class. The class for every log message is
 * determined by the level of that message. The levels of messages will be 
 * referred to as the Set of Message Levels. The Set of Message Levels is 
 * <pre> TRACE, DEBUG, INFO, WARNING, ERROR, FATAL, ALL </pre>
 */
//TODO Ext JS 4 more appropriate name for the model maybe?
Ext.define("RP.util.LogMessage", {
    extend: "Ext.data.Model",
    fields: [
        { name: 'message' },
        { name: 'level' },
        { name: 'cls' }
    ]
});

/**
 * A client-side logger class. Messages logged are of the form 
 * {@link RP.util.LogMessage}. The levels of messages will be 
 * referred to as the Set of Message Levels. The Set of Message Levels is 
 * <pre> TRACE, DEBUG, INFO, WARNING, ERROR, FATAL, ALL </pre>
 */
Ext.define("RP.util.logger", {
    
    mixins: {
        observable: "Ext.util.Observable"
    },
    
    _maxEntries: 1000,
    _logToServer: false,
    _clientLogLastPost: new Date(),
    _clientLogInterval: 5000,
    _clientLogsBuffer: [],
    _clientLogsCaptured: false,
    _levels: {
        TRACE: "TRACE",
        DEBUG: "DEBUG",
        INFO: "INFO",
        WARNING: "WARNING",
        ERROR: "ERROR",
        FATAL: "FATAL",
        ALL: "ALL"
    },
    _store: new Ext.data.ArrayStore({
        model: "RP.util.LogMessage"
    }),
    
    _currentFilter: undefined,

    constructor: function(config) {
        this.mixins.observable.constructor.call(this, config);
        this.addEvents("logtoservertoggled");
        this.callParent(arguments);
    },

    /**
     * Getter function for the internal {@link Ext.data.ArrayStore}, where the
     * messages are stored.
     * 
     * @return {Ext.data.ArrayStore} Store where the messages are stored
     */
    getStore: function() {
        return this._store;
    },

    /**
     * Logs a message.
     * 
     * @param {String} message Message to log
     * @param {String} (Optional) Level of the message, as defined by the
     * Set of Message Levels. If undefined, defaults to INFO
     */
    log: function(message, level) {
        if (level === undefined) {
            level = this._levels.INFO;
        }

        var dateLogged = new Date();
        var formattedMessage = Ext.String.format("<{0}::{1}.{2}> {3}", RP.Date.formatTime(dateLogged, RP.core.Formats.Time.Military), dateLogged.getSeconds(), dateLogged.getMilliseconds(), message);

        var newModel = new this._store.model({
            message: formattedMessage,
            level: level,
            cls: this.getLogClass(level)
        });

        var suspended = this._store.eventsSuspended;

        // If currently filtering, do not let the store add fire events -
        // wait for the filtering to do so...
        if (this._currentFilter && !suspended) {
            this._store.suspendEvents(false);
        }

        this._store.add(newModel);

        if (this._logToServer) {
            this._clientLogsBuffer.push(Ext.String.format("{0} {1}", level, formattedMessage));
        }

        if (this._store.getCount() > this._maxEntries) {
            this._store.removeAt(0);
        }

        if (this._currentFilter && !suspended) {
            this._store.resumeEvents();
            this.filterBy(this._currentFilter);
        }
    },

    /**
     * Gets the class for a message.
     * 
     * @param {String} level Level of the message, as defined in the 
     * Set of Message Levels.
     * @return {String} Class to be used for the message
     */
    getLogClass: function(level) {
        var cls;
        switch (level) {
            case this._levels.DEBUG:
                cls = "rp-log-d";
                break;
            case this._levels.INFO:
                cls = "rp-log-i";
                break;
            case this._levels.WARNING:
                cls = "rp-log-w";
                break;
            case this._levels.ERROR:
                cls = "rp-log-e";
                break;
            case this._levels.FATAL:
                cls = "rp-log-f";
                break;
            default:
                cls = "rp-log-t";
                break;
        }
        return cls;
    },

    /**
     * Logs a "TRACE" message. See {@link #log}.
     * @param {String} message Message to log
     */
    logTrace: function(message) {
        this.log(message, this._levels.TRACE);
    },

    /**
     * Logs a "DEBUG" message. See {@link #log}.
     * @param {String} message Message to log
     */
    logDebug: function(message) {
        this.log(message, this._levels.DEBUG);
    },

    /**
     * Logs an "INFO" message. See {@link #log}.
     */
    logInfo: function(message) {
        this.log(message, this._levels.INFO);
    },

    /**
     * Logs a "Warning" message. See {@link #log}.
     * @param {String} message Message to log
     */
    logWarning: function(message) {
        this.log(message, this._levels.WARNING);
    },

    /**
     * Logs an "ERROR" message. See {@link #log}.
     * @param {String} message Message to log
     */
    logError: function(e) {
        var type, body;

        if (e.name === "Ext.Error") {
            type = "Ext.Error";
            body = e.message;
        }
        else if (e.statusText) {
            type = "AJAX Exception";
            body = Ext.String.format("status={0}; statusText={1}", e.status, e.statusText);
        }
        else {
            type = "JS Error";
            body = e.toString();
        }

        this.log(Ext.String.format("[{0}]: {1}", type, body), this._levels.ERROR);
    },

    /**
     * Logs a "FATAL" message. See {@link #log}.
     * @param {String} message Message to log
     */
    logFatal: function(message) {
        this.log(message, this._levels.FATAL);
    },

    /**
     * Diagnostic function to see log contents. 
     * @return {String} A representation of the log contents
     */
    print: function() {
        var texts = 'Number of entries: ' + this._store.getCount() + '\n';
        this._store.each( function(record) {
            var entries = record.get('message');
            texts = texts + " \n " + entries;
        });

        return texts;
    },

    /**
     * Sets the filter level to the level specified, destroying the old filer.
     * Then, filters through the store, removing all messages that are not in
     * the new filter.
     * @param {String/String[]} levels Level(s) to allow through the filter.
     */
    filterBy: function(levels) {
        if (!Ext.isArray(levels)) {
            levels = [levels];
        }

        if (this._currentFilter) {
            delete this._currentFilter;
        }
        this._currentFilter = levels;

        this._store.filterBy( function(record, id) {
            if (Ext.Array.indexOf(levels, record.get("level")) != -1) {
                return true;
            }
            return false;
        }, this);

    },

    /**
     * Returns the locale
     * @private
     */
    _getLocale: function() {
        if (navigator.language) {
            return navigator.language;
        }
        else if (navigator.browserLanguage) {
            return navigator.browserLanguage;
        }
        else if (navigator.systemLanguage) {
            return navigator.systemLanguage;
        }
        else if (navigator.userLanguage) {
            return navigator.userLanguage;
        }
    },

    /**
     * Logs many messages as info messages. Specifically, the REFS Session ID,
     * User ID, Browser Code Name, Browser Name, Browser Version, 
     * Browser Minor Version, Platform, User Agent, Locale, SSL, 
     * Cookies Enabled, Java Enabled, Ext core version, Ext JS version, and the
     * Location, which is a substring of the current address (after the "#" 
     * character).
     */
    logEnvironment: function() {
        this.logInfo("[Logger] REFS Session ID: " + (Ext.util.Cookies.get("REFSSessionID") !== null ? Ext.util.Cookies.get("REFSSessionID") : "Not logged in"));
        this.logInfo("[Logger] User ID: " + (RP.globals !== undefined ? RP.globals.USERNAME : "No current user"));
        this.logInfo("[Logger] Browser Code Name: " + navigator.appCodeName);
        this.logInfo("[Logger] Browser Name: " + navigator.appName);
        this.logInfo("[Logger] Browser Version: " + navigator.appVersion);
        this.logInfo("[Logger] Browser Minor Version: " + navigator.appMinorVersion);
        this.logInfo("[Logger] Platform: " + navigator.platform);
        this.logInfo("[Logger] User Agent: " + navigator.userAgent);
        this.logInfo("[Logger] Locale: " + this._getLocale());
        this.logInfo("[Logger] SSL: " + (Ext.isSecure ? "yes" : "no"));
        this.logInfo("[Logger] Cookies Enabled: " + (navigator.cookieEnabled ? "yes" : "no"));
        this.logInfo("[Logger] Java Enabled: " + (navigator.javaEnabled() ? "yes" : "no"));
        this.logInfo("[Logger] Ext core version: " + Ext.getVersion("core").version);
        this.logInfo("[Logger] Ext JS version: " + Ext.getVersion("extjs").version);

        var url = window.location.href.split("#");
        this.logInfo("[Logger] Location: " + url[0]);
    },

    /**
     * Removes everything from the internal {@link Ext.data.ArrayStore}. If the
     * store had events suspended, those are resumed first. 
     */
    clearAll: function() {
        // Save suspension state so that we can fire the datachanged event when the store
        // is cleared.
        var suspended = this._store.eventsSuspended;

        if (suspended) {
            this._store.resumeEvents();
        }

        this._store.removeAll();

        if (suspended) {
            this._store.suspendEvents(false);
        }
    },

    /**
     * Toggle the "log to server" function, also notifying the server to turn 
     * on/off logging. See also {@link #setLogToServer}. 
     */
    toggleLogToServer: function() {
        this.setLogToServer(!this._logToServer, true);
    },

    /**
     * Set the max number of log entries to display in the UI. Removes entries
     * if the new limit is lower than the old limit.
     * @param {Number} value New maximum limit of log entries
     */
    setUIMaxEntries: function(value) {
        this._maxEntries = value;

        // TODO set the maxEntries on the login page so we don't have extra entries in our store
        if(this._store.getCount() > this._maxEntries) {
            var diff = this._store.getCount() - this._maxEntries;

            for(var i = 0; i <= diff ; i++) {
                this._store.removeAt(0);
            }
        }
    },

    /**
     * Set the "log to server" function. 
     * @param {Boolean} flag New state for logging to the server
     * @param {Boolean} notifyServer Flag to notify the server to turn on/off logging
     */
    setLogToServer: function(flag, notifyServer) {
        if (this._logToServer) {
            this.flushClientLogs();
        }

        // If this is the first time turning on the server logs, copy the UI logs to server buffer...
        if (!this._clientLogsCaptured) {
            this._clientLogsCaptured = true;
            this._store.each( function(record) {
                this._clientLogsBuffer.push(Ext.String.format("{0} {1}", record.get("level"), record.get("message")));
            }, this);

        }

        this._logToServer = flag;

        this.fireEvent("logtoservertoggled");

        if (notifyServer) {
            // If tracing is disabled and log downloads are enabled, prompt the user to download the log files
            if (flag === false && this.isLogDownloadEnabled()) {
                Ext.Msg.show({
                    title: RP.getMessage('rp.common.misc.DownloadLogPromptTitle'),
                    msg: RP.getMessage('rp.common.misc.DownloadLogPrompt'),
                    buttons: Ext.Msg.OKCANCEL,
                    icon: Ext.Msg.QUESTION,
                    scope: this,
                    fn: function(btn) {
                        if (btn === 'ok') {
                            Ext.getBody().mask(RP.getMessage('rp.common.misc.LoadingMaskText'));
                        }
                        Ext.Ajax.request({
                            url: this._clientLogPostURL,
                            params: {
                                action: 'enable',
                                content: 'false',
                                download: (btn === 'ok' ? 'true' : 'false')
                            },
                            scope: this,
                            success: (btn !== 'ok' ? Ext.emptyFn : function(response) {
                                Ext.getBody().unmask();
                                if (response.responseObject.data.downloadable === true) {
                                    var downloadToken = Ext.id(null, 'LogDownloadToken-');
                                    var url = Ext.String.format('{0}?action=download&content={1}',
                                            this._clientLogPostURL, downloadToken);
                                    if (!this.DownloadIFrame) {
                                        this.DownloadIFrame = new Ext.ux.IFrameComponent({
                                            url: url,
                                            hidden: true
                                        });
                                        this.DownloadIFrame.render(Ext.getBody());
                                    }
                                    else {
                                        this.DownloadIFrame.load(url);
                                    }
                                    this.LogDownloadResponseTask = Ext.TaskManager.start({
                                        interval: 250,
                                        scope: this,
                                        run: this._checkLogDownloadResponse,
                                        args: [downloadToken, response.responseObject.data.filename]
                                    });
                                } else {
                                    Ext.Msg.show({
                                        title: RP.getMessage('rp.common.misc.LogDownloadFailedTitle'),
                                        msg: RP.getMessage('rp.common.misc.LogTooBigForDownload') +
                                            '$REFSDIR/logs/' + response.responseObject.data.filename,
                                        buttons: Ext.Msg.OK,
                                        icon: Ext.Msg.ERROR
                                    });
                                }
                            }),
                            callback: (btn !== 'ok' ? Ext.emptyFn : function() {
                                Ext.getBody().unmask();
                            })
                        });
                    }
                });
            } else {
                // Make AJAX request to tell REFS server we are logging to server or not
                Ext.Ajax.request({
                    url: this._clientLogPostURL,
                    params: {
                        action: "enable",
                        content: flag ? "true" : "false"
                    },
                    scope: this,
                    failure: function() {
                        this.logError("[logger] Failed to enable/disable log to server");
                    }
                });
            }
        }
    },

    isLogDownloadEnabled: function() {
        //why is true a string?
        return RP.globals.getValue("LOG_DOWNLOAD_ENABLED") === "true";
    },

    /**
     * @private
     * This method is used as a Task.  It checks the log download cookies to determine whether the 
     * download has finished and whether or not that download was successful.
     * @author Jeff Gitter
     */
    _checkLogDownloadResponse: function(downloadToken, filename) {
        var token = Ext.util.Cookies.get('REFS-LogDownloadToken');
        var success = Ext.util.Cookies.get('REFS-LogDownloadSuccess') === "true";

        if (token === downloadToken) {
            if (!success) {
                Ext.Msg.show({
                    title: RP.getMessage('rp.common.misc.LogDownloadFailedTitle'),
                    msg: RP.getMessage('rp.common.misc.LogDownloadFailed') + '$REFSDIR/logs/' + filename,
                    buttons: Ext.Msg.OK,
                    icon: Ext.Msg.ERROR
                });
            }
            Ext.TaskManager.stop(this.LogDownloadResponseTask);
        }
    },

    /**
     * Gets "log to server" state
     * @return {Boolean} True if currently logging to the server
     */
    getIsLogToServer: function() {
        return this._logToServer;
    },

    /**
     * Sets the interval to post client logs to server
     * @param {Number} intervalMS Number of milliseconds between each client
     * log
     */
    setLogToServerInterval: function(intervalMS) {
        this._clientLogInterval = intervalMS;
    },

    /**
     * Getter for the interval to post client logs to server
     * @return {Number} Number of milliseconds between each client log
     */
    getLogToServerInterval: function() {
        return this._clientLogInterval;
    },

    /**
     * Sets the URL to post client-side logs to server
     * @param {String} url New URL to post client logs to the server to
     */
    setLogToServerURL: function(url) {
        this._clientLogPostURL = url;
    },

    /**
     * Gets the URL of where client logs are posted to the server
     */
    getLogToServerURL: function() {
        return this._clientLogPostURL;
    },

    /**
     * Flush the client logs to the server
     */
    flushClientLogs: function() {
        //alert(logger.print());
        if (!this._clientLogPostURL || this._clientLogsBuffer.length === 0) {
            return;
        }

        // Flush will run on a set time interval so if nothing is in
        // the buffer then we will skip the trying to dump it to the server
        if (this._clientLogsBuffer.length > 0) {
            this.logTrace("[logger] Dumping client logs to server.... ");
            // Make AJAX request...
            Ext.Ajax.request({
                url: this._clientLogPostURL,
                params: {
                    action: "post",
                    content: this._clientLogsBuffer.join("\n") + "\n"
                },
                scope: this,
                success: function() {
                    this._clientLogLastPost = new Date();
                    this._clientLogsBuffer = [];
                },

                failure: function() {
                    this._clientLogLastPost = new Date();
                    this.logError("[logger] Client Log failed to flush to the server");
                }

            });
        }
    }

});

logger = new RP.util.logger();
logger.logEnvironment();

// Flush logs when the window unloads
if (Ext.isFunction(window.onunload)) {
    window.onunload = window.onunload.createSequence( function() {
        logger.flushClientLogs();
    });

}
else {
    window.onunload = function() {
        logger.flushClientLogs();
    };

}

// Setup a task to flush logs on a set interval when log to server is set
var serverLogTask = Ext.TaskManager.start({
    run: function() {
        if (!logger.getIsLogToServer()) {
            return;
        }
        logger.flushClientLogs();
    },

    interval: logger.getLogToServerInterval()
});
//////////////////////
// ..\js\util\Waiter.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.Waiter
 *
 * A simple synchronizer class that allows you wait on a "list of things" and invoke
 * a delegate when that "list of things" becomes empty.  The Waiter's clear() method
 * is called automatically after the list reaches zero.
 */
RP.util.Waiter = function(config) {
  config = config || {};
  this.config = Ext.apply({}, config);
  
  this.config.defer = (!Ext.isDefined(config.defer) || (config.defer !== false));
  
  /**
   * @cfg {Function/Array of Function} handler
   * A delegate to invoke when this waiter's count reaches zero.
   */
  
  /**
   * @cfg {Object/Array of Object} objs
   * When the internal list becomes empty via calls to remove(), the
   * handler(s) will be invoked.
   */
  
  /**
   * @cfg {Boolean} defer
   * Defer calling the handler(s).  Default is true.  If synchronous processing
   * is required, set this to false.
   * 
   */
  
  /**
   * @type Array
   * @property
   */
  this.waitObjs = [];
  
  /**
   * @type Array of delegates or a delegate to invoke when the list becomes empty.
   * @property
   */
  this.handlers = [];
  if (!Ext.isEmpty(config.handler)) {
    this.addHandlers(config.handler);
  }
  
  this._suspended = false;
  
  if (Ext.isArray(config.objs)) {
    this.waitObjs = config.objs.concat();
  }
  else if (!Ext.isEmpty(config.objs)) {
    this.waitObjs.push(config.objs);
  }
};

RP.util.Waiter.prototype = {
  /**
   * Add an object or array of objects to wait on
   * @param {Object} obj
   */
  add: function(obj) {
    if (Ext.isArray(obj)) {
      this.waitObjs = this.waitObjs.concat(obj);
      
      Ext.each(obj, function(o) {
        logger.logTrace("[Waiter] Added " + o);
      });
    }
    else {
      this.waitObjs.push(obj);
      logger.logTrace("[Waiter] Added " + obj);
    }
  },
  
  /**
   * Gets the count of objects still being waited on.
   * @return {Number}
   */
  getCount: function() {
    return this.waitObjs.length;
  },
  
  /**
   * @method
   * Is the wait queue empty?
   */
  isEmpty: function() {
    return (this.getCount() === 0 ? true : false);
  },
  
  /**
   * Remove an object being waited on.  If the list becomes empty as a
   * result, the handler(s) will be invoked.
   * @param {Object} obj
   */
  remove: function(obj) {
    logger.logTrace("[Waiter] Removed " + obj);
    
    Ext.Array.remove(this.waitObjs, obj);
    if (!this._suspended && this.waitObjs.length === 0) {
      this._invokeHandlers();
    }
  },
  
  /**
   * Clear the wait list and handlers; will not cause handlers to be invoked.
   */
  clear: function() {
    delete this.waitObjs;
    this.waitObjs = [];
    
    delete this.handlers;
    this.handlers = [];
  },
  
  /**
   * Add a handler (to the end of the list) to call when wait list becomes empty.
   * @param {Function} handler The handler to call.
   * @param {Object} scope The scope by which the handler should be called.
   */
  addHandler: function(handler, scope) {
    this.handlers.push({
      fnction: handler,
      scope: scope || this
    });
  },
  
  /**
   * Add a list of handlers.  The handlers can be defined two different ways, in within an array.
   * 
   * The handler can specify the function to call as a result of invoking the handlers.  In order
   * to apply the scope to the handler, Ext.bind must be used.  Two examples of add a list
   * of functions are,
   * 
   * [function() { // do some work here }]  - scope is irrelevant.
   * [Ext.bind(function() { // do some work here }, scope)] - scope is handled in delegate
   * 
   * Or, the handler can be an object defined like,
   * 
   * [{
   *   fnction: function() { // do some work here },
   *   scope: this
   * }]
   * 
   * @param {Array} handlers A set of handlers to call at the end of waiting.
   */
  addHandlers: function(handlers) {
    if (Ext.isEmpty(handlers)) {
      return;
    }
    
    // make sure we have an array before getting started.
    if (! Ext.isArray(handlers)) {
      handlers = [handlers];
    }
    
    Ext.each(handlers, function(handler) {
      if (Ext.isObject() && handler.fnction) {
        // if an object, the handler function and scope are possibly defined.
        this.addHandler(handler.fnction, handler.scope || undefined);
      }
      else {
        // if a function, add the handler without scope.
        this.addHandler(handler);
      }
    }, this);
  },
  
  /**
   * Insert a handler (to the front of the list) to call when wait list becomes empty.
   * @param {Function} handler
   */
  insertHandler: function(handler, scope) {
    this.handlers.splice(0, 0, {
      fnction: handler,
      scope: scope || this
    });
  },
  
  /**
   * Suspends the handler(s) from being invoked if the list becomes empty.
   */
  suspend: function() {
    this._suspended = true;
  },
  
  /**
   * Resume normal operation.  If the list became empty while suspended, then
   * the handler(s) will be invoked.
   */
  resume: function() {
    if (this._suspended) {
      this._suspended = false;
      if (this.waitObjs.length === 0) {
        this._invokeHandlers();
      }
    }
  },
  
  // private
  _invokeHandlers: function() {
    while (this.handlers.length) {
      var h = this.handlers.shift();
      // Execute the method outside of the scope of this function 
      // to avoid blocking to avoid getting additional handlers 
      // added before they are properly handled.
      
      // TODO extjs 4, this defer should probably be taken out... it causes a lot of
      // timing problems and is not intuitive that there is a slight defer after the
      // waiter is empty. Removing the defer will cause issues in other places, but
      // the extjs 4 conversion would be a good time to do thise changes.
      // This comment is a result of RPWEB-4292. --nkremer
      if (this.config.defer) {
        Ext.defer(h.fnction, 1, h.scope);
      }
      else {
        h.fnction.call(h.scope);
      }
    }
  }
};
//////////////////////
// ..\js\util\FunctionQueue.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.FunctionQueue
 *
 * Implements a queue of functions that are called sequentially.  The functions are executed serially,
 * and each function is called only if the one before it has completed.  Each function may execute
 * some code asynchronously.
 */

RP.util.FunctionQueue = function(config) {

  /**
   * @cfg noDefer {Boolean} Do not defer between execution of each element in the queue; default=false
   */

  this._config = config || {};    // reserved for future use...
  this._q = [];
};

RP.util.FunctionQueue.prototype = {

  /**
   * Add to the queue
   * @param {Function} f Delegate to invoke
   */
  add: function(f) {
    this._q.push(f);    
  },
  
  /**
   * Execute the functions in the queue
   * @param {Function} successFn Delegate to invoke if queue finishes successfully
   * @param {Function} errorFn Delegate to invoke if queue doesn't finish
   */
  execute: function(successFn, errorFn) {
    logger.logTrace("[FunctionQueue] Starting execution of queue (" + this._q.length + " items)...");
    
    this._successFn = successFn;
    this._errorFn = errorFn;
    this._executeNext();
  },
  
  _executeNext: function() {
    if (this._q.length > 0) {
      var f = this._q.splice(0, 1)[0];
      
      logger.logTrace("[FunctionQueue] Executing next item...");
      try {
        f(Ext.bind(this._onItemSuccess, this), Ext.bind(this._onItemFailure, this));
      }
      catch (e) {
        logger.logError(e);
        
        if (RP.globals.SERVER_TYPE === "development") {
          throw e;
        }
        
        this._onItemFailure();
      }
    }
    else {
      logger.logTrace("[FunctionQueue] Successfully executed all items...");
      if (Ext.isFunction(this._successFn)) {
        this._successFn();
      }
    }
  },
  
  _onItemSuccess: function() {
    logger.logTrace("[FunctionQueue] Item successfully executed.");
    
    if (this._config.noDefer) {
      this._executeNext();
    }
    else {
      Ext.defer(this._executeNext, 10, this);
    }
  },
  
  _onItemFailure: function() {
    logger.logTrace("[FunctionQueue] Item failed.");
    
    if (Ext.isFunction(this._errorFn)) {
      this._errorFn();
    }
  }
};
//////////////////////
// ..\js\util\URLTransformer.js
//////////////////////
/**
 * @class RP.util.ScriptLoader
 * @singleton
 */
Ext.define('RP.util.URLTransformer', {
    singleton: true,

    /**
     * @private
     * Transform the url by prepending the static path if needed and replacing {client-mode} with
     * the actual client-mode value
     */
    transformStatic: function(url) {
        var base = RP.globals && RP.globals.paths &&
                !Ext.isEmpty(RP.globals.paths.STATIC) ? RP.globals.paths.STATIC : undefined;
        return RP.util.URLTransformer.rebase(url, base);
    },

    /**
     * @private
     * Transform the url by prepending the stash path if needed and replacing {client-mode} with
     * the actual client-mode value
     */
    transformStash: function(url) {
        //rebase the url if needed
        var base = RP.globals && RP.globals.paths &&
                !Ext.isEmpty(RP.globals.paths.STASH) ? RP.globals.paths.STASH : undefined;
        return RP.util.URLTransformer.rebase(url, base);
    },

    /**
     * @private
     * prepends the base to the url if it is not already there or has an absolute
     * path (excluding beginning slash variety).
     */
    rebase: function(url, base) {
        //validate url and base and make sure there's no ://
        //and that the url does not contain the base already...if so then rebase.
        if(url && base &&
            url.indexOf("://") === -1 && url.indexOf(base) !== 0) {
            url = base + url;
        }
        return url;
    },

    /**
     * @private
     * replaces client mode placeholder in url if present
     */
    replaceClientMode: function(url) {
        if (url && url.indexOf("{client-mode}") >= 0) {
            url = url.replace(/\{client-mode\}/g, RP.globals.CLIENTMODE);
        }
        return url;
    }
});
//////////////////////
// ..\js\util\ScriptLoader.js
//////////////////////
/**
 * @class RP.util.ScriptLoader
 * @singleton
 * @author Jeff Gitter
 */
Ext.define('RP.util.ScriptLoader', {
    singleton: true,
    /**
     * @private
     * @property {Integer} semaphore
     * This private variable is used specifically to account for source mode script files.  I don't want
     * to call $LAB.wait() on the onReady handler until after all the source mode files are executed.  This
     * will prevent the PageBootstrapper from trying to build the viewport before the source files themselves
     * are executed and ready.
     */
    semaphore: 0,

    constructor: function(cfg) {
        // if this is IE 8 or less, add an implicit wait after every load
        this.lab = $LAB.setOptions({
            AlwaysPreserveOrder: Ext.isIE
        });

        this.callParent(arguments);
    },

    /**
     * Invokes the designated delegate after the script loading queue is empty; if there
     * are no items in the queue, the delegate is invoked right away.  This delegate will
     * fire as soon as all prior scripts in the queue have been executed and will never
     * be called again.  This method can be called multiple times.
     *
     * @param {Function} fn The function to fire
     */
    onReady: function(fn) {
        if (this.semaphore === 0) {
            logger.logTrace("[ScriptLoader] onReady - Called with no scripts in the queue.");
            fn();
        } else {
            logger.logTrace("[ScriptLoader] onReady - Waiting for scripts in queue to load...");
            $LAB.queueWait(fn);
        }
    },

	/**
     * Alias for load
     */
    loadSerial: function(urls, callback) {
        this.load(urls, callback);
    },
	
    /**
     * Load the set of URLS from the static location.  All URLS will be executed in the order received, though
     * they will be downloaded asynchronously.
     *
     * @param {Array} urls The list of urls to download.
     * @param {Function} callback Function to call after loading of the urls is complete
     */
    load: function(urls, callback) {
		this._doLoadWithUrlTransform(urls, callback, RP.util.URLTransformer.transformStatic);
    },
	
	/**
	 * Loads the set of urls provided asynchronously, using the stash root path as the base
	 * for any relative urls in the set.
	 * 
	 * @param {Array} urls The list of urls to download.
     * @param {Function} callback Function to call after loading of the urls is complete
	 */
	loadFromStashRoot: function(urls, callback) {
		this._doLoadWithUrlTransform(urls, callback, RP.util.URLTransformer.transformStash);
	},
	
	/**
	 * Loads the set of urls provided asynchronously and as given, without
	 * any modification (no rebasing of relative paths).
	 */
	loadDirect: function(urls, callback) {
		this._doLoadWithUrlTransform(urls, callback, function(url){return url;});
	},
	
	/**
	 * @private
	 * performs a load based on the given transformFn, which transforms the urls
	 * as needed into absolute paths and converts and client mode tokens into the proper
	 * global client mode.
	 */
	_doLoadWithUrlTransform: function(urls, callback, transformFn) {
		if (Ext.isEmpty(urls)) {
            return;
        }
        callback = callback || Ext.emptyFn;
        urls = Ext.Array.from(urls);
        var ii;
        for (ii = 0; ii < urls.length; ii++) {
            if (Ext.isString(urls[ii])) {
                urls[ii] = transformFn(urls[ii]);
				urls[ii] = RP.util.URLTransformer.replaceClientMode(urls[ii]);
            }
        }
        logger.logTrace("[ScriptLoader] Loading Scripts: " + urls.join(","));
        this.semaphore++;
        this.lab.script(urls).wait(Ext.bind(function() {
            callback();
            this.semaphore--;
            if (this.semaphore === 0) {
                logger.logTrace("[ScriptLoader] onReady - Scripts in queue loaded, so invoking handlers...");
                $LAB.runQueue();
            }
        }, this));
	},

    /**
     * @method
     * Loads a library from the Stash
     * @param {String} name Name of library, e.g., "charting"
     * @param {String} version Version of library, e.g., "1.0.0"
     * @param {Function} callback Delegate to invoke upon success
     * @param {Function} errorFn Delegate to invoke upon failure
     */
    loadStashLibrary: function(name, version, callback, errorFn) {
        var incs;
        try {
            incs = RP.stash.getLibraryIncludes(name, version);
        }
        catch (error) {
            logger.logFatal("[ScriptLoader] " + error.message);
        }

        if (incs.length === 0) {
            logger.logDebug("[ScriptLoader] No items for library '" + name + "'");
            return;
        }

        logger.logDebug("[ScriptLoader] Loading library '" + name + "' version " + version);

        // Process inline scripts and external CSS/scripts in blocks.
        for (var i = 0; i < incs.length; i++) {
            var inc = incs[i];

            if (inc.isType("s")) { // external javascript
                logger.logTrace("[ScriptLoader] Adding external script: " + inc.getUrl());
                this.load(inc.getUrl(), callback, errorFn);
            }
            else if (inc.isType("c")) { // CSS
                logger.logTrace("[ScriptLoader] Adding CSS: " + inc.getUrl());
                RP.util.CSSLoader.load(inc.getUrl(), false);
                callback();
            }
            else if (inc.isType("j")) { // inline javascript
                logger.logTrace("[ScriptLoader] Adding inline script: " + inc.getInlineCode());
                try {
                    eval(inc.getInlineCode());
                    callback();
                } catch (e) {
                    errorFn();
                    throw e;
                }
            }
            else {
                logger.logFatal("[ScriptLoader] Unhandled include type from library: " + inc.getType());
            }
        }
    }
});
//////////////////////
// ..\js\util\SourceScriptLoader.js
//////////////////////
/**
 * @class RP.util.SourceScriptLoader
 * @singleton
 * 
 * Loads script files when in client-mode "src".
 *
 * @author Jeff Gitter
 */
Ext.define('RP.util.SourceScriptLoader', {
    singleton: true,
    srcServletPath: "src/{0}",

    instrumented: (function() {
            var instrumentedFiles, map = {};
            if (localStorage) {
                instrumentedFiles = Ext.JSON.decode(localStorage.getItem('instrumentedFiles'));

                Ext.Array.each(instrumentedFiles, function(path) {
                    map[path] = true;
                });

            }
            return map;
        
        } ()),

    /**
     * @private
     * The URL transformation method to define the correct URL for
     * download.
     * 
     * @param {String} url The raw file name.
     * @return {String} The transformed url.
     */
    urlTransform: function(url) {
        var prefix = '', staticPath = '';
        if (RP.globals && RP.globals.paths && !Ext.isEmpty(RP.globals.paths.STATIC)) {
            prefix = RP.globals.paths.STATIC;
            staticPath = prefix.replace(/web\/?$/, '');
        }

        if (prefix && (url.indexOf("://") < 0)) {
            url = staticPath + url;
        }

        return url;
    },

    /**
    * Load the following source files based on their absolute path
    * in the host system.  
    * 
    * This is only to be used for development purposes!
    * 
    * @param {Array} urls The list of URLs.
    */
    load: function(urls){
        if (!Ext.isArray(urls)) {
            urls = [urls];
        }

        var srcUrls = [];
        Ext.each(urls, function(url){
            url = url.replace(/\\/g, "/");
            srcUrls.push(this.tryRedirectToInstrumented(url));
        }, this);

        RP.util.ScriptLoader.load(srcUrls);
    },

    /**
     * Redirect to the instrumented version of the file for the passed
     * in url if that url is found in the instrumentedFiles cookie.
     */
    tryRedirectToInstrumented: function(url) {
        if (this.instrumented[url] === true) {
            url = url.replace(/^(\/|[^\/]+\/)/, RP.globals.paths.STATIC + 'instrumented/');
        }
        else {
            url = this.urlTransform(Ext.String.format(this.srcServletPath, url));
        }
        return url;
    }
});
