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
// ..\js\util\DeprecationUtils.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.DeprecationUtils
 *
 * This class of methods assists with deprecated funcitonality that
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
        
        Ext.each(contexts, function(context) {
            baseContext = baseContext[context];
        });
        
        return baseContext;
    }
    
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
         * @method
         * @param {String} oldName The old class name reference.
         * @param {String} newName The new name of the class.
         *
         * This renameClass will take functionality from the new class
         * and wrap the methods with a new function that will indicate the
         * deprecation of the old class and identify the new class.
         *
         * The two parameters should be fully qualified from the window object.
         */
        renameClass: function(oldName, newName) {
            var newClass = convertNameSpaceToWindowNameSpace(newName);
            var oldClass = convertNameSpaceToWindowNameSpace(oldName);
            var deprecationMessage = String.format("[DEPRECATED] The use of the {0} shorthand is deprecated.  Please use {1}.", oldName, newName);
            
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
         * @method
         * 
         * Use this method to create warnings and stack trace in regards
         * to a deprecated instance method.
         * 
         * The newClass.prototype[newMethodName].apply will fail if the new method uses a property available
         * to the newClass that is not available to the oldClass since *this* points to an instantiated oldClass
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
            var deprecatedMsg = String.format("[DEPRECATED] The use of {0}.{1} is deprecated.  Please use {2}.{3} {4}.", oldClassName, oldMethodName, newClassName, newMethodName, optionalMessage);
            
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
         * Log a warning and stack trace to the appropriate consoles.
         * @param {String} msg The message to display in the warning.
         */
        logStackTrace: function(msg) {
            if (RP.globals.getValue("SERVER_TYPE") !== "production") {
                if (window.console && console.warn) {
                    console.warn(msg);
                    if (console.trace) {
                        console.trace();
                    }
                }
                
                logger.logWarning(msg + "  See the browser console for detailed information.");
            }
        }
    };
}();
//////////////////////
// ..\js\util\DateExt.js
//////////////////////
/**
 * @namespace Date
 * @method
 * Resets the time of this Date object to 12:00 AM (00:00), which is the start of the day.
 * @param {Boolean}  .clone() this date instance before clearing Time
 * @return {Date}    this
 */
Date.prototype.clearTime = function(clone) {
    if (clone) {
        return this.clone().clearTime();
    }
    
    // get current date before clearing time
    var d = this.getDate();
    
    // clear time
    this.setHours(0);
    this.setMinutes(0);
    this.setSeconds(0);
    this.setMilliseconds(0);
    
   if (this.getDate() != d) { // account for DST (i.e. day of month changed when setting hour = 0)
        // note: DST adjustments are assumed to occur in multiples of 1 hour (this is almost always the ca
        // refer to http://www.timeanddate.com/time/aboutdst.html for the (rare) exceptions to this rule
        // increment hour until cloned date == current date
        // http://www.sencha.com/forum/showthread.php?62250-CLOSED-35-3.x-2.x-DatePicker-Picks-wrong-date/page6&highlight=safeParse
        // for this DST adjust to work for places that change time at midnight, the Date's time needs to be set (to noon or something)
        for (var hr = 1, c = this.add(Date.HOUR, hr); c.getDate() != d; hr++, c = this.add(Date.HOUR, hr)){}
        this.setDate(d);
        this.setHours(c.getHours());
    }
    
    return this;
};

/**
 * @namespace Date
 * @method
 * Compares the first date to the second date and returns an number indication of their relative values.
 * @param {Date}     First Date object to compare [Required].
 * @param {Date}     Second Date object to compare to [Required].
 * @return {Number}  -1 = date1 is lessthan date2. 0 = values are equal. 1 = date1 is greaterthan date2.
 */
Date.compare = function(date1, date2) {
    if (isNaN(date1) || isNaN(date2)) {
        throw new RP.Exception(date1 + " - " + date2);
    }
    else 
        if ((date1 instanceof Date) && (date2 instanceof Date)) {
            return (date1 < date2) ? -1 : (date1 > date2) ? 1 : 0;
        }
        else {
            throw new TypeError(date1 + " - " + date2);
        }
};

/**
 * @namespace Date
 * Returns a new Date object that is an exact date and time copy of the original instance.
 * @return {Date}    A new Date instance
 */
Date.prototype.clone = function() {
    return new Date(this.getTime());
};

/**
 * @namespace Date
 * Compares this instance to a Date object and returns an number indication of their relative values.
 * @param {Date}     Date object to compare [Required]
 * @return {Number}  -1 = this is lessthan date. 0 = values are equal. 1 = this is greaterthan date.
 */
Date.prototype.compareTo = function(date) {
    return Date.compare(this, date);
};

/**
 * @namespace Date
 * Compares this instance to another Date object and returns true if they are equal.
 * @param {Date}     Date object to compare. If no date to compare, new Date() [now] is used.
 * @return {Boolean} true if dates are equal. false if they are not equal.
 */
Date.prototype.equals = function(date) {
    return this.compareTo(date || new Date()) === 0;
};

/**
 * @namespace Date
 * Determines if this instance is between a range of two dates or equal to either the start or end dates.
 * @param {Date}     Start of range [Required]
 * @param {Date}     End of range [Required]
 * @return {Boolean} true is this is between or equal to the start and end dates, else false
 */
Date.prototype.between = function(start, end) {
    return this.getTime() >= start.getTime() && this.getTime() <= end.getTime();
};

/**
 * @namespace Date
 * Determines if this date occurs after the date to compare to.
 * @param {Date}     Date object to compare. If no date to compare, new Date() ("now") is used.
 * @return {Boolean} true if this date instance is greater than the date to compare to (or "now"), otherwise false.
 */
Date.prototype.isAfter = function(date) {
    return this.compareTo(date || new Date()) === 1;
};

/**
 * @namespace Date
 * Determines if this date occurs on or after the date to compare to.
 * @param {Date}     Date object to compare. If no date to compare, new Date() ("now") is used.
 * @return {Boolean} true if this date instance is greater than the date to compare to (or "now"), otherwise false.
 */
Date.prototype.isOnOrAfter = function(date) {
    var c = this.compareTo(date || new Date());
    return (c >= 0);
};

/**
 * @namespace Date
 * Determines if this date occurs before the date to compare to.
 * @param {Date}     Date object to compare. If no date to compare, new Date() ("now") is used.
 * @return {Boolean} true if this date instance is less than the date to compare to (or "now").
 */
Date.prototype.isBefore = function(date) {
    return (this.compareTo(date || new Date()) === -1);
};

/**
 * @namespace Date
 * Determines if this date occurs on or before the date to compare to.
 * @param {Date}     Date object to compare. If no date to compare, new Date() ("now") is used.
 * @return {Boolean} true if this date instance is less than the date to compare to (or "now").
 */
Date.prototype.isOnOrBefore = function(date) {
    var c = this.compareTo(date || new Date());
    return (c <= 0);
};

