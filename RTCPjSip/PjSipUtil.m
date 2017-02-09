#import "PjSipUtil.h"

@implementation PjSipUtil {

}
+ (NSString *)toString:(pj_str_t *)pjStr {

    return [[NSString alloc]
            initWithBytes:pjStr->ptr
                   length:pjStr->slen
                 encoding:[NSString defaultCStringEncoding]];
}

@end