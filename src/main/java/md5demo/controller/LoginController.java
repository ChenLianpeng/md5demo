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
            System.out.println("=======>"+ MD5Util.inputToDb(user.getPassword(), dbUser.getDbflag()));
            System.out.println("------->"+dbUser.getPassword());
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
