/**
 * Mode1
 * <p>
 * support master / standby redundant running
 * <p>
 * 1）master run one project,and set it as master
 * 2)  redundant machine start
 * <p>
 * Mode2  Cluster Mode   master / slave
 * 1) 云端可以部署任意多个项目实例，可以基于用户权限分配节点。主节点可以管理所有的从节点
 * 2）从节点可以被调度装载特定项目，并运行——可以支持大规模的现场同类设备的接入
 */
package cn.doraro.flexedge.core.station;