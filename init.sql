-- =============================================
-- 校园通行码预约管理系统数据库初始化脚本
-- 适用于OpenGauss数据库
-- =============================================

-- 创建数据库（如果需要在命令行中单独执行）
-- CREATE DATABASE campus_pass;

-- 切换到campus_pass数据库
-- \c campus_pass;

-- -------------------- 部门表 --------------------
CREATE TABLE IF NOT EXISTS department (
    dept_id SERIAL PRIMARY KEY,                           -- 部门ID，自增主键
    dept_type VARCHAR(20) NOT NULL,                       -- 部门类型（行政部门、直属部门、学院）
    dept_name VARCHAR(50) NOT NULL UNIQUE,                -- 部门名称，唯一约束
    contact_person VARCHAR(50),                           -- 联系人
    contact_phone VARCHAR(20),                            -- 联系电话
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP   -- 更新时间
);

-- 添加表注释
COMMENT ON TABLE department IS '部门信息表';
COMMENT ON COLUMN department.dept_id IS '部门编号';
COMMENT ON COLUMN department.dept_type IS '部门类型';
COMMENT ON COLUMN department.dept_name IS '部门名称';
COMMENT ON COLUMN department.contact_person IS '联系人';
COMMENT ON COLUMN department.contact_phone IS '联系电话';
COMMENT ON COLUMN department.create_time IS '创建时间';
COMMENT ON COLUMN department.update_time IS '更新时间';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_department_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW(); -- 使用 NOW() 获取当前时间
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER department_update_time_trigger
BEFORE UPDATE ON department
FOR EACH ROW
EXECUTE PROCEDURE update_department_update_time();

-- -------------------- 管理员表 --------------------
CREATE TABLE IF NOT EXISTS admin (
    admin_id SERIAL PRIMARY KEY,                          -- 管理员ID，自增主键
    login_name VARCHAR(50) NOT NULL UNIQUE,               -- 登录名，唯一约束
    password_hash CHAR(64) NOT NULL,                      -- SM3加密后的密码（64个字符）
    real_name VARCHAR(50) NOT NULL,                       -- 真实姓名
    dept_id INTEGER,                                      -- 所属部门ID，外键
    phone VARCHAR(20),                                    -- 联系电话
    role VARCHAR(20) NOT NULL,                            -- 角色（SYSTEM_ADMIN, DEPARTMENT_ADMIN, AUDIT_ADMIN）
    login_attempts SMALLINT NOT NULL DEFAULT 0,           -- 登录失败尝试次数
    locked_until TIMESTAMP WITHOUT TIME ZONE,             -- 账户锁定截止时间
    last_password_change TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后一次密码修改时间
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    status SMALLINT NOT NULL DEFAULT 1,                   -- 状态(1:启用 0:禁用)
    FOREIGN KEY (dept_id) REFERENCES department(dept_id),
    CONSTRAINT chk_admin_role_values CHECK (role IN ('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN', 'AUDIT_ADMIN'))
);

-- 添加表注释
COMMENT ON TABLE admin IS '管理员表';
COMMENT ON COLUMN admin.admin_id IS '管理员ID';
COMMENT ON COLUMN admin.login_name IS '登录名';
COMMENT ON COLUMN admin.password_hash IS 'SM3加密后的密码';
COMMENT ON COLUMN admin.real_name IS '真实姓名';
COMMENT ON COLUMN admin.dept_id IS '所属部门ID';
COMMENT ON COLUMN admin.phone IS '联系电话';
COMMENT ON COLUMN admin.role IS '管理员角色';
COMMENT ON COLUMN admin.login_attempts IS '登录失败尝试次数';
COMMENT ON COLUMN admin.locked_until IS '账户锁定截止时间';
COMMENT ON COLUMN admin.last_password_change IS '最后一次密码修改时间';
COMMENT ON COLUMN admin.create_time IS '创建时间';
COMMENT ON COLUMN admin.update_time IS '更新时间';
COMMENT ON COLUMN admin.status IS '状态(1:启用 0:禁用)';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_admin_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW(); -- 使用 NOW() 获取当前时间
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER admin_update_time_trigger
BEFORE UPDATE ON admin
FOR EACH ROW
EXECUTE PROCEDURE update_admin_update_time();

