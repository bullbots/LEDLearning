package frc.robot.utility;

import edu.wpi.first.wpilibj.RobotBase;
import frc.robot.subsystems.MatrixLEDs;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VideosYamlLoader extends HashMap<String, List<Mat>> {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final String videosFilename = "offline.yaml";

    public VideosYamlLoader() {
        super();
        loadYamlFile();
    }

    @Override
    public List<Mat> put(String key, List<Mat> value) {
        System.out.println("INFO: VideosYamlLoader put");
        return super.put(key, value);
    }

    @Override
    public List<Mat> get(Object key) {
        System.out.println("INFO: VideosYamlLoader get");
        return super.get(key);
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

    private void loadYamlFile() {
        Path deployFolderPath = getDeployPath();

        Path imagePath = deployFolderPath.resolve(videosFilename);

        Yaml yaml = new Yaml(new Constructor(ArrayList.class));
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imagePath.toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        List<Map<String, Object>> gifDataList = yaml.load(inputStream);

        HashMap<String, List<Mat>> allGifFrames = new HashMap<>();
        for (Map<String, Object> gifData : gifDataList) {
            String gif_name = (String) gifData.get("gif_name");
            List<Map<String, Object>> framesData = (List<Map<String, Object>>) gifData.get("frames");
            List<Mat> gifFrames = new ArrayList<>();

            for (Map<String, Object> frameData : framesData) {
                List<List<List<Integer>>> frameArray = (List<List<List<Integer>>>) frameData.get("frame_data");
                int frameIdx = (int) frameData.get("frame_index");
                System.out.printf("INFO: current frame index: %d%n", frameIdx);
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

                gifFrames.add(frame);
            }

            allGifFrames.put(gif_name, gifFrames);
        }

        this.put("Offline", allGifFrames.get("offline"));
    }
}
