package cn.doraro.flexedge.core.util.filter;

/**
 * 中位值平均滤波法（又称防脉冲干扰平均滤波法）
 * <p>
 * A、方法：
 * <p>
 * 相当于“中位值滤波法”+“算术平均滤波法”
 * <p>
 * 连续采样N个数据，去掉一个最大值和一个最小值
 * <p>
 * 然后计算N-2个数据的算术平均值
 * <p>
 * N值的选取：3~14
 * <p>
 * B、优点：
 * <p>
 * 融合了两种滤波法的优点
 * <p>
 * 对于偶然出现的脉冲性干扰，可消除由于脉冲干扰所引起的采样值偏差
 * <p>
 * C、缺点：
 * <p>
 * 测量速度较慢，和算术平均滤波法一样
 * <p>
 * 比较浪费RAM
 * <p>
 * 5、中位值平均滤波法（又称防脉冲干扰平均滤波法）
 * <p>
 * #define N 12
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char count,i,j;
 * <p>
 * char value_buf[N];
 * <p>
 * int  sum=0;
 * <p>
 * for  (count=0;count<N;count++)
 * <p>
 * {
 * <p>
 * value_buf[count] = get_ad();
 * <p>
 * delay();
 * <p>
 * }
 * <p>
 * for (j=0;j<N-1;j++)
 * <p>
 * {
 * <p>
 * for (i=0;i<N-j;i++)
 * <p>
 * {
 * <p>
 * if ( value_buf[i]>value_buf[i+1] )
 * <p>
 * {
 * <p>
 * temp = value_buf[i];
 * <p>
 * value_buf[i] = value_buf[i+1];
 * <p>
 * value_buf[i+1] = temp;
 * <p>
 * }
 * <p>
 * }
 * <p>
 * }
 * <p>
 * for(count=1;count<N-1;count++)
 * <p>
 * sum += value[count];
 * <p>
 * return (char)(sum/(N-2));
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterMedianAve {

}
