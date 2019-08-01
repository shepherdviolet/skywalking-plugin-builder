package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 入口
 *
 * @author Sviolet
 */
@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class//排除数据库配置(可选)
        }
)
@ComponentScan({
        "sample.basic.config",
})
public class SampleWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleWebApplication.class, args);
    }

    /**
     * Tomcat调优
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setAttribute("acceptorThreadCount", "2");
                connector.setAttribute("connectionTimeout", "30000");
                connector.setAttribute("asyncTimeout", "30000");
                connector.setAttribute("enableLookups", "false");
                connector.setAttribute("compression", "on");
                connector.setAttribute("compressionMinSize", "2048");
                connector.setAttribute("redirectPort", "8443");
            });
        };
    }

}
