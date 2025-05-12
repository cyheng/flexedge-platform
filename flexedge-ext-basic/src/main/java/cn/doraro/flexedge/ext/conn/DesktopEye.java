// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.conn;

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Robot;

public class DesktopEye
{
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
