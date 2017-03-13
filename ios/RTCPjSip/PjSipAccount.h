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
@property NSString * regServer;
@property NSNumber * regTimeout;
@property NSDictionary * regHeaders;
@property NSString * regContactParams;

+ (instancetype)itemConfig:(NSDictionary *)config;

- (id)initWithConfig:(NSDictionary *)config;
- (int)id;

- (PjSipCall *)makeCall:(NSString *)destination;

- (NSDictionary *)toJsonDictionary;

@end
