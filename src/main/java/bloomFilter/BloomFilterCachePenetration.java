package bloomFilter;

import com.google.common.hash.BloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.HashMap;

/**
 * @auther G.Fukang
 * @date 4/3 20:12
 */
public class BloomFilterCachePenetration {

    private static Logger logger = LoggerFactory.getLogger(BloomFilterCachePenetration.class);

    private JedisConnectionFactory jedisConnectionFactory;

    private BloomFilter bloomFilter;

    // 模拟数据库
    private HashMap<String, String> mapDB;


    public BloomFilterCachePenetration(Builder builder) {
        this.jedisConnectionFactory = builder.jedisConnectionFactory;
        this.bloomFilter = builder.bloomFilter;
        this.mapDB = builder.mapDB;
    }

    public String getByKey(String key) {
        // 通过 key 在 Redis 中查找 value
        String value = get(key);
        if (StringUtils.isEmpty(value)) {
            logger.info("Redis 没命中 {}", key);
            // 在 BloomFilter 中查找
            if (bloomFilter.mightContain(key)) {
                logger.info("BloomFilter 命中 {}", key);
                // 命中 value 为 null
                return value;
            } else {
                // 没命中，到数据库中查询
                if (mapDB.containsKey(key)) {
                    logger.info("更新 Key {} 到 Redis", key);
                    String valDB = mapDB.get(key);
                    set(key, valDB);
                    return valDB;
                } else {
                    logger.info("更新 Key {} 到 BloomFilter", key);
                    bloomFilter.put(key);
                    return value;
                }
            }
        } else {
            logger.info("Redis 命中 {}", key);
            return value;
        }
    }

    /**
     * 获取 Redis 连接
     *
     * @author G.Fukang
     * @date: 3/31 22:20
     */
    private Object getConnection() {
        Object connection;
        RedisConnection redisConnection = jedisConnectionFactory.getConnection();
        connection = redisConnection.getNativeConnection();
        return connection;
    }

    /**
     * Redis 插入数据
     *
     * @author G.Fukang
     * @date: 4/3 20:28
     */
    private String  set(String key, String value) {
        String result = null;
        Object connection = getConnection();
        result = ((Jedis) connection).set(key, value);
        return result;
    }

    /**
     * Redis
     *
     * @author G.Fukang
     * @date: 4/3 20:37
     */
    public String get(String key) {
        String result = null;
        Object connection = getConnection();
        result = ((Jedis) connection).get(key);
        return result;
    }


    public static class Builder {

        private JedisConnectionFactory jedisConnectionFactory = null;
        private BloomFilter bloomFilter = null;
        private HashMap<String, String> mapDB;

        public Builder(JedisConnectionFactory jedisConnectionFactory, BloomFilter bloomFilter, HashMap<String, String> mapDB) {
            this.jedisConnectionFactory = jedisConnectionFactory;
            this.bloomFilter = bloomFilter;
            this.mapDB = mapDB;
        }

        public BloomFilterCachePenetration build() {
            return new BloomFilterCachePenetration(this);
        }
    }

}
