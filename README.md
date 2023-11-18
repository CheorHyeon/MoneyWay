![images](https://bow-hair-db3.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F571a24a3-05f9-4ea5-b01f-cba1a3ac070d%2F15e422df-e785-43b5-9d2e-b70edf6f20b5%2Fsave.png?table=block&id=90cba97a-58a8-43e4-a256-3a226db3d5b5&spaceId=571a24a3-05f9-4ea5-b01f-cba1a3ac070d&width=2000&userId=&cache=v2)
# Money_Way

- 사용자의 예산 계획에 맞춘 지출 추적 서비스

# 목차

- [개요](#개요)
- [ERD](#erd)
- [Skills](#skills)
- [API Swagger Docs](#api-swagger-docs)
- [Flow Chart](#flow-chart)
- [회고](#회고)

## 개요

- `Money Way`는 이름에서도 알 수 있듯이, 사용자들이 자신의 돈('Money')을 어떻게 관리하고 지출하는지('Way')를 체계적으로 돕는 애플리케이션입니다.
- 이 애플리케이션을 통해 사용자는 자신의 예산을 설정하고 지출을 효과적으로 모니터링하여 재무 목표를 명확하게 설정하고 성공적으로 달성하는 길을 찾을 수 있습니다.
- `Money Way`는 이번 달 남은 지출 가능액과 남은 일수를 기준으로 하루 사용 예산을 추천하는 컨설팅 기능을 제공하여, 사용자가 자신의 돈을 어떻게 지출할지에 대한 명확한 방향을 제시합니다.
- 또한, 각 지출 카테고리별 위험도를 분석하여 사용자에게 알려주고, 지난 주나 저번 달 해당 일자까지의 지출, 그리고 다른 사용자와의 지출률을 비교한 통계 자료를 제공하여, 사용자의 지출 패턴을 이전 데이터와 비교하거나 다른 사용자와 비교하여 보다 효율적인 지출 관리를 돕습니다.
- 이런 다양한 기능들을 통해 `Money Way`는 사용자가 지출을 절약하고 재무 목표를 달성하는 데 큰 도움을 줍니다.

## ERD
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/80d811b0-cec5-4331-8357-2d8555d33044)
- `Member - Expendituer` : 소비 데이터 사용자 1건당 없을수도 있고 많을수도 있습니다. `1:N`
- `Member - Plan` : 사용자가 예산 지출 계획 등록 전이면 없을 수 있고, 등록했다면 다수가 됩니다. `1:N`
- `Category - Plan` : 각 카테고리는 여러 사용자의 Plan에 각각 하나씩 연결될 수 있습니다. `1:N`
  - ex) '식비' 카테고리는 A 사용자의 Plan, B 사용자의 Plan 등 여러 Plan에 연결될 수 있습니다. `1:N` 모든 사용자가 예산 등록 전이면 관계가 없을 수도 있습니다.
- `Category - Expenditure` : 각 카테고리는 여러개의 지출을 가질 수 있습니다. `1:N`
  - ex) 교통 카테고리 -> 버스, 지하철, 택시, .. `1:N`


## Skills

- 언어 및 프레임워크: Java 17, Spring Boot 3.0
- 데이터베이스: MariaDB, H2(Test in Memory)
- 라이브러리 : Spring Security, Query DSL, Swagger, JWT

## API Swagger Docs
![Swagger UI_1](https://github.com/CheorHyeon/MoneyWay/assets/126079049/24fc48e3-6fd8-4a6e-adbd-553a90b242d5)

## Flow Chart
<details>
  <summary><h3>유저 Flow</h3></summary>
  
### 회원가입 & 로그인
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/12d4da01-94d2-4f5f-a772-2fa787f8003f)

- ID/PW로 회원가입
  - PW 제약 조건 3가지 적용 및 회원 Data 생성
    - 회원가입 기능 구현, PW 제약조건 3가지 설정
    - [상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/2)
    
- ID/PW로 로그인 -> JWT(Access Token 및 Refresh Token) 발급
  - ID/PW 검증 후 JWT AT & RT 발급
    - RT가 기존에 있더라도 갱신
    - [상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/4)

### 사용자 인증이 필요한 기능 동작 전 JWT 인증 방식 및 필터 설정

![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/1e22c94e-1707-452a-be7f-61f08011f200)

- JWT로 인증이 필요한 모든 URI에 접속하기 전 토큰 검증
  - Access Token이 유효하다면 통과
  - 유효하지 않다면, Refresh Token 값을 비교하고 유효하면 JWT를 재발행 응답 해주고 통과
  - Refresh Token도 유효하지 않다면 `재로그인 요청 메세지` 반환
 
- 토큰 검증을 요청 전에 적용하기 위한 필터 설정
  - UsernamePassword 인증 필터 전 JWT 인증 필터를 두어 JWT로 인증 처리 하기 위한 필터 도입
  - 회원가입 / 로그인 / Swagger 문서 관련 URI 접속 시 필터 적용되지 않도록 설정
  - 각각 상황에 맞는 예외 처리를 통해 왜 통과가 안 되었는지 사용자에게 알려줍니다.
  - [상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/6)

### 필터 발생 예외 처리 및 카테고리 조회 기능 API

#### 필터에서 발생하는 예외 처리

![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/8b47e652-659c-4407-ab2f-c66fb4f271e8)

  - 필터단에서 발생하는 예외 처리는 Servlet 단에서 발생하지 않기 때문에 @ControllerAdvice 어노테이션으로 해결할 수 없어 별도의 AuthenticationExceptionHandlerFilter 필터를 생성하였습니다.
  - Swagger 접속 시 필터 적용 안 되게 하기 위해 정규표현식을 수정하였습니다.

</details>

<details>
  <summary><h3>예산설정 및 설계 Flow</h3></summary>

#### 카테고리 조회 기능

![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/06463482-4e8b-49f7-a881-5294a9f5cbb1)
  - JWT 인증 필터를 거쳐 인증된 사용자는 카테고리 목록 조회가 가능합니다.

**[카테고리 조회 및 필터 예외 처리 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/8)**

### 예산 설계 및 추천 기능

![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/fec8e4c6-b8e9-4862-b074-4ddf7f318d85)
#### 예산 설계

- 카테고리별 설정할 예산을 받아서 설정해 주며, 입력하지 않을 시 기본값으로 0원 설정합니다.
  - 만일 모두 0원인 경우 하나라도 입력하도록 메세지 반환합니다.
#### 예산 추천

- 예산 계획에 있는 전체 예산에서 해당 카테고리의 목표 예산의 비율의 평균을 구합니다.
  - 해당 평균을 카테고리별로 총액과 곱해서 결과를 반환합니다.
- 사용자는 반환받은 추천액을 통해 예산 설계 API를 다시 호출해야 등록됩니다.
  - Query DSL 활용 시 Java Reflection API를 사용하여 동적으로 Setter 메서드를 호출하여 각 튜플에 저장된 평균값을 DTO 객체에 값 지정하여 코드의 양을 줄였습니다.

**[예산 설계 및 추천 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/10)**

### 예산 계획 수정

![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/9a8d7bb8-fb72-4aaa-9cdc-7d4a6de185dd)

- 등록 메서드에서 기존에 등록된 지출 계획이 있는지 검사합니다.
   - 등록 된 지출 계획이 있다면 해당 카테고리 지출 계획을 수정
   - 만일 없다면 신규 등록
   - [예산 계획 수정 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/12)

 </details>

 <details>
  <summary><h3>지출 기록 Flow</h3></summary>
   
### 지출 내역 생성 및 삭제

![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/1d22d16b-1b2e-405f-b390-05287bad8300)
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/e8413f1b-0ac9-4936-b1a3-27ab3e8b2c38)


#### 지출 내역 생성
- 사용자로부터 카테고리 id, 지출 금액, 비고(메모), 지출 일자, 합계 포함 여부를 입력받습니다.
- 존재하는 카테고리인지 확인 후 지출 내역에 등록합니다.

#### 지출 내역 삭제
- 삭제하고자 하는 지출 내역 id를 입력받아 이미 존재하는지, 해당 사용자가 작성한 건지 확인합니다.
- 이미 존재하고 요청한 사용자가 작성한 지출 내역이 맞다면 삭제 처리, 아니라면 실패 메세지를 반한힙니다.

**[지출 내역 등록 및 삭제 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/15)**

### 지출 내역 수정
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/d6a75d25-ac32-4c28-9df0-2aee6b661847)

- 지출 id를 URI로 포함하여 받고, 수정 내용을 DTO 객체로 받습니다.
  - 수정 내용을 아무것도 작성해서 요청을 보내지 않으면 실패 메세지를 보냅니다.
  - id에 해당하는 지출 객체를 찾고, 수정할 수 있는지 검사합니다(작성자가 현재 로그인한 사용자가 맞는지, 존재하긴 하는지)
  - 수정 가능하다면 지출 내역을 수정합니다.
  - PATCH 메서드 방식으로 수정을 구현하여, 일부 속성만 수정할 수 있도록 구현하였습니다. 
  - [예산 계획 수정 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/23)

### 지출 상세 내역 조회 및 목록 조회
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/4c82e9b1-cffa-4730-99c8-a4cd18ab39c4)
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/6f522f44-b96f-488c-8a27-7b532499fab7)

