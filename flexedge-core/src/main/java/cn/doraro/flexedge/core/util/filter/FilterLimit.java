package cn.doraro.flexedge.core.util.filter;

import cn.doraro.flexedge.core.util.xmldata.XmlData;

/**
 * 1、限幅滤波法（又称程序判断滤波法）
 * <p>
 * A、方法：
 * <p>
 * 根据经验判断，确定两次采样允许的最大偏差值（设为A）
 * <p>
 * 每次检测到新值时判断：
 * <p>
 * 如果本次值与上次值之差<=A,则本次值有效
 * <p>
 * 如果本次值与上次值之差>A,则本次值无效,放弃本次值,用上次值代替本次值
 * <p>
 * B、优点：
 * <p>
 * 能有效克服因偶然因素引起的脉冲干扰
 * <p>
 * C、缺点
 * <p>
 * 无法抑制那种周期性的干扰
 * <p>
 * 平滑度差
 * <p>
 * 1、限副滤波
 * <p>
 * A值可根据实际情况调整
 * <p>
 * value为有效值，new_value为当前采样值
 * <p>
 * 滤波程序返回有效的实际值
 * <p>
 * #define A 10
 * <p>
 * char value;
 * <p>
 * char filter()
 * <p>
 * {
 * <p>
 * char new_value;
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
 *
 * @author zzj
 */
public class FilterLimit extends AbstractFilter {
    /**
     * 限制幅度
     */
    double limitVal = -1;
    transient double lastVal;
    transient boolean hasData = false;

    @Override
    public String getFilterName() {
        return "limit";
    }

    @Override
    public String getFilterTitle() {
        return "限幅滤波法";
    }

    @Override
    public String getFilterDesc() {
        return "根据经验判断，确定两次采样允许的最大偏差值（设为A）每次检测到新值时判断： 如果本次值与上次值之差<=A,则本次值有效        如果本次值与上次值之差>A,则本次值无效,放弃本次值,用上次值代替本次值";
    }

    public void initFilter(XmlData xd) {
        limitVal = xd.getParamValueDouble("limit_v", -1);
    }

    public double filter(double d) {
        if (limitVal <= 0)
            return d;
        if (!hasData) {
            hasData = true;
            lastVal = d;
            return d;
        }

        if ((d - lastVal > limitVal) || (lastVal - d > limitVal))
            return lastVal;

        lastVal = d;
        return d;
    }
}
