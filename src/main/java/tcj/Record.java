package tcj;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Record {
  private String id;
  private String key1;
  private String key2;
  private Map<String, Integer> values = new HashMap<>();

  public Record() {
    this.id = UUID.randomUUID().toString();
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setKey1(String key1) {
    this.key1 = key1;
  }

  public void setKey2(String key2) {
    this.key2 = key1;
  }

  public void setValues(Map<String, Integer> values) {
    this.values = values;
  }

  public void withValue(String name, int value) {
    values.put(name, value);
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
}
