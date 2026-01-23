package com.heronix.service;

import com.heronix.model.domain.Course;
import com.heronix.model.enums.CourseCategory;
import com.heronix.model.enums.CourseType;
import com.heronix.model.enums.EducationLevel;
import com.heronix.model.enums.GradeLevel;
import com.heronix.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Grade-Appropriate Course Service
 *
 * Provides grade-level appropriate course filtering and validation
 * following US K-12 education standards.
 *
 * Course Appropriateness by Grade Level:
 * - Pre-K/Kindergarten: Foundational skills (letters, numbers, social skills)
 * - Elementary (1-5): Core academics + specials (art, music, PE)
 * - Middle School (6-8): Pre-algebra/Algebra, Life/Earth Science, exploratory electives
 * - High School (9-12): College prep, AP courses, career/technical education
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Service
public class GradeAppropriateCourseService {

    @Autowired(required = false)
    private CourseRepository courseRepository;

    /**
     * Get courses appropriate for a specific grade level
     * Filters from database courses based on minGradeLevel and maxGradeLevel
     */
    public List<Course> getCoursesForGrade(GradeLevel gradeLevel) {
        if (gradeLevel == null) {
            return Collections.emptyList();
        }

        if (courseRepository == null) {
            log.warn("CourseRepository not available, returning sample courses");
            return getSampleCoursesForGrade(gradeLevel);
        }

        List<Course> allCourses = courseRepository.findAll();
        return filterCoursesForGrade(allCourses, gradeLevel);
    }

