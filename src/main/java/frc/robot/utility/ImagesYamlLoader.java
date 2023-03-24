package frc.robot.utility;

import edu.wpi.first.wpilibj.RobotBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import frc.robot.subsystems.MatrixLEDs;


public class ImagesYamlLoader extends HashMap<String, Mat> {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final List<String> imagesFilenames = List.of(
            "sad-face-frown.yaml",
            "traffic-cone-512.yaml",
            "purple-3d-cube-hi.yaml");

    public ImagesYamlLoader() {
        super();
        this.put("Row One", MatrixLEDs.oneRow(0));
        this.put("Row Two", MatrixLEDs.oneRow(1));
        this.put("Col One", MatrixLEDs.oneCol(0));
        this.put("Col Two", MatrixLEDs.oneCol(1));
        this.put("Eye", MatrixLEDs.eye());

        loadYamlFiles();
    }

    @Override
    public Mat put(String key, Mat value) {
        System.out.println("INFO: ImagesYamlLoader put");
        return super.put(key, value);
    }

    @Override
    public Mat get(Object key) {
        System.out.println("INFO: ImagesYamlLoader get");

        if (this.containsKey(key)) {
            return super.get(key);
        }
        return super.get("sad-face-frown");
    }

    private Path getDeployPath() {
        if (RobotBase.isReal()) {
            // Running on roboRIO
            return Paths.get("/home/lvuser/deploy");
        } else {
            // Running in simulator
            return Paths.get("src", "main", "deploy");
        }
    }

    private void loadYamlFiles() {
        Path deployFolderPath = getDeployPath();

        for (var imagesFilename: imagesFilenames) {
            Path imagePath = deployFolderPath.resolve(imagesFilename);
            Yaml yaml = new Yaml();
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(imagePath.toFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
            Map<String, Object> imageData = yaml.load(inputStream);

            String imageName = (String) imageData.get("image_name");

            List<List<List<Integer>>> frameArray = (List<List<List<Integer>>>) imageData.get("frame");
            int rows = frameArray.size();
            int cols = frameArray.get(0).size();
            Mat frame = new Mat(rows, cols, CvType.CV_8UC3);

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    List<Integer> pixel = frameArray.get(row).get(col);
                    double[] pixelData = {pixel.get(0), pixel.get(1), pixel.get(2)};
                    frame.put(row, col, pixelData);
                }
            }

            System.out.printf("INFO: loading: \"%s\"%n", imageName);
            this.put(imageName, frame);
        }
    }
}
