package sample.basic.rpcmock;

import com.github.shepherdviolet.glacimon.java.concurrent.ThreadPoolExecutorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * RPC中间逻辑模拟
 *
 * @author S.Violet
 */
@Component
public class RpcPipeLine {

    private ExecutorService threadPool = ThreadPoolExecutorUtils.createFixed(5, "rpc-pipeline-%d");

    @Autowired
    private ProviderService service;

    public Map<String, Object> invoke(Map<String, Object> request) {
        //用异步模拟RPC调用
        Future<Map<String, Object>> future = threadPool.submit(() -> service.invoke(request));
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
