package cn.doraro.flexedge.core.util.filter;

/**
 * 递推平均滤波法（又称滑动平均滤波法）
 * <p>
 * A、方法：
 * <p>
 * 把连续取N个采样值看成一个队列
 * <p>
 * 队列的长度固定为N
 * <p>
 * 每次采样到一个新数据放入队尾,并扔掉原来队首的一次数据.(先进先出原则)
 * <p>
 * 把队列中的N个数据进行算术平均运算,就可获得新的滤波结果
 * <p>
 * N值的选取：流量，N=12；压力：N=4；液面，N=4~12；温度，N=1~4
 * <p>
 * B、优点：
 * <p>
 * 对周期性干扰有良好的抑制作用，平滑度高
 * <p>
 * 适用于高频振荡的系统
 * <p>
 * C、缺点：
 * <p>
 * 灵敏度低
 * <p>
 * 对偶然出现的脉冲性干扰的抑制作用较差
 * <p>
 * 不易消除由于脉冲干扰所引起的采样值偏差
 * <p>
 * 不适用于脉冲干扰比较严重的场合
 * <p>
 * 比较浪费RAM
 * 4、递推平均滤波法（又称滑动平均滤波法）
 * <p>
 * <p>
 * <p>
 * #define N 12
 * <p>
 * char value_buf[N];
 * <p>
 * char i=0;
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char count;
 * <p>
 * int  sum=0;
 * <p>
 * value_buf[i++] = get_ad();
 * <p>
 * if ( i == N )   i = 0;
 * <p>
 * for ( count=0;count<N,count++)
 * <p>
 * sum = value_buf[count];
 * <p>
 * return (char)(sum/N);
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterRecursiveAve {

}