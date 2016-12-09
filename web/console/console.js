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
// ..c\javascript\console\helpers\Logout.js
//////////////////////
/**
 * We want to override the stash logout behavior. We need to actually get rid of the session.
 */
Ext.apply(RP.util.Helpers, {

    /**
     * @override
     * We want to override the stash logout behavior. We need to actually get rid of the session.
     */
    logout: function() {
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
    }
});
//////////////////////
// ..c\javascript\console\helpers\IEPerformance.js
//////////////////////
// improve performance in IE, see MOCA-6487
if (Ext.isIE) {
    Ext.supports.Direct2DBug = false;
}

//////////////////////
// ..c\javascript\console\results\ResultsReader.js
//////////////////////
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

//////////////////////
// ..c\javascript\console\template\BlankNulls.js
//////////////////////
Ext.define("MOCA.RP.util.template.BlankNulls", {
    override:'Ext.XTemplate',
  
    applyOut: function(values, out) {
        var me = this,
            compiler;

        if (!me.fn) {
            compiler = new Ext.XTemplateCompiler({
                useFormat: me.disableFormats !== true,
                blankNulls: me.blankNulls
            });
            me.fn = compiler.compile(me.html);
        }

        try {
            me.fn.call(me, out, values, {}, 1, 1);
        } catch (e) {
            Ext.Logger.error(e.message);
        }

        return out;
    }
});

Ext.define("RP.MOCA.util.template.XTemplateCompilerNullBlanks", {
    override:'Ext.XTemplateCompiler',
    
    // override to blankNulls if the config is set.
    parseTag: function (tag) {
        var m = this.tagRe.exec(tag),
            name = m[1],
            format = m[2],
            args = m[3],
            math = m[4],
            v;

        // name = "." - Just use the values object.
        if (name == '.') {
            // filter to not include arrays/objects/nulls
            v = 'Ext.Array.indexOf(["string", "number", "boolean"], typeof values) > -1 || Ext.isDate(values) ? values : ""';
        }
        // name = "#" - Use the xindex
        else if (name == '#') {
            v = 'xindex';
        }
        else if (name.substr(0, 7) == "parent.") {
            v = name;
        }
        // name has a . in it - Use object literal notation, starting from values
        else if ((name.indexOf('.') !== -1) && (name.indexOf('-') === -1)) {
            v = "values." + name;
        }
        // name is a property of values
        else {
            v = "values['" + name + "']";
        }

        if (math) {
            v = '(' + v + math + ')';
        }

        if (format && this.useFormat) {
            args = args ? ',' + args : "";
            if (format.substr(0, 5) != "this.") {
                format = "fm." + format + '(';
            } else {
                format += '(';
            }
        }
        else {
            args = '';
            if (this.blankNulls){
                format = "(" + v + " === undefined || " + v + " === null ? '' : ";
            }
            else {
                format = "(" + v + " === undefined ? '' : ";
            }
        }

        return format + v + args + ')';
    }
});
//////////////////////
// ..c\javascript\console\upb\HeaderContainer.js
//////////////////////
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
//////////////////////
// ..c\javascript\console\rpux\RPUXToggle.js
//////////////////////
//We have to include this class ourselves as it isn't part of the stash.
/**
 * @class RPUX.form.field.Toggle
 * @extends Ext.form.field.Checkbox
 *
 * An on / off switch that behaves like a checkbox under the hood.
 * Text displayed for the field's two states is configurable, as
 * is the value that the field actually submits to the server.
 *
 * @author Matt Schrader
 */
Ext.define('RPUX.form.field.Toggle', {
    extend: 'Ext.form.field.Checkbox',
    alias: ['rpuxToggleField', 'widget.rpuxToggleField'],

    /**
     * @cfg {Boolean/Number/Object/String} [checkedValue=true]
     * The value submitted as the toggle's value during form submit
     * if the toggle is in a checked state. See also #uncheckedValue.
     * Note that this config effectively replaces Ext.form.field.Checkbox#inputValue.
     */
    checkedValue: true,

    /**
     * @cfg {String} inputValue
     * @deprecated Use #checkedValue instead
     * The value that should go into the generated input element's value attribute
     * and should be used as the parameter value when submitting as part of a form.
     * Inherited from Ext.form.field.Checkbox, but the variable name is misleading
     * because Toggle uses a `<div>` for an inputEl instead of an `<input>` (i.e. It has no value attribute.).
     */

    /**
     * @cfg {Boolean/Number/Object/String} [uncheckedValue=false]
     * The value submitted as the toggle's value during form submit
     * if the toggle is unchecked. Inherited from Ext.form.field.Checkbox,
     * but supports more data types than just String.
     */

    /**
     * @cfg {String} offText Text shown when control is in "Off" mode.  Defaults to "Off".
     */

    /**
     * @cfg {String} onText Text shown when control is in "On" mode.  Defaults to "On".
     */

    /**
     * @cfg {Number} [tabIndex=0]
     * The DOM tabIndex for this component's switch element.
     */
    tabIndex: 0,

    /**
     * @cfg {Number} toggleWidth
     * The width of this component's inner toggle element in pixels.
     * Defaults to the width required to display #onText / #offText
     * (as determined by {@link Ext.util.TextMetrics TextMetrics})
     * or the component's #minWidth, whichever is larger.
     */

    /**
     * @private
     * @property {Number} _buttonWidth
     * The toggle button width (in pixels).
     */
    _buttonWidth: 35,

    /**
     * @cfg {Boolean} [allowBlank=true]
     * Allow blank defaulted to true for Toggle as there isn't a blank state.
     **/
    allowBlank: true,

    minWidth: 72,

    /**
     * @private
     * @property {Boolean} _maybeAnimate
     *
     * Indicates that setValue is being called as part of an action involving user input
     * (e.g click / keypress) instead of programmatically. setValue disables all animations
     * by default; this property will allow suspendAnimations to be respected when
     * a user manually interacts with the Toggle.
     */

    fieldSubTpl: [
            '<div class="toggle" style="width: {toggleWidth}px;">',
                '<div id="{id}" class="toggle_switch" tabIndex="{tabIndex}"> </div>',
                '<div type="checkbox" class="toggle_value">', '</div>',
            '</div>',
        {
            compiled: true,
            disableFormats: true
        }
    ],

    initComponent: function() {
        if (this.checkedValue === this.uncheckedValue) {
            RP.throwError('Invalid configuration - checkedValue and uncheckedValue configs are identical');
        }

        // inputValue and checkedValue both have defaults specified on the prototype,
        // so we can't just check to see if one or the other is defined to know which config should
        // be used. Look at initialConfig instead; if the developer provided one and not the other, assume
        // they know what they're doing and use whichever one is specified.
        if (this.initialConfig.inputValue === undefined) {
            this.inputValue = this.checkedValue;
        }
        else if (this.initialConfig.checkedValue === undefined && this.initialConfig.inputValue !== undefined) {
            RP.util.DeprecationUtils.logStackTrace(
                '[DEPRECATED] RPUX.form.field.Toggle#inputValue is a deprecated config. Use #checkedValue instead.');
            this.checkedValue = this.inputValue;
        }

        var tm = new Ext.util.TextMetrics(),
            textWidth =
                this._buttonWidth + Math.max(tm.getWidth(this.onText), tm.getWidth(this.offText)) + 15,
            w = Math.max(this.minWidth, textWidth, this.toggleWidth || 0);

        this.toggleWidth = w;

        Ext.applyIf(this, {
            onText: RP.getMessage('rpux.component.Toggle.on'),
            offText: RP.getMessage('rpux.component.Toggle.off')
        });

        this.suspendAnimations = true;
        this.callParent(arguments);
        delete this.suspendAnimations;

        this.on('afterrender', this._onAfterRender, this);
    },

    initValue: function() {
        this.suspendCheckChange++;
        var checked = !!this.checked,
            checkVal = checked ? this.checkedValue : this.uncheckedValue;

        this.originalValue = this.lastValue = checkVal;
        // OVERRIDE
        this.setValue(checkVal);
        // END OVERRIDE
        this.suspendCheckChange--;
    },

    getSubTplData: function() {
        return Ext.applyIf(this.callParent(arguments), {
            tabIndex: this.tabIndex,
            toggleWidth: this.toggleWidth
        });
    },

    _onAfterRender: function() {
        this._addFocusHandling();
    },

    /**
     * @private
     * Attaches listeners to the toggle switch to support a focused state.
     */
    _addFocusHandling: function() {
        this.inputEl.on('blur', this._onInputBlur, this);
        this.inputEl.on('focus', this._onInputFocus, this);
        this.inputEl.on('keyup', this._onInputKeyUp, this);
    },

    _onInputBlur: function(event, target) {
        this.removeCls('rpux-toggle-field-focus');
    },

    _onInputFocus: function(event, target) {
        this.addCls('rpux-toggle-field-focus');
    },

    _onInputKeyUp: function(event, target) {
        var key = (typeof event.which == 'number') ? event.which : event.keyCode;

        if (!this.disabled && (key === event.ENTER || key === event.SPACE)) {
            // Toggle the field's state, respecting the suspendAnimations config.
            this._maybeAnimate = true;
            this.setValue(this.checked ? this.uncheckedValue : this.checkedValue);
            delete this._maybeAnimate;
        }
    },

    /**
     * @inheritdoc
     */
    disable: function() {
        if (this.rendered) {
            this.inputEl.dom.tabIndex = -1;
        }
        return this.callParent(arguments);
    },

    /**
     * @inheritdoc
     */
    enable: function() {
        if (this.rendered) {
            this.inputEl.dom.tabIndex = this.tabIndex || 0;
        }
        return this.callParent(arguments);
    },

    /**
     * Handler for click events detected anywhere within the Toggle's element.
     */
    _onComponentClick: function(e) {
        if(e.getTarget('.toggle') || e.getTarget('.x-form-item-label')) {
            // Clicks to both the label text and anywhere in the input row (button or value)
            // should toggle the component.  In IE, clicks to the label also fire as a click
            // against the inputEl, so we must ignore them.
            if (!this.disabled && (this.inputRow.contains(e.target) || (!Ext.isIE && this.labelEl.contains(e.target)))) {
                // Ensure that suspendAnimations is respected; setValue disables animations by default.
                this._maybeAnimate = true;
                this.setValue(this.checked ? this.uncheckedValue : this.checkedValue);
                delete this._maybeAnimate;
            }
        }
    },

    /**
     * @override
     * Toggling the field's value is handled by the click listener instead
     * so that clicks on the label & toggle_value elements register as well.
     */
    onBoxClick: Ext.emptyFn,

    // @inheritdoc
    onRender: function() {
        this.suspendAnimations = true;
        this.callParent(arguments);

        var toggleValueEl = this.getEl().down('.toggle_value');
        toggleValueEl.unselectable();

        // Relatively positioned divs within grid cells have slightly
        // skewed margins in FF14 v. other browsers. Attach a special class
        // to offset this, but be aware that this may be fixed in a later version.
        if (Ext.firefoxVersion >= 14) {
            toggleValueEl.addCls('toggle_value_ff14');
        }

        if (Ext.isEmpty(toggleValueEl.dom.innerText)) {
            this.setRawValue(this.value);
        }
        delete this.suspendAnimations;

        this.getEl().on('click', this._onComponentClick, this);
    },

    /**
     * @override
     */
    isChecked: function(rawValue, checkedValue) {
        return rawValue == checkedValue;
    },

    /**
     * Sets the Toggle's checked state.
     *
     * @param {Boolean/Number/Object/String} value
     * Sets the Toggle to a checked state when equal to #checkedValue,
     * and unchecks it otherwise.
     * @return {Boolean} The new checked state.
     */
    setRawValue: function(value) {
        var toggle_switch = this.inputEl,
            checked = this.isChecked(value, this.checkedValue),
            toggle_value, toggle, updateFn;

        this.checked = this.rawValue = checked;

        if (toggle_switch) {
            toggle_value = toggle_switch.next('.toggle_value');
            toggle = toggle_switch.parent('.toggle');
        }

        if (!toggle || !toggle_switch) {
            return checked;
        }

        updateFn = this.suspendAnimations ? '_simpleUpdate' : '_animateUpdate';

        if (checked) {
            toggle_value.update(this.onText);
            this[updateFn](toggle_value, toggle_switch, this.toggleWidth - this._buttonWidth, 5, '#FFFFFF');
        }
        else {
            toggle_value.update(this.offText);
            this[updateFn](toggle_value, toggle_switch, 0, this._buttonWidth + 5, '#000000');
        }
        toggle[checked ? 'addCls' : 'removeCls']('enabled');

        return checked;
    },

    /**
     * @private
     * Updates styles and moves the toggle slider without animation.
     */
    _simpleUpdate: function(valueEl, switchEl, switchPos, valuePos, textColor) {
        if (textColor) {
            valueEl.setStyle({
                color: textColor
            });
        }

        if (Ext.isDefined(valuePos)) {
            valueEl.setLeft(valuePos);
        }

        switchEl.setLeft(switchPos);
    },

    /**
     * @private
     * Updates styles and animates the toggle slider's motion to its new position.
     */
    _animateUpdate: function(valueEl, switchEl, switchPos, valuePos, textColor) {
        if (Ext.isDefined(valuePos) && textColor) {
            valueEl.animate({
                from: {
                    left: valuePos
                },
                to: {
                    left: valuePos,
                    color: textColor
                }
            });
        }

        switchEl.animate({to: {
            left: switchPos
        }});
    },

    /**
     * Sets the Toggle's checked state and invokes change detection
     * @param {Boolean/Number/Object/String} value
     * Sets the Toggle to a checked state when equal to #checkedValue,
     * and unchecks it otherwise.
     * @return {RPUX.form.field.Toggle} this.
     */
    setValue: function(value) {
        var oldSuspendAnimations;

        // Don't animate when the field's value is set programmatically.
        if (!this._maybeAnimate) {
            oldSuspendAnimations = this.suspendAnimations;
            this.suspendAnimations = true;
        }

        this.checked = value;
        this.callParent(arguments);

        if (!this._maybeAnimate) { this.suspendAnimations = oldSuspendAnimations; }

        return this;
    },

    getHeight: function() {
        return 23;
    },

    setWidth: function(width) {
        this.callParent(arguments);

        var toggleEl = this.el.down('.toggle'),
            w = Math.max(this.getWidth(), this.minWidth);

        if (w < toggleEl.getWidth()) {
            // Ensure that the toggle is constrained by its owner.
            toggleEl.setWidth(w);
            this.toggleWidth = w;
        }
    },

    /**
     * Sets the width of the toggle element within this component.
     * Valid values are constrained by #minWidth and this field's
     * current width.
     * @param {Number} The width (in pixels) to set.
     */
    setToggleWidth: function(width) {
        var toggleEl = this.el.down('.toggle'),
            // The toggle element should not be larger than its container.
            w = Math.max(Math.min(width, this.getWidth()), this.minWidth);

        toggleEl.setWidth(w);
        this.toggleWidth = w;
    }
});
//////////////////////
// ..c\javascript\console\util\Store.js
//////////////////////
Ext.define("RP.Moca.util.Store", {
    extend: "Ext.data.Store",
    
    constructor: function(config) {
        
        // Apply the config to ourselves
        Ext.apply(this, config);
        
        // Apply some universal things
        Ext.apply(this.proxy, {
            type: 'ajax',
            url: '/console',
            reader: new RP.Moca.Console.Results.ResultsReader({})       
        });

        this.callParent(arguments);
    },

    listeners: {
        exception: function () {
            Ext.Msg.show({
                title: 'Console',
                msg: 'A connection with the server could not be established.\n\n' + 
                     'Check to make sure the server has not been shutdown and try again.',
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    }
});

//////////////////////
// ..c\javascript\console\util\SessionExpiredDialog.js
//////////////////////
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
//////////////////////
// ..c\javascript\console\util\GroupingStore.js
//////////////////////
/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

Ext.define("RP.Moca.util.GroupingStore", {
    extend: "Ext.data.Store",

    initComponent: function () {
        Ext.apply(this, {
            listeners: {
                exception: function () {
                    window.alert('A connection with the server could not be established.\n\n' +
                    'Check to make sure the server has not been shutdown and try again.\n\n');
                }
            }
        });

        this.callParent(arguments);
    },
    
    constructor: function() {
      console.log("I don't work yet");
    },
    
    clearGrouping: function() {
      console.log("I don't work yet");
    },
    
    groupBy: function(field, forceRegroup) {
      console.log("I don't work yet");
    }
});

//////////////////////
// ..c\javascript\console\util\NodeComboBoxStore.js
//////////////////////
Ext.ns('RP.util');

clusterNodeId = undefined;

// Store
Ext.define("RP.util.NodeComboBoxStore", {
    extend: "RP.Moca.util.Store",

    constructor: function (config) {

        // Apply the given configuration to ourselves.
        Ext.apply(this, config);

        // Apply shtuff
        Ext.apply(this, {
            model: "RP.util.NodeComboBoxStore.Model",
            storeId: "clusterComboBox",
            proxy: {
                extraParams: {
                    m: 'getClusterHosts'
                },
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({
                    model: "RP.util.NodeComboBoxStore.Model"
                })
            }
        });

        this.callParent(arguments);
    },

    load: function (config) {
        RP.util.NodeComboBoxStore.superclass.load.call(this, config);
    }
});

Ext.define("RP.util.NodeComboBoxStore.Model", {
    extend: "Ext.data.Model",
    fields: [
        { name: 'node' }, 
        { name: 'url' }, 
        { name: 'current' }
    ]
});
//////////////////////
// ..c\javascript\console\util\MessageBox.js
//////////////////////
/*
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/javascript/console/util/MessageBox.js $
 *  $Author: mlange $
 *  $Date: 2010-10-28 14:19:19 -0500 (Thu, 28 Oct 2010) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

Ext.namespace("RP.Moca.util");

RP.Moca.util.Msg = {
    alert: function(title, msg) {
	var fixedMsg = msg.replace(/\n/g, '<br>');
	Ext.Msg.alert(title, fixedMsg + '<br>');
    }
};
//////////////////////
// ..c\javascript\console\util\Ajax.js
//////////////////////
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


//////////////////////
// ..c\javascript\console\util\CheckBoxColumn.js
//////////////////////
/**
 * @class Ext.ux.CheckColumn
 * @extends Ext.grid.column.Column
 * <p>A Header subclass which renders a checkbox in each column cell which toggles the truthiness of the associated data field on click.</p>
 * <p><b>Note. As of ExtJS 3.3 this no longer has to be configured as a plugin of the GridPanel.</b></p>
 * <p>Example usage:</p>
 * <pre><code>
// create the grid
var grid = Ext.create('Ext.grid.Panel', {
    ...
    columns: [{
           text: 'Foo',
           ...
        },{
           xtype: 'checkcolumn',
           text: 'Indoor?',
           dataIndex: 'indoor',
           width: 55
        }
    ]
    ...
});
 * </code></pre>
 * In addition to toggling a Boolean value within the record data, this
 * class adds or removes a css class <tt>'x-grid-checked'</tt> on the td
 * based on whether or not it is checked to alter the background image used
 * for a column.
 */
Ext.define('Ext.ux.CheckColumn', {
    extend: 'Ext.grid.column.Column',
    alias: 'widget.checkcolumn',
    
    constructor: function() {
        this.addEvents(
            /**
             * @event checkchange
             * Fires when the checked state of a row changes
             * @param {Ext.ux.CheckColumn} this
             * @param {Number} rowIndex The row index
             * @param {Boolean} checked True if the box is checked
             */
            'checkchange'
        );
        this.callParent(arguments);
    },

    // Note: class names are not placed on the prototype bc renderer scope
    // is not in the header.
    renderer : function(value){
        var cssPrefix = Ext.baseCSSPrefix,
            cls = [cssPrefix + 'grid-checkheader'];

        cls.push("force-dimension");
        cls.push(value ? "rp-check" : "rp-uncheck");

        return '<div class="' + (cls.join(' ')) + '">&#160;</div>';
    }
});
//////////////////////
// ..c\javascript\console\util\Format.js
//////////////////////
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

//////////////////////
// ..c\javascript\console\util\HyperLink.js
//////////////////////
RP.Moca.util.HyperLink = function() {
    return {
        sessions: function (value) {
            if (value === null || value == "") {
                return "";
            }
    
            // Build the URL we're going to link to.
            var protocol = document.location.protocol;
            var host = document.location.host;
            var url = protocol + '//' + host + '/console/console.do#moca.console.taskflow:sessions';
    
            // Build the HTML with the link and value.
            var html = '<a href="' + url + '">' + value + '</a>';
    
            return html;
        },
        
        node: function (value) {
            if (value === null || value == "") {
                return "";
            }
    
            var url = value + "/console";
    
            // Build the HTML with the link and value.
            var html = '<a href="' + url + '">' + value + '</a>';
    
            return html;
        }
    };
}();

//////////////////////
// ..c\javascript\console\util\TracingBox.js
//////////////////////
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

//////////////////////
// ..c\javascript\console\util\UsageChart.js
//////////////////////
Ext.define("RP.Moca.util.UsageChart", {
    extend: "Ext.panel.Panel",
 
    initComponent: function() {
        this.store.on("load", this.drawChart, this);

        this.drawComponent = new Ext.draw.Component({ viewBox: false });

        Ext.apply(this, {
            layout: 'fit',
            padding: "20px 20px 0",
            flex: 1,
            border: false,
            items: this.drawComponent
        });

        this.callParent(arguments);
    },

    drawChart: function() {

        if (!this.rendered) {
            this.on('afterlayout', this.drawChart, this, { single: true });
            return;
        }

        this.drawComponent.surface.removeAll();

        var data  = this.store.getAt(0).data,
            width = this.getWidth(),
            maxWidth     = data[this.max],
            currentWidth = data[this.current]/maxWidth * width,
            peakWidth    = data[this.peak]/maxWidth * width;

        this.drawComponent.surface.add({
            type: 'rect',
            x: 0, y: 0, height: 20,
            width: width,
            fill: '#7FBF7F'
        }).show(true);

        this.drawComponent.surface.add({
            type: 'rect',
            x: 0, y: 0, height: 20,
            width: peakWidth,
            fill: '#FFFF7F'
        }).show(true);

        this.drawComponent.surface.add({
            type: 'rect',
            x: 0, y: 0, height: 20,
            width: currentWidth,
            fill: '#E57F7F'
        }).show(true);
    }
});
//////////////////////
// ..c\javascript\console\util\BaseTaskForm.js
//////////////////////
Ext.ns('RP.util.taskflow');

/**
 * Used as a base class for task forms that are launched by the taskflow widgets.  This
 * class adds Application Event handling for a panel.  TaskflowFrame interfaces with
 * this to automatically set this panel to the Awake or Asleep state when it switches
 * task forms.  A task form configures Application Events to listen to by setting the
 * "appEventHandlers" config option.
 */
Ext.define("RP.util.taskflow.BaseTaskForm", {
    extend: "RP.taskflow.BaseTaskForm",

    initComponent: function() {
    
        Ext.QuickTips.init();

        // Create the store containing the list of cluster nodes.
        this.myStore = new RP.util.NodeComboBoxStore();
     
        // Create a listener to configure the combo box after its store loads.
        this.myStore.on('load', this.configureComboBoxOnStoreLoad, this);
        
        // Actually load the combo box's store.
        this.myStore.load();

        this.callParent(arguments);
    },
    
    configureComboBoxOnStoreLoad: function(store) {
        if (store.getCount() === 0) {
            Ext.getCmp("clusteringWidget").hide();
            Ext.getCmp("clusteringAsyncWidget").hide();
        }
    }
    
});

//////////////////////
// ..c\javascript\console\util\RowExpander.js
//////////////////////
/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial Software License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
// feature idea to enable Ajax loading and then the content
// cache would actually make sense. Should we dictate that they use
// data or support raw html as well?

/**
 * @class Ext.ux.RowExpander
 * @extends Ext.AbstractPlugin
 * Plugin (ptype = 'rowexpander') that adds the ability to have a Column in a grid which enables
 * a second row body which expands/contracts.  The expand/contract behavior is configurable to react
 * on clicking of the column, double click of the row, and/or hitting enter while a row is selected.
 *
 * @ptype rowexpander
 */
Ext.define('Ext.ux.RowExpander', {
    extend: 'Ext.AbstractPlugin',

    requires: [
        'Ext.grid.feature.RowBody',
        'Ext.grid.feature.RowWrap'
    ],

    alias: 'plugin.rowexpander',

    /**
     * @cfg {String/Ext.Template} rowBodyTpl
     * A string or template to use for the row body 
     */
    rowBodyTpl: null,

    /**
     * @cfg {Boolean} expandOnEnter
     * <tt>true</tt> to toggle selected row(s) between expanded/collapsed when the enter
     * key is pressed (defaults to <tt>true</tt>).
     */
    expandOnEnter: true,

    /**
     * @cfg {Boolean} expandOnDblClick
     * <tt>true</tt> to toggle a row between expanded/collapsed when double clicked
     * (defaults to <tt>true</tt>).
     */
    expandOnDblClick: true,

    /**
     * @cfg {Boolean} selectRowOnExpand
     * <tt>true</tt> to select a row when clicking on the expander icon
     * (defaults to <tt>false</tt>).
     */
    selectRowOnExpand: false,

    /**
     * @cfg {String} rowBodyTrSelector
     * The seletor string to use to grab the row body elements. (defaults to '.x-grid-rowbody-tr') 
     */
    rowBodyTrSelector: '.x-grid-rowbody-tr',

    /**
     * @cfg {String} rowBodyHiddenCls
     * The class to add to the row body when it should be hidden. (defaults to 'x-grid-row-body-hidden')
     */
    rowBodyHiddenCls: 'x-grid-row-body-hidden',

    /**
     * @cfg {String} rowCollapsedCls
     * The class to add to a collapsed row element. (defaults to 'x-grid-row-collapsed') 
     */
    rowCollapsedCls: 'x-grid-row-collapsed',



    /**
     * The renderer for the expand/collapse column.
     *
     * @param {Object} value The value of the current cell
     * @param {Object} metaData A collection of metadata about the current cell; can be used or modified
     * by the renderer. Recognized properties are: tdCls, tdAttr, and style
     * @param {Ext.data.Model} record The record for the current row
     * @param {Number} rowIdx The index of the current row
     * @param {Number} colIdx The Index of the current column
     */
    renderer: function(value, metadata, record, rowIdx, colIdx) {
        if (colIdx === 0) {
            metadata.tdCls = 'x-grid-td-expander';
        }
        return '<div class="x-grid-row-expander">&#160;</div>';
    },

    /**
     * @event expandbody
     * <b<Fired through the grid's View</b>
     * @param {HtmlElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HtmlElement} expandRow The &lt;tr> element containing the expanded data.
     */
    /**
     * @event collapsebody
     * <b<Fired through the grid's View.</b>
     * @param {HtmlElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HtmlElement} expandRow The &lt;tr> element containing the expanded data.
     */

    constructor: function() {
        this.callParent(arguments);
        var grid = this.getCmp();
        this.recordsExpanded = {};
        // <debug>
        if (!this.rowBodyTpl) {
            Ext.Error.raise("The 'rowBodyTpl' config is required and is not defined.");
        }
        // </debug>
        
        var rowBodyTpl = this.rowBodyTpl instanceof Ext.XTemplate ? this.rowBodyTpl : 
            Ext.create('Ext.XTemplate', this.rowBodyTpl),
            features = [{
                ftype: 'rowbody',
                columnId: this.getHeaderId(),
                recordsExpanded: this.recordsExpanded,
                rowBodyHiddenCls: this.rowBodyHiddenCls,
                rowCollapsedCls: this.rowCollapsedCls,
                getAdditionalData: this.getRowBodyFeatureData,
                getRowBodyContents: function(data) {
                    return rowBodyTpl.applyTemplate(data);
                }
            },{
                ftype: 'rowwrap'
            }];

        if (grid.features) {
            grid.features = features.concat(grid.features);
        } else {
            grid.features = features;
        }

        // NOTE: features have to be added before init (before Table.initComponent)
    },

    /**
     * Initialize the plugin
     * @param {Ext.grid.Panel} grid The grid this plugin was added to. 
     */
    init: function(grid) {
        this.callParent(arguments);

        // Columns have to be added in init (after columns has been used to create the
        // headerCt). Otherwise, shared column configs get corrupted, e.g., if put in the
        // prototype.
        grid.headerCt.insert(0, this.getHeaderConfig());
        grid.on('render', this.bindView, this, {single: true});
    },

    /**
     * Get the identifier for the expand/collapse column. This method will generate a unique
     * id string if one does not exist for the header yet.
     * @return {String} The identifier
     */
    getHeaderId: function() {
        if (!this.headerId) {
            this.headerId = Ext.id();
        }
        return this.headerId;
    },

    /**
     * @inheritdoc Ext.grid.feature.RowBody#getAdditionalData
     */
    getRowBodyFeatureData: function(data, idx, record, orig) {
        var o = Ext.grid.feature.RowBody.prototype.getAdditionalData.apply(this, arguments),
            id = this.columnId;
        o.rowBodyColspan = o.rowBodyColspan - 1;
        o.rowBody = this.getRowBodyContents(data);
        o.rowCls = this.recordsExpanded[record.internalId] ? '' : this.rowCollapsedCls;
        o.rowBodyCls = this.recordsExpanded[record.internalId] ? '' : this.rowBodyHiddenCls;
        o[id + '-tdAttr'] = ' valign="top" rowspan="2" ';
        if (orig[id+'-tdAttr']) {
            o[id+'-tdAttr'] += orig[id+'-tdAttr'];
        }
        return o;
    },

    /**
     * Attach keyboard and click event listeners to the view. If the view is not yet rendered when this
     * method is called, it will listen for the render event and attach the listeners at that point.
     */
    bindView: function() {
        var view = this.getCmp().getView(),
            viewEl;

        if (!view.rendered) {
            view.on('render', this.bindView, this, {single: true});
        } else {
            viewEl = view.getEl();
            if (this.expandOnEnter) {
                this.keyNav = Ext.create('Ext.KeyNav', viewEl, {
                    'enter' : this.onEnter,
                    scope: this
                });
            }
            if (this.expandOnDblClick) {
                view.on('itemdblclick', this.onDblClick, this);
            }
            this.view = view;
        }
    },

    /**
     * Handle the ENTER key event on the view. If {@link #expandOnEnter} is true, the row
     * will be toggled between the expanded and collapsed states when ENTER is pressed. Otherwise
     * ENTER will be ignored.
     * @param {Ext.EventObject} e The event object  
     */
    onEnter: function(e) {
        var view = this.view,
            ds   = view.store,
            sm   = view.getSelectionModel(),
            sels = sm.getSelection(),
            ln   = sels.length,
            i = 0,
            rowIdx;

        for (; i < ln; i++) {
            rowIdx = ds.indexOf(sels[i]);
            this.toggleRow(rowIdx);
        }
    },

    /**
     * Toggle the specified row between being expanded or collapsed.
     *
     * @param {Number} rowIdx The index of the row to expand or collapse
     */
    toggleRow: function(rowIdx) {
        var rowNode = this.getCmp().view.getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector),
            record = this.getCmp().view.getRecord(rowNode),
            grid = this.getCmp();

        if (row.hasCls(this.rowCollapsedCls)) {
            row.removeCls(this.rowCollapsedCls);
            nextBd.removeCls(this.rowBodyHiddenCls);
            this.recordsExpanded[record.internalId] = true;
            this.view.fireEvent('expandbody', rowNode, record, nextBd.dom);
        } else {
            row.addCls(this.rowCollapsedCls);
            nextBd.addCls(this.rowBodyHiddenCls);
            this.recordsExpanded[record.internalId] = false;
            this.view.fireEvent('collapsebody', rowNode, record, nextBd.dom);
        }

        this.view.up('gridpanel').view.refresh();
    },

    /**
     * Handle the double click event on a row. If {@link #expandOnDblClick} is true, the row will be
     * expanded or collapsed (depending on its previous state). See also {@link Ext.view.View#itemdblclick}
     *
     * @param {Ext.grid.View} view The view the click event occured on
     * @param {Ext.data.Model} record The record that belongs to the item that was clicked on
     * @param {HTMLElement} item The clicked item's element
     * @param {Number} rowIdx The item's index
     * @param {Ext.EventObject} e The raw event object
     */
    onDblClick: function(view, record, item, rowIdx, e) {
        this.toggleRow(rowIdx);
    },

    /**
     * @return {Object} the configuration for the expand/collapse column
     */
    getHeaderConfig: function() {
        var me                = this,
            toggleRow         = Ext.Function.bind(me.toggleRow, me),
            selectRowOnExpand = me.selectRowOnExpand;

        return {
            id: this.getHeaderId(),
            width: 24,
            sortable: false,
            resizable: true,
            draggable: false,
            hideable: false,
            menuDisabled: true,
            cls: Ext.baseCSSPrefix + 'grid-header-special',
            renderer: this.renderer,
            // changed this from mousedown to click for Jesse / testing
            processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
                if (type == "click" && e.getTarget('.x-grid-row-expander')) {
                    var row = e.getTarget('.x-grid-row');
                    toggleRow(row);
                    return selectRowOnExpand;
                }
            }
        };
    }
});
//////////////////////
// ..c\javascript\console\util\Role.js
//////////////////////
RP.Moca.util.Role = function() {

return {
    isReadOnly: function (params) {
        // Apply the given configuration to ourselves.
        Ext.apply(this, params);

        RP.Moca.util.Ajax.requestWithTextParams({
                scope: params.scope,
                url: '/console',
                method: 'GET',
                params: {
                    m: 'getUserRole'
                },
                success : params.success
        });
    }
};
}();
//////////////////////
// ..c\javascript\console\util\MaintenanceBox.js
//////////////////////
// Maintenance
Ext.define('RP.Moca.Console.Util.Maintenance', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
    
        Ext.apply(this, {
            id: 'maintenance',
            layout: 'column',
            region: 'west',
            flex: 4,
            border: false,
            defaults: {
                xtype: 'textarea',
                labelAlign: 'top',
                padding: 9,
                height: 45
            },
            items: [{
                fieldLabel: 'Job ID',
                itemId: 'job_id',
                id: 'job_id',
                width: '95%'
            },{
                fieldLabel: 'Task ID',
                itemId: 'task_id',
                id: 'task_id',
                width: '95%'
            },{
                fieldLabel: 'Name',
                itemId: 'name',
                id: 'name',
                width: '95%'
            },{
                fieldLabel: 'Group Name',
                itemId: 'grp_nam',
                id: 'grp_nam',
                width: '46%'
            },{
                fieldLabel: 'Role ID',
                xtype: 'combobox',
                itemId: 'role_id',
                id: 'role_id',
                store: ['*']
            },{
                fieldLabel: 'Command',
                itemId: 'command',
                id: 'command',
                width: '95%',
                autoScroll: true,
                height: 380
            },{
                xtype: 'hidden',
                itemId: 'type',
                id: 'type'
            },{
                xtype: 'hidden',
                itemId: 'task_typ',
                id: 'task_typ'
            },{
                xtype: 'hidden',
                itemId: 'action',
                id: 'action'
            }]
        });
        
        this.callParent(arguments);
    
    }

});