/**
 * @namespace Date
 * Adds the specified number of milliseconds to this instance.
 * @param {Number}   The number of milliseconds to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addMilliseconds = function(value) {
    this.setTime(this.getTime() + value);
    return this;
};

/**
 * @namespace Date
 * Adds the specified number of seconds to this instance.
 * @param {Number}   The number of seconds to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addSeconds = function(value) {
    return this.addMilliseconds(value * 1000);
};

/**
 * @namespace Date
 * Adds the specified number of seconds to this instance.
 * @param {Number}   The number of seconds to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addMinutes = function(value) {
    return this.addMilliseconds(value * 60000); /* 60*1000 */
};

/**
 * @namespace Date
 * Adds the specified number of hours to this instance.
 * @param {Number}   The number of hours to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addHours = function(value) {
    return this.addMilliseconds(value * 3600000); /* 60*60*1000 */
};

/**
 * @namespace Date
 * Adds the specified number of days to this instance.
 * @param {Number}   The number of days to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addDays = function(value) {
    var expectedHours = this.getHours(),
        expectedMinutes = this.getMinutes();
    this.setTime(this.getTime() + value * 86400000); /* 24*60*60*1000 */
    this.adjustForDST(expectedHours, expectedMinutes);
    return this;
};


/**
 * Adjusts the Current Time by either adding 1 hour or 30 minutes so that hour or minutes
 * will be the same numerically.
 * This will adjust for either 1 hour or 30 minutes.
 * @param {Object} expectedHours the hour that it should be
 * @param {Object} expectedMinutes the minutes that it should be
 * @return {Date} this for chaining
 */
Date.prototype.adjustForDST = function(expectedHours, expectedMinutes) {
    var currentMinutes = this.getMinutes(),
        currentHours = this.getHours();
    
    if (currentHours == expectedHours && currentMinutes == expectedMinutes) { //if same, no DST
        return this;
    }
    
    var vectorHours = currentHours - expectedHours,     //1-2   normal dst
        deltaHours = Math.abs(vectorHours),
        deltaMinutes = Math.abs(currentMinutes - expectedMinutes);  //30-00
        
    if (Math.abs((currentHours+24) - expectedHours) < deltaHours){   //0-23
        vectorHours = (currentHours+24) - expectedHours;
        deltaHours = Math.abs(vectorHours);
    }
    if (Math.abs(currentHours - (expectedHours+24)) < deltaHours) { //23-0
        vectorHours = currentHours - (expectedHours+24);
        deltaHours = Math.abs(vectorHours);
    }

    if (deltaMinutes > 0) {
        this.addMinutes(-deltaMinutes * (vectorHours / Math.abs(vectorHours)));
    } else {
        this.addHours(-vectorHours);
    }
    return this;
};

/**
 * @namespace Date
 * Adds the specified number of weeks to this instance.
 * @param {Number}   The number of weeks to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addWeeks = function(value) {
    return this.addDays(value * 7);
};

/**
 * @namespace Date
 * Adds the specified number of months to this instance.
 * @param {Number}   The number of months to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addMonths = function(value) {
    var n = this.getDate();
    this.setDate(1);
    this.setMonth(this.getMonth() + value);
    this.setDate(Math.min(n, Date.__getDaysInMonth(this.getFullYear(), this.getMonth())));
    return this;
};

/**
 * @namespace Date
 * Adds the specified number of years to this instance.
 * @param {Number}   The number of years to add. The number can be positive or negative [Required]
 * @return {Date}    this
 */
Date.prototype.addYears = function(value) {
    return this.addMonths(value * 12);
};

/**
 * @namespace Date
 * Decodes a date string (that uses dashes) such as 5-18-2008 to a date object; reason is
 * you can't use forward slashes in date strings in a URL...
 * @param {String} date as a string (mm-dd-yyyy)
 * @return {Date} new date
 */
Date.decodeDateInURL = function(val) {
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
};

/**
 * @namespace Date
 * Decodes a date string (that uses dashes) such as 5-18-2008 to a date object; reason is
 * you can't use forward slashes in date strings in a URL...
 * @param {Date} date
 * @return {String} date formatted as (mm-dd-yyyy)
 */
Date.encodeDateToURL = function(dt) {
    return String.format("{0}-{1}-{2}", dt.getMonth() + 1, dt.getDate(), dt.getFullYear());
};

/**
 * @namespace Date
 * Determines if the current date instance is within a LeapYear.
 * @param {Number}   The year.
 * @return {Boolean} true if date is within a LeapYear, otherwise false.
 */
Date.__isLeapYear = function(year) {
    return ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0);
};

/**
 * @namespace Date
 * Gets the number of days in the month, given a year and month value. Automatically corrects for LeapYear.
 * @param {Number}   The year.
 * @param {Number}   The month (0-11).
 * @return {Number}  The number of days in the month.
 */
