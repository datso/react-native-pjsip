#import <React/RCTUtils.h>
#import <VialerPJSIP/pjsua.h>

@interface PjSipUtil : NSObject

+(NSString *) toString: (pj_str_t *) str;
+(BOOL) isEmptyString : (NSString *) str;

+(NSString *) callStateToString: (pjsip_inv_state) state;
+(NSString *) callStatusToString: (pjsip_status_code) status;
+(NSString *) mediaDirToString: (pjmedia_dir) dir;
+(NSString *) mediaStatusToString: (pjsua_call_media_status) status;
+(NSString *) mediaTypeToString: (pjmedia_type) type;

+(void) fillCallSettings: (pjsua_call_setting*) callSettings dict:(NSDictionary*) dict;
+(void) fillMsgData: (pjsua_msg_data*) msgData dict:(NSDictionary*) dict pool:(pj_pool_t*) pool;

@end
