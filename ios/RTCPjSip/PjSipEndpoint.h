//
//  PjSipEndpoint.h
//  CarustoConnect
//
//  Created by Vadim Ruban on 3/25/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PjSipAccount.h"
#import "PjSipCall.h"
#import "RCTBridgeModule.h"

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
