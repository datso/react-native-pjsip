# Android installation

## Step 1
Link library

```bash
react-native link
```

## Step 2
Open project in xcode.
1) In the project build settings, make sure you have enabled All settings to be visible.
2) The Build Options are the 4th section down. Select *No* for the Enable Bitcode option.

## Step 3
Add permissions and capabilities to use microphone and camera by adding following lines to `ios/%PROJECT_NAME%/Info.plist`

```xml
<key>NSCameraUsageDescription</key>
<string>Video calls</string>
<key>NSMicrophoneUsageDescription</key>
<string>Audio calls</string>
<key>UIBackgroundModes</key>
<array>
  <string>audio</string>
  <string>fetch</string>
  <string>voip</string>
</array>
```