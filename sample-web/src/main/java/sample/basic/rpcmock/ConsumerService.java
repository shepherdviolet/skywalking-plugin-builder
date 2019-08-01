package sample.basic.rpcmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RpcPipeLine pipeLine;

    public Map<String, Object> invoke(Map<String, Object> request) {
        logger.info("consumer entry");
        return pipeLine.invoke(request);
    }

}