// Scheduled Panel
Ext.define('RP.Moca.Console.Util.ScheduledMaint', {
    extend: 'Ext.form.Panel',
    
    initComponent: function () {
    
        var times = Ext.create('Ext.data.Store', {
            fields: ['time','hour'],
            data: [
                {'time':'Every Hour','hour':'*'},
                {'time':'12:00','hour':'0'},
                {'time':'1:00','hour':'1'},
                {'time':'2:00','hour':'2'},
                {'time':'3:00','hour':'3'},
                {'time':'4:00','hour':'4'},
                {'time':'5:00','hour':'5'},
                {'time':'6:00','hour':'6'},
                {'time':'7:00','hour':'7'},
                {'time':'8:00','hour':'8'},
                {'time':'9:00','hour':'9'},
                {'time':'10:00','hour':'10'},
                {'time':'11:00','hour':'11'}
            ]
        });
        
        var dates = Ext.create('Ext.data.Store', {
            fields: ['date','dateNum'],
            data: [
                {'date':'Sunday','dateNum':'1'},
                {'date':'Monday','dateNum':'2'},
                {'date':'Tuesday','dateNum':'3'},
                {'date':'Wednesday','dateNum':'4'},
                {'date':'Thursday','dateNum':'5'},
                {'date':'Friday','dateNum':'6'},
                {'date':'Saturday','dateNum':'7'}
            ]
        });
    
        Ext.apply(this, {
            width: '50%',
            border: false,
            id: 'schedulePanel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            region: 'east',
            flex: 7,
            padding: '5px 5px 5px 5px',
            defaults: {
                padding: '0 0 5px 0'
            },
            items: [
                Ext.create('Ext.form.Panel',{
                    id: 'simpleSchedule',
                    layout: 'column',
                    height: 125,
                    bodyStyle: 'background: #DEDEDE;',
                    defaults: {
                        labelAlign: 'top',
                        padding: 5
                    },
                    items: [{
                        xtype:'label',
                        text: 'Schedule',
                        style: 'font-weight:bold;font-size:14px;',
                        padding: '5px 0 0 5px'
                    }, {
                        xtype: 'hidden',
                        width: '55%',
                        height: 25,
                        padding: 0
                    }, {
                        xtype: 'combobox',
                        store: times,
                        displayField: 'time',
                        valueField: 'hour',
                        fieldLabel: 'Time',
                        itemId: 'schedule_time',
                        id: 'schedule_time',
                        editable: false,
                        listeners: {
                            'select': function(combo, row, index) {
                                if (row[0].data.time == RP.getMessage('rp.moca.common.everyHour')) {
                                    Ext.getCmp('am_time').disable();
                                    Ext.getCmp('am_time').setValue(true);
                                    Ext.getCmp('pm_time').disable();
                                }
                                else {
                                    Ext.getCmp('am_time').enable();
                                    Ext.getCmp('pm_time').enable();
                                }
                                
                                Ext.getCmp('hours').setValue(row[0].data.hour);
                            },
                            change: function(field, newValue, oldValue) {
                                if (Ext.getCmp('pm_time').value){
                                    Ext.getCmp('hours').setValue(parseInt(newValue, 10)+12);
                                }
                                else {
                                    Ext.getCmp('hours').setValue(newValue);
                                }
                            }
                        }
                    }, {
                        xtype: 'radiofield',
                        boxLabel: 'AM',
                        name: 'time',
                        inputValue: 'am',
                        itemId: 'am_time',
                        id: 'am_time',
                        checked: true,
                        padding: '30px 5px 0 0'
                    }, {
                        xtype: 'radiofield',
                        boxLabel: 'PM',
                        name: 'time',
                        inputValue: 'pm',
                        itemId: 'pm_time',
                        id: 'pm_time',
                        padding: '30px 15px 0 0',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                if (newValue) {
                                    Ext.getCmp('hours').setValue(parseInt(Ext.getCmp('hours').value, 10)+12);
                                }
                                if (oldValue) {
                                    Ext.getCmp('hours').setValue(parseInt(Ext.getCmp('hours').value, 10)-12);
                                }
                            }
                        }
                    }, {
                        xtype: 'combobox',
                        store: dates,
                        displayField: 'date',
                        valueField: 'dateNum',
                        fieldLabel: 'Repeat',
                        itemID: 'schedule_repeat',
                        id: 'schedule_repeat',
                        multiSelect: true,
                        editable: false,
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                Ext.getCmp('dayWeek').setValue(newValue);
                            }
                        }
                    }],
                    dockedItems: [{
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            { xtype: 'tbfill' },
                            { xtype: 'button', text: 'Advanced Schedule', handler: function() {
                                            Ext.getCmp('simpleSchedule').hide();
                                            Ext.getCmp('advancedSchedule').setHeight(125);
                                            Ext.getCmp('advancedSchedule').show(); } },
                            { xtype: 'tbfill' }
                        ],
                        style: 'background: #DEDEDE;'
                    }]
            }),
                Ext.create('Ext.form.Panel',{
                    id: 'advancedSchedule',
                    hidden: true,
                    layout: 'column',
                    height: 125,
                    bodyStyle: 'background: #DEDEDE;',
                    defaults: {
                        labelAlign: 'top',
                        padding: 5,
                        xtype: 'textarea',
                        hidden: true,
                        value: '0'
                    },
                    items: [{
                        xtype:'label',
                        text: 'Schedule',
                        style: 'font-weight:bold;font-size:14px;',
                        padding: '5px 0 0 5px',
                        hidden: false
                    }, {
                        xtype: 'hidden',
                        width: '55%',
                        height: 25,
                        padding: 0,
                        hidden: false
                    }, {
                        itemId: 'schedule',
                        id: 'schedule',
                        height: 30,
                        width: '60%',
                        hidden: false,
                        value: '0 0 0 * * ?',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                if (Ext.getCmp('simpleScheduleButton').disabled) {
                                    var schedule = newValue;
                                    var complicated = schedule.split(' ');
                                    complicated.splice(5, 1);
                                    complicated = complicated.join(' ');

                                    if (schedule.match('/') || complicated.match(',')){
                                        Ext.getCmp('simpleScheduleButton').disable();
                                        Ext.getCmp('tooAdvanced').show();
                                    }
                                    else {
                                        Ext.getCmp('simpleScheduleButton').enable();
                                        Ext.getCmp('tooAdvanced').hide();
                                    }
                                }
                            }
                        }
                    }, {
                        xtype: 'label',
                        cls: 'rp-form-field-help-link',
                        hidden: false,
                        style: 'float: left;',
                        margin: '9px 0 0 0',
                        listeners: {
                            click: {
                                element: 'el',
                                fn: function() {
                                    if (!Ext.getCmp('scheduleHelp')) {
                                        var panel = Ext.create('RP.Moca.Console.Util.ScheduleHelp', {});
                                        var topPanel;
                                        if (Ext.getCmp('addJobPanel')) {
                                            topPanel = Ext.getCmp('addJobPanel');
                                        }
                                        else if (Ext.getCmp('modifyJobPanel')) {
                                            topPanel = Ext.getCmp('modifyJobPanel');
                                        }
                                        else if (Ext.getCmp('copyJobPanel')) {
                                            topPanel = Ext.getCmp('copyJobPanel');
                                        }
                                        topPanel.setWidth(1000);
                                        topPanel.down('#advancedSchedule').setHeight(100);
                                        topPanel.down('#tracingPanel').setHeight(225);
                                        topPanel.down('#command').setHeight(200);
                                        topPanel.add(panel);
                                        topPanel.center();
                                        panel.animate({
                                            duration: 500,
                                            to: {
                                                width: 370
                                            },
                                            dynamic: true
                                        });
                                    }
                                }
                            }
                        }
                    }, {
                        xtype: 'label',
                        itemId: 'tooAdvanced',
                        id: 'tooAdvanced',
                        text: 'The schedule is too complicated for the simple view.',
                        width: '80%'
                    }, {
                        itemId: 'hours',
                        id: 'hours',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                if (parseInt(newValue, 10) < 12 && Ext.getCmp('pm_time').value) {
                                    field.setValue(oldValue);
                                }
                                else {
                                    field.setValue(newValue);
                                }
                                
                                var schedule = Ext.getCmp('schedule').value.split(' ');
                                schedule.splice(2, 1, field.value);
                                schedule = schedule.join(' ');
                                Ext.getCmp('schedule').setValue(schedule);
                            }
                        }
                    }, {
                        itemId: 'dayWeek',
                        id: 'dayWeek',
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                var schedule = Ext.getCmp('schedule').value.split(' ');
                                if (newValue && newValue.length < 13 && newValue.length % 2 !== 0) {
                                    newValue = newValue.split(',').sort().toString();
                                    schedule.splice(5, 1, newValue);
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                                else if (newValue.length === 13) {
                                    schedule.splice(5, 1, '*');
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                                else if (newValue.length % 2 === 0 && newValue.length !== 0) {
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                                else {
                                    schedule.splice(5, 1, '?');
                                    schedule = schedule.join(' ');
                                    Ext.getCmp('schedule').setValue(schedule);
                                }
                            }
                        }
                    }],
                    dockedItems: [{
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            { xtype: 'tbfill' },
                            { xtype: 'button', 
                                id: 'simpleScheduleButton',
                                itemId: 'simpleScheduleButton',
                                text: 'Simple Schedule', 
                                handler: this.populateSimple
                            },
                            { xtype: 'tbfill' }
                        ],
                        style: 'background: #DEDEDE;'
                    }]
            }),
                Ext.create('RP.Moca.Console.Util.Tracing', {}),
                Ext.create('RP.Moca.Console.Util.Behavior', {})
            ]
        });
        
        this.callParent(arguments);
    },
    
    populateSimple: function() {
        var panel = Ext.getCmp('advancedSchedule');
        var schedule = panel.down('#schedule').value;
        var complicated = schedule.split(' ');
        complicated.splice(5, 1);
        complicated = complicated.join(' ');
        
        if (schedule.match('/') || complicated.match(',')){
            panel.down('#simpleScheduleButton').disable();
            panel.down('#tooAdvanced').show();
        }
        else{ 
            Ext.getCmp('simpleSchedule').show();
            Ext.getCmp('advancedSchedule').hide();
            if (Ext.getCmp('scheduleHelp')) {
                Ext.getCmp('scheduleHelp').close();
            }
            Ext.getCmp('tracingPanel').setHeight(200);
            Ext.getCmp('command').setHeight(255);
            
            var cron = Ext.getCmp('schedule').value.split(' ');
            if (cron.length >= 6) {
                var days = cron.splice(5,1);
                var hours = cron.splice(2,1);
                if (days[0].length > 0) {
                    var weekDays;
                    if (days[0] === '*') { 
                        weekDays = ['1','2','3','4','5','6','7'];
                    }
                    else if (days[0] === '?') {
                        weekDays = '';
                    }
                    else if (days[0].length % 2 !== 0) {
                        weekDays = days[0].split(',');
                    }
                    else {
                        weekDays = days[0];
                    }
                    Ext.getCmp('schedule_repeat').setValue(weekDays);
                }
                if (hours === '*') {
                    Ext.getCmp('schedule_time').setValue('*');
                }
                else if (hours >= 12) {
                    hours = hours - 12;
                    Ext.getCmp('pm_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
                else if (hours < 12) {
                    Ext.getCmp('am_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
            }
        }
    } 
});

// Schedule Help Panel
Ext.define('RP.Moca.Console.Util.ScheduleHelp', {
    extend: 'Ext.panel.Panel',
    
    initComponent: function() {
        Ext.apply(this, {
            title: 'Schedule Helper',
            width: 0,
            height: '100%',
            closable: true,
            layout: 'vbox',
            autoScroll: true,
            id: 'scheduleHelp',
            items: [{
                xtype: 'label',
                text: 'The expression[cron] should be string comprising of 6 or 7 fields separated by white space. Fields can contain any of the allowed values, along with various combinations of the allowed special characters for that field.',
                margin: 5,
                width: 353
            }, {
                xtype: 'fieldset',
                title: 'Format',
                layout: {
                    type: 'table',
                    columns: 4
                },
                margin: 5,
                width: 353,
                height: 250,
                defaults: {
                    border: false,
                    padding: '0 10px 10px 0'
                },
                items: [{html: 'Field Name', style: 'font-weight:bold;'},{html: 'Mandatory', style: 'font-weight:bold;'},{html: 'Allowed Values', style: 'font-weight:bold;'},{html: 'Allowed Special Characters', style: 'font-weight:bold;'},
                        {html: 'seconds'},{html: 'YES'},{html: '0-59'},{html: ', - * /'},
                        {html: 'minutes'},{html: 'YES'},{html: '0-59'},{html: ', - * /'},
                        {html: 'hours'},{html: 'YES'},{html: '0-23'},{html: ', - * /'},
                        {html: 'Day of Month'},{html: 'YES'},{html: '1-31'},{html: ', - * ? / L W C'},
                        {html: 'Month'},{html: 'YES'},{html: '1-12 or JAN-DEC'},{html: ', - * /'},
                        {html: 'Day of Week'},{html: 'YES'},{html: '1-7 or SUN-SAT'},{html: ', - * ? / L C #'},
                        {html: 'Year (Optional)'},{html: 'NO'},{html: 'empty, 1970-2099'},{html: ', - * /'}]
            }, {
                xtype: 'fieldset',
                title: 'Special Characters',
                margin: 5,
                width: 353,
                items: [{
                    xtype: 'label',
                    html: '[*] - Expression will match all values <br/> "* * * * * ? 2010" - Execute every second in the year 2010 <br/><br/> [/] - Used to describe increments <br/> "10/15 * * * * ?" - Execute every 15 seconds starting at 10 seconds <br/><br/> [,] - Used to separate items in a list <br/> "0 0 0 1,15 * ?" - Execute every 1st and 15th of the month at midnight <br/><br/> [-] - Used to define a range <br/> "0 0 0 ? * MON-WED" - Execute at midnight Monday through Wednesday <br/><br/> [?] - Used to omit specification for day of week or month <br/> "0 0 0 1 * ?" - Execute at midnight on the first of the month regardless of day of week <br/><br/> [L] - Stands for "last" <br/> "0 0 0 L * ?" - Execute at midnight on the last day of the month <br/> "0 0 0 ? * 6L" - Execute at midnight on the last Friday of the month <br/><br/> [#] - Used to specify the occurence of a day in a month <br/> 0 0 0 ? * SUN#3" - Execute on the third Sunday of the month at midnight <br/><br/> [W] - Denotes the nearest weekday <br/> "0 0 0 15W * ?" - Execute on the weekday nearest to the 15th at midnight <br/> "0 0 0 LW * ?" - Execute on the weekday nearest to the end of the month at midnight'
                }]
            }],
            listeners: {
                close: {
                    fn: function(el, e) {
                        var panel = el.up();
                        panel.setWidth(800);
                        panel.down('#command').setHeight(255);
                        panel.center();
                    }
                }
            }
        });
        this.callParent(arguments);
    }
});

// Timer Panel
Ext.define('RP.Moca.Console.Util.TimerMaint', {
    extend: 'Ext.form.Panel',
    
    initComponent: function () {
        Ext.apply(this, {
            border: false,
            id: 'timerPanel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            region: 'east',
            padding: 5,
            flex: 5,
            defaults: {
                padding: '0 0 5px 0'
            },
            items: [
                Ext.create('Ext.form.Panel',{
                    itemID: 'timerSection',
                    layout: 'column',
                    height: 100,
                    bodyStyle: 'background: #DEDEDE;',
                    padding: '0 0 5px 0',
                    defaults: {
                        xtype: 'textarea',
                        height: 45,
                        labelAlign: 'top',
                        padding: 5
                    },
                    items: [{
                        xtype:'label',
                        text: 'Timer',
                        style: 'font-weight:bold;font-size:14px;',
                        height: 30
                    }, {
                        xtype: 'hidden',
                        width: '100%',
                        height: 10,
                        padding: 0
                    }, {
                        fieldLabel: 'Interval (Seconds)',
                        itemId: 'timer',
                        id: 'timer'
                    }, {
                        fieldLabel: 'Start Delay (Seconds)',
                        itemId: 'start_delay',
                        id: 'start_delay'
                    }]
            }),
                Ext.create('RP.Moca.Console.Util.Tracing', {}),
                Ext.create('RP.Moca.Console.Util.Behavior', {})
            ]
        });
        
        this.callParent(arguments);
    }
});

// Tracing Panel
Ext.define('RP.Moca.Console.Util.Tracing', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        Ext.apply(this, {
            id: 'tracingPanel',
            height: 200,
            bodyStyle: 'background: #DEDEDE;',
            defaults: {
                labelAlign: 'top',
                padding: '5px 5px 5px 5px',
                width: '92%'
            },
            items: [{
                xtype:'label',
                text: 'Tracing',
                style: 'font-weight:bold;font-size:14px;'
            }, {
                xtype: 'hidden',
                width: '100%',
                height: 20,
                padding: 0
            }, {
                xtype: 'rpuxToggleField',
                offText: 'Off',
                onText: 'On',
                itemId: 'trace_level',
                fieldLabel: 'Tracing Enabled',
                labelAlign: 'top'
            }, {
                fieldLabel: 'Start In Directory',
                xtype: 'textarea',
                itemId: 'run_dir',
                height: 50,
                width: '98%',
                id: 'run_dir'
            }, {
                fieldLabel: 'Trace File',
                xtype: 'textarea',
                height: 92,
                width: '98%',
                itemId: 'log_file',
                id: 'log_file'
            }]
        });
        this.callParent(arguments);
    }
});

// Behavior Panel
Ext.define('RP.Moca.Console.Util.Behavior', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        Ext.apply(this, {
            id: 'behaviorPanel',
            bodyStyle: 'background: #DEDEDE;',
            height: 100,
            defaults: {
                padding: 5
            },
            items: [{
                xtype:'label',
                text: 'Behavior',
                style: 'font-weight:bold;font-size:14px;'
            }, {
                xtype: 'hidden',
                width: '100%',
                height: 10,
                padding: 0
            }, {
                xtype: 'container',
                layout: 'hbox',
                items:[{
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Enabled',
                    labelAlign: 'top',
                    itemId: 'enabled',
                    id: 'enabled'
                }, {
                    itemId: 'spacer1',
                    width: 100
                }, {
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Overlap Executions',
                    labelAlign: 'top',
                    itemId: 'overlap',
                    id: 'overlap'
                }, {
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Auto Start',
                    labelAlign: 'top',
                    itemId: 'auto_start',
                    id: 'auto_start'
                }, {
                    itemId: 'spacer2',
                    width: 100
                }, {
                    xtype: 'rpuxToggleField',
                    offText: 'Off',
                    onText: 'On',
                    fieldLabel: 'Restart on Termination',
                    labelAlign: 'top',
                    itemId: 'restart',
                    id: 'restart'
                }]
            }]
        });
        this.callParent(arguments);
    }
});

