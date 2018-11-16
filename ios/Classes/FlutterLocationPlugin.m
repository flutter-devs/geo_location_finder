#import "FlutterLocationPlugin.h"

@implementation FlutterLocationPlugin

@synthesize locationManager;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"github.com/geo_location_finder"
            binaryMessenger:[registrar messenger]];
  FlutterLocationPlugin* instance = [[FlutterLocationPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getLocation" isEqualToString:call.method]) {

    self.locationManager = [[CLLocationManager alloc]init];
    [self.locationManager setDelegate:self];
    self.locationManager.desiredAccuracy    =   kCLLocationAccuracyBest;
      if (@available(iOS 9.0, *)) {
          self.locationManager.allowsBackgroundLocationUpdates = YES;
      } else {
          // Fallback on earlier versions
      }

      returnResult = result;

      [self startMonitoringLocation];

  } else {
    result(FlutterMethodNotImplemented);
  }
}


-(void) startMonitoringLocation {

    if (!self.locationManager)
    {
        self.locationManager = [[CLLocationManager alloc]init];
        [self.locationManager setDelegate:self];
        self.locationManager.desiredAccuracy    =   kCLLocationAccuracyBest;
        //self.locationManager.distanceFilter = 8.0f;
    }

    if ([self.locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
        [self.locationManager requestWhenInUseAuthorization];
    }

    if([CLLocationManager locationServicesEnabled] &&
       [CLLocationManager authorizationStatus] != kCLAuthorizationStatusDenied)
    {
        [self.locationManager startUpdatingLocation];
         //timer = [NSTimer scheduledTimerWithTimeInterval:2.0 target:self selector:@selector(helpBoolFunction) userInfo:nil repeats:YES];
    }
    else
    {
        NSDictionary *map = @{@"status":@"false",@"message":@"Location service is disabled."};
        returnResult(map);
    }
}



-(void)stopRecievingLocation
{

    [timer invalidate];
    timer                       =   nil;
    isUserPositionAvailable     =   NO;
    currentLocation             =   nil;
    tempLocation                =   nil;

    [locationManager stopUpdatingLocation];
    locationManager =   nil;

}



- (void)locationManager:(CLLocationManager *)manager
       didFailWithError:(NSError *)error
{

    NSString *errorString = [error localizedDescription];
    NSString *finalError;

    if ([errorString rangeOfString:@"kCLErrorDomain error 1"].location != NSNotFound)
    {
        NSDictionary *map = @{@"status":@"false",@"message":@"Location service access is denied. Please allow app to access your account in Settings App."};
        returnResult(map);
        finalError = @"Location service access is denied. Please allow app to access your account in Settings App.";
    }
    else{

        NSDictionary *map = @{@"status":@"false",@"message":@"Sorry, can't find your location please try again later."};
        returnResult(map);
        finalError = @"Sorry, can't find your location please try again later.";
    }
}


- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{

    if(newLocation.coordinate.latitude != oldLocation.coordinate.latitude && newLocation.coordinate.longitude != oldLocation.coordinate.latitude)
    {
        if ([self isValidLocation:newLocation withOldLocation:oldLocation])
        {
            currentLocation = newLocation;
            NSString *lat = [NSString stringWithFormat:@"%f",currentLocation.coordinate.latitude];
            NSString *lng = [NSString stringWithFormat:@"%f",currentLocation.coordinate.longitude];
            NSDictionary *map = @{@"status":@"true",@"latitude":lat, @"longitude":lng};
            returnResult(map);
            // Stop location updates
            [self stopRecievingLocation];
        }
    }

    if (!isUserPositionAvailable) {

        isUserPositionAvailable  =   YES;
        currentLocation          =   newLocation;

        NSString *lat = [NSString stringWithFormat:@"%f",currentLocation.coordinate.latitude];
        NSString *lng = [NSString stringWithFormat:@"%f",currentLocation.coordinate.longitude];
        NSDictionary *map = @{@"status":@"true",@"latitude":lat, @"longitude":lng};
        returnResult(map);
        // Stop location updates
        [self stopRecievingLocation];
    }

}

-(void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    if (locations.count > 0)
    {
        CLLocation  *newLocation = [locations lastObject];
        if (![self isValidLocation:newLocation withOldLocation:currentLocation] || currentLocation.coordinate.latitude || 0.0 || currentLocation.coordinate.longitude != 0.0)
        {
            currentLocation  =   newLocation;
            if (!isUserPositionAvailable)
            {
                isUserPositionAvailable  =   YES;
                currentLocation          =   newLocation;
            }
            
            NSString *lat = [NSString stringWithFormat:@"%f",currentLocation.coordinate.latitude];
            NSString *lng = [NSString stringWithFormat:@"%f",currentLocation.coordinate.longitude];
            NSDictionary *map = @{@"status":@"true",@"latitude":lat, @"longitude":lng};
            returnResult(map);
            // Stop location updates
            [self stopRecievingLocation];
        }

        [self logArray:locations];
    }
}


-(void)logLocation {
    NSString *lat = [NSString stringWithFormat:@"%f",currentLocation.coordinate.latitude];
    NSString *lng = [NSString stringWithFormat:@"%f",currentLocation.coordinate.longitude];
    NSLog(@"Location ( %@, %@ )",lat,lng);
}


-(void)logArray:(NSArray*)arr
{
    NSLog(@"Location Array - \n %@ \n\n",arr);
}



-(void)helpBoolFunction
{
/*
    if (tempLocation.coordinate.latitude != currentLocation.coordinate.latitude && tempLocation.coordinate.longitude != currentLocation.coordinate.longitude)
    {
        tempLocation    =   currentLocation;

        if ([Delegate respondsToSelector:@selector(UserPositionCoordinate:)])
        {
            [Delegate performSelector:@selector(UserPositionCoordinate:) withObject:currentLocation];
        }
    }

    if ([Delegate respondsToSelector:@selector(UserPositionCoordinate:)])
    {
        [Delegate performSelector:@selector(UserPositionCoordinate:) withObject:currentLocation];
    }
*/
}




- (BOOL)isValidLocation:(CLLocation *)newLocation withOldLocation:(CLLocation *)oldLocation {

    if (!newLocation) return NO;
    if (newLocation.horizontalAccuracy < 0) return NO;
    if (newLocation.horizontalAccuracy > 66) return NO;


    if (newLocation.coordinate.latitude == 0.0 && newLocation.coordinate.longitude == 0.0) {
        return NO;
    }


#if !TARGET_IPHONE_SIMULATOR
    if (newLocation.verticalAccuracy < 0) return NO;
#endif

    NSTimeInterval secondsSinceLastPoint = [newLocation.timestamp timeIntervalSinceDate:oldLocation.timestamp];
    if ( isnan(secondsSinceLastPoint) || secondsSinceLastPoint < 0) return NO;

    NSTimeInterval locationAge = -[newLocation.timestamp timeIntervalSinceNow];
    if (locationAge > 5.0) return NO;

    if ((oldLocation.coordinate.latitude == newLocation.coordinate.latitude) && (oldLocation.coordinate.longitude == newLocation.coordinate.longitude))
        return NO;

    return YES;
}


@end
