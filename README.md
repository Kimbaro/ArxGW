# Aerix G/W

![Untitled](Aerix%20G%20W%20a2a195e367ce40f69a54b45a0f77f062/Untitled.png)

# Introduction

AerixG/W는 다양한 센서데이터가 담긴 Payload 정보를 구독하고 ConnectorHub로 발행하기 위한 중간다리 역할을 한다. 여기서 ConnectorHub는 Kafka의 Connector-Source의 기능이 구현된 모듈이다.

사용자는 UI를 통해 간편하게 시스템정보에 접근하여 Gateway 정보를 변경/적용한다.

변경/적용하기 위한 Gateway 정보는 다음과 같이 Network, KeepAlived, MQTT, Certificates 가 이에 해당한다.  설정된 시스템정보를 기반으로 여러 보안정보와 이중화로 구성된 MQTT Broker가 동작되며 구독자와 발행자는 MQTT Broker을 통해 Data PipeLine을 구성한다

# Getting Started

## Step 1 — requirement

- `openjdk_version=11`
- `springboot_version=2.6.2`
- `kotlin_version=1.6.10`

## Step 2 — build & deploy

- 설치 및 배포에 대해서 참고하세요 [이중화 G/W](https://www.notion.so/G-W-7001c86f46a348c0b3b0e365dea0af71)

# API Reference

## /raspgw/cert

### GET

생성된 SSL 인증서 정보를 반환합니다.

```bash
GET http://localhost:3030/raspgw/cert
Accept: application/json
```

### POST

SSL 인증서 정보를 등록합니다.

```bash
POST http://localhost:3030/raspgw/cert
Accept: application/json

{
  "cacert": "example!@#example!@#example!@#example!@#example!@#example!@#",
  "devicecert": "example!@#example!@#example!@#example!@#example!@#example!@#",
  "devicekey": "example!@#example!@#example!@#example!@#example!@#example!@#"
}
```

## /raspgw/info

### GET

시스템, 디바이스 정보를 반환합니다.

```bash
GET http://localhost:3030/raspgw/info
Accept: application/json
```

## /raspgw/broker

### GET

MQTT 설정 정보를 반환합니다.

```bash
GET http://localhost:3030/raspgw/broker
Accept: application/json
```

### POST

MQTT 설정 정보를 새로 등록합니다.

```bash
POST http://localhost:3030/raspgw/broker
Accept: application/json

{
  "enable": false,
  "ip": "127.0.0.1",
  "port": 1883,
  "topic": "test",
  "anonymous": false,
  "ssl": false,
  "id": "admin",
  "password": "1234"
}

```

## /raspgw/network

### GET

시스템 네트워크 정보를 반환합니다.

```bash
GET http://localhost:3030/raspgw/network
Accept: application/json
```

### POST

시스템 네트워크 정보를 변경합니다.

```bash
POST http://localhost:3030/raspgw/network
Accept: application/json

{
  "dhcp": true,
  "ip": "127.0.0.1",
  "gateway": "127.0.0.1",
  "dns": "8.8.8.8",
  "subnet": "255.255.255.255"
}
```

## /raspgw/mode

### GET

Gateway 모드정보를 반환합니다. state 값이 true 인 경우 keepalived, false 인 경우 HAproxy 이며 HAproxy는 현재 미구현 상태입니다.

```bash
GET http://localhost:3030/raspgw/mode
Accept: application/json
```

### POST

Gateway 모드정보를 변경합니다. state 값이 true 인 경우 keepalived, false 인 경우 HAproxy 이며 HAproxy는 현재 미구현 상태입니다.

```bash
POST http://localhost:3030/raspgw/mode
Accept: application/json

{
  "mode": true,
  "vip": "127.0.0.1",
  "subnet": "127.0.0.1",
  "state": true
}
```
