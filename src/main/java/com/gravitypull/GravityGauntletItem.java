package gravity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GravityGauntletItem extends Item {

    public GravityGauntletItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // Если игрок крадется (Shift) -> Запускаем блоки в орбите
        if (user.isSneaking()) {
            if (!world.isClient) {
                launchOrbitalBlocks(user);
            }
            return TypedActionResult.success(stack, world.isClient());
        }

        // Иначе пытаемся захватить блок
        HitResult hit = user.raycast(10.0, 0.0f, false);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isAir() || state.getBlock().getHardness() < 0) {
                return TypedActionResult.pass(stack);
            }

            if (!world.isClient) {
                // Создаем падающий блок
                FallingBlockEntity floatingBlock = new FallingBlockEntity(
                        world, 
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
                        state
                );
                
                // Удаляем блок из мира
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                
                // Настраиваем блок для орбиты
                floatingBlock.setNoGravity(true);
                floatingBlock.setGlowing(true);
                
                // Записываем UUID владельца в имя (хак для сохранения данных без сложных NBT)
                floatingBlock.setCustomName(Text.of("owner:" + user.getUuidAsString()));
                floatingBlock.setCustomNameVisible(false);

                world.spawnEntity(floatingBlock);
                
                user.getItemCooldownManager().set(this, 5);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, user.getSoundCategory(), 1.0f, 1.2f);
            }
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    private void launchOrbitalBlocks(PlayerEntity user) {
        World world = user.getWorld();
        Vec3d lookVec = user.getRotationVec(1.0f);
        String uuid = user.getUuidAsString();
        
        // Находим все блоки, которые принадлежат этому игроку
        for (FallingBlockEntity block : world.getEntitiesByClass(FallingBlockEntity.class, user.getBoundingBox().expand(20), e -> true)) {
            if (block.hasCustomName() && block.getCustomName().getString().startsWith("owner:" + uuid)) {
                // Запускаем!
                block.setNoGravity(false); // Включаем физику
                block.setVelocity(lookVec.x * 1.8, lookVec.y * 1.5, lookBin.z * 1.8); // Скорость полета
                block.setCustomName(null); // Сбрасываем владельца, чтобы он больше не летал вокруг
                block.dropItem = true; // При падении выпадет предмет
                // Урон при падении настраивается через mixin или будет стандартный
            }
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, user.getSoundCategory(), 0.5f, 1.0f);
    }
}
