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