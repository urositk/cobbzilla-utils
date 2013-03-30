package org.cobbzilla.util.security.bcrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class BCryptUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BCryptUtil.class);

    private static final SecureRandom random = new SecureRandom();

    private static volatile Integer bcryptRounds = null;

    public synchronized static void setBcryptRounds(int rounds) {
        if (bcryptRounds != null) {
            LOG.warn("Cannot change bcryptRounds after initialization");
            return;
        }
        bcryptRounds = rounds < 4 ? 4 : rounds; // 4 is minimum bcrypt rounds
        LOG.info("setBcryptRounds: initialized with "+bcryptRounds+" rounds (param was "+rounds+")");
    }

    public static Integer getBcryptRounds() { return bcryptRounds; }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(getBcryptRounds(), random));
    }
}
