package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.Permission;
import com.yjxxt.crm.bean.Role;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.mapper.ModuleMapper;
import com.yjxxt.crm.mapper.PermissionMapper;
import com.yjxxt.crm.mapper.RoleMapper;
import com.yjxxt.crm.query.RoleQuery;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoleService extends BaseService<Role,Integer> {

    @Autowired(required = false)
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private ModuleMapper moduleMapper;

    //查询所有的角色信息
    public List<Map<String,Object>> findRoles(Integer userId){
        return roleMapper.selectRoles(userId);
    }
    //角色的条件查询及分页
    public Map<String,Object> queryRoleByParams(RoleQuery roleQuery){
        //实例化map
        HashMap<String, Object> map = new HashMap<>();
        //实例化分页对象
        PageHelper.startPage(roleQuery.getPage(),roleQuery.getLimit());
        //开始分页
        PageInfo<Role> rlist = new PageInfo<>(selectByParams(roleQuery));
        //准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",rlist.getTotal());
        map.put("data",rlist.getList());
        //返回map
        return map;
    }

    //添加
    public void addRole(Role role){
        //非空
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名");
        //唯一
        Role temp=roleMapper.selectRoleByName(role.getRoleName());
        AssertUtil.isTrue(temp!=null,"角色名已存在");
        //设置默认值
        role.setIsValid(1);
        role.setCreateDate(new Date());
        role.setUpdateDate(new Date());
        //验证是否成功
        AssertUtil.isTrue(insertHasKey(role)<1,"添加失败");
    }

    //修改
    public void updateRole(Role role){
        //验证当前对象是否存在
        Role role1 = roleMapper.selectByPrimaryKey(role.getId());
        AssertUtil.isTrue(role1==null,"待修改记录不存在");
        //唯一
        Role temp=roleMapper.selectRoleByName(role.getRoleName());
        AssertUtil.isTrue(temp!=null && !(temp.getId().equals(role.getId())),"角色名已存在");
        //设置默认值
        role.setUpdateDate(new Date());
        //验证是否成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(role)<1,"修改失败");
    }

    public void removeRoleById(Role role) {
        //验证
        AssertUtil.isTrue(role.getId()==null || selectByPrimaryKey(role.getId())==null,"请选择数据");
        //设定默认值
        role.setIsValid(0);
        role.setUpdateDate(new Date());
        //判断是否成功
        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(role)<1,"删除失败");
    }

    //授权
    public void addGrant(Integer roleId, Integer[] mids) {
        AssertUtil.isTrue(roleId==null || roleMapper.selectByPrimaryKey(roleId)==null,"请选择角色");
        //t_permission  roleId,mid
        //统计当前角色的资源数量
        int count=permissionMapper.countRoleModulesByRoleId(roleId);
        if (count>0){
            //删除当前角色的资源
            AssertUtil.isTrue(permissionMapper.deleteRoleModuleByRoleId(roleId)!=count,"角色资源分配失败");
        }
        List<Permission> plist=new ArrayList<>();
        if (mids!=null && mids.length>0){
            //遍历mids
            for (Integer mid:mids){
                //实例化对象
                Permission permission=new Permission();
                permission.setRoleId(roleId);
                permission.setModuleId(mid);
                //权限码
                permission.setAclValue(moduleMapper.selectByPrimaryKey(mid).getOptValue());
                permission.setCreateDate(new Date());
                permission.setUpdateDate(new Date());
                plist.add(permission);
            }
        }
        AssertUtil.isTrue(permissionMapper.insertBatch(plist)<1,"添加失败");

    }
}
