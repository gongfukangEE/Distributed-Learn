package lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import util.ScriptUtil;

import java.util.Collections;


/**
 * @auther G.Fukang
 * @date 3/31 21:52
 */
public class RedisLock {

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final String LOCK_MSG = "OK";

    private static final Long UNLOCK_MSG = 1L;

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    // 前缀
    private String lockPrefix;

    private int sleepTime;

    private JedisConnectionFactory jedisConnectionFactory;

    // 等待时间
    private static final int Time = 1000;

    // lua
    private String script;

    private RedisLock(Builder builder) {
        this.jedisConnectionFactory = builder.jedisConnectionFactory;
        this.lockPrefix = builder.lockPrefix;
        this.sleepTime = builder.sleepTime;

        buildScript();
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
     * 加锁
     * 非阻塞锁 + 过期时间
     *
     * @author G.Fukang
     * @date: 3/31 22:21
     */
    public boolean tryLock(String key, String request, int expireTime) {
        Object connection = getConnection();
        String result;
        result = ((Jedis) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        ((Jedis) connection).close();

        if (LOCK_MSG.equals(result)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解锁
     *
     * @author G.Fukang
     * @date: 3/31 22:35
     */
    public boolean unlock(String key, String request) {
        Object connection = getConnection();

        Object result = null;

        result = ((Jedis) connection).eval(script, Collections.singletonList(lockPrefix + key), Collections.singletonList(request));
        ((Jedis) connection).close();

        if (UNLOCK_MSG.equals(result)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Redis 指令实现加锁
     *
     * @author G.Fukang
     * @date: 3/31 23:06
     */
    public boolean tryLockWithRedis(String key, String request, int expireTime) {
        Object connection = getConnection();
        Long setnxResult = ((Jedis) connection).setnx(lockPrefix + key + request, String.valueOf(System.currentTimeMillis() + expireTime));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            return true;
        } else {
            // 未获取到锁，继续判断时间戳，看是否可以重置锁
            String lockValueStr = ((Jedis) connection).get(lockPrefix + key + request);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                String getSetResult = ((Jedis) connection).getSet(lockPrefix + key + request, String.valueOf(System.currentTimeMillis() + expireTime));
                // 锁过期了，可以获取锁
                if (getSetResult == null || (getSetResult != null && lockValueStr.equals(getSetResult))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                // 旧的锁还没有失效
                return false;
            }
        }
    }

    /**
     * Redis 指令实现解锁
     *
     * @author G.Fukang
     * @date: 3/31 23:21
     */
    public boolean tryUnlockWithRedis(String key, String request) {
        Object connection = getConnection();
        Long delResult = ((Jedis) connection).del(lockPrefix + key + request);

        if (UNLOCK_MSG.equals(delResult)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 读取 lua 文件
     *
     * @author G.Fukang
     * @date: 3/31 22:19
     */
    private void buildScript() {
        script = ScriptUtil.getScript("lock.lua");
    }

/**
 * 构建器 builder
 *
 * @author G.Fukang
 * @date: 3/31 22:20
 */
public static class Builder {
    private static final String DEFAULT_LOCK_PREFIX = "lock_";

    // 默认等待时间
    private static final int DEFAULT_SLEEP_TIME = 100;

    private JedisConnectionFactory jedisConnectionFactory = null;

    private String lockPrefix = DEFAULT_LOCK_PREFIX;
    private int sleepTime = DEFAULT_SLEEP_TIME;

    public Builder(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }

    public Builder lockPrefix(String lockPrefix) {
        this.lockPrefix = lockPrefix;
        return this;
    }

    public Builder sleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public RedisLock builder() {
        return new RedisLock(this);
    }
}
}
