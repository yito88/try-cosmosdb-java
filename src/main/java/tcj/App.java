package tcj;

import com.azure.cosmos.CosmosClientException;
import java.util.UUID;

public class App {
  private static final int NUM_RECORDS = 10000;
  private static String host = "localhost";
  private static String masterKey = "password";
  private static String database = "database";
  private static String container = "container";
  private static String storedProcedureDir = "/path/to/stored-procedure";

  public static void main(String[] args) {

    for (int i = 0; i < args.length; ++i) {
      if ("-h".equals(args[i])) {
        host = args[++i];
      } else if ("-k".equals(args[i])) {
        masterKey = args[++i];
      } else if ("-d".equals(args[i])) {
        database = args[++i];
      } else if ("-c".equals(args[i])) {
        container = args[++i];
      } else if ("-sp".equals(args[i])) {
        storedProcedureDir = args[++i];
      }
    }

    try (Storage storage = new Storage(host, masterKey)) {
      // Preparation
      storage.with(database, container);
      System.out.println("Register stored procedures...");
      StoredProcedureHandler spHandler = new StoredProcedureHandler(storage);
      spHandler.registerAll(storedProcedureDir);

      System.out.println("Start testing...");
      smallTest(storage);

      // Benchmark bench = new Benchmark(storage);
      // bench.runInsertion(100);
      // bench.runInsertionWithStoredProcedure(100);
      // bench.runInsertionIfNotExists(100);
      // bench.runUpdateIfExists(100);
    }
  }

  private static void smallTest(Storage storage) {

    Record rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k0");
    rec.setKey2("0");
    rec.withValue("v1", 100);
    rec.withValue("v2", 200);
    try {
      storage.insert(rec);
    } catch (CosmosClientException e) {
      System.err.println("Insertion failed unexpectedly");
      e.printStackTrace();
    }

    storage.get("k0");

    rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k0");
    rec.setKey2("1");
    rec.withValue("v1", 101);
    rec.withValue("v2", 202);
    try {
      storage.insert(rec);
    } catch (CosmosClientException e) {
      System.err.println("Insertion failed unexpectedly");
      e.printStackTrace();
    }

    storage.get("k0");

    rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k0");
    rec.setKey2("0");
    rec.withValue("v1", 111);
    rec.withValue("v2", 222);
    try {
      storage.insertIfNotExists(rec);
    } catch (CosmosClientException e) {
      System.err.println("Insertion failed as expected");
    }

    rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k1");
    rec.setKey2("0");
    rec.withValue("v1", 100);
    rec.withValue("v2", 200);
    try {
      storage.insertIfNotExists(rec);
    } catch (CosmosClientException e) {
      System.err.println("Insertion failed unexpectedly");
      e.printStackTrace();
    }

    rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k1");
    rec.setKey2("1");
    rec.withValue("v1", 111);
    rec.withValue("v2", 222);
    try {
      storage.insertIfNotExists(rec);
    } catch (CosmosClientException e) {
      System.err.println("Insertion failed unexpectedly");
      e.printStackTrace();
    }

    storage.get("k0");
    storage.get("k1");

    rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k0");
    rec.setKey2("2");
    rec.withValue("v1", 111);
    rec.withValue("v2", 222);
    try {
      storage.updateIfExists(rec);
    } catch (CosmosClientException e) {
      System.err.println("Update failed as expected");
    }

    rec = new Record();
    rec.setId(UUID.randomUUID().toString());
    rec.setKey1("k0");
    rec.setKey2("0");
    rec.withValue("v1", 1111);
    rec.withValue("v2", 2222);
    try {
      storage.updateIfExists(rec);
    } catch (CosmosClientException e) {
      System.err.println("Insertion failed unexpectedly");
      e.printStackTrace();
    }

    storage.get("k0");
    storage.get("k1");
  }
}
