package com.pppopipupu.decaylib.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.pppopipupu.decaylib.event.DecayEvent.DecayContext;

/**
 * 物品在容器中进行腐烂更新计时（每秒触发一次）时触发的事件。
 * 允许第三方 Mod 监听并改变每次计时扣除的 tick 进度值（例如根据环境调节速度），或直接取消该次计时更新。
 */
@Cancelable
public class DecayTickEvent extends Event {

    public final World world;
    public final double x;
    public final double y;
    public final double z;
    public final ItemStack stack;
    public final DecayContext context;
    public final Object carrier;
    public final int slot;

    private long progressAmount = 20L;

    public DecayTickEvent(World world, double x, double y, double z, ItemStack stack, DecayContext context, Object carrier, int slot) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.stack = stack;
        this.context = context;
        this.carrier = carrier;
        this.slot = slot;
    }

    public long getProgressAmount() {
        return progressAmount;
    }

    public void setProgressAmount(long progressAmount) {
        this.progressAmount = progressAmount;
    }
}
