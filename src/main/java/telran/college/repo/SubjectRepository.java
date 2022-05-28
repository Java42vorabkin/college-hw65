package telran.college.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import telran.college.entities.SubjectEntity;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
	@Query("select s from SubjectEntity s where s.id in"
			+ " (select ms.id from MarkEntity m right join m.subject"
			+ " ms group by ms.id having avg(m.mark) < :avgMark) ")
	List<SubjectEntity> getSubjectsAvgMarkLess(double avgMark);
}
