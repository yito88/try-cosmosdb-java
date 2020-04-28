# Try CosmosDB (Core API) with Java SDK
- Conditional insertion with stored procedure
  - Insert a document if it doesn't exist
- Conditional update with stored procedure
  - Update a document if it exists

## Run small benchmark
- Build
```
$ ./gradlew installDist
```

- Run
```
$ ./build/install/tcj/bin/tcj -h <COSMOS_DB_URI> -k <MASTER_KEY> -d <DABABASE_NAME> -c <CONTAINER_NAME> -sp ./stored-procedure/
```
