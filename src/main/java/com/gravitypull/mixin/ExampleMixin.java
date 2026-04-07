package gravity.mixin;

import gravity.GravityMod;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    
    // Внедряемся в метод tick (выполняется каждый кадр игры)
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        FallingBlockEntity self = (FallingBlockEntity) (Object) this;
        World world = self.getWorld();

        // Проверяем, является ли блок "магическим" (есть наше имя и нет гравитации)
        if (self.hasCustomName() && self.hasNoGravity()) {
            String name = self.getCustomName().getString();
            
            if (name.startsWith("owner:")) {
                try {
                    // Извлекаем UUID игрока
                    String uuidStr = name.replace("owner:", "");
                    UUID ownerUUID = UUID.fromString(uuidStr);

                    // Ищем игрока в мире
                    PlayerEntity owner = world.getPlayerByUuid(ownerUUID);
                    
                    if (owner != null && !world.isClient) {
                        // МАТЕМАТИКА ОРБИТЫ
                        // Используем время мира как угол вращения
                        double time = world.getTime() + self.getId(); // +ID чтобы блоки не сливались
                        float speed = 0.05f; // Скорость вращения
                        double radius = 3.0;   // Радиус орбиты
                        
                        double angle = time * speed;
                        
                        // Вычисляем новые координаты вокруг головы игрока
                        double x = owner.getX() + Math.cos(angle) * radius;
                        double z = owner.getZ() + Math.sin(angle) * radius;
                        double y = owner.getY() + 2.0 + Math.sin(time * 0.1) * 0.5; // Легкое покачивание вверх-вниз
                        
                        // Перемещаем блок
                        self.setPosition(x, y, z);
                        
                        // Отменяем стандартную логику падения (важно!)
                        ci.cancel();
                    }
                } catch (IllegalArgumentException e) {
                    // Если имя не UUID, игнорируем
                }
            }
        }
    }
}
