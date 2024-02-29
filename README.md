# simple-httpd

python의 SimpleHTTPServer 모듈과 유사하게 구현해본다.

### 요구 사항
- Argument로 port를 받는다.
  - port가 주어지지 않을 경우, 80을 사용한다.
- 프로그램이 실행된 디렉토리를 document-root로 하는 웹서버로 동작한다.

- GET "/"
  - 현재 폴더 내부 목록 응답
- GET "/file-path"
  - 파일이 존재하면, 200 OK + 파일 컨텐츠 응답
  - 파일이 존재하지 않으면, 404 Not Found 응답
  - Content-Type, Content-Length 포함 응답
    
- document-root보다 상위 디렉토리를 요청하거나, 읽기 권한이 없는 파일을 요청하면 403 Forbidden 응답

- multipart/form-data 파일 업로드 구현
  - 저장 권한이 없으면, 403 Forbidden 응답
  - 같은 이름의 파일이 이미 있으면, 409 Conflict 응답
  - multipart/form-data 파일 업로드 외의 POST 요청이면, 405 Method Not Allowed 응답

- DELETE "/{file}"
  - 파일을 지울 수 있으면 지우고, 204 No Content 응답
  - URL에 지정된 파일이 존재하지 않으면, 204 No Content 응답
  - URL에 지정된 파일을 지울 수 없으면, 403 Forbidden 응답
