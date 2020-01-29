# Raspberrypi-hashicorp-demo

<kbd>
  <img src="https://github-image-tkaburagi.s3-ap-northeast-1.amazonaws.com/rpi-demo/rpi-1.png">
</kbd>

## Pre-requisite

* Raspberry Pi Model4 4GB RAM version and Micro SSD for RPi Storage.
* Latest Raspbian OS.
* RPi enables ssh and vnc. VNC is for the demo.
* RPi hosts a Consul Sidecar and a pic-encryptor app.
* RPi need to have a camera module.
* Local PC(MAC) hosts a Consul Sidecar, face-bootifier app, Consul Server, Vault Server, Prometheus and Grafana.
* SLACK Token for sending direct messages to you

## Installation

### MAC
* Download Vault from [here](https://www.vaultproject.io/downloads/)
* Download Consul from [here](https://www.consul.io/downloads.html)
* Set the arbitrary directory as `$HOME_DIR` 
* `git clone`this repo at `${HOME_DIR}`
* If you use [auto-unseal](https://www.vaultproject.io/docs/configuration/seal/awskms/), set the 3 environmets
	* AWS_SECRET_ACCESS_KEY
	* VAULT_AWSKMS_SEAL_KEY_ID
	* AWS_ACCESS_KEY_ID
* After Setting up above,
	* `export MAC=<MAC's IP>`
	* `export PI=<PI'S IP>`
	* `./run.sh`

This will fail to start Java but Vault and Consul will run. And let's create the key on Vault with this command to create the encryption key, `vault write transit/encrypt/springdemo`.

And stop all processes by `./pkill.sh`.

### RPi
* Download Vault from [here](https://www.vaultproject.io/downloads/)
* Download Consul from [here](https://www.consul.io/downloads.html)
	* After downloading, `unzip`, `chmod +x ***`and put the binary to the bash path.
* Set the arbitrary directory as `$HOME_DIR` 
* mkdir `/home/pi/pics/`
* Install the [Java](https://www.oracle.com/technetwork/java/javase/downloads/jdk12-downloads-5295953.html) to run pic-encryptor app
* `git clone`this repo at `${HOME_DIR}`
* At the `${HOME_DIR}/raspberrypi-hashicorp-demo/pic-handler` dir, `./mvnw clean package -DskipTests`
* Put the `run-pi.sh` file in this repository.

## Run
* Hit the `./run.sh` on Mac
* Hit the `run-pi.sh` on RPi
* Import the Grafana Dashboad at `http://localhost:3000/`

## Demo 
1. Take the Picture of your(or your friends') face!
	* `raspistill -o /home/pi/pics/face.jpg -t 20`
2. Make sure there is a photo at the `/home/pi/pics/face.jpg` on the RPi
3. Hit the api, `curl ${PI}:8080/api/v1/encrypt-pic`
4. Make sure there is a encrypted photo at the `/home/pi/pics/face-encrpted.jpg` on the RPi
	* if you see the file by `more`, you can see the `vault:v1:` prefix this shows the photo has been encrypted by Vault.
5. Hit the api, `curl ${PI}:8080/api/v1/upstream-pic`
	* This Upstream the encrypted photo to Mac and hit the `face-bootifier` api.
	* As you can see the source codes, `face-bootifier` decrypt the photo -> detect the face -> then bootify each face -> send the bootified photo to the Slack. 
6. You can see Grafana Dashboard, `http://localhost:3000/`