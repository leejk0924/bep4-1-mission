# 회원과 글 모듈 분리하기

> Spring Boot와 Spring Data JPA로 회원, 글, 댓글을 만들고, 이벤트·HTTP API·회원 복제본을 이용해
> 회원 모듈과 글 모듈을 분리한다.

이 문서는 작업이 진행되는 동안 계속 갱신된다. 특히 "확인 결과"는 특정 시점의 실행 결과를 캡처한
것이므로, 구조를 바꾸는 리팩토링 이후에는 다시 실행해서 갱신해야 한다.

## 1. 실행 방법

- **JDK**: 25 (`build.gradle`의 Gradle Toolchain이 `JavaLanguageVersion.of(25)`를 강제한다)
- **DB**: H2 파일 DB, MySQL 호환 모드
  - 프로필: `dev` (`application.yml`의 `spring.profiles.active: dev`)
  - 접속 정보(`application-dev.yml`): `jdbc:h2:./db_dev;MODE=MySQL`, user `sa`, password 없음
  - 프로젝트 루트에 `db_dev.mv.db` 파일로 생성되며 `.gitignore`에 포함되어 커밋되지 않는다
- **실행 명령**

  ```bash
  ./gradlew bootRun
  ```

- 기본 포트: `8080`
- H2 웹 콘솔: `http://localhost:8080/h2-console` (JDBC URL 위와 동일)

## 2. 구조 설명

### 모듈 구성

```
com.back
├── boundedContext
│   ├── member            # 회원 가입, 활동점수, 보안 팁 API
│   │   ├── in             #  컨트롤러 / 이벤트 리스너 / 데이터 초기화 → Facade만 호출
│   │   ├── app             #  Facade(트랜잭션 경계) + UseCase(가입 로직)
│   │   ├── domain          #  Member(순수 엔티티), MemberPolicy(순수 정책 객체)
│   │   └── out              #  MemberRepository
│   └── post               # 글, 댓글, 회원 복제본
│       ├── in / app / domain / out  (member와 동일한 계층 구조)
│       └── domain 안에 PostMember(회원 복제본), Post, PostComment
├── shared                 # 모듈 간 통신 전용 — 이벤트 / DTO / ApiClient (엔티티 절대 금지)
│   ├── member  (MemberDto, MemberJoinedEvent, MemberModifiedEvent, MemberApiClient)
│   └── post    (PostDto, PostCommentDto, PostCreatedEvent, PostCommentCreatedEvent)
└── global                 # 프레임워크 공통 관심사 (RsData, DomainException, BaseEntity, ...)
```

두 모듈은 서로의 도메인 클래스나 Repository를 직접 참조하지 않는다. `member`와 `post`가
주고받는 것은 오직 `shared`에 있는 이벤트/DTO/ApiClient뿐이다.

### 이벤트와 HTTP API를 구분한 이유

같은 "모듈 간 통신"이라도 요구되는 성질이 다르기 때문에 두 가지 통신 방식을 분리했다.

- **이벤트 (활동점수 반영, 회원 복제)** — "언젠가 결과적으로 상태가 맞춰지면 되는" 비동기적
  관심사. 글/댓글 작성 트랜잭션과 점수 반영 트랜잭션을 분리해야 "작성은 성공했는데 점수 반영은
  실패"하거나 "작성이 롤백됐는데 점수만 올라가는" 상황을 막을 수 있다. 그래서
  `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Transactional(propagation = REQUIRES_NEW)`
  조합을 쓴다 — 원본 트랜잭션이 커밋된 뒤에만, 별도 트랜잭션으로 반영된다.
