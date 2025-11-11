# LagCleaner

一个Minecraft Paper服务器插件，用于清理内存和减少服务器卡顿。

## 功能特点

- **内存清理**：清理服务器内存，释放不必要的占用
- **系统优化**：释放系统级内存资源
- **实体清理**：智能清理不必要的实体（掉落物、箭矢等）
- **区块卸载**：卸载未使用的区块，减少内存占用
- **配置灵活**：通过配置文件自定义清理行为
- **多平台支持**：支持Linux、Windows和Mac系统

## 命令

### 基本命令

- `/lag clean` - 清理服务器内存
- `/rem clean` - 清理系统内存
- `/lagcleaner reload` - 重新加载配置文件
- `/lagcleaner info` - 显示插件信息

### 权限

- `lagcleaner.use` - 使用所有命令（默认OP）
- `lagcleaner.command.lag` - 使用/lag命令
- `lagcleaner.command.rem` - 使用/rem命令
- `lagcleaner.reload` - 使用/reload命令

## 配置文件

配置文件位于 `plugins/LagCleaner/config.yml`，可以自定义以下设置：

- 物品清理设置
- 实体清理设置
- 区块卸载设置
- 系统清理设置
- 消息设置

## 安装

### 方法1：使用已编译的JAR文件

1. 下载最新版本的LagCleaner.jar
2. 将JAR文件放入服务器的plugins文件夹
3. 重启服务器
4. 插件会自动生成配置文件

### 方法2：从源代码构建

1. 安装Java 17或更高版本
2. 安装Maven
3. 克隆或下载源代码
4. 运行 `mvn clean package` 或使用提供的构建脚本
5. 在target文件夹中找到生成的JAR文件

## 构建脚本

### Windows

运行 `build.bat` 文件，该脚本会自动检查Java和Maven是否安装，并构建项目。

### Linux/Mac

运行 `./build.sh` 文件（如果提供），或手动执行 `mvn clean package`。

## 支持的服务器版本

- Minecraft 1.21.8 Paper
- 可能兼容其他Paper/Folia基于1.21.x的版本

## 注意事项

- 系统级内存清理功能在Linux系统上效果最佳
- 使用系统级清理功能可能需要root权限
- 建议定期备份服务器数据
- 插件需要Java 17或更高版本

## 常见问题

### Q: 插件无法加载怎么办？

A: 请检查以下几点：
1. 服务器是否使用Paper或兼容的服务器软件
2. 服务器版本是否为1.21.x
3. 是否安装了Java 17或更高版本
4. 查看服务器日志，寻找错误信息

### Q: 清理命令没有效果怎么办？

A: 请检查以下几点：
1. 您是否有使用命令的权限
2. 配置文件中的清理功能是否已启用
3. 查看服务器日志，寻找错误信息

### Q: 编译时出现"找不到符号: 方法 getSystemCpuLoad()"错误怎么办？

A: 这是因为您的Java版本或环境不支持getSystemCpuLoad()方法。插件已经使用反射机制来处理这个问题，确保在不支持该方法的环境中也能正常编译和运行。如果仍然遇到问题，请尝试：
1. 使用Java 17或更高版本
2. 检查您的Java环境配置
3. 忽略该错误，插件会自动使用替代方法

## 作者

- Kim

## 版本历史

- 1.0.0 - 初始版本

## 许可证

MIT License
