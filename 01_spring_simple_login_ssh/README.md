# HTTPS(TSL) 적용 예제

# 1. localhost 인증서 설치 (window)
# powershell 관리자 권한으로 실행할것!
# pwd : changeit
cd dev
choco install mkcert -y
mkcert -install
mkcert -pkcs12 localhost 127.0.0.1 ::1

# 1. localhost 인증서 설치 (mac)
# pwd : changeit
brew install mkcert
mkcert -install
mkcert -pkcs12 localhost 127.0.0.1 ::1

# 2. 인증서 복사
# src/main/resources/ssl/localhost.p12

# 3. https로 접근하기
# https://localhost:8080/