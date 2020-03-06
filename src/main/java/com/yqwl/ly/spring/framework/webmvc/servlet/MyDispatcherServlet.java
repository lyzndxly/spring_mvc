package com.yqwl.ly.spring.framework.webmvc.servlet;

import com.yqwl.ly.spring.framework.annotation.MyController;
import com.yqwl.ly.spring.framework.annotation.MyRequestMapping;
import com.yqwl.ly.spring.framework.annotation.MyRequestParam;
import com.yqwl.ly.spring.framework.context.MyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyDispatcherServlet extends HttpServlet {

    //
//    private Map<String,Method> handlerMapping = new HashMap<>();
    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<MyHandlerMapping,MyHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<MyViewResolver> myViewResolvers = new ArrayList<>();

    MyApplicationContext myApplicationContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }
    //运行阶段
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调度
        try {
            doDispatcher(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
//            resp.getWriter().write("500 Exception detail:"+ Arrays.toString(e.getStackTrace()));

            Map<String,Object> model = new HashMap<>();
            model.put("detail",e.getMessage());
            model.put("stackTrace",Arrays.toString(e.getStackTrace()));
            try {
                processDispatchResult(req,resp,new MyModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     *
     * @param req
     * @param resp
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {



        //1、根据URL拿到handlerMapping对象
        MyHandlerMapping myHandlerMapping = getHandler(req);
        if (myHandlerMapping==null){
            processDispatchResult(req,resp,new MyModelAndView("404"));
            return;
        }

        //2、根据handlerMapping拿到对应的handlerAdapter，动态参数匹配
        MyHandlerAdapter myHandlerAdapter = getHandlerAdapter(myHandlerMapping);
        //3、从handlerAdapter中拿到ModelAndView对象
        MyModelAndView myModelAndView = myHandlerAdapter.getHandler(req,resp,myHandlerMapping);
        //4、通过ViewResolver解析MyModelAndView对象，得到view对象或者Json
        processDispatchResult(req,resp,myModelAndView);




    }

    //区分json对象还是view
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView myModelAndView) throws Exception {
        if (null == myViewResolvers){return;}
        if (this.myViewResolvers.isEmpty()){return;}
        for (MyViewResolver myViewResolver : this.myViewResolvers) {
            MyView myView = myViewResolver.resolveViewName(myModelAndView.getViewName());

            myView.render(myModelAndView.getModel(),req,resp);
        }
    }

    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping myHandlerMapping) {

        if (this.handlerAdapters.isEmpty()){return null;}
        return this.handlerAdapters.get(myHandlerMapping);
    }

    private MyHandlerMapping getHandler(HttpServletRequest req) {

        if (this.handlerMappings.isEmpty()){return null;}
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();

        requestURI  = requestURI.replaceAll(contextPath, "").replaceAll("/+", "/");
        for (MyHandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(requestURI);
            if (!matcher.matches()){continue;}
            return handlerMapping;
        }

        return null;
    }

    @Override
    public void destroy() {
        System.out.println("destroy ......................s");
    }

    //初始化阶段
    @Override
    public void init(ServletConfig config) {

        try {
            myApplicationContext = new MyApplicationContext(config.getInitParameter("contextConfiguration"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        //===============MVC====================

        initStrategies(myApplicationContext);

        System.out.println("my spring framework is init ..............");
    }

    private void initStrategies(MyApplicationContext myApplicationContext) {
        initHandlerMapping(myApplicationContext);//通过handlerMapping请求映射代处理器
        initHandlerAdapters(myApplicationContext);//通过HandlerAdapters进行多类型的参数动态绑定
        initViewResolvers(myApplicationContext);//通过viewResolvers解析逻辑视图到具体的视图实现
    }

    private void initViewResolvers(MyApplicationContext myApplicationContext) {
        String templateRoot = myApplicationContext.getConfig().getProperty("templateRoot");

        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for ( File file1: templateRootDir.listFiles()) {
            this.myViewResolvers.add(new MyViewResolver(templateRoot));
        }


    }

    private void initHandlerAdapters(MyApplicationContext myApplicationContext) {

        for (MyHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new MyHandlerAdapter());
        }

    }

    private void initHandlerMapping(MyApplicationContext myApplicationContext) {
        if (myApplicationContext.getBeanDefinationCount()==0){return;}
        //遍历ioc容器
        String[] beanDefiantionNames = myApplicationContext.getBeanDefiantionNames();

        for (String beanDefiantionName : beanDefiantionNames) {

            Object beanObject = myApplicationContext.getBean(beanDefiantionName);
            Class<?> clazz = beanObject.getClass();

            if (!clazz.isAnnotationPresent(MyController.class)){ continue;}

            String baseUrl = "";
            //类上含有MyRequestMapping注解的类,获取其value
            if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping myRequestMappingAnnotation = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = myRequestMappingAnnotation.value();
            }
            //方法上含有的MyRequestMapping注解的，获取其value
            for (Method method:clazz.getMethods()){
                if(!method.isAnnotationPresent(MyRequestMapping.class)){return;}

                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);

                String regex = ("/"+ baseUrl+"/"+requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);

                handlerMappings.add(new MyHandlerMapping(pattern, beanObject, method));

                System.out.println("Mapped:----"+regex+",----"+method);
            }


        }
    }


    private String toLowerFirstCase(String clazzSimpleName){

        char[] chars = clazzSimpleName.toCharArray();
        char ch = chars[0];
        if (ch >= 'A' && ch <= 'Z') {
            chars[0]+=32;
            return String.valueOf(chars);
        }
        return clazzSimpleName;

    }


}
