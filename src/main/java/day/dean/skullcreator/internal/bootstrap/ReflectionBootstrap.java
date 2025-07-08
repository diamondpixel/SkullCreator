package day.dean.skullcreator.internal.bootstrap;

import day.dean.skullcreator.internal.ProfileResolver;
import day.dean.skullcreator.internal.strategy.FallbackApplier;
import day.dean.skullcreator.internal.strategy.ModernApplier;
import day.dean.skullcreator.internal.strategy.ProfileApplier;

/**
 * Central entry for reflection initialisation. Ensures we perform heavy
 * bootstrap once and pick the best {@link ProfileApplier} implementation.
 */
public final class ReflectionBootstrap {

    private ReflectionBootstrap() {}

    public static ProfileApplier bootstrap() {
        // Run original init
        ProfileResolver.initializeReflection();

        ProfileApplier modern = ModernApplier.tryCreate();
        return modern != null ? modern : new FallbackApplier();
    }
}
