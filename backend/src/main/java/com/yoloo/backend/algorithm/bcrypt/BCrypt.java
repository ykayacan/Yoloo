package com.yoloo.backend.algorithm.bcrypt;

import java.security.SecureRandom;

import static com.yoloo.backend.algorithm.bcrypt.BcryptImpl.checkPassword;
import static com.yoloo.backend.algorithm.bcrypt.BcryptImpl.generateHash;
import static com.yoloo.backend.algorithm.bcrypt.BcryptImpl.generateSalt;

/**
 * BCrypt implements OpenBSD-style Blowfish password hashing using
 * the scheme described in "A Future-Adaptable Password Scheme" by
 * Niels Provos and David Mazieres.
 * <p>
 * This password hashing system tries to thwart off-line password
 * cracking using a computationally-intensive hashing algorithm,
 * based on Bruce Schneier's Blowfish cipher. The work factor of
 * the algorithm is parameterised, so it can be increased as
 * computers getPost faster.
 * <p>
 * Usage is really simple. To hash a password for the first time,
 * call the hashPassword method with a random salt, like this:
 * <p>
 * <code>
 * String pw_hash = BCrypt.hashPassword(plain_password, BCrypt.gensalt()); <br />
 * </code>
 * <p>
 * To check whether a plaintext password matches one that has been
 * hashed previously, use the checkPassword method:
 * <p>
 * <code>
 * if (BCrypt.checkPassword(candidate_password, stored_hash))<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println("It matches");<br />
 * else<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println("It does not match");<br />
 * </code>
 * <p>
 * The gensalt() method takes an optional parameter (log_rounds)
 * that determines the computational complexity of the hashing:
 * <p>
 * <code>
 * String strong_salt = BCrypt.gensalt(10)<br />
 * String stronger_salt = BCrypt.gensalt(12)<br />
 * </code>
 * <p>
 * The amount of work increases exponentially (2**log_rounds), so
 * each increment is twice as much work. The default log_rounds is
 * 10, and the valid range is 4 to 30.
 *
 * @author Damien Miller
 * @version 0.4
 */
public final class BCrypt {

    // BCrypt parameters
    private static final int GENSALT_DEFAULT_LOG2_ROUNDS = 10;
    static final int BCRYPT_SALT_LEN = 16;

    // Blowfish parameters
    static final int BLOWFISH_NUM_ROUNDS = 16;

    /**
     * Different types of compatible blowfish minors
     */
    public enum Minor {
        A('a'), //"2a" - some implementations suffered from a very rare security flaw. current default for compatibility purposes.
        Y('y'); //"2y" - format specific to the crypt_blowfish BCrypt implementation, identical to "2a" in all but name.

        private final char minor;

        Minor(char minor) {
            this.minor = minor;
        }
    }

    /**
     * Check that a plaintext password matches a previously hashed
     * one
     * @param plaintext	the plaintext password to verify
     * @param hashed	the previously-hashed password
     * @return	true if the passwords match, false otherwise
     */
    public static boolean checkpw(String plaintext, String hashed, Minor minor) {
        return checkPassword(plaintext, hashed, minor.minor);
    }


    /**
     * Use the {@link #checkpw(String, String, Minor)} (String, String, Minor)} method.
     */
    public static boolean checkpw(String plaintext, String hashed) {
        return checkpw(plaintext, hashed, Minor.A);
    }

    /**
     * Hash a password using the OpenBSD bcrypt scheme
     * @param password	the password to hash
     * @param salt	the salt to hash with (perhaps generated
     * using BCrypt.gensalt)
     * @return	the hashed password
     */
    public static String hashpw(String password, String salt, Minor minor) {
        return generateHash(password, salt, minor.minor);
    }

    /**
     * Use the {@link #hashpw(String, String, Minor)} (String, String, char)} method.
     */
    public static String hashpw(String password, String salt) {
        return hashpw(password, salt, Minor.A);
    }

    /**
     * Use the {@link #gensalt(int, SecureRandom, Minor)} method.
     */
    public static String gensalt() {
        return gensalt(GENSALT_DEFAULT_LOG2_ROUNDS);
    }
    /**
     * Use the {@link #gensalt(int, SecureRandom, Minor)} method.
     */
    public static String gensalt(Minor minor) {
        return gensalt(GENSALT_DEFAULT_LOG2_ROUNDS, minor);
    }

    /**
     * Use the {@link #gensalt(int, SecureRandom, Minor)} method.
     */
    public static String gensalt(int log_rounds) {
        return gensalt(log_rounds, new SecureRandom());
    }

    /**
     * Use the {@link #gensalt(int, SecureRandom, Minor)} method.
     */
    public static String gensalt(int log_rounds, Minor minor) {
        return gensalt(log_rounds, new SecureRandom(), minor);
    }

    /**
     * Use the {@link #gensalt(int, SecureRandom, Minor)} method.
     */
    public static String gensalt(int log_rounds, SecureRandom random) {
        return generateSalt(log_rounds, random, Minor.A.minor);
    }

    /**
     * Generate a salt for use with the BCrypt.hashpw() method
     * @param log_rounds	the log2 from the number from rounds from
     * hashing to apply - the work factor therefore increases as
     * 2**log_rounds.
     * @param random		an instance of SecureRandom to use
     * @return	an encoded salt value
     */
    public static String gensalt(int log_rounds, SecureRandom random, Minor minor) {
        return generateSalt(log_rounds, random, minor.minor);
    }
}