- **HTTP API (보안 팁 조회)** — "글 작성 응답 그 자리에 동기적으로 포함되어야 하는" 요청-응답형
  관심사. 이벤트로 처리하면 응답을 만드는 시점에 아직 값이 없을 수 있어 맞지 않는다. 그래서
  `post` 모듈이 `MemberApiClient`로 `member` 모듈의 HTTP API를 직접, 동기적으로 호출한다.
  주소는 `member.api.base-url`(`application.yml`)로 외부에서 주입받는다 — 지금은 두 모듈이
  같은 프로세스(8080)에서 돌기 때문에 `localhost:8080`을 가리키지만, `member` 모듈이 실제로
  분리 배포되면 이 값만 그 서비스의 진짜 주소로 바꾸면 된다. `post` 모듈이 "내가 지금 몇 번
  포트에서 떠 있는지"를 스스로 알아내 호출하는 방식은 두 모듈이 항상 같은 프로세스라는 잘못된
  전제를 코드에 박아 넣는 것이라 채택하지 않았다.

### 회원 복제 흐름

1. `MemberJoinUseCase.join()` → `Member` 저장 → `MemberJoinedEvent(MemberDto)` 발행
2. `post` 모듈의 `PostEventListener`가 커밋 후 이벤트를 수신 → `PostFacade.syncMember()`가
   같은 ID로 `PostMember`를 저장(최초 1회는 insert)
3. 이후 글/댓글 작성으로 `Member.increaseActivityScore()`가 호출되면 `MemberModifiedEvent` 발행
   → 같은 흐름으로 `PostFacade.syncMember()`가 다시 호출됨
4. `PostMember`(`ReplicaMember`)의 `@Id`는 `@GeneratedValue` 없이 원본과 동일한 값을 그대로
   받아서 쓴다 → `save()` 시 이미 있는 id면 update, 없으면 insert로 동작해서 갱신할 때 행이
   추가되지 않는다.
5. 비밀번호/비밀번호 해시는 `MemberDto`에 담기지 않으므로 복제본에는 절대 복사되지 않는다.

## 3. 확인 결과

아래는 `./gradlew bootRun`으로 실행한 뒤 H2 파일(`db_dev.mv.db`)을 직접 조회하거나 API를
호출해서 얻은 실제 결과다.

### 3-1. 초기 데이터 개수

```sql
SELECT (SELECT count(*) FROM member_member) AS member_count,
       (SELECT count(*) FROM post_member) AS post_member_count,
       (SELECT count(*) FROM post_post) AS post_count,
       (SELECT count(*) FROM post_post_comment) AS comment_count;
-- 6 | 6 | 6 | 8
```

회원 6명(system, holding, admin, user1, user2, user3), 글 6개, 댓글 8개 — 요구사항과 일치.

### 3-2. 회원별 글/댓글 수와 활동점수

```sql
-- 회원별 글 수
author_id | post_count
        4 |          3   -- user1
        5 |          2   -- user2
        6 |          1   -- user3

-- 회원별 댓글 수
author_id | comment_count
        4 |             2   -- user1
        5 |             3   -- user2
        6 |             3   -- user3

-- member_member 활동점수
id | username | activity_score
 1 | system   | 0
 2 | holding  | 0
 3 | admin    | 0
 4 | user1    | 11   -- 글 3×3 + 댓글 2×1 = 11
 5 | user2    | 9    -- 글 2×3 + 댓글 3×1 = 9
 6 | user3    | 6    -- 글 1×3 + 댓글 3×1 = 6
```

글 3점 / 댓글 1점 가중치가 실제 실행 결과와 정확히 일치한다.

### 3-3. 원본(Member)과 복제본(PostMember) 일치

```sql
SELECT id, username, nickname, activity_score FROM member_member ORDER BY id;
-- 1 system  시스템  0
-- 2 holding 홀딩    0
-- 3 admin   관리자  0
-- 4 user1   유저1   11
-- 5 user2   유저2   9
-- 6 user3   유저3   6

SELECT id, username, nickname, activity_score FROM post_member ORDER BY id;
-- 1 system  시스템  0
-- 2 holding 홀딩    0
-- 3 admin   관리자  0
-- 4 user1   유저1   11
-- 5 user2   유저2   9
-- 6 user3   유저3   6
```

두 테이블의 id/username/nickname/activity_score가 완전히 일치한다.

### 3-4. 재실행 시 중복 없음

같은 DB 파일로 `./gradlew bootRun`을 다시 실행한 로그:

```
insert into member_member ... : 0건
insert into post_post ...     : 0건
insert into post_post_comment : 0건
```

