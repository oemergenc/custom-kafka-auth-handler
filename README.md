# Custom Kafka Auth Handlers

Project demonstrating the usage of a custom kafka authorization handler.

## Usage

```
./mvnw clean verify
docker-compose up -d

#Succesfull list
kafkacat -b localhost:9092 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanisms=PLAIN -X sasl.username=test -X sasl.password=testpw -L

#Failure list
kafkacat -b localhost:9092 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanisms=PLAIN -X sasl.username=test -X sasl.password=test123 -L
```

## Release

Pushing changes directly on the remote `main` branch or merging feature branches will trigger the release ci pipeline on
github and publish a new release.

```
git push origin main
```
