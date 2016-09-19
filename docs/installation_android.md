# Android installation

## Step 1
Add permissions & service to `android/app/src/main/AndroidManifest.xml`

```xml
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus"/>

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

```xml
<application>
    ...
    <service android:name="com.carusto.PjSipService" android:enabled="true" android:exported="true"></service>
    ...
</application>
```

## Step 2
In `android/settings.gradle`, include PJSIPModule

```gradle
include ':PJSIPModule', ':app'
project(':PJSIPModule').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-pjsip/android')
```

## Step 3
In `android/app/build.gradle`, add PJSIPModule to dependencies

```gradle
dependencies {
  ...
  compile project(':PJSIPModule')
}
```

## Step 4
In `android/app/src/main/java/com/xxx/MainApplication.java`

```java
import com.carusto.PjSipModulePackage;  // <--- Add this line
...
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new PjSipModulePackage()                  // <--- Add this line
      );
    }
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