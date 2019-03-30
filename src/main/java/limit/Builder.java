package limit;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * @auther G.Fukang
 * @date 3/27 16:27
 */
public class Builder {
    private JedisConnectionFactory jedisConnectionFactory = null;

    private int limit = 200;

    public Builder(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }

    public Builder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public RedisLimit builder() {
        return new RedisLimitImpl(this);
    }

    public JedisConnectionFactory getJedisConnectionFactory() {
        return jedisConnectionFactory;
    }

    public void setJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
