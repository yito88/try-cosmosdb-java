package tcj;

import com.azure.data.cosmos.CosmosClientException;
import java.util.Random;

public class Benchmark {
  private final Storage storage;
  private final Random random;

  public Benchmark(Storage storage) {
    this.storage = storage;
    this.random = new Random(System.currentTimeMillis());
  }

  public void runInsertion(int runtime) {
    int cnt = 0;

    System.out.println("Inserting records...");
    long end = System.currentTimeMillis() + runtime * 1000;
    do {
      String key = "insert" + String.valueOf(random.nextInt());
      Record rec = new Record();
      rec.setKey1(key);
      rec.setKey2("0");
      rec.withValue("v1", 111);
      rec.withValue("v2", 222);

      try {
        storage.insert(rec);
        cnt++;
      } catch (CosmosClientException e) {
        // ignored
      }
    } while (System.currentTimeMillis() < end);

    System.out.println("insertion: " + (double) cnt / runtime + " ops");
  }

  public void runInsertionWithStoredProcedure(int runtime) {
    int cnt = 0;

    System.out.println("Inserting records with sotred procedure...");
    long end = System.currentTimeMillis() + runtime * 1000;
    do {
      String key = "insertWithSp" + String.valueOf(random.nextInt());
      Record rec = new Record();
      rec.setKey1(key);
      rec.setKey2("0");
      rec.withValue("v1", 111);
      rec.withValue("v2", 222);

      try {
        storage.insertWithStoredProcedure(rec);
        cnt++;
      } catch (CosmosClientException e) {
        // ignored
      }
    } while (System.currentTimeMillis() < end);

    System.out.println("Insertion with stored procedure: " + (double) cnt / runtime + " ops");
  }

  public void runInsertionIfNotExists(int runtime) {
    int cnt = 0;

    System.out.println("Inserting records if not exists...");
    long end = System.currentTimeMillis() + runtime * 1000;
    do {
      String key = "insertIfNotExists" + String.valueOf(random.nextInt());
      Record rec = new Record();
      rec.setKey1(key);
      rec.setKey2("0");
      rec.withValue("v1", 111);
      rec.withValue("v2", 222);

      try {
        storage.insertIfNotExists(rec);
        cnt++;
      } catch (CosmosClientException e) {
        // ignored
      }
    } while (System.currentTimeMillis() < end);

    System.out.println("Conditional insertion: " + (double) cnt / runtime + " ops");
  }

  public void runUpdateIfExists(int runtime) {
    int cnt = 0;

    System.out.println("Populating records...");
    for (int i = 0; i < 10000; i++) {
      try {
        String key = "update" + String.valueOf(i);
        Record rec = new Record();
        rec.setKey1(key);
        rec.setKey2("0");
        rec.withValue("v1", 111);
        rec.withValue("v2", 222);

        storage.insert(rec);
      } catch (CosmosClientException e) {
        // ignored
      }
    }

    System.out.println("Updating records if exists...");
    long end = System.currentTimeMillis() + runtime * 1000;
    do {
      String key = "update" + String.valueOf(random.nextInt(10000));
      Record rec = new Record();
      rec.setKey1(key);
      rec.setKey2("0");
      rec.withValue("v1", 1111);
      rec.withValue("v2", 2221);

      try {
        storage.updateIfExists(rec);
        cnt++;
      } catch (CosmosClientException e) {
        // ignored
      }
    } while (System.currentTimeMillis() < end);

    System.out.println("Conditional update: " + (double) cnt / runtime + " ops");
  }
}