// Jobs Information Panel
Ext.define('RP.Moca.Console.Util.JobsInformation', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
    
        // The Information Panel Tool Bar
        var myInfoToolbar = [{
            xtype: 'toolbar',
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{ 
                xtype: 'tbfill'
            },{
                text: 'General',
                id: 'generalInfoButton',
                pressed: true,
                enableToggle: true,
                toggleGroup: 'JobGenEnv',
                handler: function() {
                    if (Ext.getCmp('scheduledJobsButton').pressed) {
                        Ext.getCmp('informationPanel').getLayout().setActiveItem('schedulePanel');
                    }
                    if (Ext.getCmp('timerJobsButton').pressed) {
                        Ext.getCmp('informationPanel').getLayout().setActiveItem('timerPanel');
                    }    
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Environment',
                id: 'environmentInfoButton',
                toggleGroup: 'JobGenEnv',
                enableToggle: true,
                handler: function() {
                    Ext.getCmp('informationPanel').getLayout().setActiveItem('envPanel');
                },
                style: 'padding:0;margin:0;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            }]
        }];
        
        // Apply some stuff
        Ext.apply(this, {
            id: 'informationPanel',
            layout: 'card',
            region: 'east',
            paddingz: 1,
            flex: 5,
            dockedItems: myInfoToolbar,
            items: [
                Ext.create('RP.Moca.Console.Util.JobEnvironment', {
                    job_id: this.initialConfig.job_id,
                    id: 'mocaJobEnvGrid'
                })
            ]
        });

        this.callParent(arguments);
    }
});

Ext.define('JobEnvDefinition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'job_id',     type: 'string'},
        {name: 'var_nam',      type: 'string'},
        {name: 'value',     type: 'string'},
        {name: 'isNew',     type: 'boolean'}
    ],
    validations: [
        {type: 'length',    field: 'job_id',     min: 1},
        {type: 'length',    field: 'var_nam',     min: 1},
        {type: 'length',    field: 'value',     min: 1}
    ]
});

// Environment Panel
Ext.define('RP.Moca.Console.Util.JobEnvironment', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        var me = this;

        me.jobEnvironmentStore = Ext.create('Ext.data.ArrayStore', {
            autoDestroy: true,
            model: JobEnvDefinition,
            fields: [
                {name: 'var_nam'},
                {name: 'value'},
                {name: 'isNew'}
            ],
            pruneModifiedRecords: true,
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({}),
                extraParams: {
                    m: 'getJobEnvironment',
                    job_id: this.initialConfig.job_id
                },
                simpleSortMode: true
            }
        });
        
        // Set overlay
        var loaded = false;
        me.on('render', function() {
            // only set the overlay when store isnt loaded yet
            // and this isnt an 'add' form
            if (!loaded && me.initialConfig.job_id !== undefined) {
                me.setLoading(true);
            }
        });
        
        // Load our store if job_id is fixed
        if (me.initialConfig.job_id) {
            me.jobEnvironmentStore.load({
                scope: me,
                callback: function(records, operation, success) {
                    loaded = true;
                    me.setLoading(false);
                }
            });
        }
        
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1,
            listeners: {
                beforeedit: function(editor, e)  {
                    // if record is old, disable editing of column 1
                    var isNew = e.record.get('isNew');
                    if (!(isNew) && e.colIdx === 0) {
                        return false;
                    }
                }   
            }
        });
        
        Ext.apply(this, {
            layout: 'fit',
            id: 'envPanel',
            height: 200,
            bodyStyle: 'background: #DEDEDE;',
            defaults: {
                xtype: 'textarea',
                height: 35,
                labelAlign: 'top',
                padding: 2,
                width: '92%'
            },
            bbar: [{ 
                xtype: 'tbfill'
            }, {
                text: 'Add Variable',
                handler : function(){
                    var jobId = Ext.getCmp('maintenance').down('#job_id').value;
                    
                    if (!jobId || Ext.isEmpty(jobId)) {
                        Ext.Msg.show({
                            title:'Warning',
                            msg: 'Job ID field is required to add variables.',
                            buttons: Ext.Msg.OK
                        });
                    }
                    else {
                        var r = Ext.create('JobEnvDefinition', {
                            job_id: jobId,
                            var_nam: '',
                            value: '',
                            isNew: true
                        });
                        this.jobEnvironmentStore.insert(0, r);
                        cellEditing.startEditByPosition({row: 0, column: 0});
                    }
                },
                scope: this
            }, {
                text: 'Remove Variable',
                handler : function() {
                    var record = this.down('#jobEnvGrid').getSelectionModel().getSelection()[0];
                    if (record) {
                        Ext.Msg.show({
                            title:'Confirm Delete',
                            msg: 'Are you sure you want to delete the selected variable?',
                            buttons: Ext.Msg.OKCANCEL,
                            fn: function(btn) {
                                if (btn == 'cancel') {
                                    //nothing
                                }
                                if (btn == 'ok') {
                                    this.jobEnvironmentStore.remove(record);
                                }
                            },
                            scope: this
                        });
                    }
                },
                scope: this
            },{ 
                xtype: 'tbfill'
            }],
            items: [
                Ext.create('Ext.grid.Panel', {
                    id: 'jobEnvGrid',
                    store: this.jobEnvironmentStore,
                    columns: [{
                        header   : 'Name',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'var_nam'
                    },{
                        header   : 'Value',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'value'
                    }],
                    height: '95%',
                    width: 440,
                    title: 'Environment Variables',
                    selModel: {
                        selType: 'cellmodel'
                    },
                    viewConfig: {
                        stripeRows: true
                    },
                    plugins: [cellEditing]
                })
            ]
        });
        this.callParent(arguments);
    }
});

// Tasks Information Panel
Ext.define('RP.Moca.Console.Util.TasksInformation', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
    
        // The Information Panel Tool Bar
        var myInfoToolbar = [{
            xtype: 'toolbar',
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{ 
                xtype: 'tbfill'
            },{
                text: 'General',
                id: 'generalTaskInfoButton',
                pressed: true,
                enableToggle: true,
                toggleGroup: 'TaskGenEnv',
                handler: function() {
                    Ext.getCmp('taskInformationPanel').getLayout().setActiveItem('timerPanel');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Environment',
                id: 'taskEnvironmentInfoButton',
                toggleGroup: 'TaskGenEnv',
                enableToggle: true,
                handler: function() {
                    Ext.getCmp('taskInformationPanel').getLayout().setActiveItem('taskEnvPanel');
                },
                style: 'padding:0;margin:0;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            }]
        }];
        
        // Apply some stuff
        Ext.apply(this, {
            id: 'taskInformationPanel',
            layout: 'card',
            region: 'east',
            paddingz: 1,
            flex: 5,
            dockedItems: myInfoToolbar,
            items: [
                Ext.create('RP.Moca.Console.Util.TimerMaint', {}),
                Ext.create('RP.Moca.Console.Util.TaskEnvironment', {
                    task_id: this.initialConfig.task_id
                })
            ]
        });

        this.callParent(arguments);
    }
});

Ext.define('TaskEnvDefinition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'var_nam',      type: 'string'},
        {name: 'value',     type: 'string'},
        {name: 'isNew',     type: 'boolean'}
    ],
    validations: [
        {type: 'length',    field: 'var_nam',     min: 1},
        {type: 'length',    field: 'value',     min: 1}
    ]
});

// Task Environment Panel
Ext.define('RP.Moca.Console.Util.TaskEnvironment', {
    extend: 'Ext.form.Panel',
    
    initComponent: function() {
        var me = this;

        me.taskEnvironmentStore = Ext.create('Ext.data.ArrayStore', {
            autoDestroy: true,
            model: TaskEnvDefinition,
            fields: [
                {name: 'var_nam'},
                {name: 'value'},
                {name: 'isNew'}
            ],
            pruneModifiedRecords: true,
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({}),
                extraParams: {
                    m: 'getTaskEnvironment',
                    task_id: this.initialConfig.task_id
                },
                simpleSortMode: true
            }
        });
        
        // Set overlay
        var loaded = false;
        me.on('render', function() {
            // only set the overlay when store isnt loaded yet
            // and this isnt an 'add' form
            if (!loaded && me.initialConfig.task_id !== undefined) {
                me.setLoading(true);
            }
        });
        
        // Load our store if task_id is fixed
        if (me.initialConfig.task_id) {
            me.taskEnvironmentStore.load({
                scope: me,
                callback: function(records, operation, success) {
                    loaded = true;
                    me.setLoading(false);
                }
            });
        }
        
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1,
            listeners: {
                beforeedit: function(editor, e)  {
                    // if record is old, disable editing of column 1
                    var isNew = e.record.get('isNew');
                    if (!(isNew) && e.colIdx === 0) {
                        return false;
                    }
                }   
            }
        });
        
        Ext.apply(me, {
            layout: 'fit',
            id: 'taskEnvPanel',
            height: 200,
            bodyStyle: 'background: #DEDEDE;',
            //padding: '0 0 5px 0',
            defaults: {
                xtype: 'textarea',
                height: 35,
                labelAlign: 'top',
                padding: 2,
                width: '92%'
            },
            bbar: [{ 
                xtype: 'tbfill'
            },{
                text: 'Add Variable',
                handler : function(){
                    var taskId = Ext.getCmp('maintenance').down('#task_id').value;
                    
                    if (!taskId || Ext.isEmpty(taskId)) {
                        Ext.Msg.show({
                            title:'Warning',
                            msg: 'Task ID field is required to add variables.',
                            buttons: Ext.Msg.OK
                        });
                    }
                    else {
                        var r = Ext.create('TaskEnvDefinition', {
                            task_id: taskId,
                            var_nam: '',
                            value: '',
                            isNew: true
                        });
                        me.taskEnvironmentStore.insert(0, r);
                        cellEditing.startEditByPosition({row: 0, column: 0});
                    }
                },
                scope: me
            }, {
                text: 'Remove Variable',
                handler : function() {
                    var record = me.down('#taskEnvGrid').getSelectionModel().getSelection()[0];
                    if (record) {
                        Ext.Msg.show({
                            title:'Confirm Delete',
                            msg: 'Are you sure you want to delete the selected variable?',
                            buttons: Ext.Msg.OKCANCEL,
                            fn: function(btn) {
                                if (btn == 'cancel') {
                                    //nothing
                                }
                                if (btn == 'ok') {
                                    me.taskEnvironmentStore.remove(record);
                                }
                            },
                            scope: me
                        });
                    }
                },
                scope: me
            },{ 
                xtype: 'tbfill'
            }],
            items: [
                Ext.create('Ext.grid.Panel', {
                    id: 'taskEnvGrid',
                    store: me.taskEnvironmentStore,
                    columns: [{
                        header   : 'Name',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'var_nam'
                    },{
                        header   : 'Value',
                        flex     : 1,
                        sortable : true,
                        field: {
                            allowBlank: false
                        },
                        dataIndex: 'value'
                    }],
                    height: '95%',
                    width: 440,
                    title: 'Environment Variables',
                    selModel: {
                        selType: 'cellmodel'
                    },
                    viewConfig: {
                        stripeRows: true
                    },
                    plugins: [cellEditing]
                })
            ]
        });
        me.callParent(arguments);
    }
});


//////////////////////
// ..c\javascript\console\sysinfo\sessions\Sessions.js
//////////////////////
// View
Ext.define("RP.Moca.Console.Sessions.SessionsViewer", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('mocaSessionViewerContainer').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {

        Ext.apply(this, {
            allowRefresh: true,
            title: 'Sessions',
            layout: 'fit',
            border: false,
            items: Ext.create('RP.Moca.Console.Sessions.SessionViewerContainer', {
                padding: '0px 5px 7px 0px',
                layout: 'border',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('mocaSessionViewerContainer').refresh();
                        }
                    })
                ]
            })
        });

        // Call our superclass.
        this.callParent(arguments);
    }
});

Ext.define('RP.Moca.Console.Sessions.SessionViewerContainer', {
    extend: "Ext.panel.Panel",
    id: 'mocaSessionViewerContainer',
    
    initComponent: function () {
    
        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Interrupt Server Thread',
                scope: this,
                handler: this.interruptSession,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Enable/Disable Tracing',
                scope: this,
                handler: this.tracing,
                style: {
                    margin: '3px'
                }
            }]
        });

        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.hide();
                }
            }
        });
    
        Ext.apply(this, {
            dockedItems: myToolbar,
            layout: {
                type: "hbox",
                align: "stretch"
            },
            bodyStyle: {
                background: '#ffffff'
            },
            items: [
                Ext.create("RP.Moca.Console.Sessions.SessionList", {
                    itemId: 'sessionListGrid',
                    //split: true,
                    region: 'west',
                    flex: 7
                }),
                Ext.create("RP.Moca.Console.Sessions.SessionDetails", {
                    region: 'east',
                    flex: 5
                })
            ]
        });
        this.callParent(arguments);
    },
    
    // Handler function to refresh
    refresh: function() {
        var me = Ext.getCmp('sessionList');
        me.store.load();
        me.fireEvent('afterrender', this);
        Ext.getCmp('sessionDetails').clear();
    },

    makeDataRequest: function (config) {
        var defaultConfig = {
            url: '/console',
            scope: this,
            autoLoad: true
        };

        config = Ext.applyIf(defaultConfig, config);

        RP.Moca.util.Ajax.request(config);
    },
    
    // Handler function to interrupt a selected session
    interruptSession: function() {
        var selected = Ext.getCmp('sessionList').selModel.selected.items[0];
        
        // Make sure a row was selected first.
        if (!selected || selected.get("session")) {
            RP.Moca.util.Msg.alert('Console', 'Please select an active session to interrupt and try again.');
            return;
        }
        var sessionName = selected.get("name");
        var threadId = selected.get("threadId");
        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not interrupt server thread.'
        };

        this.makeDataRequest({
            params: {
                name: sessionName,
                threadId: threadId,
                m: 'interruptSession'
            },
            success: function(response, options) {
                Ext.define('interrupt', {
                    extend: 'Ext.data.Model',
                    fields: ['status', 'message', 'data'],
                
                    belongsTo: 'Post'
                });
                var reader = new RP.Moca.Console.Results.ResultsReader({ 
                    model : "interrupt" 
                });

                var records = reader.read(response);

                if (reader.getStatus() === 0) {
                    var me = Ext.getCmp('sessionList');
                    me.store.load();
                    me.fireEvent('afterrender', this);
                }
            },
            failureAlert: myFailureAlert
        });
    },

    // Handler function for enabling tracing
    tracing: function() {
        var selected = Ext.getCmp('sessionList').selModel.selected.items[0];
        
        // Make sure a row was selected first.
        if (!selected) {
            RP.Moca.util.Msg.alert('Console', 'Please select a session to configure tracing on and try again.');
            return;
        }
        
        var sessionName = selected.get("name"),
            traceName = selected.get("traceName"),
            w = new RP.Moca.util.TracingBox(sessionName, traceName);
            
        w.show();
    }
});

// Session List
Ext.define("RP.Moca.Console.Sessions.SessionList", {
    extend: "Ext.grid.Panel",

    // Delegate refresh to parent container
    refresh: function() {
        Ext.getCmp('mocaSessionViewerContainer').refresh();
    },
    
    initComponent: function() {
        // Handler function to display things in the details
        var display = function(sm, records) {
            if (records.length > 0) {
                Ext.getCmp("sessionDetails").display(records[0]);
            }
        };

        var myColumns = [{
            header: 'Session',
            dataIndex: 'name',
            minWidth: 150,
            flex: 2
        }, {
            header: 'Thread ID',
            dataIndex: 'threadId',
            id: 'threadId',
            align: 'right',
            minWidth: 100,
            flex: 1
        }, {
            header: 'Type / Status',
            renderer: function(value, meta, record) {
                return '<div>' + RP.Moca.util.Format.sessionTypeString(record.getData().sessionType) + '</div> <div>' + RP.Moca.util.Format.sessionStatusString(record.getData().status) + '</div>';
            },
            minWidth: 125,
            flex: 2
        }, {
            header: 'Type',
            dataIndex: 'sessionType',
            hidden: true,
            minWidth: 125,
            flex: 2
        }, {
            header: 'Status',
            dataIndex: 'status',
            hidden: true,
            minWidth: 150,
            flex: 2
        }, {
            header: 'Started / Last Command Time',
            minWidth: 220,
            flex: 2,
            renderer: function(value, meta, record) {
                return '<div>' + RP.Moca.util.Format.datetimeString(record.getData().startedTime) + '</div> <div>' + RP.Moca.util.Format.datetimeString(record.getData().lastCommandTime) + '</div>';
            }
        }, {
            header: 'Started Command Time',
            dataIndex: 'startedTime',
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Last Command Time',
            dataIndex: 'lastCommandTime',
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Environment',
            dataIndex: 'environment',
            renderer: RP.Moca.util.Format.objectToString,
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Last Script Time',
            dataIndex: 'lastScriptTime',
            renderer: RP.Moca.util.Format.datetimeString,
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Last SQL Time',
            dataIndex: 'lastSQLTime',
            renderer: RP.Moca.util.Format.datetimeString,
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            header: 'Connected IP Address',
            dataIndex: 'connectedIpAddress',
            minWidth: 175,
            flex: 2,
            hidden: true
        }, {
            xtype: 'checkcolumn',
            header: "Tracing",
            dataIndex: "traceName",
            minWidth: 85,
            flex: 1
        }, {
            header: 'Is Idle',
            dataIndex: 'session',
            minWidth: 85,
            flex: 1,
            hidden: true
        }];

        this.myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({}),
                extraParams: {
                    m: 'getSessions'
                },
                simpleSortMode: true
            },
            fields: [
                'name', 'threadId', 'environment', 'status', 'connectedIpAddress', 'lastCommand',
                'lastSQL', 'lastScript', 'commandPath', 'traceName', 'session', 'sessionType', {
                    name: 'startedTime', type: 'date'
                }, {
                    name: 'lastCommandTime', type: 'date'
                }, {
                    name: 'lastSQLTime', type: 'date'
                }, {
                    name: 'lastScriptTime', type: 'date'
            }],
            sorters: [{
                property: 'status',
                direction: 'DESC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('mocaSessionViewerContainer').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('mocaSessionViewerContainer').setLoading(false);
                }
            }
        });

        // Load our store
        this.myStore.on('load', display, this);

        // Apply some stuff
        Ext.apply(this, {
            border: false,
            id: 'sessionList',
            columns: myColumns,
            store: this.myStore,
            loadMask: false,
            style: 'border-right:1px solid #dedede',
            viewConfig: {
                stripeRows: true,
                emptyText: 'No sessions to display.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            },
            listeners: {
                selectionchange: display
            }
        });

        // Call our superclass
        this.callParent(arguments);
    }    
});

