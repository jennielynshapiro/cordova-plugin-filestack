# Filestack Cordova
Filestack plugin for Cordova. Available for Android and iOS

Combines Filestack's native Android (https://github.com/filestack/filestack-android) and iOS (https://github.com/filestack/filestack-ios) SDKs into a Cordova plugin.

A completely rewritten version of https://github.com/dbaq/cordova-plugin-filepickerio. 
The iOS native library used in that project has been depreciated. The Android implementation does not support app links wich is required for at least Google oAuth flows.

## Installing the plugin ##
```
cordova plugin add https://github.com/jennielynshapiro/cordova-plugin-filestack.git --save
```

## iOS Setup

### Installing CocoaPods
CocoaPods dependency manager is used for iOS 
https://cocoapods.org/
```
sudo gem install cocoapods
```

### URL Scheme
Add a url scheme to your info.plst
https://coderwall.com/p/mtjaeq/ios-custom-url-scheme

### Framework Swift Version in Xcode
The swift version for each framework has to me manually set in Xcode.

## Android Setup

### App Links
App links should be setup and working.
https://developer.android.com/training/app-links/

## Usage

Open the picker using
```
window.filestack.openFilePicker(params, callback)
```

### Params
 * ```apiKey``` string : Your filestack api key.
 * ```sources``` string array : An array of sources.
 * ```mimeTypes``` string array : An array of mime type selector.
 * ```returnUrl``` string : App link for Android
 * ```appURLScheme``` string : Url scheme for iOS
 * ```location``` string : Storage location
 * ```container``` string : Storage container
 * ```region``` string : Storage region
 
### Response
The callback should accept (error, result). Result is an object with a file object property and a boolean complete property. The callback will be called once for each file uploaded. The last callback will have the complete property set to true.

File Properties:
 * ```filename``` string : File name
 * ```size``` string int : Size in bytes
 * ```mimetype``` string : Mime Type
 * ```handle``` string : Filestack file identifier
 * ```key``` string : Storage key

### Example
```
 let files = [];

      window.filestack.openFilePicker({
        apiKey: "YOUR_API_KEY",
        sources: ["device", "googledrive", "facebook", "instagram", "dropbox", "box", "github", "gmail", "picasa", "onedrive", "clouddrive"],
        mimeTypes: ["*/*"],
        returnUrl: "https://demo.filestack.com",
        appURLScheme: "filestack",
        location: "S3",
        container: "my-bucket-name",
        region: "us-west-2"
      }, (err, result) => {

        if(err) {
          // Handle Error
          return;
        }

        if(!result) {
          return;
        }

        if(result.file) {
          files.push(result.file);
        }

        if(files.length > 0 && result.complete) {
          onFilestackUploadDone({filesUploaded: files});
          files = [];
        }

      });
```

## Notes

* Currently the iOS implementation only supports appURLScheme and the Android implmentation only supports returnUrl.
* The iOS implementation does not support filtering mime types.
