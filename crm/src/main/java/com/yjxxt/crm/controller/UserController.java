package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.exceptions.ParamsException;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.service.UserService;
import com.yjxxt.crm.utils.LoginUserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping("toPasswordPage")
    public String toPasswordPage(){
        return "user/Password";
    }

    @RequestMapping("index")
    public String index(){
        return "user/user";
    }

    @RequestMapping("addOrUpdateUserPage")
    public String addOrUpdateUserPage(Integer id, Model model){
        if (id!=null){
            User user = userService.selectByPrimaryKey(id);
            model.addAttribute("user",user);
        }
        return "user/add_update";
    }

    @RequestMapping("toSettingPage")
    public String setting(HttpServletRequest request){
        //获取用户ID
        int userId = LoginUserUtil.releaseUserIdFromCookie(request);
        //调用方法
        User user = userService.selectByPrimaryKey(userId);
        //存储
        request.setAttribute("user",user);
        //转发
        return "user/setting";
    }

    @RequestMapping("login")
    @ResponseBody
    public ResultInfo say(User user){
        ResultInfo resultInfo = new ResultInfo();
            UserModel userModel = userService.userLogin(user.getUserName(), user.getUserPwd());
            resultInfo.setResult(userModel);
        return resultInfo;
    }

    @RequestMapping("setting")
    @ResponseBody
    public ResultInfo sayUpdate(User user){
        ResultInfo resultInfo = new ResultInfo();
            //修改信息
        userService.updateByPrimaryKeySelective(user);
        //返回目标数据对象
        return resultInfo;
    }

    @RequestMapping("adduser")
    @ResponseBody
    public ResultInfo addUser(User user){
        userService.addUser(user);
        //返回目标数据对象
        return success("用户添加成功");
    }

    @RequestMapping("deleteuser")
    @ResponseBody
    public ResultInfo deleteUser(Integer[] ids){
        userService.removeUser(ids);
        //返回目标数据对象
        return success("用户删除成功");
    }

    @RequestMapping("updateuser")
    @ResponseBody
    public ResultInfo updateUser(User user){
        userService.updateUser(user);
        //返回目标数据对象
        return success("用户修改成功");
    }

    @PostMapping("updatepwd")
    @ResponseBody
    public ResultInfo updatePwd(HttpServletRequest req, String oldPassword,String newPassword,
                                String confirmPassword){
        ResultInfo resultInfo = new ResultInfo();
        //获取cookie的userid
        int userId = LoginUserUtil.releaseUserIdFromCookie(req);
        //修改密码
//        try{
            userService.changeUserPwd(userId,oldPassword,newPassword,confirmPassword);
//        }catch (ParamsException pe){
//            pe.printStackTrace();
//            resultInfo.setCode(pe.getCode());
//            resultInfo.setMsg(pe.getMsg());
//        }catch (Exception ex){
//            ex.printStackTrace();
//            resultInfo.setCode(500);
//            resultInfo.setMsg("操作失败");
//        }
        return resultInfo;
    }

    @RequestMapping("sales")
    @ResponseBody
    public List<Map<String, Object>> findSales(){
        List<Map<String, Object>> lists = userService.querySales();
        return lists;
    }


    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> list(UserQuery userQuery){
        return userService.queryUserByParams(userQuery);
    }


}
