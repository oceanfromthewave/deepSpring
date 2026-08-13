# DeepSpring 작업 기록

## 2026-08-11 - 프로젝트 환경 확인 및 학습 Roadmap 수립

### Input

CLAUDE.md 파일 확인 후 진행

### Output

**환경 확인 결과**

- 프로젝트 디렉터리: `C:\dev\DeepSpring` (현재 `.idea/`, `CLAUDE.md` 만 존재)
- PATH `java`: JDK 18.0.2.1 (non-LTS)
- `JAVA_HOME`: `C:\Users\김재현\.jdks\corretto-21.0.11` (JDK 21 LTS) — 실제 사용할 JDK
- 설치된 JDK: corretto-1.8.0_482, corretto-21.0.11, ms-21.0.11, jdk-18.0.2.1, jdk1.7_80
- Gradle CLI: 없음 / Maven CLI: 없음 (IntelliJ 내장 또는 Wrapper 사용 예정)
- Git: 저장소 아님 (`git init` 미실행)
- Spring Boot: 미도입 (Phase 1은 Plain Java로 진행)

**결정 사항**

- Phase 1(Plain Java)은 빌드 도구 없이 `javac`/`java` 직접 실행으로 진행
- Gradle은 Spring Core 도입 시점(Phase 2 후반~3)에 Wrapper로 추가
- 학습 Roadmap 및 Phase 1 목표 제시, 사용자 진행 확인 대기

## 2026-08-11 17:30 - Phase 2~3 (IoC/DI, BeanDefinition, Bean Lifecycle) 완료 + Phase 4 Gradle/Spring Core 착수

### Input

"다음 진행" 반복 요청으로 Phase 2~4 이어서 진행. 중간중간 "너가 작성해줘"/"수정해줘"로 명시적 요청 시 Claude가 직접 코드 작성. "Gradle 세팅부터 시작하자" 요청으로 Phase 4 착수.

### Output

**Phase 2~3: 손수 구현한 IoC Container (`SimpleApplicationContext`)**

- `container/BeanDefinition.java`, `ComponentScanner.java`, `Component.java`: BeanDefinition 등록 + `@Component` 클래스패스 스캔
- `SimpleApplicationContext`: 생성자 리플렉션 기반 DI, 싱글톤 캐시, 순환 의존성 → `StackOverflowError` 재현 확인
- 타입 모호성(Multiple Candidates) 문제 발견 → 해결 우선순위 체인 구현: **타입 매칭 → `@Qualifier`(못 찾으면 즉시 예외) → `@Primary` → 그래도 모호하면 예외**. `container/Primary.java`, `Qualifier.java` 추가
- `@PostConstruct` 초기화 콜백 구현 → 이후 `BeanPostProcessor` 인터페이스로 일반화 (`beforeInitialization`/`afterInitialization` 훅, 여러 processor 등록/순서 실행 확인). `container/PostConstruct.java`, `BeanPostProcessor.java` 추가
- 검증용 실험 파일: `AmbiguousBeanExperiment`, `QualifierCheck`, `MultiProcessorCheck` (test 소스, Claude가 직접 작성)
- 전 과정 `javac`/`java` 직접 컴파일·실행으로 회귀 검증 반복

**Phase 4 착수: Gradle 세팅 + 진짜 Spring Core 첫 실험**

- 로컬 `.gradle` 캐시(IntelliJ가 받아둔 Gradle 8.13)로 `gradle wrapper` 생성 → `gradlew`/`gradlew.bat`/`gradle/wrapper/*`
- `build.gradle`: Java 21, `org.springframework:spring-context:6.2.1` 단일 의존성 (Spring Boot 아직 미도입, Spring Core만)
- `settings.gradle`: `rootProject.name = 'deepspring'`
- `./gradlew build` 성공, 기존 손수 짠 소스 전부 무수정 컴파일 확인
- `config/SpringAppConfig.java`: 진짜 `@Configuration`/`@Bean`으로 기존 도메인 POJO(`MemberRepository`, `OrderService` 등) 재사용 조립
- `test/config/RealSpringExperiment.java`(Claude 작성): `AnnotationConfigApplicationContext` 로 실행 → `internalConfigurationAnnotationProcessor`/`internalAutowiredAnnotationProcessor` 등 실제 `BeanPostProcessor` 기반 인프라 빈 확인, `@Bean` 메서드 싱글톤 캐싱 확인, `SpringAppConfig$$SpringCGLIB$$0` 클래스명으로 CGLIB Proxy 직접 확인
- `build.gradle`에 `printTestClasspath` task 추가 (gradle 없이 `java -cp`로 실험 클래스 직접 실행할 때 재사용)

**다음 단계**: `@ComponentScan`을 손수 짠 `ComponentScanner`와 비교하는 실험 예정

## 2026-08-11 - @ComponentScan 실험: 실제 Spring이 도메인 클래스를 못 찾는 이유

### Input
"진행" (Phase 4 다음 단계 진행 요청)

### Output
- `config/SpringAppConfig.java`에 `@ComponentScan(basePackages = "com.deepspring")` 1줄 추가 (Claude 편집)
- `RealSpringExperiment` 재실행 결과: `getBeanDefinitionNames()`에 `memberRepository`/`discountPolicy`/`orderService`(전부 `@Bean` 메서드 산출물)만 등록됨. `com.deepspring.member`, `order` 패키지의 도메인 클래스들은 **하나도 스캔되지 않음**
- 원인: 도메인 클래스들이 달고 있는 `@Component`는 Phase 2~3에서 손수 만든 `com.deepspring.container.Component` 어노테이션 — 실제 Spring의 `org.springframework.stereotype.Component`가 아니므로 Spring `@ComponentScan`이 인식 못 함
- 사용자에게 이 결과 해석을 직접 설명하도록 질문 던짐 (다음 턴 대기)

## 2026-08-11 (계속) - 도메인 클래스 실제 Spring 전환 + Full/Lite Configuration

### Input
"코드나 치자" → 파일별 import 교체 지시 따라 사용자가 직접 타이핑 → "1번 가자"(수동 @Bean 삭제) → "너 추천대로 진행하겠음"

### Output
- `MemoryMemberRepository`/`FixDiscountPolicy`/`RateDiscountPolicy`/`OrderService`: 커스텀 `@Component`/`@Qualifier`/`@Primary`/`@PostConstruct` → 진짜 Spring/JSR-250 어노테이션으로 교체 (사용자 직접 타이핑)
- `build.gradle`: `jakarta.annotation:jakarta.annotation-api:2.1.1` 추가 (Claude, 설정 파일)
- 1차 실행에서 `NoUniqueBeanDefinitionException` 재현: `SpringAppConfig`의 수동 `@Bean memberRepository()`와 `@ComponentScan`이 찾은 `memoryMemberRepository`가 같은 타입으로 중복 등록됨
- `SpringAppConfig`에서 수동 `@Bean` 3개 삭제, `@ComponentScan`만 남김(사용자 타이핑) → 정상화, `internalCommonAnnotationProcessor` 자동 등록 확인 (`@PostConstruct` 처리 담당)
- `SpringAppConfig`가 `@Bean` 메서드 하나도 없는데 여전히 CGLIB Proxy인 점 발견 → Full Configuration(`proxyBeanMethods` 기본 true) 논의로 이어짐
- `test/config/ConfigurationProxyExperiment.java`(Claude 작성): Full vs Lite(`proxyBeanMethods=false`) 나란히 비교 — Full은 `beanA()` 직접 호출도 CGLIB가 가로채 싱글톤 리턴(`==` true), Lite는 매번 새 인스턴스(`==` false), 프록시 클래스명(`$$SpringCGLIB$$`) 유무로 직접 확인

**다음 단계**: Phase 5(Scope: singleton/prototype) 진행 예정

## 2026-08-11 (계속) - Phase 5: Singleton + Prototype 문제, ObjectProvider

### Input
"ㄱ" (Phase 5 진행) → "나한테 문제내지말고 진행해" (소크라테스식 질문 중단 요청)

### Output
- `test/container/ScopeExperiment.java`(Claude 작성): `@Scope("prototype")` PrototypeBean + 이를 생성자 주입받는 singleton `SingletonHolder`로 고전 문제 재현
  - 컨테이너 직접 `getBean(PrototypeBean.class)` 반복 호출 시 매번 다른 인스턴스(id 증가) 확인
  - `SingletonHolder`는 싱글톤(h1==h2 true)이지만, 그 안의 `PrototypeBean` 필드는 생성자 주입 시점(1회)에 고정된 채 안 바뀜 — "prototype dependency in singleton" 문제
  - 원인 설명: 생성자 주입은 Bean 생성 시 1회만 resolve. Singleton은 재생성 안 되니 필드도 고정
  - 해결: `ObjectProvider<PrototypeBean>` 필드로 바꾸고 사용 시점마다 `.getObject()` 호출 → 매번 새 인스턴스 확인 (`SingletonHolderWithProvider`)

### Note
사용자가 소크라테스식 질문(CLAUDE.md 기본 학습 방식) 대신 바로 진행/설명 원함 — 이번 세션은 질문 없이 결과+설명 바로 제공하는 방식으로 전환.

