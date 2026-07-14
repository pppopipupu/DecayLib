package com.pppopipupu.decaylib.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.pppopipupu.decaylib.DecayManager;
import com.pppopipupu.decaylib.DecayRegistry;
import com.pppopipupu.decaylib.DecayRule;

public class DecayMergeRecipe implements IRecipe {

    private long lastWorldTime = 0;

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (world != null) {
            this.lastWorldTime = world.getTotalWorldTime();
        }
        int slotsCount = 0;
        Item targetItem = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                if (!DecayManager.isDecayable(stack)) {
                    return false;
                }
                if (targetItem == null) {
                    targetItem = stack.getItem();
                } else if (stack.getItem() != targetItem) {
                    return false;
                }
                slotsCount++;
            }
        }

        return slotsCount >= 2;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        Item targetItem = null;
        long totalDecayTime = 0;
        int slotsCount = 0;
        long defaultDuration = 72000L;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null) {
                if (targetItem == null) {
                    targetItem = stack.getItem();
                    DecayRule rule = DecayRegistry.getRule(targetItem);
                    if (rule != null) {
                        defaultDuration = rule.decayTime;
                    }
                }

                NBTTagCompound nbt = stack.getTagCompound();
                long decayTime = 0;
                if (nbt != null) {
                    if (nbt.hasKey("decayTime")) {
                        decayTime = nbt.getLong("decayTime");
                    } else if (nbt.hasKey("spoilTime")) {
                        decayTime = nbt.getLong("spoilTime");
                    }
                }
                if (decayTime == 0) {
                    decayTime = lastWorldTime + defaultDuration;
                }

                totalDecayTime += decayTime;
                slotsCount++;
            }
        }

        if (slotsCount == 0) return null;

        long avgDecayTime = totalDecayTime / slotsCount;

        ItemStack result = new ItemStack(targetItem, slotsCount);
        NBTTagCompound resultNbt = new NBTTagCompound();
        resultNbt.setLong("decayTime", avgDecayTime);
        resultNbt.setLong("decayDuration", defaultDuration);
        result.setTagCompound(resultNbt);

        return result;
    }

    @Override
    public int getRecipeSize() {
        return 4;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }
}
