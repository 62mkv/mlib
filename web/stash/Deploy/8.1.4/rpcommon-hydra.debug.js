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
// ..\js\patches\taskflow\TaskflowFrame.js
//////////////////////
// override to maintain a change in behavior within the card layout from 3 to 4.
// in 4, the card layout's first item will be rendered/shown by default...
// this was causing the "no roles/taskflows" task to be shown briefly while the rest
// of the page was loading.
// see: http://www.sencha.com/forum/showthread.php?134442-card-layout-deferredRender-not-working
Ext.override(RP.taskflow.TaskflowFrame, {
    _getViewPanelLayoutConfig: function() {
        return {
            type: "card",
            deferredRender: true,
            alwaysLayout: false,
            parseActiveItem: function(item) {
                if (!Ext.isDefined(item)) {
                    return null;
                }
                else if (item && item.isComponent) {
                    return item;
                }
                else if (typeof item == 'number') {
                    return this.getLayoutItems()[item];
                }
                else {
                    return this.owner.getComponent(item);
                }
            }
        };
    }
});
//////////////////////
// ..\js\taskflow\prototype\ViewContainer.js
//////////////////////
/**
 * @class RP.taskflow.prototype.ViewContainer
 *
 * Class that by default gets displayed below the taskflow container
 *
 * @author plee
 */
Ext.define('RP.taskflow.prototype.ViewContainer', {
    extend: 'Ext.container.Container',
    
    initComponent: function() {
        Ext.apply(this, {
            layout: {
                type: 'border',
                targetCls: 'rp-transparent-panel'
            },
            items: this._createItems()
        });
        
        this.callParent();
    },
    
    _createItems: function() {
        this._widgetContainer = this._createWidgetContainer();
        this._roundedCorner = this._createRoundedCorner();
        this._setupViewPanel();
        
        return [this._widgetContainer, this.viewPanel, this._roundedCorner];
    },
    
    _setupViewPanel: function(){
        this.viewPanel.region = 'center';
        //show the rounded corner when the intial view panel pop in happens
        this.viewPanel.on('show', function() {
            this._roundedCorner.show();
        }, this);
        
    },
    
    _createRoundedCorner: function() {
        return new Ext.container.Container({
            hidden: true,
            weight: -100,
            cls: 'rp-taskflow-taskflowframe-rounded',
            region: this.navRegion || "west",
            width: 4,
            items: [new Ext.Component({
                cls: 'top'
            }), new Ext.Component({
                cls: 'bottom'
            })]
        });
    },
    
    _createWidgetContainer: function() {
        return new Ext.container.Container({
            itemId: 'widgetContainer',
            region:  this.navRegion || "west",
            layout: 'card',
            margin: '10 0 0 0'
        });
    },
    
    getWidgetContainer: function() {
        return this._widgetContainer;
    }
});
//////////////////////
// ..\js\taskflow\prototype\TaskflowFrame.js
//////////////////////
/**
 * @class RP.taskflow.prototype.TaskflowFrame
 * @extends RP.taskflow.TaskflowFrame
 */
