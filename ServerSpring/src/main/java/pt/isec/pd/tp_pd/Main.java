package pt.isec.pd.tp_pd;

import pt.isec.pd.tp_pd.database.DatabaseManager;

import java.sql.SQLException;

public class Main {
	private static DatabaseManager manager;

	public static void main(String[] args) throws SQLException {
		manager = manager.getInstance(null);

//		manager.insertUser(
//				"client@test",
//				"1234",
//				"participant",
//				2021133564
//		);
//
//		manager.insertUser(
//				"client2@test",
//				"1234",
//				"participant",
//				2021133565
//		);
//
//		manager.insertUser(
//				"admin@test",
//				"1234",
//				"admin",
//				666
//		);
	}
}