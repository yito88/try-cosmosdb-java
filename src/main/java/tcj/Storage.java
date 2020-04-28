package tcj;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.documentdb.PartitionKey;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.StoredProcedure;
import java.net.HttpURLConnection;

public class Storage implements AutoCloseable {
  private String database;
  private String container;
  private String storedProcedureDir;
  private DocumentClient client;

  public Storage(String host, String masterKey) {
    this.client =
        new DocumentClient(host, masterKey, ConnectionPolicy.GetDefault(), ConsistencyLevel.Strong);
  }

  public void close() {
    client.close();
  }

  public void with(String database, String container) {
    this.database = database;
    this.container = container;
  }

  public String getDatabaseLink() {
    return String.format("/dbs/%s", database);
  }

  public String getContainerLink() {
    return String.format("%s/colls/%s", getDatabaseLink(), container);
  }

  public void registerStoredProcedure(StoredProcedure sp) {
    try {
      client.createStoredProcedure(getContainerLink(), sp, new RequestOptions());
    } catch (DocumentClientException e) {
      throw new RuntimeException("Failed to register stored procedures", e);
    }
  }

  public boolean isRegistered(StoredProcedure sp) {
    String sprocLink = String.format("%s/sprocs/%s", getContainerLink(), sp.getId());

    try {
      StoredProcedure result = client.readStoredProcedure(sprocLink, null).getResource();
    } catch (DocumentClientException e) {
      if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        return false;
      }
      throw new RuntimeException("Failed to check a stored procedure", e);
    }

    return true;
  }

  public void insert(Record rec) throws DocumentClientException {
    try {
      client.createDocument(getContainerLink(), rec, new RequestOptions(), true);
    } catch (DocumentClientException e) {
      //System.err.println("Insertion failed");
      throw e;
    }
  }

  public void insertWithStoredProcedure(Record rec) throws DocumentClientException {
    String sprocLink = String.format("%s/sprocs/putWithoutRead.js", getContainerLink());

    RequestOptions requestOptions = new RequestOptions();
    requestOptions.setPartitionKey(new PartitionKey(rec.getKey1()));

    try {
      client.executeStoredProcedure(sprocLink, requestOptions, new Object[] {rec});
    } catch (DocumentClientException e) {
      //System.err.println("Insertion failed");
      throw e;
    }
  }

  public void insertIfNotExists(Record rec) throws DocumentClientException {
    String sprocLink = String.format("%s/sprocs/putIfNotExists.js", getContainerLink());

    RequestOptions requestOptions = new RequestOptions();
    requestOptions.setPartitionKey(new PartitionKey(rec.getKey1()));

    try {
      client.executeStoredProcedure(sprocLink, requestOptions, new Object[] {rec});
    } catch (DocumentClientException e) {
      //System.err.println("Insertion failed");
      throw e;
    }
  }

  public void updateIfExists(Record rec) throws DocumentClientException {
    String sprocLink = String.format("%s/sprocs/putIfExists.js", getContainerLink());

    RequestOptions requestOptions = new RequestOptions();
    requestOptions.setPartitionKey(new PartitionKey(rec.getKey1()));

    try {
      client.executeStoredProcedure(sprocLink, requestOptions, new Object[] {rec});
    } catch (DocumentClientException e) {
      //System.err.println("Update failed");
      throw e;
    }
  }

  public void get(String key) {
    String query =
        String.format("SELECT r.key1, r.key2, r.values FROM Record r WHERE r.key1 = '%s'", key);
    FeedResponse<Document> results = client.queryDocuments(getContainerLink(), query, null);

    for (Document doc : results.getQueryIterable()) {
      System.out.println(String.format("\tRead %s", doc));
    }
  }
}
