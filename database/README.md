# Flyway Database Migrations
This project uses Flyway to perform database migrations.

> [!IMPORTANT]
> Docker must be installed and running to execute the migration

### Migrate
1. Run Docker
2. Pull the Flyway docker image:
```console
docker pull redgate/flyway:10
```
3. Pull the MySql docker image:
```console
# flyway 10 support up to MySQL 8.1
docker pull mysql:8.1
```
4. Run MySql locally
```console
# This is a test! Never expose any password!
docker run -p 3306:3306 --name popular-vote-mysql-test -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=popularVote -d mysql:8.1
# It may take a few seconds before the database is initialized and ready to accept connections
```
5. Build and run the migration
```console
# From the PopularVote/database directory
docker build -t local-popular-vote-migration .

# This is for testing only, never expose a password!
docker run \
--rm \
--name=popular-vote-migration \
--net=host \
--env FLYWAY_USER=root \
--env FLYWAY_PASSWORD=my-secret-pw \
--env FLYWAY_URL=jdbc:mysql://localhost:3306?allowPublicKeyRetrieval=true \
--env FLYWAY_SCHEMAS=popularVote \
local-popular-vote-migration \
migrate
```

If the migration fails with the following error `Caused by: java.sql.SQLNonTransientConnectionException: Socket fail to connect to host:localhost, port:3306. Connection refused`, the database is not initialized yet. Wait a few seconds and try again.
