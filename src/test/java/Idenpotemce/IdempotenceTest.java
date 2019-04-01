package Idenpotemce;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.*;

/**
 * @auther G.Fukang
 * @date 4/1 20:41
 */
public class IdempotenceTest {

    private static Logger logger = LoggerFactory.getLogger(IdempotenceTest.class);
    private static ExecutorService executorServicePool;


    private static Idempotence idempotence;


    public static void main(String[] args) throws InterruptedException {
        IdempotenceTest idempotenceTest = new IdempotenceTest();
        idempotenceTest.init();
        initThread();

        for (int i = 0; i < 100; i++) {
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

        idempotence = new Idempotence.Builder(jedisConnectionFactory).idempotencePrefix("idem_").build();
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
            boolean requestIdem = idempotence.insertRequestId("request001", 1000);
            if (requestIdem) {
                logger.info("***** 请求执行中 {} *****", index);
                boolean requestDel = idempotence.delRequestId("request001");
                if (requestDel) {
                    logger.info("请求执行结束 ****");
                } else {
                    logger.info("请求未能结束");
                }
            } else {
                logger.info("请求 {} 重复提交", index);
            }
        }
    }
}