Ext.define("RP.taskflow.prototype.TaskflowFrame", {
    extend: "RP.taskflow.TaskflowFrame",

    /**
     * @cfg
     */
    menuItemBaseCls: 'rp-prototype-taskflow-dropdown-item',

    /**
     * @cfg
     */
    menuItemCheckedCls: 'rp-prototype-taskflow-dropdown-item-checked',

    helpMargin: '0 5 0 0',

    modulesWidth: 216,

    /**
     * @cfg {String} This is the css class to be applied to the
     * modules button
     */
    modulesBtnCls: 'rp-modules-btn',

    /**
     * @cfg {String} This is the css class to be applied to the
     * extra Widgets button.  The states are '-hover', '-pressed', '-normal'
     */
    extraWidgetsBtnCls: 'rp-extra-widgets-btn',

    /**
     * @cfg {Number} This is the height of the taskflow bar.
     * all the buttons inside will also get this height
     */
    taskflowBarHeight: 35,

    initComponent: function() {
        this.addEvents(
        /**
         * @event taskchange
         * Fires when a task has been changed.
         */
        'taskchange');

        this.callParent();
    },

    _createItems: function() {
        return [this._viewContainer, this._createNavigationBar()];
    },

    /**
     * @override
     */
    _createTaskflowContainer: function() {
        var height = this.taskflowBarHeight - 1, //take account for 1px top border
            container = Ext.create('RP.taskflow.prototype.TaskflowsContainer', {
            layout: {
                type: 'hbox',
                pack: (RP.globals.NAV_REGION === 'east') ? 'end' : 'start'
            },
            defaults: {
                height:height,
                style: {
                    lineHeight: height + 'px'
                }
            }
        });

        container.on('afterlayout', this._onAfterResizeTaskflowContainer, this);
        container.on('add', this._onAddTaskflowContainer, this);

        return container;
    },

    /**
     * @override
     */
    _createTaskflowContainerConfig: function() {
        var config = this.callParent(arguments);

        config.text = config.isTitleMessageName ? RP.getMessage(config.title) : config.title;

        return config;
    },

    /**
     * @override
     */
    _onAppActivated: function(tfc, taskForm) {
        this.callParent(arguments);

        if(this._helpButton) {
            var taskCfg = RP.globals.CURRENT_TASK.taskConfig,
                helpButton = this._helpButton,
                helpUrl = helpButton ? helpButton.getHelpUrl() : undefined,
                helpVisibilityFn = (!RP.globals.getValue('IS_HELP_DISABLED') && helpUrl) ? 'show' : 'hide';

            helpButton[helpVisibilityFn]();
        }

        this._maybeFireTaskChanged(taskForm);
    },

    _maybeFireTaskChanged: function(taskForm) {
        var taskId = taskForm.id;
        if(this._currentTaskform !== taskId) {
            if(this._currentTaskform) {
                this.fireEvent('taskChange');
            }
            this._currentTaskform = taskId;
        }

    },

    _hideOrShowDivider: function(tfc) {
        var clsFn = (tfc === this._rpTaskflowsContainer.items.first()) ? 'addCls' : 'removeCls';
        this._dividerComponent[clsFn]('first-active');
    },

    _onAddTaskflowContainer: function(taskflowContainer, taskflow) {
        this._extraTaskflowsMenu.add(this._createMenuItem(taskflow));
        taskflow.on('rpext-before-appactivated',
                    this._createHideOrShowDividerOnBeforeActivateFn(taskflow), this);
    },

    _createHideOrShowDividerOnBeforeActivateFn: function(taskflow) {
        return function() {
            this._hideOrShowDivider(taskflow);
        };
    },

    _addTaskflowContainers: function(){
        this.callParent(arguments);
        this._taskflowsContainerSpacer = this._createTaskflowsContainerSpacer();
        this._rpTaskflowsContainer.un('add', this._onAddTaskflowContainer, this);
        this._rpTaskflowsContainer.insert(RP.globals.NAV_REGION === 'east' ? 0 : undefined, this._taskflowsContainerSpacer);
    },

    _createTaskflowsContainerSpacer: function(){
        return new Ext.Component({
            cls: 'rp-taskflow-container-gradient rp-taskflow-container-item',
            flex: 1
        });
    },

    _createMenuItem: function(taskFlowContainer) {
        var menuItem = Ext.create('Ext.menu.CheckItem', {
            text: taskFlowContainer.getText(),
            group: this.id,
            itemId: taskFlowContainer.itemId,
            baseCls: this.menuItemBaseCls,
            hidden: true,
            activeCls: '',
            checkedCls: this.menuItemCheckedCls
        });

        taskFlowContainer.on('beforehide', menuItem.show, menuItem);
        taskFlowContainer.on('beforeshow', menuItem.hide, menuItem);
        taskFlowContainer.on('hide', this._updateMenuItemStyle, this);
        taskFlowContainer.on('show', this._updateMenuItemStyle, this);

        taskFlowContainer.on('rpext-before-appactivated', menuItem.setChecked, menuItem);

        menuItem.on('click', function() {
            taskFlowContainer.handler();
            menuItem.setChecked(true);
        });

        return menuItem;
    },

    /**
     * @private
     * Ensure that menu items have the correct CSS applied, since the default
     * first-, last-, & only-child selectors don't work well with hidden items.
     */
    _updateMenuItemStyle: function(taskflowContainer, eOpts) {
        var firstVisible = this._extraTaskflowsMenu.items.findBy(function(item) {
            return !item.hidden;
        }, this),
            lastItem = this._extraTaskflowsMenu.items.last();

        // If any items are visible, the lastItem is guaranteed to be visible
        // because we always hide taskflows from right to left.
        if(!lastItem.hidden) {
            if(firstVisible === lastItem) {
                lastItem.addCls('rp-only-visible-menu-item');
                lastItem.removeCls('rp-first-visible-menu-item');
                lastItem.removeCls('rp-last-visible-menu-item');
            }
            else {
                firstVisible.addCls('rp-first-visible-menu-item');
                firstVisible.removeCls('rp-only-visible-menu-item');
                firstVisible.removeCls('rp-last-visible-menu-item');
                lastItem.addCls('rp-last-visible-menu-item');
                lastItem.removeCls('rp-first-visible-menu-item');
                lastItem.removeCls('rp-only-visible-menu-item');
            }
        }
    },

    /**
     * This will check to see if any widgets are not visible,
     * and if they are, add them to a drop down menu.  If they are
     * in the drop down menu and there is room for them, they
     * should be removed from the drop down menu and added back to
     * the container
     * this._extraTaskflowsMenu, this._extraTaskflowsButton
     **/
    _onAfterResizeTaskflowContainer: function(container) {
        if (!this._bufferedButtonShowOrHideFn) {
            this._bufferedButtonShowOrHideFn = Ext.Function.createBuffered(this._showOrHideButton, 10, this);
        }

        var taskflowFrame = container;
        var widgetsMenu = this._extraTaskflowsMenu;
        var width = 0;
        var curWidth = 0;
        var space = taskflowFrame.getWidth();

        Ext.suspendLayouts();
        for (var i = 0; i < taskflowFrame.items.getCount(); i++) {
            var widget = taskflowFrame.items.get(i);
            var menuItem = widgetsMenu.items.get(i);
            if (widget !== this._taskflowsContainerSpacer) {

                //since width = 0 when widgets are hidden, we need to preserve
                //the actual width of the widget so we know how wide it was
                if (!widget.standardWidth) {
                    Ext.apply(widget, {
                        standardWidth: widget.getWidth()
                    });
                }

                curWidth = widget.standardWidth;
                width += curWidth;

                if (width >= space) {
                    widget.hide();
                }
                else {
                    widget.show();
                }
            }
        }

        Ext.resumeLayouts(true);
        this._bufferedButtonShowOrHideFn();
    },

    _showOrHideButton: function() {
        var hide = Ext.Array.every(this._extraTaskflowsMenu.items.getRange(), function(taskflow) {
            return taskflow.isHidden();
        });

        if (hide) {
            this._extraTaskflowsButton.hide();
            // Make sure the menu only shows when the user is allowed to manually show / hide it.
            this._extraTaskflowsMenu.hide();
        }
        else {
            this._extraTaskflowsButton.show();
        }
    },

    _createHelpButton: function() {
        return Ext.create('RP.help.Button');
    },

    _createExtraWidgetsButton: function() {
        this._extraTaskflowsMenu = Ext.create('RP.menu.BalloonMenu');
        this._extraTaskflowsMenu.addCls('rp-extra-taskflows-menu');

        this._extraTaskflowsButton = Ext.create('Ext.button.Button', {
            menu: this._extraTaskflowsMenu,
            cls: this.extraWidgetsBtnCls + '-normal',
            arrowCls: this.extraWidgetsBtnCls + '-arrow',
            itemId: 'RP.ExtraTaskflowsMenuButton',
            width: 38,
            hidden: true
        });

        //The menu doesn't seem to know how to align itself properly, so use this listener.
        this._extraTaskflowsButton.on('click', function(){
            this._extraTaskflowsMenu.getEl().alignTo(this._extraTaskflowsButton.getEl(), 't-b', [-4, -8]);
        }, this);
    },

    _getInactiveModulesDataSet: function() {
        var inactiveModules = [], activeModuleName = RP.core.PageContext.getActiveModuleName(), name;
        Ext.each(this.modules, function(module) {
            name = module.name;
            if (activeModuleName !== name) {
                inactiveModules.push({
                    label: RP.getMessage(module.label),
                    name: name
                });
            }
        }, this);

        return inactiveModules;
    },

    _getActiveModule: function() {
        var activeModuleName = RP.core.PageContext.getActiveModuleName(), activeModule;
        Ext.each(this.modules, function(module) {
            if (module.name === activeModuleName) {
                activeModule = module;
                return false; //break
            }
            return true;//continue
        }, this);
        return activeModule;
    },

    _createModulesContainer: function(){
        return new Ext.container.Container({
            cls: 'rp-taskflow-container-gradient',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            width: this.modulesWidth,
            items: this._createModulesContainerItems()
        });
    },

    _createModulesContainerItems: function() {
        var items = [{xtype: 'component', width: 10}, this._createModulesButton(), {
            xtype: 'component',
            flex: 1
        }];

        return items;
    },

    _createModulesButton: function() {
        var modulesButton = Ext.create('RP.taskflow.button.Button', {
            itemId: 'moduleSelector',
            cls: this.modulesBtnCls,
            text: RP.getMessage(this._getActiveModule().label),
            menu: this._createModulesMenu(),
            menuAlign: 'tl-bl'
        });

        // We could hide the dropdown arrow by simply not creating a menu on
        // the modulesButton, but we would still see the wrong cursor (pointer)
        // on the button and its children, even though clicking it does
        // nothing. Easier to add a new class and change everything in CSS.
        if(modulesButton.menu.items.getCount() === 0) {
            modulesButton.addCls(this.modulesBtnCls + '-hide-menu');
        }

        return modulesButton;
    },

    _createModulesMenu: function() {
        var menu = Ext.create('Ext.menu.Menu', {
            lastChildCls: 'rp-last-child',
            minWidth: 0,
            cls: 'rp-modules-menu',
            collapsed: true,
            items: this._createModuleMenuItems(),
            shadow: 'drop',
            useArrow: false,
            listeners: {
                scope: this,
                click: function(menu, item, event, options) {
                    RP.core.PageContext.setActiveModule(item.value);
                }
            }
        });

        this._setupModuleMenuAnimation(menu);

        return menu;
    },

    /**
     * @private
     * Current design pattern says that the menu should animate.
     * Element's slideIn/slideOut methods won't work because they wrap the animated
     * el, causing its box shadow to disappear.
     */
    _setupModuleMenuAnimation: function(moduleMenu) {
        Ext.apply(moduleMenu, {
            // Override so that the menu doesn't build a header el
            // with a re-expand button in it.
            getCollapsedDockedItems: function() {return [];}
        });

        moduleMenu.on('beforehide', function(menu) {
            if (!menu.collapsed) {
                menu.collapse();
                return false;
            }
        }, this);
        moduleMenu.on('collapse', function(menu) {
            menu.hide();
        }, this);
        moduleMenu.on('show', function(menu) {
            menu.expand();
        }, this);
    },

    _createModuleMenuItems: function() {
        var menuItemData = this._getInactiveModulesDataSet();
        var menuItems = [];
        Ext.each(menuItemData, function(item) {
            menuItems.push({
                plain: true,
                text: item.label,
                value: item.name,
                itemId: item.name
            });
        }, this);
        return menuItems;
    },

    _createDividerComponent: function() {
        this._dividerComponent = Ext.create('Ext.Component', {
            cls: 'rp-taskflow-divider',
            width: 2
        });

        return this._dividerComponent;
    },

    /**
     * @private
     * Creates the top taskflow container which contains
     * the help button and modules dropdown.
     */
    _createNavigationBar: function() {
        this._helpContainer = this._createHelpContainer();
        this._rpTaskflowsContainer.flex = 1;

        var items,
            isEastNav = (RP.globals.NAV_REGION === 'east');

        if(isEastNav) {
            items = [this._helpContainer, this._rpTaskflowsContainer,
                this._createDividerComponent(), this._createModulesContainer()];
        }
        else {
            items = [this._createModulesContainer(), this._createDividerComponent(),
                this._rpTaskflowsContainer, this._helpContainer];
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

    _afterTaskflowBarRender: function(taskflowBar) {
        taskflowBar.el.unselectable();
    },

    _createHelpContainer: function() {
        this._createExtraWidgetsButton();
        this._helpButton = this._createHelpButton();

        var isEastNav = RP.globals.NAV_REGION === 'east',
            items;

        if(isEastNav) {
            items = [this._helpButton, {
                xtype: 'component',
                flex: 1
            }, this._extraTaskflowsButton];
        }
        else {
            items = [this._extraTaskflowsButton, {
                xtype: 'component',
                flex: 1
            }, this._helpButton];
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
    }
});
//////////////////////
// ..\js\taskflow\prototype\TaskflowsContainer.js
//////////////////////
Ext.define('RP.taskflow.prototype.TaskflowsContainer', {
    extend: 'Ext.container.Container',
    
    cls: 'rp-taskflow-prototype-taskflows-container',
    
    /**
     * @cfg {String} activeCls css class that will be applied to the
     * active taskflow
     */
    activeCls: 'rp-taskflow-prototype-active',
    
    /**
     * @cfg {String} finishedCls the css class the will be applied to a completed taskflow
     */
    finishedCls: '',
    
    id: "_rp_taskflow_container",
    
    
    initComponent: function() {
        this.on('add', this._onAdd, this);
        
        this.callParent(arguments);
    },
    
    /**
     * @private
     * Add the active cls on activate
     */
    _onTaskflowActivated: function(taskflow) {
        //keep track the height on the incoming taskflow to reset the height
        var height = taskflow.getHeight();
        if(this._activeTaskflow !== taskflow) {
            if(this._activeTaskflow) {
                this._activeTaskflow.removeCls(this.activeCls);
                this._activeTaskflow.setHeight(height);
            }

            this._activeTaskflow = taskflow;

            this._activeTaskflow.addCls(this.activeCls);
            //set the height on the active task to overlay the shadow on the container
            this._activeTaskflow.setHeight(height + 5);
        }
    },

    /**
     * @private
     * Add the appactivated listener on add
     */
    _onAdd: function(container, component) {
        component.on('rpext-before-appactivated', this._onTaskflowActivated, this);
    }
});
//////////////////////
// ..\js\taskflow\prototype\TaskflowContainerMixin.js
//////////////////////
Ext.define("RP.taskflow.prototype.TaskflowContainerMixin", {
    /**
     * @cfg true to enable widget/ taskflow animations
     */
    useAnimations: true,
    
    /**
     * @cfg the default animation duration length
     */
    defaultAnimDuration: 200,
    /**
     * @cfg autoLoadWidgets true to load the widget code upfront 
     */
    autoLoadWidgets: true,
    
    /**
     * @cfg {String/Object} taskflow (Required) The taskflow xtype name or
     * object. Must implement {@link RP.interfaces.ITaskflowContainer}
     */
    /**
     * @cfg {String} title The taskflow title.
     */
    
    initComponent: function() {
        logger.logTrace("[TaskflowContainer] initComponent; taskflow: " + this.taskflow + "; itemId: " + this.itemId);
        
        this._taskflow = null;
        this._initializing = false;
        this._widgets = null;
        this._activeWidget = null;
        
        this.addEvents(        /**
         * @event rpextappactivated
         * Fires after an app in this taskflow is activated. Full name :
         * "rpext-appactivated".
         * @param {Ext.Component} this
         * @param {Ext.Component} application component
         */
        "rpext-appactivated",        /**
         * @event rpexttfcreated
         * Fires after the taskflow object has been created. Full name :
         * "rpext-tf-created"
         * @param {Ext.Component} this
         */
        "rpext-tf-created",        /**
         * @event rpexttfaborted
         * Fires after the taskflow has aborted (failed to download its task
         * code, etc.) Full name : "rpext-tf-aborted"
         * @param {Ext.Component} this
         */
        "rpext-tf-aborted",   /** @event rpext-widgets-loaded
         * Fires after the widgets have been loaded
         * @param {Ext.Component} this
         */
        "rpext-widgets-loaded", /** @event rpext-taskflow-finished
         * Fires when the taskflow has been finished, ie when widgets have dependencies and 
         * those dependencies have been fulfilled.  This is different than taskflow complete, taskflow 
         * complete gets fired when none of the widgets have dependencies which essentially completes a taskflow
         * once it is clicked if there are no dependancies.
         * @param {Ext.Component} this
         */
        "rpext-taskflow-finished",
        /** @event rpext-before-appactivated
         * Fires when before appactiveated is fired
         * @param {Ext.Component} this
         */
        "rpext-before-appactivated");
        
        if (this.autoLoadWidgets) {
            this.ensureTaskflowInitialized();
        }
        
        this.on('rpext-widgets-loaded', this._onWidgetsLoaded, this);
        
        this.on('click', this.handler, this, {
            element: 'el'
        });
        
        this.on("destroy", this._onDestroy, this);
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * @return {String} Name of the taskflow
     */
    getTaskflowName: function() {
        return this.taskflow;
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * @return {RP.taskflow.Taskflow} This taskflow object
     */
    getTaskflow: function() {
        return this._taskflow;
    },
    
    /**
     * @private
     * Don't deactivate any widget
     */
    onDeactivated: function(){
        
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * Decides activateDefaultWidget based on the rendered, and taskflow being
     * instantiated.
     */
    activateDefaultWidget: function() {
        if (!this.rendered || !this._taskflow) {
            this._activateDefaultWidget = true;
            return;
        }
        
        var activateFn = Ext.bind((function() {
            logger.logTrace("[TaskflowContainer] Activating widget...");
            
            this.ensureTaskflowInitialized();
            // determine index of default widget to activate
            var idx = 0;
            var defaultWidget = this.getWidgetByID(this._taskflow.getDefaultTaskId());
            
            if (defaultWidget) {
                idx = this._widgets.indexOf(defaultWidget);
            }
            var widget = this._widgets.getAt(idx);
            while (widget.disabledWidget) {
                widget = this._widgets.getAt(++idx);
            }
      
            
            this.onWidgetClick(widget, undefined);
        }), this);
        
        if (this._widgets && this._widgets.getCount() > 0) {
            activateFn();
        }
        else if (!this._started) {
            this._taskflow.addPostInitListener(activateFn);
        }
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * @param {Object} widget Widget which was clicked
     * @private
     */
    onWidgetClick: function(widget) {
        //if the widget has changed or the taskflow has changed.
        if (widget !== this._activeWidget || (this.taskflowFrame._activeTFC && this.taskflow !== this.taskflowFrame._activeTFC.taskflow)) {
            var fn = function(result) {
                if (result) {
                    this._widgetClickHandler(widget);
                }
                else {
                    logger.logTrace("[TaskflowContainer] onWidgetClick widget canceled; itemId: " + widget.itemId);
                }
            };
            
           this._beforeAppActivateHandler(Ext.bind(fn, this));
        }
        else {
            this._widgetClickHandler(widget);
        }
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation.
     * Get the task widget component by task ID (roster element ID member)
     * @param {String} id The taskflow task ID
     * @return {RP.taskflow.BaseTaskflowWidget} The widget
     */
    getWidgetByID: function(id) {
        if (this._widgets) {
            return this._widgets.get(id);
        }
        return null;
    },
    
    /**
     * RP.interfaces.ITaskflowContainer2 implementation.
     * Sets the single handler for beforeAppActivate.
     * @param {Function} cb The new beforeAppActivate handler
     */
    setBeforeAppActivateHandler: function(cb) {
        this._beforeAppActivateHandler = cb;
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * Adds a handler for when applications are activated
     * @param {Function} cb Handler function
     */
    addApplicationActivatedHandler: function(cb) {
        this._addEventHandler("rpext-appactivated", cb);
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * Adds a listener for the taskflow created event
     * @param {Function} Function to call on taskflow created event
     */
    addTaskflowCreationListener: function(cb) {
        this.on("rpext-tf-created", cb);
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * Removes a listener for the task flow creation event
     * @param {Function} cb Taskflow creation listener to be removed
     */
    removeTaskflowCreationListener: function(cb) {
        this.un("rpext-tf-created", cb);
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * Adds a handler function for the taskflow aborted event.
     * @param {Function} cb Function which listens for taskflow abortion
     */
    addTaskflowAbortHandler: function(cb) {
        this.on("rpext-tf-aborted", cb);
    },
    
    /**
     * {@link RP.interfaces.ITaskflowContainer} implementation
     * Sets the delegate to invoke to rebuild this taskflow. Creates an 
     * internal rebuild handler
     * @param {Function} delegate The delegate to invoke in order to rebuild this
     * taskflow.  It takes a lone argument, which is either a config object
     * to create an ITaskflowContainer2 instance with, or an actual
     * ITaskflowContainer2 instance. 
     */
    setRebuildTaskflowHandler: function(delegate) {
        this._rebuildHandler = delegate;
    },
    
    /**
     * Rebuild this taskflow.
     * @param {Object} config New taskflow config
     */
    rebuild: function(config) {
        this._rebuildHandler(this.getTaskflow(), config.title, config);
    },
    
    /**
     * Returns the active widget
     * @return {Object} The active widget
     */
    getActiveWidget: function() {
        return this._activeWidget;
    },
    
    /** 
     * Adds an event handler
     * @param {String} evtName Name of the event
     * @param {Function} cb Handler function
     * @private 
     */
    _addEventHandler: function(evtName, cb) {
        if (this.rendered) {
            this.mon(this, evtName, cb);
        }
        else {
            this.on("render", function() {
                this.mon(this, evtName, cb);
            });
        }
    },
    
    /** 
     * Removes an event handler based
     * @param {String} evtName Name of the event
     * @param {Function} cb Handler function
     * @private 
     */
    _removeEventHandler: function(evtName, cb) {
        this.mun(this, evtName, cb);
    },
    
    /**
     * Destroys all taskforms in each widget
     * @private 
     */
    _onDestroy: function() {
        if (this._widgets) {
            this._widgets.each(function(widget) {
                var taskForm = widget.getTaskForm();
                if (taskForm) {
                    taskForm.destroy();
                }
            }, this);
        }
    },
    
    /** 
     * Guarantees that the taskflow has been initialized. If not,
     * initializes the taskflow
     * @private 
     */
    ensureTaskflowInitialized: function() {
        var itf, el, initProgressMask;
        
        if (this._taskflow || this._initializing) {
            return;
        }
        
        this._initializing = true;
        el = this.getEl();
        if (el) {
            initProgressMask = new Ext.LoadMask(el, {
                msg: RP.getMessage("rp.common.misc.LoadingMaskText")
            });
        }
        
        if (initProgressMask) {
            Ext.defer(initProgressMask.show, 5, initProgressMask);
        }
        logger.logTrace("[TaskflowContainer] ensureTaskflowInitialized() starting");
        var steps = new RP.util.FunctionQueue();
        
        if (!this._taskflow) {
            steps.add(Ext.bind(this._loadTaskflowCodeStep, this));
        }
        steps.add(Ext.bind(this._initTaskflowStep, this));
        
        steps.execute(Ext.bind((function() {
            if (initProgressMask) {
                initProgressMask.hide();
            }
            this._initializing = false;
            logger.logTrace("[TaskflowContainer] ensureTaskflowInitialized() completed successfully");
        }), this), Ext.bind((function() {
            if (window.leavingPage) {
                return;
            }
            if (initProgressMask) {
                initProgressMask.hide();
            }
            this._initializing = false;
            logger.logFatal("[TaskflowContainer] ensureTaskflowInitialized() completed with ERROR");
            
            Ext.Msg.alert(RP.getMessage("rp.common.misc.Error"), RP.getMessage("rp.common.misc.TaskflowInitFailed"));
        }), this));
    },
    
    /**
     * Creates the success and failure function for creating taskflows.
     * Adds listeners and rebuild handlers, setting them accordingly. Calls
     * successFn or errorFn for each, based on success or failure.
     * @param {Function} successFn Function to call if successful
     * @param {Function} errorFn Function to call if failure occurs
     * @private 
     */
    _loadTaskflowCodeStep: function(successFn, errorFn) {
        var createTFSuccessFn = Ext.bind((function(tf) {
            this._taskflow = tf;
            
            this.fireEvent("rpext-tf-created", this);
            
            var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
            
            if (!itf) {
                logger.logDebug("[TaskflowContainer] Taskflow does not implement the RP.interfaces.ITaskflow2 interface");
                errorFn();
                return;
            }
            
            itf.addTaskCompletedListener(Ext.bind(this._onTaskCompleted, this));
            itf.addTaskflowCompletedListener(Ext.bind(this._onTaskflowCompleted, this));
            itf.setRebuildHandler(Ext.bind(this.rebuild, this));
            
            if (this._activateDefaultWidget) {
                this.activateDefaultWidget();
            }
            
            successFn();
        }), this);
        
        var createTFFailedFn = Ext.bind((function(tfDef) {
            this.fireEvent("rpext-tf-aborted", this);
            errorFn();
        }), this);
        
        RP.createTaskflow(this.taskflow, this.initialContext, createTFSuccessFn, createTFFailedFn);
    },
    
    /**
     * Initializes the taskflow if it isn't already. Calls the errorFn if
     * an error occurs, but does not call successFn for success.
     * @param {Function} successFn Function to call if successful
     * @param {Function} errorFn Function to call if failure occurs
     * @private 
     */
    _initTaskflowStep: function(successFn, errorFn) {
        var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
        
        if (!itf.isInitialized()) {
            try {
                itf.initTaskflow(Ext.bind((function() {
                    this._loadWidgets(successFn, errorFn);
                }), this));
            } 
            catch (e) {
                logger.logError(e);
                errorFn();
                
                if (RP.globals.SERVER_TYPE === "development") {
                    throw e;
                }
            }
        }
    },
    
    /**
     * Loads the widgets. Calls either successFn or errorFn for whichever one
     * occurs.
     * @param {Function} successFn Function to call if successful
     * @param {Function} errorFn Function to call if failure occurs
     * @private 
     */
    _loadWidgets: function(successFn, errorFn) {
        var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
        var tfConfig = Ext.apply({}, this.initialConfig);
        var roster = itf.getTaskflowRoster(tfConfig);
        
        this._widgets = new RP.collections.MixedCollection();
        
        logger.logDebug("[TaskflowContainer] Loading roster of " + roster.length + " widgets.");
        
        Ext.each(roster, function(item) {
            var widgetCfg = itf.createTaskWidgetConfig(item);
            
            if (typeof widgetCfg.itemId === "undefined") {
                widgetCfg.itemId = item.id;
            }
            
            if (widgetCfg) {
                var widget = Ext.ComponentMgr.create(widgetCfg);
                var iwidget = RP.iget(widget, RP.interfaces.ITaskWidget);
                
                iwidget.setTaskflow(this._taskflow);
                iwidget.setTaskId(item.id);
                iwidget.initializeLocalContext();
                iwidget.subscribeWidgetClick(Ext.bind(this.onWidgetClick, this));
                iwidget.subscribeWidgetChange(Ext.bind(this._onWidgetChange, this));
                
                this._widgets.add(item.id, widget);
                
                widget.failedDependencies = [];
                widget.on("render", function() {
                    if (Ext.isIE) {
                        this.mon(widget.el, "mouseenter", this._handleMouseOver, widget);
                        this.mon(widget.el, "mouseleave", this._handleMouseOut, widget);
                    }
                    else {
                        this.mon(widget.el, "mouseover", this._handleMouseOver, widget);
                        this.mon(widget.el, "mouseout", this._handleMouseOut, widget);
                    }
                }, this);
            }
        }, this);
        
        // Add Description panel.
        var descriptionText = itf.getTaskflowDescription();
        var uicfg = itf.getTaskflowUIConfig();
        var descriptionComp;
        
        if (uicfg) {
            if (uicfg.createDescriptionCompFn) {
                descriptionComp = new Ext.Panel({
                    itemId: "description",
                    cls: "rp-taskflow-description",
                    items: uicfg.createDescriptionCompFn(this._taskflow, descriptionText)
                });
            }
            else if (descriptionText) {
                descriptionComp = new Ext.Panel({
                    itemId: "description",
                    html: descriptionText,
                    cls: "rp-taskflow-description"
                });
            }
            
        }
        
        itf.startTaskflow();
        
        // Update status and UI...
        this._onWidgetChange();
        successFn();
        
        this.fireEvent('rpext-widgets-loaded', this);
    },
    
    /**
     * Handles when widgets are clicked. Makes sure that the widget has a
     * taskform, and sets the corrent current widget.
     * @param {Object} widget Widget which was clicked 
     * @private 
     */
    _widgetClickHandler: function(widget) {
        logger.logTrace("[TaskflowContainer] Widget clicked itemId: " + widget.itemId);
        
        var newInst = false;
		
        var lastLocation = RP.CURRENT_LOCATION;
        RP.CURRENT_LOCATION = {
            module: RP.globals.CURRENT_MODULE.name,
            taskflow: this.getTaskflowName(),
            task: this._widgets.keyOf(widget)
        };
        
        if (!widget.getTaskForm()) {
            widget.createTaskForm();
            if (!widget.getTaskForm()) {
                RP.CURRENT_LOCATION = lastLocation;
                return;
            }
            
            if (!widget.taskForm.itemId) {
                logger.logWarning("[TaskflowContainer] Task form for widget with itemId '" + widget.itemId + "' is missing itemId.  Use BaseTaskflowWidget._getTaskFormItemId() to generate default itemId");
            }
            newInst = true;
        }
        
        var taskID;
        var itf = RP.iget(widget.taskForm, RP.interfaces.ITaskForm);
        
        if (newInst) {
            if (!itf) {
                logger.logDebug("[TaskflowContainer] Your taskflow app needs to implement the RP.interfaces.ITaskForm interface");
                RP.CURRENT_LOCATION = lastLocation;
                return;
            }
            
            taskID = this._widgets.keyOf(widget);
            if (taskID) {
                itf.setUrlHash(RP.core.PageContext.getTaskHash(this.getTaskflowName(), taskID, this._taskflow.getTaskflowContext(), widget.getTaskContext()));
            }
        }
        else if (widget.taskForm) {
            taskID = this._widgets.keyOf(widget);
            itf.setUrlHash(RP.core.PageContext.getTaskHash(this.getTaskflowName(), taskID, this._taskflow.getTaskflowContext(), widget.getTaskContext()));
        }
        
        var isNewActiveWidget;
        
        
        if (widget !== this._activeWidget) {
            isNewActiveWidget = true;
            this._deactivateCurrentWidget();
            
            var activeTaskID = this._widgets.keyOf(widget);
            var taskCfg = this._taskflow.getTaskConfig(activeTaskID);
            if (taskCfg.completeTaskOnClick) {
                // Mark the task that is represented by this widget as 'complete'.
                var itf2 = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
                itf2.setTaskCompleted(activeTaskID, true);
                
                if (itf2.isCompleted()) {
                    this.fireEvent('rpext-taskflow-finished', this);
                }
            }
            
            this._activeWidget = widget;
            widget.setActive(true);
        }
        else {
            isNewActiveWidget = false;
        }
        
        this.fireEvent('rpext-before-appactivated', this);
        this._activateWidgetSet(isNewActiveWidget);
    },
    
    /**
     * Deactivates the current widget.
     * @private
     */
    _deactivateCurrentWidget: function() {
        if (this._activeWidget) {
            this._activeWidget.setActive(false);
            this._activeWidget = null;
        }
    },
    
    /**
     * For every failed dependency, add the rp-widget-dependency class.
     * @param {Object} ev Event object (Unused)
     * @param {Object} t Placeholder parameter (Unused)
     * @private
     */
    _handleMouseOver: function(ev, t) {
        if (this.failedDependencies) {
            Ext.each(this.failedDependencies, function(dependency) {
                dependency.addCls("rp-widget-dependency");
            }, this);
        }
    },
    
    /**
     * Activate the current widgets.
     */
    handler: function() {
        this.activateTasks(true);
    },
    
    /**
     * For every failed dependency, add the rp-widget-dependency class.
     * @param {Object} ev Event object (Unused)
     * @param {Object} t Placeholder parameter (Unused)
     * @private
     */
    _handleMouseOut: function(ev, t) {
        if (this.failedDependencies) {
            Ext.each(this.failedDependencies, function(dependency) {
                dependency.removeCls("rp-widget-dependency");
            }, this);
        }
    },
    
    /**
     * Responds to widgets changing active status. Creates an array of 
     * failed dependencies, relevent to {@link #_handleMouseOver} and 
     * {@link #_handleMouseOut}. Items in this array are tasks that haven't
     * been completed in the taskflow.
     * @private
     * */
    _onWidgetChange: function() {
        
        var itf = RP.iget(this._taskflow, RP.interfaces.ITaskflow2);
        
        itf.updateStatus();
        if (itf.isCompleted()) {
            return;
        }
        
        this._widgets.each(function(widget) {
            var failedDependecies = [];
            var taskID = this._widgets.keyOf(widget);
            var taskCfg = this._taskflow.getTaskConfig(taskID);
            
            if (taskCfg.dependencies) {
                Ext.each(taskCfg.dependencies, function(dependency) {
                    if (!itf.isTaskCompleted(dependency)) {
                        failedDependecies.push(this._widgets.get(dependency));
                    }
                }, this);
            }
            
            widget.setEnabled(failedDependecies.length === 0);
            widget.failedDependencies = failedDependecies;
        }, this);
    },
    
    /**
     * If the widget has a onTaskComplete function, it is called.
     * @param {RP.taskflow.Taskflow} tf The taskflow object
     * @param {Object} taskConfig Config options of the taskflow object
     * @private 
     */
    _onTaskCompleted: function(tf, taskConfig) {
        logger.logInfo("[TaskflowContainer] Task completed.  Taskflow name: " + tf.name + "; roster item id: " + taskConfig.id);
        var widget = this.getWidgetByID(taskConfig.id);
        
        if (Ext.isFunction(widget.onTaskCompleted)) {
            widget.onTaskCompleted.call(widget);
        }
    },
    
    /**
     * Logs that the taskflow is completed. Then, if the taskflow has a UI 
     * config and the config has disableOnComplete, each of the 
     * this_widgets is disabled.
     * @private */
    _onTaskflowCompleted: function(tf) {
        logger.logInfo("[TaskflowContainer] Taskflow completed: " + this.taskflow);
        
        var itf = RP.iget(tf, RP.interfaces.ITaskflow2);
        var uiCfg = itf.getTaskflowUIConfig();
        
        if (uiCfg && uiCfg.disableOnComplete) {
            this._widgets.each(function(widget) {
                widget.setEnabled(false);
            }, this);
        }
    },
    
    /**
     * @private
     * Activate the taskflowframe's widget container with the widgets
     * in this taskflow container
     */
    _activateWidgetSet: function(isNewActiveWidget) {
        if (this.useAnimations) {
            this._doAnimations(isNewActiveWidget);
        }
        else  {
            Ext.suspendLayouts();
            this._setActiveWidgetContainer();
            this._fireAppActivated();
            Ext.resumeLayouts(true);
        }
    },
    
    _onWidgetsLoaded: function() {
        this._widgetContainer = this._createWidgetContainer();
    },
    
    _createWidgetContainer: function() {
        var widgets = this._getWidgets(),
            widgetContainer;

        if(widgets.length === 1 && widgets[0].hideIfOnlyWidget) {
            widgetContainer = new RP.taskflow.widgetContainer.Empty();
        } else if(this.widgetContainerXtype) {
            widgetContainer = Ext.widget(this.widgetContainerXtype, {
                widgets: widgets
            });
        } else {
            widgetContainer = this._createDefaultWidgetContainer();
        }

        return widgetContainer;
    },

    _createDefaultWidgetContainer: function() {
        var cls = 'rp-widget-container rp-widget-container-y-scroll' + (Ext.isMobileSafari ? ' ' : '-hidden ') +
        (this.navRegion === 'east' ? 'rp-east-region' : '');

        return Ext.create('Ext.container.Container', {
            items: this._getWidgets(),
            overCls: 'rp-widget-container-y-scroll',
            cls: cls,
            listeners: {
                afterrender: this._afterWidgetCtRender
            }
        });
    },

    _getWidgets: function() {
        var widgets = this._widgets.getRange();
        widgets[widgets.length -1].addCls('last');

        return widgets;
    },

    _afterWidgetCtRender: function(widgetCt) {
        // Mostly a stylistic choice.
        widgetCt.el.unselectable();
    },

    /**
     * Activate the tasks for a taskflow.  This works a little different than the other taskflow
     * container because the active widget must be preserved on the taskflow container level
     * instead of the taskflow frame level.
     *
     * @param force the loading of a widget
     */
    activateTasks: function(forceActivation) {
        this.ensureTaskflowInitialized();
        if (forceActivation) {
            if (!this._activeWidget) {
                this.activateDefaultWidget();
            }
            else {
                this.onWidgetClick(this._activeWidget);
            }
        }
        
    },
    
    getText: function(){
        return this.text;
    },
    
    /**
     * @private
     */
    _doAnimations: function(isNewActiveWidget) {
        var layout = this.taskflowFrame.getWidgetContainer().getLayout(), 
            activeItem = layout.getActiveItem();
            
        if (activeItem !== this._widgetContainer) {
            this._fadeCurrentTaskform();
            //Fade and slide simultaneously
            this._doSlideInAnimation();
        }
        //If the user is just switching widgets just do the taskform fade 
        else if(isNewActiveWidget){
            this._fireActivateFadeInTaskform();
        }
        else {
            this._fireAppActivated();
        }
        
    },
    
    /**
     * @private
     * Fade in and slide 
     */
    _doSlideInAnimation: function() {
        var keyFrames = {}, duration = this.defaultAnimDuration,
        slideDirection = RP.globals.NAV_REGION === 'east' ? 'r' : 'l';
        
        this._setActiveWidgetContainer();
        //Don't show the "arrow" until the slide is finished
        this._activeWidget.setActive(false);
        
        this.taskflowFrame.getWidgetContainer().getEl().slideIn(slideDirection, {
            duration: duration
        });
        
        for (var i = 0; i <= 100; i += 25) {
            keyFrames[i] = {
                opacity: i / 100
            };
        }
        
        //Give the fade in look
        Ext.create('Ext.fx.Animator', {
            target: this.taskflowFrame.getWidgetContainer().getEl(),
            listeners: {
                afteranimate: this._onSideInAfteranimate,
                scope: this
            },
            duration: duration,
            keyframes: keyFrames
        });
    },
    
    /**
     * @private
     */
    _onSideInAfteranimate: function(){
        this._activeWidget.setActive(true);
        this._fireActivateFadeInTaskform();
    },
    
    /**
     * @private
     */
    _setActiveWidgetContainer: function() {
        //if this._widgetContainer is already active no layout is run
        this.taskflowFrame.getWidgetContainer().getLayout().setActiveItem(this._widgetContainer);
    },
    
    /**
     * @private
     */
    _fireAppActivated: function() {
        this.fireEvent('rpext-appactivated', this, this._activeWidget.taskForm);
    },
    
    /**
     * @private
     * Fire activate and fade in the newly activated taskform 
     */
    _fireActivateFadeInTaskform: function() {
        this._fireAppActivated();
        var taskform = this.taskflowFrame.getActiveTaskform();
        taskform.getEl().hide();
        taskform.getEl().show({
            duration: this.defaultAnimDuration,
            easing: 'ease-in'
        
        });
    },
    
    /**
     * @private
     */
    _fadeCurrentTaskform: function() {
        var currentTaskform = this.taskflowFrame.getActiveTaskform();
        if (currentTaskform) {
            currentTaskform.getEl().hide({
                duration: this.defaultAnimDuration
            });
            
        }
    }
});
//////////////////////
// ..\js\taskflow\TaskflowContainer.js
//////////////////////
/**
 * This is a base class that contains a taskflow.  A taskflow includes a list
 * of tasks, each of which is represented in the UI by a widget
 * ({@link RP.taskflow.BaseTaskflowWidget} base class). The responsibility of
 * this class is to handle the user-interface aspect of a taskflow. The
 * business logic of taskflows is handled by this class's ITaskflow instance
 * returned by getTaskflow().
 */
Ext.define("RP.taskflow.TaskflowContainer", {
    extend: "Ext.Component",
    alias: "widget.rptfcontainer",
    
     renderTpl: [
            '<div class = "rp-taskflowcontainer x-column">{text}</div>'
    ],
    
    
    noDefaultCss: true,
    
    baseCls: '',
    
    cls: 'rp-taskflow-prototype-taskflow-container',
    
    overCls: 'rp-taskflow-prototype-taskflow-container' + '-hover',
    
    mixins: {
        taskflowController: 'RP.taskflow.prototype.TaskflowContainerMixin'
    },
    
    initComponent: function() {
        this.renderData = {
            text: this.text
        };
        
        this.mixins.taskflowController.initComponent.call(this);
        
        this.callParent();
    }
});

// Validate interface.
RP.iimplement(RP.taskflow.TaskflowContainer, RP.interfaces.ITaskflowContainer2);
//////////////////////
// ..\js\overrides\Menu.js
//////////////////////
Ext.define('RP.menu.Menu', {
    override: 'Ext.menu.Menu',

    // override this method to prevent calling me.setPagePosition when me.show() is cancelled
    // by returning false on beforeshow.  That makes an empty 6px box show up aligned to your
    // show by cmp.
    showBy: function(cmp, pos, off) {
        var me = this;
        if (me.floating && cmp) {
            me.show();
            // Align to Component or Element using setPagePosition because normal show
            // methods are container-relative, and we must align to the requested element
            // or Component
            if (me.hidden !== true) {
                me.setPagePosition(me.el.getAlignToXY(cmp.el || cmp, pos || me.defaultAlign, off));
                me.setVerticalPosition();
            }
        }
        return me;
    }
});

Ext.define('Ext.menu.overrides.Item', {
    override: 'Ext.menu.Item',

    /**
     * @override
     * Added mobile support for menu items with submenus (since hover is not an option).
     * Clicking on such an item will not hide the menu, but instead display the submenu.
     */
    initComponent: function() {
        this.callParent(arguments);

        if(this.menu) {
            this.hideOnClick = false;
        }
    }
});
//////////////////////
// ..\js\overrides\Base.js
//////////////////////
Ext.define('RP.overrides.Base', {
    override: 'Ext.Base',

    /**
     * Check if the object has the given mixin
     * @param {Ext.Base} mixin The mixin to check for
     * @return {Boolean}
     */
    hasMixin: function(mixin) {
        var found = false,
            mixinName = mixin.$className,
            mixins = [],
            mixinsOwner = this,
            pushMixin = function(name, mixedIn) {
                mixins.push(mixedIn);
            };

        // We need to look up the inheritance chain and collect all the mixins.
        while (mixinsOwner) {
            Ext.Object.each(mixinsOwner.mixins, pushMixin);
            mixinsOwner = mixinsOwner.superclass;
        }
        
        Ext.Array.each(mixins, function(mixedIn) {
            // instanceof cannot check if the mixedIn object (which is a prototype)
            // is a direct instance of the passed in mixin constructor. It can only
            // check if any of the classes mixedIn extends from are instances of mixin.
            // Because of this we need to check the className as well.
            found = mixedIn.$className === mixinName || mixedIn instanceof mixin;
            return !found;
        });
        return found;
    }
});
//////////////////////
// ..\js\overrides\form\CheckboxGroup.js
//////////////////////
Ext.define('Ext.form.overrides.CheckboxGroup', {
    override: 'Ext.form.CheckboxGroup',

    cls: 'rp-checkbox-group',
    columns: 1,
    // Use vbox instead of checkboxgroup layout by default to 
    // ensure that all child items have the same width.
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    vertical: true,

    /**
     * @cfg {String} dirtyCls
     * The CSS class to use when the group is dirty.
     * Borrowed from Ext.form.field.Base#dirtyCls
     */
    dirtyCls: Ext.baseCSSPrefix + 'form-dirty',

    /**
     * @cfg {Boolean} [markDirty=false]
     * True to apply the component's dirtyCls when the field is dirty.
     */
    markDirty: false,

    /**
     * @private
     * Per design, checkboxes in a group should toggle on any click detected within
     * their borders, not just clicks over the inputEl & label
     */
    initEvents: function() {
        this.callParent(arguments);

        this.eachBox(function(checkbox, idx) {
            if (Ext.isFunction(checkbox.onBoxClick)) {
                checkbox.mun(checkbox.inputEl, 'click', checkbox.onBoxClick, checkbox);
                checkbox.mon(checkbox.el, 'click', checkbox.onBoxClick, checkbox);
            }
        }, this);
    },

    /**
     * Called when the field's dirty state changes. Adds/removes the #dirtyCls on the main element.
     * Overrides the emptyFn mixed in from Ext.form.field.Field.
     * @param {Boolean} isDirty True if the field's value has changed from its original value, false otherwise.
     */
    onDirtyChange: function(isDirty) {
        if (this.markDirty) {
            this[isDirty ? 'addCls' : 'removeCls'](this.dirtyCls);
        }
    }
});
//////////////////////
// ..\js\overrides\prototype\style\Basic.js
//////////////////////
/**
 * Style overrides
 */
(function() {
    var defaults = {
        headerHeight: 36,
        tabbarHeight: 30,
        buttonHeight: 20 + 2, //1px border
        checkcolumnWidth: 40,
        datePickerWidth: 280,
        separatorMargin: '-2 0 -2 0',
        columnHeaderBorderCls: Ext.baseCSSPrefix + 'column-double-border'
    };

    Ext.override(Ext.panel.Header, {
        minHeight: defaults.headerHeight
    });

    Ext.override(Ext.menu.Separator, {
        margin: defaults.separatorMargin
    });

    Ext.override(Ext.picker.Date, {
        showToday: false,
        width: defaults.datePickerWidth
    });

    Ext.override(Ext.selection.CheckboxModel, {
        headerWidth: defaults.checkcolumnWidth
    });

    Ext.override(Ext.tab.Bar, {
        height: defaults.tabbarHeight,
        defaults: {
            margin: '0 3 0 0'
        }
    });

    Ext.override(Ext.grid.header.Container, {
        height: defaults.headerHeight
    });

    Ext.button.Button.addStatics({
        /**
         * @static
         * @member Ext.button.Button
         * An enumeration of valid priority values for buttons.
         * HIGH, MEDIUM, and LOW are valid. A HIGH priority button
         * will be blue. A MEDIUM priority button has no special
         * styles. A LOWE priority button has no border and is
         * transparent aside from its text and icon.
         */
        priority: {
            HIGH: 'high',
            MEDIUM: 'medium',
            LOW: 'low'
        }
    });

    Ext.override(Ext.button.Button, {
        /**
         * @cfg {Boolean} useShadow True to add #shadowCls to the button,
         * false to omit it. If undefined, shadowCls will only be added if #cls is also empty.
         */

        /**
         * @cfg {String} shadowCls
         * The css class applied to this button's shadow.
         */
        shadowCls: 'rp-btn-shadow',

        menuAlign: 'tr-tr?',

         /**
         * @cfg {String} priority Sets the importance of the button which affects
         * how the button looks. See {@link Ext.button.Button.priority} for more
         * information.
         */
        priority: Ext.button.Button.priority.MEDIUM,

        constructor: function() {
            this.callParent(arguments);

            if (this.baseCls === (Ext.baseCSSPrefix + 'btn')) {
            //Only override the default height if the button is using the
            //x-btn cls and the height is not defined.
                if (!Ext.isDefined(this.height)) {
                    this.height = defaults.buttonHeight;
                }

                if (this.useShadow === true || (this.useShadow !== false && !Ext.isDefined(this.cls))) {
                    this.addCls(this.shadowCls);
                }
            }

            this.setPriority(this.priority);
        },

        /**
         * Sets the priority styling on the button. See {@link Ext.button.Button.priority}
         * for more information.
         */
        setPriority: function(priority) {
            this.priority = priority;
            switch (priority) {
                case Ext.button.Button.priority.HIGH:
                    this.addCls('rp-important-btn');
                    this.removeCls('rp-unimportant-btn');
                    break;
                case Ext.button.Button.priority.LOW:
                    this.addCls('rp-unimportant-btn');
                    this.removeCls('rp-important-btn');
                    break;
                default:
                    this.removeCls('rp-unimportant-btn');
                    this.removeCls('rp-important-btn');
                    break;
            }
        },

        /*
         * Overrides for button menus
         */
        initComponent: function() {
            var menu = this.menu;
            if (menu) {
                this.menu = this._buildMenu(menu || {});
                this.addCls('rp-menu-btn');
            }
            this.callParent(arguments);
        },

        _buildMenu: function(menuCfg) {
            if (Ext.isString(menuCfg) || menuCfg.isMenu) {
                return menuCfg;
            }
            var cfg = this._buildDefaultMenuConfig();

            if (Ext.isArray(menuCfg)) {
                cfg.items = menuCfg;
            }
            else {
                cfg = Ext.applyIf(menuCfg, cfg);
            }
            return cfg;
        },

        _buildDefaultMenuConfig: function() {
            return {
                cls: 'rp-menu-btn-menu',
                showSeparator: false,
                dockedItems: this._buildMenuToolbar()
            };
        },

        _buildMenuToolbar: function() {
            return {
                xtype: 'toolbar',
                height: 20,
                dock: 'top',
                cls: 'rp-menu-btn-toolbar',
                items: ['->', this._buildCloseButton()]
            };
        },

        _buildCloseButton: function() {
            return {
                xtype: 'button',
                iconCls: 'rp-menu-close-icon',
                listeners: {
                    click: function() {
                        this.menu.hide();
                    },
                    scope: this
                }
            };
        }
    });

    Ext.define('Ext.grid.column.overrides.Column', {
        override: 'Ext.grid.column.Column',

        /**
         * Change the render tpl of column to put the pipe borders
         */
        renderTpl:
        '<div id="{id}-titleEl" class="' + Ext.baseCSSPrefix + 'column-header-inner">' +
            '<span id="{id}-textEl" class="' + Ext.baseCSSPrefix + 'column-header-text">' +
                '{text}' +
            '</span>' +
            '<tpl if="!menuDisabled">'+
                '<div id="{id}-triggerEl" class="' + Ext.baseCSSPrefix + 'column-header-trigger"></div>'+
            '</tpl>' +
            '<div class= "' +  defaults.columnHeaderBorderCls + '"></div>'+
        '</div>' +
        '{%this.renderContainer(out,values)%}',

        /**
         * @cfg {boolean} [useDefaultAlignment=true]
         * True to respect default column styles defined in RPWeb.
         * For custom columns, can be set to false to override defaults.
         * (e.g. Prevent a column with a numeric backend from being right-aligned)
         */
        useDefaultAlignment: true,

        /**
         * @cfg {String} [overCls=x-column-header-component-over]
         * Sencha applies a hoverCls to the inner el instead of the usual overCls to the component
         * element. We need to have an overCls applied to the component element in order to control
         * the behavior of the double-borders, which are styled based on a mouseover on the adjacent
         * element. Clearing this value will prevent a mouseover on this column from affecting the
         * display of the adjacent border.
         */
        overCls: Ext.baseCSSPrefix + 'column-header-component-over',

        /**
         * @override
         */
        beforeRender: function() {
            this.callParent(arguments);

            this._setAlignmentByType();
        },

        /**
         * @private
         * Optionally updates a column's alignment based on the type
         * of data that will be displayed in it.
         */
        _setAlignmentByType: function() {
            var grid = this.up('tablepanel');

            // By default, numeric columns should be right-aligned.
            if(grid) {
                var fields = grid.store.model.prototype.fields || grid.store.fields,
                    colField = fields.getByKey(this.dataIndex);

                // In theory, if this.align is already 'right',
                // we shouldn't need to run anything inside of this conditional.
                if(this.useDefaultAlignment && colField &&
                        this._isTypeNumeric(colField.type) && this.align !== 'right') {
                    this.removeCls(Ext.baseCSSPrefix + 'column-header-align-' + this.align);
                    this.align = 'right';
                    this.addCls(Ext.baseCSSPrefix + 'column-header-align-' + this.align);
                }
            }
        },

        /**
         * @private
         * Returns a boolean indicating whether a specified type is considered numeric
         *
         * @param {Ext.data.Types} A type value to check
         */
        _isTypeNumeric: function(type) {
            return (type === Ext.data.Types.INT ||
                    type === Ext.data.Types.FLOAT ||
                    type === Ext.data.Types.NUMBER);
        },

        /**
         * @override
         * @version 4.1.0
         * http://redwiki.redprairie.com/display/UXD/Grid,+Column+Sorting
         * Columns can no longer be sorted by clicking the header.
         * Menu arrow only shows on hover (desktop) or click (tablet).
         */
        onElClick: function(e, t) {
            if (this.triggerEl) {
                this.triggerEl.addCls('rp-column-header-trigger-selected');
            }

            this._showColumnMenu(e, t);
        },

        /**
         * @private
         */
        _showColumnMenu: function(e, t) {
            var ownerHeaderCt = this.getOwnerHeaderCt(),
                triggerEl = this.triggerEl,
                isFirst = this._isFirstColumnWithMenu(),
                isOnEdge = this.isOnLeftEdge(e) || this.isOnRightEdge(e),
                menu;

            if (ownerHeaderCt && !ownerHeaderCt.ddLock) {
                // Display the menu on any non-edge header click.
                if (triggerEl && !isOnEdge) {
                    menu = ownerHeaderCt.getMenu();
                    // The menu should be left-aligned for the first column and
                    // right aligned for every subsequent column.
                    menu.defaultAlign = isFirst ? 'tl-bl?' : 'tr-br?';
                    ownerHeaderCt.onHeaderTriggerClick(this, e, triggerEl.dom);

                    // If the column menu would overflow the viewport edge, Ext will slide it
                    // over only as much as necessary to keep it on the screen.
                    // We'd rather have the corners line up, so swap the default align to match
                    // right corners and reposition the column menu.
                    // If it still doesn't line up at this point, you're on your own.
                    if (isFirst && menu.getEl().getLeft() !== triggerEl.getLeft()) {
                        menu.setPagePosition(menu.el.getAlignToXY(triggerEl.dom, 'tr-br?'));
                    }
                }
                else if (e.getKey() || !isOnEdge) {
                    ownerHeaderCt.onHeaderClick(this, e, t);
                }
            }
        },

        /**
         * @private
         * Returns a boolean indicating if this column is the leftmost
         * visible item within its header that has a menu.
         * Includes special pleading for grids with lockable columns, where
         * "first" needs to consider columns from both child grids (locked & normal).
         */
        _isFirstColumnWithMenu: function() {
            var columns, first,
                headerCt = this.getOwnerHeaderCt(),
                ownerGrid = this.up('gridpanel');

            if (Ext.isFunction(ownerGrid.ownerCt.getView)) {
                var parentView = ownerGrid.ownerCt.getView(),
                    lockedView = parentView.lockedView,
                    normalView = parentView.normalView;

                if(this.locked) {
                    // Performance!
                    // Locked columns are always on the left, so the normalGrid's
                    // items can be ignored.
                    columns = headerCt.items.getRange();
                }
                else {
                    columns = Ext.Array.merge(
                        lockedView.getHeaderCt().items.getRange(), headerCt.items.getRange());
                }
            }
            else {
                columns = headerCt.items.getRange();
            }

            Ext.Array.each(columns, function(col, idx) {
                if (col.isVisible() && !col.menuDisabled) {
                    first = col;
                    return false; // Break
                }
            }, this);

            return this === first;
        }
    });

    Ext.define('Ext.grid.header.overrides.Container', {
        override: 'Ext.grid.header.Container',

        /**
         * @override
         * @version 4.1.0
         * http://redwiki.redprairie.com/display/UXD/Grid,+Column+Sorting
         * Hide the menu arrow when the menu deactivates.
         */
        onMenuDeactivate: function() {
            this.callParent(arguments);

            var menu = this.getMenu(),
                activeTrigger = menu.activeHeader.triggerEl;

            if (activeTrigger) {
                activeTrigger.removeCls('rp-column-header-trigger-selected');
            }
        },

        /**
         * @inheritdoc
         */
        showMenuBy: function(t, header) {
            this.callParent(arguments);
            // The menu is shared across all columns, but the active
            // sort is not, so we have to update the menu item selected
            // state every time the menu is shown.
            this._updateActiveSort();
        },

        /**
         * @private
         * Applies a selected sort css to a sort menu item's iconEl, where appropriate.
         */
        _updateActiveSort: function() {
            var menu = this.getMenu(),
                col = menu.activeHeader,
                isActiveSort;

            Ext.Array.each(menu.items.getRange(), function(item, idx) {
                if (item.iconEl) {
                    isActiveSort = (item.itemId === 'ascItem' && col.sortState === 'ASC') ||
                            (item.itemId === 'descItem' && col.sortState === 'DESC');

                    item[isActiveSort ? 'addCls' : 'removeCls']('rp-menu-item-active-sort');
                }
            }, this);
        }
    });

    Ext.define('Ext.panel.overrides.Tool', {
        override: 'Ext.panel.Tool',

        height: 22,
        width: 22
    });

    Ext.override(Ext.form.field.ComboBox, {
        // The default config of 'sides' isn't what the designers want.
        defaultListConfig: Ext.applyIf({
            shadow: 'frame'
        }, Ext.form.field.ComboBox.prototype.defaultListConfig)
    });

    Ext.override(Ext.LoadMask, {
        shadow: false
    });

    Ext.define('Ext.util.overrides.Floating', {
        override: 'Ext.util.Floating',

        constructor: function(dom) {
            // private
            this._origShadow = this.shadow;
            // Remove Ext's default shadow handling, which builds separate shadow divs
            // and positions them behind the floating components.
            this.shadow = false;

            this.callParent(arguments);

            // Add classes for a default box shadow, but only
            // if one was originally requested.
            if (this._origShadow) {
                this.on('afterrender', function(cmp) {
                    cmp.addCls('rp-floating rp-shadow-' + (Ext.isString(this._origShadow) ?
                        this._origShadow.toLowerCase() : 'sides'));
                });
            }
        }
    });
} ());
//////////////////////
// ..\js\overrides\prototype\style\ExpandingRow.js
//////////////////////
Ext.override(Ext.grid.header.Container, {
    /**
     * @cfg flexLastColumnToFit {Boolean}
     * True to flex the last column to fit the grid if the view rows 
     * aren't wide enough.
     * 
     */
    flexLastColumnToFit: true,
    
    afterLayout: function() {
        this.callParent(arguments);
        var visibleColumns, lastColumn;
        if (this.flexLastColumnToFit && !this._isVisibleColumnFlexed() && this.view.getWidth() > this.getFullWidth()) {
            visibleColumns = this.getVisibleGridColumns(true);
            lastColumn = visibleColumns[visibleColumns.length - 1];
            
            if (lastColumn) {
                lastColumn.flex = 1;
                this.doLayout();
                delete lastColumn.flex;
            }
        }
    },
    
    /**
     * @private
     */
    _isVisibleColumnFlexed: function() {
        return !Ext.Array.every(this.getVisibleGridColumns(true), function(column) {
            return !column.flex;
        });
    }
    
});
//////////////////////
// ..\js\overrides\prototype\style\GridMenu.js
//////////////////////
Ext.override(Ext.grid.header.Container, {
    menuCls: 'rp-menu-grid-menu',
    
    //remove icon css classes and line divder, add menu cls and remove separator 
    getMenuItems: function() {
        var me = this, menuItems = [], hideableColumns = me.enableColumnHide ? me.getColumnMenu(me) : null;
        
        if (me.sortable) {
            menuItems = [{
                itemId: 'ascItem',
                text: me.sortAscText,
                handler: me.onSortAscClick,
                scope: me
            }, {
                itemId: 'descItem',
                text: me.sortDescText,
                handler: me.onSortDescClick,
                scope: me
            }];
        }
        if (hideableColumns && hideableColumns.length) {
            menuItems.push( {
                iconCls: Ext.baseCSSPrefix + 'cols-icon',
                itemId: 'columnItem',
                text: me.columnsText,
                menu: {
                    showSeparator: false,
                    cls: this.menuCls,
                    items: hideableColumns
                }
            });
        }
        return menuItems;
    },
    
    //add menu cls and remove separator
    getMenu: function(){
        var isMenuCreated = !!this.menu, menu = this.callParent();

        if(!isMenuCreated){
            menu.shadow = 'frame';
            menu.addCls(this.menuCls);
            menu.showSeparator = false;
        }
        
        menu.items.each(function(item, index, items) {
            if(item instanceof Ext.menu.Separator){
                this.menu.remove(item);
            }
        }, this);
        
        return menu;
    },
    
    //add menushow event 
    showMenuBy: function(t, header){
        this.callParent(arguments);
        this.fireEvent('menushow',this, this.menu, header);
    }
});

Ext.override(Ext.grid.column.Column, {
    triggerPressedCls: Ext.baseCSSPrefix + 'column-header-trigger-pressed',
    triggerHoverCls: Ext.baseCSSPrefix + 'column-header-trigger-over',
    /**
     * @property {Boolean} true if the column is in a pressed state.
     */
    pressed: false,
    
    initComponent: function(){
        this.callParent();
        this.on('afterrender', this._onAfterRender, this);
    },
    
    //private
    _onAfterRender: function() {
        var headerContainer = this.getOwnerHeaderCt();
        
        if(this.triggerEl) {
            this.triggerEl.hover(this._addTriggerHoverCls, this._removeTriggerHoverCls, this);
        }

        if(this.menuDisabled) {
            this.addCls(Ext.baseCSSPrefix + 'column-menu-disabled');
        }
        
        //headerCt does not exist in init component, listen to the headerCt afterrender
        headerContainer.on('menushow', this._onHeaderCtMenuShow, this);
        headerContainer.on('menucreate', function() {
            headerContainer.menu.on('hide', this._onMenuHide, this);
        }, this);
    },
    
    //private
    _onHeaderCtMenuShow: function(headerContainer, menu, column) {
        var wasPressed = this.pressed;
        this.pressed = this === column;
        
        if (wasPressed && !this.pressed) {
            this.triggerEl.removeCls(this.triggerPressedCls);
        }
        else if (this.pressed) {
            this.triggerEl.addCls(this.triggerPressedCls);
        }
    },
    
    //private
    _onMenuHide: function() {
        var wasPressed = this.pressed;
        this.pressed = false;
        
        if (wasPressed) {
            this.triggerEl.removeCls(this.triggerPressedCls);
        }
    },
    
    //private
    _addTriggerHoverCls: function(){
        this.triggerEl.addCls(this.triggerHoverCls);
    },
    
    //private
    _removeTriggerHoverCls: function(){
        this.triggerEl.removeCls(this.triggerHoverCls);
    }
});
//////////////////////
// ..\js\overrides\prototype\style\Time.js
//////////////////////
Ext.override(Ext.form.field.Time, {
    pickerAlign: 'tr-br',
    minWidth: 110,
    useDefaultCls: true,
    initComponent: function() {
        if(this.useDefaultCls) {
            this.addCls('rp-field-time rp-field-right-align');
        }

        this.callParent();
    }
});
//////////////////////
// ..\js\overrides\prototype\style\ToolbarButtonSpacing.js
//////////////////////
Ext.override(Ext.toolbar.Toolbar, {
    /**
     * @cfg useSpacingDefaults {Boolean} true to get default spacing 
     * according to the style guide for toolbars in panels.
     */
    useSpacingDefaults: false,
    initComponent: function() {
        if (this.useSpacingDefaults) {
            Ext.Object.merge(this, this._getSpacingDefaults());
        }
        this.callParent(arguments);
    },
    //private
    _getSpacingDefaults: function() {
        /**
         * According the the style guide there should be an 8px top and bottom padding.
         * 
         * for bottom bars : The first item should be 16px from the edge of the container every 
         * preceding item should be 8px.
         * 
         * for top bars : 8px to the left on everything
         */
        
        
        return {
            padding: this.dock === 'bottom' ? '8 0 8 8' : '8 0',
            defaults: {
                margin: '0 0 0 8'
            }
        };
    }
});
//////////////////////
// ..\js\overrides\prototype\bugs\ConfigedHeaderWithNoTitle.js
//////////////////////
/**
 * @extBug
 * @version 4.1.0
 */
Ext.define('Ext.panel.overrides.Panel', {
    override: 'Ext.panel.Panel', 
    //OVERRIDE Copy paste method from 4.1.3 which adds a nastier conditional which fixes the bug.
    updateHeader: function(force) {
        var me = this,
            header = me.header,
            title = me.title,
            tools = me.tools,
            icon = me.icon || me.iconCls,
            vertical = me.headerPosition == 'left' || me.headerPosition == 'right';

        if (Ext.isObject(header) || (header !== false && (force || (title || icon) || (tools && tools.length) || (me.collapsible && !me.titleCollapse)))) {
            if (header && header.isHeader) {
                header.show();
            } else {
                // Apply the header property to the header config
                header = me.header = Ext.widget(Ext.apply({
                    xtype       : 'header',
                    title       : title,
                    titleAlign  : me.titleAlign,
                    orientation : vertical ? 'vertical' : 'horizontal',
                    dock        : me.headerPosition || 'top',
                    textCls     : me.headerTextCls,
                    iconCls     : me.iconCls,
                    icon        : me.icon,
                    baseCls     : me.baseCls + '-header',
                    tools       : tools,
                    ui          : me.ui,
                    id          : me.id + '_header',
                    indicateDrag: me.draggable,
                    frame       : (me.frame || me.alwaysFramed) && me.frameHeader,
                    ignoreParentFrame : me.frame || me.overlapHeader,
                    ignoreBorderManagement: me.frame || me.ignoreHeaderBorderManagement,
                    listeners   : me.collapsible && me.titleCollapse ? {
                        click: me.toggleCollapse,
                        scope: me
                    } : null
                }, me.header));
                me.addDocked(header, 0);

                // Reference the Header's tool array.
                // Header injects named references.
                me.tools = header.tools;
            }
            me.initHeaderAria();
        } else if (header) {
            header.hide();
        }
    }
});
//////////////////////
// ..\js\overrides\prototype\LastChildCls.js
//////////////////////
/**
 * Add last ability to have a lastChildCls which gets added
 * to the containers last item.
 */
Ext.define('Ext.container.overrides.Container', {
    override: 'Ext.container.Container',
    /**
     * @cfg {String} lastChildCls
     * If not falsy add the lastChildCls css class will be added to the 
     * containers last item, hidden or not.  Remember the last-child selector
     * is not supported in IE8.
     */
    lastChildCls: '',

    initComponent: function() {
        if(this.lastChildCls) {
            this.on('add', this._$onLastChildAdd, this);
        }
        this.callParent();
    },

    _$onLastChildAdd: function(container, child) {
        var oldLast = this._$lastChild,
            currentLast = this.items.last();

        if(child === currentLast && child !== oldLast) {
            if(oldLast) {
                oldLast.removeCls(this.lastChildCls);
            }

            this._$lastChild = child;
            this._$lastChild.addCls(this.lastChildCls);
        }
    }
});
//////////////////////
// ..\js\overrides\DatePicker.js
//////////////////////
Ext.override(Ext.picker.Date, {
    //identical to Ext's createMonthPicker method.  Replaces
    //their month picker with ours
    createMonthPicker: function(){
        var me = this,
            picker = me.monthPicker;

        if (!picker) {
            me.monthPicker = picker = Ext.create('RP.picker.Month', {
                renderTo: me.el,
                floating: true,
                shadow: false,
                hidden: true,
                listeners: {
                    scope: me,
                    okclick: me.onOkClick
                }
            });
            if (!me.disableAnim) {
                //hide the element if we're animating to prevent an initial flicker
                picker.el.setStyle('display', 'none');
            }
        }

        return picker;
    },

    /**
     * Change the position that is being set in the original
     * method to make it look like the design.  Remove 
     * the animation.
     */
    showMonthPicker : function(){
        var ret = this.callParent([false]);
        if(this.monthPicker) {
            this._alignMonthPicker();
        }
        return ret;
    },

    //The design calls to show the month picker over the picker's
    //calendar.  this.eventEl is the calendar element.
    _alignMonthPicker: function() {
        this.monthPicker.setPosition(5,38);
        this.monthPicker.setSize(this.eventEl.getSize());
    }
});

Ext.override(Ext.form.field.Date, {
    /**
     * @override
     * Prevent the picker from collapsing when the combobox
     * boundlists recieve focus
     */
    collapseIf: function(e){
        if (!this.isDestroyed && !e.within(this.bodyEl, false, true) && !e.within(this.picker.el, false, true) && !this.isEventWithinPickerLoadMask(e)) {
            var shouldCollapse = true;

            if(this.picker.monthPicker){
                if(!this.picker.monthPicker.isAnythingExpanded()){
                    this.collapse();
                }
            }else if(!this.picker.monthPicker){
                this.collapse(); 
            }
        }
    }
});
//////////////////////
// ..\js\search\models\SearchAPIField.js
//////////////////////
Ext.define('RP.search.model.SearchAPIField', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'fieldName',        type: 'string' },
        { name: 'fieldLabel',       type: 'string' },
        { name: 'fieldDescription', type: 'string' },
        { name: 'fieldExample',     type: 'string' }
    ],

    hasMany  : {model: 'RP.search.model.SearchAPIResource', name: 'resources'},

    proxy: {
        type: 'ajax',
        url: '/data/search_api',
        reader: {
            type: 'json',
            root: 'data.results'
        }
    }
});
//////////////////////
// ..\js\search\models\SearchAPIResource.js
//////////////////////
Ext.define('RP.search.model.SearchAPIResource', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'resourceName',  type: 'string' },
        { name: 'resourceLabel', type: 'string' }
    ],
    belongsTo: 'RP.search.model.SearchAPIField'
});
//////////////////////
// ..\js\search\models\SearchCategory.js
//////////////////////
/**
 * Search category which user can select to limit searches.
 */
Ext.define('RP.search.model.SearchCategory', {
    extend: 'Ext.data.Model',

    fields: [{
        name: 'categoryType',
        type: 'string'
    }, {
        name: 'categoryLabel',
        type: 'string'
    }, {
        name: 'idField',
        type: 'string'
    }, {
        name: 'data'
    }, {
        name: 'resultCount',
        type: 'int'
    }, {
        name: 'totalCount',
        type: 'int'
    }, {
        name: 'headerTemplate',
        type: 'string'
    }, {
        name: 'bodyTemplate',
        type: 'string'
    }, {
        name: 'detailsTemplate',
        type: 'string'
    }, {
        name: 'detailsUrl',
        type: 'string'
    }, {
        name: 'supportMultiSelect',
        type: 'boolean'
    }, {
        name: 'plugins'
    }],

    hasMany: [
        {model: 'RP.search.model.SearchResultFlow', name: 'flow'}
    ],

    proxy: {
        type: 'ajax',
        url: '/data/search',
        reader: {
            type: 'json',
            root: 'data.results'
        }
    }
});
//////////////////////
// ..\js\search\models\SearchHistory.js
//////////////////////
/**
 * Search category which user can select to limit searches.
 */
Ext.define('RP.search.model.SearchHistory', {
    extend: 'Ext.data.Model',

    fields: [{
        name: 'query',
        type: 'string'
    }, {
        name: 'date',
        type: 'date'
    }, {
        name: 'category',
        type: 'string'
    }, {
        name: 'categoryLabel',
        type: 'string'
    }, {
        name: 'totalCount',
        type: 'number'
    }]
});
//////////////////////
// ..\js\search\models\SearchResultFlow.js
//////////////////////
Ext.define('RP.search.model.SearchResultFlow', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'moduleName' },
        { name: 'taskflowName' },
        { name: 'taskId' },
        { name: 'moduleLabel' },
        { name: 'taskflowLabel' },
        { name: 'taskLabel' },
        { name: 'validation'}
    ],

    belongsTo: 'RP.search.model.SearchCategory'
});
//////////////////////
// ..\js\search\field\ComboBox.js
//////////////////////
/**
 * Combobox that will handle Search Result models
 */
Ext.define('RP.search.field.ComboBox', {
    extend: 'Ext.form.field.ComboBox',

    valueField: 'queryString',
    displayField: 'queryString',

    minChars: 2,
    forceSelection: false,
    identifierField: 'identifier',

    matchFieldWidth: true,

    pickerAlign: 'tl-bl',
    pickerOffset: [0, 6],
    triggerWidth: 25,

    validateOnBlur: false,

    clearOnSearch: false,
    cls: 'rp-search-combo-box',
    triggerCls: 'rp-search-btn',

    autoSelect: false,

    initComponent: function() {
        this.addEvents(
            /**
             * Fired when criteria is selected by the user to be added to the filter.
             * @param {RPUX.filtering.field.FilterComboBox} this
             * @param {Ext.data.Model} criterion The record specifying the criterion to add.
             */
            'search',
            'requestsearchapi'
        );

        this.store = this._getStore();

        this._buildListConfig();
        this.callParent(arguments);

        this.on({
            select: this._onSelect,
            specialkey: this._onEnterKeyInFilterBox,
            search: this._onSearch,
            focus: this._onComboFocus,
            blur: this._onComboBlur,
            scope: this
        });

        this.initialWidth = this.width;
    },

    _onEnterKeyInFilterBox: function(combo, e) {
        if (e.getKey() === e.ENTER) {
            var highlightedItem;
            if(combo.listKeyNav && combo.listKeyNav.boundList) {
                highlightedItem = combo.listKeyNav.boundList.highlightedItem;
            }

            // let the select handler take care of this
            if (combo.isExpanded && highlightedItem) {
                return;
            }

            if(this.store.isLoading() === true) {
                this.cancelQuery = true;
            }

            this.fireEvent('search', this, undefined);

            if(this.clearOnSearch === true) {
                this.reset();
            }
            this.collapse();
        }
    },

    setWidth: function(number) {

        this.currentWidth = number;

        this.stopAnimation();
        this.animate({
            to: {
                width: number
            },
            duration: 500,
            easing: 'ease',
            dynamic: true,
            listeners: {
                afterAnimate: this._afterAnimate,
                scope: this
            }
        });
    },

    _afterAnimate: function() {
        if(this.hasFocus === true) {
            if(RP.search.field.ComboBox.searchHistory && RP.search.field.ComboBox.searchHistory.length > 0) {
                this._getHistoryStore().loadData(RP.search.field.ComboBox.searchHistory);
            }
            else {
                this._getHistoryStore().loadData([]);
            }
            this.getPicker().refresh();

            this.expand();
        }
    },

    /**
     * @private
     * Build the list config object.
     */
    _buildListConfig: function() {
        this.listConfig = Ext.applyIf(this.listConfig || {}, {
            tpl: this._buildDropDownTpl(),
            cls: 'search-result-list',
            listeners: {
                afterrender: this._listConfigAfterRender,
                scope: this
            }
        });
    },

    _listConfigAfterRender: function() {
        this.getPicker().getEl().on('click', function() {
            this.collapse();
            this.reset();

            this.fireEvent('requestsearchapi', this, undefined);
        }, this, {delegate: '.rp-search-api-help'});
    },

    /**
     * @private
     * Build the drop down template.
     */
    _buildDropDownTpl: function() {
        this.resultsTpl = Ext.create('Ext.XTemplate',
            '<ul><tpl for=".">',
                '<li role="option" class="x-boundlist-item">',
                    '<span class="search-category-query">{queryString}</span> ',
                    '<span class="search-category-result">{[this.getItemText(values)]}</span>',
                '</li>',
            '</tpl></ul>',
            '{[this.getAPIHelpTemplate()]}', {
            compiled: true,
            getItemText: function(values) {
                var returnValue = '';
                if(values.totalCount) {
                    returnValue += '<span class="count">' + values.totalCount +
                        '</span> ' + RP.getMessage('rp.common.misc.in') + ' ';
                }
                returnValue += values.categoryLabel;

                return returnValue;
            },
            getAPIHelpTemplate: this.getAPIHelpTemplate
        });
        return this.resultsTpl;
    },

    _getStore: function() {
        if(!this.categoryOptionStore) {
            this.categoryOptionStore = Ext.create('Ext.data.Store', {
                model: 'RP.search.model.SearchCategory',
                autoLoad: false,
                listeners: {
                    load: this._onStoreLoad,
                    scope: this
                }
            });
        }

        return this.categoryOptionStore;
    },

    _onStoreLoad: function() {
        if(this.cancelQuery === true) {
            this.cancelQuery = false;

            if (this.isExpanded) {
                this.collapse();
            }

            return;
        }

        this.store.suspendEvents();

        this.store.each(function(record) {
            record.set({
                queryString: this.lastQueryString
            });
        }, this);

        if(this.picker) {
            this.picker.refresh();
        }

        this.store.resumeEvents();

        this.expand();
    },

    /**
     * @private call the record onSelect method if there is one
     */
    _onSelect: function(me, records) {
        this.fireEvent('search', this, records);

        this.collapse();

        if(this.clearOnSearch === true) {
            this.reset();
        }
    },

    _onSearch: function(combo, records) {

        var query;
        if(records && records[0] && records[0].store.model.$className === 'RP.search.model.SearchHistory') {
            query = records[0].get('query');
        } else {
            query = combo.getValue();
        }

        var category, categoryLabel;
        if(records !== undefined && records[0] !== undefined) {
            category = records[0].get('categoryType');
            categoryLabel = records[0].get('categoryLabel');
        }

        this._addToHistory(query, category, categoryLabel);
    },

    _addToHistory: function(queryString, category, categoryLabel) {
        if(!RP.search.field.ComboBox.searchHistory) {
            RP.search.field.ComboBox.searchHistory = [];
        }

        var history;
        Ext.each(RP.search.field.ComboBox.searchHistory, function(nextHistory) {
            if(nextHistory.query === queryString && nextHistory.category === category) {
                history = nextHistory;
            }
        });

        if(history) {
            history.date = new Date();
        } else {
            RP.search.field.ComboBox.searchHistory.push({
                query: queryString,
                date: new Date(),
                category: category,
                categoryLabel: categoryLabel
            });
        }
    },

    _getHistoryTpl: function() {
        if(!this.historyTpl) {
            this.historyTpl = Ext.create('Ext.XTemplate',
                '<ul><tpl for=".">',
                    '<li role="option" class="x-boundlist-item">',
                        '<span class="search-history-query">{query}</span> ',
                        '<span class="search-history-result">{[this.getItemText(values)]}</span>',
                    '</li>',
                '</tpl></ul>',
                '{[this.getAPIHelpTemplate()]}', {
                    compiled: true,
                    getItemText: function(values) {
                        var returnValue = '';
                        if(values.totalCount) {
                            returnValue += '<span class="count">' + values.totalCount +
                                '</span> ' + RP.getMessage('rp.common.misc.in') + ' ';
                        }
                        if(Ext.isEmpty(values.category)) {
                            returnValue += RP.getMessage('rp.common.search.AllCategory');
                        } else {
                            returnValue += values.categoryLabel || values.category;
                        }

                        return returnValue;
                    },
                    getAPIHelpTemplate: this.getAPIHelpTemplate
                }
            );
        }
        return this.historyTpl;
    },

    getAPIHelpTemplate: function() {
        if(!this.apiHelpTemplate) {
            this.apiHelpTemplate = Ext.create('Ext.XTemplate',
                '<div class="rp-search-api-help">{[RP.getMessage("rp.common.search.HelpText")]}</div>');
        }

        return this.apiHelpTemplate.apply();
    },

    _getHistoryStore: function() {
        if(!this.historyStore) {
            this.historyStore = Ext.create('Ext.data.Store', {
                model: 'RP.search.model.SearchHistory',
                sorters: [{
                    property: 'date',
                    direction: 'DESC'
                }]
            });
        }
        return this.historyStore;
    },

    _onComboFocus: function() {
        this.hasFocus = true;

        this.getPicker().tpl = this._getHistoryTpl();
        this.getPicker().store = this._getHistoryStore();
        this.getPicker().updateLayout();

        if(this.focusWidth && this.focusWidth !== this.initialWidth) {
            this.setWidth(this.focusWidth);
        }

        if(this.cls) {
            this.addCls(this.cls + '-active');
        }

        if(this.triggerCls) {
            this.triggerEl.addCls(this.triggerCls + '-active');
        }
    },

    _onComboBlur: function() {
        this.hasFocus = false;
        this.setWidth(this.initialWidth);

        if(this.cls) {
            this.removeCls(this.cls + '-active');
        }

        if(this.triggerCls) {
            this.triggerEl.removeCls(this.triggerCls + '-active');
        }
    },

    /**
     * doQuery has been completely overridden. It takes the raw
     * string, parses out all the possible values, determines which
     * columns support those values and then displays a list of
     * values and the columns that may be used to filter results.
     */
    doQuery: function(queryString, forceAll, rawQuery) {
        this.store.suspendEvents();

        if(Ext.isEmpty(queryString)) {
            this.collapse();
            this.store.loadData([]);
            return;
        }
        else {
            this.getPicker().tpl = this.resultsTpl;
            this.getPicker().store = this._getStore();
            this.getPicker().updateLayout();
            this.lastQueryString = queryString;

            // Make sure this is set. Ext does some onLoad processing in ComboBox
            // that could potentially clear the field after a remote query if this is empty.
            this.rawQuery = rawQuery;

            var params = this.getParams(queryString);

            this.store.load({ params: params});
        }

        this.store.resumeEvents();
        this.expand();
    },

    // @inheritdoc
    getParams: function(queryString) {
        return {
            query: RP.search.StringParser.parseSearchString(queryString),
            rawQuery: queryString,
            categoryOnly: true
        };
    },

    onListSelectionChange: function() {
        var inputEl = this.inputEl,
            focusFn = inputEl.focus;

        // The superclass logic calls me.inputEl.focus() as the
        // very last thing it does. Refocusing the inputEl causes
        // the dropdown to reappear after the combobox has already
        // blurred because of all the other crazy logic this field
        // has. Temporarily replacing inputEl's focus function with
        // a function that does nothing ensures that the dropdown
        // remains hidden.
        inputEl.focus = Ext.emptyFn;
        this.callParent(arguments);
        inputEl.focus = focusFn;
    }
});
//////////////////////
// ..\js\search\BooleanParser.js
//////////////////////
/**
 * This object is responsible for parsing possible boolean values from
 * strings. It does not look for strings like 'true'/'false', but rather
 * special strings that represent boolean values for Searchable entities
 * in the database/lucene index.
 *
 * Usage:
 * Suppose you have a Searchable order entity that has a boolean
 * flag on it that specifies whether the order has been shipped or not. That
 * entity can be configured to use a special string linked to the shipped state.
 * Let's say the string is 'shipped'. This value is not localized and the server
 * side search logic will treat 'shipped' as TRUE for that column and 'not_shipped'
 * as FALSE.
 *
 * This parser is in charge of converting a localized string value into the string
 * that represents a true or false value for specific server side entities. If the
 * localized translation for 'shipped' is 'was shipped' and 'not_shipped' is 'has
 * not been shipped'. This parser will look for those strings and return 
 *'\uFFFEshipped\uFFFE' or '\uFFFEnot_shipped\uFFFE' if it finds them. The unicode
 * sentinel characters are needed by the server so that it can differentiate normal
 * string values from string values that are meant to represent booleans.
 *
 * Localized translations for boolean ids should be added to the rp.search.booleans
 * namespace. Note that all messages in rp.search.booleans are used, even those
 * contained in message packs that aren't loaded for the current module/taskflow/task.
 *
 * Note: the same translated string may be linked to multiple ids. In this case all ids
 * that match that translated string will be returned from {@link #parseBoolean}.
 *
 * Example:
 *      // messagePack contents:
 *      //    <msgs msg-ns="rp.search.booleans">
 *      //        <msg id="shipped">shipped</msg>
 *      //        <msg id="not_shipped">not shipped</msg>
 *      //        <msg id="hazmat">hazardous</msg>
 *      //        <msg id="not_hazmat">not hazardous</msg>
 *      //    </msgs>
 *
 *      RP.search.BooleanParser.parseBooleans('test not shipped hazardous') === [
 *          {bool: '\ufffenot_shipped\ufffe', text: 'not shipped'},
 *          {bool: '\ufffehazmat\ufffe', text: 'hazardous'},
 *          'test ',
 *      ];
 *
 * @author Nate Nichols
 */
Ext.define('RP.search.BooleanParser', {
    singleton: true,

    // @inheritdoc
    constructor: function() {
        this.callParent(arguments);
    },

    /**
     * Hits the server to retrieve localized boolean translations.
     */
    requestTranslations: function() {
        Ext.Ajax.request({
            url: '/data/localized_search_booleans',
            success: this._onTranslationsLoaded,
            scope: this
        });
    },

    /**
     * Parse the text looking for any substrings that may represent
     * a boolean id registered with the parser. Any ids found
     * are wrapped in the unicode sentinel character '\ufffe'.
     *
     * @return {Object[]} The returned array will contain Strings and/or
     * Objects. Any strings that are returned are the parts of the original
     * text that did not represent a boolean value. The objects returned
     * in the Array represent the parts of the text that were found to be
     * translated boolean id strings. The object consists of 'bool', and 'text'
     * properties, where bool stores the wrapped id string, and text stores
     * the part of the text the Parser consumed for the id.
     */
    parseBooleans: function(text) {
        text = text.trim();
        var lowerText = text.toLowerCase(),
            results = [];

        if (Ext.isEmpty(text)) {
            return [];
        }

        Ext.Object.each(this._booleans, function(id, boolData) {
            var result;

            if (!boolData) {
                return true; // try the next id
            }

            result = this._parseBoolean('not_' + id, boolData.ne, text, lowerText);

            if (result.length > 0)  {
                results = results.concat(result);
                return true; // move on to the next. We don't short circuit
                // since the same localized string may be used by several ids
            }

            result = this._parseBoolean(id, boolData.eq, text, lowerText);

            if (result.length > 0) {
                results = results.concat(result);
                return true; // move on to the next. We don't short circuit
                // since the same localized string may be used by several ids
            }

        }, this);

        if (results.length === 0) {
            results.push(text);
        } 

        return results;
    },

    /**
     * @private
     */
    _onTranslationsLoaded: function(response) {
        var responseObj = Ext.JSON.decode(response.responseText, true) || {},
            translations = responseObj.data;
            len = 'rp.search.booleans.'.length;

        Ext.Object.each(translations, function(key, msg) {
            var id = key.slice(len),
                isNot = id.indexOf('not_') === 0,
                idObj;

            if (isNot) {
                id = id.slice(4);
            }

            idObj = this._booleans[id] || (this._booleans[id] = {});
            idObj[isNot ? 'ne' : 'eq'] = msg ? msg.toLowerCase() : '\uffff';
        }, this);
    },

    /**
     * @private
     */
    _parseBoolean: function(id, translation, text, lowerText) {
        var len = translation.length,
            startIdx = lowerText.indexOf(translation),
            endIdx = startIdx + len,
            results = [];
 
        if (startIdx !== -1) {
            results.push({
                bool: '\ufffe' + id + '\ufffe',
                text: text.slice(startIdx, endIdx)
            });
            
            return this.parseBooleans(text.slice(0, startIdx))
                .concat(results)
                .concat(this.parseBooleans(text.slice(endIdx)));
        }
        else {
            return [];
        }
    },

    /**
     * @private
     */
    _booleans: {}

});
//////////////////////
// ..\js\search\StringParser.js
//////////////////////
Ext.define('RP.search.StringParser', {
    singleton: true,

    /**
     * Transform a plain search string into a string encoding and
     * array of values to search for. The string will be split on
     * whitespace and parsed for date ranges and strings that represent
     * boolean values. The final result will
     * be an array of values encoded in a string.
     *      Util.stringToSearchQueryValue('Madison today - friday') ===
     *          '["Madison",{"startDate":<todaysDate>,"endDate":<endOfFriday>}, "Madison"]'
     * @param {String} stringValue The value to transform
     * @return {String} The encoded array string.
     */
    parseSearchString: function(stringValue) {
        var booleanValues = RP.search.BooleanParser.parseBooleans(stringValue),
            rangeValues = RP.util.date.RangeParser.parseRanges(stringValue),
            queryArray = [], strings = {};

        Ext.Array.each(booleanValues, function(value) {
            if (Ext.isObject(value)) {
                queryArray.push(value.bool);
            }
        });

        Ext.Array.each(rangeValues, function(value) {
            if (Ext.isObject(value)) {
                queryArray.push(value.dateRange);
            }
        });

        // remove duplicate strings and '-'.
        Ext.Array.each(stringValue.trim().split(/\s+/), function(value) {
            if (value !== '-') {
                strings[value] = true;
            }
        });

        Ext.Object.each(strings, function(value) {
            queryArray.push(value);
        });

        return Ext.JSON.encode(queryArray);
    }
});
//////////////////////
// ..\js\help\Container.js
//////////////////////
/**
 * @private
 */
Ext.define('RP.help.Container', {
    extend: 'Ext.window.Window',
    
    /**
     * @cfg alignComponent required.  Component to align to and sync its
     * height with.
     */
    alignComponent: undefined,
    
    maxWidth: 250,
    width: 250,
    
    bodyCls: 'rp-help-container-body',
    cls: 'rp-help-container',
    
    closeAction: 'hide',
    
    modal: false,
    
    autoScroll: true,
    
    shadow: false,
    
    draggable: false,
    
    initComponent: function() {
        this.callParent();

        this.on('show', this._onShow, this);
        
        this.alignComponent.on('resize', this._onAlignComponentResize, this, {
            buffer: 1
        });
        
        this._syncHeightWithAlignComponent();
    },

    _onShow: function() {
        this._doSlideInAnimation();
    },
    
    /**
     * Show help content from the passed in contentId (currently the
     * loaded message pack message id).
     * @param {String} contentId
     */
    showContent: function(contentId) {
        this.update(RP.getMessage(contentId));
        this.show();
    },
    
    //private
    _doSlideInAnimation: function() {
        this._alignWithComponent();
        // Setting the window's display to none prevents the animation from flickering.
        this.getEl().setStyle('display', 'none').slideIn('r', {
            duration: 350
        });
    },
    
    // override
    hide: function() {
        var me = this,
            callbackFn = Ext.window.Window.prototype.hide;

        this.getEl().slideOut('r', {
            duration: 350,
            callback: Ext.bind(callbackFn, me)
        });
    },

    //private
    _onAlignComponentResize: function() {
        if (!this.isHidden()) {
            this._alignWithComponent();
        }
    },
    
    //private
    _alignWithComponent: function() {
        this._syncHeightWithAlignComponent();
        this.alignTo(this.alignComponent, 'tr-tr');
    },
    
    //private
    _syncHeightWithAlignComponent: function() {
        this.setHeight(this.alignComponent.getHeight());
    }
});
//////////////////////
// ..\js\help\Button.js
//////////////////////
/**
 * @class RP.help.HelpButton
 * @extends Ext.button.Button
 */
 Ext.define('RP.help.Button', {
    extend: 'Ext.button.Button',
    alias: ['helpbutton', 'widget.helpbutton'],

    /**
     * @cfg {String} helpBtnCls 
     * This is the css class to be applied to the help button.
     * The states are '-hover', '-pressed', '-normal'
     */
    helpBtnCls: 'rp-help-button',

    /**
     * @cfg {String} mainHelpUrl 
     * URL pointing to the online help's landing page.
     */
    mainHelpUrl: RP.globals.getPath('BASE_URL') + 
                    RP.globals.getValue('PATH_TO_ROOT') + 'web/refs/help/main.htm',

    /**
     * @cfg {String} target
     * Target attribute assigned to the new browser window
     * that opens when the help button is clicked.
     */
    target: 'rphelpwindow',

    width: 38,

    initComponent: function() {
        Ext.apply(this, {
            cls: this.helpBtnCls + '-normal'
        });

        this.callParent(arguments);

        this.on('click', this._onHelpClick, this);
    },

    /**
     * Gets the current task's help URL. If it is not defined
     * by CURRENT_TASK.taskConfig.helpMapperFn, it constructs one from
     * taskConfig.helpUrl and RP.globals.HELP_BASE_URL.
     * @return {String} Task-specific help URL
     */
    getHelpUrl: function() {
        // This value is updated at runtime by the taskflow engine, so getValue doesn't work
        var currTask = RP.globals.CURRENT_TASK;
        
        if (!Ext.isDefined(currTask)) {
            return null;
        }
        
        var currTaskConfig = currTask.taskConfig,
            taskMappedHelpUrl = undefined;
        
        if (Ext.isFunction(currTaskConfig.helpMapperFn)) {
            taskMappedHelpUrl = currTaskConfig.helpMapperFn();
        }
        
        // Get the components of the Help URL.
        // Note that the help map function takes precedence over 
        // everything else, but the global baseURL is never prepended to it.
        var taskHelpUrl = taskMappedHelpUrl || currTaskConfig.helpUrl,
            helpRootUrl = RP.globals.getValue('HELP_BASE_URL'),
            spaceMappings = taskHelpUrl.match(/\{.*\}/);

        Ext.Array.each(spaceMappings, function(mapping) {
            var mappingContent = mapping.substring(1, mapping.length - 1);

            taskHelpUrl = 
                taskHelpUrl.replace(mapping, RP.globals.getValue('HELP_SPACE_MAPPINGS')[mappingContent]);
        }, this);
        
        // If the taskHelpUrl is a full URL, or if a helpMapperFn was defined, 
        // return it instead of appending the the helpRootUrl.
        if (Ext.isFunction(currTaskConfig.helpMapperFn) || taskHelpUrl.indexOf("://") !== -1) {
            return taskHelpUrl;
        }
        
        // If either help URL component is empty, default to Main Help...
        if (Ext.isEmpty(taskHelpUrl) || Ext.isEmpty(helpRootUrl)) {
            return null;
        }
        
        // Valid Help URL components, so combine them for full URL...
        return (helpRootUrl + taskHelpUrl);
    },

    /**
     * @private
     * @param {Ext.button.Button} button The component that fired the event
     * @param {Event} e The click event
     * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener
     *
     * Event handler for the help button's click event
     */
    _onHelpClick: function(button, e, eOpts) {
        var w,
            urlHash = window.location.hash;

        if (urlHash) {
            rawHelpUrl = this.getHelpUrl(urlHash);

            if (rawHelpUrl) {
                var currTaskConfig = RP.globals.CURRENT_TASK.taskConfig;
                
                // Give precedence to the helpMapperFn over the generated UAP link
                if (Ext.isFunction(currTaskConfig.helpMapperFn)) {
                    w = window.open(rawHelpUrl, this.target, "height=768,left=700,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,titlebar=no,toolbar=no,top=20,width=1024,zoominherit=no");
                    
                    if (w) {
                        w.focus();
                    }
                }
                else {
                    Ext.Ajax.request({
                        url: RP.globals.HELP_PORTAL_AUTH_URL,
                        params: {
                            taskHelpUrl: rawHelpUrl
                        },
                        scope: this,
                        success: function(result) {
                            if (result.responseObject.data.helpUrl) {
                                var url = result.responseObject.data.helpUrl.toString();
                                w = window.open(url, this.target, "height=768,left=700,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,titlebar=no,toolbar=no,top=20,width=1024,zoominherit=no");
                                
                                if (w) {
                                    w.focus();
                                }
                            }
                            else {
                                Ext.Msg.show({
                                    title: RP.getMessage("rp.common.misc.NoHelpFoundTitle"),
                                    msg: RP.getMessage("rp.common.misc.NoHelpFoundText"),
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            }
                        },
                        failure: function(result) {
                            Ext.Msg.show({
                                title: RP.getMessage("rp.common.misc.NoHelpFoundTitle"),
                                msg: RP.getMessage("rp.common.misc.NoHelpFoundText"),
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                }
            }
            else {
                Ext.Msg.show({
                    title: RP.getMessage("rp.common.misc.NoHelpFoundTitle"),
                    msg: RP.getMessage("rp.common.misc.NoHelpFoundText"),
                    buttons: Ext.Msg.OK,
                    icon: Ext.MessageBox.WARNING
                }); 
            }
        }
        else {
            // Main help.
            w = window.open(this.mainHelpUrl, this.target, 
                      "height=768,left=400,top=20,width=1024,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,titlebar=no,toolbar=no,zoominherit=no");
            
            if (w) {
                w.focus();
            }
        }
    }
 });
//////////////////////
// ..\js\form\combobox\plugin\StyleDisplayFromInput.js
//////////////////////
/**
 * Takes the user inputed raw value and applys a configed css class to the matches in
 * the comboboxs dropdown.  If no cls is configured it will by default apply a "bold" style to
 * the matches.
 */
Ext.define('RP.form.combbox.plugin.StyleDisplayFromInput', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.styleDisplayFromInput',
    
    cls: 'rp-form-combobox-plugin-bold ',
    
    init: function(combo) {
        combo.listConfig = {
            prepareData: Ext.bind(this._prepareListData, this, [combo], true)
        };
    },
    
    _prepareListData: function(data, index, record, combo) {
        //clone
        var d = Ext.apply({}, data), displayValue = d[combo.displayField], raw = combo.getRawValue();
        if (record) {
            Ext.apply(d, record.getAssociatedData());
        }
        d[combo.displayField] = this._applyCssString(displayValue, raw);
        
        return d;
    },
    
    /**
     * @private
     */
    _applyCssString: function(value, raw) {
        if(!this._boundStringReplaceFn){
            this._boundStringReplaceFn = Ext.bind(this._replacerStringFn, this);
        }
        
        return value.replace(new RegExp(raw, "gi"), this._boundStringReplaceFn);
    },
    
    /**
     * @private
     */
    _replacerStringFn: function(str) {
        return '<span class="' + this.cls + '">' + str + '</span>';
    }
});
//////////////////////
// ..\js\form\field\plugin\Help.js
//////////////////////
/**
 * @class RP.form.field.plugin.Help
 * @extends Ext.AbstractPlugin
 *
 *  Plugin that will add a "help" link to a field and when clicked will show a the help
 *  container with the content id passed in.  By defualt if the field label is wide enough
 *  it will put the label to the right of the field label.
 *
 * @author plee
 */
Ext.define('RP.form.field.plugin.Help', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.RP.FieldHelp',
    
    /**
     * @cfg contentId, the id of the message to pull
     * the help content.
     */
    contentId: '',
    
    /**
     * @cfg
     * @protected 
     */
    _linkTemplate: '<div class="rp-form-field-help-link" ></div>',
    
    init: function(component) {
        if (component.hasMixin(Ext.form.Labelable)) {
            this._setupLabelable(component);
        }
        else if (component instanceof Ext.panel.Panel) {
            this._setupPanel(component);
        }
        else {
            Ext.Error.raise('Help plugin currently supports only Labelable components and Panels');
        }
    },    
    
    //private
    _setupLabelable: function(labelable){
        //this div assumes a top label arrangement will be used
        labelable.beforeLabelTpl = '<div class="rp-form-field-help-label-wrapper">';
        labelable.afterLabelTpl = this._getLinkTemplate() + '</div>';
        labelable.renderSelectors = {
            helpLink: 'div.rp-form-field-help-link'
        };
        
        labelable.on('render', this._onLabelableRender, this);
    },

    //private
    _setupPanel: function(panel) {
        panel.on('render', this._onPanelRender, this);
    },
    
    //private
    _getLinkTemplate: function() {
        return this._linkTemplate;
    },
    
    //private
    _onHelpLinkClick: function(){
        var container = this._getHelpContainer();
        container.showContent(this.contentId);
    },
    
    //private
    _onLabelableRender: function(labelable) {
        labelable.helpLink.on('click', this._onHelpLinkClick, this);
    },

    //private
    _onPanelRender: function(panel) {
        var header = panel.header,
            helpLink;

        if (header && header.titleCmp) {
            header.titleCmp.on('render', function() {
                helpLink = Ext.DomHelper.insertAfter(header.titleCmp.textEl, this._linkTemplate, true);
                helpLink.on('click', this._onHelpLinkClick, this);
            }, this);
        }
    },
    
    //private
    _getHelpContainer: function(){
        return RP.getTaskflowFrame().getRPHelpContainer();
    }
});
//////////////////////
// ..\js\form\field\overrides\Time.js
//////////////////////
Ext.define('Ext.form.field.overrides.Time', {
    override: 'Ext.form.field.Time',

    // The new framework adds a great deal of padding to boundlist items;
    // attempting to make the picker match the field width results in text
    // wrapping to a second line.
    matchFieldWidth: false,

    initComponent: function() {
        var listConfig = this.listConfig || {};

        Ext.applyIf(listConfig, {
            // Scrollbars don't take up as much space in webkit browsers,
            // so we don't need to make the picker as wide.
            width: Ext.isWebKit ? 95 : 105
        });

        Ext.apply(this, {
            listConfig: listConfig
        });

        this.callParent(arguments);
    }
});
//////////////////////
// ..\js\form\field\overrides\Trigger.js
//////////////////////
Ext.define('Ext.form.field.overrides.Trigger', {
    override: 'Ext.form.field.Trigger',

    _hiddenTriggerCls: 'rp-hidden-trigger',

    /**
    * This override fixes the appearance of a combo box when the trigger is hidden.
    * The combo box used to have a missing right border when the trigger was hidden; now it does not.
    */
    initComponent: function(){
        this.callParent(arguments);
        this.on('afterrender', function() {
            if(this.hideTrigger || this.readOnly) {
                this.triggerWrap.addCls(this._hiddenTriggerCls);
            }
        });
    },

    /**
     * Add / remove extra style options required to make field borders
     * look right when the trigger is hidden.
     */
    setReadOnly: function(readOnly) {
        this.callParent(arguments);

        this._updateHiddenTriggerCls(readOnly);
    },

    /**
     * Add / remove extra style options required to make field borders
     * look right when the trigger is hidden.
     */
    setHideTrigger: function(hideTrigger) {
        this.callParent(arguments);

        this._updateHiddenTriggerCls(hideTrigger);
    },

    /**
     * @private
     */
    _updateHiddenTriggerCls: function(shouldAdd) {
        if (this.rendered) {
            this.triggerWrap[shouldAdd ? 'addCls' : 'removeCls'](this._hiddenTriggerCls);
        }
        else {
            this.triggerWrapCls += (' ' + this._hiddenTriggerCls);
        }
    },
    
    /**
     * Ext ignores the triggerWrapCls config.  Fix that.
     */
    getSubTplMarkup: function() {
        var me = this,
            field = Ext.form.field.Text.prototype.getSubTplMarkup.call(this, arguments),
            triggerWrapCls = me.triggerWrapCls || Ext.baseCSSPrefix + 'form-trigger-wrap';

        return '<table id="' + me.id + '-triggerWrap" class="' + triggerWrapCls + '" cellpadding="0" cellspacing="0"><tbody><tr>' +
            '<td id="' + me.id + '-inputCell" class="' + Ext.baseCSSPrefix + 'form-trigger-input-cell">' + field + '</td>' +
            me.getTriggerMarkup() +
            '</tr></tbody></table>';
    },

    /**
     * This override is being added because when focusing on a component while a trigger field has focus does not
     * cause the blur to be triggered for the trigger field.
     *
     * ExtJS has given us this override and logged EXTJSIV-6651.
     */
    onBlur: function(e) {
        var me = this;
 
        /**
         * This method is now within the component's blur lifecycle and will be called under the following scenarios.
         * 1. Normal blur operations when another item is focused via clicking and tabbing.
         * 2. Blur is call programatically.
         * 3. Item is clicked on BoundList view.
         *
         * It's extremely important to know the provenance of a blur event, which is why Component.blur() attaches a blurred property.
         * For example, we don't want mimicBlur to be called in certain scenarios, such as when the BoundList is expanded and an item is selected.
         * This would trigger a blur event, but then it shouldn't be handed off to mimicBlur. So, there needs to be a way to determine the provenance of the blur.
         *
         * Setting me.blurred only occurs when blur is called programatically, so we can use this to determine when not to call mimicBlur.
         */
 
        // Either the BoundList was clicked, or the trigger button was.
        // In either case, we don't want to run blur logic.
        if (!me.blurred && me.isExpanded) {
            return;
        }
 
        /**
         * A 'normal' blur event occurred so call mimicBlur.
         *
         * Normal = {
         *     me.blurred = false,
         *     me.hasFocus = true
         * };
         *
         * Blur is called programatically.
         * e.g., Component.blur() -> Element.blur() -> Trigger.onBlur()
         * Programatically called = {
         *     me.blurred = true,
         *     me.hasFocus = true
         * };
         */
        if (me.blurred || me.hasFocus) {
            // Call mimicBlur here instead of triggerBlur so that we
            // don't blur the field if the trigger button has been clicked.
            me.mimicBlur(e);
        }
    }
});
//////////////////////
// ..\js\menu\BalloonMenu.js
//////////////////////
/**
 * Menu which shows as a balloon style in a downward arrangement.
 * NOTE: does not support nested menus.
 * RECOMMENDED: BalloonMenu has a config of plain: true by default to remove the vertical separator.
 * Regular (plain) menu items should have this same config set as well to remove the space allocated
 * for an icon.
 */
Ext.define('RP.menu.BalloonMenu', {
    
    extend: 'Ext.menu.Menu',
    alias: ['balloonmenu', 'widget.balloonmenu'],

    /**
     * @cfg
     * @override
     */
    plain: true, // Hide the icon/text vertical line

    /**
     * @cfg
     * @override
     */
    showSeparator: false,

    /**
     * @cfg
     * @override
     */
    padding: 0,

    /**
     * @cfg
     * @override
     */
    shadow: false,

    /**
     * @cfg {Boolean} useArrow
     * If true, the menu will be separated from its container by an upward-facing arrow.
     */
    useArrow: true,

    /**
     * @cfg
     * @override
     */
    cls: 'rp-balloon-menu',

    /**
     * @cfg
     * @override
     */
    bodyCls: 'rp-balloon-menu-body',

    /**
     * @cfg
     */
    menuItemPressedCls: 'rp-menu-item-mousedown',

    /**
     * @override
     * Replacement for the menu renderTpl to add custom classes. id's for body and targetEl must be provided. 
     * These are the childEls the ElementContainer mixin defines on the VBoxLayout.body and VBoxLayout.targetEl.
     */
    renderTpl:  '<div id="{id}-body" class="rp-balloon-menu-body">' +
                    '<div id="{id}-innerCt" class="rp-balloon-menu-innerCt">' +
                        '<div id="{id}-targetEl" class="rp-balloon-menu-items"></div></div></div>',

    /**
     * @override
     */
    initComponent: function() {
        this.addEvents(
            /**
             * @event itemclick
             * Fired when a menu item is clicked. Forwarded from the contained BoundList
             * @param {RP.menu.BalloonMenu} menu
             * @param {Ext.view.View} view
             * @param {Ext.data.Record} record
             */
            'itemclick'
        );

        if (this.useArrow) {
            this._buildArrow();
        }

        this.callParent(arguments);

        this.on('activate', this._onBeforeActivate, this, {single: true});
        this.on('afterrender', this._afterMenuRender);
        this.on('show', this._onShow, this);
    },

    _buildArrow: function() {
        // The menu's vbox layout doesn't set its size correctly if we just add a div
        // to the component's renderTpl for the arrow element, so we'll have to build
        // a top-docked container to contain it instead. Not ideal, but it seems to work.
        var arrowContainerConfig = {
            xtype: 'container',
            cls: 'rp-balloon-menu-pointarea',
            dock: 'top',
            itemId: 'arrowContainer',
            height: 15
        };

        if (Ext.isEmpty(this.dockedItems)) {
            Ext.apply(this, {
                dockedItems: [arrowContainerConfig]
            });
        }
        else {
            this.dockedItems.insert([arrowContainerConfig], 0);
        }
    },

    /**
     * @override ensure an actual menu item was clicked on, and not the arrow
     * element or a menu separator.
     */
    onClick: function(e) {
        var item = (e.type === 'click') ? this.getItemFromEvent(e) : this.activeItem;
        var cont = true;


        if (item) { //if an actual item was clicked
            if ((item == this._getArrow()) || (item instanceof Ext.menu.Separator)) {
                //ignore it if it was the arrow or a separator.
                e.stopEvent();
                cont = false;
            }
        } else { //otherwise ignore it and hide the menu
            this.hide();
            cont = false;
        }

        if (!cont) {
            return false;
        }

        this.callParent(arguments);
    },

    _afterMenuRender: function(balloonMenu) {
        balloonMenu.el.unselectable();
    },

    _getArrow: function() {
        return this.dockedItems.findBy(function(item, key) {
            return key === 'arrowContainer';
        }, this);
    },

    _onShow: function(buttonMenu) {
        var arrowContainer = this._getArrow();

        // here we align the menu arrow manually to the center of the button's bottom
        // because the menu body doesn't have a fixed size
        if (arrowContainer) {

            arrowContainer.getEl().setX(
                buttonMenu.getEl().getLeft() + buttonMenu.getWidth() / 2 - arrowContainer.getWidth() / 2);
        }
    },
    
    _onBeforeActivate: function(cmp, options) {
        var menuItemHtmlEls = this.el.query('.x-menu-item');
        Ext.each(menuItemHtmlEls, function(menuItemHtmlEl, index, all) {
            var menuItemEl = Ext.get(menuItemHtmlEl);

            if (!(this.items.getAt(index) instanceof Ext.menu.Separator)) {
                menuItemEl.menuItemPressedCls = this.menuItemPressedCls;
                menuItemEl.onMouseUp = function(e, t, eOpts) {
                    if (this.menuItemPressedCls && e.button === 0) {
                        this.removeCls(this.menuItemPressedCls);
                        Ext.getDoc().un('mouseup', this.onMouseUp, this);
                    }
                };

                menuItemEl.on('mousedown', function(e, t, eOpts) {
                    if (this.menuItemPressedCls && e.button === 0) {
                        this.addCls(this.menuItemPressedCls);
                        Ext.getDoc().on('mouseup', this.onMouseUp, this);
                    }
                }, menuItemEl);

                // Insert a spacer element before every menu item except the first one
                if (index !== 0) {
                    var separator = Ext.get(document.createElement('div')).
                            addCls('rp-balloon-menu-item-separator');
                            
                    menuItemEl.insertFirst(separator);
                }
            }
        }, this);

        this.items.getAt(0).setWidth(15);
    }
});
//////////////////////
// ..\js\menu\BoundMenu.js
//////////////////////
/**
 * Simple menu class that binds a store to a "combo" like menu.  This is useful for
 * converting combos to menus or creating simple data driven menus that 
 * react to name-value pairs changes.
 * # Example usage:
 *     @example
 *     // The data store containing the list of states
 *     var states = Ext.create('Ext.data.Store', {
 *         fields: ['abbr', 'name'],
 *         data : [
 *             {"abbr":"AL", "name":"Alabama"},
 *             {"abbr":"AK", "name":"Alaska"},
 *             {"abbr":"AZ", "name":"Arizona"}
 *             //...
 *         ]
 *     });
 *     Ext.create('Ext.button.Button', {
 *           menu: Ext.create('RPUX.menu.BoundMenu', {
 *              store: states,
 *              displayField: 'name',
 *              valueField: 'abbr',
 *              listeners: {
 *                  change: function() {
 *                      this.up('button').setText(this.getDisplayValue())
 *              }
 *           }
 *       }),
 *       renderTo: Ext.getBody(),
 *       text: 'States'
 *     });
 * @author plee
 */
Ext.define('RP.menu.BoundMenu', {
    extend: 'Ext.menu.Menu',
    // this class is bumped up from the RPUX layer
    alternateClassName: ['RPUX.menu.BoundMenu'],
    alias: ['widget.boundmenu'],
    
    cls: 'rp-bound-menu',
    
    showSeparator: false,
    
    /**
     * @property {Ext.data.Model} currentRecord the record associated with the current value
     * if the value does not match a record in the store it will be a falsy value.
     */
    
    /**
     * @cfg itemClassName {String}, className the class name of the menu item to
     * create.  The only supported config out of the box is Ext.menu.Item'
     */
    itemClassName: 'Ext.menu.Item',
    
    /**
     * @cfg {String} displayField The underlying display field name to bind to this
     *
     * See also {@link #valueField}.
     */
    displayField: undefined,
    
    /**
     * @cfg {String} valueField The underlying value field name to bind to this
     */
    valueField: undefined,
    
    /**
     * @cfg {Ext.data.Store} store
     * The data source to which this is bound.
     */
    store: undefined,
    
    initComponent: function() {
        this.addEvents(
            /**
             * @event valueSet
             * Fires when an a value is set
             * @param {RPUX.menu.BoundMenu} this
             * @param {Object} value the value
             */'valueSet',
            /**
             * @event select
             * Fires when an item is selected.
             * @param {RPUX.menu.BoundMenu} this
             * @param {Object} value the value selected
             */'select',
             
             /**
             * @event change
             * Fires when the value this is changed via the {@link #setValue} method.
             * @param {RPUX.menu.BoundMenu} this
             * @param {Object} newValue The new value
             * @param {Object} oldValue The original value
             * @param {Ext.menu.Item} newItem The new selected item
             * @param {Ext.menu.Item} oldValue The original selected item
             */
            'change');
        
        this.callParent();
        
        this._setupStore();
    },
    
    //private
    _setupStore: function() {
        if (this.store.getCount() > 0) {
            this._addBoundItems();
        }
        else {
            this.store.on('load', this._onStoreLoad, this);
        }
    },
    
    //private
    _onStoreLoad: function() {
        this._addBoundItems();
    },
    
    //private
    _addBoundItems: function() {
        this.removeAll(true);
        this.add(this._createItems());
        if (this.value) {
            this.setValue(this.value);
        }
    },
    
    //private
    _createItems: function(){
        var items = [];
        this.store.each(function(record){
            items.push(this._createMenuItem(record));
        }, this);
        
        return items;
    },
    
    //private
    _createMenuItem: function(record) {
        return Ext.create(this.itemClassName, {
            text: record.get(this.displayField),
            value: record.get(this.valueField),
            scope: this,
            handler: this._itemHandler,
            record: record
        });
    },
    
    /**
     * @private
     * The handler for the menu item, if this needs to be changed {@link Ext.menu.Menu} 
     * should probably be used.
     */
    _itemHandler: function(item) {
        this.setValue(item.value);
        this.fireEvent('select', this, this.value);
    },
    
    /**
     * Returns the most recently set value (It may not be a valid store value).
     * @return {Object}
     */
    getValue: function() {
        return this.value;
    },
    /**
     * Sets the current value to passed in value.  The current record will
     * be updated based on the new value.  There is no validation to make sure the value
     * is containted in the store.  If the value being set does not match anything in the store
     * currentRecord will be set to a falsy value.
     * @param {Object} value
     */
    setValue: function(value) {
        var oldValue = this.value;
        this.value = value;
        this.currentRecord = this.findRecordByValue(this.value);
        
        this.fireEvent('valueSet', this, this.value);
        
        if (oldValue !== this.value) {
            this.fireEvent('change', this, this.value, oldValue,
                    this.getItem(this.currentRecord), this.getItem(this.findRecordByValue(oldValue)));
        }
    },
    
    /**
     * Generates the string value to be displayed for the currently stored value.
     */
    getDisplayValue: function() {
        return this.currentRecord ? this.currentRecord.get(this.displayField) : '';
    },
    
    /**
     * Get a reference to a menu item by record
     * @param {Ext.data.Model} The record to search by
     * @return {Ext.menu.Item} The menu item or false if not found
     */
    getItem: function(record) {
        var result = false;
        this.items.each(function(item) {
            if (item.record === record) {
                result = item;
                return false;
            }
        });
        return result;
    }
}, function() {
        this.borrow(Ext.form.field.ComboBox, ['findRecord', 'findRecordByValue']);
}); 
//////////////////////
// ..\js\notification\NotificationMenu.js
//////////////////////
Ext.define('RP.notification.NotificationMenu', {

    extend: 'RP.menu.BalloonMenu',
    alias: ['widget.notificationmenu', 'notificationmenu'],

    modelName: 'RP.notification.NotificationModel',

    initComponent: function() {
        this.callParent(arguments);

        this.notificationStore = Ext.create('Ext.data.Store', {
            model: this.modelName,
            listeners: {
                scope: this,
                load: this._onNotificationStoreLoad
            }
        });

        this.notificationStore.load();
        this.on('click', this._onClick, this);
    },

    _onNotificationStoreLoad: function(store, records, success, options) {
        var notificationRecords = this.notificationStore.getRange();

        Ext.each(notificationRecords, function(record) {

            //ignore any published exceptions
            if(record.get('type') !== 'REFS_EXCEPTION') {
                this.add({
                    text: record.get('message'),
                    url: record.get('url'),
                    plain: true
                });
            }
        }, this);

        this.fireEvent('load', this, store, records, success, options);
    },
    
    _onClick: function(menu, menuItem, event, options) {
        alert('Where to go for ' + menuItem.text + '?');
        //TODO:
    }
});
//////////////////////
// ..\js\notification\NotificationButton.js
//////////////////////
Ext.define('RP.notification.NotificationButton', {
    extend: 'Ext.button.Button',
    alias: ['widget.notificationbutton', 'notificationbutton'],
    
    menuAlign: 't-b',

    idCls: 'rp-notify-btn',
    iconAlign: 'right',
    iconCls: 'rp-notify-btn-count',
    iconClsNone: 'rp-notify-btn-count-none',
    iconClsSome: 'rp-notify-btn-count-some',

    initComponent: function() {
        this.callParent(arguments);

        this.addCls(this.idCls);

        if(!this.menu) {
            this.menu = Ext.create('RP.notification.NotificationMenu');
        }

        this.menu.on('load', this._onMenuLoad, this);
    },

    _onMenuLoad: function(menu, store, records, success, options) {
        var count = store.count();
        if(this.rendered) {
            this._updateCountDisplay(count);
        }
        else {
            this.on('afterrender', function() {
                this._updateCountDisplay(count);
            }, this, {single: true});
        }
    },

    _updateCountDisplay: function(count) {
        var iconEl = this.getEl().down('.' + this.iconCls);
        iconEl.update(count.toString()); //number zero doesn't show

        if(count === 0) {
            iconEl.addCls(this.iconClsNone);
            iconEl.removeCls(this.iconClsSome);
        }
        else {
            iconEl.removeCls(this.iconClsNone);
            iconEl.addCls(this.iconClsSome);
        }

        //count icon area sizing may need to change
        this.updateLayout();
    }
});
//////////////////////
// ..\js\notification\NotificationModel.js
//////////////////////
Ext.define('RP.notification.NotificationModel', {
    extend: 'Ext.data.Model',

    fields: [{
        name: 'message',
        type: 'string'
    }, {
       name: 'site',
       type: 'string'
    }, {
        name: 'type',
        type: 'string'
    }, {
        name: 'url',
        type: 'string'
    }],

    proxy: {
        type: 'ajax',
        url: '/data/notifications',
        reader: {
            type: 'json',
            root: 'data',
            getData: function(data) {
                var dataArray = data.data;
                var result = [];
                Ext.each(dataArray, function(item){
                    result.push(Ext.decode(item));
                }, this);
                return result;
            }
        }
    }
});
//////////////////////
// ..\js\testing\CSUnit.js
//////////////////////
/**
 *
 * This singleton class is an alternative to the CSUnit testing facility provided by RPWEB's csunit stash library.
 * This class supports the use of CSUnit testing within a taskflow.  See {@link RP.csunit.task.Panel} for more details.
 */
Ext.define('RP.testing.CSUnit', {
    alternateClassName: 'CSUnit',
    mixins: {
        observable: "Ext.util.Observable"
    },
    singleton: true,

    Yahoo: undefined,
    tests: {},
    files: [],
    webRoot: "",
    Test: {},

    constructor: function(config) {
        this.addEvents("loadTests", "runTests", "completeTests", "beginTestCase", "completeTestCase");
        
        this.mixins.observable.constructor.apply(this, arguments);
    },

    init: function() {
        YUI({ logInclude: { TestRunner: true}, filter: 'debug'}).use("test","console","console-filters", "event-simulate", function(Yahoo) {
            CSUnit.Yahoo = Yahoo;
            CSUnit.Assert = Yahoo.Assert;
            CSUnit.assert = Yahoo.assert;
            CSUnit.fail = Yahoo.fail;
            CSUnit.ArrayAssert = Yahoo.ArrayAssert;
            CSUnit.ObjectAssert = Yahoo.ObjectAssert;
            CSUnit.Test.Case = Yahoo.Test.Case;
            CSUnit.Test.Suite = Yahoo.Test.Suite;
            CSUnit.log = Yahoo.log;
            CSUnit.Event = Yahoo.Event;

            CSUnit.Assert.datesAreEqual = function(expected, actual, msg) {
                CSUnit.Assert.isTrue(expected instanceof Date, "Expected value is not a Date object");
                CSUnit.Assert.isTrue(actual instanceof Date, "Actual value is not a Date object");
                CSUnit.Assert.areEqual(expected.getTime(), actual.getTime(), msg || "Dates are not equal");
            };

            CSUnit.Assert.intervalsAreEqual = function(expected, actual, msg) {
                msg = msg || 'Intervals are not equal';
                if ((expected.Start && expected.End) && (actual.Start && actual.End)) {
                    CSUnit.Assert.isTrue(RP.core.IntervalJN.isCoincident(expected, actual), msg);
                }
                else {
                    CSUnit.Assert.fail("Not intervals");
                }
            };

            /**
             * Asserts that all properties in the object exist in another object.
             * @param {Object} expected An object with the expected properties.
             * @param {Object} actual An object with the actual properties.
             * @param {String} message (Optional) The message to display if the assertion fails.
             * @method propertiesAreEqual
             * @static
             */
            CSUnit.ObjectAssert.propertiesAreEqual = function(expected, actual, message) {
                var Assert = CSUnit.Assert, property, i;

                //get all properties in the object
                var expectedProperties /*:Array*/ = [];
                Ext.Object.each(expected, function(property) {
                    expectedProperties.push(property);
                });

                //see if the expectedProperties are in the expected object
                for (i = 0; i < expectedProperties.length; i++) {
                    Assert.isNotUndefined(actual[expectedProperties[i]], Assert._formatMessage(message, "Property '" + expectedProperties[i] + "' expected."));
                }

                var actualProperties /*:Array*/ = [];
                Ext.Object.each(actual, function(property) {
                    actualProperties.push(property);
                });

                //see if the actualProperties are in the expected object
                for (i = 0; i < actualProperties.length; i++) {
                    Assert.isNotUndefined(expected[actualProperties[i]], Assert._formatMessage(message, "Property '" + actualProperties[i] + "' expected."));
                }

            };

            Yahoo.Test.Runner.subscribe(Yahoo.Test.Runner.COMPLETE_EVENT, Ext.bind(CSUnit.completeTests, CSUnit));
            Yahoo.Test.Runner.subscribe(Yahoo.Test.Runner.TEST_CASE_BEGIN_EVENT, Ext.bind(CSUnit.beginTestCase, CSUnit));
            Yahoo.Test.Runner.subscribe(Yahoo.Test.Runner.TEST_CASE_COMPLETE_EVENT, Ext.bind(CSUnit.completeTestCase, CSUnit));
            CSUnit.on("runTests", CSUnit.run);
            CSUnit.on("completeTests", CSUnit.complete);

            CSUnit.loadTests(this);

            CSUnit.yconsole = new Yahoo.Console({
                newestOnTop: false,
                useBrowserConsole: false,
                width: "100%",
                height: "99%",
                plugins: [ Yahoo.Plugin.ConsoleFilters ]
            });
            CSUnit.yconsole.render('#TestConsole');
        });
    },

    loadTests: function(scope) {
        this.fireEvent('loadTests', scope);
    },

    runTests: function() {
        this.fireEvent("runTests");
    },

    completeTests: function() {
        this.fireEvent('completeTests');
    },

    beginTestCase: function(obj) {
        this.fireEvent('beginTestCase', obj);
    },

    completeTestCase: function(obj) {
        this.fireEvent('completeTestCase', obj);
    },

    addTest: function(testCase) {
        this.tests[testCase.name] = testCase;
        this.Yahoo.Test.Runner.add(testCase);
    },

    addSuite: function(testSuite) {
        this.Yahoo.Test.Runner.add(testSuite);
    },

    setRoot: function(root) {
        this.webRoot = root;
    },

    include: function(urls, callback) {
        var urlsArray = Ext.Array.from(urls);
        RP.util.ScriptLoader.loadSerial(
            Ext.Array.map(urlsArray, function(url) {
                return this.webRoot + url;
            }, this));
    },

    getTest: function(testName) {
        return tests[testName];
    },

    getTestNames: function() {
        var names = [];

        Ext.iterate(tests, function(key) {
            names.push(key);
        });

        return names;
    },

    getXMLResults: function() {
        return this.Yahoo.Test.Runner.getResults(this.Yahoo.Test.Format.XML);
    },

    getJUNITXMLResults: function(){
        return this.Yahoo.Test.Runner.getResults(this.Yahoo.Test.Format.JUnitXML);
    },

    getCoverage: function() {
        return this.Yahoo.Test.Runner.getCoverage(this.Yahoo.Coverage.Format.JSON);
    },

    getJsonResults: function() {
        return this.Yahoo.Test.Runner.getResults(this.Yahoo.Test.Format.JSON);
    },

    loadTestScripts: function(files) {
        RP.util.ScriptLoader.load(files, Ext.emptyFn, Ext.emptyFn);
    },

    run: function() {
        this.Yahoo.Test.Runner.run();
    },

    complete: function() {
    },

    getAllTestCases: function() {
        var runner = this.Yahoo.Test.Runner,
            originalAddTestCaseToTree = runner._addTestCaseToTestTree,
            testCases = [];
        //Intercept _addTestCaseToTestTree to get a reference to all of 
        //the test cases.  Test cases can be nested inside
        //suites so this should catch them all.
        runner._addTestCaseToTestTree = function(root, testCase) {
            testCases.push(testCase);
            return originalAddTestCaseToTree.apply(this, arguments);
        };

        //build test tree calls _addTestCaseToTestTree which
        //adds all of the tests
        runner._buildTestTree();

        runner._addTestCaseToTestTree = originalAddTestCaseToTree;

        return testCases;
    },

    /**
     * Ignore the passed in test cases.
     */
    ignoreTestCases: function(testCases){
        this._ignoreTestCases = Ext.Array.from(testCases);
        this._overrideRunTest();
    },

    _overrideRunTest: function() {
        if(!this._hasRunTestBeenOverriden) {

            this.Yahoo.Test.Runner._runTest = this._createRunTestFn();

            this._hasRunTestBeenOverriden = true;
        }
    },

    _createRunTestFn: function() {
        var runner = this.Yahoo.Test.Runner,
            originalRunTest = runner._runTest,
            me = this;

        //ignore entire test cases by using the documented
        //ignore property.  Set the original ignore after 
        //modifying it.
        return function(node) {
            var testCase = node.parent.testObject,
                shouldIgnore = me._ignoreTestCases.indexOf(testCase) > -1,
                originalIgnore;

            if(shouldIgnore) {
                originalShould = me._ignoreTest(node);
            }

            originalRunTest.call(this, node);

            if(shouldIgnore) {
                testCase._should = originalShould;
            }
        };
    },

    /**
     * Spoof the ignoring of tests by modifying the should object.
     * @param  {[type]} node [description]
     * @return {Object} the original should object 
     */
    _ignoreTest: function(node) {
        var testCase = node.parent.testObject,
            originalShould = testCase._should,
            testName = node.testObject;

        testCase._should = {
            ignore: {}
        };

        testCase._should.ignore[testName] = true;

        return originalShould;
    }
});

// Load the yui library required for CSUnit initialization
RP.util.ScriptLoader.loadDirect('/stash/Deploy/3rdparty/yui/yui/yui-debug.js');
//////////////////////
// ..\js\testing\Instrument.js
//////////////////////
Ext.define('RP.testing.Instrument', {
    singleton: true,

    getParentScripts: function() {
        var scripts = document.getElementsByTagName('script'),
            parents = [], count = 0,
            files = [], finshed, parseFiles, setInstrumented;

        Ext.Array.each(scripts, function(script) {
            var url = script.src;
            if (url.match(/\.src\.js$/)) {
                parents.push(url);
            }
        });

        return parents;
    },

    setInstrumented: function(files, savedInstrumented) {
        Ext.Array.each(files, function(obj) {
            if (Ext.Array.contains(savedInstrumented, obj.fullPath)) {
                obj.instrumented = true;
            }
        });
    },

    getInstrumented: function() {
        return localStorage ? Ext.JSON.decode(localStorage.getItem('instrumentedFiles')) || [] : [];
    },

    parseFileList: function(parentUrl, text) {
        var match = text.match(/.*\[([^\]]*)\].*/),
            names = [],
            group;
        if (match && match[1]) {
            group = match[1];
            names = Ext.Array.map(group.split(','), function(path) {
                    path = path.substring(1, path.length - 1);

                    var chunks = path.split(/\\\\|\\|\//),
                        fullPath = chunks.join('/');

                    return {
                        parentUrl: parentUrl,
                        fileName: chunks.pop(),
                        path: chunks.join('/'),
                        fullPath: fullPath,
                        instrumented: false
                    };
                });
        }
        return names;
    },

    listInstrumentableFiles: function(callback, scope) {
        var scripts = this.getParentScripts(),
            count = scripts.length,
            files = [],
            finished = function() {
                if (--count === 0) {
                    this.setInstrumented(files, this.getInstrumented());
                    callback.call(scope, files);
                }
            };

        if (scripts.length > 0) {
            Ext.Array.each(scripts, function(url) {
                Ext.Ajax.request({
                    url: url,
                    method: 'GET',
                    disableCaching: false,
                    success: function(responseObj) {
                        files = files.concat(
                            this.parseFileList(url, responseObj.responseText));
                        finished.call(this);
                    },
                    failure: finished,
                    scope: this
                });
            }, this);
        }
        else {
            callback.call(scope, []);
        }
    },

    persistInstrumented: function(toInstrument) {
        if (localStorage) {
            if (Ext.isEmpty(toInstrument)) {
                localStorage.removeItem('instrumentedFiles');
            }
            else {
                localStorage.setItem('instrumentedFiles', Ext.JSON.encode(toInstrument));
            }
        }
    }
});
//////////////////////
// ..\js\testing\TestSelector.js
//////////////////////
/** 
  @private
  Test selector for the csunit panel.
 */
Ext.define('RP.testing.TestSelector', {
    extend: 'RP.menu.BoundMenu',
    cls: '',

    displayField: 'name',
    valueField: 'testCase',
    itemClassName: 'Ext.menu.CheckItem',

    _cookieName: 'rpux-test-selector-checked-items',

    initComponent: function() {
        Ext.apply(this, {
            store: this._createStore()
        });

        this.callParent();

        this._addListeners();
        this._checkItemsFromCookie();
        this._ignoreUncheckedTests();
    },

    //@private
    _createStore: function() {
        var testCases = CSUnit.getAllTestCases(),
            data = [{name: 'All'}];

        Ext.Array.forEach(testCases, function(testCase) {
            data.push({
                name: testCase.name,
                testCase: testCase
            });
        });

        return Ext.create('Ext.data.Store', {
            fields: ['name', 'testCase'],
            data: data
        });
    },

    /**
     * @private
     * Check persisted checked information in a cookie so devs don't have to
     * select the test they are running on every screen load.
     */
    _checkItemsFromCookie: function(){
        var cookieValue = Ext.Array.from(Ext.JSON.decode(Ext.util.Cookies.get(this._cookieName))),
            item;

       if(!Ext.isEmpty(cookieValue)) {
            Ext.Array.forEach(cookieValue, function(testName){
                item = this._getItemFromTestName(testName);
                if(item) {
                    item.setChecked(true);
                }
            }, this);
       }
       else {
            this._getAllCheckItem().setChecked(true);
       }
    },

    //@private
    _getItemFromTestName: function(testName) {
        return this.items.findBy(function(item) {
            return item.record.get('name') === testName;
        });
    },

    //@private
    _addListeners: function() {
        this.on('valueSet', this._onValueSet, this);
    },

    //@private
    _onValueSet: function(me, value) {
        var checkItem = this.getItem(this.currentRecord);

        this._checkItemsFromValueSet(checkItem);
        this._ignoreUncheckedTests();
        this._updateCookieValue();
    },

    /**
     * @private
     * Uncheck "All" if a single test is selected.  If no tests are selected,
     * check "All".  If "All" is checked, uncheck the other tests.
     */
    _checkItemsFromValueSet: function(selectedItem) {
        var allItem = this._getAllCheckItem(),
            isAllChecked = selectedItem === allItem;
            isATestChecked = false;

        this.items.each(function(item) {
            if(item !== allItem) {
                //uncheck everything if the all item is checked
                if(isAllChecked) {
                    item.setChecked(false);
                }
                if(item.checked) {
                    isATestChecked = true;
                }
            }
        });
        //if all is checked select the all, if no tests are checked, check them all
        //uncheck the all if a test is checked
        allItem.setChecked(isAllChecked || !isATestChecked);
    },

    /**
     * @private
     * Ignore tests based on what is selected.
     */
    _ignoreUncheckedTests: function() {
        var ignore = [], isAllItemChecked = this._getAllCheckItem().checked;

        this.items.each(function(item) {
            if(!isAllItemChecked && !item.checked) {
                ignore.push(item.record.get('testCase'));
            }
        }, this);

        CSUnit.ignoreTestCases(ignore);
    },

    _updateCookieValue: function() {
        var checkedTestNames = [];
        this.items.each(function(item) {
            if(item.checked) {
                checkedTestNames.push(item.record.get('name'));
            }
        });
        Ext.util.Cookies.set(this._cookieName, Ext.JSON.encode(checkedTestNames));
    },

    /**
     * @private
     * @return Return the "All" check item.
     */
    _getAllCheckItem: function(){
        return  this.items.first();
    }
});
//////////////////////
// ..\js\testing\CSUnitPanel.js
//////////////////////
/**
 *
 * This panel can be used to display jsunit tests inside a taskflow.
 * If you are using this panel, do not load the REFS csunit stash
 * library. This will give you the {@link RP.testing.CSUnit unit
 * testing facility} that works inside a taskflow, as well as this
 * panel for containing the testing results and the required YUI
 * libraries.  In addition, make sure you don't include your test
 * suites in the scriptUrls for your task.  All tests must be included
 * when CSUnit throws the loadTests event which is always fired after
 * you call RP.testing.CSUnit.init().
 *
 * Example:
 *
 *      Ext.define('RPUX.sample.JsUnit', {
 *          alias: ['sample-jsunit', 'widget.sample-jsunit'],
 *          extend: 'RP.taskflow.BaseTaskflowWidget',
 *
 *          initComponent: function() {
 *              Ext.apply(this, {
 *                  uiTitle: 'RPUX JS Unit tests'
 *              });
 *              this.callParent(arguments);
 *          },
 *
 *          createTaskForm: function() {
 *              this.taskForm = Ext.ComponentMgr.create({
 *                  title: 'RPUX JS Unit tests',
 *                  xtype: 'rptaskform',
 *                  layout: 'fit',
 *                  items: [{ xtype: 'csunittestingpanel' }]
 *              });
 *
 *              var CSUnit = RP.testing.CSUnit;
 *
 *              CSUnit.testing.CSUnit.on('loadTests', function() {
 *                  CSUnit.setRoot('/web/');
 *                  CSUnit.include(['RPUX/RPUX.tests.{client-mode}.js']);
 *              });
 *              CSUnit.init();
 *          }
 *      });
 *
 *      RP.registerWidget({...});
 *
 * @author Jeff Gitter
 * @docauthor Jeff Gitter
 */
Ext.define("RP.testing.CSUnitPanel", {
    extend: "Ext.panel.Panel",

    alias: "widget.csunittestingpanel",

    testsRan: false,

    initComponent: function() {
        Ext.apply(this, {
            xtype: 'container',
            layout: 'fit',
            id: 'CSUnitPanel',
            tbar: new Ext.Toolbar({
                layout: {
                    pack: 'center'
                },
                items: [
                    this.buildRunningTestsButton(),
                    this.buildRunTestsButton(),
                    this.buildInstrumentFilesButton(),
                    this.buildGetCoverageReportButton()
                ]
            }),
            listeners: {
                scope: this,
                afterrender: function() {
                    CSUnit.init();
                }
            },
            items: [{
                xtype: 'container',
                id: 'TestConsole',
                cls: 'yui3-skin-sam yui-skin-sam',
                listeners: {
                    // for some reason, width auto-updates, but not height.  Go YUI.
                    resize: function(cnt, width, height) {
                        if(CSUnit.yconsole) {
                            CSUnit.yconsole.set('height', height * 0.99);
                        }
                    }
                }
            }]
        });

        this.callParent(arguments);
    },

    onRunTestClick: function() {
        RP.util.ScriptLoader.onReady(Ext.bind(function() {
            this.testsRan = true;
            CSUnit.runTests();
        }, this));
    },

    onGetCoverageClick: function() {
        var params = Ext.urlDecode(window.location.search.substr(1));
        var coverageJSON = CSUnit.getCoverage();
        var file = Ext.isEmpty(params.outputfile) ? this.defaultReportLocation : params.outputfile;
        if (!this.testsRan) {
            Ext.Msg.show({
                title: "Coverage Report",
                msg: "You must run the tests to get the coverage data first."
            });
        }
        else if (Ext.isEmpty(coverageJSON)) {
            Ext.Msg.show({
                title: "Coverage Report",
                msg: "No coverage data was found. The project needs to be built (instrumented) for jsunit coverage."
            });
        }
        else {
            var mask = new Ext.LoadMask(Ext.getBody());
            mask.show();
            Ext.Ajax.request({
                url: '/data/csunit/report',
                params: {
                    reportJSON: coverageJSON,
                    outputfile: file
                },
                success: function(response) {
                    mask.hide();
                    var reportLocation = Ext.JSON.decode(response.responseText).data;
                    var url = window.location.protocol + "//" + window.location.host + reportLocation;
                    RP.util.Helpers.openWindow(url);
                },
                failure: function() {
                    mask.hide();
                }
            });
        }
    },

    buildRunningTestsButton: function() {
        return Ext.create('Ext.button.Button', {
            text: 'Tests to run',
            disabled: true,
            width: 200,
            listeners: {
                afterrender: {
                    fn: function() {
                        this.menu = new RP.testing.TestSelector();
                        this.enable();
                    },
                    //Do a delay here cause the tests and YUI code are 
                    //loaded async
                    delay: 2000
                }
            }
        });
    },

    buildRunTestsButton: function() {
        return Ext.create('Ext.button.Button', {
            text: 'Run Tests',
            width: 200,
            handler: this.onRunTestClick,
            scope: this
        });
    },

    buildInstrumentFilesButton: function() {
        return Ext.create('Ext.button.Button', {
            text: 'Instrument Files',
            handler: this.onInstrumentFilesClick,
            width: 200,
            scope: this
        });
    },

    buildGetCoverageReportButton: function() {
        return Ext.create('Ext.button.Button', {
            text: 'Get Coverage Report',
            width: 200,
            handler: this.onGetCoverageClick,
            scope: this
        });
    },

    onInstrumentFilesClick: function() {
        var loadFilesFn = function(files) {
                var win = this.getInstrumentedFilesWindow(),
                    grid = win.grid,
                    store = grid.getStore();

                store.loadData(files);

                if (grid.rendered) {
                    selectInstrumented(grid);
                }
                else {
                    grid.on('afterrender', Ext.pass(selectInstrumented, [grid], this), this);
                }

                win.show();
                mask.hide();
            },
            selectInstrumented = function(grid) {
                grid.getStore().each(function(record) {
                    if (record.get('instrumented')) {
                        grid.getSelectionModel().select(record, true);
                    }
                }, this);
            },
            mask = new Ext.LoadMask(Ext.getBody());

        mask.show();
        
        RP.testing.Instrument.listInstrumentableFiles(loadFilesFn, this);
    },

    getInstrumentedFilesWindow: function() {
        var grid, win, store,
            selectInstrumented;

        if (!this._instrumentedFilesWindow) {
            grid = this.buildInstrumentedFilesGrid();
            this._instrumentedFilesWindow = Ext.ComponentManager.create({
                    title: 'Select Files to Instrument',
                    modal: true,
                    closable: true,
                    layout: 'fit',
                    height: 600,
                    width: 900,
                    items: grid,
                    grid: grid,
                    buttons: this.buildInstrumentWindowButtons()
                }, 'window');
        }

        return this._instrumentedFilesWindow;
    },

    buildInstrumentedFilesGrid: function() {
        var grid = Ext.ComponentManager.create({
            tools: [{
                xtype: 'textfield',
                width: 200,
                emptyText: 'Filter By...',
                listeners: {
                    change: function(field, newval) {
                        var value;
                        try {
                            value = new RegExp(newval.replace(/\//g, '(\\/)'), 'i');
                            grid.getStore().clearFilter(true);
                            grid.getStore().filter([{
                                property:'fullPath',
                                value: new RegExp(newval.replace(/\//g, '(\\/)'), 'i')
                            }]);
                        }
                        catch(e) {
                            // don't filter if the filter value isnt a valid regex
                        }
                    }
                }
            }],
            columns: [{
                dataIndex: 'path',
                text: 'Path'
            }, {
                dataIndex: 'fileName',
                text: 'File Name'
            }],
            features: [{ftype:'grouping',  hideGroupedHeader: true}],
            selModel: {
                selType: 'checkboxmodel',
                mode: 'MULTI'
            },
            store: Ext.create('Ext.data.Store', {
                fields: ['parentUrl', 'fullPath', 'path', 'fileName', 'instrumented'],
                groupField: 'path',
                proxy: {
                    type: 'memory',
                    reader: {
                        type: 'json'
                    }
                },
                sorters: ['path', 'fileName']
            })
        }, 'grid');
        this.instrumentedFilesGrid = grid;
        return grid;
    },

    buildInstrumentWindowButtons: function() {
        return [{
            xtype: 'button',
            text: 'Instrument Files',
            handler: this.onDoFileInstrumentation,
            scope: this
        }, {
            xtype: 'button',
            text: 'Deinstrument All',
            handler: function() {
                RP.testing.Instrument.persistInstrumented([]);
                document.location.reload(true);
            },
            scope: this
        }, {
            xtype: 'button',
            text: 'Cancel',
            handler: function() {
                this.getInstrumentedFilesWindow().close();
            },
            scope: this
        }];
    },

    onDoFileInstrumentation: function() {
        var sm = this.getInstrumentedFilesWindow().grid.getSelectionModel(),
            selection = sm.getSelection(),
            files = Ext.Array.map(selection, function(record) {
                    return record.get('fullPath');
                }),
            mask = new Ext.LoadMask(Ext.getBody());
            
        mask.show();

        Ext.Ajax.request({
            url: '/data/csunit/instrumentFiles',
            params: {
                files: Ext.JSON.encode(files)
            },
            success: function() {
                mask.hide();
                RP.testing.Instrument.persistInstrumented(files);
                document.location.reload(true);
            },
            failure: function() {
                mask.hide();
                alert("problem instrumenting files");
            }
        });
    }
});
//////////////////////
// ..\js\util\MessageBox.js
//////////////////////
/**
 * @class RP.util.MessageBox
 *
 * This is an extension of the Ext.window.MessageBox that provides some static factory
 * methods for building commonly used versions of the message box
 *
 * For a singleton instance of this class, use RPUX.Msg
 *
 * @author Jeff Gitter
 */
Ext.define('RP.util.MessageBox', {
    alternateClassName: 'RP.Msg',

    statics: {
        /**
         * @static
         * Show a YES/NO prompt to the user. The MessageBox icon and buttons are defaulted to
         * Ext.Msg.QUESTION and Ext.Msg.YESNO. These defaults may be overridden by passing in your
         * own settings in the config object.
         *
         * @param {String} message The message to display with the prompt
         * @param {Object} config The configuration object.  See {@link Ext.Msg#show} for more information
         * @return {Ext.window.MessageBox} the message box
         */
        promptYesNo: function(message, config) {
            Ext.apply(config, {
                msg: message
            });

            return Ext.Msg.show(Ext.applyIf(config, {
                title: '',
                width: 450,
                cls: 'rp-message-box-yn',
                buttons: Ext.Msg.YESNO
            }));
        },

        /**
         * @static
         * Show a YES/NO/Cancel prompt to the user. The MessageBox icon and buttons are defaulted to
         * Ext.Msg.QUESTION and Ext.Msg.YESNOCANCEL. These defaults may be overridden by passing in your
         * own settings in the config object.
         *
         * @param {String} message The message to display with the prompt
         * @param {Object} config The configuration object.  See {@link Ext.Msg#show} for more information
         * @return {Ext.window.MessageBox} the message box
         */
        promptYesNoCancel: function(message, config) {
            Ext.apply(config, {
                msg: message
            });

            return Ext.Msg.show(Ext.applyIf(config, {
                title: '',
                width: 450,
                cls: 'rp-message-box-ync',
                buttons: Ext.Msg.YESNOCANCEL
            }));
        },

        /**
         * @static
         * Show a OK/Cancel prompt to the user. The MessageBox icon and buttons are defaulted to
         * Ext.Msg.QUESTION and Ext.Msg.OKCANCEL. These defaults may be overridden by passing in your
         * own settings in the config object.
         *
         * @param {String} message The message to display with the prompt
         * @param {Object} config The configuration object.  See {@link Ext.Msg#show} for more information
         * @return {Ext.window.MessageBox} the message box
         */
        promptOkCancel: function(message, config) {
            Ext.apply(config, {
                msg: message
            });

            return Ext.Msg.show(Ext.applyIf(config, {
                title: '',
                width: 450,
                cls: 'rp-message-box-oc',
                buttons: Ext.Msg.OKCANCEL
            }));
        },

        promptUnsavedChanges: function(config) {
            this.promptYesNoCancel(
                RP.getMessage('rp.common.misc.unsavedChangesMsg'), config);
        }
    }
}, function() {
    Ext.Msg.msgButtons.yes.setPriority(Ext.button.Button.priority.HIGH);
    Ext.Msg.msgButtons.cancel.setPriority(Ext.button.Button.priority.LOW);
});
