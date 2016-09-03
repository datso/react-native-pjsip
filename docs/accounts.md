
# Events

All interaction from javascript to pjsip module is asynchromius.
So for each action, promise will be returned.

# Create account

```
let configuration = {
	username: '100',
	password: '****',
	host: '192.168.1.100' // IP or Domain name.
	port: 5060,
	realm: 'my.pbx.com' // Authorization host (see TODO: Link to wiki)
	transport: 'UDP' // Default TCP
}

let endpoint = new Endpoint();
let state = await endpoint.start();
let account = await endpoint.createAccont(configuration);

// Do smth with account. For example wait until registration complete and make a call.
```

* There is no change account method. But this functionality is easy to implement by calling delete and create account methods.


# Remove account

TODO: Description

```
let account = ...;
await endpoint.deleteAccont(account);

await endpoint.deleteAccont(account); // There should be exception, bcs account already removed.
```


# Events


## registration_changed

TODO: Answer how much times it will be executed during lifetime, with examples.

```

```


Example: Forbidden

Example: Invalid host






