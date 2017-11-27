#import <React/RCTViewManager.h>
#import <VialerPJSIP/pjsua.h>
#import <React/RCTView.h>
#import <AVFoundation/AVFoundation.h>
#import "PjSipVideo.h"
#import "PjSipPreviewVideoViewManager.h"

@implementation PjSipPreviewVideoViewManager

RCT_EXPORT_MODULE()

-(UIView *) view {
    return [[PjSipVideo alloc] init];
}

- (dispatch_queue_t) methodQueue {
    return dispatch_get_main_queue();
}

RCT_CUSTOM_VIEW_PROPERTY(deviceId, NSNumber, PjSipVideo) {
    pjmedia_vid_dev_index device = [[RCTConvert NSNumber:json] intValue];
    pjsua_vid_preview_param param;
    pjsua_vid_preview_param_default(&param);
    
    pj_status_t status = pjsua_vid_preview_start(device, &param);
    
    if (status == PJ_SUCCESS) {
        pjsua_vid_win_id winId = pjsua_vid_preview_get_win(device);
        [view setWindowId:winId];
    } else {
        NSLog(@"Failed to pjsua_vid_win_get_info %@ (%@ error code)", @(device), @(status));
        [view setWindowId:-1];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(objectFit, NSString, PjSipVideo) {
    ObjectFit fit = contain;
    
    if ([[RCTConvert NSString:json] isEqualToString:@"cover"]) {
        fit = cover;
    }
    
    [view setObjectFit:fit];
}

@end
