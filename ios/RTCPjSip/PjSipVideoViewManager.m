#import <React/RCTViewManager.h>
#import "PjSipVideoViewManager.m"
#import <VialerPJSIP/pjsua.h>
#import <React/RCTView.h>

/**
 * Implements an equivalent of {@code HTMLVideoElement} i.e. Web's video
 * element.
 */
@interface RTCVideoView : RCTView

// @property pjsua_vid_win_id vidWinId;

@end

@implementation RTCVideoView {
    
}

///**
// * Initializes and returns a newly allocated view object with the specified
// * frame rectangle.
// *
// * @param frame The frame rectangle for the view, measured in points.
// */
//- (instancetype)init {
//    self = [super init];
//
//    UITextView* textView = [[UITextView alloc] initWithFrame:self.bounds];
//    textView.text = @"Hi BRO!";
//    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//
////    UIView* textView = [[UIView alloc] initWithFrame:self.bounds];
////    textView.backgroundColor = [UIColor redColor];
////    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//    
//    [self addSubview:textView];
//    
//    return self;
//}

//- (instancetype)init
//{
//    self = [super init];
//    
//    
//    UITextView *myTextView = [[UITextView alloc] initWithFrame:asd];
//    [self.view addSubview:myTextView];
//    
//    UITextView* textView = [[UITextView alloc] init];
//    textView.text = @"Hi BRO!";
//    
//    [self addSubview:textView];
//
//    return self;
//}


@end

@interface PjSipVideoViewManager : RCTViewManager

@end


@implementation PjSipVideoViewManager

RCT_EXPORT_MODULE()

-(UIView *)view {
    return [[RTCVideoView alloc] init];
}

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

RCT_CUSTOM_VIEW_PROPERTY(windowId, NSNumber, RTCVideoView) {
    
//    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY");
//    
//    UIView* textView = [[UIView alloc] initWithFrame:view.bounds];
//    textView.backgroundColor = [UIColor redColor];
//    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//    
//    [view addSubview:textView];
//    
//    
//    __weak UIView *weakSelf = view;
//    dispatch_async(dispatch_get_main_queue(), ^{
//        UIView *strongSelf = weakSelf;
//        [strongSelf setNeedsLayout];
//    });
    
    
    // TODO: Remove this

    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY");
    
    pjsua_vid_win_id c[64];
    unsigned k, count = PJ_ARRAY_SIZE(c);
    pjsua_vid_enum_wins(c, &count);
    
    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY Size: %d", count);
    
    for (NSUInteger i = 0; i < count; i++) {
        NSLog(@"Window Id: %d", c[i]);
    }
    
    
    // Start
    NSNumber *s = [RCTConvert NSNumber:json];
    
    int d = [s intValue], i, last;
    i = (d == PJSUA_INVALID_ID) ? 0 : d;
    last = (d == PJSUA_INVALID_ID) ? PJSUA_MAX_VID_WINS : d+1;
    
    
    
    for (;i < last; ++i) {
        pjsua_vid_win_info wi;
        
        if (pjsua_vid_win_get_info(i, &wi) == PJ_SUCCESS) {
            UIView *parent = view;
            UIView *videoView = (__bridge UIView *)wi.hwnd.info.ios.window;
            videoView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
            
            if (videoView && ![videoView isDescendantOfView:parent]) {
                CGRect newFrame = videoView.frame;
                newFrame.size.width = wi.size.w;
                newFrame.size.height = wi.size.h;
                
                [videoView setFrame:view.bounds];
                [view addSubview:videoView];
                
                
                NSLog(@"Add video to view!!!");
                
                
//                
//                
//                
//                
//                dispatch_async(dispatch_get_main_queue(), ^{
//                    UIView *strongSelf = weakSelf;
//                    
//                    /* Add the video window as subview */
//                    // [strongSelf addSubview:videoView];
//                    
////                    CGRect newFrame = videoView.frame;
////                    newFrame.size.width = wi.size.w;
////                    newFrame.size.height = wi.size.h;
//                    
////                    [videoView setFrame:newFrame];
//                    
//                    
//                    UITextView* textView = [[UITextView alloc] initWithFrame:strongSelf.bounds];
//                    textView.text = @"Hi BRO!";
//                    textView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//                    
//                    [view addSubview:textView];
//                    
//                    NSLog(@"Add subview!!");
//                    
////                    if (!wi.is_native) {
////                        /* Center it horizontally */
////                        videoView.center = CGPointMake(parent.bounds.size.width/2.0,
////                                                  videoView.bounds.size.height/2.0);
////                    } else {
////                        /* Preview window, move it to the bottom */
////                        videoView.center = CGPointMake(parent.bounds.size.width/2.0,
////                                                  parent.bounds.size.height-
////                                                  videoView.bounds.size.height/2.0);
////                    }
//                    [strongSelf setNeedsLayout];
//                });
            }
        }
    }
    
    
    
//    RTCVideoTrack *videoTrack;
//    
//    if (json) {
//        NSString *streamId = (NSString *)json;
//        
//        WebRTCModule *module = [self.bridge moduleForName:@"WebRTCModule"];
//        RTCMediaStream *stream = module.mediaStreams[streamId];
//        NSArray *videoTracks = stream.videoTracks;
//        
//        videoTrack = videoTracks.count ? videoTracks[0] : nil;
//    } else {
//        videoTrack = nil;
//    }
//    
//    view.videoTrack = videoTrack;
}


@end
