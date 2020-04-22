# Android installation

## Step 1
Add permissions & service to `android/app/src/main/AndroidManifest.xml`

```xml
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus"/>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS"/>
<uses-permission android:name="android.permission.CALL_PHONE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

```xml
<application>
    ...
    <service
        android:name="com.carusto.ReactNativePjSip.PjSipService"
        android:enabled="true"
        android:exported="true" />
    ...
</application>
```

## Step 2
```bash
react-native link
```

## Additional step: Ability to answer incoming call without Lock Screen

In `android/app/src/main/java/com/xxx/MainActivity.java`

```java
import android.view.Window;
import android.view.WindowManager;
import android.os.Bundle;
...
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window w = getWindow();
        w.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        );
    }
```

## If Android targetSdk >= 23

If your Android targetSdk is 23 or above you should grant `android.permission.RECORD_AUDIO` at runtime before making/receiving an audio call.

To check and request Android permissions, please check out [react-native-android-permissions](https://github.com/lucasferreira/react-native-android-permissions).
