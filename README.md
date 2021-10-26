#Artineer Spring 4주차 강의노트

# 테스트
### 테스트는 개발자들에게 매우 중요하다.
### 테스트 패키지는 소스(원본) 패키지와 동일한 구조, 테스트하는 클래스 파일은 대상이 되는 클래스파일 뒤에 test를 붙여 만든다.
### 각 layer에 대한 특성이 다르기 때문에 테스트 코드를 작성하는 방법이 다 다르다.
#### @Test : 테스트 코드로 동작시키는 어노테이션
테스트 코드에 들어가는 함수 명은 어떤 것을 테스트 하려는지 명시를 해야 한다.(한글로 작성 가능)
테스트 코드는 무언가 주어지고(given), 실행 했을 때(when) 어떤 결과가 나와야 한다.(then) 라는 패턴으로 작성
- 유닛테스트 - 기능, 함수가 정상적으로 동작 하는지 검증하기 위한 코드 - 테스트 자체의 목적도 있지만 함수의 동작을 명시하는 문서의 역할도 하기 때문에 자세하게 적는 것이 중요하다.
## 도메인 유닛 테스트
### Article.update() 테스트 코드
#### ArticleTest.java
~~~ java
@Test
public void update_호출하면_title_content_필드_값이_변경되어야_한다() {
    // given -> 테스트 코드를 작성하기 위해 필요한 선처리 작업
    Article article = Article.builder()
            .title("title before")
            .content("content before")
            .build();

    // when -> 업데이트 함수를 통해 변경
    article.update("title after", "content after");

    /// then -> 이런 결과가 나와야 한다.
    assertThat("title after").isEqualTo(article.getTitle());
    assertThat("content after").isEqualTo(article.getContent());
}
~~~

## TDD(Test Driven Development)
무조건 실패하는 형태로 코드를 작성하고(실제 내용물이 없기 때문에), 테스트 코드가 성공하도록 하는 코드를 작성한다.
- 테스트 코드 먼저 작성하고 함수의 스펙을 정하고 실제로 구현
### Article.of() 테스트 코드
#### ArticleTest.java
~~~ java
class ArticleTest {
    // ...
  
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
~~~

#### Article.java
~~~ java
public class Article {
  // ...
  public static Article of(ArticleDto.ReqPost from) {
    return Article.builder()
//                .title(from.getTitle())
//                .content(from.getContent())
            .build();
  }
  
  // ...
}
~~~
- of()함수가 테스트 코드를 통과할 수 있도록 변경 하려면  .title(from.getTitle())과 .content(from.getContent())의 주석을 제거

## 서비스 계층 유닛 테스트
- 서비스 계층은 특성상 기본적으로 의존성을 가지고 있다.
- 의존성 때문에 조금은 어렵다고 느낄 수 있다.
- @ExtendWith : 타겟 클래스에 의존성이 존재하기 때문에 가짜 객체를 만들어주는 어노테이션 -  가짜 객체는 스펙으로 가지고 있는 특징들만 가지고 온다 - 즉, 구현체는 담겨있지 않다. (함수명, 파라미터, 반환타입 등)
- @InjectMocks : 가짜 객체 대상을 선언하는 어노테이션 (클래스)
- @Mock : 가짜 객체를 만드는 어노테이션 (객체)
- Service도 Repository도 가짜 객체를 사용

### findById 테스트
#### ArticleServiceTest.java
~~~ java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
  @InjectMocks
  private ArticleService articleService;

  @Mock
  private ArticleRepository articleRepository;

  @Test
  public void findById_id_에_맞는_Article_객체를_가져온다() {
    // given
    Long idStub = 1L; // Stub은 가짜 값을 표현한다.
    Optional<Article> articleOptStub = Optional.of(Article.builder()
            .id(idStub).title("title").content("content").build());
    when(articleRepository.findById(idStub)).thenReturn(articleOptStub); 
	// when -> 가짜 객체가 해야하는 역할(적지 않으면 가짜 객체는 실체가 없기 때문에 null이 반환됨

    // when
    Article result = articleService.findById(idStub);

    // then
    assertThat(result.getId()).isEqualTo(articleOptStub.get().getId());
  }
}
~~~
### 예외처리 테스트
#### ArticleServiceTest.java
~~~ java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    // ...
  @Test
  public void findById_id_에_맞는_Article_객체가_없다면_DATA_IS_NOT_FOUND_오류발생() {
    // given
    Long idStub = 1L;

    // when
    when(articleRepository.findById(idStub)).thenReturn(Optional.empty());

    // then
    assertThatThrownBy(() -> articleService.findById(idStub))
            .isInstanceOf(ApiException.class)
            .hasMessage("article is not found");
  }
}
~~~

## 테스트 커버리지
- 테스트 코드를 어느 수준으로 작성 하는지(모든 케이스, 필요한 부분만, 짜지 않는다. 등) 회사마다 다르다.
- 테스트 코드를 많이 작성하면 튼튼한 시스템을 구축할 수 있다.
- 참고할 수 있는 문서가 되기 때문에 리팩토링에 있어 부담을 덜어준다.
- 하지만, 일이 더 늘어나기 때문에 시간적으로 부담이 될 수 있다.

