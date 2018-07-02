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

### Installing the CocoaPods
CocoaPods dependency manager is used for iOS https://cocoapods.org/
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
https://developer.android.com/training/app-links/

## Usage

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

## Usage

* Currently the iOS implementation only supports appURLScheme and the Android implemntation only supports returnUrl.
* The iOS implementation does not support filtering mime types.
