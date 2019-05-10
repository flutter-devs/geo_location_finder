#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'geo_location_finder'
  s.version          = '1.0.8'
  s.summary          = 'Flutter plugin for getting accurate locations on the Android & iOS devices.'
  s.description      = <<-DESC
A new Flutter geo location finder plugin.
                       DESC
  s.homepage         = ' https://www.aeologic.com/'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'support@aeologic.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.ios.deployment_target = '8.0'
end

