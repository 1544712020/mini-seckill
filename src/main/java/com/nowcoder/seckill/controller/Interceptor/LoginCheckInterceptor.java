package com.nowcoder.seckill.controller.Interceptor;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.seckill.common.ErrorCode;
import com.nowcoder.seckill.common.ResponseModel;
import com.nowcoder.seckill.entity.User;
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

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
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
