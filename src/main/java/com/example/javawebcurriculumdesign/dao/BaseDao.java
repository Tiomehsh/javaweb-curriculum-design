package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问基类，提供通用的数据库操作方法
 */
public abstract class BaseDao {
    
    /**
     * 执行更新操作（插入、更新、删除）
     * @param sql SQL语句
     * @param params SQL参数
     * @return 影响的行数
     */
    protected int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("执行更新操作失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
    }
    
    /**
     * 执行批量更新操作
     * @param sql SQL语句
     * @param paramsList 批量参数列表
     * @return 影响的行数数组
     */
    protected int[] executeBatchUpdate(String sql, List<Object[]> paramsList) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 关闭自动提交
            ps = conn.prepareStatement(sql);
            
            // 添加批处理
            for (Object[] params : paramsList) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.addBatch();
            }
            
            int[] result = ps.executeBatch();
            conn.commit(); // 提交事务
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // 发生异常时回滚
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("执行批量更新操作失败", e);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // 恢复自动提交
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.close(conn, ps);
        }
    }
    
    /**
     * 执行查询操作，并将结果映射为指定类型的对象列表
     * @param sql SQL语句
     * @param rowMapper 行映射器
     * @param params SQL参数
     * @return 对象列表
     */
    protected <T> List<T> executeQuery(String sql, RowMapper<T> rowMapper, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            rs = ps.executeQuery();
            List<T> result = new ArrayList<>();
            
            // 将结果集映射为对象列表
            while (rs.next()) {
                T obj = rowMapper.mapRow(rs);
                result.add(obj);
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("执行查询操作失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    
    /**
     * 查询单个对象
     * @param sql SQL语句
     * @param rowMapper 行映射器
     * @param params SQL参数
     * @return 单个对象，如果没有找到则返回null
     */
    protected <T> T querySingle(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> list = executeQuery(sql, rowMapper, params);
        return list.isEmpty() ? null : list.get(0);
    }
    
    /**
     * 查询记录数
     * @param sql SQL语句
     * @param params SQL参数
     * @return 记录数
     */
    protected long queryCount(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("查询记录数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
    
    /**
     * 行映射器接口，用于将结果集的一行映射为对象
     */
    protected interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }
} 