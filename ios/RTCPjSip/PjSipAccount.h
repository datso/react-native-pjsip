#import <React/RCTUtils.h>

#import "PjSipCall.h"

@interface PjSipAccount : NSObject

@property int id;
@property NSString * name;
@property NSString * username;
@property NSString * domain;
@property NSString * password;
@property NSString * proxy;
@property NSString * transport;

@property NSString * contactParams;
@property NSString * contactUriParams;


@property NSString * regServer;
@property NSNumber * regTimeout;
@property NSDictionary * regHeaders;
@property NSString * regContactParams;
@property bool regOnAdd;

+ (instancetype)itemConfig:(NSDictionary *)config;

- (id)initWithConfig:(NSDictionary *)config;
- (int)id;

- (PjSipCall *) makeCall: (NSString *) destination;
- (void) register: (bool) renew;

- (NSDictionary *)toJsonDictionary;

@end
