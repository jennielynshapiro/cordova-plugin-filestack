import Filestack
import FilestackSDK

@objc(FilestackCordova)
class FilestackCordova : CDVPlugin {

    var currentCallbackId:String = ""
    var currentPicker:PickerNavigationController?

    @objc(openFilePicker:)
    func openFilePicker(command: CDVInvokedUrlCommand) {

            self.currentCallbackId = command.callbackId

            self.presentPicker(arguments: command.arguments);

            let pluginResult = CDVPluginResult(status:CDVCommandStatus_NO_RESULT)
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )

    }

    func getLocalSources(sources:[Any]) -> [LocalSource] {
        var result:[LocalSource] = [];

        for source in sources {
            let s = source as? String ?? ""
            switch s {
            case "camera":
                result.append(.camera)
            case "device":
                result.append(.photoLibrary)
                result.append(.documents)
            default: ()
            }
        }

        return result;
    }

    func getCloudSources(sources:[Any]) -> [CloudSource] {
        var result:[CloudSource] = [];

        for source in sources {
            let s = source as? String ?? ""
            switch s {
            case "facebook":
                result.append(.facebook)
            case "instagram":
                result.append(.instagram)
            case "googledrive":
                result.append(.googleDrive)
            case "dropbox":
                result.append(.dropbox)
            case "box":
                result.append(.box)
            case "github":
                result.append(.gitHub)
            case "gmail":
                result.append(.gmail)
            case "picasa":
                result.append(.googlePhotos)
            case "onedrive":
                result.append(.oneDrive)
            case "clouddrive":
                result.append(.amazonDrive)
            default: ()
            }
        }

        return result;
    }

    func getLocation(location:String) -> StorageLocation {
        var loc:StorageLocation = .s3

        switch location {
        case "dropbox": loc = .dropbox
        case "rackspace": loc = .rackspace
        case "azure": loc = .azure
        case "gcs": loc = .gcs
        default: ()
        }

        return loc;
    }

    func presentPicker(arguments: [Any]) {

        let filestackAPIKey = arguments[0] as? String ?? ""
        let sources:[Any] = arguments[1] as? [Any] ?? []
        let appURLScheme = arguments[4] as? String ?? ""
        let location = arguments[5] as? String ?? ""

        let config = Filestack.Config()

        if appURLScheme != "" {
            config.appURLScheme = appURLScheme
        }

        if sources.count > 0 {
            config.availableLocalSources = getLocalSources(sources: sources)
            config.availableCloudSources = getCloudSources(sources: sources)
        } else {
            config.availableLocalSources = LocalSource.all()
            config.availableCloudSources = CloudSource.all()
        }

        config.videoQuality = .typeHigh

        if #available(iOS 11.0, *) {
            config.imageURLExportPreset = .current
            //config.videoExportPreset = AVAssetExportPresetHEVCHighestQuality
        }

        config.documentPickerAllowedUTIs = ["public.item"]

        let storeOptions = StorageOptions(location: getLocation(location: location))

        let client = Filestack.Client(apiKey: filestackAPIKey, config: config)

        self.currentPicker = client.picker(storeOptions: storeOptions)

        self.currentPicker?.pickerDelegate = self

        self.viewController.present(self.currentPicker!, animated: true)

    }

}

extension FilestackCordova: PickerNavigationControllerDelegate {

    func pickerUploadedFiles(picker: PickerNavigationController, responses: [NetworkJSONResponse]) {

        print("pickerUploadedFiles")

        //for response in responses {
        for (index, response) in responses.enumerated() {
            if let contents = response.json {
                // Our local file was stored into the destination location.
                print("Uploaded file response: \(contents)")

                let result:[String:Any] = ["file":contents, "complete":(responses.count - 1) == index];

                let pluginResult = CDVPluginResult(status:CDVCommandStatus_OK, messageAs:result)
                pluginResult?.setKeepCallbackAs((responses.count - 1) != index)
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: self.currentCallbackId
                )

            } else if let error = response.error {
                // The upload operation failed.
                print("Error uploading file: \(error)")
            }
        }

        print("self.currentPicker: \(self.currentPicker!)")
        DispatchQueue.main.async { self.currentPicker!.dismiss(animated: true) }
    }

    func pickerStoredFile(picker: PickerNavigationController, response: StoreResponse) {

        print("pickerStoredFile")

        if let contents = response.contents {
            // Our cloud file was stored into the destination location.
            print("Stored file response: \(contents)")

            let result:[String:Any] = ["file":contents, "complete":true];

            let pluginResult = CDVPluginResult(status:CDVCommandStatus_OK, messageAs:result)
            pluginResult?.setKeepCallbackAs(false)
            self.commandDelegate!.send(
                pluginResult,
                callbackId: self.currentCallbackId
            )

        } else if let error = response.error {
            // The store operation failed.
            print("Error storing file: \(error)")
        }

        print("self.currentPicker: \(self.currentPicker!)")
        print("self.currentPicker: \(self.currentPicker!.presentingViewController!)")
        DispatchQueue.main.async { self.currentPicker?.presentingViewController?.dismiss(animated: true) }
    }
}
