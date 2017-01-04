/* Copyright (c) Sam Corporation, All rights reserved, 2016.*/

function buildModeForCmd(cmd, modelName)
{
	//model = {};
    Ext.Ajax.request({
        url: HOST_STRING,
        async: false,
        params:{
            Query:cmd,
            ResponseFormat:'xml'
         },
         success: function(resp,opts)
         {
//        	     model = Ext.create(modelName,
//        	             extend: 'Ext.data.Model'		 
//        	     );
        	     modstr = "Ext.define('" + modelName + "', { extend: 'Ext.data.Model', fields:[";
                 var xmlDoc = resp.responseXML;
                 if(xmlDoc!=null)
                 {
                     var meta = xmlDoc.getElementsByTagName("metadata");
                     var columns = meta[0];
                     var addedflg = false;
                     for(var i = 0; i < columns.childNodes.length; i++)
                     {
                         var col = columns.childNodes[i].attributes.name.value;
                         var type = columns.childNodes[i].attributes.type.value;
                         if (type == 'S')
                         {
                        	 if (addedflg == true)
                        	 {
                        		 modstr += ",";
                        	 }
                        	 modstr += "{name: '" + col + "', type: 'string'}";
                        	 addedflg = true;
                         }
                         else if (type == 'I')
                         {
                        	 if (addedflg == true)
                        	 {
                        		 modstr += ",";
                        	 }
                        	 modstr += "{name: '" + col + "', type: 'integer'}";
                        	 addedflg = true;
                         }
                         else if (type == 'D')
                         {
                        	 if (addedflg == true)
                        	 {
                        		 modstr += ",";
                        	 }
                        	 modstr += "{name: '" + col + "', type: 'date'}";
                        	 addedflg = true;
                         }
                      }
                     modstr += "]});"
                     model = eval(modstr);
                     //console.log(col, type);
                     //Ext.apply(model, {fields:[flds]});
                     //console.log(model);
                 }
                 //Ext.Msg.alert('����', respText.name+"====="+respText.id);   
         },
         failure: function(resp,opts) {
                 var status = xmlDoc.getElementsByTagName("status");
                 Ext.Msg.alert('����', status[0].value);   
          }
       });
    //return model;
}