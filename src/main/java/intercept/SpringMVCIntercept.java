package intercept;

import limit.RedisLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @auther G.Fukang
 * @date 3/29 10:02
 */
public class SpringMVCIntercept extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(SpringMVCIntercept.class);

    @Autowired
    private RedisLimit redisLimit;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

       if (redisLimit == null) {
           throw new NullPointerException("redisLimit is null");
       }

       if ()

        return false;
    }

}
