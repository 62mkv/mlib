
Ext.ns("RP.core");

(function() {
    /* Fix Ext's override to the date format */
    Ext.apply(Ext.form.DateField.prototype, {
        /* Fix for RPWEB-4949, until Sencha fixes it. */
        altFormats: "d/m/Y|j/n/Y|j/n/y|j/m/y|d/n/y|j/m/Y|d/n/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|d|Y-d-m|j-n|j/n",
        format: "d/m/Y"
    });
    
    Ext.apply(Ext.DatePicker.prototype, {
        format: "d/m/Y"
    });
    
    //These are used to set the separators and currency signs for RP.core.Formats.
    //UNICORN: Use the Ext.util.Format values.
    //DONKEY: Use a string with the correct symbol.
    var thousandSeparator = '.';
    var decimalSeparator = ',';
    var currencySign = "€"; 
    
    /**
     * @class RP.core.Formats
     */
    RP.core.Formats = {
    Date: {
        Long: {
            type: "date",
            order: "d,m,y",
            formatstring: "dd'/'MM'/'yyyy HH':'mm"
        },
        Default: {
            type: "date",
            order: "d,m,y",
            formatstring: "dd'/'MM'/'yyyy"
        },
        Medium: {
            type: "date",
            order: "d,m,y",
            formatstring: "dd'/'MM'/'yyyy"
        },
        Short: {
            type: "date",
            order: "d,m,y",
            formatstring: "d'/'M'/'yy"
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
            order: "d,m",
            formatstring: "dd'/'MM"
        },
        Weekday: {
            type: "date",
            order: "d",
            formatstring: "dddd"
        },
        TimeStamp: {
            type: "date",
            order: "d,m,y",
            formatstring: "dd'/'MM'/'yyyy HH':'mm':'ss"
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
            formatstring: "dddd', 'MMMM dd', 'yyyy HH':'mm"
        },
        POSDate: {
            type: "date",
            order: "y,m,d",
            formatstring: "yyyy'-'MM'-'dd'T'HH':'mm':'ss"
        },
        WeekdayDate: {
            type: "date",
            order: "d, m",
            formatstring: "ddd, MM/dd"
        },
        ShortDateTime: {
            type: "date",
            order: "m,d,y",
            formatstring: "MMM dd', 'HH':'mm"
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
            formatstring: "HH':'mm"
        },
        Short: {
            type: "time",
            formatstring: "H':'mm"
        },
        Long: {
            type: "time",
            formatstring: "HH':'mm':'ss"
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
            formatstring: "H"
        }
    },
    Number: {
        Default: {
            type: "number",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()"
        },
        HighPrecision: {
            type: "number",
            thousand: ".",
            dec: ",",
            scale: "4",
            negsign: "()",
            trimzero: "n"
        },
        MediumPrecision: {
            type: "number",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()",
            trimzero: "n"
        },
        MediumLowPrecision: {
            type: "number",
            thousand: ".",
            dec: ",",
            scale: "1",
            negsign: "()",
            trimzero: "n"
        },
        LowPrecision: {
            type: "number",
            thousand: ".",
            dec: ",",
            scale: "0",
            negsign: "()",
            trimzero: "n"
        },
        ScheduleTotal: {
            type: "number",
            thousand: "",
            dec: ",",
            scale: "2",
            negsign: "()",
            trimzero: "y"
        },
        MediumPrecisionRaw: {
            type: "number",
            thousand: "",
            dec: ",",
            scale: "2",
            negsign: "()",
            trimzero: "n"
        }
    },
    Currency: {
        Default: {
            type: "currency",
            leadchar: "€",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()",
            trailchar: "",
            trimzero: "y"
        },
        HighPrecision: {
            type: "currency",
            leadchar: "€",
            thousand: ".",
            dec: ",",
            scale: "4",
            negsign: "()",
            trailchar: "",
            trimzero: "n"
        },
        MediumPrecision: {
            type: "currency",
            leadchar: "€",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()",
            trailchar: "",
            trimzero: "n"
        },
        LowPrecision: {
            type: "currency",
            leadchar: "€",
            thousand: ".",
            dec: ",",
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
            thousand: ".",
            dec: ",",
            scale: "4",
            negsign: "()",
            trailchar: "%",
            trimzero: "n"
        },
        MediumPrecision: {
            type: "number",
            leadchar: "",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()",
            trailchar: "%",
            trimzero: "n"
        },
        MediumLowPrecision: {
            type: "number",
            leadchar: "",
            thousand: ".",
            dec: ",",
            scale: "1",
            negsign: "()",
            trailchar: "%",
            trimzero: "n"
        },
        LowPrecision: {
            type: "number",
            leadchar: "",
            thousand: ".",
            dec: ",",
            scale: "0",
            negsign: "()",
            trailchar: "%",
            trimzero: "n"
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
    
    RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);
})();
