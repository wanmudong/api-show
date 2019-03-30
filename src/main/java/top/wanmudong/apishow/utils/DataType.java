package top.wanmudong.apishow.utils;

/**
 * @author wanmudong
 * @date 19:36 2019/3/29
 */
public enum  DataType {
    /**
     * 各种数据类型，用作数据模型以及Api参数的类型展示
     */

    BYTE("byte"),
    SHORT("short"),
    INT("integer"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    CHAR("char"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    OBJECT("object"),
    STRING("string"),
    MODEL("model"),
    REFERENCE("reference"),
    UNKNOW("");

    DataType(String v) {
        val = v;
    }

    private String val;

    public static String getType(Class<?> clazz){
        if (String.class.equals(clazz)){
            return STRING.getValue();
        }else if (Integer.class.equals(clazz)){
            return INT.getValue();
        }else if (Long.class.equals(clazz)){
            return LONG.getValue();
        }else{
            return clazz.getName();
        }
    }

    public String getValue() {
        return this.val;
    }

}
