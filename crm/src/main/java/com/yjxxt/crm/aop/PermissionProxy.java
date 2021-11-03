package com.yjxxt.crm.aop;

import com.yjxxt.crm.annotation.RequirePermission;
import com.yjxxt.crm.exceptions.NoLoginException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.List;

@Component
@Aspect
public class PermissionProxy {

    @Autowired
    private HttpSession session;

    @Around(value = "@annotation(com.yjxxt.crm.annotation.RequirePermission)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        //判断是否登录
        List<String> permissions = (List<String>) session.getAttribute("permissions");
        if (permissions==null || permissions.size()==0){
            throw new NoLoginException("未登录");
        }
        //判断是否有权限访问目标
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        RequirePermission requirePermission =
                methodSignature.getMethod().getDeclaredAnnotation(RequirePermission.class);
        //对比
        if (!(permissions.contains(requirePermission.code()))){
            throw new NoLoginException("无权访问");
        }
        Object result=pjp.proceed();
        return result;
    }
}
