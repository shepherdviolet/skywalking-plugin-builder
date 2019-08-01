package sample.basic.asyncmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
public class AsyncService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService threadPool = ThreadPoolExecutorUtils.createFixed(5, "caller-%d");

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
