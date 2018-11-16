```dart

  import 'package:flutter/material.dart';
  import 'dart:async';

  import 'package:flutter/services.dart';
  import 'package:geo_location_finder/geo_location_finder.dart';

  void main() => runApp(new MyApp());

  class MyApp extends StatefulWidget {
    @override
    _MyAppState createState() => new _MyAppState();
  }

  class _MyAppState extends State<MyApp> {
    String _result = 'Unknown';

    @override
    void initState() {
      super.initState();
      _getLocation();
    }

    /*
    *  Method :- _getLocation
    *  Return Type : void
    *  Parameters :
    *
    *
    * This method will invoke Location Plugin and will give a Map Object in response.
    * */

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

    @override
    Widget build(BuildContext context) {
      return new MaterialApp(
        debugShowCheckedModeBanner: false,
        home: new Scaffold(
          appBar: new AppBar(
            title: const Text('Plugin location app'),
          ),
          body: new Center(
            child: new Text('$_result'),
          ),
        ),
      );
    }
  }


 ```