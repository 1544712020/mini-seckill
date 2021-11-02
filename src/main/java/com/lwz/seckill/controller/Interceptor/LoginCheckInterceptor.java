package com.lwz.seckill.controller.Interceptor;

import com.alibaba.fastjson.JSONObject;
import com.lwz.seckill.common.ErrorCode;
import com.lwz.seckill.common.ResponseModel;
import com.lwz.seckill.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor, ErrorCode {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*从session中获取用户信息判断用户是否登录
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");*/

        // 从request中获取用户信息
        String token = request.getParameter("token");
        if (token == null || !redisTemplate.hasKey(token)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            PrintWriter writer = response.getWriter();
            Map<Object, Object> data = new HashMap<>();
            data.put("code", USER_NOT_LOGIN);
            data.put("message", "用户未登录！");
            ResponseModel model = new ResponseModel(ResponseModel.STATUS_FAILURE, data);
            writer.write(JSONObject.toJSONString(model));
            return false;
        }
        return true;
    }

}