**다음 단계**: Phase 5 나머지(`@Primary`/`@Qualifier`/Collection Injection은 Phase 2~3서 이미 다룸 — 남은 건 `@Lazy`, Circular Dependency 재확인 정도) 또는 Phase 6(AOP/Proxy)로 바로 진행

## 2026-08-11 (계속) - Phase 6 착수: Cross-cutting Concern → Manual Wrapper → JDK Dynamic Proxy

### Input
Phase 6(AOP) 진행 지시, 순차적으로 파일 타이핑

### Output
- `order/OrderServiceInterface.java` 신설(사용자 작성), `OrderService implements OrderServiceInterface`로 변경(사용자 작성) — Proxy 만들려면 인터페이스 필요해서 선행 작업
- 문제 제시: `calculatePrice()`에 실행시간 로깅 직접 넣으면 메서드/클래스마다 중복(Cross-cutting Concern)
- `order/OrderServiceTimeLoggingWrapper.java`(사용자 작성, `src/main`에 위치): 수동 Decorator/Wrapper 패턴. `test/order/ManualWrapperExperiment.java`(Claude 작성)로 동작 확인 — 로깅 잘 찍히지만 클래스마다 손으로 다 써야 하는 한계 확인
- `order/TimeLoggingInvocationHandler.java`(사용자 작성, `src/main`에 위치): `InvocationHandler` 구현, 리플렉션(`Method.invoke`)으로 target 타입 몰라도 범용 로깅 가능
- `test/order/JdkDynamicProxyExperiment.java`(Claude 작성): `Proxy.newProxyInstance()`로 실제 프록시 생성 확인
  - `proxy.getClass()` = `jdk.proxy1.$Proxy0` (런타임 생성 클래스)
  - `proxy instanceof OrderServiceInterface` true / `instanceof OrderService` false → 인터페이스 기반이라 원본 클래스 계층과 무관하다는 점 확인
  - 로깅 정상 동작(`calculatePrice 실행시간 = 5ms`), `result = 9000` 정상

### Note
사용자 요청대로 소크라테스식 질문 없이 결과+설명 바로 진행 중.

**다음 단계**: CGLIB(상속 기반 Proxy, 인터페이스 없는 클래스 대상) 비교 → Advice/Pointcut/Advisor → 진짜 Spring AOP(`@Aspect`, `@Around`)로 연결

## 2026-08-11 (계속) - CGLIB 실험, JDK vs CGLIB 비교 정리

### Output
- `order/TimeLoggingMethodInterceptor.java`(사용자 작성): cglib `MethodInterceptor`, `proxy.invokeSuper()`로 원본 호출 (JDK 버전과 달리 target 필드 없음 — 자기 자신이 곧 원본)
- `test/order/CglibProxyExperiment.java`(Claude 작성): `Enhancer`로 `OrderService` 상속 기반 프록시 생성 확인
  - `proxy.getClass()` = `OrderService$$EnhancerByCGLIB$$...`
  - `instanceof OrderService` true (JDK 버전은 false였음) — 상속 기반이라 원본 타입 그대로 유지되는 게 핵심 차이
- 수동 Advisor/Pointcut 객체 모델 구현은 스킵 결정 (실무에서 `@Aspect` 방식이 대세, 레거시 XML AOP 아니면 안 씀)

**다음 단계**: 진짜 Spring AOP(`@Aspect`+`@Around`) 실험, `AopUtils.isJdkDynamicProxy`/`isCglibProxy`로 실제 어느 쪽 골랐는지 확인 → Self Invocation 문제로 연결

## 2026-08-11 (계속) - 진짜 Spring AOP (@Aspect/@Around) 실험

### Output
- `build.gradle`: `spring-aspects:6.2.1`, `aspectjweaver:1.9.22` 추가 (Claude)
- `test/order/SpringAopExperiment.java`(Claude 작성): `@Aspect`+`@Around("execution(...)")`로 지금까지 손으로 만든 로깅 재현, `@EnableAspectJAutoProxy`
- 1차 실행에서 `NoSuchBeanDefinitionException` 재현: `context.getBean(OrderService.class)`로 조회 시 실패 — `OrderService`가 인터페이스 있어서 Spring이 JDK Dynamic Proxy 자동 선택했는데, JDK Proxy는 `instanceof OrderService` false(CGLIB 실험서 확인한 그 성질)라 구체 타입으로 못 찾음. `getBean(OrderServiceInterface.class)`로 고쳐서 해결 — 방금 배운 개념이 실제 실무 함정으로 바로 재현된 케이스
- 최종 확인: `AopUtils.isAopProxy`=true, `isJdkDynamicProxy`=true, `isCglibProxy`=false, `joinPoint.proceed()` 호출 시 로깅 정상 동작

**다음 단계**: Self Invocation 문제(내부에서 `this.method()` 호출 시 AOP 안 먹는 이유) 실험

## 2026-08-11 (계속) - Self Invocation 실측, Phase 6(AOP) 마무리

### Output
- `order/OrderServiceInterface.java`/`OrderService.java`에 `outerCall()` 추가(사용자 작성) — 내부에서 `this.calculatePrice()` 호출(Self Invocation)
- `order/OrderServiceTimeLoggingWrapper.java`에 위임 메서드 추가(Claude, 인터페이스 변경 따라가는 기계적 수정)
- `SpringAopExperiment` 확장: `bean.outerCall()` 호출 시 `outerCall` 로그만 찍히고 내부 `calculatePrice` 호출은 로그 안 찍힘 → Self Invocation이 AOP 프록시 건너뛴다는 것 실측 확인
- `@Transactional`/`@Async`/`@Cacheable` 전부 같은 함정 겪는다는 점 언급, 해결책(자기 주입/`AopContext.currentProxy()`/메서드 분리)은 원리만 짚고 구현은 보류

**Phase 6(AOP) 완료.** 다음 단계: Phase 7 Transaction — 지금 배운 Proxy/AOP 메커니즘이 `@Transactional`에 그대로 적용됨. Manual Transaction Template(begin/commit/rollback 직접 구현)부터 시작 예정

## 2026-08-12 - Phase 7: Manual Transaction → 손수 만든 @Transactional (Proxy 기반)

### Input
"진행" (클리어 후 새 세션 시작) → Phase 7 순차 진행 → "너 추천대로 진행" (Proxy 자동화 방향 선택)

### Output
- `transaction/Account.java`(사용자 작성): 잔액 보유 POJO, rollback 실험 대상
- `transaction/AccountTransferService.java`(사용자 작성, 3단계 리팩터링):
  1차: 스냅샷/diff 방식 수동 rollback (`try/catch` 안에서 balance 차이 계산해 복원)
  2차: `FakeConnection`(undo log 방식)으로 교체, 도메인 지식 없는 범용 rollback으로 전환
  3차: `TransactionSynchronizationManager.getConnection()`으로 Connection 받아옴, Transaction 관련 코드 완전 제거
- `transaction/FakeConnection.java`(사용자 작성): `Deque<Runnable>` 기반 undo log. `execute(action, undo)`/`commit()`(로그 clear)/`rollback()`(역순 실행) — 실제 DB의 undo log 개념 축소 구현
- `transaction/TransactionCallback.java`/`TransactionTemplate.java`(사용자 작성): try/commit/catch/rollback 보일러플레이트를 템플릿 메서드 패턴으로 분리 (Spring `TransactionTemplate` 원형) — 이후 Proxy 방식으로 대체되며 미사용 상태로 남음
- `transaction/TransactionSynchronizationManager.java`(사용자 작성): `ThreadLocal<FakeConnection>` 기반 Connection 바인딩/조회/해제. 실제 Spring 클래스와 이름 동일
- `transaction/AccountTransferServiceInterface.java`(사용자 작성): JDK Dynamic Proxy용 인터페이스 (Phase 6 학습 재사용)
- `transaction/TransactionInvocationHandler.java`(사용자 작성): `InvocationHandler` — method.invoke 전 Connection 생성+바인딩, 성공 시 commit, `InvocationTargetException` 발생 시 rollback+원인 예외 rethrow, `finally`에서 항상 unbind (ThreadLocal 누수 방지, Tomcat Thread Pool 재사용 시나리오 설명)
- `test/transaction/ManualTransactionExperiment.java`(Claude 작성, 이후 단계에서 구조상 deprecated됨): 스냅샷 → undo log → TransactionTemplate 각 단계마다 재실행, 결과 동일 확인(성공 A=700/B=800, 실패 시 A=1000 rollback 성공)
- `test/transaction/ProxyTransactionExperiment.java`(Claude 작성): `Proxy.newProxyInstance()`로 `TransactionInvocationHandler` 적용
  - Proxy 없이 직접 호출 시 `IllegalStateException`("Connection 없음") 재현 — Connection 바인딩 안 됐으므로 예상된 실패
  - Proxy 경유 시 자동 commit/rollback 정상 동작 확인 (성공 A=700/B=800, 실패 시 A=1000 자동 rollback)
- `build.gradle`: `runExperiment`(JavaExec, `-PmainClass`로 지정) task 추가 — 매번 새 실험 클래스마다 gradle task 안 만들어도 되게

