/*
 * Written by Cliff Click and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.vvcephei.occ_map;

/**
 * A simple high-performance counter.  Merely renames the extended {@link
 * org.cliffc.high_scale_lib.ConcurrentAutoTable} class to be more obvious.
 * {@link org.cliffc.high_scale_lib.ConcurrentAutoTable} already has a decent
 * counting API.
 *
 * Copied from https://github.com/boundary/high-scale-lib @ 3654434eda00b68d37d22dcd70e4f65db9432d06
 *
 * @author Cliff Click
 */

public class Counter extends ConcurrentAutoTable {

  // Add the given value to current counter value.  Concurrent updates will
  // not be lost, but addAndGet or getAndAdd are not implemented because but
  // the total counter value is not atomically updated.
  //public void add( long x );
  //public void decrement();
  //public void increment();

  // Current value of the counter.  Since other threads are updating furiously
  // the value is only approximate, but it includes all counts made by the
  // current thread.  Requires a pass over all the striped counters.
  //public long get();
  //public int  intValue();
  //public long longValue();

  // A cheaper 'get'.  Updated only once/millisecond, but fast as a simple
  // load instruction when not updating.
  //public long estimate_get( );

}

