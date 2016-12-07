RP.Moca.util.Format = function() {
    // Data structure for mapping byte conversions
    // Base 2 = IEC standard; Base 10 = SI standard
    // The larger the unit the more decimal places that are shown
    var byteSizeConversionData = [{
        base2suffix: 'bytes',
        base10suffix: 'bytes',
        precision: 0
    }, {
        base2suffix: 'KiB',
        base10suffix: 'kB',
        precision: 0
    }, {
        base2suffix: 'MiB',
        base10suffix: 'MB',
        precision: 1
    }, {
        base2suffix: 'GiB',
        base10suffix: 'GB',
        precision: 2
    }, {
        base2suffix: 'TiB',
        base10suffix: 'TB',
        precision: 3
    }, {
        base2suffix: 'PiB',
        base10suffix: 'PB',
        precision: 4
    }, {
        base2suffix: 'EiB',
        base10suffix: 'EB',
        precision: 5
    }];

    return {
        datetimeString: function (value) {
            if (!value || value == "") {
                return "";
            }

            var myDate = new Date(value);
    
            return Ext.Date.format(myDate, 'Y-m-d H:i:s');
        },

        /**
         * @method 
         * @param {number} number The number to truncate.
         */
        truncateNumber: function (number) {
            var places = 3;
            var newNumber = Math.round(number * Math.pow(10, places)) / Math.pow(10, places);

            return newNumber;
        },

        /**
         * @method 
         * @param {Object} str The string to truncate.
         * @param {number} maxLength The length to trim to.
         */
        truncateString: function (str, maxLength) {
            if (str.length > maxLength) {
                str = str.substring(0, maxLength - 3) + "...";
            }

            return str;
        },

        /**
         * @method 
         * @param {Object} value The string to coerce into a nicer string.
         * 
         * The following are defined in CommandType.
         */
        commandTypeString: function (value) {
            switch (value) {
            case 'LOCAL_SYNTAX':
                value = 'Local Syntax';
                break;
            case 'JAVA_METHOD':
                value = 'Java Method';
                break;
            case 'C_FUNCTION':
            case 'SIMPLE_C_FUNCTION':
                value = 'C Function';
                break;
            case 'MANAGED_METHOD':
            case 'SIMPLE_MANAGED_METHOD':
                value = 'Managed Method';
                break;
            case 'COM_METHOD':
                value = 'COM Method';
                break;
            case 'TRIGGER':
                value = 'Trigger';
                break;
            case 'UNKNOWN':
                value = 'Unkonwn';
                break;
            }

            return value;
        },

        /**
         * @method 
         * @param {Object} value The string to coerce into a nicer string.
         * 
         */
        jobTypeString: function (value) {
            switch (value) {
            case 'timer':
                value = 'Timer';
                break;
            case 'cron':
                value = 'Scheduled';
                break;
            }

            return value;
        },

        /**
         * @method 
         * @param {Object} value The string to coerce into a nicer string.
         */
        taskTypeString: function (value) {
            switch (value) {
            case 'T':
                value = 'Thread';
                break;
            case 'P':
                value = 'Process';
                break;
            case 'D':
                value = 'Daemon';
                break;
            }

            return value;
        },

        /**
         * @method 
         * @param {Object} value The string to coerce into a nicer string.
         * 
         * The following are defined in ServerContextStatus.
         */
        sessionStatusString: function (value) {
            switch (value) {
            case 'INACTIVE':
                value = 'Inactive';
                break;
            case 'IN_ENGINE':
                value = 'In Engine';
                break;
            case 'JAVA_EXECUTION':
                value = 'Executing Java';
                break;
            case 'C_EXECUTION':
                value = 'Executing C';
                break;
            case 'COM_EXECUTION':
                value = 'Executing COM';
                break;
            case 'SQL_EXECUTION':
                value = 'Executing SQL';
                break;
            case 'SCRIPT_EXECUTION':
                value = 'Executing Script';
                break;
            case 'LOCAL_SYNTAX_EXECUTION':
                value = 'Executing Local Syntax';
                break;
            case 'REMOTE_EXECUTION':
                value = 'Executing Remote';
                break;
            case 'MANAGED_EXECUTION':
                value = 'Executing .NET';
                break;
            }

            return value;
        },

        /**
         * @method 
         * @param {Object} value The string to coerce into a nicer string.
         * 
         * The following are defined in SessionType.
         */
        sessionTypeString: function (value) {
            switch (value) {
            case 'CLIENT':
                value = 'Client';
                break;
            case 'CLIENT_LEGACY':
                value = 'Client (Legacy)';
                break;
            case 'TASK':
                value = 'Task';
                break;
            case 'JOB':
                value = 'Job';
                break;
            case 'ASYNC':
                value = 'Async Execution';
                break;
            case 'CONSOLE':
                value = 'Console';
                break;
            case 'SERVER':
                value = 'Server';
                break;
            }

            return value;
        },
		
		/**
         * @method 
         * @param {Object} value The string to coerce into a nicer string.
         */
        consoleRoleString: function (value) {
            switch (value) {
            case 'CONSOLE_ADMIN':
                value = 'Administrator';
                break;
            case 'CONSOLE_READ':
                value = 'Read Only';
                break;
            case 'NO_CONSOLE_ACCESS':
                value = 'No Console Access';
                break;
            }

            return value;
        },
        
        objectToString: function (values) {
            string = "";
            for (var key in values) {
                if (values.hasOwnProperty(key)) {
                    if (string.length > 0) {
                        string += ', ';
                    }
                    string += key;
                    string += "='";
                    string += values[key];
                    string += "'";
                }
            }
            
            return string;
        },


        // For more information on byte conversions (SI vs IEC/base 10 vs base 2) see - https://wiki.ubuntu.com/UnitsPolicy

        /**
         * @method
         * @param {Object} bytes The number of bytes to convert into a readable string.
         *
         * This method converts the number of bytes using base 2 into a readable string
         * using the IEC standard.
         * Base 2 should only be used when specifying physical RAM size, for most other
         * purposes such as disk space use base 10 (which modern operating systems/disk retailers follow).
         * The larger the unit the more decimal places that will be shown.
         */
        convertBytesBase2: function (bytes) {
            if (bytes <= 0)  {
                return 'n/a';
            }

            var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)), 10);
            
            // Wrap in parse float to remove trailing zeros
            return parseFloat((bytes / Math.pow(1024, i)).toFixed(byteSizeConversionData[i].precision)) +
                            ' ' + byteSizeConversionData[i].base2suffix;
        },

        /**
         * @method
         * @param {Object} bytes The number of bytes to convert into a readable string.
         *
         * This method converts the number of bytes using base 2 into a readable string
         * using the SI standard.
         * Base 10 should be used when dealing with bytes for file size or disk space.
         * Alternatively, base 2 is used when representing physical RAM.
         * The larger the unit the more decimal places that will be shown.
         */
        convertBytesBase10: function (bytes) {
            if (bytes <= 0)  {
                return 'n/a';
            }

            var iterations = 0;
            while (true) {
                 if (bytes >= 1000) {
                     bytes = bytes / 1000;
                 }
                 else {
                      break;
                 }
                 iterations++;
            }

            // Wrap in parse float to remove trailing zeros
            return parseFloat(bytes.toFixed(byteSizeConversionData[iterations].precision), 0) +
                           ' ' + byteSizeConversionData[iterations].base10suffix;
        }
        
    };
}();
