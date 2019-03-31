package limit.tokenBucketLimit;

/**
 * @auther G.Fukang
 * @date 3/31 19:36
 */
public interface BucketToken {

    // 令牌桶唯一标识
    String bucketToken = "BUCKET_TOKEN";

    // 上次请求的时间
    String LAST_QUEST_TIME = "LAST_QUEST_TIME";
}
