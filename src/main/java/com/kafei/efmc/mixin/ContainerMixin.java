package com.kafei.efmc.mixin;

import com.kafei.efmc.common.inventory.MultiSlotContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMixin {
    @Shadow @Final
    public NonNullList<Slot> slots;

    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;I)V",
            at = @At("TAIL")
    )
    private void onContainerInit(CallbackInfo ci) {
        // 更安全的类型检查和转换
        if ((Object)this instanceof InventoryMenu) {
            InventoryMenu menu = (InventoryMenu)(Object)this;
            // 使用反射作为最后手段
            try {
                java.lang.reflect.Field field = InventoryMenu.class.getDeclaredField("container");
                field.setAccessible(true);
                Inventory inventory = (Inventory) field.get(menu);
                MultiSlotContainer.updateSlots(this.slots, inventory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}