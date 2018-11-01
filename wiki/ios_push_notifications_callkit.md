# PushNotifications

To be able to receive incoming call when app in background you have to use PushKit.
This is the only way to receive information and wake up application to perform some action like use CallKit to show incoming call dialog.

PushNotifications didn't work inside emualtor.

## How it works
1. Your application registers for receiving VoIP notifications
2. After a successful registration your application adds *device token* to Contact header when REGISTER.
3. Your SIP server should parse those headers and when someone calling to those user, server should also send push notification for those device(s).
4. When iOS application receives this PushNotificaiton it should show incoming call dialog via callkit, and register on server.
5. Server should send INVITE to new registration from this iOS device.
6. When user press Answer button via callkit, iOS application answers those SIP call.

## Client side
When application starts, it should send REGISTER with additional attributes of `Contact` header.

Your configuration might looks like this
```javascript
endpoint.createAccount({
    "username":"****",
    "domain":"****",
    "password":"****",
    "regContactParams": ";app-id=****;pn-voip-tok=XXXXXXXXX;pn-im-tok=XXXXXXXXXX"
})
```

By using react-native-voip-nitifications
1. Register for *VoIP* notifications.
2. Obtain `Device token`
2. Send device token in `Contact` header options by using `contactUriParams` property of account configuration.

Ensure that the Push Notification Capability is ON

## Server side
Your SIP server should support ability to send PushNotifications and also have addtional logic that re-send's INVITE messages during calling to user when new registration is available.
For example an working module for freeswitch consider using *mod_apn*.

## Background mode

When your application goes to background mode it should send UNREGISTER to ensure that PJSIP will send NEW REGISTRATION when VoIP notification will be received from server.
When new registration 

# CallKit




CallKit app receives an incoming call while it is in the background, the system's native incoming call UI will be shown.




TODO: Example