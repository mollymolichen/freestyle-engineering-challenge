/**
 * 
 */
package freestyle;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

/**
 * @author mollychen
 *
 */
public class PartyPlannerOld {
	// global variables
	private int NUM_FILES = 3;
	List<Guest> guestList = new ArrayList<Guest>();
	
	// store all food and drink listings	
	HashMap<String, Double> drinkCost = new HashMap<String,Double>(); 
	HashMap<String, Double> foodCost = new HashMap<String, Double>(); 
	HashMap<String, Integer> drinkQty = new HashMap<String,Integer>(); 
	HashMap<String, Integer> foodQty = new HashMap<String, Integer>(); 
	HashMap<String, Double> drinkBudget = new HashMap<String, Double>();
	HashMap<String, Double> foodBudget = new HashMap<String, Double>();
	
	public PartyPlannerOld(int numFiles){
		this.NUM_FILES = numFiles;  
	}
	
	// sort values in HashMap, descending
	/*static <K,V extends Comparable<? super V>> List<Entry<K, V>> sortDescending(Map<K,V> map) {
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		Comparator<Entry<K,V>> c = new Comparator<Entry<K,V>>() {
	        @Override
	        public int compare(Entry<K,V> e1, Entry<K,V> e2) {
	            return e2.getValue().compareTo(e1.getValue());
	        }
	    };
		Collections.sort(sortedEntries, c);
		
		return sortedEntries;
	}*/
	
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
				g.setName(name);
				g.setDrinks(drinkPrefs);
				g.setFood(foodPrefs);
				guestList.add(g);
			}
			
			// handle drinks.txt - combine these two while loops?
			sc = new Scanner(files.get(1));
			while (sc.hasNextLine()){										// or hasNextLine()?
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
	
	// assumption that you won't have two of the same drinks (from different vendors)
	// assume you're buying all the drinks from the same store, at the same price per brand of drink
	// basically no duplicate keys
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
			drinkBudget.put(d, drinkQty.get(d) * drinkCost.get(d));
		}
		for (String f : foodQty.keySet()){
			foodBudget.put(f, foodQty.get(f) * foodCost.get(f));
		}
	}
	
	// find top item, sortedItems.get(0)
	// see how many people want top item
	// give top item to x% of people
		// find biggest x st budget doesn't go over
	// 100 = (1st_cost * 1st_qty * 0.5) + (2nd_cost * 2nd_qty * 0.5)
	
	public HashMap<String, Double> distributeBudget(Double budget){
		HashMap<String, Double> itemsToBuy = new HashMap<String, Double>();
		
		// sort descending w/ TreeSet - working, TreeMap didn't work for whatever reason
		TreeSet<Double> sortedDrinks = new TreeSet<Double>(Collections.reverseOrder());
		sortedDrinks.addAll(drinkBudget.values());
		for (Double d : sortedDrinks){
			System.out.println(d + " " + getKeyFromValue(drinkBudget, d));
		}
		System.out.println();
				
		TreeSet<Double> sortedFood = new TreeSet<Double>(Collections.reverseOrder());
		sortedFood.addAll(foodBudget.values());
		for (Double d : sortedFood){
			System.out.println(d + " " + getKeyFromValue(foodBudget, d));
		}
		
		// calculate which products to buy
		Iterator<Double> it1 = sortedDrinks.iterator();
		Iterator<Double> it2 = sortedFood.iterator();
		
		// alternate, or roughly calculate ratios
		Double cost;
		while (budget > 0 && it1.hasNext() || it2.hasNext()){
			// buy a drink
			cost = it1.next();
			if (cost != null && budget - cost > 0){
				budget -= cost;
				System.out.println("Getting " + getKeyFromValue(drinkBudget, cost) + " " + cost);
				itemsToBuy.put(getKeyFromValue(drinkBudget, cost), cost);
			}
			cost = it2.next();
			// buy food
			if (cost != null && budget - cost > 0){
				budget -= cost;
				System.out.println("Getting " + getKeyFromValue(foodBudget, cost) + " " + cost);
				itemsToBuy.put(getKeyFromValue(foodBudget, cost), cost);
			}
			/*if (it1.hasNext()){
				Double price = it1.next();
				//if (budget - price > 0){
					System.out.println("Getting " + price);
					budget -= price;
					System.out.println(budget);
					// itemsToBuy.put(getKeyFromValue(drinkBudget, price), price);
				//}
			}
			// buy food
			if (it2.hasNext()){
				Double price = it2.next();
				// if (budget - price > 0){
					System.out.println("Getting " + price);
					budget -= price;
					System.out.println(budget);
					// itemsToBuy.put(getKeyFromValue(foodBudget, price), price);
				// }
			}
			// System.out.println("loop");*/
		}
		
		// display items to buy
		System.out.println("Things to buy: ");
		for (String s : itemsToBuy.keySet()){
			System.out.print(s + " " + itemsToBuy.get(s));
		}
		
		return itemsToBuy;
	}
	
	public static void main(String[] args) {
		ArrayList<File> fileDirectory = new ArrayList<File>();

		fileDirectory.add(new File("people.txt"));	
		fileDirectory.add(new File("drinks.txt"));
		fileDirectory.add(new File("food.txt"));
		
		PartyPlannerOld p = new PartyPlannerOld(3);
		p.parseFiles(fileDirectory);
		p.calcPurchases();
		p.distributeBudget(17.98);
	}
}
