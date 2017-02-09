#import <React/RCTUtils.h>
#import "pjsua.h"

@interface PjSipUtil : NSObject

+(NSString *) toString: (pj_str_t *) pjStr;

@end
