package com.smartcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.admin.service.AdminStatService;
import com.smartcampus.common.mapper.*;
import com.smartcampus.common.po.*;
import com.smartcampus.common.vo.ChartDataVO;
import com.smartcampus.common.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminStatServiceImpl implements AdminStatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // total users
        stats.setTotalUsers(userMapper.selectCount(null));

        // total courses
        stats.setTotalCourses(courseMapper.selectCount(null));

        // total enrollments
        stats.setTotalEnrollments(enrollmentMapper.selectCount(null));

        // today active users (enrollments created today)
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<Enrollment> enrollWrapper = new LambdaQueryWrapper<>();
        enrollWrapper.between(Enrollment::getEnrollTime, todayStart, todayEnd);
        stats.setTodayActiveUsers(enrollmentMapper.selectCount(enrollWrapper));

        // user growth: group by month for last 12 months
        List<ChartDataVO> userGrowth = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            LocalDateTime startTime = LocalDateTime.of(monthStart, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(monthEnd, LocalTime.MAX);

            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.between(User::getCreateTime, startTime, endTime);
            long count = userMapper.selectCount(userWrapper);

            ChartDataVO data = new ChartDataVO();
            data.setName(monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            data.setValue(count);
            userGrowth.add(data);
        }
        stats.setUserGrowth(userGrowth);

        // course popularity: top 10 courses by enrollment count
        List<Enrollment> allEnrollments = enrollmentMapper.selectList(null);
        Map<Long, Long> courseCountMap = allEnrollments.stream()
                .collect(Collectors.groupingBy(Enrollment::getCourseId, Collectors.counting()));

        List<ChartDataVO> coursePopularity = courseCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    ChartDataVO data = new ChartDataVO();
                    Course course = courseMapper.selectById(entry.getKey());
                    data.setName(course != null ? course.getTitle() : "未知课程");
                    data.setValue(entry.getValue());
                    return data;
                })
                .collect(Collectors.toList());
        stats.setCoursePopularity(coursePopularity);

        return stats;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> result = new HashMap<>();

        // user role distribution
        List<User> allUsers = userMapper.selectList(null);
        Map<String, Long> roleDistribution = allUsers.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
        result.put("roleDistribution", roleDistribution);
        result.put("totalUsers", allUsers.size());

        return result;
    }

    @Override
    public Map<String, Object> getCourseStats() {
        Map<String, Object> result = new HashMap<>();

        // course status distribution
        List<Course> allCourses = courseMapper.selectList(null);
        Map<String, Long> statusDistribution = allCourses.stream()
                .collect(Collectors.groupingBy(course -> course.getStatus() != null ? course.getStatus() : "UNKNOWN",
                        Collectors.counting()));
        result.put("statusDistribution", statusDistribution);

        // category distribution
        Map<Long, Long> categoryDistribution = allCourses.stream()
                .filter(c -> c.getCategoryId() != null)
                .collect(Collectors.groupingBy(Course::getCategoryId, Collectors.counting()));

        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : categoryDistribution.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            CourseCategory category = courseCategoryMapper.selectById(entry.getKey());
            item.put("categoryName", category != null ? category.getName() : "未知分类");
            item.put("count", entry.getValue());
            categoryList.add(item);
        }
        result.put("categoryDistribution", categoryList);
        result.put("totalCourses", allCourses.size());

        return result;
    }

    @Override
    public Map<String, Object> getDailyActivity() {
        Map<String, Object> result = new HashMap<>();

        List<Enrollment> allEnrollments = enrollmentMapper.selectList(null);

        // group by date for last 30 days
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            long count = allEnrollments.stream()
                    .filter(e -> e.getEnrollTime() != null
                            && !e.getEnrollTime().isBefore(startTime)
                            && !e.getEnrollTime().isAfter(endTime))
                    .count();

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            item.put("count", count);
            dailyData.add(item);
        }

        result.put("dailyActivity", dailyData);
        return result;
    }
}
