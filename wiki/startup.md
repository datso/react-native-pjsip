
# Initialization

First of all you have to initialize module to be able to work with it.

There are some interesting moment in initialization.
When application goes to background, PJSIP module is still working and able to receive calls, but your javascipt is totally suspended.
When User open your application, javascript start to work and now your js application need to know what status have your account or may be you have pending incoming call.

So thats why first step should call start method for pjsip module.

```
let endpoint = new Endpoint();
let state = await endpoint.start();
let {accounts, calls} = state;
```

It works in background because in Android where is a service PjSip service, that you included in AndroidManifest.xml.
TODO: Describe how it works on iOS.
