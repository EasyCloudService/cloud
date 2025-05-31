import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class EasyCloudUpdater {

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.sleep(500);

        var mainPath = Path.of("").toAbsolutePath().getParent().getParent();
        Files.copy(mainPath.resolve("tmp-loader.jar"), mainPath.resolve("easycloud-loader.jar"), StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(mainPath.resolve("tmp-loader.jar"));
    }
}