// Session Details
Ext.define("RP.Moca.Console.Sessions.SessionDetails", {
    extend: "Ext.panel.Panel",
    id: 'sessionDetails',
     
    constructor: function (config) {
        var serverThreadStore = Ext.create("RP.Moca.util.Store", {
            proxy: {            
                extraParams: {
                    m: 'getServerThreadInformation'
                }
            },
            fields: [
                { name: 'blockedTime'},
                { name: 'lockOwnerId'}, 
                { name: 'threadState'}, 
                { name: 'blockedCount' },
                { name: 'suspended' },
                { name: 'threadId' },
                { name: 'threadName' },
                { name: 'waitedCount' },
                { name: 'inNative' },
                { name: 'waitedTime' },
                { name: 'lockedSynchronizers' },
                { name: 'lockedMontiors' },
                { name: 'lockOwnerName' },
                { name: 'stackTrace' },
                { name: 'lockName' },
                { name: 'lockInfo' }
            ],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('sessionDetails').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('sessionDetails').setLoading(false);
                }
            }
        });

        var myToolbar = [{
            xtype: 'toolbar',
            dock: 'top',
            height: 35,
            defaults: {
                scope: this,
                enableToggle: true,
                toggleGroup: 'sessionDetails',
                allowDepress: false
            },
            items: [{
                xtype: 'tbfill'
            },{
                text: 'Execution',
                pressed: true,
                handler: function() {
                    this.getLayout().setActiveItem('execution');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Environment',
                handler: function() {
                    this.getLayout().setActiveItem('environment');
                },
                style: 'padding:0;margin:0;border-radius:0;border-width:0!important;border-right-color:transparent;border-left-color:transparent;'
            },{
                text: 'Server Thread',
                handler: function() {
                    this.getLayout().setActiveItem('serverThread');
                },
                style: 'padding:0;margin:0;border-radius:0;border-width:0!important;border-right-color:transparent;border-left-color:transparent;'
            },{
                text: 'Call Stack',
                handler: function() {
                    this.getLayout().setActiveItem('callStack');
                },
                style: 'padding:0;margin:0px 5px 0px 0px;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{
                xtype: 'tbfill'
            }]
        }];

        // Apply some stuff
        Ext.apply(this, {
            border: false,
            dockedItems: myToolbar,
            layout: 'card',
            items: [
                Ext.create("RP.Moca.Console.Sessions.Execution", {
                    id: 'execution',
                    layout: 'fit'
                }), 
                Ext.create("RP.Moca.Console.Sessions.Environment", {
                    id: 'environment',
                    layout: 'fit'
                }), 
                Ext.create("RP.Moca.Console.Sessions.ServerThread", {
                    id: 'serverThread',
                    layout: 'form',
                    store: serverThreadStore
                }), 
                Ext.create("RP.Moca.Console.Sessions.CallStack", {
                    id: 'callStack',
                    store: serverThreadStore
                })
            ]
        });

        // Call our superclass
        this.callParent(arguments);
    },

    /**
     * displaySession
     * @param {Object} record
     */
    display: function(record) {
        this.clear();

        var execution = Ext.getCmp('execution');
        var environment = Ext.getCmp('environment');

        //this.fireEvent('afterLayout');
        execution.loadRecord(record);
        environment.loadRecord(record);

        var sessionName = record.get('name');
        var threadId = record.get('threadId');

        Ext.getCmp('callStack').store.load({
            params: {
                m: 'getServerThreadInformation',
                name: sessionName,
                threadId: threadId
            }
        });
        
        Ext.getCmp('serverThread').store.load({
            params: {
                name: sessionName,
                threadId: threadId
            }
        });
        
    },

    clear: function () {
        Ext.getCmp('execution').clear();
        Ext.getCmp('environment').clear();
        Ext.getCmp('serverThread').clear();
        Ext.getCmp('callStack').clear();  
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\sessions\SessionPanels.js
//////////////////////
// Execution panel
Ext.define("RP.Moca.Console.Sessions.Execution", {
    extend: "Ext.form.Panel",

    initComponent: function () {

        // Apply some stuff
        Ext.apply(this, {
            border: false,
            autoScroll: true,
            layout: 'anchor',
            anchor: '100%',
            xtype: 'panel',
            defaults: {
                xtype: 'textarea',
                labelAlign: 'top',
                readOnly: true,
                padding: 5,
                width: '95%',
                grow: true,
                maxHeight: 230
            },
            items: [
                { hidden: true }, // helps with the layout
                {
                    fieldLabel: 'Connected IP Address',
                    name: 'connectedIpAddress',
                    height: 45
                },{
                    fieldLabel: 'Current Command Path',
                    name: 'commandPath'
                },{
                    fieldLabel: 'Last Command Time',
                    name: 'lastCommandTime',
                    height: 55
                }, {
                    fieldLabel: 'Last Command',
                    name: 'lastCommand'
                },{
                    name: 'lastSQLTime',
                    fieldLabel: 'Last SQL Time',
                    height: 55
                }, {
                    fieldLabel: 'Last SQL',
                    name: 'lastSQL'
                }
            ]
        });

        this.callParent(arguments);
    },

    /**
     * loadRecord
     * @desc Load the record for the current thread as passed.
     * @param Object record The data for the current thread.
     * @return void
     */
    loadRecord: function (record) {
        this.getForm().loadRecord(record);
    },

    /**
     * getValue
     * @desc Retrieve the value of a field in the form.
     * @param Object fieldName The name of the field to retrieve from the form.
     * @return String The value of the field as specified by fieldName.
     */
    getValue: function (fieldName) {
        if (this.getForm().findField(fieldName)) {
            return this.getForm().findField(fieldName).getValue();
        }
        return null;
    },

    clear: function () {
        this.getForm().reset();
    }
});

// CallStack panel
Ext.define("RP.Moca.Console.Sessions.CallStack", {
    extend: "Ext.panel.Panel", 

    initComponent: function () {
        var execution = Ext.getCmp('execution');

        // register the listeners to the store for rendering the data when it changes
        this.store.on('load', this.display, this);
        this.store.on("loadException", this.clear, this);

        Ext.apply(this, {
            border: false,
            autoScroll: true,
            tpl: new Ext.XTemplate(
                    '<ul>',
                        '<tpl for = "stackTrace">',
                            '<li style="padding:6px 0 6px 15px;font-size:11px">{.}</li>',
                        '</tpl>',
                    '</ul>'
                )
        });

        // Call our superclass.
        this.callParent(arguments);
    },
    
    display: function(store, records) {
        if (typeof records[0] != "undefined") {
            this.update(records[0].data);
        }
    },
    
    clear: function() {
        this.update();
    }
});

// Environment panel
Ext.define("RP.Moca.Console.Sessions.Environment", {
    extend: "Ext.grid.Panel",

    initComponent: function (config) {

        this.environmentGrid = new Ext.data.Store({
            fields: ['name','value'],
            layout: 'fit',
            autoScroll: true,
            enableColumnResize: true,
            viewConfig: {
                stripeRows: true
            },
            source: {}
        });

        Ext.apply(this, {
            border: false,
            layout: 'fit',
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            columns: [{header: 'Name', dataIndex:'name',flex: 1},{header:'Value', dataIndex:'value', flex: 2}],
            store: this.environmentGrid
        });
        
        this.callParent(arguments);
    },

    /**
     * Load the record for the current thread as passed.
     * 
     * @param {Object} record The data for the current thread.
     */
    loadRecord: function (record) {
        if (typeof record != "undefined") {
            var records = record.get("environment");
            var arr = []; 
            Ext.Object.each(records, function(key, value) {  
              arr.push({ 
                name: key, 
                value: value 
              }); 
             }, this); 
             
            this.environmentGrid.add(arr);
        }
    },

    clear: function () {
        this.environmentGrid.removeAll();
    }
});

// ServerThread panel
Ext.define("RP.Moca.Console.Sessions.ServerThread", {
    extend: "Ext.grid.Panel",
    
    initComponent: function () {
    
        this.store.on("load", this.display, this);
        this.store.on("loadException", this.clear, this);
    
        this.serverThreadDetailsGrid = new Ext.data.Store({
            fields: ['name','value'],
            autoScroll: true,
            id: "threadPropertiesGrid",
            listeners: {
                validateedit: function () { return false; }
            },
            viewConfig: {
                stripeRows: true
            },
            source: {}
        });

        Ext.apply(this, {
            border: false,
            layout: 'fit',
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            columns: [{header: 'Name', dataIndex:'name',flex: 1},{header:'Value', dataIndex:'value', flex: 2}],
            store: this.serverThreadDetailsGrid
        });

        this.callParent(arguments);
    },

    /**
     * displayDetails
     * Displays the details of the thread in the grid to the user.
     * @param {Object} store The store.
     * @param {Object} record The records returned during the load.
     * @param {Object} options The options used to load the store.
     */
    display: function (store, records, options) {
        var detailSpec, index;
        var values = {};

        var propertyFields = [{
            fieldLabel: 'Thread ID',
            dataIndex: 'threadId',
            name: 'threadId'
        }, {
            fieldLabel: 'Thread Name',
            dataIndex: 'threadName',
            name: 'threadName'
        }, {
            fieldLabel: 'Thread State',
            dataIndex: 'threadState',
            name: 'threadState'
        }, {
            fieldLabel: 'In Native',
            dataIndex: 'inNative',
            name: 'inNative'
        }, {
            fieldLabel: 'Suspended',
            dataIndex: 'suspended',
            name: 'suspended'
        }, {
            fieldLabel: 'Blocked Time',
            dataIndex: 'blockedTime',
            name: 'blockedTime'
        }, {
            fieldLabel: 'Lock Owner ID',
            dataIndex: 'lockOwnerId',
            name: 'lockOwnerId'
        }, {
            fieldLabel: 'Blocked Count',
            dataIndex: 'blockedCount',
            name: 'blockedCount'
        }, {
            fieldLabel: 'Waited Count',
            dataIndex: 'waitedCount',
            name: 'waitedCount'
        }, {
            fieldLabel: 'Waited Time',
            dataIndex: 'waitedTime',
            name: 'waitedTime'
        }];

        // if the store return an non-emtpy set, then grab the 
        // data portion of the object and process it. 
        var data = {};
        if (typeof records[0] != "undefined") {
            data = records[0].data;

            // build up an object that the grid can used and pass it
            for (var i = 0; i < propertyFields.length; i++) {
                if (!Ext.isFunction(propertyFields[i])) {
                    detailSpec = propertyFields[i];
                    values[detailSpec.fieldLabel] = data[detailSpec.dataIndex];
                }
            }
        }
        
        var arr = [];
        Ext.Object.each(values, function(key, value) {
            arr.push({
                name: key,
                value: value
            });
        }, this);
        
        this.serverThreadDetailsGrid.add(arr);
    },

    /**
     * clear
     * Clear the form out in the case a load exception occurs
     * or there was no data retrieved from the store.
     * @return void
     */
    clear: function () {
        this.serverThreadDetailsGrid.removeAll();
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\AsyncExecutor.js
//////////////////////
// View
Ext.define("RP.Moca.Console.AsyncExecutor.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Asynchronous Executor',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.AsyncExecutor.Panel", {
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('asyncExecutorPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    },

    onActivate: function () {
        RP.Moca.Console.AsyncExecutor.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('asyncExecutorPanel').refresh();
        }

        this.activateCount++;
    }
});

// Panel
Ext.define("RP.Moca.Console.AsyncExecutor.Panel", {
    extend: "Ext.panel.Panel",
    id: 'asyncExecutorPanel',

    initComponent: function () {

        // Handler function for setting the title of the second store
        var setTitle1 = function (store, records, options) {
            Ext.getCmp('asyncGrid1').setTitle('Current Asynchronous Executions (' + store.getCount() + ')');
        };

        // Handler function for setting the title of the first store
        var setTitle2 = function (store, records, options) {
            Ext.getCmp('asyncGrid2').setTitle('Current Queued Executions (' + store.getCount() + ')');
        };

        var myStore1 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['task_thread', 'task_name'],
            proxy: {
                extraParams: {
                    m: 'getAsyncExecutionInfo'
                }
            },
            sorters: [{
                property: 'task_thread',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('asyncGrid1').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('asyncGrid1').setLoading(false);
                }
            }
        });

        var myStore2 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['queued', 'order'],
            proxy: {
                extraParams: {
                    m: 'getAsyncQueueInfo'
                }
            },
            sorters: [{
                property: 'order',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('asyncGrid2').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('asyncGrid2').setLoading(false);
                }
            }
        });

        myStore1.on('load', setTitle1, this);
        myStore2.on('load', setTitle2, this);

        Ext.apply(this, {
            layout: {
                type: "hbox",
                align: "stretch"
            },
            bodyStyle: {
                background: '#ffffff'
            },
            items: [
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Asynchronous Executions',
                    id: 'asyncGrid1',
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    autoScroll: true,
                    store: myStore1,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [
                        { header: 'Task Thread', dataIndex: 'task_thread',
                          flex: 1, minWidth: 150, renderer: RP.Moca.util.HyperLink.sessions },
                        { header: 'Task Name', dataIndex: 'task_name', flex: 2, minWidth: 150 }
                    ]
                }), 
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Queued Executions',
                    id: 'asyncGrid2',
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    autoScroll: true,
                    store: myStore2,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [
                        { header: 'Sequence', dataIndex: 'order', flex: 1, minWidth: 120 },
                        { header: 'Queued Task', dataIndex: 'queued', flex: 2, minWidth: 150 }
                    ]
                })
            ]
        });

        this.callParent(arguments);
    },

    refresh: function() {
        this.getComponent('asyncGrid1').store.load();
        this.getComponent('asyncGrid2').store.load();
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\ClusterAsyncExecutor.js
//////////////////////
// View
Ext.define("RP.Moca.Console.ClusterAsyncExecutor.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Cluster Async Executor',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.ClusterAsyncExecutor.Panel", {
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('clusterAsyncExecutorPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    },

    onActivate: function () {
        RP.Moca.Console.ClusterAsyncExecutor.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('clusterAsyncExecutorPanel').refresh();
        }

        this.activateCount++;
    }
});

// Panel
Ext.define("RP.Moca.Console.ClusterAsyncExecutor.Panel", {
    extend: "Ext.panel.Panel",
    id: 'clusterAsyncExecutorPanel',
    
    initComponent: function () {

        // Handler function for setting the title of the second store
        var setTitle1 = function (store, records, options) {
            Ext.getCmp('clusterAsyncGrid1').setTitle('Current Runner Tasks (' + store.getCount() + ')');
        };

        // Handler function for setting the title of the first store
        var setTitle2 = function (store, records, options) {
            Ext.getCmp('clusterAsyncGrid2').setTitle('Current Requests (' + store.getCount() + ')');
        };

        var myStore1 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['task_thread', 'task_name'],
            proxy: {
                extraParams: {
                    m: 'getClusterAsyncExecutionInfo'
                }
            },
            sorters: [{
                property: 'task_thread',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('clusterAsyncGrid1').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('clusterAsyncGrid1').setLoading(false);
                }
            }
        });

        var myStore2 = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            fields: ['queued', 'noderunning'],
            proxy: {
                extraParams: {
                    m: 'getClusterAsyncQueueInfo'
                }
            },
            sorters: [{
                property: 'noderunning',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('clusterAsyncGrid2').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('clusterAsyncGrid2').setLoading(false);
                }
            }
        });
        
        var myResultHandler = function (result) {
            Ext.getCmp('clusterAsyncGrid1').store.load();
            Ext.getCmp('clusterAsyncGrid2').store.load();
        };
        
        // Handler function to start a runner
        var startRunner = function() {
            // Make the call     
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'handleClusterAsyncRunnerRequest',
                    add: 'true'
                },
                success: myResultHandler,
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not start Runner Thread.'
                }
            });
        };
        
        // Handler function to stop a runner
        var stopRunner = function() {
            // Make the call     
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'handleClusterAsyncRunnerRequest',
                    add: 'false'
                },
                success: myResultHandler,
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not stop Runner Thread.'
                }
            });
        };

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Remove Idle Thread',
                scope: this,
                handler: stopRunner,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Add Runner Thread',
                scope: this,
                handler: startRunner,
                style: {
                    margin: '3px'
                }
            }]
        });        

        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.disable();
                }
            }
        });

        myStore1.on('load', setTitle1, this);
        myStore2.on('load', setTitle2, this);

        Ext.apply(this, {
            dockedItems: myToolbar,
            layout: {
                type: "hbox",
                align: "stretch"
            },
            bodyStyle: {
                background: '#ffffff'
            },
            items: [
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Runner Tasks',
                    id: 'clusterAsyncGrid1',
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    autoScroll: true,
                    store: myStore1,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [{ 
                        header: 'Task Thread', 
                        dataIndex: 'task_thread',
                        flex: 1,
                        minWidth: 120, 
                        renderer: RP.Moca.util.HyperLink.sessions
                     }, {
                         header: 'Task Name', 
                         dataIndex: 'task_name',
                         flex: 2,
                         minWidth: 150
                    }
                    ]
                }), 
                Ext.create("Ext.grid.Panel", {
                    title: 'Current Requests',
                    id: 'clusterAsyncGrid2',
                    autoScroll: true,
                    viewConfig: {
                        preserveScrollOnRefresh: true
                    },
                    store: myStore2,
                    layout: 'fit',
                    padding: 5,
                    flex: 3,
                    columns: [{ 
                        header: 'Queued Task', 
                        dataIndex: 'queued', 
                        minWidth: 120,
                        flex: 1
                    }, { 
                        header: 'Node Running', 
                        dataIndex: 'noderunning', 
                        renderer: RP.Moca.util.HyperLink.node,
                        flex: 2,
                        minWidth: 150
                    }]
                })
            ]
        });

        this.callParent(arguments);
    },

    refresh: function() {
        this.getComponent('clusterAsyncGrid1').store.load();
        this.getComponent('clusterAsyncGrid2').store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\CommandProfile.js
//////////////////////
Ext.define("RP.Moca.Console.Util", {
    singleton: true,
    
    // Handler function to add "ms"
    formatAndAddMS: function(number) {
        return RP.Moca.util.Format.truncateNumber(number) + " ms";
    }
});

// View
Ext.define("RP.Moca.Console.CommandProfile.View", {
    extend: "RP.util.taskflow.BaseTaskForm",
    
    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('commandProfileGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Command Profile',
            layout: 'fit',
            items: [ new RP.Moca.Console.CommandProfile.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander',
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Component Level:</b></td><td>{component_level}</td></tr><tr><td><b>Command:</b></td><td>{command}</td></tr><tr><td><b>Command Path:</b></td><td>{command_path}</td></tr><tr><td><b>Type:</b></td><td>{type}</td></tr><tr><td><b>Executions:</b></td><td>{execution_count}</td></tr><tr><td><b>Min:</b></td><td>{min_ms} ms</td></tr><tr><td><b>Max:</b></td><td>{max_ms} ms</td></tr><tr><td><b>Avg:</b></td><td>{avg_ms} ms</td></tr><tr><td><b>Total:</b></td><td>{total_ms} ms</td></tr><tr><td><b>Self Avg:</b></td><td>{avg_self_ms} ms</td></tr><tr><td><b>Self Total:</b></td><td>{self_ms} ms</td></tr></table>', {
                        blankNulls: true
                    })
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('commandProfileGrid').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Level',
                    dataIndex: 'component_level',
                    minWidth: 110,
                    flex: 1
                }, {
                    header: 'Command',
                    dataIndex: 'command',
                    minWidth: 150,
                    flex: 1
                }, {
                    header: 'Type',
                    dataIndex: 'type',
                    renderer: RP.Moca.util.Format.commandTypeString,
                    minWidth: 95,
                    flex: 1
                }, {
                    header: 'Executions',
                    dataIndex: 'execution_count',
                    align: 'right',
                    minWidth: 115,
                    flex: 1
                }, {
                    header: 'Min',
                    dataIndex: 'min_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 80,
                    flex: 1,
                    hidden: true
                }, {
                    header: 'Max',
                    dataIndex: 'max_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 80,
                    flex: 1
                }, {
                    header: 'Avg',
                    dataIndex: 'avg_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 100,
                    flex: 1
                }, {
                    header: 'Total',
                    dataIndex: 'total_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 80,
                    flex: 1
                }, {
                    header: 'Self Avg',
                    dataIndex: 'avg_self_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 100,
                    flex: 1
                }, {
                    header: 'Self Total',
                    dataIndex: 'self_ms',
                    renderer: RP.Moca.Console.Util.formatAndAddMS,
                    minWidth: 100,
                    flex: 1
                }],
                store: new RP.Moca.util.Store({
                    autoLoad: true,
                    proxy: {
                        type: 'ajax',
                        url: '/console',
                        extraParams: {
                            m: 'getCommandProfile'
                        },
                        reader: new RP.Moca.Console.Results.ResultsReader({}),
                        simpleSortMode: true
                    },
                    fields: [
                        'component_level', 'command', 'type', 'command_path', 'execution_count',
                        'min_ms', 'max_ms', 'avg_ms', 'total_ms', 'self_ms', 'avg_self_ms'
                    ],
                    sorters: [{
                            property: 'self_ms',
                            direction: 'DESC'
                    }],
                    listeners: {
                        'beforeload' :  function() {
                            Ext.getCmp('commandProfileGrid').setLoading(true);
                        },
                        'load' :  function() {
                            Ext.getCmp('commandProfileGrid').setLoading(false);
                        }
                    }
                })
            }) ]
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.CommandProfile.Grid", {
    extend: "Ext.grid.Panel",
    id: 'commandProfileGrid',
    
    initComponent: function() {

        // Handler function for clearing the command profile
        var clearProfile = function() {

            // Make an Ajax call to the server.       
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'clearCommandProfile'
                },
                success: function (result) {
                    var status = Ext.decode(result.responseText).status;

                    if (status !== 0) {
                        var message = Ext.decode(result.responseText).message;
                        Ext.Msg.alert('Console', 'An error occurred attempting to clear the command profile.\n\n' + message);
                    }
                },
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not send the clear profile request to the server.'
                }
            });

            this.store.load();
        };

        // Handler function for downloading
        var downloadProfile = function () {
            var body = Ext.getBody();

            var frame = body.createChild({
                tag: 'iframe',
                cls: 'x-hidden',
                src: '/console/profile'
            });
        };

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Clear Command Profile',
                scope: this,
                handler: clearProfile,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Download Command Profile',
                scope: this,
                handler: downloadProfile,
                style: {
                    margin: '3px'
                }
            }]
        });

        var myTopToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'top',
            height: 1,
            items: [{
                text: 'Download Command Profile',
                scope: this,
                handler: downloadProfile,
                style: {
                    margin: '3px'
                }
            }]
        });

         RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.items.items[0].disable();
                }
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            dockedItems: [myToolbar, myTopToolbar],
            viewConfig: {
                emptyText: 'No command profiling is available.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\ComponentLibraries.js
//////////////////////
// View
Ext.define("RP.Moca.Console.ComponentLibraries.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('componentLibrariesGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            title: 'Component Libraries',
            layout: 'fit',
            items: new RP.Moca.Console.ComponentLibraries.Grid({
                padding: '0px 5px 7px 0px'
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.ComponentLibraries.Grid", {
    extend: "RP.ui.GridPanel",
    id: 'componentLibrariesGrid',
    
    initComponent: function () {
        var myColumns = [{
            header: 'Component Library',
            dataIndex: 'category',
            flex: 2,
            minWidth: 155
            }, {
            header: 'Version',
            dataIndex: 'version',
            flex: 2,
            minWidth: 125
            }, {
            header: 'Java Package',
            dataIndex: 'package_name',
            flex: 2,
            minWidth: 300
            }, {
            header: 'C Library',
            dataIndex: 'library_name',
            flex: 1,
            minWidth: 100
            }, {
            header: 'Precedence',
            dataIndex: 'sort_seq',
            align: 'right',
            flex: 1,
            minWidth: 110
            }, {
            header: 'Product',
            dataIndex: 'product',
            flex: 1,
            minWidth: 75
        }];

        //Create and load store
        var myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getComponentLibraries'
                }
            },
            fields: [
                'category',
                {name: 'progid', mapping: 'progid',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'progid', mapping: 'progid',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'library_type', mapping: 'library_type',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'library_name', mapping: 'library_name',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'package_name', mapping: 'package_name',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'namespace', mapping: 'namespace',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'sort_seq', mapping: 'sort_seq',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'version', mapping: 'version',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                },
                {name: 'product', mapping: 'product',
                    convert: function(value) {
                        return value === null ? '' : value;
                    }
                }
            ],
            sorters: [{
                property: 'category',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('componentLibrariesGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('componentLibrariesGrid').setLoading(false);
                }
            }
        });

        // Apply config information
        Ext.apply(this, {
            autoScroll: true, 
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }],
            store: myStore,
            columns: myColumns,
            viewConfig: {
                emptyText: 'No component libraries currently exist. This is likely a configuration problem.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        // Call super
        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\DatabaseConnections.js
//////////////////////
// View
Ext.define("RP.Moca.Console.DatabaseConnections.View", {
    extend: "RP.util.taskflow.BaseTaskForm",
    id: 'databaseConnectionView',
    
    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('databaseConnectionsGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Database Connections',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.DatabaseConnections.Grid", {
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Connection:</b></td><td>{id}</td></tr><tr><td><b>Thread ID:</b></td><td>{thread_id}</td></tr><tr><td><b>Last SQL Time:</b></td><td>{last_sql_dt}</td></tr><tr><td><b>Last SQL:</b></td><td>{last_sql}</td></tr><tr><td><b>Executions:</b></td><td>{executions}</td></tr><tr><td><b>Last Command Path:</b></td><td>{command_path}</td></tr></table>', {
                        blankNulls: true
                    })
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('databaseConnectionsGrid').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Connection',
                    dataIndex: 'id',
                    minWidth: 150,
                    flex: 1
                }, {
                    header: 'Thread ID',
                    dataIndex: 'thread_id',
                    renderer: RP.Moca.util.HyperLink.sessions,
                    minWidth: 100,
                    flex: 1
                }, {
                    header: 'Last SQL Time',
                    dataIndex: 'last_sql_dt',
                    renderer: RP.Moca.util.Format.datetimeString,
                    minWidth: 175,
                    flex: 1
                }, {
                    header: 'Last SQL',
                    dataIndex: 'last_sql',
                    minWidth: 300,
                    flex: 2
                }, {
                    header: 'Executions',
                    dataIndex: 'executions',
                    minWidth: 80,
                    flex: 1,
                    align: 'right'
                }]
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.DatabaseConnections.Grid", {
    extend: "Ext.grid.Panel",
    id: 'databaseConnectionsGrid',
    
    initComponent: function () {
        var myStore = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getDatabaseConnections'
                }
            },
            fields: ['id', 'thread_id',/*'threat_id',*/ 'last_sql', 'executions', 'command_path', {
                name: 'last_sql_dt',
                type: 'date'
            }],
            sorters: [{
                property: 'id',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('databaseConnectionsGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('databaseConnectionsGrid').setLoading(false);
                }
            }
        });

        Ext.apply(this, {
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }],
            autoScroll: true,
            store: myStore,
            viewConfig: {
                emptyText: 'No database connections exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\EnvironmentVariables.js
//////////////////////
// View
Ext.define("RP.Moca.Console.EnvironmentVariables.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('environmentVariablesGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Environment Variables',
            layout: 'fit',
            items: new RP.Moca.Console.EnvironmentVariables.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    rowBodyTpl: new Ext.XTemplate('<div class="rp-expanded-value">', '{value}', '</div>')
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('environmentVariablesGrid').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Name',
                    dataIndex: 'name',
                    flex: 1,
                    minWidth: 150
                },{
                    header: 'Value',
                    dataIndex: 'value',
                    flex: 3,
                    minWidth: 300
                }]
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.EnvironmentVariables.Grid", {
    extend: "Ext.grid.Panel",
    id: 'environmentVariablesGrid',
    
    initComponent: function () {

        var myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getEnvironmentVariables'
                }
            },
            fields: [ 'name', 'value' ],
            sorters: [{
                property: 'name',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('environmentVariablesGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('environmentVariablesGrid').setLoading(false);
                }
            }
        });

        // Call our superclass.
        Ext.apply(this, {
            autoScroll: true,
            store: myStore,
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }]
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\Jobs.js
//////////////////////
// View
Ext.define("RP.Moca.Console.Jobs.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    onActivate: function () {
        RP.Moca.Console.Jobs.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('jobsContainer').refresh();
        }

        this.activateCount++;
    },

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            itemId: 'jobsView',
            title: 'Jobs',
            layout: 'fit',
            border: false,
            items: Ext.create("RP.Moca.Console.Jobs.Container", {
                padding: '0px 5px 7px 0px',
                layout: 'border',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('jobsContainer').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

Ext.define('RP.Moca.Console.Jobs.Container', {
    extend: "Ext.panel.Panel",
    id: 'jobsContainer',
    
    /**
     * This is regex that tests if a job schedule is simple. The schedule format
     * for a cron expression is <seconds> <minutes> <hours> <day of month> <month> <day of week> [year]
     * Basically for the simple schedule the only fields that can be set are "hours" (1-23)
     * day of week (1-7 with repeating commas e.g. 1,2,3). Anything else is considered complex.
     * So dissecting this regex, we have the following:
     * 1) 0 for both seconds and minutes as we don't allow these to be configured in the simple settings
     * 2) A hours field that is either * (all values), ? (not specified), or 1-23
     * 3) For "day of month" a value of "?" (not specific) or "*" (all)
     * 4) For "month" a value of "*" (all)
     * 5) For "day of week" either * (all days), ? (unspecified), or a comma separated list of days 1-7 e.g. 1,2,3 or just a single day "1"
     * 6) Year is not included
     * Here's the regex with each above mentioned step highlighted:
     * [ 1 ][          2        ] [  3  ] [4] [            5               ]
     * /0 0 (\*|\?|\d|1\d|2[0-3]) (\*|\?) \*( (\*|\?|[1-7](\,[1-7]){0,6}))?$/
     */
    simpleScheduleRegex: /0 0 (\*|\?|\d|1\d|2[0-3]) (\*|\?) \*( (\*|\?|[1-7](\,[1-7]){0,6}))?$/,
    
    initComponent: function () {
    
        Ext.define('JobModel', {
            extend: 'Ext.data.Model',
            fields: [
                'job_id', 'role_id', 'name', 'scheduled', 'enabled',
                'type', 'command', 'log_file', 'trace_level', 'overlap',
                'schedule', 'start_delay', 'timer', 'grp_nam', 'nodes'
            ]
        });
    
        this.store = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            storeId: 'masterJobStore',
            proxy: {
                extraParams: {
                    m: 'getJobs'
                },
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({})
            },
            model: 'JobModel',
            groupers: [{
                property: 'type',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('jobsContainer').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('jobsContainer').setLoading(false);
                }
            }
        });
        
        var myEditToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{
                text: 'Add',
                handler: this.addJob
            }, {
                text: 'Copy',
                handler: this.copyJob
            }, {
                text: 'Modify',
                handler: this.modifyJob
            }, {
                text: 'Delete',
                handler: this.deleteJob
            }, {
                xtype: 'tbfill'
            },{
                text: 'Scheduled',
                id: 'scheduledJobsButton',
                enableToggle: true,
                toggleGroup: 'Jobs',
                allowDepress: false,
                pressed: true,
                handler: function() {
                    Ext.getCmp('jobsContainer').getLayout().setActiveItem('scheduledJobs');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Timer',
                id: 'timerJobsButton',
                enableToggle: true,
                toggleGroup: 'Jobs',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('jobsContainer').getLayout().setActiveItem('timedJobs');
                },
                style: 'padding:0;margin:0px 5px 0px 0px;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            },{ 
                xtype: 'tbfill'
            }]
        });

        var myActionToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Schedule Job',
                id: 'startScheduledJob',
                scope: this,
                handler: this.startJobHandler,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Unschedule Job',
                id: 'stopScheduledJob',
                scope: this,
                handler: this.stopJobHandler,
                style: {
                    margin: '3px'
                }
            }]
        });
        
        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    //Add
                    myEditToolbar.items.items[0].disable();
                    //Copy
                    myEditToolbar.items.items[1].disable();
                    //Modify
                    myEditToolbar.items.items[2].disable();
                    //Remove
                    myEditToolbar.items.items[3].disable();
                    myActionToolbar.disable();
                }
            }
        });

        Ext.apply(this, {
            layout: 'card',
            dockedItems: [myEditToolbar, myActionToolbar],
            items: [
                Ext.create("RP.Moca.Console.Jobs.Scheduled", {
                    border: false,
                    id: 'scheduledJobs',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Job ID:</b></td><td>{job_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command:</b></td><td>{command}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Scheduled:</b></td><td>{scheduled}</td></tr><tr><td><b>Schedule:</b></td><td>{schedule}</td></tr><tr><td><b>Enabled:</b></td><td>{enabled}</td></tr><tr><td><b>Overlap:</b></td><td>{overlap}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr><tr><td><b>Type:</b></td><td>{type}</td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                }),
                Ext.create("RP.Moca.Console.Jobs.Timed", {
                    border: false,
                    id: 'timedJobs',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Job ID:</b></td><td>{job_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command:</b></td><td>{command}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Scheduled:</b></td><td>{scheduled}</td></tr><tr><td><b>Enabled:</b></td><td>{enabled}</td></tr><tr><td><b>Overlap:</b></td><td>{overlap}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Timer:</b></td><td>{timer}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr><tr><td><b>Type:</b></td><td>{type}</td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                })
            ]
        });
        this.callParent(arguments);
    },
    refresh: function() {
        this.store.load();
        Ext.getCmp('scheduledJobs').fireEvent('afterrender', this);
        Ext.getCmp('timedJobs').fireEvent('afterrender', this);
    },
    
    // Handler function for the start job button
    startJobHandler: function () {
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            Ext.Msg.alert('Console', 'Please select a job to schedule and try again.');
            return;
        }

        // This list will contain all the jobs that we need to work with.
        var job_id_list = '';
        
        // Build the list of jobs to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the job id and scheduled flag for this job.
            var job_id = s.get('job_id');
            var scheduled = s.get('scheduled');
            
            if (scheduled === false) {
                if (job_id_list === '') {
                    job_id_list = job_id;
                }
                else {
                    job_id_list = job_id_list + ',' + job_id;
                }
            }
        }

        var msgAlreadyScheduled;
        var msgScheduledJob;
        var msgErrorSchedulingJob;
            
        if (selected.length == 1) {
            msgAlreadyScheduled = 'The selected job is already scheduled.';
            msgSchedulingJob = 'Scheduling job...';
            msgErrorSchedulingJob = 'An error occurred attempting to schedule the selected job.';
        }
        else {
            msgAlreadyScheduled = 'The selected jobs are already scheduled.';
            msgSchedulingJob = 'Scheduling jobs...';
            msgErrorSchedulingJob = 'An error occurred attempting to schedule the selected jobs.';   
        }
        
        // Make sure we don't have jobs that are already scheduled to schedule.
        if (job_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgAlreadyScheduled);
            return;
        }

        var myParams = {
            job_id_list: job_id_list,
            m: 'startSchedulingJob'
        };

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgSchedulingJob + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorSchedulingJob + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to start.
                        Ext.getCmp('jobsContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.MessageBox.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send schedule request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    },

    // Handler function for the stop job button
    stopJobHandler: function () {
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }
        
        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            RP.Moca.util.Msg.alert('Console', 'Please select a job to unschedule and try again.');
            return;
        }

        // This list will contain all the jobs that we need to work with.
        var job_id_list = '';
        
        // Build the list of jobs to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the job id and scheduled flag for this job.
            var job_id = s.get('job_id');
            var scheduled = s.get('scheduled');
            
            if (scheduled === true) {
                if (job_id_list === '') {
                    job_id_list = job_id;
                }
                else {
                    job_id_list = job_id_list + ',' + job_id;
                }
            }
        }
        
        var msgNotScheduled;
        var msgUnscheduledJob;
        var msgErrorUnschedulingJob;
            
        if (selected.length == 1) {
            msgNotScheduled = 'The selected job is not scheduled.';
            msgUnschedulingJob = 'Unscheduling job...';
            msgErrorUnschedulingJob = 'An error occurred attempting to unschedule the selected job.';
        }
        else {
            msgNotScheduled = 'The selected jobs are not scheduled.';
            msgUnschedulingJob = 'Unscheduling jobs...';
            msgErrorUnschedulingJob = 'An error occurred attempting to unschedule the selected jobs.';   
        }
        
        // Make sure we don't have jobs that are not scheduled to unschedule.
        if (job_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgNotScheduled);
            return;
        }

        var myParams = {
            job_id_list: job_id_list,
            m: 'stopSchedulingJob'
        };

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgUnschedulingJob + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorUnschedulingJob + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to start.
                        Ext.getCmp('jobsContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.MessageBox.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send unschedule request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: {
                job_id_list: job_id_list,
                m: 'stopSchedulingJob'
            },
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    },
    
    // Handler function to add a job
    addJob: function() {
        var addJobPanel = Ext.create('Ext.panel.Panel', {
            modal: true,
            id: 'addJobPanel',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            width: 800,
            height: 500,
            floating: true,
            closable: true,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'add'
                }),
                Ext.create('RP.Moca.Console.Util.JobsInformation', {})
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveJob, scope: addJobPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {addJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            addJobPanel.setTitle('Add Scheduled Based Job');
            addJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.ScheduledMaint', {}));
            addJobPanel.setHeight(525);
            addJobPanel.down('#command').setHeight(255);
            addJobPanel.down('#type').setValue('cron');
            addJobPanel.down('#informationPanel').getLayout().setActiveItem('schedulePanel');
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            addJobPanel.setHeight(500);
            addJobPanel.setTitle('Add Timer Based Job');
            addJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.TimerMaint', {}));
            addJobPanel.down('#command').setHeight(230);
            addJobPanel.down('#type').setValue('timer');
            addJobPanel.down('#informationPanel').getLayout().setActiveItem('timerPanel');
        }
        
        addJobPanel.down('#task_id').hide();
        addJobPanel.down('#auto_start').hide();
        addJobPanel.down('#restart').hide();
        addJobPanel.down('#run_dir').hide();
        addJobPanel.down('#spacer2').hide();
        addJobPanel.down('#tracingPanel').setHeight(200);
        addJobPanel.down('#action').setValue('add');
        
        var role_id = Ext.getCmp('timedJobs').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('scheduledJobs').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             addJobPanel.down('#role_id').store.add({field1:key});
        });
        
        addJobPanel.show();
    },
    
    // Handler to modify selected Job
    modifyJob: function() {
        var selected;
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single job to modify and try again.');
            return;
        }  
        
        var jobTitle = selected[0].data.name;
        
        var modifyJobPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'modifyJobPanel',
            id: 'modifyJobPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'edit'
                }),
                Ext.create('RP.Moca.Console.Util.JobsInformation', {
                    job_id: selected[0].data.job_id
                })
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveJob, scope: modifyJobPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {modifyJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
               
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            modifyJobPanel.setTitle('Modify Job: ' + jobTitle);
            modifyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.ScheduledMaint', {}));
            modifyJobPanel.setHeight(525);
            modifyJobPanel.down('#command').setHeight(255);
            modifyJobPanel.down('#type').setValue('cron');
            this.populateSchedule(modifyJobPanel, selected[0].data.schedule);
            modifyJobPanel.down('#informationPanel').getLayout().setActiveItem('schedulePanel');
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            modifyJobPanel.setTitle('Modify Job: ' + jobTitle);
            modifyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.TimerMaint', {}));
            modifyJobPanel.down('#start_delay').setValue(selected[0].data.start_delay);
            modifyJobPanel.down('#timer').setValue(selected[0].data.timer);
            modifyJobPanel.down('#type').setValue('timer');
            modifyJobPanel.setHeight(500);
            modifyJobPanel.down('#command').setHeight(230);
            modifyJobPanel.down('#informationPanel').getLayout().setActiveItem('timerPanel');
        }
        
        modifyJobPanel.down('#job_id').setValue(selected[0].data.job_id);
        modifyJobPanel.down('#job_id').disable();
        modifyJobPanel.down('#name').setValue(selected[0].data.name);
        modifyJobPanel.down('#log_file').setValue(selected[0].data.log_file);
        modifyJobPanel.down('#action').setValue('edit');
        
        this.populate(modifyJobPanel, selected[0].data);
        
        modifyJobPanel.show();
    },
    
    // Handler to copy selected Job
    copyJob: function() {
        var selected;
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single job to copy and try again.');
            return;
        }  
        
        var jobTitle = selected[0].data.name;
    
        var copyJobPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'copyJobPanel',
            id: 'copyJobPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'copy'
                }),
                Ext.create('RP.Moca.Console.Util.JobsInformation', {})
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveJob, scope: copyJobPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {copyJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            copyJobPanel.setTitle('Copy Job: ' + jobTitle);
            copyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.ScheduledMaint', {}));
            copyJobPanel.setHeight(525);
            copyJobPanel.down('#command').setHeight(255);
            copyJobPanel.down('#type').setValue('cron');
            this.populateSchedule(copyJobPanel, selected[0].data.schedule);
            copyJobPanel.down('#informationPanel').getLayout().setActiveItem('schedulePanel');
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            copyJobPanel.setTitle('Copy Job: ' + jobTitle);
            copyJobPanel.down('#informationPanel').add(Ext.create('RP.Moca.Console.Util.TimerMaint', {}));
            copyJobPanel.down('#start_delay').setValue(selected[0].data.start_delay);
            copyJobPanel.down('#timer').setValue(selected[0].data.timer);
            copyJobPanel.down('#type').setValue('timer');
            copyJobPanel.setHeight(500);
            copyJobPanel.down('#command').setHeight(230);
            copyJobPanel.down('#informationPanel').getLayout().setActiveItem('timerPanel');
        }
        
        copyJobPanel.down('#job_id').setValue('COPY_' + selected[0].data.job_id);
        copyJobPanel.down('#name').setValue('Copy of ' + selected[0].data.name);
        var logFile = selected[0].data.log_file;
        if (Ext.isEmpty(logFile)) {
            copyJobPanel.down('#log_file').setValue('');
        }
        else {
            copyJobPanel.down('#log_file').setValue(logFile + '.copy');
        }
        copyJobPanel.down('#action').setValue('add');
        
        this.populate(copyJobPanel, selected[0].data);
    
        copyJobPanel.show();
    },
        
    // Handler to delete selected Job
    deleteJob: function() {
        var selected;
        if (Ext.getCmp('scheduledJobsButton').pressed) {
            selected = Ext.getCmp('scheduledJobs').selModel.selected.items;
        } 
        if (Ext.getCmp('timerJobsButton').pressed) {
            selected = Ext.getCmp('timedJobs').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single job to delete and try again.');
            return;
        }
    
        var deleteJobPanel = Ext.create('Ext.panel.Panel', {
            layout: 'fit',
            modal: true,
            floating: true,
            closable: true,
            width: 450,
            title: 'Delete Job',
            id: 'deleteJobPanel',
            items: [{
                xtype: 'label',
                text: selected[0].data.name,
                style: 'font-weight:bold;font-size:12px;',
                margin: 30
            },{
                xtype: 'hidden',
                text: selected[0].data.job_id,
                itemId: 'job_id'
            }],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Delete', handler: this.removeJob },
                { xtype: 'button', text: 'Cancel', handler: function() {deleteJobPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
    
        deleteJobPanel.show();
    },
    
    populateSchedule: function(panel, schedule) {
        panel.down('#schedule').setValue(schedule);
        panel.down('#spacer2').hide();
        
        // If the schedule isn't simple we just show the user the
        // schedule otherwise populate the simple widgets (hours + day of week)
        if (!this.simpleScheduleRegex.test(schedule.trim())) {
            panel.down('#simpleScheduleButton').disable();
            panel.down('#tooAdvanced').show();
            panel.down('#simpleSchedule').hide();
            panel.down('#advancedSchedule').show();
        }
        else{ 
            var cron = schedule.split(' ');
            if (cron.length >= 6) {
                var days = cron.splice(5,1);
                var hours = cron.splice(2,1);
                if (days[0].length > 0) {
                    var weekDays;
                    if (days[0] === '*') { 
                        weekDays = ['1','2','3','4','5','6','7'];
                    }
                    else if (days[0] === '?') {
                        weekDays = '';
                    }
                    else if (days[0].length % 2 !== 0) {
                        weekDays = days[0].split(',');
                    }
                    else {
                        weekDays = days[0];
                    }
                    Ext.getCmp('schedule_repeat').setValue(weekDays);
                }
                if (hours[0] === '*') {
                    Ext.getCmp('schedule_time').setValue('*');
                }
                else if (hours[0] >= 12) {
                    hours = hours - 12;
                    Ext.getCmp('pm_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
                else if (hours[0] < 12) {
                    Ext.getCmp('am_time').setValue(true);
                    Ext.getCmp('schedule_time').setValue(hours.toString());
                }
            }
        }
    },
    
    populate: function(panel, selected) {
        var role_id = Ext.getCmp('timedJobs').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('scheduledJobs').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             panel.down('#role_id').store.add({field1:key});
        });
        
        panel.down('#task_id').hide();
        panel.down('#auto_start').hide();
        panel.down('#restart').hide();
        panel.down('#run_dir').hide();
        panel.down('#tracingPanel').setHeight(200);
        panel.down('#command').setValue(selected.command);
        panel.down('#role_id').setValue(selected.role_id);
        panel.down('#grp_nam').setValue(selected.grp_nam);
        panel.down('#type').setValue(selected.type);
        
        panel.down('#enabled').setValue(selected.enabled);
        panel.down('#overlap').setValue(selected.overlap);
        panel.down('#trace_level').setValue(selected.trace_level);
    },
    
    // Handler function to save a job
    saveJob: function() {
        var maintenance = Ext.getCmp('maintenance'),
            job_id = maintenance.down('#job_id').value,
            name = maintenance.down('#name').value,
            command = maintenance.down('#command').value;
        
        if (job_id === '') {
            RP.Moca.util.Msg.alert('Console', 'A Job ID is required.');
            return;
        }
        if (name === '') {
            RP.Moca.util.Msg.alert('Console', 'A name is required.');
            return;
        }
        if (command === '') {
            RP.Moca.util.Msg.alert('Console', 'A command is required.');
            return;
        }
        
        var panel = this.up().up();
        
        var chkGroup = Ext.getCmp("traceCheckBoxGroup");
        
        var traceLevel = '';
        if (Ext.getCmp('tracingPanel').down('#trace_level').getValue()) {    
            traceLevel = '*';
        }
        
        var start_delay;
        var timer;
        var schedule;
        var timerPanel = Ext.getCmp('timerPanel');
        var schedulePanel = Ext.getCmp('schedulePanel');
        
        if (timerPanel) {
            start_delay = timerPanel.down('#start_delay').value;
            timer = timerPanel.down('#timer').value;
        }
        
        if (schedulePanel){
            if (schedulePanel.down('#advancedSchedule').hidden) {
                var array = schedulePanel.down('#schedule').value.split(' ');
                var seconds = array[0];
                var minutes = array[1];
                var hours;
                var dayMonth = array[3];
                var month = array[4];
                var dayWeek;
                if (schedulePanel.down('#am_time').value){
                    hours = schedulePanel.down('#schedule_time').value;
                }
                if (schedulePanel.down('#pm_time').value) {
                    hours = parseInt(schedulePanel.down('#schedule_time').value, 10)+12;
                }
                if (!hours) {
                    hours = '?';
                }
                dayWeek = schedulePanel.down('#schedule_repeat').value;
                if(!dayWeek) {
                    dayWeek = [''];
                }
                dayWeek.sort();
                if (dayWeek[0] === '') {
                    dayWeek = '?';
                }
                if (dayWeek.length === 7) {
                    dayWeek = '*';
                }
                if (dayWeek.length > 0 && dayWeek[0] !== '?') {
                    dayMonth = '?';
                }
                
                schedule = seconds + ' ' + minutes + ' ' + hours + ' ' + dayMonth + ' ' + month + ' ' + dayWeek;
            }
            else {
                schedule = schedulePanel.down('#schedule').value;
            }
        }

        var grp_nam = maintenance.down('#grp_nam').value,
            role_id = maintenance.down('#role_id').lastValue,
            type = maintenance.down('#type').value,
            action = maintenance.down('#action').value,
            log_file = Ext.getCmp('tracingPanel').down('#log_file').value,
            enabled = Ext.getCmp('behaviorPanel').down('#enabled').value,
            overlap = Ext.getCmp('behaviorPanel').down('#overlap').value,
            jobEnvStore = Ext.getCmp('envPanel').jobEnvironmentStore,
            jobStore = Ext.getStore('masterJobStore'),
            modifiedRecords = jobEnvStore.getModifiedRecords(),
            newRecords = jobEnvStore.getNewRecords(),
            removedRecords = jobEnvStore.getRemovedRecords(),
            mode = maintenance.mode;
        
        if (mode === undefined) {
            RP.Moca.util.Msg.alert('Console', 'Error: maintenance panel mode undefined.');
            return;
        }
        
        // Break if job already exists
        if (jobStore.find('job_id', job_id) !== -1 && mode !== 'edit') {
            RP.Moca.util.Msg.alert('Console', 'Cannot create duplicate job.');
            return;
        }
        
        // Sync environment variables
        var ifBreak = false;
        jobEnvStore.each(function(record) {
            if (Ext.isEmpty(record.get('var_nam')) || Ext.isEmpty(record.get('value'))) {
                Ext.Msg.show({
                    title: 'Warning',
                    width: 400,
                    msg: 'Invalid environment variable. Name and Value are required.',
                    buttons: Ext.MessageBox.OK,
                    icon: Ext.MessageBox.WARNING
                }); 
                ifBreak = true;
            }
        });
        
        // We don't want to post anything if there is an invalid variable
        if (ifBreak) {
            return;
        }
        // Sync created
        Ext.each(newRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    job_id: job_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveJobEnvironment',
                    action: 'add'
                }
            });
        }, this);
        
        // Sync modified
        Ext.each(modifiedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    job_id: job_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveJobEnvironment',
                    action: 'edit'
                }/*,
                success : function(response){
                    jobEnvStore.commitChanges();
                },
                failure : function(response){
                    jobEnvStore.rejectChanges();
                }*/
            });
        }, this);
               
        // Sync removed
        Ext.each(removedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    job_id: job_id,
                    name: record.data.var_nam,
                    m: 'removeJobEnvironment'
                }
                /*success : function(response){
                    jobEnvStore.commitChanges();
                },
                failure : function(response){
                    jobEnvStore.rejectChanges();
                }*/
            });
        }, this);
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                job_id: job_id,
                name: name,
                grp_nam: grp_nam,
                role_id: role_id,
                command: command,
                type: type,
                enabled: enabled,
                overlap: overlap,
                start_delay: start_delay,
                log_file: log_file,
                trace_level: traceLevel,
                schedule: schedule,
                timer: timer,
                action: action,
                m: 'saveJob'
            },
            success: function(response){
                var json = Ext.decode(response.responseText);
                this._status = parseInt(json.status, 10);
                this._message = json.message;
                
                if (this._status !== 0 && this._status !== null) {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: this._message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                }
                else {
                    Ext.getCmp('jobsContainer').refresh();
                    panel.close();
                }
            }
        });
    },
    
    // Handler function to delete a job
    removeJob: function() {
        var panel = Ext.getCmp('deleteJobPanel');

        var job_id = panel.down('#job_id').text.trim();
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                job_id: job_id,
                m: 'removeJob'
            },
            success: function(response){
                var json = Ext.decode(response.responseText);
                this._status = parseInt(json.status, 10);
                this._message = json.message;
                
                if (this._status !== 0 && this._status !== null) {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: this._message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                }
                else {
                    Ext.getCmp('jobsContainer').refresh();
                    panel.close();
                }
            }
        });
    }
});


