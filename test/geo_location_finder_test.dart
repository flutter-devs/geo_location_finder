import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:geo_location_finder/geo_location_finder.dart';

void main() {
  const MethodChannel channel = MethodChannel('geo_location_finder');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await GeoLocationFinder.platformVersion, '42');
  });
}
