/*
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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
// ..\js\compat\..\..\..\stashLibBeginLoad.js
//////////////////////
RP.globals = RP.globals || {};
RP.stash.api.beginLoadLib();
//////////////////////
// ..\js\compat\core\ClassOperations.js
//////////////////////
/**
 * RP.core.ClassOperations compatibility layer.
 *
 * @class RP.core.ClassOperations
 */
RP.core.ClassOperations.extend = function(baseClass, overrides) {
    RP.util.DeprecationUtils.logStackTrace("[DEPRECATED] The use of RP.extend is deprecated. Use Ext.define instead.");
    
    var derivedClass = Ext.extend(baseClass, overrides);
    var derivedPrototype = derivedClass.prototype;
    var errorFormatStr = "The type {0} must implement the inherited abstract method {1}";
    
    // check for any missed implementations of RP.abstractFn
    for (var field in derivedPrototype) {
        if (derivedPrototype[field] === RP.abstractFn) {
            var name = derivedPrototype._name || "[Unknown Class]";
            var msg = Ext.String.format(errorFormatStr, name, field);
            
            logger.logError(msg);
            RP.throwError(msg);
        }
    }
    
    return derivedClass;
};

RP.extend = RP.core.ClassOperations.extend;
//////////////////////
// ..\js\compat\data\JSONProxy.js
//////////////////////
RP.util.DeprecationUtils.renameClass("Ext.data.AspNetAjaxHttpProxy", "Ext.data.proxy.Ajax");
RP.util.DeprecationUtils.renameClass("RP.data.JSONProxy", "Ext.data.proxy.Ajax");
//////////////////////
// ..\js\compat\taskflow\BaseTaskflowHyperlinkWidget.js
//////////////////////
// "compatability" layer for the BaseTaskflowHyperlinkWidget class that is no longer going to be supported/exist.
// the functionality that the class claimed to support (being able to right click -> open in new window) didn't
// actually work in the first place...
Ext.define("RP.taskflow.BaseTaskflowHyperlinkWidget", {
    constructor: function() {
        var msg = "RP.taskflow.BaseTaskflowHyperlinkWidget no longer exists.  Please just use the " +
                  "RP.taskflow.BaseTaskflowWidget class instead.  If there is any gap in functionality " +
                  "between the two, please write up a JIRA under RPWEB.";
        
        RP.util.DeprecationUtils.logStackTrace(msg);
        
        RP.throwError(msg);
    }
});
//////////////////////
// ..\js\compat\ui\ImageButton.js
//////////////////////
/**
 * A button with an image as its background.
 * @param {Object} config
 */
Ext.define("RP.ui.ImageButton", {
  extend: "Ext.button.Button",
    
  alias: "widget.rpimagebutton",
  constructor: function() {
        var messageShown = false;
        var deprecatedMessage = "[DEPRECATED] The class RP.ui.ImageButton has been deprecated.  It will be removed in future release.";
        
        return function() {
            if (messageShown === false) {
                RP.util.DeprecationUtils.logStackTrace(deprecatedMessage);
                messageShown = true;
            }
            
            this.callParent(arguments);
        };
  }(),
  
  /**
   * @private
   */
  initComponent: function(){
      
    var config = this.initialConfig;
    
    config.iconWidth = config.iconWidth || 110;
    config.iconHeight = config.iconHeight || 46;
    config.iconFontColor = config.iconFontColor || "white";
    config.iconFontWeight = config.iconFontWeight || "bold";
    
    // set the background icon to use when the button is disabled
    config.disabledIcon = config.disabledIcon || config.icon;
    config.disabledText = config.disabledText || config.text;
    
    // applies override styles found in rp-buttons.css
    Ext.apply(this, {
      cls: "rp-img-button",
      scale: "" // prevent Ext from sizing the button with a default size
    });    
    
    this.callParent();
    
    this.on("render", function(){
      this.setIconSize(this.initialConfig.iconWidth, this.initialConfig.iconHeight);
      this.btnEl.setStyle("color", this.initialConfig.iconFontColor);
      this.btnEl.setStyle("font-weight", this.initialConfig.iconFontWeight);
    }, this);
  },
  
  /**
   * Changes the button's image to a disabled button style.
   * @method
   */
  disable: function(){
    this.setIcon(this.initialConfig.disabledIcon);
    this.setText(this.initialConfig.disabledText);
    this.callParent();
    this.removeClass("x-item-disabled");
  },
  
  /**
   * Changes the button's image to an enabled button style.
   * @method
   */
  enable: function(){
    this.setIcon(this.initialConfig.icon);
    this.setText(this.initialConfig.text);
    this.callParent();
  },
  
  /**
   * Changes the button's icon size.
   * @method
   * @param {Number} width The desired width of the button icon in pixels.
   * @param {Number} height The desired height of the button icon in pixels.
   */
  setIconSize: function(width, height){
    this.btnEl.setStyle("width", width + "px");
    this.btnEl.setStyle("height", height + "px");
  }
});
//////////////////////
// ..\js\compat\ui\Toolbar.js
//////////////////////
/**
 * Toolbar Compatibility Layer
 * 
 * @class RP.ui.Toolbar
 */