(`MemberDataInit.makeBaseMembers()`, `PostDataInit.makeBasePosts()`가 `count() > 0`이면
바로 return, `makeBasePostComments()`는 `post1.hasComments()`면 바로 return하기 때문)

재실행 후 다시 조회한 개수와 활동점수:

```sql
-- 6 | 6 | 6 | 8   (재실행 전과 동일)
-- user1 11 / user2 9 / user3 6  (재실행 전과 동일 — 재증가 없음)
```

### 3-5. 보안 팁 HTTP API 호출 결과

요청:

```
GET http://localhost:8080/api/v1/member/members/randomSecureTip
```

응답:

```
HTTP/1.1 200
Content-Type: text/plain;charset=UTF-8

비밀번호의 유효기간은 90일 입니다.
```

`post` 모듈이 글 작성 시 같은 API를 내부적으로 호출한 로그(초기 데이터 생성 중 캡처):

```
c.b.boundedContext.post.in.PostDataInit : 1번 글이 생성되었습니다. 보안 팁 : 비밀번호의 유효기간은 90일 입니다.
c.b.boundedContext.post.in.PostDataInit : 2번 글이 생성되었습니다. 보안 팁 : 비밀번호의 유효기간은 90일 입니다.
... (6번까지 동일)
```

### 3-6. 자동화된 테스트 실행 결과

```bash
./gradlew test
```

단위 테스트 31개 + 통합 테스트 5개, 총 36개 전부 통과(클린 Gradle 데몬 기준):

| 테스트 클래스 | 개수 |
|---|---|
| `BackApplicationTests` | 1 |
| `MemberFacadeTest`, `MemberPolicyTest`, `MemberTest` | 5 + 7 + 3 |
| `ApiV1MemberControllerTest` | 1 |
| `PostFacadeTest`, `PostWriteUseCaseTest`, `PostTest` | 9 + 2 + 3 |
| `ActivityScoreIntegrationTest` | 3 |
| `PostMemberSyncIntegrationTest` | 2 |

**`ActivityScoreIntegrationTest`**(`src/test/java/com/back/integration`)가 체크리스트 #13(필수)을
검증하는 핵심 테스트다. `@SpringBootTest(webEnvironment = DEFINED_PORT)`로 실제 내장 서버를 띄우고
다음을 검증한다:

- 글을 작성하면 작성자 활동점수가 3점 증가한다.
- 댓글을 작성하면(`PostFacade.writeComment()`) 댓글 작성자 활동점수가 1점 증가한다.
- `TransactionTemplate` + `status.setRollbackOnly()`로 글 작성 트랜잭션을 강제 롤백시키면, 글도
  저장되지 않고 작성자 활동점수도 증가하지 않는다.

`PostWriteUseCase`가 `MemberApiClient`로 회원 서비스(`member.api.base-url`)를 실제 HTTP 호출하므로,
테스트도 그 주소가 가리키는 포트에 실제 서버가 떠 있어야 한다. 그런데 `8080`을 그대로 쓰면 `dev`
서버나 다른 프로세스가 이미 그 포트를 쓰고 있을 때 테스트가 깨진다. 그래서 `@DynamicPropertySource`로
컨텍스트가 뜨기 *전에* `ServerSocket(0)`으로 OS가 실제로 비어있는 포트를 하나 찾고,
`server.port`와 `member.api.base-url`을 동시에 그 값으로 맞춘다 — 8080이 이미 점유돼 있어도
테스트는 항상 자기만의 빈 포트를 쓴다(더미 서버로 8080을 점유시킨 채 `--rerun-tasks`로 재실행해도
통과하는 것으로 확인).

이 테스트는 `dev` 프로필의 파일 DB(`db_dev.mv.db`)를 그대로 사용하므로(별도 테스트 프로필 없음),
매 실행마다 UUID가 섞인 회원명으로 새 회원을 만들어 기존 시드 데이터와 충돌하지 않게 했다 — 다만
테스트로 생성된 회원/글 데이터가 파일 DB에 누적되는 점은 감안해야 한다.

