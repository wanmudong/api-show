package top.wanmudong.apishow.core;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.annotation.*;
import top.wanmudong.apishow.annotation.*;
import top.wanmudong.apishow.config.ApiShowProperties;
import top.wanmudong.apishow.model.*;
import top.wanmudong.apishow.utils.DataType;

import javax.xml.crypto.Data;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wanmudong
 * @date 20:08 2019/3/29
 */
public class ApiShowBuilder {

    private ConcurrentHashMap<String, Document> docMap = new ConcurrentHashMap<String, Document>(16);

    private ConcurrentHashMap<String, ApiModelDefinition> modelMap = new ConcurrentHashMap<>(64);

    private ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();


    public void addApiDoc(ApiDoc apiDoc, Class<?> apiDocClass, Object apiDocObject, ApiShowProperties properties){

        Document document = builderApiDoc(apiDoc, apiDocClass, properties);

        Method [] methods = apiDocClass.getDeclaredMethods();

        List<ApiDefinition> apiDefinitionList = new LinkedList<>();

        for (Method method : methods){

            if (!Modifier.isPublic(method.getModifiers())){

                method.setAccessible(true);

            }

            Api api = method.getAnnotation(Api.class);

            if (api == null){
                continue;
            }

            apiDefinitionList.add(builderApi(api, method, document.getBasePath()));
        }

        document.setApiList(apiDefinitionList);

        docMap.put(document.getName(), document);

    }


    private ApiDefinition builderApi(Api api, Method method, String basePath) {

        ApiDefinition apiDefinition = new ApiDefinition();

        apiDefinition.setName(getApiName(api, method));
        apiDefinition.setPath(getApiUrl(api, method, basePath));
        apiDefinition.setHttpMethod(getApiHttpMethod(api, method));

        apiDefinition.setRemark(api.remark());
        apiDefinition.setDescription(api.description());

        String demoResponse = api.demoResponse();

        String responseExample;
        try{

            responseExample = JSON.toJSONString(JSON.parseObject(demoResponse), true);

        }catch (Exception e){

            responseExample = demoResponse;

        }

        apiDefinition.setDemoResponse(responseExample);

        //构建requestParam
        ApiParam [] requestParams = api.requestParams();
        List<ApiParamDefinition> requestParamList = buildRequestApiParams(requestParams, method);
        apiDefinition.setRequestParams(requestParamList);

        //构建responseParam
        ApiParam [] responseParams = api.responseParams();
        List<ApiParamDefinition> responseParamList = buildResponseApiParams(responseParams, method);
        apiDefinition.setResponseParams(responseParamList);

        Class<?> resultModel = api.resultModel();

        if (Void.class.equals(resultModel)){

            resultModel = method.getReturnType();

        }

        String key = resultModel.getName();

        if (modelMap.containsKey(key)){

            apiDefinition.setResultModel(modelMap.get(key));

        }

        return apiDefinition;
    }

    private List<ApiParamDefinition> buildResponseApiParams(ApiParam[] apiParams, Method method) {

        List<ApiParamDefinition> apiParamDefinitionList = new LinkedList<>();

        ApiParamDefinition apiParamDefinition;

        Class<?> returnType = method.getReturnType();

        if(modelMap.containsKey(returnType.getName())){

            String returnTypeName = returnType.getSimpleName();

            apiParamDefinition = new ApiParamDefinition();

            apiParamDefinition.setType(DataType.MODEL.getValue());
            apiParamDefinition.setDescription("详细请参见数据字典-" + returnTypeName);
            apiParamDefinition.setName(returnTypeName.toLowerCase());
            apiParamDefinition.setRequired("是");
            apiParamDefinition.setExample("");
            apiParamDefinition.setDefaultValue("");

            apiParamDefinitionList.add(apiParamDefinition);

            return apiParamDefinitionList;
        }

        if (apiParams.length > 0){

            for (ApiParam apiParam : apiParams){

                apiParamDefinition = new ApiParamDefinition();

                apiParamDefinition.setName(apiParam.name());
                apiParamDefinition.setDescription(apiParam.description());
                apiParamDefinition.setExample(apiParam.example());
                apiParamDefinition.setRequired(apiParam.required() ? "是" : "否");
                apiParamDefinition.setType(apiParam.dataType().getValue());
                apiParamDefinition.setDefaultValue(apiParam.defaultValue());

                apiParamDefinitionList.add(apiParamDefinition);

            }

        }else {

            Field [] fields = returnType.getDeclaredFields();

            for (Field field : fields){

                apiParamDefinition = new ApiParamDefinition();

                apiParamDefinition.setType(DataType.getType(field.getType()));
                apiParamDefinition.setRequired("是");
                apiParamDefinition.setExample("");
                apiParamDefinition.setDescription("");
                apiParamDefinition.setDefaultValue("");

                apiParamDefinitionList.add(apiParamDefinition);

            }

        }

        return apiParamDefinitionList;
    }

