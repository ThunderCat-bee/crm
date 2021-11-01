layui.use(['form','jquery','jquery_cookie'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery,
        $ = layui.jquery_cookie($);

    //监听提交
    form.on("submit(saveBtn)", function(data){
        var fieldData=data.field;

        //发送ajax
        $.ajax({
            type:"post",
            url:ctx+"/user/updatepwd",
            data:{
                "oldPassword":fieldData.old_password,
                "newPassword":fieldData.new_password,
                "confirmPassword":fieldData.again_password
            },
            dataType:"json",
            success:function (data) {
                //resultInfo
                if (data.code==200){
                        layer.msg("修改成功，稍后跳转",function () {
                            //清空cookie
                            $.removeCookie("userIdStr",{domain:"localhost",path:"/crm"});
                            $.removeCookie("userName",{domain:"localhost",path:"/crm"});
                            $.removeCookie("trueName",{domain:"localhost",path:"/crm"});
                            //跳转
                            window.parent.location.href=ctx+"/index";
                        })
                }else {
                    //失败
                    layer.msg(data.msg);
                }
            }
        })



        //取消默认行为
        return false;
    });
});