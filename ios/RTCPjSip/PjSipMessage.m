#import "PjSipMessage.h"

@implementation PjSipMessage

+ (instancetype)itemConfig:(NSDictionary *)config {
    return [[self alloc] initWithConfig:config];
}

- (id)initWithConfig:(NSDictionary *)config {
    self = [super init];
    
    if (self) {
        self.data = config;
    }
    
    return self;
}

- (NSDictionary *)toJsonDictionary {
    return self.data;
}

@end
