package Hash;

/**
 * @auther G.Fukang
 * @date 4/4 9:41
 */
public class StringHashCodeTest {

    public static void main(String[] args) {
        System.out.println("192.168.0.0:111 的哈希值：" + Math.abs("192.168.0.0:111".hashCode()));
        System.out.println("192.168.0.1:111 的哈希值：" + Math.abs("192.168.0.1:111".hashCode()));
        System.out.println("192.168.0.2:111 的哈希值：" + Math.abs("192.168.0.2:111".hashCode()));
        System.out.println("192.168.0.3:111 的哈希值：" + Math.abs("192.168.0.3:111".hashCode()));
        System.out.println("192.168.0.4:111 的哈希值：" + Math.abs("192.168.0.4:111".hashCode()));
    }
}
