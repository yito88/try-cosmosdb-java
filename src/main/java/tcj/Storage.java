package tcj;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosStoredProcedure;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import java.net.HttpURLConnection;

public class Storage implements AutoCloseable {
  private String databaseId;
  private String containerId;
  private String storedProcedureDir;
  private CosmosClient client;
  private CosmosContainer container;

  public Storage(String host, String masterKey) {
    this.client =
        new CosmosClientBuilder()
            .endpoint(host)
            .key(masterKey)
            .directMode()
            .consistencyLevel(ConsistencyLevel.STRONG)
            .buildClient();
  }

  public void close() {
    client.close();
  }

  public void with(String database, String container) {
    this.databaseId = database;
    this.containerId = container;
    this.container = client.getDatabase(database).getContainer(container);
  }

  public String getDatabaseLink() {
    return String.format("/dbs/%s", databaseId);
  }

  public String getContainerLink() {
    return String.format("%s/colls/%s", getDatabaseLink(), containerId);
  }

  public void registerStoredProcedure(CosmosStoredProcedureProperties sp) {
    try {
      container.getScripts().createStoredProcedure(sp, new CosmosStoredProcedureRequestOptions());
    } catch (CosmosClientException e) {
      throw new RuntimeException("Failed to register stored procedures", e);
    }
  }

  public boolean isRegistered(String id) {
    try {
      CosmosStoredProcedure sp = getStoredProcedure(id);
    } catch (CosmosClientException e) {
      if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        return false;
      }
      throw new RuntimeException("Failed to check a stored procedure", e);
    }

    return true;
  }

  private CosmosStoredProcedure getStoredProcedure(String id) throws CosmosClientException {
    return container.getScripts().getStoredProcedure(id);
  }

  public void insert(Record rec) throws CosmosClientException {
    try {
      container.createItem(rec);
    } catch (CosmosClientException e) {
      // System.err.println("Insertion failed");
      throw e;
    }
  }

  public void insertWithStoredProcedure(Record rec) throws CosmosClientException {
    executeStoredProcedure("putWithoutRead.js", rec);
  }

  public void insertIfNotExists(Record rec) throws CosmosClientException {
    executeStoredProcedure("putIfNotExists.js", rec);
  }

  public void updateIfExists(Record rec) throws CosmosClientException {
    executeStoredProcedure("putIfExists.js", rec);
  }

  private void executeStoredProcedure(String id, Record rec) throws CosmosClientException {
    CosmosStoredProcedure sp = getStoredProcedure(id);
    CosmosStoredProcedureRequestOptions requestOptions = new CosmosStoredProcedureRequestOptions();
    requestOptions.setPartitionKey(new PartitionKey(rec.getKey1()));

    sp.execute(new Record[] {rec}, requestOptions);
  }

  public void get(String key) {
    String query =
        String.format("SELECT r.key1, r.key2, r.values FROM Record r WHERE r.key1 = '%s'", key);
    FeedOptions options = new FeedOptions().setPartitionKey(new PartitionKey(key));
    CosmosPagedIterable<Record> results = container.queryItems(query, options, Record.class);

    results.forEach(r -> System.out.println(String.format("\tRead %s", r.readColumns())));
  }
}