### Note
- `TransactionTemplate`/`TransactionCallback`은 3차 리팩터링 후 미사용 상태로 파일만 남아있음 (다음 세션서 정리 여부 결정 필요)
- 모델: Sonnet high 유지, Opus 전환은 Persistence Context/Security Filter Chain/Production Architecture 단계까지 보류하기로 합의

**다음 단계**: 진짜 Spring `@Transactional` + `PlatformTransactionManager` 붙여서 지금 만든 구조와 실제 Spring 내부 동작 비교 (Propagation/Isolation 실험으로 연결)

## 2026-08-12 (계속) - 진짜 Spring @Transactional 연결, 손수 만든 구조와 비교

### Input
"진행" (진짜 Spring @Transactional 실험 진행)

### Output
- `build.gradle`: `spring-jdbc:6.2.1`, `h2:2.3.232` 추가 (Claude, Phase 11 DB 내용 조금 당겨씀)
- `src/main/resources/schema.sql`(Claude): H2 임베디드용 DDL — `account` 테이블, `A/B`(성공 케이스용)·`C/broken`(실패 케이스용) 초기 데이터 분리 등록
- `transaction/SpringAccountTransferService.java`(사용자 작성): `@Transactional` + `JdbcTemplate` 기반 실제 이체 서비스. 1차 작성 시 버그 2개(`balance + ?` 대신 `balance - ?` 오타, 테이블명 `acctount` 오타) 발견 → 사용자 요청으로 Claude가 수정
- `transaction/TxConfig.java`(사용자 작성): `@Configuration` + `@EnableTransactionManagement`, `DataSource`(H2 임베디드)/`JdbcTemplate`/`PlatformTransactionManager`(`DataSourceTransactionManager`)/`SpringAccountTransferService` Bean 등록 (Full Configuration, `@Bean` 명시적 조립 방식 선택 — `@ComponentScan` 안 씀)
- `test/transaction/SpringTransactionExperiment.java`(Claude 작성): `AnnotationConfigApplicationContext(TxConfig.class)` 부팅 후 확인
  - `bean.getClass()` = `SpringAccountTransferService$$SpringCGLIB$$0`, `AopUtils.isCglibProxy`=true, `isJdkDynamicProxy`=false — 인터페이스 없어서 CGLIB 선택된 것 실측
  - 성공 케이스: A=700/B=800, 실패 케이스: C=1000(자동 rollback)/broken=500 — 손수 만든 버전과 결과 동일
  - 핵심 발견: 실패 케이스서 `fromName` 출금에 대한 undo 코드 전혀 없어도 DB Connection이 Transaction 내 모든 SQL 자동 취소함 — 손수 만든 `FakeConnection.execute(action, undo)` 방식(반대 동작 직접 등록)과 달리 진짜 DB는 Redo/Undo Log가 엔진 내부에 있어 개발자가 되돌릴 동작 등록할 필요 없음

### Note
직접 구현 ↔ 진짜 Spring 매핑 완성:
```
FakeConnection                      ↔ java.sql.Connection (H2)
TransactionSynchronizationManager   ↔ 동일 이름의 진짜 Spring 클래스
TransactionInvocationHandler        ↔ TransactionInterceptor (CGLIB Proxy로 적용)
@EnableTransactionManagement 없으면 @Transactional 무효 ↔ @EnableAspectJAutoProxy 패턴과 동일
```

**다음 단계**: Propagation(REQUIRED vs REQUIRES_NEW) 실험으로 Phase 7 마무리 또는 바로 Phase 8(Spring MVC)로 이동 — 사용자 선택 대기

## 2026-08-12 (계속) - Propagation 실험 (REQUIRED vs REQUIRES_NEW), Phase 7 완료

### Input
"1번으로 진행" (Propagation 실험 선택)

### Output
- `src/main/resources/schema.sql`(Claude): `D/E`(REQUIRED 테스트용), `F/G`(REQUIRES_NEW 테스트용) 초기 데이터 1000씩 추가
- `transaction/PropagationServiceB.java`(사용자 작성): `doWorkRequired()`(`Propagation.REQUIRED`)/`doWorkRequiresNew()`(`Propagation.REQUIRES_NEW`) — 둘 다 SQL UPDATE 후 무조건 `RuntimeException` 던짐
- `transaction/PropagationServiceA.java`(사용자 작성): 자기 계좌 먼저 갱신 후 `serviceB` 호출(Proxy 경유, 별개 Bean이라 Self Invocation 문제 없음), `try/catch`로 B의 예외 삼킴 — REQUIRED에서도 "예외 잡으면 안전하다"는 직관이 깨지는지 확인하려는 설계
- `transaction/TxConfig.java`: `PropagationServiceA`/`PropagationServiceB` `@Bean` 등록 추가 (Claude, 단순 배선)
- `test/transaction/PropagationExperiment.java`(Claude 작성): 실행 결과
  - `REQUIRED`: `A`가 `catch`로 예외 삼켰음에도 `UnexpectedRollbackException` 발생(커밋 시점에 rollbackOnly 마킹 감지) → D=1000/E=1000 전체 롤백
  - `REQUIRES_NEW`: B의 실패는 B만의 독립 Transaction 롤백(G=1000), A는 정상 커밋(F=1100)
  - 두 케이스 모두 예측과 100% 일치

### Note
직접 만든 구조(Phase 7 전반부)와 실제 Spring Propagation의 관계: `REQUIRED`는 우리가 만든 `TransactionSynchronizationManager`가 이미 바인딩된 Connection을 그대로 재사용하는 것과 동일한 개념. `REQUIRES_NEW`는 기존 바인딩을 suspend하고 새 Connection을 bind하는 것 — 우리 버전엔 없던 개념(항상 새 `FakeConnection` 하나만 다뤘음).

**Phase 7(Transaction) 완료.** 다음 단계: Phase 8 Spring MVC — Servlet/Tomcat 기반 아키텍처 이해부터 시작 (DispatcherServlet 이전에 순수 Servlet으로 HTTP 요청 처리 직접 구현)

## 2026-08-12 (계속) - 기록 누락분 정리 (Phase 8~13 착수분 소급 기록)

### Note
지난 세션서 `/clear` 이후 record.md 갱신 없이 Phase 8~12 완료 + Phase 13 착수까지 진행됨. 파일 mtime 기준 재구성:
- Phase 8 (Servlet/MVC): `servlet/*`(HelloServlet, FrontServlet, GetMapping, RequestParam, LoggingFilter 등 순수 Servlet 라우팅 직접 구현) → `springmvc/*`(WebConfig, LoggingInterceptor, GreetController)로 진짜 Spring MVC 연결
- Phase 9 (Boot): `boot/ConditionalOnClassDemo`, `ConditionalOnMissingBeanDemo`, `AppProps`, `MinimalBootApp` — Auto Configuration/Conditional 실험
- Phase 10 (Validation/Exception): `validation/*` — `@Valid`, Custom Constraint, `@RestControllerAdvice` 계열
- Phase 11 (JDBC): `jdbc/JdbcMemberRepository`(직접 Connection/PreparedStatement) → `SpringJdbcMemberRepository`(JdbcTemplate), `ConnectionPoolExperiment`(HikariCP)
- Phase 12 (JPA/Hibernate): `jpa/MemberEntity`, `OrderEntity` + `JpaBasicExperiment`, `DirtyCheckingExperiment`, `DetachedStateExperiment`, `LazyLoadingExperiment`, `NPlusOneExperiment`, `FetchJoinExperiment` — Persistence Context/Dirty Checking/N+1/Fetch Join 전부 실측 완료
- Phase 13 착수: `build.gradle`에 `spring-boot-starter-data-jpa` 추가, `jpa/MemberJpaRepository`(`JpaRepository<MemberEntity,Long>` 상속 인터페이스만 선언) + `test/jpa/SpringDataJpaExperiment.java`(`@SpringBootApplication`+`@EnableJpaRepositories`로 부팅, `save/findById/findAll` 호출 + Proxy 클래스 확인 코드) 작성됨 — **실행 여부 미확인**

**다음 액션**: `SpringDataJpaExperiment` 실행 결과 확인부터 재개

## 2026-08-12 (계속) - Phase 13: Query Method, PropertyReferenceException, @Query

### Input
Phase13 진행 → Query Method 3종 추가 → 오타로 기동 실패 재현 → @Query + @Param 추가

### Output
- `jpa/MemberJpaRepository.java`(사용자 작성): `JpaRepository<MemberEntity,Long>` 상속에 Query Method 3개 + `@Query` 1개 추가
  - `findByGrade`/`findByNameContaining`/`findByGradeOrderByNameAsc`: 메서드 이름 파싱 기반, 각각 `where grade=?`/`like ? escape`/`order by name` JPQL 자동 생성 확인
  - 오타 실험: `findByGrde(String)` 추가 후 기동 → `BeanCreationException` → `QueryCreationException` → `PropertyReferenceException: No property 'grde' found ... Did you mean 'grade'` — Bean 생성 시점(런타임 첫 호출 아님)에 이름 파싱/검증되는 fail-fast 특성 확인, 이후 삭제
  - `findByGradeAndNameKeyword(@Param grade, @Param keyword)`: `@Query("... like %:keyword%")` JPQL 직접 작성, Spring Data JPA 전용 확장 문법(`%:param%`) 사용
