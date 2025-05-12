package cn.doraro.flexedge.core.util.filter;

/**
 * 一阶滞后滤波法
 * <p>
 * A、方法：
 * <p>
 * 取a=0~1
 * <p>
 * 本次滤波结果=（1-a）*本次采样值+a*上次滤波结果
 * <p>
 * B、优点：
 * <p>
 * 对周期性干扰具有良好的抑制作用
 * <p>
 * 适用于波动频率较高的场合
 * <p>
 * C、缺点：
 * <p>
 * 相位滞后，灵敏度低
 * <p>
 * 滞后程度取决于a值大小
 * <p>
 * 不能消除滤波频率高于采样频率的1/2的干扰信号
 * <p>
 * 为加快程序处理速度假定基数为100，a=0~100
 * <p>
 * #define a 50
 * <p>
 * char value;
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char  new_value;
 * <p>
 * new_value = get_ad();
 * <p>
 * return (100-a)*value + a*new_value;
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterFirstOrderLag {

}
