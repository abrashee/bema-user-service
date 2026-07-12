package com.bema.bema_user_service.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserAuditLogger {

    private static final Logger log =
            LoggerFactory.getLogger(UserAuditLogger.class);

    public void success(
            String event,
            String actorId,
            Long targetUserId,
            String targetIdentityId
    ) {
        log.info(
                "security_audit event={} outcome=SUCCESS actorId={} "
                        + "targetUserId={} targetIdentityId={}",
                event,
                safe(actorId),
                targetUserId,
                safe(targetIdentityId)
        );
    }

    public void denied(
            String event,
            String actorId,
            String targetIdentityId,
            String reason
    ) {
        log.warn(
                "security_audit event={} outcome=DENIED actorId={} "
                        + "targetIdentityId={} reason={}",
                event,
                safe(actorId),
                safe(targetIdentityId),
                reason
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "anonymous"
                : value;
    }
}