## AnyMatcher
- 정해진 특정 값을 넣어주지 않고 같은 타입이 들어오게 된다면 정상적으로 동작되는 테스트 코드를 작성할 수 있게 도와준다.
- 하지만 사용하려면 모든 값을 any로 사용해야 한다.
### AnyMatcher를 사용한 테스트 코드
#### ArticleServiceTest.java
~~~ java
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
~~~

## Repository 유닛 테스트
- 데이터베이스와 관련된 테스트 코드 작성은 어렵다.
- 어떤 Insert가 성공하고 updata를 하는 등 수정이 이루어지는 테스트를 했을 때 실제 데이터베이스에 반영되고 롤백이 안된다면 곤란.
    - 롤백을 하는 코드도 모두 작성해야 하기 때문에 어려웠다.
- @DataJpaTest : 위에 이슈들을 해결할 수 있게 해주는 어노테이션
    - H2 DB를 자동으로 벤더로 적용시켜줌
- 의존성이 따로 없기 때문에 Mock을 사용하지 않고 실제 객체를 사용하기 위해 빈을 가지고 오는 SpringExtension.class를 사용 
    - @ExtendWith(SpringExtension.class) : 실제 빈을 가지고 와줌
    - Repository에 해당하는 빈만 가지고 온다. 
- 테스트 코드에서는 순환참조 이슈를 생각하지 않고 필드주입으로 작성 (자유롭게 사용해도 된다.)
### 테스트 코드 작성을 위해 간단한 쿼리메소드 
#### ArticleRepository.java
~~~ java
public interface ArticleRepository extends CrudRepository<Article, Long> {
    List<Article> findByTitle(String title);
}
~~~
### Repository 유닛 테스트 코드 작성 (findByTitle)
#### ArticleRepositoryTest.java
~~~ java
@ExtendWith(SpringExtension.class)
@DataJpaTest
class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void findByTitle_title_조회하면_일치하는_Article_객체들이_반환된다() {
        // given
        String javaStub = "Java";
        String pythonStub = "Python";

        articleRepository.save(Article.builder().title(javaStub).build());
        articleRepository.save(Article.builder().title(javaStub).build());
        articleRepository.save(Article.builder().title(pythonStub).build());

        // when
        List<Article> articles = articleRepository.findByTitle(javaStub);

        // then
        assertThat(articles.size()).isEqualTo(2);
        assertTrue(articles.stream().allMatch(a -> javaStub.equals(a.getTitle())), "title 필드 값이 모두 " + javaStub + " 인가?");
    }
}
~~~

## Controller 유닛 테스트
- @WebMvcTest(ArticleController.class) : Controller와 관련된 빈만 가져오는 어노테이션
#### ArticleRepositoryTest.java
~~~ java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

}
~~~
- Controller는 Repository와 다르게 Service에 대한 의존성이 있다
    - Service에 대한 의존성이 있기 때문에 가짜 객체를 사용해야 하는데 Controller는 실제 객체를 사용하고 Service만 가짜 객체를 사용하고 싶다.
    - @MockBean을 사용하면 객체를 스프링 컨테이너에 등록
#### ArticleRepositoryTest.java
~~~ java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
}
~~~
- Controller 함수들은 API 진입점으로 활용되기 때문에 API를 호출할 수 있는 클라이언트 MockMvc 추가
    - WebMvcTest를 등록하면 MockMvc도 사용가능
#### ArticleRepositoryTest.java
~~~ java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;
}
~~~
### MockMvc를 활용해서 테스트 코드 구현
#### ArticleRepositoryTest.java
~~~ java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Response.ok() 함수 호출시 code 값이 ApiCode.SUCCESS 값을 가져야 한다.")
	// 한글로 만들기 싫을 때 사용할 수 있음.
    public void get_whenYouCallOk_ThenReturnSuccessCode() throws Exception {
        // given
        Long idStub = 1L;
        when(articleService.findById(idStub)).thenReturn(Article.builder().id(idStub).build());

        // when, then
        mvc.perform(get("/api/v1/article/" + idStub)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // print를 사용하지 않는다면 내용 출력이 안됨.
                .andExpect(status().isOk());
    }
}
~~~
### 실제 응답을 받는 값을 얻고 싶다면…
#### ArticleRepositoryTest.java
~~~ java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Response.ok() 함수 호출시 code 값이 ApiCode.SUCCESS 값을 가져야 한다.")
    public void get_whenYouCallOk_ThenReturnSuccessCode() throws Exception {
        // given
        Long idStub = 1L;
        when(articleService.findById(idStub)).thenReturn(Article.builder().id(idStub).build());

        // when
        String response = mvc.perform(get("/api/v1/article/" + idStub)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        assertThat(ApiCode.SUCCESS.getName()).isSubstringOf(response);
    }
}
~~~

# 이후 내용은 28일
