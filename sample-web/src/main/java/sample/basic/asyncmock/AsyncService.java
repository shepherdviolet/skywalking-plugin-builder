package sample.basic.asyncmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 被侵入的异步逻辑示例
 *
 * @author S.Violet
 */
@Component
public class AsyncService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService threadPool = ThreadPoolExecutorUtils.createFixed(5, "caller-%d");

    /**
     * 这个异步示例不是常见情况, 是模拟相对比较棘手的情况, 假设ExecutorService无法被侵入(是Java核心类),
     * 且异步执行的Runnable还是匿名内部类, 我们尝试利用invoke方法的Map参数携带追踪信息,
     * 因为匿名内部类实际上会将Map作为成员变量持有, 可以反射获取到.
     *
     * 如果是一般的异步场景, 即可以侵入threadPool.execute方法的, 可以拦截这个方法, 在Runnable里塞入追踪信息,
     * 然后在Runnable#run中继续追踪即可.
     */
    public void invoke(Map<String, Object> context, Runnable callback){

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ignored) {
                }
                logger.info("handled");
                context.put("handled", "true");
                callback.run();
            }
        });

    }

}
