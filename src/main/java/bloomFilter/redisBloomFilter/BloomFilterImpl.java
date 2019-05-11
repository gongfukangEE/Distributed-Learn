package bloomFilter.redisBloomFilter;

import com.google.common.hash.HashFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther G.Fukang
 * @date 5/10 18:55
 */
public class BloomFilterImpl<T> implements BloomFilter<T>{

    private Logger logger = LoggerFactory.getLogger(BloomFilterImpl.class);

    private BloomFilterBuilder config;

    public BloomFilterImpl(BloomFilterBuilder bloomFilterBuilder) {
        this.config = bloomFilterBuilder;
    }

    @Override
    public boolean add(T object) {
        if (object == null) {
            return false;
        }
        // 偏移量列表
        List<Integer> offsetList = hash(object);
        if (offsetList == null || offsetList.isEmpty()) {
            return false;
        }
        for (Integer offset : offsetList) {
            Jedis jedis = null;
            try {
                jedis = getJedisPool().getResource();
                // 每个 Hash 函数得到一个位置，然后将按照位置设置 bit
                jedis.setbit(getName(), offset, true);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return true;
    }

    @Override
    public Map<Object, Boolean> batchAdd(List<T> objectList) {
        if (objectList == null || objectList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Object, Boolean> resultMap = new HashMap<>();
        for (T object : objectList) {
            boolean result = add(object);
            resultMap.put(object, result);
        }
        return resultMap;
    }

    @Override
    public boolean contains(T object) {
        if (object == null) {
            return false;
        }
        // 偏移量列表
        List<Integer> offsetList = hash(object);
        if (offsetList == null || offsetList.isEmpty()) {
            return false;
        }
        for (int offset : offsetList) {
            Jedis jedis = null;
            try {
                jedis = getJedisPool().getResource();
                boolean result = jedis.getbit(getName(), offset);
                if (!result) {
                    return false;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return true;
    }

    public BloomFilterBuilder getConfig() {
        return config;
    }

    @Override
    public long getExpectedInsertions() {
        return getConfig().getExpectedInsertions();
    }

    @Override
    public double getFalseProbability() {
        return getConfig().getFalseProbability();
    }

    @Override
    public long getSize() {
        return getConfig().getTotalSize();
    }

    @Override
    public int getHashIterations() {
        return getConfig().getHashIterations();
    }

    public String getName() {
        return getConfig().getName();
    }

    public JedisPool getJedisPool() {
        return getConfig().getJedisPool();
    }

    public HashFunction getHashFuction() {
        return getConfig().getHashFunction();
    }


    public List<Integer> hash(Object object) {
        byte[] bytes = object.toString().getBytes();
         getHashFuction().hashBytes(bytes, (int)getSize(), getConfig().getHashIterations());
    }
}
