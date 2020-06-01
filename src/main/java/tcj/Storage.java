package tcj;

import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.sync.CosmosSyncClient;
import com.azure.data.cosmos.sync.CosmosSyncContainer;
import com.azure.data.cosmos.sync.CosmosSyncStoredProcedure;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.io.IOException;

public class Storage implements AutoCloseable {
  private String databaseId;
  private String containerId;
  private CosmosSyncClient client;
  private CosmosSyncContainer container;

  public Storage(String host, String masterKey) {
    ConnectionPolicy connectionPolicy = new ConnectionPolicy();
    connectionPolicy.connectionMode(ConnectionMode.DIRECT);
    this.client =
        CosmosClient.builder()
            .endpoint(host)
            .key(masterKey)
            .consistencyLevel(ConsistencyLevel.STRONG)
            .buildSyncClient();
  }

  public void close() {
    client.close();
  }

  public void with(String database, String container) {
    this.databaseId = database;
    this.containerId = container;
    this.container = client.getDatabase(databaseId).getContainer(containerId);
  }

  public String getDatabaseLink() {
    return String.format("/dbs/%s", databaseId);
  }

  public String getContainerLink() {
    return String.format("%s/colls/%s", getDatabaseLink(), containerId);
  }

  public void registerStoredProcedure(CosmosStoredProcedureProperties sp) {
    try {
      container.getScripts().createStoredProcedure(sp);
    } catch (CosmosClientException e) {
      throw new RuntimeException("Failed to register stored procedures", e);
    }
  }

  public boolean isRegistered(String id) {
    CosmosSyncStoredProcedure sp = container.getScripts().getStoredProcedure(id);
    if (sp == null) {
      return false;
    }

    return true;
  }

  public void insert(Record rec) throws CosmosClientException {
    container.createItem(rec);
  }

  public void insertWithStoredProcedure(Record rec) throws CosmosClientException {
    upsertWithStoredProcedure("putWithoutRead.js", rec);
  }

  public void insertIfNotExists(Record rec) throws CosmosClientException {
    upsertWithStoredProcedure("putIfNotExists.js", rec);
  }

  public void updateIfExists(Record rec) throws CosmosClientException {
    upsertWithStoredProcedure("putIfExists.js", rec);
  }

  private void upsertWithStoredProcedure(String id, Record rec) throws CosmosClientException {
    CosmosSyncStoredProcedure sp = container.getScripts().getStoredProcedure(id);
    CosmosStoredProcedureRequestOptions requestOptions =
        new CosmosStoredProcedureRequestOptions().partitionKey(new PartitionKey(rec.getKey1()));

    sp.execute(new Record[] {rec}, requestOptions);
  }

  public void get(String key) {
    String query =
        String.format("SELECT r.key1, r.key2, r.values FROM Record r WHERE r.key1 = '%s'", key);
    Iterator<FeedResponse<CosmosItemProperties>> results = container.queryItems(query, null);

    results.forEachRemaining(
        rs -> {
          for (CosmosItemProperties r : rs.results()) {
            try {
              System.out.println(String.format("\tRead %s", r));
            } catch (Exception e) {
              System.err.println("Failed to deserialize: " + e.getMessage());
            }
          }
        });
  }
}