- `test/jpa/SpringDataJpaExperiment.java`(Claude 작성, 계속 누적): save 3건(kim/lee VIP, park NORMAL) + 4개 Query Method 호출부 추가, 전부 기대한 SQL/결과와 일치 확인

**다음 단계**: Native Query(`nativeQuery=true`) 또는 Pagination/Sorting(`Pageable`)로 진행 예정 — 사용자 선택 대기

## 2026-08-12 (계속) - Phase 13: Pagination(Pageable/Page)

### Output
- `jpa/MemberJpaRepository.java`(사용자 작성): `Page<MemberEntity> findByGrade(String grade, Pageable pageable)` 오버로딩 추가 (기존 `List` 버전과 공존)
- `test/jpa/SpringDataJpaExperiment.java`(Claude): `PageRequest.of(page, size, Sort.by("name"))`로 page0/page1 각각 호출
  - page0(size=1): SQL `... order by name fetch first ? rows only` + 별도 count 쿼리 자동 실행 → content=[kim], totalElements=2, totalPages=2
  - page1(size=1): SQL `... offset ? rows fetch first ? rows only` + count 쿼리 → content=[lee]
  - `Pageable` 파라미터는 메서드 이름 파싱 대상에서 제외되고 페이징 전용 처리된다는 것, `Page<T>`가 본 쿼리+count 쿼리 합쳐서 매 호출 2번 SQL 나간다는 것 확인
  - H2는 `LIMIT/OFFSET` 대신 SQL 표준 `fetch first ... rows only` 방언 사용하는 것도 확인 (Oracle 경험 있는 사용자에게 `ROWNUM`과 다른 부분으로 짚어줌)

**다음 단계**: Native Query 또는 Specification, 혹은 Phase 13 마무리하고 Phase 14(Security)로 이동 — 사용자 선택 대기

## 2026-08-12 (계속) - Phase 13 완료, Phase 14 전환

### Input
"Phase 13 마무리하고 Phase 14로 넘어가자"

### Output
Phase 13(Spring Data JPA) 종료. 다룬 것: Repository 인터페이스 하나로 JDK Dynamic Proxy 자동 생성, Query Method(이름 파싱 → JPQL, Bean 생성 시점 fail-fast 검증), `@Query`+`@Param`(JPQL 직접 작성), `Pageable`/`Page`(자동 count 쿼리, DB 방언별 LIMIT/OFFSET 번역). Native Query/Specification은 스킵(실무에서 QueryDSL 등으로 대체되는 경우 많아 우선순위 낮음, 필요시 나중에 추가).

**다음 단계**: Phase 14 Spring Security 시작

## 2026-08-12 (계속) - Phase 14 착수: Manual Auth Filter (Plain Java 문제 재현)

### Output
- `security/ManualAuthFilter.java`(사용자 작성): `jakarta.servlet.Filter` 구현, `Authorization` 헤더 하드코딩 토큰 비교 후 불일치 시 `401` 응답
  - 1차 버그: `import java.util.logging.Filter`(엉뚱한 인터페이스) → `jakarta.servlet.Filter`로 수정
- `security/SecurityFilterConfig.java`(사용자 작성): `@Configuration`+`@Bean FilterRegistrationBean<ManualAuthFilter>`, `/hello`에만 적용
- `boot/MinimalBootApp.java`: `@ComponentScan`에 `com.deepspring.security` 패키지 추가 (Claude, 사용자 요청으로 대신 수정)
- 실행 확인(`curl`, port 18080):
  - 토큰 없음 → 401
  - 틀린 토큰 → 401
  - 올바른 토큰(`Bearer secret-token`) → 200, `HelloBootController` 응답 정상
  - (중간에 Claude가 옛 오타 토큰으로 테스트해서 401 뜬 걸 버그로 오인 → 재확인 결과 사용자가 이미 오타 고쳐놨던 것, Claude 실수로 판명)

**다음 단계**: 진짜 Spring Security 의존성 추가, `SecurityFilterChain`으로 지금 만든 수동 Filter를 대체 — Authentication/Authorization 개념 분리부터 연결

## 2026-08-12 (계속) - Phase 14: 진짜 Spring Security, SecurityFilterChain

### Output
- `build.gradle`: `spring-boot-starter-security` 추가 (Claude)
- 의존성만 추가한 상태로 먼저 실행 → Auto Configuration 기본 동작 관찰
  - 모든 경로 기본 인증 요구, `user`/랜덤 UUID 비밀번호 로그 출력(`Using generated security password: ...`)
  - `ManualAuthFilter`(FilterRegistrationBean, 낮은 우선순위)와 Spring Security Filter(높은 우선순위, `SecurityProperties.DEFAULT_FILTER_ORDER`)가 동시에 걸려있어서 직렬 통과 필요하다는 것 3가지 curl 케이스로 실측
    - 헤더 없음/틀린 Bearer 토큰 → Spring Security가 먼저 막음(body 없음, WWW-Authenticate)
    - 올바른 Basic 인증(생성된 비밀번호) → Spring Security 통과했지만 그 다음 ManualAuthFilter가 다시 막음(body 있음, /error JSON) — 두 Filter 다 통과해야 최종 응답
- `security/SecurityConfig.java`(사용자 작성): `@Bean SecurityFilterChain` — `anyRequest().authenticated()` + `httpBasic()`, Boot 기본 SecurityFilterChain을 `@ConditionalOnMissingBean`으로 대체
- `security/ManualAuthFilter.java`, `security/SecurityFilterConfig.java` 삭제 (Claude, 사용자 동의) — SecurityConfig가 완전히 대체하는 중복 코드라 정리
- 재실행 확인: 헤더 없음 401, 올바른 Basic 인증(생성된 비밀번호) 200 — 단일 Filter Chain으로 깔끔하게 동작

**다음 단계**: 매번 랜덤 비밀번호 대신 `UserDetailsService`+`PasswordEncoder`로 실제 사용자 저장/인증 구현, 또는 `AuthenticationManager`/`AuthenticationProvider` 내부 흐름으로 연결 — 사용자 선택 대기

## 2026-08-12 (계속) - Phase 14: UserDetailsService + PasswordEncoder

### Output
- `security/SecurityConfig.java`(사용자 작성, 기존 파일에 Bean 2개 추가):
  - `passwordEncoder()`: `BCryptPasswordEncoder`
  - `userDetailsService(PasswordEncoder)`: `InMemoryUserDetailsManager`에 kim(USER)/admin(ADMIN) 2명, 비밀번호는 encoder로 해시 저장
- 실행 확인:
  - 로그에서 "Using generated security password" 사라짐 — `@ConditionalOnMissingBean(UserDetailsService.class)` 밀려남 확인
  - `kim:1234`/`admin:admin1234` → 200, `kim:wrong` → 401
- Runtime 흐름 정리: httpBasic Filter → AuthenticationManager → DaoAuthenticationProvider(자동 조립, 우리가 직접 안 만듦) → UserDetailsService.loadUserByUsername → PasswordEncoder.matches

**다음 단계**: AuthenticationManager/AuthenticationProvider 내부 구조, 또는 Role 기반 인가(`hasRole("ADMIN")`)로 확장 — 사용자 선택 대기

## 2026-08-12 (계속) - Phase 14: Role 기반 인가(hasRole), 401 vs 403

### Input
"작성해줘" — 사용자 명시 요청으로 Claude가 직접 작성

### Output
- `security/SecurityConfig.java`(Claude, 사용자 명시 요청): `authorizeHttpRequests`에 `.requestMatchers("/admin/**").hasRole("ADMIN")`를 `anyRequest().authenticated()`보다 먼저 추가 — 순서가 매치 우선순위라는 점 강조
- `boot/HelloBootController.java`(Claude, 사용자 명시 요청): `/admin/hello` 엔드포인트 추가
- 실행 확인: kim(ROLE_USER)→`/hello` 200, kim→`/admin/hello` 403(Forbidden), admin(ROLE_ADMIN)→`/admin/hello` 200
- 401(인증 실패) vs 403(인가 실패/권한 부족) 차이 실측 완료

**다음 단계**: AuthenticationManager/AuthenticationProvider 내부 흐름, 또는 JWT로 이동(JWT는 Opus 전환 체크포인트로 합의됨) — 사용자 선택 대기

## 2026-08-12 (계속) - Phase 14: JWT 직접 구현 (HMAC 서명/검증)

### Output
- `security/jwt/SimpleJwt.java`(사용자 작성): `javax.crypto.Mac`(HmacSHA256) + `Base64.getUrlEncoder()`만으로 JWT 발급/검증 직접 구현, 외부 라이브러리(jjwt 등) 없음
  - 파일 위치/이름 오류 있어서 Claude가 이동: `security/SimpleSwt.java` → `security/jwt/SimpleJwt.java` (package 선언은 `.jwt`인데 폴더 불일치 + 파일명 Swt 오타 → 그대로면 컴파일 에러)
- `test/security/jwt/SimpleJwtExperiment.java`(Claude 작성): 실행 결과
  - `verify(원본)` = true, `verify(변조: 서명 뒤 5글자 교체)` = false → 비밀키 없으면 위조 불가 확인
  - `decoded payload = {"sub":"kim","exp":1786579181837}` → Payload는 Base64 인코딩일 뿐 암호화 아님, 누구나 열람 가능하다는 것 실측 (비밀번호/개인정보 넣으면 안 되는 이유)