    /**
     * Filter a list of courses to only those appropriate for a grade level
     */
    public List<Course> filterCoursesForGrade(List<Course> courses, GradeLevel gradeLevel) {
        if (courses == null || gradeLevel == null) {
            return Collections.emptyList();
        }

        int gradeValue = gradeLevel.getNumericValue();

        return courses.stream()
                .filter(course -> isCourseAppropriateForGrade(course, gradeValue))
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific course is appropriate for a grade level
     */
    public boolean isCourseAppropriateForGrade(Course course, int gradeLevel) {
        if (course == null) {
            return false;
        }

        // Check minimum grade level
        if (course.getMinGradeLevel() != null && gradeLevel < course.getMinGradeLevel()) {
            return false;
        }

        // Check maximum grade level
        if (course.getMaxGradeLevel() != null && gradeLevel > course.getMaxGradeLevel()) {
            return false;
        }

        // If no grade restrictions, check education level
        if (course.getMinGradeLevel() == null && course.getMaxGradeLevel() == null) {
            return isEducationLevelAppropriate(course.getLevel(), gradeLevel);
        }

        return true;
    }

    /**
     * Check if education level matches grade level
     */
    private boolean isEducationLevelAppropriate(EducationLevel courseLevel, int gradeLevel) {
        if (courseLevel == null) {
            return true; // No restriction
        }

        switch (courseLevel) {
            case PRE_K:
                return gradeLevel == -1;
            case KINDERGARTEN:
                return gradeLevel == 0;
            case ELEMENTARY:
                return gradeLevel >= 1 && gradeLevel <= 5;
            case MIDDLE_SCHOOL:
                return gradeLevel >= 6 && gradeLevel <= 8;
            case HIGH_SCHOOL:
                return gradeLevel >= 9 && gradeLevel <= 12;
            default:
                return true;
        }
    }

    /**
     * Get validation message if course is not appropriate for grade
     */
    public String getIneligibilityReason(Course course, GradeLevel gradeLevel) {
        if (course == null || gradeLevel == null) {
            return "Invalid course or grade level";
        }

        int gradeValue = gradeLevel.getNumericValue();

        if (course.getMinGradeLevel() != null && gradeValue < course.getMinGradeLevel()) {
            GradeLevel minGrade = GradeLevel.fromNumericValue(course.getMinGradeLevel());
            return String.format("%s requires at least %s. Student is in %s.",
                    course.getCourseName(),
                    minGrade != null ? minGrade.getDisplayName() : "Grade " + course.getMinGradeLevel(),
                    gradeLevel.getDisplayName());
        }

        if (course.getMaxGradeLevel() != null && gradeValue > course.getMaxGradeLevel()) {
            GradeLevel maxGrade = GradeLevel.fromNumericValue(course.getMaxGradeLevel());
            return String.format("%s is only available up to %s. Student is in %s.",
                    course.getCourseName(),
                    maxGrade != null ? maxGrade.getDisplayName() : "Grade " + course.getMaxGradeLevel(),
                    gradeLevel.getDisplayName());
        }

        return null; // Course is appropriate
    }

    // ========================================================================
    // SAMPLE COURSE DATA (for UI when database is not available)
    // ========================================================================

    /**
     * Get sample courses appropriate for a grade level
     * Used when CourseRepository is not available (e.g., during registration form testing)
     */
    public List<Course> getSampleCoursesForGrade(GradeLevel gradeLevel) {
        if (gradeLevel == null) {
            return Collections.emptyList();
        }

        List<Course> courses = new ArrayList<>();

        switch (gradeLevel.getEducationLevel()) {
            case PRE_K:
                courses.addAll(createPreKCourses());
                break;
            case KINDERGARTEN:
                courses.addAll(createKindergartenCourses());
                break;
            case ELEMENTARY:
                courses.addAll(createElementaryCourses(gradeLevel));
                break;
            case MIDDLE_SCHOOL:
                courses.addAll(createMiddleSchoolCourses(gradeLevel));
                break;
            case HIGH_SCHOOL:
                courses.addAll(createHighSchoolCourses(gradeLevel));
                break;
            default:
                break;
        }

        return courses;
    }

    // ========================================================================
    // PRE-K COURSES (Age 3-4)
    // ========================================================================
    private List<Course> createPreKCourses() {
        List<Course> courses = new ArrayList<>();
        long id = 1;

        // Core developmental areas
        courses.add(createCourse(id++, "PRK-LA", "Language Development", -1, -1, true,
                "Letters, phonics awareness, vocabulary building"));
        courses.add(createCourse(id++, "PRK-MATH", "Early Math Concepts", -1, -1, true,
                "Numbers 1-10, shapes, patterns, sorting"));
        courses.add(createCourse(id++, "PRK-SOC", "Social-Emotional Learning", -1, -1, true,
                "Sharing, cooperation, emotional regulation"));
        courses.add(createCourse(id++, "PRK-SCI", "Discovery Science", -1, -1, true,
                "Nature exploration, weather, five senses"));

        // Specials
        courses.add(createCourse(id++, "PRK-ART", "Creative Arts", -1, -1, false,
                "Drawing, painting, crafts"));
        courses.add(createCourse(id++, "PRK-MUS", "Music & Movement", -1, -1, false,
                "Songs, rhythm, dance"));
        courses.add(createCourse(id++, "PRK-PE", "Gross Motor Skills", -1, -1, false,
                "Running, jumping, climbing, coordination"));

        return courses;
    }

    // ========================================================================
    // KINDERGARTEN COURSES (Age 5-6)
    // ========================================================================
    private List<Course> createKindergartenCourses() {
        List<Course> courses = new ArrayList<>();
        long id = 100;

        // Core subjects
        courses.add(createCourse(id++, "K-ELA", "Kindergarten Language Arts", 0, 0, true,
                "Alphabet mastery, sight words, beginning reading"));
        courses.add(createCourse(id++, "K-MATH", "Kindergarten Math", 0, 0, true,
                "Numbers 1-20, addition/subtraction basics, measurement"));
        courses.add(createCourse(id++, "K-SCI", "Kindergarten Science", 0, 0, true,
                "Living things, seasons, basic physics"));
        courses.add(createCourse(id++, "K-SS", "Kindergarten Social Studies", 0, 0, true,
                "Community helpers, families, rules"));

        // Specials
        courses.add(createCourse(id++, "K-ART", "Art", 0, 0, false, "Basic art skills"));
        courses.add(createCourse(id++, "K-MUS", "Music", 0, 0, false, "Singing, instruments"));
        courses.add(createCourse(id++, "K-PE", "Physical Education", 0, 0, false, "Motor skills, games"));

        return courses;
    }

    // ========================================================================
    // ELEMENTARY COURSES (Grades 1-5)
    // ========================================================================
    private List<Course> createElementaryCourses(GradeLevel gradeLevel) {
        List<Course> courses = new ArrayList<>();
        int grade = gradeLevel.getNumericValue();
        long id = 200 + (grade * 20);

        String gradePrefix = "G" + grade;

        // Core - English Language Arts
        courses.add(createCourse(id++, gradePrefix + "-ELA", "English Language Arts " + grade, grade, grade, true,
                "Reading, writing, grammar, vocabulary"));

        // Core - Mathematics (progressive)
        String mathName = getMathNameForGrade(grade);
        courses.add(createCourse(id++, gradePrefix + "-MATH", mathName, grade, grade, true,
                getMathDescForGrade(grade)));

        // Core - Science
        String scienceName = getScienceNameForGrade(grade);
        courses.add(createCourse(id++, gradePrefix + "-SCI", scienceName, grade, grade, true,
                getScienceDescForGrade(grade)));

        // Core - Social Studies
        String ssName = getSocialStudiesNameForGrade(grade);
        courses.add(createCourse(id++, gradePrefix + "-SS", ssName, grade, grade, true,
                getSocialStudiesDescForGrade(grade)));

        // Specials (same across elementary)
        courses.add(createCourse(id++, gradePrefix + "-ART", "Art", grade, grade, false, "Visual arts"));
        courses.add(createCourse(id++, gradePrefix + "-MUS", "Music", grade, grade, false, "Music education"));
        courses.add(createCourse(id++, gradePrefix + "-PE", "Physical Education", grade, grade, false, "PE and health"));

        // Technology (grades 3-5)
        if (grade >= 3) {
            courses.add(createCourse(id++, gradePrefix + "-TECH", "Computer Skills", grade, grade, false,
                    "Keyboarding, basic applications"));
        }

        // World Language introduction (grades 4-5)
        if (grade >= 4) {
            courses.add(createCourse(id++, gradePrefix + "-SPAN", "Spanish Introduction", grade, grade, false,
                    "Basic Spanish vocabulary and culture"));
        }

        return courses;
    }

    // ========================================================================
    // MIDDLE SCHOOL COURSES (Grades 6-8)
    // ========================================================================
    private List<Course> createMiddleSchoolCourses(GradeLevel gradeLevel) {
        List<Course> courses = new ArrayList<>();
        int grade = gradeLevel.getNumericValue();
        long id = 600 + ((grade - 6) * 30);

        // Core - English Language Arts
        courses.add(createCourse(id++, "MS-ELA" + grade, "English " + grade, grade, grade, true,
                "Literature, composition, grammar"));

        // Core - Mathematics (differentiated tracks)
        if (grade == 6) {
            courses.add(createCourse(id++, "MS-MATH6", "Math 6", 6, 6, true, "Ratios, fractions, decimals"));
            courses.add(createCourse(id++, "MS-MATH6A", "Math 6 Advanced", 6, 6, false, "Pre-Algebra readiness"));
        } else if (grade == 7) {
            courses.add(createCourse(id++, "MS-MATH7", "Math 7", 7, 7, true, "Pre-Algebra concepts"));
            courses.add(createCourse(id++, "MS-PREALG", "Pre-Algebra", 7, 8, false, "Advanced pre-algebra"));
        } else { // Grade 8
            courses.add(createCourse(id++, "MS-MATH8", "Math 8", 8, 8, true, "Pre-Algebra"));
            courses.add(createCourse(id++, "MS-ALG1", "Algebra I", 8, 9, false, "First year algebra (advanced)"));
        }

        // Core - Science
        if (grade == 6) {
            courses.add(createCourse(id++, "MS-ESCI", "Earth Science", 6, 6, true, "Geology, weather, space"));
        } else if (grade == 7) {
            courses.add(createCourse(id++, "MS-LSCI", "Life Science", 7, 7, true, "Biology basics, ecosystems"));
        } else {
            courses.add(createCourse(id++, "MS-PSCI", "Physical Science", 8, 8, true, "Physics and chemistry intro"));
        }

        // Core - Social Studies
        if (grade == 6) {
            courses.add(createCourse(id++, "MS-WHIST6", "World History & Geography", 6, 6, true, "Ancient civilizations"));
        } else if (grade == 7) {
            courses.add(createCourse(id++, "MS-WHIST7", "World History", 7, 7, true, "Medieval to modern"));
        } else {
            courses.add(createCourse(id++, "MS-USHIST", "US History", 8, 8, true, "American history"));
        }

        // Electives - World Languages
        courses.add(createCourse(id++, "MS-SPAN1", "Spanish I", 6, 8, false, "Beginning Spanish"));
        courses.add(createCourse(id++, "MS-FREN1", "French I", 6, 8, false, "Beginning French"));

        // Electives - Arts
        courses.add(createCourse(id++, "MS-ART", "Visual Art", 6, 8, false, "Drawing, painting, sculpture"));
        courses.add(createCourse(id++, "MS-BAND", "Band", 6, 8, false, "Instrumental music"));
        courses.add(createCourse(id++, "MS-CHOIR", "Choir", 6, 8, false, "Vocal music"));

        // Electives - Technology
        courses.add(createCourse(id++, "MS-COMP", "Computer Applications", 6, 8, false, "Office applications, typing"));
        if (grade >= 7) {
            courses.add(createCourse(id++, "MS-CODE", "Intro to Coding", 7, 8, false, "Basic programming"));
        }

        // Physical Education & Health
        courses.add(createCourse(id++, "MS-PE", "Physical Education", 6, 8, true, "Fitness, sports, teamwork"));
        courses.add(createCourse(id++, "MS-HLTH", "Health Education", 6, 8, true, "Wellness, nutrition, safety"));

        return courses;
    }

    // ========================================================================
    // HIGH SCHOOL COURSES (Grades 9-12)
    // ========================================================================
    private List<Course> createHighSchoolCourses(GradeLevel gradeLevel) {
        List<Course> courses = new ArrayList<>();
        int grade = gradeLevel.getNumericValue();
        long id = 900 + ((grade - 9) * 50);

        // ===== ENGLISH (Required 4 years) =====
        courses.add(createCourse(id++, "ENG" + grade, "English " + (grade - 8), grade, grade, true,
                getEnglishDesc(grade)));
        if (grade >= 11) {
            courses.add(createCourse(id++, "AP-ENG", "AP English Language", 11, 12, false,
                    "College-level composition and rhetoric"));
        }
        if (grade == 12) {
            courses.add(createCourse(id++, "AP-LIT", "AP English Literature", 12, 12, false,
                    "College-level literary analysis"));
        }

        // ===== MATHEMATICS (Required 3-4 years) =====
        if (grade == 9) {
            courses.add(createCourse(id++, "ALG1", "Algebra I", 9, 10, true, "Linear equations, inequalities, functions"));
            courses.add(createCourse(id++, "GEOM", "Geometry", 9, 10, false, "For students who completed Algebra I in 8th grade"));
        } else if (grade == 10) {
            courses.add(createCourse(id++, "GEOM", "Geometry", 9, 10, true, "Proofs, shapes, trigonometry intro"));
            courses.add(createCourse(id++, "ALG2", "Algebra II", 10, 11, false, "Advanced algebra for accelerated students"));
        } else if (grade == 11) {
            courses.add(createCourse(id++, "ALG2", "Algebra II", 10, 11, true, "Polynomials, rational functions, logarithms"));
            courses.add(createCourse(id++, "PRECALC", "Pre-Calculus", 11, 12, false, "Trigonometry, limits, sequences"));
        } else { // Grade 12
            courses.add(createCourse(id++, "PRECALC", "Pre-Calculus", 11, 12, true, "Required if not yet taken"));
            courses.add(createCourse(id++, "CALC", "Calculus", 12, 12, false, "Derivatives, integrals"));
            courses.add(createCourse(id++, "AP-CALC-AB", "AP Calculus AB", 11, 12, false, "College-level calculus"));
            courses.add(createCourse(id++, "AP-CALC-BC", "AP Calculus BC", 12, 12, false, "Advanced calculus"));
            courses.add(createCourse(id++, "STATS", "Statistics", 11, 12, false, "Data analysis, probability"));
            courses.add(createCourse(id++, "AP-STATS", "AP Statistics", 11, 12, false, "College-level statistics"));
        }

        // ===== SCIENCE (Required 3-4 years) =====
        if (grade == 9) {
            courses.add(createCourse(id++, "BIO", "Biology", 9, 10, true, "Cell biology, genetics, ecology"));
        } else if (grade == 10) {
            courses.add(createCourse(id++, "CHEM", "Chemistry", 10, 11, true, "Atomic structure, reactions, stoichiometry"));
            courses.add(createCourse(id++, "AP-BIO", "AP Biology", 10, 12, false, "College-level biology"));
        } else if (grade == 11) {
            courses.add(createCourse(id++, "PHYS", "Physics", 11, 12, true, "Mechanics, waves, electricity"));
            courses.add(createCourse(id++, "AP-CHEM", "AP Chemistry", 11, 12, false, "College-level chemistry"));
        } else { // Grade 12
            courses.add(createCourse(id++, "AP-PHYS", "AP Physics", 11, 12, false, "College-level physics"));
            courses.add(createCourse(id++, "ANAT", "Anatomy & Physiology", 11, 12, false, "Human body systems"));
            courses.add(createCourse(id++, "ENVS", "Environmental Science", 11, 12, false, "Ecosystems, sustainability"));
            courses.add(createCourse(id++, "AP-ENVS", "AP Environmental Science", 11, 12, false, "College-level environmental science"));
        }

        // ===== SOCIAL STUDIES (Required 3-4 years) =====
        if (grade == 9) {
            courses.add(createCourse(id++, "WHIST", "World History", 9, 10, true, "Ancient to modern world civilizations"));
        } else if (grade == 10) {
            courses.add(createCourse(id++, "AP-WHIST", "AP World History", 10, 11, false, "College-level world history"));
        }
        if (grade == 10 || grade == 11) {
            courses.add(createCourse(id++, "USHIST", "US History", 10, 11, true, "American history"));
            courses.add(createCourse(id++, "AP-USHIST", "AP US History", 10, 11, false, "College-level US history"));
        }
        if (grade == 12) {
            courses.add(createCourse(id++, "GOV", "Government", 12, 12, true, "US government and civics"));
            courses.add(createCourse(id++, "ECON", "Economics", 12, 12, true, "Micro and macroeconomics"));
            courses.add(createCourse(id++, "AP-GOV", "AP Government", 12, 12, false, "College-level government"));
            courses.add(createCourse(id++, "AP-ECON", "AP Economics", 12, 12, false, "College-level economics"));
        }

        // ===== WORLD LANGUAGES (2 years recommended) =====
        courses.add(createCourse(id++, "SPAN1", "Spanish I", 9, 11, false, "Beginning Spanish"));
        courses.add(createCourse(id++, "SPAN2", "Spanish II", 9, 12, false, "Intermediate Spanish"));
        if (grade >= 10) {
            courses.add(createCourse(id++, "SPAN3", "Spanish III", 10, 12, false, "Advanced Spanish"));
        }
        if (grade >= 11) {
            courses.add(createCourse(id++, "AP-SPAN", "AP Spanish", 11, 12, false, "College-level Spanish"));
        }
        courses.add(createCourse(id++, "FREN1", "French I", 9, 11, false, "Beginning French"));
        courses.add(createCourse(id++, "FREN2", "French II", 9, 12, false, "Intermediate French"));

        // ===== PHYSICAL EDUCATION & HEALTH =====
        courses.add(createCourse(id++, "PE", "Physical Education", 9, 12, true, "Fitness, sports, wellness"));
        courses.add(createCourse(id++, "HLTH", "Health", 9, 10, true, "Health education"));

        // ===== ELECTIVES - FINE ARTS =====
        courses.add(createCourse(id++, "ART1", "Art I", 9, 12, false, "Drawing, painting fundamentals"));
        courses.add(createCourse(id++, "ART2", "Art II", 10, 12, false, "Advanced visual arts"));
        courses.add(createCourse(id++, "BAND", "Band", 9, 12, false, "Instrumental ensemble"));
        courses.add(createCourse(id++, "CHOIR", "Choir", 9, 12, false, "Vocal ensemble"));
        courses.add(createCourse(id++, "DRAMA", "Drama", 9, 12, false, "Theater arts"));

        // ===== ELECTIVES - TECHNOLOGY & CAREER =====
        courses.add(createCourse(id++, "COMP1", "Computer Science Intro", 9, 12, false, "Programming basics"));
        if (grade >= 10) {
            courses.add(createCourse(id++, "AP-CSP", "AP Computer Science Principles", 10, 12, false, "College-level CS"));
            courses.add(createCourse(id++, "AP-CSA", "AP Computer Science A", 10, 12, false, "Java programming"));
        }

        // CTE (Career & Technical Education)
        if (grade >= 10) {
            courses.add(createCourse(id++, "BUS", "Business Fundamentals", 10, 12, false, "Business basics"));
            courses.add(createCourse(id++, "ACCT", "Accounting", 10, 12, false, "Financial accounting"));
        }
        if (grade >= 11) {
            courses.add(createCourse(id++, "AUTO", "Automotive Technology", 11, 12, false, "Vehicle maintenance"));
            courses.add(createCourse(id++, "HLTHSCI", "Health Science", 11, 12, false, "Medical career prep"));
            courses.add(createCourse(id++, "ENGTECH", "Engineering Technology", 11, 12, false, "Engineering principles"));
        }

        return courses;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Course createCourse(long id, String code, String name, int minGrade, int maxGrade,
                                 boolean isCore, String description) {
        Course course = new Course();
        course.setId(id);
        course.setCourseCode(code);
        course.setCourseName(name);
        course.setMinGradeLevel(minGrade);
        course.setMaxGradeLevel(maxGrade);
        course.setIsCoreRequired(isCore);
        course.setCourseCategory(isCore ? CourseCategory.CORE : CourseCategory.ELECTIVE);
        course.setDescription(description);
        course.setCredits(1.0);
        course.setMaxStudents(30);
        course.setCurrentEnrollment(new Random().nextInt(20) + 5);
        course.setCourseType(CourseType.REGULAR);
        return course;
    }

    // Elementary math names by grade
    private String getMathNameForGrade(int grade) {
        switch (grade) {
            case 1: return "Math 1";
            case 2: return "Math 2";
            case 3: return "Math 3";
            case 4: return "Math 4";
            case 5: return "Math 5";
            default: return "Mathematics";
        }
    }

    private String getMathDescForGrade(int grade) {
        switch (grade) {
            case 1: return "Addition, subtraction to 20, place value";
            case 2: return "Addition/subtraction to 100, intro multiplication";
            case 3: return "Multiplication, division, fractions intro";
            case 4: return "Multi-digit operations, fractions, decimals";
            case 5: return "Fractions operations, decimals, volume";
            default: return "Grade-level mathematics";
        }
    }

    // Elementary science names by grade
    private String getScienceNameForGrade(int grade) {
        switch (grade) {
            case 1: return "Science 1";
            case 2: return "Science 2";
            case 3: return "Science 3";
            case 4: return "Science 4";
            case 5: return "Science 5";
            default: return "Science";
        }
    }

    private String getScienceDescForGrade(int grade) {
        switch (grade) {
            case 1: return "Weather, plants, animals";
            case 2: return "Life cycles, habitats, matter";
            case 3: return "Force, motion, ecosystems";
            case 4: return "Energy, electricity, Earth systems";
            case 5: return "Human body, chemistry basics, space";
            default: return "Grade-level science";
        }
    }

    // Elementary social studies names by grade
    private String getSocialStudiesNameForGrade(int grade) {
        switch (grade) {
            case 1: return "Social Studies 1";
            case 2: return "Social Studies 2";
            case 3: return "Social Studies 3";
            case 4: return "Social Studies 4";
            case 5: return "Social Studies 5";
            default: return "Social Studies";
        }
    }

    private String getSocialStudiesDescForGrade(int grade) {
        switch (grade) {
            case 1: return "Family, neighborhood, citizenship";
            case 2: return "Community, maps, local history";
            case 3: return "Communities around the world";
            case 4: return "State history and geography";
            case 5: return "US history, geography, government";
            default: return "Grade-level social studies";
        }
    }

    // High school English descriptions
    private String getEnglishDesc(int grade) {
        switch (grade) {
            case 9: return "Literature, writing fundamentals, grammar";
            case 10: return "World literature, essay writing";
            case 11: return "American literature, research writing";
            case 12: return "British literature, college prep writing";
            default: return "English language arts";
        }
    }
}
