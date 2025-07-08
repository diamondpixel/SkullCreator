package day.dean.skullcreator.internal.strategy;

import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;

/**
 * Strategy interface used by ProfileResolver to apply a base64 skin texture
 * either to an {@link ItemStack} (SkullMeta) or to a block {@link Skull} state.
 */
public interface ProfileApplier {

    /**
     * Apply the texture to the provided skull item.
     *
     * @param item   player-head ItemStack (will be validated by implementation)
     * @param base64 texture string
     * @return mutated ItemStack (usually the same instance for chaining)
     */
    ItemStack applyToItem(ItemStack item, String base64);

    /**
     * Apply the texture to the provided Skull block state.
     * Implementation must call {@link Skull#update()} if needed.
     *
     * @param skull  block state
     * @param base64 texture string
     */
    void applyToBlock(Skull skull, String base64);
}
