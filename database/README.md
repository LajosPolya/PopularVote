# Flyway Database Migrations
This project uses Flyway to perform database migrations.

> [!IMPORTANT]
> Docker must be installed to run the migrations

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
docker run -p 3306:3306 --name local-popular-vote-test-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=popular-vote -d mysql:8.1
# It may take a few seconds before the database is initialized and ready to accept connections
```
5. Build and run the migration
```console
docker build -t local-popular-vote-migration .
docker run --net=host local-popular-vote-migration
```
