# 为什么要用MD5加密？

当我们的数据库存储的都是明文密码时，一旦你的数据库被截取或破解，就会导致用户的信息暴露，我们采用MD5加密方式来降低这个风险，即使你的数据库被盗取了，攻击者也不会那么轻易就能得到账户和密码。

**MD5信息摘要算法**（英语：MD5 Message-Digest Algorithm），一种被广泛使用的[密码散列函数](https://baike.baidu.com/item/密码散列函数/14937715)，可以产生出一个128位（16[字节](https://baike.baidu.com/item/字节/1096318)）的散列值（hash value），用于确保信息传输完整一致。MD5由美国密码学家[罗纳德·李维斯特](https://baike.baidu.com/item/罗纳德·李维斯特/700199)（Ronald Linn Rivest）设计，于1992年公开，用以取代[MD4](https://baike.baidu.com/item/MD4/8090275)算法。这套算法的程序在 RFC 1321 标准中被加以规范。1996年后该算法被证实存在弱点，可以被加以破解，对于需要高度安全性的数据，专家一般建议改用其他算法，如[SHA-2](https://baike.baidu.com/item/SHA-2/22718180)。2004年，证实MD5算法无法防止碰撞（collision），因此不适用于安全性认证，如[SSL](https://baike.baidu.com/item/SSL/320778)公开密钥认证或是[数字签名](https://baike.baidu.com/item/数字签名/212550)等用途。----《百度百科》



**本例子采用前后端都加密的双重加密方式，前端采用的是JQuery提供的加密方法（jquery.md5.js），后端加密采用Apache提供的commons包**



将前端用户输入的密码用JQuery进行第一次加密

## 前端注册页面

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org"
>
<head>
    <meta charset="UTF-8">
    <title>注册</title>
    <link th:href="@{/css/bootstrap.min.css}" rel="stylesheet"/>
</head>
<body>
<div class="container" style="text-align:center;margin-top:50px;">
    <div class="row col-md-6 col-md-offset-3">
        <div class="panel panel-default">
            <div class="panel-body">
                <form id="registerForm"  th:action="@{/register}" th:object="${user}" method="post">
                    <div class="input-group">
                        <span class="input-group-addon">username</span>
                        <input id="username" type="text" th:field="*{username}" class="form-control" placeholder="用户名" >
                    </div>
                    <div><span th:if="${#fields.hasErrors('username')}" th:errors="*{username}" style="color:red;"></span></div>
                    <br>
                    <div class="input-group">
                        <span class="input-group-addon">password</span>
                        <input id="password" type="password" name="password" class="form-control" placeholder="密码" >
                    </div>
                    <div><span th:if="${#fields.hasErrors('password')}" th:errors="*{password}" style="color:red;"></span></div>
                    <br>
                    <br>
                    <button type="submit" class="btn btn-primary register-btn">注册</button>
                    <button type="reset" class="btn btn-warning">重置</button>
                </form>
            </div>
        </div>
    </div>
</div>
<script th:src="@{/js/jquery-3.1.1.js}"></script>
<script th:src="@{/js/jquery.validate.min.js}"></script>
<script th:src="@{/js/additional-methods.min.js}"></script>
<script th:src="@{/js/messages_zh.min.js}"></script>
<script th:src="@{/js/jquery.md5.js}"></script>
<script type="text/javascript">
    $("#registerForm").validate({ //这里前端采用jQuery的表单验证，进行一些简单地输入校验
        rules: {
            username: "required",
            password: {
                required: true,
                minlength:4
            },
        },
        submitHandler: function(form) {
            var salt = 'springboot';
            var newPassword = $.md5($("#password").val()+salt); //md5盐值加密
            $("#password").val(newPassword);
            form.submit();
        }
    });
</script>
</body>
</html>

```

前端页面采用jQuery提供的validate检测表单的基本输入。**这里值得一提的是，在提交表单的时候，采用jQuery提供的jquery.md5.js来进行md5加密，写法如注册页面的submitHandler函数：**

```html
var salt = 'springboot'; //设定一个盐值，这个盐值可以是任意的，用来在加密的时候插入明文中
var newPassword = $.md5($("#password").val()+salt); //md5盐值加密
```





## 后端MD5工具类

```java
package md5demo.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
    public static String salt = "springboot";//这里要和前端的盐值一样,拿来做第一次加密

    public static String md5(String str){
       return DigestUtils.md5Hex(str);
    }

    //因为前端做了一次加盐加密，后端也做了一次加盐加密，所以在登录的时候需要做两次加盐加密进行匹配
    //两次加密后的密文如果和数据库中的密文一样的话，证明输入的是正确的密码

    //第一次加密，前端传过来的加密
    public static String inputToBack(String str){
        return md5(str+salt);
    }

    //第二次加密。第二次加密会用前端传过来的密文再加密一次
    public static String backToDb(String str,String dbsalt){
        return md5(str + dbsalt);
    }

    //两次加密以后和数据库里面的作对比，如果一样就证明输入的密码正确
    public static String inputToDb(String str,String dbsalt){
       return backToDb(inputToBack(str),dbsalt);
    }
}
```





## 实现注册功能：

```java
package md5demo.controller;

