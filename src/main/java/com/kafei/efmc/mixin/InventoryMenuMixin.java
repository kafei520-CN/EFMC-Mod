package com.kafei.efmc.mixin;

import com.kafei.efmc.common.inventory.MultiSlotContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.world.entity.player.Player;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/player/Inventory;ZLnet/minecraft/world/entity/player/Player;)V",
            at = @At("RETURN")
    )
    private void onInit(Inventory inventory, boolean isClientSide, Player player, CallbackInfo ci) {
        // 在这里可以直接访问构造函数的参数
        MultiSlotContainer.updateSlots(((AbstractContainerMenu)(Object)this).slots, inventory);
    }
}