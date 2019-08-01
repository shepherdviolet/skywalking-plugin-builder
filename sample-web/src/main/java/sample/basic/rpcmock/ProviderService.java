package sample.basic.rpcmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProviderService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Map<String, Object> invoke(Map<String, Object> request) {
        logger.info("provider entry, request:" + request);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ignored) {
        }
        logger.info("provider exit");
        return new HashMap<>(16);
    }

}
