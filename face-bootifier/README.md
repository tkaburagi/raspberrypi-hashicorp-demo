# Face Bootifier


```
base64 -i ./lena.png |  curl -v localhost:8080/bootifier -H "Content-Type: text/plain" -d @- | base64 -D >  after.png
```

![image](https://user-images.githubusercontent.com/106908/46780887-cd015480-cd59-11e8-91af-47d38ccee414.png)

## Deploy to Cloud Foundry

```
./mvnw clean package -DskipTests=true
cf push
```

```
base64 -i ./lena.png |  curl -v https://face-bootifier.cfapps.io/bootifier -H "Content-Type: text/plain" -d @- | base64 -D >  after.png
```

## Deploy to Riff

tested with riff 0.1.3

```
export DOCKER_USERNAME=<your docker username>

riff function create java face-bootifier \
  --git-repo https://github.com/making/face-bootifier.git \
  --image ${DOCKER_USERNAME}/face-bootifier \
  --verbose
```

```
base64 -i ./lena.png | riff service invoke face-bootifier --text -- -d @- | awk 'NR>1 {print}' | base64 -D >  after.png
```