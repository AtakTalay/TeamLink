package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import models.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Controls the settings scene and all its functions
 */
public class SettingsScreenController extends MainTemplateController {
    private final String DARK_STYLE_SHEET = getClass().getResource("/stylesheets/DarkTheme.css").toExternalForm();
    private final String LIGHT_STYLE_SHEET = getClass().getResource("/stylesheets/LightTheme.css").toExternalForm();

    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();

    private ObservableList<String> cityList = FXCollections.observableArrayList("Istanbul", "Ankara", "İzmir");
    private ObservableList<String> ageGroupList = FXCollections.observableArrayList("U18", "U16", "U14", "U12");
    private String selectedAgeGroup = "", selectedCity = "", selectedLeague = "";

    private File createTeamLogoFile;
    private File editTeamLogoFile;
    private File userPhotoFile;

    @FXML
    private GridPane settingsPane;
    @FXML
    private Pane darkPane;
    @FXML
    private ImageView accountPhoto;
    @FXML
    private ImageView teamPhoto;
    @FXML
    private Label teamCode;
    @FXML
    private ComboBox<Team> teamCombobox;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField emailField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button editAccountButton;
    @FXML
    private Button changePhotoButton;
    @FXML
    private CheckBox darkThemeCheck;
    @FXML
    private ImageView copyIcon;
    @FXML
    private GridPane editTeamPane;

    @FXML
    private TextField teamNameEditField;

    @FXML
    private TextField abbrevationEditField;

    @FXML
    private ComboBox chooseCityBox;

    @FXML
    private ComboBox chooseLeagueBox;
    @FXML
    private ComboBox<String> chooseAgeGroup;
    @FXML
    private ComboBox chooseLeagueTeamBox;
    @FXML
    private ImageView logoChangeImage;
    @FXML
    private Button uploadTeamLogoButton;
    @FXML
    private GridPane createTeamPane;
    @FXML
    private TextField teamNameCreateField;
    @FXML
    private TextField abbrevationCreateField;
    @FXML
    private ComboBox chooseCityBoxCreate;
    @FXML
    private ComboBox chooseLeagueBoxCreate;
    @FXML
    private ComboBox<String> chooseAgeGroupCreate;
    @FXML
    private ComboBox chooseLeagueTeamBoxCreate;
    @FXML
    private ImageView logoChangeImageCreate;
    @FXML
    private Button uploadTeamLogoButtonCreate;
    @FXML
    private GridPane helpPane;
    @FXML
    private ImageView helpPaneIcon;
    @FXML
    private GridPane changePasswordPane;
    @FXML
    private PasswordField firstPasswordField;
    @FXML
    private PasswordField secondPasswordField;
    @FXML
    private Button openCreationPaneButton;
    @FXML
    private Button openEditPaneButton;
    @FXML
    private Button deleteTeamButton;
    @FXML
    private TextField teamApplicationCodeField;

    /**
     * Initialises scene properties
     * @param user current user session
     */
    public void initData(UserSession user){
        super.initData(user);

        // Theme selection
        if(user.isStyleDark()) {
            darkIcons();
            darkThemeCheck.setSelected(true);
        }
        else {
            lightIcons();
            darkThemeCheck.setSelected(false);
        }

        // Filling in account info
        userNameField.setText(user.getUser().getFirstName() + " " + user.getUser().getLastName());
        emailField.setText(user.getUser().getEmail());
        datePicker.setValue(user.getUser().getBirthday());

        if (user.getUser().getProfilePhoto() != null) {
            System.out.println("NOT NULL");
            accountPhoto.setImage(user.getUser().getProfilePhoto().getImage());
        }

        // Filling team combo box
        for(Team team : user.getUserTeams()){
            teamCombobox.getItems().add(team);
        }
        teamCombobox.getSelectionModel().selectFirst();

        if (teamCombobox.getValue().getTeamLogo() != null) {
            teamPhoto.setImage(teamCombobox.getValue().getTeamLogo().getImage());
            teamCode.setText("Team code : ");
        }
        teamCode.setText("Team code : " + teamCombobox.getValue().getTeamCode());

        // Editing only
        changePhotoButton.setDisable(true);
        changePhotoButton.setVisible(false);
        if (user.getUser().getProfilePhoto() == null) {
            changePhotoButton.setText("Select Photo");
        }

        // Top panes initialise
        editTeamPane.setVisible(false);
        editTeamPane.setDisable(true);
        createTeamPane.setVisible(false);
        createTeamPane.setDisable(true);
        darkPane.setDisable(true);
        darkPane.setVisible(false);
        helpPane.setDisable(true);
        helpPane.setVisible(false);
        changePasswordPane.setVisible(false);
        changePasswordPane.setDisable(true);

        // Coach buttons
        if(!user.getUser().getTeamRole().equals("Head Coach")){
            openCreationPaneButton.setVisible(false);
            openCreationPaneButton.setDisable(true);
            openEditPaneButton.setVisible(false);
            openEditPaneButton.setDisable(true);
            deleteTeamButton.setVisible(false);
            deleteTeamButton.setDisable(true);
        }

        chooseCityBoxCreate.getItems().addAll(cityList);
        chooseAgeGroupCreate.getItems().addAll(ageGroupList);

        chooseCityBox.getItems().addAll(cityList);
        chooseAgeGroup.getItems().addAll(ageGroupList);

        // Fade in
        AppManager.fadeIn(settingsPane,500);
    }

