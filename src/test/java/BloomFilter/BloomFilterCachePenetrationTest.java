package BloomFilter;

import bloomFilter.BloomFilterCachePenetration;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.concurrent.*;

/**
 * @auther G.Fukang
 * @date 4/3 21:03
 */
public class BloomFilterCachePenetrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BloomFilterCachePenetrationTest.class);

    private static ExecutorService executorServicePool;

    private static final int capacity = 1000;
    private BloomFilter<String> bloomFilter = BloomFilter
            .create((Funnel<String>) (s, primitiveSink) -> primitiveSink.putString(s, Charsets.UTF_8), capacity);

    private HashMap<String, String> mapDB = new HashMap<>();

    private static BloomFilterCachePenetration cache;

    private void init() {

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(10);
        config.setMaxTotal(300);
        config.setMaxWaitMillis(10000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.setHostName("47.106.218.181");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setPassword("");
        jedisConnectionFactory.setTimeout(100000);
        jedisConnectionFactory.afterPropertiesSet();

        // 设置 Redis 初始状态
        Jedis connection = (Jedis) jedisConnectionFactory.getConnection().getNativeConnection();
        // 清空 Redis
        for (int i = 70; i < 90; i++) {
            connection.del(String.valueOf(i));
        }
        LOGGER.info("Redis 清理完成");
        for (int i = 70; i < 80; i++) {
            connection.set(String.valueOf(i), String.valueOf(i) + "A");
        }
        // 设置 DB
        for (int i = 70; i < 90; i++) {
            this.mapDB.put(String.valueOf(i), String.valueOf(i) + "A");
        }
        // 设置 BloomFilter
        for (int i = 90; i < 95; i++) {
            this.bloomFilter.put(String.valueOf(i));
        }

        cache = new BloomFilterCachePenetration.Builder(jedisConnectionFactory, bloomFilter, mapDB).build();
    }

    public static void initThread() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("current-thread-%d").build();
        executorServicePool = new ThreadPoolExecutor(350, 350, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(200), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    @Before
    public void setBefore() {
        init();
    }

    public static void main(String[] args) throws InterruptedException {

        BloomFilterCachePenetrationTest cacheTest = new BloomFilterCachePenetrationTest();
        cacheTest.init();
        initThread();

        for (int i = 70; i < 100; i++) {
            executorServicePool.execute(new Worker(i, String.valueOf(i)));
        }

        for (int i = 90; i < 100; i++) {
            executorServicePool.execute(new Worker(i, String.valueOf(i)));
        }

        executorServicePool.shutdown();
        while (!executorServicePool.awaitTermination(1, TimeUnit.SECONDS)) {
            LOGGER.info("worker running");
        }
        LOGGER.info("worker over");
    }

    private static class Worker implements Runnable {
        private int index;
        private String key;

        public Worker(int index, String key) {
            this.index = index;
            this.key = key;
        }

        @Override
        public void run() {
            String result = cache.getByKey(key);
            // LOGGER.info("Value 值为：" + result);
        }
    }
}
