
# Events

All interaction from javascript to pjsip module is asynchromius.
So for each action, promise will be returned.

# Create account

```
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
  "regContactParams": ";unique-device-token-id=XXXXXXXXX",
  "regOnAdd": false,  // Default true, use false for manual REGISTRATION
};

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



.then((account) => {
                console.log("Account: ", account);

                setTimeout(() => {
                    endpoint.registerAccount(account, true);
                }, 10000);

                setTimeout(() => {
                    endpoint.registerAccount(account, false);
                }, 20000);
            });




