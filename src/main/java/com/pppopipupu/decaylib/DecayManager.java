package com.pppopipupu.decaylib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.pppopipupu.decaylib.event.DecayEvent;
import com.pppopipupu.decaylib.event.DecayEvent.DecayContext;
import com.pppopipupu.decaylib.event.DecayTickEvent;

/**
 * 腐烂系统核心逻辑管理器。
 * 负责物品腐烂状态的初始化、每秒的更新计时、产物生成、事件分发以及粒子音效的播放。
 */
public class DecayManager {

    /**
     * 判断给定的物品堆是否是可以腐烂的物品。
     *
     * @param stack 物品堆实例
     * @return 若物品堆不为空且其物品已注册了腐烂规则返回 true，否则返回 false
     */
    public static boolean isDecayable(ItemStack stack) {
        return stack != null && stack.getItem() != null && DecayRegistry.isDecayable(stack.getItem());
    }

    /**
     * 初始化物品堆的腐烂时间和相关 NBT 数据。
     * 支持从旧版的 spoil 属性平滑过渡迁移到新版格式。
     *
     * @param stack 物品堆实例
     * @param world 游戏世界实例
     */
    public static void initDecayTime(ItemStack stack, World world) {
        if (!isDecayable(stack)) return;
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        if (nbt.hasKey("spoilTime") && !nbt.hasKey("decayTime")) {
            long spoilTime = nbt.getLong("spoilTime");
            long duration = nbt.hasKey("spoilDuration") ? nbt.getLong("spoilDuration") : 72000L;
            nbt.setLong("decayTime", spoilTime);
            nbt.setLong("decayDuration", duration);
            nbt.removeTag("spoilTime");
            nbt.removeTag("spoilDuration");
        }



        if (!nbt.hasKey("decayTime")) {
            DecayRule rule = DecayRegistry.getRule(stack.getItem());
            if (rule != null) {
                long duration = (long) (rule.decayTime * DecayConfig.globalDecayMultiplier);
                nbt.setLong("decayTime", world.getTotalWorldTime() + duration);
                nbt.setLong("decayDuration", duration);
            }
        }
    }

    private static Entity createEntity(String entityId, World world) {
        if (entityId == null || entityId.isEmpty()) return null;

        Entity entity = EntityList.createEntityByName(entityId, world);
        if (entity != null) return entity;

        if (entityId.contains(":")) {
            entity = EntityList.createEntityByName(entityId.replace(":", "."), world);
            if (entity != null) return entity;
        }

        Class<?> clazz = (Class<?>) EntityList.stringToClassMapping.get(entityId);
        if (clazz == null && entityId.contains(":")) {
            clazz = (Class<?>) EntityList.stringToClassMapping.get(entityId.replace(":", "."));
        }
        if (clazz != null) {
            try {
                return (Entity) clazz.getConstructor(World.class)
                    .newInstance(world);
            } catch (Exception e) {}
        }

        for (Object objKey : EntityList.stringToClassMapping.keySet()) {
            if (objKey instanceof String) {
                String registeredName = (String) objKey;
                if (registeredName.equalsIgnoreCase(entityId) || registeredName.endsWith("." + entityId)
                    || registeredName.endsWith(":" + entityId)) {
                    Class<?> c = (Class<?>) EntityList.stringToClassMapping.get(registeredName);
                    try {
                        return (Entity) c.getConstructor(World.class)
                            .newInstance(world);
                    } catch (Exception e) {}
                }
            }
        }

        return null;
    }

    /**
     * 更新物品堆的腐烂进度计时。
     * 每秒钟调用一次，驱动倒计时减少，当倒计时归零时触发变质并销毁/替换物品。
     *
     * @param stack   物品堆实例
     * @param world   游戏世界实例
     * @param x       当前事件发生的 X 坐标
     * @param y       当前事件发生的 Y 坐标
     * @param z       当前事件发生的 Z 坐标
     * @param context 触发更新的物品容器上下文 (例如玩家背包、容器方块、掉落物实体)
     * @param carrier 携带该物品堆的容器或实体对象实例
     * @param slot    该物品在载体中所处的槽位索引
     */
    public static void updateDecay(ItemStack stack, World world, double x, double y, double z, DecayContext context,
        Object carrier, int slot) {
        if (world.isRemote) return;
        if (!isDecayable(stack)) return;

        initDecayTime(stack, world);

        DecayTickEvent tickEvent = new DecayTickEvent(world, x, y, z, stack, context, carrier, slot);
        MinecraftForge.EVENT_BUS.post(tickEvent);

        NBTTagCompound nbt = stack.getTagCompound();
        long decayTime = nbt.getLong("decayTime");
        long defaultProgress = 20L;

        if (tickEvent.isCanceled()) {
            nbt.setLong("decayTime", decayTime + defaultProgress);
            return;
        }

        long actualProgress = tickEvent.getProgressAmount();
        if (actualProgress != defaultProgress) {
            nbt.setLong("decayTime", decayTime + defaultProgress - actualProgress);
            decayTime = decayTime + defaultProgress - actualProgress;
        }

        long remaining = decayTime - world.getTotalWorldTime();
        if (remaining > 0) return;

        DecayRule rule = DecayRegistry.getRule(stack.getItem());
        if (rule == null) return;

        ItemStack defaultProductStack = null;
        Entity defaultProductEntity = null;

        if (rule.action != null) {
            if ("item".equalsIgnoreCase(rule.action.type)) {
                Item item = (Item) Item.itemRegistry.getObject(rule.action.id);
                if (item != null) {
                    defaultProductStack = new ItemStack(item, rule.action.count, rule.action.damage);
                }
            } else if ("entity".equalsIgnoreCase(rule.action.type)) {
                defaultProductEntity = createEntity(rule.action.id, world);
            }
        }

        DecayEvent event = new DecayEvent(world, x, y, z, stack, context, carrier, defaultProductStack, defaultProductEntity);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return;
        }

