#!/bin/sh

echo "### Killing ..."

pkill consul
pkill vault
pkill java
pkill prometheus

echo "### Setting Values ..."
sleep 10

if [ $MAC = ""]; then
	echo "No value MAC address"
	exit 1
else
	echo "MAC LAN IP = " $MAC
fi

if [ $SLACK_TOKEN = ""]; then
	echo "No value SLACK_TOKEN"
	exit 1
fi

if [ $PI = ""]; then
  echo "No value PI address"
  exit 1
else
  echo "PI LAN IP = " $PI
fi

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

cat << EOF > /Users/kabu/prometheus/prometheus-template.yml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).
  external_labels:
    origin_prometheus: prometheus01
# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      # - alertmanager:9093

scrape_configs:
  - job_name: 'Consul Metrics'
    scrape_interval: 5s
    metrics_path: /v1/agent/metrics
    scheme: http
    params:
      format: ['prometheus']
    static_configs:   
    - targets: ['${MAC}:8500','${PI}:8500']
  - job_name: 'Consul Exporter'
    metrics_path: /metrics
    scheme: http
    static_configs:
      - targets:
        - 127.0.0.1:9107
EOF

echo "### Building App ..."
sleep 10

cd face-bootifier

./mvnw clean package -DskipTests

cd ..

export VAULT_HOST=${MAC}
export VAULT_TOKEN=s.M8D76JOdWrjd81we2CTnj8Zw
export SLACK_TOKEN=${SLACK_TOKEN}

echo ${SLACK_TOKEN}

echo "### Starting Consul ..."
sleep 10

consul agent -server -bind=0.0.0.0 \
-client=0.0.0.0 \
-data-dir=/Users/kabu/hashicorp/consul/raspberry-data \
-bootstrap-expect=1 -ui \
-config-dir=/Users/kabu/hashicorp/consul/pidemo &


echo "### Starting Vault ..."
sleep 10

source ~/hashicorp/vault/scripts/vault-kms-setup.sh
vault server -config ~/hashicorp/vault/configs/local-config-oss.hcl start &


consul connect proxy -sidecar-for face-bootifier &

echo "### Starting Java ..."
sleep 10

java -jar /Users/kabu/hashicorp/intellij/raspberrypi-hashicorp-demo/face-bootifier/target/face-bootifier-0.0.1-SNAPSHOT.jar &


echo "### Starting Consul Exporter"
sleep 10

/Users/kabu/hashicorp/github/consul_exporter/consul_exporter &

echo "### Starting Prometheus ..."
sleep 10

prometheus --config.file=/Users/kabu/prometheus/prometheus-template.yml &

echo "### Starting Grafana ..."

brew services restart grafana
