package com.yqwl.ly.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyView {
    private File viewFile;

    public MyView() {
    }

    public MyView(File templateFile) {
        this.viewFile=templateFile;
    }

    public void render(Map<String,?> model, HttpServletRequest req, HttpServletResponse resp) throws Exception{
        StringBuffer stringBuffer = new StringBuffer();
        RandomAccessFile accessFile = new RandomAccessFile(this.viewFile, "r");

        String line = null;

        while(null!=(line=accessFile.readLine())){
            line= new String(line.getBytes("ISO-8859-1"),"UTF-8");
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);

            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                String paraName = matcher.group();
                paraName = paraName.replaceAll("￥\\{|\\}", "");

                Object paramValue = model.get(paraName);

                if (null==paramValue){return;}

                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                matcher = pattern.matcher(line);

            }


            stringBuffer.append(line);
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write(stringBuffer.toString());

        }
    }

    public static String makeStringForRegExp(String str){
        return str.replace("\\","\\\\").replace("*","\\*")
                .replace("+","\\+").replace("|","\\|")
                .replace("{","\\{").replace("}","\\}")
                .replace("(","\\(").replace(")","\\)")
                .replace("^","\\^").replace("$","\\$")
                .replace("[","\\[").replace("]","\\]")
                .replace("?","\\?").replace(",","\\,")
                .replace(".","\\.").replace("&","\\&");
    }

}
