/**
 * ported to 0.78
 */
package arcadeflex.v078.platform;

import static common.util.*;
import arcadeflex036.osdepend;

public class Main {

    public static void main(String[] args) {
        ConvertArguments("arcadeflex", args);
        System.exit(osdepend.main(argc, argv));
    }
}
