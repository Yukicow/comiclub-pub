#  코믹클럽 ( Comiclub )

#### 개발기간 : 2023-01 ~ 2023-10

#### 배포 ULR :https://www.comiclub.co.kr/

<br>


### 📖 웹 만화 연재 서비스

<img src="https://github.com/Yukicow/comiclub-pub/assets/106314016/660b9857-5e4b-4759-a478-bb3d5e1c77c4" />

#### 1. 이미지를 편집하여 만화를 연재하세요! 
코믹클럽은 <strong>`이미지 편집기`</strong>를 제공하여 자신의 이미지에 여러가지 요소를 추가할 수 있습니다. <br>
내가 만든 만화를 업로드하거나 다른 작품을 각색하여 재미있는 작품을 만들어 주세요.
<br>

#### 2. 오디오를 추가하세요! 
<strong>`효과음`</strong>이나 <strong>`배경음악`</strong>을 삽입하여 역동적인 오디오 만화를 만들 수 있습니다. <br>
코믹클럽은 <strong>`오디오 편집기`</strong>를 제공하여 만화에 필요한 오디오 요소를 직접 편집할 수 있습니다!
<br>

#### 3. 만화를 자동 재생하세요! 
레이어 전환 시간을 설정하면 만화를 <strong>`자동 재생`</strong>할 수 있는 기능이 추가됩니다!  <br>
마치 영상을 보는 듯한 생생한 오디오 만화를 감상하실 수 있습니다.
<br>

#### 4. 사람들과 소통해 보세요! 
<strong>`클럽`</strong>에서는 자신이 감상한 작품이나 특정 주제를 가지고 자유롭게 소통할 수 있습니다.<br>
자신의 작품을 여러 사람에게 알릴 수 있는 기회가 될 수 있습니다.

<br>
<br>

# ⚙ 사용 기술
- Backend: `Java 17` `Spring Boot 3.0.4` `Spring Security` `Spring Data JPA` `QueryDsl`
- Frontend: `Javascript(TypeScript)`
- DB: `MariaDB`
- Server: `AWS EC2`
- Tools: `Intellij IDEA`
<br>



<br>

# ERD

<img src="https://github.com/Yukicow/comiclub-pub/assets/106314016/cfd576d6-9a02-4dd8-9cd4-fdfa0b25b160" />

<br>
<br>
<br>
<br>


# 💡 트러블 슈팅

|Problem| Canvas API에서 S3 이미지를 불러올 경우 CORS가 발생 <a href="https://velog.io/@yukicow/S3%EC%97%90%EC%84%9C-%EB%B0%9B%EC%95%84%EC%98%A8-%EC%9D%B4%EB%AF%B8%EC%A7%80%EB%A5%BC-Canvas-API%EC%97%90%EC%84%9C-%EC%82%AC%EC%9A%A9%ED%95%A0-%EA%B2%BD%EC%9A%B0-%EB%B0%9C%EC%83%9D%ED%95%98%EB%8A%94-CORS-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0-%EB%B0%A9%EB%B2%95">[ 링크 ]</a>|
|:-----------:|-----------------------------------------------------|
|해결| S3는 요청에 `Origin` 속성이 빠져 있으면 `access-control-allow-origin`을 응답에 담지 않는 문제가 있기 때문에 image 오브젝트에 `crossOrigin` 속성을 `anonymous`로 설정하여 이미지 요청 시에 항상 `Origin`속성이 헤더에 담기도록 하여 해결. <br><br> 그러나 Canvas API는 받아온 이미지를 `canvas`로 재구성하는 과정에서 한번 내려받은 이미지를 재사용하는데, 다른 페이지에서 캐시된 이미지들은 응답에 `access-control-allow-origin`이 없어 `cors` 정책을 위반한다고 브라우저가 판단하여 2차적으로 문제가 발생함. <br><br> 모든 이미지 요청에 `crossOrigin` 속성을 추가하여 해결|
<br>
<br>

|Problem| 오디오 편집에 메모리를 과하게 사용함 |
|:-----------:|-----------------------------------------------------|
|해결|오디오 버퍼는 원본만 저장하고 편집이 발생할 경우 오디오의 메타 정보를 변경하도록 함. 원본 오디오 버퍼와 메타 정보를 활용하여 새로운 오디오 버퍼를 생성하는 방식으로 변경하여 해결. <br><br> 버퍼를 추가적으로 저장하지 않아 메모리 효율이 크게 향상됨. ( 최대 N 배 ) |
<br>
<br>

|Problem|오디오 파장 출력 속도가 너무 느린 문제 |
|:-----------:|-----------------------------------------------------|
|해결| Canvas API에서 좌표 설정에 실수를 사용할 경우 앤티 앨리어싱(anti-aliasing) 처리를 위한 추가적인 연산을 발생시켜 GPU 사용 시간이 급격하게 늘어나는 것이었음.  <br><br> 44100Hz의 오디오 raw 데이터를 나누는 블록 단위를 조정하여 초당 출력될 파장의 수를 줄이고 실수 대신 정수를 사용하여 오버헤드를 줄여 해결. |
<br>
<br>

|Problem| 파일 업로드와 관련된 비즈니스 로직 처리 중 오류 발생 시 이미지가 영구 저장됨 |
|:-----------:|-----------------------------------------------------|
|해결| 비즈니스 로직 내에서 직접 Flush를 사용하고 Exception 발생 시 이미지를 다시 삭제하도록 트랜잭션을 관리하여 해결|
