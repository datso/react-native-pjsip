#import "PjSipAccount.h"
#import "pjsua.h"
#import "PjSipUtil.h"

@implementation PjSipAccount

+ (instancetype)itemConfig:(NSDictionary *)config {
    return [[self alloc] initWithConfig:config];
}

- (id)initWithConfig:(NSDictionary *)config {
    self = [super init];

    // TODO: Fire registration_changed
    // TODO: Fire call_received

    if (self) {
        self.username = config[@"username"];
        self.password = config[@"password"];
        self.host = config[@"host"];
        // self.port = config[@"port"]; // TODO: Support port in URI
        self.realm = config[@"realm"];

        NSString *cfgId = [NSString stringWithFormat:@"sip:%@@%@", self.username, self.realm];
        NSString *cfgURI = [NSString stringWithFormat:@"sip:%@", self.host];

        pjsua_acc_config cfg;
        pjsua_acc_config_default(&cfg);

        cfg.id = pj_str((char *) [cfgId UTF8String]);
        cfg.reg_uri = pj_str((char *) [cfgURI UTF8String]);

        pjsip_cred_info cred;
        cred.scheme = pj_str((char *) [@"digest" UTF8String]);
        cred.realm = pj_str((char *) [self.realm UTF8String]);
        cred.username = pj_str((char *) [self.username UTF8String]);
        cred.data = pj_str((char *) [self.password UTF8String]);
        cred.data_type = PJSIP_CRED_DATA_PLAIN_PASSWD;

        cfg.cred_count = 1;
        cfg.cred_info[0] = cred;

        // cfg.transport_id = [self initTransport]; // NSString *transport = config[@"transport"];
        NSLog(@"cfg.transport_id %d", cfg.transport_id);

        pj_status_t status;
        pjsua_acc_id account_id;

        status = pjsua_acc_add(&cfg, PJ_TRUE, &account_id);
        if (status != PJ_SUCCESS) {
            NSLog(@"Error adding account");
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
        @"username": self.username,
        @"password": self.password,
        @"host": self.host,
        @"port": @(self.port),
        @"realm": self.realm,
        @"registration": registration,
    };
}


@end
