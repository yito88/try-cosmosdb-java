package tcj;

import com.microsoft.azure.documentdb.StoredProcedure;
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
                StoredProcedure sp = makeStoredProcedure(name, p);

                if (storage.isRegistered(sp)) {
                  return;
                }

                storage.registerStoredProcedure(sp);
              });
    } catch (IOException e) {
      throw new RuntimeException("The directory is not found", e);
    }
  }

  private StoredProcedure makeStoredProcedure(String spName, Path path) {
    try {
      return new StoredProcedure(
          "{"
              + "  'id':'"
              + spName
              + "',"
              + "  'body':'"
              + Files.lines(path, Charset.forName("UTF-8")).collect(Collectors.joining())
              + "'}");
    } catch (IOException e) {
      throw new RuntimeException("Stored procedure is not found", e);
    }
  }
}
