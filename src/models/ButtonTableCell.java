package models;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.net.URISyntaxException;
import java.util.function.Function;

public class ButtonTableCell<S> extends TableCell<S, Button> {

    private final Button tableButton;

    public ButtonTableCell(String label, Function< S, S> function) throws URISyntaxException {
        this.tableButton = new Button(label);
        this.tableButton.getStylesheets().add(getClass().getResource("/stylesheets/ButtonStyleSheet.css").toURI().toString());
        this.tableButton.getStyleClass().add("viewButton");
        this.tableButton.setOnAction((ActionEvent e) -> {
            function.apply(getCurrentItem());
        });
    }

    public S getCurrentItem() {
        return (S) getTableView().getItems().get(getIndex());
    }

    public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, Function< S, S> function) {
        return param -> {
            try {
                return new ButtonTableCell<>(label, function);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        return null;};
    }

    @Override
    public void updateItem(Button item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(tableButton);
        }
    }
}
