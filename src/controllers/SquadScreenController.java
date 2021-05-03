package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import models.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;


public class SquadScreenController extends MainTemplateController{

    @FXML
    private ComboBox<String> teamBox;

    @FXML
    private ComboBox<String> memberFilterBox;

    @FXML
    private TableView<TeamMember> squadTable;

    @FXML
    private TableColumn<TeamMember, String> firstNameColumn;

    @FXML
    private TableColumn<TeamMember, String> lastNameColumn;

    @FXML
    private TableColumn<TeamMember, String> birthdayColumn;

    @FXML
    private TableColumn<TeamMember, String> teamRoleColumn;

    @FXML
    private TableColumn<TeamMember, String> emailColumn;

    @FXML
    private TableColumn<TeamMember, Button> detailedViewColumn;

    @FXML
    private Pane disablePane;

    @FXML
    private Pane detailsPane;

    @FXML
    private GridPane lastFiveTrainingsGrid;

    @FXML
    private GridPane trainingAveragesGrid;

    @FXML
    private GridPane averageGameStatsGrid;

    @FXML
    private GridPane lastGameStatsGrid;

    @FXML
    private GridPane squadPane;

    @FXML
    private Label detailsFullNameLabel;

    @FXML
    private Label detailsBirthdayLabel;

    @FXML
    private Label detailsEmailLabel;

    @FXML
    private Label detailsTeamRoleLabel;

    @FXML
    private ImageView playerPhotoView;

    private ObservableList<String> teamNames = FXCollections.observableArrayList();

    private ObservableList<String> memberFilterOptions = FXCollections.observableArrayList("All Members", "Players", "Assistant Coaches", "Head Coaches");

    private String[] footballAverages = {"Gpg","Apg","Spg","YCpg", "RCpg"};
    private String[] footballGameStats = {"Gls", "Ast","Svs","YC","RC"};

    private String[] basketballAverages = {"Ppg","Apg","Rpg","Spg", "Bpg"};
    private String[] basketballGameStats = {"Pts", "Ast","Rbd","Stl","Blk"};

    public void initData(UserSession user){
        super.initData(user);

        if(user.isStyleDark()) {
            try {
                darkIcons();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                lightIcons();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        for(Team team : user.getUserTeams()){
            teamNames.add(team.getTeamName());
        }
        teamBox.getItems().addAll(teamNames);
        memberFilterBox.getItems().addAll(memberFilterOptions);
        teamBox.getSelectionModel().selectFirst();
        memberFilterBox.getSelectionModel().selectFirst();
        updateSquadTable();
        setUpDetailsPane();
        disablePane.setVisible(false);
        detailsPane.setVisible(false);


        for(CalendarEvent calendarEvent : user.getCalendarEvents(user.getUserTeams().get(0))) {
            System.out.println(calendarEvent.getEventTitle() + " " + calendarEvent.getEventDateTime());
        }
        AppManager.fadeIn(squadPane,500);
    }

    private void setUpDetailsPane() {
        //TODO get values from database and show it here
        for(int i = 0; i < 5; i++){
            if(user.getUser().getSportBranch().equals("Basketball")){
                Label averageLabel = new Label(basketballAverages[i]);
                Label statsLabel = new Label(basketballGameStats[i]);
                averageLabel.getStyleClass().add("detailsLabel");
                statsLabel.getStyleClass().add("detailsLabel");
                averageGameStatsGrid.add(averageLabel, i, 0);
                lastGameStatsGrid.add(statsLabel, i, 0);
            }
        }
    }

    public void updateSquadTable() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<TeamMember, String>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<TeamMember, String>("lastName"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<TeamMember, String>("birthdayString"));
        teamRoleColumn.setCellValueFactory(new PropertyValueFactory<TeamMember, String>("teamRole"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<TeamMember, String>("email"));


        for(Team team : user.getUserTeams()){
            if(team.getTeamName().equals(teamBox.getValue().toString())){
                if(memberFilterBox.getValue().toString().equals("All Members")){
                    ObservableList<TeamMember> allMembers = FXCollections.observableArrayList(team.getTeamMembers());
                    squadTable.setItems(allMembers);
                }
                else if(memberFilterBox.getValue().toString().equals("Players")){
                    squadTable.setItems(team.getMembersWithRole("Player"));
                }
                else if(memberFilterBox.getValue().toString().equals("Assistant Coaches")){
                    squadTable.setItems(team.getMembersWithRole("Assistant Coach"));
                }
                else if(memberFilterBox.getValue().toString().equals("Head Coaches")){
                    squadTable.setItems(team.getMembersWithRole("Head Coach"));
                }
            }
        }

        detailedViewColumn.setCellFactory(ButtonTableCell.<TeamMember>forTableColumn("View", (TeamMember p) -> {
            showPane(p);
            return p;
        }));
    }

    private void showPane(TeamMember member) {
        disablePane.setVisible(true);
        detailsPane.setVisible(true);
        if(member.getProfilePhoto() != null){
            playerPhotoView.setImage(member.getProfilePhoto().getImage());
        }
        else{
            if(user.isStyleDark()) {
                try {
                    darkIcons();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    lightIcons();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        detailsFullNameLabel.setText(member.getFullName());
        detailsBirthdayLabel.setText(member.getBirthdayString());
        detailsEmailLabel.setText(member.getEmail());
        detailsTeamRoleLabel.setText(member.getTeamRole());
    }

    public void closeButtonPushed(ActionEvent event) {
        disablePane.setVisible(false);
        detailsPane.setVisible(false);
    }

    private void darkIcons() throws URISyntaxException {
        playerPhotoView.setImage(new Image(getClass().getResource("/Resources/Images/white/big_profile_white.png").toURI().toString()));
    }

    private void lightIcons() throws URISyntaxException {
        playerPhotoView.setImage(new Image(getClass().getResource("/Resources/Images/black/big_profile_black.png").toURI().toString()));
    }
}
