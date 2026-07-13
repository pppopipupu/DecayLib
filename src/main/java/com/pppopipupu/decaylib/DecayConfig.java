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

public class DecayConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();
    private static final File CONFIG_FILE = new File("config/decaylib/decay.json");

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
            if (list != null && list.rules != null) {
                for (DecayRule rule : list.rules) {
                    Item item = (Item) Item.itemRegistry.getObject(rule.input);
                    if (item != null) {
                        configRules.put(item, rule);
                    } else {
                        DecayLib.LOG.warn("Failed to find item for decay rule: " + rule.input);
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

        List<DecayRule> rules = new ArrayList<>();
    }
}