    private List<ApiParamDefinition> buildRequestApiParams(ApiParam[] requestParams, Method method) {
        List<ApiParamDefinition> apiParamDefinitionsList = new LinkedList<>();

        ApiParamDefinition apiParamDefinition;

        if (requestParams.length > 0){

            for (ApiParam apiParam : requestParams){

                apiParamDefinition = new ApiParamDefinition();

                apiParamDefinition.setName(apiParam.name());
                apiParamDefinition.setDescription(apiParam.description());
                apiParamDefinition.setExample(apiParam.example());
                apiParamDefinition.setRequired(apiParam.required() ? "是" : "否");
                apiParamDefinition.setType(apiParam.dataType().getValue());
                apiParamDefinition.setDefaultValue(apiParam.defaultValue());

                apiParamDefinitionsList.add(apiParamDefinition);
            }

        }else {

            String [] parameterNames = discoverer.getParameterNames(method);

            Parameter [] parameters = method.getParameters();

            for (int i =1; i < parameterNames.length; i++){

                Parameter parameter = parameters[i];

                apiParamDefinition = new ApiParamDefinition();

                apiParamDefinition.setName(parameterNames[i]);
                apiParamDefinition.setRequired("是");
                apiParamDefinition.setExample("");
                apiParamDefinition.setDescription("");
                apiParamDefinition.setDefaultValue("");

                Class<?> parameterType = parameter.getType();

                if (String.class.equals(parameterType)){

                    apiParamDefinition.setType(DataType.STRING.getValue());

                }else if (Integer.class.equals(parameterType)){

                    apiParamDefinition.setType(DataType.INT.getValue());

                }else if (Long.class.equals(parameterType)){

                    apiParamDefinition.setType(DataType.LONG.getValue());

                }else {

                    String key = parameterType.getName();

                    if (modelMap.containsKey(key)){

                        apiParamDefinition.setDescription("详细信息请参见数据字典-" + parameterType.getSimpleName());
                        apiParamDefinition.setType(DataType.MODEL.getValue());

                    }else {

                        apiParamDefinition.setType(DataType.REFERENCE.getValue());
                        apiParamDefinition.setModel(buildReferenceModel(parameterType));

                    }

                }

                apiParamDefinitionsList.add(apiParamDefinition);

            }

        }

        return apiParamDefinitionsList;
    }

    private ApiModelDefinition buildReferenceModel(Class<?> referenceClass) {

        ApiModelDefinition apiModelDefinition = new ApiModelDefinition();

        apiModelDefinition.setRemark("");
        apiModelDefinition.setDescription("");

        Field [] fields = referenceClass.getDeclaredFields();

        List<ApiModelPropertyDefinition> propertyList = new LinkedList<>();

        ApiModelPropertyDefinition propertyDefinition;

        for (Field field : fields){

            propertyDefinition = new ApiModelPropertyDefinition();

            propertyDefinition.setType(DataType.getType(field.getType()));
            propertyDefinition.setName(field.getName());
            propertyDefinition.setRequired("是");
            propertyDefinition.setExample("");
            propertyDefinition.setDescription("");
            propertyDefinition.setDefaultValue("");

            propertyList.add(propertyDefinition);
        }

        apiModelDefinition.setPropertyList(propertyList);

        return apiModelDefinition;
    }

    /**
     * 获取到每个具体api方法的请求方法
     * method可以是注解中写的，也可以是api方法上对应的各类mapping注解的method的name
     * @param api api方法上的Api注解
     * @param method api方法
     * @return 具体api方法的请求方法
     */
    private String getApiHttpMethod(Api api, Method method) {

        String [] httpMethods = api.httpMethod();

        if (httpMethods.length > 0){

            return Arrays.toString(httpMethods);

        }

        RequestMapping rm = method.getAnnotation(RequestMapping.class);

        if (rm != null){

            return buildHttpMethod(rm.method());

        }

        GetMapping gm = method.getAnnotation(GetMapping.class);

        if (gm != null) {

            return buildHttpMethod(RequestMethod.GET);

        }

        PostMapping pm = method.getAnnotation(PostMapping.class);

        if (pm != null) {

            return buildHttpMethod(RequestMethod.POST);

        }

        DeleteMapping dm = method.getAnnotation(DeleteMapping.class);

        if (dm != null) {

            return buildHttpMethod(RequestMethod.DELETE);

        }

        PutMapping ptm = method.getAnnotation(PutMapping.class);

        if (ptm != null) {

            return buildHttpMethod(RequestMethod.PUT);

        }

        return "[GET,POST,PUT,DELETE]";
    }

    private String buildHttpMethod(RequestMethod... method) {

        int length = method.length;

        String [] httpMethod = new String[length];

        for (int i = 0; i < length; i++){

            httpMethod[i] = method[i].name();

        }
        return Arrays.toString(httpMethod);
    }

