##派拼是一个跨平台数据集成框架

##主要特性有：

1. 每个任务分成吸收 转换 去重合并三个阶段，集成过程可回溯，发生错误可重试。

2. 基于项目的管理方式，项目日志隔离，可以对每个任务进行单独管理。

3. 易于扩展，分别实现转换，合并逻辑，开发过程简单。

4. 采用流式处理的技术，异步处理更高效。

5. 支持分布式任务调度。

6. TODO: 提供操作界面。


环境依赖：

运行pipin-core 需要mongodb

运行pipin-scheduler 需要zookeeper

可以根据需要自行选择
