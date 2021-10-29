package com.lwz.seckill.service.impl;

import com.lwz.seckill.common.BusinessException;
import com.lwz.seckill.common.ErrorCode;
import com.lwz.seckill.common.Toolbox;
import com.lwz.seckill.dao.OrderMapper;
import com.lwz.seckill.dao.SerialNumberMapper;
import com.lwz.seckill.entity.Item;
import com.lwz.seckill.entity.Order;
import com.lwz.seckill.entity.SerialNumber;
import com.lwz.seckill.entity.User;
import com.lwz.seckill.service.ItemService;
import com.lwz.seckill.service.UserService;
import com.lwz.seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class OrderServiceImpl implements OrderService, ErrorCode {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SerialNumberMapper serialNumberMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    /**
     * 格式：日期 + 流水
     * 示例：20210123000000000001
     * 事务传播性为REQUIRES_NEW：当创建订单事务回滚时，生成订单号的事务不会受到影响
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderID() {
        StringBuilder sb = new StringBuilder();

        // 拼入日期
        sb.append(Toolbox.format(new Date(), "yyyyMMdd"));

        // 获取流水号
        SerialNumber serial = serialNumberMapper.selectByPrimaryKey("order_serial");
        Integer value = serial.getValue();

        // 更新流水号
        serial.setValue(value + serial.getStep());
        serialNumberMapper.updateByPrimaryKey(serial);

        // 拼入流水号
        String prefix = "000000000000".substring(value.toString().length());
        sb.append(prefix).append(value);

        return sb.toString();
    }

    @Transactional
    public Order createOrder(int userId, int itemId, int amount, Integer promotionId) {
        // 校验参数
        if (amount < 1 || (promotionId != null && promotionId.intValue() <= 0)) {
            throw new BusinessException(PARAMETER_ERROR, "指定的参数不合法！");
        }

        // 校验用户
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new BusinessException(PARAMETER_ERROR, "指定的用户不存在！");
        }

        // 校验商品
        Item item = itemService.findItemById(itemId);
        if (item == null) {
            throw new BusinessException(PARAMETER_ERROR, "指定的商品不存在！");
        }

        // 校验库存
        int stock = item.getItemStock().getStock();
        if (amount > stock) {
            throw new BusinessException(STOCK_NOT_ENOUGH, "库存不足！");
        }

        // 校验活动
        if (promotionId != null) {
            if (item.getPromotion() == null) {
                throw new BusinessException(PARAMETER_ERROR, "指定的商品无活动！");
            } else if (!item.getPromotion().getId().equals(promotionId)) {
                throw new BusinessException(PARAMETER_ERROR, "指定的活动不存在！");
            } else if (item.getPromotion().getStatus() == 1) {
                throw new BusinessException(PARAMETER_ERROR, "指定的活动未开始！");
            }
        }

        // 扣减库存
        boolean successful = itemService.decreaseStock(itemId, amount);
        if (!successful) {
            throw new BusinessException(STOCK_NOT_ENOUGH, "库存不足！");
        }

        // 生成订单
        Order order = new Order();
        order.setId(this.generateOrderID());
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setPromotionId(promotionId);
        order.setOrderPrice(promotionId != null ? item.getPromotion().getPromotionPrice() : item.getPrice());
        order.setOrderAmount(amount);
        order.setOrderTotal(order.getOrderPrice().multiply(new BigDecimal(amount)));
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));
        orderMapper.insert(order);

        // 更新销量
        itemService.increaseSales(itemId, amount);

        return order;
    }

}
