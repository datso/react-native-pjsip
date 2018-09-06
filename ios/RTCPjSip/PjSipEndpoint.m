@import AVFoundation;

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>
#import <VialerPJSIP/pjsua.h>

#import "PjSipUtil.h"
#import "PjSipEndpoint.h"
#import "PjSipMessage.h"

@implementation PjSipEndpoint

+ (instancetype) instance {
    static PjSipEndpoint *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[PjSipEndpoint alloc] init];
    });

    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    self.accounts = [[NSMutableDictionary alloc] initWithCapacity:12];
    self.calls = [[NSMutableDictionary alloc] initWithCapacity:12];

    pj_status_t status;

    // Create pjsua first
    status = pjsua_create();
    if (status != PJ_SUCCESS) {
        NSLog(@"Error in pjsua_create()");
    }
    
    // Init pjsua
    {
        // Init the config structure
        pjsua_config cfg;
        pjsua_config_default(&cfg);

        // cfg.cb.on_reg_state = [self performSelector:@selector(onRegState:) withObject: o];
        cfg.cb.on_reg_state = &onRegStateChanged;
        cfg.cb.on_incoming_call = &onCallReceived;
        cfg.cb.on_call_state = &onCallStateChanged;
        cfg.cb.on_call_media_state = &onCallMediaStateChanged;
        cfg.cb.on_call_media_event = &onCallMediaEvent;
        
        cfg.cb.on_pager2 = &onMessageReceived;
        
        // on_call_video_state
        
//        cfg.cfg.cb.on_call_media_state = &on_call_media_state;
//        cfg.cfg.cb.on_incoming_call = &on_incoming_call;
//        cfg.cfg.cb.on_call_tsx_state = &on_call_tsx_state;
//        cfg.cfg.cb.on_dtmf_digit = &call_on_dtmf_callback;
//        cfg.cfg.cb.on_call_redirected = &call_on_redirected;
//        cfg.cfg.cb.on_reg_state = &on_reg_state;
//        cfg.cfg.cb.on_incoming_subscribe = &on_incoming_subscribe;
//        cfg.cfg.cb.on_buddy_state = &on_buddy_state;
//        cfg.cfg.cb.on_buddy_evsub_state = &on_buddy_evsub_state;
//        cfg.cfg.cb.on_pager = &on_pager;
//        cfg.cfg.cb.on_typing = &on_typing;
//        cfg.cfg.cb.on_call_transfer_status = &on_call_transfer_status;
//        cfg.cfg.cb.on_call_replaced = &on_call_replaced;
//        cfg.cfg.cb.on_nat_detect = &on_nat_detect;
//        cfg.cfg.cb.on_mwi_info = &on_mwi_info;
//        cfg.cfg.cb.on_transport_state = &on_transport_state;
//        cfg.cfg.cb.on_ice_transport_error = &on_ice_transport_error;
//        cfg.cfg.cb.on_snd_dev_operation = &on_snd_dev_operation;
//        cfg.cfg.cb.on_call_media_event = &on_call_media_event;
        
        // pjsua_vid_enum_wins(<#pjsua_vid_win_id *wids#>, <#unsigned int *count#>)

        // Init the logging config structure
        pjsua_logging_config log_cfg;
        pjsua_logging_config_default(&log_cfg);
        log_cfg.console_level = 10;

        // Init media config
        pjsua_media_config mediaConfig;
        pjsua_media_config_default(&mediaConfig);
        mediaConfig.clock_rate = PJSUA_DEFAULT_CLOCK_RATE;
        mediaConfig.snd_clock_rate = 0;
        
        // Init the pjsua
        status = pjsua_init(&cfg, &log_cfg, &mediaConfig);
        if (status != PJ_SUCCESS) {
            NSLog(@"Error in pjsua_init()");
        }
    }

    // Add UDP transport.
    {
        // Init transport config structure
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        pjsua_transport_id id;

        // Add TCP transport.
        status = pjsua_transport_create(PJSIP_TRANSPORT_UDP, &cfg, &id);
        
        if (status != PJ_SUCCESS) {
            NSLog(@"Error creating UDP transport");
        } else {
            self.udpTransportId = id;
        }
    }
    
    // Add TCP transport.
    {
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        pjsua_transport_id id;
        
        status = pjsua_transport_create(PJSIP_TRANSPORT_TCP, &cfg, &id);
        
        if (status != PJ_SUCCESS) {
            NSLog(@"Error creating TCP transport");
        } else {
            self.tcpTransportId = id;
        }
    }
    
    // Add TLS transport.
    {
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        pjsua_transport_id id;
        
        status = pjsua_transport_create(PJSIP_TRANSPORT_TLS, &cfg, &id);
        
        if (status != PJ_SUCCESS) {
            NSLog(@"Error creating TLS transport");
        } else {
            self.tlsTransportId = id;
        }
    }
    
    // Initialization is done, now start pjsua
    status = pjsua_start();
    if (status != PJ_SUCCESS) NSLog(@"Error starting pjsua");
    
    return self;
}