- `verify()`는 서명 위조 여부만 검사, 만료(exp) 체크는 아직 없음 — 다음 단계 과제

**다음 단계**: 만료 시간 검증 추가 → JWT 발급 엔드포인트(`/login`) → 커스텀 `OncePerRequestFilter`로 Spring Security Filter Chain에 JWT 인증 끼워넣기

## 2026-08-12 (계속) - Phase 14: JWT 만료 검증 + 권한 상승 공격 방어 실측 (Opus 5 high 전환)

### Output
- `security/jwt/SimpleJwt.java`(사용자 작성): `parseUsername(token)` + `payload(token)` 추가
  - 설계 의도: `verify()`와 claim 조회를 별도 public API로 나누지 않고 하나로 묶음 → 호출자가 서명 검증 건너뛸 방법 자체를 제거 (JWT 취약점 대부분이 "검증 전 payload 신뢰"에서 발생)
  - Jackson `ObjectMapper`로 payload 파싱 (이미 classpath에 있는 의존성 재사용, 새 라이브러리 없음)
- `test/security/jwt/SimpleJwtExperiment.java`(Claude 작성): 3케이스 실측
  1. 정상 토큰 → `parseUsername = kim`
  2. 만료 토큰(1분 전 만료) → `IllegalStateException: 토큰 만료`
  3. payload를 `"sub":"kim"` → `"sub":"admin"`으로 조작(권한 상승 시도) → `verify = false`, `IllegalArgumentException: 서명 검증 실패`
- 핵심 정리: payload 바뀌면 `sign(header.payload)` 결과도 바뀌어 원본 서명과 불일치. 공격자가 서명 재생성하려면 SECRET 필요 → 서버만 보유하므로 불가능. `exp`도 서명 대상에 포함되어 있어 만료시간 연장 조작 역시 서명에서 걸림
- JWT 한계 언급: stateless라 서버가 토큰 즉시 무효화 불가(로그아웃/탈취 대응 어려움) → Refresh Token/짧은 만료로 완화하는 것뿐
- 미해결 이슈: `SECRET`이 소스에 하드코딩됨 — 유출 시 admin 토큰 위조 가능. 환경변수/`@ConfigurationProperties`로 분리 필요

**다음 단계**: `/login` 엔드포인트(인증 성공 시 JWT 발급) → 커스텀 `OncePerRequestFilter`로 Spring Security Filter Chain에 JWT 인증 끼워넣기

## 2026-08-13 - Phase 14: /login JWT 발급, CSRF 403 → 401 진단 (Opus 5 high)

### Input
"ㄱ" → LoginController 작성 → curl 확인 요청 → 붙여넣기 사고 수정 요청

### Output
- `security/jwt/LoginController.java`(사용자 작성): `AuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))` 직접 호출 후 성공 시 `SimpleJwt.issue()`로 토큰 발급. 요청 DTO는 nested `record LoginRequest`로 파일 하나에 유지
- `security/SecurityConfig.java`(사용자 작성): `/login` permitAll, `AuthenticationManager` Bean 노출(`AuthenticationConfiguration.getAuthenticationManager()`) — Spring Security 6에서 AuthenticationManager는 기본 Bean이 아니라 HttpSecurity 내부에서만 조립되므로 명시적으로 꺼내야 함

### 장애 진단: POST /login 이 401 (핵심 학습)
`logging.level.org.springframework.security=TRACE`로 Filter Chain 실제 순서 확보:
```
Securing POST /login
Invoking CsrfFilter (5/12)  → Invalid CSRF token found → AccessDeniedHandlerImpl: 403
Securing POST /error        ← 컨테이너 ERROR dispatch, Filter Chain 처음부터 재실행
Invoking BasicAuthenticationFilter (7/12) ... AuthorizationFilter (12/12)
  → /error는 permitAll 아님 → 익명 거부 → entry point → 401 (원래 403을 덮어씀)
```
- 세 겹 오해였음: (1) 실제 원인은 CSRF 403, (2) 클라이언트가 본 401은 `/error` 재dispatch에서 나온 것, (3) Basic 자격증명 줘도 소용없던 이유는 `CsrfFilter`(5) < `BasicAuthenticationFilter`(7) 순서 때문 — CSRF 검사 시점엔 인증 자체가 아직 안 일어남
- `permitAll`은 정상 작동했음. 인가는 12번 필터인데 5번에서 잘린 것
- CSRF 비활성화 판단 근거: CSRF 공격 전제는 "브라우저가 자격증명 자동 첨부"(쿠키). `Authorization` 헤더는 자동 첨부 안 됨. 단 **JWT를 쿠키에 저장하면 취약성 부활** — "JWT면 CSRF 불필요"는 틀린 명제
- `SecurityConfig` 수정(Claude, 사용자 붙여넣기 사고 복구): `.csrf(disable)` + `.sessionManagement(STATELESS)` 추가. 응답에서 `Set-Cookie: JSESSIONID` 사라진 것으로 STATELESS 확인
- 결과: `POST /login {"username":"kim","password":"1234"}` → 200 + JWT 문자열 정상 발급

### 미해결 이슈 (Phase 10 잔재)
`ValidationExceptionHandler#handleUnexpected(Exception)`가 과도하게 넓음:
- `GET /login` → `HttpRequestMethodNotSupportedException`(405여야 함)을 삼켜서 500
- 틀린 비밀번호 → `BadCredentialsException`(401이어야 함)을 삼켜서 500
- 우리가 Controller 안에서 직접 `authenticate()` 호출했으므로 Spring Security Filter의 예외 변환(ExceptionTranslationFilter)이 관여하지 않음 → HTTP 상태 매핑 책임이 우리에게 있음

**다음 단계**: `AuthenticationException` → 401 핸들러 추가로 위 이슈 해결 → 그 다음 `OncePerRequestFilter`로 JWT 인증을 Filter Chain에 끼워넣기

## 2026-08-13 (계속) - Phase 14: 인증 실패 401 매핑

### Output
- `validation/ValidationExceptionHandler.java`(사용자 작성): `@ExceptionHandler(AuthenticationException.class)` → 401 추가
  - 1차 버그: IDE 자동완성이 `javax.naming.AuthenticationException`(JNDI 예외) import함 → **컴파일은 통과하지만 핸들러가 영원히 호출 안 되는 무음 버그**. `org.springframework.security.core.AuthenticationException`으로 수정(Claude, 기계적 오류)
- 개념: Basic 인증은 `BasicAuthenticationFilter`(7/12) 실패 → `ExceptionTranslationFilter`(11/12)가 401로 변환(Spring Security 자동). 반면 JWT 로그인은 Filter Chain 전부 통과 후 Controller에서 `authenticate()`를 우리가 직접 호출 → 예외가 Filter 바깥에서 발생 → 상태코드 매핑 책임이 우리에게 넘어옴
- `@ExceptionHandler` 우선순위: `ExceptionDepthComparator`로 상속 거리 가장 가까운 핸들러 선택. `BadCredentialsException`은 `AuthenticationException`(거리 1~2)이 `Exception`(거리 4)보다 가까워 새 핸들러가 이김 — 실측 확인
- 검증 결과: 틀린 비번 401, 없는 유저 401(동일 메시지), 맞는 비번 200+JWT, `GET /login` 여전히 500(별개 이슈)
- 보안 포인트: "없는 유저"와 "비번 틀림"을 같은 응답으로 처리하는 게 정답 — 구분해서 알려주면 user enumeration 공격에 계정 목록 노출됨. Spring Security가 `UsernameNotFoundException`을 `BadCredentialsException`으로 감싸는 이유

**다음 단계**: `OncePerRequestFilter`로 JWT 인증 Filter 구현 → Security Filter Chain에 끼워넣기(`addFilterBefore`) → 발급받은 토큰으로 `/hello` 접근

## 2026-08-13 (계속) - Phase 14 완료: JWT 인증 Filter를 Security Chain에 연결

### Output
- `security/jwt/JwtAuthenticationFilter.java`(사용자 작성): `OncePerRequestFilter` 상속
  - 작성 중 버그 2개: `extends OncePerRequestFilter` 누락(컴파일 에러), `startsWith("Bearer")` 공백 누락(substring(7) offset과 불일치) → 사용자 수정
  - 설계: 401을 직접 쏘지 않음. 토큰 유효하면 SecurityContext 채우고, 없거나 잘못되면 익명 상태로 그냥 통과 → 거부 판정은 `AuthorizationFilter`(13/13) 담당. Authentication/Authorization 책임 분리의 실제 구현
  - `new UsernamePasswordAuthenticationToken(userDetails, null, authorities)` 3인자 = 인증 완료 상태(`isAuthenticated()=true`). LoginController의 2인자(미인증 요청서)와 정반대 의미 — 서명 검증 없이 3인자를 만들면 인증 우회 취약점
  - `@Component` 안 붙임: Boot가 Filter 타입 Bean을 Servlet Container에 자동 등록해버려 이중 등록됨(ManualAuthFilter 실험서 확인한 동작)
