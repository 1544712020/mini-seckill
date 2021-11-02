package com.lwz.seckill.controller;

import com.lwz.seckill.common.ResponseModel;
import com.lwz.seckill.common.Toolbox;
import com.lwz.seckill.common.BusinessException;
import com.lwz.seckill.common.ErrorCode;
import com.lwz.seckill.entity.User;
import com.lwz.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/user")
@CrossOrigin(origins = "${lwz.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class UserController implements ErrorCode {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户注册时获取验证码
     * @param phone
     * @param
     * @return ResponseModel
     */
    @RequestMapping(path = "/otp/{phone}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getOTP(@PathVariable("phone") String phone/*, HttpSession session*/) {
        // 生成OTP
        String otp = this.generateOTP();

        // 使用session绑定OTP
        // session.setAttribute(phone, otp);

        // 使用redis绑定OPT
        redisTemplate.opsForValue().set(phone, otp, 5, TimeUnit.MINUTES);
        // 发送OTP
        logger.info("[mini-sec-kill] 尊敬的{}您好, 您的注册验证码是{}, 请注意查收!", phone, otp);
        return new ResponseModel();
    }

    /**
     * 用户注册接口
     * @param otp
     * @param user
     * @param
     * @return ResponseModel
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel register(String otp, User user/*, HttpSession session*/) {
        // 从session中获取验证码以验证OTP
        // String realOTP = (String) session.getAttribute(user.getPhone());

        // 从redis中获取验证码以验证OPT
        String realOTP = (String) redisTemplate.opsForValue().get(user.getPhone());
        if (StringUtils.isEmpty(otp) || StringUtils.isEmpty(realOTP) || !StringUtils.equals(otp, realOTP)) {
            throw new BusinessException(PARAMETER_ERROR, "验证码不正确！");
        }
        // 加密处理
        user.setPassword(Toolbox.md5(user.getPassword()));
        // 注册用户
        userService.register(user);
        return new ResponseModel();
    }

    /**
     * 用户登录接口
     * @param phone
     * @param password
     * @param
     * @return ResponseModel
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel login(String phone, String password/*, HttpSession session*/) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }
        String md5pwd = Toolbox.md5(password);
        User user = userService.login(phone, md5pwd);

        // 用户登录后将用户的信息存储在session中
        // session.setAttribute("loginUser", user);

        // 创建token作为key将用户信息存储在redis中
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(token, user, 1, TimeUnit.DAYS);
        return new ResponseModel(token);
    }

    /**
     * 用户注销接口
     * @param
     * @return ResponseModel
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel logout(/*HttpSession session*/String token) {
        // 让session失效
        // session.invalidate();

        // 同过token删除redis中的用户信息
        if (!StringUtils.isEmpty(token)) {
            redisTemplate.delete(token);
        }
        return new ResponseModel();
    }

    /**
     * 获取登录用户信息
     * @param
     * @return ResponseModel
     */
    @RequestMapping(path = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getUser(/*HttpSession session*/String token) {
        // 从session中获取用户信息
        // User user = (User) session.getAttribute("loginUser");

        User user = null;
        // 从redis中获取用户信息
        if (!StringUtils.isEmpty(token)) {
            user = (User) redisTemplate.opsForValue().get(token);
        }
        // System.out.println("从redis中获取用户信息");
        return new ResponseModel(user);
    }

    /**
     * 生成验证码
     * @return 验证码
     */
    private String generateOTP() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
