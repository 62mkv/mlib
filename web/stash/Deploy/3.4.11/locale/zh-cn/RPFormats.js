
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = {
    Date: {
        Long: {
            type: "date",
            order: "y,m,d",
            formatstring: "yyyy年MM月dd日 HH':'mm"
        },
        Default: {
            type: "date",
            order: "y,m,d",
            formatstring: "yyyy年MM月dd日"
        },
        Medium: {
            type: "date",
            order: "y,m,d",
            formatstring: "yyyy年MM月dd日"
        },
        Short: {
            type: "date",
            order: "y,m,d",
            formatstring: "yy年M月d日"
        },
        MonthYear: {
            type: "date",
            order: "y,m",
            formatstring: "yyyy MMM"
        },
        FullMonthYear: {
            type: "date",
            order: "y,m",
            formatstring: "yyyy MMMM"
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
            formatstring: "MM月dd日"
        },
        Weekday: {
            type: "date",
            order: "d",
            formatstring: "dddd"
        },
        TimeStamp: {
            type: "date",
            order: "y,m,d",
            formatstring: "yyyy年MM月dd日 HH':'mm':'ss"
        },
        FullDateOnly: {
            type: "date",
            order: "m,d,y",
            formatstring: "dddd', 'MMMM dd', 'yyyy"
        },
        FullDateWithoutDayName: {
            type: "date",
            order: "y,m,d",
            formatstring: "yyyy年MM月dd日"
        },
        LongYear: {
            type: "date",
            order: "y",
            formatstring: "yyyy"
        },
        MonthDateDay: {
            type: "date",
            order: "m,d",
            formatstring: "MM月dd日 ddd"
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
            formatstring: "yyyy年MM月dd日'T'HH':'mm':'ss"
        },
        WeekdayDate: {
            type: "date",
            order: "m,d",
            formatstring: "ddd, MM月dd日"
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
            leadchar: "\u00a5",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()",
            trailchar: "",
            trimzero: "y"
        },
        HighPrecision: {
            type: "currency",
            leadchar: "\u00a5",
            thousand: ".",
            dec: ",",
            scale: "4",
            negsign: "()",
            trailchar: "",
            trimzero: "n"
        },
        MediumPrecision: {
            type: "currency",
            leadchar: "\u00a5",
            thousand: ".",
            dec: ",",
            scale: "2",
            negsign: "()",
            trailchar: "",
            trimzero: "n"
        },
        LowPrecision: {
            type: "currency",
            leadchar: "\u00a5",
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
    },
    TimeSpan: {
        Short: {
            type: "timespan",
            formatstring: "{H}:{mm}",
            template: undefined // compiled Ext template
        },
        Medium: {
            type: "timespan",
            formatstring: "{H}:{mm}:{ss}",
            template: undefined // compiled Ext template
        },
        Long: {
            type: "timespan",
            formatstring: "{HH}:{mm}:{ss}",
            template: undefined // compiled Ext template
        }
        /*,
         NarrativeMedium: {  // e.g., "2h, 30m"
         }
         NarrativeLong: {  // e.g., "2 hours, 30 minutes"
         } */
    }
};

/**
 * @namespace RP.core
 * @class FormatConstants
 * @singleton
 */
RP.core.FormatConstants = {
    Weekdays: ["日", "一", "二", "三", "四", "五", "六"],
    FullWeekdays: ["日", "一", "二", "三", "四", "五", "六"],
    Months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
    FullMonths: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"]
};

/* Fix Ext's override to the date format */
Ext.apply(Ext.form.DateField.prototype, {
    format: "Y年m月d日"
});

Ext.apply(Ext.DatePicker.prototype, {
    format: "Y年m月d日"
});

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);
