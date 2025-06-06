package com.example.javawebcurriculumdesign.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类 - 使用 HikariCP 连接池
 */
public class DBUtil {
    private static final String CONFIG_FILE = "database.properties";
    private static HikariDataSource dataSource;

    // 静态代码块，在类加载时执行，初始化连接池
    static {
        try {
            initDataSource();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("初始化数据库连接池失败", e);
        }
    }
    
    /**
     * 初始化数据源（连接池）
     */
    private static void initDataSource() throws IOException {
        // 创建 HikariCP 配置
        HikariConfig config = new HikariConfig();

        // 优先从环境变量读取配置，如果不存在则从properties文件读取
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbHost != null && dbPort != null && dbName != null && dbUser != null && dbPassword != null) {
            // 使用环境变量配置
            System.out.println("使用环境变量配置数据库连接");
            
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            
            // 连接池默认配置
            config.setPoolName("CampusPassPool");
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(20);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setConnectionTestQuery("SELECT 1");
            
            System.out.println("========== 数据库连接池配置信息 ==========");
            System.out.println("配置来源: 环境变量");
            System.out.println("驱动: org.postgresql.Driver");
            System.out.println("URL: jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName);
            System.out.println("用户名: " + dbUser);
            System.out.println("连接池名称: " + config.getPoolName());
            System.out.println("最小空闲连接数: " + config.getMinimumIdle());
            System.out.println("最大连接池大小: " + config.getMaximumPoolSize());
            System.out.println("=========================================");
        } else {
            // 回退到properties文件配置
            System.out.println("环境变量未配置，使用properties文件配置数据库连接");
            
            Properties prop = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is == null) {
                throw new IOException("找不到配置文件: " + CONFIG_FILE + "，且环境变量未配置");
            }

            try {
                prop.load(is);

                // 基本数据库连接配置
                config.setDriverClassName(prop.getProperty("jdbc.driver"));
                config.setJdbcUrl(prop.getProperty("jdbc.url"));
                config.setUsername(prop.getProperty("jdbc.username"));
                config.setPassword(prop.getProperty("jdbc.password"));

                // 连接池配置
                config.setPoolName(prop.getProperty("hikari.poolName", "CampusPassPool"));
                config.setMinimumIdle(Integer.parseInt(prop.getProperty("hikari.minimumIdle", "5")));
                config.setMaximumPoolSize(Integer.parseInt(prop.getProperty("hikari.maximumPoolSize", "20")));
                config.setConnectionTimeout(Long.parseLong(prop.getProperty("hikari.connectionTimeout", "30000")));
                config.setIdleTimeout(Long.parseLong(prop.getProperty("hikari.idleTimeout", "600000")));
                config.setMaxLifetime(Long.parseLong(prop.getProperty("hikari.maxLifetime", "1800000")));
                config.setConnectionTestQuery(prop.getProperty("hikari.connectionTestQuery", "SELECT 1"));

                System.out.println("========== 数据库连接池配置信息 ==========");
                System.out.println("配置来源: " + CONFIG_FILE);
                System.out.println("驱动: " + prop.getProperty("jdbc.driver"));
                System.out.println("URL: " + prop.getProperty("jdbc.url"));
                System.out.println("用户名: " + prop.getProperty("jdbc.username"));
                System.out.println("连接池名称: " + config.getPoolName());
                System.out.println("最小空闲连接数: " + config.getMinimumIdle());
                System.out.println("最大连接池大小: " + config.getMaximumPoolSize());
                System.out.println("=========================================");
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // 创建数据源
        dataSource = new HikariDataSource(config);
    }
    
    /**
     * 从连接池获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源未初始化");
        }
        return dataSource.getConnection();
    }

    /**
     * 获取连接池状态信息
     */
    public static String getPoolStatus() {
        if (dataSource == null) {
            return "数据源未初始化";
        }
        return String.format("连接池状态 - 活跃连接: %d, 空闲连接: %d, 总连接: %d, 等待连接: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    /**
     * 关闭连接池
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("数据库连接池已关闭");
        }
    }
    
    /**
     * 关闭数据库连接资源
     */
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭数据库连接和PreparedStatement
     */
    public static void close(Connection conn, PreparedStatement ps) {
        close(conn, ps, null);
    }
} 