-- -------------------- 社会公众预约表 --------------------
CREATE TABLE IF NOT EXISTS public_appointment (
    appointment_id SERIAL PRIMARY KEY,                    -- 预约ID，自增主键
    campus VARCHAR(50) NOT NULL,                          -- 预约校区
    visit_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,      -- 预约进校时间
    organization VARCHAR(100) NOT NULL,                   -- 所在单位
    name VARCHAR(50) NOT NULL,                            -- 姓名
    id_card_encrypted VARCHAR(200) NOT NULL,              -- 加密后的身份证号
    phone_encrypted VARCHAR(200) NOT NULL,                -- 加密后的手机号
    transportation VARCHAR(50) NOT NULL,                  -- 交通方式
    plate_number VARCHAR(20),                             -- 车牌号（可选）
    visitors INTEGER NOT NULL DEFAULT 1,                  -- 访问人数
    apply_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 申请时间
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',       -- 状态（自动审核通过）
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP   -- 更新时间
);

-- 添加表注释
COMMENT ON TABLE public_appointment IS '社会公众预约表';
COMMENT ON COLUMN public_appointment.appointment_id IS '预约ID';
COMMENT ON COLUMN public_appointment.campus IS '预约校区';
COMMENT ON COLUMN public_appointment.visit_time IS '预约进校时间';
COMMENT ON COLUMN public_appointment.organization IS '所在单位';
COMMENT ON COLUMN public_appointment.name IS '姓名';
COMMENT ON COLUMN public_appointment.id_card_encrypted IS '加密后的身份证号';
COMMENT ON COLUMN public_appointment.phone_encrypted IS '加密后的手机号';
COMMENT ON COLUMN public_appointment.transportation IS '交通方式';
COMMENT ON COLUMN public_appointment.plate_number IS '车牌号';
COMMENT ON COLUMN public_appointment.visitors IS '访问人数';
COMMENT ON COLUMN public_appointment.apply_time IS '申请时间';
COMMENT ON COLUMN public_appointment.status IS '状态';
COMMENT ON COLUMN public_appointment.create_time IS '创建时间';
COMMENT ON COLUMN public_appointment.update_time IS '更新时间';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_public_appointment_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW(); -- 使用 NOW() 获取当前时间
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER public_appointment_update_time_trigger
BEFORE UPDATE ON public_appointment
FOR EACH ROW
EXECUTE PROCEDURE update_public_appointment_update_time();

-- -------------------- 公务预约表 --------------------
CREATE TABLE IF NOT EXISTS official_appointment (
    appointment_id SERIAL PRIMARY KEY,                    -- 预约ID，自增主键
    campus VARCHAR(50) NOT NULL,                          -- 预约校区
    visit_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,      -- 预约进校时间
    organization VARCHAR(100) NOT NULL,                   -- 所在单位
    name VARCHAR(50) NOT NULL,                            -- 姓名
    id_card_encrypted VARCHAR(200) NOT NULL,              -- 加密后的身份证号
    phone_encrypted VARCHAR(200) NOT NULL,                -- 加密后的手机号
    transportation VARCHAR(50) NOT NULL,                  -- 交通方式
    plate_number VARCHAR(20),                             -- 车牌号（可选）
    visitors INTEGER NOT NULL DEFAULT 1,                  -- 访问人数
    visit_dept_id INTEGER NOT NULL,                       -- 公务访问部门ID
    visit_contact VARCHAR(50) NOT NULL,                   -- 公务访问接待人
    visit_reason TEXT NOT NULL,                           -- 来访事由
    apply_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 申请时间
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',        -- 状态（待审核）
    approver_id INTEGER,                                  -- 审核人ID
    approve_time TIMESTAMP WITHOUT TIME ZONE,             -- 审核时间
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    FOREIGN KEY (visit_dept_id) REFERENCES department(dept_id),
    FOREIGN KEY (approver_id) REFERENCES admin(admin_id)
);

