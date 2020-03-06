package com.yqwl.ly.spring.framework.webmvc.servlet;


import java.io.File;


public class MyViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFX = ".html";

    private File templateDir;
    public MyViewResolver(String templateRoot) {
        String templateDirPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateDir = new File(templateDirPath);
    }

    public MyView resolveViewName(String viewName){
        if (null==viewName||"".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFX);

        File templateFile = new File((templateDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new MyView(templateFile);
    }

    public MyViewResolver(){}
}
