package Hash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @auther G.Fukang
 * @date 4/4 10:26
 */
public class SortedMapWithoutVirtualNode implements LoadBalancer {

    private static Logger logger = LoggerFactory.getLogger(SortedMapWithoutVirtualNode.class);

    private TreeMap<Integer, String> treeMapHash;

    @Override
    public void addServerNode(String serverNodeName) {
        int hash = new GetHashCode().getHashCode(serverNodeName);
        treeMapHash.put(hash, serverNodeName);
    }

    @Override
    public void delServerNode(String serverNodeName) {
        int hash = new GetHashCode().getHashCode(serverNodeName);
        treeMapHash.remove(hash);
        logger.info("服务器节点：{} 下线", serverNodeName);
    }

    @Override
    public String selectServerNode(String requestURL) {
        int hash = new GetHashCode().getHashCode(requestURL);
        // 向右寻找第一个 key
        Map.Entry<Integer, String> subEntry= treeMapHash.ceilingEntry(hash);
        // 设置成一个环，如果超过尾部，则取第一个点
        subEntry = subEntry == null ? treeMapHash.firstEntry() : subEntry;
        return subEntry.getValue();
    }

    // 构建 Hash 环
    public SortedMap<Integer, String> buildHash(TreeMap<Integer, String> treeMap) {
        this.treeMapHash = treeMap;
        return treeMapHash;
    }
}
