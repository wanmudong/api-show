package top.wanmudong.apishow.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import top.wanmudong.apishow.annotation.ApiDoc;
import top.wanmudong.apishow.annotation.ApiModel;
import top.wanmudong.apishow.config.ApiShowProperties;
import top.wanmudong.apishow.model.ApiModelDefinition;
import top.wanmudong.apishow.model.Document;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * @author wanmudong
 * @date 20:05 2019/3/29
 */
public class ApiShowContext implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApiShowContext.class);

    private Executor executor;

    private ApiShowProperties properties;

    private static ApiShowBuilder apiShowBuilder;

    public ApiShowContext(ApiShowProperties properties) {
        this.properties = properties;
        apiShowBuilder = new ApiShowBuilder();
    }

    public static HashMap<String, Document> getDocMap() {
        return apiShowBuilder.getDocMap();
    }


    public static HashMap<String, ApiModelDefinition> getModelMap() {
        return apiShowBuilder.getModelMap();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(properties.isEnable()){
            scanApi(contextRefreshedEvent.getApplicationContext());
        }
    }

    /**
     * 进行api扫描
     * @param ctx
     */
    private void scanApi(ApplicationContext ctx) {
        Assert.notNull(ctx,"ApplicationContext不能为空");
        log.info("开始进行api扫描......");

        long start = System.currentTimeMillis();

        //初始化线程池
        initThreadPool(ctx);

        String basePath = ctx.getClass().getResource("/").getPath();
        basePath = basePath.endsWith("/") ? basePath : basePath + "/";

        String contrillerPackage = properties.getControllerPackage();

        if(StringUtils.isNotBlank(contrillerPackage)){

            String modelPackage = properties.getModelPackage();

            if (StringUtils.isNotBlank(modelPackage)){

                log.info("开始扫描model......");

                scanModel(basePath, modelPackage);

            }

            log.info("开始扫描controller......");

            scanController(ctx, basePath, contrillerPackage);

        }else {
            //TODO:全包扫描
        }

        executor.destroy();

        long end = System.currentTimeMillis();

        long time = end - start;

        log.info("扫描Api完毕，共耗时：" + time / 1000.0 + "秒");
    }

    /**
     *扫描controller
     */
    private void scanController(ApplicationContext ctx, String basePath, String contrillerPackage) {

        String filePath = basePath + contrillerPackage.replace(".", "/");

        File dir = new File(filePath);

        String [] classNames = dir.list(((dir1, name) -> name.endsWith(".class")));

        if(classNames != null && classNames.length > 0){

            String classFullName;

            for (String className : classNames){

                classFullName = contrillerPackage + "." + className.substring(0, className.lastIndexOf("."));

                try {
                    Class<?> aClass = Class.forName(classFullName);

                    ApiDoc apiDoc = aClass.getAnnotation(ApiDoc.class);

                    if (apiDoc != null){

                        Object apiDocObject = ctx.getBean(aClass);

                        executor.execute(() -> loadApi(aClass,apiDocObject,apiDoc));
                    }

                } catch (ClassNotFoundException e) {

                    log.warn("加载：{}类失败",classFullName,e);

                }
            }
        }
    }


    private void loadApi(Class<?> apiDocClass, Object apiDocObject, ApiDoc apiDoc) {

        apiShowBuilder.addApiDoc(apiDoc, apiDocClass, apiDocObject, properties);

    }

    /**
     * 扫描model
     */
    private void scanModel(String basePath, String modelPackage) {

        String filePath = basePath + modelPackage.replace(".", "/");

        File dir = new File(filePath);

        String [] classNames = dir.list((dir1, name) -> name.endsWith(".class"));

        if (classNames != null && classNames.length > 0){

            String classFullName;

            for(String className : classNames){

                classFullName = modelPackage + "." + className.substring(0, className.lastIndexOf("."));

                try{
                    Class<?> aClass = Class.forName(classFullName);

                    ApiModel apiModel = aClass.getAnnotation(ApiModel.class);

                    if (apiModel != null){

                        executor.execute(() -> loadModel(aClass, apiModel));

                    }
                } catch (ClassNotFoundException e) {

                    log.warn("加载：{}类失败",classFullName,e);

                }
            }


        }
    }

    /**
     * 加载model
     * @param modelClass
     * @param apiModel
     */
    private void loadModel(Class<?> modelClass, ApiModel apiModel) {

        apiShowBuilder.addModel(apiModel, modelClass);

    }

    /**
     * 初始化线程池
     * @param ctx
     */
    private void initThreadPool(ApplicationContext ctx) {
        ThreadPoolTaskExecutor taskExecutor = null;
        try{
            taskExecutor = ctx.getBean(ThreadPoolTaskExecutor.class);
        }catch (Exception e){
            //ignore
        }
        if (taskExecutor != null){
            executor = new Executor(taskExecutor);
            return;
        }

        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("api-thread-pool-%d")
                .daemon(false)
                .build();

        ThreadPoolExecutor poolTaskExecutor = new ThreadPoolExecutor(
                5,10,30,TimeUnit.SECONDS,new LinkedBlockingDeque<>(1024),threadFactory);
        executor = new Executor(poolTaskExecutor);

    }
    private static class Executor {

        ThreadPoolTaskExecutor taskExecutor;
        ThreadPoolExecutor poolExecutor;

        Executor(ThreadPoolTaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
            this.poolExecutor = null;
        }

        Executor(ThreadPoolExecutor poolExecutor) {
            this.taskExecutor = null;
            this.poolExecutor = poolExecutor;
        }

        void execute(Runnable task) {
            if (taskExecutor != null) {
                taskExecutor.execute(task);
            } else {
                poolExecutor.execute(task);
            }
        }

        void execute(Runnable task, long startTimeout) {
            this.execute(task);
        }

        Future<?> submit(Runnable task) {
            if (taskExecutor != null) {
                return taskExecutor.submit(task);
            } else {
                return poolExecutor.submit(task);
            }

        }

        <T> Future<T> submit(Callable<T> task) {
            if (taskExecutor != null) {
                return taskExecutor.submit(task);
            } else {
                poolExecutor.shutdown();
                return poolExecutor.submit(task);
            }
        }

        void destroy() {
            if (poolExecutor != null) {
                poolExecutor.shutdown();
                poolExecutor = null;
            }

            if (taskExecutor != null) {
                taskExecutor.shutdown();
            }
        }


    }
}
