package com.an.springplusplus.core.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/21 7:51 上午
 * @description
 */
public class StringUtils {

    public static String toUpperCaseFirst(String str){
        char[] chars = str.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char)(chars[0] - 32);
        }
        return new String(chars);
    }


    /**
     * 驼峰转下划线
     * @param humpString
     * @return
     */
    static
    public String humpToUnderline(String humpString) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(humpString)) return "";
        String regexStr = "[A-Z]";
        Matcher matcher = Pattern.compile(regexStr).matcher(humpString);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String g = matcher.group();
            matcher.appendReplacement(sb, "_" + g.toLowerCase());
        }
        matcher.appendTail(sb);
        if (sb.charAt(0) == '_') {
            sb.delete(0, 1);
        }
        return sb.toString();
    }
}
