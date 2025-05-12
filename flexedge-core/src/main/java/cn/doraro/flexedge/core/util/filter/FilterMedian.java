package cn.doraro.flexedge.core.util.filter;

/**
 * 中位值滤波法
 * <p>
 * A、方法：
 * <p>
 * 连续采样N次（N取奇数）
 * <p>
 * 把N次采样值按大小排列
 * <p>
 * 取中间值为本次有效值
 * <p>
 * B、优点：
 * <p>
 * 能有效克服因偶然因素引起的波动干扰
 * <p>
 * 对温度、液位的变化缓慢的被测参数有良好的滤波效果
 * <p>
 * C、缺点：
 * <p>
 * 对流量、速度等快速变化的参数不宜
 * <p>
 * 2、中位值滤波法
 * <p>
 * N值可根据实际情况调整
 * <p>
 * 排序采用冒泡法
 * <p>
 * #define N  11
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char value_buf[N];
 * <p>
 * char count,i,j,temp;
 * <p>
 * for ( count=0;count<N;count++)
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
 * return value_buf[(N-1)/2];
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterMedian {

}
