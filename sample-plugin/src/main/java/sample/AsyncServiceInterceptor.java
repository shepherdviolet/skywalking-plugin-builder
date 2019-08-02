package sample;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * AsyncService拦截器.
 *
 * 这个异步示例不是常见情况, 是模拟相对比较棘手的情况, 假设ExecutorService无法被侵入(是Java核心类),
 * 且异步执行的Runnable还是匿名内部类, 我们尝试利用invoke方法的Map参数携带追踪信息,
 * 因为匿名内部类实际上会将Map作为成员变量持有, 可以反射获取到.
 *
 * 如果是一般的异步场景, 即可以侵入threadPool.execute方法的, 可以拦截这个方法, 在Runnable里塞入追踪信息,
 * 然后在Runnable#run中继续追踪即可.
 */
public class AsyncServiceInterceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog logger = LogManager.getLogger(AsyncServiceInterceptor.class);

    /**
     * @param objInst 每个被侵入的类, 都会实现EnhancedInstance接口, 可以用来传递一个对象
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

        //创建本地SPAN
        AbstractSpan span = ContextManager.createLocalSpan("sample.basic.asyncmock.AsyncService#invoke(Map, Runnable)");
        //必要: 组件定义, 这个光这里配置其实不够, 还要在SW的服务端配置对应信息, 否则只会看到Undefined
        span.setComponent(new OfficialComponent(1792, "SampleAsync"));

        //取方法入参Map
        Map<String, Object> arg0 = (Map<String, Object>) allArguments[0];

        //跨线程传递追踪信息不需要用ContextCarrier, 用ContextSnapshot即可
        arg0.put("_skywalking_snapshot", ContextManager.capture());
        //可选: 将SPAN用作异步统计, 这个SPAN会在AsyncServiceInner1Interceptor里结束, 目的是统计任务从加入线程池到开始被执行之间的耗时
        arg0.put("_skywalking_span", span.prepareForAsync());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        //结束当前SPAN, 这里其实没结束, 因为之前做了span.prepareForAsync()
        ContextManager.stopSpan();
        /*
            如果需要, 还可以在这里处理异常的情况
         */
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        dealException(t);
    }

    private void dealException(Throwable throwable) {
        //获取当前SPAN, 记录错误
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred();
        span.log(throwable);
    }

}
