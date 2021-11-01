layui.use(['form','jquery','jquery_cookie'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery,
        $ = layui.jquery_cookie($);

    //监听提交
    form.on("submit(login)", function(data){
        var fieldData=data.field;

        if (fieldData.username=='undefinded' || fieldData.username.trim()==''){
            layer.msg("用户名不能为空");
            return
        }
        if (fieldData.password=='undefinded' || fieldData.password.trim()==''){
            layer.msg("密码名不能为空");
            return ;
        }
        //发送ajax
        $.ajax({
            type:"post",
            url:ctx+"/user/login",
            data:{
                "userName":fieldData.username,
                "userPwd":fieldData.password
            },
            dataType:"json",
            success:function (msg) {
                //resultInfo
                if (msg.code==200){
                    layer.msg("登录成功",function () {
                        //将用户数据存到cookie
                        $.cookie("userIdStr",msg.result.userIdStr);
                        $.cookie("userName",msg.result.userName);
                        $.cookie("trueName",msg.result.trueName);
                        //判断是否勾选记住密码
                        if ($("input[type='checkbox']").is(":checked")){
                            $.cookie("userIdStr",msg.result.userIdStr,{expires:30});
                            $.cookie("userName",msg.result.userName,{expires:30});
                            $.cookie("trueName",msg.result.trueName,{expires:30});
                        }
                        //跳转
                        window.location.href=ctx+"/main";
                    })
                }else {
                    //失败
                    layer.msg(msg.msg);
                }
            }
        })



        //取消默认行为
        return false;
    });
});