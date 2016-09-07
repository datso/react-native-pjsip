#import <Foundation/Foundation.h>
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

// -(void) change: (NSDictionary *) configuration;
// -(PjSipCall *) makeCall: (NSDictionary *) configuration;

+ (instancetype)itemConfig:(NSDictionary *)config;

- (id)initWithConfig:(NSDictionary *)config;

- (int)id;

- (NSDictionary *)toJsonDictionary;

- (void)onRegistrationChanged;
@end
