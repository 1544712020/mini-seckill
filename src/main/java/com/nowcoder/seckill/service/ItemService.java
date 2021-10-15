package com.nowcoder.seckill.service;

import com.nowcoder.seckill.entity.Item;

import java.util.List;

public interface ItemService {

    List<Item> findItemsOnPromotion();

    Item findItemById(int id);

    boolean decreaseStock(int itemId, int amount);

    void increaseSales(int itemId, int amount);

}