Ext.override(RP.ui.Toolbar, {
    initComponent: function() {
        var deprecate = RP.util.DeprecationUtils;
        
        this._deprecateItems("center");
        this._deprecateItems("right");
        this._deprecateItems("left");
        
        if (Ext.isDefined(this.centerWidth)) {
            this.center = this.center || {};
            this.center.width = this.center.width || this.centerWidth;
            deprecate.deprecatedConfigOption("RP.ui.Toolbar", "centerWidth","Now use:\ncenter: {\n  width: _WIDTH_\n}.");
        }
        this.callOverridden(arguments);
    },
    
    _deprecateItems: function(itemsKey) {
        var deprecate = RP.util.DeprecationUtils;
        var oldItemsKey = itemsKey + "Items";
        
        // backwards compat
        if(Ext.isDefined(this[oldItemsKey])) {
            this[itemsKey] = this[itemsKey] || {};
            this[itemsKey].items = this[itemsKey].items || this[oldItemsKey];
            deprecate.deprecatedConfigOption("RP.ui.Toolbar", oldItemsKey, "Now use:\n" + itemsKey + ": {\n  items: _ITEMS_\n}.");
        }
    }
});
//////////////////////
// ..\js\compat\ui\SummaryGroupingView.js
//////////////////////
// "compatability" layer for the SummaryGroupingView class that is no longer going to be supported/exist.
Ext.define("RP.ui.SummaryGroupingView", {
    constructor: function() {
        var msg = "RP.ui.SummaryGroupingView is no longer supported. An equivalent " +
                   "\"grid feature\" will be added as part of RPWEB-4602.";
        
        RP.util.DeprecationUtils.logStackTrace(msg);
        
        RP.throwError(msg);
    }
});
//////////////////////
// ..\js\compat\ui\GridRowGroup.js
//////////////////////
// "compatability" layer for the GridRowGroup class that is no longer going to be supported/exist.
Ext.define("RP.ui.GridRowGroup", {
    constructor: function() {
        var msg = "RP.ui.GridRowGroup is no longer supported, please use the " +
                   "Ext.grid.feature.Grouping feature now instead.  The Ext control " +
                   "provides the majority of the same functionality, support for any " +
                   "missing functionality will be added as part of RPWEB-4544.";
        
        RP.util.DeprecationUtils.logStackTrace(msg);
        
        RP.throwError(msg);
    }
});
//////////////////////
// ..\js\compat\ui\GridSummary.js
//////////////////////
// "compatability" layer for the GridSummary class that is no longer going to be supported/exist.
Ext.define("RP.ui.GridSummary", {
    constructor: function() {
        var msg = "RP.ui.GridSummary is no longer supported, please use the " +
                   "Ext.grid.feature.Summary feature now instead.  The Ext control " +
                   "provides the majority of the same functionality, support for any " +
                   "missing functionality will be added as part of RPWEB-4654.";
        
        RP.util.DeprecationUtils.logStackTrace(msg);
        
        RP.throwError(msg);
    }
});
//////////////////////
// ..\js\compat\ux\Exception.js
//////////////////////
Ext.define("RP.Exception", {
    extend: "Ext.Error",
    
    name: "RP.Exception",
    
    constructor: function(message, innerError) {
        RP.util.DeprecationUtils.logStackTrace("throw new RP.Exception is deprecated. Please use RP.throwError. *Note* Don't use 'throw' keyword with RP.throwError");
        RP.throwError(message);
        this.callParent(arguments);
    },
    
    /**
     * Gets the inner error, if any, associated with this exception.
     * @return {Error} The inner error.
     */
    getInnerError: function() {
        return this.innerError;
    }
});
//////////////////////
// ..\js\compat\ux\GridView.js
//////////////////////
Ext.override(RP.ui.GridPanel, {
    initComponent: function() {
        
        if(!Ext.isEmpty(this.viewConfig) && !Ext.isEmpty(this.viewConfig.statusIcon)) {
            RP.util.DeprecationUtils.deprecatedConfigOption("RP.ui.GridView", "statusIcon", "Now use \n statusIcon: true on the RP.ui.GridPanel's config. e.g. \n new RP.ui.GridPanel({\n    ...\n    statusIcon:true\n    ...\n});");
            this.statusIcon = this.viewConfig.statusIcon;
        }
        this.callOverridden();
    }
});
RP.util.DeprecationUtils.deprecatedInstanceMethod("Ext.grid.View", "RP.ui.GridView", "renderRowsForExport", "renderRowsForExport");
RP.util.DeprecationUtils.deprecatedInstanceMethod("Ext.grid.View", "RP.ui.GridView", "isHideableColumn", "isHideableColumn");
//////////////////////
// ..\js\compat\ux\MixedCollection.js
//////////////////////
RP.util.DeprecationUtils.deprecatedInstanceMethod("Ext.util.MixedCollection", "RP.collections.MixedCollection", "keyOf", "keyOf");
RP.util.DeprecationUtils.deprecatedInstanceMethod("Ext.util.MixedCollection", "RP.collections.MixedCollection", "trimAt", "trimAt");
//////////////////////
// ..\js\compat\Date.js
//////////////////////
/**
 * Date Compatibility Layer
 *
 * @class RP.Date
 */
