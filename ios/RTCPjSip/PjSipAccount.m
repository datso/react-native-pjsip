#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>

#import "PjSipEndpoint.h"
#import "PjSipAccount.h"
#import "PjSipUtil.h"

@implementation PjSipAccount

+ (instancetype)itemConfig:(NSDictionary *)config {
    return [[self alloc] initWithConfig:config];
}

- (id)initWithConfig:(NSDictionary *)config {
    self = [super init];

    if (self) {
        self.name = config[@"name"] == nil ? [NSNull null] : config[@"name"];
        self.username = config[@"username"];
        self.domain = config[@"domain"];
        self.password = config[@"password"];

        self.proxy = config[@"proxy"] == nil ? [NSNull null] : config[@"proxy"];
        self.transport = config[@"transport"] == nil ? [NSNull null] : config[@"transport"];
        
        self.contactParams = config[@"contactParams"] == nil ? [NSNull null] : config[@"contactParams"];
        self.contactUriParams = config[@"contactUriParams"] == nil ? [NSNull null] : config[@"contactUriParams"];
        
        self.regServer = config[@"regServer"] == nil ? [NSNull null] : config[@"regServer"];
        self.regTimeout = config[@"regTimeout"] == nil ? [NSNumber numberWithInteger:600] : config[@"regTimeout"];
        self.regHeaders = config[@"regHeaders"] == nil ? [NSNull null] : config[@"regHeaders"];
        self.regContactParams = config[@"regContactParams"] == nil ? [NSNull null] : config[@"regContactParams"];
        self.regOnAdd = config[@"regOnAdd"] == @YES || config[@"regOnAdd"] == nil ? true : false;
        
        pj_status_t status;

        pjsua_acc_config cfg;
        pjsua_acc_config_default(&cfg);
        
        cfg.vid_in_auto_show = PJ_TRUE;
        cfg.vid_out_auto_transmit = PJ_TRUE;
        
        // General settings
        {
            NSString *cfgId;
            NSString *cfgURI = [NSString stringWithFormat:@"sip:%@", self.domain];
        
            if (![PjSipUtil isEmptyString:self.name]) {
                cfgId = [NSString stringWithFormat:@"%@ <sip:%@@%@>", self.name, self.username, self.domain];
            } else {
                cfgId = [NSString stringWithFormat:@"<sip:%@@%@>", self.username, self.domain];
            }

            cfg.id = pj_str((char *) [cfgId UTF8String]);
            cfg.reg_uri = pj_str((char *) [cfgURI UTF8String]);

            pjsip_cred_info cred;
            cred.scheme = pj_str("digest");
            cred.realm = ![PjSipUtil isEmptyString:self.regServer] ? pj_str((char *) [self.regServer UTF8String]) : pj_str("*");
            cred.username = pj_str((char *) [self.username UTF8String]);
            cred.data = pj_str((char *) [self.password UTF8String]);
            cred.data_type = PJSIP_CRED_DATA_PLAIN_PASSWD;

            cfg.cred_count = 1;
            cfg.cred_info[0] = cred;
        
            if (![PjSipUtil isEmptyString:self.contactParams]) {
                cfg.contact_params = pj_str((char *) [self.contactParams UTF8String]);
            }
            if (![PjSipUtil isEmptyString:self.contactUriParams]) {
                cfg.contact_uri_params = pj_str((char *) [self.contactUriParams UTF8String]);
            }
        }
    
        // Registration settings
        {
            if (![self.regHeaders isKindOfClass:[NSNull class]]) {
                pj_list_init(&cfg.reg_hdr_list);
            
                for(NSString* key in self.regHeaders) {
                    struct pjsip_generic_string_hdr hdr;
                    pj_str_t name = pj_str((char *) [key UTF8String]);
                    pj_str_t value = pj_str((char *) [[self.regHeaders objectForKey:key] UTF8String]);
                    pjsip_generic_string_hdr_init2(&hdr, &name, &value);
                    pj_list_push_back(&cfg.reg_hdr_list, &hdr);
                }
            }
        
            if (![PjSipUtil isEmptyString:self.regContactParams]) {
                cfg.reg_contact_params = pj_str((char *) [self.regContactParams UTF8String]);
            }
        
            if (self.regTimeout != nil && ![self.regTimeout isKindOfClass:[NSNull class]]) {
                cfg.reg_timeout = (unsigned) [self.regTimeout intValue];
            }

            cfg.register_on_acc_add = self.regOnAdd;
        }
        
        // Transport settings
        {
            if (![PjSipUtil isEmptyString:self.proxy]) {
                cfg.proxy_cnt = 1;
                cfg.proxy[0] = pj_str((char *) [[NSString stringWithFormat:@"%@", self.proxy] UTF8String]);
            }

            cfg.transport_id = [[PjSipEndpoint instance] tcpTransportId];
        
            if (![PjSipUtil isEmptyString:self.transport] && ![self.transport isEqualToString:@"TCP"]) {
                if ([self.transport isEqualToString:@"UDP"]) {
                    cfg.transport_id = [[PjSipEndpoint instance] udpTransportId];
                } else if ([self.transport isEqualToString:@"TLS"]) {
                    cfg.transport_id = [[PjSipEndpoint instance] tlsTransportId];
                } else {
                    NSLog(@"Illegal \"%@\" transport (possible values are UDP, TCP or TLS) use TCP instead", self.transport);
                }
            }
        }
        
        pjsua_acc_id account_id;

        status = pjsua_acc_add(&cfg, PJ_TRUE, &account_id);
        if (status != PJ_SUCCESS) {
            [NSException raise:@"Failed to create account" format:@"See device logs for more details."];
        }

        self.id = account_id;
    }

    return self;
}

- (void) dealloc {
    pjsua_acc_set_registration(self.id, PJ_FALSE);
    pjsua_acc_del(self.id);
}

- (void) register: (bool) renew {
    pj_status_t status = pjsua_acc_set_registration((pjsua_acc_id)self.id, renew ? PJ_TRUE : PJ_FALSE);
    if (status != PJ_SUCCESS) {
        [NSException raise:@"Failed to register account" format:@"See device logs for more details."];
    }
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
    
    return @{
        @"id": @(self.id),
        @"uri": [PjSipUtil toString:&info.acc_uri],
        @"name": self.name,
        @"username": self.username,
        @"domain": self.domain,
        @"password": self.password,
        @"proxy": self.proxy,
        @"transport": self.transport,
        @"contactParams": self.contactParams,
        @"contactUriParams": self.contactUriParams,
        @"regServer": self.regServer,
        @"regTimeout": self.regTimeout,
        @"regContactParams": self.regContactParams,
        @"regHeaders": self.regHeaders,
        
        @"registration": registration
    };
}

@end
