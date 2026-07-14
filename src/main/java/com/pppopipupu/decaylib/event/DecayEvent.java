package com.pppopipupu.decaylib.event;

import java.util.ArrayList;
import java.util.List;

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

    private final List<ItemStack> productStacks = new ArrayList<>();
    private final List<Entity> productEntities = new ArrayList<>();

    public DecayEvent(World world, double x, double y, double z, ItemStack originalStack, DecayContext context,
        Object carrier, List<ItemStack> productStacks, List<Entity> productEntities) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.originalStack = originalStack;
        this.context = context;
        this.carrier = carrier;
        if (productStacks != null) {
            this.productStacks.addAll(productStacks);
        }
        if (productEntities != null) {
            this.productEntities.addAll(productEntities);
        }
    }

    @Deprecated
    public DecayEvent(World world, double x, double y, double z, ItemStack originalStack, DecayContext context,
        Object carrier, ItemStack productStack, Entity productEntity) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.originalStack = originalStack;
        this.context = context;
        this.carrier = carrier;
        if (productStack != null) {
            this.productStacks.add(productStack);
        }
        if (productEntity != null) {
            this.productEntities.add(productEntity);
        }
    }

    public ItemStack getProductStack() {
        return productStacks.isEmpty() ? null : productStacks.get(0);
    }

    public void setProductStack(ItemStack productStack) {
        if (productStack == null) {
            if (!productStacks.isEmpty()) {
                productStacks.remove(0);
            }
        } else {
            if (productStacks.isEmpty()) {
                productStacks.add(productStack);
            } else {
                productStacks.set(0, productStack);
            }
        }
    }

    public Entity getProductEntity() {
        return productEntities.isEmpty() ? null : productEntities.get(0);
    }

    public void setProductEntity(Entity productEntity) {
        if (productEntity == null) {
            if (!productEntities.isEmpty()) {
                productEntities.remove(0);
            }
        } else {
            if (productEntities.isEmpty()) {
                productEntities.add(productEntity);
            } else {
                productEntities.set(0, productEntity);
            }
        }
    }

    public List<ItemStack> getProductStacks() {
        return productStacks;
    }

    public List<Entity> getProductEntities() {
        return productEntities;
    }

    public enum DecayContext {
        PLAYER_INVENTORY,
        TILE_ENTITY,
        ENTITY_ITEM
    }
}
