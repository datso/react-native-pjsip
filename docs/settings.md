# Settings


# Connectivity settings
TODO

# Network settings
TODO


# Codecs settings
Print codec settings
```javascript
let endpoint = new Endpoint();
let state = await endpoint.start();
let {accounts, calls, settings, connectivity} = state;

console.log("codecs", settings.codecs); // Shows a list of available codecs with priority
```

Change codec configuration
```javascript
// Not listed codecs are automatically will have zero priority
endpoint.changeCodecSettings({
  "PCMA/8000/1": 0,
  "G722/16000/1": 0,  // Zero means to disable the codec.
  "iLBC/8000/1": 210,
  "speex/8000/1": 0,
  "speex/16000/1": 0,
  "speex/32000/1": 0
})
```