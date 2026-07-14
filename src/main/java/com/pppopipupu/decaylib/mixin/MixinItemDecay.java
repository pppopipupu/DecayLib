package com.pppopipupu.decaylib.mixin;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.pppopipupu.decaylib.DecayManager;
import com.pppopipupu.decaylib.DecayRegistry;
import com.pppopipupu.decaylib.DecayRule;
import com.pppopipupu.decaylib.IDecayTooltipProvider;
import com.pppopipupu.decaylib.event.DecayEvent.DecayContext;

@Mixin(value = Item.class)
public class MixinItemDecay {

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void decaylib$onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected,
        CallbackInfo ci) {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;
        if (!DecayManager.isDecayable(stack)) return;

        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;

        DecayManager.updateDecay(stack, world, x, y, z, DecayContext.PLAYER_INVENTORY, entity, slot);
    }

    @Inject(method = "onEntityItemUpdate", at = @At("HEAD"), remap = false)
    private void decaylib$onEntityItemUpdate(EntityItem entityItem, CallbackInfoReturnable<Boolean> cir) {
        World world = entityItem.worldObj;
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;

        ItemStack stack = entityItem.getEntityItem();
        if (!DecayManager.isDecayable(stack)) return;

        DecayManager.updateDecay(
            stack,
            world,
            entityItem.posX,
            entityItem.posY,
            entityItem.posZ,
            DecayContext.ENTITY_ITEM,
            entityItem,
            0);
    }

    @Inject(method = "addInformation", at = @At("RETURN"))
    private void decaylib$addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced,
        CallbackInfo ci) {
        if (!DecayManager.isDecayable(stack)) return;

        Item item = stack.getItem();
        DecayRule rule = DecayRegistry.getRule(item);
        long duration = (rule != null) ? rule.decayTime : 72000L;

        NBTTagCompound nbt = stack.getTagCompound();
        long timeLeft = duration;
        if (nbt != null) {
            if (nbt.hasKey("decayTime")) {
                timeLeft = nbt.getLong("decayTime") - player.worldObj.getTotalWorldTime();
            } else if (nbt.hasKey("spoilTime")) {
                timeLeft = nbt.getLong("spoilTime") - player.worldObj.getTotalWorldTime();
            }
        }
        if (timeLeft < 0) timeLeft = 0;

        if (timeLeft > 0) {
            long minutes = timeLeft / 1200;
            long seconds = (timeLeft % 1200) / 20;
            double ratio = (double) timeLeft / duration;
            String color;
            if (ratio > 0.5) {
                color = "§a";
            } else if (ratio > 0.2) {
                color = "§e";
            } else {
                color = "§c";
            }
            int filled = (int) (ratio * 20);
            StringBuilder bar = new StringBuilder("§7[");
            bar.append(color);
            for (int i = 0; i < filled; i++) bar.append('|');
            bar.append("§7");
            for (int i = filled; i < 20; i++) bar.append('|');
            bar.append("§r]");
            list.add(StatCollector.translateToLocalFormatted("tooltip.decaylib.decay", minutes, seconds));
            list.add(bar.toString());

            if (item instanceof IDecayTooltipProvider) {
                ((IDecayTooltipProvider) item).addDecayTooltip(stack, list, advanced);
            } else if (rule != null) {
                if (rule.productTooltip != null && !rule.productTooltip.isEmpty()) {
                    list.add(StatCollector.translateToLocal(rule.productTooltip));
                } else if (rule.action != null) {
                    if ("item".equalsIgnoreCase(rule.action.type)) {
                        Item prodItem = (Item) Item.itemRegistry.getObject(rule.action.id);
                        if (prodItem != null) {
                            list.add(
                                StatCollector.translateToLocal("tooltip.decaylib.product") + " §b"
                                    + prodItem.getItemStackDisplayName(new ItemStack(prodItem)));
                        }
                    }
                }
            }
        } else {
            list.add(StatCollector.translateToLocal("tooltip.decaylib.decayed"));
        }
    }
}
