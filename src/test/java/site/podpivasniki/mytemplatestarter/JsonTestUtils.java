package site.podpivasniki.mytemplatestarter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.nio.file.Files;
import org.springframework.util.ResourceUtils;

public final class JsonTestUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  public static Object getJsonFromPath(String path) {
    try {
      File expectedFile = ResourceUtils.getFile(path);
      String expectedJsonPath = new String(Files.readAllBytes(expectedFile.toPath()));
      return objectMapper.readTree(expectedJsonPath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String getJsonStringFromObject(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
