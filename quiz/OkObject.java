import java.util.*;
public class OkObject implements Comparator<OkObject> {
    public boolean isOk;
    public static void main(String[] args){
        OkObject okObject1 = new OkObject(true);
        OkObject okObject2 = new OkObject(false);
        TreeSet treeSet = new TreeSet<OkObject>(okObject1);
        treeSet.add(okObject1);
        treeSet.add(okObject2);
        System.out.println("Vege");
    }
    public OkObject(boolean isOk){
        this.isOk = isOk;
    }
    public int compare(OkObject o1, OkObject o2) {
        if(o1.isOk == false && o2.isOk == true){
            return 1;
        } else if(o1.isOk == true && o1.isOk == false){
            return 0;
        } else {
            throw new IllegalStateException("Ket OkObject-nek nem lehet egyszerre ugyanaz az isOk flagje!");
        }
    }

}
