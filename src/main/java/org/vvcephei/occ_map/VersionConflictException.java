package org.vvcephei.occ_map;

import java.util.Objects;

/**
 * Exception result signaling a version conflict on puts into the map.
 * We extend IllegalArgumentException to comply with the Map interface, which says:
 * <p/>
 * throws IllegalArgumentException - if some property of the specified key or value prevents it from being stored in this map
 * <p/>
 * <p/>
 * This exception provides getters for structured handling of the error condition.
 */
public class VersionConflictException extends IllegalArgumentException {
    private final Object key;
    private final long existingVersion;
    private final long putVersion;

    public VersionConflictException(final Object key, final long existingVersion, final long putVersion) {
        super("Version conflict on key[" + Objects.toString(key) + "]. Existing version[" + existingVersion + "]; Attepted to put version[" + putVersion + "].");
        this.key = key;
        this.existingVersion = existingVersion;
        this.putVersion = putVersion;
    }

    /**
     * @return The key on which there was a conflict.
     */
    public Object getKey() {
        return key;
    }

    /**
     * @return The version that was in the map at the time of the put. This will be greater than the putVersion.
     */
    public long getExistingVersion() {
        return existingVersion;
    }

    /**
     * @return The version at which the put failed, since it was less than or equal to the existingVersion.
     */
    public long getPutVersion() {
        return putVersion;
    }
}
