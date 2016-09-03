TODO: Introduction + links to other sections.

# Events

All interaction from javascript to pjsip module is asynchromius.
So for each action, promise will be returned.

## call_received

TODO: Description

## call_changed

TODO: Description

## call_terminated

TODO: Description 


# Actions

## Initiate a call
To be able to make a call first of all you should createAccount, and pass account instance into Endpoint.makeCall function.
This function will return a promise that will be resolved when PjSIP initializes the call.

```
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

## Answer the call

After answer there will be event "call_changed" that reflect the changes.
If there is already active call, it will be placed on hold (so expect "call_changed" event)

```
let options = {};
let call = ...;
let promise = endpoint.answerCall(call, options);
promise.then(() => {
	// Answer complete, expect that "call_changed" will be fired.
}));

promise.catch(() => {
	// Answer failed, show error
});
```

## Hangup
Use this function when you have active call, and Decline for unanswered incoming calls.
After successul hangup, Endpoint should fire "call_terminated" event, use it to how final call duration and status.

```
let options = {};
let call = ...;
await endpoint.hangupCall(call, options);
```

## Decline
Use this function when you have unanswered incoming call.
After successul decline, Endpoint should fire "call_terminated" event.

```
let options = {};
let call = ...;
await endpoint.declineCall(call, options);
```

## Hold/Unhold

TODO: Description
After successul hold/unhold, Endpoint should fire "call_changed" event, where `isHeld` should be false or true.

```
let options = {};
let call = ...;

await endpoint.holdCall(call, options);
await endpoint.unholdCall(call, options);
```

## Transfer

TODO: Description

```
let options = {};
let call = ...;

await endpoint.xferCall(call, destination, options);
```

## DTMF

TODO: Description

```
let options = {};
let call = ...;
let key = "3";

await endpoint.dtmfCall(call, key, options);
```
