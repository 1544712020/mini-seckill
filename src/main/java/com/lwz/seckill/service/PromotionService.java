package com.lwz.seckill.service;

public interface PromotionService {

    String generateToken(int userId, int itemId, int promotionId);

}
