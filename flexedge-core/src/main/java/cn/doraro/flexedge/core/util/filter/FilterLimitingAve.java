package cn.doraro.flexedge.core.util.filter;

/**
 * 限幅平均滤波法
 * <p>
 * A、方法：
 * <p>
 * 相当于“限幅滤波法”+“递推平均滤波法”
 * <p>
 * 每次采样到的新数据先进行限幅处理，
 * <p>
 * 再送入队列进行递推平均滤波处理
 * <p>
 * B、优点：
 * <p>
 * 融合了两种滤波法的优点
 * <p>
 * 对于偶然出现的脉冲性干扰，可消除由于脉冲干扰所引起的采样值偏差
 * <p>
 * C、缺点：
 * <p>
 * 比较浪费RAM
 * <p>
 * 6、限幅平均滤波法
 * <p>
 * <p>
 * <p>
 * 略 参考子程序1、3
 * <p>
 * #define A 10
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
 * if ( ( new_value - value > A ) || ( value - new_value > A )
 * <p>
 * return value;
 * <p>
 * return new_value;
 * <p>
 * <p>
 * <p>
 * }
 * <p>
 * <p>
 * --------------------------------
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
public class FilterLimitingAve {

}
