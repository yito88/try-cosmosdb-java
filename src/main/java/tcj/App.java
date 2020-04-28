package tcj;

import com.microsoft.azure.documentdb.DocumentClientException;

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

      //smallTest(storage);

      Benchmark bench = new Benchmark(storage);
      bench.runInsertion(100);
      bench.runInsertionWithStoredProcedure(100);
      bench.runInsertionIfNotExists(100);
      bench.runUpdateIfExists(100);
    }
  }

  private static void smallTest(Storage storage) {

    Record rec = new Record("k0", "0");
    rec.withValue("v1", 100);
    rec.withValue("v2", 200);
    try {
      storage.insert(rec);
    } catch (DocumentClientException e) {
      System.err.println("Insertion failed unexpectedly");
    }

    storage.get("k0");

    rec = new Record("k0", "1");
    rec.withValue("v1", 101);
    rec.withValue("v2", 202);
    try {
      storage.insert(rec);
    } catch (DocumentClientException e) {
      System.err.println("Insertion failed unexpectedly");
    }

    storage.get("k0");

    rec = new Record("k0", "0");
    rec.withValue("v1", 111);
    rec.withValue("v2", 222);
    try {
      storage.insertIfNotExists(rec);
    } catch (DocumentClientException e) {
      System.err.println("Insertion failed as expected");
    }

    rec = new Record("k1", "0");
    rec.withValue("v1", 100);
    rec.withValue("v2", 200);
    try {
      storage.insertIfNotExists(rec);
    } catch (DocumentClientException e) {
      System.err.println("Insertion failed unexpectedly");
    }

    rec = new Record("k1", "1");
    rec.withValue("v1", 111);
    rec.withValue("v2", 222);
    try {
      storage.insertIfNotExists(rec);
    } catch (DocumentClientException e) {
      System.err.println("Insertion failed unexpectedly");
    }

    storage.get("k0");
    storage.get("k1");

    rec = new Record("k0", "2");
    rec.withValue("v1", 111);
    rec.withValue("v2", 222);
    try {
      storage.updateIfExists(rec);
    } catch (DocumentClientException e) {
      System.err.println("Update failed as expected");
    }

    rec = new Record("k0", "0");
    rec.withValue("v1", 1111);
    rec.withValue("v2", 2222);
    try {
      storage.updateIfExists(rec);
    } catch (DocumentClientException e) {
      System.err.println("Insertion failed unexpectedly");
    }

    storage.get("k0");
    storage.get("k1");
  }
}
