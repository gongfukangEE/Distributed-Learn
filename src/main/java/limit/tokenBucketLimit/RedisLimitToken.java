package limit.tokenBucketLimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import util.ScriptUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @auther G.Fukang
 * @date 3/27 16:50
 */
public class RedisLimitToken {

    private static Logger logger = LoggerFactory.getLogger(RedisLimitToken.class);

    // 令牌桶容量
    private int limit = 0;
    // 令牌桶的速率 n/s
    private int rate = 1;
    // 请求令牌的数量
    private int permits= 1;

    private JedisConnectionFactory jedisConnectionFactory;

    private static final int FAIL_CODE = 0;

    /**
     * lua script
     */
    private String script;

    private RedisLimitToken(Builder builder) {
        this.limit = builder.limit;
        this.rate = builder.rate;
        this.permits = builder.permits;
        this.jedisConnectionFactory = builder.jedisConnectionFactory;
        buildScript();
    }


    /**
     * limit traffic
     *
     * @return if true
     */
    public boolean limit() {

        //get connection
        Object connection = getConnection();

        Object result = limitRequest(connection);

        if (FAIL_CODE != (Long) result) {
            return true;
        } else {
            return false;
        }
    }

    private Object limitRequest(Object connection) {
        Object result = null;
        List<String> keys = Arrays.asList(BucketToken.bucketToken, BucketToken.LAST_QUEST_TIME);
        List<String> argvs = Arrays.asList(String.valueOf(limit), String.valueOf(permits), String.valueOf(rate),
                String.valueOf(System.currentTimeMillis() / 1000));
        // win 下不支持多个参数
        // 令牌桶标识、上次请求时间、令牌桶容量、请求令牌的数量、令牌流入速率、当前时间
        result = ((Jedis) connection).eval(script, keys, argvs);
        ((Jedis) connection).close();
        return result;
    }

    /**
     * get Redis connection
     *
     * @return
     */
    private Object getConnection() {
        Object connection;
        RedisConnection redisConnection = jedisConnectionFactory.getConnection();
        connection = redisConnection.getNativeConnection();
        return connection;
    }

    /**
     * read lua script
     */
    private void buildScript() {
        script = ScriptUtil.getScript("limitToken.lua");
    }


    /**
     * the builder
     */
    public static class Builder {
        private JedisConnectionFactory jedisConnectionFactory = null;

        private int limit = 200;

        private int rate = 1;

        private int permits = 1;


        public Builder(JedisConnectionFactory jedisConnectionFactory) {
            this.jedisConnectionFactory = jedisConnectionFactory;
        }

        public Builder limit(int limit, int rate, int permits) {
            this.limit = limit;
            this.rate = rate;
            this.permits = permits;
            return this;
        }

        public RedisLimitToken build() {
            return new RedisLimitToken(this);
        }

    }
}
