package it.pagopa.gov.rtdmsexporter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;


public final class PerformanceUtils {

  private static Logger log = LoggerFactory.getLogger("Performance");
  private static boolean isEnabled;

  private PerformanceUtils() {}

  public static void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public static <T> T timeIt(String logPrefix, Supplier<T> supplier) {
    if (isEnabled) {
      final var start = System.currentTimeMillis();
      final var it = supplier.get();
      log.info("{} - time {}ms", logPrefix, (System.currentTimeMillis() - start));
      return it;
    } else {
      return supplier.get();
    }
  }
}
