import {DeviceEventEmitter, NativeModules, requireNativeComponent} from 'react-native';
import PropTypes from 'prop-types';

const RemoteVideoView = {
  name: 'PjSipRemoteVideoView',
  propTypes: {
  	windowId: PropTypes.string.isRequired,
	objectFit: PropTypes.oneOf(['contain', 'cover'])
  },
};

const View = requireNativeComponent('PjSipRemoteVideoView', null);

export default View;
