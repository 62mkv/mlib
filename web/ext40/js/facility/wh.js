function mntWH(item, evt)
{
    var arg = arguments;
    //Ext.Msg.alert(item.text + " clicked, from addUser");
    console.log("new mntWH...");
    var context_panel = Ext.getCmp("context_panel");
    var page_panel=Ext.getCmp("page_panel");
    context_panel.hide();
    //page_panel.remove(context_panel);
    var fp_wh = Ext.getCmp("fp_wh");
    if (!fp_wh)
    {
        console.log("now creating panel fp_wh...");
        var whModelAndCols = buildModeAndColumnsForCmd("list warehouses", "Warehouse");
        var ds_wh = Ext.create('Ext.data.Store',
                {
                    model: whModelAndCols.model,
                    proxy: MLIB_XML_PROXY,
                    autoLoad: false
                });
            ds_wh.on("load",
                      function(ds_wh)
                      {
                         if (ds_wh.data.items.length > 0)
                         {
                              var savebtn = Ext.getCmp("bbar_save_wh");
                              //savebtn.enable();
                         }
                         else
                         {
                              var savebtn = Ext.getCmp("bbar_save_wh");
                              //savebtn.disable();
                         }
                      });
        var grd_wh = Ext.create('Ext.grid.Panel',
                {
                    title: '仓库',
                    width:page_panel.body.el.dom.clientWidth,
                    height:page_panel.body.el.dom.clientHeight * 2/3,
                    bodyPadding:5,
                    columns: whModelAndCols.columns,
                    store: ds_wh,
                    listeners:{
                       itemdblclick: function(grid, row, e){
                            Ext.Msg.alert('rowdblclick');
                       },
                       itemclick: function(grid, row, e){
                           Ext.getCmp("wh_id").setValue(row.data["wh_id"]);
                           Ext.getCmp("adr_id").setValue(row.data["adr_id"]);
                       }
                    }
                });
                
        fp_wh = Ext.create('Ext.form.Panel',
                {
                   title:'仓库维护',
                   id: 'fp_wh',
                   width:'100%',
                   height:'100%',
                   layout:'vbox',
                   bodyPadding:5,
                   defaultType: 'textfield',
                   fieldDefaults: {
                       labelAlign:'left',
                       labelWidth: 100
                   },
                   items:[
                          {
                              xtype:'fieldcontainer',
                              layout:'hbox',
                              border: false,
                              items:[
                                     {
                                         xtype:'textfield',
                                         fieldLabel: '仓库名',
                                         id: 'wh_id',
                                          width: 270,
                                          height:25,
                                          minLength:4,
                                          maxLength:10,
                                          emptyText: '请输入仓库名',
                                          vtype:'alpha',
                                          allowBlank: false,
                                          blankText: '仓库名不能为空'
                                      },
                                     {
                                          text: '查询',
                                          xtype: 'button',
                                          id: 'wh_find',
                                          listeners: {
                                           click: function()
                                           {
                                               //Ext.Msg.alert('find clicked');
                                               var clause = "";
                                               var wh_id = Ext.getCmp("wh_id");
                                               if (wh_id.value !== undefined && wh_id.value !== '')
                                               {
                                                clause = " where wh_id ='" + wh_id.value + "'";
                                               }
                                               else
                                               {
                                                clause = " ";
                                               }
                                               ACTIVE_MODEL = whModelAndCols.model;
                                               ds_wh.load({
                                                            page: 3,
                                                            limit: 90,
                                                            params: {
                                                                Query: 'list warehouses' + clause
                                                            },
                                                        callback: function (records, operation, success) {
                                                            //Ext.Msg.alert("callback called");
                                                            if (success) {
                                                                var msg = [];
                                                                ds_wh.each(function (users) {
                                                                    //users.get('column');
                                                                });
                                                            }
                                                        }
                                               });
                                           }
                                          }
                                       }
                                     ]
                               },
                               {
                                     xtype: 'textarea',
                                     fieldLabel: '地址',
                                     width:270,
                                     height:125,
                                     id: 'adr_id',
                                   allowBlank: false,
                                   blankText: '地址不能为空'
                               },
                               grd_wh
                          ],
                   bbar:[
                         {xtype:'tbfill'},
                         {xtype:'button',
                             text: '清除',
                             id: 'bbar_clear_wh',
                             handler: function(a,b)
                             {
                                 ds_wh.removeAll();
                                 fp_wh.getForm().reset();
                                 var savebtn = Ext.getCmp("bbar_save_wh");
                             }
                            },
                            {xtype:'button',
                                text: '删除',
                                id: 'bbar_delete_wh',
                                handler: function(a,b)
                                {
                                    var wh_id_str = Ext.getCmp("wh_id").value;
                                    if (wh_id_str !== undefined && wh_id_str !== '')
                                    {
                                        var clause = " where wh_id ='" + wh_id_str + "'";
                                        Ext.Ajax.request(
                                        {
                                               url: HOST_STRING,
                                               params:{
                                                       Query: 'remove warehouse' + clause,
                                                       ResponseFormat:'xml'
                                                      },
                                               success: function(resp,opts)
                                               {
                                                   var xmlDoc = resp.responseXML;
                                                   if(xmlDoc!=null)
                                                   {
                                                       var status = xmlDoc.getElementsByTagName("status");
                                                       var msg = xmlDoc.getElementsByTagName("message");
                                                       if (status && status[0].childNodes[0].nodeValue != 0)
                                                       {
                                                           Ext.Msg.alert(status[0].childNodes[0].nodeValue, msg[0].childNodes[0].nodeValue);
                                                       }
                                                       else
                                                       {
                                                           Ext.Msg.alert("成功删除:", "仓库:" + wh_id_str + "!");
                                                           ds_wh.removeAll();
                                                           fp_wh.getForm().reset();
                                                           Ext.getCmp('wh_find').fireEvent('click');
                                                       }
                                                   }
                                               }
                                         });
                                    }
                                    else {
                                     Ext.Msg.alert('错误','请指定仓库ID！');
                                    }
                                }
                               },
                         {xtype:'button',
                          text: '保存',
                          id: 'bbar_save_wh',
                          //formBind: true,
                          handler: function(a,b)
                          {
                             //Ext.Msg.alert('Save clicked!');
                             //alert("aaaaabbb");
                             if (fp_wh.isValid())
                             {
                                 var goflg = false;
                                 var clause = " where wh_id ='" + Ext.getCmp("wh_id").value + "'";
                                 var res = executeQuery('[select \'x\' from wh ' + clause + ' and rownum < 2]');
                              if (res.rowcount > 0)
                              {
                                    Ext.Msg.confirm('Information:', '仓库已经存在，要保存吗?', function(op){
                                        if(op == 'yes') {
                                            goflg = false; 
                                            cmd = 'change warehouse ';
                                            fp_wh.submit(
                                            {
                                                    url: HOST_STRING,
                                                    params:{Query: cmd + clause,
                                                    ResponseFormat:'xml'},
                                                    method: 'POST',
                                                    submitEmptyText:false,
                                                    success: function(form, action)
                                                    {
                                                           Ext.Msg.alert('Success');
                                                    },
                                                    failure: function(form, action)
                                                    {
                                                           var xmlDoc = action.response.responseXML;
                                                           if(xmlDoc!=null)
                                                           {
                                                               var status = xmlDoc.getElementsByTagName("status");
                                                               var msg = xmlDoc.getElementsByTagName("message");
                                                               if (status && status[0].childNodes[0].nodeValue != 0)
                                                               {
                                                                   Ext.Msg.alert(status[0].childNodes[0].nodeValue, msg[0].childNodes[0].nodeValue);
                                                               }
                                                               else
                                                               {
                                                                   var un = xmlDoc.getElementsByTagName("field");
                                                                   Ext.Msg.alert("成功:", "仓库:" + un[1].childNodes[0].nodeValue + "成功修改!");
                                                                   Ext.getCmp('wh_find').fireEvent('click');
                                                                   //fp_dict.getForm().reset();
                                                               }
                                                           }
                                                    },
                                                });
                                        }
                                        else {
                                            goflg = false;
                                        }
                                    });
                              }
                              else {
                                goflg = true;
                              }
                              clause += " and adr_id ='" + Ext.getCmp("adr_id").value + "'";
                              if (goflg == true) {
                                 fp_wh.submit(
                                    {
                                                url: HOST_STRING,
                                                params:{Query: 'create warehouse' + clause,
                                                  ResponseFormat:'xml'},
                                                method: 'POST',
                                                submitEmptyText:false,
                                            //reader:Ext.data.reader.Xml,
                                            success: function(form, action)
                                            {
                                                   Ext.Msg.alert('Success');
                                            },
                                            failure: function(form, action)
                                            {
                                                   var xmlDoc = action.response.responseXML;
                                                   if(xmlDoc!=null)
                                                   {
                                                       var status = xmlDoc.getElementsByTagName("status");
                                                       var msg = xmlDoc.getElementsByTagName("message");
                                                       if (status && status[0].childNodes[0].nodeValue != 0)
                                                       {
                                                           Ext.Msg.alert(status[0].childNodes[0].nodeValue, msg[0].childNodes[0].nodeValue);
                                                       }
                                                       else
                                                       {
                                                           var un = xmlDoc.getElementsByTagName("field");
                                                           Ext.Msg.alert("成功:", "仓库:" + un[1].childNodes[0].nodeValue + "成功创建!");
                                                           Ext.getCmp('wh_find').fireEvent('click');
                                                       }
                                                   }
                                            },
                                            callback:function(p1,p2,p3)
                                            {
                                                    Ext.Msg.alert('completed','ccc');
                                            }
                                        });
                                     }
                                 }
                             else {
                                 Ext.Msg.alert('错误','数据不全！');
                             }
                          }
                         },
                         {xtype:'button',
                          text: '取消',
                          id: 'bbar_cancel_wh',
                          handler: function(a,b)
                          {
                             Ext.getCmp("fp_wh").hide();
                             context_panel.show();
                          }
                         },
                         {xtype:'tbfill'}
                         ]
        });
        var savebtn = Ext.getCmp("bbar_save_wh");
        if (ACTIVE_RIGHT_PANEL != undefined)
        {
            ACTIVE_RIGHT_PANEL.hide();
        }
        ACTIVE_RIGHT_PANEL = fp_wh;
        page_panel.add(fp_wh);
    }
    else {
        console.log("showing already created fp_wh panel...");
        if (ACTIVE_RIGHT_PANEL != fp_wh)
        {
            ACTIVE_RIGHT_PANEL.hide();
        }
        ACTIVE_RIGHT_PANEL = fp_wh;
        fp_wh.show();
    }
    page_panel.doLayout();
}