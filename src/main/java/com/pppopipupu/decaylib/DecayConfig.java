package com.pppopipupu.decaylib;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 配置文件加载与导出管理类。
 * 负责从磁盘读取或向磁盘写出腐烂规则配置 JSON 数据，并提供全局时间乘数的配置支持。
 */
public class DecayConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();
    private static final File CONFIG_FILE = new File("config/decaylib/decay.json");

    /**
     * 全局腐烂时间乘数。
     * 所有物品新产生的变质倒计时都会乘以该数值。默认值为 1.0，调大该值会延长保质期，调小会缩短保质期。
     */
    public static double globalDecayMultiplier = 1.0;

    /**
     * 从磁盘加载腐烂配置文件。
     * 若配置文件不存在，则根据当前 Mod 注册的默认值自动在磁盘上生成默认的配置文件。
     */
    public static void load() {
        if (!CONFIG_FILE.getParentFile()
            .exists()) {
            CONFIG_FILE.getParentFile()
                .mkdirs();
        }
        if (!CONFIG_FILE.exists()) {
            writeDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            DecayRuleList list = GSON.fromJson(reader, DecayRuleList.class);
            Map<Item, DecayRule> configRules = new HashMap<>();
            if (list != null) {
                globalDecayMultiplier = list.globalDecayMultiplier;
                if (list.rules != null) {
                    for (DecayRule rule : list.rules) {
                        Object obj = Item.itemRegistry.getObject(rule.input);
                        if (obj instanceof Item) {
                            Item item = (Item) obj;
                            configRules.put(item, rule);
                        } else {
                            DecayLib.LOG.warn("Failed to find item for decay rule: " + rule.input);
                        }
                    }
                }
            }
            DecayRegistry.applyConfigRules(configRules);
        } catch (Exception e) {
            DecayLib.LOG.error("Failed to load decaylib config", e);
        }
    }

    private static void writeDefaultConfig() {
        DecayRuleList list = new DecayRuleList();
        list.globalDecayMultiplier = globalDecayMultiplier;
        for (Map.Entry<Item, DecayRule> entry : DecayRegistry.getDefaultRules()
            .entrySet()) {
            list.rules.add(entry.getValue());
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(list, writer);
        } catch (Exception e) {
            DecayLib.LOG.error("Failed to write default decaylib config", e);
        }
    }

    private static class DecayRuleList {

        double globalDecayMultiplier = 1.0;
        List<DecayRule> rules = new ArrayList<>();
    }
}
