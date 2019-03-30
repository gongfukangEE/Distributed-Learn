package limit;

/**
 * @auther G.Fukang
 * @date 3/27 16:18
 */
public interface RedisLimit {

    /**
     * 是否需要限流
     * @return boolean
     * @author G.Fukang
     * @date: 3/27 16:19
     */
    public boolean limit();
}
