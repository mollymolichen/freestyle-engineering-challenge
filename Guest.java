package freestyle;

public class Guest {
	private String name;
	private String[] drinks;
	private String[] food;
	
	public Guest(String name, String[] drinkPrefs, String[] foodPrefs){
		this.name = name;
		this.drinks = drinkPrefs;
		this.food = foodPrefs;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getDrinks() {
		return drinks;
	}

	public void setDrinks(String[] drinks) {
		this.drinks = drinks;
	}

	public String[] getFood() {
		return food;
	}

	public void setFood(String[] food) {
		this.food = food;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
