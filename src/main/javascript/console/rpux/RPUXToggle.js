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