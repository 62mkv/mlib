Ext.define("RP.MOCA.override.upb.prototype.HeaderContainer", {
    override: "RP.upb.prototype.HeaderContainer",
    
    /**
     * @override
     * @private
     */
    _createItems: function() {
        this.logoutButton = this._createLogoutButton();
        this.supportButton = this._createSupportButton();
        
        var items = [
            this._createRpLogoContainer(),
            {
                xtype: 'component',
                flex: 1
            },
            this.supportButton,
            this.logoutButton
        ];
        
        return this._mayberReorderForNavRegion(items);
    },
    
    /**
     * @private
     */
    _createLogoutButton: function() {
        return this._createButton({
            text: "Logout",
            handler: this._onExitClick,
            cls: this.buttonCls,
            scope: this
        });
    },
    
    /**
     * @private
     */
    _createSupportButton: function() {
        return this._createButton({
            text: "Support",
            handler: this._onSupportClick,
            cls: this.buttonCls,
            scope: this
        });
    },
    
    /**
     * @private
     */
    _onExitClick: function() {
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console?m=logout',
            method: 'POST',
            success: function() {
                setTimeout("window.location.reload()", 100);
            },
            failureAlert: {
                title: 'Console',
                msg: 'Could not send the logout request to the server.'
            }
        });
    },
    
    /**
     * @private
     */
    _onSupportClick: function() {
        Ext.MessageBox.confirm("Support", 
        "<br />Are you sure you want to create a support file for download?",
        function(btn) {
            if (btn == 'yes') {
                var body = Ext.getBody();
                
                var frame = body.createChild({
                    tag: 'iframe',
                    cls: 'x-hidden',
                    src: '/console/support'
                });
            }
        });
    }
});