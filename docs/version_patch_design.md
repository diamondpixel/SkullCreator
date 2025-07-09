# Version‐specific Patch Module System for SkullCreator

This document outlines a clean, single-jar strategy for handling Minecraft version differences in SkullCreator without “modern / fallback” branching.

---

## 1. Concept
Each **version patch** is a tiny, self-contained class that implements the common `VersionPatch` interface.  At start-up SkullCreator:

1. Detects the running **major.minor** MC version (`1.20.6`, `1.19.4`, …).
2. Loads all `VersionPatch` implementations (via `ServiceLoader` or class-path scanning).
3. Picks the first patch whose `supports()` returns `true` and calls `apply()`.
4. If no patch claims the version, SkullCreator falls back or aborts.

---

## 2. Directory / Package Layout (single project)
```
src/main/java
└── com/skullcreator/patch
    ├── VersionPatch.java          <- interface
    ├── v1_18/Patch118x.java
    ├── v1_19/Patch119x.java
    └── v1_20/Patch120x.java
```
No extra sub-modules are necessary; all classes live in the same jar.

---

## 3. Core API
```java
package com.skullcreator.patch;

public interface VersionPatch {
    /** @param version e.g. "1.20.6" */
    boolean supports(String version);

    /** Install hooks / reflection rewrites etc. */
    void apply();
}
```

Example patch (`1.20.x`):
```java
package com.skullcreator.patch.v1_20;

import com.skullcreator.patch.VersionPatch;

public final class Patch120x implements VersionPatch {
    @Override public boolean supports(String v) { return v.startsWith("1.20."); }
    @Override public void apply() {
        // reflection offsets specific to 1.20.x
    }
}
```

---

## 4. Bootstrap Integration
```java
public final class ReflectionBootstrap {
    public static ProfileApplier bootstrap() {
        String mcVersion = Bukkit.getBukkitVersion();          // "1.20.6-R0.1-SNAPSHOT"

        VersionPatch chosen = ServiceLoader.load(VersionPatch.class).stream()
              .map(ServiceLoader.Provider::get)
              .filter(p -> p.supports(mcVersion))
              .findFirst()
              .orElse(null);

        if (chosen != null) {
            chosen.apply();
            return new ModernApplier(/* ctx */);
        }
        Bukkit.getLogger().severe("[SkullCreator] No patch for " + mcVersion);
        return new FallbackApplier();
    }
}
```

### Service registration (no external deps)
Create `src/main/resources/META-INF/services/com.skullcreator.patch.VersionPatch`:
```
com.skullcreator.patch.v1_18.Patch118x
com.skullcreator.patch.v1_19.Patch119x
com.skullcreator.patch.v1_20.Patch120x
```
`ServiceLoader` will now discover all patches automatically.

---

## 5. Advantages
* **Single responsibility** – each patch knows only its own offsets.
* **No runtime branching** – once selected, code path is straight.
* **Bukkit-neutral** – uses only Bukkit API + reflection, works on Spigot / Paper.
* **Easy upgrades** – to support 1.21, add `Patch121x` and update the service file.

---