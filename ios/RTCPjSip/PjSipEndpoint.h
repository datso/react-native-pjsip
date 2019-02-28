#import <React/RCTBridgeModule.h>

#import "PjSipAccount.h"
#import "PjSipCall.h"


@interface PjSipEndpoint : NSObject

@property NSMutableDictionary* accounts;
@property NSMutableDictionary* calls;
@property (nonatomic, strong) RCTBridge *bridge;
@property (nonatomic, strong) NSArray *stunServerList;
@property (nonatomic, strong) NSDictionary *turnServerConfig;

@property pjsua_transport_id tcpTransportId;
@property pjsua_transport_id udpTransportId;
@property pjsua_transport_id tlsTransportId;

@property bool isSpeaker;

+(instancetype)instance;

-(NSDictionary *)start: (NSDictionary *) config;

-(void) updateStunServers: (int) accountId stunServerList:(NSArray *)stunServerList;

/**
 * @brief TURN configuration
 * @discussion This method accepts a key-value object with TURN server configuration.
 *
 * Usage:
 * <b>server</b> - Specify TURN domain name or host name in "DOMAIN:PORT" or "HOST:PORT" format.
 * value type: string
 *
 * <b>tp_type</b> - TURN transport type.
 * value type: string
 * value: "TP_UDP" or "TP_TCP".
 *
 * <b>realm</b> - If not-empty, it indicates that this is a long term credential.
 * value type: string
 *
 * <b>username</b> - The username of the credential.
 * value type: string
 *
 * <b>password_data_type</b> - Data type to indicate the type of password in the \a data field.
 * value type: string
 * value: "PLAIN" or "HASHED".
 *
 * <b>data</b> - The data, which depends depends on the value of \a data_type
 *      field. When \a data_type is zero, this field will contain the
 *      plaintext password.
 */
-(void) updateTurnSever: (int) accountId configuration: (NSDictionary*) turnConfiguration;

-(PjSipAccount *)createAccount:(NSDictionary*) config;
-(void) deleteAccount:(int) accountId;
-(PjSipAccount *)findAccount:(int)accountId;
-(PjSipCall *)makeCall:(PjSipAccount *) account destination:(NSString *)destination callSettings: (NSDictionary *)callSettings msgData: (NSDictionary *)msgData;
-(void)pauseParallelCalls:(PjSipCall*) call; // TODO: Remove this feature.
-(PjSipCall *)findCall:(int)callId;
-(void)useSpeaker;
-(void)useEarpiece;

-(void)changeOrientation: (NSString*) orientation;
-(void)changeCodecSettings: (NSDictionary*) codecSettings;

-(void)emmitRegistrationChanged:(PjSipAccount*) account;
-(void)emmitCallReceived:(PjSipCall*) call;
-(void)emmitCallUpdated:(PjSipCall*) call;
-(void)emmitCallChanged:(PjSipCall*) call;
-(void)emmitCallTerminated:(PjSipCall*) call;

@end