#### 지출 내역 상세
- 사용자로부터 URI에 조회하고자 하는 지출 내역 id를 입력받습니다.
- 존재하는 지출 내역인지 확인한 후 결과를 반환합니다.

#### 지출 내역 목록 조회
- `특정 기간 / 카테고리별 / 금액별` 조회가 가능합니다.
  - 3가지 조건 중 일부(0개 이상) 혹은 전부 적용할 수 있습니다.
  - 페이지를 적용한 결과를 반환하며, 아무런 정보도 입력하지 않으면 기본값으로 0페이지 데이터를 반환하며, 한 페이지당 10개씩 보여줍니다!
  - 조건에 맞게 쿼리를 생성할 수 있는 `BooleanBuilder 객체를 활용`하여 Pagenation 처리를 합니다.

**[지출 내역 등록 및 삭제 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/17)**

</details>

<details>
  <summary><h3>지출 컨설팅 Flow</h3></summary>
  
### 이번 달 지출 가능 금액 조회(총액, 카테고리별)
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/7c968574-e4fd-4783-b8c2-52ef2d54b630)
- 현재 날짜 기준으로 이번 달 지출 가능 금액을 계산합니다.
  - 총액 : 총지출액 - 계획
  - 카테고리별 : 카테고리별 총지출액 - 계획(지출 내역 없으면 계획 값 반환)
  - 만일 사용자가 목표 설정 전에 해당 API 요청을 하면 지출 계획부터 세우라는 메세지 반환
  - [이번 달 지출 가능 금액 조회(총액, 카테고리별) 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/19)

