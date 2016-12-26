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
                              xtype:'panel',
                              layout:'hbox',
                              border: false,
                              items:[
                                     {
                                         xtype:'textfield',
                                         fieldLabel: '用户名',
                                         name: 'au_name',
                                          width: 270,
                                          height:25,
                                          minLength:4,
                                          maxLength:10,
                                          vtype:'alpha',//alphanum,email,url.
                                          allowBlank: false
                                      },
                                     {
                                          text: '查询',
                                          xtype: 'button',
                                          name: 'au_find',
                                           handler: function()
                                           {
                                               Ext.Msg.alert('find clicked');
                                           }
                                       },
                                     ]
                               },
                               {
                                     fieldLabel: '密码',
                                     width:270,
                                     height:25,
                                     inputType:'password',
                                     name: 'au_pswd',
                                   allowBlank: false
                               },
                               {
                                   fieldLabel: '确认密码',
                                   width:270,
                                   height:25,
                                   inputType:'password',
                                   name: 'au_pswd2',
                                 allowBlank: false
                             },
                               {
                                 fieldLabel:'超级用户',
                                 xtype:'checkbox',
                                 name:'sup_usr_flg',
                                 width:270,
                                 height:25
                               }
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
                                 au_mntuser.submit(
                                    {
                                                url: HOST_STRING,
                                                params:{Query: 'create user where usr_id = 123 and usr_pswd=dsfds',
                                                  ResponseFormat:'json'},
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
                                                   
                                                   var jsonv = eval("("+action.response.responseText+")");
                                                   //Ext.Msg.alert('User', jsonv.values[0][0]);
                                                   //Ext.Msg.alert('Session_key', jsonv.values[0][1]);
                                                   env ="USR_ID="+jsonv.values[0][0] + ":SESSION_KEY="+jsonv.values[0][1];
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