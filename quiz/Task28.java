import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
public class Task28 {

    public static void main(String[] args) {
        int[] arr1 = new int[] { 1, 2, 3 };
        int[] arr2 = new int[] { 3, 2, 1 };
        
        System.out.println("Arrays contains same element, but equals? : " + Arrays.equals(arr1, arr2));
        ArrayList<Integer> arrList1 = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> arrList2 = new ArrayList<Integer>(Arrays.asList(3, 2, 1));
        System.out.println("ArrayLists contains same element, but equals? : " + arrList1.equals(arrList2));
        
        try {
            new ArrayList<Integer>().remove(0);
        } catch(Exception e) {
            System.out.println("Got an exception an empty arraylist remove functioncall : " + e);
        }
    }
}
