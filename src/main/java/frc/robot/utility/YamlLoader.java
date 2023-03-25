// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import edu.wpi.first.wpilibj.Filesystem;

/** Add your docs here. */
public class YamlLoader {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final List<String> filenames;
    static {
        File[] files = Paths.get(Filesystem.getDeployDirectory().toString(), "").toFile().listFiles();
        ArrayList<String> f = new ArrayList<>();
        for (File file : files) {
            if (file.getName().endsWith(".yaml")) {
                f.add(file.getName());
                
                System.out.printf("Info: found file: %s\n",  file.getName());
            }
        }
        filenames = f;
    }

    private static final HashMap<String, Mat> images = new HashMap<>();
    private static final HashMap<String, List<Mat>> videos = new HashMap<>();

    public YamlLoader() {
        loadYamlFiles();
    }

    public Mat putImage(String key, Mat value) {
        System.out.println("Info: Yaml put image: " + key);
        return images.put(key, value);
    }

    public Mat getImage(String key) {
        if (images.containsKey(key)) {
            return images.get(key);
        }
        
        // Make sure the sad face loaded too.
        Mat ret = images.get("sad-face-frown");
        if (ret == null) {
            return new Mat(16, 16, CvType.CV_8UC3);
        }
        return ret;
    }

    public List<Mat> putVideo(String key, List<Mat> value) {
        System.out.println("Info: Yaml put video: " + key);
        return videos.put(key, value);
    }

    public List<Mat> getVideo(String key) {
        if (videos.containsKey(key))  {
            return videos.get(key);
        }

        // Make sure the sad face loaded too.
        List<Mat> ret = videos.get("sad-face-frown-one-frame");
        if (ret == null) {
            return List.of(new Mat(16, 16, CvType.CV_8UC3));
        }
        return ret;
    }

    public void loadYamlFiles() {
        Path deployPath = Filesystem.getDeployDirectory().toPath();

        for (String filename : filenames) {
            Path filePath = deployPath.resolve(filename);

            // Try handling as an image
            try {
                Yaml yaml = new Yaml();
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(filePath.toFile());
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
    
                this.putImage(imageName, frame);
                continue;

            } catch (Exception e) {}

            // Try handling as a video
            try {
                Yaml yaml = new Yaml(new Constructor(ArrayList.class));
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(filePath.toFile());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                List<Map<String, Object>> gifDataList = yaml.load(inputStream);
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

                    this.putVideo(gif_name, gifFrames);
                }
                continue;
            } catch (Exception ignored) {}

            System.err.printf("Could not load \"%s\" as video nor as image.\n", filename);
        }
    }
}
