/*
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */
//////////////////////
// ..c\javascript\console\login\Login.js
//////////////////////
Ext.override(RP.login.LoginForm, {

    /**
     * Submits a login request
     * @method submit
     */
    submit: function() {
        var results = Ext.getCmp("loginForm").getForm().getValues();
        
        if (results.userName && results.password) {
            this.login(results.userName, results.password);
        }
        else {
            Ext.getCmp("warning").show();
        }
    },
    
    /**
     * @private
     * @override
     * We want to override this method because MOCA responses are different.
     */
    login: function(userId, password) {
        logger.logTrace("[Login] Submitting credentials...");
        
        var myMask = new Ext.LoadMask(document.body, {
            msg: RP.getMessage("rp.common.misc.PleaseWait")
        });
        myMask.show();
        this._disableFields();
        
        var params = {
            loginName: userId,
            password: password
        };
        
        return Ext.Ajax.requestWithTextParams({
            url: this.initialConfig.securityServiceURL,
            method: "POST",
            params: params,
            scope: this,
            disableExceptionHandling: true,
            callback: function(options, success, response) {
                Ext.getCmp("warning").update(RP.getMessage("rp.common.login.LoginFailed"));
                // if the response times out, we won't try to cast the response text
                if (!success) {
                    logger.logFatal("[Login] Request timed out.");
                    myMask.hide();
                    this._enableFields();
                    Ext.getCmp("warning").show();
                }
                else {
                    var result;
                    try {
                        result = Ext.util.JSON.decode(response.responseText);
                        logger.logTrace("[Login] Got result from server: " + result.message);
                    }
                    catch (ex) {
                        result = {};
                        result.message = "Received an invalid response from server.";
                    }
                    
                    // We dont have contains method because its old ExtJS
                    var auth = false,
                        access = true;
                    Ext.each(result.data, function(i) {
                        if (i === "Authenticated") {
                            auth = true;
                            return false; // stop iteration
                        }
                        else if (i === 'NoAccess') {
                            access = false;
                            return false;
                        }
                        else if (i === "NotAuthenticated") {
                            auth = false;
                            return false; // stop iteration
                        }
                    }, this);
                    
                    if (result.status === this.authenticatedStatus && auth) {
                        var redirectUrl = this.onAuthenticatedHandler(window.location.href);
                        if (redirectUrl) {
                            RP.util.Helpers.redirect(redirectUrl);
                        }
                        else {
                            RP.core.PageContext.setInitialURL(result.data.siteId, result.data.module);
                        }
                    }
                    else if (!access) {
                        logger.logFatal("[Login] " + "Unauthorized to use this service");
                        myMask.hide();
                        this._enableFields();
                        var warn = Ext.getCmp("warning");
                        warn.update('You do not have privileges to use the MOCA Console.');
                        warn.show();
                    }
                    else {
                        logger.logFatal("[Login] " + result.message);
                        myMask.hide();
                        this._enableFields();
                        Ext.getCmp("warning").show();
                    }
                }
            }
        });
    }
});
