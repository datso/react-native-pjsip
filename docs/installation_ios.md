# iOS installation

## Step 1
Link library

```bash
react-native link
```

## Step 2
Open project in xcode.
1. In the project build settings, make sure you have enabled All settings to be visible.
2. The Build Options are the 4th section down. Select *No* for the Enable Bitcode option.

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

# PushNotifications

To be able to receive incoming call when app in background you have to use PushKit.
This is the only way to receive information and wake up application to perform some action like use CallKit to show incoming call dialog.

## How it works
1. Your application registers for receiving VoIP notifications
2. After a successful registration your application adds *device token* to Contact header when REGISTER.
3. Your SIP server should parse those headers and when someone calling to those user, server should also send push notification for those device(s).
4. When iOS application receives this PushNotificaiton it should show incoming call dialog via callkit, and register on server.
5. Server should send INVITE to new registration from this iOS device.
6. When user press Answer button via callkit, iOS application answers those SIP call.

## Client side
When application starts, it should send REGISTER with additional attributes of `Contact` header and use a long term *registration timeout*.
In example bellow we use one month as regTimeout to be sure that our registration will not be expired when application goes to background.

Your configuration might looks like this
```javascript
endpoint.createAccount({
    "username":"****",
    "domain":"****",
    "password":"****",
    "regTimeout": 2592000, // one month
    "regContactParams": ";app-id=****;pn-voip-tok=XXXXXXXXX;pn-im-tok=XXXXXXXXXX"
})
```

## Server side
Your SIP server should support ability to send PushNotifications and also have addtional logic that re-send's INVITE messages during calling to user when new registration is available.
For example an working module for freeswitch consider using *mod_apn*.

# CallKit

Ensure that the Push Notification Capability is ON
TODO: Example