- (NSDictionary *)start: (NSDictionary *)config {
    NSMutableArray *accountsResult = [[NSMutableArray alloc] initWithCapacity:[@([self.accounts count]) unsignedIntegerValue]];
    NSMutableArray *callsResult = [[NSMutableArray alloc] initWithCapacity:[@([self.calls count]) unsignedIntegerValue]];
    NSDictionary *settingsResult = @{ @"codecs": [self getCodecs] };

    for (NSString *key in self.accounts) {
        PjSipAccount *acc = self.accounts[key];
        [accountsResult addObject:[acc toJsonDictionary]];
    }
    
    for (NSString *key in self.calls) {
        PjSipCall *call = self.calls[key];
        [callsResult addObject:[call toJsonDictionary:self.isSpeaker]];
    }
    
    if ([accountsResult count] > 0 && config[@"service"] && config[@"service"][@"stun"]) {
        for (NSDictionary *account in accountsResult) {
            int accountId = account[@"_data"][@"id"];
            [[PjSipEndpoint instance] updateStunServers:accountId stunServerList:config[@"service"][@"stun"]];
        }
    }
    
    return @{@"accounts": accountsResult, @"calls": callsResult, @"settings": settingsResult, @"connectivity": @YES};
}

- (void)updateStunServers:(int)accountId stunServerList:(NSArray *)stunServerList {
    int size = [stunServerList count];
    int count = 0;
    pj_str_t srv[size];
    for (NSString *stunServer in stunServerList) {
        srv[count] = pj_str([stunServer UTF8String]);
        count++;
    }
    
    pjsua_acc_config cfg_update;
    pj_pool_t *pool = pjsua_pool_create("tmp-pjsua", 1000, 1000);
    pjsua_acc_config_default(&cfg_update);
    pjsua_acc_get_config(accountId, pool, &cfg_update);
    NSLog([NSString stringWithFormat: @"I AM ACC ID: %d", accountId]);
    pjsua_update_stun_servers(size, srv, false);
    
    pjsua_acc_modify(accountId, &cfg_update);
}

- (PjSipAccount *)createAccount:(NSDictionary *)config {
    PjSipAccount *account = [PjSipAccount itemConfig:config];
    self.accounts[@(account.id)] = account;
    
    return account;
}

- (void)deleteAccount:(int) accountId {
    // TODO: Destroy function ?
    if (self.accounts[@(accountId)] == nil) {
        [NSException raise:@"Failed to delete account" format:@"Account with %@ id not found", @(accountId)];
    }

    [self.accounts removeObjectForKey:@(accountId)];
}

- (PjSipAccount *) findAccount: (int) accountId {
    return self.accounts[@(accountId)];
}


#pragma mark Calls

