节点：标签数据写入
==


根据选择的写入标签和赋值方式，对标签进行写入操作。由于标签写入可能涉及底层驱动和通信，会有一定的延迟，因此此节点还支持异步方式运行

除了获取消息内的某个数据作为标签写入值，其他情况下，节点对输入的消息不做任何分析处理，一切标签写入动作都由标签自己决定。节点运行结束之后，会直接输出输入的消息（异步情况下，输出消息会延迟）。

### 参数设置

双击可以打开节点参数设置对话框

#### 异步运行

勾选此项，节点内部的标签写入会以异步方式运行——也即是接收到输入消息之后，触发内部一个运行线程，这样可以避免写入时间过长影响前面节点的运行实时性。

<font color="red">注意：如果设置异步运行，则节点在运行过程中，会忽略所有输入的消息。</font>

#### 写入标签Tag列表参数

可以设置多个标签的写入，运行时按照顺序执行，每个标签写入设置有如下参数内容

1. 延迟(毫秒)

如果此延迟设置大于0,则对应这条标签设置写入前会延迟对应的毫秒数。这个对于一些控制场合很有用：如下达一个开关按钮指令，先写标签值1，等待几秒钟之后再对相同的标签写入0，这样就可以模拟一个按钮控制过程。

2. 标签Tag

点击标签输入框，可以弹出标签选择对话框，选择确定即可。

#### 写入值

根据值获取类型，进而选择或输入相关的第二个。这些参数可以来自消息成员、节点变量和流程变量，也可以来自常量。



