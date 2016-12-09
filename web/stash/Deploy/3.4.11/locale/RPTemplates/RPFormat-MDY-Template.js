Ext.ns("RP.core");

Ext.onReady(function() {
    
    //These are used to set the separators and currency signs for RP.core.Formats
    //UNICORN: Use the Ext.util.Format values.
    //DONKEY: Use a string with the correct symbol.

	// THIS TEMPLATE IS DESIGNED FOR DONKEY DEPLOYMENT
	
	// These variables must always be set to the appropriate values
    var thousandSeparator = ",";
    var decimalSeparator = ".";
    var currencySign = "$";
	var dateFormat = 'm/d/y';
    var dateSeparator = '/';
    var timeSeparator = ':';
    var negativeSign = "()";
	
	// Set to true if using an ExtFormats language template with the wrong data format
    var overrideDataLoc = false;
    var defaultTimeFormat = "HH'" + timeSeparator + "'mm";
	var defaultDateFormat = "MM'" + dateSeparator + "'dd'" + dateSeparator + "'yyyy";
	
	// DO NOT CHANGE ANYTHING BELOW THIS LINE; THE FOLLOWING CODE IS DEFINED IN \locale\RPTemplates
	
    /* SENCHA OVERRIDES */

    /* Fix Ext's override to the date format */
    Ext.apply(Ext.form.DateField.prototype, {
        /* Fix for RPWEB-4949, until Sencha fixes it. */
        altFormats: "m/d/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d|n-j|n/j|m.d.Y",
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
                order: "m,d,y",
                formatstring: "MM'" + dateSeparator + "'dd'" + dateSeparator + "'yyyy hh'" + timeSeparator + "'mm"
            },
            Default: {
                type: "date",
                order: "m,d,y",
                formatstring: defaultDateFormat
            },
            Medium: {
                type: "date",
                order: "m,d,y",
                formatstring: "MM'" + dateSeparator + "'dd'" + dateSeparator + "'yyyy"
            },
            Short: {
                type: "date",
                order: "m,d,y",
                formatstring: "M'" + dateSeparator + "'d'" + dateSeparator + "'yy"
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
                formatstring: "MM'" + dateSeparator + "'dd"
            },
            Weekday: {
                type: "date",
                order: "d",
                formatstring: "dddd"
            },
            TimeStamp: {
                type: "date",
                order: "m,d,y",
                formatstring: "MM'" + dateSeparator + "'dd'" + dateSeparator + "'yyyy hh'" + timeSeparator + "'mm'" + timeSeparator + "'ss tt"
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
                formatstring: "MM'" + dateSeparator + "'dd ddd"
            },
            ShortWeekday: {
                type: "date",
                order: "d",
                formatstring: "ddd"
            },
            FullDateTime: {
                type: "date",
                order: "m,d,y",
                formatstring: "dddd', 'MMMM dd', 'yyyy hh'" + timeSeparator + "'mm tt"
            },
            POSDate: {
                type: "date",
                order: "y,m,d",
                formatstring: "yyyy'" + dateSeparator + "'MM'" + dateSeparator + "'dd'T'hh'" + timeSeparator + "'mm'" + timeSeparator + "'ss"
            },
            WeekdayDate: {
                type: "date",
                order: "d, m",
                formatstring: "ddd, dd" + dateSeparator + "MM"
            },
            ShortDateTime: {
                type: "date",
                order: "m,d,y",
                formatstring: "dd MMM', 'hh'" + timeSeparator + "'mm tt"
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
                formatstring: defaultTimeFormat
            },
            Short: {
                type: "time",
                formatstring: "h'" + timeSeparator + "'mm tt"
            },
            Long: {
                type: "time",
                formatstring: "hh'" + timeSeparator + "'mm'" + timeSeparator + "'ss tt"
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
                formatstring: "h t"
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
