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

public class AsyncServiceInner1Interceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog logger = LogManager.getLogger(AsyncServiceInner1Interceptor.class);

    private Field mapField;

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        logger.info("before inner class");

        Field mapField = this.mapField;
        //不需要锁
        if (mapField == null) {
            Field[] fields = objInst.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(Map.class)) {
                    field.setAccessible(true);
                    mapField = field;
                    this.mapField = field;
                }
            }
        }

        Map<String, Object> map = (Map<String, Object>) mapField.get(objInst);

        //必须先创建本地Span, 不然Context没有会导致下面continued里空指针
        AbstractSpan span = ContextManager.createLocalSpan("sample.basic.asyncmock.AsyncService#lambda$invoke$0(Map, Runnable)");

        //先继承context
        ContextManager.continued((ContextSnapshot) map.get("_skywalking_snapshot"));
        //再结束异步span
        ((AbstractSpan) map.get("_skywalking_span")).asyncFinish();

        //如果Runnable(匿名内部类)也被侵入了, 就可以给它携带东西, 但是这里没用, 因为同一个线程
//        if (allArguments[1] instanceof EnhancedInstance) {
//            ((EnhancedInstance)allArguments[1]).setSkyWalkingDynamicField(something);
//        }

        span.setComponent(new OfficialComponent(1792, "SampleAsync"));
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        dealException(t);
    }

    private void dealException(Throwable throwable) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred();
        span.log(throwable);
    }

}
