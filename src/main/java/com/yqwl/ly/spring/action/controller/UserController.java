package com.yqwl.ly.spring.action.controller;

import com.yqwl.ly.spring.action.service.DemoServiceImpl;
import com.yqwl.ly.spring.framework.annotation.MyAutowired;
import com.yqwl.ly.spring.framework.annotation.MyController;
import com.yqwl.ly.spring.framework.annotation.MyRequestMapping;
import com.yqwl.ly.spring.framework.annotation.MyRequestParam;
import com.yqwl.ly.spring.framework.webmvc.servlet.MyModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@MyController
@MyRequestMapping("/user")
public class UserController {

    @MyAutowired
    DemoServiceImpl demoService;

    @MyRequestMapping("query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @MyRequestParam("name")String name,
//                      @MyRequestParam("id") int id,
                      @MyRequestParam("addr") String addr){

        String s = demoService.get(name,addr);
        try {
            resp.getWriter().write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/first.html")
    public MyModelAndView query(@MyRequestParam("teacher") String teacher){
        HashMap<String, Object> model = new HashMap<>();
        model.put("teacher",teacher);
        model.put("data","188888888888");
        model.put("token","123456");

        return new MyModelAndView("first.html",model);
    }
}
