public class MyObjectMain {

	// A - nem jó
	/*private static enum MyObject {
		A, S, D
	};*/

	// B - jó
	//private static class MyObject {};

	// C - jó
	//private static interface MyObject {};

	public static void main(String[] args) {
		Object obj = new MyObject(){};
		System.out.println(obj.getClass().getTypeName()); // class, interface: MyObjectMain$1
		System.out.println(obj.getClass().getSuperclass().getTypeName()); // class: MyObjectMain$MyObject, interface: java.lang.Object
	}
}
