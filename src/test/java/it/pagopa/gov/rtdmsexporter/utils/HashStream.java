package it.pagopa.gov.rtdmsexporter.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HashStream {
  private final static MessageDigest digest = DigestUtils.getSha256Digest();

  public static Stream<String> of(int size) {
    return of(size, 0);
  }

  public static Stream<String> of(int size, int seed) {
    return IntStream.range(0, size)
            .boxed()
            .map(it -> Hex.encodeHexString(digest.digest((it.toString() + seed).getBytes())));
  }

  public static Stream<String> of(int size, String seed) {
    return IntStream.range(0, size)
            .boxed()
            .map(it -> Hex.encodeHexString(digest.digest((it + seed).getBytes())));
  }
}
