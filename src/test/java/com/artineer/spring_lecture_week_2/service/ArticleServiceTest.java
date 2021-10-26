package com.artineer.spring_lecture_week_2.service;

import com.artineer.spring_lecture_week_2.domain.Article;
import com.artineer.spring_lecture_week_2.domain.ArticleRepository;
import com.artineer.spring_lecture_week_2.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    private ArticleService articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Test
    public void findById_id_에_맞는_Article_객체를_가져온다() {
        // given
        Long idStub = anyLong();
        Optional<Article> articleOptStub = Optional.of(Article.builder()
                .id(idStub).title("title").content("content").build());

        when(articleRepository.findById(idStub)).thenReturn(articleOptStub);
        // when -> 가짜 객체가 해야하는 역할(적지 않으면 가짜 객체는 실체가 없기 때문에 null이 반환됨
        // when
        Article result = articleService.findById(idStub);

        // then
        assertThat(result.getId()).isEqualTo(articleOptStub.get().getId());
    }

    @Test
    public void findById_id_에_맞는_Article_객체가_없다면_DATA_IS_NOT_FOUND_오류발생() {
        // given
        Long idStub = anyLong();

        // when
        when(articleRepository.findById(idStub)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> articleService.findById(idStub))
                .isInstanceOf(ApiException.class)
                .hasMessage("article is not found");
    }
}