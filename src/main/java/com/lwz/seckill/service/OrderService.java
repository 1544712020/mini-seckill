package com.lwz.seckill.service;

import com.lwz.seckill.entity.Order;

public interface OrderService {

//    Order createOrder(int userId, int itemId, int amount, Integer promotionId);

    Order createOrder(int userId, int itemId, int amount, Integer promotionId, String itemStockLogId);

    void createOrderAsync(int userId, int itemId, int amount, Integer promotionId);

}
