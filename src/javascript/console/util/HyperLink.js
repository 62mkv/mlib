RP.Moca.util.HyperLink = function() {
    return {
        sessions: function (value) {
            if (value === null || value == "") {
                return "";
            }
    
            // Build the URL we're going to link to.
            var protocol = document.location.protocol;
            var host = document.location.host;
            var url = protocol + '//' + host + '/console/console.do#moca.console.taskflow:sessions';
    
            // Build the HTML with the link and value.
            var html = '<a href="' + url + '">' + value + '</a>';
    
            return html;
        },
        
        node: function (value) {
            if (value === null || value == "") {
                return "";
            }
    
            var url = value + "/console";
    
            // Build the HTML with the link and value.
            var html = '<a href="' + url + '">' + value + '</a>';
    
            return html;
        }
    };
}();
