# 设计思路

## 数据、设备、驱动、通道之间的关系

### 模拟器

#### tag数据模拟

单个数据模拟，如正弦、方波、三角波曲线等
--通过模拟设备——模拟

*kepserver* 使用模拟函数实现

模拟通道

## 虚拟设备支持

iottree可以基于内部已经存在的数据配置，建立一些虚拟设备，如modbus设备等。这些设备可以对外提供标准的输出数据格式。可以通过网络，被顶层实际的设备或上位软件进行调用。
如，某个plc希望获取iottree设备中的一些数据，进行相关的控制逻辑。plc

## Support Virtual Device

iottree can create some virtual device based on inner data tree. e.g it can virtual a modbus device.Then,through
network,these virtual devices can be accessed by some other software or acutal devices.
you can use a PLC access virtual modbus device,and implements special control logic.


