package cn.doraro.flexedge.core.util.filter;

/**
 * 加权递推平均滤波法
 * <p>
 * A、方法：
 * <p>
 * 是对递推平均滤波法的改进，即不同时刻的数据加以不同的权
 * <p>
 * 通常是，越接近现时刻的数据，权取得越大。
 * <p>
 * 给予新采样值的权系数越大，则灵敏度越高，但信号平滑度越低
 * <p>
 * B、优点：
 * <p>
 * 适用于有较大纯滞后时间常数的对象
 * <p>
 * 和采样周期较短的系统
 * <p>
 * C、缺点：
 * <p>
 * 对于纯滞后时间常数较小，采样周期较长，变化缓慢的信号
 * <p>
 * 不能迅速反应系统当前所受干扰的严重程度，滤波效果差
 * coe数组为加权系数表，存在程序存储区。
 * <p>
 * #define N 12
 * <p>
 * char code coe[N] = {1,2,3,4,5,6,7,8,9,10,11,12};
 * <p>
 * char code sum_coe = 1+2+3+4+5+6+7+8+9+10+11+12;
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char count;
 * <p>
 * char value_buf[N];
 * <p>
 * int  sum=0;
 * <p>
 * for (count=0,count<N;count++)
 * <p>
 * {
 * <p>
 * value_buf[count] = get_ad();
 * <p>
 * delay();
 * <p>
 * }
 * <p>
 * for (count=0,count<N;count++)
 * <p>
 * sum += value_buf[count]*coe[count];
 * <p>
 * return (char)(sum/sum_coe);
 * <p>
 * }
 *
 * @author zzj
 */
public class FilterWeightedRecursiveMean {

}