    /**
     * 获取到每个具体api方法的url
     * url可以是注解中写的，也可以是api方法上对应的各类mapping注解的value
     * @param api Api注解
     * @param method api方法
     * @param basePath api方法所在类的基础路径
     * @return 每个具体api方法的url
     */
    private String getApiUrl(Api api, Method method, String basePath) {

        String path = api.path();

        if (StringUtils.isNotEmpty(path)){

            return path;

        }

        RequestMapping rm = method.getAnnotation(RequestMapping.class);
        if (rm != null){
            return buildUrl(basePath, rm.value());
        }

        GetMapping gm = method.getAnnotation(GetMapping.class);
        if (gm != null) {
            return buildUrl(basePath, gm.value());
        }

        PostMapping pm = method.getAnnotation(PostMapping.class);
        if (pm != null) {
            return buildUrl(basePath, pm.value());
        }

        DeleteMapping dm = method.getAnnotation(DeleteMapping.class);
        if (dm != null) {
            return buildUrl(basePath, dm.value());
        }

        PutMapping ptm = method.getAnnotation(PutMapping.class);
        if (ptm != null) {
            return buildUrl(basePath, ptm.value());
        }

        return basePath;
    }


    private String buildUrl(String basePath, String[] paths) {

        StringBuffer url = new StringBuffer();

        for (String p : paths){

            url.append(basePath)
                    .append(p.startsWith("/") ? p : "/" + p)
                    .append(",");

        }

        url.deleteCharAt(url.length() - 1);

        return url.toString();
    }

    /**
     * 获取到每个具体api方法的名字
     * @param api
     * @param method
     * @return
     */
    private String getApiName(Api api, Method method) {

        String name = api.name();

        if (StringUtils.isNotEmpty(name)){
            return name;
        }

        return method.getName();
    }

    /**
     * 获取document实体
     * @param apiDoc
     * @param apiDocClass
     * @param properties
     * @return
     */
    private Document builderApiDoc(ApiDoc apiDoc, Class<?> apiDocClass, ApiShowProperties properties) {

        String docName = apiDocClass.getName();

        Document document = new Document();

        document.setBasePath(getApiBasePath(apiDoc, apiDocClass));
        document.setDescription(apiDoc.description());
        document.setName(docName);
        document.setContact(properties.getContact());
        document.setHost(properties.getHost());

        String version = apiDoc.version();

        if (StringUtils.isEmpty(version)){

            version = properties.getVersion();

        }

        document.setVersion(version);

        ApiCode [] apiCode = apiDoc.codes();

        List<ApiCodeDefinition> apiCodeDefinitionList = new LinkedList<>();

        for (ApiCode code : apiCode){

            apiCodeDefinitionList.add(new ApiCodeDefinition(code.code(), code.description()));

        }

        document.setCodeList(apiCodeDefinitionList);

        return document;
    }

    /**
     * 获取api的基础路径
     * @param apiDoc apiDoc注解实体
     * @param apiDocClass apiDoc注解所注解的类的Class对象
     * @return api基础路径
     */
    private String getApiBasePath(ApiDoc apiDoc, Class<?> apiDocClass) {
        String basePath = apiDoc.basePath();

        if (StringUtils.isNotEmpty(basePath)){

            return basePath.startsWith("/") ? basePath : "/" + basePath;

        }

        RequestMapping rm = apiDocClass.getAnnotation(RequestMapping.class);

        if (rm != null){

            String [] value = rm.value();

            return value[0];
        }

        return "";

    }

    public void addModel(ApiModel apiModel, Class<?> modelClass) {

        ApiModelDefinition apiModelDefinition = new ApiModelDefinition();

        apiModelDefinition.setName(modelClass.getSimpleName());
        apiModelDefinition.setDescription(apiModel.description());
        apiModelDefinition.setRemark(apiModel.remark());

        Field [] fields = modelClass.getDeclaredFields();

        List<ApiModelPropertyDefinition> propertyList = new LinkedList<>();

        for (Field field : fields){

            ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);

            if(apiModelProperty == null){
                continue;
            }

            ApiModelPropertyDefinition apiModelPropertyDefinition = new ApiModelPropertyDefinition();

            apiModelPropertyDefinition.setName(apiModelProperty.name());
            apiModelPropertyDefinition.setDefaultValue(apiModelProperty.defaultValue());
            apiModelPropertyDefinition.setDescription(apiModelProperty.description());
            apiModelPropertyDefinition.setExample(apiModelProperty.example());
            apiModelPropertyDefinition.setRequired(apiModelProperty.required() ? "是" : "否");
            apiModelPropertyDefinition.setType(apiModelProperty.dataType().getValue());

            propertyList.add(apiModelPropertyDefinition);

        }

        apiModelDefinition.setPropertyList(propertyList);

        modelMap.put(modelClass.getName(), apiModelDefinition);

    }

    public HashMap<String, Document> getDocMap() {

        return new HashMap<>(docMap);

    }

    public HashMap<String, ApiModelDefinition> getModelMap() {

        return new HashMap<>(modelMap);

    }
}
