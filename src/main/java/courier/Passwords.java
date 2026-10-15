package courier;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class Passwords {
  private static final int ITERATIONS = 210000;
  private static final int KEY_LENGTH = 256;

  private Passwords() {}

  static String hash(char[] password) throws Exception {
    if (password == null) {
      throw new IllegalArgumentException("Password cannot be null");
    }
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return ITERATIONS
        + ":"
        + Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  static byte[] derive(char[] password, byte[] salt, int rounds) throws Exception {
    if (password == null) {
      throw new IllegalArgumentException("Password cannot be null");
    }
    PBEKeySpec spec = new PBEKeySpec(password, salt, rounds, KEY_LENGTH);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } finally {
      spec.clearPassword();
    }
  }

  static boolean verify(char[] password, String stored) throws Exception {
    if (password == null || stored == null) {
      return false;
    }
    String[] v = stored.split(":");
    if (v.length != 3) {
      return false;
    }
    try {
      int rounds = Integer.parseInt(v[0]);
      byte[] salt = Base64.getDecoder().decode(v[1]);
      byte[] expectedHash = Base64.getDecoder().decode(v[2]);
      byte[] actualHash = derive(password, salt, rounds);
      return MessageDigest.isEqual(expectedHash, actualHash);
    } catch (Exception e) {
      return false;
    }
  }
}
