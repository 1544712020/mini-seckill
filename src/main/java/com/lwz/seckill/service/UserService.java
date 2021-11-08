package com.lwz.seckill.service;

import com.lwz.seckill.entity.User;

public interface UserService {

    void register(User user);

    User login(String phone, String password);

    User findUserById(int id);

    User findUserByCache(int id);

}
