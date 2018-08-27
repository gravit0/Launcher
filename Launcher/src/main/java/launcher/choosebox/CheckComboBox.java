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
    @Override protected Skin<?> createDefaultSkin() {
        checkComboBoxSkin = new CheckComboBoxSkin<>(this);
        return checkComboBoxSkin;
    }

    public ObservableList<T> getItems() {
        return items;
    }
    
    public BooleanProperty getItemBooleanProperty(int index) {
        if (index < 0 || index >= items.size()) return null;
        return getItemBooleanProperty(getItems().get(index));
    }
    
    public BooleanProperty getItemBooleanProperty(T item) {
        return itemBooleanMap.get(item);
    }
    
    private ObjectProperty<IndexedCheckModel<T>> checkModel = 
            new SimpleObjectProperty<>(this, "checkModel");
    
    public final void setCheckModel(IndexedCheckModel<T> value) {
        checkModelProperty().set(value);
    }

    public final IndexedCheckModel<T> getCheckModel() {
        return checkModel == null ? null : checkModel.get();
    }

    public final ObjectProperty<IndexedCheckModel<T>> checkModelProperty() {
        return checkModel;
    }
    
    private ObjectProperty<StringConverter<T>> converter = 
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    
    public final ObjectProperty<StringConverter<T>> converterProperty() { 
        return converter; 
    }
    
    public final void setConverter(StringConverter<T> value) { 
        converterProperty().set(value); 
    }
    
    public final StringConverter<T> getConverter() { 
        return converterProperty().get(); 
    }
    
    private StringProperty title = new SimpleStringProperty(null);
    
    public final StringProperty titleProperty() {
        return title;
    }

    public final void setTitle(String value) {
        title.setValue(value);
    }
    
    public final String getTitle() {
        return title.getValue();
    }

    public void show() {
        if (checkComboBoxSkin != null) {
            checkComboBoxSkin.show();
        }
    }

    public void hide() {
        if (checkComboBoxSkin != null) {
            checkComboBoxSkin.hide();
        }
    }
    
    private static class CheckComboBoxBitSetCheckModel<T> extends CheckBitSetModelBase<T> {

    private final ObservableList<T> items;
        

        CheckComboBoxBitSetCheckModel(final ObservableList<T> items, final Map<T, BooleanProperty> itemBooleanMap) {
            super(itemBooleanMap);
            
            this.items = items;
            this.items.addListener((ListChangeListener<T>) c -> updateMap());
            
            updateMap();
        }
        
        @Override public T getItem(int index) {
            return items.get(index);
        }
        
        @Override public int getItemCount() {
            return items.size();
        }
        
        @Override public int getItemIndex(T item) {
            return items.indexOf(item);
        }
    }
}
