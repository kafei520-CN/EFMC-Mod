package com.kafei.efmc.common.inventory;

import com.kafei.efmc.common.config.MultiSlotConfig;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MultiSlotContainer {
    // 移除未使用的SLOT_MAPPING，因为它确实从未被查询
    // 如果需要未来扩展，可以在此处添加注释说明

    // 预计算槽位位置以提高性能
    private static final int[] SLOT_X_CACHE = new int[54]; // 假设最大54个槽位
    private static final int[] SLOT_Y_CACHE = new int[54];

    static {
        // 初始化时预计算所有可能槽位的位置
        for (int i = 0; i < 54; i++) {
            SLOT_X_CACHE[i] = (i % 9) * 18 + 8;
            SLOT_Y_CACHE[i] = (i / 9) * 18 + 8;
        }
    }

    public static class MultiSlot extends Slot {
        private final int masterSlot;

        public MultiSlot(Container container, int slot, int x, int y, int masterSlot) {
            super(container, slot, x, y);
            this.masterSlot = masterSlot;
            // 移除了SLOT_MAPPING的使用
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return false; // 从属槽位不允许放置物品
        }

        @Nonnull
        @Override
        public ItemStack getItem() {
            return container.getItem(masterSlot);
        }
    }

    public static void updateSlots(@Nonnull List<Slot> slots, @Nonnull Container container) {
        List<Slot> newSlots = new ArrayList<>(container.getContainerSize());

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemId != null && MultiSlotConfig.isMultiSlot(itemId)) {
                    MultiSlotConfig.SlotData size = MultiSlotConfig.getSize(itemId);
                    createMultiSlotGroup(newSlots, container, i, size.width(), size.height());
                    i += size.width() * size.height() - 1; // 跳过已处理的槽位
                    continue;
                }
            }
            // 使用预计算的槽位位置
            newSlots.add(new Slot(container, i, SLOT_X_CACHE[i], SLOT_Y_CACHE[i]));
        }

        slots.clear();
        slots.addAll(newSlots);
    }

    private static void createMultiSlotGroup(
            @Nonnull List<Slot> slots,
            @Nonnull Container container,
            int masterIndex,
            int width,
            int height) {

        int startX = masterIndex % 9;
        int startY = masterIndex / 9;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int slotIndex = (startY + y) * 9 + (startX + x);
                if (x == 0 && y == 0) {
                    slots.add(new Slot(container, masterIndex,
                            SLOT_X_CACHE[slotIndex], SLOT_Y_CACHE[slotIndex]));
                } else {
                    slots.add(new MultiSlot(container, slotIndex,
                            SLOT_X_CACHE[slotIndex], SLOT_Y_CACHE[slotIndex],
                            masterIndex));
                }
            }
        }
    }
}