(function() {
    var deprecationMessageTracker = {};

    var deprecateDatePrototypeMethod = function(oldMethod, newMethod) {
        if (arguments.length == 1) {
            newMethod = oldMethod;
        }
        RP.util.DeprecationUtils.deprecatedPrototypeMethodToStaticMethod("Date", "RP.Date", oldMethod, newMethod, "\nNote that this is now a static method, so the date instance will be the first argument to the new version.");
    };
    
    var deprecateDateStaticMethod = function(oldMethod, newMethod) {
        if (arguments.length == 1) {
            newMethod = oldMethod;
        }
        RP.util.DeprecationUtils.deprecatedStaticMethodToStaticMethod("Date", "RP.Date", oldMethod, newMethod);
    };
    
    
    
    /**
     * This function is used to emulate what the old date methods did when they were on the Date prototype. They will do
     * the operation and also modify the Date object to keep it consistent for compatibility.
     *
     * e.g. Date.prototype.clearTime(clone) -> RP.Date.clearTime(date, clone).
     *
     * @param {Object} oldMethod The old static method name.
     * @param {Object} newMethod The new static method name.
     */
    deprecateDateMethodThatModifiesTheDate = function(oldMethod, newMethod) {
        var oldClass = Date;
        var newClass = RP.Date;
        
        if (arguments.length == 1) {
            newMethod = oldMethod;
        }
        
        var deprecatedMsg = Ext.String.format("[DEPRECATED] The use of Date.prototype.{0} is deprecated.  Please use RP.Date.{1} \nNote that this is now a static method, so the date instance will be the first argument to the new version. \nAlso, the new method does not modify the date passed in.", oldMethod, newMethod);
        
        deprecationMessageTracker[oldClass + oldMethod] = false;
        oldClass.prototype[oldMethod] = function() {
            if (deprecationMessageTracker[oldClass + oldMethod] === false) {
                RP.util.DeprecationUtils.logStackTrace(deprecatedMsg);
                deprecationMessageTracker[oldClass + oldMethod] = true;
            }
            
            // Put the object as the first argument.
            var newArgs = Ext.Array.clone(arguments);
            newArgs.unshift(this);
            var newDate = newClass[newMethod].apply(newClass, newArgs);
            this.setTime(newDate.getTime());
            
            return newDate;
        };
    };
    
    // For Date.prototype
    deprecateDateMethodThatModifiesTheDate("clearTime");
    deprecateDateStaticMethod("compare");
    deprecateDatePrototypeMethod("clone");
    deprecateDatePrototypeMethod("compareTo", "compare");
    deprecateDatePrototypeMethod("equals");
    deprecateDatePrototypeMethod("between");
    deprecateDatePrototypeMethod("isAfter");
    deprecateDatePrototypeMethod("isOnOrAfter");
    deprecateDatePrototypeMethod("isBefore");
    deprecateDatePrototypeMethod("isOnOrBefore");
    deprecateDateMethodThatModifiesTheDate("addMilliseconds");
    deprecateDateMethodThatModifiesTheDate("addSeconds");
    deprecateDateMethodThatModifiesTheDate("addMinutes");
    deprecateDateMethodThatModifiesTheDate("addHours");
    deprecateDateMethodThatModifiesTheDate("addDays");
    deprecateDateMethodThatModifiesTheDate("adjustForDST");
    deprecateDateMethodThatModifiesTheDate("addWeeks");
    deprecateDateMethodThatModifiesTheDate("addMonths");
    deprecateDateMethodThatModifiesTheDate("addYears");
    deprecateDateStaticMethod("decodeDateInURL");
    deprecateDateStaticMethod("encodeDateToURL");
    deprecateDateStaticMethod("__isLeapYear", "isLeapYear");
    deprecateDateStaticMethod("__getDaysInMonth", "getDaysInMonth");
    deprecateDatePrototypeMethod("formatDate");
    deprecateDatePrototypeMethod("dateFormat");
    deprecateDatePrototypeMethod("formatTime");
    RP.util.DeprecationUtils.deprecatedStaticMethodToStaticMethod("Date", "Ext.Date", "extParseDate", "parse");
    deprecateDateStaticMethod("parseDate");
    deprecateDateStaticMethod("parseTime");
    deprecateDatePrototypeMethod("deltaT");
    deprecateDateMethodThatModifiesTheDate("round");
    deprecateDateMethodThatModifiesTheDate("roundToNearestMinute");
    deprecateDateMethodThatModifiesTheDate("roundToNearestSeconds");
    deprecateDateMethodThatModifiesTheDate("roundBackToHour");
    deprecateDateMethodThatModifiesTheDate("addTime");
    deprecateDatePrototypeMethod("getISO8601StringNoTranslation");
    deprecateDatePrototypeMethod("getISO8601StringInUserTimeZone");
    
    // For RP.util.Dates
    Ext.ns("RP.util.Dates");
    RP.util.DeprecationUtils.deprecatedStaticMethodToPrototypeMethod("RP.util.Dates", "Date", "setMilliseconds", "setMilliseconds");
    RP.util.DeprecationUtils.deprecatedStaticMethodToPrototypeMethod("RP.util.Dates", "Date", "setSeconds", "setSeconds");
    RP.util.DeprecationUtils.deprecatedStaticMethodToPrototypeMethod("RP.util.Dates", "Date", "setMinutes", "setMinutes");
    
})();


//////////////////////
// ..\js\compat\Math.js
//////////////////////
RP.util.DeprecationUtils.deprecatedStaticMethodToStaticMethod("Math", "RP.Math", "roundDecimal", "roundDecimal");
//////////////////////
// ..\js\compat\String.js
//////////////////////
RP.util.DeprecationUtils.deprecatedPrototypeMethodToStaticMethod("String", "RP.String", "contains", "contains");
//////////////////////
// ..\js\compat\..\..\..\stashLibEndLoad.js
//////////////////////
RP.stash.api.endLoadLib();
