package com.skullcreator.internal.interfaces;

/**
 * Contract for Minecraft version-specific patches that know how to embed a custom<br>
 * Base64 skin texture into player-head <i>items</i> and <i>block states</i> for a
 * particular range of server builds.
 * <p>
 * Usage overview:
 * <ol>
 *   <li>Each concrete patch advertises its supported version prefixes via the
 *       {@link SupportedVersion} annotation <em>or</em> by overriding
 *       {@link #supports(String)} for more complex logic.</li>
 *   <li>Patches register themselves at class-load time using
 *       {@code PatchesRegistry.register(this)} inside a static block.</li>
 *   <li>{@link com.skullcreator.internal.bootstrap.PatchBootstrap#bootstrap()}
 *       picks the first registered patch whose {@code supports(..)} method
 *       returns {@code true} for the running server, then exposes it through
 *       {@link com.skullcreator.internal.bootstrap.PatchBootstrap#active()}.</li>
 *   <li>Public API callers go through {@link com.skullcreator.internal.TextureApplier}
 *       which delegates to the active patch.</li>
 * </ol>
 * This interface therefore contains only the two operations actually needed at
 * runtime: mutate an {@link org.bukkit.inventory.ItemStack} or a placed
 * {@link org.bukkit.block.Skull}. Patch classes should keep internal state
 * (e.g. reflection caches) private and may optionally expose {@link #clearCache}
 * and {@link #getCacheSize} for monitoring.
 */
public interface VersionPatch {

    /**
     * Whether this patch supports the supplied Minecraft version string.
     * Implementations can rely on the {@link SupportedVersion} annotation instead
     * of overriding this method. If the annotation is present, the default
     * implementation performs a prefix check on all declared values.
     *
     * @param version full Bukkit version, e.g. "1.20.6-R0.1-SNAPSHOT"
     * @return {@code true} if the patch should be used
     */
    default boolean supports(String version) {
        SupportedVersion ann = this.getClass().getAnnotation(SupportedVersion.class);
        if (ann == null) {
            return false;
        }
        for (String prefix : ann.value()) {
            if (version.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Apply the texture to a skull ItemStack.
     * @param item   player-head ItemStack (SkullMeta)
     * @param base64 Base64-encoded texture string
     * @return mutated ItemStack (can be the same instance for chaining)
     */
    org.bukkit.inventory.ItemStack applyToItem(org.bukkit.inventory.ItemStack item, String base64);

    /**
     * Apply the texture to a placed Skull block state.
     * @param skull  Skull block state
     * @param base64 Base64-encoded texture string
     */
    void applyToBlock(org.bukkit.block.Skull skull, String base64);

    /** Clears any internal caches kept by the patch (optional). */
    default void clearCache() {}

    /** Returns the size of any internal caches (optional, default 0). */
    default int getCacheSize() { return 0; }
}
