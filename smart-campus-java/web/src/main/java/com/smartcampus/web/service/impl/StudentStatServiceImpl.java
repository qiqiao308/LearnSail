package com.smartcampus.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.mapper.CourseMapper;
import com.smartcampus.common.mapper.CourseSectionMapper;
import com.smartcampus.common.mapper.EnrollmentMapper;
import com.smartcampus.common.mapper.ExamRecordMapper;
import com.smartcampus.common.mapper.LearningProgressMapper;
import com.smartcampus.common.mapper.UserMapper;
import com.smartcampus.common.po.Course;
import com.smartcampus.common.po.CourseSection;
import com.smartcampus.common.po.Enrollment;
import com.smartcampus.common.po.ExamRecord;
import com.smartcampus.common.po.LearningProgress;
import com.smartcampus.common.po.User;
import com.smartcampus.common.vo.EnrollmentVO;
import com.smartcampus.common.vo.StudentDashboardVO;
import com.smartcampus.web.service.StudentStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class StudentStatServiceImpl implements StudentStatService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private CourseSectionMapper courseSectionMapper;

    @Autowired
    private LearningProgressMapper learningProgressMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public StudentDashboardVO getMyDashboard(Long userId) {
        StudentDashboardVO vo = new StudentDashboardVO();

        // enrolled course count
        LambdaQueryWrapper<Enrollment> enrolledWrapper = new LambdaQueryWrapper<>();
        enrolledWrapper.eq(Enrollment::getUserId, userId);
        enrolledWrapper.ne(Enrollment::getStatus, "DROPPED");
        vo.setEnrolledCourseCount(enrollmentMapper.selectCount(enrolledWrapper));

        // completed course count
        LambdaQueryWrapper<Enrollment> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Enrollment::getUserId, userId);
        completedWrapper.eq(Enrollment::getStatus, "COMPLETED");
        vo.setCompletedCourseCount(enrollmentMapper.selectCount(completedWrapper));

        // total learning minutes
        LambdaQueryWrapper<LearningProgress> progressWrapper = new LambdaQueryWrapper<>();
        progressWrapper.eq(LearningProgress::getUserId, userId);
        progressWrapper.eq(LearningProgress::getIsCompleted, 1);
        long completedSectionCount = learningProgressMapper.selectCount(progressWrapper);
        vo.setTotalLearningMinutes(completedSectionCount * 30L);

        // average score
        LambdaQueryWrapper<ExamRecord> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.eq(ExamRecord::getUserId, userId);
        examWrapper.eq(ExamRecord::getStatus, "SUBMITTED");
        List<ExamRecord> examRecords = examRecordMapper.selectList(examWrapper);
        OptionalDouble avgScore = examRecords.stream()
                .mapToDouble(r -> r.getScore() != null ? r.getScore().doubleValue() : 0.0)
                .average();
        vo.setAverageScore(avgScore.isPresent() ? Math.round(avgScore.getAsDouble() * 100.0) / 100.0 : 0.0);

        // recent enrollments (latest 5)
        LambdaQueryWrapper<Enrollment> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(Enrollment::getUserId, userId);
        recentWrapper.orderByDesc(Enrollment::getEnrollTime);
        recentWrapper.last("LIMIT 5");
        List<Enrollment> recentEnrollments = enrollmentMapper.selectList(recentWrapper);

        List<EnrollmentVO> enrollmentVOList = new ArrayList<>();
        for (Enrollment enrollment : recentEnrollments) {
            EnrollmentVO enrollmentVO = new EnrollmentVO();
            enrollmentVO.setId(enrollment.getId());
            enrollmentVO.setCourseId(enrollment.getCourseId());
            enrollmentVO.setStatus(enrollment.getStatus());
            if (enrollment.getEnrollTime() != null) {
                enrollmentVO.setEnrollTime(enrollment.getEnrollTime().format(FORMATTER));
            }

            // course info
            Course course = courseMapper.selectById(enrollment.getCourseId());
            if (course != null) {
                enrollmentVO.setCourseTitle(course.getTitle());
                enrollmentVO.setCourseCover(course.getCoverImage());

                // teacher name
                User teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    enrollmentVO.setTeacherName(teacher.getRealName());
                }

                // total sections
                LambdaQueryWrapper<CourseSection> sectionWrapper = new LambdaQueryWrapper<>();
                sectionWrapper.eq(CourseSection::getCourseId, course.getId());
                long totalSections = courseSectionMapper.selectCount(sectionWrapper);
                enrollmentVO.setTotalSections((int) totalSections);

                // completed sections
                LambdaQueryWrapper<LearningProgress> lpWrapper = new LambdaQueryWrapper<>();
                lpWrapper.eq(LearningProgress::getUserId, userId);
                lpWrapper.eq(LearningProgress::getCourseId, course.getId());
                lpWrapper.eq(LearningProgress::getIsCompleted, 1);
                long completedSections = learningProgressMapper.selectCount(lpWrapper);
                enrollmentVO.setCompletedSections((int) completedSections);

                // progress
                if (totalSections > 0) {
                    double progress = (double) completedSections / totalSections * 100.0;
                    enrollmentVO.setProgress(Math.round(progress * 100.0) / 100.0);
                } else {
                    enrollmentVO.setProgress(0.0);
                }
            }

            enrollmentVOList.add(enrollmentVO);
        }
        vo.setRecentEnrollments(enrollmentVOList);

        return vo;
    }
}
