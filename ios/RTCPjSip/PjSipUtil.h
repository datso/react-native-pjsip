#import <React/RCTUtils.h>
#import <VialerPJSIP/pjsua.h>

@interface PjSipUtil : NSObject

+(NSString *) toString: (pj_str_t *) str;
+(BOOL) isEmptyString : (NSString *) str;

+(NSString *) callStateToString: (pjsip_inv_state) state;
+(NSString *) callStatusToString: (pjsip_status_code) status;

@end
