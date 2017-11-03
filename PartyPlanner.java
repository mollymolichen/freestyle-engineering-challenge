/**
 * 
 */
package freestyle;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

/**
 * @author mollychen
 *
 */
public class PartyPlanner {

	// keep track of guest preferences
	ArrayList<Guest> guestList = new ArrayList<Guest>();
	
	// store all food and drink listings	
	HashMap<String, Double> drinkCost = new HashMap<String,Double>(); 
	HashMap<String, Double> foodCost = new HashMap<String, Double>(); 
	HashMap<String, Integer> drinkQty = new HashMap<String,Integer>(); 
	HashMap<String, Integer> foodQty = new HashMap<String, Integer>(); 
	HashMap<String, Double> partyBudget = new HashMap<String, Double>();
	
	/**
	 * Sorts a Map based on value.
	 * Credit: Carter Page
	 * URL: https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super Integer>> Map<String, Integer> sortByValue(Map<String, Integer> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap::new
	              ));
	}
	
	/**
	 * Reverse lookup a key from its value
	 * ASSUMPTIONS: unique key:value mapping 
	 * @param map
	 * @param value
	 * @return
	 */
	public static String getKeyFromValue(HashMap<String, Double> map, Object value) {
	    for (String s : map.keySet()) {
	      if (map.get(s).equals(value)) {
	        return s;
	      }
	    }
	    return null;
	  }
	
	/**
	 * Method to parse input files people.txt, drinks.txt and food.txt.
	 * @param files
	 */
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
	
	/**
	 * Method to calculate party budget based on quantity and price of items.
	 */
	public void sortByQuantity(double budget){
		HashMap<String, Double> itemsToBuy = new HashMap<String, Double>();
		
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
		
		HashMap<String, Integer> allItems = new HashMap<String, Integer>();
		allItems.putAll(drinkQty);
		allItems.putAll(foodQty);

		sortByValue(allItems);
		
		List<Map.Entry<String, Integer>> temp = new LinkedList<Map.Entry<String, Integer>>(allItems.entrySet());
		Map<String, Integer> sortedItems = new LinkedHashMap<String, Integer>();
		Collections.sort(temp, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		
		// add to linked hash map in reverse order
		for (int i = temp.size() - 1; i >= 0; i--){
			sortedItems.put(temp.get(i).getKey(), temp.get(i).getValue());
		}
		
		System.out.println("After sorting by quantity");	 
		for (String s : sortedItems.keySet()){
			System.out.println(s + " " + sortedItems.get(s));
		}
		
//		TreeSet<Integer> quantities = new TreeSet<Integer>();
//		quantities.addAll(sortedItems.values());
		Iterator<Map.Entry<String, Integer>> it = sortedItems.entrySet().iterator();
		double cost = 0.0;
		
		while (budget > 0.0 && it.hasNext()){
			Map.Entry<String, Integer> entry = it.next();
			int qty = entry.getValue();
			int i;
			for (i = 0; i < qty; i++){
				// TODO combine food and drink costs
				if (drinkCost.get(entry.getKey()) != null){
					cost = drinkCost.get(entry.getKey());
				}
				else {
				 cost = foodCost.get(entry.getKey());		// subtract from budget the cost of ONE more item
				}
				if (budget - cost >= 0.0){
					budget -= cost;
					System.out.println("Buy " + i + " " + entry.getKey());
				}
			}
		}
	}
	
	/**
	 * Algorithm to distribute the party budget.
	 * @param budget
	 * @return
	 */
	public HashMap<String, Double> allocateBudget(Double budget){
		HashMap<String, Double> itemsToBuy = new HashMap<String, Double>();
		
		// calculate costs per item
		for (String d : drinkQty.keySet()){
			partyBudget.put(d, drinkQty.get(d) * drinkCost.get(d));
		}
		for (String f : foodQty.keySet()){
			partyBudget.put(f, foodQty.get(f) * foodCost.get(f));
		}
		TreeSet<Double> sortedItems = new TreeSet<Double>(Collections.reverseOrder());
		sortedItems.addAll(partyBudget.values());
		
		// calculate which products to buy based on COST ONLY
		Iterator<Double> it = sortedItems.iterator();
		
		// alternate, or roughly calculate ratios
		Double cost;
		Double multiplier = 0.2;
		// aim to get at least 1 food and 1 drink
		// simpler to just get some of 2 unique items
		// find multiplier for first item st there's still enough money for second most item
		while (budget > 0.0 && it.hasNext()){
			cost = it.next();
			if (cost != null && budget - cost >= 0){
				budget -= cost;
				itemsToBuy.put(getKeyFromValue(partyBudget, cost), cost);
			}
		}
		
		// display items to buy
		System.out.println("Things to buy: ");
		for (String s : itemsToBuy.keySet()){
			System.out.println(s + " $" + itemsToBuy.get(s));
		}
		
		return itemsToBuy;
	}
	
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<File> fileDirectory = new ArrayList<File>();

		fileDirectory.add(new File("people.txt"));	
		fileDirectory.add(new File("drinks.txt"));
		fileDirectory.add(new File("food.txt"));
		
		PartyPlanner p = new PartyPlanner();
		p.parseFiles(fileDirectory);
		p.sortByQuantity(18.);
		p.allocateBudget(18.);
	}
}
