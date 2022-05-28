package telran.college.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import telran.college.entities.StudentEntity;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
	@Modifying
@Query("delete from StudentEntity s where s.id not in"
		+ " (select m.student.id from MarkEntity m group by m.student.id having avg(m.mark) >= :avgMark) ")
	void deleteStudentsAvgMarkLess(double avgMark);
	 /*************************************/
@Query("select s from StudentEntity s where s.id in"
		+ " (select ms.id from MarkEntity m right join m.student ms group by ms.id having count(m.mark) < :countMin) ")
	List<StudentEntity> getStudentsCountLess(long countMin);
}
