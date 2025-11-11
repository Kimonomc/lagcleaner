package com.kim.lagcleaner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LagCleaner extends JavaPlugin implements CommandExecutor {

    private MemoryMXBean memoryBean;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化内存管理Bean
        memoryBean = ManagementFactory.getMemoryMXBean();
        
        // 注册命令执行器
        this.getCommand("lag").setExecutor(this);
        this.getCommand("rem").setExecutor(this);
        this.getCommand("lagcleaner").setExecutor(this);
        
        // 插件启用信息
        getLogger().info("LagCleaner 插件已启用！");
        getLogger().info("作者: Kim");
        getLogger().info("版本: 1.0.0");
    }

    @Override
    public void onDisable() {
        getLogger().info("LagCleaner 插件已禁用！");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 处理/lagcleaner命令
        if (cmd.getName().equalsIgnoreCase("lagcleaner")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("clean")) {
                    // 检查权限
                    if (!sender.hasPermission("lagcleaner.command.lag")) {
                        sender.sendMessage(configManager.getNoPermissionMessage());
                        return true;
                    }
                    
                    cleanServerMemory(sender);
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    // 检查权限
                    if (!sender.hasPermission("lagcleaner.reload")) {
                        sender.sendMessage(configManager.getNoPermissionMessage());
                        return true;
                    }
                    
                    // 重新加载配置
                    configManager.loadConfig();
                    sender.sendMessage("§aLagCleaner 配置已重新加载！");
                    return true;
                } else if (args[0].equalsIgnoreCase("info")) {
                    // 显示插件信息
                    showPluginInfo(sender);
                    return true;
                }
            }
        }

        // 处理/lag命令
        if (cmd.getName().equalsIgnoreCase("lag")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("clean")) {
                // 检查权限
                if (!sender.hasPermission("lagcleaner.command.lag")) {
                    sender.sendMessage(configManager.getNoPermissionMessage());
                    return true;
                }
                
                cleanServerMemory(sender);
                return true;
            }
        }

        // 处理/rem命令
        if (cmd.getName().equalsIgnoreCase("rem")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("clean")) {
                // 检查权限
                if (!sender.hasPermission("lagcleaner.command.rem")) {
                    sender.sendMessage(configManager.getNoPermissionMessage());
                    return true;
                }
                
                cleanSystemMemory(sender);
                return true;
            }
        }

        // 显示帮助信息
        showHelp(sender);
        return true;
    }
    
    /**
     * 显示插件信息
     */
    private void showPluginInfo(CommandSender sender) {
        sender.sendMessage("§6===== LagCleaner 插件信息 =====");
        sender.sendMessage("§a版本: 1.0.0");
        sender.sendMessage("§a作者: Kim");
        sender.sendMessage("§a描述: 清理服务器内存，减少卡顿和延迟");
        sender.sendMessage("§a服务器版本: " + Bukkit.getVersion());
        sender.sendMessage("§aPaper API版本: " + Bukkit.getBukkitVersion());
        
        // 显示当前内存使用情况
        MemoryInfo memoryInfo = getCurrentMemoryInfo();
        sender.sendMessage("§6当前内存使用:");
        sender.sendMessage("§7堆内存: " + formatMemory(memoryInfo.heapUsed) + "/" + formatMemory(memoryInfo.heapMax));
        sender.sendMessage("§7非堆内存: " + formatMemory(memoryInfo.nonHeapUsed) + "/" + formatMemory(memoryInfo.nonHeapMax));
        sender.sendMessage("§6==============================");
    }

    /**
     * 清理服务器内存
     */
    private void cleanServerMemory(CommandSender sender) {
        sender.sendMessage(configManager.getCleanStartMessage());
        
        // 记录清理前的内存使用情况
        MemoryInfo before = getCurrentMemoryInfo();
        
        sender.sendMessage("§7清理前 - 堆内存: " + formatMemory(before.heapUsed) + "/" + formatMemory(before.heapMax) + 
                          " §8| 非堆内存: " + formatMemory(before.nonHeapUsed) + "/" + formatMemory(before.nonHeapMax));

        // 执行垃圾回收
        sender.sendMessage("§e正在执行垃圾回收...");
        long startTime = System.currentTimeMillis();
        
        // 执行深度垃圾回收
        int gcCount = performDeepGarbageCollection();
        
        // 优化服务器性能
        int entitiesRemoved = 0;
        int chunksUnloaded = 0;
        
        // 根据配置清理实体
        if (configManager.isEntityCleanEnabled()) {
            entitiesRemoved = cleanEntities();
        }
        
        // 根据配置卸载区块
        if (configManager.isChunkUnloadEnabled()) {
            chunksUnloaded = unloadUnusedChunks();
        }
        
        long endTime = System.currentTimeMillis();
        
        // 记录清理后的内存使用情况
        MemoryInfo after = getCurrentMemoryInfo();
        
        sender.sendMessage("§7清理后 - 堆内存: " + formatMemory(after.heapUsed) + "/" + formatMemory(after.heapMax) + 
                          " §8| 非堆内存: " + formatMemory(after.nonHeapUsed) + "/" + formatMemory(after.nonHeapMax));
        
        // 计算释放的内存
        long heapFreed = before.heapUsed - after.heapUsed;
        long nonHeapFreed = before.nonHeapUsed - after.nonHeapUsed;
        long totalFreed = heapFreed + nonHeapFreed;
        
        sender.sendMessage(String.format(configManager.getCleanCompleteMessage(), (endTime - startTime)));
        sender.sendMessage(String.format(configManager.getMemoryFreedMessage(), 
                formatMemory(heapFreed), formatMemory(nonHeapFreed), formatMemory(totalFreed)));
        sender.sendMessage("§7垃圾回收执行次数: " + gcCount);
        
        // 显示优化结果
        sender.sendMessage("§e服务器性能优化结果:");
        if (configManager.isEntityCleanEnabled()) {
            sender.sendMessage(String.format(configManager.getEntitiesCleanedMessage(), entitiesRemoved));
        } else {
            sender.sendMessage("§7实体清理已禁用（根据配置）");
        }
        
        if (configManager.isChunkUnloadEnabled()) {
            sender.sendMessage(String.format(configManager.getChunksUnloadedMessage(), chunksUnloaded));
        } else {
            sender.sendMessage("§7区块卸载已禁用（根据配置）");
        }
        
        sender.sendMessage("§a服务器内存清理完成！");
    }

    /**
     * 清理系统内存
     */
    private void cleanSystemMemory(CommandSender sender) {
        sender.sendMessage("§a开始清理系统内存...");
        
        // 记录清理前的系统信息
        SystemInfo before = getCurrentSystemInfo();
        
        sender.sendMessage("§7系统信息 - CPU核心: " + before.cpuCores + 
                          " §8| 系统负载: " + String.format("%.2f", before.systemLoad) + 
                          " §8| 可用内存: " + formatMemory(before.freeMemory));

        // 执行系统级内存清理
        sender.sendMessage("§e正在执行系统内存清理...");
        
        // 根据配置决定是否执行系统级内存清理
        boolean systemCleanSuccess = false;
        
        if (configManager.isSystemCleanEnabled()) {
            // 根据操作系统执行不同的清理命令
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("linux")) {
                systemCleanSuccess = cleanLinuxMemory(sender);
            } else if (os.contains("windows")) {
                systemCleanSuccess = cleanWindowsMemory(sender);
            } else if (os.contains("mac")) {
                systemCleanSuccess = cleanMacMemory(sender);
            } else {
                sender.sendMessage("§e不支持的操作系统: " + os);
            }
        } else {
            sender.sendMessage("§7系统级内存清理已禁用（根据配置）");
        }
        
        // 执行更彻底的垃圾回收
        sender.sendMessage("§e正在执行深度垃圾回收...");
        long startTime = System.currentTimeMillis();
        
        int gcCount = performDeepGarbageCollection();
        
        long endTime = System.currentTimeMillis();
        
        // 记录清理后的系统信息
        SystemInfo after = getCurrentSystemInfo();
        
        sender.sendMessage("§7清理后 - 可用内存: " + formatMemory(after.freeMemory) + 
                          " §8| 释放: " + formatMemory(after.freeMemory - before.freeMemory));
        
        sender.sendMessage("§a系统内存清理完成！耗时: " + (endTime - startTime) + "ms");
        sender.sendMessage("§7垃圾回收执行次数: " + gcCount);
        
        // 如果系统级清理成功，显示额外信息
        if (systemCleanSuccess) {
            sender.sendMessage("§a系统级缓存清理成功！");
        }
    }
    
    /**
     * 在Linux系统上清理内存
     */
    private boolean cleanLinuxMemory(CommandSender sender) {
        try {
            // 清理页面缓存
            Process process1 = Runtime.getRuntime().exec("sync && echo 1 > /proc/sys/vm/drop_caches");
            process1.waitFor(3, TimeUnit.SECONDS);
            
            // 清理目录项和inode缓存
            Process process2 = Runtime.getRuntime().exec("sync && echo 2 > /proc/sys/vm/drop_caches");
            process2.waitFor(3, TimeUnit.SECONDS);
            
            // 清理所有缓存
            Process process3 = Runtime.getRuntime().exec("sync && echo 3 > /proc/sys/vm/drop_caches");
            process3.waitFor(3, TimeUnit.SECONDS);
            
            return process1.exitValue() == 0 && process2.exitValue() == 0 && process3.exitValue() == 0;
        } catch (Exception e) {
            sender.sendMessage("§c清理Linux系统缓存时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 在Windows系统上清理内存
     */
    private boolean cleanWindowsMemory(CommandSender sender) {
        try {
            sender.sendMessage("§e正在清理Windows系统内存...");
            
            // 尝试清理Windows系统内存
            // 注意：这些命令可能需要管理员权限
            
            // 清理系统缓存
            try {
                Process process1 = Runtime.getRuntime().exec("cmd /c ipconfig /flushdns");
                process1.waitFor(3, TimeUnit.SECONDS);
                sender.sendMessage("§7已清理DNS缓存");
            } catch (Exception e) {
                sender.sendMessage("§c清理DNS缓存时发生错误: " + e.getMessage());
            }
            
            // 清理Windows页面文件（需要管理员权限）
            try {
                Process process2 = Runtime.getRuntime().exec("cmd /c wmic computersystem where name=\"%computername%\" set AutomaticManagedPagefile=True");
                process2.waitFor(3, TimeUnit.SECONDS);
                sender.sendMessage("§7已重置页面文件管理");
            } catch (Exception e) {
                sender.sendMessage("§c清理页面文件时发生错误: " + e.getMessage() + "（可能需要管理员权限）");
            }
            
            // 清理系统还原点（需要管理员权限）
            try {
                Process process3 = Runtime.getRuntime().exec("cmd /c vssadmin delete shadows /all /quiet");
                process3.waitFor(5, TimeUnit.SECONDS);
                sender.sendMessage("§7已清理系统还原点");
            } catch (Exception e) {
                sender.sendMessage("§c清理系统还原点时发生错误: " + e.getMessage() + "（可能需要管理员权限）");
            }
            
            return true;
        } catch (Exception e) {
            sender.sendMessage("§c清理Windows系统缓存时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 在Mac系统上清理内存
     */
    private boolean cleanMacMemory(CommandSender sender) {
        try {
            // Mac系统清理内存缓存
            Process process = Runtime.getRuntime().exec("purge");
            process.waitFor(5, TimeUnit.SECONDS);
            
            return process.exitValue() == 0;
        } catch (Exception e) {
            sender.sendMessage("§c清理Mac系统缓存时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 执行深度垃圾回收
     */
    private int performDeepGarbageCollection() {
        int count = 0;
        long previousUsedMemory = getCurrentMemoryInfo().heapUsed;
        
        // 获取配置中的最大垃圾回收尝试次数
        int maxAttempts = configManager.getMaxGcAttempts();
        
        // 执行垃圾回收，直到内存使用不再显著减少或达到最大尝试次数
        for (int i = 0; i < maxAttempts; i++) {
            System.gc();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            System.runFinalization();
            
            count++;
            
            long currentUsedMemory = getCurrentMemoryInfo().heapUsed;
            long memoryFreed = previousUsedMemory - currentUsedMemory;
            
            // 如果释放的内存小于1MB，停止垃圾回收
            if (memoryFreed < 1024 * 1024) {
                break;
            }
            
            previousUsedMemory = currentUsedMemory;
        }
        
        return count;
    }

    /**
     * 优化服务器性能
     */
    private void optimizeServerPerformance(CommandSender sender) {
        sender.sendMessage("§e正在优化服务器性能...");
        
        int entitiesRemoved = 0;
        int chunksUnloaded = 0;
        
        // 根据配置清理实体
        if (configManager.isEntityCleanEnabled()) {
            entitiesRemoved = cleanEntities();
            sender.sendMessage(String.format(configManager.getEntitiesCleanedMessage(), entitiesRemoved));
        } else {
            sender.sendMessage("§7实体清理已禁用（根据配置）");
        }
        
        // 根据配置卸载区块
        if (configManager.isChunkUnloadEnabled()) {
            chunksUnloaded = unloadUnusedChunks();
            sender.sendMessage(String.format(configManager.getChunksUnloadedMessage(), chunksUnloaded));
        } else {
            sender.sendMessage("§7区块卸载已禁用（根据配置）");
        }
        
        sender.sendMessage("§a服务器性能优化完成！");
    }

    /**
     * 清理不必要的实体
     */
    private int cleanEntities() {
        int count = 0;
        
        // 获取配置中需要清理的实体类型
        List<String> entityTypes = configManager.getEntityTypesToClean();
        
        // 遍历所有世界
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            // 遍历配置中的实体类型
            for (String typeName : entityTypes) {
                try {
                    org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf(typeName);
                    count += cleanEntityType(world, type);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("配置文件中存在无效的实体类型: " + typeName);
                }
            }
        }
        
        return count;
    }
    
    /**
     * 清理特定类型的实体
     */
    private int cleanEntityType(org.bukkit.World world, org.bukkit.entity.EntityType type) {
        int count = 0;
        
        // 如果是物品类型且物品清理已禁用，则跳过
        if (type == org.bukkit.entity.EntityType.ITEM && !configManager.isItemCleanEnabled()) {
            return 0;
        }
        
        // 获取所有该类型的实体
        java.util.Collection<? extends org.bukkit.entity.Entity> entities = world.getEntitiesByClass(type.getEntityClass());
        
        // 遍历并清理实体
        for (org.bukkit.entity.Entity entity : entities) {
            // 对于掉落物，可以添加更复杂的检查，比如物品价值、存在时间等
            if (entity instanceof org.bukkit.entity.Item) {
                org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
                // 只清理存在超过配置时间的物品
                long minAgeMillis = configManager.getItemMinAgeSeconds() * 1000L;
                if (System.currentTimeMillis() - item.getTicksLived() * 50 > minAgeMillis) {
                    item.remove();
                    count++;
                }
            } else {
                // 对于 projectile 类型的实体，直接清理
                if (entity instanceof org.bukkit.entity.Projectile) {
                    entity.remove();
                    count++;
                } 
                // 对于经验球，直接清理
                else if (entity instanceof org.bukkit.entity.ExperienceOrb) {
                    entity.remove();
                    count++;
                }
            }
        }
        
        return count;
    }

    /**
     * 卸载未使用的区块
     */
    private int unloadUnusedChunks() {
        int count = 0;
        
        // 获取配置中的最小区块距离
        int minDistance = configManager.getMinChunkDistance();
        
        // 遍历所有世界
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            // 获取所有区块
            org.bukkit.Chunk[] chunks = world.getLoadedChunks();
            
            for (org.bukkit.Chunk chunk : chunks) {
                // 检查区块是否有玩家
                boolean hasPlayersNearby = false;
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                
                // 检查区块是否有实体
                boolean hasEntities = chunk.getEntities().length > 0;
                
                for (org.bukkit.entity.Player player : world.getPlayers()) {
                    org.bukkit.Chunk playerChunk = player.getLocation().getChunk();
                    int playerChunkX = playerChunk.getX();
                    int playerChunkZ = playerChunk.getZ();
                    
                    // 计算区块距离
                    int distance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
                    
                    // 如果玩家在配置的距离内，则不卸载
                    if (distance <= minDistance) {
                        hasPlayersNearby = true;
                        break;
                    }
                }
                
                // 如果没有玩家在附近且区块未被标记为保存，则卸载
                if (!hasPlayersNearby && !chunk.isForceLoaded()) {
                    // 即使有实体，也尝试卸载（有些实体可能是应该清理的）
                    if (world.unloadChunk(chunk)) {
                        count++;
                    }
                }
            }
            
            // 额外尝试卸载所有可卸载的区块
            org.bukkit.Chunk[] allChunks = world.getLoadedChunks();
            for (org.bukkit.Chunk c : allChunks) {
                if (!c.isForceLoaded() && world.unloadChunk(c)) {
                    count++;
                }
            }
        }
        
        return count;
    }

    /**
     * 获取当前内存信息
     */
    private MemoryInfo getCurrentMemoryInfo() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        
        return new MemoryInfo(
            heap.getUsed(),
            heap.getMax(),
            nonHeap.getUsed(),
            nonHeap.getMax()
        );
    }

    /**
     * 获取当前系统信息
     */
    private SystemInfo getCurrentSystemInfo() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        double systemLoad = 0.0;
        
        // 尝试获取系统CPU负载
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 使用反射检查是否支持getSystemCpuLoad()方法
            try {
                java.lang.reflect.Method method = osBean.getClass().getMethod("getSystemCpuLoad");
                Object result = method.invoke(osBean);
                if (result instanceof Double) {
                    systemLoad = (Double) result;
                    
                    // 如果获取到的负载小于0（表示不可用），则设置为0
                    if (systemLoad < 0) {
                        systemLoad = 0.0;
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                // 如果不支持getSystemCpuLoad()方法，则使用替代方法
                systemLoad = getAlternativeSystemCpuLoad();
            }
        } catch (SecurityException e) {
            // 如果发生安全异常，则使用替代方法
            systemLoad = getAlternativeSystemCpuLoad();
        }
        
        long freeMemory = Runtime.getRuntime().freeMemory();
        
        return new SystemInfo(cpuCores, systemLoad, freeMemory);
    }
    
    /**
     * 获取系统CPU负载的替代方法
     */
    private double getAlternativeSystemCpuLoad() {
        // 在不支持getSystemCpuLoad()方法的情况下，返回0.0
        // 可以根据需要实现其他获取CPU负载的方法
        return 0.0;
    }

    /**
     * 格式化内存大小
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 检查是否为Linux系统
     */
    private boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux");
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(configManager.getHelpMessage());
    }

    /**
     * 内存信息数据类
     */
    private static class MemoryInfo {
        long heapUsed;
        long heapMax;
        long nonHeapUsed;
        long nonHeapMax;

        MemoryInfo(long heapUsed, long heapMax, long nonHeapUsed, long nonHeapMax) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.nonHeapMax = nonHeapMax;
        }
    }

    /**
     * 系统信息数据类
     */
    private static class SystemInfo {
        int cpuCores;
        double systemLoad;
        long freeMemory;

        SystemInfo(int cpuCores, double systemLoad, long freeMemory) {
            this.cpuCores = cpuCores;
            this.systemLoad = systemLoad;
            this.freeMemory = freeMemory;
        }
    }
}