- `security/SecurityConfig.java`(사용자 작성, 괄호 오류는 Claude 수정): 메서드에 `UserDetailsService` 파라미터 추가 + `.addFilterBefore(new JwtAuthenticationFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class)`
  - 실제 체인 위치 확인: `JwtAuthenticationFilter (6/13)`, `BasicAuthenticationFilter (7/13)`, `AuthorizationFilter (13/13)`

### 장애 진단 2: kim JWT로 /admin/hello 가 403 아닌 401 (핵심 학습)
TRACE 로그로 규명 — 코드는 정상, 403이 나온 뒤 덮인 것:
```
Securing GET /admin/hello
  AnonymousAuthenticationFilter: Did not set ... since already authenticated  ← JWT 필터 정상
  AccessDeniedHandlerImpl: Responding with 403 status code                    ← 원래 응답 403
Securing GET /error                                                           ← 컨테이너 ERROR dispatch
  AnonymousAuthenticationFilter: Set ... AnonymousAuthenticationToken         ← 익명, JWT 필터 미실행
  ExceptionTranslationFilter: ... to authentication entry point → 401         ← 403 덮어씀
```
세 가지가 겹친 결과:
1. ERROR dispatch가 Filter Chain을 처음부터 재실행 (CSRF 사건 때 목격한 그 메커니즘)
2. `OncePerRequestFilter.shouldNotFilterErrorDispatch()` 기본값 true → 에러 dispatch에서 JWT 필터 미실행 → SecurityContext 빔
3. `STATELESS`라 복원할 세션 없음 (Basic 인증 시절엔 JSESSIONID 덕에 `SecurityContextHolderFilter`가 복원해줘서 403이 정상 노출됐던 것. 로그의 `SessionId=null`이 증거)
- 해결: `.requestMatchers("/login", "/error").permitAll()` — `/error`는 컨테이너 내부 dispatch 대상이므로 인증 요구하면 모든 에러 상태코드가 401로 뭉개짐. JWT+STATELESS 조합에선 사실상 필수
- 대안(미채택): 필터에서 `shouldNotFilterErrorDispatch()`를 false로 오버라이드 — 동작하지만 에러마다 토큰 재파싱 비용

### 최종 검증 (7 시나리오)
kim JWT→/hello 200, kim JWT→/admin/hello 403, admin JWT→/admin/hello 200, 조작 토큰 401, 토큰 없음 401, 틀린 비번 401. `GET /login`만 500(Phase 10 잔재, 별개 이슈)

**Phase 14(Spring Security) 완료.** 남은 정리 항목: (1) `ValidationExceptionHandler`의 과도한 `@ExceptionHandler(Exception.class)` — `ResponseEntityExceptionHandler` 상속으로 정리, (2) `SimpleJwt.SECRET` 하드코딩 → 환경변수/`@ConfigurationProperties` 분리, (3) Refresh Token/토큰 무효화 전략 미구현

**다음 단계**: Phase 15 Testing (`@SpringBootTest`/`@WebMvcTest`/`@DataJpaTest`/MockMvc/TestContext 캐시)

## 2026-08-13 (계속) - Phase 15: Testing (짧게 훑기)

### Input
"testing는 넘어가자" → Phase 15가 실제로 뭘 배우는지 설명 후 "진행하자"로 선회 → 3개 파일로 압축 진행

### Output
- `build.gradle`(Claude): `spring-boot-starter-test:3.4.1`, `spring-security-test:6.4.2` 추가 + `tasks.named('test') { useJUnitPlatform() }` (없으면 JUnit 5 테스트를 아예 인식 못함)
- `test/security/jwt/SimpleJwtTest.java`(사용자 작성): JUnit 5 + AssertJ. Spring Context 없이 static 메서드만 검증 → 배너/Started 로그 전혀 없음, 밀리초 단위. 기존 `SimpleJwtExperiment`(main+System.out)와 대비: 단언 vs 눈으로 확인, 회귀 보존 여부
  - 참고: 기존 `OrderServiceTest`는 이름만 Test고 실제론 `main()`+수제 assertEquals — JUnit이 인식 못하는 가짜 테스트
- `test/security/jwt/LoginControllerTest.java`(사용자 작성): `@WebMvcTest` + `@MockitoBean AuthenticationManager`(Boot 3.4부터 `@MockBean` 대체) + MockMvc로 200/401 검증

### 장애 진단: Unable to find a @SpringBootConfiguration
- Slice Test는 테스트 패키지에서 **위로만** 거슬러 올라가며 `@SpringBootConfiguration`을 찾음. 우리 `MinimalBootApp`은 `com.deepspring.boot`(형제 패키지)라 `com.deepspring.security.jwt`에서 못 찾음
- "Boot 앱 클래스는 루트 패키지에 둬라"는 관례의 실제 이유 = Component Scan 범위와 테스트 탐색 범위가 동시에 해결되기 때문
- 루트 이동은 보류: `com.deepspring` 전체 스캔 시 Phase 1~7 실험용 `@Configuration`들(`TxConfig`의 자체 DataSource, `SpringAppConfig`의 중첩 `@ComponentScan`)이 딸려와 Bean 충돌. Phase 18 Architecture에서 정리할 부채
- 해결(Claude): `@Import` 대신 `@ContextConfiguration(classes = {LoginController.class, SecurityConfig.class})`로 Context 구성 명시
- 통과 후 로그로 확인: `Initializing Spring TestDispatcherServlet` — MockMvc는 Tomcat이 아니라 DispatcherServlet 테스트용 서브클래스를 직접 호출. Context 로딩 4.4초(단위 테스트는 0초)

### TestContext 캐시 실측 (Phase 15 핵심)
- `test/security/jwt/LoginControllerCacheTest.java`(Claude 작성): `LoginControllerTest`와 애노테이션 구성 100% 동일하게 만들고 `System.identityHashCode(mockMvc)` 출력
- **캐시 hit**: `Started ...` 로그 1개뿐, 두 클래스의 MockMvc `identityHashCode`가 동일(1872928774) → 문자 그대로 같은 인스턴스 재사용
- **캐시 miss**: `@MockitoBean UserDetailsService` **한 줄** 추가 → `Started ...` 로그 2개, MockMvc 해시 서로 다름(1824877362 / 342373282) → Context를 통째로 재생성
- 캐시 키 구성요소: `@ContextConfiguration` classes, `@ActiveProfiles`, `@TestPropertySource`, ContextCustomizer 목록(`@MockitoBean`이 여기), ContextLoader 타입, parent context
- 실무 처방: 테스트 설정 조합 가짓수를 줄여야 함. mock은 공통 베이스 클래스/공유 `@TestConfiguration`으로 묶어 캐시 키 통일. "테스트 수 그대로인데 빌드가 8분 됐다"의 전형적 원인

**Phase 15 완료(압축 진행).** 다루지 않은 것: `@DataJpaTest`, Testcontainers, `@Transactional` 테스트 rollback 함정

**다음 단계**: Phase 16 Async/Concurrency — `@Async`에서 Self Invocation 함정 재현(Phase 6 Proxy 지식 회수), Transaction + Async 조합 문제

## 2026-08-13 (계속) - Phase 16: @Async, Self Invocation 재현, Transaction 미전파

### Output
- `async/AsyncService.java`(사용자 작성): `@Async` 메서드 + 내부에서 `this.asyncWork()` 호출하는 `outCall()`. 모든 출력에 `Thread.currentThread().getName()` 포함(동작을 눈으로 보는 장치)
- `async/AsyncConfig.java`(사용자 작성): `@EnableAsync` + `ThreadPoolTaskExecutor`(core 2 / max 4 / queue 10 / prefix "async-")
- `test/async/AsyncExperiment.java`(Claude 작성) 실측 결과:
  1. **외부 호출**: `[main] 호출 직후`가 먼저 찍히고 `[async-1] asyncWork`가 나중 → 호출자 스레드 즉시 해방
  2. **Self Invocation**: `outCall()` 내부 `this.asyncWork()`가 전부 `[main]`에서 **동기 실행**. `bean class = AsyncService$$SpringCGLIB$$0`, `isCglibProxy=true`로 프록시는 존재하지만 `this`는 원본 객체라 인터셉터 미개입 — Phase 6 `@Transactional` 함정과 동일 메커니즘
     - 위험도는 `@Transactional`보다 높음: 트랜잭션은 데이터 이상으로 드러나지만 `@Async` 미적용은 **에러 없이 그냥 느려질 뿐**이라 몇 달 모를 수 있음
  3. **풀 동작**: 5건 제출에 스레드는 `async-1`, `async-2` 둘뿐. 큐(10)에 여유 있으면 maxPoolSize까지 안 늘어남 확인. 실행 순서도 비결정적(job-2가 job-1보다 먼저 시작)
- Executor 탐색 순서 정리: `@Async("이름")` → 유일한 Executor 타입 Bean → 이름이 `taskExecutor`인 Bean → **없으면 `SimpleAsyncTaskExecutor`(풀 아님, 매번 new Thread → OOM 위험)**
- 풀 크기 3값 관계(직관과 반대): corePoolSize 채움 → **큐 먼저 채움** → 큐 꽉 차야 maxPoolSize까지 증설 → 초과 시 `RejectedExecutionException`. `queueCapacity`를 무한대로 두면 maxPoolSize는 죽은 설정

