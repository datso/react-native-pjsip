#import <RCTBridge.h>
#import "PjSipEndpoint.h"
#import "PjSipCallbacks.h"
#import "pjsua.h"

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
        cfg.cb.on_reg_state = &PjSipOnRegState;

//        cfg.cb.on_call_state = &on_call_state;
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

        // Init the logging config structure
        pjsua_logging_config log_cfg;
        pjsua_logging_config_default(&log_cfg);
        log_cfg.console_level = 4;

        // Init the pjsua
        status = pjsua_init(&cfg, &log_cfg, NULL);
        if (status != PJ_SUCCESS) {
            NSLog(@"Error in pjsua_init()");
        }
    }

    // Add UDP transport.
    {
        // Init transport config structure
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        cfg.port = 5080;

        // Add TCP transport.
        status = pjsua_transport_create(PJSIP_TRANSPORT_UDP, &cfg, NULL);
        if (status != PJ_SUCCESS) NSLog(@"Error creating transport");
    }

    // Add TCP transport.
    {
        // Init transport config structure
        pjsua_transport_config cfg;
        pjsua_transport_config_default(&cfg);
        cfg.port = 5080;

        // Add TCP transport.
        status = pjsua_transport_create(PJSIP_TRANSPORT_TCP, &cfg, NULL);
        if (status != PJ_SUCCESS) NSLog(@"Error creating transport");
    }

    // Initialization is done, now start pjsua
    status = pjsua_start();
    if (status != PJ_SUCCESS) NSLog(@"Error starting pjsua");

    return self;
}

- (NSDictionary *) start {
    NSMutableArray *accountsResult = [[NSMutableArray alloc] initWithCapacity:[@([self.accounts count]) unsignedIntegerValue]];
    NSMutableArray *callsResult = [[NSMutableArray alloc] init];

    for (NSString *key in self.accounts) {
        PjSipAccount *acc = self.accounts[key];
        [accountsResult addObject:[acc toJsonDictionary]];
    }

    return @{@"accounts": accountsResult, @"calls": callsResult};
}

- (PjSipAccount *)createAccount:(NSDictionary *)config {
    PjSipAccount *account = [PjSipAccount itemConfig:config];
    self.accounts[@(account.id)] = account;

    return account;
}

- (void)deleteAccount:(int) accountId {
    if (self.accounts[@(accountId)] == nil) {
        [NSException raise:@"Failed to delete account" format:@"Account with %@ id not found", @(accountId)];
    }

    [self.accounts removeObjectForKey:@(accountId)];
}

- (PjSipAccount *) findAccount: (int) accountId {
    return self.accounts[@(accountId)];
}

@end