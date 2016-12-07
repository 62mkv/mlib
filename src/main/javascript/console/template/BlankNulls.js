Ext.define("MOCA.RP.util.template.BlankNulls", {
    override:'Ext.XTemplate',
  
    applyOut: function(values, out) {
        var me = this,
            compiler;

        if (!me.fn) {
            compiler = new Ext.XTemplateCompiler({
                useFormat: me.disableFormats !== true,
                blankNulls: me.blankNulls
            });
            me.fn = compiler.compile(me.html);
        }

        try {
            me.fn.call(me, out, values, {}, 1, 1);
        } catch (e) {
            Ext.Logger.error(e.message);
        }

        return out;
    }
});

Ext.define("RP.MOCA.util.template.XTemplateCompilerNullBlanks", {
    override:'Ext.XTemplateCompiler',
    
    // override to blankNulls if the config is set.
    parseTag: function (tag) {
        var m = this.tagRe.exec(tag),
            name = m[1],
            format = m[2],
            args = m[3],
            math = m[4],
            v;

        // name = "." - Just use the values object.
        if (name == '.') {
            // filter to not include arrays/objects/nulls
            v = 'Ext.Array.indexOf(["string", "number", "boolean"], typeof values) > -1 || Ext.isDate(values) ? values : ""';
        }
        // name = "#" - Use the xindex
        else if (name == '#') {
            v = 'xindex';
        }
        else if (name.substr(0, 7) == "parent.") {
            v = name;
        }
        // name has a . in it - Use object literal notation, starting from values
        else if ((name.indexOf('.') !== -1) && (name.indexOf('-') === -1)) {
            v = "values." + name;
        }
        // name is a property of values
        else {
            v = "values['" + name + "']";
        }

        if (math) {
            v = '(' + v + math + ')';
        }

        if (format && this.useFormat) {
            args = args ? ',' + args : "";
            if (format.substr(0, 5) != "this.") {
                format = "fm." + format + '(';
            } else {
                format += '(';
            }
        }
        else {
            args = '';
            if (this.blankNulls){
                format = "(" + v + " === undefined || " + v + " === null ? '' : ";
            }
            else {
                format = "(" + v + " === undefined ? '' : ";
            }
        }

        return format + v + args + ')';
    }
});