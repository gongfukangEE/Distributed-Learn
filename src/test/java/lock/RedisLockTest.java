package lock;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.concurrent.Worker;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.*;

/**
 * @auther G.Fukang
 * @date 3/31 22:38
 */
public class RedisLockTest {

    private static Logger logger = LoggerFactory.getLogger(RedisLockTest.class);
    private static ExecutorService executorServicePool;


    private static RedisLock redisLock;


    public static void main(String[] args) throws InterruptedException {
        RedisLockTest redisLockTest = new RedisLockTest();
        redisLockTest.init();
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

        redisLock = new RedisLock.Builder(jedisConnectionFactory).lockPrefix("lock_").sleepTime(100).builder();
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
            boolean tryLock =  redisLock.tryLock("test", "12345", 1000);
            if (tryLock) {
                logger.info("加锁成功 ***** ");
                boolean tryUnLock = redisLock.unlock("test", "12345");
                if (tryUnLock) {
                    logger.info("解锁成功 ****");
                } else {
                    logger.info("解锁失败");
                }
            } else {
                logger.info("加锁失败");
            }
        }
    }
}
