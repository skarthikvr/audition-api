package com.audition.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModelPojoTest {

    @Test
    void auditionPost_gettersSetters() {
        final AuditionPost p = new AuditionPost();
        p.setId(10);
        p.setUserId(5);
        p.setTitle("t");
        p.setBody("b");

        assertThat(p.getId()).isEqualTo(10);
        assertThat(p.getUserId()).isEqualTo(5);
        assertThat(p.getTitle()).isEqualTo("t");
        assertThat(p.getBody()).isEqualTo("b");
    }

    @Test
    void comment_gettersSetters() {
        final Comment c = new Comment();
        c.setId(2);
        c.setPostId(10);
        c.setName("n");
        c.setBody("bb");
        c.setEmail("e@e.com");

        assertThat(c.getId()).isEqualTo(2);
        assertThat(c.getPostId()).isEqualTo(10);
        assertThat(c.getEmail()).isEqualTo("e@e.com");
    }
}

