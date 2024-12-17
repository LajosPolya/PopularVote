# PopularVote API

Allows the creation of Popular Vote resources.

> [!IMPORTANT]  
> This API uses K2 annotation processor as defined in [gradle.properties](gradle.properties)

> [!NOTE]  
> This project uses Spring Data R2DBC which doesn't support entity relationships via @OneToOne, @OneToMany, 
> and @ManyToOne, therefore, such relationships are managed by the application code.

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

docker build -t popilar-vote-api:latest ./

docker run -p 8080:8080 --name popilar-vote-api popilar-vote-api:latest

# call the API via `curl`
curl localhost:8080/health -v
```
