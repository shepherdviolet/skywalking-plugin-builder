package sample;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * RPC追踪示例: RpcProvider拦截器
 *
 * @author S.Violet
 */
public class RpcProviderInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {

        //取方法入参Map
        Map<String, Object> arg0 = (Map<String, Object>) allArguments[0];

        //这个示例从Map获取追踪信息
        ContextCarrier contextCarrier = new ContextCarrier();
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            next.setHeadValue((String) arg0.get(next.getHeadKey()));
        }

        //创建EntrySpan
        AbstractSpan span = ContextManager.createEntrySpan(
                //操作名称, 即在SW界面上显示的信息
                "Provider:sample.basic.rpcmock.ProviderService#invoke",
                //ContextCarrier
                contextCarrier
        );

        //必要: 组件定义, 这个光这里配置其实不够, 还要在SW的服务端配置对应信息, 否则只会看到Undefined
        span.setComponent(new OfficialComponent(1791, "SampleRpc"));
        //RPC插件需要这个: URL
        Tags.URL.set(span, "my-protocol://remote-host:8080/sample.basic.rpcmock.ProviderService#invoke(Map)");
        //RPC插件需要这个: 标记为RPC
        SpanLayer.asRPCFramework(span);
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
