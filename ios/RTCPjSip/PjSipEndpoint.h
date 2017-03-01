#import <React/RCTBridgeModule.h>

#import "PjSipAccount.h"
#import "PjSipCall.h"


@interface PjSipEndpoint : NSObject

@property NSMutableDictionary* accounts;
@property NSMutableDictionary* calls;
@property(nonatomic, strong) RCTBridge *bridge;
@property bool isSpeaker;

+(instancetype)instance;

-(NSDictionary *)start;

-(PjSipAccount *)createAccount:(NSDictionary*) config;
-(void) deleteAccount:(int) accountId;
-(PjSipAccount *)findAccount:(int)accountId;
-(void)emmitRegistrationChanged:(PjSipAccount*) account;

-(PjSipCall *)makeCall:(PjSipAccount *) account destination:(NSString *)destination;
-(PjSipCall *)findCall:(int)callId;
-(void)useSpeaker;
-(void)useEarpiece;
-(void)emmitCallReceived:(PjSipCall*) call;
-(void)emmitCallUpdated:(PjSipCall*) call;
-(void)emmitCallChanged:(PjSipCall*) call;
-(void)emmitCallTerminated:(PjSipCall*) call;

@end
