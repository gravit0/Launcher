package launcher.choosebox;

import javafx.collections.ObservableList;

public interface CheckModel<T> {
    
    public int getItemCount();

    public ObservableList<T> getCheckedItems();

    public void checkAll();

    public void clearCheck(T item);
    
    public void clearChecks();
    
    public boolean isEmpty();

    public boolean isChecked(T item);
    
    public void check(T item);

    public void toggleCheckState(T item);
}
