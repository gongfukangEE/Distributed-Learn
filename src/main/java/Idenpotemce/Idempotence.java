package Idenpotemce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

/**
 * 保证幂等性
 *
 * @auther G.Fukang
 * @date 4/1 20:16
 */
public class Idempotence {

    private static Logger logger = LoggerFactory.getLogger(Idempotence.class);

    // 前缀
    private String idempotencePrfix;

    private JedisConnectionFactory jedisConnectionFactory;

    // lua
    private String script;

    private Idempotence(Builder builder) {
        this.jedisConnectionFactory = builder.jedisConnectionFactory;
        this.idempotencePrfix = builder.idemPrefix;
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

    public boolean insertRequestId(String requestId, int expireTime) {
        // 向 Redis 中插入 requestId
        Object connection = getConnection();
        Long setnxResult = ((Jedis) connection).setnx(idempotencePrfix + requestId, String.valueOf(expireTime));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean delRequestId(String requestId) {
        Object connection = getConnection();
        Long delResult = ((Jedis) connection).del(idempotencePrfix + requestId);
        if (delResult.equals(1L)) {
            return true;
        } else {
            return false;
        }
    }

    public static class Builder {

        private static final String DEFAULT_PREFIX = "idem_";

        private JedisConnectionFactory jedisConnectionFactory = null;

        private String idemPrefix = DEFAULT_PREFIX;

        public Builder(JedisConnectionFactory jedisConnectionFactory) {
            this.jedisConnectionFactory = jedisConnectionFactory;
        }

        public Builder idempotencePrefix(String idemPrefix) {
            this.idemPrefix = idemPrefix;
            return this;
        }

        public Idempotence build() {
            return new Idempotence(this);
        }
    }
}
