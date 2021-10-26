# 4주차 강의노트

## 테스트의 종류

- 유닛 테스트
  - 함수, 기능 수준에서의 테스트 코드 작성
  - 특정 기능에 대한 문서 수준의 기능을 제공
  - 운영되고 있는 서비스를 리팩토링하는 것은 굉장히 두렵다.
    - 코드를 리팩토링하고 싶을 때 충분한 유닛 테스트코드가 작성되어 있으면 안정적으로 변경이 가능

- 통합 테스트
  - 전체 코드, DB와 같은 외부 환경, 실행되는 환경까지 확인하는 테스트
  - 일반적으로 스프링에서 통합테스트라고 하면 하나의 API를 진입부터 마지막까지 테스트

- 인수 테스트
  - 프로젝트에 참여하는 사람들이 모두 모여 시나리오를 만들고 이를 테스트
  - E2E 테스트

## TDD (Test Driven Development) 테스트 주도 개발

- 테스트 코드를 먼저 작성하고 함수의 스펙을 정한다.
- 테스트 코드가 성공 될 수 있도록 소스 코드를 작성한다.

### Article.update() 테스트 코드 작성해보기

- 테스트 코드를 먼저 작성하여 실패하는 테스트를 만든다.
- 만드려고 하는 함수의 의도, 틀을 먼저 잡는 과정

```java
class ArticleTest {
    @Test
    public void update_호출하면_title_content_필드_값이_변경되어야_한다() {
        // given
        Article article = Article.builder()
                .title("title before")
                .content("content before")
                .build();

        // when
        article.update("title after", "content after");

        // then
        assertThat(article.getTitle()).isEqualTo("title after");
        assertThat(article.getContent()).isEqualTo("content after");
    }
}
```

```java
public class Article {
    // ...
  
    public void update(String title, String content) {
//        this.title = title;
//        this.content = content;
    }
    
    // ...
}
```

- update() 함수를 테스트 코드가 합격할 수 있도록 구현한다.

```java
public class Article {
    // ...
  
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    // ...
}
```

### Article.of() 테스트 코드 작성해보기

```java
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
```

- of() 함수가 테스트 코드를 통과할 수 있도록 변경

```java
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

```

- of() 함수가 테스트 코드를 통과할 수 있도록 변경

```java
public class Article {
  // ...
  public static Article of(ArticleDto.ReqPost from) {
    return Article.builder()
                .title(from.getTitle())
                .content(from.getContent())
            .build();
  }
  
  // ...
}

```

## 내가 작성한 테스트 코드 한번에 돌리기

- gradle > verification > test 실행
- CI/CD 환경을 구축하게 된다면 형상이 PUSH 되었을 때 지속적으로 테스트 코드를 확인하게 하여 이상이 없는지 오류를 검출한다.

### 도메인 유닛 테스트
- 방금까지 Article 같은 도메인 객체를 테스트하는 것을 이야기 함.

### 서비스 계층 유닛 테스트
- Service 에 해당하는 계층을 테스트하는 유닛 테스트를 의미
- 다른 Service 나 Repository 들을 의존하는 구조가 일반적
- Mocking 하는 기법을 기본적으로 사용해서 테스트를 한다.

### Mocking 의 필요성
- 유닛테스트는 하나의 기능을 테스트 하는 걸 목적으로 한다.
- 기능에 외부와의 의존성이 있다면 테스트를 위해 Mocking 처리를 한다.
- 테스트의 속도를 위해, 서비스 레이어에 필요한 빈들만 가져오기 위해서 Mockito 라이브러리를 활용해 모두 Mock 객체를 가져온다.
- 실제 객체로 테스트를 한다면 다른 의존성들을 배제하는 것이 어렵다.

```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    private ArticleService articleService;

    @Test
    public void findById_id_에_맞는_Article_객체를_가져온다() {
        
    }
}
```

- ArticleService 객체가 가지고 있는 의존성에도 가짜 객체를 넣어준다.

```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
  @InjectMocks
  private ArticleService articleService;

  @Mock
  private ArticleRepository articleRepository;

  @Test
  public void findById_id_에_맞는_Article_객체를_가져온다() {

  }
}
```

```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
  @InjectMocks
  private ArticleService articleService;

  @Mock
  private ArticleRepository articleRepository;

  @Test
  public void findById_id_에_맞는_Article_객체를_가져온다() {
    // given
    Long idStub = 1L;
    Optional<Article> articleOptStub = Optional.of(Article.builder()
            .id(idStub).title("title").content("content").build());
    when(articleRepository.findById(idStub)).thenReturn(articleOptStub);

    // when
    Article result = articleService.findById(idStub);

    // then
    assertThat(result.getId()).isEqualTo(articleOptStub.get().getId());
  }
}
```

- Mocking 을 통해 ArticleService 객체가 가지고 있는 의존성을 제거한다.
- 이를 활용하여 ArticleService 로직만을 집중하여 테스트가 가능하다.
- 테스트 코드를 작성할 때에는 내가 작성한 테스트 코드가 정말 의미가 있는지를 한번씩 확인해볼 필요가 있다.

