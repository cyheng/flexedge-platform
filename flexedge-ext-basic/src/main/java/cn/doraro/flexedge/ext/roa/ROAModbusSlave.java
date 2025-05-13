

package cn.doraro.flexedge.ext.roa;

import java.io.IOException;

public class ROAModbusSlave {
    private void test() {
        try {
            throw new IOException();
        } catch (final IOException | IllegalArgumentException ex) {
        }
    }
}
