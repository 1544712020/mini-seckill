package com.lwz.seckill.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lwz.seckill.common.BusinessException;
import com.lwz.seckill.common.ErrorCode;
import com.lwz.seckill.component.ObjectValidator;
import com.lwz.seckill.dao.ItemMapper;
import com.lwz.seckill.dao.ItemStockLogMapper;
import com.lwz.seckill.dao.ItemStockMapper;
import com.lwz.seckill.dao.PromotionMapper;
import com.lwz.seckill.entity.Item;
import com.lwz.seckill.entity.ItemStock;
import com.lwz.seckill.entity.ItemStockLog;
import com.lwz.seckill.entity.Promotion;
import com.lwz.seckill.service.ItemService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService, ErrorCode {

    private Logger logger = (Logger) LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private PromotionMapper promotionMapper;

    @Autowired
    private ItemStockLogMapper itemStockLogMapper;

    @Autowired
    private ObjectValidator validator;

    @Autowired
    private RedisTemplate redisTemplate;

    // guava本地缓存
    private Cache<String, Object> cache;

    // 初始化本地缓存
    @PostConstruct
    private void init() {
        cache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public List<Item> findItemsOnPromotion() {
        List<Item> items = itemMapper.selectOnPromotion();
        // 使用stream流将在活动中的商品添加上库存以及活动
        return items.stream().map(item -> {
            // 查库存
            ItemStock stock = itemStockMapper.selectByItemId(item.getId());
            item.setItemStock(stock);
            // 查活动
            Promotion promotion = promotionMapper.selectByItemId(item.getId());
            if (promotion != null && promotion.getStatus() == 0) {
                item.setPromotion(promotion);
            }
            return item;
        }).collect(Collectors.toList());
    }

    public Item findItemById(int id) {
        // 查商品
        Item item = itemMapper.selectByPrimaryKey(id);
        // 查库存
        ItemStock stock = itemStockMapper.selectByItemId(id);
        item.setItemStock(stock);
        // 查活动
        Promotion promotion = promotionMapper.selectByItemId(id);
        if (promotion != null && promotion.getStatus() == 0) {
            item.setPromotion(promotion);
        }
        return item;
    }

    /**
     * 从redis缓存中查找商品详情
     * @param id
     * @return
     */
    @Override
    public Item findItemInCache(int id) {
        if (id < 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }
        Item item = null;
        String key = "item:" + id;
        // 一级缓存中查找
        item = (Item) cache.getIfPresent(key);
        if (item != null) {
            return item;
        }
        // 二级缓存中查找
        item = (Item) redisTemplate.opsForValue().get(key);
        if (item != null) {
            cache.put(key, item);
            return item;
        }
        // mysql中查找
        item = this.findItemById(id);
        if (item != null) {
            cache.put(key, item);
            redisTemplate.opsForValue().set(key, item, 3, TimeUnit.MINUTES);
            return item;
        }
        return null;
    }

    @Transactional
    public boolean decreaseStock(int itemId, int amount) {
        int rows = itemStockMapper.decreaseStock(itemId, amount);
        return rows > 0;
    }

    @Transactional
    public void increaseSales(int itemId, int amount) {
        itemMapper.increaseSales(itemId, amount);
    }

    /**
     * 从缓存中扣减商品库存
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    public boolean decreaseStockInCache(int itemId, int amount) {
        if (itemId < 0 || amount < 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }
        String key = "item:stock:" + itemId;
        Long result = redisTemplate.opsForValue().decrement(key, amount);
        if (result < 0) {
            // 回补库存
            this.increaseStockInCache(itemId, amount);
            logger.debug("回补库存完成 [" + itemId + "]");
        } else if (result == 0) {
            // 售罄标识
            redisTemplate.opsForValue().set("item:stock:over:" + itemId, 1);
            logger.debug("售罄标识完成 [" + itemId + "]");
        }
        return result >= 0;
    }

    /**
     * 用于生成订单流水信息
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    public ItemStockLog createItemStockLog(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        ItemStockLog log = new ItemStockLog();
        log.setId(UUID.randomUUID().toString().replace("-", ""));
        log.setItemId(itemId);
        log.setAmount(amount);
        log.setStatus(0);

        itemStockLogMapper.insert(log);

        return log;
    }

    /**
     * 从缓存中添加商品库存
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    public boolean increaseStockInCache(int itemId, int amount) {
        if (itemId < 0 || amount < 0) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }
        String key = "item:stock:" + itemId;
        redisTemplate.opsForValue().increment(key, amount);
        return true;
    }

    @Override
    public void updateItemStockLogStatus(String id, int status) {
        ItemStockLog log = itemStockLogMapper.selectByPrimaryKey(id);
        log.setStatus(status);
        itemStockLogMapper.updateByPrimaryKey(log);
    }

    @Override
    public ItemStockLog findItemStorkLogById(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        return itemStockLogMapper.selectByPrimaryKey(id);
    }

}
