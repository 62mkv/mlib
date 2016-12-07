Ext.define("RP.MOCA.override.ui.SessionExpiredDialog", {
    override: "RP.ui.SessionExpiredDialog",

    /**
     * Submits login credentials entered by the user to re-authenticate a session.
     * @method
     */
    submit: function() {
        var userName = this.getComponent("form").getComponent("userName").getValue();
        var password = this.getComponent("form").getComponent("password").getValue();
        
        if (Ext.isEmpty(userName) || Ext.isEmpty(password)) {
            this._displayAlertMessageBox(RP.getMessage("rp.common.login.LoginFailed"));
            return;
        }
        
        // show a load mask while the ajax call is running
        this.loadMask = new Ext.LoadMask(this.body, {
            msg: RP.getMessage("rp.common.misc.PleaseWait"),
            style: {
                'z-index' : '100000 !important'
            }
        });
        
        this.loadMask.show();
        
        logger.logTrace("[SessionExpiredLoginDialog] Submitting reauthentication request.");
        
        var params = {
            loginName: userName,
            password: password
        };
        
        // perform a request to attempt to re-authenticate the user
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console?m=login',
            method: "POST",
            params: params,
            scope: this,
            disableExceptionHandling: true,
            callback: this._loginCallback
        });
    },
    
    /**
     * Handles a result from a request for reauthentication.
     * @private
     * @override
     */
    _loginCallback: function(options, success, response) {
        var r,
            auth = false;
            
        try {
            r = Ext.decode(response.responseText);

            Ext.each(r.data, function(v) {
                if (v === 'Authenticated') {
                    auth = true;
                }
            });
        }
        catch (err) {
            r = null;
        }
        
        if (success === true) {
            this.loadMask.hide();
            
            if (auth === true) {
                this.destroy();
                
                // Fire session reauthenticated event
                RP.event.AppEventManager.fire(RP.upb.AppEvents.SessionReauthenticated, {});
                
                // Reload the page
                window.location.reload();
                // history.go(0);
                // window.location.href = window.location.href;
            }
            else {
                // User must still log in 
                this._displayAlertMessageBox('Unable to log in. Please try again.');
            }
        }
        else {
            if (r && r.message && !Ext.isEmpty(r.message)) {
                this._displayAlertMessageBox(r.message);
            }
            else {
                this._displayAlertMessageBox('Unable to log in. Please try again.');
            }
            
            RP.util.Helpers.redirectToLogin();
            return;
        } 
    }
});