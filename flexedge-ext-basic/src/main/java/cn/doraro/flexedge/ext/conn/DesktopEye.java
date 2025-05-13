

package cn.doraro.flexedge.ext.conn;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class DesktopEye {
    public void capScreen() throws Exception {
        final Robot robot = new Robot();
        for (int i = 0; i < 10; ++i) {
            robot.delay(3000);
            robot.mouseMove(150 * i, 100 * i);
        }
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final Rectangle rect = new Rectangle(dim);
        final BufferedImage bi = robot.createScreenCapture(rect);
        ImageIO.write(bi, "jpg", new File("D:\\tmp\\ssss.jpg"));
    }
}
