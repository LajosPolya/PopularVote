from redgate/flyway:10

copy sql/ /flyway/sql/

# This is for testing only! Never expose any password!
entrypoint ["flyway", "-user=root", "-password=my-secret-pw", "-url=jdbc:mysql://localhost:3306/popular-vote?allowPublicKeyRetrieval=true", "-locations=filesystem:/flyway/sql", "migrate"]
