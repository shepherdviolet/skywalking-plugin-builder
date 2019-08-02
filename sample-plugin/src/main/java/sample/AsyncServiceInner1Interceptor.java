package sample;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * AsyncService$1 (匿名内部类Runnable) 拦截器.
 *
 * 这个异步示例不是常见情况, 是模拟相对比较棘手的情况, 假设ExecutorService无法被侵入(是Java核心类),
 * 且异步执行的Runnable还是匿名内部类, 我们尝试利用invoke方法的Map参数携带追踪信息,
 * 因为匿名内部类实际上会将Map作为成员变量持有, 可以反射获取到.
 *
 * 如果是一般的异步场景, 即可以侵入threadPool.execute方法的, 可以拦截这个方法, 在Runnable里塞入追踪信息,
 * 然后在Runnable#run中继续追踪即可.
 */
public class AsyncServiceInner1Interceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog logger = LogManager.getLogger(AsyncServiceInner1Interceptor.class);

    private Field mapField;

    /**
     * @param objInst 每个被侵入的类, 都会实现EnhancedInstance接口, 可以用来传递一个对象
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

        /*
            AsyncService的匿名内部类Runnable因为需要访问方法入参Map, Java实际上是给这个匿名内部类增加了一个成员变量去持有,
            所以我们可以遍历匿名内部类的成员变量获取AsyncService#invoke的入参Map.
         */
        Field mapField = this.mapField;
        //这里不需要锁, 多执行几次也没关系
        if (mapField == null) {
            Field[] fields = objInst.getClass().getDeclaredFields();
            for (Field field : fields) {
                //这里判断也比较简单, 根据类型, 如果存在同类型的, 还需要判断名称(不过名称是很奇怪的)
                if (field.getType().equals(Map.class)) {
                    field.setAccessible(true);
                    mapField = field;
                    this.mapField = field;
                }
            }
        }

        //重要: 必须先创建本地Span, 不然Context没有会导致下面continued方法抛出空指针
        AbstractSpan span = ContextManager.createLocalSpan(
//                "sample.basic.asyncmock.AsyncService$1#run()"
                "AsyncService$1#run()"
        );
        //必要: 组件定义, 这个光这里配置其实不够, 还要在SW的服务端配置对应信息, 否则只会看到Undefined
        span.setComponent(new OfficialComponent(1792, "SampleAsync"));

        //取方法入参Map
        Map<String, Object> map = (Map<String, Object>) mapField.get(objInst);

        //先继承context
        ContextManager.continued((ContextSnapshot) map.get("_skywalking_snapshot"));
        //再结束异步span, 这个异步SPAN统计了AsyncService#invoke开始到AsyncService$1#run开始的时间, 即任务从加入线程池到开始被执行之间的耗时
        ((AbstractSpan) map.get("_skywalking_span")).asyncFinish();
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        //结束当前SPAN
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
