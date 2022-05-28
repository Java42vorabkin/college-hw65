package telran.college.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import telran.college.dto.*;
import telran.college.entities.MarkEntity;
import telran.college.entities.StudentEntity;
import telran.college.entities.SubjectEntity;
import telran.college.entities.projection.*;
import telran.college.repo.*;
@Service
public class CollegeServiceImpl implements CollegeService {
	StudentRepository studentsRepository;
	SubjectRepository subjectsRepository;
	MarkRepository marksRepository;
	

	public CollegeServiceImpl(StudentRepository studentsRepository, SubjectRepository subjectsRepository,
			MarkRepository marksRepository) {
		this.studentsRepository = studentsRepository;
		this.subjectsRepository = subjectsRepository;
		this.marksRepository = marksRepository;
	}

	@Override
	@Transactional
	public void addStudent(Student student) {
		if (studentsRepository.existsById(student.id)) {
			throw new RuntimeException(String.format("Student with id %d already exists", student.id));
		}
		StudentEntity studentEntity = new StudentEntity(student.id, student.name);
		studentsRepository.save(studentEntity);
		

	}

	@Override
	@Transactional
	public void addSubject(Subject subject) {
		if (subjectsRepository.existsById(subject.id)) {
			throw new RuntimeException(String.format("Subject with id %d already exists", subject.id));
		}
		SubjectEntity subjectEntity = new SubjectEntity(subject.id, subject.subjectName);
		subjectsRepository.save(subjectEntity);

	}

	@Override
	@Transactional
	public void addMark(Mark mark) {
		StudentEntity studentEntity = studentsRepository.findById(mark.stid).orElse(null);
		if (studentEntity == null) {
			throw new RuntimeException(String.format("Student with id %d doesn't exist", mark.stid));
		}
		SubjectEntity subjectEntity = subjectsRepository.findById(mark.suid).orElse(null);
		if (subjectEntity == null) {
			throw new RuntimeException(String.format("Subject with id %d doesn't exist", mark.suid));
		}
		MarkEntity markEntity = new MarkEntity(mark.mark, studentEntity, subjectEntity);
		marksRepository.save(markEntity);

	}
	@Override
	public List<Integer> getStudentMarksSubject(String name, String subjectName) {
		// Getting student's (defined by name) marks
		// on certain subject (defined by subjectName)
		List<MarkProj> markEntities =
				marksRepository.findByStudentNameAndSubjectSubjectName(name, subjectName);
		return markEntities.stream().map(MarkProj::getMark).toList();
	}

	@Override
	public List<Student> goodCollegeStudents() {
		//students with avg mark greater than total avg mark of the college
		return marksRepository.findGoodStudents().stream().map(sp -> new Student(sp.getId(),
				sp.getName())).toList();
	}
	

	@Override
	public List<Student> bestStudents(int nStudents) {
		//the given number of the best students
		return toStudentsFromProj(marksRepository.findBestStudents(nStudents));
				
	}
	private List<Student> toStudentsFromProj(List<IdNameProj> projList) {
		return projList.stream().map(in -> new Student(in.getId(), in.getName()))
		.toList();
	}

	@Override
	public List<Student> bestStudentsSubject(int nStudents, String subjectName) {
		// V.R.
		// best students (nStudents) on the subject (subjectName)
		return toStudentsFromProj(marksRepository.findBestStudentsSubject(nStudents, subjectName));
	}

	@Override
	public Subject subjectGreatestAvgMark() {
		// V.R.
		// The subject with the greatest average mark
		if(marksRepository.count() == 0) {
			return null;
		}
		IdNameProj inp = marksRepository.findSubjectWithGreatestAvgMark(); 
		return new Subject(inp.getId(), inp.getName());
	}

	@Override
	@Transactional
	public void deleteStudentsAvgMarkLess(int avgMark) {
		/* delete students of following types:
		 * - with average mark less than certain marks 
		 * - without any marks
		 */
		studentsRepository.deleteStudentsAvgMarkLess((double)avgMark);

	}

	@Override
	public List<String> getStudentsSubjectMark(String subjectName, int mark) {
		//getting student names those who have at least one mark on certain subject
		// greater or equal the given one		
		return marksRepository.findDistinctBySubjectSubjectNameAndMarkGreaterThanEqual(subjectName, mark)
				.stream().map(StudentNameProj::getStudentName).toList();
	}

	@Override
	@Transactional
	public List<Student> deleteStudentsMarksCountLess(int count) {
		//remove all students having amount of marks less 
		//than the given one; returns being deleted students
		List<StudentEntity> studentsForDelete = studentsRepository.getStudentsCountLess(count);
		studentsForDelete.forEach(studentsRepository::delete);
		return studentsForDelete.stream().map(se -> new Student(se.getId(), se.getName()))
				.toList();
	}

	@Override
	public List<Subject> subjectsAvgMarkGreater(int avgMark) {
		// returns subjects with average marks greater than certain mark
		return marksRepository.findSubjectsAvgMarkGreater(avgMark).
				stream().map(in -> new Subject(in.getId(), in.getName()))
				.toList();
	}

	@Override
	public List<Student> getStudentsAllMarksSubject(int mark, String subject) {
		//getting students having all marks on given subject >= given mark
		return toStudentsFromProj(marksRepository.findStudentsAllMarksGreaterEqual(mark, subject));
	}

	@Override
	public List<Student> getStudentsMaxMarksCount() {
		//getting students having maximal COUNT of marks
		// V.R.
		if(marksRepository.count() == 0) {
			return new ArrayList<>();
		}
		return toStudentsFromProj(marksRepository.findStudentsMaxMarks());
	}

	@Override
	public List<Subject> getSubjectsAvgMarkLess(int avgMark) {
		//getting subjects having avg mark less than the given (using right join 
		// for subjects not having  marks at all)
		return subjectsRepository.getSubjectsAvgMarkLess(avgMark).
				stream().map(in -> new Subject(in.getId(), in.getSubjectName()))
				.toList();
	}

}
