import java.util.*;

public class IntegerSorter implements Comparator<Integer>{

    private Set<Integer> integers = new HashSet<>();

    public void add(Integer i){
        if (i==null) throw new IllegalArgumentException();
        integers.add(i);
    }

    public List<Integer> sort(){
        List<Integer> result = new ArrayList<>(integers);
        Collections.sort(result, this);
        return result;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        return o2-o1;
    }

    public static void main(String[] args) {
        // A
        IntegerSorter is = new IntegerSorter();
        is.add(Integer.MAX_VALUE);
        is.add(Integer.MIN_VALUE);
        System.out.println(Arrays.toString(is.sort().toArray()));
        // D
        is.compare(null, 1);
    }
}
