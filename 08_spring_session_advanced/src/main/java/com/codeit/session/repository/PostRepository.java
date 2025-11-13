package com.codeit.session.repository;

import com.codeit.session.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.deleted = false ORDER BY p.id DESC")
    List<Post> findAllVisiblePostsWithAuthor();

    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.id DESC")
    List<Post> findAllPostsWithAuthor();
}
