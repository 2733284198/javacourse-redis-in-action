/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: CarController
 * Author:   jimisun
 * Date:     2018-11-24 15:04
 * Description: 购物车控制器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.jimisun.controller;

import com.jimisun.domain.Item;
import com.jimisun.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈购物车控制器〉
 *
 * @author jimisun
 * @create 2018-11-24
 * @since 1.0.0
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private Jedis jedis;

    @GetMapping("/productlist.html")
    public String goProductList() {
        return "ProductList";
    }


    @GetMapping("/addCart")
    public String addCart(Item item, Model model) {
        User user = getUser();
        addTOCart(jedis, user, item);
        Map<String, String> cart = getCart(jedis, user);
        model.addAttribute("result", "添加购物车成功!");
        model.addAttribute("cartList", cart);
        return "ProductList";
    }


    private static void addTOCart(Jedis jedis, User user, Item item) {
        if (item.getNumber() <= 0) {
            jedis.hdel("cart:" + user.getUsername(), item.getProductName());
        } else {
            jedis.hset("cart:" + user.getUsername(), item.getProductName(), item.getNumber().toString());
        }
    }

    private static Map<String, String> getCart(Jedis jedis, User user) {
        return jedis.hgetAll("cart:" + user.getUsername());
    }


    private static User getUser() {
        return new User("jimisun", "jimisun");
    }


}
