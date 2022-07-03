package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody  User user, HttpSession session){

        // 第一步 获取手机号
        String phone = user.getPhone();
        if(StringUtils.hasLength(phone)){
            // 生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code= {}",code);

            // 调用阿里云的短信服务API
            SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            // 把生成的验证码存起来 Session
            session.setAttribute("phone",code);
            return R.success("手机验证码发送成功");
        }
        return R.error("手机验证码发送失败");
    }
    // 移动端用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();

        // 从 Session 中获取保存的验证码
        String codeInSession = (String) session.getAttribute("phone");
        // 进行验证码的比对 (页面提交的验证码和Session中保存的验证码进行比对)
        log.info(codeInSession);
        if(codeInSession != null && codeInSession.equals(code)){
            //如果能够登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

            //判断当前用户是否是新用户
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                // 判断当前用户为新用户， 如果是空就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
