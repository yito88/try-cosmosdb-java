package tcj;

import java.util.HashMap;
import java.util.Map;

public class Record {
  private final String key1;
  private final String key2;
  private final Map<String, Integer> values = new HashMap<>();

  public Record(String key1, String key2) {
    this.key1 = key1;
    this.key2 = key2;
  }

  public void withValue(String name, int value) {
    values.put(name, value);
  }

  public String getId() {
    return key1;
  }

  public String getKey1() {
    return key1;
  }

  public String getKey2() {
    return key2;
  }

  public Map getValues() {
    return values;
  }
}
