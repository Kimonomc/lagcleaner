package com.kim.lagcleaner;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 如果配置文件不存在，创建默认配置
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        // 加载配置文件
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取配置文件
     */
    public FileConfiguration getConfig() {
        return config;
    }

    // ===== 清理设置 =====

    /**
     * 是否启用物品清理
     */
    public boolean isItemCleanEnabled() {
        return config.getBoolean("clean.items.enabled", true);
    }

    /**
     * 获取物品最小存在时间（秒）
     */
    public int getItemMinAgeSeconds() {
        return config.getInt("clean.items.min-age-seconds", 30);
    }

    /**
     * 是否启用实体清理
     */
    public boolean isEntityCleanEnabled() {
        return config.getBoolean("clean.entities.enabled", true);
    }

    /**
     * 获取需要清理的实体类型列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getEntityTypesToClean() {
        return config.getStringList("clean.entities.types");
    }

    /**
     * 是否启用区块卸载
     */
    public boolean isChunkUnloadEnabled() {
        return config.getBoolean("clean.chunks.enabled", true);
    }

    /**
     * 获取最小区块距离
     */
    public int getMinChunkDistance() {
        return config.getInt("clean.chunks.min-distance", 10);
    }

    // ===== 系统清理设置 =====

    /**
     * 是否启用系统级内存清理
     */
    public boolean isSystemCleanEnabled() {
        return config.getBoolean("system.enabled", true);
    }

    /**
     * 获取最大垃圾回收尝试次数
     */
    public int getMaxGcAttempts() {
        return config.getInt("system.max-gc-attempts", 10);
    }

    // ===== 消息设置 =====

    /**
     * 获取清理开始消息
     */
    public String getCleanStartMessage() {
        return config.getString("messages.clean-start", "§a开始清理服务器内存...");
    }

    /**
     * 获取清理完成消息
     */
    public String getCleanCompleteMessage() {
        return config.getString("messages.clean-complete", "§a内存清理完成！耗时: %dms");
    }

    /**
     * 获取内存释放消息
     */
    public String getMemoryFreedMessage() {
        return config.getString("messages.memory-freed", "§a释放内存 - 堆: %s §8| 非堆: %s §8| 总计: %s");
    }

    /**
     * 获取实体清理消息
     */
    public String getEntitiesCleanedMessage() {
        return config.getString("messages.entities-cleaned", "§7清理实体: %d 个");
    }

    /**
     * 获取区块卸载消息
     */
    public String getChunksUnloadedMessage() {
        return config.getString("messages.chunks-unloaded", "§7卸载区块: %d 个");
    }

    /**
     * 获取权限不足消息
     */
    public String getNoPermissionMessage() {
        return config.getString("messages.no-permission", "§c你没有使用此命令的权限！");
    }

    /**
     * 获取帮助消息
     */
    public String getHelpMessage() {
        return config.getString("messages.help", "§6===== LagCleaner 插件帮助 =====\n§a/lag clean §7- 清理服务器内存\n§a/rem clean §7- 清理系统内存\n§6==============================");
    }
}
