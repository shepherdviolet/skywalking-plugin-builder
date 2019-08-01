package sample.basic.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sample.basic.asyncmock.AsyncService;
import sample.basic.rpcmock.ConsumerService;

import java.util.HashMap;

/**
 * 基本模板Controller
 *
 * 请求报文体和响应报文体可以是byte[]类型, 但是这种情况下, 需要自行处理MediaType(Content-Type), 返回头也得自行设置Content-Type
 *
 * @author S.Violet
 */
@RestController
public class BasicRestController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConsumerService service;

    @Autowired
    private AsyncService asyncService;

    /**
     * PRC插件
     * URL不能是空的, 会导致追踪报错
     */
    @RequestMapping("/")
    public String index() {
        logger.info("index");
        return service.invoke(new HashMap<>(16)) + "";
    }

    /**
     * 异步插件
     */
    @RequestMapping("/async")
    public String async() {
        logger.info("async");
        asyncService.invoke(new HashMap<>(16), new Runnable() {
            @Override
            public void run() {
                logger.info("runnable run");
            }
        });
        return "ok";
    }

}
