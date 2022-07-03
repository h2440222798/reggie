package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// mybatis plus 里面有page
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    //员工登录
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1,密码加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2根据页面提交的结果用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3 查看结果是否真的查询到
        if(emp == null){
            return R.error("登录失败");
        }

        //4 密码进行比对
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        // 查看状态
        if(emp.getStatus() == 0){
            return R.error("账号已经禁用");
        }
        // 可以登录成功 id 存入session
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    // 员工退出操作
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清理Session中保存的当前员工id
        request.getSession().removeAttribute("employee");

        return R.success("退出成功");
    }
    //新增员工

    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //设置初始密码，需要进行md5的加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        // 获取当前用户的id
//       Long empId = (Long) request.getSession().getAttribute("employee");
//
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
//        try {
//            employeeService.save(employee);
//        }catch (Exception e){
//            R.error("新增员工失败");
//        }
        employeeService.save(employee);
        return R.success("创建成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        log.info("page = {},pageSize = {}, name = {}",page,pageSize,name);

        //构造分页器
        Page pageInfo = new Page(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        //添加一个过滤条件
        queryWrapper.like(StringUtils.hasLength(name),Employee::getName,name);

        //添加一个排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }
    // 根据id修改信息 编辑员工信息的时候复用了这个部分
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody  Employee employee){

        log.info(employee.toString());
        Long id = Thread.currentThread().getId();
        log.info("线程id为: {}",id);
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee =employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有这个员工的消息");
    }


}
