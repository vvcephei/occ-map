# OCCMap

A Java HashMap that provides Optimistic Concurrency Control (OCC).

## OCC

To understand OCC, the [wikipedia page](https://en.wikipedia.org/wiki/Optimistic_concurrency_control) is a good jumping-off point. I'll give a brief description here:

OCC is a database concept to allow lock-free concurrent updates efficiently when most updates do not conflict. Consider the typical read-modify-write scenario:

Assume we have two threads, A and B. and a data record R, whose initial value is 0. These threads both read R, increment it, and write it back.

Non-concurrently, it looks like:

1. Thread A reads R and gets 0.
1. Thread A increments R.
1. Thread A writes R.
1. Thread B reads R and gets 1.
1. Thread B increments R.
1. Thread B writes R.

The final value of R is 2.

Concurrently, it might be fine, but it might also go bad:

1. Thread A reads R and gets 0.
1. Thread A increments R.
1. Thread B reads R and gets 0.
1. Thread B increments R.
1. Thread A writes R.
1. Thread B writes R.

The final value of R is 1. Oops! There was a conflict.

To prevent this, A and B can aquire some kind of lock, either on the whole database, or a chunk of it, or just on the record R. 
Granular locking will clearly get you better performance if there is a lot of contention, but any way you cut it, there is a
substantial amount of overhead in acquiring the lock.

Another way to manage this situation is to assert that the existing data record is unchanged when you write it back:


1. Thread A reads R and gets 0.
1. Thread A increments R.
1. Thread B reads R and gets 0.
1. Thread B increments R.
1. Thread A writes R=1 if and only if the current value is 0 (what it read to begin with).
1. Thread B writes R=1 if and only if the current value is 0 (what it read to begin with). // Error: the condition failed, since the value is now 1
1. Thread B reads R and gets 1. // Because of the error B tries again
1. Thread B increments R.
1. Thread B writes R.

### Versions

Rather than make an assertion about the whole data of the data object (which could be large or complex), we can achieve the same benefits from using a version number.
You keep track of the version you read the record at, and when you write you increment the version number. The implicit assertion is that version numbers can only increase,
never decrease, and never stay the same.

### Drawbacks

This is a much more practical approach in (for example) distributed databases or other systems where locks are costly to acquire, but there are some drawbacks.
The main ones that come to mind are:

1. If there turns out to be a lot of contention for the same records, OCC cannot guarantee fairness the way a mutex can.
1. If there is a lot of contention, repeatedly reading, modifying, and writing the record could be more costly than simply waiting for a mutex.
 
## Why a Java Optimistic Concurrency Control Map?

I mentioned that OCC is great for distributed databases, but a hash map in the Java heap is definitely not one of those.

There are plenty of cases in which a simple in-memory key-value "database" is all an application needs, and these applications may be highly concurrent.
java.util.ConcurrentHashMap provides thread-safe read and write access to the map, but it cannot give you thread-safe read-modify-write semantics.

Synchronizing on access to the map introduces potentially harmful contention. You can create an OCC facade that only synchronizes on write, but your only
option is to synchronize on all writes, which could still be too much contention. Any further granularity requires a new Map implementation, which is what you
have in front of you!

## Details

OCCHashMap adds a restriction that the value class must implement Versioned (it must expose getVersion()). This allows us to implement OCC operations in the map
without departing from the map interface. The downside is that if your values are simple Strings or other objects you don't have control over. In this case, you will have to
wrap your value classes in a simple Versioned container like this one from the test:

    public static class VersionedString implements Versioned {
        public final String string;
        public final int version;
    
        public VersionedString(final String string, final int version) {
          this.string = string;
          this.version = version;
        }

        @Override public int getVersion() { return version; }

        @Override public boolean equals(final Object o) {
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;
          final VersionedString that = (VersionedString) o;
          return version == that.version && !(string != null ? !string.equals(that.string) : that.string != null);
        }
    
        @Override public int hashCode() {
          int result = string != null ? string.hashCode() : 0;
          result = 31 * result + version;
          return result;
        }
    
        @Override public String toString() { return Objects.toString(string)+"@v"+version; }
      }

ConcurrentHashMap gives better write concurrency than a synchronized map by chopping the map up into shards and synchronizing writes within the shards.
I could have extended ConcurrentHashMap to check the version of the values after acquiring the shard lock.
 
Instead, I chose to extend Cliff Click's awesome NonBlockingHashMap. He figured out that you don't have to lock at all on writes if you are very careful about
the order of operations when you write into the map. For more information, I'll refer you to [his blog article](http://www.azulsystems.com/blog/cliff/2007-03-26-non-blocking-hashtable)
and [the source code](https://github.com/boundary/high-scale-lib/blob/master/src/main/java/org/cliffc/high_scale_lib/NonBlockingHashMap.java) (this is a fork, which appears more
actively maintained than Cliff's original one).

The change I needed requires only the addition of the Versioned interface and the following line of code right before the new value gets set:

    if (V != null && putval instanceof Versioned && V instanceof Versioned && ((Versioned) putval).getVersion() <= ((Versioned) V).getVersion()) return V;

Pretty simple eh?

The rest of the code in here is copied from [https://github.com/boundary/high-scale-lib](https://github.com/boundary/high-scale-lib) to support the OCCHashMap. It needed to be copied 
becuase I didn't want to change the package-protected access on the relevant members.
