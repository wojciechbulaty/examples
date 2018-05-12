#!/usr/bin/env bash
PROTO_HOME=$HOME/opt/protobuf-3.5.1
$PROTO_HOME/bin/protoc --java_out=shop-backend/src/main/java/ shop-backend/src/main/resources/order-service.proto