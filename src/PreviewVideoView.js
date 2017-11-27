import {DeviceEventEmitter, NativeModules, requireNativeComponent} from 'react-native';
import PropTypes from 'prop-types';

const PreviewVideoView = {
  name: 'PjSipPreviewVideoView',
  propTypes: {
    deviceId: PropTypes.number.isRequired,
	objectFit: PropTypes.oneOf(['contain', 'cover'])
  },
};

const View = requireNativeComponent('PjSipPreviewVideoView', null);

export default View;
