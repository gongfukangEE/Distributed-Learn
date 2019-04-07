package BloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * @auther G.Fukang
 * @date 4/3 14:30
 */
public class GuavaBloomFilterTest {

    private static final int capacity = 10000000;

    private static final int key = 9999998;

    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), capacity);

    static {
        for (int i = 0; i < capacity; i++) {
            bloomFilter.put(i);
        }
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        if (bloomFilter.mightContain(key)) {
            System.out.println( " key : " + key + " 包含在布隆过滤器中 ");
        }

        long endTime = System.nanoTime();

        System.out.println("消耗时间: " + (endTime - startTime) + " 微秒");

        // 错误率判断
        double errNums = 0;
        for (int i = capacity + 1000000; i < capacity + 2000000; i++) {
            if (bloomFilter.mightContain(i)) {
                ++errNums;
            }
        }

        System.out.println("错误率: " + (errNums/1000000));
    }
}
