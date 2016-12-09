
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: { Long:        { type: "date",
                                      order: "d,m,y",
                                      formatstring: "dd'/'MM'/'yyyy hh':'mm tt"},
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
                                          formatstring:"dd'/'MM'/'yyyy hh':'mm':'ss tt"},
                       FullDateOnly:    { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dddd', 'dd MMMM', 'yyyy"},
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
                                          formatstring:"dddd', 'dd MMMM', 'yyyy hh':'mm tt"},
                       POSDate:         { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd'T'hh':'mm':'ss"},
                       WeekdayDate:     { type: "date",
                                          order:"d, m",
                                          formatstring:"ddd, dd/MM"},
                       ShortDateTime:   { type: "date",
                                          order:"d,m,y",
                                          formatstring:"dd MMM', 'hh':'mm tt"},
                       MediumDateExp:   { type: "date",
                                          order:"d,m",
                                          formatstring:"dd MMM"}
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
                                              leadchar:"£",
                                              thousand:",",
                                              dec:".",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency",
                                              leadchar:"£",
                                              thousand:",",
                                              dec:".",
                                              scale:"4",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency",
                                              leadchar:"£",
                                              thousand:",",
                                              dec:".",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency",
                                              leadchar:"£",
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
    format: "d/m/Y",
    /* Fix for RPWEB-5148 */
    altFormats: "d/m/Y|j/n/Y|j/n/y|j/m/y|d/n/y|j/m/Y|d/n/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|d|Y-d-m|j-n|j/n"
});

Ext.apply(Ext.DatePicker.prototype, {
    format: "d/m/Y"
});

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);