package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseManager {

    public static UserSession login( Connection databaseConnection, String email, String password) throws SQLException {
        TeamMember user = createUser(databaseConnection, email, password);
        if(user == null){
            return null;
        }
        ArrayList<Team> userTeams = createUserTeams(user, databaseConnection);
        HashMap<Team, ObservableList<Team>> standings = createStandings(databaseConnection, userTeams);
        HashMap<Team, ObservableList<Game>> gamesOfTheCurrentRound = createGamesOfTheCurrentRound(databaseConnection, userTeams, standings);
        ArrayList<Notification> notifications = createNotifications(databaseConnection, user,0);
        HashMap<Team, ArrayList<Gameplan>> gameplans = createGameplans(databaseConnection, userTeams);
        ObservableList<Training> trainings = createTrainings(databaseConnection, userTeams);
        ArrayList<TeamApplication> teamApplications = createApplication(databaseConnection, user, userTeams);
        ArrayList<CalendarEvent> calendarEvents = createCalendarEvents(databaseConnection, user, userTeams);
        Date lastSync = new Date();
        return new UserSession(user, userTeams, gamesOfTheCurrentRound, standings, notifications, calendarEvents, trainings, databaseConnection, teamApplications, gameplans, lastSync);
    }

    private static ArrayList<CalendarEvent> createCalendarEvents(Connection databaseConnection, TeamMember user, ArrayList<Team> userTeams) {
        return null;
    }

    private static ObservableList<Training> createTrainings(Connection databaseConnection, ArrayList<Team> userTeams) throws SQLException {
        String[] colorCodes = {"a","b","c","d","e"};
        List<Integer> teamIds = new ArrayList<>();
        for( Team team : userTeams){
            teamIds.add(team.getTeamId());
        }
        ArrayList<Training> trainings = new ArrayList<>();
        PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from trainings where training_date_time < now() " +
                "and team_id in (?) ORDER BY training_date_time desc LIMIT 2");
        prepStmt.setArray(1, databaseConnection.createArrayOf("VARCHAR", teamIds.toArray()));
        ResultSet pastTraininingsResultSet = prepStmt.executeQuery();
        while(pastTraininingsResultSet.next()){
            int trainingId = pastTraininingsResultSet.getInt("training_id");
            String title = pastTraininingsResultSet.getString("title");
            Date  trainingDate = pastTraininingsResultSet.getDate("training_date_time");
            String description = pastTraininingsResultSet.getString("training_description");
            String locationName = pastTraininingsResultSet.getString("location_name");
            String locationLink = pastTraininingsResultSet.getString("location_link");
            String actionLink = "/views/TrainingsScreen.fxml";
            String notes = pastTraininingsResultSet.getString("notes");
            int teamIndex = teamIds.indexOf(pastTraininingsResultSet.getInt("team_id"));
            trainings.add( new Training(trainingId, title, trainingDate, description, actionLink, colorCodes[teamIndex % 5],
                    locationName, locationLink, userTeams.get(teamIndex), notes));
        }
        pastTraininingsResultSet.last();

        PreparedStatement preparedStatement = databaseConnection.prepareStatement("select * from trainings where training_date_time > now() " +
                "and team_id in (?) ORDER BY training_date_time asc LIMIT ?");
        preparedStatement.setArray(1, databaseConnection.createArrayOf("VARCHAR", teamIds.toArray()));
        preparedStatement.setInt(2, 8 - pastTraininingsResultSet.getRow());
        ResultSet futureTrainingsResultSet = preparedStatement.executeQuery();
        while(futureTrainingsResultSet.next()){
            int trainingId = futureTrainingsResultSet.getInt("training_id");
            String title = futureTrainingsResultSet.getString("title");
            Date  trainingDate = futureTrainingsResultSet.getDate("training_date_time");
            String description = futureTrainingsResultSet.getString("training_description");
            String locationName = futureTrainingsResultSet.getString("location_name");
            String locationLink = futureTrainingsResultSet.getString("location_link");
            String actionLink = "/views/TrainingsScreen.fxml";
            String notes = futureTrainingsResultSet.getString("notes");
            int teamIndex = teamIds.indexOf(futureTrainingsResultSet.getInt("team_id"));
            trainings.add(0, new Training(trainingId, title, trainingDate, description, actionLink, colorCodes[teamIndex % 5],
                    locationName, locationLink, userTeams.get(teamIndex), notes));
        }
        return FXCollections.observableArrayList(trainings);
    }


    private static ArrayList<TeamApplication> createApplication(Connection databaseConnection, TeamMember user, ArrayList<Team> userTeams) throws SQLException {
        ArrayList<TeamApplication> teamApplications = new ArrayList<>();
        if(user.getTeamRole().equals("Head Coach")){
            for(Team team : userTeams){
                PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from team_applications join  team_members tm on team_applications.applicant_id = tm.member_id and team_id = ? and isDeclined = false");
                prepStmt.setInt(1, team.getTeamId());
                ResultSet resultSet = prepStmt.executeQuery();
                while(resultSet.next()){
                    int applicationId = resultSet.getInt("id");
                    int memberId = resultSet.getInt("member_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String email = resultSet.getString("email");
                    Date birthday = resultSet.getDate("birthday");
                    String teamRole = resultSet.getString("team_role");
                    //TODO get Image
                    Image profilePicture;
                    byte[] photoBytes = resultSet.getBytes("photo");
                    if(photoBytes != null)
                    {
                        InputStream imageFile = resultSet.getBinaryStream("photo");
                        profilePicture = new Image(imageFile);
                    }
                    else{
                        profilePicture = null;
                    }
                    TeamMember applicant = new TeamMember(memberId, firstName, lastName, birthday, teamRole, email, user.getSportBranch(), profilePicture);
                    teamApplications.add(new TeamApplication(applicationId, applicant, team, false));
                }
            }
        }
        else{
            PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from team_applications join teams t on team_applications.team_id = t.team_id and applicant_id = ?");
            prepStmt.setInt(1, user.getMemberId());
            ResultSet resultSet = prepStmt.executeQuery();
            while(resultSet.next()){
                    int applicationId = resultSet.getInt("id");
                    boolean isDeclined = resultSet.getBoolean("isDeclined");
                    int teamId = resultSet.getInt("t.team_id");
                    String teamName = resultSet.getString("team_name");
                    String city = resultSet.getString("city");
                    String ageGroup = resultSet.getString("age_group");
                    Image teamLogo;
                    byte[] photoBytes = resultSet.getBytes("team_logo");
                    if(photoBytes != null)
                    {
                        InputStream imageFile = resultSet.getBinaryStream("team_logo");
                        teamLogo = new Image(imageFile);
                    }
                    else{
                        teamLogo = null;
                    }
                    Team appliedTeam = new Team(teamId, teamName, city, ageGroup, teamLogo);
                    teamApplications.add(new TeamApplication(applicationId, user, appliedTeam , isDeclined));
            }
        }
        return teamApplications;
    }

    private static HashMap<Team, ArrayList<Gameplan>>  createGameplans(Connection databaseConnection, ArrayList<Team> userTeams) throws SQLException {
        HashMap<Team, ArrayList<Gameplan>> teamGameplans = new HashMap<>();
        for( Team team : userTeams){
            ArrayList<Gameplan> gameplans = new ArrayList<>();
            PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from gameplans join team_and_gameplans tg on gameplans.gameplan_id = tg.gameplan_id and team_id = ?");
            prepStmt.setInt(1, team.getTeamId());
            ResultSet resultSet = prepStmt.executeQuery();
            while(resultSet.next()){
                int gameplanId = resultSet.getInt("tg.gameplan_id");
                String title = resultSet.getString("title");
                int version = resultSet.getInt("version");
                //TODO get pdf
                gameplans.add(new Gameplan(gameplanId, title, team, version ));
            }
            teamGameplans.put(team, gameplans);
        }
        return teamGameplans;
    }

    public static ArrayList<Notification> createNotifications(Connection databaseConnection, TeamMember user, int pageNumber) throws SQLException {
        ArrayList<Notification> notifications = new ArrayList<>();
        PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from notifications join team_members tm on tm.member_id = notifications.sender_id and recipent_id = ? LIMIT ?,8");
        prepStmt.setInt(1, user.getMemberId());
        prepStmt.setInt(1,pageNumber * 8);
        ResultSet resultSet = prepStmt.executeQuery();
        while (resultSet.next()){
            int id = resultSet.getInt("id");
            int senderId = resultSet.getInt("sender_id");
            String senderFirstName = resultSet.getString("first_name");
            String senderLastName = resultSet.getString("last_name");
            String title = resultSet.getString("title");
            String message = resultSet.getString("message");
            boolean isUnread = resultSet.getBoolean("is_unread");
            Date timeSent = resultSet.getDate("time_sent");
            String clickAction = resultSet.getString("click_action");
            Notification notification = new Notification(id, title, new TeamMember(senderId, senderFirstName, senderLastName), user ,clickAction, timeSent, isUnread, null);
            notifications.add(notification);
        }
        return notifications;
    }

    public static HashMap<Team, ObservableList<Team>> createStandings(Connection databaseConnection, ArrayList<Team> userTeams ) throws SQLException {
        HashMap<Team, ObservableList<Team>> standings = new HashMap<>();
        for( Team team: userTeams){
            ArrayList<Team> teams = new ArrayList<>();
            PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from league_teams join team_performances tp on league_teams.league_id = tp.league_id and tp.league_team_id = league_teams.league_team_id and tp.league_id = ? order by points desc");
            prepStmt.setInt(1, team.getLeagueId());
            ResultSet resultSet = prepStmt.executeQuery();
            int placement = 1;
            while (resultSet.next()){
                int teamId = resultSet.getInt("tp.league_id");
                int id = resultSet.getInt("id");
                String teamName = resultSet.getString("team_name");
                String abbrevation = resultSet.getString("abbrevation");
                int gamesPlayed = resultSet.getInt("games_played");
                int gamesWon = resultSet.getInt("games_won");
                int gamesDrawn = resultSet.getInt("games_drawn");
                int gamesLost = resultSet.getInt("games_lost");
                int points = resultSet.getInt("points");
                TeamStats teamStats = new TeamStats(id, gamesPlayed, gamesWon, gamesLost, gamesDrawn, placement, points);
                Team leagueTeam = new Team(teamId, teamName, abbrevation, teamStats);
                placement++;
                teams.add(leagueTeam);
            }
            standings.put(team, FXCollections.observableArrayList(teams));
        }
        return standings;
    }

    private static HashMap<Team, ObservableList<Game>> createGamesOfTheCurrentRound(Connection databaseConnection, ArrayList<Team> userTeams, HashMap<Team, ObservableList<Team>> standings) throws SQLException {
        HashMap<Team, ObservableList<Game>> gamesOfTheCurrentRound = new HashMap<>();
        for (Team team : userTeams){
            ArrayList<Game> games = new ArrayList<>();
            PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from league_games join leagues l " +
                    "on league_games.league_id = l.league_id and league_games.round_no = l.current_round and l.league_id = ? " +
                    "join league_teams lt on lt.league_team_id = league_games.away_team_id join league_teams t " +
                    "on t.league_team_id = league_games.home_team_id");
            prepStmt.setInt(1, team.getLeagueId());
            ResultSet gamesResultSet = prepStmt.executeQuery();
            while (gamesResultSet.next()){
                int gameId = gamesResultSet.getInt("game_id");
                Date gameDate = gamesResultSet.getDate("game_date_time");
                int roundNo = gamesResultSet.getInt("round_no");
                String gameLocationName = gamesResultSet.getString("game_location_name");
                String gameLocationLink = gamesResultSet.getString("game_location_link");
                String result = gamesResultSet.getString("final_score");
                int homeTeamId = gamesResultSet.getInt("home_team_id");
                int awayTeamId = gamesResultSet.getInt("away_team_id");
                Team homeTeam = null;
                Team awayTeam = null;
                for (Team leagueTeam : standings.get(team)){
                    if(leagueTeam.getTeamId() == homeTeamId){
                        homeTeam = leagueTeam;
                    }
                    if(leagueTeam.getTeamId() == awayTeamId){
                        awayTeam = leagueTeam;
                    }
                }
                Game game = new Game(gameId, "Game", gameDate, "","/views/LeagueScreen.fxml","COLORCODE", roundNo, homeTeam, awayTeam, gameLocationName, gameLocationName, result);
                games.add(game);
            }
            gamesOfTheCurrentRound.put(team, FXCollections.observableArrayList(games));
        }
        return null;
    }

    private static  TeamMember createUser(Connection databaseConnection, String email, String password) throws SQLException { //TODO If player
        PreparedStatement prepStmt = databaseConnection.prepareStatement("SELECT * FROM team_members " +
                " where password = MD5(?) AND email = ?");

        prepStmt.setString(1,password);
        prepStmt.setString(2,email);

        ResultSet resultSet = prepStmt.executeQuery();
        if(resultSet.next()){
            int memberId = resultSet.getInt("member_id");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            Date birthday = resultSet.getDate("birthday");
            String teamRole = resultSet.getString("team_role");
            String sportBranch = resultSet.getString("sport_branch");
            Image profilePicture;
            byte[] photoBytes = resultSet.getBytes("photo");
            if(photoBytes != null)
            {
                InputStream imageFile = resultSet.getBinaryStream("photo");
                profilePicture = new Image(imageFile);
            }
            else{
                profilePicture = null;
            }
            if( teamRole.equals("Player")){
                boolean isCaptain = false; //TODO set it afterwards
                String position = resultSet.getString("position");
                ArrayList<Injury> playerInjuries = getPlayerInjuries(databaseConnection, memberId);
                PlayerStats playerStats = null;
                if(sportBranch.equals("Basketball")){
                    playerStats = getBasketballStats(databaseConnection, memberId);
                }
                else if (sportBranch.equals("Football")){
                    playerStats = getFootballStats(databaseConnection, memberId);
                }
                return new Player(memberId, firstName, lastName, birthday, teamRole, email, sportBranch, profilePicture, position, playerInjuries, playerStats, isCaptain);
            }
            else{
                return new TeamMember(memberId, firstName, lastName, birthday, teamRole, email, sportBranch, profilePicture);
            }
        }
        else{
            return null;
        }
    }

    private static ArrayList<Team> createUserTeams(TeamMember user, Connection databaseConnection) throws SQLException {
        ArrayList<Team> userTeams = new ArrayList<>();
        PreparedStatement prepStmt = databaseConnection.prepareStatement("select * from team_and_members JOIN team_members tm " +
                "on tm.member_id = team_and_members.team_member_id and tm.member_id = ? " +
                "join teams t on t.team_id = team_and_members.team_id;");
        prepStmt.setInt(1, user.getMemberId());
        ResultSet teamsResultSet = prepStmt.executeQuery();
        while(teamsResultSet.next()){
            ArrayList<TeamMember> teamMembers = new ArrayList<>();
            int teamId = teamsResultSet.getInt("t.team_id");
            int databaseTeamId = teamsResultSet.getInt("database_team_id");
            int leagueId = teamsResultSet.getInt("league_id");
            String teamName = teamsResultSet.getString("team_name");
            String abbrevation = teamsResultSet.getString("abbrevation");
            String city = teamsResultSet.getString("city");
            String ageGroup = teamsResultSet.getString("age_group");
            int teamCode = teamsResultSet.getInt("team_code");
            int captainId = teamsResultSet.getInt("captain_id");
            Image teamLogo;
            byte[] photoBytes = teamsResultSet.getBytes("team_logo");
            if(photoBytes != null)
            {
                InputStream imageFile = teamsResultSet.getBinaryStream("team_logo");
                teamLogo = new Image(imageFile);
            }
            else{
                teamLogo = null;
            }
            prepStmt = databaseConnection.prepareStatement("select * from team_and_members join team_members tm on tm.member_id = team_and_members.team_member_id and team_id = ?");
            prepStmt.setInt(1, teamId);
            ResultSet membersResultSet = prepStmt.executeQuery();
            while(membersResultSet.next()){
                int memberId = membersResultSet.getInt("member_id");
                String firstName = membersResultSet.getString("first_name");
                String lastName = membersResultSet.getString("last_name");
                String email = membersResultSet.getString("email");
                Date birthday = membersResultSet.getDate("birthday");
                String teamRole = membersResultSet.getString("team_role");
                String sportBranch = membersResultSet.getString("sport_branch");
                Image profilePicture;
                byte[] profilePhotoBytes = membersResultSet.getBytes("photo");
                if(profilePhotoBytes != null)
                {
                    InputStream imageFile = membersResultSet.getBinaryStream("photo");
                    profilePicture = new Image(imageFile);
                }
                else{
                    profilePicture = null;
                }
                if( teamRole.equals("Player")){
                    boolean isCaptain = captainId == memberId;
                    String position = membersResultSet.getString("position");
                    ArrayList<Injury> playerInjuries = getPlayerInjuries(databaseConnection, memberId);
                    PlayerStats playerStats = null;
                    if(sportBranch.equals("Basketball")){
                        playerStats = getBasketballStats(databaseConnection, memberId);
                    }
                    else if (sportBranch.equals("Football")){
                        playerStats = getFootballStats(databaseConnection, memberId);
                    }
                    teamMembers.add(new Player(memberId, firstName, lastName, birthday, teamRole, email, sportBranch, profilePicture, position, playerInjuries, playerStats, isCaptain));
                }
                else{
                    teamMembers.add(new TeamMember(memberId, firstName, lastName, birthday, teamRole, email, sportBranch, profilePicture));
                }
            }
            prepStmt = databaseConnection.prepareStatement("select title from leagues where league_id = ?");
            prepStmt.setInt(1, leagueId);
            ResultSet leagueResultSet = prepStmt.executeQuery();
            String leagueName = "";
            if(leagueResultSet.next()){
                leagueName = leagueResultSet.getString(1);
            }
            TeamStats teamStats = getTeamStats( databaseConnection, teamId);
            userTeams.add( new Team(teamId, databaseTeamId, leagueId, teamName, abbrevation, teamCode, leagueName, city, ageGroup, teamLogo, teamStats, teamMembers));
        }
        return userTeams;
    }

    //TODO make this work
    private static TeamStats getTeamStats(Connection databaseConnection, int teamId) {
        return null;
    }

    //TODO make this Work
    private static PlayerStats getFootballStats(Connection databaseConnection, int memberId) {
        return null;
    }

    //TODO make this Work
    private static PlayerStats getBasketballStats(Connection databaseConnection, int memberId) {
        return null;
    }

    //TODO make this work
    private static ArrayList<Injury> getPlayerInjuries(Connection databaseConnection, int memberId) {
        return null;
    }

    private String formatDateToMYSQL(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

}
