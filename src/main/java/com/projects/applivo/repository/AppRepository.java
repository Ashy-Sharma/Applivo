package com.projects.applivo.repository;

import com.projects.applivo.entity.App;
import com.projects.applivo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    Optional<App> findByIdAndDeveloper(Long id, User developer);

    List<App> findByDeveloper(User developer);

    Page<App> findByIsPublishedTrue(Pageable pageable);

    Page<App> findByIsPublishedTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    boolean existsByNameAndDeveloper(String name, User developer);

    Optional<App> findByIdAndIsPublishedTrue(Long id);

    @Query(value = """
                select *
                    from apps
                    where is_published = true
                    and match(name, description)
                    against(:query in natural language mode)
            """,
            countQuery = """
                    select count(*)
                    from apps
                    where is_published = true
                      and match(name, description)
                       against(:query in natural language mode)
                    """,
            nativeQuery = true)
    Page<App> searchPublishedApps(@Param("query") String query, Pageable pageable);

}
