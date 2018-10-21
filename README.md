# react-native-pjsip

A [PJSIP](http://www.pjsip.org/) module for React Native.

## Support
- Currently support for iOS and Android.  
- Support video and audio communication.
- Ability to use Callkit and PushNotifications.
- You can use it to build an iOS/Android app that can communicate with SIP server.
- Android version is based on [react-native-pjsip-builder](https://github.com/datso/react-native-pjsip-builder)
- iOS version is based on [Vialer-pjsip-iOS](https://github.com/VoIPGRID/Vialer-pjsip-iOS)

## Installation

- [iOS](https://github.com/datso/react-native-pjsip/blob/master/docs/installation_ios.md)
- [Android](https://github.com/datso/react-native-pjsip/blob/master/docs/installation_android.md)

## Usage

First of all you have to initialize module to be able to work with it.

There are some interesting moment in initialization.
When application goes to background, PJSIP module is still working and able to receive calls, but your javascipt is totally suspended.
When User open your application, javascript start to work and now your js application need to know what status have your account or may be you have pending incoming call.

So thats why first step should call start method for pjsip module.

```javascript
import {Endpoint} from 'react-native-pjsip'

let endpoint = new Endpoint();
let state = await endpoint.start(); // List of available accounts and calls when RN context is started, could not be empty because Background service is working on Android
let {accounts, calls, settings, connectivity} = state;

// Subscribe to endpoint events
endpoint.on("registration_changed", (account) => {});
endpoint.on("connectivity_changed", (online) => {});
endpoint.on("call_received", (call) => {});
endpoint.on("call_changed", (call) => {});
endpoint.on("call_terminated", (call) => {});
endpoint.on("call_screen_locked", (call) => {}); // Android only
```

Account creating is pretty strainghforward.

```javascript
let configuration = {
  "name": "John",
  "username": "sip_username",
  "domain": "pbx.carusto.com",
  "password": "****",
  "proxy": null,
  "transport": null, // Default TCP
  "regServer": null, // Default wildcard
  "regTimeout": null // Default 3600
  "regHeaders": {
    "X-Custom-Header": "Value"
  },
  "regContactParams": ";unique-device-token-id=XXXXXXXXX"
};
endpoint.createAccount().then((account) => {
  console.log("Account created", account);
});

```

To be able to make a call first of all you should createAccount, and pass account instance into Endpoint.makeCall function.
This function will return a promise that will be resolved when PjSIP initializes the call.

```javascript
let options = {
  headers: {
    "P-Assserted-Identity": "Header example",
    "X-UA": "React native"
  }
}

let call = await endpoint.makeCall(account, destination, options);
call.getId() // Use this id to detect changes and make actions

endpoint.addListener("call_changed", (newCall) => {
  if (call.getId() === newCall.getId()) {
     // Our call changed, do smth.
  }
}
endpoint.addListener("call_terminated", (newCall) => {
  if (call.getId() === newCall.getId()) {
     // Our call terminated
  }
}
```

## API

1. [Startup](https://github.com/datso/react-native-pjsip/blob/master/docs/startup.md)
2. [Accounts](https://github.com/datso/react-native-pjsip/blob/master/docs/accounts.md)
3. [Calls](https://github.com/datso/react-native-pjsip/blob/master/docs/calls.md)
4. [Settings](https://github.com/datso/react-native-pjsip/blob/master/docs/settings.md)

## Demo
The demo project is https://github.com/datso/react-native-pjsip-app. And you will need a SIP server.
