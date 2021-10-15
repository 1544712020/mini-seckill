package com.nowcoder.seckill.controller;

import com.nowcoder.seckill.common.ErrorCode;
import com.nowcoder.seckill.common.ResponseModel;
import com.nowcoder.seckill.entity.User;
import com.nowcoder.seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseModel create(
            HttpSession session, int itemId, int amount, Integer promotionId) {
        User user = (User) session.getAttribute("loginUser");
        orderService.createOrder(user.getId(), itemId, amount, promotionId);
        return new ResponseModel();
    }

}
