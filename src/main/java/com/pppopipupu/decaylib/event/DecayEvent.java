package com.pppopipupu.decaylib.event;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * 物品彻底发生变质并转化为产物时触发的事件。
 * 该事件在 Forge 的 EVENT_BUS 上发布，允许第三方 Mod 在此阶段拦截、修改或取消变质。
 */
@Cancelable
public class DecayEvent extends Event {

    public final World world;
    public final double x;
    public final double y;
    public final double z;
    public final ItemStack originalStack;
    public final DecayContext context;
    public final Object carrier;

    private ItemStack productStack;
    private Entity productEntity;

    public DecayEvent(World world, double x, double y, double z, ItemStack originalStack, DecayContext context,
        Object carrier, ItemStack productStack, Entity productEntity) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.originalStack = originalStack;
        this.context = context;
        this.carrier = carrier;
        this.productStack = productStack;
        this.productEntity = productEntity;
    }

    public ItemStack getProductStack() {
        return productStack;
    }

    public void setProductStack(ItemStack productStack) {
        this.productStack = productStack;
    }

    public Entity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(Entity productEntity) {
        this.productEntity = productEntity;
    }

    public enum DecayContext {
        PLAYER_INVENTORY,
        TILE_ENTITY,
        ENTITY_ITEM
    }
}
