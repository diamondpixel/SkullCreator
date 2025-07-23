package com.skullcreator.internal;

import com.skullcreator.internal.interfaces.VersionPatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple central registry that mimics {@code ServiceLoader} for {@link VersionPatch} implementations.
 * <p>
 * Each concrete patch class simply calls {@code PatchesRegistry.register(new PatchXYZ())}
 * from a static initialiser.  The registry is therefore filled automatically once the
 * class is loaded by the JVM (e.g. when referenced from plugin code or via {@code Class.forName}).
 */
public final class PatchesRegistry {
    private PatchesRegistry() {}

    private static final List<VersionPatch> PATCHES = new ArrayList<>();

    /**
     * Registers a {@link VersionPatch}.  Should be called from the patch's static block.
     */
    public static void register(VersionPatch patch) {
        PATCHES.add(patch);
    }

    /**
     * Unmodifiable view of all registered patches.
     */
    public static List<VersionPatch> patches() {
        return Collections.unmodifiableList(PATCHES);
    }

    /**
     * Finds the first patch whose {@link VersionPatch#supports(String)} returns {@code true}.
     *
     * @param mcVersion full version string, e.g. "1.20.6-R0.1-SNAPSHOT"
     * @return matching patch or {@code null}
     */
    public static VersionPatch select(String mcVersion) {
        for (VersionPatch patch : PATCHES) {
            try {
                if (patch.supports(mcVersion)) {
                    return patch;
                }
            } catch (Throwable t) {
                // Ignore mis-behaving patch
            }
        }
        return null;
    }
}
