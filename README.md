## 派拼是一个高效数据集成框架

## 主要特性有：

1. 每个任务分成吸收 转换 去重合并三个阶段，集成过程可回溯，发生错误可重试。

2. 支持拉和推两种方式集成。

3. 基于项目的管理方式，项目日志隔离，可以对每个任务进行单独管理。

4. 易于扩展，分别实现转换，合并逻辑，开发过程简单。

5. 采用全链路流式处理的技术，异步处理更高效。

6. 支持分布式任务调度。

7. 提供Restful接口方便集成。

8. 提供操作界面。


## 环境依赖：

运行pipin-core 需要mongodb

运行pipin-scheduler 需要zookeeper

可以根据需要自行选择




[开发文档 https://github.com/pidata/pipin/wiki](https://github.com/pidata/pipin/wiki)

[Restful接口文档 https://reference-pidata.doc.coding.io](https://reference-pidata.doc.coding.io)

[实例项目 https://github.com/pidata/pipin-examples](https://github.com/pidata/pipin-examples)


![缩略图](http://mi.lepiepie.com/images/upload/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202020-05-20%20%E4%B8%8A%E5%8D%885.26.45.png)

[更多缩略图](https://github.com/pidata/pipin/wiki/Snapshots)