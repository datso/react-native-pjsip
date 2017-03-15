#import "PjSipUtil.h"

@implementation PjSipUtil {

}
+ (NSString *) toString : (pj_str_t *)pjStr {
    if (pjStr->slen < 0) {
        return [NSNull null];
    }
    
    return [[NSString alloc]
            initWithBytes:pjStr->ptr
                   length:pjStr->slen
                 encoding:NSUTF8StringEncoding];
}

+(BOOL) isEmptyString : (NSString *)string
{
    if([string isKindOfClass:[NSNull class]] || [string length] == 0 ||
       [string isEqualToString:@""]||[string  isEqualToString:NULL]  ||
       string == nil)
    {
        return YES;         //IF String Is An Empty String
    }
    return NO;
}

+(NSString *) callStateToString: (pjsip_inv_state) state {
    switch (state) {
        case PJSIP_INV_STATE_CALLING:
            return @"PJSIP_INV_STATE_CALLING";
        case PJSIP_INV_STATE_INCOMING:
            return @"PJSIP_INV_STATE_INCOMING";
        case PJSIP_INV_STATE_EARLY:
            return @"PJSIP_INV_STATE_EARLY";
        case PJSIP_INV_STATE_CONNECTING:
            return @"PJSIP_INV_STATE_CONNECTING";
        case PJSIP_INV_STATE_CONFIRMED:
            return @"PJSIP_INV_STATE_CONFIRMED";
        case PJSIP_INV_STATE_DISCONNECTED:
            return @"PJSIP_INV_STATE_DISCONNECTED";
        default:
            return @"PJSIP_INV_STATE_NULL";
    }
}

