
/*global RP, Ext, _FormatBasedOnType */


Ext.ns("RP.core");

/**
 * @namespace RP.core
 * @class Formats
 */
RP.core.Formats = { Date: { Long:        { type: "date",
                                      order: "y,m,d",
                                      formatstring: "yyyy'-'MM'-'dd HH':'mm"},
                       Default:     { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd"},
                       Medium:      { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd"},
                       Short:       { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yy'-'M'-'d"},
                       MonthYear:       { type: "date",
                                          order:"y,m",
                                          formatstring:"yyyy MMM"},
                       FullMonthYear:   { type: "date",
                                          order:"y,m",
                                          formatstring:"yyyy MMMM"},
                       Month:           { type: "date",
                                          order: "m",
                                          formatstring:"MMM"},
                       FullMonth:       { type: "date",
                                          order: "m",
                                          formatstring:"MMMM"},
                       MonthDate:       { type: "date",
                                          order:"m,d",
                                          formatstring:"MM'-'dd"},
                       Weekday:         { type: "date",
                                          order:"d",
                                          formatstring:"dddd"},
                       TimeStamp:       { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd HH':'mm':'ss"},
                       FullDateOnly:    { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy', 'MMMM dd', 'dddd"},
                       FullDateWithoutDayName:    { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy, dd MMMM"},
                       LongYear:        { type: "date",
                                          order:"y",
                                          formatstring:"yyyy"},
                       MonthDateDay:    { type: "date",
                                          order:"m,d",
                                          formatstring:"MM'-'dd ddd"},
                       ShortWeekday:    { type: "date",
                                          order:"d",
                                          formatstring:"ddd"},
                       FullDateTime:    { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy', 'MMMM dddd', 'dd HH':'mm"},
                       POSDate:         { type: "date",
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd'T'HH':'mm':'ss"},
                       WeekdayDate:     { type: "date",
                                          order:"m, d",
                                          formatstring:"ddd, MM-dd"},
                       ShortDateTime:   { type: "date",
                                          order:"y,m,d",
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
                                              leadchar:"kr",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency",
                                              leadchar:"kr",
                                              thousand:".",
                                              dec:",",
                                              scale:"4",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency",
                                              leadchar:"kr",
                                              thousand:".",
                                              dec:",",
                                              scale:"2",
                                              negsign: "()",
                                              trailchar:"",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency",
                                              leadchar:"kr",
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
RP.core.FormatConstants =  { Weekdays: ["sön","mån","tis","ons","tor","fre","lör"],
                        FullWeekdays:["söndag","måndag","tisdag","onsdag","torsdag","fredag","lördag"],
                        Months:["jan","feb","mar","apr","maj","jun","jul","aug","sep","okt","nov","dec"],
                        FullMonths:["januari","februari","mars","april","maj","juni","juli","augusti","september","oktober","november","december"]
};


/* Fix Ext's override to the date format */
Ext.apply(Ext.form.DateField.prototype, {
    format: "Y-m-d",
    /* Fix for RPWEB-5148 */
    altFormats: "Y-m-d|Y-j-n|y-j-n|y-j-m|y-d-n|Y-j-m|Y-d-n|j-n|ydm|dm|Ydm"
});

Ext.apply(Ext.DatePicker.prototype, {
    format: "Y-m-d"
});

Ext.apply(Ext.form.TimeField.prototype, {
    minText: 'De tijd in dit veld moet op of na {0} liggen',
    maxText: 'De tijd in dit veld moet op of voor {0} liggen',
    invalidText: '{0} is geen geldig tijdstip',
    format: 'H:i',
    altFormats: 'g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H'
});

RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);