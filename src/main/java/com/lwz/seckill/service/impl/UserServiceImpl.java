package com.lwz.seckill.service.impl;

import com.lwz.seckill.common.BusinessException;
import com.lwz.seckill.common.ErrorCode;
import com.lwz.seckill.component.ObjectValidator;
import com.lwz.seckill.dao.UserMapper;
import com.lwz.seckill.entity.User;
import com.lwz.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, ErrorCode {

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectValidator validator;

    @Autowired
    private RedisTemplate redisTemplate;

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

    @Override
    public User findUserByCache(int id) {
        if (id <= 0) {
            return null;
        }
        User user = null;
        String key = "user:" + id;
        // 查询redis
        user = (User) redisTemplate.opsForValue().get(key);
        if (user != null) {
            logger.debug("缓存命中 [" + user + "]");
            return user;
        }
        // 如果缓存没命中就查询数据库
        user = this.findUserById(id);
        if (user != null) {
            logger.debug("同步缓存 [" + user + "]");
            redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
            return user;
        }
        return null;
    }

}
