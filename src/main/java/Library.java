import com.google.gson.Gson;

import java.io.IOException;

/*
 * @author sunxi, @date 1/22/18 8:26 PM
 */
public class Library {
	public static void main(String[] args) throws IOException {
//		List<String> resr = Files.readLines(new File(Resources.getResource("test").getPath()),
//			Charset.defaultCharset());
//		System.out.println(resr);
		NewLimitOrder a = new NewLimitOrder("af", "afd", 1.1d, 2.2d);
		Gson gson = new Gson();
		String jsonBody = gson.toJson(a);
		System.out.println(jsonBody);

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
