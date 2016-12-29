/* Copyright (c) Sam Corporation, All rights reserved, 2016.*/

function buildModeForCmd(cmd)
{
    Ext.Ajax.request({
        url: HOST_STRING,
        async: false,
        params:{
            Query:cmd,
            ResponseFormat:'xml'
         },
         success: function(resp,opts)
         {
                 var xmlDoc = resp.responseXML;
                 if(xmlDoc!=null)
                 {
                     var meta = xmlDoc.getElementsByTagName("metadata");
                     var columns = meta[0];
                     for(var i = 0; i < columns.childNodes.length; i++)
                     {
                         var col = columns.childNodes[i].attributes.name.value;
                         var type = columns.childNodes[i].attributes.type.value;
                         console.log(col, type);
                     }
                 }
                 //Ext.Msg.alert('´íÎó', respText.name+"====="+respText.id);   
         },
         failure: function(resp,opts) {
                 var status = xmlDoc.getElementsByTagName("status");
                 Ext.Msg.alert('´íÎó', status[0].value);   
          }
       });
}