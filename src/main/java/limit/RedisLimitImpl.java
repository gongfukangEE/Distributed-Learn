package limit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * @auther G.Fukang
 * @date 3/27 16:21
 */
public class RedisLimitImpl implements RedisLimit {

    private static Logger logger = LoggerFactory.getLogger(RedisLimitImpl.class);

    private JedisConnectionFactory jedisConnectionFactory;
    // 令牌桶总量
    private int limit = 200;

    private static final int FAIL_CODE = 0;

    /**
     * lua script
     */
    private String script;

    public RedisLimitImpl(Builder builder) {
        this.limit = builder.getLimit();
        this.jedisConnectionFactory = builder.getJedisConnectionFactory();
        // TODO
    }


    @Override
    public boolean limit() {
        Object connection = getConnection();
        return false;
    }

    private Object limitRequest(Object connection) {
        Object result = null;
        String key = String.valueOf(System.currentTimeMillis() / 1000);
        if (connection instanceof Jedis) {
            result = ((Jedis) connection).eval(script, Collections.singletonList(key), Collections.singletonList(String.valueOf(limit)));
            ((Jedis) connection).close();
        }
        return result;
    }

    // 获得 Jedis 连接
    private Object getConnection() {
        Object connection;
        RedisConnection redisConnection = jedisConnectionFactory.getConnection();
        connection = redisConnection.getNativeConnection();
        return connection;
    }
}
