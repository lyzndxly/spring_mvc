package com.yqwl.ly.spring.framework.webmvc.servlet;

import com.yqwl.ly.spring.framework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.annotation.Annotation;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyHandlerAdapter {

    public MyHandlerAdapter() {
    }

    public MyModelAndView getHandler(HttpServletRequest req, HttpServletResponse resp, MyHandlerMapping myHandlerMapping) throws InvocationTargetException, IllegalAccessException {

        //        request.getServletPath()-----/user/register.action
        //        request.getContextPath()-----/testWeb
        //        request.getRequestURI()-----/testWeb/user/register.action
        //        request.getRequestURL()-----http://localhost:8080/testWeb/user/register.action
//        String requestURI = req.getRequestURI();
//        String contextPath = req.getContextPath();
//
//        requestURI = requestURI.replace(contextPath, "").replaceAll("/+", "/");

        //判断前端请求的地址 handlerMapping中是否存在
//        if (!this.handlerMapping.containsKey(requestURI)){
//                try {
//                    resp.getWriter().write("404 not found!!");
//                    return;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//        }
        //拼接形参列表
        //保存controller方法上对应参数的位置保存下来。
        Map<String,Integer> paramIndexMapping = new HashMap<>();







        //如果是字符串类型，可能有多个参数，反射获取参数注解数组->二维数组
        Annotation[][] parameterAnnotations = myHandlerMapping.getMethod().getParameterAnnotations();
        //遍历这个二维数组，第一维是注解类型，二维是注解中的参数
        for ( int i = 0;i < parameterAnnotations.length;i++){
            //获取第I个注解并
            for (Annotation a:parameterAnnotations[i]){
                //判断注解类型，并获取注解中的值
                if(a instanceof MyRequestParam){
                    String paramName = ((MyRequestParam) a).value();

                    if (!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);

                    }
                }
            }
        }

        //反射获取方法中的参数类型数组
        Class<?>[] parameterTypes = myHandlerMapping.getMethod().getParameterTypes();

        //遍历获取参数类型并赋值
        for (int i = 0;i < parameterTypes.length;i++) {
            //获取第I个参数类型并赋值
            Class<?> parameterType = parameterTypes[i];
            //判断第I个参数类型并赋值
            if(parameterType == HttpServletRequest.class||parameterType ==HttpServletResponse.class){
                paramIndexMapping.put(parameterType.getName(),i);

            }

        }


        //获取前端传递的参数 URL中的
        Map<String,String[]> parameterMap = req.getParameterMap();

        Object[] parameValues = new Object[parameterTypes.length];

        for (Map.Entry<String,String[]> entry : parameterMap.entrySet()) {
            String value = Arrays.toString(parameterMap.get(entry.getKey()))
                    .replaceAll("\\[\\]","")
                    .replaceAll("\\s",",");
            if (!parameterMap.containsKey(entry.getKey())){continue;}

            Integer index = paramIndexMapping.get(entry.getKey());

            parameValues[index] = caseStringValue(value,parameterTypes[index]);



        }

        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            Integer index = paramIndexMapping.get(HttpServletRequest.class.getName());

            parameValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            Integer index = paramIndexMapping.get(HttpServletResponse.class.getName());

            parameValues[index] = resp;
        }

        Object result = myHandlerMapping.getMethod().invoke(myHandlerMapping.getController(), parameValues);

        if (result==null||result instanceof Void){return  null;}

        Boolean isModelAndView = myHandlerMapping.getMethod().getReturnType()==MyModelAndView.class;

        if (isModelAndView){
            return (MyModelAndView)result;
        }
        return  null;
    }

    /**
     *  类型转换
     * @param value
     * @param parameterType
     * @return
     */
    private Object caseStringValue(String value, Class<?> parameterType) {
        if (String.class ==parameterType){
            return value;
        }else if (Integer.class==parameterType){
            return Integer.valueOf(value);
        }else if(Double.class==parameterType){
            return Double.valueOf(value);
        }else {
            //暂不做处理，后续在扩展
            if (value!=null){
                return value;
            }
        }

        return null;
    }
}
