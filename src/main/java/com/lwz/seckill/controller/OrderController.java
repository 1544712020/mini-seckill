package com.lwz.seckill.controller;

import com.lwz.seckill.common.ResponseModel;
import com.lwz.seckill.common.ErrorCode;
import com.lwz.seckill.entity.User;
import com.lwz.seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/order")
@CrossOrigin(origins = "${nowcoder.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class OrderController implements ErrorCode {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel create(/*HttpSession session, */int itemId, int amount, Integer promotionId, String token) {
        // 从session中获取用户信息
        // User user = (User) session.getAttribute("loginUser");

        // 从redis中获取用户信息
        User user = (User) redisTemplate.opsForValue().get(token);
        orderService.createOrder(user.getId(), itemId, amount, promotionId);
        return new ResponseModel();
    }

}
