<!DOCTYPE html>
<html>
<title>WMS</title>
<head>
<meta charset="utf-8" />
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
    <script language="javascript" type="text/javascript" src="ext-debug.js"></script>
    <script language="javascript" type="text/javascript" src="js/global/global.js"></script>
    <script language="javascript" type="text/javascript" src="js/util/util.js"></script>
    <script language="javascript" type="text/javascript" src="js/user/mntuser.js"></script>
    <link href="resources/css/ext-all.css" rel="stylesheet" type="text/css" />
</head>

<body>
    <script type="text/javascript">
        Ext.onReady(function(){
            //alert('aaaa');
            var loginForm= Ext.create('Ext.form.Panel',
                       {
                             title:'请登录',
                             bodyPadding:5,
                             width:400,
                             height:200,
                             shadow:true,
                             layout:'vbox',
                             defaults: {
                                 anchor: '100%'
                             },
                             defaultType: 'textfield',
                             fieldDefaults: {
                                 labelAlign:'left',
                                 labelWidth: 50
                             },
                             items:[
                                    {
                                        fieldLabel: '服务器',
                                        id: 'host',
                                         width: 370,
                                         height:25,
                                         placeholder:'请输入服务器地址',
                                         value:'http://localhost:4900/service',
                                         allowBlank: false
                                     },
                               {
                                  fieldLabel: '用户名',
                                  name: 'name',
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
                                     name: 'pswd',
                                   allowBlank: false
                               }
                              ],
                              buttons:[
                               {
                                   text:'重置',
                                     width:100,
                                     height:25,
                                   handler:function()
                                   {
                                       this.up('form').getForm().reset();
                                   }
                               },
                               {
                                   text:'登录',
                                   width:100,
                                   height:25,
                                   formBind:true,
                                   disabled:true,
                                   handler:function()
                                   {
                            //alert("aaaaabbb");
                            var form=this.up('form').getForm();
                            var user=this.up('form').down('[name=name]').value;
                            var password=this.up('form').down('[name=pswd]').value;
                                 //form.standardSubmit=true;
                            if (form.isValid())
                            {
                                HOST_STRING = Ext.getCmp("host").value;
                                MLIB_XML_PROXY.url = HOST_STRING;
                                //alert('hoststring is:' + hoststring);
                                   form.submit(
                                   {
                                               url: HOST_STRING,
                                               params:{Query: 'login user where usr_id ="' + user + '" and usr_pswd="' + password +'"',
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
                                                  ENV ="USR_ID="+jsonv.values[0][0] + ":SESSION_KEY="+jsonv.values[0][1];
                                                  //Ext.Msg.alert("Environment:", env);
                                                  loginForm.hide();
                                                  PagePanel.show();
                                                  //Ext.Msg.alert('failed','bbbb');
                                           },
                                           callback:function(p1,p2,p3)
                                           {
                                                   Ext.Msg.alert('completed','ccc');
                                           }
                                       });
                            }
                                   }
                               }
                              ]
                              ,renderTo: Ext.getBody()
                    });

                    loginForm.center();
                    
                    var commandForm= Ext.create('Ext.form.Panel',
                    {
                           title:'Command:',
                           bodyPadding:5,
                           width:350,
                           shadow:true,
                           layout:'anchor',
                           defaultType: 'textarea',
                           fieldDefaults: {
                               labelAlign:'left',
                               labelWidth: 100
                           },
                           items:[
                           {
                                   fieldLabel: 'Command to execute:',
                                   name: 'command',
                                 allowBlank: false
                           }
                           ],
                        buttons:[
                               {
                                   text:'Submit',
                                   formBind:true,
                                   disabled:true,
                                   handler:function()
                                   {
                            //alert("aaaaabbb");
                            var form=this.up('form').getForm();
                            var cmd=this.up('form').down('[name=command]').value;
                                 //form.standardSubmit=true;
                            if (form.isValid())
                            {
                                   form.submit(
                                   {
                                               url: 'http://localhost:4900/service',
                                               params:{Query: cmd,
                                                         Environment: env,
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
                                                  //Ext.Msg.alert("result:", jsonv);
                                                  for(var i=0; i<jsonv.values.length; i++)
                                                  {
                                                          var row;
                                                          for(var j=0; j<jsonv.values[i].length; j++)
                                                          {
                                                                if (row == undefined)
                                                                {
                                                                    row= jsonv.values[i][j];
                                                                }
                                                                else
                                                                {
                                                                      row += jsonv.values[i][j];
                                                                }
                                                          }
                                                          console.log('Row '+ i, row);
                                                          Ext.Msg.alert('Row '+i, row);
                                                      }
                                                  //Ext.Msg.alert('failed','bbbb');
                                           }
                                       });
                            }
                                   }
                               }
                              ]
                           //,renderTo: Ext.getBody()
                    });
                    commandForm.center();
                    //commandForm.hide();
                    var usermenu = new Ext.create('Ext.menu.Menu',
                            {
                            width: 120,
                            floating: false,
                            layout:'vbox',
                            items: [
                                {
                                    text: '用户维护',
                                    id: 'mntuser',
                                    width:100,
                                    handler: function(item, evt)
                                    {
                                       // alert(item.text + " clicked!");
                                    	mntUser(item, evt);
                                    }
                                },
                                //{
                                //    xtype: 'menuseparator'
                                //},
                                {
                                    text: '权限维护',
                                    handler: function(item, evt)
                                    {
                                        alert(item.text + ' was clicked!');
                                    }
                                }
                            ]});
                    
                    var menuForm= Ext.create('Ext.panel.Panel',
                            {
                               title:'菜单',
                               bodyPadding:5,
                               width:140,
                              height:600,
                               resizable: true,
                               plain: true,
                               pinned: true,
                               handles:'e',
                               layout:'accordion',
//                              layoutConfig:{
//                                 activeOnTop: true,
//                                 fill: true,
//                                 hideCollapseTool: true,
//                                 titleCollapse: false,
//                                 animate: false
//                              },
                                   items:[
                                       {
                                           id:'user',
                                           xtype: 'panel',
                                           title: '用户',
                                           items: [
                                               usermenu
                                               ]
                                       },
                                       {
                                           id:'facility',
                                           title: '设施',
                                           html:'This area is for executing command...'
                                       },
                                       {
                                           id:'inbound',
                                           title: '入库',
                                           html: 'This area is for login...'
                                       },
                                       {
                                           id:'outbound',
                                           title: '出库',
                                           html:'This area is for executing command...'
                                       },
                                       {
                                           id:'inventory',
                                           title: '库存',
                                           html:'This area is for executing command...'
                                       },
                                       {
                                           id:'count',
                                           title: '盘点',
                                           html:'This area is for executing command...'
                                       },
                                       {
                                           id:'command',
                                           title: '命令',
                                           html:'This area is for executing command...'
                                       },
                                       {
                                           id:'policy',
                                           title: '策略',
                                           html:'This area is for executing command...'
                                       },
                                       {
                                           id:'print',
                                           title: '打印',
                                           html:'This area is for executing command...'
                                       }]
                            });

                    var scrn_w = document.body.style.width;
                    var scrn_h = document.body.style.height;
                    console.log("screen width:" + scrn_w);
                    console.log("screen height:" + scrn_h);
                    var PagePanel= Ext.create('Ext.panel.Panel',
                            {
                               title:'WMS系统',
                               id: 'page_panel',
                               bodyPadding:5,
                               bodyStyle: {
                                   backgroundColor: 'gray'
                               },
                               layout:'hbox',
                                   items:[
                                       menuForm,
                                       {
                                           xtype: 'container',
                                           id: 'context_panel',
                                           title: '操作',
                                           html: 'texting area',
                                           width: '100%',
                                           height: '100%',
                                           items:[]
                                       }
                                       ],
                                       renderTo: Ext.getBody()
                    }).hide();
                    PagePanel.center();
      });
</script>
<div id='resulttext'></div>
</body>
</html>