// Timed Jobs
Ext.define("RP.Moca.Console.Jobs.Timed", {
    extend: "Ext.grid.Panel",

    initComponent: function () {
        // Handler function to render interval/schedule
        var intervalScheduleHandler = function(sprite, record, attributes, index, store) {
            return (attributes.data.schedule === null ? attributes.data.timer : attributes.data.schedule);
        };
        
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
        
        // FLAG new CheckBoxColumn works, but Ext.create doesn't. 
        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Scheduled',
            dataIndex: 'scheduled',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Name',
            dataIndex: 'name',
            id: 'timed_jobs_name',
            flex: 2,
            minWidth: 150
        },{
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'command',
            id: 'timed_jobs_command',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Interval',
            renderer: intervalScheduleHandler,
            flex: 1,
            align: 'right',
            minWidth: 125
        }, {
            xtype: 'checkcolumn',
            text: 'Enabled',
            dataIndex: 'enabled',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            text: 'Overlap',
            dataIndex: 'overlap',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Start Delay',
            dataIndex: 'start_delay',
            id: 'timed_jobs_start_delay',
            flex: 1,
            minWidth: 125,
            align: 'right'
        }];

        this.store = Ext.create("RP.Moca.util.Store", {
            viewConfig: {
                stripeRows: true
            },
            model: 'JobModel',
            groupers: [{
                property: 'type',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }, {
                property: 'command',
                direction: 'ASC',
                root: 'data'
            }]
        });
 
        // Hook into the Master Job Store to refresh the Local Store.
        this.masterStore = Ext.getStore('masterJobStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            id: 'timedJobs',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No timer based jobs currently exist.',
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // Refresh the Master Job Store which will
        // initiate a Local Job Store refresh.
        this.masterStore.load();
    },    
    
    onMasterStoreLoad: function() {
        // Refresh the Local Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('Timer-Based');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
        
        // Refresh the Button Text.
        var button = Ext.getCmp('timerJobsButton');
        if (button.rendered) {
            button.setText('Timer (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Timer (' + this.store.getCount() +')');
            }, this);
        }
    }
});

// Scheduled Jobs
Ext.define("RP.Moca.Console.Jobs.Scheduled", {
    extend: "Ext.grid.Panel",

    initComponent: function () {
    
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }

        // Handler function to render interval/schedule
        var intervalScheduleHandler = function(sprite, record, attributes, index, store) {
            return (attributes.data.schedule === null ? attributes.data.timer : attributes.data.schedule);
        };
        
        // FLAG new CheckBoxColumn works, but Ext.create doesn't. 
        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Scheduled',
            dataIndex: 'scheduled',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Name',
            dataIndex: 'name',
            id: 'schedule_jobs_name',
            flex: 2,
            minWidth: 150
        },{
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'command',
            id: 'schedule_jobs_command',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Schedule',
            renderer: intervalScheduleHandler,
            flex: 2,
            minWidth: 150
        }, {
            xtype: 'checkcolumn',
            text: 'Enabled',
            dataIndex: 'enabled',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            text: 'Overlap',
            dataIndex: 'overlap',
            flex: 1,
            minWidth: 100
        }];

        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'JobModel',
            viewConfig: {
                stripeRows: true
            },
            groupers: [{
                property: 'type',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }, {
                property: 'command',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook into the Master Job Store to refresh the local Store.
        this.masterStore = Ext.getStore('masterJobStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            id: 'scheduledJobs',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No schedule based jobs currently exist.',
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // Refresh the Master Job Store which will
        // initiate a local store refresh.
        this.masterStore.load();
    },
    
    onMasterStoreLoad: function() {
        // Refresh the Schedule Based Job Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('Schedule Based');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
    
        // Refresh the Button Text.
        var button = Ext.getCmp('scheduledJobsButton');
        if (button.rendered) {
            button.setText('Scheduled (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Scheduled (' + this.store.getCount() +')');
            }, this);
        }
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\LogFiles.js
//////////////////////
// View
Ext.define("RP.Moca.Console.LogFiles.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('logFilesGridPanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Log Files',
            layout: 'fit',
            items: new RP.Moca.Console.LogFiles.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('logFilesGridPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.LogFiles.Grid", {
    extend: "Ext.grid.Panel",
    id: 'logFilesGridPanel',
    
    initComponent: function() {

        // Handler function for downloading
        var downloadLogFile = function() {
            var selected = this.selModel.selected.items;

            // Make sure a row was selected first.
            if (!selected || selected.length === 0) {
                Ext.Msg.alert('Console', 'Please select a log file to download and try again.');
                return;
            }

            var filename = "";

            for (var i = 0, len = selected.length; i < len; i++) {
                var s = selected[i];

                // Get the filename for this log file.
                var tmpFilename = s.get("pathname");
                if (i === 0) {
                    filename = tmpFilename;
                }
                else {
                    filename = filename + "," + tmpFilename;
                }
            }

            var body = Ext.getBody();

            var frame = body.createChild({
                tag: 'iframe',
                cls: 'x-hidden',
                src: '/console/download?filename=' + filename
            });
        };

        var myColumns = [{
            header: 'Filename',
            dataIndex: 'filename',
            minWidth: 125,
            flex: 4
            }, {
            header: 'Pathname',
            dataIndex: 'pathname',
            minWidth: 150,
            hidden: true,
            flex: 4
            }, {
            header: 'Last Modified',
            dataIndex: 'modified',
            minWidth: 150,
            flex: 2,
            renderer: RP.Moca.util.Format.datetimeString
            }, {
            header: 'Size',
            dataIndex: 'size',
            minWidth: 150,
            flex: 1,
            align: 'right',
            renderer: RP.Moca.util.Format.convertBytesBase10
        }];

        var myStore = new RP.Moca.util.Store({
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getLogFiles'
                }
            },
            fields:[
                'filename', 'pathname', 'size', {
                name: 'modified',
                type: 'date'
            }],
            sorters: [{
                property: 'pathname',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('logFilesGridPanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('logFilesGridPanel').setLoading(false);
                }
            }
        });
            
        var myToolbar = [{
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Download Log File(s)',
                scope: this,
                handler: downloadLogFile,
                style: {
                    margin: '3px'
                }
            }]
        }, {
            xtype: 'toolbar',
            dock: 'top',
            height: 1
        }];

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            selModel: Ext.create("Ext.selection.RowModel", {
                mode: "MULTI"
            }),
            dockedItems: myToolbar,
            store: myStore,
            columns: myColumns,
            viewConfig: {
                emptyText: 'No log files currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\SessionKeys.js
//////////////////////
// View
Ext.define("RP.Moca.Console.SessionKeys.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('sessionKeysGridPanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Connected Users',
            itemId: 'sessionView',
            layout: 'fit',
            items: new RP.Moca.Console.SessionKeys.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('sessionKeysGridPanel').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

Ext.define("RP.Moca.Console.SessionKeys.ResultsReader", {
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
        var json = Ext.decode(response.responseText);
        if (json !== null) {
            this._status = parseInt(json.status, 10);
            this._message = json.message;
            
            this.checkOkStatus();
        }

        // Parse the data and return it.
        var data = this.callParent(arguments);
        
        var realData = data.records;
        
        for (var i = 0; i < realData.length; i++) {
            realData[i].data.ip = realData[i].data.environment.WEB_CLIENT_ADDR;
            delete realData[i].data.environment.WEB_CLIENT_ADDR;
        }
        
        return data;
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

// Grid
Ext.define("RP.Moca.Console.SessionKeys.Grid", {
    extend: "Ext.grid.Panel",
    id: 'sessionKeysGridPanel',
    
    initComponent: function() {
        var setTitle = function (store, records, options) {
            var me = this;
            var sessionView= me.up('#sessionView');
            if (sessionView.rendered) { 
                sessionView.getHeader().items.items[0].update('Connected Users (' + store.getCount() + ')'); 
            } else { 
                sessionView.on('afterrender', function() { 
                    sessionView.getHeader().items.items[0].update('Connected Users (' + store.getCount() +   ')'); 
                }); 
            }
        };
        
        // Handler function for downloading
        var revokeAuthentication = function() {
            var selected = this.selModel.selected.items[0];

            // Make sure a row was selected first.
            if (!selected || selected.length === 0) {
                Ext.Msg.alert('Console', 'Please select a key to revoke.');
                return;
            }
            
            var sessionId = selected.get("sessionId");
            
            // Make the call     
            RP.Moca.util.Ajax.requestWithTextParams({
                url: '/console',
                method: 'POST',
                params: {
                    m: 'revokeSessionKey',
                    sessionId: sessionId
                },
                success: function (result) {
                    Ext.getCmp('sessionKeysGridPanel').refresh();
                },
                failureAlert: {
                    title: 'Console',
                    msg: 'Could not send user revocation request to the server.'
                }
            });
        };

        var myColumns = [{
            header: 'User',
            dataIndex: 'userId',
            flex: 1,
            minWidth: 150
            }, {
            header: 'Start Date',
            dataIndex: 'createdDate',
            flex: 1,
            minWidth: 175,
            renderer: RP.Moca.util.Format.datetimeString
            }, {
            header: 'Last Request Date',
            dataIndex: 'lastAccess',
            flex: 1,
            minWidth: 175,
            renderer: RP.Moca.util.Format.datetimeString
            }, {
            header: 'IP Address',
            dataIndex: 'ip',
            flex: 1,
            minWidth: 175
            }, {
            header: 'Key',
            dataIndex: 'sessionId',
            flex: 1,
            minWidth: 150,
            hidden: true
            }, {
            header: 'Environment',
            dataIndex: 'environment',
            renderer: RP.Moca.util.Format.objectToString,
            flex: 2,
            minWidth: 150
		    }, {
            header: 'Console Role',
			renderer: function(value, meta, record) {
                            return '<div>' + RP.Moca.util.Format.consoleRoleString(record.getData().role) + '</div>';
            },
            flex: 1,
            minWidth: 150
        }];
        
        Ext.define('SessionKeys', {
            extend: 'Ext.data.Model',
            fields: ['sessionId', {name: 'createdDate',   type: 'date'}, 
            'userId', 'ip', 'lastAccess', 'environment','role']
        });
        
        var myReader = new RP.Moca.Console.SessionKeys.ResultsReader({});
        
        var myStore = new Ext.data.Store({
            autoLoad: true,
            model: 'SessionKeys',
            proxy: {
                type: 'ajax',
                url: '/console',
                extraParams: {
                    m: 'getSessionKeys'
                },
                reader: myReader
            },
            sorters: [{
                property: 'userId',
                direction: 'ASC'
            }],
            listeners: {
                'exception' : function () {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: 'A connection with the server could not be established.\n\n' + 
                             'Check to make sure the server has not been shutdown and try again.',
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                },
                'beforeload' :  function() {
                    Ext.getCmp('sessionKeysGridPanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('sessionKeysGridPanel').setLoading(false);
                }
            }
        });

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Revoke Authentication',
                scope: this,
                handler: revokeAuthentication,
                style: {
                    margin: '3px'
                }
            }]
        });
        var myTopToolbar = Ext.create('Ext.toolbar.Toolbar', { 
            xtype: 'toolbar',
            dock: 'top',
            height: 1
        });
        
        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.disable();
                }
            }
        });

        myStore.on('load', setTitle, this);

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            dockedItems: [myToolbar, myTopToolbar],
            store: myStore,
            columns: myColumns,
            viewConfig: {
                emptyText: 'No logged in sessions exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\NativeProcesses.js
//////////////////////
// View
Ext.define("RP.Moca.Console.NativeProcesses.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('nativeGridPanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Native Processes',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.NativeProcesses.Grid", {
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value" style="table-layout: fixed"><tr><td><b>Native Process:</b></td><td>{moca_prc}</td></tr><tr><td><b>Thread ID:</b></td><td>{threadId}</td></tr><tr><td><b>Created Time:</b></td><td>{created_dt}</td></tr><tr><td><b>Last Request Time:</b></td><td>{last_call_dt}</td></tr><tr><td><b>Last Request:</b></td><td>{last_call}</td></tr><tr><td><b>Requests:</b></td><td>{requests}</td></tr><tr><td><b>Last Command Path:</b></td><td>{command_path}</td></tr></table>', {
                        blankNulls: true
                    })
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('nativeGridPanel').refresh();
                        }
                    })
                ],
                columns: [{
                    header: 'Native Process',
                    dataIndex: 'moca_prc',
                    flex: 1,
                    minWidth: 150
                }, {
                    header: 'Thread ID',
                    dataIndex: 'thread_id',
                    renderer: RP.Moca.util.HyperLink.sessions,
                    flex: 1,
                    minWidth: 100
                }, {
                    header: 'Created Time',
                    dataIndex: 'created_dt',
                    renderer: RP.Moca.util.Format.datetimeString,
                    flex: 1,
                    minWidth: 175
                }, {
                    header: 'Last Request Time',
                    dataIndex: 'last_call_dt',
                    renderer: RP.Moca.util.Format.datetimeString,
                    flex: 1,
                    minWidth: 175
                }, {
                    header: 'Last Request',
                    dataIndex: 'last_call',
                    flex: 3,
                    minWidth: 200
                }, {
                    header: 'Requests',
                    dataIndex: 'requests',
                    align: 'right',
                    flex: 1,
                    minWidth: 100
                }, {
                    header: 'Temporary',
                    dataIndex: 'temp',
                    xtype: 'checkcolumn',
                    hidden: true,
                    flex: 1,
                    minWidth: 100
                }]
            }) 
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.NativeProcesses.Grid", {
    extend: "Ext.grid.Panel",
    id: 'nativeGridPanel',
    
    initComponent: function() {

        // Handler function for Stop Native Process button.
        var stopProcessHandler = function() {
            var selected = this.selModel.selected.items[0];

            // Make sure a row was selected first.
            if (!selected) {
                Ext.Msg.alert('Console', 'Please select a native process to stop and try again.');
                return;
            }
            
            var thread = selected.get("thread_id");
            var moca_prc = selected.get("moca_prc");
            
            var myParams = {
                moca_prc: moca_prc,
                m: 'stopNativeProcess'
            };
            
            var myResultsHandler = function (result) {
                var response = Ext.decode(result.responseText);
                var message = response.message;

                if (response.status !== 0) {
                    Ext.Msg.alert('Console', 'An error occurred attempting to stop the selected native process.\n\n' + message);
                }

                // Refresh the grid panel after we've stopped the process.
                Ext.getCmp('nativeGridPanel').refresh();
            };
            
            var myFailureAlert = {
                title: 'Console',
                msg: 'Could not stop the selected native process.'
            };
            
            // Make sure the task isn't already running.
            if (thread) {
                // Show confirmation dialog
                Ext.Msg.show({
                    title: 'Stop Native Process?',
                    msg: 'You are closing a native process that is running.  This can cause instability.  Are you sure you want to do this?',
                    buttons: Ext.Msg.YESNO,
                    fn: function(result) {
                        if (result == 'no') {
                            return;
                        }
                        
                        Ext.getCmp('nativeGridPanel').realStopProcess(myParams, 
                            myResultsHandler, myFailureAlert);
                    }
                });
            }
            else {
                this.realStopProcess(myParams, myResultsHandler, myFailureAlert);
            }
        };

        var myStore = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getNativeProcesses'
                }
            },
            fields: [ 'moca_prc', 'requests', 'last_call', 'thread_id', 
                      'command_path', 'temp', {
                name: 'created_dt',
                type: 'date'
            }, {
                name: 'last_call_dt',
                type: 'date'
            }],
            sorters: [{
                property: 'moca_prc',
                direction: 'ASC'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('nativeGridPanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('nativeGridPanel').setLoading(false);
                }
            }
        });

        var myToolbar = Ext.create('Ext.toolbar.Toolbar', {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [{
                text: 'Stop Native Process',
                scope: this,
                handler: stopProcessHandler,
                style: {
                    margin: '3px'
                }
            }]
        });

        var myTopToolbar = Ext.create('Ext.toolbar.Toolbar', { 
            xtype: 'toolbar',
            dock: 'top',
            height: 1
        });

         RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    myToolbar.disable();
                }
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            dockedItems: [myToolbar, myTopToolbar],
            store: myStore,
            viewConfig: {
                emptyText: 'No native processes currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        // Call our superclass
        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    },
    realStopProcess: function(myParams, myResultsHandler, myFailureAlert) {
        // Make an Ajax call to the server.
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultsHandler,
            failureAlert: myFailureAlert
        });
    
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\Clustering.js
//////////////////////
Ext.define("RP.Moca.Console.Clustering.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Clustering',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.Clustering.Grid", {
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander', 
                    header: '+',
                    rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value" style="table-layout: fixed; width: 100%;"><tr><td style="padding-bottom:5px;padding-right:5px;width:45px;" valign="top"><b>Name:</b></td><td style="padding-bottom:5px;" valign="top">{name}</td></tr><tr><td style="padding-right:5px; "  valign="top"><b>Roles:</b></td><td style= "word-wrap: break-word;">{value}</td></tr></table>', '<br>')
                },
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('clusteringGrid').refresh();
                        }
                    })
                ],
                columns:  [
                    { header: 'Name', dataIndex: 'name', width: 300},
                    { header: 'Value', dataIndex: 'value'}
                ]
            })
        });

        this.callParent(arguments);
    },

    onActivate: function () {
        RP.Moca.Console.Clustering.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('clusteringGrid').refresh();
        }

        this.activateCount++;
    }
});

