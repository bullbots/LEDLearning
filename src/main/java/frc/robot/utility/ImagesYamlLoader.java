package frc.robot.utility;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.numbers.N16;
import frc.robot.subsystems.MatrixLEDs;

import java.util.HashMap;

public class ImagesYamlLoader extends HashMap<String, Matrix<N16, N16>> {

    public ImagesYamlLoader() {
        super();
        this.put("Row One", MatrixLEDs.oneRow(0));
        this.put("Row Two", MatrixLEDs.oneRow(1));
        this.put("Eye", Matrix.eye(Nat.N16()));
    }

    @Override
    public Matrix<N16, N16> put(String key, Matrix<N16, N16> value) {
        System.out.println("INFO: ImagesYamlLoader put");
        return super.put(key, value);
    }

    @Override
    public Matrix<N16, N16> get(Object key) {
        System.out.println("INFO: ImagesYamlLoader get");
        return super.get(key);
    }
}
