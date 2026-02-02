# PopularVote API

Allows the creation of Popular Vote resources.

> [!IMPORTANT]
> This API uses K2 annotation processor as defined in [gradle.properties](gradle.properties)

> [!NOTE]
> This project uses Spring Data R2DBC which doesn't support entity relationships via @OneToOne, @OneToMany, 
> and @ManyToOne, therefore, such relationships are managed by the application code.

## Running the integration tests

The integration tests are located in the [src/test/kotlin/com/github/lajospolya/popularVote/controller package](src/test/kotlin/com/github/lajospolya/popularVote/controller).
They test simple use cases of the API, so they're a good place to start to get a good idead of how everything works.
They use `testcontainers` to start a database instance, as long as you have docker installed and running on your machine, everything should work fine.

> [!WARNING]
> I had to add the following config to the Docker Engine config because testcontainers doesn't support the latest version of Docker (v29 at time of writing) :
> ```json
> {
> ...
>   "min-api-version": "1.24"
> }
> ```
## Build the API
```shell
./gradlew build
```

## Build a Docker Image of the API
```shell
# as a prerequisite, install and run docker
./gradlew build
docker build -t <image_name>:<image_tag> ./
```

Where `image_name` is the name of the image and `image_tag` is a tag assigned to the image.


## Build and Run via Docker
```shell
# as a prerequisite, install and run docker
./gradlew build

docker build -t popular-vote-api:latest ./

docker run -p 8080:8080 --name popular-vote-api popular-vote-api:latest

# call the API via `curl`
curl localhost:8080/health -v
```


# Use the API

### Check if the application is running
```shell
curl localhost:8080/health -v
```

## The Citizen API

### Create a Citizen
```shell
curl -X POST http://localhost:8080/citizens \
     -H "Content-Type: application/json" \
     -d '{
           "givenName": "John",
           "surname": "Doe",
           "middleName": "Quincy"
         }'
``` 

### Fetch every Citizen managed by the application
```shell
curl http://localhost:8080/citizens
```

### Fetch the Citizen, which was just created
```shell
curl http://localhost:8080/citizens/1
```

## The Policy API

### Create a Policy
```shell
curl -X POST http://localhost:8080/policies \
     -H "Content-Type: application/json" \
     -d '{
           "description": "Should it be legally permissible for citizens to utilize AI-generated code within production environments?"
         }'
```

### Fetch every Policy managed by the application
```shell
curl http://localhost:8080/policies
```

### Fetch the Policy, which was just created
```shell
curl http://localhost:8080/policies/1
```

## The Opinion API

### Create an Opinion
```shell
curl -X POST http://localhost:8080/opinions \
     -H "Content-Type: application/json" \
     -d '{
           "politicalAffiliation": "LIBERAL_PARTY_OF_CANADA",
           "description": "Citizens should be allowed to utilize AI-generated code within production environments to reduce the risk of introducing bugs.",
           "author": "Jane Doe",
           "policyId": 1
         }'
```

### Fetch every Opinion managed by the application
```shell
curl http://localhost:8080/opinions
```

### Fetch the Opinion, which was just created
```shell
curl http://localhost:8080/opinions/1
```

## The Vote API

### Cast a Vote
```shell
curl -X POST http://localhost:8080/votes \
     -H "Content-Type: application/json" \
     -d '{
           "citizenId": 1,
           "policyId": 1,
           "selectionId": 1
         }'
```

### Poll the Votes for a Policy
```shell
curl http://localhost:8080/polls/1
```
