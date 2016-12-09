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