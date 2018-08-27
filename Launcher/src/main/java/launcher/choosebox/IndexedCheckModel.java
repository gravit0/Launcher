package launcher.choosebox;

import javafx.collections.ObservableList;

public interface IndexedCheckModel<T> extends CheckModel<T> {

    public T getItem(int index);

    public int getItemIndex(T item);

    public ObservableList<Integer> getCheckedIndices();

    public void checkIndices(int... indices);

    public void clearCheck(int index);

    public boolean isChecked(int index);

    public void check(int index);

    public void toggleCheckState(int index);

}