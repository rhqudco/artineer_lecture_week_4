package com.artineer.spring_lecture_week_2.domain;

import com.artineer.spring_lecture_week_2.dto.ArticleDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {
    @Test
    public void update_호출하면_title_content_필드_값이_변경되어야_한다() {
        // given -> 테스트 코드를 작성하기 위해 필요한 선처리 작업
        Article article = Article.builder()
                .title("title before")
                .content("content before")
                .build();

        // when -> 업데이트 함수를 통해 변경
        article.update("title after", "content after");

        // then -> 이런 결과가 나와야 한다.
        assertThat("title after").isEqualTo(article.getTitle());
        assertThat("content after").isEqualTo(article.getContent());
    }

    @Test
    public void of_호출하면_Article_객체를_반환해야_한다() {
        // given
        ArticleDto.ReqPost request = ArticleDto.ReqPost.builder()
                .title("title")
                .content("content")
                .build();

        // when
        Article article = Article.of(request);

        // then
        assertThat(article.getTitle()).isEqualTo("title");
        assertThat(article.getContent()).isEqualTo("content");
    }
}