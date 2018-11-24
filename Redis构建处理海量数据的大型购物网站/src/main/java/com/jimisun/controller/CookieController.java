/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: CookieController
 * Author:   jimisun
 * Date:     2018-11-24 12:23
 * Description: 进行Cookie管理
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.jimisun.controller;

import com.jimisun.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 〈一句话功能简述〉<br>
 * 〈进行Cookie管理〉
 *
 * @author jimisun
 * @create 2018-11-24
 * @since 1.0.0
 */
@Controller
@RequestMapping("/cookie")
public class CookieController {

    private static Map<String, User> userFactory = new HashMap<>();


    @Autowired
    private Jedis jedis;

    public static void main(String[] args) {
        updateToken(new Jedis(), UUID.randomUUID().toString(), new User("jimisun", "jimisun"), "123", "保健品1");
        updateToken(new Jedis(), UUID.randomUUID().toString(), new User("jimisun", "jimisun"), "123", "保健品2");
        updateToken(new Jedis(), UUID.randomUUID().toString(), new User("jimisun", "jimisun"), "123", "保健品3");
        updateToken(new Jedis(), UUID.randomUUID().toString(), new User("jimisun", "jimisun"), "123", "保健品4");
    }


    @RequestMapping("/LogOrReg")
    public String LogOrReg() {
        return "RegAndLog";
    }

    @RequestMapping("/register")
    public String register(User user, Model model) {
        userFactory.put(user.getUsername(), user);
        model.addAttribute("result", "注册成功!");
        return "RegAndLog";
    }

    @RequestMapping("/login")
    public String login(User user, Model model, @RequestParam(required = false) String token, HttpServletRequest request) {
        Boolean exists = jedis.exists("login:" + token);
        String clientIp = getClientIp(request);
        String url = getURL(request);
        if (!exists) {
            User result = userFactory.get(user.getUsername());
            if (result != null && user.getUsername().equals(result.getUsername()) && user.getPassword().equals(result.getPassword())) {
                /*将用户登录缓存到Redis*/
                String tokenUUID = UUID.randomUUID().toString();
                updateToken(jedis, tokenUUID, result, clientIp, url);
                /*获取用户的登录记录*/
                Set<String> IPList = jedis.zrange("recent:" + user.getUsername(), 0, -1);
                /*获取用户最新访问的页面*/
                Set<String> URLList = jedis.zrange("viewed:" + user.getUsername(), 0, -1);
                model.addAttribute("record", IPList);
                model.addAttribute("URLList", URLList);
                model.addAttribute("result", "登录成功!TOKEN为" + tokenUUID + ",30秒后过期.....30秒内可使用Token登录");
                return "RegAndLog";
            }
            model.addAttribute("result", "登录失败!");
            return "RegAndLog";
        }
        model.addAttribute("result", "使用Token登录成功!");
        return "RegAndLog";
    }


    private static void updateToken(Jedis jedis, String token, User user, String ip, String item) {
        long timestamp = System.currentTimeMillis() / 1000;
        jedis.sadd("login:" + token, user.getUsername());
        jedis.expire("login:" + token, 30);
        jedis.zadd("recent:" + user.getUsername(), timestamp, ip);
        if (item != null) {
            /*加入该用户最近浏览的有序集合中*/
            jedis.zadd("viewed:" + user.getUsername(), timestamp, item);
            /*剪裁最近浏览集合,仅仅保留最新*/
            jedis.zremrangeByRank("viewed:" + token, 0, -3);
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    private static String getURL(HttpServletRequest request) {
        return request.getRequestURI();
    }


}
