package limit;

import limit.tokenBucketLimit.RedisLimitToken;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.*;


/**
 * @auther G.Fukang
 * @date 3/31 10:26
 */
public class RedisLimitTokenTest {

    private static Logger logger = LoggerFactory.getLogger(RedisLimitTokenTest.class);

    private static final String initTime = String.valueOf(System.currentTimeMillis() / 1000);

    private static ExecutorService executorServicePool;


    private static RedisLimitToken redisLimitToken;


    public static void main(String[] args) throws InterruptedException {
        RedisLimitTokenTest redisLimitTest = new RedisLimitTokenTest();
        redisLimitTest.init();
        initThread();

        for (int i = 0; i < 250; i++) {
            executorServicePool.execute(new Worker(i));
        }

        executorServicePool.shutdown();
        while (!executorServicePool.awaitTermination(1, TimeUnit.SECONDS)) {
            logger.info("worker running");
        }
        logger.info("worker over");

    }

    @Before
    public void setBefore() {
        init();
    }

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

        redisLimitToken = new RedisLimitToken.Builder(jedisConnectionFactory).limit(100, 5, 1).build();
    }

    public static void initThread() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("current-thread-%d").build();
        executorServicePool = new ThreadPoolExecutor(350, 350, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(200), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    }


    private static class Worker implements Runnable {

        private int index;

        public Worker(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            boolean limit = redisLimitToken.limit();
            if (!limit) {
                logger.info("请求 {} 被限流了", index);
            } else {
                logger.info("***** 请求 {} 没有被限流 *****", index);
            }
        }
    }
}
