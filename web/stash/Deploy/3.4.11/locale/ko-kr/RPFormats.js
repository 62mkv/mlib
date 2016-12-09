Ext.ns("RP.core");

Ext.onReady(function() {
    
    //These are used to set the separators and currency signs for RP.core.Formats
    //UNICORN: Use the Ext.util.Format values.
    //DONKEY: Use a string with the correct symbol.
	
	// THIS TEMPLATE IS DESIGNED FOR DONKEY DEPLOYMENT with EXT JS 3
	
	// These variables must always be set to the appropriate values
    var thousandSeparator = ",";
    var decimalSeparator = ".";
    var currencySign = "₩";
	var dateFormat = 'y-m-d';
    var dateSeparator = '-';
    var timeSeparator = ':';
    var negativeSign = "()";
	
	// Set to true if using an ExtFormats language template with the wrong data format
    var overrideDataLoc = false;
    var defaultTimeFormat = "HH'" + timeSeparator + "'mm";
	var defaultDateFormat = "yyyy'" + dateSeparator + "'MM'" + dateSeparator + "'dd";
	
    /* SENCHA OVERRIDES */

    /* Fix Ext's override to the date format */
    Ext.apply(Ext.form.DateField.prototype, {
        /* Fix for RPWEB-4949, until Sencha fixes it. */
        altFormats: "Y-m-d|Y-j-n|y-j-n|y-j-m|y-d-n|Y-j-m|Y-d-n|j-n|ydm|dm|Ydm|y/m/d",
        format: dateFormat
    });
    
    Ext.apply(Ext.DatePicker.prototype, {
        format: dateFormat
    });
   
    /* Overrides for Sencha language files with wrong data localization */
    if (overrideDataLoc) {
        if(Ext.util.Format){
            Ext.util.Format.date = function(v, format){
				if(!v) return "";
				if(!(v instanceof Date)) v = new Date(Date.parse(v));
				return v.dateFormat(format || dateFormat);
			};
        }
        if(Ext.DatePicker){
              Ext.apply(Ext.DatePicker.prototype, {
                format            : dateFormat,
              });
        }
		if(Ext.form.NumberField){
		  Ext.apply(Ext.form.NumberField.prototype, {
			decimalSeparator : decimalSeparator
		  });
		}
		if(Ext.form.DateField) {
			Ext.apply(Ext.form.DateField.prototype, {
				format : dateFormat
			});
		}
		if(Ext.grid.PropertyColumnModel) {
			Ext.apply(Ext.grid.PropertyColumnModel.prototype, {
				dateFormat : dateFormat
			});
		}
		if(Ext.grid.NumberColumn) {
			Ext.apply(Ext.grid.NumberColumn.prototype, {
				format : "0" + thousandSeparator + "000" + decimalSeparator + "00"
			});
		}
		if(Ext.grid.DateColumn) {
			Ext.apply(Ext.grid.DateColumn.prototype, {
				format : dateFormat
			});
		}
		if(Ext.form.TimeField) {
			Ext.apply(Ext.form.TimeField.prototype, {
				format : "g" + timeSeparator + "i A"
			});
		}
    }

    
    /**
     * @class RP.core.Formats
     */
    RP.core.Formats = {
        Date: {
            Long: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'년'M'월'd'일' dddd hh'" + timeSeparator + "'mm"
            },
            Default: {
                type: "date",
                order: "y,m,d",
                formatstring: defaultDateFormat
            },
            Medium: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'" + dateSeparator + "'MM'" + dateSeparator + "'dd"
            },
            Short: {
                type: "date",
                order: "y,m,d",
                formatstring: "yy'" + dateSeparator + "'M'" + dateSeparator + "'d"
            },
            MonthYear: {
                type: "date",
                order: "y,m",
                formatstring: "yyyy'년'M'월'"
            },
            FullMonthYear: {
                type: "date",
                order: "y,m",
                formatstring: "yyyy'년'M'월'"
            },
            Month: {
                type: "date",
                order: "m",
                formatstring: "MMMM"
            },
            FullMonth: {
                type: "date",
                order: "m",
                formatstring: "MMMM"
            },
            MonthDate: {
                type: "date",
                order: "m,d",
                formatstring: "M'월'd'일' dddd"
            },
            Weekday: {
                type: "date",
                order: "d",
                formatstring: "dddd"
            },
            TimeStamp: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'" + dateSeparator + "'MM'" + dateSeparator + "'dd HH'" + timeSeparator + "'mm'" + timeSeparator + "'ss"
            },
            FullDateOnly: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'년'M'월'd'일' dddd"
            },
            FullDateWithoutDayName: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'년'M'월'd'일'"
            },
            LongYear: {
                type: "date",
                order: "y",
                formatstring: "yyyy"
            },
            MonthDateDay: {
                type: "date",
                order: "m,d",
                formatstring: "M'월'd'일' dddd"
            },
            ShortWeekday: {
                type: "date",
                order: "d",
                formatstring: "dddd"
            },
            FullDateTime: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'년'M'월'd'일' HH'" + timeSeparator + "'mm"
            },
            POSDate: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'" + dateSeparator + "'MM'" + dateSeparator + "'dd'T'HH'" + timeSeparator + "'mm'" + timeSeparator + "'ss"
            },
            WeekdayDate: {
                type: "date",
                order: "m, d",
                formatstring: "M'월'd'일' dddd"
            },
            ShortDateTime: {
                type: "date",
                order: "m,d",
                formatstring: "MM" + dateSeparator + "dd' 'HH'" + timeSeparator + "'mm"
            },
            MediumDateExp: {
                type: "date",
                order: "m,d",
                formatstring: "MMMM" + dateSeparator + "dd"
            }
        },
        Time: {
            Default: {
                type: "time",
                formatstring: defaultTimeFormat
            },
            Short: {
                type: "time",
                formatstring: "HH'" + timeSeparator + "'mm"
            },
            Long: {
                type: "time",
                formatstring: "HH'" + timeSeparator + "'mm'" + timeSeparator + "'ss"
            },
            Military: {
                type: "time",
                formatstring: "HH'" + timeSeparator + "'mm"
            },
            MilitaryWithSeconds: {
                type: 'time',
                formatstring: "HH'" + timeSeparator + "'mm'" + timeSeparator + "'ss"
            },
            HourOnly: {
                type: "time",
                formatstring: "t h"
            }
        },
        Number: {
            Default: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: negativeSign
            },
            HighPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "4",
                negsign: negativeSign,
                trimzero: "n"
            },
            MediumPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: negativeSign,
                trimzero: "n"
            },
            MediumLowPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "1",
                negsign: negativeSign,
                trimzero: "n"
            },
            LowPrecision: {
                type: "number",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "0",
                negsign: negativeSign,
                trimzero: "n"
            },
            ScheduleTotal: {
                type: "number",
                thousand: "",
                dec: decimalSeparator,
                scale: "2",
                negsign: negativeSign,
                trimzero: "y"
            },
            MediumPrecisionRaw: {
                type: "number",
                thousand: "",
                dec: decimalSeparator,
                scale: "2",
                negsign: negativeSign,
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
                negsign: negativeSign,
                trailchar: "",
                trimzero: "y"
            },
            HighPrecision: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "4",
                negsign: negativeSign,
                trailchar: "",
                trimzero: "n"
            },
            MediumPrecision: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: negativeSign,
                trailchar: "",
                trimzero: "n"
            },
            LowPrecision: {
                type: "currency",
                leadchar: currencySign,
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "0",
                negsign: negativeSign,
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
                negsign: negativeSign,
                trailchar: "%",
                trimzero: "n"
            },
            MediumPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "2",
                negsign: negativeSign,
                trailchar: "%",
                trimzero: "n"
            },
            MediumLowPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "1",
                negsign: negativeSign,
                trailchar: "%",
                trimzero: "n"
            },
            LowPrecision: {
                type: "number",
                leadchar: "",
                thousand: thousandSeparator,
                dec: decimalSeparator,
                scale: "0",
                negsign: negativeSign,
                trailchar: "%",
                trimzero: "n"
            }
        },
        TimeSpan: {
            Short: {
                type: "timespan",
                formatstring: "{h}" + timeSeparator + "{mm}",
                template: undefined // compiled Ext template
            },
            Medium: {
                type: "timespan",
                formatstring: "{h}" + timeSeparator + "{mm}" + timeSeparator + "{ss}",
                template: undefined // compiled Ext template
            },
            Long: {
                type: "timespan",
                formatstring: "{hh}" + timeSeparator + "{mm}" + timeSeparator + "{ss}",
                template: undefined // compiled Ext template
            }
        }
    };
    
    /**
     * @class RP.core.FormatConstants
     * @singleton
     */
    RP.core.FormatConstants = {
		Weekdays: [],
        FullWeekdays: Date.dayNames,
        Months: [],
        FullMonths: Date.monthNames,
    };
    for (var i=0; i < Date.dayNames.length; i++) {
		RP.core.FormatConstants.Weekdays[i] = Date.getShortDayName(i);
	};
	for (var i=0; i < Date.monthNames.length; i++) {
		RP.core.FormatConstants.Months[i] = Date.getShortMonthName(i);
	};
    RP.core.Format = RP.core.FormatEngine(RP.core.Formats, RP.core.FormatConstants);
});
