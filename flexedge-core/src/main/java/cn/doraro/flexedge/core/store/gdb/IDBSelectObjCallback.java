package cn.doraro.flexedge.core.store.gdb;

/**
 * ��һЩ����£���Ҫ�Բ��ҵ����ݽ�����д�����ÿ����Ҫ������������ܴ�
 * ���ͨ��ֱ�Ӳ��������ҽ�������γɽ�����Ļ�����Ҫռ�ü�����ڴ�
 * <p>
 * Ϊ�˱���������������������ʵ�ִ������ݽӿڣ��������ݿ��ѯ��ʱ����Ϊ
 * �����ṩ��
 * <p>
 * �÷���Ϊ�����ݿ����о�XORM�����б�����У�ֱ�ӽ��д���ķ���
 *
 * @author Jason Zhu
 */
public interface IDBSelectObjCallback<T> {
    /**
     * ����true��ʾ����������һ�еĶ�ӦXORM����
     * ����false��ʾֹͣ����
     *
     * @param rowidx
     * @param o
     * @return
     */
    public boolean onFindObj(int rowidx, T o);
}
