/**
 * 
 */
package freestyle;
import java.util.*;
import java.io.*;

/**
 * @author mollychen
 *
 */
public class PartyPlannerCopy {
	// keep track of guest preferences
	ArrayList<Guest> guestList = new ArrayList<Guest>();
	
	// store all food and drink listings	
	HashMap<String, Double> drinkCost = new HashMap<String,Double>(); 
	HashMap<String, Double> foodCost = new HashMap<String, Double>(); 
	HashMap<String, Integer> drinkQty = new HashMap<String,Integer>(); 
	HashMap<String, Integer> foodQty = new HashMap<String, Integer>(); 
	HashMap<String, Double> itemBudget = new HashMap<String, Double>();

	// reverse lookup - assumes unique key:value mapping, however
	public static String getKeyFromValue(HashMap<String, Double> map, Object value) {
	    for (String s : map.keySet()) {
	      if (map.get(s).equals(value)) {
	        return s;
	      }
	    }
	    return null;
	  }
	
	public void parseFiles(ArrayList<File> files){
		Guest g;
		
		try {
			Scanner sc = new Scanner(files.get(0));
			
			// handle people.txt
			String name;
			String[] drinkPrefs = null;
			String[] foodPrefs = null;
			
			// create a new guest
			while (sc.hasNext()){
				name = sc.nextLine();
				drinkPrefs = sc.nextLine().split(",");		
				foodPrefs = sc.nextLine().split(",");	
				g = new Guest(name, drinkPrefs, foodPrefs);
				guestList.add(g);
			}
			
			// handle drinks.txt - combine these two while loops?
			sc = new Scanner(files.get(1));
			while (sc.hasNextLine()){										 
				String[] drink = sc.nextLine().split(":");
				if (drink.length == 2){
					drinkCost.put(drink[0], Double.parseDouble(drink[1]));
					drinkQty.put(drink[0], 0); 								// qty initialized to 0
				}	
			}
			
			// handle food.txt
			sc = new Scanner(files.get(2));
			while (sc.hasNextLine()){
				String[] food = sc.nextLine().split(":");
				if (food.length == 2){
					foodCost.put(food[0], Double.parseDouble(food[1]));
					foodQty.put(food[0], 0);
				}
			}
		}
		catch (FileNotFoundException e){
			System.out.println("File not found");							
		}
	}
	
	public void calcPurchases(){
		// update quantities
		for (Guest g : guestList){
			for (String d : g.getDrinks()){
				if (drinkQty.keySet().contains(d)){
					drinkQty.put(d, drinkQty.get(d)+1);					 
				}
			}
			for (String f : g.getFood()){
				if (foodQty.keySet().contains(f)){
					foodQty.put(f, foodQty.get(f)+1);					 
				}
			}
		}
		
		// calculate costs per item
		for (String d : drinkQty.keySet()){
			itemBudget.put(d, drinkQty.get(d) * drinkCost.get(d));
		}
		for (String f : foodQty.keySet()){
			itemBudget.put(f, foodQty.get(f) * foodCost.get(f));
		}
	}
	
	public HashMap<String, Double> distributeBudget(Double budget){
		HashMap<String, Double> itemsToBuy = new HashMap<String, Double>();
		
		// sort descending 
		TreeSet<Double> sortedItems = new TreeSet<Double>(Collections.reverseOrder());
		sortedItems.addAll(itemBudget.values());
		
		// calculate which products to buy
		Iterator<Double> it = sortedItems.iterator();
		
		// alternate, or roughly calculate ratios
		Double cost;
		while (budget > 0.0 && it.hasNext()){
			cost = it.next();
			if (cost != null && budget - cost >= 0){
				budget -= cost;
				itemsToBuy.put(getKeyFromValue(itemBudget, cost), cost);
			}
		}
		
		// display items to buy
		System.out.println("Things to buy: ");
		for (String s : itemsToBuy.keySet()){
			System.out.println(s + " $" + itemsToBuy.get(s));
		}
		
		return itemsToBuy;
	}
	
	public static void main(String[] args) {
		ArrayList<File> fileDirectory = new ArrayList<File>();

		fileDirectory.add(new File("people.txt"));	
		fileDirectory.add(new File("drinks.txt"));
		fileDirectory.add(new File("food.txt"));
		
		PartyPlanner p = new PartyPlanner();
		p.parseFiles(fileDirectory);
		p.sortByQuantity();
		p.allocateBudget(18.);
	}
}
