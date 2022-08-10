#import "ReactNativeBleAdvertiser.h"
#import "TransferService.h"

@implementation ReactNativeBleAdvertiser {
    CBMutableCharacteristic* transferCharacteristics;
    NSString* dataToSend;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(ReactNativeBleAdvertiser)

-(NSArray<NSString *> *)supportedEvents{ 
  return @[@"level"];
}

RCT_EXPORT_METHOD(startBroadcast: (nonnull NSString *)data) {

   RCTLogInfo(@"data is set to %@", data);
   dataToSend = data;

   RCTLogInfo(@"initializeBle function called");
   peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil options:nil];
 
  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
      NSLog(@"startAdvertising");
      [peripheralManager startAdvertising:@{ CBAdvertisementDataServiceUUIDsKey : @[[CBUUID UUIDWithString:TRANSFER_SERVICE_UUID]] }];
   });
   
}

RCT_EXPORT_METHOD(stopBroadcast) {
    NSLog(@"stopAdvertising");
    [peripheralManager stopAdvertising];
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    // Opt out from any other state
    if (@available(iOS 10.0, *)) {
        if (peripheral.state != CBManagerStatePoweredOn) {
            return;
        }
    } else {
        // TODO
    }
    
    // We're in CBPeripheralManagerStatePoweredOn state...
    NSLog(@"self.peripheralManager powered on.");
    
    // ... so build our service.
    NSData* data = [self->dataToSend dataUsingEncoding:NSUTF8StringEncoding];
    
    RCTLogInfo(@"data is %@", data);

    // Start with the CBMutableCharacteristic
    self->transferCharacteristics  = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:TRANSFER_CHARACTERISTIC_UUID]
                                                                      properties:CBCharacteristicPropertyRead
                                                                           value:data
                                                                     permissions:CBAttributePermissionsReadable];

    // Then the service
    CBMutableService *transferService = [[CBMutableService alloc] initWithType:[CBUUID UUIDWithString:TRANSFER_SERVICE_UUID]
                                                                        primary:YES];
    
    // Add the characteristic to the service
    transferService.characteristics = @[self->transferCharacteristics];
    
    // And add it to the peripheral manager
    [peripheralManager removeAllServices];
    NSLog(@"self.peripheralManager removeAllServices");


    [peripheralManager addService:transferService];
    NSLog(@"self.peripheralManager transferService");
}

- (void)peripheralManager:(CBPeripheralManager *)peripheral
            didAddService:(CBService *)service
                    error:(NSError *)error {
 
    RCTLogInfo(@"didAddService function called");

    if (error) {
        NSLog(@"Error publishing service: %@", [error localizedDescription]);
    }else {
        NSLog(@"Successfully added service");
    }
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral
                                       error:(NSError *)error {
 
    RCTLogInfo(@"peripheralManagerDidStartAdvertising function called, peripheral is %@", peripheral);

    if (error) {
        NSLog(@"Error advertising: %@", [error localizedDescription]);
    }else{
        NSLog(@"Advertising started");
    }
}

- (void)peripheralManager:(CBPeripheralManager *)peripheral
    didReceiveReadRequest:(CBATTRequest *)request {
    NSLog(@"Received valid read request");
    if (request.offset > self->transferCharacteristics.value.length) {
            [peripheral respondToRequest:request
                withResult:CBATTErrorInvalidOffset];
            return;
        }
    
    request.value = [self->transferCharacteristics.value
            subdataWithRange:NSMakeRange(request.offset,
            self->transferCharacteristics.value.length - request.offset)];
    
    [peripheral respondToRequest:request withResult:CBATTErrorSuccess];
}

@end
