package vv.pms.project.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vv.pms.project.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // JpaRepository provides findAll(), findById(), save(), deleteById()
}
