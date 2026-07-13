package com.pppopipupu.decaylib;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * 物品自定义腐烂信息提示词的接口。
 * 实现该接口的物品可以完全控制其腐烂信息中关于产物或变质后的提示词输出。
 */
public interface IDecayTooltipProvider {
    /**
     * 向物品提示词列表中添加自定义的腐烂或产物信息。
     *
     * @param stack    当前的物品堆实例
     * @param tooltip  当前已有的提示词列表，可直接向其中追加新的提示行
     * @param advanced 是否启用了高级提示词显示模式 (F3 + H)
     */
    void addDecayTooltip(ItemStack stack, List<String> tooltip, boolean advanced);
}
