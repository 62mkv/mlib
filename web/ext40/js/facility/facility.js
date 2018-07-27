/*
 * Copyright (c) 2016 Sam Corporation, All rights reserved.
 */


function createFacilityMenu()
{
  //Ext.require('js/facility/wh');
  //  Ext.require('js/facility/building', function() {
  //     alert('loaded building.js'); 
  //  });
    var facilitymenu = new Ext.create('Ext.menu.Menu',
            {
            width: '100%',
            floating: false,
            layout:'vbox',
            items: [
                {
                    text: '仓库维护',
                    id: 'mntwh',
                    width:'100%',
                    handler: function(item, evt)
                    {
                        //alert(item.text + " clicked!");
                        mntWH(item, evt);
                    }
                },
                //{
                //    xtype: 'menuseparator'
                //},
                {
                    text: '建筑维护',
                    id: 'mntbldg',
                    width:'100%',
                    handler: function(item, evt)
                    {
                        //alert(item.text + ' was clicked!');
                        mntBldg(item, evt);
                    }
                },
                  {
                    text: '区域维护',
                    id: 'mntarea',
                    width:'100%',
                    handler: function(item, evt)
                    {
                        alert(item.text + ' was clicked!');
                    }
                },
                   {
                    text: '地点维护',
                    id: 'mntloc',
                    width:'100%',
                    handler: function(item, evt)
                    {
                        alert(item.text + ' was clicked!');
                    }
                }
            ]});
                            
           fm = {
                           id:'facility',
                           xtype: 'panel',
                           title: '设施',
                           items: [
                               facilitymenu
                               ]
                       };
return fm;
}
