package bloomFilter.simpleBloomFilter;

/**
 * @auther G.Fukang
 * @date 4/5 15:58
 */
public interface BloomFilter {
    // 添加元素
    public void put(String key);
    // 判断元素是否存在
    public boolean mightContain(String key);
}
