package md5demo.util;

import org.apache.commons.codec.digest.DigestUtils;

//因为前端做了一次加盐加密，后端也做了一次加盐加密，所以在登录的时候需要做两次加盐加密进行匹配
public class MD5Util {
    public static String salt = "springboot";//这里要和前端的盐值一样,拿来做第一次加密

    public static String md5(String str){
       return DigestUtils.md5Hex(str);
    }

    //第一次加密，匹配前端的加密
    public static String inputToBack(String str){
        return md5(str+salt);
    }

    //第二次加密，做后端的数据库加密
    public static String backToDb(String str,String dbsalt){
        return md5(str + dbsalt);
    }

    //两次加密以后和数据库里面的作对比，如果一样就证明输入的密码正确
    public static String inputToDb(String str,String dbsalt){
       return backToDb(inputToBack(str),dbsalt);
    }

}
