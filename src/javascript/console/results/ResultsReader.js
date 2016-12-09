Ext.define("RP.Moca.Console.Results.ResultsReader", {
    extend: "Ext.data.reader.Json",

    root: 'data',
    _status: null,
    _message: "",

    /**
     * read
     * Override the read method of the JsonReader to grab some additional data
     * before passing the data back to the handler.
     * @param {Object} response
     */
    read: function(response) {
        // console.log("response.responseText="+response.responseText);
        // console.log("\n\n\n");
        var json = Ext.decode(response.responseText);
        
        if (json !== null) {
            this._status = parseInt(json.status, 10);
            this._message = json.message;
            
            this.checkOkStatus();
        }

        // Parse the data and return it.
        return this.callParent(arguments);
    },

    /**
     * Checks if this has a status of 0 or null. If not, displays an error
     * message.
     */
    checkOkStatus: function() {
        if (this._status !== 0 && this._status !== null) {
            Ext.Msg.show({
                title: 'Console',
                msg: this._message,
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },

    getStatus: function() { return this._status; },
    getMessage: function() { return this._message; }
});
