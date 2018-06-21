var exec = require('cordova/exec');

function parseParams(params) {
    var a = [];
    a.push(params.apiKey || null);
    a.push(params.returnUrl || null);
    a.push(params.sources || null);
    a.push(params.mimeTypes || null);
    return a;
}

var filestack = {
    openFilePicker: function(params, callback) {
        exec(
            function() {
                // result
                callback(null, null);
            },
            function() {
                // error
                callback('Error', null);
            },
            "FilestackCordova",
            "openFilePicker",
            parseParams(params)
        );
    }
};

module.exports = filestack;