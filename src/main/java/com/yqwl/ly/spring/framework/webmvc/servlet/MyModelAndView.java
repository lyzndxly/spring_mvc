package com.yqwl.ly.spring.framework.webmvc.servlet;

import java.util.Map;

public class MyModelAndView {
    private String viewName;

    private Map<String,?> model;

    public MyModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public MyModelAndView(String viewName,Map<String,Object> model){
        this.viewName = viewName;
        this.model =model;
    }
    public MyModelAndView() {

    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
