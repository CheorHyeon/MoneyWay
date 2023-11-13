![images](https://bow-hair-db3.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F571a24a3-05f9-4ea5-b01f-cba1a3ac070d%2F15e422df-e785-43b5-9d2e-b70edf6f20b5%2Fsave.png?table=block&id=90cba97a-58a8-43e4-a256-3a226db3d5b5&spaceId=571a24a3-05f9-4ea5-b01f-cba1a3ac070d&width=2000&userId=&cache=v2)
# Money_Way

- 사용자의 예산 계획에 맞춘 지출 추적 서비스

# 목차

- [개요](#개요)
- [ERD](#erd)
- [Skills](#skills)
- [Flow Chart](#flow-chart)
- [TIL(Today I Learn)](#til)
- [회고](#회고)

## 개요

- 본 서비스는 사용자들이 개인 재무를 관리하고 지출을 추적하는 데 도움을 주는 애플리케이션입니다.
- 이 앱은 사용자들이 예산을 설정하고 지출을 모니터링하며 재무 목표를 달성하는 데 도움이 됩니다.

## ERD
![image](https://github.com/CheorHyeon/MoneyWay/assets/126079049/80d811b0-cec5-4331-8357-2d8555d33044)
- `Member - Expendituer` : 소비 데이터 사용자 1건당 없을수도 있고 많을수도 있음 `1:N`
- `Member - Plan` : 사용자가 예산 지출 계획 등록 전이면 없을 수 있고, 등록했다면 다수 `1:N`
- `Category - Plan` : 각 카테고리는 여러개의 Plan 객체에 하나씩만 포함됨
  - ex) 식비 카테고리 -> A라는 사람 Plan에서 1개, B라는 사람 Plan에서 1개, .. `1:N` 모든 사용자가 예산 등록 전이면 없을 수도 있음
- `Category - Expenditure` : 각 카테고리는 여러개의 지출을 가질 수 있음 `1:N`
  - ex) 교통 카테고리 -> 버스, 지하철, 택시, .. `1:N`


## Skills

- 언어 및 프레임워크: Java 17, Spring Boot 3.0
- 데이터베이스: MariaDB, H2(Test in Memory)
- 라이브러리 : Spring Security, Query DSL, Swagger, JWT

## Flow Chart

### 회원가입 & 로그인


- ID/PW로 회원가입
  - PW 제약 조건 3가지 적용 및 회원 Data 생성
    - 회원가입 기능 구현, PW 제약조건 3가지 설정
    - [상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/2)
    
- ID/PW로 로그인 -> JWT(Access Token 및 Refresh Token) 발급
  - ID/PW 검증 후 JWT AT & RT 발급
    - RT가 기존에 있더라도 갱신
    - [상세 설명 및 코드 - PR 바로가기](https://github.com/CheorHyeon/MoneyWay/pull/4)

### 사용자 인증이 필요한 기능 동작 전 JWT 인증 방식

...(작성중)

 
## 회고


## TIL

| 키워드 | 링크 |
| :----: | :----: |

