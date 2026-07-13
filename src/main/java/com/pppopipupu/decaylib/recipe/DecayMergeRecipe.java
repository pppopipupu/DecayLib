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

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
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
        long totalTimeLeft = 0;
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
                long timeLeft = defaultDuration;
                if (nbt != null && nbt.hasKey("decayTimeLeft")) {
                    timeLeft = nbt.getLong("decayTimeLeft");
                }

                totalTimeLeft += timeLeft;
                slotsCount++;
            }
        }

        if (slotsCount == 0) return null;

        long avgTimeLeft = totalTimeLeft / slotsCount;

        ItemStack result = new ItemStack(targetItem, slotsCount);
        NBTTagCompound resultNbt = new NBTTagCompound();
        resultNbt.setLong("decayTimeLeft", avgTimeLeft);
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