-(PjSipCall *) makeCall:(PjSipAccount *) account destination:(NSString *)destination callSettings: (NSDictionary *)callSettingsDict msgData: (NSDictionary *)msgDataDict {
    pjsua_call_setting callSettings;
    [PjSipUtil fillCallSettings:&callSettings dict:callSettingsDict];
    
    pj_caching_pool cp;
    pj_pool_t *pool;
    
    pj_caching_pool_init(&cp, &pj_pool_factory_default_policy, 0);
    pool = pj_pool_create(&cp.factory, "header", 1000, 1000, NULL);
    
    pjsua_msg_data msgData;
    pjsua_msg_data_init(&msgData);
    [PjSipUtil fillMsgData:&msgData dict:msgDataDict pool:pool];
    
    
    pjsua_call_id callId;
    pj_str_t callDest = pj_str((char *) [destination UTF8String]);
    
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
    
    pj_status_t status = pjsua_call_make_call(account.id, &callDest, &callSettings, NULL, &msgData, &callId);
    
    if (status != PJ_SUCCESS) {
        [NSException raise:@"Failed to make a call" format:@"See device logs for more details."];
    }
    pj_pool_release(pool);
    
    PjSipCall *call = [PjSipCall itemConfig:callId];
    self.calls[@(callId)] = call;
    
    return call;
}

- (PjSipCall *) findCall: (int) callId {
    return self.calls[@(callId)];
}

-(void) pauseParallelCalls:(PjSipCall*) call {
    for(id key in self.calls) {
        if (key != call.id) {
            for (NSString *key in self.calls) {
                PjSipCall *parallelCall = self.calls[key];
                
                if (call.id != parallelCall.id && !parallelCall.isHeld) {
                    [parallelCall hold];
                    [self emmitCallChanged:parallelCall];
                }
            }
        }
    }
}

-(void)useSpeaker {
    self.isSpeaker = true;
    
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    [audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:nil];
    
    for (NSString *key in self.calls) {
        PjSipCall *call = self.calls[key];
        [self emmitCallChanged:call];
    }
}

-(void)useEarpiece {
    self.isSpeaker = false;
    
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    [audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:nil];
    
    for (NSString *key in self.calls) {
        PjSipCall *call = self.calls[key];
        [self emmitCallChanged:call];
    }
}

#pragma mark - Settings

-(void) changeOrientation: (NSString*) orientation {
    pjmedia_orient orient = PJMEDIA_ORIENT_ROTATE_90DEG;
    
    if ([orientation isEqualToString:@"PJMEDIA_ORIENT_ROTATE_270DEG"]) {
        orient = PJMEDIA_ORIENT_ROTATE_270DEG;
    } else if ([orientation isEqualToString:@"PJMEDIA_ORIENT_ROTATE_180DEG"]) {
        orient = PJMEDIA_ORIENT_ROTATE_180DEG;
    } else if ([orientation isEqualToString:@"PJMEDIA_ORIENT_NATURAL"]) {
        orient = PJMEDIA_ORIENT_NATURAL;
    }
    
    /* Here we set the orientation for all video devices.
     * This may return failure for renderer devices or for
     * capture devices which do not support orientation setting,
     * we can simply ignore them.
    */
    for (int i = pjsua_vid_dev_count() - 1; i >= 0; i--) {
        pjsua_vid_dev_set_setting(i, PJMEDIA_VID_DEV_CAP_ORIENTATION, &orient, PJ_TRUE);
    }
}

-(void) changeCodecSettings: (NSDictionary*) codecSettings {
    
    for (NSString * key in codecSettings) {
        pj_str_t codec_id = pj_str((char *) [key UTF8String]);
        NSNumber * priority = codecSettings[key];
        pj_uint8_t convertedPriority = [priority integerValue];
        pjsua_codec_set_priority(&codec_id, convertedPriority);
    }
}

- (NSMutableDictionary *) getCodecs {
    //32 max possible codecs
    pjsua_codec_info codec[32];
    NSMutableDictionary *codecs = [[NSMutableDictionary alloc] initWithCapacity:32];
    unsigned uCount = 32;
    
    if (pjsua_enum_codecs(codec, &uCount) == PJ_SUCCESS) {
        for (unsigned i = 0; i < uCount; ++i) {
            NSString * codecName = [NSString stringWithFormat:@"%s", codec[i].codec_id.ptr];
            [codecs setObject:[NSNumber numberWithInt: codec[i].priority] forKey: codecName];
        }
    }
    return codecs;
}


#pragma mark - Events

-(void)emmitRegistrationChanged:(PjSipAccount*) account {
    [self emmitEvent:@"pjSipRegistrationChanged" body:[account toJsonDictionary]];
}

