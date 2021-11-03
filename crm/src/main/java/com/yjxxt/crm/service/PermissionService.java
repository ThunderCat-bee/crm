package com.yjxxt.crm.service;

import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.Permission;
import com.yjxxt.crm.mapper.PermissionMapper;
import com.yjxxt.crm.utils.AssertUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService extends BaseService<Permission,Integer> {

    @Resource
    private PermissionMapper permissionMapper;

    //查询用户拥有的资源权限码
    public List<String> queryUserRolesHasPermissions(Integer userId){
        return permissionMapper.selectUserHasRolesPermission(userId);
    }
}
