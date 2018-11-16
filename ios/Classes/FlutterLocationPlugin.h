#import <Flutter/Flutter.h>
#import <CoreLocation/CoreLocation.h>

@interface FlutterLocationPlugin : NSObject<FlutterPlugin, CLLocationManagerDelegate>
{
    CLLocation          *currentLocation;
    BOOL                isUserPositionAvailable;
    CLLocationManager   *locationManager;
    NSTimer             *timer;
    CLLocation          *tempLocation;
    FlutterResult       returnResult;
}
@property (nonatomic, retain)CLLocationManager *locationManager;
@end