**댓글 작성 경로에 쓰인 `PostFacade.writeComment(postId, author, content)`**는 이 테스트를 작성하며
추가했다. 기존에는 댓글 작성 경로를 Facade가 제공하지 않아서, 댓글을 쓰려면 `findById()`로 가져온
`Post`에 직접 `addComment()`를 호출해야 했다. `PostDataInit`은 여전히 이 방식을 쓴다 —
`Facade.findById()`로 애그리거트를 가져와 도메인 메서드를 호출하고 같은 트랜잭션 안에서 cascade로
영속화하는 것은 리치 도메인 모델의 정상적인 패턴이라 그대로 두었다. 다만 테스트 코드에서 매번 그
트랜잭션 경계를 `TransactionTemplate`으로 직접 만들어주는 건 번거로워서, 같은 일을 하는 전용 Facade
메서드를 하나 추가했다(`PostFacadeTest`에 단위 테스트 포함).

이 테스트가 실제로 잡아내는 버그 클래스를 확인하기 위해, `MemberEventListener`의
`@TransactionalEventListener(phase = AFTER_COMMIT)`을 일부러 평범한 `@EventListener`로 바꿔본 적이
있다. 그러면 "글 작성 +3점" 테스트는 여전히 통과하지만(정상 케이스만 봐서는 회귀를 못 잡음), 롤백
테스트는 `expected: 0 but was: 3`으로 실패한다 — 글 작성이 롤백됐는데도 이벤트가 즉시·별도
트랜잭션으로 실행돼 점수가 올라가버리기 때문이다. 즉 이 테스트는 "Spring이 롤백을 하는가"(프레임워크
책임)가 아니라 "우리 이벤트 리스너 배선이 롤백을 존중하는가"(애플리케이션 책임)를 검증한다.

**`PostMemberSyncIntegrationTest`**는 선택(가산점) 항목 #14를 검증한다:

- 회원 가입 시 같은 ID의 `PostMember`가 생성되고, id/username/nickname/activityScore/createDate/
  modifyDate가 원본과 일치하며, 비밀번호는 복사되지 않는다(`replica.getPassword()`가 비어 있음).
- 활동점수가 바뀌면(글 작성 → +3점) 기존 `PostMember` 행이 갱신되고, `postMemberRepository.count()`가
  변경 전후로 동일하다(행이 추가되지 않음을 직접 카운트로 증명).

이 테스트를 작성하며 실제 동기화 버그 하나를 발견했다: 활동점수 변경 후 `PostMember.modifyDate`가
원본 `Member.modifyDate`와 어긋난다. 원인은 `Member.increaseActivityScore()`가
`setActivityScore()` 직후 `new MemberDto(this)`로 DTO 스냅샷을 즉시 떠서 이벤트를 발행하는데,
`@LastModifiedDate`는 UPDATE가 실제로 flush되는 시점(트랜잭션 끝)에야 갱신되기 때문이다. 즉 이벤트에
담기는 `modifyDate`는 "이번 변경으로 갱신될 새 값"이 아니라 "이번 변경 직전의 값"이다(가입 시
`PostMember` 최초 생성은 `@GeneratedValue(IDENTITY)`라 `save()` 즉시 INSERT가 나가 auditing이
바로 적용되므로 문제없다 — UPDATE 경로에서만 발생). 근본 수정은 `Member.increaseActivityScore()`가
이벤트를 발행하는 시점을 바꿔야 해서 `Post.addComment()`와 동일하게 쓰이는 "도메인 메서드 안에서
즉시 publishEvent" 패턴 전반에 영향을 준다. 영향 범위 대비 실익을 고려해, 이번에는 프로덕션 코드를
고치는 대신 테스트에서 `modifyDate`는 비교 대상에서 제외했다 — `activityScore`/행 개수처럼 실제로
의도한 동기화 대상은 정상 검증된다.

## 4. 체크리스트 대비 구현 현황

