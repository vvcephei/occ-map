package org.vvcephei.occ_map;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Simple class to obtain access to the {@link Unsafe} object.  {@link Unsafe}
 * is required to allow efficient CAS operations on arrays.  Note that the
 * versions in {@link java.util.concurrent.atomic}, such as {@link
 * java.util.concurrent.atomic.AtomicLongArray}, require extra memory ordering
 * guarantees which are generally not needed in these algorithms and are also
 * expensive on most processors.
 *
 * Copied from https://github.com/boundary/high-scale-lib @ 3654434eda00b68d37d22dcd70e4f65db9432d06
 */
class UtilUnsafe {
  private UtilUnsafe() { } // dummy private constructor
  /** Fetch the Unsafe.  Use With Caution. */
  public static Unsafe getUnsafe() {
    // Not on bootclasspath
    if( UtilUnsafe.class.getClassLoader() == null )
      return Unsafe.getUnsafe();
    try {
      final Field fld = Unsafe.class.getDeclaredField("theUnsafe");
      fld.setAccessible(true);
      return (Unsafe) fld.get(UtilUnsafe.class);
    } catch (Exception e) {
      throw new RuntimeException("Could not obtain access to sun.misc.Unsafe", e);
    }
  }
}
