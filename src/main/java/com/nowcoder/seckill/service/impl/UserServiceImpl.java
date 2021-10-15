package com.nowcoder.seckill.service.impl;

import com.nowcoder.seckill.common.BusinessException;
import com.nowcoder.seckill.common.ErrorCode;
import com.nowcoder.seckill.component.ObjectValidator;
import com.nowcoder.seckill.dao.UserMapper;
import com.nowcoder.seckill.entity.User;
import com.nowcoder.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService, ErrorCode {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectValidator validator;

    @Transactional
    public void register(User user) {
        if (user == null) {
            throw new BusinessException(PARAMETER_ERROR, "参数不能为空！");
        }

        Map<String, String> result = validator.validate(user);
        if (result != null && result.size() > 0) {
            throw new BusinessException(PARAMETER_ERROR,
                    StringUtils.join(result.values().toArray(), ", ") + "！");
        }

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(PARAMETER_ERROR, "该手机号已注册！");
        }
    }

    public User login(String phone, String password) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        User user = userMapper.selectByPhone(phone);
        if (user == null || !StringUtils.equals(password, user.getPassword())) {
            throw new BusinessException(USER_LOGIN_FAILURE, "账号或密码错误！");
        }

        return user;
    }

    public User findUserById(int id) {
        return userMapper.selectByPrimaryKey(id);
    }

}
