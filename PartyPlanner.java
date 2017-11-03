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
    HashMap<String, Integer> costs = new HashMap<String,Integer>(); 
    HashMap<String, Integer> drinkQty = new HashMap<String,Integer>(); 
    HashMap<String, Integer> foodQty = new HashMap<String, Integer>(); 
    HashMap<String, Integer> partyBudget = new HashMap<String, Integer>();
    Map<String, Integer> sortedItems;
    
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
     * ASSUMPTIONS: unique key:value mapping (read assumptions.txt for more) 
     * @param map
     * @param value
     * @return
     */
    public static String getKeyFromValue(HashMap<String, Integer> map, Object value) {
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
            
            // handle drinks.txt 
            sc = new Scanner(files.get(1));
            while (sc.hasNextLine()){                                        
                String[] drink = sc.nextLine().split(":");
                if (drink.length == 2){
                    costs.put(drink[0], Integer.parseInt(drink[1]));
                    drinkQty.put(drink[0], 0);                              // qty initialized to 0
                }   
            }
            
            // handle food.txt
            sc = new Scanner(files.get(2));
            while (sc.hasNextLine()){
                String[] food = sc.nextLine().split(":");
                if (food.length == 2){
                    costs.put(food[0], Integer.parseInt(food[1]));
                    foodQty.put(food[0], 0);                                // qty initialized to 0
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println("File not found");                           
        }
    }
    
    /**
     * Method to sort items based on popularity (quantity requested).
     */
    public void sortByQuantity(){       
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
        
        // combine food and drinks
        HashMap<String, Integer> allItems = new HashMap<String, Integer>();
        allItems.putAll(drinkQty);
        allItems.putAll(foodQty);

        sortByValue(allItems);
        
        List<Map.Entry<String, Integer>> temp = new LinkedList<Map.Entry<String, Integer>>(allItems.entrySet());
        sortedItems = new LinkedHashMap<String, Integer>();
        // create custom comparator
        Collections.sort(temp, new Comparator<Map.Entry<String, Integer>>(){
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        
        // add to linked hash map in reverse order
        for (int i = temp.size() - 1; i >= 0; i--){
            sortedItems.put(temp.get(i).getKey(), temp.get(i).getValue());
        }       
    }
    
    /**
     * Algorithm to distribute the party budget.
     * @param budget
     * @return
     */
    public void allocateBudget(int budget){
        if (budget < 0){
            System.out.println("Please enter a value greater than 0.");
        }
        
        HashMap<String, Integer> itemsToBuy = new HashMap<String, Integer>();
        Iterator<Map.Entry<String, Integer>> it = sortedItems.entrySet().iterator();
        Integer cost = 0;
        int i;
        
        while (budget > 0 && it.hasNext()){
            Map.Entry<String, Integer> entry = it.next();
            int qty = entry.getValue();
            for (i = 0; i < qty; i++){
                if (costs.get(entry.getKey()) != null){
                    cost = costs.get(entry.getKey());
                }
                else {
                 cost = costs.get(entry.getKey());      // subtract from budget the cost of ONE more item
                }
                if (budget - cost >= 0){
                    budget -= cost;
                    if (itemsToBuy.containsKey(entry.getKey())){
                        int currentCost = itemsToBuy.get(entry.getKey());  
                        itemsToBuy.put(entry.getKey(), currentCost + costs.get(entry.getKey()));
                    }
                    else {
                        itemsToBuy.put(entry.getKey(), costs.get(entry.getKey()));
                    }
                }
            }
        }
        
        // return all items to buy
        for (String s : itemsToBuy.keySet()){
            System.out.println("Spend $" + itemsToBuy.get(s) + " on " + s);
        }
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
        p.sortByQuantity();
        p.allocateBudget(500);
    }
}
