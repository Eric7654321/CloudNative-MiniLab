package com.minilab.interceptor;


import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.minilab.pojo.entity.Result;
import com.minilab.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {

        String url = req.getRequestURI().toString();
        log.info("請求的url:{}",url);

        if(url.contains("login")){
            log.info("登入操作, 放行");

            return true;
        }
        if(url.contains("test") || url.contains("actuator") || url.contains("assets")){
            log.info("檢查操作, 放行");

            return true;
        }

        String jwt = req.getHeader("token");

        if(!StringUtils.hasLength(jwt)){
            log.info("請求token為null,未登入");
            Result error = Result.error("NOT_LOGIN");

            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);

            return false;
        }

        try {
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {//解析失敗
            e.printStackTrace();
            log.info("解析失敗，返回錯誤訊息");

            Result error = Result.error("NOT_LOGIN");

            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);

            return false;
        }

        log.info("合法!");
        return true;
    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        //HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
//        System.out.println("postHandle!");
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        //HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
//        System.out.println("afterCompletion!");
//    }
}
