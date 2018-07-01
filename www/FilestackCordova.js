var exec = require('cordova/exec');

function parseParams(params) {
    var a = [];

    a.push(params.apiKey || null);
    a.push(params.sources || null);
    a.push(params.mimeTypes || null);
    a.push(params.returnUrl || null);
    a.push(params.appURLScheme || null);
    a.push(params.location || null);
    a.push(params.container || null);
    a.push(params.region || null);

    return a;

}

function getFileKeyFromHandle(handle, callback) {

    var url = "https://www.filestackapi.com/api/file/" + handle + "/metadata";

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4) {
            if(xmlHttp.status == 200) {

                try {

                    var response = JSON.parse(this.responseText);

                    if (response.key) {
                        callback(null, response.key);
                    } else {
                        callback("Error: Unable to get file key");
                    }

                } catch (e) {
                    callback("Error: Unable to get file key");
                }

            } else {
                callback("Error: Unable to get file key");
            }
        }

    };

    xmlHttp.open("GET", url, true); // true for asynchronous
    xmlHttp.send(null);

}

function processFileResult(result, callback) {

    if(result && result.file && result.file.handle && !result.file.key) {

        getFileKeyFromHandle(result.file.handle, function(error, key) {

            if(error || !key) {
                callback(error, null);
            }

            result.file.key = key;

            callback(null, result);

        });

    } else {

        callback(null, result);

    }

}

var filestack = {
    openFilePicker: function(params, callback) {
        exec(
            function(result) {
                processFileResult(result, callback);
            },
            function(error) {
                // error
                callback({error: error}, null);
            },
            "FilestackCordova",
            "openFilePicker",
            parseParams(params)
        );
    }
};

module.exports = filestack;