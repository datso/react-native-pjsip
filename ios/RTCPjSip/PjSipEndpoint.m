#import "PjSipEndpoint.h"
#import "pjsua.h"

@implementation PjSipEndpoint

+ (instancetype)instance {
    static PjSipEndpoint *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[PjSipEndpoint alloc] init];
    });

    return sharedInstance;
}

- (instancetype)init {
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

- (NSDictionary *)start {
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

    NSLog(@"create account and put it inside account dictionary (%@)", self.accounts);

    return account;
}


@end
