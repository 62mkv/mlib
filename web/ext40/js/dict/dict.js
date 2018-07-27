/*
 * Copyright (c) 2016 Sam Corporation, All rights reserved.
 */
function createDictionaryMenu()
{
                    var dictionarymenu = new Ext.create('Ext.menu.Menu',
                            {
                            width: '100%',
                            floating: false,
                            layout:'vbox',
                            items: [
                                {
                                    text: '字典维护',
                                    id: 'mntdic',
                                    width:'100%',
                                    handler: function(item, evt)
                                    {
                                        //alert(item.text + " clicked!");
                                        mntDictionary(item, evt);
                                    }
                                }
                            ]});
                            
                           dm = {
                                           id:'dictionary',
                                           xtype: 'panel',
                                           title: '字典',
                                           width:'20%',
                                           items: [
                                               dictionarymenu
                                               ]
                                       };
      return dm;
}

function mntDictionary(item, evt)
{
    var arg = arguments;
    //Ext.Msg.alert(item.text + " clicked, from addUser");
    console.log("new dictionary...");
    var context_panel = Ext.getCmp("context_panel");
    var page_panel=Ext.getCmp("page_panel");
    context_panel.hide();
    //page_panel.remove(context_panel);
    var fp_dict = Ext.getCmp("fp_dict");
    if (!fp_dict)
    {
        console.log("now creating panel fp_dict..");

        var dictModelAndCols = buildModeAndColumnsForCmd("[select * from dict where 1=2]", "Dictionary");
        var ds_dict = Ext.create('Ext.data.Store',
        		{
        	        model: dictModelAndCols.model,
        	        proxy: MLIB_XML_PROXY,
                    pageSize: 2,
        	        autoLoad: false
        		});
            ds_dict.on("load",
            		  function(ds_dict)
            		  {
            	         if (ds_dict.data.items.length > 0)
            	         {
           	                  var savebtn = Ext.getCmp("bbar_save_dict");
        	                  savebtn.enable();
            		     }
            	         else
            	         {
          	                  var savebtn = Ext.getCmp("bbar_save_dict");
        	                  savebtn.disable();
            	         }
            		  });
        var localeStore = {
            fields: ['locale', 'language'],
            data: [{locale: 'US_ENGLISH', language: 'English'},
                   {locale: 'SIMPLIFIED_CHINESE', language: 'Chinese'}]
        };
        
        var pn_dict = Ext.create('Ext.grid.Panel',
        		{
        	        title: '字典',
                    width:page_panel.body.el.dom.clientWidth * 8/10,
                    //width:'100%',
                    height:page_panel.body.el.dom.clientHeight * 2/3,
                    bodyPadding:5,
                    autoScroll: true,
        	        columns: dictModelAndCols.columns,
        	        store: ds_dict,
                    listeners:{
                                   itemdblclick: function(grid, row, e){
                                        Ext.Msg.alert('rowdblclick');
                                   },
                                   itemclick: function(grid, row, e){
                                       Ext.getCmp("dict_id").setValue(row.data["dict_id"]);
                                       Ext.getCmp("locale_id").setValue(row.data["locale_id"]);
                                       Ext.getCmp("frm_id").setValue(row.data["frm_id"]);
                                       Ext.getCmp("vartn").setValue(row.data["vartn"]);
                                       Ext.getCmp("srtseq").setValue(row.data["srtseq"]);
                                       Ext.getCmp("dict_text").setValue(row.data["dict_text"]);
                                   }
                             }
        		})
        fp_dict = Ext.create('Ext.form.Panel',
                {
                   title:'字典维护',
                   id: 'fp_dict',
                   width:'100%',
                   height:'100%',
                   layout:'vbox',
                   bodyPadding:5,
                    autoScroll: true,
        	        columns: dictModelAndCols.columns,
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
                                         fieldLabel: '字典ID',
                                         id: 'dict_id',
                                          width: 270,
                                          height:25,
                                          maxLength:30,
                                          emptyText: '请输入字典ID',
                                          //vtype:'alpha',
                                          allowBlank: false,
                                          blankText: '字典ID不能为空'
                                      },
                                     {
                                          text: '查询',
                                          xtype: 'button',
                                          id: 'find_dict',
                                          listeners: {
                                           click: function()
                                           {
                                               //Ext.Msg.alert('find clicked');
                                               var clause = "";
                                               var dict_id = Ext.getCmp("dict_id");
                                               if (dict_id.value !== undefined && dict_id.value !== '')
                                               {
                                                    var idx = dict_id.value.indexOf('%');
                                                    if (idx >= 0) {
                                                        clause = " where dict_id like '" + dict_id.value + "'";
                                                    }
                                                    else {
                                               	        clause = " where dict_id = '" + dict_id.value + "'";
                                                    }
                                               }
                                               else
                                               {
                                               	clause = " ";
                                               }
                                               ACTIVE_MODEL = dictModelAndCols.model;
                                               ds_dict.load({
                                            			    page: 3,
                                            			    limit: 90,
                                            			    params: {
                                            			    	Query: '[select * from dict ' + clause + ']'
                                            			    },
                                            		    callback: function (records, operation, success) {
                                            		    	//Ext.Msg.alert("callback called");
                                            		        if (success) {
                                            		            var msg = [];
                                            		            ds_dict.each(function (dict) {
                                            		                //dict.get('dict_text');
                                            		            });
                                            		        }
                                            		    }
                                               });
                                               //alert("hhhhh");
                                           }
                                          }
                                       },
                                     ]
                               },
                               {
                                     fieldLabel: '语言',
                                     width:270,
                                     height:25,
                                     id: 'locale_id',
                                     xtype: 'combo',
                                     valueField: 'locale',
                                     displayField: 'language',
                                   allowBlank: false,
                                   forceSelection: true,
                                   store: localeStore,
                                   listeners : {
                                                  afterRender : function(combo) {
                                                  var firstValue = localeStore.data[0].locale;
                                                  combo.setValue(firstValue);
                                               }
                                   }
                               },
                               {
                                   fieldLabel: 'Form ID',
                                   width:270,
                                   height:25,
                                   id: 'frm_id',
                                   value: 'ALL',
                                 allowBlank: false
                             },
                               {
                                 fieldLabel:'variation',
                                 id:'vartn',
                                 width:270,
                                 height:25,
                                 value: '1'
                               },
                               {
                                 fieldLabel:'Sequence',
                                 id:'srtseq',
                                 value: '1',
                                 width:270,
                                 height:25
                               },
                               {
                                 fieldLabel:'Customization Level:',
                                 id:'cust_lvl',
                                 value: '0',
                                 width:270,
                                 height:25
                               },
                               {
                                 xtype: 'textarea',
                                 fieldLabel:'Text',
                                 id:'dict_text',
                                 width:270,
                                 height:50
                               },
                               {xtype: 'splitter'},
                               pn_dict
                          ],
                   bbar:[
                    //     {xtype:'pagingtoolbar',
                    //      store: ds_dict,
                   //       displayInfor: true
                  //        },
                         {xtype:'tbfill'},
                         {xtype:'button',
                             text: '清除',
                             id: 'bbar_clear_dict',
                             handler: function(a,b)
                             {
                            	 ds_dict.removeAll();
                            	 fp_dict.getForm().reset();
                                 Ext.getCmp('locale_id').setValue(localeStore.data[0].locale);
                            	 //var savebtn = Ext.getCmp("bbar_save_dict");
                            	 //savebtn.disable();
                             }
                            },
                         {xtype:'button',
                                text: '删除',
                                id: 'bbar_delete_dict',
                                handler: function(a,b)
                                {
                                	var dict_id_str = Ext.getCmp("dict_id").value;
                                    if (dict_id_str !== undefined && dict_id_str !== '')
                                    {
                                   	    var clause = " where dict_id ='" + dict_id_str + "']";
                                   	    Ext.Ajax.request(
                                        {
                                               url: HOST_STRING,
                                               params:{
                                                	   Query: '[delete from dict ' + clause,
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
                                                           Ext.Msg.alert("成功删除:", "字典:" + dict_id_str + "!");
                                                           ds_dict.removeAll();
                                                           fp_dict.getForm().reset();
                                                       }
                                                   }
                                               }
                                         });
                                    }
                                    else {
                                   	 Ext.Msg.alert('错误','请指定字典ID！');
                                    }
                                }
                               },
                         {xtype:'button',
                          text: '保存',
                          id: 'bbar_save_dict',
                          //formBind: true,
                          handler: function(a,b)
                          {
                             if (fp_dict.isValid())
                             {
                             var goflg = false;
                             var cmd = 'create dictionary ';
                        	 var clause = " where dict_id ='" + Ext.getCmp("dict_id").value
                        	            + "' and frm_id ='" + Ext.getCmp("frm_id").value
                        	            + "' and locale_id ='" + Ext.getCmp("locale_id").value
                                        + "' and vartn = '" + Ext.getCmp("vartn").value
                                        + "' and cust_lvl = '"  + Ext.getCmp("cust_lvl").value
                                        + "' and srtseq = '" + Ext.getCmp("srtseq").value + "'";
                              var res = executeQuery('[select \'x\' from dict ' + clause + ' and rownum < 2]');
                              if (res.rowcount > 0)
                              {
                                    Ext.Msg.confirm('Information:', '字典已经存在，要保存吗?', function(op){
                                        if(op == 'yes') {
                                            goflg = false; 
                                            cmd = 'change dictionary ';
                                            fp_dict.submit(
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
                                                                   Ext.Msg.alert("成功:", "字典:" + un[1].childNodes[0].nodeValue + "成功修改!");
                                                                   Ext.getCmp('find_dict').fireEvent('click');
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
                                  clause += " and dict_text = '" + Ext.getCmp("dict_text").value + "'";
                              if (goflg == true) {
                                 fp_dict.submit(
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
                                                           Ext.Msg.alert("成功:", "字典:" + un[1].childNodes[0].nodeValue + "成功创建!");
                                                           Ext.getCmp('find_dict').fireEvent('click');
                                                       }
                                                   }
                                            },
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
                          id: 'bbar_cancel_dict',
                          handler: function(a,b)
                          {
                             Ext.getCmp("fp_dict").hide();
                             context_panel.show();
                          }
                         },
                         {xtype:'tbfill'}
                         ]
        });
   	    var savebtn = Ext.getCmp("bbar_save_dict");
	    savebtn.disable();
        if (ACTIVE_RIGHT_PANEL != undefined)
        {
            ACTIVE_RIGHT_PANEL.hide();
        }
        ACTIVE_RIGHT_PANEL = fp_dict;
        page_panel.add(fp_dict);
    }
    else {
        console.log("showing already created fp_dict panel...");
        if (ACTIVE_RIGHT_PANEL != fp_dict)
        {
            ACTIVE_RIGHT_PANEL.hide();
        }
        ACTIVE_RIGHT_PANEL = fp_dict;
        fp_dict.show();
    }
    page_panel.doLayout();
}

