#import <React/RCTBridgeModule.h>

#import "PjSipAccount.h"
#import "PjSipCall.h"


@interface PjSipEndpoint : NSObject
@property NSMutableDictionary* accounts;
@property NSMutableDictionary* calls;
@property(nonatomic, strong) RCTBridge *bridge;

+(instancetype)instance;
-(NSDictionary *)start;

-(PjSipAccount *)createAccount:(NSDictionary*) config;
-(void) deleteAccount:(int) accountId;
- (PjSipAccount *)findAccount:(int)accountId;

// -(void)makeCall:(NSDictionary*)config;
// -(void)hangupCall:(NSDictionary*)config;


@end
