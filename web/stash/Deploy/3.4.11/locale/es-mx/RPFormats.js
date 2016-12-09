Ext.ns("RP.core");

(function() {
    //These are used to set the separators and currency signs for RP.core.Formats.
    //UNICORN: Use the Ext.util.Format values.
    //DONKEY: Use a string with the correct symbol.
    var thousandSeparator = ',';
    var decimalSeparator = '.';
    var currencySign = '\u0024'; // Mexican Dollar Sign
    /**
     * @class RP.core.Formats
     */
    RP.core.Formats = {
        Date: {
            Long: {
                type: "date",
                order: "m,d,y",
                formatstring: "MM'/'dd'/'yyyy hh':'mm tt"
            },
            Default: {
                type: "date",
                order: "m,d,y",
                formatstring: "MM'/'dd'/'yyyy"
            },
            Medium: {
                type: "date",
                order: "m,d,y",
                formatstring: "MM'/'dd'/'yyyy"
            },
            Short: {
                type: "date",
                order: "m,d,y",
                formatstring: "M'/'d'/'yy"
            },
            MonthYear: {
                type: "date",
                order: "m,y",
                formatstring: "MMM yyyy"
            },
            FullMonthYear: {
                type: "date",
                order: "m,y",
                formatstring: "MMMM yyyy"
            },
            Month: {
                type: "date",
                order: "m",
                formatstring: "MMM"
            },
            FullMonth: {
                type: "date",
                order: "m",
                formatstring: "MMMM"
            },
            MonthDate: {
                type: "date",
                order: "m,d",
                formatstring: "MM'/'dd"
            },
            Weekday: {
                type: "date",
                order: "d",
                formatstring: "dddd"
            },
            TimeStamp: {
                type: "date",
                order: "m,d,y",
                formatstring: "MM'/'dd'/'yyyy hh':'mm':'ss tt"
            },
            FullDateOnly: {
                type: "date",
                order: "m,d,y",
                formatstring: "dddd', 'MMMM dd', 'yyyy"
            },
            FullDateWithoutDayName: {
                type: "date",
                order: "m,d,y",
                formatstring: "MMMM dd, yyyy"
            },
            LongYear: {
                type: "date",
                order: "y",
                formatstring: "yyyy"
            },
            MonthDateDay: {
                type: "date",
                order: "m,d",
                formatstring: "MM'/'dd ddd"
            },
            ShortWeekday: {
                type: "date",
                order: "d",
                formatstring: "ddd"
            },
            FullDateTime: {
                type: "date",
                order: "m,d,y",
                formatstring: "dddd', 'MMMM dd', 'yyyy hh':'mm tt"
            },
            POSDate: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'-'MM'-'dd'T'hh':'mm':'ss"
            },
            WeekdayDate: {
                type: "date",
                order: "d, m",
                formatstring: "ddd, MM/dd"
            },
            ShortDateTime: {
                type: "date",
                order: "m,d,y",
                formatstring: "MMM dd', 'hh':'mm tt"
            },
            MediumDateExp: {
                type: "date",
                order: "m,d",
                formatstring: "MMM dd"
            }
        },
        Time: {
            Default: {
                type: "time",
                formatstring: "hh':'mm tt"
            },
            Short: {
                type: "time",
                formatstring: "h':'mm tt"
            },
            Long: {
                type: "time",
                formatstring: "hh':'mm':'ss tt"
            },
            Military: {
                type: "time",
                formatstring: "HH':'mm"
            },
            MilitaryWithSeconds: {
                type: 'time',
                formatstring: "HH':'mm':'ss"
            },
            HourOnly: {
                type: "time",
                formatstring: "h t"
            }
        },
        Number: {
            Default: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: "()"
            },
            HighPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "4",
                negsign: "()",
                trimzero: "n"
            },
            MediumPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: "()",
                trimzero: "n"
            },
            MediumLowPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "1",
                negsign: "()",
                trimzero: "n"
            },
            LowPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "0",
                negsign: "()",
                trimzero: "n"
            },
            ScheduleTotal: {
                type: "number",
                thousand: "",
                dec: decimalSeparator,
                scale: "2",
                negsign: "()",
                trimzero: "y"
            },
            MediumPrecisionRaw: {
                type: "number",
                thousand: "",
                dec: decimalSeparator,
                scale: "2",
                negsign: "()",
                trimzero: "n"
            }
        },
        Currency: {
            Default: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: "()",
                trailchar: "",
                trimzero: "y"
            },
            HighPrecision: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "4",
                negsign: "()",
                trailchar: "",
                trimzero: "n"
            },
            MediumPrecision: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: "()",
                trailchar: "",
                trimzero: "n"
            },
            LowPrecision: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "0",
                negsign: "()",
                trailchar: "",
                trimzero: "n"
            }
        },
        Percent: {
            HighPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "4",
                negsign: "()",
                trailchar: "%",
                trimzero: "n"
            },
            MediumPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: "()",
                trailchar: "%",
                trimzero: "n"
            },
            MediumLowPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "1",
                negsign: "()",
                trailchar: "%",
                trimzero: "n"
            },
            LowPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "0",
                negsign: "()",
                trailchar: "%",
                trimzero: "n"
            }
        },
        TimeSpan: {
            Short: {
                type: "timespan",
                formatstring: "{h}:{mm}",
                template: undefined // compiled Ext template
            },
            Medium: {
                type: "timespan",
                formatstring: "{h}:{mm}:{ss}",
                template: undefined // compiled Ext template
            },
            Long: {
                type: "timespan",
                formatstring: "{hh}:{mm}:{ss}",
                template: undefined // compiled Ext template
            }
        }
    };
    
    /**
     * @class RP.core.FormatConstants
     * @singleton
     */
    RP.core.FormatConstants = {
        Weekdays: (function() { var array = []; Ext.each(Date.dayNames, function(item, index) { array[index] = Date.getShortDayName(index); }); return array; })(),
        FullWeekdays: Date.dayNames,
        Months: (function() { var array = []; Ext.each(Date.monthNames, function(item, index) { array[index] = Date.getShortMonthName(index); }); return array; })(),
        FullMonths: Date.monthNames
    };
    
    Ext.apply(Ext.form.DateField.prototype, {
        /* Fix for RPWEB-5148 */
        altFormats: "m/d/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d|n-j|n/j",
        format: "m/d/Y"
    });
    
    if (Ext.grid.PropertyColumnModel) {
        Ext.apply(Ext.grid.PropertyColumnModel.prototype, {
            dateFormat: "m/j/Y",
            trueText: "true",
            falseText: "false"
        });
    }
    
    if (Ext.grid.BooleanColumn) {
        Ext.apply(Ext.grid.BooleanColumn.prototype, {
            trueText: "true",
            falseText: "false",
            undefinedText: '&#160;'
        });
    }
    
    if (Ext.grid.NumberColumn) {
        Ext.apply(Ext.grid.NumberColumn.prototype, {
            format: '0,000.00'
        });
    }
    
    if (Ext.grid.DateColumn) {
        Ext.apply(Ext.grid.DateColumn.prototype, {
            format: 'm/d/Y'
        });
    }
    
    if (Ext.DatePicker) {
        Ext.apply(Ext.DatePicker.prototype, {
            format: "m/d/Y"
        });
    }
    
    if (Ext.util.Format) {
        Ext.util.Format.date = function(v, format) {
            if (!v) 
                return "";
            if (!(v instanceof Date)) 
                v = new Date(Date.parse(v));
            return v.dateFormat(format || "m/d/Y");
        };
    }
    
    RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);
})();