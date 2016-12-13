﻿
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: { Long:        { type: "date",
                                      order: "d,m,y",
                                      formatstring: "dd'/'MM'/'yyyy HH':'mm"},
                       Default:     { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dd'/'MM'/'yyyy"},
                       Medium:      { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dd'/'MM'/'yyyy"},
                       Short:       { type: "date",
                                          order:"d,m,y",
                                          formatstring:"d'/'M'/'yy"},
                       MonthYear:       { type: "date",
                                          order:"m,y",
                                          formatstring:"MMM yyyy"},
                       FullMonthYear:   { type: "date",
                                          order:"m,y",
                                          formatstring:"MMMM yyyy"},
                       Month:           { type: "date",
                                          order: "m",
                                          formatstring:"MMM"},
                       FullMonth:       { type: "date",
                                          order: "m",
                                          formatstring:"MMMM"},
                       MonthDate:       { type: "date",
                                          order:"d,m",
                                          formatstring:"dd'/'MM"},
                       Weekday:         { type: "date",
                                          order:"d",
                                          formatstring:"dddd"},
                       TimeStamp:       { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dd'/'MM'/'yyyy HH':'mm':'ss"},
                       FullDateOnly:    { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dddd', 'dd 'de 'MMMM' de 'yyyy"},
                       FullDateWithoutDayName:    { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dd MMMM, yyyy"},
                       LongYear:        { type: "date",
                                          order:"y",
                                          formatstring:"yyyy"},
                       MonthDateDay:    { type: "date",
                                          order:"d,m",
                                          formatstring:"dd'/'MM ddd"},
                       ShortWeekday:    { type: "date",
                                          order:"d",
                                          formatstring:"ddd"},
                       FullDateTime:    { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dddd', 'dd MMMM', 'yyyy HH':'mm"},
                       POSDate:         { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd'T'HH':'mm':'ss"},
                       WeekdayDate:     { type: "date",
                                          order:"d, m",
                                          formatstring:"ddd, dd/MM"},
                       ShortDateTime:   { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dd MMM', 'HH':'mm"},
                       MediumDateExp:   { type: "date",
                                          order:"d,m",
                                          formatstring:"dd MMM"}
                     },
               Time: { Default:     { type: "time",
                                          formatstring: "HH':'mm"},
                       Short:       { type: "time",
                                          formatstring: "H':'mm"},
                       Long:        { type: "time",
                                          formatstring: "HH':'mm':'ss"},
                       Military:    { type: "time",
                                          formatstring:"HH':'mm"},
                       MilitaryWithSeconds: { type: 'time', 
                                                  formatstring: "HH':'mm':'ss" },
                       HourOnly:    { type: "time",
                                          formatstring: "H"}
                     },
               Number: { Default:   { type:"number",
                                            thousand:".",
                                            dec:",",
                                            scale:"2",
                                            negsign:"()"},
                         HighPrecision:{ type:"number",
                                            thousand:".",
                                            dec:",",
                                            scale:"4",
                                            negsign:"()",
                                            trimzero:"n"},
                         MediumPrecision: { type:"number",
                                            thousand:".",
                                            dec:",",
                                            scale:"2",
                                            negsign:"()",
                                            trimzero:"n"},
                         MediumLowPrecision: { type:"number",
                                            thousand:".",
                                            dec:",",
                                            scale:"1",
                                            negsign:"()",
                                            trimzero:"n"},
                         LowPrecision: { type:"number",
                                            thousand:".",
                                            dec:",",
                                            scale:"0",
                                            negsign: "()",
                                            trimzero:"n"},
                         ScheduleTotal: { type:"number",
                                            thousand:"",
                                            dec:",",
                                            scale:"2",
                                            negsign: "()",
                                            trimzero:"y"},
                         MediumPrecisionRaw: { type:"number",
                                            thousand:"",
                                            dec:",",
                                            scale:"2",
                                            negsign: "()",
                                            trimzero:"n"}
                     },
               Currency: { Default:         { type:"currency",
                                              leadchar:"R$",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency",
                                              leadchar:"R$",
                                              thousand:".",
                                              dec:",",
                                              scale:"4",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency",
                                              leadchar:"R$",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency",
                                              leadchar:"R$",
                                              thousand:".",
                                              dec:",",
                                              scale:"0",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"}
                     },
               Percent: { HighPrecision:  { type:"number",
                                            leadchar:"",
                                            thousand:".",
                                            dec:",",
                                            scale:"4",
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"},
                         MediumPrecision: { type:"number",
                                            leadchar:"",
                                            thousand:".",
                                            dec:",",
                                            scale:"2",
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"},
                         MediumLowPrecision: { type:"number", 
                                            leadchar:"",
                                            thousand:".", 
                                            dec:",", 
                                            scale:"1", 
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"},
                         LowPrecision: { type:"number",
                                            leadchar:"",
                                            thousand:".",
                                            dec:",",
                                            scale:"0",
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"}
                     },
               TimeSpan: {
                 Short: {
                   type: "timespan",
                   formatstring: "{H}:{mm}",
                   template: undefined    // compiled Ext template
                 },
                 Medium: {
                   type: "timespan",
                   formatstring: "{H}:{mm}:{ss}",
                   template: undefined    // compiled Ext template
                 },
                 Long: {
                   type: "timespan",
                   formatstring: "{HH}:{mm}:{ss}",
                   template: undefined    // compiled Ext template
                 } /*,
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
        Weekdays: (function() { var array = []; Ext.each(Date.dayNames, function(item, index) { array[index] = Date.getShortDayName(index); }); return array; })(),
        FullWeekdays: Date.dayNames,
        Months: (function() { var array = []; Ext.each(Date.monthNames, function(item, index) { array[index] = Date.getShortMonthName(index); }); return array; })(),
        FullMonths: Date.monthNames
    };

/* Fix Ext's override to the date format */
Ext.apply(Ext.form.DateField.prototype, {
    format: "d/m/Y"
});

Ext.apply(Ext.DatePicker.prototype, {
    format: "d/m/Y"
});

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);