    /**
     * Makes user information editable or saves the changes
     * @param actionEvent edit button pushed
     * @throws SQLException
     * @throws IOException
     */
    public void editAccount(ActionEvent actionEvent) throws SQLException, IOException {
        if (editAccountButton.getText().equals("Edit")) {
            userNameField.setEditable(true);
            emailField.setEditable(true);
            datePicker.setEditable(true);
            changePhotoButton.setVisible(true);
            changePhotoButton.setDisable(false);
            userNameField.getStylesheets().add("/stylesheets/Active.css");
            emailField.getStylesheets().add("/stylesheets/Active.css");
            datePicker.getStylesheets().add("/stylesheets/Active.css");
            editAccountButton.getStylesheets().add("/stylesheets/Active.css");
            editAccountButton.setText("Save");
        }
        else if (validInput()) {
            userNameField.setEditable(false);
            emailField.setEditable(false);
            datePicker.setEditable(false);
            changePhotoButton.setVisible(false);
            changePhotoButton.setDisable(true);
            userNameField.getStylesheets().remove("/stylesheets/Active.css");
            emailField.getStylesheets().remove("/stylesheets/Active.css");
            datePicker.getStylesheets().remove("/stylesheets/Active.css");
            editAccountButton.getStylesheets().remove("/stylesheets/Active.css");
            user.getUser().setName(userNameField.getText());
            user.getUser().setEmail(emailField.getText());
            user.getUser().setBirthday(datePicker.getValue());

            editAccountButton.setText("Edit");
            userNameLabel.setText(user.getUser().getFirstName());
            if (userPhotoFile != null) {
                user.getUser().setProfilePhoto(accountPhoto);
                profileIcon.setImage(new Image(userPhotoFile.toURI().toString()));
            }
            DatabaseManager.updateUser(user, userPhotoFile);
        }
    }

    /**
     * Opens change password pane
     * @param actionEvent change password button pushed
     */
    public void changePassword(ActionEvent actionEvent) {
        darkPane.setVisible(true);
        darkPane.setDisable(false);
        changePasswordPane.setVisible(true);
        changePasswordPane.setDisable(false);
    }

    /**
     * Closes change password pane
     * @param actionEvent cancel or close button pushed
     */
    public void closeChangePasswordPane(ActionEvent actionEvent) {
        darkPane.setVisible(false);
        darkPane.setDisable(true);
        changePasswordPane.setVisible(false);
        changePasswordPane.setDisable(true);
    }

    /**
     * Updates the password of the user
     * @param actionEvent save button bushed
     * @throws SQLException
     */
    public void savePasswordChange(ActionEvent actionEvent) throws SQLException {
        if (validPasswordInput()) {
            DatabaseManager.passwordChange(user.getDatabaseConnection(), user.getUser(), firstPasswordField.getText());
            displayMessage(messagePane,"Password is changed", false);
            closeChangePasswordPane(actionEvent);
        }
    }

    /**
     * Deletes the account and opens login screen
     * @param actionEvent delete account button pushed
     */
    public void deleteAccount(ActionEvent actionEvent) {
    }

