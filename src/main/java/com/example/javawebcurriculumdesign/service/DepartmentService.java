package com.example.javawebcurriculumdesign.service;

import com.example.javawebcurriculumdesign.dao.DepartmentDao;
import com.example.javawebcurriculumdesign.dao.SystemLogDao;
import com.example.javawebcurriculumdesign.model.Department;
import com.example.javawebcurriculumdesign.model.SystemLog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门服务类
 * 提供部门管理相关的业务逻辑
 */
public class DepartmentService {
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final SystemLogDao systemLogDao = new SystemLogDao();
    
    // 状态常量（数据库中可能不存在，在这里定义）
    public static final Integer STATUS_ENABLED = 1;
    public static final Integer STATUS_DISABLED = 0;
    
    /**
     * 获取所有部门列表
     * @return 部门列表
     */
    public List<Department> getAllDepartments() {
        return departmentDao.getAll();
    }
    
    /**
     * 根据ID获取部门
     * @param deptId 部门ID
     * @return 部门对象
     */
    public Department getDepartmentById(Integer deptId) {
        if (deptId == null || deptId <= 0) {
            return null;
        }
        return departmentDao.getById(deptId);
    }
    
    /**
     * 添加部门
     * @param department 部门对象
     * @param operatorId 操作人ID
     * @return 新增部门的ID，失败返回-1
     */
    public int addDepartment(Department department, Integer operatorId) {
        if (department == null || department.getDeptName() == null || 
            department.getDeptType() == null) {
            return -1;
        }
        
        // 检查部门名称是否已存在
        Department existingDept = departmentDao.getByName(department.getDeptName());
        if (existingDept != null) {
            return -1;
        }
        
        int result = departmentDao.add(department);
        int deptId = result > 0 ? department.getDeptId() : -1;
        
        // 记录添加部门日志
        if (operatorId != null && deptId > 0) {
            String operation = "添加部门";
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("部门ID: " + deptId + ", 部门名称: " + department.getDeptName());
            systemLogDao.add(log);
        }
        
        return deptId;
    }
    
    /**
     * 更新部门信息
     * @param department 部门对象
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    public boolean updateDepartment(Department department, Integer operatorId) {
        if (department == null || department.getDeptId() == null || 
            department.getDeptId() <= 0 || department.getDeptName() == null) {
            return false;
        }
        
        // 检查部门是否存在
        Department existingDept = departmentDao.getById(department.getDeptId());
        if (existingDept == null) {
            return false;
        }
        
        // 检查部门名称是否与其他部门重复
        Department deptWithSameName = departmentDao.getByName(department.getDeptName());
        if (deptWithSameName != null && !deptWithSameName.getDeptId().equals(department.getDeptId())) {
            return false;
        }
        
        boolean success = departmentDao.update(department) > 0;
        
        // 记录更新部门日志
        if (operatorId != null && success) {
            String operation = "更新部门信息";
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("部门ID: " + department.getDeptId() + ", 部门名称: " + department.getDeptName());
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 删除部门
     * @param deptId 部门ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    public boolean deleteDepartment(Integer deptId, Integer operatorId) {
        if (deptId == null || deptId <= 0) {
            return false;
        }
        
        // 检查部门是否存在
        Department department = departmentDao.getById(deptId);
        if (department == null) {
            return false;
        }
        
        // TODO: 检查部门下是否有管理员或预约，如果有则不允许删除
        
        boolean success = departmentDao.deleteById(deptId) > 0;
        
        // 记录删除部门日志
        if (operatorId != null && success) {
            String operation = "删除部门";
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("部门ID: " + deptId + ", 部门名称: " + department.getDeptName());
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 获取指定类型的部门列表
     * @param deptType 部门类型
     * @return 部门列表
     */
    public List<Department> getDepartmentsByType(String deptType) {
        return departmentDao.getByType(deptType);
    }
    
    /**
     * 根据部门类型统计部门数量
     * @param deptType 部门类型
     * @return 部门数量
     */
    public long countDepartmentsByType(String deptType) {
        List<Department> departments = departmentDao.getByType(deptType);
        return departments != null ? departments.size() : 0;
    }
} 