import md5demo.model.User;
import md5demo.service.UserService;
import md5demo.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class RegisterController {

    private static Logger log = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    public UserService userService;

    //跳转到注册页面
    @RequestMapping(value="/register", method=RequestMethod.GET)
    public String toRegister(Model model){
        model.addAttribute("user",new User());
        return "register";
    }

    //拦截请求注册的表单
    @RequestMapping(value="/register", method=RequestMethod.POST)
    public ModelAndView register(@ModelAttribute(value="user") @Valid User user, BindingResult bindingResult){
        log.info("username="+user.getUsername()+";password="+user.getPassword());
        if(bindingResult.hasErrors()){
            return new ModelAndView("register");
        }
        String salt = "hello";//加入的盐
        System.out.println("前端加密（第一次加密）："+user.getPassword());
        String newPassword = MD5Util.backToDb(user.getPassword(), salt);
        System.out.println("后端加密（第二次加密）:"+newPassword);
        user.setId(2021);
        user.setPassword(newPassword);
        user.setDbflag(salt);
        userService.register(user);
        return new ModelAndView("register");
    }
}
```



其中，从前端获取到的password拿过来再用后端的MD5Util进行二次加密

![image-20210521001150157](Custom%20Themes.assets/image-20210521001150157.png)

输入账号和密码：

小泽又沐风

123456



### 控制台输出：

![image-20210521001317711](Custom%20Themes.assets/image-20210521001317711.png)

### 数据库写入数据：

![image-20210521001405569](Custom%20Themes.assets/image-20210521001405569.png)



这里我们可以看到，前端传过来的数据进行二次加密以后写入到数据库的形式是这样！



## 实现登录功能：

```java
package md5demo.controller;

import md5demo.model.User;
import md5demo.service.UserService;
import md5demo.util.MD5Util;
import md5demo.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@Controller
public class LoginController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    public UserService userService;

    @RequestMapping(value="/login", method=RequestMethod.GET)
    public String login(Model model){
        model.addAttribute("user",new User());
        return "login";
    }

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public String login(@ModelAttribute(value="user") @Valid UserVO user, BindingResult bindingResult) {
        log.info("username=" + user.getUsername() + ";password=" + user.getPassword());
        if (bindingResult.hasErrors()) {
            return "login";
        }
        User dbUser = userService.getUser(user.getUsername());
        if(dbUser != null){
            System.out.println("前端输送过来的代码："+ MD5Util.inputToDb(user.getPassword(), dbUser.getDbflag()));
            System.out.println("数据库得到的代码："+dbUser.getPassword());
            if(dbUser.getPassword().equals(MD5Util.inputToDb(user.getPassword(), dbUser.getDbflag()))){
               return "home";
            }else{
                return "login";
            }
        }else{
            return "login";
        }
    }
}
```

进入登录页面，输入正确的账号密码：

![image-20210521002041801](Custom%20Themes.assets/image-20210521002041801.png)

### 控制台输出：

![image-20210521002117164](Custom%20Themes.assets/image-20210521002117164.png)



然后我们输入错误的密码

### 控制台输出：

![image-20210521002203747](Custom%20Themes.assets/image-20210521002203747.png)





以上就是用md5进行简单的密码加密过程：

前端模板用的是Thymeleaf，后端持久层框架是SpringDataJPA