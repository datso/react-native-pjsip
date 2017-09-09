#import <React/RCTViewManager.h>
#import "PjSipLocalVideoViewManager.m"
#import <VialerPJSIP/pjsua.h>
#import <React/RCTView.h>
#import <AVFoundation/AVFoundation.h>

/**
 * Implements an equivalent of {@code HTMLVideoElement} i.e. Web's video
 * element.
 */
@interface RTCLocalVideoView : RCTView

@property UIView* videoView;

@end

@implementation RTCLocalVideoView {
    
    
    
}

///**
// * Initializes and returns a newly allocated view object with the specified
// * frame rectangle.
// *
// * @param frame The frame rectangle for the view, measured in points.
// */
- (instancetype)init {
    self = [super init];
    
//    CGRect rc = self.frame;
//    self.frame = CGRectMake(rc.origin.x, rc.origin.y, 150, 150);
//    
//    self.backgroundColor = [UIColor greenColor];

    
    int numOfDevices = pjsua_vid_dev_count();
    
    NSLog(@"Number of devices %@", @(numOfDevices));
    
    
    unsigned dev_id, count;
    pjmedia_vid_dev_info vdi;
    pj_status_t status;
    
    count = pjsua_vid_dev_count();
    
    dev_id = 2;
    
//    int ori = PJMEDIA_ORIENT_ROTATE_180DEG;
//    pjsua_vid_dev_set_setting(dev_id, PJMEDIA_VID_DEV_CAP_ORIENTATION, &ori, PJ_TRUE);
    
    // for (dev_id=0; dev_id < count; ++dev_id) {
        NSLog(@"Render video device with %@ id", @(dev_id));
        
        status = pjsua_vid_dev_get_info(dev_id, &vdi);
        if (status == PJ_SUCCESS) {
            NSLog(@"Render video vdi.dir: %@", @(vdi.dir));
            NSLog(@"Render video vdi.name: %@", @(vdi.name));
            
            // Render video
            pjsua_vid_preview_param param;
            pjsua_vid_preview_param_default(&param);
            
            status = pjsua_vid_preview_start(dev_id, &param);
            
            if (status == PJ_SUCCESS) {
                pjsua_vid_win_info wi;
                pjsua_vid_win_id winId = pjsua_vid_preview_get_win(dev_id);
                
                NSLog(@"Render preview video window id: %@", @(winId));
                
                status = pjsua_vid_win_get_info(winId, &wi);
                
                if (status == PJ_SUCCESS) {
                    self.videoView = (__bridge UIView *)wi.hwnd.info.ios.window;
                    self.videoView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
                    
//                    CGRect newFrame = self.videoView.frame;
//                    newFrame.size.width = 100; // wi.size.w;
//                    newFrame.size.height = 100; // wi.size.h;
//                    newFrame.origin.x = 0;
//                    newFrame.origin.y = 0;
//                    
//                    [videoView setFrame:newFrame];
//                    [videoView setBounds:newFrame];
                    
                    NSLog(@"Render video info width : %@", @(wi.size.w));
                    NSLog(@"Render video info height : %@", @(wi.size.h));
                    
                    [self addSubview:self.videoView];

                    NSLog(@"Render video end with device %@ id", @(dev_id));
                } else {
                    NSLog(@"Failed to pjsua_vid_win_get_info %@ (%@ error code)", @(dev_id), @(status));
                }
            } else {
                NSLog(@"Failed to pjsua_vid_preview_start %@ (%@ error code)", @(dev_id), @(status));
            }
        } else {
            NSLog(@"Failed to pjsua_vid_dev_get_info %@ (%@ error code)", @(dev_id), @(status));
        }
        
    // }
    
    
//    UITextView* textView = [[UITextView alloc] initWithFrame:self.bounds];
//    textView.text = @"Hi BRO!";
//    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//
////    UIView* textView = [[UIView alloc] initWithFrame:self.bounds];
////    textView.backgroundColor = [UIColor redColor];
////    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//
//    [self addSubview:textView];

    return self;
}



