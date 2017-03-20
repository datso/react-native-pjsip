import {DeviceEventEmitter, NativeModules, requireNativeComponent} from 'react-native';
import {PropTypes} from 'react';

const VideoView = {
  name: 'PjSipVideoView',
  propTypes: {
    windowId: PropTypes.number
  },
};

// const View = requireNativeComponent('PjSipVideoView', VideoView, {nativeOnly: {
//   testID: true,
//   accessibilityComponentType: true,
//   renderToHardwareTextureAndroid: true,
//   accessibilityLabel: true,
//   accessibilityLiveRegion: true,
//   importantForAccessibility: true,
//   onLayout: true,
// }});
const View = requireNativeComponent('PjSipVideoView', null);

export default View;
