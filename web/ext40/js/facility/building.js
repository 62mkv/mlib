function mntBldg(item, evt)
{
    var arg = arguments;
    //Ext.Msg.alert(item.text + " clicked, from addUser");
    console.log("new mntBLDG...");
    var context_panel = Ext.getCmp("context_panel");
    var page_panel=Ext.getCmp("page_panel");
    context_panel.hide();
    //page_panel.remove(context_panel);
    var fp_bldg = Ext.getCmp("fp_bldg");
    if (!fp_bldg)
    {
        console.log("now creating panel fp_bldg...");
        var bldgModelAndCols = buildModeAndColumnsForCmd("list buildings", "Building");
        var ds_bldg = Ext.create('Ext.data.Store',
                {
                    model: bldgModelAndCols.model,
                    proxy: MLIB_XML_PROXY,
                    autoLoad: false
                });
            ds_bldg.on("load",
                      function(ds_bldg)
                      {
                         if (ds_bldg.data.items.length > 0)
                         {
                              var savebtn = Ext.getCmp("bbar_save_bldg");
                              //savebtn.enable();
                         }
                         else
                         {
                              var savebtn = Ext.getCmp("bbar_save_bldg");
                              //savebtn.disable();
                         }
                      });
        var grd_bldg = Ext.create('Ext.grid.Panel',
                {
                    title: '楼栋',
                    width:page_panel.body.el.dom.clientWidth,
                    height:page_panel.body.el.dom.clientHeight * 2/3,
                    bodyPadding:5,
                    columns: bldgModelAndCols.columns,
                    store: ds_bldg,
                    listeners:{
                       itemdblclick: function(grid, row, e){
                            Ext.Msg.alert('rowdblclick');
                       },
                       itemclick: function(grid, row, e){
                           Ext.getCmp("bldg_id").setValue(row.data["bldg_id"]);
                           Ext.getCmp("bldg_adr_id").setValue(row.data["adr_id"]);
                       }
                    }
                })
        fp_bldg = Ext.create('Ext.form.Panel',
                {
                   title:'楼栋维护',
                   id: 'fp_bldg',
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
                             xtype:'textfield',
                             fieldLabel: '仓库',
                             id: 'bldg_wh_id',
                              width: 270,
                              height:25,
                              minLength:4,
                              maxLength:10,
                              value: 'HUIPING',
                              //vtype:'alpha',
                              allowBlank: false
                          },
                          {
                              xtype:'fieldcontainer',
                              layout:'hbox',
                              border: false,
                              items:[
                                     {
                                         xtype:'textfield',
                                         fieldLabel: '楼栋名',
                                         id: 'bldg_id',
                                          width: 270,
                                          height:25,
                                          minLength:4,
                                          maxLength:10,
                                          emptyText: '请输入楼栋名',
                                          //vtype:'alpha',
                                          allowBlank: false,
                                          blankText: '楼栋名不能为空'
                                      },
                                     {
                                          text: '查询',
                                          xtype: 'button',
                                          id: 'bldg_find',
                                          listeners: {
                                           click: function()
                                           {
                                               //Ext.Msg.alert('find clicked');
                                               var clause = "";
                                               var bldg_id = Ext.getCmp("bldg_id");
                                               var bldg_wh_id = Ext.getCmp("bldg_wh_id");
                                               if (bldg_id.value !== undefined && bldg_id.value !== '')
                                               {
                                                clause = " where bldg_id ='" + bldg_id.value + "'";
                                                clause += " and wh_id = '" + bldg_wh_id.value + "'";
                                               }
                                               else
                                               {
                                                clause = " ";
                                               }
                                               ACTIVE_MODEL = bldgModelAndCols.model;
                                               ds_bldg.load({
                                                            page: 3,
                                                            limit: 90,
                                                            params: {
                                                                Query: 'list buildings ' + clause
                                                            },
                                                        callback: function (records, operation, success) {
                                                            //Ext.Msg.alert("callback called");
                                                            if (success) {
                                                                var msg = [];
                                                                ds_bldg.each(function (users) {
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
                                     id: 'bldg_adr_id',
                                   allowBlank: false,
                                   blankText: '地址不能为空'
                               },
                               grd_bldg
                          ],
                   bbar:[
                         {xtype:'tbfill'},
                         {xtype:'button',
                             text: '清除',
                             id: 'bbar_clear_bldg',
                             handler: function(a,b)
                             {
                                 ds_bldg.removeAll();
                                 fp_bldg.getForm().reset();
                                 var savebtn = Ext.getCmp("bbar_save_bldg");
                             }
                            },
                            {xtype:'button',
                                text: '删除',
                                id: 'bbar_delete_bldg',
                                handler: function(a,b)
                                {
                                    var bldg_id_str = Ext.getCmp("bldg_id").value;
                                    var wh_id_str = Ext.getCmp("bldg_wh_id").value;
                                    if (bldg_id_str !== undefined && bldg_id_str !== '')
                                    {
                                        var clause = " where bldg_id ='" + bldg_id_str + "'";
                                            clause += " and wh_id = '" + wh_id_str + "'";
                                        Ext.Ajax.request(
                                        {
                                               url: HOST_STRING,
                                               params:{
                                                       Query: 'remove building ' + clause,
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
                                                           Ext.Msg.alert("成功删除:", "楼栋:" + bldg_id_str + "!");
                                                           ds_bldg.removeAll();
                                                           fp_bldg.getForm().reset();
                                                           Ext.getCmp('bldg_find').fireEvent('click');
                                                       }
                                                   }
                                               }
                                         });
                                    }
                                    else {
                                     Ext.Msg.alert('错误','请指定楼栋ID！');
                                    }
                                }
                               },
                         {xtype:'button',
                          text: '保存',
                          id: 'bbar_save_bldg',
                          //formBind: true,
                          handler: function(a,b)
                          {
                             //Ext.Msg.alert('Save clicked!');
                             //alert("aaaaabbb");
                             if (fp_bldg.isValid())
                             {
                                 var goflg = false;
                                 var clause = " where bldg_id ='" + Ext.getCmp("bldg_id").value + "'";
                                     clause += " and wh_id = '" + Ext.getCmp("bldg_wh_id").value + "'";
                                 var res = executeQuery('[select \'x\' from bldg ' + clause + ' and rownum < 2]');
                              if (res.rowcount > 0)
                              {
                                    Ext.Msg.confirm('Information:', '楼栋已经存在，要保存吗?', function(op){
                                        if(op == 'yes') {
                                            goflg = false; 
                                            cmd = 'change building ';
                                            fp_bldg.submit(
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
                                                                   Ext.Msg.alert("成功:", "楼栋:" + un[2].childNodes[0].nodeValue + "成功修改!");
                                                                   Ext.getCmp('bldg_find').fireEvent('click');
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
                              clause += " and adr_id ='" + Ext.getCmp("bldg_adr_id").value + "'";
                              if (goflg == true) {
                                 fp_bldg.submit(
                                    {
                                                url: HOST_STRING,
                                                params:{Query: 'create building ' + clause,
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
                                                           Ext.Msg.alert("成功:", "楼栋:" + un[2].childNodes[0].nodeValue + "成功创建!");
                                                           Ext.getCmp('bldg_find').fireEvent('click');
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
                          id: 'bbar_cancel_bldg',
                          handler: function(a,b)
                          {
                             Ext.getCmp("fp_bldg").hide();
                             context_panel.show();
                          }
                         },
                         {xtype:'tbfill'}
                         ]
        });
        var savebtn = Ext.getCmp("bbar_save_bldg");
        if (ACTIVE_RIGHT_PANEL != undefined)
        {
            ACTIVE_RIGHT_PANEL.hide();
        }
        ACTIVE_RIGHT_PANEL = fp_bldg;
        page_panel.add(fp_bldg);
    }
    else {
        console.log("showing already created fp_bldg panel...");
        if (ACTIVE_RIGHT_PANEL != fp_bldg)
        {
            ACTIVE_RIGHT_PANEL.hide();
        }
        ACTIVE_RIGHT_PANEL = fp_bldg;
        fp_bldg.show();
    }
    page_panel.doLayout();
}