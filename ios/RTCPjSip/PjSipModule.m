#import "PjSipEndpoint.h"
#import "PjSipModule.h"

#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"

@interface PjSipModule ()

@end

@implementation PjSipModule

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue {
    // TODO: Use special thread may be ?
    // return dispatch_queue_create("com.carusto.PJSipMdule", DISPATCH_QUEUE_SERIAL);
    return dispatch_get_main_queue();
}

- (instancetype)init {
    self = [super init];
    return self;
}

RCT_EXPORT_METHOD(start: (RCTResponseSenderBlock) callback) {
    NSLog(@"start thread%@", [NSThread currentThread]);

    [PjSipEndpoint instance].bridge = self.bridge;

    NSDictionary *result = [[PjSipEndpoint instance] start];
    callback(@[@TRUE, result]);
}

// Account methods

RCT_EXPORT_METHOD(createAccount: (NSDictionary *) config callback:(RCTResponseSenderBlock) callback) {
    PjSipAccount *account = [[PjSipEndpoint instance] createAccount:config];
    callback(@[@TRUE, [account toJsonDictionary]]);
}

RCT_EXPORT_METHOD(deleteAccount: (int) accountId callback:(RCTResponseSenderBlock) callback) {
    [[PjSipEndpoint instance] deleteAccount:accountId];
    callback(@[@TRUE]);
}

// -----

RCT_EXPORT_METHOD(makeCall: (int) accountId destination: (NSString *) destination callback:(RCTResponseSenderBlock) callback) {
    PjSipAccount *account = [[PjSipEndpoint instance] findAccount:accountId];

    // account
    
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_METHOD(answerCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_METHOD(hangupCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_METHOD(holdCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_METHOD(unholdCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_METHOD(xferCall: (int) callId destination: (NSString *) destination callback:(RCTResponseSenderBlock) callback) {
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_METHOD(dtmfCall: (int) callId destination: (NSString *) digits callback:(RCTResponseSenderBlock) callback) {
    callback(@[@FALSE, @"Not implemented"]);
}

RCT_EXPORT_MODULE();

@end