### Transaction + Async 실측
- `async/TxAsyncService.java`(사용자 작성): `@Async` 메서드에서 `TransactionSynchronizationManager.isActualTransactionActive()` 출력
- `async/TxCallerService.java`(사용자 작성, 파일명이 `TxCallService.java`로 클래스명과 불일치해 Claude가 리네임): `@Transactional` 메서드에서 다른 Bean인 `TxAsyncService` 호출(Self Invocation 회피)
- `test/async/TxAsyncExperiment.java`(Claude): `AsyncConfig` + `TxConfig`(Phase 7) 동시 로딩
```
[main]    호출자(@Transactional) | active = true  | name = TxCallerService.doWorkInTransaction
[async-1] 비동기 작업            | active = false | name = null
[async-2] 트랜잭션 밖(대조군)    | active = false | name = null
```
- 핵심: 비동기 스레드 입장에선 "트랜잭션 안에서 호출됨"과 "밖에서 호출됨"이 **구분 불가**. Phase 7에서 직접 만든 `ThreadLocal<FakeConnection>` 구조가 그대로 제약으로 작용 — 트랜잭션 전파(REQUIRED/REQUIRES_NEW)는 **같은 스레드 안에서만** 유효
- 실무 사고 2종: (1) 비동기가 아직 커밋 안 된 데이터 조회 → 타이밍 의존 유령 버그, (2) 호출자가 롤백해도 메일/알림은 이미 발송됨
- 처방: `@TransactionalEventListener(AFTER_COMMIT)`, Outbox 패턴, 비동기 메서드에 별도 `@Transactional` 명시

**다음 단계**: `@TransactionalEventListener(AFTER_COMMIT)`로 위 문제 해결 실습 (Spring Events = 커리큘럼 Phase 41 내용도 같이 회수)

## 2026-08-13 (계속) - Phase 16: Spring Events + @TransactionalEventListener

### Output
- `async/UserRegisteredEvent.java`(사용자 작성): `record` 기반 이벤트. Spring 4.2부터 `ApplicationEvent` 상속 불필요(POJO 가능). 이벤트=이미 발생한 사실이라 불변이어야 함
- `async/RegistrationEventListener.java`(사용자 작성): `@EventListener` / `@TransactionalEventListener(AFTER_COMMIT)` 두 리스너를 나란히 배치해 대비
- `async/TxCallerService.java`(사용자 작성): `ApplicationEventPublisher` 주입 + `registerSuccess`(커밋) / `registerFail`(예외로 롤백) 추가
  - `ApplicationEventPublisher`는 우리가 만든 Bean이 아니라 `ApplicationContext` 자신이 구현한 인터페이스 — Phase 11의 "ApplicationContext가 BeanFactory보다 큰 개념인 이유" 목록 중 하나
- `test/async/TransactionalEventExperiment.java`(Claude) 실측:
```
1. 커밋 성공:
   [main] @EventListener | tx active = true              ← publishEvent 즉시 실행
   [main] registerSuccess 반환
   [main] @TransactionalEventListener(AFTER_COMMIT)      ← 메서드 종료 후 실행
2. 롤백:
   [main] @EventListener | user = lee                    ← 롤백될 작업인데 이미 실행됨
   [main] 예외 잡음
   (AFTER_COMMIT 없음)                                    ← 정확히 침묵
3. 트랜잭션 없이 publishEvent:
   [main] @EventListener | tx active = false
   (AFTER_COMMIT 조용히 무시됨)                           ← 에러/경고 없음
```
- 내부 동작: `publishEvent` 시 트랜잭션 활성이면 `TransactionSynchronizationManager.registerSynchronization()`으로 콜백 등록 → 커밋 완료 시 실행. `ThreadLocal`에 Connection뿐 아니라 **동기화 콜백 목록**도 붙어있음. 트랜잭션 없으면 등록할 곳이 없어 이벤트 소실(`fallbackExecution = true`로 변경 가능)
- `TransactionPhase` 4종: BEFORE_COMMIT(여기서 예외 시 롤백 가능) / AFTER_COMMIT(기본) / AFTER_ROLLBACK / AFTER_COMPLETION
- **주의 발견**: `AFTER_COMMIT` 리스너인데 `transaction active = true`로 찍힘 — 커밋은 끝났지만 ThreadLocal 바인딩은 `AFTER_COMPLETION`까지 유지되기 때문. 여기서 DB 쓰기하면 이미 커밋된 커넥션에 쓰는 꼴이라 저장 안 됨. DB 작업 필요하면 `@Transactional(propagation = REQUIRES_NEW)` 명시 필수
- 이벤트 사용 판단: 발행자가 수신자를 모르게 되어 결합도는 낮아지지만 **실행 흐름 추적이 어려워짐**(호출자 검색 불가). 트랜잭션 경계를 넘거나 수신자가 여럿일 때만 쓰는 게 적절

**다음 단계 후보**: Phase 16 잔여(`CompletableFuture` 반환, `@Scheduled`, Self Invocation 해결법) 또는 Phase 17(Cache/External API) 또는 Phase 18(Architecture, 누적 부채 정리)

## 2026-08-13 (계속) - Phase 17: External API 타임아웃, Connection Pool 고갈 재현

### Output
- `boot/HelloBootController.java`(사용자 작성): `/slow` 엔드포인트 추가(`Thread.sleep(5000)`) — 응답을 안 주는 외부 시스템 흉내
- `security/SecurityConfig.java`(사용자 작성): `/slow` permitAll 추가. 1차에 `"slow"`(앞 슬래시 누락)로 작성 → 매칭 실패로 401 되는 문제 지적, 사용자 수정
- `integration/ExternalApiClient.java`(사용자 작성): `RestClient` 2종 — 타임아웃 없는 것 / `SimpleClientHttpRequestFactory`로 connect 1초·read 2초 건 것
  - 1차 버그: `import com.deepspring.container.Component`(Phase 3에서 우리가 만든 자작 애노테이션)를 IDE가 자동 삽입 → 컴파일은 되지만 Spring Bean으로 등록 안 됨. `javax.naming.AuthenticationException` 사건과 동일 유형. 패키지명 오타(`intergration`)와 함께 Claude가 수정
- `test/integration/TimeoutExperiment.java`(Claude) 실측:
```
타임아웃 있음(read 2초) → ResourceAccessException
                          cause: java.net.SocketTimeoutException - Read timed out
                          걸린 시간 = 2040ms
타임아웃 없음           → 응답 정상 수신, 걸린 시간 = 5064ms   ← 서버가 끝날 때까지 무한 대기
```
- `RestClient`/`RestTemplate` 타임아웃 **기본값은 무한대**. 상대가 응답 안 주면 우리 스레드도 영원히 묶임
- 예외 계층: `ResourceAccessException`(Spring 추상화) ← `SocketTimeoutException`(java.net). Phase 11 JDBC Exception Translation과 같은 패턴이라 잡을 땐 Spring 예외를 잡아야 함
- 타임아웃 값 기준: 상대 API p99 × 2~3배, 우리 SLA 안. "넉넉하게 30초"가 제일 위험(장애 시 스레드 200개가 30초씩 잠김)

### Connection Pool 고갈 실측 (Phase 17 핵심)
- `integration/PaymentService.java`(사용자 작성): `@Transactional` 안에서 `select 1` → 외부 API 5초 → `select 1`
- `test/integration/ConnectionPoolExhaustionExperiment.java`(Claude): HikariCP `maximumPoolSize=2`, `connectionTimeout=3000`, 동시 3건
```
[worker-3] req-3 DB 커넥션 확보 (+36ms)
[worker-2] req-2 DB 커넥션 확보 (+36ms)
[worker-1] req-1 FAILED: CannotCreateTransactionException
    cause: SQLTransientConnectionException - Connection is not available,
           request timed out after 3012ms (total=2, active=2, idle=0, waiting=0)
[worker-2] req-2 트랜잭션 종료 (+5144ms)
[worker-3] req-3 트랜잭션 종료 (+5144ms)
```
- 핵심: active 2개가 실제로 하는 일은 **아무것도 없음**. 5초 중 DB 쿼리는 `select 1` 두 번(1ms 미만), 나머지는 외부 API 대기. DB는 완전히 한가한데 커넥션 풀만 고갈
- 예외 타입이 `CannotCreateTransactionException` = 쿼리 실패가 아니라 **트랜잭션 시작 실패**. 프록시가 커넥션을 못 얻어 트랜잭션 자체를 못 엶
- 실무 번역: "DB 지표는 정상인데 주문 조회가 500" → 결제사 API 지연이 커넥션 풀을 말려서 무관한 DB 작업까지 연쇄 실패. DB 지표만 보면 절대 못 찾음
- 해결 우선순위: (1) 외부 호출을 트랜잭션 밖으로 (근본), (2) 타임아웃 필수(피해 시간 단축), (3) Circuit Breaker(상대 장애 시 호출 차단)

**다음 단계**: 해결책 1번(트랜잭션 밖으로 분리) 적용해서 같은 실험 통과시키기

## 2026-08-13 (계속) - Phase 17: 트랜잭션 경계 분리, Self Invocation 3번째 실측

