#import "RCTEventDispatcher.h"
#import "RCTBridge.h"
#import "PjSipEndpoint.h"
#import "PjSipAccount.h"
#import "PjSipUtil.h"
#import "pjsua.h"

@implementation PjSipAccount

+ (instancetype)itemConfig:(NSDictionary *)config {
    return [[self alloc] initWithConfig:config];
}

- (id)initWithConfig:(NSDictionary *)config {
    self = [super init];

    // TODO: Fire registration_changed
    // TODO: Fire call_received

    if (self) {
        self.name = config[@"name"];
        self.username = config[@"username"];
        self.domain = config[@"domain"];
        self.password = config[@"password"];
        self.proxy = config[@"proxy"];
        self.transport = config[@"transport"];
        self.regServer = config[@"regServer"];
        self.regTimeout = config[@"regTimeout"];

        NSString *cfgId = [NSString stringWithFormat:@"%@ <sip:%@@%@>", self.name, self.username, self.domain];
        NSString *cfgURI = [NSString stringWithFormat:@"sip:%@", self.domain];

        pjsua_acc_config cfg;
        pjsua_acc_config_default(&cfg);

        cfg.id = pj_str((char *) [cfgId UTF8String]);
        cfg.reg_uri = pj_str((char *) [cfgURI UTF8String]);

        pjsip_cred_info cred;
        cred.scheme = pj_str("digest");
        cred.realm = [self.regServer length] > 0 ? pj_str((char *) [self.regServer UTF8String]) : pj_str("*");
        cred.username = pj_str((char *) [self.username UTF8String]);
        cred.data = pj_str((char *) [self.password UTF8String]);
        cred.data_type = PJSIP_CRED_DATA_PLAIN_PASSWD;

        cfg.cred_count = 1;
        cfg.cred_info[0] = cred;

        if ([self.proxy length] > 0) {
            cfg.proxy_cnt = 1;
            cfg.proxy[0] = pj_str((char *) [[NSString stringWithFormat:@"sip:%@", self.proxy] UTF8String]);
        }

        if (self.regTimeout != nil) {
            cfg.reg_timeout = (unsigned) [self.regTimeout intValue];
        } else {
            cfg.reg_timeout = (unsigned) 600;
        }

        // TODO: Create transport depending on configuration
        // cfg.transport_id = [self initTransport]; // NSString *transport = config[@"transport"];
        // NSLog(@"cfg.transport_id %d", cfg.transport_id);

        pj_status_t status;
        pjsua_acc_id account_id;

        status = pjsua_acc_add(&cfg, PJ_TRUE, &account_id);
        if (status != PJ_SUCCESS) {
            [NSException raise:@"Failed to create account" format:@"See device logs for more details."];
        }

        self.id = account_id;
    }

    return self;
}

// TODO: Add configuration parameter for UDP/TLS
- (pjsua_transport_id)initTransport {
    pjsua_transport_id id;
    pjsua_transport_config cfg;
    pjsua_transport_config_default(&cfg);

    // Add TCP transport.
    pj_status_t status = pjsua_transport_create(PJSIP_TRANSPORT_TCP, &cfg, &id);
    if (status != PJ_SUCCESS) {
        NSLog(@"Error creating transport");
    }

    return id;
}

- (void) dealloc {
    NSLog(@"Called dealloc");
    pjsua_acc_set_registration(self.id, PJ_FALSE);
    pjsua_acc_del(self.id);
}


#pragma mark EventHandlers

- (void)onRegistrationChanged {
    [[[PjSipEndpoint instance].bridge eventDispatcher] sendAppEventWithName:@"pjSipRegistrationChanged" body:[self toJsonDictionary]];
}

#pragma mark -


- (NSDictionary *)toJsonDictionary {
    pjsua_acc_info info;
    pjsua_acc_get_info(self.id, &info);

    // Format registration status

    NSDictionary * registration = @{
        @"status": [PjSipUtil toString:(pj_str_t *) pjsip_get_status_text(info.status)],
        @"statusText": [PjSipUtil toString:&info.status_text],
        @"active": @"test",
        @"reason": @"test"
    };

    // Format account info

    return @{
        @"id": @(self.id),
        @"uri": [PjSipUtil toString:&info.acc_uri],
        @"name": self.name,
        @"username": self.username,
        @"domain": self.domain,
        @"password": self.password,
        @"proxy": self.proxy,
        @"transport": self.transport,
        @"regServer": self.regServer,
        @"regTimeout": self.regTimeout,
        @"registration": registration,
    };
}

@end
