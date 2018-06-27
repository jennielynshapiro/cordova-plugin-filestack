# Filestack Cordova
Filestack plugin for Cordova. Available for Android and iOS

Creating an updated version of https://github.com/dbaq/cordova-plugin-filepickerio. 
The native librabires used in that project have been depreciated.

Setup and usage is quite finicky for multiple reasons. First because of recent changes to webview based oAuth flows, url schemes are now required. Also iOS uses Cocoapods and Swift and requires manualy setting the swift version in Xcode for the required Cocoapod frameworks. 
