package com.volmit.iris.core.link;

import com.volmit.iris.engine.framework.Engine;
import com.volmit.iris.util.collection.KMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.MissingResourceException;

@RequiredArgsConstructor
public abstract class ExternalDataProvider {

    @Getter
    private final String pluginId;

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(pluginId);
    }

    public boolean isReady() {
        return getPlugin() != null && getPlugin().isEnabled();
    }

    public abstract void init();

    public BlockData getBlockData(Identifier blockId) throws MissingResourceException {
        return getBlockData(blockId, new KMap<>());
    }

    public abstract BlockData getBlockData(Identifier blockId, KMap<String, String> state) throws MissingResourceException;

    public ItemStack getItemStack(Identifier itemId) throws MissingResourceException {
        return getItemStack(itemId, new KMap<>());
    }

    public abstract ItemStack getItemStack(Identifier itemId, KMap<String, Object> customNbt) throws MissingResourceException;

    public void processUpdate(Engine engine, Block block, Identifier blockId) {}

    public abstract Identifier[] getBlockTypes();

    public abstract Identifier[] getItemTypes();

    public abstract boolean isValidProvider(Identifier id, boolean isItem);
}
