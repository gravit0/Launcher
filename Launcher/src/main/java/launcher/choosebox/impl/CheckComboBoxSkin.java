package launcher.choosebox.impl;

import java.util.Collections;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import launcher.LauncherAPI;
import launcher.choosebox.CheckComboBox;
import launcher.choosebox.IndexedCheckModel;

import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

public class CheckComboBoxSkin<T> extends BehaviorSkinBase<CheckComboBox<T>, BehaviorBase<CheckComboBox<T>>> {

	private final ComboBox<T> comboBox;
	private final ListCell<T> buttonCell;

	private final CheckComboBox<T> control;
	private final ObservableList<T> items;
	private final ReadOnlyUnbackedObservableList<Integer> selectedIndices;
	private final ReadOnlyUnbackedObservableList<T> selectedItems;

	@SuppressWarnings("unchecked")
	public CheckComboBoxSkin(final CheckComboBox<T> control) {
		super(control, new BehaviorBase<>(control, Collections.emptyList()));

		this.control = control;
		this.items = control.getItems();

		selectedIndices = (ReadOnlyUnbackedObservableList<Integer>) control.getCheckModel().getCheckedIndices();
		selectedItems = (ReadOnlyUnbackedObservableList<T>) control.getCheckModel().getCheckedItems();

		comboBox = new ComboBox<T>(items) {
			@Override
			protected javafx.scene.control.Skin<?> createDefaultSkin() {
				return createComboBoxListViewSkin(this);
			}
		};
		comboBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		comboBox.setCellFactory(listView -> {
			CheckBoxListCell<T> result = new CheckBoxListCell<>(control::getItemBooleanProperty);
			result.focusedProperty().addListener((o, ov, nv) -> {
				if (nv)
					result.getParent().requestFocus();
			});
			result.setOnMouseClicked(e -> {
				T item = result.getItem();
				if (control.getCheckModel().isChecked(item))
					control.getCheckModel().clearCheck(item);
				else
					control.getCheckModel().check(item);
			});
			result.converterProperty().bind(control.converterProperty());
			return result;
		});

		buttonCell = new ListCell<T>() {
			@Override
			protected void updateItem(T item, boolean empty) {
				setText(getTextString());
			}
		};
		comboBox.setButtonCell(buttonCell);
		comboBox.setValue((T) getTextString());

		selectedIndices.addListener((ListChangeListener<Integer>) c -> buttonCell.updateIndex(0));

		getChildren().add(comboBox);
	}

	@LauncherAPI
	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return comboBox.minWidth(height);
	}

	@LauncherAPI
	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return comboBox.minHeight(width);
	}

	@LauncherAPI
	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return comboBox.prefWidth(height);
	}

	@LauncherAPI
	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return comboBox.prefHeight(width);
	}

	@LauncherAPI
	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return getSkinnable().prefWidth(height);
	}

	@LauncherAPI
	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset,
			double leftInset) {
		return getSkinnable().prefHeight(width);
	}

	@LauncherAPI
	public void show() {
		comboBox.show();
	}

	@LauncherAPI
	public void hide() {
		comboBox.hide();
	}

	@LauncherAPI
	protected String getTextString() {

		if (control.getTitle() != null)
			return control.getTitle();
		else
			return buildString();

	}

	@LauncherAPI
	private String buildString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, max = selectedItems.size(); i < max; i++) {
			T item = selectedItems.get(i);
			if (control.getConverter() == null)
				sb.append(item);
			else
				sb.append(control.getConverter().toString(item));
			if (i < max - 1)
				sb.append(", "); //$NON-NLS-1$
		}
		return sb.toString();
	}

	@LauncherAPI
	private Skin<?> createComboBoxListViewSkin(ComboBox<T> comboBox) {
		final ComboBoxListViewSkin<T> comboBoxListViewSkin = new ComboBoxListViewSkin<T>(comboBox) {
			@Override
			protected boolean isHideOnClickEnabled() {
				return false;
			}
		};
		final ListView<T> listView = (ListView<T>) comboBoxListViewSkin.getPopupContent();
		listView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.SPACE) {
				T item = listView.getSelectionModel().getSelectedItem();
				if (item != null) {
					final IndexedCheckModel<T> checkModel = control.getCheckModel();
					if (checkModel != null)
						checkModel.toggleCheckState(item);
				}
			} else if (e.getCode() == KeyCode.ESCAPE)
				hide();
		});
		return comboBoxListViewSkin;
	}
}
