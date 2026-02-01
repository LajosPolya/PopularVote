FROM redgate/flyway:10

COPY sql/ /flyway/sql/

ENTRYPOINT [ "flyway", "-locations=filesystem:/flyway/sql", "migrate"]