// Grid
Ext.define("RP.Moca.Console.Clustering.Grid", {
    extend: "Ext.grid.Panel",
    id: 'clusteringGrid',

    constructor: function (config) {

        var myStore = Ext.create("RP.Moca.util.Store", {
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getClusterRoles'
                }
            },
           fields: [
                { name: 'name' },
                { name: 'value' }
            ],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('clusteringGrid').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('clusteringGrid').setLoading(false);
                }
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            store: myStore,
            autoExpandColumn: 'roleColumn',
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }]
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\Registry.js
//////////////////////
// View
Ext.define("RP.Moca.Console.Registry.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    initComponent: function () {
        Ext.apply(this, {
            title: 'Registry',
            layout: 'fit',
            items: new RP.Moca.Console.Registry.Panel({
                padding: '0px 5px 7px 0px'
            })
        });

        this.callParent(arguments);
    }
});

// Panel
Ext.define("RP.Moca.Console.Registry.Panel", {
    extend: "Ext.panel.Panel",
 
    initComponent: function () {

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: {
                m: 'getRegistry'
            },
            scope: this,
            success: function (result) {
                var content = Ext.decode(result.responseText).data;
                var format = '<pre>{0}</pre>';
                this.update(Ext.String.format(format, content));
            },
            failureAlert: {
                title: 'Console',
                msg: 'Could not get registry information from the server.'
            }
        });

        // Apply some stuff
        Ext.apply(this, {
            autoScroll: true,
            bodyStyle: {
                background: '#ffffff'
            }
        });

        this.callParent(arguments);
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\JobHistory.js
//////////////////////
// View
Ext.define("RP.Moca.Console.JobHistory.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('jobHistoryGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Job Execution History',
            layout: 'fit',
            padding: '0px 5px 7px 0px',
            items: [
                this.createGrid()
            ]
        });

        this.callParent(arguments);
    },
    
    
    createGrid: function() {
        return new RP.Moca.Console.JobHistory.Grid({
            plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr>' +
                    '<td><b>Job ID:</b></td><td>{job_id}</td>' +
                    '</tr><tr>' +
                    '<td><b>Node URL:</b></td><td>{node_url}</td>' +
                    '</tr><tr>' +
                    '<td><b>Status:</b></td><td>{status}</td>' +
                    '</tr><tr>' +
                    '<td><b>Message:</b></td><td>{message}</td>' +
                    '</tr><tr>' +
                    '<td><b>Start Date:</b></td><td>{start_dte}</td>' +
                    '</tr><tr>' +
                    '<td><b>End Date:</b></td><td>{end_dte}</td>' +
                    '</tr></table>',
                    {
                        blankNulls: true
                    })
                },
                new RP.ui.RefreshablePlugin({
                    performRefresh: function() {
                        Ext.getCmp('jobHistoryGrid').refresh();
                    }
                })
            ],
            columns: [{
                header: 'Job ID',
                dataIndex: 'job_id',
                flex: 2
            },{
                header: 'Node URL',
                dataIndex: 'node_url',
                flex: 3
            },{
                header: 'Status',
                dataIndex: 'status',
                flex: 1
            },{
                header: 'Message',
                dataIndex: 'message',
                flex: 2
            },{
                header: 'Start Date',
                dataIndex: 'start_dte',
                renderer: RP.Moca.util.Format.datetimeString,
                flex: 3
            },{
                header: 'End Date',
                dataIndex: 'end_dte',
                renderer: RP.Moca.util.Format.datetimeString,
                flex: 3
            }],
            bbar: this.createToolbar(),
            store: this.getStore()
        });
    },
    
    createToolbar: function() {
        if (!this.jobPageToolbar) {
            this.jobPageToolbar =  Ext.create('Ext.PagingToolbar', {
                store: this.getStore(),
                displayInfo: true,
                inputItemWidth: 70,
                displayMsg: 'Displaying {0} - {1} of {2}',
                emptyMsg: 'No data.'
            });
            this.jobPageToolbar.down('#refresh').hide();
            this.jobPageToolbar.on({
                change: function( pagingToolBar, changeEvent ) {
                    var t = pagingToolBar.down('#inputItem'),
                        m = new Ext.util.TextMetrics(t.getEl()),
                        s = m.getSize(t.getValue());
                    t.setWidth(s.width + 22);
                }
            }); 
        }
        return this.jobPageToolbar;
    },
    
    getStore: function() {
        if (!this.store) {
            this.store = new RP.Moca.util.Store({
                autoLoad: true,
                pageSize: 25,
                remoteSort: true,
                proxy: {
                    type: 'ajax',
                    url: '/console',
                    extraParams: {
                        m: 'getJobHistory'
                    },
                    reader: new RP.Moca.Console.Results.ResultsReader()
                },
                fields: [ 'job_id', 'node_url', { name: 'start_dte', type: 'date' }, 'status', 'message', { name: 'end_dte', type: 'date' } ],
                sorters: [{
                    property: 'start_dte',
                    direction: 'DESC'
                }],
                listeners: {
                    'beforeload' :  function() {
                        Ext.getCmp('jobHistoryGrid').setLoading(true);
                    },
                    'load' :  function() {
                        Ext.getCmp('jobHistoryGrid').setLoading(false);
                    }
                }
            });
        }
        return this.store;
    }
});

// Grid
Ext.define("RP.Moca.Console.JobHistory.Grid", {
    extend: "Ext.grid.Panel",
    id: 'jobHistoryGrid',
    
    initComponent: function () {

        // Call our superclass.
        Ext.apply(this, {
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }]
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\TaskHistory.js
//////////////////////
// View
Ext.define("RP.Moca.Console.TaskHistory.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('taskHistoryGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Task Execution History',
            layout: 'fit',
            padding: '0px 5px 7px 0px',
            items: [
                this.createGrid()
            ]
        });

        this.callParent(arguments);
    },
    
    
    createGrid: function() {
        return new RP.Moca.Console.TaskHistory.Grid({
            plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr>' +
                    '<td><b>Task ID:</b></td><td>{task_id}</td>' +
                    '</tr><tr>' +
                    '<td><b>Node URL:</b></td><td>{node_url}</td>' +
                    '</tr><tr>' +
                    '<td><b>Status:</b></td><td>{status}</td>' +
                    '</tr><tr>' +
                    '<td><b>Start Cause:</b></td><td>{start_cause}</td>' +
                    '</tr><tr>' +
                    '<td><b>Start Date:</b></td><td>{start_dte}</td>' +
                    '</tr><tr>' +
                    '<td><b>End Date:</b></td><td>{end_dte}</td>' +
                    '</tr></table>',
                    {
                        blankNulls: true
                    })
                },
                new RP.ui.RefreshablePlugin({
                    performRefresh: function() {
                        Ext.getCmp('taskHistoryGrid').refresh();
                    }
                })
            ],
            columns: [{
                header: 'Task ID',
                dataIndex: 'task_id',
                flex: 2
            },{
                header: 'Node URL',
                dataIndex: 'node_url',
                flex: 3
            },{
                header: 'Status',
                dataIndex: 'status',
                flex: 3
            },{
                header: 'Start Cause',
                dataIndex: 'start_cause',
                flex: 1
            },{
                header: 'Start Date',
                dataIndex: 'start_dte',
                renderer: RP.Moca.util.Format.datetimeString,
                flex: 3
            },{
                header: 'End Date',
                dataIndex: 'end_dte',
                renderer: RP.Moca.util.Format.datetimeString,
                flex: 3
            }],
            bbar: this.createToolbar(),
            store: this.getStore()
        });
    },
    
    createToolbar: function() {
        if (!this.taskPageToolbar) {
            this.taskPageToolbar =  Ext.create('Ext.PagingToolbar', {
                store: this.getStore(),
                displayInfo: true,
                inputItemWidth: 70,
                displayMsg: 'Displaying {0} - {1} of {2}',
                emptyMsg: 'No data.'
            });
            this.taskPageToolbar.down('#refresh').hide();
            this.taskPageToolbar.on({
                change: function( pagingToolBar, changeEvent ) {
                    var t = pagingToolBar.down('#inputItem'),
                        m = new Ext.util.TextMetrics(t.getEl()),
                        s = m.getSize(t.getValue());
                    t.setWidth(s.width + 22);
                }
            }); 
        }
        return this.taskPageToolbar;
    },
    
    getStore: function() {
        if (!this.store) {
            this.store = new RP.Moca.util.Store({
                autoLoad: true,
                pageSize: 25,
                remoteSort: true,
                proxy: {
                    type: 'ajax',
                    url: '/console',
                    extraParams: {
                        m: 'getTaskHistory'
                    },
                    reader: new RP.Moca.Console.Results.ResultsReader()
                },
                fields: [ 'task_id', 'node_url', { name: 'start_dte', type: 'date' }, 'status', 'start_cause', { name: 'end_dte', type: 'date' } ],
                sorters: [{
                    property: 'start_dte',
                    direction: 'DESC'
                }],
                listeners: {
                    'beforeload' :  function() {
                        Ext.getCmp('taskHistoryGrid').setLoading(true);
                    },
                    'load' :  function() {
                        Ext.getCmp('taskHistoryGrid').setLoading(false);
                    }
                }
            });
        }
        return this.store;
    }
});

// Grid
Ext.define("RP.Moca.Console.TaskHistory.Grid", {
    extend: "Ext.grid.Panel",
    id: 'taskHistoryGrid',
    
    initComponent: function () {

        // Call our superclass.
        Ext.apply(this, {
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }]
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\ResourceUsage.js
//////////////////////
// View
Ext.define("RP.Moca.Console.ResourceUsage.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('resourceUsagePanel').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Resource Usage',
            layout: 'fit',
            items: new RP.Moca.Console.ResourceUsage.Panel({
                padding: '0px 5px 7px 0px',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('resourceUsagePanel').refresh();
                        }
                    })
                ]
           })
       });
 
       this.callParent(arguments);
   }
});

// Panel
Ext.define("RP.Moca.Console.ResourceUsage.Panel", {
    extend: "Ext.Panel",
    id: 'resourceUsagePanel',
    
    initComponent: function () {
 
        this.store = Ext.create("RP.Moca.util.Store", { 
            autoLoad: true,
            proxy: {
                extraParams: {
                    m: 'getResourceUsage'
                }
            },
            fields: [
              'current_heap_used', 'current_heap_size', 'max_heap_size', 'delta_current_heap_size', 
              'delta_max_heap_size', 'current_sessions', 'peak_sessions', 'max_sessions', 
              'delta_peak_sessions', 'delta_max_sessions', 'current_native_processes', 
              'peak_native_processes', 'max_native_processes', 'delta_peak_processes', 
              'delta_max_processes', 'current_db_connections', 'peak_db_connections', 
              'max_db_connections', 'delta_peak_db_connections', 'delta_max_db_connections'
            ],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('resourceUsagePanel').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('resourceUsagePanel').setLoading(false);
                }
            }
        });

        this.store.on("load", this.onStoreLoad, this);
 
        var myMemoryUsage = new RP.Moca.Console.UsagePanel({
            title: 'Memory',
            currentName: 'Current Heap Used',
            peakName: 'Current Heap Size',
            maxName: 'Max Heap Size',
            current: 'current_heap_used',
            peak: 'current_heap_size',
            max: 'max_heap_size',
            delta_peak: 'delta_current_heap_size',
            delta_max: 'delta_max_heap_size',
            units: 'mb',
            store: this.store
        });

        var mySessionUsage = new RP.Moca.Console.UsagePanel({
            title: 'Sessions',
            currentName: 'Current Sessions',
            peakName: 'Peak Sessions',
            maxName: 'Max Sessions',
            current: 'current_sessions',
            peak: 'peak_sessions',
            max: 'max_sessions',
            delta_peak: 'delta_peak_sessions',
            delta_max: 'delta_max_sessions',
            store: this.store,
            style: 'padding-left: 5px;'
        });

        var myNativeProcessUsage = new RP.Moca.Console.UsagePanel({
            title: 'Native Processes',
            currentName: 'Current Native Processes',
            peakName: 'Peak Native Processes',
            maxName: 'Max Native Processes',
            current: 'current_native_processes',
            peak: 'peak_native_processes',
            max: 'max_native_processes',
            delta_peak: 'delta_peak_processes',
            delta_max: 'delta_max_processes',
            store: this.store
        });

        var myDatabaseConnectionUsage = new RP.Moca.Console.UsagePanel({
            title: 'Database Connections',
            currentName: 'Current Database Connections',
            peakName: 'Peak Database Connections',
            maxName: 'Max Database Connections',
            current: 'current_db_connections',
            peak: 'peak_db_connections',
            max: 'max_db_connections',
            delta_peak: 'delta_peak_db_connections',
            delta_max: 'delta_max_db_connections',
            store: this.store,
            style: 'padding-left: 5px;'
        });

        Ext.apply(this, {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'panel',
                border: false,
                padding: 5,
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                flex: 1,
                items: [
                    myMemoryUsage, mySessionUsage
                ]
            }, {
                xtype: 'panel',
                border: false,
                padding: 5,
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                flex: 1,
                items: [
                    myNativeProcessUsage, myDatabaseConnectionUsage
                ]
            }]
        });

        this.callParent(arguments);
   },

   refresh: function() {
        this.store.load();
   },

   onStoreLoad: function() {
        this.items.items[0].items.items[0].refresh();
        this.items.items[0].items.items[1].refresh();
        this.items.items[1].items.items[0].refresh();
        this.items.items[1].items.items[1].refresh();
   }
});
 
// Memory Usage Panel
Ext.define("RP.Moca.Console.UsagePanel", {
    extend: "Ext.panel.Panel",
    units: '',
 
    initComponent: function() {

        this.store.on('load', this.refresh, this);

        this.fieldsPanel = new Ext.form.Panel({
            flex: 1,
            border: false,
            defaultType: 'displayfield',
            layout: 'anchor',
            defaults: {
                labelWidth: this.lwidth
            },
            padding: '10px 20px',
            items: [{
                fieldLabel: this.currentName,
                itemId: 'current'
            }, {
                fieldLabel: this.peakName,
                itemId: 'peak'
            }, {
                fieldLabel: this.maxName,
                itemId: 'max'
            }]
        });

        this.chart = Ext.create("Ext.chart.Chart", {
            xtype: 'chart',
            height: 100,
            animate: true,
            shadow: false,
            store: this.store,
            axes: [{
                type: 'Numeric',
                position: 'bottom',
                fields: [this.current, this.delta_peak, this.delta_max],
                title: false,
                grid: true
            }],
            gradients: [{
                id: 'greenGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#89e746'
                    },
                    100: {
                        color: '#3ba100'
                    }
                }
            },  {
                id: 'yellowGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#f8fd77'
                    },
                    100: {
                        color: '#fff600'
                    }
                }
            },  {
                id: 'redGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#ff4a4c'
                    },
                    100: {
                        color: '#c41515'
                    }
                }
            }],
            series: [{
                type: 'bar',
                axis: 'bottom',
                gutter: 80,
                yField: [this.current, this.delta_peak, this.delta_max],
                stacked: true,
                showInLegend: false,
                renderer: function(sprite, record, attr, index, store) {
                    var color = ['url(#redGradient)',
                                 'url(#yellowGradient)',
                                 'url(#greenGradient)'][index];
                    return Ext.apply(attr, {
                        fill: color
                    });
                },
                tips: {
                    trackMouse: true,
                    width: 255,
                    height: 28,
                    currentName: this.currentName,
                    current: this.current,
                    peakName: this.peakName,
                    peak: this.peak,
                    maxName: this.maxName,
                    max: this.max,
                    delta_max: this.delta_max,
                    delta_peak: this.delta_peak,
                    units: this.units,
                    renderer: function(storeItem, item) {
                        var select, amount;
                        if (item.value[1] === storeItem.data[this.current]) {
                            select = this.currentName;
                            amount = storeItem.data[this.current];
                        }
                        else if (item.value[1] == storeItem.data[this.delta_max]) {
                            select = this.maxName;
                            amount = storeItem.data[this.max];
                        }
                        else {
                            select = this.peakName;
                            amount = storeItem.data[this.peak];
                        }
                        this.setTitle(select + ': ' + amount + this.units);
                    }
                }
            }]
        });

        Ext.apply(this, {
            title: this.title,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            border: true,
            flex: 1,
            items: [
                this.chart, this.fieldsPanel
            ]
        });

        this.callParent(arguments);
    },

    refresh: function (scope, records, successful, eOpts) {
        if (this.store.totalCount === 0) {
            return;
        }

        if (successful) {
            var myRecord = this.store.getAt(0).data;

            this.fieldsPanel.getComponent('current').setValue(myRecord[this.current] + this.units);
            this.fieldsPanel.getComponent('peak').setValue(myRecord[this.peak] + this.units);
            this.fieldsPanel.getComponent('max').setValue(myRecord[this.max] + this.units);
        }
    }
});
//////////////////////
// ..c\javascript\console\sysinfo\SystemProperties.js
//////////////////////
// View
Ext.define("RP.Moca.Console.SystemProperties.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,
    onActivate: function () {
        this.callParent(arguments);
        if (this.activateCount > 0) {
            Ext.getCmp('systemPropertiesGrid').refresh();
        }
        this.activateCount++;
    },
    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'System Properties',
            layout: 'fit',
            items: [ new RP.Moca.Console.SystemProperties.Grid({
                padding: '0px 5px 7px 0px',
                plugins: [{
                    ptype: 'rowexpander',
                    rowBodyTpl: new Ext.XTemplate('<div class="rp-expanded-value">', '{value}', '</div>')
                },
                new RP.ui.RefreshablePlugin({
                    performRefresh: function() {
                        Ext.getCmp('systemPropertiesGrid').refresh();
                    }
                })],
                columns: [{
                    header: 'Name',
                    dataIndex: 'name',
                    minWidth: 150,
                    flex: 1
                },{
                    header: 'Value',
                    dataIndex: 'value',
                    minWith: 300,
                    flex: 3
                }],
                store: new RP.Moca.util.Store({
                    autoLoad: true,
                    proxy: {
                        type: 'ajax',
                        url: '/console',
                        extraParams: {
                            m: 'getSystemProperties'
                        },
                        reader: new RP.Moca.Console.Results.ResultsReader({})
                    },
                    fields: [ 'name', 'value' ],
                    sorters: [{
                        property: 'name',
                        direction: 'ASC'
                    }],
                    listeners: {
                        'beforeload' :  function() {
                            Ext.getCmp('systemPropertiesGrid').setLoading(true);
                        },
                        'load' :  function() {
                            Ext.getCmp('systemPropertiesGrid').setLoading(false);
                        }
                    }
                })
            })]
        });

        this.callParent(arguments);
    }
});

// Grid
Ext.define("RP.Moca.Console.SystemProperties.Grid", {
    extend: "Ext.grid.Panel",
    id: 'systemPropertiesGrid',
    
    initComponent: function () {

        // Call our superclass.
        Ext.apply(this, {
            autoScroll: true,
            viewConfig: {
                preserveScrollOnRefresh: true
            },
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                height: 1
            }]
        });

        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
    }
});

//////////////////////
// ..c\javascript\console\sysinfo\Tasks.js
//////////////////////
// View
Ext.define("RP.Moca.Console.Tasks.View", {
    extend: "RP.util.taskflow.BaseTaskForm",

    activateCount: 0,

    onActivate: function () {
        RP.Moca.Console.Tasks.View.superclass.onActivate.call(this);

        if (this.activateCount > 0) {
            Ext.getCmp('tasksContainer').refresh();
        }

        this.activateCount++;
    },

    initComponent: function () {
        Ext.apply(this, {
            allowRefresh: true,
            title: 'Tasks',
            layout: 'fit',
            items: Ext.create("RP.Moca.Console.Tasks.Container", {
                padding: '0px 5px 7px 0px',
                layout: 'border',
                plugins: [
                    new RP.ui.RefreshablePlugin({
                        performRefresh: function() {
                            Ext.getCmp('tasksContainer').refresh();
                        }
                    })
                ]
            })
        });

        this.callParent(arguments);
    }
});

