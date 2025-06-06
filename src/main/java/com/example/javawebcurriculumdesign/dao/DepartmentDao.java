package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.model.Department;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 部门数据访问对象
 */
public class DepartmentDao extends BaseDao {
    
    /**
     * 添加部门
     * @param department 部门对象
     * @return 影响的行数
     */
    public int add(Department department) {
        String sql = "INSERT INTO department (dept_type, dept_name, contact_person, contact_phone) VALUES (?, ?, ?, ?)";
        int result = executeUpdate(sql, department.getDeptType(), department.getDeptName(),
                           department.getContactPerson(), department.getContactPhone());
        
        // 如果插入成功，查询并设置生成的ID
        if (result > 0) {
            Department inserted = getByName(department.getDeptName());
            if (inserted != null) {
                department.setDeptId(inserted.getDeptId());
            }
        }
        
        return result;
    }
    
    /**
     * 根据ID删除部门
     * @param deptId 部门ID
     * @return 影响的行数
     */
    public int deleteById(Integer deptId) {
        String sql = "DELETE FROM department WHERE dept_id = ?";
        return executeUpdate(sql, deptId);
    }
    
    /**
     * 更新部门
     * @param department 部门对象
     * @return 影响的行数
     */
    public int update(Department department) {
        String sql = "UPDATE department SET dept_type = ?, dept_name = ?, contact_person = ?, contact_phone = ? WHERE dept_id = ?";
        return executeUpdate(sql, department.getDeptType(), department.getDeptName(),
                           department.getContactPerson(), department.getContactPhone(), department.getDeptId());
    }
    
    /**
     * 根据ID查询部门
     * @param deptId 部门ID
     * @return 部门对象
     */
    public Department getById(Integer deptId) {
        String sql = "SELECT * FROM department WHERE dept_id = ?";
        return querySingle(sql, new DepartmentRowMapper(), deptId);
    }
    
    /**
     * 根据部门名称查询部门
     * @param deptName 部门名称
     * @return 部门对象
     */
    public Department getByName(String deptName) {
        String sql = "SELECT * FROM department WHERE dept_name = ?";
        return querySingle(sql, new DepartmentRowMapper(), deptName);
    }
    
    /**
     * 查询所有部门
     * @return 部门列表
     */
    public List<Department> getAll() {
        String sql = "SELECT * FROM department ORDER BY dept_id";
        return executeQuery(sql, new DepartmentRowMapper());
    }
    
    /**
     * 根据部门类型查询部门
     * @param deptType 部门类型
     * @return 部门列表
     */
    public List<Department> getByType(String deptType) {
        String sql = "SELECT * FROM department WHERE dept_type = ? ORDER BY dept_id";
        return executeQuery(sql, new DepartmentRowMapper(), deptType);
    }
    
    /**
     * 查询部门总数
     * @return 部门总数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM department";
        return queryCount(sql);
    }
    
    /**
     * 部门行映射器
     */
    private static class DepartmentRowMapper implements RowMapper<Department> {
        @Override
        public Department mapRow(ResultSet rs) throws SQLException {
            Department department = new Department();
            department.setDeptId(rs.getInt("dept_id"));
            department.setDeptType(rs.getString("dept_type"));
            department.setDeptName(rs.getString("dept_name"));
            
            // 安全地获取新字段，如果不存在则设为null
            try {
                department.setContactPerson(rs.getString("contact_person"));
                department.setContactPhone(rs.getString("contact_phone"));
            } catch (SQLException e) {
                // 如果字段不存在，设置为null
                department.setContactPerson(null);
                department.setContactPhone(null);
            }
            
            department.setCreateTime(rs.getTimestamp("create_time"));
            department.setUpdateTime(rs.getTimestamp("update_time"));
            return department;
        }
    }
}