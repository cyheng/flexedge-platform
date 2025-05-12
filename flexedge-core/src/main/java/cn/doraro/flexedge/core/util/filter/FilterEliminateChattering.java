package cn.doraro.flexedge.core.util.filter;

/**
 * 消抖滤波法
 * <p>
 * A、方法：
 * <p>
 * 设置一个滤波计数器
 * <p>
 * 将每次采样值与当前有效值比较：
 * <p>
 * 如果采样值＝当前有效值，则计数器清零
 * <p>
 * 如果采样值<>当前有效值，则计数器+1，并判断计数器是否>=上限N(溢出)
 * <p>
 * 如果计数器溢出,则将本次值替换当前有效值,并清计数器
 * <p>
 * B、优点：
 * <p>
 * 对于变化缓慢的被测参数有较好的滤波效果,
 * <p>
 * 可避免在临界值附近控制器的反复开/关跳动或显示器上数值抖动
 * <p>
 * C、缺点：
 * <p>
 * 对于快速变化的参数不宜
 * <p>
 * 如果在计数器溢出的那一次采样到的值恰好是干扰值,则会将干扰值当作有效值导入系
 * <p>
 * 统
 * <p>
 * 9、消抖滤波法
 * <p>
 * #define N 12
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char count=0;
 * <p>
 * char new_value;
 * <p>
 * new_value = get_ad();
 * <p>
 * while (value !=new_value);
 * <p>
 * {
 * <p>
 * count++;
 * <p>
 * if (count>=N)   return new_value;
 * <p>
 * delay();
 * <p>
 * value = get_ad();
 * <p>
 * }
 * <p>
 * return value;
 * }
 *
 * @author zzj
 */
public class FilterEliminateChattering {

}
