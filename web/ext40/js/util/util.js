/* Copyright (c) Sam Corporation, All rights reserved, 2016.*/

function buildModeAndColumnsForCmd(cmd, modelName)
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
                 var xmlDoc = resp.responseXML;
                 if(xmlDoc!=null)
                 {
                     var meta = xmlDoc.getElementsByTagName("metadata");
                     var modstr = "Ext.define('" + modelName + "', { extend: 'Ext.data.Model', fields:[";
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
                     var columnsArrayStr = "[";
                     addedflg = false;
                     for(var i = 0; i < columns.childNodes.length; i++)
                     {
                         var col = columns.childNodes[i].attributes.name.value;
                         var type = columns.childNodes[i].attributes.type.value;
                         if (type == 'S')
                         {
                        	 if (addedflg == true)
                        	 {
                        		 columnsArrayStr += ",";
                        	 }
                        	 columnsArrayStr += "{text: '" + col + "', dataIndex: '" + col +"'}";
                        	 addedflg = true;
                         }
                         else if (type == 'I')
                         {
                        	 if (addedflg == true)
                        	 {
                        		 columnsArrayStr += ",";
                        	 }
                        	 columnsArrayStr += "{text: '" + col + "', dataIndex: '" + col +"'}";
                        	 addedflg = true;
                         }
                         else if (type == 'D')
                         {
                        	 if (addedflg == true)
                        	 {
                        		 columnsArrayStr += ",";
                        	 }
                        	 columnsArrayStr += "{text: '" + col + "', dataIndex: '" + col +"'}";
                        	 addedflg = true;
                         }
                      }
                     columnsArrayStr += "]";
                     columnsArray = eval(columnsArrayStr);
                 }
                 //Ext.Msg.alert('����', respText.name+"====="+respText.id);   
         },
         failure: function(resp,opts) {
                 var status = xmlDoc.getElementsByTagName("status");
                 Ext.Msg.alert('����', status[0].value);   
          }
       });
    var modeAndColumns = {model: model,
    		              columns: columnsArray};
    return modeAndColumns;
}

function extractXmlDataForMlib(root) {
    var me = this,
        Model   = me.model,
        length  = root.childNodes.length,
        records = new Array(length),
        convertedValues, node, record, i;

    if (!root.length && Ext.isObject(root)) {
        root = [root];
        length = 1;
    }
//    alert("extraData called?");
//    console.log("extraData called?");

    for (i = 0; i < length; i++) {
        node = root.childNodes[i];
        if (node.isModel) {
            // If we're given a model instance in the data, just push it on
            // without doing any conversion
            records.childNodes[i] = node;
        } else {
            // Create a record with an empty data object.
            // Populate that data object by extracting and converting field values from raw data.
            // Must pass the ID to use because we pass no data for the constructor to pluck an ID from
            records[i] = record = new Model(undefined, me.getId(node), node, convertedValues = {});

            // If the server did not include an id in the response data, the Model constructor will mark the record as phantom.
            // We  need to set phantom to false here because records created from a server response using a reader by definition are not phantom records.
            record.phantom = false;

            var data = {};
            for(j = 0; j < Model.prototype.fields.keys.length - 1; j++)
            {
            	var fieldname = Model.prototype.fields.keys[j];
            	var fieldvalue = node.childNodes[j].innerHTML;
            	var entry = "{" + fieldname + ":'" + fieldvalue + "'}";
            	Ext.apply(data, Ext.decode(entry));
            }
            record.data = data;
            // Use generated function to extract all fields at once
            //me.convertRecordData(convertedValues, node, record);

            if (me.implicitIncludes && record.associations.length) {
                me.readAssociated(record, node);
            }
        }
    }

    return records;
}

var MLIB_XML_PROXY = {
	     type: 'ajax',
	     url: HOST_STRING,
	     reader: {
	    	 type: 'xml',
	    	 root: 'data',
	    	 record:'row',
	    	 extractData : extractXmlDataForMlib
	    }
}