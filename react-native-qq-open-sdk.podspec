# react-native-qq-open-sdk.podspec

require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-qq-open-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-qq-open-sdk
                   DESC
  s.homepage     = "https://github.com/github_account/react-native-qq-open-sdk"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Byron ZHU" => "byron.zhuwenbo@gmail.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/github_account/react-native-qq-open-sdk.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,cc,cpp,m,mm,swift}"
  s.requires_arc = true
  # s.resources = ['ios/TencentOpenApi_IOS_Bundle.bundle'] # 从sdk3.3.5版本开始，此文件可以不要了
  s.vendored_frameworks = "TencentOpenAPI.framework" # iOS_SDK_V3.5.11
  s.dependency "React"
  # ...
  # s.dependency "..."
end

