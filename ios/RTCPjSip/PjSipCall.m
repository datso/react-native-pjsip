#import "PjSipCall.h"

@implementation PjSipCall






- (NSDictionary *)toJsonDictionary {


    // Retrieve call info

//    pjsua_call *call = NULL;
//    pj_json_elem *data_json = NULL;
//    struct pjsua_data *pjsua_var = pjsua_get_var();
//
//    if (id < 0) return data_json;
//
//    PJSUA_LOCK();
//    call = &pjsua_var->calls[id];
//    if (call->inv != NULL && call->async_call.dlg != NULL) {
//        pjsua_call_info call_info;
//        pjsua_call_get_info(id, &call_info);
//        data_json = crst_get_call_info_to_json(pool, &call_info);
//    }
//    PJSUA_UNLOCK();




    // Format response

//    if (!call)
//        return NULL;
//
//    char *call_id = NULL, *status = NULL;
//
//    pj_json_elem *root = json_add_element_with_name(pool, NULL, "data");
//    if (!root) return NULL;
//
//    call_id = pj_pool_alloc(pool, call->call_id.slen + 1);
//    status = pj_pool_alloc(pool, call->state_text.slen + 1);
//
//    snprintf(call_id, call->call_id.slen + 1, "%s", call->call_id.ptr);
//    snprintf(status, call->state_text.slen + 1, "%s", call->state_text.ptr);
//
//    json_add_string_element(pool, root, "id", call_id);
//    json_add_string_element(pool, root, "status", status);
//    json_add_string_element(pool, root, "direction", call->role == PJSIP_ROLE_UAC ? "OUTGOING" : "INCOMING");
//    json_add_number_element(pool, root, "duration", call->connect_duration.sec);
////	json_add_number_element(pool, root, "quality", -1);
//    char *name = NULL;
//    char *number = NULL;
//    if (call->remote_pai[0] != '\0') {
//        name = crst_strdup(pool, (char *)call->remote_pai);
//        number = crst_strdup(pool, (char *)call->remote_pai);
//    } else if (call->remote_info.slen > 0)
//    {
//        name = crst_strndup(pool, call->remote_info.ptr, call->remote_info.slen);
//        number = crst_strndup(pool, call->remote_info.ptr, call->remote_info.slen);
//    }
//    if (name) {
//        char *foo = strchr(name, '"');
//        if (foo) {
//            name = ++foo;
//            foo = strchr(foo, '"');
//            if (foo) {
//                *foo = '\0';
//                foo++;
//                number = foo;
//            }
//        }
//        if (name == strstr(name, "sip")) name = NULL;
//    }
//    if (number) {
//        char *foo = strchr(number, ':');
//        if (foo) {
//            number = ++foo;
//        }
//        if (number) {
//            foo = strchr(number, '@');
//            if (foo) {
//                *foo = '\0';
//            }
//        }
//    }
//    if (name) {
//        json_add_string_element(pool, root, "callee_name", name);
//    }
//    if (number) {
//        json_add_string_element(pool, root, "callee_number", number);
//    }
//    return root;


















//    json.put("id", getId());
//    json.put("callId", getInfo().getCallIdString());
//    json.put("accountId", account.getId());
//
//    // -----
//    json.put("localContact", getInfo().getLocalContact());
//    json.put("localUri", getInfo().getLocalUri());
//    json.put("remoteContact", getInfo().getRemoteContact());
//    json.put("remoteUri", getInfo().getRemoteUri());
//
//    // -----
//    json.put("state", getInfo().getState());
//    json.put("stateText", getInfo().getStateText());
//    json.put("connectDuration", getInfo().getConnectDuration().getSec());
//    json.put("totalDuration", getInfo().getTotalDuration().getSec());
//
//    // -----
//    info.put("lastStatusCode", getInfo().getLastStatusCode());
//    info.put("lastReason", getInfo().getLastReason());
//
//    // -----
//    json.put("remoteOfferer", getInfo().getRemOfferer());
//    json.put("remoteAudioCount", getInfo().getRemAudioCount());
//    json.put("remoteVideoCount", getInfo().getRemVideoCount());
//
//    // -----
//    json.put("audioCount", getInfo().getSetting().getAudioCount());
//    json.put("videoCount", getInfo().getSetting().getVideoCount());


    return nil;
}


@end
