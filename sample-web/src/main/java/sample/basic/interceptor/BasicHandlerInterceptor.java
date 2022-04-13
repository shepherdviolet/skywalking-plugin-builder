package sample.basic.interceptor;

import com.github.shepherdviolet.glacimon.java.x.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC拦截器
 */
@Component
public class BasicHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(BasicHandlerInterceptor.class);

    @Value("${server.error.path:${error.path:/error}}")
    private String errorPath;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //路径
        String uri = request.getRequestURI();
        //生成跟踪号
        if (!errorPath.equals(uri)) {
            Trace.start();
        }
        //打印路径
        logger.info("URI: " + uri);
        return true;
    }

}
