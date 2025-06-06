package com.kafei.efmc.common.inventory;

import com.kafei.efmc.common.config.MultiSlotConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SlotPlacementHandler {
    // 使用线程安全的WeakHashMap
    private static final Map<Container, Set<Integer>> OCCUPIED_SLOTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static boolean canPlaceItem(Container container, int slotIndex, ItemStack stack) {
        if (stack.isEmpty()) return true;

        // 检查槽位是否已被占用
        if (isSlotOccupied(container, slotIndex)) {
            return false;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !MultiSlotConfig.isMultiSlot(itemId)) {
            return container.getItem(slotIndex).isEmpty();
        }

        MultiSlotConfig.SlotData size = MultiSlotConfig.getSize(itemId);
        int startX = slotIndex % 9;
        int startY = slotIndex / 9;

        // 边界检查
        if (startX + size.width() > 9 || startY + size.height() > container.getContainerSize() / 9) {
            return false;
        }

        // 检查所有目标插槽
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                int targetSlot = (startY + y) * 9 + (startX + x);
                if (isSlotOccupied(container, targetSlot) || !container.getItem(targetSlot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static synchronized void placeMultiSlotItem(Container container, int slotIndex, ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !MultiSlotConfig.isMultiSlot(itemId)) {
            container.setItem(slotIndex, stack);
            return;
        }

        MultiSlotConfig.SlotData size = MultiSlotConfig.getSize(itemId);
        Set<Integer> occupied = OCCUPIED_SLOTS.computeIfAbsent(container,
                k -> Collections.synchronizedSet(new HashSet<>()));

        // 放置主物品
        container.setItem(slotIndex, stack);
        occupied.add(slotIndex);

        // 标记从属插槽
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                if (x == 0 && y == 0) continue;
                int targetSlot = (slotIndex / 9 + y) * 9 + (slotIndex % 9 + x);
                container.setItem(targetSlot, ItemStack.EMPTY);
                occupied.add(targetSlot);
            }
        }
    }

    public static synchronized void removeMultiSlotItem(Container container, int slotIndex) {
        ItemStack stack = container.getItem(slotIndex);
        if (stack.isEmpty()) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !MultiSlotConfig.isMultiSlot(itemId)) return;

        MultiSlotConfig.SlotData size = MultiSlotConfig.getSize(itemId);
        Set<Integer> occupied = OCCUPIED_SLOTS.get(container);
        if (occupied == null) return;

        // 清理所有关联插槽
        for (int y = 0; y < size.height(); y++) {
            for (int x = 0; x < size.width(); x++) {
                int targetSlot = (slotIndex / 9 + y) * 9 + (slotIndex % 9 + x);
                container.setItem(targetSlot, ItemStack.EMPTY);
                occupied.remove(targetSlot);
            }
        }
    }

    public static boolean isSlotOccupied(Container container, int slotIndex) {
        Set<Integer> occupied = OCCUPIED_SLOTS.get(container);
        return occupied != null && occupied.contains(slotIndex);
    }

    @Nullable
    public static Integer getMasterSlot(Container container, int slotIndex) {
        Set<Integer> occupied = OCCUPIED_SLOTS.get(container);
        if (occupied == null) return null;

        // 优化搜索性能
        for (int masterSlot : occupied) {
            ItemStack stack = container.getItem(masterSlot);
            if (stack.isEmpty()) continue;

            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId == null || !MultiSlotConfig.isMultiSlot(itemId)) continue;

            MultiSlotConfig.SlotData size = MultiSlotConfig.getSize(itemId);
            int startX = masterSlot % 9;
            int startY = masterSlot / 9;
            int endX = startX + size.width();
            int endY = startY + size.height();

            int currentX = slotIndex % 9;
            int currentY = slotIndex / 9;

            if (currentX >= startX && currentX < endX &&
                    currentY >= startY && currentY < endY) {
                return masterSlot;
            }
        }
        return null;
    }
}