    /**
     * Copies the team code
     * @param actionEvent copy button pushed
     */
    public void copyCode(ActionEvent actionEvent) {
        content.putString(String.valueOf(teamCombobox.getValue().getTeamCode()));
        clipboard.setContent(content);
        displayMessage(messagePane,"Code copied",false);
    }

    /**
     * Deletes the team if it is not the only team of the user
     * @param actionEvent delete team button pushed
     */
    public void deleteTeam(ActionEvent actionEvent) {}

    /**
     * Opens file chooser for profile photo and sets the display image
     * @param actionEvent
     */
    public void changePhoto(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Picture Chooser");
        // Sets up the initial directory as user folder when filechooser is opened
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Sets the file type options
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG and JPG files", "*.png","*.jpg","*.jpeg"));

        userPhotoFile = fileChooser.showOpenDialog(null);

        if( userPhotoFile != null)
        {
            // Upload button's text is changed and the display image is changed to the selected image
            accountPhoto.setImage(new Image(userPhotoFile.toURI().toString()));
            changePhotoButton.setText("Change Photo");
        }
    }

    /**
     * Changes the theme to light or dark
     * @param actionEvent dark mode check box selection
     * @throws IOException
     */
    public void changeTheme(ActionEvent actionEvent) throws IOException {
        if (user.isStyleDark()){
            user.setStyleSheet(LIGHT_STYLE_SHEET);
        }
        else {
            user.setStyleSheet(DARK_STYLE_SHEET);
        }
        AppManager.changeScene(getClass().getResource("/views/SettingsScreen.fxml"),actionEvent, user);

    }

    /**
     * Changes the team that is displayed in the screen
     */
    public void teamChange() {
        if (teamCombobox.getValue().getTeamLogo() != null) {
            teamPhoto.setImage(teamCombobox.getValue().getTeamLogo().getImage());
        }
        teamCode.setText("Team code : " + teamCombobox.getValue().getTeamCode());
    }

    /**
     * Opens team creation pane
     * @param actionEvent create team button pushed
     * @throws IOException
     */
    public void createNewTeam(ActionEvent actionEvent) throws IOException {
        createTeamPane.setDisable(false);
        createTeamPane.setVisible(true);
        darkPane.setDisable(false);
        darkPane.setVisible(true);

        teamNameCreateField.setText("");
        abbrevationCreateField.setText("");
        chooseCityBoxCreate.getSelectionModel().clearSelection();
        chooseLeagueBoxCreate.getSelectionModel().clearSelection();
        chooseAgeGroupCreate.getSelectionModel().clearSelection();
        chooseLeagueTeamBoxCreate.getSelectionModel().clearSelection();

        chooseLeagueBoxCreate.setDisable(true);
        chooseLeagueTeamBoxCreate.setDisable(true);
    }