// Container
Ext.define("RP.Moca.Console.Tasks.Container", {
    extend: "Ext.panel.Panel",
    id: 'tasksContainer',
    
    initComponent: function (config) {
    
        Ext.define('TaskModel', {
            extend: 'Ext.data.Model',
            fields: [
                'task_id', 'role_id', 'name', 'cmd_line',
                'run_dir', 'log_file', 'restart', 'auto_start',
                'start_delay', 'running', 'grp_nam','task_typ', 'nodes', 'trace_level'
            ]
        });
        
        this.store = Ext.create('RP.Moca.util.Store', {
            autoLoad: true,
            storeId: 'masterTaskStore',
            proxy: {
                extraParams: {
                    m: 'getTasks'
                },
                type: 'ajax',
                url: '/console',
                reader: new RP.Moca.Console.Results.ResultsReader({})
            },
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'name',
                direction: 'ASC',
                root: 'data'
            }],
            listeners: {
                'beforeload' :  function() {
                    Ext.getCmp('tasksContainer').setLoading(true);
                },
                'load' :  function() {
                    Ext.getCmp('tasksContainer').setLoading(false);
                }
            }
        });

        var myEditToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'top',
            defaults: {
                scope: this
            },
            items: [{
                text: 'Add',
                handler: this.addTask
            },{
                text: 'Copy',
                handler: this.copyTask
            },{
                text: 'Modify',
                handler: this.modifyTask
            },{
                text: 'Delete',
                handler: this.deleteTask
            },{ 
                xtype: 'tbfill'
            },{
                text: 'Thread',
                id: 'threadTasksButton',
                pressed: true,
                enableToggle: true,
                toggleGroup: 'Tasks',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('tasksContainer').getLayout().setActiveItem('threadTasks');
                },
                style: 'padding:0;margin:0;border-top-right-radius:0;border-bottom-right-radius:0;border-width:0!important;border-right-color:transparent;'
            },{
                text: 'Process',
                id: 'processTasksButton',
                enableToggle: true,
                toggleGroup: 'Tasks',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('tasksContainer').getLayout().setActiveItem('processTasks');
                },
                style: 'padding:0;margin:0;border-radius:0;border-width:0!important;border-right-color:transparent;border-left-color:transparent;'
            },{
                text: 'Daemon',
                id: 'daemonTasksButton',
                enableToggle: true,
                toggleGroup: 'Tasks',
                allowDepress: false,
                handler: function() {
                    Ext.getCmp('tasksContainer').getLayout().setActiveItem('daemonTasks');
                },
                style: 'padding:0;margin:0px 5px 0px 0px;border-top-left-radius:0;border-bottom-left-radius:0;border-width:0!important;border-left-color:transparent;'
            },{ 
                xtype: 'tbfill'
            },{ 
                xtype: 'tbfill'
            }]
        });

        var myActionToolbar = Ext.create('Ext.toolbar.Toolbar', {
            dock: 'bottom',
            items: [{
                text: 'Start Task',
                scope: this,
                handler: this.startTaskHandler,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Stop Task',
                scope: this,
                handler: this.stopTaskHandler,
                style: {
                    margin: '3px'
                }
            }, {
                text: 'Restart Task',
                scope: this,
                handler: this.restartTaskHandler,
                style: {
                    margin: '3px'
                }
            }]
        });

        RP.Moca.util.Role.isReadOnly({ 
            success: function(response){
                if(response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                    //Add
                    myEditToolbar.items.items[0].disable();
                    //Copy
                    myEditToolbar.items.items[1].disable();
                    //Modify
                    myEditToolbar.items.items[2].disable();
                    //Remove
                    myEditToolbar.items.items[3].disable();
                    myActionToolbar.disable();
                }
            }
        });
        
        // Apply some stuff
        Ext.apply(this, {
            layout: 'card',
            dockedItems: [myEditToolbar, myActionToolbar] ,
            items: [
                Ext.create('RP.Moca.Console.Tasks.Thread', {
                    border: false,
                    id: 'threadTasks',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Task ID:</b></td><td>{task_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Running:</b></td><td>{running}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Auto Start:</b></td><td>{auto_start}</td></tr><tr><td><b>Restart:</b></td><td>{restart}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                }),
                Ext.create('RP.Moca.Console.Tasks.Process', {
                    border: false,
                    id: 'processTasks',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Task ID:</b></td><td>{task_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command Line:</b></td><td>{cmd_line}</td></tr><tr><td><b>Running:</b></td><td>{running}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Start In:</b></td><td>{run_dir}</td></tr><tr><td><b>Auto Start:</b></td><td>{auto_start}</td></tr><tr><td><b>Restart:</b></td><td>{restart}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                }),
                Ext.create('RP.Moca.Console.Tasks.Daemon', {
                    border: false,
                    id: 'daemonTasks',
                    plugins: [{
                        ptype: 'rowexpander',
                        rowBodyTpl: new Ext.XTemplate('<table class="rp-expanded-value"><tr><td><b>Task ID:</b></td><td>{task_id}</td></tr><tr><td><b>Name:</b></td><td>{name}</td></tr><tr><td><b>Command Line:</b></td><td>{cmd_line}</td></tr><tr><td><b>Running:</b></td><td>{running}</td></tr><tr><td><b>Nodes:</b></td><td>{nodes}</td></tr><tr><td><b>Role:</b></td><td>{role_id}</td></tr><tr><td><b>Class Name:</b></td><td>{cmd_line}</td></tr><tr><td><b>Start In:</b></td><td>{run_dir}</td></tr><tr><td><b>Auto Start:</b></td><td>{auto_start}</td></tr><tr><td><b>Restart:</b></td><td>{restart}</td></tr><tr><td><b>Group Name:</b></td><td>{grp_nam}</td></tr><tr><td><b>Log File:</b></td><td>{log_file}</td></tr><tr><td><b>Start Delay:</b></td><td>{start_delay}</td></tr><tr><td><b>Tracing:</b></td><td><tpl if="trace_level">On</tpl><tpl if="!trace_level">Off</tpl></td></tr></table>', {
                            blankNulls: true
                        })
                    }]
                })
            ]
        });
        
        this.callParent(arguments);
    },
    refresh: function () {
        this.store.load();
        Ext.getCmp('threadTasks').fireEvent('afterrender', this);
        Ext.getCmp('processTasks').fireEvent('afterrender', this);
        Ext.getCmp('daemonTasks').fireEvent('afterrender', this);
    },
    
    // Handler function for adding a Task
    addTask: function() {
        var addTaskPanel = Ext.create('Ext.form.Panel', {
            modal: true,
            id: 'addTaskPanel',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            width: 800,
            height: 625,
            floating: true,
            closable: true,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'add'
                }),
                Ext.create('RP.Moca.Console.Util.TasksInformation', {}) 
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveTask, scope: addTaskPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {addTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('threadTasksButton').pressed) {
            addTaskPanel.setTitle('Add Thread Based Task');
            addTaskPanel.down('#command').setFieldLabel('Class Name');
            addTaskPanel.down('#run_dir').hide();
            addTaskPanel.setHeight(500);
            addTaskPanel.down('#command').setHeight(230);
            addTaskPanel.down('#task_typ').setValue('T');
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            addTaskPanel.setTitle('Add Process Based Task');
            addTaskPanel.setHeight(560);
            addTaskPanel.down('#tracingPanel').setHeight(260);
            addTaskPanel.down('#command').setHeight(290);
            addTaskPanel.down('#task_typ').setValue('P');
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            addTaskPanel.setTitle('Add Daemon Based Task');
            addTaskPanel.setHeight(560);
            addTaskPanel.down('#tracingPanel').setHeight(260);
            addTaskPanel.down('#command').setHeight(290);
            addTaskPanel.down('#task_typ').setValue('D');
        }
        
        addTaskPanel.down('#job_id').hide();
        addTaskPanel.down('#overlap').hide();
        addTaskPanel.down('#enabled').hide();
        addTaskPanel.down('#timer').hide();
        addTaskPanel.down('#spacer1').hide();
        addTaskPanel.down('#action').setValue('add');
        
        var role_id = Ext.getCmp('threadTasks').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('processTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(Ext.getCmp('daemonTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             addTaskPanel.down('#role_id').store.add({field1:key});
        });
        
        addTaskPanel.show();
    },
    
    // Handler to modify selected Task
    modifyTask: function() {
        var selected;
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single task to modify and try again.');
            return;
        }  
        
        var taskTitle = selected[0].data.name;
        
        var modifyTaskPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'modifyTaskPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'edit'
                }),
                Ext.create('RP.Moca.Console.Util.TasksInformation', {
                    task_id: selected[0].data.task_id
                })
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveTask, scope: modifyTaskPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {modifyTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('threadTasksButton').pressed) {
            modifyTaskPanel.down('#command').setFieldLabel('Class Name');
            modifyTaskPanel.down('#run_dir').hide();
            modifyTaskPanel.setHeight(500);
            modifyTaskPanel.down('#command').setHeight(255);
        }
        else {
            modifyTaskPanel.setHeight(560);
            modifyTaskPanel.down('#tracingPanel').setHeight(260);
            modifyTaskPanel.down('#command').setHeight(315);
        }
        
        modifyTaskPanel.setTitle('Modify Task: ' + taskTitle);
        modifyTaskPanel.down('#task_id').setValue(selected[0].data.task_id);
        modifyTaskPanel.down('#task_id').disable();
        modifyTaskPanel.down('#log_file').setValue(selected[0].data.log_file);
        modifyTaskPanel.down('#name').setValue(selected[0].data.name);
        modifyTaskPanel.down('#action').setValue('edit');
        
        this.populate(modifyTaskPanel, selected[0].data);
        
        modifyTaskPanel.show();
    },
    
    // Handler to copy selected Task
    copyTask: function() {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single task to copy and try again.');
            return;
        }  
        
        var taskTitle = selected[0].data.name;
    
        var copyTaskPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            itemId: 'copyTaskPanel',
            modal: true,
            floating: true,
            closable: true,
            width: 800,
            height: 625,
            items: [ 
                Ext.create('RP.Moca.Console.Util.Maintenance', {
                    mode: 'copy'
                }),
                Ext.create('RP.Moca.Console.Util.TasksInformation', {
                    task_id: selected[0].data.task_id
                })
            ],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Save', handler: this.saveTask, scope: copyTaskPanel },
                { xtype: 'button', text: 'Cancel', handler: function() {copyTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
        
        if (Ext.getCmp('threadTasksButton').pressed) {
            copyTaskPanel.down('#command').setFieldLabel('Class Name');
            copyTaskPanel.down('#run_dir').hide();
            copyTaskPanel.setHeight(500);
            copyTaskPanel.down('#command').setHeight(255);
        }
        else {
            copyTaskPanel.setHeight(560);
            copyTaskPanel.down('#tracingPanel').setHeight(260);
            copyTaskPanel.down('#command').setHeight(315);
        }
        
        copyTaskPanel.setTitle('Copy Task: ' + taskTitle);
        copyTaskPanel.down('#task_id').setValue('COPY_' + selected[0].data.task_id);
        copyTaskPanel.down('#name').setValue('Copy of ' + selected[0].data.name);
        var logFile = selected[0].data.log_file;
        if (Ext.isEmpty(logFile)) {
            copyTaskPanel.down('#log_file').setValue('');
        }
        else {
            copyTaskPanel.down('#log_file').setValue(logFile + '.copy');
        }
        copyTaskPanel.down('#action').setValue('add');
        
        this.populate(copyTaskPanel, selected[0].data);
    
        copyTaskPanel.show();
    },
    
    // Handler to delete selected Task
    deleteTask: function() {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0 || selected.length > 1) {
            Ext.Msg.alert('Console', 'Please select a single task to delete and try again.');
            return;
        }
    
        var deleteTaskPanel = Ext.create('Ext.panel.Panel', {
            id: 'deleteTaskPanel',
            layout: 'fit',
            modal: true,
            floating: true,
            closable: true,
            width: 450,
            title: 'Delete Task',
            items: [{
                xtype: 'label',
                text: selected[0].data.name,
                style: 'font-weight:bold;font-size:12px;',
                margin: 30
            },{
                xtype: 'hidden',
                text: selected[0].data.task_id,
                itemId: 'task_id'
            }],
            bbar: [
                { xtype: 'tbfill'},
                { xtype: 'button', text: 'Delete', handler: this.removeTask },
                { xtype: 'button', text: 'Cancel', handler: function() {deleteTaskPanel.close();} },
                { xtype: 'tbfill'}
            ]
        });
    
        deleteTaskPanel.show();
    },
    
    // Function to populate the copy and modify task popups.
    populate: function(panel, selected) {
    
        var role_id = Ext.getCmp('threadTasks').getStore().collect('role_id',false);
        Ext.each(Ext.getCmp('processTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(Ext.getCmp('daemonTasks').getStore().collect('role_id',false), function(key, value) {
            role_id.push(key);
        });
        Ext.each(role_id, function(key, value) {
             panel.down('#role_id').store.add({field1:key});
        });
        
        panel.down('#job_id').hide();
        panel.down('#timer').hide();
        panel.down('#overlap').hide();
        panel.down('#enabled').hide();
        panel.down('#spacer1').hide();
        panel.down('#command').setValue(selected.cmd_line);
        panel.down('#role_id').setValue(selected.role_id);
        panel.down('#grp_nam').setValue(selected.grp_nam);
        panel.down('#task_typ').setValue(selected.task_typ);
        panel.down('#start_delay').setValue(selected.start_delay);
        panel.down('#run_dir').setValue(selected.run_dir);
        
        panel.down('#auto_start').setValue(selected.auto_start);
        panel.down('#restart').setValue(selected.restart);
        panel.down('#trace_level').setValue(selected.trace_level);
    },
    
    // Handler function to save a task
    saveTask: function() {
        var maintenance = Ext.getCmp('maintenance'),
            mode = maintenance.mode,
            task_id = maintenance.down('#task_id').value,
            name = maintenance.down('#name').value,
            cmd_line = maintenance.down('#command').value,
            task_typ = maintenance.down('#task_typ').value,
            run_dir = Ext.getCmp('tracingPanel').down('#run_dir').value,
            taskStore = Ext.getCmp('threadTasks').getStore(),
            taskEnvStore = Ext.getCmp('taskEnvPanel').taskEnvironmentStore;
        
        if (task_id === '') {
            RP.Moca.util.Msg.alert('Console', 'A Task ID is required.');
            return;
        }
        if (name === '') {
            RP.Moca.util.Msg.alert('Console', 'A name is required.');
            return;
        }
        if (cmd_line === '') {
            if (task_typ === 'T') {
                RP.Moca.util.Msg.alert('Console', 'A class name is required.');
                return;
            }
            else {
                RP.Moca.util.Msg.alert('Console', 'A command is required.');
                return;
            }
        }
        if (task_typ === 'P' && Ext.isEmpty(run_dir)) {
            RP.Moca.util.Msg.alert('Console', 'A starting directory is required.');
            return;
        }
        
        if (mode === undefined) {
            RP.Moca.util.Msg.alert('Console', 'Error: maintenance panel mode undefined.');
            return;
        }
        
        if (taskStore.find('task_id', task_id) !== -1 && mode !== 'edit') {
            RP.Moca.util.Msg.alert('Console', 'Cannot create duplicate task.');
            return;
        }
        
        var panel = this.up().up();
        
        var chkGroup = Ext.getCmp("traceCheckBoxGroup");
        
        var traceLevel = '';
        if (Ext.getCmp('tracingPanel').down('#trace_level').getValue()) {    
            traceLevel = '*';
        }
        
        var grp_nam = maintenance.down('#grp_nam').value;
        var role_id = maintenance.down('#role_id').lastValue;
        var action = maintenance.down('#action').value;
        var start_delay = Ext.getCmp('timerPanel').down('#start_delay').value;
        var log_file = Ext.getCmp('tracingPanel').down('#log_file').value;
        var auto_start = Ext.getCmp('behaviorPanel').down('#auto_start').value;
        var restart = Ext.getCmp('behaviorPanel').down('#restart').value;
        
        // Sync Task Environment variables
        var modifiedRecords = taskEnvStore.getModifiedRecords();
        var newRecords = taskEnvStore.getNewRecords();
        var removedRecords = taskEnvStore.getRemovedRecords();
        
        var ifBreak = false;
        taskEnvStore.each(function(record) {
            if (Ext.isEmpty(record.get('var_nam')) || Ext.isEmpty(record.get('value'))) {
                Ext.Msg.show({
                    title: 'Warning',
                    width: 400,
                    msg: 'Invalid environment variable. Name and Value are required.',
                    buttons: Ext.MessageBox.OK,
                    icon: Ext.MessageBox.WARNING
                }); 
                ifBreak = true;
            }
        });
        
        // We don't want to post anything if there is an invalid variable
        if (ifBreak) {
            return;
        }
        
        // Sync created
        Ext.each(newRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    task_id: task_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveTaskEnvironment',
                    action: 'add'
                }
            });
        }, this);
        
        // Sync modified
        Ext.each(modifiedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    task_id: task_id,
                    name: record.data.var_nam,
                    value: record.data.value,
                    m: 'saveTaskEnvironment',
                    action: 'edit'
                }/*,
                success : function(response){
                    jobStore.commitChanges();
                },
                failure : function(response){
                    jobStore.rejectChanges();
                }*/
            });
        }, this);
               
        // Sync removed
        Ext.each(removedRecords, function(record) {
            RP.Moca.util.Ajax.request({
                url : '/console',
                params : {
                    task_id: task_id,
                    name: record.data.var_nam,
                    m: 'removeTaskEnvironment'
                }
                /*success : function(response){
                    jobStore.commitChanges();
                },
                failure : function(response){
                    jobStore.rejectChanges();
                }*/
            });
        }, this);
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                task_id: task_id,
                name: name,
                grp_nam: grp_nam,
                role_id: role_id,
                cmd_line: cmd_line,
                task_typ: task_typ,
                start_delay: start_delay,
                run_dir: run_dir,
                log_file: log_file,
                auto_start: auto_start,
                restart: restart,
                trace_level: traceLevel,
                action: action,
                m: 'saveTask'
            },
            success: function(response){
                var json = Ext.decode(response.responseText);
                this._status = parseInt(json.status, 10);
                this._message = json.message;
                
                if (this._status !== 0 && this._status !== null) {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: this._message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                }
                else {
                    Ext.getCmp('tasksContainer').refresh();
                    panel.close();
                }
            }
        });
    },
    
    // Handler function to delete a task
    removeTask: function() {
        var panel = Ext.getCmp('deleteTaskPanel');

        var task_id = panel.down('#task_id').text.trim();
        
        RP.Moca.util.Ajax.request({
            url: '/console',
            method: 'POST',
            params: {
                task_id: task_id,
                m: 'removeTask'
            },
            success: function(response){
                var json = Ext.decode(response.responseText);
                this._status = parseInt(json.status, 10);
                this._message = json.message;
                
                if (this._status !== 0 && this._status !== null) {
                    Ext.Msg.show({
                        title: 'Console',
                        msg: this._message,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.ERROR
                    });
                }
                else {
                    Ext.getCmp('tasksContainer').refresh();
                    panel.close();
                }
            }
        });
    },
    
    // Handler function for the start task button
    startTaskHandler: function () {
        var selected;
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            //RP.Moca.util.Msg.alert('Console', 'Please select a task to start and try again.');
            Ext.Msg.alert('Console', 'Please select a task to start and try again.');
            return;
        }

        // This list will contain all the tasks that we need to work with.
        var task_id_list = '';
        
        // Build the list of tasks to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the task id and running flag for this task.
            var task_id = s.get('task_id'); // flag - maybe problem?
            var running = s.get('running');
            
            if (running === false) {
                if (task_id_list === '') {
                    task_id_list = task_id;
                }
                else {
                    task_id_list = task_id_list + ',' + task_id;
                }
            }
        }

        var msgAlreadyRunning;
        var msgStartingTask;
        var msgErrorStartingTask;

        if (selected.length == 1) {
            msgAlreadyRunning = 'The selected task is already running.';
            msgStartingTask = 'Starting task...';
            msgErrorStartingTask = 'An error occurred attempting to start the selected task.';
        }
        else {
            msgAlreadyRunning = 'The selected tasks are already running.';
            msgStartingTask = 'Starting tasks...';
            msgErrorStartingTask = 'An error occurred attempting to start the selected tasks.';   
        }
        
        // Make sure we have tasks that aren't already running to start.
        if (task_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgAlreadyRunning);
            return;
        }

        var myParams = {
            task_id_list: task_id_list,
            m: 'startTask'
        };
                 
        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgStartingTask + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorStartingTask + '\n\n' + message);
                        }
                        // Refresh the grid panel after we've given the task enough time to start.
                        Ext.getCmp('tasksContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.MessageBox.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send start task request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    },

    // Handler function for the stop task button
    stopTaskHandler: function () {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            RP.Moca.util.Msg.alert('Console', 'Please select a task to stop and try again.');
            return;
        }

        // This list will contain all the tasks that we need to work with.
        var task_id_list = '';
        
        // Build the list of tasks to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the task id and running flag for this task.
            var task_id = s.get('task_id');
            var running = s.get('running');
            
            if (running === true) {
                if (task_id_list === '') {
                    task_id_list = task_id;
                }
                else {
                    task_id_list = task_id_list + ',' + task_id;
                }
            }
        }

        var msgNotRunning;
        var msgStoppingTask;
        var msgErrorStoppingTask;
            
        if (selected.length == 1) {
            msgNotRunning = 'The selected task is not running.';
            msgStoppingTask = 'Stopping task...';
            msgErrorStoppingTask = 'An error occurred attempting to stop the selected task.';
        }
        else {
            msgNotRunning = 'The selected tasks are not running.';
            msgStoppingTask = 'Stopping tasks...';
            msgErrorStoppingTask = 'An error occurred attempting to stop the selected tasks.';   
        }
        
        // Make sure we have tasks that are actually running to stop.
        if (task_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgNotRunning);
            return;
        }

        var myParams = {
            task_id_list: task_id_list,
            m: 'stopTask'
        }; 

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgStoppingTask + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorStoppingTask + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to stop.
                        Ext.getCmp('tasksContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.Msg.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };


        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: myParams,
            scope: this,
            success: myResultHandler,
            failureAlert: {
                title: 'Console',
                msg: 'Could not send stop task request to the server.'
            }
        });
    },

    // Handler function for the restart task button
    restartTaskHandler: function () {
        if (Ext.getCmp('threadTasksButton').pressed) {
            selected = Ext.getCmp('threadTasks').selModel.selected.items;
        }
        if (Ext.getCmp('processTasksButton').pressed) {
            selected = Ext.getCmp('processTasks').selModel.selected.items;
        }
        if (Ext.getCmp('daemonTasksButton').pressed) {
            selected = Ext.getCmp('daemonTasks').selModel.selected.items;
        }

        // Make sure a row was selected first.
        if (!selected || selected.length === 0) {
            RP.Moca.util.Msg.alert('Console', 'Please select a task to restart and try again.');
            return;
        }

        // This list will contain all the tasks that we need to work with.
        var task_id_list = '';
        
        // Build the list of tasks to work with.
        for (var i = 0, len = selected.length; i < len; i++) {
            var s = selected[i];

            // Get the task id and running flag for this task.
            var task_id = s.get('task_id');
            var running = s.get('running');
            
            if (running === true) {
                if (task_id_list === '') {
                    task_id_list = task_id;
                }
                else {
                    task_id_list = task_id_list + ',' + task_id;
                }
            }
        }

        var msgNotRunning;
        var msgRestartingTask;
        var msgErrorRestartingTask;
            
        if (selected.length == 1) {
            msgNotRunning = 'The selected task is not running.';
            msgRestartingTask = 'Restarting task...';
            msgErrorRestartingTask = 'An error occurred attempting to restart the selected task.';
        }
        else {
            msgNotRunning = 'The selected tasks are not running.';
            msgStoppingTask = 'Restarting tasks...';
            msgErrorStoppingTask = 'An error occurred attempting to restart the selected tasks.';   
        }
        
        // Make sure we have tasks that are actually running to restart.
        if (task_id_list === '') {
            RP.Moca.util.Msg.alert('Console', msgNotRunning);
            return;
        }

        var myParams = {
            task_id_list: task_id_list,
            m: 'restartTask'
        }; 

        var myResultHandler = function (result) {

            Ext.MessageBox.show({
                title: 'Console',
                msg: '<br><br>' + msgRestartingTask + '<br><br>',
                width: 300,
                progress: true,
                closable: false
            });

            // This hideous block does the progress update.
            var f = function (v) {
                return function () {
                    if (v == 10) {
                        Ext.MessageBox.hide();

                        var status = Ext.decode(result.responseText).status;
                        var message = Ext.decode(result.responseText).message;

                        if (status !== 0) {
                            RP.Moca.util.Msg.alert('Console', msgErrorRestartingTask + '\n\n' + message);
                        }

                        // Refresh the grid panel after we've given the task enough time to stop.
                        Ext.getCmp('tasksContainer').refresh();
                    }
                    else {
                        var i = v / 10;
                        Ext.Msg.updateProgress(i, Math.round(100 * i) + '% completed');
                    }
                };
            };

            for (var i = 1; i <= 10; i++) {
                setTimeout(f(i), i * 100);
            }
        };

        var myFailureAlert = {
            title: 'Console',
            msg: 'Could not send restart task request to the server.'
        };

        // Make an Ajax call to the server.       
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            //params: myParams,
            params: {
                task_id_list: task_id_list,
                m: 'restartTask'
            },
            scope: this,
            success: myResultHandler,
            failureAlert: myFailureAlert
        });
    }
});

// Thread Tasks
Ext.define('RP.Moca.Console.Tasks.Thread', {
    extend: 'Ext.grid.Panel',
    
    initComponent: function() {
    
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
    
        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook in our local store to load when the master store loads.
        this.masterStore = Ext.getStore('masterTaskStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Running',
            dataIndex: 'running',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            minWidth: 150
        }, {
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            flex: 2,
            minWidth: 150
        }, {
            header: 'Class Name',
            dataIndex: 'cmd_line',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Start In',
            dataIndex: 'run_dir',
            hidden: true,
            flex: 1,
            minWidth: 150
        }, {
            header: 'Tracing',
            dataIndex: 'trace_level',
            hidden: true,
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Auto Start',
            dataIndex: 'auto_start',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Restart',
            dataIndex: 'restart',
            flex: 1,
            minWidth: 100
        }];

        // Apply some stuff
        Ext.apply(this, {
            id: 'threadTasks',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No thread based tasks currently exist.',
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // Refresh the Master Store which will
        // initiate a refresh to the local store.
        this.masterStore.load();
    },

    onMasterStoreLoad: function() {
        // Refresh the Thread Based Task Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('T');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
    
        // Refresh the Button Text.
        var button = Ext.getCmp('threadTasksButton');
        if (button.rendered) {
            button.setText('Thread (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Thread (' + this.store.getCount() +')');
            }, this);
        }
    }
});

