package top.wanmudong.apishow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.wanmudong.apishow.core.ApiShowContext;

/**
 * @author wanmudong
 * @date 19:55 2019/3/29
 */
@Configuration
@ComponentScan(basePackages = "top.wanmudong.apishow.web")
@EnableConfigurationProperties({ApiShowProperties.class})
public class ApiShowAutoConfiguration {

    private ApiShowProperties apiShowProperties;

    public ApiShowAutoConfiguration(ApiShowProperties apiShowProperties){
        this.apiShowProperties = apiShowProperties;
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolTaskExecutor.class)
    public ThreadPoolTaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setKeepAliveSeconds(60);
        return taskExecutor;
    }

    @Bean
    public ApiShowContext apiShowContext(){
        return new ApiShowContext(apiShowProperties);
    }
}
