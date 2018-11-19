#import "PjSipEndpoint.h"
#import "PjSipModule.h"

#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>

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

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_METHOD(start: (NSDictionary *) config callback: (RCTResponseSenderBlock) callback) {
    [PjSipEndpoint instance].bridge = self.bridge;

    NSDictionary *result = [[PjSipEndpoint instance] start: config];
    callback(@[@TRUE, result]);
}

RCT_EXPORT_METHOD(updateStunServers: (int) accountId stunServerList:(NSArray *) stunServerList callback:(RCTResponseSenderBlock) callback) {
    [[PjSipEndpoint instance] updateStunServers:accountId stunServerList:stunServerList];
    callback(@[@TRUE]);
}

#pragma mark - Account Actions

RCT_EXPORT_METHOD(createAccount: (NSDictionary *) config callback:(RCTResponseSenderBlock) callback) {
    PjSipAccount *account = [[PjSipEndpoint instance] createAccount:config];
    callback(@[@TRUE, [account toJsonDictionary]]);
}

RCT_EXPORT_METHOD(deleteAccount: (int) accountId callback:(RCTResponseSenderBlock) callback) {
    [[PjSipEndpoint instance] deleteAccount:accountId];
    callback(@[@TRUE]);
}

RCT_EXPORT_METHOD(registerAccount: (int) accountId renew:(BOOL) renew callback:(RCTResponseSenderBlock) callback) {
    @try {
        PjSipEndpoint* endpoint = [PjSipEndpoint instance];
        PjSipAccount *account = [endpoint findAccount:accountId];
        
        [account register:renew];
        
        callback(@[@TRUE]);
    }
    @catch (NSException * e) {
        callback(@[@FALSE, e.reason]);
    }
}

#pragma mark - Call Actions

RCT_EXPORT_METHOD(makeCall: (int) accountId destination: (NSString *) destination callSettings:(NSDictionary*) callSettings msgData:(NSDictionary*) msgData callback:(RCTResponseSenderBlock) callback) {
    @try {
        PjSipEndpoint* endpoint = [PjSipEndpoint instance];
        PjSipAccount *account = [endpoint findAccount:accountId];
        PjSipCall *call = [endpoint makeCall:account destination:destination callSettings:callSettings msgData:msgData];
        
        // TODO: Remove this function
        // Automatically put other calls on hold.
        [endpoint pauseParallelCalls:call];
        
        callback(@[@TRUE, [call toJsonDictionary:endpoint.isSpeaker]]);
    }
    @catch (NSException * e) {
        callback(@[@FALSE, e.reason]);
    }
}

RCT_EXPORT_METHOD(hangupCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipCall *call = [[PjSipEndpoint instance] findCall:callId];
    
    if (call) {
        [call hangup];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(declineCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipCall *call = [[PjSipEndpoint instance] findCall:callId];
    
    if (call) {
        [call decline];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(answerCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    PjSipCall *call = [endpoint findCall:callId];
    
    if (call) {
        [call answer];
        
        // Automatically put other calls on hold.
        [endpoint pauseParallelCalls:call];
        
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(holdCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    PjSipCall *call = [endpoint findCall:callId];
    
    if (call) {
        [call hold];
        [endpoint emmitCallChanged:call];
        
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(unholdCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    PjSipCall *call = [endpoint findCall:callId];
    
    if (call) {
        [call unhold];
        [endpoint emmitCallChanged:call];
        
        // Automatically put other calls on hold.
        [endpoint pauseParallelCalls:call];
        
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(muteCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    PjSipCall *call = [endpoint findCall:callId];
    
    if (call) {
        [call mute];
        [endpoint emmitCallChanged:call];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(unMuteCall: (int) callId callback:(RCTResponseSenderBlock) callback) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    PjSipCall *call = [endpoint findCall:callId];
    
    if (call) {
        [call unmute];
        [endpoint emmitCallChanged:call];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(xferCall: (int) callId destination: (NSString *) destination callback:(RCTResponseSenderBlock) callback) {
    PjSipCall *call = [[PjSipEndpoint instance] findCall:callId];
    
    if (call) {
        [call xfer:destination];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(xferReplacesCall: (int) callId destinationCallId: (int) destinationCallId callback:(RCTResponseSenderBlock) callback) {
    PjSipCall *call = [[PjSipEndpoint instance] findCall:callId];
    
    if (call) {
        [call xferReplaces:destinationCallId];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(redirectCall: (int) callId destination: (NSString *) destination callback:(RCTResponseSenderBlock) callback) {
    PjSipCall *call = [[PjSipEndpoint instance] findCall:callId];
    
    if (call) {
        [call redirect:destination];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(dtmfCall: (int) callId digits: (NSString *) digits callback:(RCTResponseSenderBlock) callback) {
    PjSipCall *call = [[PjSipEndpoint instance] findCall:callId];
    
    if (call) {
        [call dtmf:digits];
        callback(@[@TRUE]);
    } else {
        callback(@[@FALSE, @"Call not found"]);
    }
}

RCT_EXPORT_METHOD(useSpeaker: (int) callId callback:(RCTResponseSenderBlock) callback) {
    [[PjSipEndpoint instance] useSpeaker];
}

RCT_EXPORT_METHOD(useEarpiece: (int) callId callback:(RCTResponseSenderBlock) callback) {
    [[PjSipEndpoint instance] useEarpiece];
}

RCT_EXPORT_METHOD(activateAudioSession: (RCTResponseSenderBlock) callback) {
    pjsua_set_no_snd_dev();
    pj_status_t status;
    status = pjsua_set_snd_dev(PJMEDIA_AUD_DEFAULT_CAPTURE_DEV, PJMEDIA_AUD_DEFAULT_PLAYBACK_DEV);
    if (status != PJ_SUCCESS) {
        NSLog(@"Failed to active audio session");
    }
}

RCT_EXPORT_METHOD(deactivateAudioSession: (RCTResponseSenderBlock) callback) {
    pjsua_set_no_snd_dev();
}

#pragma mark - Settings

RCT_EXPORT_METHOD(changeOrientation: (NSString*) orientation) {
    [[PjSipEndpoint instance] changeOrientation:orientation];
}

RCT_EXPORT_METHOD(changeCodecSettings: (NSDictionary*) codecSettings callback:(RCTResponseSenderBlock) callback) {
    [[PjSipEndpoint instance] changeCodecSettings:codecSettings];
    callback(@[@TRUE]);
}

RCT_EXPORT_MODULE();

@end