-- 添加表注释
COMMENT ON TABLE official_appointment IS '公务预约表';
COMMENT ON COLUMN official_appointment.appointment_id IS '预约ID';
COMMENT ON COLUMN official_appointment.campus IS '预约校区';
COMMENT ON COLUMN official_appointment.visit_time IS '预约进校时间';
COMMENT ON COLUMN official_appointment.organization IS '所在单位';
COMMENT ON COLUMN official_appointment.name IS '姓名';
COMMENT ON COLUMN official_appointment.id_card_encrypted IS '加密后的身份证号';
COMMENT ON COLUMN official_appointment.phone_encrypted IS '加密后的手机号';
COMMENT ON COLUMN official_appointment.transportation IS '交通方式';
COMMENT ON COLUMN official_appointment.plate_number IS '车牌号';
COMMENT ON COLUMN official_appointment.visitors IS '访问人数';
COMMENT ON COLUMN official_appointment.visit_dept_id IS '公务访问部门ID';
COMMENT ON COLUMN official_appointment.visit_contact IS '公务访问接待人';
COMMENT ON COLUMN official_appointment.visit_reason IS '来访事由';
COMMENT ON COLUMN official_appointment.apply_time IS '申请时间';
COMMENT ON COLUMN official_appointment.status IS '状态';
COMMENT ON COLUMN official_appointment.approver_id IS '审核人ID';
COMMENT ON COLUMN official_appointment.approve_time IS '审核时间';
COMMENT ON COLUMN official_appointment.create_time IS '创建时间';
COMMENT ON COLUMN official_appointment.update_time IS '更新时间';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_official_appointment_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW(); -- 使用 NOW() 获取当前时间
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER official_appointment_update_time_trigger
BEFORE UPDATE ON official_appointment
FOR EACH ROW
EXECUTE PROCEDURE update_official_appointment_update_time();



-- -------------------- 系统日志表 --------------------
CREATE TABLE IF NOT EXISTS system_log (
    log_id SERIAL PRIMARY KEY,                            -- 日志ID，自增主键
    admin_id INTEGER,                                     -- 操作人ID（可为空，表示系统操作）
    operation VARCHAR(100) NOT NULL,                      -- 操作类型
    description TEXT,                                     -- 操作描述
    ip_address VARCHAR(50),                               -- 操作IP地址
    operation_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 操作时间
    log_hash VARCHAR(64),                                 -- 日志记录的HMAC-SM3值
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- 创建时间
    FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
);

-- 添加表注释
COMMENT ON TABLE system_log IS '系统日志表';
COMMENT ON COLUMN system_log.log_id IS '日志ID';
COMMENT ON COLUMN system_log.admin_id IS '操作人ID';
COMMENT ON COLUMN system_log.operation IS '操作类型';
COMMENT ON COLUMN system_log.description IS '操作描述';
COMMENT ON COLUMN system_log.ip_address IS '操作IP地址';
COMMENT ON COLUMN system_log.operation_time IS '操作时间';
COMMENT ON COLUMN system_log.log_hash IS '日志记录的HMAC-SM3值';
COMMENT ON COLUMN system_log.create_time IS '创建时间';

-- -------------------- 管理员权限表 --------------------
CREATE TABLE IF NOT EXISTS admin_permission (
    permission_id SERIAL PRIMARY KEY,                    -- 权限ID，自增主键
    admin_id INTEGER NOT NULL,                           -- 管理员ID
    permission_type VARCHAR(50) NOT NULL,                -- 权限类型
    permission_value VARCHAR(100),                       -- 权限值（可选）
    granted_by INTEGER NOT NULL,                         -- 授权人ID
    granted_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 授权时间
    status INTEGER NOT NULL DEFAULT 1,                   -- 状态(1:有效 0:无效)
    create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    FOREIGN KEY (admin_id) REFERENCES admin(admin_id),
    FOREIGN KEY (granted_by) REFERENCES admin(admin_id)
);

