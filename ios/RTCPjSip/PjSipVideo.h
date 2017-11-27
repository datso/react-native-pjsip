#import <Foundation/Foundation.h>
#import <React/RCTViewManager.h>
#import <VialerPJSIP/pjsua.h>
#import <AVFoundation/AVFoundation.h>

// TODO: Add ability to change device orientation!

typedef enum ObjectFit : NSUInteger {
    contain,
    cover
} ObjectFit;

@interface PjSipVideo : UIView
@property pjsua_vid_win_id winId;
@property UIView* winView;
@property ObjectFit winFit;

-(void)setWindowId:(pjsua_vid_win_id) windowId;
-(void)setObjectFit:(ObjectFit) objectFit;
@end