        int toConsume = (event.getProductEntity() != null) ? 1 : stack.stackSize;
        ItemStack prod = event.getProductStack();

        if (prod != null) {
            if (toConsume == stack.stackSize) {
                ItemStack finalProduct = new ItemStack(
                    prod.getItem(),
                    stack.stackSize * prod.stackSize,
                    prod.getItemDamage());
                if (context == DecayContext.PLAYER_INVENTORY) {
                    EntityPlayer player = (EntityPlayer) carrier;
                    player.inventory.setInventorySlotContents(slot, finalProduct);
                } else if (context == DecayContext.TILE_ENTITY) {
                    IInventory inv = (IInventory) carrier;
                    inv.setInventorySlotContents(slot, finalProduct);
                    inv.markDirty();
                } else if (context == DecayContext.ENTITY_ITEM) {
                    EntityItem entityItem = (EntityItem) carrier;
                    entityItem.setEntityItemStack(finalProduct);
                }
            } else {
                ItemStack finalProduct = new ItemStack(
                    prod.getItem(),
                    toConsume * prod.stackSize,
                    prod.getItemDamage());
                EntityItem newEi = new EntityItem(world, x, y, z, finalProduct);
                world.spawnEntityInWorld(newEi);

                if (context == DecayContext.PLAYER_INVENTORY) {
                    EntityPlayer player = (EntityPlayer) carrier;
                    player.inventory.decrStackSize(slot, toConsume);
                } else if (context == DecayContext.TILE_ENTITY) {
                    IInventory inv = (IInventory) carrier;
                    inv.decrStackSize(slot, toConsume);
                    inv.markDirty();
                } else if (context == DecayContext.ENTITY_ITEM) {
                    EntityItem entityItem = (EntityItem) carrier;
                    stack.splitStack(toConsume);
                    if (stack.stackSize <= 0) {
                        entityItem.setDead();
                    } else {
                        entityItem.setEntityItemStack(stack);
                    }
                }
            }
        } else {
            if (context == DecayContext.PLAYER_INVENTORY) {
                EntityPlayer player = (EntityPlayer) carrier;
                player.inventory.decrStackSize(slot, toConsume);
            } else if (context == DecayContext.TILE_ENTITY) {
                IInventory inv = (IInventory) carrier;
                inv.decrStackSize(slot, toConsume);
                inv.markDirty();
            } else if (context == DecayContext.ENTITY_ITEM) {
                EntityItem entityItem = (EntityItem) carrier;
                stack.splitStack(toConsume);
                if (stack.stackSize <= 0) {
                    entityItem.setDead();
                } else {
                    entityItem.setEntityItemStack(stack);
                }
            }
        }

        if (event.getProductEntity() != null) {
            Entity ent = event.getProductEntity();
            ent.setPosition(x, y, z);
            world.spawnEntityInWorld(ent);
        }

        spawnDecayEffects(world, x, y, z);
    }

    /**
     * 在发生腐烂变质的世界位置播放史莱姆音效并生成绿色粘液粒子效果。
     *
     * @param world 游戏世界实例
     * @param x     粒子生成的 X 坐标
     * @param y     粒子生成的 Y 坐标
     * @param z     粒子生成的 Z 坐标
     */
    public static void spawnDecayEffects(World world, double x, double y, double z) {
        world.playSoundEffect(x, y, z, "mob.slime.big", 1.0F, 1.0F);
        for (int i = 0; i < 8; i++) {
            world.spawnParticle(
                "slime",
                x + world.rand.nextGaussian() * 0.5,
                y + world.rand.nextGaussian() * 0.5,
                z + world.rand.nextGaussian() * 0.5,
                0,
                0,
                0);
        }
    }
}