// Process Tasks
Ext.define('RP.Moca.Console.Tasks.Process', {
    extend: 'Ext.grid.Panel',
    
    initComponent: function() {
    
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
    
        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook in our local store to load when the master store loads.
        this.masterStore = Ext.getStore('masterTaskStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Running',
            dataIndex: 'running',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Task ID',
            dataIndex: 'task_id',
            hidden: true,
            minWidth: 125
        }, {
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            minWidth: 150
        }, {
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 175
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'cmd_line',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Start In',
            dataIndex: 'run_dir',
            flex: 1,
            minWidth: 125
        }, {
            xtype: 'checkcolumn',
            header: 'Auto Start',
            dataIndex: 'auto_start',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Restart',
            dataIndex: 'restart',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Start Delay',
            dataIndex: 'start_delay',
            hidden: true,
            align: 'right',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Log File',
            dataIndex: 'log_file',
            hidden: true,
            flex: 1,
            minWidth: 150
        }, {
            header: 'Tracing',
            dataIndex: 'trace_level',
            hidden: true,
            flex: 1,
            minWidth: 100
        }];

        // Apply some stuff
        Ext.apply(this, {
            id: 'processTasks',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No process based tasks currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        // We want to reload the Master Store which will
        // initiate a refresh of the local store.
        this.masterStore.load();
    },

    onMasterStoreLoad: function() {
        // Refresh the Process Based Task Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('P');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
    
        // Refresh the Button Text.
        var button = Ext.getCmp('processTasksButton');
        if (button.rendered) {
            button.setText('Process (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Process (' + this.store.getCount() +')');
            }, this);
        }
    }
});

// Daemon Tasks
Ext.define('RP.Moca.Console.Tasks.Daemon', {
    extend: 'Ext.grid.Panel',
    
    initComponent: function() {
        
        var hideNodes = false;
        if (Ext.getStore('clusterComboBox').getCount() === 0) {
            hideNodes = true;
        }
    
        this.store = Ext.create("RP.Moca.util.Store", {
            model: 'TaskModel',
            groupers: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }],
            sorters: [{
                property: 'task_typ',
                direction: 'ASC',
                root: 'data'
            }]
        });
        
        // Hook in our local store to load when the master store loads.
        this.masterStore = Ext.getStore('masterTaskStore');
        this.masterStore.on('load', this.onMasterStoreLoad, this);

        var myColumns = [{
            xtype: 'checkcolumn',
            header: 'Running',
            dataIndex: 'running',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Task ID',
            dataIndex: 'task_id',
            hidden: true,
            minWidth: 125
        }, {
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            minWidth: 125
        }, {
            header: 'Nodes',
            dataIndex: 'nodes',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Role',
            dataIndex: 'role_id',
            hidden: hideNodes,
            flex: 2,
            minWidth: 150
        }, {
            header: 'Command',
            dataIndex: 'cmd_line',
            flex: 3,
            minWidth: 200
        }, {
            header: 'Start In',
            dataIndex: 'run_dir',
            flex: 1,
            minWidth: 125
        }, {
            xtype: 'checkcolumn',
            header: 'Auto Start',
            dataIndex: 'auto_start',
            flex: 1,
            minWidth: 100
        }, {
            xtype: 'checkcolumn',
            header: 'Restart',
            dataIndex: 'restart',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Start Delay',
            dataIndex: 'start_delay',
            hidden: true,
            align: 'right',
            flex: 1,
            minWidth: 100
        }, {
            header: 'Log File',
            dataIndex: 'log_file',
            hidden: true,
            flex: 1,
            minWidth: 150
        }, {
            header: 'Tracing',
            dataIndex: 'trace_level',
            hidden: true,
            flex: 1,
            minWidth: 100
        }];

        // Apply some stuff
        Ext.apply(this, {
            id: 'daemonTasks',
            layout: 'fit',
            selModel: {
                mode: "multi"
            },
            columns: myColumns,
            viewConfig: {
                emptyText: 'No daemon based tasks currently exist.',
                deferEmptyText: false,
                preserveScrollOnRefresh: true
            }
        });

        this.callParent(arguments);
    },
    
    refresh: function () {
        this.masterStore.load();
    },

    onMasterStoreLoad: function() {
        // Refresh the Daemon Based Task Store.
        this.store.removeAll();
        var records = this.masterStore.getGroups('D');
        if (!Ext.isEmpty(records)) {
            var clones = [];
            Ext.each(records.children, function(record) {
                clones.push(record.copy());
            }, this);
            this.store.add(clones);
        }
        
        // Refresh the Button Text.
        var button = Ext.getCmp('daemonTasksButton');
        if (button.rendered) {
            button.setText('Daemon (' + this.store.getCount() +')');
        }
        else {
            button.on('afterrender', function() {
                button.setText('Daemon (' + this.store.getCount() +')');
            }, this);
        }
    }
});
//////////////////////
// ..c\javascript\console\config\Widgets.js
//////////////////////
// Sessions
Ext.define("RP.Moca.SessionsWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Sessions';
        this.uiTitle = this.headerText;
        this.itemId = "sessionsWidget";

        this.callParent(arguments);
    },

    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Sessions.SessionsViewer({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.sessions.xtype",
    classRef: RP.Moca.SessionsWidget,
    paramArray: []
});
 
// Native Processes
Ext.define("RP.Moca.NativeProcessesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Native Processes';
        this.uiTitle = this.headerText;
        this.itemId = "nativeprocessesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.NativeProcesses.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.nativeprocs.xtype",
    classRef: RP.Moca.NativeProcessesWidget,
    paramArray: []
});
 
// Database Connections
Ext.define("RP.Moca.DatabaseConnectionsWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Database Connections';
        this.uiTitle = this.headerText;
        this.itemId = "dbconnsWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.DatabaseConnections.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.dbconns.xtype",
    classRef: RP.Moca.DatabaseConnectionsWidget,
    paramArray: []
});
 
// Jobs
Ext.define("RP.Moca.JobsWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Jobs';
        this.uiTitle = this.headerText;
        this.itemId = "jobsWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Jobs.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.jobs.xtype",
    classRef: RP.Moca.JobsWidget,
    paramArray: []
});
 
// Tasks
Ext.define("RP.Moca.TasksWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Tasks';
        this.uiTitle = this.headerText;
        this.itemId = "tasksWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Tasks.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.tasks.xtype",
    classRef: RP.Moca.TasksWidget,
    paramArray: []
});

// Async Executor
Ext.define("RP.Moca.AsyncExecutorWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget", 
 
    initComponent: function() {
        this.headerText = 'Asynchronous Executor';
        this.uiTitle = this.headerText;
        this.itemId = "asyncexecutorWidget";
       
        RP.Moca.AsyncExecutorWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.AsyncExecutor.View({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.asyncexecutor.xtype",
    classRef: RP.Moca.AsyncExecutorWidget,
    paramArray: []
});

// Cluster Async Executor
Ext.define("RP.Moca.ClusterAsyncExecutorWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget", 
 
    initComponent: function() {
        this.headerText = 'Cluster Async Executor';
        this.uiTitle = this.headerText;
        this.itemId = "clusterasyncexecutorWidget";
       
        RP.Moca.ClusterAsyncExecutorWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.ClusterAsyncExecutor.View({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.clusterasyncexecutor.xtype",
    classRef: RP.Moca.ClusterAsyncExecutorWidget,
    paramArray: []
});

// Registry
Ext.define("RP.Moca.RegistryWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Registry';
        this.uiTitle = this.headerText;
        this.itemId = "registryWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Registry.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.registry.xtype",
    classRef: RP.Moca.RegistryWidget,
    paramArray: []
});
 
// Component Libraries
Ext.define("RP.Moca.ComponentLibrariesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Component Libraries';
        this.uiTitle = this.headerText;
        this.itemId = "componentLibrariesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.ComponentLibraries.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.complibs.xtype",
    classRef: RP.Moca.ComponentLibrariesWidget,
    paramArray: []
});
 
// Environment Variables
Ext.define("RP.Moca.EnvironmentVariablesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Environment Variables';
        this.uiTitle = this.headerText;
        this.itemId = "environmentVariablesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.EnvironmentVariables.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.envvars.xtype",
    classRef: RP.Moca.EnvironmentVariablesWidget,
    paramArray: []
});
 
// System Properties
Ext.define("RP.Moca.SystemPropertiesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'System Properties';
        this.uiTitle = this.headerText;
        this.itemId = "systemPropertiesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.SystemProperties.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.sysprops.xtype",
    classRef: RP.Moca.SystemPropertiesWidget,
    paramArray: []
});
 
// Resource Usage
Ext.define("RP.Moca.ResourceUsageWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Resource Usage';
        this.uiTitle = this.headerText;
        this.itemId = "resourceUsageWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.ResourceUsage.View({});
    }
});

RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.resource.xtype",
    classRef: RP.Moca.ResourceUsageWidget,
    paramArray: []
});
 
// Session Keys
Ext.define("RP.Moca.SessionKeysWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Connected Users';
        this.uiTitle = this.headerText;
        this.itemId = "sessionKeysWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.SessionKeys.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.sessionkeys.xtype",
    classRef: RP.Moca.SessionKeysWidget,
    paramArray: []
});
 
// Command Profile
Ext.define("RP.Moca.CommandProfileWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Command Profile';
        this.uiTitle = this.headerText;
        this.itemId = "commandProfileWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.CommandProfile.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.cmdprofile.xtype",
    classRef: RP.Moca.CommandProfileWidget,
    paramArray: []
});
 
// Log Files
Ext.define("RP.Moca.LogFilesWidget", {
	extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Log Files';
        this.uiTitle = this.headerText;
        this.itemId = "logFilesWidget";
       
        this.callParent(arguments);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.LogFiles.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.logfiles.xtype",
    classRef: RP.Moca.LogFilesWidget,
    paramArray: []
});

// Clustering
Ext.define("RP.Moca.ClusteringWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Clustering';
        this.uiTitle = this.headerText;
        this.itemId = "clusteringWidget";
       
        RP.Moca.ClusteringWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.Clustering.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.clustering.xtype",
    classRef: RP.Moca.ClusteringWidget,
    paramArray: []
});

// Job History
Ext.define("RP.Moca.JobHistoryWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Job Execution History';
        this.uiTitle = this.headerText;
        this.itemId = "jobHistoryWidget";
       
        RP.Moca.JobHistoryWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.JobHistory.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.jobhistory.xtype",
    classRef: RP.Moca.JobHistoryWidget,
    paramArray: []
});

// Task History
Ext.define("RP.Moca.TaskHistoryWidget", {
    extend: "RP.taskflow.BaseTaskflowWidget",
 
    initComponent: function() {
        this.headerText = 'Task Execution History';
        this.uiTitle = this.headerText;
        this.itemId = "taskHistoryWidget";
       
        RP.Moca.TaskHistoryWidget.superclass.initComponent.call(this);
    },
 
    createTaskForm: function() {
        this.taskForm = new RP.Moca.Console.TaskHistory.View({});
    }
});
 
RP.registerWidget({
    appId: "moca.console.app",
    xtype: "moca.console.taskhistory.xtype",
    classRef: RP.Moca.TaskHistoryWidget,
    paramArray: []
});

//////////////////////
// ..c\javascript\console\config\Taskflows.js
//////////////////////
RP.registerTaskflow({  
    name: "moca.console.taskflow",
    title: "Menu",
    tasks:[{
        appId: "moca.console.app",
        id: "sessions",
        taskId: "sessions"
    }, {
        appId: "moca.console.app",
        id: "sessionKeys",
        taskId: "sessionKeys"
    }, {
        appId: "moca.console.app",
        id: "nativeProcesses",
        taskId: "nativeProcesses"
    }, {
        appId: "moca.console.app",
        id: "databaseConnections",
        taskId: "databaseConnections"
    }, {
        appId: "moca.console.app",
        id: "jobs",
        taskId: "jobs"
    }, {
        appId: "moca.console.app",
        id: "tasks",
        taskId: "tasks"
    }, {
        appId: "moca.console.app",
        id: "jobHistory",
        taskId: "jobHistory",
        widgetCfg: {
            id: "jobHistoryWidget"
        }
    }, {
        appId: "moca.console.app",
        id: "taskHistory",
        taskId: "taskHistory",
        widgetCfg: {
            id: "taskHistoryWidget"
        }
    }, {
        appId: "moca.console.app",
        id: "asyncExecutor",
        taskId: "asyncExecutor"
    }, {
        appId: "moca.console.app",
        id: "clusterAsyncExecutor",
        taskId: "clusterAsyncExecutor",
         // This is defined so we can later extract the id using 
        // Ext.getComponent to hide this widget if clustering is not
        // currently configured
        widgetCfg: {
            id: "clusteringAsyncWidget"
        }
    }, {
        appId: "moca.console.app",
        id: "registry",
        taskId: "registry"
    }, {
        appId: "moca.console.app",
        id: "componentLibraries",
        taskId: "componentLibraries"
    }, {
        appId: "moca.console.app",
        id: "environmentVariables",
        taskId: "environmentVariables"
    }, {
        appId: "moca.console.app",
        id: "systemProperties",
        taskId: "systemProperties"
    }, {
        appId: "moca.console.app",
        id: "resourceUsage",
        taskId: "resourceUsage"
    }, {
        appId: "moca.console.app",
        id: "commandProfile",
        taskId: "commandProfile"
    }, {
        appId: "moca.console.app",
        id: "logFiles",
        taskId: "logFiles"
    }, {
        appId: "moca.console.app",
        id: "clustering",
        taskId: "clustering",
        // This is defined so we can later extract the id using 
        // Ext.getComponent to hide this widget if clustering is not
        // currently configured
        widgetCfg: {
            id: "clusteringWidget"
        }
    }],
    listeners: {},
    uiConfig: {}
});
 
RP.registerTask([{
    appId: "moca.console.app",
    taskId: "sessions",
    widgetXtype: "moca.console.sessions.xtype"
}, {
    appId: "moca.console.app",
    taskId: "databaseConnections",
    widgetXtype: "moca.console.dbconns.xtype"
}, {
    appId: "moca.console.app",
    taskId: "jobs",
    widgetXtype: "moca.console.jobs.xtype"
}, {
    appId: "moca.console.app",
    taskId: "tasks",
    widgetXtype: "moca.console.tasks.xtype"
}, {
    appId: "moca.console.app",
    taskId: "asyncExecutor",
    widgetXtype: "moca.console.asyncexecutor.xtype"
}, {
    appId: "moca.console.app",
    taskId: "clusterAsyncExecutor",
    widgetXtype: "moca.console.clusterasyncexecutor.xtype"
}, {
    appId: "moca.console.app",
    taskId: "registry",
    widgetXtype: "moca.console.registry.xtype"
}, {
    appId: "moca.console.app",
    taskId: "componentLibraries",
    widgetXtype: "moca.console.complibs.xtype"
}, {
    appId: "moca.console.app",
    taskId: "environmentVariables",
    widgetXtype: "moca.console.envvars.xtype"
}, {
    appId: "moca.console.app",
    taskId: "systemProperties",
    widgetXtype: "moca.console.sysprops.xtype"
}, {
    appId: "moca.console.app",
    taskId: "resourceUsage",
    widgetXtype: "moca.console.resource.xtype"
}, {
    appId: "moca.console.app",
    taskId: "sessionKeys",
    widgetXtype: "moca.console.sessionkeys.xtype"
}, {
    appId: "moca.console.app",
    taskId: "commandProfile",
    widgetXtype: "moca.console.cmdprofile.xtype"
}, {
    appId: "moca.console.app",
    taskId: "nativeProcesses",
    widgetXtype: "moca.console.nativeprocs.xtype"
}, {
    appId: "moca.console.app",
    taskId: "logFiles",
    widgetXtype: "moca.console.logfiles.xtype"
}, {
    appId: "moca.console.app",
    taskId: "clustering",
    widgetXtype: "moca.console.clustering.xtype"
}, {
    appId: "moca.console.app",
    taskId: "jobHistory",
    widgetXtype: "moca.console.jobhistory.xtype"
}, {
    appId: "moca.console.app",
    taskId: "taskHistory",
    widgetXtype: "moca.console.taskhistory.xtype"
}]);

//////////////////////
// ..c\javascript\console\config\Modules.js
//////////////////////
RP.upb.modules = [];
RP.upb.modules.push("console.do");

RP.registerModule({
    name: "console.do",
    title: "Console",
    label: "Console",
    taskflows: [
        "moca.console.taskflow"
    ]
});
//////////////////////
// ..c\javascript\console\Console.js
//////////////////////
/*
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/javascript/console/Console.js $
 *  $Author: wburns $
 *  $Date: 2012-04-03 14:07:13 -0500 (Tue, 03 Apr 2012) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

Ext.namespace("RP.globals");
Ext.namespace("RP.globals.paths");

/* We blank out the fix ajax response so it doesn't mess with JSON */
RP.core.fixAjaxResponses = function(){};

RP.upb.PageBootstrapper.bootstrap({ 
	"pathInfo": "",
	"queryString": "",
	"showLogout": true,
	"currentModule" : "console.do" 
});

// This along with css defines allow for grid rows to be selectable
if(typeof Ext != 'undefined') {
  Ext.core.Element.prototype.unselectable = function() {
      return this;
  };
  Ext.view.TableChunker.metaRowTpl = [
   '<tr class="' + Ext.baseCSSPrefix + 'grid-row {addlSelector} {[this.embedRowCls()]}" {[this.embedRowAttr()]}>',
    '<tpl for="columns">',
     '<td class="{cls} ' + Ext.baseCSSPrefix + 'grid-cell ' + Ext.baseCSSPrefix + 'grid-cell-{columnId} {{id}-modified} {{id}-tdCls} {[this.firstOrLastCls(xindex, xcount)]}" {{id}-tdAttr}><div class="' + Ext.baseCSSPrefix + 'grid-cell-inner ' + Ext.baseCSSPrefix + 'unselectable" style="{{id}-style}; text-align: {align};">{{id}}</div></td>',
    '</tpl>',
   '</tr>'
  ];
}

Ext.getDoc().on("keydown", function(e) {
    if (e.ctrlKey && e.shiftKey) {
        if (e.getKey() === e.F10) {
            Ext.MessageBox.confirm("Support", "<br>Are you sure you want to create a support file for download?",
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

            e.stopEvent();
        }
    }
});
//////////////////////
// ..c\javascript\console\TaskflowFrame.js
//////////////////////
Ext.define("RP.MOCA.overide.taskflow.prototype.TaskflowFrame", {
    override: "RP.taskflow.prototype.TaskflowFrame",
    taskflowsContainerHeight: 0,
    
    /**
     * @override
     * @private
     */
    initComponent: function() {
        Ext.QuickTips.init();
        
        // Create the store containing the list of cluster nodes.
        var me = this,
            myStore = new RP.util.NodeComboBoxStore();
        
        this.mocaServerButton = me._createServerButton(myStore);
        myStore.load();
        
        Ext.apply(me, {
            id: '_mocaTaskflowFrame'
        });
        
        me.callParent(arguments);
    },
    
    /**
     * @private
     * @override
     * Creates the top taskflow container which contains
     * the help button and modules dropdown.
     */
    _createNavigationBar: function() {
        // We still need to create the help container,
        // so that _extraTaskflowsMenu is created
        this._helpContainer = this._createHelpContainer();
        this._rpTaskflowsContainer.flex = 1;
        
        var items,
            isEastNav = (RP.globals.NAV_REGION === 'east');
        
        // We want to get rid of the taskflow tabs and make the left over space blend in.
        this._rpTaskflowsContainer.setVisible(false);
        this._rpTaskflowFiller = Ext.create('Ext.Component', {flex: 1, cls: this.modulesBtnCls});
        
        if(isEastNav) {
            items = [this.mocaServerButton, this._rpTaskflowsContainer, this._rpTaskflowFiller,
                this._createDividerComponent(), this._createModulesContainer()];
        }
        else {
            items = [this._createModulesContainer(), this._createDividerComponent(),
                this._rpTaskflowsContainer, this._rpTaskflowFiller, this.mocaServerButton];
        }
        
        Ext.Array.forEach(items, function(item) {
            item.addCls('rp-taskflow-container-item');
        });
        
        return Ext.create('Ext.container.Container', {
            cls: 'rp-taskflow-bar',
            itemId: 'rpTaskflowBar',
            margin: '0 0 11 0',
            height: this.taskflowBarHeight,
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: items,
            region: 'north',
            listeners: {
                afterrender: this._afterTaskflowBarRender
            }
        });
    },
    
    /**
     * @override
     * We override this method so that we don't actually make the help button
     */
    _createHelpContainer: function() {
        this._createExtraWidgetsButton();
        
        var isEastNav = RP.globals.NAV_REGION === 'east',
            items;
        
        if(isEastNav) {
            items = [{
                xtype: 'component',
                flex: 1
            }, this._extraTaskflowsButton];
        }
        else {
            items = [this._extraTaskflowsButton, {
                xtype: 'component',
                flex: 1
            }];
        }
        
        return Ext.create('Ext.container.Container', {
            width: 95,
            cls: 'rp-taskflow-container-gradient',
            // We want 15px total from the edge of the window, but each button
            // has 2px of padding built into it by default for which we have to account.
            padding: isEastNav ? '0 0 0 13px' : '0 13px 0 0',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: items
        });
    },
    
    /**
     * @private
     */
    _createServerButton: function(store) {
        return Ext.create('Ext.button.Button', {
            id:'_mocaServerButton',
            cls: 'rp-modules-btn',
            menuAlign: 't-b',
            padding: '0px 10px 0px 0px',
            menu: {
                // Create basic menu for component layout, this will be replaced later
                items: [{
                    text: 'Restart Server',
                    frame: false,
                    plain: true,
                    handler: this._restartServer
                }]
            },
            listeners: {
                beforerender: function() {
                    store.on('load', function(store,records,success,operation,opts) {
                        var myServerButton = Ext.getCmp('_mocaServerButton'),
                            serverButtonText = "Server: ",
                            menu = null,
                            text = null;
                            
                        if (store.getCount() !== 0) {
                            // Hack to call our own function due to scope issues.
                            menu = Ext.getCmp('_mocaTaskflowFrame')._createClusterServerMenu(store, false);
                            store.each(function(record) {
                                if (record.data.current === true) {
                                    serverButtonText += record.data.node;
                                }
                            });
                            text = serverButtonText;
                        }
                        else {
                            // Hack to call our own function due to scope issues.
                            menu = Ext.getCmp('_mocaTaskflowFrame')._createNormalServerMenu();
                            text = serverButtonText + window.location.hostname;
                        }
                        
                        Ext.apply(myServerButton, {
                            menu: menu
                        });
                        myServerButton.setText(text);
                        
                        // disable action buttons if read only
                        RP.Moca.util.Role.isReadOnly({
                            success: function(response) {
                                if (response.getResponseHeader("CONSOLE-ROLE") === "CONSOLE_READ") {
                                    myServerButton.menu.down('#consoleRestartServerButton').disable();
                                    var clusterButton = myServerButton.menu.down('#consoleRestartClusterButton');
                                    if (clusterButton) {
                                        clusterButton.disable();
                                    }
                                }
                            }
                        });
                    });
                }
            }
        });
    },
    
    /**
     * @private
     */
    _createNormalServerMenu: function(store) {
        return Ext.create('RP.menu.BalloonMenu', {
            items: [{
                text: 'Restart Server',
                frame: false,
                plain: true,
                itemId: 'consoleRestartServerButton',
                handler: this._restartServer
            }]
        });
    },
    
    /**
     * @private
     */
    _createClusterServerMenu: function(store, readOnly) {
        var items = [],
            node;
            
        store.each(function(record) {
            if (record.data.current === true) {
                node = record.data.node;
                
                items.push({
                    text: node,
                    plain: true,
                    style: {
                        width: '100%'
                    },
                    cls: 'rp-check'
                });
            }
            else {
                node = record.data.node;
                
                items.push({
                    text: node,
                    plain: true,
                    style: {
                        width: '100%'
                    },
                    listeners: {
                        click: {
                            fn: function() {
                                var url = record.data.url;
                                var pathname = window.location.pathname;
                                var hash = window.location.hash;
                                var redirectUrl = url + pathname + hash;
                                RP.util.Helpers.redirect(redirectUrl);
                            }
                        }
                    }
                });
            }
        }, this);
        
        if(!readOnly) {
            items.push({
                text: 'Restart Server',
                plain: true,
                itemId: 'consoleRestartServerButton',
                handler: this._restartServer
            });
            
            items.push({
                text: 'Restart Cluster',
                plain: true,
                itemId: 'consoleRestartClusterButton',
                handler: this._restartCluster
            });
        }
        
        return Ext.create('RP.menu.BalloonMenu', {
            items: items
        });
       
    },
    
    /**
     * @private
     */
    _restartServer: function() {
        // Notify
        Ext.MessageBox.wait('Restarting the server... this could take some time.<br><br>', 
            'Console', {
                width: 300
            }
         );
        
        var myResultHandler = function (result) {
            Ext.MessageBox.hide();
            
            var status = Ext.JSON.decode(result.responseText).status;
            var message = Ext.JSON.decode(result.responseText).message;
            
            if (status === 0) {
                RP.Moca.util.Msg.alert('Console', 'The server restarted.');
            }
            else {
                RP.Moca.util.Msg.alert('Console', 'An error occurred attempting to restart the server.' + message);
            }
            
            // refresh the sessions page if the user if it is open
            var sessions = Ext.getCmp('sessionList');
            if (sessions) {
                sessions.refresh();
            }
        };
        
        // Make the call     
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: {
                m: 'restartServer'
            },
            timeout: 35000,
            success: myResultHandler,
            failureAlert: {
                title: 'Console',
                msg: 'Could not send restart request to the server.'
            }
        });
    },
    
    /**
     * @private
     */
    _restartCluster: function() {
        Ext.MessageBox.wait('Restarting the cluster... this could take some time.<br><br>', 
            'Console',
            { width: 300 }
        ); 
        
        var myResultHandler = function (result) {
            Ext.MessageBox.hide();
            
            var responseText = Ext.JSON.decode(result.responseText);
            
            if (responseText.status === 0) {
                RP.Moca.util.Msg.alert('Console', 'The cluster restarted.');
            }
            else {
                RP.Moca.util.Msg.alert('Console', 'An error occurred attempting to restart the cluster.' + responseText.message);
            }
            
            // refresh the sessions page if the user if it is open
            var sessions = Ext.getCmp('sessionList');
            if (sessions) {
                sessions.refresh();
            }
        };
        
        // Make an Ajax call to the server.
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console',
            method: 'POST',
            params: {
                m: 'restartCluster'
            },
            timeout: 35000,
            success: myResultHandler,
            failureAlert: {
                title: 'Console',
                msg: 'Could not send restart cluster request to the server.'
            }
        });
    }
});
