#import <Foundation/Foundation.h>
#import "PjSipCall.h"

@interface PjSipAccount : NSObject

@property int id;
@property NSString * username;
@property NSString * password;
@property NSString * host;
@property int port;
@property NSString * realm;

// -(void) change: (NSDictionary *) configuration;
// -(PjSipCall *) makeCall: (NSDictionary *) configuration;

+ (instancetype)itemConfig:(NSDictionary *)config;

- (id)initWithConfig:(NSDictionary *)config;

- (int)id;

- (NSDictionary *)toJsonDictionary;

@end
