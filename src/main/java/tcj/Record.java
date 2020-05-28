package tcj;

import java.util.HashMap;
import java.util.Map;

public class Record {
  private String id;
  private String key1;
  private String key2;
  private Map<String, Integer> values = new HashMap<>();

  public Record() {
  }

  public void withValue(String name, int value) {
    values.put(name, value);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setKey1(String key1) {
    this.key1 = key1;
  }

  public void setKey2(String key2) {
    this.key2 = key2;
  }

  public void setValues(Map<String, Integer> values) {
    this.values = values;
  }

  public String getId() {
    return id;
  }

  public String getKey1() {
    return key1;
  }

  public String getKey2() {
    return key2;
  }

  public Map<String, Integer> getValues() {
    return values;
  }

  public String readColumns() {
    return String.format("id: %s, key1: %s, key2: %s, values: %s", id, key1, key2, values);
  }
}
