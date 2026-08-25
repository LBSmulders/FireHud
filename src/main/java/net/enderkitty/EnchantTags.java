package net.enderkitty;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantTags {
    TagKey<Enchantment> FROST_WALKER = EnchantTags.of("prevents_fire_hearts");
            
    private static TagKey<Enchantment> of(String id) {
        return TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(FireHud.MOD_ID, id));
    }
}
