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
