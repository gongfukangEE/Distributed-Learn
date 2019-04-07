package BloomFilter;

import bloomFilter.simpleBloomFilter.SimpleBloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @auther G.Fukang
 * @date 4/5 16:51
 */
public class SimpleBloomFilterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBloomFilterTest.class);

    private static SimpleBloomFilter bloomFilter = new SimpleBloomFilter();

    static {
        bloomFilter.build();

        for (int i = 0; i < 2000000; i++) {
            bloomFilter.put(String.valueOf(i));
        }
    }

    public static void main(String[] args) {

        String testKey = "9999";

        long startTime = System.nanoTime();

        if (bloomFilter.mightContain(testKey)) {
            LOGGER.info(" key : " + testKey + " 包含在布隆过滤器中 ");
        }

        long endTime = System.nanoTime();

        LOGGER.info("消耗时间: " + (endTime - startTime) + " 微秒");

        double errNum = 0;
        for (int i = 2000000; i < 4000000; i++) {
            if (bloomFilter.mightContain(String.valueOf(i))) {
                ++errNum;
            }
        }

        LOGGER.info("误差：" + (errNum / 2000000) * 100 + "%");
    }
}
