package cn.doraro.flexedge.core.util.filter;

/**
 * 限幅消抖滤波法
 * <p>
 * A、方法：
 * <p>
 * 相当于“限幅滤波法”+“消抖滤波法”
 * <p>
 * 先限幅,后消抖
 * <p>
 * B、优点：
 * <p>
 * 继承了“限幅”和“消抖”的优点
 * <p>
 * 改进了“消抖滤波法”中的某些缺陷,避免将干扰值导入系统
 * <p>
 * C、缺点：
 * <p>
 * 对于快速变化的参数不宜
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterEliminateChatteringLimit {

}
