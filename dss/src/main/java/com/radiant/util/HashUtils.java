package com.radiant.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashUtils {
   private static final Logger LOG = LoggerFactory.getLogger(HashUtils.class);

   public static String digestMessage(String input) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
         return HexFormat.of().formatHex(encodedhash);
      } catch (NoSuchAlgorithmException var3) {
         LOG.error("Failed to initialize hasher, unknown algorithm");
         throw new IllegalStateException("Unknown hash algorithm");
      }
   }

   public static String hmacEncode(String algorithm, String key, String data) {
      try {
         Mac mac = Mac.getInstance(algorithm);
         SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
         mac.init(secretKeySpec);
         return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
      } catch (InvalidKeyException | NoSuchAlgorithmException e) {
         LOG.error("Failed to init hmac", e);
         throw new IllegalStateException("Unknown hmac algorithm");
      }
   }

}
