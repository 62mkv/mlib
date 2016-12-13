﻿
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: { Long:       { type: "date",
                                          order: "y,m,d",
                                          formatstring: "yyyy'-'MM'-'dd hh':'mm tt"},
                       Default:         { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd"},
                       Medium:          { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd"},
                       Short:           { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yy'-'M'-'d"},
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
                                          order:"m,d",
                                          formatstring:"MM'/'dd"},
                       Weekday:         { type: "date",
                                          order:"d",
                                          formatstring:"dddd"},
                       TimeStamp:       { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd hh':'mm':'ss tt"},
                       FullDateOnly:    { type: "date",
                                          order:"m,d,y",
                                          formatstring:"dddd', 'MMMM dd', 'yyyy"},
                       FullDateWithoutDayName:    { type: "date",
                                          order:"m,d,y",
                                          formatstring:"MMMM dd, yyyy"},
                       LongYear:        { type: "date",
                                          order:"y",
                                          formatstring:"yyyy"},
                       MonthDateDay:    { type: "date",
                                          order:"m,d",
                                          formatstring:"MM'/'dd ddd"},
                       ShortWeekday:    { type: "date",
                                          order:"d",
                                          formatstring:"ddd"},
                       FullDateTime:    { type: "date",
                                          order:"m,d,y",
                                          formatstring:"dddd', 'MMMM dd', 'yyyy hh':'mm tt"},
                       POSDate:         { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd'T'hh':'mm':'ss"},
                       WeekdayDate:     { type: "date",
                                          order:"d, m",
                                          formatstring:"ddd, MM/dd"},
                       ShortDateTime:   { type: "date",
                                          order:"m,d,y",
                                          formatstring:"MMM dd', 'hh':'mm tt"},
                       MediumDateExp:   { type: "date",
                                          order:"m,d",
                                          formatstring:"MMM dd"}
                     },
               Time: { Default:     { type: "time",
                                          formatstring: "hh':'mm tt"},
                       Short:       { type: "time",
                                          formatstring: "h':'mm tt"},
                       Long:        { type: "time",
                                          formatstring: "hh':'mm':'ss tt"},
                       Military:    { type: "time",
                                          formatstring:"HH':'mm"},
                       MilitaryWithSeconds: { type: 'time', 
                                                  formatstring: "HH':'mm':'ss" },
                       HourOnly:    { type: "time",
                                          formatstring: "h t"}
                     },
               Number: { Default:   { type:"number",
                                            thousand:",",
                                            dec:".",
                                            scale:"2",
                                            negsign:"()"},
                         HighPrecision:{ type:"number",
                                            thousand:",",
                                            dec:".",
                                            scale:"4",
                                            negsign:"()",
                                            trimzero:"n"},
                         MediumPrecision: { type:"number",
                                            thousand:",",
                                            dec:".",
                                            scale:"2",
                                            negsign:"()",
                                            trimzero:"n"},
                         MediumLowPrecision: { type:"number",
                                            thousand:",",
                                            dec:".",
                                            scale:"1",
                                            negsign:"()",
                                            trimzero:"n"},
                         LowPrecision: { type:"number",
                                            thousand:",",
                                            dec:".",
                                            scale:"0",
                                            negsign: "()",
                                            trimzero:"n"},
                         ScheduleTotal: { type:"number",
                                            thousand:"",
                                            dec:".",
                                            scale:"2",
                                            negsign: "()",
                                            trimzero:"y"},
                         MediumPrecisionRaw: { type:"number",
                                            thousand:"",
                                            dec:".",
                                            scale:"2",
                                            negsign: "()",
                                            trimzero:"n"}
                     },
               Currency: { Default:         { type:"currency",
                                              leadchar:"$",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency",
                                              leadchar:"$",
                                              thousand:".",
                                              dec:",",
                                              scale:"4",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency",
                                              leadchar:"$",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency",
                                              leadchar:"$",
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
    Weekdays: ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"],
    FullWeekdays: ["Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"],
    Months: ["Janv", "Févr", "Mars", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"],
    FullMonths: ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"]
};


/* Fix Ext's override to the date format */
Ext.apply(Ext.form.DateField.prototype, {
    format: "Y-m-d",
    /* Fix for RPWEB-5148 */
    altFormats: "Y-n-j|y-n-j|y-m-j|y-n-d|Y-m-j|Y-n-d|y-m-d|Y-m-d|m-d|md|ymd|Ymd|d|Y-m-d|n-j"
});

Ext.apply(Ext.form.DateField.prototype, {
    format: "Y-m-d"
});

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);