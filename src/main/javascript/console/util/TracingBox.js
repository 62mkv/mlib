Ext.namespace('RP.Moca.util');

/**
 * Tracing Box used to enable session tracing.
 */
Ext.define('RP.Moca.util.TracingBox', {
    extend: 'Ext.Window',
    
    constructor: function(sessionName, traceLevel, config) {
        this.sessionName = sessionName;
        this.traceLevel = traceLevel;
        
        RP.Moca.util.TracingBox.superclass.constructor.call(this, config);
    },
    
    initComponent: function() {
        this.okButton = new Ext.Button({
            itemId: 'btnOk',
            text: 'OK',
            handler: this.okHandler,
            scope: this
        });
        
        this.cancelButton = new Ext.Button({
            itemId: 'btnCancel',
            text: 'Cancel',
            handler: this.closeHandler,
            scope: this
        });
        
        this.fileNameBox = new Ext.form.TextField({
            xtype: 'textfield',
            inputValue: 'traceFile',
            fieldLabel: 'Trace File',
            width: 300
        });
        this.fileNameBox.on('change', function(t, n, o) {
            this.maybeUpdateCheckbox(n);
        }, this);
        
        this.traceCheckBox = new RPUX.form.field.Toggle({
            id: 'sessionTraceCheckBox',
            disabled: true,
            fieldLabel: 'Tracing Enabled',
            offText: 'Off',
            onText: 'On'
        });
        
        // make label NOT disabled
        this.on('afterrender', function() {
            Ext.getCmp('sessionTraceCheckBox').labelEl.setOpacity(1);
            this.maybeUpdateCheckbox(undefined);
        }, this);
        
        Ext.apply(this, {
            title: 'Session Tracing',
            draggable: false,
            resizable: false,
            modal: true,
            padding: 20,
            items: [{
                xtype: 'container',
                margin: '10 10 5 10',
                items: [
                    this.traceCheckBox,
                    this.fileNameBox
                ]
            }],
            buttonAlign: 'center',
            buttons: [
                this.okButton,
                this.cancelButton
            ]
        });
        
        RP.Moca.util.TracingBox.superclass.initComponent.call(this);
    },
    
    okHandler: function() {
        var params = {
            name: this.sessionName,
            filename: Ext.isEmpty(this.fileNameBox.getValue()) ? null : this.fileNameBox.getValue(),
            level: this.traceCheckBox.getValue() ? '*' : null
        };
        
        this.makeDataRequest({
            params: params,
            failureAlert: {
                title: 'Console',
                msg: 'Could not enable tracing.'
            }
        });
        
        this.closeHandler();
    },
    
    /**
     * Enables or disabled checkbox depending on value
     * coming from trace file field.
     */
    maybeUpdateCheckbox: function(val) {
        if (Ext.isEmpty(val)) {
           Ext.getCmp('sessionTraceCheckBox').setValue(false);
        }
        else {
            Ext.getCmp('sessionTraceCheckBox').setValue(true);
        }
    },
    
    closeHandler: function() {
        this.destroy();
    },
    
    /**
     * makeDataRequest
     * @param {Object} model
     * @param {Object} successCallback
     * @param {Object} failCallback
     */
    makeDataRequest: function (config) {
        var defaultConfig = {
            url: '/console',
            scope: this,
            autoLoad: true
        };

        config = Ext.applyIf(defaultConfig, config);
        RP.Moca.util.Ajax.request(config);
    }
});