-- 添加表注释
COMMENT ON TABLE admin_permission IS '管理员权限表';
COMMENT ON COLUMN admin_permission.permission_id IS '权限ID';
COMMENT ON COLUMN admin_permission.admin_id IS '管理员ID';
COMMENT ON COLUMN admin_permission.permission_type IS '权限类型';
COMMENT ON COLUMN admin_permission.permission_value IS '权限值';
COMMENT ON COLUMN admin_permission.granted_by IS '授权人ID';
COMMENT ON COLUMN admin_permission.granted_time IS '授权时间';
COMMENT ON COLUMN admin_permission.status IS '状态(1:有效 0:无效)';
COMMENT ON COLUMN admin_permission.create_time IS '创建时间';
COMMENT ON COLUMN admin_permission.update_time IS '更新时间';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_admin_permission_update_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER admin_permission_update_time_trigger
BEFORE UPDATE ON admin_permission
FOR EACH ROW
EXECUTE PROCEDURE update_admin_permission_update_time();

-- 创建索引
CREATE INDEX idx_admin_permission_admin_id ON admin_permission(admin_id);
CREATE INDEX idx_admin_permission_type ON admin_permission(permission_type);
CREATE INDEX idx_admin_permission_status ON admin_permission(status);

-- -------------------- 初始数据 --------------------

-- 插入初始部门数据
INSERT INTO department (dept_type, dept_name, contact_person, contact_phone) VALUES
('行政部门', '教务处', '张主任', '0571-88000001'),
('行政部门', '学生处', '李主任', '0571-88000002'),
('行政部门', '保卫处', '王处长', '0571-88000003'),
('直属部门', '图书馆', '刘馆长', '0571-88000004'),
('直属部门', '信息中心', '陈主任', '0571-88000005'),
('学院', '计算机学院', '赵院长', '0571-88000006'),
('学院', '经济管理学院', '孙院长', '0571-88000007'),
('学院', '外国语学院', '周院长', '0571-88000008');

-- 插入初始管理员数据（密码为'Admin123!@#'使用SM3加密后的值）
INSERT INTO admin (login_name, password_hash, real_name, dept_id, phone, role, status) VALUES
('admin', '13214E14B550A1ACDA98B19B03258CA9C9AE31027481EF24030654135DD11A96', '系统管理员', NULL, '13800000000', 'SYSTEM_ADMIN', 1),
('audit', '13214E14B550A1ACDA98B19B03258CA9C9AE31027481EF24030654135DD11A96', '审计管理员', NULL, '13800000001', 'AUDIT_ADMIN', 1),
('dept1', '13214E14B550A1ACDA98B19B03258CA9C9AE31027481EF24030654135DD11A96', '计算机学院管理员', 6, '13800000002', 'DEPARTMENT_ADMIN', 1),
('dept2', '13214E14B550A1ACDA98B19B03258CA9C9AE31027481EF24030654135DD11A96', '经济管理学院管理员', 7, '13800000003', 'DEPARTMENT_ADMIN', 1);

-- 插入初始权限数据
-- 给计算机学院管理员授权查看社会公众预约的权限（由系统管理员授权）
INSERT INTO admin_permission (admin_id, permission_type, granted_by, status) VALUES
(3, 'VIEW_PUBLIC_APPOINTMENT', 1, 1);

-- 权限类型说明：
-- 'VIEW_PUBLIC_APPOINTMENT' - 查看社会公众预约权限
-- 'MANAGE_PUBLIC_APPOINTMENT' - 管理社会公众预约权限（暂时保留）
-- 'VIEW_ALL_DEPARTMENTS' - 查看所有部门权限
-- 'MANAGE_DEPARTMENT' - 管理指定部门权限