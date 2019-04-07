package bloomFilter.simpleBloomFilter;

import java.util.BitSet;

/**
 * @auther G.Fukang
 * @date 4/5 15:18
 */
public class SimpleBloomFilter implements BloomFilter {

    // bit 数组的长度
    private static final int DEFAULT_SIZE = 2 << 24;
    // Hash 算法中所用的乘数
    private static final int[] seeds = new int[]{3, 5, 7, 11, 13, 17, 19, 23};
    // bit 数组
    private static BitSet bitSet = new BitSet(DEFAULT_SIZE);
    // Hash 函数
    private static Hash[] hashes = new Hash[seeds.length];

    @Override
    public void put(String key) {
        if (key != null) {
            // 将 key Hash 为 8 个或者多个整数，然后在这些整数的 bit 上变为 1
            for (Hash hash : hashes) {
                bitSet.set(hash.Hash(key), true);
            }
        }
    }

    @Override
    public boolean mightContain(String key) {
        if (key == null) {
            return false;
        }
        boolean res = true;
        for (Hash hash : hashes) {
            res = res && bitSet.get(hash.Hash(key));
            if (!res) {
                return res;
            }
        }
        return res;
    }

    public Hash[] build() {
        Hash[] res = new Hash[seeds.length];
        for (int i = 0; i < seeds.length; i++) {
            res[i] = new Hash(DEFAULT_SIZE, seeds[i]);
        }
        hashes = res;
        return hashes;
    }
}