+(NSString *) callStatusToString: (pjsip_status_code) status {
    switch (status) {
        case PJSIP_SC_TRYING:
            return @"PJSIP_SC_TRYING";
        case PJSIP_SC_RINGING:
            return @"PJSIP_SC_RINGING";
        case PJSIP_SC_CALL_BEING_FORWARDED:
            return @"PJSIP_SC_CALL_BEING_FORWARDED";
        case PJSIP_SC_QUEUED:
            return @"PJSIP_SC_QUEUED";
        case PJSIP_SC_PROGRESS:
            return @"PJSIP_SC_PROGRESS";
        case PJSIP_SC_OK:
            return @"PJSIP_SC_OK";
        case PJSIP_SC_ACCEPTED:
            return @"PJSIP_SC_ACCEPTED";
        case PJSIP_SC_MULTIPLE_CHOICES:
            return @"PJSIP_SC_MULTIPLE_CHOICES";
        case PJSIP_SC_MOVED_PERMANENTLY:
            return @"PJSIP_SC_MOVED_PERMANENTLY";
        case PJSIP_SC_MOVED_TEMPORARILY:
            return @"PJSIP_SC_MOVED_TEMPORARILY";
        case PJSIP_SC_USE_PROXY:
            return @"PJSIP_SC_USE_PROXY";
        case PJSIP_SC_ALTERNATIVE_SERVICE:
            return @"PJSIP_SC_ALTERNATIVE_SERVICE";
        case PJSIP_SC_BAD_REQUEST:
            return @"PJSIP_SC_BAD_REQUEST";
        case PJSIP_SC_UNAUTHORIZED:
            return @"PJSIP_SC_UNAUTHORIZED";
        case PJSIP_SC_PAYMENT_REQUIRED:
            return @"PJSIP_SC_PAYMENT_REQUIRED";
        case PJSIP_SC_FORBIDDEN:
            return @"PJSIP_SC_FORBIDDEN";
        case PJSIP_SC_NOT_FOUND:
            return @"PJSIP_SC_NOT_FOUND";
        case PJSIP_SC_METHOD_NOT_ALLOWED:
            return @"PJSIP_SC_METHOD_NOT_ALLOWED";
        case PJSIP_SC_NOT_ACCEPTABLE:
            return @"PJSIP_SC_NOT_ACCEPTABLE";
        case PJSIP_SC_PROXY_AUTHENTICATION_REQUIRED:
            return @"PJSIP_SC_PROXY_AUTHENTICATION_REQUIRED";
        case PJSIP_SC_REQUEST_TIMEOUT:
            return @"PJSIP_SC_REQUEST_TIMEOUT";
        case PJSIP_SC_GONE:
            return @"PJSIP_SC_GONE";
        case PJSIP_SC_REQUEST_ENTITY_TOO_LARGE:
            return @"PJSIP_SC_REQUEST_ENTITY_TOO_LARGE";
        case PJSIP_SC_REQUEST_URI_TOO_LONG:
            return @"PJSIP_SC_REQUEST_URI_TOO_LONG";
        case PJSIP_SC_UNSUPPORTED_MEDIA_TYPE:
            return @"PJSIP_SC_UNSUPPORTED_MEDIA_TYPE";
        case PJSIP_SC_UNSUPPORTED_URI_SCHEME:
            return @"PJSIP_SC_UNSUPPORTED_URI_SCHEME";
        case PJSIP_SC_BAD_EXTENSION:
            return @"PJSIP_SC_BAD_EXTENSION";
        case PJSIP_SC_EXTENSION_REQUIRED:
            return @"PJSIP_SC_EXTENSION_REQUIRED";
        case PJSIP_SC_SESSION_TIMER_TOO_SMALL:
            return @"PJSIP_SC_SESSION_TIMER_TOO_SMALL";
        case PJSIP_SC_INTERVAL_TOO_BRIEF:
            return @"PJSIP_SC_INTERVAL_TOO_BRIEF";
        case PJSIP_SC_TEMPORARILY_UNAVAILABLE:
            return @"PJSIP_SC_TEMPORARILY_UNAVAILABLE";
        case PJSIP_SC_CALL_TSX_DOES_NOT_EXIST:
            return @"PJSIP_SC_CALL_TSX_DOES_NOT_EXIST";
        case PJSIP_SC_LOOP_DETECTED:
            return @"PJSIP_SC_LOOP_DETECTED";
        case PJSIP_SC_TOO_MANY_HOPS:
            return @"PJSIP_SC_TOO_MANY_HOPS";
        case PJSIP_SC_ADDRESS_INCOMPLETE:
            return @"PJSIP_SC_ADDRESS_INCOMPLETE";
        case PJSIP_AC_AMBIGUOUS:
            return @"PJSIP_AC_AMBIGUOUS";
        case PJSIP_SC_BUSY_HERE:
            return @"PJSIP_SC_BUSY_HERE";
        case PJSIP_SC_REQUEST_TERMINATED:
            return @"PJSIP_SC_REQUEST_TERMINATED";
        case PJSIP_SC_NOT_ACCEPTABLE_HERE:
            return @"PJSIP_SC_NOT_ACCEPTABLE_HERE";
        case PJSIP_SC_BAD_EVENT:
            return @"PJSIP_SC_BAD_EVENT";
        case PJSIP_SC_REQUEST_UPDATED:
            return @"PJSIP_SC_REQUEST_UPDATED";
        case PJSIP_SC_REQUEST_PENDING:
            return @"PJSIP_SC_REQUEST_PENDING";
        case PJSIP_SC_UNDECIPHERABLE:
            return @"PJSIP_SC_UNDECIPHERABLE";
        case PJSIP_SC_INTERNAL_SERVER_ERROR:
            return @"PJSIP_SC_INTERNAL_SERVER_ERROR";
        case PJSIP_SC_NOT_IMPLEMENTED:
            return @"PJSIP_SC_NOT_IMPLEMENTED";
        case PJSIP_SC_BAD_GATEWAY:
            return @"PJSIP_SC_BAD_GATEWAY";
        case PJSIP_SC_SERVICE_UNAVAILABLE:
            return @"PJSIP_SC_SERVICE_UNAVAILABLE";
        case PJSIP_SC_SERVER_TIMEOUT:
            return @"PJSIP_SC_SERVER_TIMEOUT";
        case PJSIP_SC_VERSION_NOT_SUPPORTED:
            return @"PJSIP_SC_VERSION_NOT_SUPPORTED";
        case PJSIP_SC_MESSAGE_TOO_LARGE:
            return @"PJSIP_SC_MESSAGE_TOO_LARGE";
        case PJSIP_SC_PRECONDITION_FAILURE:
            return @"PJSIP_SC_PRECONDITION_FAILURE";
        case PJSIP_SC_BUSY_EVERYWHERE:
            return @"PJSIP_SC_BUSY_EVERYWHERE";
        case PJSIP_SC_DECLINE:
            return @"PJSIP_SC_DECLINE";
        case PJSIP_SC_DOES_NOT_EXIST_ANYWHERE:
            return @"PJSIP_SC_DOES_NOT_EXIST_ANYWHERE";
        case PJSIP_SC_NOT_ACCEPTABLE_ANYWHERE:
            return @"PJSIP_SC_NOT_ACCEPTABLE_ANYWHERE";
        default:
            return [NSNull null];
    }
}

@end
