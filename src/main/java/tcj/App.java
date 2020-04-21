package tcj;

public class App {
  public static void main(String[] args) {
    String host = "localhost";
    String masterKey = "password";
    String database = "database";
    String container = "container";
    String storedProcedureDir = "/path/to/stored-procedure";

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
      storage.with(database, container);

      System.out.println("Register stored procedures...");
      StoredProcedureHandler spHandler = new StoredProcedureHandler(storage);
      spHandler.registerAll(storedProcedureDir);

      Record rec = new Record("0", "0");
      rec.withValue("v1", 100);
      rec.withValue("v2", 200);
      storage.insert(rec);

      storage.get("0");

      rec = new Record("0", "1");
      rec.withValue("v1", 101);
      rec.withValue("v2", 202);
      storage.insert(rec);

      storage.get("0");

      rec = new Record("0", "0");
      rec.withValue("v1", 111);
      rec.withValue("v2", 222);
      storage.insertIfNotExists(rec); // this will fail

      rec = new Record("1", "0");
      rec.withValue("v1", 100);
      rec.withValue("v2", 200);
      storage.insertIfNotExists(rec);

      rec = new Record("1", "1");
      rec.withValue("v1", 111);
      rec.withValue("v2", 222);
      storage.insertIfNotExists(rec);

      storage.get("0");
      storage.get("1");

      rec = new Record("0", "2");
      rec.withValue("v1", 111);
      rec.withValue("v2", 222);
      storage.updateIfExists(rec); // this will fail

      rec = new Record("0", "0");
      rec.withValue("v1", 1111);
      rec.withValue("v2", 2222);
      storage.updateIfExists(rec);

      storage.get("0");
      storage.get("1");
    }
  }
}
