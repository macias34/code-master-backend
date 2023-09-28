package com.macias34.codemastery.course.service;

import com.macias34.codemastery.course.dto.course.CourseDto;
import com.macias34.codemastery.course.dto.course.CourseResponseDto;
import com.macias34.codemastery.course.dto.course.CreateCourseDto;
import com.macias34.codemastery.course.dto.course.UpdateCourseDto;
import com.macias34.codemastery.course.entity.CategoryEntity;
import com.macias34.codemastery.course.entity.ChapterEntity;
import com.macias34.codemastery.course.entity.CourseEntity;
import com.macias34.codemastery.course.entity.LessonEntity;
import com.macias34.codemastery.course.mapper.CourseMapper;
import com.macias34.codemastery.course.model.CourseFilter;
import com.macias34.codemastery.course.repository.CategoryRepository;
import com.macias34.codemastery.course.repository.CourseRepository;
import com.macias34.codemastery.exception.BadRequestException;
import com.macias34.codemastery.exception.ResourceNotFoundException;
import com.macias34.codemastery.exception.StorageException;
import com.macias34.codemastery.util.DateTimeUtil;
import com.macias34.codemastery.util.DtoValidator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService storageService;
    private final CourseMapper courseMapper;

    public CourseResponseDto searchCourses(CourseFilter courseFilter, int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<CourseEntity> coursesPage = this.courseRepository.searchCourseEntitiesByFilters(courseFilter, paging);
        List<CourseEntity> courses = coursesPage.getContent();

        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("Courses not found");
        }

        List<CourseDto> courseDtos = courses.stream().map(courseMapper::fromEntityToDto).toList();

        return CourseResponseDto.builder().currentPage(page)
                .totalElements(coursesPage.getTotalElements())
                .totalPages(coursesPage.getTotalPages())
                .courses(courseDtos).build();
    }

    public CourseDto getCourseById(int id) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        return courseMapper.fromEntityToDto(course);
    }

    @Transactional
    public CourseDto deleteCourseById(int id) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        try {
            storageService.deleteByfileName(course.getId() + course.getAvatarFileExtension());

            for (ChapterEntity chapter : course.getChapters()) {
                for (LessonEntity lesson : chapter.getLessons()) {
                    try {
                        storageService.deleteByfileName(lesson.getId() + ".mp4");
                    } catch (Exception e) {
                        throw new StorageException("Error with removing file occurred");
                    }
                }
            }

            courseRepository.deleteById(id);
        } catch (Exception e) {
            throw new StorageException("Error with removing file occurred");
        }

        return courseMapper.fromEntityToDto(course);
    }

    public CourseDto createCourse() {

        CourseEntity courseEntity = CourseEntity.builder().name("Untitled course").build();

        courseRepository.save(courseEntity);

        return courseMapper.fromEntityToDto(courseEntity);
    }

    public Resource getCourseAvatarById(int id) {
        try {
            CourseEntity course = courseRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            return storageService.load(id + course.getAvatarFileExtension());
        } catch (Exception e) {
            throw new RuntimeException("Error with returning file for given lesson");
        }
    }

    @Transactional
    public CourseDto updateCourse(int id, UpdateCourseDto dto) {
        DtoValidator.validate(dto);

        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Set<CategoryEntity> categories = new HashSet<>();

        if (dto.getCategoriesIds() != null) {
            for (int categoryId : dto.getCategoriesIds()) {
                Optional<CategoryEntity> category = categoryRepository.findById(categoryId);
                category.ifPresent(categories::add);
            }
            course.setCategories(categories);
        }

        if (dto.getPrice() != null) {
            course.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }
        if (dto.getName() != null) {
            course.setName(dto.getName());
        }
        if (dto.getInstructorName() != null) {
            course.setInstructorName(dto.getInstructorName());
        }

        courseRepository.save(course);

        return courseMapper.fromEntityToDto(course);
    }
}
