import net.qiujuer.lesson.sample.foo.Foo;

import java.io.File;
import java.io.IOException;


public class test {
    public static void main(String[] args) {
        File file=new File(Foo.getCacheDir("mxh"),"lyp");
        System.out.println(file.getName());
        System.out.println(file.getAbsoluteFile());
        try {
            System.out.println(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