    /**
     * Opens team edit pane
     * @param actionEvent edit team button pushed
     * @throws IOException
     * @throws SQLException
     */
    public void editTeam(ActionEvent actionEvent) throws IOException, SQLException {
        teamNameEditField.setText(teamCombobox.getValue().getTeamName());
        abbrevationEditField.setText(teamCombobox.getValue().getAbbrevation());

        chooseAgeGroup.getSelectionModel().select(teamCombobox.getValue().getAgeGroup());
        chooseCityBox.getSelectionModel().select(teamCombobox.getValue().getCity());

        if (teamCombobox.getValue().getTeamLogo() != null) {
            logoChangeImage.setImage(teamCombobox.getValue().getTeamLogo().getImage());
            uploadTeamLogoButton.setText("Change Photo");
        }

        editTeamPane.setDisable(false);
        editTeamPane.setVisible(true);
        darkPane.setDisable(false);
        darkPane.setVisible(true);

        chooseLeagueTeamBox.getItems().clear();
        chooseLeagueBox.getItems().clear();

        ObservableList<String> leagueList = DatabaseManager.getLeagues(user, chooseCityBox.getValue().toString(), chooseAgeGroup.getValue());
        chooseLeagueBox.getSelectionModel().clearSelection();
        chooseLeagueBox .setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty) ;
                if (empty || item == null) {
                    setText("Choose League");
                } else {
                    setText(item);
                }
            }
        });
        if(leagueList.size() != 0){
            chooseLeagueBox.getItems().addAll(leagueList);
        }

        chooseLeagueBox.getSelectionModel().select(teamCombobox.getValue().getLeagueName());

        ObservableList<String> teamList = DatabaseManager.getLeagueTeams(user, chooseCityBox.getValue().toString(), chooseAgeGroup.getValue(), chooseLeagueBox.getValue().toString());
        chooseLeagueTeamBox.getSelectionModel().clearSelection();
        chooseLeagueTeamBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty) ;
                if (empty || item == null) {
                    setText("Choose Team");
                } else {
                    setText(item);
                }
            }
        });
        if(teamList.size() != 0){
            chooseLeagueTeamBox.getItems().addAll(teamList);
        }
        //chooseLeagueTeamBox.getSelectionModel().select();
        // TODO
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

    /**
     * Saves the changes of team and updates the database
     * @param actionEvent save button pushed
     * @throws SQLException
     * @throws IOException
     */
    public void saveChanges(ActionEvent actionEvent) throws SQLException, IOException {
        if (validEditInput()) {
            teamCombobox.getValue().setTeamName(teamNameEditField.getText());
            teamCombobox.getValue().setCity(chooseCityBox.getValue().toString());
            teamCombobox.getValue().setAbbrevation(abbrevationEditField.getText());
            teamCombobox.getValue().setLeagueName(chooseLeagueBox.getValue().toString());
            if (logoChangeImage != null) {
                teamCombobox.getValue().setTeamLogo(logoChangeImage);
                teamPhoto.setImage(teamCombobox.getValue().getTeamLogo().getImage());
            }
            DatabaseManager.updateTeam(teamCombobox.getValue(), user.getDatabaseConnection(), editTeamLogoFile);
            displayMessage(messagePane,"Changes are saved", false);

            teamCombobox.getItems().clear();
            teamCombobox.getItems().addAll(user.getUserTeams());
            teamCombobox.getSelectionModel().selectFirst();

            closeButtonPushed(actionEvent);
        }
    }

    /**
     * Closes the edit team pane
     * @param actionEvent close or cancel button pushed
     */
    public void closeButtonPushed(ActionEvent actionEvent) {
        editTeamPane.setDisable(true);
        editTeamPane.setVisible(false);
        darkPane.setDisable(true);
        darkPane.setVisible(false);
        editTeamLogoFile = null;
    }

    /**
     * Opens file chooser for editing screen team logo and displays it
     * @param actionEvent change logo button pushed
     */
    public void changeTeamLogo(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Picture Chooser");
        // Sets up the initial directory as user folder when filechooser is opened
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Sets the file type options
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG and JPG files", "*.png","*.jpg","*.jpeg"));

        editTeamLogoFile = fileChooser.showOpenDialog(null);

        if( editTeamLogoFile != null)
        {
            // Upload button's text is changed and the display image is changed to the selected image
            uploadTeamLogoButton.setText("Change Photo");
            logoChangeImage.setImage(new Image(editTeamLogoFile.toURI().toString()));
        }
    }

    /**
     * Opens file chooser for creation screen team logo and displays it
     * @param actionEvent add logo button pushed
     */
    public void createTeamLogo(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Picture Chooser");
        // Sets up the initial directory as user folder when filechooser is opened
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Sets the file type options
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG and JPG files", "*.png","*.jpg","*.jpeg"));

        createTeamLogoFile = fileChooser.showOpenDialog(null);

        if( createTeamLogoFile != null)
        {
            // Upload button's text is changed and the display image is changed to the selected image
            uploadTeamLogoButtonCreate.setText("Change Photo");
            logoChangeImageCreate.setImage(new Image(createTeamLogoFile.toURI().toString()));
        }
    }

    /**
     * Creates the team with given input and saves it to the database
     * @param actionEvent create button pushed
     * @throws IOException
     * @throws SQLException
     */
    public void createTeam(ActionEvent actionEvent) throws IOException, SQLException {
        if (validCreateInput()) {
            user = DatabaseManager.createTeam(user, teamNameCreateField.getText(), abbrevationCreateField.getText(), chooseCityBoxCreate.getValue().toString(),
                    chooseAgeGroupCreate.getValue(), chooseLeagueBoxCreate.getValue().toString(), chooseLeagueTeamBoxCreate.getValue().toString(), createTeamLogoFile);

            createPaneClose(actionEvent);
            displayMessage(messagePane, "Team created", false);
        }
    }

    /**
     * Initialises comboboxes and makes others enabled
     * @param event age and city combobox selection
     * @throws SQLException
     */
    public void onSelectionCreate(ActionEvent event) throws SQLException {
        //TODO think about creating league model class to get id easily
        if(chooseAgeGroupCreate.getValue() != null && chooseCityBoxCreate.getValue() != null){
            chooseLeagueBoxCreate.setDisable(false);
            chooseLeagueTeamBoxCreate.getItems().clear();
            chooseLeagueBoxCreate.getItems().clear();
            ObservableList<String> leagueList = DatabaseManager.getLeagues(user, chooseCityBoxCreate.getValue().toString(), chooseAgeGroupCreate.getValue());
            chooseLeagueBoxCreate.getSelectionModel().clearSelection();
            chooseLeagueBoxCreate .setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (empty || item == null) {
                        setText("Choose League");
                    } else {
                        setText(item);
                    }
                }
            });
            if(leagueList.size() != 0){
                chooseLeagueBoxCreate.getItems().addAll(leagueList);
            }
        }
    }

    /**
     * Creates the team list for combobox and activates it
     * @param actionEvent league combobox selection
     * @throws SQLException
     */
    public void onLeagueSelectionCreate(ActionEvent actionEvent) throws SQLException {
        if(chooseLeagueBoxCreate.getValue() != null){
            chooseLeagueTeamBoxCreate.setDisable(false);
            ObservableList<String> teamList = DatabaseManager.getLeagueTeams(user, chooseCityBoxCreate.getValue().toString(), chooseAgeGroupCreate.getValue().toString(), chooseLeagueBoxCreate.getValue().toString());
            chooseLeagueTeamBoxCreate.getSelectionModel().clearSelection();
            chooseLeagueTeamBoxCreate.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (empty || item == null) {
                        setText("Choose Team");
                    } else {
                        setText(item);
                    }
                }
            });
            if(teamList.size() != 0){
                chooseLeagueTeamBoxCreate.getItems().addAll(teamList);
            }
        }
    }

    /**
     * Closes the create pane
     * @param actionEvent close or cancel button pushed
     */
    public void createPaneClose(ActionEvent actionEvent) {
        createTeamPane.setDisable(true);
        createTeamPane.setVisible(false);
        darkPane.setDisable(true);
        darkPane.setVisible(false);
        logoChangeImageCreate.setImage(new Image("/Resources/Images/emptyTeamLogo.png"));
        createTeamLogoFile = null;
    }

    /**
     * Helps initialising the icons according to the chosen team
     */
    public void darkIcons() {
        teamPhoto.setImage((new Image("/Resources/Images/emptyTeamLogo.png")));
        copyIcon.setImage((new Image("/Resources/Images/white/copy_white.png")));
        helpPaneIcon.setImage((new Image("/Resources/Images/white/help_white.png")));
        if (user.getUser().getProfilePhoto() == null) {
            accountPhoto.setImage((new Image("/Resources/Images/white/big_profile_white.png")));
        }
    }

    /**
     * Helps initialising the icons according to the chosen team
     */
    public void lightIcons() {
        teamPhoto.setImage((new Image("/Resources/Images/emptyTeamLogo.png")));
        copyIcon.setImage((new Image("/Resources/Images/black/copy_black.png")));
        helpPaneIcon.setImage((new Image("/Resources/Images/black/help_black.png")));
        if (user.getUser().getProfilePhoto() == null) {
            accountPhoto.setImage((new Image("/Resources/Images/black/big_profile_black.png")));
        }
    }

    /**
     * Checks account edit input and displays messages accordingly
     * @return true if all the input are valid
     * @throws SQLException
     */
    private boolean validInput() throws SQLException {
        if (userNameField.getText().length() == 0) {
            displayMessage(messagePane, "Name cannot be empty", true);
            return false;
        }
        else if (userNameField.getText().length() == 1) {
            displayMessage(messagePane, "Name cannot be empty", true);
            return false;
        }
        else if (!isAllLetters(userNameField.getText())) {
            displayMessage(messagePane, "Names must be all letters", true);
            return false;
        }
        else if (!(userNameField.getText().contains(" ")) || userNameField.getText().charAt(0) == ' ') {
            displayMessage(messagePane, "Name must consist of two words", true);
            return false;
        }
        else if (!(emailField.getText().contains("@"))) {
            displayMessage(messagePane, "Invalid email", true);
            return false;
        }
        else if (DatabaseManager.isEmailTaken(user.getDatabaseConnection(), emailField.getText()) && !(emailField.getText().equals(user.getUser().getEmail()))){
            displayMessage(messagePane, "Email is used before", true);
            return false;
        }
        else {
            displayMessage(messagePane,"Changes are saved", false);
            return true;
        }
    }

    /**
     * Checks team creation input and displays message
     * @return true if all input are valid
     * @throws SQLException
     */
    private boolean validCreateInput() throws SQLException {
        // Checks if any of the fields is empty
        if(teamNameCreateField.getText().equals("") || abbrevationCreateField.getText().equals("")
                || chooseCityBoxCreate.getValue() == null || chooseLeagueBoxCreate.getValue() == null
                || chooseAgeGroupCreate.getValue() == null || chooseLeagueTeamBoxCreate.getValue() == null){
            displayMessage(messagePane,"Please fill all the fields",true);
            return false;
        }
        // Checks the abbrevation length
        else if(abbrevationCreateField.getText().length() > 3)
        {
            displayMessage(messagePane,"Abbrevations must be at most 3 characters",true);
            return false;
        }
        // Checks team name length
        else if (teamNameCreateField.getText().length() > 30){
            displayMessage(messagePane,"Team Names must be smaller than 30 characters",true);
            return false;
        }
        return true;
    }

    /**
     * Checks team edit input and displays message
     * @return true if all input are valid
     * @throws SQLException
     */
    private boolean validEditInput() throws SQLException {
        // Checks if any of the fields is empty
        if(teamNameEditField.getText().equals("") || abbrevationEditField.getText().equals("")
                || chooseCityBox.getValue() == null || chooseLeagueBox.getValue() == null
                || chooseLeagueTeamBox.getValue() == null) {
            displayMessage(messagePane,"Please fill all the fields",true);
            return false;
        }
        // Checks the abbrevation length
        else if(abbrevationEditField.getText().length() > 3)
        {
            displayMessage(messagePane,"Abbrevations must be at most 3 characters",true);
            return false;
        }
        // Checks team name length
        else if (teamNameEditField.getText().length() > 30){
            displayMessage(messagePane,"Team Names must be smaller than 30 characters",true);
            return false;
        }
        return true;
    }

    /**
     * Checks passwords
     * @return true if the passwords are valid
     * @throws SQLException
     */
    private boolean validPasswordInput() throws SQLException {
        // Checks if the password and the confirmation are the same
        if (!firstPasswordField.getText().equals(secondPasswordField.getText())){
            displayMessage(messagePane, "Passwords do not match", true);
            return false;
        }
        //Checks the password length
        else if(firstPasswordField.getText().length() < 8 || firstPasswordField.getText().length() > 16)
        {
            displayMessage(messagePane, "Passwords must be between 8-16 characters", true);
            return false;
        }
        return true;
    }

    /**
     * Checkes if the given text consists only of letters
     * @param text the given string to check
     * @return true if the text is all letters
     */
    private boolean isAllLetters(String text) {
        for ( int i = 0; i < text.length(); i++){
            if(!(Character.isAlphabetic(text.charAt(i)) || text.charAt(i) == ' '))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    /**
     * Deactivates the settings button on the left pane
     */
    public void toSettingsScreen(ActionEvent actionEvent) throws IOException {}

    /**
     * Sends application
     * @param event apply button pushed
     */
    public void applyButtonPushed(ActionEvent event) throws SQLException {
        String response = DatabaseManager.isTeamCodeProper(user, teamApplicationCodeField.getText());
        System.out.println(response);
        if(response.equals("Success")){
            user = DatabaseManager.updateApplications(user);
            displayMessage(messagePane, "Application sent", false);
        }
        else{
            displayMessage(messagePane,"Invalid code", true);
        }
    }

}
