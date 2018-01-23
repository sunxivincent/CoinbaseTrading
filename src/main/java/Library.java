import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/*
 * @author sunxi, @date 1/22/18 8:26 PM
 */
public class Library {
	public static void main(String[] args) throws IOException {
		List<String> resr = Files.readLines(new File(Resources.getResource("test").getPath()),
			Charset.defaultCharset());
		System.out.println(resr);

	}
}
