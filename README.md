# Custom Kafka Auth Handlers

TBD

## Usage

```
./mvnw clean verify
docker-compose up -d

#Succesfull list
kafkacat -b localhost:9092 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanisms=PLAIN -X sasl.username=test -X sasl.password=testpw -L

#Failure list
kafkacat -b localhost:9092 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanisms=PLAIN -X sasl.username=test -X sasl.password=test123 -L
```
