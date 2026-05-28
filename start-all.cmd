@echo off
title Levantando Ecosistema GymFlow Portable

echo ===================================================
echo [1/11] Iniciando Servidor de Descubrimiento Eureka...
echo ===================================================
start "MS-EUREKA [8761]" cmd /k "cd eureka-server && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 12

echo ===================================================
echo [2/11] Iniciando User Service...
echo ===================================================
start "MS-USER [8080]" cmd /k "cd user-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [3/11] Iniciando Branch Service...
echo ===================================================
start "MS-BRANCH [8081]" cmd /k "cd branch-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [4/11] Iniciando Membership Service...
echo ===================================================
start "MS-MEMBERSHIP [8083]" cmd /k "cd membership-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [5/11] Iniciando Access Service...
echo ===================================================
start "MS-ACCESS [8084]" cmd /k "cd access-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [6/11] Iniciando QR Generator Service...
echo ===================================================
start "MS-QR [8085]" cmd /k "cd qr-generator-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [7/11] Iniciando Capacity Service...
echo ===================================================
start "MS-CAPACITY [8086]" cmd /k "cd capacity-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [8/11] Iniciando Class Service...
echo ===================================================
start "MS-CLASS [8087]" cmd /k "cd class-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [9/11] Iniciando Routine Service...
echo ===================================================
start "MS-ROUTINE [8088]" cmd /k "cd routine-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [10/11] Iniciando Equipment Service...
echo ===================================================
start "MS-EQUIPMENT [8089]" cmd /k "cd equipment-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo [11/11] Iniciando Notification Service...
echo ===================================================
start "MS-NOTIFICATION [8090]" cmd /k "cd notification-service && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

echo ===================================================
echo Ecosistema GymFlow desplegado de forma portable.
echo ===================================================
pause