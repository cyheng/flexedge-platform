数据存储、处理和展示
==

## 1 标签数据简单存储和输出

IOT-Tree支持对外部数据库进行标签数据的配置输出。你如果希望对接IOT-Tree的配置标签数据，那么使用中间数据库表进行数据获取也是一个不错的选择。你只需要在部署的IOT-Tree中配置外部数据源，然后通过简单的配置，就可以使IOT-Tree项目在运行过程中，定时的输入标签数据到你的数据库中。

你接着可以在你的顶层业务系统中，方便的使用这些数据。

详细信息请参考[对应链接][store]。

## 2 内部时序段记录器(TSS)

为了满足更多的需要，从1.3版本开始，IOT-Tree内部基于SQLite实现了一个标签数据高速记录器。

由于标签数据变化基于时间序列，联系记录值不变时，不会新增记录，所以称为时序端记录器(TSS Recorder)。

你只需要在标签上通过简单的配置，就可以让IOT-Tree项目在运行过程中，自动为您记录所有的采集变化值。由于内部使用SQLite，你不需要专门使用特定的数据库就可以满足绝大多数应用场合。

详细信息请参考[对应链接][tss]。

有了这个基础，IOT-Tree还专门为数据处理设计了一个架构，可以很方便的对这些数据进行二次加工。

## 3 记录数据二次处理

以上面的"TSS Recorder"为基础，IOT-Tree定义了一个数据分析处理框架。基于这个框架，系统可以统一管理所有的数据处理对象和每个处理对象输出的数据内容。并且以此为基础，定义输出数据结果内容所需要的展示UI。

考虑到数据如何具体展示使用，这是每个使用者都会有自己特殊要求的

详细信息请参考[对应链接][rec]。

[store]:./store.md

[tss]:./inner_tssdb.md

[rec]:./inner_rec.md
