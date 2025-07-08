package com.skullcreator.internal.bootstrap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Immutable holder for all reflective handles required to apply skull profiles
 * via CraftBukkit internals.  Encapsulating these details lets us decouple the
 * bootstrap phase from the runtime logic inside {@code ModernApplier}.
 */
public final class ReflectionContext {
    public final Field profileField;
    public final Method setProfileMethod;

    public final Class<?> gameProfileClass;
    public final Constructor<?> gameProfileCtor;

    public final Class<?> propertyClass;
    public final Constructor<?> propertyCtor;
    public final Field propertiesField;
    public final Method putMethod;

    public final boolean useResolvableProfile;
    public final Class<?> resolvableProfileClass;
    public final Constructor<?> resolvableProfileCtor;

    public ReflectionContext(Field profileField,
                              Method setProfileMethod,
                              Class<?> gameProfileClass,
                              Constructor<?> gameProfileCtor,
                              Class<?> propertyClass,
                              Constructor<?> propertyCtor,
                              Field propertiesField,
                              Method putMethod,
                              boolean useResolvableProfile,
                              Class<?> resolvableProfileClass,
                              Constructor<?> resolvableProfileCtor) {
        this.profileField = profileField;
        this.setProfileMethod = setProfileMethod;
        this.gameProfileClass = gameProfileClass;
        this.gameProfileCtor = gameProfileCtor;
        this.propertyClass = propertyClass;
        this.propertyCtor = propertyCtor;
        this.propertiesField = propertiesField;
        this.putMethod = putMethod;
        this.useResolvableProfile = useResolvableProfile;
        this.resolvableProfileClass = resolvableProfileClass;
        this.resolvableProfileCtor = resolvableProfileCtor;
    }
}