### Output
- `integration/PaymentService.java`(사용자 작성): `payOutsideTransaction` 추가. 1차엔 같은 클래스의 `saveInTransaction()`을 `this`로 호출(Self Invocation 의도적 배치)
- 풀 고갈 실험 재실행 → 3건 전부 성공. **하지만 통과 이유가 틀림**
- `test/integration/SelfInvocationCheckExperiment.java`(Claude) + `PaymentService.saveInTransaction`에 tx 상태 출력 추가(사용자):
```
bean = PaymentService$$SpringCGLIB$$0, isCglibProxy = true
1. 프록시 경유 직접 호출   → transaction active = true
2. Self Invocation 경유    → transaction active = false
```
  - 같은 메서드, 같은 `@Transactional`, 호출 경로만 다른데 정반대. 실험이 통과한 건 "트랜잭션을 잘 분리해서"가 아니라 **트랜잭션이 아예 없어서**였음
  - Self Invocation 3번째 실측: Phase 6(`@Transactional`), Phase 16(`@Async`), Phase 17(여기). 매번 다른 기능인데 원인 동일 — 프록시 기반 AOP의 구조적 한계
- `integration/PaymentRepository.java`(사용자 작성): `@Repository` + `@Transactional save()`로 DB 작업 분리
- `integration/PaymentService.java` 수정(사용자 작성, 생성자 미할당/호출 누락은 Claude 보완): `paymentRepository.save(label)` 호출 후 외부 API 호출
- 최종 검증: 3건 모두 `transaction active = true`이면서 풀 크기 2로 동시 3건 전부 성공, DB 구간 218ms / 외부 API 5327ms

### 정리
```
                       트랜잭션 안   Self Invocation   Bean 분리
트랜잭션 실제 열림?     O             X                 O
풀 고갈(2/3건)?        req-1 실패     통과              통과
통과 이유              -             트랜잭션 없어서    경계가 올바라서
```
가운데가 실무에서 가장 위험 — **동작은 하는데 이유가 틀림**. 부하 테스트도 통과하고, DB 작업이 2개로 늘어나는 순간 원자성이 깨져 데이터 정합성 사고 발생

### 설계 원칙
- `@Transactional`을 어디에 붙일지 = 트랜잭션 경계 설계. **외부 호출이 있는 메서드에는 붙이면 안 됨**
- 계층 책임: Application 계층(PaymentService)은 흐름 조율 + 경계 결정, Infrastructure 계층(PaymentRepository)은 DB 작업 + 원자성
- 자기 주입(`@Lazy` self-inject)이나 `AopContext.currentProxy()`로도 우회 가능하지만 설계 문제를 기술로 덮는 것. Bean 분리가 정공법

**다음 단계**: Phase 17 잔여(`@Cacheable`, Retry/Circuit Breaker) 또는 Phase 18 Architecture(누적 부채 정리)

## 2026-08-13 (계속) - Phase 17: @Cacheable, Cache Stampede

### Output
- `integration/CachedApiService.java`(사용자 작성): `@Cacheable("slowApi")` / `@Cacheable(value="slowApi", sync=true)` 두 메서드로 5초짜리 외부 호출 감쌈
- `test/integration/CacheExperiment.java`(Claude): `@EnableCaching` + `ConcurrentMapCacheManager`, CountDownLatch로 동시 시작 정렬
```
1. 첫 호출 (miss)        5109ms
2. 같은 키 재호출 (hit)     1ms   ← "실제 외부 호출 발생" 로그 안 찍힘
3. 다른 키 "b" (miss)    5008ms
4. 동시 3건 sync=false   5010ms  → worker-0/1/2 전부 외부 호출 (3번)
5. 동시 3건 sync=true    5019ms  → worker-2 하나만 호출 (1번)
```
- 캐시 히트 시 `println`조차 안 찍힘 = `CacheInterceptor`가 **원본 메서드를 호출하지 않음**(메서드 진입 자체가 없음)
- 네 번째 프록시 기능: `@Transactional`(TransactionInterceptor) / `@Async`(AsyncExecutionInterceptor) / `@Cacheable`(CacheInterceptor) / AOP. Self Invocation 함정도 동일하게 적용
- 캐시 키: 파라미터로 자동 생성(`SimpleKeyGenerator`). 파라미터가 객체면 `equals`/`hashCode` 필수 — 없으면 영원히 miss. `key = "#key"` SpEL로 직접 지정 가능
- **Cache Stampede**: 4번과 5번의 전체 시간은 같지만(외부 API가 5초) **외부 시스템이 받은 부하가 3배 vs 1배**. 실서비스에선 인기 키 만료 순간 동시 500건이 백엔드에 그대로 꽂힘 → DB 사망 → 캐시 계속 비어있어 복구 후 또 몰림(장애 자기증식)
- `sync=true` 한계: 단일 JVM 내 락일 뿐. 서버 3대면 서버마다 1번씩 총 3번. 분산 환경은 Redis 분산 락 또는 TTL jitter 필요
- `ConcurrentMapCacheManager`는 **TTL 자체가 없음** — 학습용. 운영은 Caffeine(로컬) / Redis(분산)

**Phase 17 완료** (Retry/Circuit Breaker는 resilience4j 의존성 필요해 스킵)

**다음 단계**: Phase 18 Architecture — 누적 부채 정리
1. `ValidationExceptionHandler`의 `@ExceptionHandler(Exception.class)`가 405/BadCredentials까지 삼켜 500으로 만듦
2. `SimpleJwt.SECRET` 하드코딩 → 환경변수/`@ConfigurationProperties`
3. `MinimalBootApp`이 루트 패키지가 아니라 `com.deepspring.boot`에 있어 테스트 슬라이스가 못 찾음
4. Phase 1~7 실험용 `@Configuration` 난립(`AppConfig`, `SpringAppConfig`, `TxConfig`)으로 루트 스캔 불가
5. 미사용 파일: `transaction/TransactionTemplate`, `TransactionCallback` 등

## 2026-08-13 (계속) - Phase 18: 부채 정리 1·2 (상태코드, JWT SECRET 외부화)

### 부채 1 — @ExceptionHandler(Exception.class) 제거
- `validation/ValidationExceptionHandler.java`(사용자): `handleUnexpected(Exception)` 6줄 **삭제**
- 검증: `GET /login` 405(이전 500), 틀린 비번 401, 정상 로그인 200, `/admin/hello` 403 — 전부 정상
- 핵심: 코드를 **추가하지 않고 삭제해서** 고침. 프레임워크(DefaultHandlerExceptionResolver)가 원래 잘 하던 일을 우리가 가로채고 있었던 것
- `@ExceptionHandler` 우선순위가 상속 거리 기반이라 `Exception.class`는 모든 예외의 후보로 등록됨 → 더 가까운 핸들러가 없는 프레임워크 예외들이 전부 빨려옴
- 스택 트레이스 노출 우려 없음: Boot 3 기본값 `server.error.include-stacktrace=never`

### 부채 2 — SECRET 하드코딩 제거
- `security/jwt/JwtProperties.java`(사용자 작성): `@ConfigurationProperties(prefix="jwt")` — secret, expirationMillis
- `security/jwt/SimpleJwt.java`(사용자 작성): **static 유틸 → Bean 전환**
  - 이유: 설정값을 쓰기 시작하면 DI가 필요 → static은 주입 불가 → Bean이어야 함
  - 생성자에서 secret 없으면 `IllegalStateException` → **Fail Fast**(비밀키 없으면 앱이 아예 안 뜸). Phase 13 Query Method 검증과 같은 원리
  - API 변경: `issue(username, expireEpochMillis)` → `issue(username)`, 만료시간은 설정에서
- 배선(Claude, 기계적): `application.yml`에 `jwt.secret`/`jwt.expiration-millis` 추가, `MinimalBootApp`에 `@ConfigurationPropertiesScan`, `LoginController`/`JwtAuthenticationFilter`에 `SimpleJwt` 주입, `SecurityConfig`가 필터에 전달
- 테스트 수정(Claude): `SimpleJwtTest`/`SimpleJwtExperiment`를 인스턴스 API로 변경 + "secret 없으면 생성 실패" 테스트 추가. `@WebMvcTest` 슬라이스에 `SimpleJwt` Bean이 없어 기동 실패 → `test/security/jwt/TestJwtConfig.java` 신설해 `@ContextConfiguration`에 추가
- 검증: JWT 테스트 7개 통과, 실제 앱에서 200/403/401 정상
- **환경변수 오버라이드 실측**: `JWT_SECRET=...`로 기동 → 새 키 토큰 200, 다른 서명 토큰 401. `JWT_SECRET` → `jwt.secret` Relaxed Binding 동작 확인. 코드 수정 없이 배포 환경에서만 비밀키 교체 가능
- 부수 발견: 비밀키 교체 = **기존 발급 토큰 전체 무효화** 수단. Phase 14에서 "JWT는 무효화 불가"라 했지만 전체 무효화는 이 방법으로 가능(선택적 무효화는 여전히 불가)

**다음 단계**: 부채 3(루트 패키지 문제) / 부채 4(실험용 @Configuration 난립) / 부채 5(미사용 파일 정리)
