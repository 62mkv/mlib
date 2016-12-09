/**
 * We want to override the stash logout behavior. We need to actually get rid of the session.
 */
Ext.apply(RP.util.Helpers, {

    /**
     * @override
     * We want to override the stash logout behavior. We need to actually get rid of the session.
     */
    logout: function() {
        RP.Moca.util.Ajax.requestWithTextParams({
            url: '/console?m=logout',
            method: 'POST',
            success: function() {
                setTimeout("window.location.reload()", 100);
            },
            failureAlert: {
                title: 'Console',
                msg: 'Could not send the logout request to the server.'
            }
        });
    }
});