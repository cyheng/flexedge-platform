package cn.doraro.flexedge.core.store.gdb;

/**
 * ��һЩ����£���Ҫ�Բ��ҵ����ݽ�����д�����ÿ����Ҫ������������ܴ�
 * ���ͨ��ֱ�Ӳ��������ҽ�������γɽ�����Ļ�����Ҫռ�ü�����ڴ�
 * <p>
 * Ϊ�˱���������������������ʵ�ִ������ݽӿڣ��������ݿ��ѯ��ʱ����Ϊ
 * �����ṩ��
 * <p>
 * GDB�ڽ�����й��������У��Ϳ���ֱ�Ӵ�����û��Ҫ�γɽ���������ҿ��Ը������
 * ��ʱ�жϽ�����
 * <p>
 * ʹ�ûص���ʽ�����ݿ��ѯ���ʣ��ر������벻��Ҫ���ؽ�����ĺ�̨���������
 *
 * @author Jason Zhu
 */
public interface IDBSelectCallback {
    /**
     * ��һ�����ݲ�ѯ�Ľ���ı�ṹ����ʱ���ص��ķ���
     * �÷����������true�����������ÿһ��.
     * <p>
     * ���false��������Ĵ������̽���
     *
     * @param dt
     * @return
     * @throws Exception
     */
    public boolean onFindDataTable(int tableidx, DataTable dt) throws Exception;

    /**
     * �ڴ���ÿһ��ʱӦ�ÿ��ǵ�����
     *
     * @param tableidx
     * @param dt
     * @param rowidx
     * @param dr
     * @return ���false��������Ĵ������̽���
     * @throws Exception
     */
    public boolean onFindDataRow(int tableidx, DataTable dt, int rowidx, DataRow dr) throws Exception;


}
