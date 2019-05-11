package bloomFilter.redisBloomFilter;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;


/**
 * @auther G.Fukang
 * @date 5/10 19:13
 */
public class BloomFilterBuilder {

    private Logger logger = LoggerFactory.getLogger(BloomFilterBuilder.class);

    private static final long MAX_SIZE = Integer.MAX_VALUE * 100L;

    private JedisPool jedisPool;

    // 布隆过滤器名（位图的 key）
    private String name;

    // 位图长度
    private long totalSize;

    // hash 函数循环次数
    private int hashIterations;

    // 预期插入条数
    private long expectedInsertions;

    // 预期错误率概率
    private double falseProbability;

    // Hash 函数
    private HashFunction hashFunction = Hashing.murmur3_128();

    // 是否完成
    private boolean done = false;

    public BloomFilterBuilder(JedisPool jedisPool, String name, long expectedInsertions, double falseProbability) {
        this.jedisPool = jedisPool;
        this.name = name;
        this.expectedInsertions = expectedInsertions;
        this.falseProbability = falseProbability;
    }

    public BloomFilterBuilder setHashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        return this;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public String getName() {
        return name;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public int getHashIterations() {
        return hashIterations;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public double getFalseProbability() {
        return falseProbability;
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }

    public <T> BloomFilterImpl<T> builder() {
        checkBloomFilterParam();
        return new BloomFilterImpl<T>(this);
    }

    /**
     * 检查布隆过滤器参数
     */
    private void checkBloomFilterParam() {
        if (done) {
            return;
        }

        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("Bloom filter name is empty");
        }

        if (expectedInsertions < 0 || expectedInsertions > MAX_SIZE) {
            throw new IllegalArgumentException("Bloom filter expectedInsertions can't be greater than " + MAX_SIZE + " or smaller than 0");
        }

        if (falseProbability > 1) {
            throw new IllegalArgumentException("Bloom filter false probability cant't be greater than 1");
        }

        if (falseProbability < 0) {
            throw new IllegalArgumentException("Bloom filter false probability cant't be negative");
        }

        // 计算布隆过滤器位图的长度
        totalSize = optimalNumOfBits();
        logger.info("{} optimalNumOfBits is {}", name, totalSize);
        if (totalSize == 0) {
            throw new IllegalArgumentException("Bloom filter calculated totalSize is " + totalSize);
        }

        if (totalSize > MAX_SIZE) {
            throw new IllegalArgumentException("Bloom filter totalSize can't be greater than " + MAX_SIZE +
                    "But calculated totalSize is " + totalSize);
        }

        // Hash 函数迭代次数
        hashIterations = optimalNumOfHashFunctions();
        logger.info("{} hashIterations is {}", name, hashFunction);

        done = true;
    }

    // 根据预期插入条数和概率计算布隆过滤器位图长度
    private long optimalNumOfBits() {
        if (falseProbability == 0) {
            falseProbability = Double.MIN_VALUE;
        }
        return (long) (-expectedInsertions * Math.log(falseProbability) / (Math.log(2) * Math.log(2)));
    }

    // 根据布隆过滤器长度与预期插入长度对比，计算 Hash 函数个数
    private int optimalNumOfHashFunctions() {
        return Math.max(1, (int) Math.round((double) totalSize / expectedInsertions * Math.log(2)));
    }
}
