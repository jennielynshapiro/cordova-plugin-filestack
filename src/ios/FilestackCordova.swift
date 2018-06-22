import Filestack
import FilestackSDK

@objc(FilestackCordova)
class FilestackCordova : CDVPlugin {

  @objc(openFilePicker:)
  func openFilePicker(command: CDVInvokedUrlCommand) {
    
    var pluginResult = CDVPluginResult(
      status: CDVCommandStatus_ERROR
    )

    let msg = command.arguments[0] as? String ?? ""

    if msg.characters.count > 0 {

        self.presentPicker(filestackAPIKey: msg);

      pluginResult = CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAs: msg
      )
    }

    self.commandDelegate!.send(
      pluginResult,
      callbackId: command.callbackId
    )
  }
    
    func presentPicker(filestackAPIKey: String) {
        

        let config = Filestack.Config()
    
        config.appURLScheme = appURLScheme

        config.videoQuality = .typeHigh
        
        if #available(iOS 11.0, *) {
            config.imageURLExportPreset = .current
            config.videoExportPreset = AVAssetExportPresetHEVCHighestQuality
        }
        

        config.availableLocalSources = LocalSource.all()
        
        config.availableCloudSources = CloudSource.all()
        
        config.documentPickerAllowedUTIs = ["public.item"]
        
        let client = Filestack.Client(apiKey: filestackAPIKey, config: config)
        
        let storeOptions = StorageOptions(location: .s3)
        
        let picker = client.picker(storeOptions: storeOptions)
        
        // Optional. Set the picker's delegate.
        picker.pickerDelegate = self
        
        // Finally, present the picker on the screen.
        self.viewController?.viewController(picker, animated: true)
    }

}

extension FilestackCordova: PickerNavigationControllerDelegate {
    
    func pickerStoredFile(picker: PickerNavigationController, response: StoreResponse) {
        
        if let contents = response.contents {
            // Our cloud file was stored into the destination location.
            print("Stored file response: \(contents)")
        } else if let error = response.error {
            // The store operation failed.
            print("Error storing file: \(error)")
        }
    }
    
    func pickerUploadedFile(picker: PickerNavigationController, response: NetworkJSONResponse?) {
        
        if let contents = response?.json {
            // Our local file was stored into the destination location.
            print("Uploaded file response: \(contents)")
        } else if let error = response?.error {
            // The upload operation failed.
            print("Error uploading file: \(error)")
        }
    }
}
