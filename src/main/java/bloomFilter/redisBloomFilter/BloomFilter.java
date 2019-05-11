package bloomFilter.redisBloomFilter;

import java.util.List;
import java.util.Map;

/**
 * 布隆过滤器接口
 * @auther G.Fukang
 * @date 5/10 18:47
 */
public interface BloomFilter<T> {

    /**
     * 是否添加成功
     */
    boolean add(T object);

    /**
     * 批量添加
     */
    Map<Object, Boolean> batchAdd(List<T> objectList);

    /**
     * 是否包含
     */
    boolean contains(T object);

    /**
     * 预期插入的数量
     */
    long getExpectedInsertions();

    /**
     * 预期错误率
     */
    double getFalseProbability();

    /**
     * 布隆过滤器总长度
     */
    long getSize();

    /**
     * Hash 函数迭代次数
     */
    int getHashIterations();
}
