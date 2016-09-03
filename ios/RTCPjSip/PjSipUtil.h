//
// Created by Vadim Ruban on 9/3/16.
// Copyright (c) 2016 Vadim Ruban. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "pjsua.h"

@interface PjSipUtil : NSObject

+(NSString *) toString: (pj_str_t *) pjStr;

@end