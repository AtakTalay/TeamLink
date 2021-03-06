package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import models.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Chat screen with announcements of the selected team
 */
public class ChatScreenController extends MainTemplateController implements InitializeData {

    private int currentIndex;
    private ArrayList<GridPane> gridPanes;

    @FXML
    private Label teamNameLabel;
    @FXML
    private ComboBox<Team> teamBox;
    @FXML
    private ImageView teamLogo;
    @FXML
    private Button downButton;
    @FXML
    private TextField textField;
    @FXML
    private TextArea textArea;
    @FXML
    private GridPane chatPane;
    @FXML
    private ImageView arrowIcon;
    @FXML
    private GridPane announcementsGrid;
    @FXML
    private HBox announcementsEmptyHBox;
    @FXML
    private GridPane helpPane;
    @FXML
    private Pane darkPane;
    @FXML
    private ImageView helpPaneIcon;

    /**
     * Initialises scene properties
     * @param user current user session
     */
    public void initData(UserSession user){
        super.initData(user);

        if(user.isStyleDark()) {
            darkIcons();
        }
        else {
            lightIcons();
        }

        darkPane.setDisable(true);
        darkPane.setVisible(false);
        helpPane.setDisable(true);
        helpPane.setVisible(false);

        gridPanes = new ArrayList<GridPane>();

        teamBox.getItems().addAll(user.getUserTeams());
        teamBox.getSelectionModel().selectFirst();

        currentIndex = 0;

        downButton.setDisable(true);

        teamNameLabel.setText(teamBox.getValue().getTeamName());

        if (teamBox.getValue().getTeamLogo() != null) {
            teamLogo.setImage(teamBox.getValue().getTeamLogo().getImage());
        }
        else {
            try {
                teamLogo.setImage(new Image(getClass().getResource("/Resources/Images/emptyTeamLogo.png").toURI().toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        AppManager.fadeIn(chatPane,500);

        Platform.runLater(() -> {
            try {
                setUpAnnouncementsGrid();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    /**
     * Gets teams announcemets and displays in the grid
     * @throws SQLException
     */
    public void setUpAnnouncementsGrid() throws SQLException {
        for (int i = 0; i < 5; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(20);
            announcementsGrid.getRowConstraints().add(row);
        }
        int rowIndex = 4;
        for (Announcement announcement : user.getAnnouncements(teamBox.getValue())) {
            GridPane customGrid = createCustomAnnouncementGridPane(announcement.getTitle(), announcement.getDescription());
            GridPane senderPane = createSenderInfoGrid(announcement);
            announcementsGrid.add(senderPane, 0, rowIndex);
            announcementsGrid.add(customGrid, 1, rowIndex);
            gridPanes.add(senderPane);
            gridPanes.add(customGrid);
            rowIndex--;
        }

    }

    public void setTeamAnnouncements() {
        int rowIndex = 4;
        for (Announcement announcement : user.getAnnouncements(teamBox.getValue())) {
            GridPane customGrid = createCustomAnnouncementGridPane(announcement.getTitle(), announcement.getDescription());
            GridPane senderPane = createSenderInfoGrid(announcement);
            announcementsGrid.add(senderPane, 0, rowIndex);
            announcementsGrid.add(customGrid, 1, rowIndex);
            gridPanes.add(senderPane);
            gridPanes.add(customGrid);
            rowIndex--;
        }
    }

    /**
     * Updates the announcements according to current index
     * @throws SQLException
     */
    public void updateGrid() throws SQLException {
        Announcement announcement;
        int rowIndex;

        for(GridPane grid : gridPanes){
            announcementsGrid.getChildren().remove(grid);
        }
        rowIndex = 4;

        for (int i = 0; i < 5; i++) {
            if (currentIndex + i < user.getAnnouncements(teamBox.getValue()).size()) {
                announcement = user.getAnnouncements(teamBox.getValue()).get(currentIndex + i);
            }
            else {
                announcement = DatabaseManager.getAnnouncementsByIndex(user.getDatabaseConnection(),teamBox.getValue(),currentIndex + i);
            }
            GridPane customGrid = createCustomAnnouncementGridPane(announcement.getTitle(), announcement.getDescription());
            GridPane senderPane = createSenderInfoGrid(announcement);
            announcementsGrid.add(senderPane, 0, rowIndex - i);
            announcementsGrid.add(customGrid, 1, rowIndex - i);
            gridPanes.add(senderPane);
            gridPanes.add(customGrid);
        }

    }

    /**
     * Moves announcements up
     * @param actionEvent up button pushed
     * @throws SQLException
     */
    public void moveUp(ActionEvent actionEvent) throws SQLException {
        currentIndex++;
        downButton.setDisable(false);
        updateGrid();
    }

    /**
     * Moves the announcements down
     * @param actionEvent down button pushed
     * @throws SQLException
     */
    public void moveDown(ActionEvent actionEvent) throws SQLException {
        if (currentIndex > 0) {
            currentIndex--;
            updateGrid();
        }
        if (currentIndex == 0) {
            downButton.setDisable(true);
        }

    }

    /**
     * Gets selected team and displays it's announcements
     * @param actionEvent team combo box selection
     * @throws SQLException
     */
    public void teamSelected(ActionEvent actionEvent) throws SQLException {
        currentIndex = 0;

        for(GridPane grid : gridPanes){
            announcementsGrid.getChildren().remove(grid);
        }

        setTeamAnnouncements();
        currentIndex = 0;

        downButton.setDisable(true);

        teamNameLabel.setText(teamBox.getValue().getTeamName());

        if (teamBox.getValue().getTeamLogo() != null) {
            teamLogo.setImage(teamBox.getValue().getTeamLogo().getImage());
        }
        else {
            try {
                teamLogo.setImage(new Image(getClass().getResource("/Resources/Images/emptyTeamLogo.png").toURI().toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends an announcement in a team
     * @param actionEvent submit button pushed
     * @throws SQLException
     */
    public void sendAnnouncement(ActionEvent actionEvent) throws SQLException {
        System.out.println(DatabaseManager.getAnnouncementsByIndex(user.getDatabaseConnection(), teamBox.getValue(),0).getTitle());
        if (!textArea.getText().equals("") && !textField.getText().equals("")) {
            Announcement announcement = new Announcement(textField.getText(), textArea.getText(), user.getUser());
            user.getAnnouncements(teamBox.getValue()).add(0,announcement);
            DatabaseManager.createNewAnnouncement(user.getDatabaseConnection(), announcement, teamBox.getValue());
            System.out.println(DatabaseManager.getAnnouncementsByIndex(user.getDatabaseConnection(), teamBox.getValue(),0).getTitle());
            updateGrid();
            textField.setText("");
            textArea.setText("");
        }

    }

    /**
     * Initialises icons according to selected theme
     */
    private void lightIcons() {
        arrowIcon.setImage((new Image("/Resources/Images/black/outline_arrow_back_ios_black_24dp.png")));
        helpPaneIcon.setImage((new Image("/Resources/Images/black/help_black.png")));

    }

    /**
     * Initialises icons according to selected theme
     */
    private void darkIcons() {
        arrowIcon.setImage((new Image("/Resources/Images/white/outline_arrow_back_ios_white_24dp.png")));
        helpPaneIcon.setImage((new Image("/Resources/Images/white/help_white.png")));
    }

    /**
     * Creates the part to display with announcement title and description
     * @param notTitle title
     * @param notDescription content
     * @return grid with texts
     */
    private GridPane createCustomAnnouncementGridPane(String notTitle, String notDescription){
        GridPane gridPane = new GridPane();
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(10);
        row1.setMinHeight(0);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(40);
        row2.setMinHeight(0);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(40);
        row3.setMinHeight(0);
        RowConstraints row4 = new RowConstraints();
        row4.setPercentHeight(10);
        row4.setMinHeight(0);
        gridPane.getRowConstraints().addAll(row1, row2, row3, row4);
        Label title = new Label(notTitle);
        Label description = new Label(notDescription);
        title.setPrefWidth(announcementsEmptyHBox.getWidth() * 0.70);
        description.setPrefWidth(announcementsEmptyHBox.getWidth()*0.70);
        title.getStyleClass().add("title");
        description.getStyleClass().add("description");
        gridPane.add(title, 0, 1);
        gridPane.add(description, 0, 2);
        return gridPane;
    }


    /**
     * Creates the sender information part to display
     * @param announcement announcement to display
     * @return sender info grid
     */
    private GridPane createSenderInfoGrid(Announcement announcement){
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        String formattedDate =  sdf.format(announcement.getTimeSent());
        ImageView senderPhoto = new ImageView();

        if(announcement.getSender().getProfilePhoto() == null){
            if(user.isStyleDark()) {
                try {
                    senderPhoto.setImage(new Image(getClass().getResource("/Resources/Images/white/big_profile_white.png").toURI().toString()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    senderPhoto.setImage(new Image(getClass().getResource("/Resources/Images/white/big_profile_black.png").toURI().toString()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            senderPhoto.setImage(announcement.getSender().getProfilePhoto().getImage());
        }

        senderPhoto.setFitHeight(40);
        senderPhoto.setFitWidth(40);
        GridPane gridPane = new GridPane();
        RowConstraints row1 = new RowConstraints();
        BorderPane imageContainer = new BorderPane(senderPhoto);
        row1.setPercentHeight(60);
        row1.setMinHeight(0);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(20);
        row2.setMinHeight(0);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(20);
        row3.setMinHeight(0);
        gridPane.getRowConstraints().addAll(row1, row2, row3);
        Label senderNameLabel = new Label(announcement.getSender().getFirstName());
        Label sentDateLabel = new Label(formattedDate);
        senderNameLabel.setPrefWidth(announcementsEmptyHBox.getWidth()*0.55);
        senderNameLabel.getStyleClass().add("little");
        sentDateLabel.setPrefWidth(announcementsEmptyHBox.getWidth()*0.55);
        sentDateLabel.getStyleClass().add("little");
        gridPane.add(imageContainer, 0,0);
        gridPane.add(senderNameLabel, 0, 1);
        gridPane.add(sentDateLabel, 0, 2);
        return gridPane;
    }

    @Override
    /**
     * Shows help information of the screen
     */
    public void helpButtonPushed(ActionEvent actionEvent){
        darkPane.setVisible(true);
        darkPane.setDisable(false);
        helpPane.setDisable(false);
        helpPane.setVisible(true);
    }

    /**
     * Closes the help pane
     * @param actionEvent close button pushed
     */
    public void helpPaneClose(ActionEvent actionEvent) {
        darkPane.setDisable(true);
        darkPane.setVisible(false);
        helpPane.setDisable(true);
        helpPane.setVisible(false);
    }

    @Override
    public void toChatScreen(ActionEvent actionEvent) throws IOException {}

}
