layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery;

    /**
     * 监听submit事件
     * 实现营销机会的添加与更新
     */
    form.on("submit(addOrUpdateSaleChance)", function (obj) {
// 提交数据时的加载层 （https://layer.layui.com/）
        var index = layer.msg("数据提交中,请稍后...", {
            icon: 16, // 图标
            time: false, // 不关闭
            shade: 0.8 // 设置遮罩的透明度
        });
        //判断添加还是修改
        var url = ctx + "/sale_chance/add";
        if ($("input[name=id]").val()){
            url=ctx+"/sale_chance/update";
        }
        //发送ajax
        $.ajax({
            type:"post",
            url:url,
            data:obj.field,
            dataType:"json",
            success:function (obj) {
                if (obj.code==200){
                    layer.msg("添加成功");
                    //刷新页面
                    window.parent.location.reload();
                }else {
                    layer.msg(obj.msg,{icon: 6});
                }
            }
        })

        return false; // 阻止表单提交
    });
    /**
     * 关闭弹出层
     */
    $("#closeBtn").click(function () {
// 先得到当前iframe层的索引
        var index = parent.layer.getFrameIndex(window.name);
// 再执行关闭
        parent.layer.close(index);
    });

    /*添加下拉框*/
    var assignMan=$("input[name='man']").val();
    $.ajax({
        type: "post",
        url:ctx+"/user/sales",
        dataType: "json",
        success:function (data) {
            //遍历
            for (var x in data) {
                if (data[x].id==assignMan){
                    $("#assignMan").append("<option selected value='"+data[x].id+"'>"+data[x].uname+"</option>");
                }else {
                    $("#assignMan").append("<option  value='"+data[x].id+"'>"+data[x].uname+"</option>");
                }

            }
            //重新渲染
            layui.form.render("select");

        }

    })




});
