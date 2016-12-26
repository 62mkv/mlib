function addUser(item, evt)
{
    var arg = arguments;
    //Ext.Msg.alert(item.text + " clicked, from addUser");
    console.log("new adduser...");
    var context_panel = Ext.getCmp("context_panel");
    var page_panel=Ext.getCmp("page_panel");
    context_panel.hide();
    //page_panel.remove(context_panel);
    var au_adduser = Ext.getCmp("au_adduser");
    if (!au_adduser)
    {
        console.log("now creating panel au_adduser..");
        var au= Ext.create('Ext.panel.Panel',
                {
                   title:'添加用户',
                   id: 'au_adduser',
                   width:page_panel.body.el.dom.clientWidth,
                   height:page_panel.body.el.dom.clientHeight,
                   layout:'vbox',
                   defaultType: 'textfield',
                   fieldDefaults: {
                       labelAlign:'left',
                       labelWidth: 50
                   },
                   items:[
                              {
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
                          text: '保存',
                          handler: function(a,b)
                          {
                             Ext.Msg.alert('Save clicked!');
                          }
                         },
                         {xtype:'button',
                          text: '取消',
                          handler: function(a,b)
                          {
                             Ext.getCmp("au_adduser").hide();
                             context_panel.show();
                          }
                         },
                         {xtype:'tbfill'}
                         ]
        });
        page_panel.add(au);
    }
    else {
        console.log("showing already created au_adduser panel...");
        au_adduser.show();
    }
    page_panel.doLayout();
}