## 예외 테스트하기

```java
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
```

## AnyMatcher 활용하기

- 고정적인 stub 객체를 생성하는 것이 거북하다면 any 함수들을 사용할 수 있다.

```java
        Long idStub = anyLong();
```

## Repository 유닛 테스트

- 데이터베이스와 관련된 영역을 테스트 코드를 작성하는 것은 사실 어렵다
- 무언가 데이터베이스를 update 하는 함수를 테스트한다고 했을 때 실제 데이터베이스가 수정되버린다면?
  - 롤백처리는 어떻게 해야지?
- 변경된 데이터베이스 내용이 다른 테스트 코드에 영향을 준다면?
  - 테스트 코드 중 데이터베이스에 아무것도 없을 때 조회하면 오류를 반환해야 정상인 케이스가 있다.
  - 만약 다른 테스트 코드에서 이 데이터베이스에 INSERT를 했다면?

```java
@ExtendWith(SpringExtension.class)
@DataJpaTest
class ArticleRepositoryTest {

}
```

- Repository 영역은 Service 와 달리 의존성을 많이 가지고 있지 않기 때문에 가짜객체를 생성하는 MocktoExtension.class 보다는
- 실제 Spring Container 에서 빈을 가져오는 SpringExtension.class 를 사용한다.
- @DataJpaTest 어노테이션의 역할을 3가지 정도 이다.
  - Spring Container 에서 Repository 에 해당하는 빈들만 가져온다.
  - 데이터베이스 벤더를 자동으로 H2Database 를 적용해준다
    - 이는 인메모리 데이터베이스로 테스트 목적으로 사용하기 적합 (테스트하고 바로 휘발됨으로)
  - 하나의 테스트 코드들이 하나의 트랜잭션으로 잡혀서 다른 테스트 코드와는 독립적으로 테스트 되도록 구성한다.

```java
@ExtendWith(SpringExtension.class)
@DataJpaTest
class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;
}
```

- 등록된 스프링 컨테이너에서 ArticleRepository 의존성을 주입한다.

## 테스트코드 작성을 위해 간단한 쿼리메소드 구현

```java
public interface ArticleRepository extends CrudRepository<Article, Long> {
    List<Article> findByTitle(String title);
}
```

- JPA 는 메소드 명을 정해진 규칙에 따라 작성하면 간단한 쿼리들을 구현할 수 있도록 한다.
- JPA 는 쿼리를 작성하기 위해서 다양한 방법을 제공
  - 쿼리 메소드
  - @Query 를 활용한 native 쿼리
  - QueryDSL 라이브러리 활용
  - ...
- 세부 내용이 궁금하다면 JPA 내용을 확인

```java
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
```

## Controller 유닛 테스트

- @WebMvcTest 어노테이션은 Controller 와 관련된 객체만 스프링 컨테이너 빈으로 등록해준다.

```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

}
```

- Mock 객체를 스프링 컨테이너에 등록하고 싶다면 @MockBean 어노테이션을 활용

```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
}
```

- 컨트롤러 함수들은 api 진입점으로 활용되기 때문에 가짜 api client(postman 같은) 역할을 하는 MockMvc 를 추가

```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;
}
```

- MockMvc 를 활용해서 테스트 코드 구현

```java
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

        // when, then
        mvc.perform(get("/api/v1/article/" + idStub)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}
```

- Result 까지 비교해야 한다면 아래처럼 구현 가능

```java
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
```

## 통합 테스트

```java
@SpringBootTest
public class ArticleControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ArticleRepository articleRepository;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
  }

  @Test
  @DisplayName("post() 실행되면 article 객체가 새로 생성되어야 한다")
  public void post_whenItIsOccured_thenArticleShouldbeStored() throws Exception {
    // given
    ArticleDto.ReqPost requestBodyStub = ArticleDto.ReqPost.builder()
            .title("title")
            .content("content")
            .build();

    mvc.perform(post("/api/v1/article/")
                    .characterEncoding("utf-8")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestBodyStub))
            )
            .andDo(print())
            .andExpect(status().isOk());

    // when
    long size = articleRepository.count();

    // then
    assertThat(size).isEqualTo(1);
  }
}
```

## 테스트 커버리지
- 어느정도 수준으로 테스트 코드를 작성하는가?
- 이는 회사마다 원하는 정도가 다르고, 정해진 답은 없다.
- 테스트 코드를 많이 작성한 회사는 그만큼 참고가 될 문서가 존재하는 것.
- 시스템에 테스트 코드가 많이 존재한다면 오류를 맡을 수 있는 장치를 만드는 것으로, 안정적인 서비스 제공이 가능하다.
- 테스트 코드를 작성하는 것은 그렇지 않은 것 보다 분명 시간이 더 걸리는 일이다. 그것을 기억해야 한다.

## 4주차 강의

- https://www.youtube.com/watch?v=xfJfEKhnedk