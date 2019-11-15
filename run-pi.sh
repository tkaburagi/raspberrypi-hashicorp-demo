#!/bin/sh

echo "### Setting Values ..."
sleep 10

if [ $MAC = ""]; then
	echo "No value MAC"
	exit 1
fi

if [ $PI = ""]; then
	echo "No value PI"
	exit 1
fi

export VAULT_HOST=${MAC}
export VAULT_TOKEN=s.M8D76JOdWrjd81we2CTnj8Zw

echo "### Killing ..."
sleep 10

pkill consul
pkill vault
pkill java

cat << EOF > /home/pi/demo/consul-config/config.json
{
  "service": {
    "name": "pic-encrypter",
    "address": "$PI",
    "tags": ["spring-boot"],
    "port": 8080,
    "check": {
      "id": "pic-encrypter-actuator",
      "name": "Spring Actuator Health",
      "http": "http://${PI}:8080/actuator/health",
      "interval": "10s"
    },
    "connect": {
      "sidecar_service": {
        "port": 20001,
        "tags": ["sidecar"],
        "check": {
          "name": "Connect Envoy Sidecar",
          "tcp": "$PI:20001",
          "interval": "10s"
        },
        "proxy": {
          "upstreams": [
            {
             "destination_name": "face-bootifier",
             "local_bind_address": "127.0.0.1",
             "local_bind_port": 5000
           }
         ]
        }
      }
    }
  }
}
EOF

echo "### Starting Consul Agent ..."
sleep 10

consul agent -client=$PI -data-dir=/home/pi/demo/consul-data -join=$MAC -config-dir=/home/pi/demo/consul-config &

echo "### Starting Consul Sidecar ..."
sleep 10

CONSUL_HTTP_ADDR=$PI:8500 consul connect proxy -sidecar-for pic-encrypter &

echo "### Starting Java ..."
sleep 10

java -jar /home/pi/demo/demo-0.0.1-SNAPSHOT.jar  &