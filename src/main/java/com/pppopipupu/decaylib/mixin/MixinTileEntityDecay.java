package com.pppopipupu.decaylib.mixin;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.pppopipupu.decaylib.DecayManager;
import com.pppopipupu.decaylib.event.DecayEvent;
import com.pppopipupu.decaylib.event.DecayEvent.DecayContext;

@Mixin(value = TileEntity.class)
public class MixinTileEntityDecay {

    @Shadow
    protected World worldObj;
    @Shadow
    public int xCoord;
    @Shadow
    public int yCoord;
    @Shadow
    public int zCoord;

    @Inject(method = "updateEntity", at = @At("RETURN"))
    private void decaylib$updateEntity(CallbackInfo ci) {
        if (this.worldObj == null) return;
        if (this.worldObj.isRemote) return;
        if (this.worldObj.getTotalWorldTime() % 20 != 0) return;
        if (!(this instanceof IInventory)) return;

        IInventory inv = (IInventory) this;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) continue;
            if (!DecayManager.isDecayable(stack)) continue;

            double x = this.xCoord + 0.5;
            double y = this.yCoord + 1.1;
            double z = this.zCoord + 0.5;

            DecayManager.updateDecay(stack, this.worldObj, x, y, z, DecayContext.TILE_ENTITY, inv, i);
        }
    }
}