- (void)layoutSubviews
{
    [super layoutSubviews];
    
//    NSLog(@"layoutSubviews called");
//    NSLog(@"layoutSubviews parent bounds size %@ / %@", @(self.bounds.size.width), @(self.bounds.size.height));
//    NSLog(@"layoutSubviews videoView bounds size %@ / %@", @(self.videoView.bounds.size.width), @(self.videoView.bounds.size.height));
//    
//    /* Resize it to fit width */
//    self.videoView.bounds = CGRectMake(0, 0, self.bounds.size.width,
//                                       (self.bounds.size.height *
//                                        1.0*self.bounds.size.width/
//                                        self.videoView.bounds.size.width));
//    
//    /* Center it horizontally */
//    self.videoView.center = CGPointMake(self.bounds.size.width/2.0,
//                                        self.videoView.bounds.size.height/2.0);
    
    
    UIView *subview = self.videoView;
    if (!subview) {
        return;
    }
    
    CGFloat width = self.videoView.bounds.size.width, height = self.videoView.bounds.size.height;
    CGRect newValue;
    if (width <= 0 || height <= 0) {
        newValue.origin.x = 0;
        newValue.origin.y = 0;
        newValue.size.width = 0;
        newValue.size.height = 0;
    } else if (true /*RTCVideoViewObjectFitCover == self.objectFit*/) { // cover
        newValue = self.bounds;
        // Is there a real need to scale subview?
        if (newValue.size.width != width || newValue.size.height != height) {
            CGFloat scaleFactor
            = MAX(newValue.size.width / width, newValue.size.height / height);
            // Scale both width and height in order to make it obvious that the aspect
            // ratio is preserved.
            width *= scaleFactor;
            height *= scaleFactor;
            newValue.origin.x += (newValue.size.width - width) / 2.0;
            newValue.origin.y += (newValue.size.height - height) / 2.0;
            newValue.size.width = width;
            newValue.size.height = height;
        }
    } else { // contain
        // The implementation is in accord with
        // https://www.w3.org/TR/html5/embedded-content-0.html#the-video-element:
        //
        // In the absence of style rules to the contrary, video content should be
        // rendered inside the element's playback area such that the video content
        // is shown centered in the playback area at the largest possible size that
        // fits completely within it, with the video content's aspect ratio being
        // preserved. Thus, if the aspect ratio of the playback area does not match
        // the aspect ratio of the video, the video will be shown letterboxed or
        // pillarboxed. Areas of the element's playback area that do not contain the
        // video represent nothing.
        newValue
        = AVMakeRectWithAspectRatioInsideRect(
                                              CGSizeMake(width, height),
                                              self.bounds);
    }
    
    CGRect oldValue = subview.frame;
    if (newValue.origin.x != oldValue.origin.x
        || newValue.origin.y != oldValue.origin.y
        || newValue.size.width != oldValue.size.width
        || newValue.size.height != oldValue.size.height) {
        subview.frame = newValue;
    }
    
    subview.transform
    = /*self.mirror
    ? CGAffineTransformMakeScale(-1.0, 1.0)
    : */ CGAffineTransformIdentity;
    
}

@end

@interface PjSipLocalVideoViewManager : RCTViewManager

@end


@implementation PjSipLocalVideoViewManager

RCT_EXPORT_MODULE()

-(UIView *) view {
    return [[RTCLocalVideoView alloc] init];
}

- (dispatch_queue_t) methodQueue {
    return dispatch_get_main_queue();
}

RCT_CUSTOM_VIEW_PROPERTY(deviceId, NSString, RTCLocalVideoView) {
//    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY deviceId %@", [RCTConvert NSString:json]);
//    
//    // TODO: Close previous camera device ID
//    pjmedia_vid_dev_index dev_id = [[RCTConvert NSString:json] intValue];
//    pjsua_vid_preview_param param;
//    pjsua_vid_preview_param_default(&param);
//    // param.wnd_flags = PJMEDIA_VID_DEV_WND_BORDER | PJMEDIA_VID_DEV_WND_RESIZABLE;
//    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY pjsua_vid_preview_start %@", @(pjsua_vid_preview_start(dev_id, &param)));
//    
//    pjsua_vid_win_info wi;
//    pjsua_vid_win_get_info(dev_id, &wi);
//    
//    view.videoInfo = wi;
//    view.videoView = (__bridge UIView *)wi.hwnd.info.ios.window;
//    
//    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY view %p", view);
//    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY view.videoView %p", view.videoView);
//    
//    view.videoView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//    
//    // if (videoView && ![videoView isDescendantOfView:parent]) {
//    CGRect newFrame = view.videoView.frame;
//    newFrame.size.width = wi.size.w;
//    newFrame.size.height = wi.size.h;
//    newFrame.origin.x = 0;
//    newFrame.origin.y = 0;
//    
////    NSLog(@"newFrame.size.width: %@", @(newFrame.size.width));
////    NSLog(@"newFrame.size.height: %@", @(newFrame.size.height));
//    
//    [view.videoView setFrame:view.bounds];
//    
//    NSLog(@"view.bounds.size.height: %@", @(view.bounds.size.height));
//    NSLog(@"view.bounds.size.width: %@", @(view.bounds.size.width));
//    NSLog(@"view.bounds.origin.x: %@", @(view.bounds.origin.x));
//    NSLog(@"view.bounds.origin.y: %@", @(view.bounds.origin.y));
//    
//    [view addSubview:view.videoView];
//    
////    if (!wi.is_native) {
////        NSLog(@"Video view is native");
////        
////        /* Resize it to fit width */
////        videoView.bounds = CGRectMake(0, 0, parent.bounds.size.width,
////                                 (parent.bounds.size.height *
////                                  1.0*parent.bounds.size.width/
////                                  videoView.bounds.size.width));
////        /* Center it horizontally */
////        videoView.center = CGPointMake(parent.bounds.size.width/2.0,
////                                  videoView.bounds.size.height/2.0);
////    } else {
////        NSLog(@"Video view is NOT native");
////        
////        /* Preview window, move it to the bottom */
////        videoView.center = CGPointMake(parent.bounds.size.width/2.0,
////                                  parent.bounds.size.height-
////                                  videoView.bounds.size.height/2.0);
////    }
//    
//    // }
//    
//    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY camera end! %@", [RCTConvert NSString:json]);
//    
//    
//    
//    //    UITextView* textView = [[UITextView alloc] initWithFrame:self.bounds];
//    //    textView.text = @"Hi BRO!";
//    //    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//    //
//    ////    UIView* textView = [[UIView alloc] initWithFrame:self.bounds];
//    ////    textView.backgroundColor = [UIColor redColor];
//    ////    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//    //
//    //    [self addSubview:textView];
    
}


//RCT_CUSTOM_VIEW_PROPERTY(translucent, BOOL, RCCToolBarView)
//{
//    view.toolBarTranslucent = [RCTConvert BOOL:json];
//}



@end
