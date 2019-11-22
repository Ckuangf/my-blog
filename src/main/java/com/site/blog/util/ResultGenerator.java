package com.site.blog.util;

import com.site.blog.constants.HttpStatusConstants;
import com.site.blog.dto.Result;
import org.springframework.util.StringUtils;

/**
 * 响应结果生成工具
 * @author nanjie
 */
public class ResultGenerator {

    public static Result getFailedResult(){
        Result result = new Result();
        result.setResultCode(HttpStatusConstants.FAILED.getStatus());
        result.setMessage(HttpStatusConstants.FAILED.getContent());
        return result;
    }

    public static Result getSuccessResult() {
        Result result = new Result();
        result.setResultCode(HttpStatusConstants.OK.getStatus());
        result.setMessage(HttpStatusConstants.OK.getContent());
        return result;
    }

    public static Result getSuccessResult(String message) {
        Result result = new Result();
        result.setResultCode(HttpStatusConstants.OK.getStatus());
        result.setMessage(message);
        return result;
    }

    public static Result getSuccessResult(Object data) {
        Result result = new Result();
        result.setResultCode(HttpStatusConstants.OK.getStatus());
        result.setMessage(HttpStatusConstants.OK.getContent());
        result.setData(data);
        return result;
    }

    public static Result getFailResult(String message) {
        Result result = new Result();
        result.setResultCode(HttpStatusConstants.INTERNAL_SERVER_ERROR.getStatus());
        if (StringUtils.isEmpty(message)) {
            result.setMessage(HttpStatusConstants.INTERNAL_SERVER_ERROR.getContent());
        } else {
            result.setMessage(message);
        }
        return result;
    }

    public static Result getErrorResult(int code, String message) {
        Result result = new Result();
        result.setResultCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * @Description: 根据传入的常量和data数据返回对应result
     * @Param: [constants] http状态
     * @return: com.zhulin.blog.dto.Result
     * @date: 2019/8/24 16:25
     */
    public static Result getResultByHttp(HttpStatusConstants constants,Object data){
        Result result = ResultGenerator.getErrorResult(constants.getStatus(), constants.getContent());
        result.setData(data);
        return result;
    }

    /**
     * @Description: 根据传入的常量返回对应result
     * @Param: [constants] http状态
     * @return: com.zhulin.blog.dto.Result
     * @date: 2019/8/24 16:25
     */
    public static Result getResultByHttp(HttpStatusConstants constants){
        Result result = ResultGenerator.getErrorResult(constants.getStatus(), constants.getContent());
        return result;
    }
}
