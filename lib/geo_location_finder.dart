import 'dart:async';

import 'package:flutter/services.dart';

class GeoLocation {
  static const MethodChannel _channel =
      const MethodChannel('github.com/geo_location_finder');

  /*
  *  Method :- getLocation
  *  Return Type : Future<Map>
  *  Parameters :
  *
  *
  * This method will invoke platform specific location code, and will return a Dictionary or Key Value pair
  * response.
  *  ---------------- PLUGIN RESPONSE --------------------
  *
  *  iOS :- [Key : Response]
  *         [status : String] // true/false
  *         [message : String]
  *         [latitude : String]
  *         [longitude : String]
  *
  *
  *  Android :- [Key : Response]
  *         [status : bool] // true/false
  *         [message : String]
  *         [latitude : double]
  *         [longitude : double]
  * */

  static Future<Map<dynamic, dynamic>> get getLocation async {
    var result = await _channel.invokeMethod('getLocation');
    return result;
  }
}
