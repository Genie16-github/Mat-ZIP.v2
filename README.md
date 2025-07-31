<div align="center">

![logo](https://github.com/LikeLion-team10/Mat-ZIP/assets/80519614/1df73ebf-848e-45e6-bede-9af087c90931)


</div>

<div align="center">
<div>
    <h1> 🍴 Mat.ZIP ( 맛집 지도 만들기 ) .v2</h1>

[<img src="https://img.shields.io/badge/-matzip.site-purple?style=flat&logo=google-chrome&logoColor=white" />](https://matzip.site)
[<img src="https://img.shields.io/badge/-Mat.ZiP Notion-blue?style=flat&logo=google-chrome&logoColor=white" />](https://www.notion.so/MatZIP-MZ-2739bdf0a306410a831bbfc5128c437d)

</div>
<br />
<div align="left">

## 목차

#### 1. [**프로젝트 소개**](#프로젝트-소개)

#### 2. [**사용 기술 스택**](#사용-기술-스택)

#### 3. [**주요 기능 요약**](#주요-기능-요약)

#### 4. [**시스템 아키텍처**](#시스템-아키텍처)

#### 5. [**CI/CD 자동 배포**](#cicd-자동-배포)

#### 6. [**프로젝트 구조**](#프로젝트-구조)

#### 7. [**ERD(Entity Relationship Diagram)**](#erdentity-relationship-diagram)

#### 8. [**트러블슈팅 & 기술 포인트**](#트러블슈팅--기술-포인트)

#### 9. [**프로젝트 프리뷰**](#프로젝트-프리뷰)

<br />


## 📌프로젝트 소개

**Mat-ZIP**은 지역 기반 맛집 정보를 공유하고 사용자 간 소통이 가능한 커뮤니티 웹 애플리케이션입니다.  
로그인한 사용자는 게시글과 댓글을 등록할 수 있으며, 이미지를 포함한 맛집 후기를 공유할 수 있습니다.  
Spring Boot, JPA, JWT, S3 등 실무에서 사용되는 기술을 바탕으로 설계하고 구현하였으며,  
Docker와 GitHub Actions, AWS EC2를 통해 **CI/CD 자동 배포 환경까지 구축**했습니다.

<br />


## 🛠사용 기술 스택

### FrontEnd / BackEnd
| <img src="./assets/html5.svg" width="50px" alt="HTML5" /><br> <sub>HTML</sub> | <img src="./assets/css.svg" width="50px" alt="CSS" /><br> <sub>CSS</sub> | <img src="./assets/javascript.svg" width="50px" alt="JavaScript" /><br> <sub>JavaScript</sub> | <img src="./assets/java.svg" width="50px" alt="Java" /><br> <sub>Java</sub> | <img src="./assets/spring.svg" width="50px" alt="Spring" /><br> <sub>Spring</sub> |
|:--:|:--:|:--:|:--:|:--:|

### Database / File Storage

| <img src="./assets/mysql.svg" width="50px" alt="MySQL" /><br> <sub>MySQL</sub> | <img src="https://github.com/LikeLion-team10/Mat-ZIP/assets/80519614/5771b88a-ef65-44d4-843d-1d757a92cc63" width="70px" alt="AWS S3" /><br> <sub>AWS S3</sub> |
|:--:|:--:|

### CI/CD & Infra

| <img src="https://github.com/LikeLion-team10/Mat-ZIP/assets/80519614/4952a8e3-a6dc-40ef-964a-352c8cf6065c" width="50px" alt="AWS EC2" /><br> <sub>AWS EC2</sub> | <img src="./assets/docker.svg" width="50px" alt="Docker" /><br> <sub>Docker</sub> | <img src="./assets/githubactions.svg" width="50px" alt="Github Actions" /><br> <sub>Github Actions</sub> | <img src="./assets/jenkins.svg" width="50px" alt="Jenkins" /><br> <sub>Jenkins</sub> |
|:--:|:--:|:--:|:--:|

### Documentation

| <img src="./assets/markdown.svg" width="50px" alt="Markdown" /><br> <sub>Markdown</sub> |
|:--:|

<br />

## 🔥주요 기능 요약

### 🔐 사용자 인증
- 회원가입 / 로그인 (JWT 토큰 발급)
- Access Token + Refresh Token 기반 인증 시스템 구축
- 로그인 상태 유지 및 만료 시 자동 재발급

### 📝 게시판
- 맛집 후기 게시글 CRUD
- 게시글 별 이미지 업로드 (S3 연동)
- 게시글 목록 페이징 처리

### 💬 댓글 기능
- 게시글 별 댓글 작성/수정/삭제
- 사용자 권한에 따라 수정/삭제 제한

### 🛡 예외 처리 및 보안
- 커스텀 예외 핸들링 (`@RestControllerAdvice`)
- Spring Security + JWT 기반 인가 필터 적용
- 비정상 요청에 대한 명확한 에러 메시지 제공
<br />

## 🧩시스템 아키텍처

<img src="https://github.com/LikeLion-team10/Mat-ZIP/assets/80519614/50291713-06da-4580-bebd-da3033d91c04" alt="architecture" width="700px" >

<br /><br />

## 🚀CI/CD 자동 배포

- 초기에는 Jenkins를 활용해 EC2 서버 내에서 CI/CD 환경을 구축
  - Docker, Nginx, GitHub Webhook을 연동하여 코드 변경 시 자동 배포 흐름 구현
- Jenkins는 커스터마이징 자유도는 높았으나, 지속적인 자원 점유 및 관리 비용 이슈 발생
- 이후 GitHub Actions 기반으로 전환하여, 외부 서버 없이도 안정적인 CI/CD 자동화 구성
- 주요 구성:
  - GitHub Actions를 활용한 빌드 자동화
  - Dockerfile을 기반으로 컨테이너 이미지 생성
  - EC2 서버로 SSH 접속하여 자동 배포
  - '.yml' 환경 변수 파일 기반의 민감 정보 관리 및 보안 강화
 
<br /><br />

## 📂프로젝트 구조

```bash
Mat-ZIP/
├── src/
│   ├── config/           # 설정 클래스
│   ├── controller/       # REST API 엔드포인트
│   ├── dto/              # 요청 및 응답 DTO
│   ├── entity/           # JPA Entity 클래스
│   ├── repository/       # Spring Data JPA 리포지토리
│   ├── security/         # JWT, 인증 필터, 시큐리티 설정
│   ├── service/          # 비즈니스 로직
│   └── util/             # 유틸성 클래스
├── Dockerfile            # 백엔드용 Docker 이미지 빌드 설정
├── docker-compose.yml    # 배포용 서비스 통합 설정 (DB, 앱)
├── application.yml       # 전역 환경 설정 파일
├── .github/
│   └── workflows/
│       └── deploy.yml    # GitHub Actions 자동 배포 워크플로우
└── README.md
```

<br /><br />

## 📎ERD(Entity Relationship Diagram)

<img src="https://github.com/LikeLion-team10/Mat-ZIP/assets/80519614/f6dac71b-e251-43f6-bd40-cfc912d00f4b" alt="ERD" width="700px" >

<br /><br />

## 🧠트러블슈팅 & 기술 포인트

| 문제 | 해결 방법 |
|------|------------|
| Jenkins 기반 자동 배포 (자원 소모, 유지보수 부담) | GitHub Actions로 전환하여 경량화된 CI/CD 환경 구축 |

<br /><br />

## 🖼프로젝트 프리뷰

