
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: { Long:        { type: "date",
                                      order: "m,d,y",
                                      formatstring: "MM'/'dd'/'yyyy hh':'mm tt"},
                       Default:     { type: "date",
                                          order:"m,d,y",
                                          formatstring:"MM'/'dd'/'yyyy"},
                       Medium:      { type: "date",
                                          order:"m,d,y",
                                          formatstring:"MM'/'dd'/'yyyy"},
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
                                          formatstring:"MM'/'dd'/'yyyy hh':'mm':'ss tt"},
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
                                              leadchar:"B/.",
                                              thousand:",",
                                              dec:".",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency",
                                              leadchar:"B/.",
                                              thousand:",",
                                              dec:".",
                                              scale:"4",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency",
                                              leadchar:"B/.",
                                              thousand:",",
                                              dec:".",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency",
                                              leadchar:"B/.",
                                              thousand:",",
                                              dec:".",
                                              scale:"0",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"}
                     },
               Percent: { HighPrecision:  { type:"number",
                                            leadchar:"",
                                            thousand:",",
                                            dec:".",
                                            scale:"4",
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"},
                         MediumPrecision: { type:"number",
                                            leadchar:"",
                                            thousand:",",
                                            dec:".",
                                            scale:"2",
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"},
                         MediumLowPrecision: { type:"number", 
                                            leadchar:"",
                                            thousand:",", 
                                            dec:".", 
                                            scale:"1", 
                                            negsign:"()",
                                            trailchar:"%",
                                            trimzero:"n"},
                         LowPrecision: { type:"number",
                                            leadchar:"",
                                            thousand:",",
                                            dec:".",
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
RP.core.FormatConstants =  { Weekdays: ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],
                        FullWeekdays:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],
                        Months:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
                        FullMonths:["January","February","March","April","May","June","July","August","September","October","November","December"]
};

/* Fix Ext's override to the date format */
Ext.apply(Ext.form.DateField.prototype, {
    format: "m/d/Y",
    /* Fix for RPWEB-5148 */
    altFormats: "m/d/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d|n-j|n/j"
});

Ext.apply(Ext.DatePicker.prototype, {
    format: "m/d/Y"
});

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);