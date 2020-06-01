package tcj;

import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class StoredProcedureHandler {
  private final Storage storage;

  public StoredProcedureHandler(Storage storage) {
    this.storage = storage;
  }

  public void registerAll(String storedProcedureDir) {
    try {
      Files.list(Paths.get(storedProcedureDir))
          .forEach(
              p -> {
                String name = p.toFile().getName();
                CosmosStoredProcedureProperties sp = makeStoredProcedure(name, p);

                if (storage.isRegistered(name)) {
                  return;
                }

                storage.registerStoredProcedure(sp);
              });
    } catch (IOException e) {
      throw new RuntimeException("The directory is not found", e);
    }
  }

  private CosmosStoredProcedureProperties makeStoredProcedure(String spName, Path path) {
    try {
      return new CosmosStoredProcedureProperties(
          spName, Files.lines(path, Charset.forName("UTF-8")).collect(Collectors.joining()));
    } catch (IOException e) {
      throw new RuntimeException("Stored procedure is not found", e);
    }
  }
}
