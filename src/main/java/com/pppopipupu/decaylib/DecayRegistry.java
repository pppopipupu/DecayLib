package com.pppopipupu.decaylib;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;

/**
 * 腐烂规则注册表中心。
 * 提供默认腐烂规则的注册与维护，以及基于配置文件的活动规则的装载和应用。
 */
public class DecayRegistry {

    private static final Map<Item, DecayRule> defaultRules = new HashMap<>();
    private static final Map<Item, DecayRule> activeRules = new HashMap<>();

    /**
     * 向注册表中注册默认的腐烂规则。
     * 该规则在无配置文件重写时默认生效，且会在配置文件不存在时作为默认模板被自动保存写出。
     *
     * @param item 被注册的物品实例
     * @param rule 该物品对应的腐烂配置规则
     */
    public static void registerDefault(Item item, DecayRule rule) {
        defaultRules.put(item, rule);
        activeRules.put(item, rule);
    }

    /**
     * 获取指定物品对应的活动腐烂规则。
     *
     * @param item 需要查询的物品
     * @return 该物品的腐烂规则实例；若不支持腐烂则返回 null
     */
    public static DecayRule getRule(Item item) {
        return activeRules.get(item);
    }

    /**
     * 判断指定物品是否是可腐烂的物品。
     *
     * @param item 需要判断的物品
     * @return 若该物品已注册了活动腐烂规则返回 true，否则返回 false
     */
    public static boolean isDecayable(Item item) {
        return activeRules.containsKey(item);
    }

    /**
     * 获取当前所有已注册的活动腐烂规则的只读集合。
     *
     * @return 活动腐烂规则集合
     */
    public static Collection<DecayRule> getRules() {
        return activeRules.values();
    }

    /**
     * 获取所有由开发商通过代码注册的初始默认腐烂规则的映射关系。
     *
     * @return 默认规则映射表 Map
     */
    public static Map<Item, DecayRule> getDefaultRules() {
        return defaultRules;
    }

    /**
     * 应用从外部配置文件中读取的规则，用以覆盖默认的腐烂设置。
     *
     * @param configRules 从配置文件反序列化得到的规则映射表
     */
    public static void applyConfigRules(Map<Item, DecayRule> configRules) {
        activeRules.clear();
        activeRules.putAll(defaultRules);
        activeRules.putAll(configRules);
    }

    /**
     * 清空所有的默认和活动规则，用于重新加载或注销。
     */
    public static void clear() {
        defaultRules.clear();
        activeRules.clear();
    }
}
