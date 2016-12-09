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