아래는 과제 체크리스트 항목을 그대로 옮겨 하나씩 대응시킨 표다. "필수" 13개와 "선택(가산점)" 1개
(#14, PostMember 동기화 통합 테스트) 모두 구현·확인했다.

| # | 체크리스트 항목 | 구분 | 상태 | 근거 |
|---|---|---|---|---|
| 0 | 소스에 문법 오류가 없다 | 기본 | ✅ | `./gradlew compileJava compileTestJava` 통과 |
| 1 | Spring Boot + Spring Data JPA 사용, README에 실행 방법/모듈 구조 설명/확인 결과 존재 | 필수 | ✅ | 본 문서 1~3절 |
| 2 | 회원 6명·글 6개·댓글 8개를 실제 기능 호출로 생성, 재실행해도 데이터·활동점수 중복 증가 없음 | 필수 | ✅ | 3-1, 3-4절 |
| 3 | member/post를 in·app·domain·out으로 구성, 입력은 Facade 경유, 가입·글 작성은 UseCase로 분리 | 필수 | ✅ | 2절 모듈 구성, `MemberJoinUseCase`/`PostWriteUseCase` |
| 4 | 댓글을 글의 도메인 메서드로 생성, 별도 저장 호출 없이 연관관계로 저장 | 필수 | ✅ | `Post.addComment()` + `@OneToMany(cascade = PERSIST)` |
| 5 | 글/댓글 작성 시 DTO를 담은 공유 이벤트 발행, 회원 모듈이 받아 +3점/+1점 반영 | 필수 | ✅ | `PostCreatedEvent`/`PostCommentCreatedEvent` → `MemberEventListener` |
| 6 | 작성 트랜잭션 커밋 후 별도 트랜잭션으로 점수 반영, 작성 롤백 시 점수 미증가 | 필수 | ✅ | `@TransactionalEventListener(AFTER_COMMIT)` + `REQUIRES_NEW`, `ActivityScoreIntegrationTest` |
| 7 | 글 작성 시 ApiClient로 보안 팁 API 실제 HTTP 호출, 90일 값은 MemberPolicy에서 사용 | 필수 | ✅ | `MemberApiClient`, `MemberPolicy.PASSWORD_CHANGE_DAYS` |
| 8 | 가입/글 작성 성공 결과를 RsData(결과코드·메시지·데이터)로 반환, 중복 username은 409-1 DomainException | 필수 | ✅ | `MemberJoinUseCase.join()`, `PostWriteUseCase.write()` |
| 9 | 가입 이벤트로 같은 ID의 PostMember 생성, 원본 정보·시각 복제하되 비밀번호/해시는 미복사 | 필수 | ✅ | `PostFacade.syncMember()`, 3-3절, `PostMemberSyncIntegrationTest` |
| 10 | 활동점수 변경 이벤트로 기존 PostMember 갱신, 처리 후 원본·복제본 일치 & 행 추가 없음 | 필수 | ✅ | `MemberModifiedEvent` → `syncMember()`(merge), 3-3절, `PostMemberSyncIntegrationTest` |
| 11 | 글/댓글 작성자는 PostMember 참조, member/post는 상대 모듈 내부 클래스·Repository·원본 테이블에 직접 의존하지 않음 | 필수 | ✅ | `Post.author: PostMember`, `shared`의 이벤트/DTO/ApiClient로만 통신 |
| 12 | 회원 초기화와 글 초기화를 모듈별로 분리, 글 초기화는 자기 모듈 Facade로 복제본 조회 후 글·댓글 생성 | 필수 | ✅ | `MemberDataInit`(`@Order(1)`) → `PostDataInit`(`@Order(2)`) |
| 13 | 통합 테스트로 글 작성 +3점, 댓글 작성 +1점, 작성 롤백 시 점수 미증가 검증 | 필수 | ✅ | `ActivityScoreIntegrationTest` (3-6절) |
| 14 | 통합 테스트로 가입·활동점수 변경에 따른 PostMember 동기화 검증 | 선택 | ✅ | `PostMemberSyncIntegrationTest` (3-6절) |

#13(글/댓글 작성 활동점수 + 롤백 미증가)과 #14(PostMember 동기화)의 자동화된 테스트 증거는
3-6절에 있다. 체크리스트 필수/선택 항목 모두 구현·확인을 마쳤다.
