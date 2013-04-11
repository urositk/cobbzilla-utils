package org.cobbzilla.util.security.bcrypt;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class BCryptUtil {

    private static final SecureRandom random = new SecureRandom();

    private static volatile Integer bcryptRounds = null;

    public synchronized static void setBcryptRounds(int rounds) {
        if (bcryptRounds != null) {
            log.warn("Cannot change bcryptRounds after initialization");
            return;
        }
        bcryptRounds = rounds < 4 ? 4 : rounds; // 4 is minimum bcrypt rounds
        log.info("setBcryptRounds: initialized with "+bcryptRounds+" rounds (param was "+rounds+")");
    }

    public static Integer getBcryptRounds() { return bcryptRounds; }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(getBcryptRounds(), random));
    }
}