Date.__getDaysInMonth = function(year, month) {
    return [31, (Date.__isLeapYear(year) ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
};

/**
 * @namespace Date
 * @param {Object} format The format of the date.
 */
Date.prototype.formatDate = function(format) {
    if (format && !RP.Formatting.Dates.isValidFormat(format)) {
        return RP.Formatting.Dates.formatExt(this, format);
    }
    return RP.core.Format.formatDate(this, format);
};

/**
 * @namespace Date
 */
Date.prototype.dateFormat = function() {
    return this.formatDate.apply(this, [arguments[0], true]);
};

/**
 * @namespace Date
 * @param {Object} formatObj
 */
Date.prototype.formatTime = function(formatObj) {
    return RP.core.Format.formatTime(this, formatObj);
};

/**
 * @namespace Date
 * @param {Object} someDate
 * @param {Object} format
 */
Date.extParseDate = Date.parseDate;
Date.parseDate = function(someDate, format) {
        if (arguments.length > 1 && (format.length > 1 || format.length === 1 && someDate.length < 3)) {
            return Date.extParseDate.apply(Date, arguments);
        }
        return RP.core.Format.parseDateTime(someDate, format);
    };

/**
 * @namespace Date
 * @param {Object} someTime
 * @param {Object} format
 */
Date.parseTime = function(someTime, format) {
    return RP.core.Format.parseTime(someTime, format);
};

/**
 * @namespace Date
 * @param {Object} date
 */
Date.prototype.deltaT = function(date) {
    /// <summary>
    /// Returns the difference in time between this date and the specified date.
    /// </summary>
    /// <param name="date" type="Date">The date to subtract from the current date.</param>
    /// <returns type="TimeSpan">the time span between dates</returns>
    return new RP.TimeSpan(this.getTime() - date.getTime());
};

/**
 * Rounds the Date by minutes.
 * @param {Number} roundToMinutes (optional) The number of minutes to round to.
 * Defaults to 15.
 */
Date.prototype.round = function(roundToMinutes) {
    // Default to 15 minute rounding if not specified.
    if (roundToMinutes === undefined) {
        roundToMinutes = 15;
    }
    
    var m = this.getMinutes() % roundToMinutes;
    
    if (m > 0) {
        var halfRound = parseInt(roundToMinutes / 2, 10);
        if (m <= halfRound) {
            this.addMinutes(-1 * m);
        }
        else {
            this.addMinutes(roundToMinutes - m);
        }
    }
    
    RP.util.Dates.setSeconds(this, 0);
    RP.util.Dates.setMilliseconds(this, 0);
    return this;
};

/**
 * Banker's rounding: the value is rounded to the nearest even number minute
 * @namespace Date
 * @return Date rounded to nearest minute.
 */
Date.prototype.roundToNearestMinute = function() {
    if (this.getSeconds() >= 30) {
        this.addMinutes(1);
    }
    
    RP.util.Dates.setSeconds(this, 0);
    RP.util.Dates.setMilliseconds(this, 0);
    return this;
};

Date.prototype.roundToNearestSeconds = function(secondsInterval) {
    if (secondsInterval === undefined) {
        secondsInterval = 15;
    }
    
    var seconds = this.getSeconds() % secondsInterval;
    
    if (seconds > 0) {
        var halfRound = parseInt(secondsInterval / 2, 10);
        if (seconds <= halfRound) {
            this.addSeconds(-1 * seconds);
        }
        else {
            this.addSeconds(secondsInterval - seconds);
        }
    }
    
    RP.util.Dates.setMilliseconds(this, 0);
    return this;
};

/**
 * @namespace Date
 * Modifies the date to clear the minutes, seconds, and
 * milliseconds.  This modifies the original date.
 *
 * @return {Date} this
 */
Date.prototype.roundBackToHour = function() {
    RP.util.Dates.setMinutes(this, 0);
    RP.util.Dates.setSeconds(this, 0);
    RP.util.Dates.setMilliseconds(this, 0);
    
    return this;
};

/**
 * @namespace Date
 * @param {Object} time
 */
Date.prototype.addTime = function(time) {
    this.setHours(time.getHours());
    this.setMinutes(time.getMinutes());
    this.setSeconds(time.getSeconds());
    this.setMilliseconds(time.getMilliseconds());
    return this;
};

Date.prototype.getISO8601StringNoTranslation = function() {
    return RP.core.Format.formatDate(this, RP.core.Format.ISO8601NoOffset);
};

Date.prototype.getISO8601StringInUserTimeZone = function() {
    var userTimeZoneOffset = this.isDST() ? RP.globals.getValue("USER_TIME_ZONE").daylightTimeOffset : RP.globals.getValue("USER_TIME_ZONE").standardTimeOffset;
    var minDecimal = userTimeZoneOffset % 1;
    var hours = userTimeZoneOffset - minDecimal;

    var min = Math.round(minDecimal * 60);
    var sign = userTimeZoneOffset >= 0 ? "+" : "-";
    
    hours = Math.abs(hours);
    
    hours = (hours.toString().length === 1 ? "0" : "") + hours;
    min = (min.toString().length === 1 ? "0" : "") + min;
    var sFormatVal = sign + hours + ":" + min;

    
    return RP.core.Format.formatDate(this, RP.core.Format.ISO8601NoOffset) + sFormatVal;
};

/**
 * Translates an Ext date format to something more human readable.
 * @param {Object} format
 */
Date.translateFormat = function(format) {
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
};

//////////////////////
// ..\js\util\Dates.js
//////////////////////
Ext.ns("RP.util");

/**
 * @class RP.util.Dates
 * @singleton
 *
 * A class for Date utilities.
 * 
 */
RP.util.Dates = (function() {

    /**
     * @method
     * _setTimeValue
     * This method will set the time value (milliseconds | seconds | mintues) of a date object to the
     * passed in value.
     *
     * @param {Date} date The date to set the time value of.
     * @param {Number} newValue The new time value to set the date to.
     * @param {Function} timeUnitGetterFn The function that will be used to get the value of the proper
     * unit that you are modifying (ex. getMilliseconds, getSeconds).
     * @param {Number} multiplier The number that is used to convert the time unit to milliseconds.
     */
    var _setTimeValue = function(date, newValue, timeUnitGetterFn, multiplier) {
        var currentTimeValue = date[timeUnitGetterFn]();
        date.setTime((date.getTime() - currentTimeValue * multiplier) + newValue * multiplier);
    };
    
    return ({
    
        /**
         * @method
         * setMilliseconds
         *
         * <p>A method is used to set time values on a date object with correct Daylight Savings Time.
         *
         * <p>This is a fix for the default Date.prototype.setMillisecond which will incorrectly
         * set the Time Zone when dealing with the hour between 1:00 am and 1:59 am on a Daylight Savings Day.
         *
         * <p>This method will set the milliseconds of the date object to the given value.
         *
         * @param {Date} date The date to set the time value of.
         * @param {Number} value The new milliseconds value to set the date to.
         */
        setMilliseconds: function(date, milliseconds) {
            _setTimeValue(date, milliseconds, "getMilliseconds", 1);
        },
        
        /**
         * @method
         * setSeconds
         *
         * <p>A method is used to set time values on a date object with correct Daylight Savings Time.
         *
         * <p>This is a fix for the default Date.prototype.setSeconds which will incorrectly
         * set the Time Zone when dealing with the hour between 1:00 am and 1:59 am on a Daylight Savings Day.
         *
         * <p>This method will set the seconds of the date object to the given value.
         *
         * @param {Date} date The date to set the time value of.
         * @param {Number} value The new seconds value to set the date to.
         */
        setSeconds: function(date, seconds) {
            _setTimeValue(date, seconds, "getSeconds", 1000);
        },
        
        /**
         * @method
         * setMinutes
         *
         * * <p>A method is used to set time values on a date object with correct Daylight Savings Time.
         *
         * <p>This is a fix for the default Date.prototype.setMinutes which will incorrectly
         * set the Time Zone when dealing with the hour between 1:00 am and 1:59 am on a Daylight Savings Day.
         *
         * This method will set the minutes of the date object to the given value.
         * @param {Date} date The date to set the time value of.
         * @param {Number} value The new minutes value to set the date to.
         */
        setMinutes: function(date, minutes) {
            _setTimeValue(date, minutes, "getMinutes", 60000);
        }
        
    });
})();
//////////////////////
// ..\js\util\DateOverride.js
//////////////////////
Ext.ns("RP.date");

(function() {
  RP.date.NativeDate = Date;
  RP.date.DateOverride = function() {
    var date = new RP.date.NativeDate();
   
    // Look for an ISO date string in the constructor argument, and if exists, construct
    // using Ext's parseDate method... 
    if (arguments.length === 1 && typeof arguments[0] === "string" && arguments[0].match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/)) {
      date = Date.extParseDate(arguments[0], "c");
      
      // Get the server time zone off of the date string 
      // and store it in decimal form on the date object.
      // this is used for RP.core.Format.formatDateNoTranslation
      var isoDateNoOffset = arguments[0].replace(/(\+|-)(\d?\d:?\d\d)/, "");
      if(!Ext.isEmpty(isoDateNoOffset)) {
          date.isoDateNoOffset = isoDateNoOffset; 
      }
    }
    else if (arguments.length === 0) {
      return new RP.date.NativeDate();
    } 
    else if (arguments.length === 1) {
      // Work around weird Firefox behavior where if the string is any 4 digit number,
      // it translates the date to Dec. 31 7PM and the number as the year...  Workaround
      // is to simply insert a colon after the first two digits, so e.g., "1400" becomes
      // 14:00, which is 2:00 PM
      var str;      
      if (Ext.isGecko && Ext.isString(arguments[0]) && arguments[0].match(/^\d{4}$/)) {
        str = arguments[0].substr(0, 2) + ":" + arguments[0].substr(2);
      }
      // Work around for Chrome trying to compensate for a time being invalid.
      // When a time with minutes greater than 59 is parsed by Date's constructor, Chrome
      // assumes that the number is meant for the years of the date. For example,
      // new Date("6:70") will create a date with the year = 1970 and the hour = 6, but the
      // minutes will be 00. We're expecting Chrome to behave like Firefox here and return an
      // "Invalid Date", but it does not.
      else if (Ext.isChrome && Ext.isString(arguments[0]) && arguments[0].match(/:\d+/)) {
        var testDate = new RP.date.NativeDate(arguments[0]); 
        var minuteInt = parseInt(arguments[0].match(/:(\d+)/)[1], 10);
        
        if(minuteInt >= 60 && testDate && !isNaN(testDate.getFullYear())) {
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
      date = new RP.date.NativeDate(arguments[0], arguments[1], arguments[2],
          arguments[3] || 0,
          arguments[4] || 0,
          arguments[5] || 0,
          arguments[6] || 0);
      
      
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


//var dt = new Date();
//alert(dt.toString());
//
//var dt = new Date(2010, 3, 22);
//alert(dt.toString());
//
//var dt = new Date(2010, 3, 22, 23, 0);
//alert(dt.toString());
//
//dt = new Date("2010-04-22T07:00:00-05:00");
//alert(dt.toString());
//
//dt = new Date("2010-04-22T07:00:00");
//alert(dt.toString());

//////////////////////
// ..\js\core\FormatEngine.js
//////////////////////
/// <reference path="../../../../extjs/3.0/ext-base-debug.js" />
/// <reference path="../../../../extjs/3.0/ext-all-debug.js" />

/*global RP, Ext, _FormatBasedOnType */

Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class FormatEngine
 * @singleton
 * @extends none
 *
 * @param {Object} formats
 * @param {Object} constants
 */
RP.core.FormatEngine = function(formats, constants) {
    
    var FORMAT_ISO8601_WITH_OFFSET = "yyyy\u0027-\u0027MM\u0027-\u0027ddTHH:mm:sszzz";
      
    var FORMAT_ISO8601_WITHOUT_OFFSET = "S";

    //used to hold information changed per public function call
    var _FormatInfo = {};


    /**************************************************************************************/
    //Formatting helper functions
    //category used to determine parse or format
    function _SetFormatInfo(formatObj, sFormatType)
    //set or reset the member variables
    //should be called first inside every public function
    {
        _FormatInfo = {};
        _FormatInfo.FormatString = formatObj.formatstring.trim();
    }

    function _IsValidInputToFormat(input) {
        var bValid = true;
        var re = /^\d+$/; //only number input should be formatted here
        if (!re.test(input)) {
            bValid = false;
        }
        return bValid;
    }

    function _TransStringFormat(value)
    //this function is called after body text is replace by <<__BodyTextX>>
    //need to fill '0' for digits
    {
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
                if (j < 0) {break;} //jump out the loop if finish looping value
                sOutput = value.charAt(j) + sOutput;
                j--;
            }
            else  //copy char
            {
                if (sChar === "'") //ignore single quote to encapsulate the body text
                {
                    if (!bBodyText) {bBodyText = true;}
                    else {
                        if (sFormatString.charAt(i - 1) === "'") {sOutput = sChar + sOutput;}
                        //check whether consecutive single quotes
                        if (sFormatString.charAt(i + 1) !== "'") {bBodyText = false;}
                    }
                }
                else  //copy body text
                {
                    if (bBodyText) {sOutput = sChar + sOutput;}
                }
            }
        }
        return sOutput;
    }

    /*************************************************************************************/
    /**************************************************************************************/

    function _FormatDate(date, format) {
        if (!format) {
          format = RP.core.Formats.Date.Default;
        }
        return RP.Formatting.Dates.format(date, format);
    }

    function _FormatTime(time, format) {
        if (!format) {
          format = RP.core.Formats.Time.Default;
        }
        return RP.Formatting.Times.format(time, format);
    }
    
    function _FormatTimeSpan(value, formatObj) {
        var s = value.totalSeconds();
        var m = Math.floor(s / 60) % 60;
        var h = Math.floor(s / 3600);
        
        var hh = String.leftPad(h, 2, "0");
        var mm = String.leftPad(m, 2, "0");
        var ss = String.leftPad(Math.round(s % 60), 2, "0");
        
        if (!formatObj.template) {
            formatObj.template = new Ext.Template(formatObj.formatstring);
            formatObj.template.compile();
        }
        
        return formatObj.template.apply({h: h, hh: hh, mm: mm, ss: ss});
    }

    function _FormatCurrency(number, format) {
        if (!format) {
          format = RP.core.Formats.Currency.Default;
        }
        return RP.Formatting.Currencies.format(number, format);
    }

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

    function _FormatString(value, formatObj) {
        if (!_IsValidInputToFormat(value)) {
            throw new RP.Exception("Invalid input: " + value.toString() + " to format as: String");
        }
        if (!formatObj) {
            throw new RP.Exception("Must specify format type object");
        }
        _SetFormatInfo(formatObj, "string");
        //as of Excel, only numbers will be formatted
        return _TransStringFormat(value);
    }

    function _ParseDate(date, format) {
        var parsedDate = RP.Formatting.Dates.parse(date, format);
        if (parsedDate) {
          return parsedDate.clearTime(parsedDate);
          
        }
        return parsedDate;
    }

    function _ParseTime(time, format) {
        return RP.Formatting.Times.parse(time, format);
    }
    //this function is useful for sorting when you want to retain the value
    //of the time from the date.  ParseDate removes the time and returns 00:00:00
    //it assumes the time is in the format 00:00 AM or 00:00:00 AM, any other format
    //is ignored.  (this is the format ParseDate uses to remove the time).
    function _ParseDateWithTime(datetime, format) {
        return RP.Formatting.Dates.parse(datetime, format);
    }


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

    function _ParseNumber(number, format) {
        return RP.Formatting.Numbers.parse(number, format);
    }

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
    * @private 
    * @method
    * _ConvertDateToUserTimeZone
    * 
    * <p> This method is used to convert a given date into the user's preferred time zone.
    * 
    * @param {Date} date The Date that you want to convert.
    */
    function _ConvertDateToUserTimeZone(date) {
        // If the authentication connection hasn't implemented getUserPreferredTimeZone throw and erro and return the original date.
        if (Ext.isEmpty(RP.globals.getValue("USER_TIME_ZONE").standardTimeOffset) || Ext.isEmpty(RP.globals.getValue("USER_TIME_ZONE").daylightTimeOffset)) {
            throw new Error("The authentication connection does not have user preferred time zone implemented");
        }
        var userTimeZoneOffset = date.isDST() ? RP.globals.getValue("USER_TIME_ZONE").daylightTimeOffset : RP.globals.getValue("USER_TIME_ZONE").standardTimeOffset; 
        var timezoneDifference = (date.getTimezoneOffset() / 60) + userTimeZoneOffset;
        return date.clone().addHours(timezoneDifference); 
    }
    
    /**
     * This method will format the given date to the Date format passed in.
     * @param {Date} date The date that should be converted.
     * @param {Object} format The RPFormat Date format the date should be formatted as.
     */
    function _FormatDateInUserTimeZone(date, format) {
        return _FormatDate(_ConvertDateToUserTimeZone(date), format);
    }

    /**
     * This method will format the given date to the Time format passed in.
     * @param {Date} date The date that should be converted.
     * @param {Object} format The RPFormat Time format the date should be formatted as.
     */  
    function _FormatTimeInUserTimeZone(date, format) {
        return _FormatTime(_ConvertDateToUserTimeZone(date), format);
    }

    function _ConvertDateToServerTimeZone(date) {
        if (Ext.isEmpty(date.isoDateNoOffset)) {
            throw new Error("This date can't be displayed with no translation, the date must be created as an ISO-8601 String.");
        }
        return new Date(date.isoDateNoOffset); 
    }
    
    function _FormatDateNoTranslation(date, format) {
        return _FormatDate(_ConvertDateToServerTimeZone(date), format);
    }
    
    function _FormatTimeNoTranslation(date, format) {
        return _FormatTime(_ConvertDateToServerTimeZone(date), format);
    }

    /***************************************************************************************/


    /***************************************************************************************/

    return {
        ISO8601: FORMAT_ISO8601_WITH_OFFSET,
        ISO8601NoOffset: FORMAT_ISO8601_WITHOUT_OFFSET,
        formatDate: _FormatDate,
        formatDateInUserTimeZone: _FormatDateInUserTimeZone,
        formatDateNoTranslation: _FormatDateNoTranslation,
        formatTime: _FormatTime,
        formatTimeInUserTimeZone: _FormatTimeInUserTimeZone,
        formatTimeNoTranslation: _FormatTimeNoTranslation,
        formatTimeSpan: _FormatTimeSpan,
        formatCurrency: _FormatCurrency,
        formatNumber: _FormatNumber,
        parseDate: _ParseDate,
        parseDateTime: _ParseDateWithTime,
        parseTime: _ParseTime,
        parseCurrency: _ParseCurrency,
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

RP.util.logger = Ext.extend(Ext.util.Observable, {
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
    fields: [{
      name: 'message'
    }, {
      name: 'level'
    }, {
      name: 'cls'
    }]
  }),
  _currentFilter: undefined,
  
  constructor: function(config){
    this.addEvents("logtoservertoggled");
    RP.util.logger.superclass.constructor.call(this, config);
  },
  
  getStore: function(){
    return this._store;
  },
  
  log: function(message, level){
    if (level === undefined) {
      level = this._levels.INFO;
    }
    
    var dateLogged = new Date();
    var formattedMessage = String.format("<{0}::{1}.{2}> {3}", dateLogged.formatTime(RP.core.Formats.Time.Military), dateLogged.getSeconds(), dateLogged.getMilliseconds(), message);
    
    var newRecord = new this._store.recordType({
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
    
    this._store.add(newRecord);
    
    if (this._logToServer) {
      this._clientLogsBuffer.push(String.format("{0} {1}", level, formattedMessage));
    }
    
    if (this._store.getCount() > this._maxEntries) {      
      this._store.removeAt(0);
    }
    
    if (this._currentFilter && !suspended) {
      this._store.resumeEvents();
      this.filterBy(this._currentFilter);
    }
  },
  
  getLogClass: function(level){
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
  
  logTrace: function(message){
    this.log(message, this._levels.TRACE);
  },
  
  logDebug: function(message){
    this.log(message, this._levels.DEBUG);
  },
  
  logInfo: function(message){
    this.log(message, this._levels.INFO);
  },
  
  logWarning: function(message){
    this.log(message, this._levels.WARNING);
  },
  
  logError: function(e){
    var type, body;
    
    if (e.name === "RP.Exception") {
      type = "RP.Exception";
      
      var inner = e.innerError;
      if (inner) {
        body = String.format("message={0}; inner.name={1}; inner.message={2}; inner.statusText={3}", e.message, inner.name, inner.message, inner.statusText);
      }
      else {
        body = String.format("message={0}", e.message);
      }
    }
    else if (e.name === "Ext.Error") {
      type = "Ext.Error";
      body = e.message;
    }
    else if (e.statusText) {
      type = "AJAX Exception";
      body = String.format("status={0}; statusText={1}", e.status, e.statusText);
    }
    else {
      type = "JS Error";
      body = e.toString();
    }
    
    this.log(String.format("[{0}]: {1}", type, body), this._levels.ERROR);
  },
  
  logFatal: function(message){
    this.log(message, this._levels.FATAL);
  },
  
  /**
   * @method
   * Diagnostic function to see log contents.
   */
  print: function(){
    var texts = 'Number of entries: ' + this._store.getCount() + '\n';
    this._store.each(function(record){
      var entries = record.get('message');
      texts = texts + " \n " + entries;
    });
    return texts;
  },
  
  filterBy: function(levels){
    if (!Ext.isArray(levels)) {
      levels = [levels];
    }
    
    if (this._currentFilter) {
      delete this._currentFilter;
    }    
    this._currentFilter = levels;
    
    this._store.filterBy(function(record, id){
      if (levels.indexOf(record.get("level")) != -1) {
        return true;
      }
      return false;
    }, this);
  },
  
  _getLocale: function(){
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
  
  logEnvironment: function(){
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
    this.logInfo("[Logger] Ext version: " + Ext.version);
    
    var url = window.location.href.split("#");
    this.logInfo("[Logger] Location: " + url[0]);
  },
  
  clearAll: function(){
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
   * @method
   * Toggle the "log to server" function
   */
  toggleLogToServer: function(){
    this.setLogToServer(!this._logToServer, true);
  },
  
  /**
   * @method
   * Set the max number of log entries to display in the UI
   * @param {Number} value
   */
  setUIMaxEntries: function(value){
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
   * @method
   * Set/clear the "log to server" function
   * @param {boolean} flag
   * @param {boolean} notifyServer Flag to notify the server to turn on/off logging
   */
  setLogToServer: function(flag, notifyServer){
    if (this._logToServer) {
      this.flushClientLogs();
    }
    
    // If this is the first time turning on the server logs, copy the UI logs to server buffer...
    if (!this._clientLogsCaptured) {
      this._clientLogsCaptured = true;
      this._store.each(function(record){
        this._clientLogsBuffer.push(String.format("{0} {1}", record.get("level"), record.get("message")));
      }, this);
      
    }
    
    this._logToServer = flag;
    
    this.fireEvent("logtoservertoggled");
    
    if (notifyServer) {
      // Make AJAX request to tell REFS server we are logging to server or not
      Ext.Ajax.requestWithTextParams({
        url: this._clientLogPostURL,
        params: {
          action: "enable",
          content: flag ? "true" : "false"
        },
        scope: this,
        failure: function(){
          this.logError("[logger] Failed to enable/disable log to server");
        }
      });
    }
  },
  
  /**
   * @method
   * Gets "log to server" state
   */
  getIsLogToServer: function(){
    return this._logToServer;
  },
  
  /**
   * @method
   * Sets the interval to post client logs to server
   * @param {Number} intervalMS
   */
  setLogToServerInterval: function(intervalMS){
    this._clientLogInterval = intervalMS;
  },
  
  getLogToServerInterval: function() {
    return this._clientLogInterval;
  },
  
  /**
   * @method
   * Sets the URL to post client-side logs to server
   * @param {String} url
   */
  setLogToServerURL: function(url){
    this._clientLogPostURL = url;
  },
  
  /**
   * @method
   * Gets the URL to post client logs to the server
   */
  getLogToServerURL: function(){
    return this._clientLogPostURL;
  },
  
  /**
   * Flush the client logs to the server
   */
  flushClientLogs: function(){
    //alert(logger.print());
    if (!this._clientLogPostURL || this._clientLogsBuffer.length === 0) {
      return;
    }
    
    // Flush will run on a set time interval so if nothing is in
    // the buffer then we will skip the trying to dump it to the server
    if (this._clientLogsBuffer.length > 0) {
      this.logTrace("[logger] Dumping client logs to server.... ");
      // Make AJAX request...
      Ext.Ajax.requestWithTextParams({
        url: this._clientLogPostURL,
        params: {
          action: "post",
          content: this._clientLogsBuffer.join("\n") + "\n"
        },
        scope: this,
        success: function(){
          this._clientLogLastPost = new Date();
          this._clientLogsBuffer = [];
        },
        failure: function(){
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
  window.onunload = window.onunload.createSequence(function(){
    logger.flushClientLogs();
  });
}
else {
  window.onunload = function(){
    logger.flushClientLogs();
  };
}

// Setup a task to flush logs on a set interval when log to server is set
logger.serverLogTask = Ext.TaskMgr.start({
  run: function(){
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
    
    this.waitObjs.remove(obj);
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
   * to apply the scope to the handler, createDelegate must be used.  Two examples of add a list
   * of functions are,
   * 
   * [function() { // do some work here }]  - scope is irrelevant.
   * [function() { // do some work here }.createDelegate(scope) - scope is handled in delegate
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
      if (this.config.defer) {
        h.fnction.defer(1, h.scope);
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
 * @namespace RP.util
 * @class FunctionQueue
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
   * @method
   * Add to the queue
   * @param {Function} f Delegate to invoke
   */
  add: function(f) {
    this._q.push(f);    
  },
  
  /**
   * @method
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
        f(this._onItemSuccess.createDelegate(this), this._onItemFailure.createDelegate(this));
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
      this._executeNext.defer(10, this);
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
// ..\js\util\DefaultUrlTransform.js
//////////////////////
Ext.ns("RP.util");

RP.util.DefaultUrlTransform = function(url) {
  var prefix = RP.globals && RP.globals.paths && !Ext.isEmpty(RP.globals.paths.STATIC);
  
  if (url && prefix && url.indexOf("://") === -1 && url.indexOf(RP.globals.paths.STATIC) !== 0) {
    url = RP.globals.paths.STATIC + url;
  }
  
  if (url && url.indexOf("{client-mode}" >= 0)) {
    url = url.replace(/\{client-mode\}/g, RP.globals.CLIENTMODE);
  }
  
  return url;
};
//////////////////////
// ..\js\util\ScriptLoader.js
//////////////////////
Ext.ns("RP.util");

/**
 * @namespace RP.util
 * @class Task
 * @singleton
 */
RP.util.ScriptLoader = (function() {
    /**
     * @private
     * JS Loader to facilitate the serial download of sets of files through
     * the loadSerial method.
     */
    var serialJSLoader = null;
    
    /**
     * @private
     * Dictionary of URLs previously downloaded.
     */
    var downloaded = {};
    
    var waiter = new RP.util.Waiter({
        defer: false
    });
    
    var isDoneHandlerAdded = false;
    
    var initialize = function() {
        if (!isDoneHandlerAdded) {
            waiter.addHandler(function() {
                logger.logDebug("[ScriptLoader] Completed loading all files.");
            });
        }
        isDoneHandlerAdded = true;
    };
    
    /**
     * @private
     * Method for finding the set of unique URLs out of the
     * set of URLs passed and within the context of all URLs already
     * downloaded.
     *
     * @param {Array} urls The set of URLs to download.
     * @param {Function} urlTransform The function to provide a proper URL.
     */
    var gatherUniqueUrls = function(urls, urlTransform) {
        var uniqueUrls = [];
        
        Ext.each(urls, function(url) {
            url = urlTransform(url);
            
            if (Ext.isEmpty(downloaded[url])) {
                if (!Ext.isEmpty(url)) {
                    uniqueUrls.push(url);
                    downloaded[url] = 1;
                }
            }
        });
        
        return uniqueUrls;
    };
    
    return {
        /**
         * Serially load the set of URLs.  Each set of URLs passed will be
         * appended to the set of URLs already being downloaded to ensure that
         * each file in the set are loaded in proper sequence.
         *
         * @param {Array} urls The list of urls to download.
         * @param {Function} successFn Callback on success.
         * @param {Function} errorFn Callback on error.
         * @param {Function} urlTransform Transform function to manipulate the URL to proper path.
         */
        loadSerial: function(urls, successFn, errorFn, urlTransform) {
            initialize();
            if (!Ext.isArray(urls)) {
                urls = [urls];
            }
            
            urlTransform = urlTransform || RP.util.DefaultUrlTransform;
            
            var uniqueUrls = gatherUniqueUrls(urls, urlTransform);
            
            if (uniqueUrls.length > 0) {
                if (Ext.isFunction(successFn)) {
                    // Insert new handler to start of list.  This is to support recursive
                    // script loading (script A loads script B, which loads script C, etc.)
                    // We want handlers for the latest scripts to execute first...
                    waiter.insertHandler(successFn);
                }
                
                if (serialJSLoader === null) {
                    var uniqueId = Ext.id();
                    waiter.add(uniqueId);
                    
                    serialJSLoader = new RP.util.ScriptLoader.JSLoader({
                        urls: uniqueUrls,
                        completionCallback: function() {
                            // null check needed because serialJSLoader could have already been nulled out
                            // by a previous completionCallback
                            if (serialJSLoader !== null && serialJSLoader.urls.length === 0) {
                                // the following check is to stop a serialJSLoader's completion callback from
                                // executing multiple times.  the true fix for this is to set JSLoader's waiter.defer
                                // to be false, but this causing other problems then because other code was built
                                // around this inappropriate behavior.  this seems to be a temporary fix at least until
                                // we can spend time refactoring the taskflow engine and how files are downloaded.
                                if (waiter.waitObjs.indexOf(uniqueId) !== -1) {
                                    logger.logTrace("[ScriptLoader.loadSerial] Completed downloading all required resources.");
                                    waiter.remove(uniqueId);
                                    serialJSLoader = null;
                                }
                            }
                        }
                    });
                }
                else {
                    serialJSLoader.appendUrls(uniqueUrls);
                }
            }
        },
        
        /**
         * Downloads one or more script files and calls a delegate when all file(s) are loaded.
         *
         * @param {Array or String} urls
         * @param {Function} successFn
         * @param {Function} errorFn
         * @param {Boolean} prefixStaticURL Prefix relative URLs with RP.globals.paths.STATIC?
         */
        load: function(urls, successFn, errorFn, urlTransform) {
            initialize();
            if (!Ext.isArray(urls)) {
                urls = [urls];
            }
            
            urlTransform = urlTransform || RP.util.DefaultUrlTransform;
            
            // Get URLs that have not been downloaded...
            var uniqueUrls = gatherUniqueUrls(urls, urlTransform);
            
            if (uniqueUrls.length > 0) {
                if (Ext.isFunction(successFn)) {
                    // Insert new handler to start of list.  This is to support recursive
                    // script loading (script A loads script B, which loads script C, etc.)
                    // We want handlers for the latest scripts to execute first...
                    waiter.insertHandler(successFn);
                }
                
                var uniqueId = Ext.id();
                waiter.add(uniqueId);
                
                var jsLoader = new RP.util.ScriptLoader.JSLoader({
                    urls: uniqueUrls,
                    failureCallback: errorFn,
                    completionCallback: function() {
                        logger.logTrace("[ScriptLoader.load] Completed downloading all required resources.");
                        waiter.remove(uniqueId);
                    }
                });
            }
            else {
                successFn();
            }
        },
        
        /**
         * @method
         * Loads a library from the Stash
         * @param {String} name Name of library, e.g., "charting"
         * @param {String} version Version of library, e.g., "1.0.0"
         * @param {Function} successFn Delegate to invoke upon success
         * @param {Function} errorFn Delegate to invoke upon failure
         */
        loadStashLibrary: function(name, version, successFn, errorFn) {
            var incs; 
            try {
                incs = RP.stash.getLibraryIncludes(name, version); 
            }
            catch(error) {
                logger.logFatal("[ScriptLoader] " + error.message);
            }
            
            if (incs.length === 0) {
                logger.logDebug("[ScriptLoader] No items for library '" + name + "'");
                return;
            }
            
            logger.logDebug("[ScriptLoader] Loading library '" + name + "' version " + version);
            
            var queue = new RP.util.FunctionQueue();
            
            function createInlineScriptItem(script) {
                return function(succFn, errFn) {
                    try {
                        eval(script);
                        succFn();
                    } 
                    catch (e) {
                        errFn();
                        throw e;
                    }
                };
            }
            
            function createCssItem(url) {
                return function(succFn, errFn) {
                    RP.util.CSSLoader.load(url, false);
                    succFn();
                };
            }
            
            var loadFn = this.load.createDelegate(this);
            function createExternalScriptsItem(urls) {
                return function(succFn, errFn) {
                    loadFn(urls, succFn, errFn, false);
                };
            }
            
            // Process inline scripts and external CSS/scripts in blocks.
            for (var i = 0; i < incs.length; i++) {
                var inc = incs[i];
                
                if (inc.isType("s")) { // external javascript
                    // Group all external scripts together...
                    var urls = [];
                    
                    while (i < incs.length) {
                        urls.push(inc.getUrl());
                        i++;
                        
                        if ((i < incs.length) && (!incs[i].isType("s"))) {
                            i--;
                            break;
                        }
                        inc = incs[i];
                    }
                    
                    logger.logTrace("[ScriptLoader] Adding " + urls.length + " external scripts");
                    queue.add(createExternalScriptsItem(urls));
                }
                else if (inc.isType("c")) { // CSS
                    logger.logTrace("[ScriptLoader] Adding CSS: " + inc.getUrl());
                    queue.add(createCssItem(inc.getUrl()));
                }
                else if (inc.isType("j")) { // inline javascript
                    logger.logTrace("[ScriptLoader] Adding inline script: " + inc.getInlineCode());
                    queue.add(createInlineScriptItem(inc.getInlineCode()));
                }
                else {
                    logger.logFatal("[ScriptLoader] Unhandled include type from library: " + inc.getType());
                }
            }
            
            queue.execute(successFn, errorFn);
        },
        
        /**
         * Invokes the designated delegate after the script loading queue is empty; if there
         * are no items in the queue, the delegate is invoked right away.  This is useful
         * for stuff like ensuring message packs and other types of scripts that get loaded
         * dynamically by scripts loading during the page load process are finished before
         * we render the document.
         *
         * @member
         * @param {Object} fn
         */
        onReady: function(fn) {
            // serialJSLoader is set to null to ensure that it's state is reset so that a new
            // uniqueId is generated for the next call to loadSerial()...
            if (waiter.getCount() === 0) {
                logger.logTrace("[ScriptLoader] onReady - Called with no scripts in the queue.");
                serialJSLoader = null;
                fn();
                return;
            }
            
            logger.logTrace("[ScriptLoader] onReady - Waiting for scripts in queue to load...");
            waiter.addHandler(function() {
                logger.logTrace("[ScriptLoader] onReady - Scripts in queue loaded, so invoking handler...");
                serialJSLoader = null;
                fn();
            });
        },
        
        JSLoader: Ext.extend(Object, {
            /**
             * This list of URLs to load.
             * @cfg urls
             */
            urls: [],
            
            /**
             * @private
             * This tells us if files currently being downloaded
             */
            activeDownload: false,
            
            /**
             * Success call back to initiate additional processing after
             * a successfull load of a URL.
             * @cfg successCallback
             */
            successCallback: null,
            
            /**
             * Failure call back to handle an exception condition when
             * a URL fails to load.
             * @cfg failureCallback
             */
            failureCallback: null,
            
            /**
             * Call back function to indicate to the initiator that
             * the download process has completed.
             * @cfg completionCallback
             */
            completionCallback: Ext.emptyFn,
            
            scriptType: "text/javascript",
            scriptInsertionPoint: document.getElementsByTagName("SCRIPT")[0],
            
            /**
             * Create a JSLoader instance.
             *
             * @param {Object} urls The list of urls.
             * @param {Object} successCallback The success call back function.
             * @param {Object} failureCallback The failure call back function.
             */
            constructor: function(config) {
                Ext.apply(this, config);
                var waiter = new RP.util.Waiter();
                this.waiter = waiter;
                var urls = this.urls;
                
                this.successCallback = function(url) {
                    logger.logDebug("[ScriptLoader.JSLoader] Loaded " + url);
                    waiter.remove(url);
                };
                
                this.failureCallback = function(url) {
                    logger.logError("[ScriptLoader.JSLoader] Failed to download " + url);
                    
                    waiter.remove(url);
                    
                    if (Ext.isFunction(config.failureCallback)) {
                        config.failureCallback(url);
                    }
                };
                
                this._startDownloads();
            },
            
            /**
             * Appends urls to the list of the urls to the current instance.  If
             * the download process has ceased, the download process will be refired
             * and retrieval of the files will continue.
             *
             * @param {Array} urls The list of proper urls to download from the server.
             */
            appendUrls: function(urls) {
                logger.logTrace("Adding " + urls.length + " to the current download.");
                
                // append the urls to the list of urls to download.
                Ext.each(urls, function(url) {
                    this.urls.push(url);
                }, this);
                
                if (!this.activeDownload) {
                    logger.logTrace("Download process is not active.  Starting download.");
                    this._startDownloads();
                }
            },
            
            /**
             * @private
             * This method will start the download process.
             */
            _startDownloads: function() {
                // TODO Enable full async support to support Firefox's support
                // for async downloads and async downloads where the configuration
                // specifies async. 
                //if (config.async || Ext.isGecko3) {
                // download all the files without regard to the order required.
                // this is allowed for firefox (3.x) specially since it properly supports
                // synchronous execution of the code.
                //this._startAsynchronousDownloading();
                //}
                //else {
                // support synchronous downloading where dependencies
                // between files are important and should be maintained.
                this._startSynchronousDownloading();
                //}
            },
            
            /**
             * Inserts the script object into the proper location to complete
             * loading the resource into the browser.
             *
             * @param {Object} script The new script object.
             */
            _appendScript: function(script) {
                var parentNode = this.scriptInsertionPoint.parentNode;
                parentNode.insertBefore(script, this.scriptInsertionPoint);
            },
            
            /**
             * Attach the error handler callback function to the script object.
             * @method _attachOnErrorHandler
             * @private
             * @param {Object} script
             */
            _attachOnErrorHandler: function(script) {
                var failureCallback = this.failureCallback;
                var url = script.src;
                
                script.onerror = function() {
                    logger.logFatal("[ScriptLoader.JSLoader] Failed to load script " + url);
                    failureCallback(url);
                };
            },
            /**
             * Attach the success handler callback function to the script object.
             * @method _attachOnSuccessHandler
             * @private
             * @param {Object} script
             */
            _attachOnSuccessHandler: function(script) {
                var successCallback = this.successCallback;
                var url = script.src;
                
                if (Ext.isIE) { // Internet Explorer only...
                    script.onreadystatechange = function() {
                        if (this.readyState === "complete" || this.readyState === "loaded") {
                            successCallback(url);
                        }
                    };
                }
                else { // all other browsers
                    var fn = function() {
                        successCallback(url);
                    };
                    script.onload = fn;
                }
            },
            
            /**
             * Retrieves a JS file based on the passed URL. On successfully loading
             * the file from the server, the success callback function is executed.
             * In the event on an error the failure callback function is executed.
             *
             * @method _download
             * @private
             * @param {String} url The url of the script to load.
             * @param {Function} successCallback The callback function to execute on success.
             * @param {Function} failureCallback The callback function to execute on failure.
             */
            _download: function(url) {
                logger.logTrace("[ScriptLoader.JSLoader] Requesting " + url);
                
                var script = document.createElement("script");
                script.type = this.scriptType;
                script.src = url;
                
                this.waiter.add(script.src);
                
                this._attachOnErrorHandler(script);
                this._attachOnSuccessHandler(script);
                
                // append the script to the document and begin the loading process.
                logger.logTrace("[ScriptLoader.JSLoader] Starting load of JS URL, " + url);
                this._appendScript(script);
            },
            
            /**
             * Downloads the list of urls passed in.
             *
             * Processing should occur in this fashion.
             * 1 or more urls to be downloaded -
             *   - Start downloading the first URL in the list.
             *   - Add a handler on the Waiter to execute again with the first URL shift off.
             * 0 urls, return with no work to be done.
             *
             * @method _startSynchronousDownloading
             */
            _startSynchronousDownloading: function() {
                var url = this.urls.shift();
                if (this.urls.length > 0) {
                    logger.logTrace("[ScriptLoader.JSLoader] Queuing " + this.urls.length + " urls...");
                    
                    this.waiter.addHandler(function() {
                        logger.logTrace("[ScriptLoader.JSLoader] Previous request finished, triggering download of next URL.");
                        this._startSynchronousDownloading();
                    }, this);
                    this.activeDownload = true;
                }
                else {
                    this.waiter.addHandler(function() {
                        logger.logTrace("[ScriptLoader.JSLoader] Completed loading list of passed urls");
                        this.completionCallback();
                    }, this);
                    this.activeDownload = false;
                }
                
                logger.logTrace("[ScriptLoader.JSLoader] Loading script url " + url + "...");
                this._download(url);
            },
            
            /**
             * Download the list of files passed in asynchronously, or without chaining one after the
             * other.
             * @method _startAsynchronousDownloading
             */
            _startAsynchronousDownloading: function() {
                this.waiter.add(this.urls);
                
                Ext.each(this.urls, function(url) {
                    logger.logTrace("[ScriptLoader.JSLoader] Loading script url " + url + "...");
                    this._download(url);
                }, this);
            }
        })
    };
})();
//////////////////////
// ..\js\util\SourceScriptLoader.js
//////////////////////
Ext.ns("RP.util");

RP.util.SourceScriptLoader = function(){
  var srcServletPath = "src/{0}";
  
  /**
   * @private
   * A dummy URL transform method for the ScriptLoader.load method.
   * 
   * @param {String} url The url.
   * @return {String} The url, unaltered.
   */
  var scriptLoaderUrlTransform = function(url) {
      return url;
  };
  
  /**
   * @private
   * The URL transformation method to define the correct URL for
   * download.
   * 
   * @param {String} url The raw file name.
   * @return {String} The transformed url.
   */
  var urlTransform = function(url) {
    var prefix = RP.globals && RP.globals.paths && !Ext.isEmpty(RP.globals.paths.STATIC);
    var staticPath = RP.globals.paths.STATIC.replace(/web\/?$/, "");
    
    if (prefix && (url.indexOf("://") < 0)) {
      url = staticPath + url;
    }
    
    return url;
  };
  
  return {
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
        srcUrls.push(urlTransform(String.format(srcServletPath, url)));
      }, this);
      
      RP.util.ScriptLoader.loadSerial(srcUrls, Ext.emptyFn, Ext.emptyFn, scriptLoaderUrlTransform);
    }
  };
}();
