package Hash;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auther G.Fukang
 * @date 4/4 16:59
 */
public class Analysis {

    // <节点，服务器>
    public double analysis(HashMap<String, String> map) {
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        // 计数 map <Server, Count>
        ConcurrentHashMap<String, Integer> countMap = new ConcurrentHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String server = entry.getValue();
            if (countMap.containsKey(server)) {
                countMap.put(server, countMap.get(server) + 1);
            } else {
                countMap.put(server, 1);
            }
        }
        Collection<Integer> values = countMap.values();
        Iterator<Integer> val = values.iterator();
        int count = 0;
        int[] res = new int[values.size()];
        while (val.hasNext()) {
            res[count] = val.next();
        }
        return variance(res);
    }

    // 求方差
    private static double variance(int[] arr) {
        int m = arr.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {
            sum += arr[i];
        }
        double dAve = sum / m;
        double dVar = 0;
        for (int i = 0; i < m; i++) {
            dVar += (arr[i] - dAve) * (arr[i] - dAve);
        }
        return dVar / m;
    }
}
