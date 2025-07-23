package com.skullcreator.internal.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation placed on {@code VersionPatch} implementations to declare the
 * Minecraft version prefix strings they support.
 * <p>
 * Example:
 * <pre><code>
 * @SupportedVersion({"1.20", "1.20.1"})
 * public final class Patch120x implements VersionPatch {
 *     // ...
 * }
 * </code></pre>
 * The bootstrap loader matches the running server's version (e.g. "1.20.6")
 * against the supplied {@code value()} prefixes using {@code String#startsWith}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportedVersion {
    /**
     * One or more version prefix strings to match (e.g. "1.20").
     */
    String[] value();
}
