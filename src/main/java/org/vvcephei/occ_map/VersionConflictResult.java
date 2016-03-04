package org.vvcephei.occ_map;

public class VersionConflictResult {
    final Object existingVal;
    final Object putVal;

    public VersionConflictResult(final Object existingVal, final Object putVal) {
        this.existingVal = existingVal;
        this.putVal = putVal;
    }
}
