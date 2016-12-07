RP.Moca.util.Role = function() {

return {
    isReadOnly: function (params) {
        // Apply the given configuration to ourselves.
        Ext.apply(this, params);

        RP.Moca.util.Ajax.requestWithTextParams({
                scope: params.scope,
                url: '/console',
                method: 'GET',
                params: {
                    m: 'getUserRole'
                },
                success : params.success
        });
    }
};
}();