package Hash;

/**
 * @auther G.Fukang
 * @date 4/4 12:57
 */
public class FNVIHASHTest {

    public static void main(String[] args) {

        GetHashCode hashCode = new GetHashCode();

        System.out.println("192.168.0.0:111 的哈希值：" + hashCode.getHashCode("192.168.0.0:111"));
        System.out.println("192.168.0.1:111 的哈希值：" + hashCode.getHashCode("192.168.0.1:111"));
        System.out.println("192.168.0.2:111 的哈希值：" + hashCode.getHashCode("192.168.0.2:111"));
        System.out.println("192.168.0.3:111 的哈希值：" + hashCode.getHashCode("192.168.0.3:111"));
        System.out.println("192.168.0.4:111 的哈希值：" + hashCode.getHashCode("192.168.0.4:111"));
    }
}
