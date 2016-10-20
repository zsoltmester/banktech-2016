class NotEnoughMoneyException extends RuntimeException {

}

public class Inflation {
	
	private double remainingMoney;
	private double cost;
	
	public Inflation(double remainingMoney, double cost) {
		this.remainingMoney = remainingMoney;
		this.cost = cost;
	}
	
	public int countWithInflation(double inflationRate) {
		if (remainingMoney < cost) {
			throw new NotEnoughMoneyException();
		}
		
		int count = 0;
		while (remainingMoney >= cost) {
			count++;
			remainingMoney -= cost;
			cost += inflationRate;
		}
		System.out.println("Cost: " + cost + ", Remaining money: " + remainingMoney);
		return count;
	}

	public static void main(String[] args) {
		// example for creating and using an Inflation object:
		Inflation d = new Inflation(0.6, 0.2);
		System.out.println("count: " + d.countWithInflation(0.2));
	}

}
