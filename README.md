

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).


[![Pub](https://img.shields.io/badge/Pub-1.0.7-orange.svg?style=flat-square)](https://pub.dartlang.org/packages/geo_location_finder)


# flutter_location_plugin

Flutter plugin for getting accurate locations on the Android & iOS devices.


## Usage


 ```dart

    // Get current latitude, longitude
    Future<void> _getLocation() async {
      Map<dynamic, dynamic> locationMap;

      String result;

      try {
        locationMap = await GeoLocation.getLocation;
        var status = locationMap["status"];
        if ((status is String && status == "true") ||
            (status is bool) && status) {
          var lat = locationMap["latitude"];
          var lng = locationMap["longitude"];

          if (lat is String) {
            result = "Location: ($lat, $lng)";
          } else {
            // lat and lng are not string, you need to check the data type and use accordingly.
            // it might possible that else will be called in Android as we are getting double from it.
            result = "Location: ($lat, $lng)";
          }
        } else {
          result = locationMap["message"];
        }
      } on PlatformException {
        result = 'Failed to get location';
      }

      if (!mounted) return;

      setState(() {
        _result = result;
      });
    }



 ```