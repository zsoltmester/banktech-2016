public interface Calculation {
	public default void calculateSomething() {
		//... calculations
		System.out.println("Calculation result");
	}
}
