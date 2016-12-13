﻿
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: { Long:        { type: "date",
                                      order: "m,d,y",
                                      formatstring: "dd'/'MM'/'yyyy HH':'mm"},
                       Default:     { type: "date",
                                          order:"m,d,y",
                                          formatstring:"dd'/'MM'/'yyyy"},
                       Medium:      { type: "date",
                                          order:"m,d,y",
                                          formatstring:"dd'/'MM'/'yyyy"},
                       Short:       { type: "date",
                                          order:"m,d,y",
                                          formatstring:"M'/'d'/'yy"},
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
                                          order:"m,d,y",
                                          formatstring:"dd'/'MM'/'yyyy HH':'mm':'ss"},
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
                                          formatstring:"dddd', 'MMMM dd', 'yyyy HH':'mm"},
                       POSDate:         { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd'T'HH':'mm':'ss"},
                       WeekdayDate:     { type: "date",
                                          order:"d, m",
                                          formatstring:"ddd, MM/dd"},
                       ShortDateTime:   { type: "date",
                                          order:"m,d,y",
                                          formatstring:"MMM dd', 'HH':'mm"},
                       MediumDateExp:   { type: "date",
                                          order:"m,d",
                                          formatstring:"MMM dd"}
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
                                              leadchar:"€",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency",
                                              leadchar:"€",
                                              thousand:".",
                                              dec:",",
                                              scale:"4",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency",
                                              leadchar:"€",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency",
                                              leadchar:"€",
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
RP.core.FormatConstants =  {Weekdays:["Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab"],
                        FullWeekdays:["Domenica", "Luned\u00EC", "Marted\u00EC", "Mercoled\u00EC", "Gioved\u00EC", "Venerd\u00EC", "Sabato"],
                        Months:["Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"],
                        FullMonths:["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"]
};


/* Fix Ext's override to the date format */
Ext.apply(Ext.form.DateField.prototype, {
    format: "d/m/Y",
    /* Fix for RPWEB-5148 */
    altFormats: "d/m/Y|j/n/Y|j/n/y|j/m/y|d/n/y|j/m/Y|d/n/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|d|Y-d-m|j-n|j/n"
});

Ext.apply(Ext.DatePicker.prototype, {
    format: "d/m/Y"
});


RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);