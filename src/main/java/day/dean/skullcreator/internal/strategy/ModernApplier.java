package day.dean.skullcreator.internal.strategy;

import day.dean.skullcreator.internal.ProfileResolver;
import day.dean.skullcreator.internal.strategy.FallbackApplier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Skull;

/**
 * Uses direct reflection into CraftBukkit internals (already initialised by ProfileResolver)
 * for fastest profile application. Returns null from {@link #tryCreate()} when reflection
 * failed during bootstrap so the caller can fall back.
 */
public class ModernApplier implements ProfileApplier {

    private ModernApplier() {}

    /**
     * Returns an instance iff {@link ProfileResolver#ready()} is true.
     */
    public static ModernApplier tryCreate() {
        return ProfileResolver.ready() ? new ModernApplier() : null;
    }

    @Override
    public ItemStack applyToItem(ItemStack item, String base64) {
        return ProfileResolver.applyProfileToItemInternal(item, base64);
    }

    @Override
    public void applyToBlock(Skull skull, String base64) {
        if (ProfileResolver.hasBlockReflection()) {
            ProfileResolver.applyProfileToBlockInternal(skull, base64);
        } else {
            // fallback for block only
            new FallbackApplier().applyToBlock(skull, base64);
        }
    }
}
