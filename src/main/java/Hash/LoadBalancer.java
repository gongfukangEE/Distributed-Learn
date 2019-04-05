package Hash;


import java.util.HashMap;

/**
 * @auther G.Fukang
 * @date 4/4 9:50
 */
public interface LoadBalancer {

    // 添加服务器节点
    public void addServerNode(String serverNodeName);

    // 删除服务器节点
    public void delServerNode(String serverNodeName);

    // 选择服务器节点
    public String selectServerNode(String requestURL);
}
