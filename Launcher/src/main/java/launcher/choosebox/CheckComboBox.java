package launcher.choosebox;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;
import launcher.LauncherAPI;
import launcher.choosebox.impl.CheckComboBoxSkin;

public class CheckComboBox<T> extends ControlsFXControl {
	private final ObservableList<T> items;
	private final Map<T, BooleanProperty> itemBooleanMap;
	private CheckComboBoxSkin<T> checkComboBoxSkin;

	public CheckComboBox() {
		this(null);
	}

	public CheckComboBox(final ObservableList<T> items) {
		final int initialSize = items == null ? 32 : items.size();

		this.itemBooleanMap = new HashMap<>(initialSize);
		this.items = items == null ? FXCollections.observableArrayList() : items;
		setCheckModel(new CheckComboBoxBitSetCheckModel<>(this.items, itemBooleanMap));
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		checkComboBoxSkin = new CheckComboBoxSkin<>(this);
		return checkComboBoxSkin;
	}

	@LauncherAPI
	public ObservableList<T> getItems() {
		return items;
	}

	@LauncherAPI
	public BooleanProperty getItemBooleanProperty(int index) {
		if (index < 0 || index >= items.size())
			return null;
		return getItemBooleanProperty(getItems().get(index));
	}

	@LauncherAPI
	public BooleanProperty getItemBooleanProperty(T item) {
		return itemBooleanMap.get(item);
	}

	@LauncherAPI
	private ObjectProperty<IndexedCheckModel<T>> checkModel = new SimpleObjectProperty<>(this, "checkModel");

	@LauncherAPI
	public final void setCheckModel(IndexedCheckModel<T> value) {
		checkModelProperty().set(value);
	}

	@LauncherAPI
	public final IndexedCheckModel<T> getCheckModel() {
		return checkModel == null ? null : checkModel.get();
	}

	@LauncherAPI
	public final ObjectProperty<IndexedCheckModel<T>> checkModelProperty() {
		return checkModel;
	}

	@LauncherAPI
	private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this,
			"converter");

	@LauncherAPI
	public final ObjectProperty<StringConverter<T>> converterProperty() {
		return converter;
	}

	@LauncherAPI
	public final void setConverter(StringConverter<T> value) {
		converterProperty().set(value);
	}

	@LauncherAPI
	public final StringConverter<T> getConverter() {
		return converterProperty().get();
	}

	@LauncherAPI
	private StringProperty title = new SimpleStringProperty(null);

	@LauncherAPI
	public final StringProperty titleProperty() {
		return title;
	}

	@LauncherAPI
	public final void setTitle(String value) {
		title.setValue(value);
	}

	@LauncherAPI
	public final String getTitle() {
		return title.getValue();
	}

	@LauncherAPI
	public void show() {
		if (checkComboBoxSkin != null)
			checkComboBoxSkin.show();
	}

	@LauncherAPI
	public void hide() {
		if (checkComboBoxSkin != null)
			checkComboBoxSkin.hide();
	}

	@LauncherAPI
	private static class CheckComboBoxBitSetCheckModel<T> extends CheckBitSetModelBase<T> {
		@LauncherAPI
		private final ObservableList<T> items;

		@LauncherAPI
		CheckComboBoxBitSetCheckModel(final ObservableList<T> items, final Map<T, BooleanProperty> itemBooleanMap) {
			super(itemBooleanMap);

			this.items = items;
			this.items.addListener((ListChangeListener<T>) c -> updateMap());

			updateMap();
		}

		@LauncherAPI
		@Override
		public T getItem(int index) {
			return items.get(index);
		}

		@LauncherAPI
		@Override
		public int getItemCount() {
			return items.size();
		}

		@LauncherAPI
		@Override
		public int getItemIndex(T item) {
			return items.indexOf(item);
		}
	}
}
