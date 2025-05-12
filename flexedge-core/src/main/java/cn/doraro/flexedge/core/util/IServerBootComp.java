package cn.doraro.flexedge.core.util;

/**
 * ���������������������ӿ�
 * <p>
 * �ýӿ��������������Ҫ��ӵ����
 * <p>
 * ��:tomato server ���tomcat��Ϊһ�����������ͬʱ����.
 * ��Ӧ�ð�Tomcat������ʵ�ָýӿ�
 * <p>
 * �����Ϳ�����tomato�п���tomcat��������ֹͣ
 *
 * @author Jason Zhu
 */
public interface IServerBootComp {
    public String getBootCompName();

    public void startComp() throws Exception;

    public void stopComp() throws Exception;

    public boolean isRunning() throws Exception;
}
