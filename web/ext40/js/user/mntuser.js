/*
 * Copyright (c) 2016 Sam Corporation, All rights reserved.
 */
function mntUser(item, evt)
{
    var arg = arguments;
    //Ext.Msg.alert(item.text + " clicked, from addUser");
    console.log("new adduser...");
    var context_panel = Ext.getCmp("context_panel");
    var page_panel=Ext.getCmp("page_panel");
    context_panel.hide();
    //page_panel.remove(context_panel);
    var au_mntuser = Ext.getCmp("au_mntuser");
    if (!au_mntuser)
    {
        console.log("now creating panel au_adduser..");
        //create validation for password:
        Ext.apply(Ext.form.field.VTypes,
        		{
        	        password: function(val, field)
        	        {
        	        	var au_pswd2 = Ext.getCmp("au_pswd");
        	        	if (au_pswd2)
        	        	{
        	        		return val == au_pswd2.value;
        	        	}
        	        	else
        	        	{
        	        		return true;
        	        	}
        	        },
        	        passwordText: '两次输入的密码不同'
        		});

//        var md_usr = Ext.define('User',
//        		{
//        		extend: 'Ext.data.Model',
//        		fields: [
//        			{name: 'usr_id', type:'string'},
//        			{name:  'usr_pswd', type:'string'},
//        			{name:  'super_usr_flg', type: 'int'}
//        		]
//                });
        var modelAndCols = buildModeAndColumnsForCmd("list users", "User");
        var ds_usr = Ext.create('Ext.data.Store',
        		{
        	        model: modelAndCols.model,
        	        proxy: MLIB_XML_PROXY,
        	        autoLoad: false
        		});
        var grd_usr = Ext.create('Ext.grid.Panel',
        		{
        	        title: '用户',
                    width:page_panel.body.el.dom.clientWidth/2,
                    height:page_panel.body.el.dom.clientHeight/2,
        	        columns: modelAndCols.columns,
        	        store: ds_usr
        		})
        au_mntuser = Ext.create('Ext.form.Panel',
                {
                   title:'用户维护',
                   id: 'au_mntuser',
                   width:page_panel.body.el.dom.clientWidth,
                   height:page_panel.body.el.dom.clientHeight,
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
                                         fieldLabel: '用户名',
                                         id: 'au_name',
                                          width: 270,
                                          height:25,
                                          minLength:4,
                                          maxLength:10,
                                          emptyText: '请输入用户名',
                                          vtype:'alpha',
                                          allowBlank: false,
                                          blankText: '用户名不能为空'
                                      },
                                     {
                                          text: '查询',
                                          xtype: 'button',
                                          name: 'au_find',
                                           handler: function()
                                           {
                                               //Ext.Msg.alert('find clicked');
                                               var clause = "";
                                               var usid = Ext.getCmp("au_name");
                                               if (usid.value !== undefined && usid.value !== '')
                                               {
                                               	clause = " where usr_id ='" + usid.value + "'";
                                               }
                                               else
                                               {
                                               	clause = " ";
                                               }
                                               ds_usr.load({
                                            			    page: 3,
                                            			    limit: 90,
                                            			    params: {
                                            			    	Query: 'list users' + clause
                                            			    },
                                            		    callback: function (records, operation, success) {
                                            		    	//Ext.Msg.alert("callback called");
                                            		        if (success) {
                                            		            var msg = [];
                                            		            store.each(function (users) {
                                            		                users.get('column');
                                            		            });
                                            		        }
                                            		    }
                                               });
                                               //alert("hhhhh");
                                           }
                                       },
                                     ]
                               },
                               {
                                     fieldLabel: '密码',
                                     width:270,
                                     height:25,
                                     inputType:'password',
                                     id: 'au_pswd',
                                   allowBlank: false,
                                   blankText: '密码不能为空'
                               },
                               {
                                   fieldLabel: '确认密码',
                                   width:270,
                                   height:25,
                                   inputType:'password',
                                   id: 'au_pswd2',
                                   vtype: 'password',
                                 allowBlank: false,
                                 blankText: '请再次输入密码'
                             },
                               {
                                 fieldLabel:'超级用户',
                                 xtype:'checkbox',
                                 id:'sup_usr_flg',
                                 width:270,
                                 height:25
                               },
                               grd_usr
                          ],
                   bbar:[
                         {xtype:'tbfill'},
                         {xtype:'button',
                             text: '新增',
                             handler: function(a,b)
                             {
                                Ext.Msg.alert('New clicked!');
                             }
                            },
                         {xtype:'button',
                                text: '删除',
                                handler: function(a,b)
                                {
                                   Ext.Msg.alert('Del clicked!');
                                }
                               },
                         {xtype:'button',
                          text: '保存',
                          formBind: true,
                          handler: function(a,b)
                          {
                             Ext.Msg.alert('Save clicked!');
                             //alert("aaaaabbb");
                             if (au_mntuser.isValid())
                             {
                            	 var clause = " where usr_id ='" + Ext.getCmp("au_name").value
                            	            + "' and usr_pswd ='" + Ext.getCmp("au_pswd").value
                            	            + "' and super_usr_flg ='" + (Ext.getCmp("sup_usr_flg").value ? "1":"0") + "'";
                                 au_mntuser.submit(
                                    {
                                                url: HOST_STRING,
                                                params:{Query: 'create user' + clause,
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
                                                   switch(action.failureType)
                                                   {
                                                          case Ext.form.action.Action.CLIENT_INVALID:
                                                                  //Ext.Msg.alert('Fail1:', Ext.form.action.Action.CLIENT_INVALID);
                                                                  //break;
                                                          case Ext.form.action.Action.CONNECT_FAILURE:
                                                                  //Ext.Msg.alert('Fail2:', Ext.form.action.Action.CONNECT_FAILURE);
                                                                  //break;
                                                          case Ext.form.action.Action.SERVER_INVALID:
                                                                  //Ext.Msg.alert('Fail3:', Ext.form.action.Action.SERVER_INVALID);
                                                                  //break;
                                                   }
                                                   
                                                   //var jsonv = eval("("+action.response.responseText+")");
                                                   //Ext.Msg.alert('User', jsonv.values[0][0]);
                                                   //Ext.Msg.alert('Session_key', jsonv.values[0][1]);
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
                                                           Ext.Msg.alert("成功:", "用户:" + un[1].childNodes[0].nodeValue + "成功创建!");
                                                           au_mntuser.getForm().reset();
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
                         },
                         {xtype:'button',
                          text: '取消',
                          handler: function(a,b)
                          {
                             Ext.getCmp("au_mntuser").hide();
                             context_panel.show();
                          }
                         },
                         {xtype:'tbfill'}
                         ]
        });
        page_panel.add(au_mntuser);
    }
    else {
        console.log("showing already created au_adduser panel...");
        au_mntuser.show();
    }
    page_panel.doLayout();
}
