#import "PjSipVideo.h"

@implementation PjSipVideo {
    
}

- (id) init {
    if (self = [super init]) {
        self.winId = -1;
        self.winFit = cover;
        self.winView = NULL;
    }
    return self;
}

- (void) setWindowId:(pjsua_vid_win_id) windowId {
    if (self.winId == windowId) {
        return;
    }
    
    self.winId = windowId;
    
    if (self.winView != NULL) {
        [self.winView removeFromSuperview];
        self.winView = NULL;
    }
    
    if (windowId >= 0) {
        pjsua_vid_win_info wi;
        pj_status_t status = pjsua_vid_win_get_info(windowId, &wi);
        
        if (status == PJ_SUCCESS) {
            self.winView = (__bridge UIView *) wi.hwnd.info.ios.window;
            // self.winView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
            
            if (wi.size.h > 0 && wi.size.w > 0) {
                self.winWidth = wi.size.w;
                self.winHeight = wi.size.h;
            } else {
                self.winWidth = self.winView.frame.size.width;
                self.winHeight = self.winView.frame.size.height;
            }
            
            NSLog(@"winView winView: %@", self.winView);
            
            NSLog(@"winView wi.pos.x: %@", @(wi.pos.x));
            NSLog(@"winView wi.pos.y: %@", @(wi.pos.y));
            NSLog(@"winView wi.size.h: %@", @(wi.size.h));
            NSLog(@"winView wi.size.w: %@", @(wi.size.w));
            NSLog(@"winView wi.is_native: %@", @(wi.is_native));

            NSLog(@"winView self.winView.bounds.size.width: %@", @(self.winView.bounds.size.width));
            NSLog(@"winView self.winView.bounds.size.height: %@", @(self.winView.bounds.size.height));
            NSLog(@"winView self.winView.frame.size.width: %@", @(self.winView.frame.size.width));
            NSLog(@"winView self.winView.frame.size.height: %@", @(self.winView.frame.size.height));
        
            [self addSubview:self.winView];
        }
    }
    
    [self dispatchAsyncSetNeedsLayout];
}

-(void)setObjectFit:(ObjectFit) objectFit {
    if (self.winFit == objectFit) {
        return;
    }

    self.winFit = objectFit;
    
    [self dispatchAsyncSetNeedsLayout];
}


- (void)layoutSubviews
{
    [super layoutSubviews];
    
    UIView *subview = self.winView;
    if (!subview) {
        return;
    }
    
    NSLog(@"layoutSubviews parent.bounds.size.width : %@", @(self.bounds.size.width));
    NSLog(@"layoutSubviews parent.bounds.size.height : %@", @(self.bounds.size.height));
    
    NSLog(@"layoutSubviews subview.bounds.size.width : %@", @(subview.bounds.size.width));
    NSLog(@"layoutSubviews subview.bounds.size.height : %@", @(subview.bounds.size.height));
    
    CGFloat width = self.winWidth, height = self.winHeight;
    
    NSLog(@"layoutSubviews self.winWidth : %@", @(self.winWidth));
    NSLog(@"layoutSubviews self.winHeight : %@", @(self.winHeight));
    
    
//    if (true) {
//        CGRect newValue = self.bounds;
//        
//        newValue.size.width = 352 / 2;
//        newValue.size.height = 288 / 2;
////        newValue.origin.x = 100;
////        newValue.origin.y = 100;
//        // self.winView.clipsToBounds = true;
//        self.winView.bounds = newValue;
//        // self.winView.center = CGPointMake(352 / 2.0, 288 / 2.0);
//        
//        return;
//    }
    
    CGRect newValue;
    if (width <= 0 || height <= 0) {
        newValue.origin.x = 0;
        newValue.origin.y = 0;
        newValue.size.width = 0;
        newValue.size.height = 0;
    } else if (self.winFit == cover) {
        newValue = self.bounds;
        
        if (newValue.size.width != width || newValue.size.height != height) {
            CGFloat scaleFactor = MAX(newValue.size.width / width, newValue.size.height / height);
            
            // Scale both width and height in order to make it obvious that the aspect
            // ratio is preserved.
            width *= scaleFactor;
            height *= scaleFactor;
            newValue.origin.x += (newValue.size.width - width) / 2.0;
            newValue.origin.y += (newValue.size.height - height) / 2.0;
            newValue.size.width = width;
            newValue.size.height = height;
        }
    } else {
        newValue = AVMakeRectWithAspectRatioInsideRect(CGSizeMake(width, height), self.bounds);
    }
    
    CGRect oldValue = subview.frame;
    if (newValue.origin.x != oldValue.origin.x
        || newValue.origin.y != oldValue.origin.y
        || newValue.size.width != oldValue.size.width
        || newValue.size.height != oldValue.size.height) {
        subview.frame = newValue;
    }
    
    NSLog(@"layoutSubviews newValue.origin.x: %@", @(newValue.origin.x));
    NSLog(@"layoutSubviews newValue.origin.y: %@", @(newValue.origin.y));
    NSLog(@"layoutSubviews newValue.size.width: %@", @(newValue.size.width));
    NSLog(@"layoutSubviews newValue.size.height: %@", @(newValue.size.height));
    
    subview.transform = CGAffineTransformIdentity;
}

/**
 * Invalidates the current layout of the receiver and triggers a layout update
 * during the next update cycle. Make sure that the method call is performed on
 * the application's main thread (as documented to be necessary by Apple).
 */
- (void)dispatchAsyncSetNeedsLayout {
    __weak UIView *weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *strongSelf = weakSelf;
        [strongSelf setNeedsLayout];
    });
}

@end
