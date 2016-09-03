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

@interface PjSipEndpoint : NSObject
@property NSMutableDictionary* accounts;
@property NSMutableDictionary* calls;

+(instancetype)instance;
-(NSDictionary *)start;

-(PjSipAccount *)createAccount:(NSDictionary*)config;
// -(void)deleteAccount:(NSDictionary*)config;

// -(void)makeCall:(NSDictionary*)config;
// -(void)hangupCall:(NSDictionary*)config;

@end
