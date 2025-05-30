节点：开关(On Off)
==

对输入的消息根据一定的条件判断是否通过或不通过，并且还支持高频度消息进行降频通过操作。此节点很适合用来做一些设备控制指令下达操作——既可以判断是否满足下达指令条件，也可以避免指令执行时间比系统判断时间长很多而造成短时间内多次下达指令。<font color="green">
此节点不会对消息进行新建或更改，以开关或阀门的方式进行工作。</font>

### 参数设置

双击可以打开节点参数设置对话框

#### 参考属性Property

此参数可以由消息msg、节点node变量或流程flow变量提供，作为条件判断基准参数。

#### 判断条件

1. 判断类型

判断类型：如比较、判空、判断是否存在、是否符合某种类型等

2. 被操作对象

根据条件判断类型，进而选择或输入相关的第二个或第三个输入参数，这些参数可以来自消息成员、节点变量和流程变量，也可以来自常量。

#### 输出最小时间间隔(毫秒)

这个参数设定大于0，表示如果两个消息之间的时间间隔小于此参数，就算满足条件，后面一个消息也不会被通过。


