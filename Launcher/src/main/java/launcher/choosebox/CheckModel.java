package launcher.choosebox;

import javafx.collections.ObservableList;
import launcher.LauncherAPI;

public interface CheckModel<T> {
	@LauncherAPI
	public int getItemCount();

	@LauncherAPI
	public ObservableList<T> getCheckedItems();

	@LauncherAPI
	public void checkAll();

	@LauncherAPI
	public void clearCheck(T item);

	@LauncherAPI
	public void clearChecks();

	@LauncherAPI
	public boolean isEmpty();

	@LauncherAPI
	public boolean isChecked(T item);

	@LauncherAPI
	public void check(T item);

	@LauncherAPI
	public void toggleCheckState(T item);
}
