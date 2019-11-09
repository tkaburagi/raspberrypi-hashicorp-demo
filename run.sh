#!/bin/sh

echo "### Killing ..."

pkill consul
pkill vault
pkill java

sleep 10

echo "### Setting Values ..."

if [ $MAC = ""]; then
	echo "No value MAC"
	exit 1
else
	echo "MAC LAN IP = " $MAC
fi

if [ $SLACK_TOKEN = ""]; then
	echo "No value SLACK_TOKEN"
	exit 1
fi

sleep 10

cat << EOF > /Users/kabu/hashicorp/consul/pidemo/config.json
{
  "service": {
    "name": "face-bootifier",
    "address": "${MAC}",
    "tags": ["spring-boot"],
    "port": 8080,
     "check": {
      "id": "face-bootifier-actuator",
      "name": "Spring Actuator Health",
      "http": "http://${MAC}:8080/actuator/health",
      "interval": "10s"
    },
    "connect": {
      "sidecar_service": {
        "tags": ["sidecar"],
        "port": 20001,
        "check": {
          "name": "Connect Envoy Sidecar",
          "tcp": "${MAC}:20001",
          "interval": "10s"
        },
        "proxy": {
        }
      }
    }
  }
}
EOF

cd face-bootifier

./mvnw clean package -DskipTests

cd ..

export VAULT_HOST=${MAC}:8200
export VAULT_TOKEN=s.M8D76JOdWrjd81we2CTnj8Zw
export SLACK_TOKEN=${SLACK_TOKEN}

echo ${SLACK_TOKEN}

echo "### Starting Consul ..."

consul agent -server -bind=0.0.0.0 \
-client=0.0.0.0 \
-data-dir=/Users/kabu/hashicorp/consul/raspberry-data \
-bootstrap-expect=1 -ui \
-config-dir=/Users/kabu/hashicorp/consul/pidemo &

sleep 10

echo "### Starting Vault ..."

vault server -config ~/hashicorp/vault/configs/local-config-oss.hcl start &

sleep 10

consul connect proxy -sidecar-for face-bootifier &

sleep 10

echo "### Starting Java ..."

java -jar /Users/kabu/hashicorp/intellij/raspberrypi-hashicorp-demo/face-bootifier/target/face-bootifier-0.0.1-SNAPSHOT.jar &

sleep 10