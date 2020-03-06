package com.yqwl.ly.spring.framework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class MyHandlerMapping {
    private Object controller;
    private Method method;
    private Pattern pattern;
    public MyHandlerMapping() {
    }

    public MyHandlerMapping(Pattern pattern, Object beanObject, Method method) {
        this.controller = beanObject;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
