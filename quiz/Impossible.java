public class Impossible {
	public static void main(String[] args) {
		Double i = Double.POSITIVE_INFINITY;
		if (i>0 & i.equals(i+1)){
		    System.out.println("Impossible!");
			System.out.println("Double.POSITIVE_INFINITY;".hashCode()); // = C
		}
	}
}
