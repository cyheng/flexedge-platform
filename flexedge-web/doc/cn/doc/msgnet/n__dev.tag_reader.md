节点：标签数据读取
==

根据选择的标签和变量命名，读取当前运行项目中的标签值，形成JSON数据输出

此节点对输入的消息不做任何分析处理，消息仅仅是触发节点运行的作用。

读取的某个标签项，还可以设置是否"必须有效"，在勾选的情况下，如果对于的标签数据是无效的，那么消息就不会正常输出。

节点有两个输出通道，第一个是正常获取数据输出，第二个是发现某个"必须有效"的数据项出现无效值，则从这个通道输出无效的标签列表。

### 参数设置

双击可以打开节点参数设置对话框

参数设置可以包含多个标签-变量的映射，最终输出时形成一个JSON对象，内部成员是变量：标签值内容。每个设置项有如下内容：

#### 标签Tag

点击标签输入框，可以弹出标签选择对话框，选择确定即可。

#### 变量

此标签值对应的变量名称，会成为输出JSON对象的成员属性名称。

#### 必须有效

你如果勾选此选项，节点读取到的对应标签值如果无效，则认为整个输出都无效。因此只会在节点第二个输出通道输出无效的标签列表。如果此项没有被勾选，并且对于的标签值无效，则正常输出时，JSON对象不包含这个数据成员。



