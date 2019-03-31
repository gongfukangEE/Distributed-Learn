package limit.countLimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import util.ScriptUtil;

import java.util.Collections;

/**
 * @auther G.Fukang
 * @date 3/27 16:50
 */
public class RedisLimit {

    private static Logger logger = LoggerFactory.getLogger(RedisLimit.class);


    private JedisConnectionFactory jedisConnectionFactory;
    private int limit = 200;

    private static final int FAIL_CODE = 0;

    /**
     * lua script
     */
    private String script;

    private RedisLimit(Builder builder) {
        this.limit = builder.limit;
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
        String key = String.valueOf(System.currentTimeMillis() / 1000);
        result = ((Jedis) connection).eval(script, Collections.singletonList(key), Collections.singletonList(String.valueOf(limit)));
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
        script = ScriptUtil.getScript("limit.lua");
    }


    /**
     * the builder
     */
    public static class Builder {
        private JedisConnectionFactory jedisConnectionFactory = null;

        private int limit = 200;


        public Builder(JedisConnectionFactory jedisConnectionFactory) {
            this.jedisConnectionFactory = jedisConnectionFactory;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public RedisLimit build() {
            return new RedisLimit(this);
        }

    }
}