### 오늘 사용 예산 추천
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/09b66a97-fdcb-4462-85c6-af2ff6b1bf05)
- 이번 달 1일 ~ 어제 날짜까지 지출 내역 및 해당 사용자 지출 계획 추출
- 총지출액 및 카테고리별 지출액과 목표 금액 차를 구한 뒤 말일까지 남은 일수로 나눠서 해당 일자 추천액 반환
**기존 : 현재 시점까지 지출액 확인 -> 변경 : 어제 날짜까지 지출액 확인(오늘의 지출 내역 추천이기 때문)**
  - 기존 : [오늘 사용 예산 추천 기능 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/19)
  - 수정 : [refactor : 오늘 예산 추천 코드 함수화 커밋](https://github.com/CheorHyeon/MoneyWay/pull/25/commits/7a4b6a6829b793adab5bff56f97866b02f964b28)
 
### 오늘 지출 내역과 카테고리별 지출 위험도 안내
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/97ed50a3-bba8-4a44-9058-ca555c3318fd)

- 오늘 지출 내용을 총액과 카테고리별 금액으로 알려줍니다.
- 컨설팅의 일부로 아래 내용을 추가로 제공합니다.
  - 오늘 적절 금액 : 오늘 사용했으면 적절했을 금액 // 오늘 추천액 중 총액
  - 오늘 지출 금액 : 오늘 기준 사용한 총금액 // 오늘 사용액 중 총액
  - 위험도 : 카테고리별 적정 금액, 지출 금액의 차이를 위험도로 나타냄 // 각 카테고리별 사용액 / 카테고리별 추천액
- 이를 위해 오늘 사용 추천액 + 오늘 실제 사용액의 데이터를 활용하여 구현하였습니다.
  - [오늘 지출 내역과 카테고리별 지출 위험도 안내 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/24)

</details>

<details>
  <summary><h3>통계 Flow</h3></summary>
  
### 지난달 대비 오늘 일자 기준 사용량 통계 데이터 추출
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/b3d1a46b-bf4a-4023-915f-8a9d94f3bfdc)

- 오늘 일자까지 사용한 이번 달 지출량과 지난달의 오늘 일자까지 지출한 지출량 대비 통계 데이터를 추출합니다.
  - 지난달의 오늘 날짜까지 소비 내역(지난달 1일 ~ 오늘 일)
  - 이번 달 오늘 날짜까지 소비 내역(이번달 1일 ~ 오늘)
    - 총액 : 오늘 사용량 / 지난달 오늘 일자 사용량
    - 카테고리별 사용량 : 카테고리별 오늘 사용량 / 지난달 오늘 일자까지 사용량
  - [지난달 대비 오늘 일자 기준 사용량 통계 데이터 추출 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/27)

### 7일 전 총 지출액과 오늘 지출액 비교
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/e2b1d3cf-63e2-44dd-b49f-7e9d2c87b0a0)

- 7일 전 지출 총액과 오늘 지출 총액을 비교하는 통계 데이터를 추출합니다.
  - 오늘 지출 총액
  - 7일 전 지출 총액
  - 오늘 지출 / 7일 전 지출 비율
  - [7일 전 총 지출액과 오늘 지출액 비교 기능 구현 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/29)

### 다른 유저 평균 지출액 대비 나의 지출액 비율 반환
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/67409dbe-c6f4-43a1-84c3-9ad16a524f86)
- 오늘 지출한 금액과 다른 사용자들의 오늘 평균 지출액, 비율을 반환하는 기능을 구현합니다.
  - 나의 총 오늘 지출액 / 다른 사람들의 평균 지출액
  - [7일 전 총지출액과 오늘 지출액 비교 기능 구현 상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/31)
  
</details>

## 회고
- 동적 쿼리 적용을 위해 Query DSL을 사용해 볼 수 있어 좋았다.
- JWT를 Refresh Token을 활용하여 구현해 볼 수 있어 좋았다.
