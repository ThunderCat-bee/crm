package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.SaleChance;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName,String userPwd){
        checkUserLoginParam(userName,userPwd);
        //用户是否存在
        User temp=userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp==null,"用户不存在");
        //用户密码是否正确
        checkUserPwd(userPwd,temp.getUserPwd());
        //构建返回对象

        return builderUserInfo(temp);
    }

    /**
     * 构建返回目标对象
     * @param user
     * @return
     */
    private UserModel builderUserInfo(User user) {
        //实例化对象
        UserModel userModel = new UserModel();
        //加密
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        //返回目标对象
        return userModel;
    }

    /**
     * 校验用户密码
     * @param userName
     * @param userPwd
     */
    private void checkUserLoginParam(String userName, String userPwd) {
        //用户非空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        //密码非空
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"密码不能为空");
    }

    /**
     * 验证密码
     * @param userPwd
     * @param userPwd1
     */
    private void checkUserPwd(String userPwd, String userPwd1) {
        //对输入的密码加密
        userPwd= Md5Util.encode(userPwd);
        //加密的密码和数据库中对比
        AssertUtil.isTrue(!userPwd.equals(userPwd1),"密码不正确");
    }

    public void changeUserPwd(Integer userId,String oldPassword,String newPassword,String confirmPassword){
        //用户登录之后修改，userid
        User user = userMapper.selectByPrimaryKey(userId);
        //密码验证
        checkPwdParam(user,oldPassword,newPassword,confirmPassword);
        //修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        //确认密码修改是否成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"修改失败");

    }

    /**
     * 修改密码验证
     * @param user
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     */
    private void checkPwdParam(User user, String oldPassword, String newPassword, String confirmPassword) {
        AssertUtil.isTrue(user==null,"用户没有登录");
        //原始密码不能为空
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原密码");
        //原密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码错误");
        //新密码不能为空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        //新密码不能与原密码一致
        AssertUtil.isTrue(newPassword.equals(oldPassword),"新密码不能与原密码一致");
        //确认密码不能为空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword),"确认密码不能为空");
        //新密码与确认密码要一致
        AssertUtil.isTrue(!confirmPassword.equals(newPassword),"新密码与确认密码要一致");
    }

    /*查询所有的销售人员*/
    public List<Map<String,Object>> querySales(){
        return userMapper.selectSales();
    }

    /**
     * 多条件分页查询用户数据
     * @param userQuery
     * @return
     */
    public Map<String,Object> queryUserByParams(UserQuery userQuery){
        //实例化map
        HashMap<String, Object> map = new HashMap<>();
        //实例化分页对象
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        //开始分页
        PageInfo<User> plist = new PageInfo<>(userMapper.selectByParams(userQuery));
        //准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());
        //返回map
        return map;
    }

    /**
     * 添加用户
     * 1. 参数校验
     * 用户名 非空 唯一性
     * 邮箱 非空
     * 手机号 非空 格式合法
     * 2. 设置默认参数
     * isValid 1
     * creteDate 当前时间
     * updateDate 当前时间
     * userPwd 123456 -> md5加密
     * 3. 执行添加，判断结果
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){
        //验证
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        //判断用户名是否已经存在
        User temp = userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp!=null,"用户名已存在");
        //设置默认值
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        //密码加密
        user.setUserPwd(Md5Util.encode("123456"));
        //验证是否成功
        AssertUtil.isTrue(insertHasKey(user)<1,"添加失败");
        System.out.println(user.getId()+"-->"+user.getRoleIds());
        //**
        relaionUserRole(user.getId(),user.getRoleIds());

    }

    /**
     * 操作中间表
     * @param userId
     * @param roleIds
     */
    private void relaionUserRole(Integer userId, String roleIds) {
        //准备集合存储对象
        List<UserRole> urlist=new ArrayList<>();
        AssertUtil.isTrue(StringUtils.isBlank(roleIds),"请选择角色信息");
        //统计当前用户有多少个角色
        int count=userRoleMapper.countUserRoleNum(userId);
        //删除当前用户的角色
        if (count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
        }

        String[] RoleStrId = roleIds.split(",");
        //遍历
        for (String rid:RoleStrId) {
            //准备对象
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            //放到集合
            urlist.add(userRole);
        }
        //批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist)!=urlist.size(),"添加失败");

    }

    private void checkUser(String userName, String email, String phone) {
        //用户名非空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"请输入用户名");
        //邮箱非空
        AssertUtil.isTrue(StringUtils.isBlank(email),"请输入邮箱");
        //手机号非空
        AssertUtil.isTrue(StringUtils.isBlank(phone),"请输入手机号");
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"手机号格式不合法");
    }

    /**
     * 更新用户
     * 1. 参数校验
     * id 非空 记录必须存在
     * 用户名 非空 唯一性
     * email 非空
     * 手机号 非空 格式合法
     * 2. 设置默认参数
     * updateDate
     * 3. 执行更新，判断结果
     * @param user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUser(User user){
        //校验
        //判断ID是否存在
        User temp = userMapper.selectByPrimaryKey(user.getId());
        AssertUtil.isTrue(temp==null,"修改记录不存在");
        //验证参数
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        //修改真实姓名时，用户名已经存在的情况
        User temp2 = userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp2!=null &&!(temp2.getId().equals(user.getId())),"用户名称已经存在");
        //设定默认值
        user.setUpdateDate(new Date());
        //判断是否修改成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(user)<1,"修改失败");

        relaionUserRole(user.getId(),user.getRoleIds());
    }

    //删除用户
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeUser(Integer[] ids){
        //验证
        AssertUtil.isTrue(ids==null ||ids.length==0,"请选择要删除的数据");
        //遍历对象
        for (Integer userId:ids) {
            //统计当前用户有多少个角色
            int count=userRoleMapper.countUserRoleNum(userId);
            //删除当前用户的角色
            if (count>0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
            }

        }
        //判断删除是否成功
        AssertUtil.isTrue(deleteBatch(ids)<1,"删除失败");
    }
}
