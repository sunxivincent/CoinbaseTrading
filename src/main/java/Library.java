import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

/*
 * @author sunxi, @date 1/22/18 8:26 PM
 */
public class Library {
	public static void main(String[] args) throws IOException {
//		List<String> resr = Files.readLines(new File(Resources.getResource("test").getPath()),
//			Charset.defaultCharset());
//		System.out.println(resr);
		File file = new File("src/main/resources/"
			+ LocalDate.now().getMonth().name()
			+"_"
			+ LocalDate.now().getDayOfMonth());

	}


	public static class NewLimitOrder {
		private String product_id;
		private String side;
		private String type;
		private double price;
		private double size;

		public NewLimitOrder(String product_id, String side, double price, double size) {
			this.product_id = product_id;
			this.side = side;
			this.type = "limit";
			this.price = price;
			this.size = size;
		}
	}
}
