package com.lwz.seckill.controller;

import com.lwz.seckill.common.ResponseModel;
import com.lwz.seckill.entity.Item;
import com.lwz.seckill.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/item")
@CrossOrigin(origins = "${lwz.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 获取商品列表
     * @return List<Item> items
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getItemList() {
        List<Item> items = itemService.findItemsOnPromotion();
        return new ResponseModel(items);
    }

    /**
     * 获取商品详情
     * @param id
     * @return item
     */
    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getItemDetail(@PathVariable("id") int id) {
        // 从数据库中获取商品详情
        // Item item = itemService.findItemById(id);

        // 从缓存中获取商品详情
        Item item = itemService.findItemInCache(id);
        return new ResponseModel(item);
    }

}
