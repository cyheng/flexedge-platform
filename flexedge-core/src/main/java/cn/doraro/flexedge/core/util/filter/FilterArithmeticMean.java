package cn.doraro.flexedge.core.util.filter;

/**
 * 算术平均滤波法
 * <p>
 * A、方法：
 * <p>
 * 连续取N个采样值进行算术平均运算
 * <p>
 * N值较大时：信号平滑度较高，但灵敏度较低
 * <p>
 * N值较小时：信号平滑度较低，但灵敏度较高
 * <p>
 * N值的选取：一般流量，N=12；压力：N=4
 * <p>
 * B、优点：
 * <p>
 * 适用于对一般具有随机干扰的信号进行滤波
 * <p>
 * 这样信号的特点是有一个平均值，信号在某一数值范围附近上下波动
 * <p>
 * C、缺点：
 * <p>
 * 对于测量速度较慢或要求数据计算速度较快的实时控制不适用
 * <p>
 * 比较浪费RAM
 * <p>
 * 3、算术平均滤波法
 * <p>
 * <p>
 * <p>
 * #define N 12
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * int  sum = 0;
 * <p>
 * for ( count=0;count<N;count++)
 * <p>
 * {
 * <p>
 * sum + = get_ad();
 * <p>
 * delay();
 * <p>
 * }
 * <p>
 * return (char)(sum/N);
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterArithmeticMean {

}
