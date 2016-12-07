if (!RP.REFSExceptionCodes) {
    RP.REFSExceptionCodes = {
        OK: 0
    };
}

if (!RP.REFSExceptionMessages) {
    RP.REFSExceptionMessages = [
        'OK'
    ];
}

RP.Moca.util.Ajax = function() {
    return {
        request: function (params) {
    
            var failureTitle = (params && params.failureAlert && params.failureAlert.title) ? params.failureAlert.title : RP.getMessage('rp.moca.common.console');
            var failureMsg = (params && params.failureAlert && params.failureAlert.msg) ? params.failureAlert.msg : RP.getMessage('rp.common.exception.dialogMessage');
            
            Ext.apply(params, {
                failure: function (result) {
                    // We need to differentiate between a failed connection and a failed request.
                    if (result.status === 0) {
                        window.alert('A connection with the server could not be established.\n\n' + 'Check to make sure the server has not been shutdown and try again.\n\n');
                        return;
                    }
    
                    Ext.Msg.alert(failureTitle, failureMsg);
                }
            });
    
            var me = this,
                s = params.success,
                scope = params.scope;
            params.success = function(result) {
                if (result.getResponseHeader("authenticated") !== "false" && typeof s === "function") {
                    s.apply(scope ? scope : me, [result]);
                }
                else {
                    Ext.Ajax.fireEvent("sessionterminated");
                }
            };
            Ext.Ajax.request(params);
        },
    
        requestWithTextParams: function (params) {
            var failureTitle = (params && params.failureAlert && params.failureAlert.title) ? params.failureAlert.title : RP.getMessage('rp.moca.common.console');
            var failureMsg = (params && params.failureAlert && params.failureAlert.msg) ? params.failureAlert.msg : RP.getMessage('rp.common.exception.dialogMessage');
    
            Ext.apply(params, {
                failure: function (result) {
                    // We need to differentiate between a failed connection and a failed request.
                    if (result.status === 0) {
                        window.alert('A connection with the server could not be established.\n\n' + 'Check to make sure the server has not been shutdown and try again.\n\n');
                        return;
                    }
    
                    Ext.Msg.alert(failureTitle, failureMsg);
                }
            });
    
            var me = this,
                s = params.success,
                scope = params.scope;
            params.success = function(result) {
                if (result.getResponseHeader("authenticated") !== "false" && typeof s === "function") {
                    s.apply(scope ? scope : me, [result]);
                }
                else {
                    Ext.Ajax.fireEvent("sessionterminated");
                }
            };
            Ext.Ajax.request(params);
        }
    };
}();

// We have to remove RPWEB listener so that we can override with ours.
// This code works because there are no other listeners. If that changes and RPWEB actually
// makes it convenient for us to override a single method that will be called, we should do that.
if (Ext.Ajax.events.requestexception) {
    Ext.Ajax.events.requestexception.clearListeners();
}

Ext.Ajax.on('requestexception', function(conn, response, options) {
    Ext.Ajax.activeRequests--;
    
    // If the user is leaving the page, swallow the exception since the exception came from
    // the browser killing the ajax requests during it's unload process.
    if (window.leavingPage) {
        return;
    }
    
    var urlMsg = options.CallMethod ? "Proxy to " + options.CallMethod : options.url;
    logger.logError(Ext.String.format("<br /><span style='padding-left: 3em'>Failed - URL: {0}</span><br /><span style='padding-left: 3em'> Parameters: {1}</span> <br /><span style='padding-left: 3em'>Status Text: {2}</span>", urlMsg, options.params, response.statusText));
    
    // If disableExceptionHandling is set to true then we don't want to display a message to the user
    var responseObject = Ext.JSON.decode(response.responseText, true);
    var status = response.status;
    if (!Ext.isEmpty(responseObject) && !Ext.isEmpty(responseObject.status)) {
        status = responseObject.status;
    }
    
    var messageId = undefined,
        exceptionMessageIdRoot = "rp.common.exception.";
        
    if (status !== 0) {
        messageId = RP.REFSExceptionMessages[status];
    }

    if (Ext.isEmpty(messageId)) {
        messageId = "GENERAL_EXCEPTION";
    }
    
    if (status === RP.REFSExceptionCodes.CONNECTION_SESSION_REVOKED_EXCEPTION) {
        var callback = RP.util.Helpers.logout;
        Ext.Msg.alert(RP.getMessage("rp.common.login.SessionErrorTitle"), RP.getMessage(exceptionMessageIdRoot + messageId), callback);
    }
    else if (!Ext.Ajax.isUserHandlingException(options)) {
        Ext.Msg.show({
            title: 'Error',
            msg: RP.getMessage(exceptionMessageIdRoot + messageId),
            buttons: Ext.MessageBox.OK,
            width: 400
        });
    }
});

