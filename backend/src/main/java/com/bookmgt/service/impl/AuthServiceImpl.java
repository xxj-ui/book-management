package com.bookmgt.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookmgt.service.AuthService;
import com.bookmgt.util.SmsUtil;
import com.bookmgt.vo.LoginVO;
import com.bookmgt.dto.LoginRequest;
import com.bookmgt.dto.PasswordChangeRequest;
import com.bookmgt.dto.PhoneChangeRequest;
import com.bookmgt.dto.SmsLoginRequest;
import com.bookmgt.entity.Admin;
import com.bookmgt.entity.BorrowConfig;
import com.bookmgt.entity.BorrowRecord;
import com.bookmgt.entity.Student;
import com.bookmgt.entity.Teacher;
import com.bookmgt.mapper.AdminMapper;
import com.bookmgt.mapper.BorrowConfigMapper;
import com.bookmgt.mapper.BorrowRecordMapper;
import com.bookmgt.mapper.StudentMapper;
import com.bookmgt.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务 — Sa-Token 双 token 实现
 *
 * 双 token 机制:
 *   access token  — 用于请求鉴权，有效期短（30 分钟），由 Sa-Token 管理
 *   refresh token — 用于刷新 access token，有效期长（7 天），存于 Redis
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final AdminMapper adminMapper;
    private final BorrowConfigMapper borrowConfigMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final StringRedisTemplate redisTemplate;

    /** access token 有效期（秒）— 默认 30 分钟 */
    private final long accessTokenTimeout;

    /** refresh token 有效期（秒）— 默认 7 天 */
    private final long refreshTokenTimeout = 7 * 24 * 60 * 60;

    private static final java.util.Map<Integer, String> EDU_MAP = java.util.Map.of(
            1, "高职", 2, "本科", 3, "硕士", 4, "博士"
    );

    private static final String REFRESH_PREFIX = "refresh:";

    public AuthServiceImpl(StudentMapper studentMapper,
                           TeacherMapper teacherMapper,
                           AdminMapper adminMapper,
                           BorrowConfigMapper borrowConfigMapper,
                           BorrowRecordMapper borrowRecordMapper,
                           StringRedisTemplate redisTemplate,
                           @Value("${sa-token.timeout:1800}") long timeout) {
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.adminMapper = adminMapper;
        this.borrowConfigMapper = borrowConfigMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.redisTemplate = redisTemplate;
        this.accessTokenTimeout = timeout;
    }

    // ==================== 登录 ====================

    @Override
    public LoginVO login(LoginRequest req) {
        String phone = req.getPhone();

        // 1. 从数据库查管理员
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getPhone, phone));
        if (admin != null) {
            if (!BCrypt.checkpw(req.getPassword(), admin.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            return buildAdminLoginVO(admin);
        }

        // 2. 尝试学生登录
        Student student = studentMapper.selectByPhone(phone);
        if (student != null) {
            if (!BCrypt.checkpw(req.getPassword(), student.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            checkStatus(student.getStatus());
            return buildLoginVO(student);
        }

        // 3. 尝试教师登录
        Teacher teacher = teacherMapper.selectByPhone(phone);
        if (teacher != null) {
            if (!BCrypt.checkpw(req.getPassword(), teacher.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            checkStatus(teacher.getStatus());
            return buildLoginVO(teacher);
        }

        throw new IllegalArgumentException("该手机号未注册");
    }

    @Override
    public LoginVO smsLogin(SmsLoginRequest req) {
        if (!SmsUtil.verify(redisTemplate, req.getPhone(), req.getCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        String phone = req.getPhone();

        // 先学生，后教师
        Student student = studentMapper.selectByPhone(phone);
        if (student != null) {
            checkStatus(student.getStatus());
            return buildLoginVO(student);
        }

        Teacher teacher = teacherMapper.selectByPhone(phone);
        if (teacher != null) {
            checkStatus(teacher.getStatus());
            return buildLoginVO(teacher);
        }

        throw new IllegalArgumentException("该手机号未注册");
    }

    @Override
    public void sendSmsCode(String phone) {
        SmsUtil.generateAndStore(redisTemplate, phone);
    }

    // ==================== Token 刷新 ====================

    @Override
    public LoginVO refreshToken(String refreshToken) {
        String redisKey = REFRESH_PREFIX + refreshToken;
        String value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            throw new IllegalArgumentException("refreshToken 已过期或无效，请重新登录");
        }

        // 删除旧 refresh token（防重用）
        redisTemplate.delete(redisKey);

        // value 格式: "userId:role"
        int colonIdx = value.indexOf(':');
        Long userId = Long.parseLong(value.substring(0, colonIdx));
        String role = value.substring(colonIdx + 1);

        switch (role) {
            case "admin": {
                Admin admin = adminMapper.selectById(userId);
                if (admin == null) throw new IllegalArgumentException("管理员不存在");
                return buildAdminLoginVO(admin);
            }
            case "student": {
                Student student = studentMapper.selectById(userId);
                if (student == null) throw new IllegalArgumentException("用户不存在");
                checkStatus(student.getStatus());
                return buildLoginVO(student);
            }
            case "teacher": {
                Teacher teacher = teacherMapper.selectById(userId);
                if (teacher == null) throw new IllegalArgumentException("用户不存在");
                checkStatus(teacher.getStatus());
                return buildLoginVO(teacher);
            }
            default:
                throw new IllegalArgumentException("未知用户类型");
        }
    }

    // ==================== 密码管理 ====================

    @Override
    public void changePassword(Long userId, PasswordChangeRequest req) {
        // 先查管理员
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            if (!BCrypt.checkpw(req.getCurrentPassword(), admin.getPassword())) {
                throw new IllegalArgumentException("当前密码错误");
            }
            if (!SmsUtil.verify(redisTemplate, admin.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            admin.setPassword(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
            adminMapper.updateById(admin);
            return;
        }

        // 先查学生表
        Student student = studentMapper.selectById(userId);
        if (student != null) {
            if (!BCrypt.checkpw(req.getCurrentPassword(), student.getPassword())) {
                throw new IllegalArgumentException("当前密码错误");
            }
            if (!SmsUtil.verify(redisTemplate, student.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }

            student.setPassword(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
            studentMapper.updateById(student);
            return;
        }

        // 再查教师表
        Teacher teacher = teacherMapper.selectById(userId);
        if (teacher != null) {
            if (!BCrypt.checkpw(req.getCurrentPassword(), teacher.getPassword())) {
                throw new IllegalArgumentException("当前密码错误");
            }
            if (!SmsUtil.verify(redisTemplate, teacher.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }

            teacher.setPassword(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
            teacherMapper.updateById(teacher);
            return;
        }

        throw new IllegalArgumentException("用户不存在");
    }

    @Override
    public void verifyIdentity(Long userId, PasswordChangeRequest req) {
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            if (!BCrypt.checkpw(req.getCurrentPassword(), admin.getPassword())) {
                throw new IllegalArgumentException("当前密码错误");
            }
            if (!SmsUtil.checkOnly(redisTemplate, admin.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            return;
        }
        Student student = studentMapper.selectById(userId);
        if (student != null) {
            if (!BCrypt.checkpw(req.getCurrentPassword(), student.getPassword())) {
                throw new IllegalArgumentException("当前密码错误");
            }
            if (!SmsUtil.checkOnly(redisTemplate, student.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            return;
        }
        Teacher teacher = teacherMapper.selectById(userId);
        if (teacher != null) {
            if (!BCrypt.checkpw(req.getCurrentPassword(), teacher.getPassword())) {
                throw new IllegalArgumentException("当前密码错误");
            }
            if (!SmsUtil.checkOnly(redisTemplate, teacher.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            return;
        }
        throw new IllegalArgumentException("用户不存在");
    }

    @Override
    public void verifySmsCode(Long userId, String code) {
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            if (!SmsUtil.checkOnly(redisTemplate, admin.getPhone(), code)) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            return;
        }
        Student student = studentMapper.selectById(userId);
        if (student != null) {
            if (!SmsUtil.checkOnly(redisTemplate, student.getPhone(), code)) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            return;
        }
        Teacher teacher = teacherMapper.selectById(userId);
        if (teacher != null) {
            if (!SmsUtil.checkOnly(redisTemplate, teacher.getPhone(), code)) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            return;
        }
        throw new IllegalArgumentException("用户不存在");
    }

    @Override
    public void changePhone(Long userId, PhoneChangeRequest req) {
        Admin admin = adminMapper.selectById(userId);
        if (admin != null) {
            if (!SmsUtil.verify(redisTemplate, admin.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            admin.setPhone(req.getNewPhone());
            adminMapper.updateById(admin);
            return;
        }

        // 先查学生表
        Student student = studentMapper.selectById(userId);
        if (student != null) {
            if (!SmsUtil.verify(redisTemplate, student.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }

            student.setPhone(req.getNewPhone());
            studentMapper.updateById(student);
            return;
        }

        // 再查教师表
        Teacher teacher = teacherMapper.selectById(userId);
        if (teacher != null) {
            if (!SmsUtil.verify(redisTemplate, teacher.getPhone(), req.getVerifyCode())) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }

            teacher.setPhone(req.getNewPhone());
            teacherMapper.updateById(teacher);
            return;
        }

        throw new IllegalArgumentException("用户不存在");
    }

    @Override
    public Object getCurrentUser(Long userId) {
        // 先查学生表
        Student student = studentMapper.selectById(userId);
        if (student != null) {
            student.setCurrentBorrows(countCurrentBorrows(student.getId()));
            student.setTotalBorrows(countTotalBorrows(student.getId()));
            if (student.getEducationLevel() != null) {
                student.setEducationLevelName(EDU_MAP.getOrDefault(student.getEducationLevel(), ""));
            }
            return student;
        }
        // 再查教师表
        Teacher teacher = teacherMapper.selectById(userId);
        if (teacher != null) {
            teacher.setCurrentBorrows(countCurrentBorrows(teacher.getId()));
            teacher.setTotalBorrows(countTotalBorrows(teacher.getId()));
            return teacher;
        }
        return null;
    }

    // ==================== 内部方法 ====================

    /**
     * Sa-Token 登录并创建 refresh token，存入 Redis
     *
     * @param userId 用户标识（Long 类型主键）
     * @param role   角色 "student" / "teacher" / "admin"
     * @return refresh token 值
     */
    private String loginAndCreateRefreshToken(Object userId, String role) {
        StpUtil.login(userId,
                new SaLoginModel()
                        .setDevice("web")
                        .setTimeout(accessTokenTimeout)
                        .setIsWriteHeader(false)
        );
        String refreshToken = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + refreshToken,
                userId + ":" + role,
                refreshTokenTimeout,
                TimeUnit.SECONDS
        );
        return refreshToken;
    }

    /**
     * 为学生构建登录响应
     */
    private LoginVO buildLoginVO(Student student) {
        String refreshToken = loginAndCreateRefreshToken(student.getId(), "student");
        String accessToken = StpUtil.getTokenValue();

        LoginVO vo = new LoginVO();
        vo.setToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(System.currentTimeMillis() + accessTokenTimeout * 1000);
        vo.setUser(buildStudentUserVO(student));
        return vo;
    }

    /**
     * 为教师构建登录响应
     */
    private LoginVO buildLoginVO(Teacher teacher) {
        String refreshToken = loginAndCreateRefreshToken(teacher.getId(), "teacher");
        String accessToken = StpUtil.getTokenValue();

        LoginVO vo = new LoginVO();
        vo.setToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(System.currentTimeMillis() + accessTokenTimeout * 1000);
        vo.setUser(buildTeacherUserVO(teacher));
        return vo;
    }

    /**
     * 为管理员构建登录响应（数据从 t_admin 表读取）
     */
    private LoginVO buildAdminLoginVO(Admin admin) {
        String refreshToken = loginAndCreateRefreshToken(admin.getId(), "admin");
        String accessToken = StpUtil.getTokenValue();

        LoginVO vo = new LoginVO();
        vo.setToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(System.currentTimeMillis() + accessTokenTimeout * 1000);

        LoginVO.UserVO uv = new LoginVO.UserVO();
        uv.setId("0");
        uv.setDbId(admin.getId());
        uv.setName(admin.getName());
        uv.setRole("admin");
        uv.setPhone(admin.getPhone());
        uv.setStatus("active");
        uv.setCurrentBorrows(0);
        uv.setTotalBorrows(0);
        vo.setUser(uv);
        return vo;
    }

    /**
     * 为 Student 构建 UserVO（含部门、专业、班级、学历、借阅配置）
     */
    private LoginVO.UserVO buildStudentUserVO(Student student) {
        LoginVO.UserVO uv = new LoginVO.UserVO();
        uv.setId(student.getUserId());
        uv.setDbId(student.getId());
        uv.setName(student.getName());
        uv.setRole("student");
        uv.setPhone(student.getPhone());
        uv.setDepartment(student.getDepartmentName());
        uv.setMajor(student.getMajorName());
        uv.setClassName(student.getClassName());
        uv.setStatus(student.getStatus() != null && student.getStatus() == 1 ? "active" : "overdue_locked");
        uv.setCurrentBorrows(countCurrentBorrows(student.getId()));
        uv.setTotalBorrows(countTotalBorrows(student.getId()));

        // 查询学生借阅规则（role = 1）
        LambdaQueryWrapper<BorrowConfig> wrapper = new LambdaQueryWrapper<BorrowConfig>()
                .eq(BorrowConfig::getRole, 1);
        if (student.getEducationLevel() != null) {
            wrapper.eq(BorrowConfig::getEducationLevel, student.getEducationLevel());
        }
        BorrowConfig config = borrowConfigMapper.selectOne(wrapper);
        if (config != null) {
            uv.setMaxBooks(config.getMaxBooks());
            uv.setMaxDays(config.getMaxDays());
        }

        if (student.getEducationLevel() != null) {
            uv.setEducationLevel(EDU_MAP.getOrDefault(student.getEducationLevel(), ""));
        }
        return uv;
    }

    /**
     * 为 Teacher 构建 UserVO（含专业、借阅配置）
     */
    private LoginVO.UserVO buildTeacherUserVO(Teacher teacher) {
        LoginVO.UserVO uv = new LoginVO.UserVO();
        uv.setId(teacher.getUserId());
        uv.setDbId(teacher.getId());
        uv.setName(teacher.getName());
        uv.setRole("teacher");
        uv.setPhone(teacher.getPhone());
        uv.setMajor(teacher.getMajorName());
        uv.setStatus(teacher.getStatus() != null && teacher.getStatus() == 1 ? "active" : "overdue_locked");
        uv.setCurrentBorrows(countCurrentBorrows(teacher.getId()));
        uv.setTotalBorrows(countTotalBorrows(teacher.getId()));

        // 查询教师借阅规则（role = 2）
        LambdaQueryWrapper<BorrowConfig> wrapper = new LambdaQueryWrapper<BorrowConfig>()
                .eq(BorrowConfig::getRole, 2);
        BorrowConfig config = borrowConfigMapper.selectOne(wrapper);
        if (config != null) {
            uv.setMaxBooks(config.getMaxBooks());
            uv.setMaxDays(config.getMaxDays());
        }
        return uv;
    }

    /**
     * 查询当前借阅数（状态 1=借阅中, 3=超期）
     */
    private int countCurrentBorrows(Long userId) {
        Long count = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .in(BorrowRecord::getStatus, 1, 3)
        );
        return count != null ? count.intValue() : 0;
    }

    /**
     * 查询总借阅数
     */
    private int countTotalBorrows(Long userId) {
        Long count = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
        );
        return count != null ? count.intValue() : 0;
    }

    /**
     * 校验用户状态（status = 3 时禁用）
     */
    private void checkStatus(Integer status) {
        if (status != null && status == 3) {
            throw new IllegalArgumentException("该账号已被禁用，请联系管理员");
        }
    }
}
