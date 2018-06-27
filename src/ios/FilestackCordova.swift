import Filestack
import FilestackSDK

@objc(FilestackCordova)
class FilestackCordova : CDVPlugin {

    var currentCallbackId:String = ""
    var currentPicker:PickerNavigationController?

    @objc(openFilePicker:)
    func openFilePicker(command: CDVInvokedUrlCommand) {

        let apiKey = command.arguments[0] as? String ?? ""

        if !apiKey.isEmpty {

            self.currentCallbackId = command.callbackId

            self.presentPicker(filestackAPIKey: apiKey);

            let pluginResult = CDVPluginResult(status:CDVCommandStatus_NO_RESULT)
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )

        } else {

        }
    }

    func presentPicker(filestackAPIKey: String) {

        //self.commandDelegate!.run(inBackground: {
        let config = Filestack.Config()

        config.appURLScheme = "meldtables"

        config.videoQuality = .typeHigh

        if #available(iOS 11.0, *) {
            config.imageURLExportPreset = .current
            //config.videoExportPreset = AVAssetExportPresetHEVCHighestQuality
        }


        config.availableLocalSources = LocalSource.all()

        config.availableCloudSources = CloudSource.all()

        config.documentPickerAllowedUTIs = ["public.item"]

        let client = Filestack.Client(apiKey: filestackAPIKey, config: config)

        let storeOptions = StorageOptions(location: .s3)

        self.currentPicker = client.picker(storeOptions: storeOptions)

        // Optional. Set the picker's delegate.
        self.currentPicker?.pickerDelegate = self

        // Finally, present the picker on the screen.
        self.viewController.present(self.currentPicker!, animated: true)
        //})
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
