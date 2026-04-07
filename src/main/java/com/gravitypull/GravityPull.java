package gravity;

import gravity.mixin.FallingBlockEntityMixin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class GravityMod implements ModInitializer {
    public static final String MOD_ID = "gravity_mod";
    
    // Создаем наш предмет
    public static final Item GRAVITY_GAUNTLET = new GravityGauntletItem(
            new FabricItemSettings().maxDamage(1000).rarity(Rarity.EPIC)
    );

    @Override
    public void onInitialize() {
        // Регистрируем предмет
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gravity_gauntlet"), GRAVITY_GAUNTLET);

        // Добавляем в вкладку "Инструменты" в креативе
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(GRAVITY_GAUNTLET);
        });
    }
}
