#import <React/RCTViewManager.h>
#import <VialerPJSIP/pjsua.h>
#import <React/RCTView.h>
#import <AVFoundation/AVFoundation.h>
#import "PjSipUtil.h"
#import "PjSipVideo.h"
#import "PjSipRemoteVideoViewManager.h"

@implementation PjSipRemoteVideoViewManager

RCT_EXPORT_MODULE()

-(UIView *) view {
    return [[PjSipVideo alloc] init];
}

- (dispatch_queue_t) methodQueue {
    return dispatch_get_main_queue();
}

RCT_CUSTOM_VIEW_PROPERTY(windowId, NSString, PjSipVideo) {
    pjsua_vid_win_id windowId = [[RCTConvert NSNumber:json] intValue];
    [view setWindowId:windowId];
}

RCT_CUSTOM_VIEW_PROPERTY(objectFit, NSString, PjSipVideo) {
    ObjectFit fit = contain;
    
    if ([[RCTConvert NSString:json] isEqualToString:@"cover"]) {
        fit = cover;
    }
    
    [view setObjectFit:fit];
}

@end