-(void)emmitCallReceived:(PjSipCall*) call {
    [self emmitEvent:@"pjSipCallReceived" body:[call toJsonDictionary:self.isSpeaker]];
}

-(void)emmitCallChanged:(PjSipCall*) call {
    [self emmitEvent:@"pjSipCallChanged" body:[call toJsonDictionary:self.isSpeaker]];
}

-(void)emmitCallTerminated:(PjSipCall*) call {
    [self emmitEvent:@"pjSipCallTerminated" body:[call toJsonDictionary:self.isSpeaker]];
}

-(void)emmitMessageReceived:(PjSipMessage*) message {
    [self emmitEvent:@"pjSipMessageReceived" body:[message toJsonDictionary]];
}

-(void)emmitEvent:(NSString*) name body:(id)body {
    [[self.bridge eventDispatcher] sendAppEventWithName:name body:body];
}


#pragma mark - Callbacks

static void onRegStateChanged(pjsua_acc_id accId) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    PjSipAccount* account = [endpoint findAccount:accId];
    
    if (account) {
        [endpoint emmitRegistrationChanged:account];
    }
}

static void onCallReceived(pjsua_acc_id accId, pjsua_call_id callId, pjsip_rx_data *rx) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    
    PjSipCall *call = [PjSipCall itemConfig:callId];
    endpoint.calls[@(callId)] = call;
    
    [endpoint emmitCallReceived:call];
}

static void onCallStateChanged(pjsua_call_id callId, pjsip_event *event) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    
    pjsua_call_info callInfo;
    pjsua_call_get_info(callId, &callInfo);
    
    PjSipCall* call = [endpoint findCall:callId];
    
    if (!call) {
        return;
    }
    
    [call onStateChanged:callInfo];
    
    if (callInfo.state == PJSIP_INV_STATE_DISCONNECTED) {
        [endpoint.calls removeObjectForKey:@(callId)];
        [endpoint emmitCallTerminated:call];
    } else {
        [endpoint emmitCallChanged:call];
    }
}

static void onCallMediaStateChanged(pjsua_call_id callId) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    
    pjsua_call_info callInfo;
    pjsua_call_get_info(callId, &callInfo);
    
    PjSipCall* call = [endpoint findCall:callId];
    
    if (call) {
        [call onMediaStateChanged:callInfo];
    }
    
    [endpoint emmitCallChanged:call];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"PjSipInvalidateVideo"
                                                        object:nil];
}

static void onCallMediaEvent(pjsua_call_id call_id,
                             unsigned med_idx,
                             pjmedia_event *event) {
    if (event->type == PJMEDIA_EVENT_FMT_CHANGED) {
        /* Adjust renderer window size to original video size */
        pjsua_call_info ci;
        pjsua_vid_win_id wid;
        pjmedia_rect_size size;
        
        pjsua_call_get_info(call_id, &ci);
        
        if ((ci.media[med_idx].type == PJMEDIA_TYPE_VIDEO) &&
            (ci.media[med_idx].dir & PJMEDIA_DIR_DECODING))
        {
            wid = ci.media[med_idx].stream.vid.win_in;
            size = event->data.fmt_changed.new_fmt.det.vid.size;

            pjsua_vid_win_set_size(wid, &size);
        }
    }
}

static void onMessageReceived(pjsua_call_id call_id, const pj_str_t *from,
                          const pj_str_t *to, const pj_str_t *contact,
                          const pj_str_t *mime_type, const pj_str_t *body,
                          pjsip_rx_data *rdata, pjsua_acc_id acc_id) {
    PjSipEndpoint* endpoint = [PjSipEndpoint instance];
    NSDictionary* data = [NSDictionary dictionaryWithObjectsAndKeys:
                          [NSNull null], @"test",
                          @(call_id), @"callId",
                          @(acc_id), @"accountId",
                          [PjSipUtil toString:contact], @"contactUri",
                          [PjSipUtil toString:from], @"fromUri",
                          [PjSipUtil toString:to], @"toUri",
                          [PjSipUtil toString:body], @"body",
                          [PjSipUtil toString:mime_type], @"contentType",
                          nil];
    PjSipMessage* message = [PjSipMessage itemConfig:data];
    
    [endpoint emmitMessageReceived:message];
}

@end
